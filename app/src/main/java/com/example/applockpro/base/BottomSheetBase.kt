package com.example.applockpro.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BottomSheetBase<T : ViewDataBinding> : BottomSheetDialogFragment() {

    private var binding: T? = null
    private var baseActivity: BaseActivity<ViewDataBinding>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(binding == null) {
            binding = onCreateBinding(inflater, container, savedInstanceState)
        }

        return binding?.root
    }

    abstract fun onCreateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): T

    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseActivity = context as BaseActivity<ViewDataBinding>
    }

    fun getBinding() = binding

    fun getBaseActivity() : BaseActivity<ViewDataBinding> {
        return baseActivity!!
    }

}