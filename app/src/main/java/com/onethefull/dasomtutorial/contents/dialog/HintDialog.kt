package com.onethefull.dasomtutorial.contents.dialog

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.ViewGroup
import com.onethefull.dasomiconv.App
import com.onethefull.dasomiconv.R

/**
 * Created by Douner on 2020/06/25.
 */
class HintDialog(context: Context) : BaseDialog<Aud>(context), DialogInterface.OnShowListener {
    var message: String? = ""
    var handler = Handler(Looper.getMainLooper()) {
        if (App.instance.currentActivity != null) this.dismiss()
        false
    }

    fun setText(message: String): HintDialog {
        this.message = message
        return this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = setViewBinding(layoutInflater)
        window?.apply {
            setBackgroundDrawableResource(R.color.transparent)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        binding.tvIntro.text = message
        binding.btnCommit.setOnClickListener {
            handler.removeCallbacksAndMessages(null)
            handler.sendEmptyMessage(0)
        }
        setOnShowListener(this)
    }

    override fun onShow(dialog: DialogInterface?) {
        handler.removeCallbacksAndMessages(null)
        handler.sendMessageDelayed(Message(), 5000)
    }
}