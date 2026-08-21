package com.example.bookreview

import android.app.Application
import com.example.bookreview.di.AppContainer
import com.example.bookreview.di.DefaultAppContainer

class BookReviewApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
