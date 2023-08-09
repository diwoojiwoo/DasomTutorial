package com.onethefull.dasomtutorial.utils

import android.content.Context
import com.onethefull.dasomtutorial.MainActivity
import com.onethefull.dasomtutorial.data.api.ApiHelper
import com.onethefull.dasomtutorial.repository.GuideRepository
import com.onethefull.dasomtutorial.repository.LearnRepository
import com.onethefull.dasomtutorial.repository.MealRepository
import com.onethefull.dasomtutorial.repository.vital.ChatRepository
import com.onethefull.dasomtutorial.ui.guide.GuideViewModelFactory
import com.onethefull.dasomtutorial.ui.learn.LearnViewModelFactory
import com.onethefull.dasomtutorial.ui.meal.MealViewModelFactory
import com.onethefull.dasomtutorial.ui.vital.ChatViewModelFactory

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

    private fun getMealRepository(context: Context): MealRepository {
        return MealRepository.getInstance(context.applicationContext)
    }

    fun provideMealViewModelFactory(
        context: Context
    ): MealViewModelFactory {
        return MealViewModelFactory(context as MainActivity, getMealRepository(context))
    }

    fun provideChatViewModelFactory(
        context: Context
    ): ChatViewModelFactory {
        return ChatViewModelFactory(context as MainActivity, getChatRepository(context))
    }

    private fun getChatRepository(context: Context): ChatRepository {
        return ChatRepository.getInstance(context.applicationContext)
    }
}