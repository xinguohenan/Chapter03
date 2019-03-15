package com.xishui.anrhandlersample.dependencyinjection

import com.xishui.anrhandlersample.MainActivity
import com.xishui.anrhandlersample.MainActivityKt
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules= arrayOf(ANRMonitorModule::class, ContextModule::class))
interface AppComponent {
    fun inject(activity: MainActivity)
    fun inject(activity: MainActivityKt)
}