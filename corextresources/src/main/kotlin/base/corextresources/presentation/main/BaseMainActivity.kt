package base.corextresources.presentation.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import arrow.core.Try
import arrow.core.getOrElse
import base.coreresources.state.addOnLoadingListener
import base.coreresources.toplevel.onActivity
import base.corextresources.R
import base.corextresources.databinding.ActivityBaseMainBinding
import base.corextresources.domain.toplevel.navigate
import base.corextresources.presentation._baseclasses.BaseBindingActivity
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseMainActivity(
    private val enableToolbarScroll: Boolean = false
) : BaseBindingActivity<ActivityBaseMainBinding>() {
    val navController: NavController by lazy { findNavController(R.id.nav_host_fragment) }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        super.onBindingCreated(savedInstanceState)
        lifecycleScope.addOnLoadingListener { isLoading -> toggleLoading(isLoading) }
        setupNavigation()
        title = navController.currentDestination?.label
        if (!enableToolbarScroll) {
            binding.layoutToolbar.toolbar.updateLayoutParams<AppBarLayout.LayoutParams> {
                scrollFlags = 0
            }
        }
    }

    private fun setupNavigation(): Unit = with(binding) {
        lifecycleScope.launch {
            setupToolbar(layoutToolbar.toolbar)
            setupBottomNav(bottomNav)
        }
    }

    open fun setupBottomNav(bottomNav: BottomNavigationView): Unit = with(bottomNav) {
        setupWithNavController(navController)
        setOnNavigationItemSelectedListener(::onOptionsItemSelected)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item) || onMenuItemSelect(item.itemId)
    }

    open fun onMenuItemSelect(itemId: Int): Boolean = when (itemId) {
        R.id.menu_logout -> onLogout().let { true }
        R.id.menu_refresh -> onRefresh().let { true }
        else -> Try { navigate(itemId).let { true } }.getOrElse { false }
    }

    open fun onLogout() {
    }

    open fun onRefresh() {
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun getLayout(): Int = R.layout.activity_base_main

    companion object {
        operator fun invoke(block: BaseMainActivity.() -> Unit): Unit = onActivity(block)
        fun setupBottomNav(nav: BottomNavigationView): Unit = invoke { this.setupBottomNav(nav) }
    }
}
