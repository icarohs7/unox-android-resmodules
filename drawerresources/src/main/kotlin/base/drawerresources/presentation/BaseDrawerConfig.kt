package base.drawerresources.presentation

import android.graphics.Color
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.ColorUtils
import androidx.navigation.ui.setupWithNavController
import arrow.core.Try
import arrow.core.Tuple2
import base.corextresources.R
import base.corextresources.data.entities.User
import base.corextresources.presentation.main.BaseMainActivity
import base.drawerresources.domain.extensions.defaultHeader
import base.drawerresources.domain.extensions.picture
import co.zsmb.materialdrawerkt.builders.DrawerBuilderKt
import co.zsmb.materialdrawerkt.builders.accountHeader
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.draweritems.profile.profile
import com.github.icarohs7.unoxcore.extensions.capitalizeWords
import com.mikepenz.materialdrawer.Drawer
import splitties.resources.color

open class BaseDrawerConfig<T : BaseMainActivity>(
    private val builder: DrawerBuilderKt.(T) -> Unit = {}
) {
    var drawer: Drawer? = null

    fun setup(activity: T, toolbar: Toolbar? = null): Drawer? {
        drawer = with(activity) {
            val navDrawer = drawer {
                defaultSettings(this)
                builder(activity)
                setupDrawer(activity)
            }

            navController.addOnDestinationChangedListener { _, destination, _ ->
                Try { navDrawer.setSelection(destination.id.toLong(), false) }
            }

            navDrawer
        }
        toolbar?.setupWithNavController(activity.navController, drawer?.drawerLayout)
        return drawer
    }


    open fun BaseMainActivity.defaultSettings(builder: DrawerBuilderKt): Unit = with(builder) {
        val (name, email) = Tuple2(User.name, User.email)
        if (name.isNotBlank() || email.isNotBlank()) accountHeader {
            selectionListEnabled = false
            onlyMainProfileImageVisible = true
            background = R.drawable.bg_drawer_header

            val luminance = ColorUtils.calculateLuminance(color(R.color.colorPrimaryDark))
            textColor = when {
                luminance < 0.5 -> Color.WHITE.toLong()
                else -> Color.BLACK.toLong()
            }

            profile(name.capitalizeWords(), email.ifBlank { null }) {
                icon = R.drawable.ic_default_profile_128dp
                val userPicture = User.picture
                if (userPicture.isNotBlank())
                    iconUrl = userPicture
            }
        } else {
            defaultHeader()
        }

        translucentStatusBar = false
        onItemClick { _, _, item ->
            drawer?.closeDrawer()
            onMenuItemSelect(item.identifier.toInt())
            false
        }
    }

    open fun DrawerBuilderKt.setupDrawer(activity: T) {}
}