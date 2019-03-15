package com.xishui.anrhandlersample.dependencyinjection

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ContextModule {
    var application: Context

    constructor(applicationParam: Application) {
        application =  applicationParam
    }

    @Provides
    @Singleton
    fun provideContext(): Context {
        return application
    }
}