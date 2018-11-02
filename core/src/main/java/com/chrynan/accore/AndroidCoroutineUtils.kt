@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.chrynan.accore

import kotlinx.coroutines.*

val UI: CoroutineDispatcher = Dispatchers.Main
val IO: CoroutineDispatcher = Dispatchers.IO

val CoroutineScope.currentScopeJob: Job?
    get() = coroutineContext[Job]

fun runOnAndroidUI(block: suspend CoroutineScope.() -> Unit): Job = GlobalScope.launch(context = UI, block = block)

fun runInBackground(block: suspend CoroutineScope.() -> Unit): Job = GlobalScope.launch(context = IO, block = block)

fun <T> asyncOnAndroidUI(block: suspend CoroutineScope.() -> T): Deferred<T> =
    GlobalScope.async(context = UI, block = block)

fun <T> asyncInBackground(block: suspend CoroutineScope.() -> T): Deferred<T> =
    GlobalScope.async(context = IO, block = block)

fun CoroutineScope.runOnAndroidUI(block: suspend CoroutineScope.() -> Unit): Job = launch(context = UI, block = block)

fun CoroutineScope.runInBackground(block: suspend CoroutineScope.() -> Unit): Job = launch(context = IO, block = block)