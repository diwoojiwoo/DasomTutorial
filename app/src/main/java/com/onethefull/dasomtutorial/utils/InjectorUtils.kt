package com.onethefull.dasomtutorial.utils

import android.content.Context
import com.onethefull.dasomtutorial.MainActivity
import com.onethefull.dasomtutorial.data.api.ApiHelper
import com.onethefull.dasomtutorial.repository.GuideRepository
import com.onethefull.dasomtutorial.repository.LearnRepository
import com.onethefull.dasomtutorial.ui.guide.GuideViewModelFactory
import com.onethefull.dasomtutorial.ui.learn.LearnViewModelFactory

/**
 * Created by sjw on 2021/11/10
 */
object InjectorUtils {
    private fun getLearnRepository(context: Context): LearnRepository {
        return LearnRepository.getInstance(context.applicationContext)
    }

    fun provideLearnViewModelFactory(
        context: Context
    ): LearnViewModelFactory {
        return LearnViewModelFactory(context as MainActivity, getLearnRepository(context))
    }

    private fun getGuideRepository(context: Context): GuideRepository {
        return GuideRepository.getInstance(context.applicationContext)
    }

    fun provideGuideViewModelFactory(
        context: Context
    ): GuideViewModelFactory {
        return GuideViewModelFactory(context as MainActivity, getGuideRepository(context))
    }
}