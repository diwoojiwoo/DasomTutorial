package com.onethefull.dasomtutorial.ui.guide

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.onethefull.dasomtutorial.R
import com.onethefull.dasomtutorial.base.OnethefullBase
import com.onethefull.dasomtutorial.databinding.FragmentGuideBinding
import com.onethefull.dasomtutorial.ui.learn.LearnStatus
import com.onethefull.dasomtutorial.ui.learn.LearnViewModel
import com.onethefull.dasomtutorial.utils.InjectorUtils
import com.onethefull.dasomtutorial.utils.Status
import com.onethefull.dasomtutorial.utils.logger.DWLog

class GuideFragment : Fragment() {
    private lateinit var viewDataBinding: FragmentGuideBinding
    private var type: String = ""
    private val viewModel: GuideViewModel by viewModels {
        InjectorUtils.provideGuideViewModelFactory(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            type = GuideFragmentArgs.fromBundle(it).type
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        DWLog.d("[Guide] onCreateView")
        viewDataBinding = FragmentGuideBinding.inflate(inflater, container, false).apply {
            viewmodel = viewModel
        }
        return viewDataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DWLog.d("[Guide] onViewCreated")
        setUpText()
    }

    private fun setUpText() {
        DWLog.d("[Guide] setUpText")
        viewModel.getGuideComment(type)
        viewModel.guideComment().observe(
            viewLifecycleOwner, {
                when (it.status) {
                    Status.SUCCESS -> {
                        it.data?.let {
                            DWLog.d("guideComment ${it.actionName}, ${it.text}")
                        }
                    }
                    Status.LOADING -> {

                    }
                    Status.ERROR -> {
                        //Handle Error
                        DWLog.e(it.message.toString())
                    }
                }
            }
        )
    }

    override fun onPause() {
        super.onPause()
        DWLog.e("[Guide] onPause")
        viewModel.disconnect()
    }

    companion object {

    }
}