package com.example.applockpro.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.applockpro.R
import com.example.applockpro.base.BaseActivity
import com.example.applockpro.changeFragment
import com.example.applockpro.databinding.ActivityMainBinding
import com.example.applockpro.ui.fragments.HomeFragment

val HOME_FRAME = R.id.frame_home
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun createBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        HomeFragment().changeFragment(HOME_FRAME, getBaseActivity(), false)

    }

}