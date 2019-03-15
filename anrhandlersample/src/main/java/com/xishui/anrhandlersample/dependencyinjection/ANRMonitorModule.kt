package com.xishui.anrhandlersample.dependencyinjection

import com.xishui.anrhandler.ANRMonitor
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ANRMonitorModule {

    @Provides
    @Singleton
    fun provideANRMonitor(): ANRMonitor {
        return ANRMonitor(null)
    }
}