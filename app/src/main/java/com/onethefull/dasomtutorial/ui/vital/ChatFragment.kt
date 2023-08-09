package com.onethefull.dasomtutorial.ui.vital

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.onethefull.dasomtutorial.databinding.FragmentChatBinding
import com.onethefull.dasomtutorial.utils.InjectorUtils
import kotlinx.coroutines.launch

/**
 * Created by sjw on 2023/08/09
 */
class ChatFragment : Fragment(){
    private lateinit var binding : FragmentChatBinding
    private val viewModel: ChatViewModel by viewModels {
        InjectorUtils.provideChatViewModelFactory(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentChatBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        test()
    }

    private fun test() {
        viewModel.requestVitalScenario()
    }
}