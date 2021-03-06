package base.dialogresources.presentation.dialogs

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.core.view.forEach
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import arrow.core.Try
import base.dialogresources.R
import base.dialogresources.domain.extensions.toolbar
import com.github.icarohs7.unoxcore.extensions.coroutines.onForeground
import com.nikialeksey.fullscreendialog.FsDialog
import com.nikialeksey.fullscreendialog.FsDialogToolbar
import com.nikialeksey.fullscreendialog.buttons.FsActionButton
import com.nikialeksey.fullscreendialog.buttons.FsCloseButton
import com.nikialeksey.fullscreendialog.buttons.SimpleButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import splitties.resources.appColor
import splitties.resources.appDrawable
import splitties.views.backgroundColor
import kotlin.coroutines.resume

abstract class BaseFullscreenMaterialDialog(
    protected val context: Context,
    protected val title: String
) : LifecycleOwner {
    private val lifecycleRegistry by lazy { LifecycleRegistry(this) }

    protected val dialog: FsDialog by lazy {
        val toolbar = createToolbar()
        val content = createDialogContent()
        createDialog(toolbar, content).apply {
            setOnCancelListener { lifecycleRegistry.currentState = Lifecycle.State.DESTROYED }
            setOnShowListener { lifecycleRegistry.currentState = Lifecycle.State.RESUMED }
            addOnClose {
                lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
                dismiss()
            }
            onCreateView(toolbar, content, this)
        }
    }

    open fun onCreateView(toolbar: FsDialogToolbar, view: View, dialog: FsDialog) {
    }

    open fun createDialog(toolbar: FsDialogToolbar, content: View): FsDialog {
        return FsDialog(
            context,
            R.style.AppTheme,
            toolbar,
            content
        )
    }

    open fun createToolbar(): FsDialogToolbar {
        return FsDialogToolbar(
            context,
            title,
            createCloseButton(),
            createActionButton()
        ).apply {
            backgroundColor = appColor(R.color.colorPrimaryVariant)
            toolbar.apply {
                setTitleTextColor(appColor(R.color.colorOnPrimary))
                menu.forEach { Try { findViewById<TextView>(it.itemId).setTextColor(Color.WHITE) } }
            }
        }
    }

    open fun createCloseButton() = FsCloseButton(SimpleButton(), appDrawable(R.drawable.fs_close_icon)!!)
    open fun createActionButton() = FsActionButton(SimpleButton(), "")

    fun show(scope: CoroutineScope): Job {
        return scope.launch { show() }
    }

    suspend fun show() {
        onForeground {
            suspendCancellableCoroutine<Unit> { cont ->
                dialog.show()
                onDismiss { cont.resume(Unit) }
                cont.invokeOnCancellation { dialog.dismiss() }
            }
        }
    }

    fun dismiss() {
        GlobalScope.launch(Dispatchers.Main.immediate) {
            dialog.dismiss()
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        }
    }

    fun onDismiss(block: () -> Unit) {
        dialog.addOnClose {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
            block()
        }
        dialog.setOnCancelListener {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
            block()
        }
    }

    abstract fun createDialogContent(): View
    override fun getLifecycle(): Lifecycle = lifecycleRegistry
}