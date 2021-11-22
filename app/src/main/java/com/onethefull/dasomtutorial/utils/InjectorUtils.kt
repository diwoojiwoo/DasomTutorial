package com.onethefull.dasomtutorial.utils

import android.content.Context
import com.onethefull.dasomtutorial.MainActivity
import com.onethefull.dasomtutorial.data.api.ApiHelper
import com.onethefull.dasomtutorial.repository.LearnRepository
import com.onethefull.dasomtutorial.ui.learn.LearnViewModelFactory

/**
 * Created by sjw on 2021/11/10
 */
object InjectorUtils {
    private fun getLearnRepository(context: Context): LearnRepository {
        return LearnRepository.getInstance(context.applicationContext)
    }

    fun provideLearnViewModelFactory(
        context: Context,
        apiHelper: ApiHelper,
    ): LearnViewModelFactory {
        return LearnViewModelFactory(context as MainActivity, getLearnRepository(context), apiHelper)
    }
}