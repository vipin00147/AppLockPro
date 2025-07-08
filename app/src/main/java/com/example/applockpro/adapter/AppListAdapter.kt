package com.example.applockpro.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.example.applockpro.base.BaseActivity
import com.example.applockpro.databinding.ItemAppBinding
import com.example.applockpro.loadImageDrawableWithGlide
import com.example.applockpro.model.AppInfo

class AppListAdapter(
    val baseActivity: BaseActivity<ViewDataBinding>,
    val onCheckChange : (Boolean, Int, AppInfo) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var appsList = ArrayList<AppInfo>()

    inner class ItemViewHolder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val binding = ItemAppBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is ItemViewHolder ) {
            with(holder.binding) {

                ivAppImage.loadImageDrawableWithGlide(appIcon = appsList[holder.adapterPosition].appIcon, baseActivity = baseActivity)
                tvAppName.text = appsList[holder.adapterPosition].appName

                btnSwitch.isChecked = appsList[holder.adapterPosition].isLocked

                btnSwitch.setOnCheckedChangeListener { _, isChecked ->
                    appsList[holder.adapterPosition].isLocked = isChecked
                    onCheckChange.invoke(isChecked, holder.adapterPosition, appsList[holder.adapterPosition])
                }
            }
        }
    }

    fun updateList(items: List<AppInfo>) {
        appsList.clear()
        appsList.addAll(items)
        notifyItemRangeChanged(0, appsList.size) // Make sure this line is called
    }

    override fun getItemCount(): Int {
        return appsList.size
    }

    fun getAppsList() = appsList
    fun lockUnlockApp(locked: Boolean, appData: AppInfo) {
        appsList.indices.find {appsList[it].packageName ==  appData.packageName }?.apply {
            appsList[this].isLocked = locked
            notifyItemChanged(this)
        }
    }
}