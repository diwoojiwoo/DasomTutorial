package com.onethefull.dasomtutorial.ui.meal

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.onethefull.dasomtutorial.repository.MealRepository

/**
 * Created by jeaseok on 2022/02/22
 */
class MealViewModelFactory(
    private val context: Activity,
    private val repository: MealRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MealViewModel::class.java)) {
            MealViewModel(context, repository) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}