package base.bottomsheetresources.presentation.dialogs

import android.content.Context
import androidx.databinding.ViewDataBinding
import base.dialogresources.presentation.dialogs.BaseMaterialDialog
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet

/**
 * Base class used to hold and handle
 * bottom sheets
 */
abstract class BaseMaterialBottomSheet<T : ViewDataBinding>(ctx: Context) : BaseMaterialDialog<T>(ctx) {
    override fun getMaterialDialog(): MaterialDialog {
        return MaterialDialog(context, BottomSheet(LayoutMode.WRAP_CONTENT))
    }
}