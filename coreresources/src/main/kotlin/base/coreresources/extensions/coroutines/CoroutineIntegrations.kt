package base.coreresources.extensions.coroutines

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import arrow.core.Try
import arrow.core.Tuple2
import com.github.icarohs7.unoxcore.UnoxCore
import com.github.icarohs7.unoxcore.extensions.coroutines.onForeground
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Convert the emissions of a broadcast using the
 * given [intentFilter] to emitted items on
 * a [ReceiveChannel], exposing the instance
 * of the receiver and the received intent
 */
fun Context.broadcastReceiverFlow(intentFilter: IntentFilter): Flow<Tuple2<Intent, BroadcastReceiver>> = callbackFlow {
    val receiver = object : BroadcastReceiver() {
        val receiver: BroadcastReceiver = this
        override fun onReceive(context: Context, intent: Intent) {
            offer(Tuple2(intent, receiver))
        }
    }
    registerReceiver(receiver, intentFilter)
    awaitClose { unregisterReceiver(receiver) }
}

/**
 * Suspend until a broadcast matching the given [intentFilter] filter
 * is emmited, then running the block with the received [Intent] and
 * [BroadcastReceiver] instances
 */
suspend fun <T> Context.awaitBroadcast(intentFilter: IntentFilter, block: (Tuple2<Intent, BroadcastReceiver>) -> T): T {
    return suspendCancellableCoroutine { continuation ->
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                unregisterReceiver(this)
                continuation.resume(block(Tuple2(intent, this)))
            }
        }
        registerReceiver(receiver, intentFilter)
        continuation.invokeOnCancellation { unregisterReceiver(receiver) }
    }
}