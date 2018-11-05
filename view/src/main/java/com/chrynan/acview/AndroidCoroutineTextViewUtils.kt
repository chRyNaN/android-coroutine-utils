@file:Suppress("unused")

package com.chrynan.acview

import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import com.chrynan.accore.currentScopeJob
import com.chrynan.accore.runInBackground
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce

@ExperimentalCoroutinesApi
suspend fun EditText.onTextChanged(block: suspend CoroutineScope.(TextChangeEvent.OnTextChanged) -> Unit) =
    coroutineScope {
        val textWatcher = CoroutineChannelTextWatcher()

        addTextChangedListener(textWatcher)

        val eventChannel = textWatcher.onTextChangedEvents

        currentScopeJob?.invokeOnCompletion {
            removeTextChangedListener(textWatcher)
            eventChannel.cancel()
        }

        val context = coroutineContext

        runInBackground {
            while (isActive and !eventChannel.isClosedForReceive) {
                withContext(context) { block(eventChannel.receive()) }
            }
        }
    }

@ExperimentalCoroutinesApi
suspend fun EditText.afterTextChanged(block: suspend CoroutineScope.(TextChangeEvent.AfterTextChanged) -> Unit) =
    coroutineScope {
        val textWatcher = CoroutineChannelTextWatcher()

        addTextChangedListener(textWatcher)

        val eventChannel = textWatcher.afterTextChangedEvents

        currentScopeJob?.invokeOnCompletion {
            removeTextChangedListener(textWatcher)
            eventChannel.cancel()
        }

        val context = coroutineContext

        runInBackground {
            while (isActive and !eventChannel.isClosedForReceive) {
                withContext(context) { block(eventChannel.receive()) }
            }
        }
    }

@ExperimentalCoroutinesApi
suspend fun EditText.beforeTextChanged(block: suspend CoroutineScope.(TextChangeEvent.BeforeTextChanged) -> Unit) =
    coroutineScope {
        val textWatcher = CoroutineChannelTextWatcher()

        addTextChangedListener(textWatcher)

        val eventChannel = textWatcher.beforeTextChangedEvents

        currentScopeJob?.invokeOnCompletion {
            removeTextChangedListener(textWatcher)
            eventChannel.cancel()
        }

        val context = coroutineContext

        runInBackground {
            while (isActive and !eventChannel.isClosedForReceive) {
                withContext(context) { block(eventChannel.receive()) }
            }
        }
    }

@ExperimentalCoroutinesApi
fun CoroutineScope.receiveOnTextChangedEvents(events: ReceiveChannel<TextChangeEvent>) =
    produce<TextChangeEvent.OnTextChanged> {
        for (event in events) {
            if (event is TextChangeEvent.OnTextChanged) offer(event)
        }
    }

@ExperimentalCoroutinesApi
fun CoroutineScope.receiveBeforeTextChangedEvents(events: ReceiveChannel<TextChangeEvent>) =
    produce<TextChangeEvent.BeforeTextChanged> {
        for (event in events) {
            if (event is TextChangeEvent.BeforeTextChanged) offer(event)
        }
    }

@ExperimentalCoroutinesApi
fun CoroutineScope.receiveAfterTextChangedEvents(events: ReceiveChannel<TextChangeEvent>) =
    produce<TextChangeEvent.AfterTextChanged> {
        for (event in events) {
            if (event is TextChangeEvent.AfterTextChanged) offer(event)
        }
    }

@ExperimentalCoroutinesApi
suspend fun TextView.textChangeEvents(): ReceiveChannel<TextChangeEvent> =
    coroutineScope {
        val textWatcher = CoroutineChannelTextWatcher()

        addTextChangedListener(textWatcher)

        val eventChannel = textWatcher.events.onTextChanges()

        currentScopeJob?.invokeOnCompletion {
            removeTextChangedListener(textWatcher)
            eventChannel.cancel()
        }

        eventChannel
    }

@ExperimentalCoroutinesApi
fun ReceiveChannel<TextChangeEvent>.afterTextChangesWithScope(coroutineScope: CoroutineScope = GlobalScope): ReceiveChannel<TextChangeEvent.AfterTextChanged> =
    let { channel ->
        coroutineScope.produce<TextChangeEvent.AfterTextChanged> {
            for (event in channel) {
                if (event is TextChangeEvent.AfterTextChanged) offer(event)
            }
        }
    }

@ExperimentalCoroutinesApi
fun ReceiveChannel<TextChangeEvent>.beforeTextChangesWithScope(coroutineScope: CoroutineScope = GlobalScope): ReceiveChannel<TextChangeEvent.BeforeTextChanged> =
    let { channel ->
        coroutineScope.produce<TextChangeEvent.BeforeTextChanged> {
            for (event in channel) {
                if (event is TextChangeEvent.BeforeTextChanged) offer(event)
            }
        }
    }

@ExperimentalCoroutinesApi
fun ReceiveChannel<TextChangeEvent>.onTextChangesWithScope(coroutineScope: CoroutineScope = GlobalScope): ReceiveChannel<TextChangeEvent.OnTextChanged> =
    let { channel ->
        coroutineScope.produce<TextChangeEvent.OnTextChanged> {
            for (event in channel) {
                if (event is TextChangeEvent.OnTextChanged) offer(event)
            }
        }
    }

@ExperimentalCoroutinesApi
suspend fun ReceiveChannel<TextChangeEvent>.afterTextChanges(): ReceiveChannel<TextChangeEvent.AfterTextChanged> =
    coroutineScope {
        produce<TextChangeEvent.AfterTextChanged> {
            for (event in this@afterTextChanges) {
                if (event is TextChangeEvent.AfterTextChanged) offer(event)

            }
        }
    }

@ExperimentalCoroutinesApi
suspend fun ReceiveChannel<TextChangeEvent>.beforeTextChanges(): ReceiveChannel<TextChangeEvent.BeforeTextChanged> =
    coroutineScope {
        produce<TextChangeEvent.BeforeTextChanged> {
            for (event in this@beforeTextChanges) {
                if (event is TextChangeEvent.BeforeTextChanged) offer(event)

            }
        }
    }

@ExperimentalCoroutinesApi
suspend fun ReceiveChannel<TextChangeEvent>.onTextChanges(): ReceiveChannel<TextChangeEvent.OnTextChanged> =
    coroutineScope {
        produce<TextChangeEvent.OnTextChanged> {
            for (event in this@onTextChanges) {
                if (event is TextChangeEvent.OnTextChanged) offer(event)

            }
        }
    }

@ExperimentalCoroutinesApi
class CoroutineChannelTextWatcher(channelQueueCapacity: Int = Channel.CONFLATED) : TextWatcher {

    val events: ReceiveChannel<TextChangeEvent>
        get() = broadcastChannel.openSubscription()
    val onTextChangedEvents: ReceiveChannel<TextChangeEvent.OnTextChanged>
        get() = onTextChangedBroadcastChannel.openSubscription()
    val beforeTextChangedEvents: ReceiveChannel<TextChangeEvent.BeforeTextChanged>
        get() = beforeTextChangedBroadcastChannel.openSubscription()
    val afterTextChangedEvents: ReceiveChannel<TextChangeEvent.AfterTextChanged>
        get() = afterTextChangedBroadcastChannel.openSubscription()

    private val broadcastChannel = BroadcastChannel<TextChangeEvent>(channelQueueCapacity)
    private val onTextChangedBroadcastChannel = BroadcastChannel<TextChangeEvent.OnTextChanged>(channelQueueCapacity)
    private val beforeTextChangedBroadcastChannel =
        BroadcastChannel<TextChangeEvent.BeforeTextChanged>(channelQueueCapacity)
    private val afterTextChangedBroadcastChannel =
        BroadcastChannel<TextChangeEvent.AfterTextChanged>(channelQueueCapacity)

    override fun afterTextChanged(s: Editable) {
        send { TextChangeEvent.AfterTextChanged(editable = s) }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        send { TextChangeEvent.BeforeTextChanged(charSequence = s, start = start, count = count, after = after) }
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        send { TextChangeEvent.OnTextChanged(charSequence = s, start = start, before = before, count = count) }
    }

    private fun send(block: () -> TextChangeEvent) =
        try {
            val event = block()
            if (event is TextChangeEvent.OnTextChanged) onTextChangedBroadcastChannel.offer(event)
            if (event is TextChangeEvent.BeforeTextChanged) beforeTextChangedBroadcastChannel.offer(event)
            if (event is TextChangeEvent.AfterTextChanged) afterTextChangedBroadcastChannel.offer(event)
            broadcastChannel.offer(event)
        } catch (e: Exception) {
            broadcastChannel.cancel(e)
        }
}

sealed class TextChangeEvent {

    data class AfterTextChanged(val editable: Editable) : TextChangeEvent()

    data class BeforeTextChanged(
        val charSequence: CharSequence,
        val start: Int,
        val count: Int,
        val after: Int
    ) : TextChangeEvent()

    data class OnTextChanged(
        val charSequence: CharSequence,
        val start: Int,
        val before: Int,
        val count: Int
    ) : TextChangeEvent()
}

@ExperimentalCoroutinesApi
suspend fun TextView.onEnterAction(block: suspend CoroutineScope.(EditorActionEvent) -> Unit) = coroutineScope {
    val onEditorActionListener = CoroutineChannelOnEditorActionListener()

    setOnEditorActionListener(onEditorActionListener)

    val eventChannel = onEditorActionListener.channel

    currentScopeJob?.invokeOnCompletion {
        setOnEditorActionListener(null)
        eventChannel.cancel()
    }

    val context = coroutineContext

    runInBackground {
        while (isActive and !eventChannel.isClosedForReceive) {
            withContext(context) { block(eventChannel.receive()) }
        }
    }
}

@ExperimentalCoroutinesApi
suspend fun ReceiveChannel<EditorActionEvent>.enterActions(): ReceiveChannel<EditorActionEvent> =
    coroutineScope {
        produce {
            for (event in this@enterActions) {
                if (event.actionId == EditorInfo.IME_ACTION_DONE || event.keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                    offer(event)
                }
            }
        }
    }

@ExperimentalCoroutinesApi
fun CoroutineScope.receiveEnterActions(events: ReceiveChannel<EditorActionEvent>) =
    produce {
        for (event in events) {
            if (event.actionId == EditorInfo.IME_ACTION_DONE || event.keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                offer(event)
            }
        }
    }

@ExperimentalCoroutinesApi
fun ReceiveChannel<EditorActionEvent>.enterActionsWithScope(coroutineScope: CoroutineScope = GlobalScope): ReceiveChannel<EditorActionEvent> =
    coroutineScope.produce {
        for (event in this@enterActionsWithScope) {
            if (event.actionId == EditorInfo.IME_ACTION_DONE || event.keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                offer(event)
            }
        }
    }

@ExperimentalCoroutinesApi
suspend fun TextView.editorActionEvents(): ReceiveChannel<EditorActionEvent> =
    coroutineScope {
        val onEditorActionListener = CoroutineChannelOnEditorActionListener()

        setOnEditorActionListener(onEditorActionListener)

        val eventChannel = onEditorActionListener.channel

        currentScopeJob?.invokeOnCompletion {
            setOnEditorActionListener(null)
            eventChannel.cancel()
        }

        eventChannel
    }

@ExperimentalCoroutinesApi
class CoroutineChannelOnEditorActionListener(channelQueueCapacity: Int = Channel.CONFLATED) :
    TextView.OnEditorActionListener {

    val channel: ReceiveChannel<EditorActionEvent>
        get() = broadcastChannel.openSubscription()

    private val broadcastChannel = BroadcastChannel<EditorActionEvent>(channelQueueCapacity)

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
        send(EditorActionEvent(actionId = actionId, keyEvent = event))

        return false
    }

    private fun send(event: EditorActionEvent) =
        try {
            broadcastChannel.offer(event)
        } catch (e: Exception) {
            broadcastChannel.cancel(e)
        }
}

data class EditorActionEvent(
    val actionId: Int,
    val keyEvent: KeyEvent
)