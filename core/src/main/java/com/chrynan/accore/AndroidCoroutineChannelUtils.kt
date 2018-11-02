package com.chrynan.accore

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce

@ExperimentalCoroutinesApi
fun <T> CoroutineScope.debounce(debounceTimeInMilliseconds: Long = 200, events: ReceiveChannel<T>) = produce<T> {
    var lastEventTimeInMilliseconds = 0L

    while (isActive and !events.isClosedForReceive) {
        val event = async { events.receive() }
        val timeDiff = System.currentTimeMillis() - lastEventTimeInMilliseconds

        if (timeDiff > debounceTimeInMilliseconds) {
            send(event.await())
            lastEventTimeInMilliseconds = System.currentTimeMillis()
        } else {
            delay(timeDiff)
            send(event.await())
            lastEventTimeInMilliseconds = System.currentTimeMillis()
        }
    }
}