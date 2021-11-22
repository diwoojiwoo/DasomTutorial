package com.onethefull.dasomtutorial.ui.learn

import android.app.Activity
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onethefull.dasomtutorial.data.api.ApiHelper
import com.onethefull.dasomtutorial.repository.LearnRepository


class LearnViewModelFactory(
    private val context: Activity,
    private val repository: LearnRepository,
    private val apiHelper: ApiHelper,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(LearnViewModel::class.java)) {
            LearnViewModel(context, repository, apiHelper) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}