package com.onethefull.dasomtutorial.ui.guide

import android.app.Activity
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onethefull.dasomtutorial.data.api.ApiHelper
import com.onethefull.dasomtutorial.repository.GuideRepository
import com.onethefull.dasomtutorial.repository.LearnRepository


class GuideViewModelFactory(
    private val context: Activity,
    private val repository: GuideRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(GuideViewModel::class.java)) {
            GuideViewModel(context, repository) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}