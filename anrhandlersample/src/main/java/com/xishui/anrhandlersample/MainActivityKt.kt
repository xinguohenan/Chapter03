package com.xishui.anrhandlersample

import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import javax.inject.Inject

class MainActivityKt: AppCompatActivity() {
    @Inject lateinit var context: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ANRSampleApplication.sAppComponent.inject(this)

        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener({
            //Toast.makeText()
        })
    }
}