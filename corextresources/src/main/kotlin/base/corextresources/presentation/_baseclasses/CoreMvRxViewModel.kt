package base.corextresources.presentation._baseclasses

import androidx.lifecycle.viewModelScope
import base.coreresources.toplevel.appIsDebug
import com.airbnb.mvrx.Async
import com.airbnb.mvrx.BaseMvRxViewModel
import com.airbnb.mvrx.Fail
import com.airbnb.mvrx.Loading
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.RealMvRxStateStore
import com.airbnb.mvrx.Success
import com.github.icarohs7.unoxcore.UnoxCore
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Base ViewModel implementing a [CoroutineScope]
 * and inheriting from [BaseMvRxViewModel]
 */
open class CoreMvRxViewModel<S : MvRxState>(initialState: S) : BaseMvRxViewModel<S>(
    initialState,
    appIsDebug,
    RealMvRxStateStore(initialState)
) {
    /**
     * Access the state after all pending changes
     * are applied and invoke the given function
     * on it, returning the result of the invocation
     */
    suspend fun <R> withStateSuspend(block: suspend (state: S) -> R): R {
        return coroutineScope {
            val state = suspendCoroutine<S> { cont -> withState { cont.resume(it) } }
            block(state)
        }
    }

    /**
     * Connect the given [Flow] to the
     * state of the viewmodel, updating
     * the state on each emission
     */
    fun Flow<S>.connectToState(): Flow<S> {
        return connectToState { this }
    }

    /**
     * Use the given transformer and for each
     * emission of the given [Flow], transform
     * the emmited item and apply the reducer to
     * evolve the state
     */
    fun <T> Flow<T>.connectToState(transformer: S.(T) -> S): Flow<T> {
        return onEach { item -> setState { transformer(this, item) } }
            .flowOn(Dispatchers.Default)
    }

    /**
     * Connect the given flowable to the
     * state of the viewmodel, updating
     * the state on each emission
     */
    fun Flowable<S>.connectToState(): Disposable {
        return connectToState { this }
    }

    /**
     * Use the given transformer and for each
     * emission of the given Flowable, transform
     * the emmited item and apply the reducer to
     * evolve the state
     */
    fun <T> Flowable<T>.connectToState(transformer: S.(T) -> S): Disposable {
        return subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { item -> setState { transformer(this, item) } }
    }

    /**
     * Execute a flow and wrap its progression with AsyncData reduced to the global state.
     *
     * @param mapper A map converting the flow type to the desired AsyncData type.
     * @param stateReducer A reducer that is applied to the current state and should return the
     *                     new state. Because the state is the receiver and it likely a data
     *                     class, an implementation may look like: `{ copy(response = it) }`.
     */
    fun <T, V> Flow<T>.execute(mapper: (T) -> V, stateReducer: S.(Async<V>) -> S): Job {
        setState { stateReducer(Loading()) }

        return map<T, Async<V>> { value -> Success(mapper(value)) }
            .catch { emit(Fail(it)) }
            .onEach { asyncData -> setState { stateReducer(asyncData) } }
            .launchIn(viewModelScope)
    }

    /**
     * Emit all changes of the state of this
     * viewModel
     */
    fun stateFlow(): Flow<S> = channelFlow {
        val disposable = subscribe { offer(it) }
        awaitClose { disposable.dispose() }
    }

    /**
     * Flow emmiting only the distinct emissions of
     * the state transformed by the sleectFn function
     */
    fun <A> selectStateFlow(selectFn: suspend S.() -> A): Flow<A> {
        return stateFlow()
            .map(selectFn)
            .flowOn(UnoxCore.backgroundDispatcher)
            .distinctUntilChanged()
    }
}