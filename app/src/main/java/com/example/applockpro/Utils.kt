package com.example.applockpro

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.applockpro.base.BaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun Fragment.changeFragment(
    containerId: Int,
    requireActivity: BaseActivity<ViewDataBinding>,
    isAddToBakStack: Boolean,
) {
    CoroutineScope(Dispatchers.Main.immediate).launch {
        val transaction = requireActivity.supportFragmentManager.beginTransaction()

        transaction.replace(containerId, this@changeFragment)

        if (isAddToBakStack) {
            transaction.addToBackStack(this::class.java.simpleName)
        }
        transaction.commitAllowingStateLoss()
    }
}

fun AppCompatImageView.loadImageDrawableWithGlide(appIcon: Drawable, baseActivity: BaseActivity<ViewDataBinding>) {
    Glide.with(baseActivity).load(appIcon).into(this)

}

fun View.hideView() {
    this.visibility = View.GONE
}

fun View.showView() {
    this.visibility = View.VISIBLE
}