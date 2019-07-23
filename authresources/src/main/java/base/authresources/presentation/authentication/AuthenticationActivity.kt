package base.authresources.presentation.authentication

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.core.view.children
import androidx.core.view.isGone
import base.authresources.R
import base.authresources.databinding.ActivityAuthenticationBinding
import base.authresources.databinding.PartialSwitchButtonBinding
import base.authresources.domain.AuthenticationType
import base.authresources.domain.LoginHandler
import base.authresources.domain.Suspend
import base.authresources.presentation.fragments.EmailLoginFragment
import base.authresources.presentation.fragments.PinLoginFragment
import base.authresources.presentation.fragments.UserPassLoginFragment
import base.corelibrary.presentation.CoreNavigation
import com.github.icarohs7.unoxandroidarch.presentation.activities.BaseBindingActivity
import com.github.icarohs7.unoxandroidarch.toplevel.FlashBar
import splitties.views.onClick
import splitties.views.textResource
import kotlin.reflect.KClass

abstract class AuthenticationActivity(
        private val appVersion: String = "v1.00"
) : BaseBindingActivity<ActivityAuthenticationBinding>() {
    val handlers: MutableMap<KClass<out AuthenticationType>, LoginHandler> = mutableMapOf()

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        super.onBindingCreated(savedInstanceState)
        binding.stateView.hideStates()
        binding.txtVersion.text = appVersion
    }

    override fun afterInitialSetup() {
        super.afterInitialSetup()
        binding.layoutButtonPanel.children.firstOrNull()?.performClick()
    }

    /**
     * Entry point of the DSL to add authentication
     * methods to the activity
     */
    fun setupLogin(block: LoginBuilder.() -> Unit): Unit = block(LoginBuilder)

    /**
     * Type used to hold the DSL functions adding
     * authentication methods to the activity
     */
    object LoginBuilder

    /**
     * Add email authentication to the activity
     */
    @Suppress("unused")
    fun LoginBuilder.email(addSwitchButton: Boolean = true, handler: Suspend<AuthenticationType.Email>) {
        handlers[AuthenticationType.Email::class] = { if (it is AuthenticationType.Email) (handler(it)) }
        addButton(R.string.email, addSwitchButton) { loadFragment(EmailLoginFragment()) }
    }

    /**
     * Add pin authentication to the activity
     */
    @Suppress("unused")
    fun LoginBuilder.pin(pinLength: Int, addSwitchButton: Boolean = true, handler: Suspend<AuthenticationType.Pin>) {
        handlers[AuthenticationType.Pin::class] = { if (it is AuthenticationType.Pin) (handler(it)) }
        addButton(R.string.pin, addSwitchButton) { loadFragment(PinLoginFragment.create(pinLength)) }
    }

    /**
     * Add user with password authentication to the activity
     */
    @Suppress("unused")
    fun LoginBuilder.userPassword(addSwitchButton: Boolean = true, handler: Suspend<AuthenticationType.UserPass>) {
        handlers[AuthenticationType.UserPass::class] = { if (it is AuthenticationType.UserPass) (handler(it)) }
        addButton(R.string.login_e_senha, addSwitchButton) { loadFragment(UserPassLoginFragment()) }
    }

    /**
     * Called when the user authentication is successful
     */
    open suspend fun onLoginSuccess(type: AuthenticationType) {
        CoreNavigation.mainActivity()
    }

    /**
     * Called when an authentication error happens
     */
    open suspend fun onLoginError(type: AuthenticationType) {
        showDefaultAuthErrorMessage()
    }

    /**
     * Show the default message used to tell the user
     * the authentication process failed
     */
    fun showDefaultAuthErrorMessage() {
        FlashBar.error(
                "Ocorreu um erro ao tentar se autenticar, verifique suas credenciais e sua conexão.",
                duration = 3500
        )
    }

    /**
     * Helper used to add the button to switch
     * to a given authentication method
     */
    fun addButton(
            @StringRes buttonTextRes: Int,
            isVisible: Boolean = true,
            navigationHandler: () -> Unit
    ): Unit = with(binding) {
        val button = PartialSwitchButtonBinding.inflate(layoutInflater, layoutButtonPanel, true).btnSwitchLoginMode
        with(button) {
            textResource = buttonTextRes
            onClick(navigationHandler)
            isGone = !isVisible
        }
    }

    override fun getLayout(): Int {
        return R.layout.activity_authentication
    }
}