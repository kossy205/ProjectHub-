package com.ProjectHub.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.ProjectHub.R
import com.ProjectHub.adapters.BoardItemsAdapter
import com.ProjectHub.databinding.ActivityMainBinding
import com.ProjectHub.firebase.FirestoreClass
import com.ProjectHub.model.Board
import com.ProjectHub.model.User
import com.ProjectHub.utils.Constants
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.iid.FirebaseInstanceId


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var mUserName: String

    private lateinit var mSharedPreferences: SharedPreferences

    private var binding: ActivityMainBinding? = null
    private var fabCreateBoard: FloatingActionButton? = null
    private var toolbarMainActivity: androidx.appcompat.widget.Toolbar? = null
    private var rvBoardsList: RecyclerView? = null
    private var tvNoBoardsAvailable: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        fabCreateBoard = findViewById(R.id.fab_create_board)
        toolbarMainActivity = findViewById(R.id.toolbar_main_activity)
        rvBoardsList = findViewById(R.id.rv_boards_list)
        tvNoBoardsAvailable = findViewById(R.id.tv_no_boards_available)

        mUserName = ""


        setupActionBar()
        binding?.navView?.setNavigationItemSelectedListener(this)

        mSharedPreferences = this.getSharedPreferences(Constants.PROGEMANAG_PREFERENCES, Context.MODE_PRIVATE)

        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        if (tokenUpdated) {

            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().loadUserData(this@MainActivity, true)
        } else {
            FirebaseInstanceId.getInstance()
                .instanceId.addOnSuccessListener(this@MainActivity) { instanceIdResult ->
                    updateFCMToken(instanceIdResult.token)
                }
        }

        fabCreateBoard?.setOnClickListener {
            val intent = Intent(this@MainActivity, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
    }

    override fun onBackPressed() {
        if (binding?.drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        } else {

            doubleBackToExit()
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.nav_my_profile -> {

                startActivityForResult(
                    Intent(this@MainActivity, MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE
                )
            }

            R.id.nav_sign_out -> {

                FirebaseAuth.getInstance().signOut()

                mSharedPreferences.edit().clear().apply()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK
            && requestCode == MY_PROFILE_REQUEST_CODE
        ) {
            FirestoreClass().loadUserData(this@MainActivity)
        } else if (resultCode == Activity.RESULT_OK
            && requestCode == CREATE_BOARD_REQUEST_CODE
        ) {
            FirestoreClass().getBoardsList(this@MainActivity)
        } else {
            Log.e("Cancelled", "Cancelled")
        }
    }


    private fun setupActionBar() {

        setSupportActionBar(toolbarMainActivity)
        toolbarMainActivity?.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbarMainActivity?.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {

        if (binding?.drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        } else {
            binding?.drawerLayout?.openDrawer(GravityCompat.START)
        }
    }


    fun updateNavigationUserDetails(user: User, readBoardsList: Boolean) {

        hideProgressDialog()

        mUserName = user.name

        val headerView = binding?.navView?.getHeaderView(0)

        val navUserImage = headerView?.findViewById<ImageView>(R.id.iv_user_image)

        Glide
            .with(this@MainActivity)
            .load(user.image) // URL of the image
            .centerCrop() // Scale type of the image.
            .placeholder(R.drawable.ic_user_place_holder) // A default place holder
            .into(navUserImage!!)

        val navUsername = headerView?.findViewById<TextView>(R.id.tv_username)
        navUsername?.text = user.name

        if (readBoardsList) {

            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this@MainActivity)
        }
    }


    private fun updateFCMToken(token: String) {

        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateUserProfileData(this@MainActivity, userHashMap)
    }


    fun populateBoardsListToUI(boardsList: ArrayList<Board>) {

        hideProgressDialog()

        if (boardsList.size > 0) {

            rvBoardsList?.visibility = View.VISIBLE
            tvNoBoardsAvailable?.visibility = View.GONE

            rvBoardsList?.layoutManager = LinearLayoutManager(this@MainActivity)
            rvBoardsList?.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this@MainActivity, boardsList)
            rvBoardsList?.adapter = adapter

            adapter.setOnClickListener(object :
                BoardItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }
            })
        } else {
            rvBoardsList?.visibility = View.GONE
            tvNoBoardsAvailable?.visibility = View.VISIBLE
        }
    }


    fun tokenUpdateSuccess() {

        hideProgressDialog()

        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().loadUserData(this@MainActivity, true)
    }


    companion object {

        const val MY_PROFILE_REQUEST_CODE: Int = 11
        const val CREATE_BOARD_REQUEST_CODE: Int = 12
    }
}