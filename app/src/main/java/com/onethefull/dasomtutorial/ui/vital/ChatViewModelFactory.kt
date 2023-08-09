package com.onethefull.dasomtutorial.ui.vital

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onethefull.dasomtutorial.repository.vital.ChatRepository


class ChatViewModelFactory(
    private val context: Activity,
    private val repository: ChatRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            ChatViewModel(context, repository) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}