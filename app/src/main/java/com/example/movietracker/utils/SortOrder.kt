package com.example.movietracker.utils

import androidx.annotation.StringRes
import com.example.movietracker.R

enum class SortOrder(val sqlRepresentation: String, @StringRes val displayNameId: Int) {
    DATE_ASC("addedDate ASC", R.string.sort_date_asc),
    DATE_DESC("addedDate DESC", R.string.sort_date_desc),
    TITLE_ASC("title ASC", R.string.sort_title_asc),
    TITLE_DESC("title DESC", R.string.sort_title_desc)
}