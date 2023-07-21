package com.ProjectHub.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ProjectHub.R
import com.ProjectHub.databinding.ActivityCreateBoardBinding
import com.ProjectHub.firebase.FirestoreClass
import com.ProjectHub.model.Board
import com.ProjectHub.utils.Constants
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    private var mSelectedImageFileUri: Uri? = null
    private lateinit var mUserName: String
    private var mBoardImageURL: String = ""
    private var binding: ActivityCreateBoardBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        if (intent.hasExtra(Constants.NAME)) {
            mUserName = intent.getStringExtra(Constants.NAME)!!
        }

        binding?.ivBoardImage?.setOnClickListener { view ->

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this@CreateBoardActivity)
            } else {
                /*Requests permissions to be granted to this application. These permissions
                 must be requested in the manifest, they should not be granted to the app,
                 and they should have protection level*/
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        binding?.btnCreate?.setOnClickListener {
                if (mSelectedImageFileUri != null) {

                    uploadBoardImage()
                } else {

                    showProgressDialog(resources.getString(R.string.please_wait))
                    createBoard()
                }
        }
    }


    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            //If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this@CreateBoardActivity)
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(
                        this,
                        "Oops, you just denied the permission for storage. You can also allow it from settings.",
                        Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
                && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
                && data!!.data != null
        ) {
            mSelectedImageFileUri = data.data

            try {
                // Load the board image in the ImageView.
                Glide
                        .with(this@CreateBoardActivity)
                        .load(Uri.parse(mSelectedImageFileUri.toString()))
                        .centerCrop()
                        .placeholder(R.drawable.ic_user_place_holder)
                        .into(binding?.ivBoardImage!!)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    private fun setupActionBar() {

        setSupportActionBar(binding?.toolbarCreateBoardActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        binding?.toolbarCreateBoardActivity?.setNavigationOnClickListener { onBackPressed() }
    }


    private fun uploadBoardImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(
                "BOARD_IMAGE" + System.currentTimeMillis() + "."
                        + Constants.getFileExtension(this@CreateBoardActivity, mSelectedImageFileUri)
        )
        sRef.putFile(mSelectedImageFileUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    // The image upload is success
                    Log.e(
                            "Firebase Image URL",
                            taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                    )
                    taskSnapshot.metadata!!.reference!!.downloadUrl
                            .addOnSuccessListener { uri ->
                                Log.e("Downloadable Image URL", uri.toString())
                                mBoardImageURL = uri.toString()
                                createBoard()
                            }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                            this@CreateBoardActivity,
                            exception.message,
                            Toast.LENGTH_LONG
                    ).show()

                    hideProgressDialog()
                }
    }



    private fun createBoard() {

        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserID())
        val board = Board(
            binding?.etBoardName?.text.toString(),
                mBoardImageURL,
                mUserName,
                assignedUsersArrayList
        )

        FirestoreClass().createBoard(this@CreateBoardActivity, board)
    }


    fun boardCreatedSuccessfully() {

        hideProgressDialog()

        setResult(Activity.RESULT_OK)
        finish()
    }
}