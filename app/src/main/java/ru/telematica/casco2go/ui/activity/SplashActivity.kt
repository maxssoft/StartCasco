package ru.telematica.casco2go.ui.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife

import ru.telematica.casco2go.R

class SplashActivity : AppCompatActivity() {

    @BindView(R.id.root_layout)
    lateinit var rootLayout: ViewGroup

    @BindView(R.id.logoImage)
    lateinit var logoImage: View

    @BindView(R.id.textDescription)
    lateinit var textDescription: View

    @BindView(R.id.textContinue)
    lateinit var textContinue: View

    @BindView(R.id.bottom_layout)
    lateinit var bottom_layout: ViewGroup

    private val handler: Handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        ButterKnife.bind(this, this)

        rootLayout.setOnTouchListener(onTouchListener)

        logoImage.animate().alpha(1f).setDuration(1500).start()
        logoImage.setOnTouchListener(onTouchListener)

        textDescription.animate().alpha(1f).setDuration(1000).start()
        textDescription.setOnTouchListener(onTouchListener)

        textContinue.animate().alpha(1f).setDuration(3000).start()
        textDescription.setOnTouchListener(onTouchListener)

        handler.postDelayed(Runnable { startNextActivity() }, 10000)
    }

    val onTouchListener: View.OnTouchListener = View.OnTouchListener { v, event -> startNextActivity() }

    fun startNextActivity(): Boolean {
        handler.removeCallbacksAndMessages(null)
        startActivity(Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY))
        finish()
        return true
    }

    override fun onBackPressed() {
        handler.removeCallbacksAndMessages(null)
        super.onBackPressed()
    }

}
