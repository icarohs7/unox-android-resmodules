package base.corextresources

import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.annotation.CallSuper
import base.coreresources.CoreRes
import com.facebook.stetho.Stetho
import com.umutbey.stateviews.StateViewsBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.android.logger.AndroidLogger
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import splitties.resources.drawable
import splitties.resources.str
import timber.log.Timber

abstract class BaseApplication : Application() {
    @CallSuper
    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
        setupKoin()
        setupTimber()
        CoreRes.init()
        lockOrientation()
        setupStateViews(getStateViewsBuilder())
    }

    private fun setupKoin() {
        startKoin {
            onSetupKoin(this)
            logger(AndroidLogger())
        }
    }

    open fun onSetupKoin(koinApplication: KoinApplication): Unit = with(koinApplication) {
        androidContext(this@BaseApplication)
        modules(onCreateKoinModules())
    }

    open fun onCreateKoinModules(): List<Module> {
        return listOf(module {})
    }

    private fun setupTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    /**
     * Override to use another orientation
     * throughout the apllication.
     * List of option on [ActivityInfo].
     * Return null to use user defined
     * orientation
     */
    open fun onSelectOrientation(): Int? {
        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    private fun lockOrientation() {
        val orientation = onSelectOrientation() ?: return

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, bundle: Bundle?) {
                activity.requestedOrientation = orientation
            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {

            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {

            }
        })
    }

    open fun setupStateViews(builder: StateViewsBuilder) {
    }

    private fun getStateViewsBuilder(): StateViewsBuilder {
        return StateViewsBuilder.init().setState(
            tag = EMPTY_ADAPTER_STATE_TAG,
            title = str(R.string.nenhum_item_carregado),
            description = "",
            icon = drawable(R.drawable.ic_search_gray_24dp)
        )
            .setIconSize(120)
    }

    companion object {
        const val EMPTY_ADAPTER_STATE_TAG: String = "base_application_empty_adapter"
    }
}