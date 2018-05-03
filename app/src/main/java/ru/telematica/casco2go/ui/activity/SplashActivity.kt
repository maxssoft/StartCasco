package ru.telematica.casco2go.ui.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

import ru.telematica.casco2go.R

class SplashActivity : AppCompatActivity() {

    private val handler: Handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        handler.postDelayed(Runnable { startActivity(Intent(this, MainActivity::class.java)) }, 1000)
    }

}
