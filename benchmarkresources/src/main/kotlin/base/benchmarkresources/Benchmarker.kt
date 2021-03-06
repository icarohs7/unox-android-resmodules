package base.benchmarkresources

import arrow.core.Try
import com.github.icarohs7.unoxcore.extensions.coroutines.forEach
import com.github.icarohs7.unoxcore.extensions.orEmpty
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.actor
import timber.log.Timber

/**
 * Object used to globally benchmark time differences
 */
object Benchmarker {
    private val l by lazy { mutableListOf<Pair<String, Long>>() }
    private val actor by lazy {
        GlobalScope.actor<Pair<String, Long>>(capacity = 10) {
            channel.forEach {
                if (it == Pair("die", -1L)) endBenchmark()
                else l += it
            }
        }
    }

    private fun endBenchmark() {
        val total = Try {
            val a = l.first()
            val b = l.last()
            val (nameA, timeA) = a
            val (nameB, timeB) = b
            "$nameA <-${timeB - timeA}-> $nameB"
        }

        val str = l
            .zipWithNext { a, b ->
                val (nameA, timeA) = a
                val (nameB, timeB) = b
                "($nameA {${timeB - timeA}} $nameB)"
            }
            .joinToString(separator = " ")

        l.clear()
        log(str)
        log(total.orEmpty())
    }

    /**
     * Add a measuring point to the benchmarker
     */
    fun measure(identifier: Any) {
        val t = System.currentTimeMillis()
        actor.offer("$identifier" to t)
    }

    /**
     * End the benchmark, logging each measured
     * point and the delta time between each
     */
    fun calculate() {
        actor.offer("die" to -1L)
    }

    private fun log(message: String) {
        Timber.tag("BENCHMARKER").i(message)
    }
}