package base.coreresources.extensions

import android.app.Activity
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import base.coreresources.CoreRes
import splitties.systemservices.activityManager
import java.util.Calendar
import kotlin.reflect.KClass

/**
 * Launch the given activity
 */
inline fun <reified T : AppCompatActivity> Context.startActivity(
    extras: Bundle = bundleOf(),
    finishActivity: Boolean = false,
    flags: List<Int> = emptyList()
) {
    startActivity(T::class, extras, finishActivity, flags = flags)
}

/**
 * Launch the given activity
 */
fun <T : AppCompatActivity> Context.startActivity(
    destination: KClass<T>,
    extras: Bundle = bundleOf(),
    finishActivity: Boolean = false,
    activityTransition: CoreRes.ActivityTransitionAnimation = CoreRes.defaultActivityTransition,
    flags: List<Int> = emptyList()
) {
    val intent = Intent(this, destination.java)
    intent.putExtras(extras)
    flags.forEach { intent.addFlags(it) }
    startActivity(intent)
    if (this is Activity) {
        overridePendingTransition(activityTransition.enterRes, activityTransition.exitRes)
        if (CoreRes.finishActivityOnNavigate || finishActivity) finish()
    }
}

/**
 * Return a date picker dialog
 */
fun Context.dialogDatePicker(listener: (year: Int, month: Int, day: Int) -> Unit): DatePickerDialog {
    val c = Calendar.getInstance()
    val year = c.get(Calendar.YEAR)
    val month = c.get(Calendar.MONTH)
    val day = c.get(Calendar.DAY_OF_MONTH)

    return DatePickerDialog(
        this,
        { _, y, m, d -> listener(y, m + 1, d) },
        year,
        month,
        day)
}

/** @return A pending intent used to start the given activity */
fun Context.pendingIntentToActivity(activity: KClass<out AppCompatActivity>): PendingIntent? {
    val intent = Intent(this, activity.java)
    return PendingIntent.getActivity(this, 0, intent, 0)
}

/** Helper used to get reference to the activity */
val Activity.context: Context
    get() = this

/**
 * Whether the given service is running
 * or not
 */
@Suppress("DEPRECATION")
fun <T : Any> Context.isServiceRunning(service: KClass<T>): Boolean {
    return (activityManager)
        .getRunningServices(Integer.MAX_VALUE)
        .any { it.service.className == service.java.name }
}

/**
 * Open the telephone dialer with the
 * given phone number
 */
fun Context.openDialer(phone: String) {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:$phone")
    startActivity(intent)
}

/**
 * Open the given website in the user's
 * browser
 */
fun Context.openWebsite(destination: Uri) {
    startActivity(Intent(Intent.ACTION_VIEW, destination))
}

/**
 * Open the share dialog for the content resolved
 * by the given uri
 */
fun Context.shareUsingUri(uri: Uri, contentType: String, shareSheetTitle: String) {
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        type = contentType
    }
    startActivity(Intent.createChooser(intent, shareSheetTitle))
}