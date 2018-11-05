@file:Suppress("MemberVisibilityCanBePrivate", "unused", "DEPRECATION")

package com.chrynan.aclifecycle

import android.app.Activity
import android.app.Fragment
import android.app.Service
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.chrynan.accore.UI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

interface BaseCoroutineScope : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = UI + job

    val job: Job
}

abstract class CoroutineLifecycleObserver : BaseCoroutineScope,
    LifecycleObserver {

    override lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = UI + job

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        job = Job()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        job.cancel()
    }
}

abstract class CoroutineActivity : Activity(),
    BaseCoroutineScope {

    override lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}

abstract class CoroutineAppCompatActivity : AppCompatActivity(),
    BaseCoroutineScope {

    override lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}

abstract class CoroutineFragment : Fragment(),
    BaseCoroutineScope {

    override lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}

abstract class CoroutineAppCompatFragment : android.support.v4.app.Fragment(),
    BaseCoroutineScope {

    override lateinit var job: Job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}

abstract class CoroutineService : Service(),
    BaseCoroutineScope {

    override lateinit var job: Job

    override fun onCreate() {
        super.onCreate()
        job = Job()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}