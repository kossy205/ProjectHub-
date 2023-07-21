package com.ProjectHub.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.ProjectHub.R
import com.ProjectHub.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {

    private var binding: ActivityIntroBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val typeface: Typeface =
            Typeface.createFromAsset(assets, "carbon bl.ttf")
        binding?.tvAppNameIntro?.typeface = typeface

        binding?.btnSignInIntro?.setOnClickListener {

            startActivity(Intent(this@IntroActivity, SignInActivity::class.java))
        }

        binding?.btnSignUpIntro?.setOnClickListener {
            startActivity(Intent(this@IntroActivity, SignUpActivity::class.java))
        }
    }
}