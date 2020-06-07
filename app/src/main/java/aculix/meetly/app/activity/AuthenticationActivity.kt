package aculix.meetly.app.activity

import aculix.core.extensions.makeGone
import aculix.core.extensions.makeVisible
import aculix.core.extensions.toast
import aculix.meetly.app.R
import aculix.meetly.app.databinding.ActivityAuthenticationBinding
import aculix.meetly.app.sharedpref.AppPref
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse

class AuthenticationActivity : AppCompatActivity() {

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, AuthenticationActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var binding: ActivityAuthenticationBinding
    private val rcSignIn = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showSkipSignIn()

        onSignInWithGoogleClick()
        onSignInWithEmailClick()
        onSkipClick()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // RC_SIGN_IN is the request code you passed when starting the sign in flow.
        if (requestCode == rcSignIn) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                AppPref.isUserAuthenticated = true
                MainActivity.startActivity(this)
                finish()
                return
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    toast(getString(R.string.authentication_login_canceled))
                    return
                }
                if (response.error!!.errorCode == ErrorCodes.NO_NETWORK) {
                    toast(getString(R.string.all_error_internet_connectivity))
                    return
                }
                if (response.error!!.errorCode == ErrorCodes.UNKNOWN_ERROR) {
                    toast(getString(R.string.all_error_unknown))
                    return
                }
            }
        }
    }

    /**
     * Sets the visibility of the chipSkip based on the mandatory authentication configuration
     */
    private fun showSkipSignIn() {
        if (resources.getBoolean(R.bool.enable_mandatory_authentication))
            binding.chipSkip.makeGone() else binding.chipSkip.makeVisible()
    }

    private fun onSignInWithGoogleClick() {
        binding.btnSignInGoogle.setOnClickListener {
            startSignInFlow(AuthUI.IdpConfig.GoogleBuilder().build())
        }
    }

    private fun onSignInWithEmailClick() {
        binding.btnSignInEmail.setOnClickListener {
            startSignInFlow(AuthUI.IdpConfig.EmailBuilder().build())
        }
    }

    private fun onSkipClick() {
        binding.chipSkip.setOnClickListener {
            MainActivity.startActivity(this)
            finish()
        }
    }

    private fun startSignInFlow(idpConfig: AuthUI.IdpConfig) {
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(
                    listOf(
                        idpConfig
                    )
                )
                .setTheme(R.style.AppTheme)
                .setIsSmartLockEnabled(false)
                .build(),
            rcSignIn
        )
    }
}
