package com.xishui.anrhandlersample

import android.app.Application
import com.xishui.anrhandlersample.dependencyinjection.AppComponent
import com.xishui.anrhandlersample.dependencyinjection.ContextModule
import com.xishui.anrhandlersample.dependencyinjection.DaggerAppComponent

class ANRSampleApplication : Application() {
    companion object {
        lateinit var sAppComponent: AppComponent
    }

    init {
        sAppComponent = DaggerAppComponent.builder().contextModule(ContextModule(this@ANRSampleApplication)).build()
    }
}