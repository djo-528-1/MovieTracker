package com.example.movietracker.utils

import android.content.Context
import android.content.SharedPreferences

class AppSettingsManager(context: Context)
{
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

    companion object{
        private const val KEY_LAST_TAB = "last_opened_tab"
        private const val KEY_SORT_ORDER = "sort_order"
    }

    fun getLastTab(): AppTab
    {
        val tabName = sharedPreferences.getString(KEY_LAST_TAB, AppTab.WATCHING.name)
        return AppTab.valueOf(tabName ?: AppTab.WATCHING.name)
    }

    fun getSortOrder(): SortOrder
    {
        val orderName = sharedPreferences.getString(KEY_SORT_ORDER, SortOrder.DATE_DESC.name)
        return SortOrder.valueOf(orderName ?: SortOrder.DATE_DESC.name)
    }

    fun setLastTab(lastTab: AppTab)
    {
        sharedPreferences.edit().putString(KEY_LAST_TAB, lastTab.name).apply()
    }

    fun setSortOrder(sortOrder: SortOrder)
    {
        sharedPreferences.edit().putString(KEY_SORT_ORDER, sortOrder.name).apply()
    }
}