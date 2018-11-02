package com.chrynan.accore

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@ExperimentalCoroutinesApi
// TODO Ugh this could emit delayed values. Not sure if there is a way around that
fun <T> CoroutineScope.debounce(debounceTimeInMilliseconds: Long = 200, events: ReceiveChannel<T>) = produce {
    var lastEventTimeInMilliseconds = 0L

    while (isActive and !events.isClosedForReceive) {
        val event = events.receive()
        val timeDiff = System.currentTimeMillis() - lastEventTimeInMilliseconds

        lastEventTimeInMilliseconds = if (timeDiff > debounceTimeInMilliseconds) {
            send(event)
            System.currentTimeMillis()
        } else {
            delay(timeDiff)
            send(event)
            System.currentTimeMillis()
        }
    }
}