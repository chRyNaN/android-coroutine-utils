@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.chrynan.aclifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.chrynan.accore.UI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class CoroutineLifecycleObservers : CoroutineScope,
    LifecycleObserver {

    protected lateinit var job: Job

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

interface BaseCoroutineScope : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = UI + job

    val job: Job
}