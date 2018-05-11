package ru.telematica.casco2go.ui.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import ru.telematica.casco2go.BuildConfig

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

    @BindView(R.id.textVersion)
    lateinit var textVersion: TextView

    private val handler: Handler = Handler()
    private var animationStarted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        ButterKnife.bind(this, this)

        rootLayout.setOnTouchListener(onTouchListener)
        logoImage.setOnTouchListener(onTouchListener)
        textDescription.setOnTouchListener(onTouchListener)
        textDescription.setOnTouchListener(onTouchListener)

        textVersion.setText("ver " + BuildConfig.VERSION_NAME)

        if (!animationStarted) {
            startAnimations()
        }

        handler.postDelayed(Runnable { startNextActivity() }, 10000)
    }

    override fun onResume() {
        super.onResume()
    }
    private fun startAnimations(){
        animationStarted = true

        logoImage.animate().alpha(1f).setInterpolator(DecelerateInterpolator()).setDuration(3000).start()
        textVersion.animate().alpha(1f).setInterpolator(DecelerateInterpolator()).setDuration(3000).start()

        textDescription.animate().alpha(1f).setInterpolator(AccelerateInterpolator()).setDuration(1500).start()
        textContinue.animate().alpha(1f).setInterpolator(AccelerateInterpolator()).setDuration(600).setStartDelay(3000).start()
    }

    val onTouchListener: View.OnTouchListener = View.OnTouchListener { v, event -> startNextActivity() }

    fun startNextActivity(): Boolean {
        handler.removeCallbacksAndMessages(null)
        finish()
        val intent = Intent(this, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        startActivity(intent)
        return true
    }

    override fun onBackPressed() {
        handler.removeCallbacksAndMessages(null)
        super.onBackPressed()
    }

}
