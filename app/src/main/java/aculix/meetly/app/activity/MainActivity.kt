package aculix.meetly.app.activity

import aculix.core.extensions.*
import aculix.meetly.app.Meetly
import aculix.meetly.app.R
import aculix.meetly.app.databinding.ActivityMainBinding
import aculix.meetly.app.model.Meeting
import aculix.meetly.app.sharedpref.AppPref
import aculix.meetly.app.utils.MeetingUtils
import aculix.meetly.app.viewmodel.MainViewModel
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.widget.doOnTextChanged
import coil.api.load
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.customview.customView
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.dialog_profile.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    companion object {
        fun startActivity(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val viewModel by viewModel<MainViewModel>() // Lazy inject ViewModel
    private lateinit var binding: ActivityMainBinding

    private val minMeetingCodeLength = 10
    private var currentUser: FirebaseUser? = null
    private lateinit var createMeetingInterstitialAd: InterstitialAd
    private lateinit var joinMeetingInterstitialAd: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUser = FirebaseAuth.getInstance().currentUser
        setProfileIcon()

        // Load ads based on configuration
        if (Meetly.isAdEnabled) {
            initializeCreateMeetingInterstitialAd()
            loadCreateMeetingInterstitialAd()
            initializeJoinMeetingInterstitialAd()
            loadJoinMeetingInterstitialAd()
        }

        onMeetingToggleChange()
        onCreateMeetingCodeChange()
        onCopyMeetingCodeFromClipboardClick()
        onShareMeetingCodeClick()
        onJoinMeetingClick()
        onCreateMeetingClick()
        onMeetingHistoryClick()
        onProfileClick()
    }

    private fun setProfileIcon() {
        if (currentUser != null) {
            binding.ivProfile.load(currentUser?.photoUrl)
        }
    }

    private fun initializeCreateMeetingInterstitialAd() {
        createMeetingInterstitialAd = InterstitialAd(this)
        createMeetingInterstitialAd.adUnitId = getString(R.string.interstitial_ad_id_create_meeting)

        // Reload ad once shown
        createMeetingInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                loadCreateMeetingInterstitialAd()
                createMeeting()
            }
        }
    }

    private fun loadCreateMeetingInterstitialAd() {
        createMeetingInterstitialAd.loadAd(AdRequest.Builder().build())
    }

    private fun initializeJoinMeetingInterstitialAd() {
        joinMeetingInterstitialAd = InterstitialAd(this)
        joinMeetingInterstitialAd.adUnitId = getString(R.string.interstitial_ad_id_join_meeting)

        // Reload ad once shown
        joinMeetingInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                loadJoinMeetingInterstitialAd()
                joinMeeting()
            }
        }
    }

    private fun loadJoinMeetingInterstitialAd() {
        joinMeetingInterstitialAd.loadAd(AdRequest.Builder().build())
    }

    /**
     * Called when the meeting toggle button check state is changed
     */
    private fun onMeetingToggleChange() {
        binding.tgMeeting.addOnButtonCheckedListener { toggleGroup, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnToggleJoinMeeting -> {
                        binding.groupCreateMeeting.makeGone()
                        binding.groupJoinMeeting.makeVisible()
                    }
                    R.id.btnToggleCreateMeeting -> {
                        binding.groupJoinMeeting.makeGone()
                        binding.groupCreateMeeting.makeVisible()
                        val meetingCode = generateMeetingCode()
                        binding.etCodeCreateMeeting.setText(meetingCode)
                    }
                }
            }
        }
    }

    /**
     * Called when the meeting code in the EditText of the CREATE MEETING toggle changes
     */
    private fun onCreateMeetingCodeChange() {
        binding.tilCodeCreateMeeting.etCodeCreateMeeting.doOnTextChanged { text, start, before, count ->
            if (count >= minMeetingCodeLength) binding.tilCodeCreateMeeting.error = null
        }
    }

    private fun generateMeetingCode(): String {
        val allowedChars = ('A'..'Z') + ('a'..'z')
        return (1..10)
            .map { allowedChars.random() }
            .joinToString("")
    }

    /**
     * Called when the clipboard icon is clicked in the EditText of the JOIN MEETING toggle
     */
    private fun onCopyMeetingCodeFromClipboardClick() {
        binding.tilCodeJoinMeeting.setEndIconOnClickListener {
            val clipboardText = getTextFromClipboard()
            if (clipboardText != null) {
                binding.etCodeJoinMeeting.setText(clipboardText)
                toast(getString(R.string.main_meeting_code_copied))
            } else {
                toast(getString(R.string.main_empty_clipboard))
            }
        }
    }

    /**
     * Called when the share icon is clicked in the EditText of the CREATE MEETING toggle
     */
    private fun onShareMeetingCodeClick() {
        binding.tilCodeCreateMeeting.setEndIconOnClickListener {
            if (binding.etCodeCreateMeeting.text.toString().length >= minMeetingCodeLength) {
                binding.tilCodeCreateMeeting.error = null
                startShareTextIntent(
                    getString(R.string.main_share_meeting_code_title),
                    binding.etCodeCreateMeeting.text.toString()
                )
            } else {
                binding.tilCodeCreateMeeting.error =
                    getString(R.string.main_error_meeting_code_length, minMeetingCodeLength)
            }
        }
    }

    /**
     * Called when the JOIN button is clicked of the JOIN MEETING toggle
     */
    private fun onJoinMeetingClick() {
        binding.btnJoinMeeting.setOnClickListener {
            if (binding.etCodeJoinMeeting.text.toString().length >= minMeetingCodeLength) {
                if (Meetly.isAdEnabled) {
                    if (joinMeetingInterstitialAd.isLoaded) joinMeetingInterstitialAd.show() else joinMeeting()
                } else {
                    joinMeeting()
                }
            } else {
                toast(getString(R.string.main_error_meeting_code_length, minMeetingCodeLength))
            }
        }
    }

    private fun joinMeeting() {
        MeetingUtils.startMeeting(
            this,
            binding.etCodeJoinMeeting.text.toString(),
            R.string.all_joining_meeting
        ) // Start Meeting

        viewModel.addMeetingToDb(
            Meeting(
                binding.etCodeJoinMeeting.text.toString(),
                System.currentTimeMillis()
            )
        ) // Add meeting to db
    }

    /**
     * Called when the CREATE button is clicked of the CREATE MEETING toggle
     */
    private fun onCreateMeetingClick() {
        binding.btnCreateMeeting.setOnClickListener {
            if (binding.etCodeCreateMeeting.text.toString().length >= minMeetingCodeLength) {
                if (Meetly.isAdEnabled) {
                    if (createMeetingInterstitialAd.isLoaded) createMeetingInterstitialAd.show() else createMeeting()
                } else {
                    createMeeting()
                }
            } else {
                toast(getString(R.string.main_error_meeting_code_length, minMeetingCodeLength))
            }
        }
    }

    private fun createMeeting() {
        MeetingUtils.startMeeting(
            this,
            binding.etCodeCreateMeeting.text.toString(),
            R.string.all_creating_meeting
        ) // Start Meeting

        viewModel.addMeetingToDb(
            Meeting(
                binding.etCodeCreateMeeting.text.toString(),
                System.currentTimeMillis()
            )
        ) // Add meeting to db
    }

    private fun onMeetingHistoryClick() {
        binding.ivMeetingHistory.setOnClickListener {
            MeetingHistoryActivity.startActivity(this)
        }
    }

    private fun onProfileClick() {
        binding.ivProfile.setOnClickListener {
            val profileDialog = MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
                customView(R.layout.dialog_profile)
            }

            profileDialog.apply {
                if (currentUser != null) {
                    ivUserProfileDialog.load(currentUser!!.photoUrl)
                    tvUserName.text = currentUser!!.displayName
                    tvEmail.text = currentUser!!.email
                    btnUserAuthenticationStatus.text = getString(R.string.all_btn_sign_out)
                } else {
                    tvUserName.makeGone()
                    tvEmail.makeGone()
                    tvUserNotAuthenticated.makeVisible()
                    btnUserAuthenticationStatus.text = getString(R.string.all_btn_sign_in)
                }

                switchDarkMode.isChecked = !AppPref.isLightThemeEnabled

                // UserAuthenticationStatus button onClick
                btnUserAuthenticationStatus.setOnClickListener {
                    dismiss()

                    if (currentUser != null) {
                        // User is currently signed in
                        AuthUI.getInstance().signOut(this@MainActivity).addOnCompleteListener {
                            AuthenticationActivity.startActivity(this@MainActivity)
                            finish()
                        }
                    } else {
                        // User is not signed in
                        AuthenticationActivity.startActivity(this@MainActivity)
                        finish()
                    }
                }

                // Dark Mode Switch
                switchDarkMode.setOnCheckedChangeListener { compoundButton, isChecked ->
                    dismiss()

                    // Change theme after dismiss to prevent memory leak
                    onDismiss {
                        if (isChecked) setThemeMode(AppCompatDelegate.MODE_NIGHT_YES) else setThemeMode(
                            AppCompatDelegate.MODE_NIGHT_NO
                        )
                    }
                }

                // Send feedback onClick
                tvSendFeedback.setOnClickListener {
                    startEmailIntent(
                        getString(R.string.app_feedback_contact_email),
                        getString(R.string.profile_feedback_email_subject)
                    )
                }

                // Rate app onClick
                tvRateApp.setOnClickListener {
                    openAppInGooglePlay(applicationContext.packageName, R.color.colorSurface)
                }

                // Share app onClick
                tvShareApp.setOnClickListener {
                    startShareTextIntent(
                        getString(R.string.profile_share_app_title),
                        getString(R.string.profile_share_app_text, applicationContext.packageName)
                    )
                }

                // Open Source Licenses onClick
                tvOpenSourceLicenses.setOnClickListener {
                    startActivity(Intent(this@MainActivity, OssLicensesMenuActivity::class.java))
                }

                // Privacy Policy onClick
                tvPrivacyPolicy.setOnClickListener {
                    openUrl(getString(R.string.app_privacy_policy_url), R.color.colorSurface)
                }

                // Terms of Service onClick
                tvTermsOfService.setOnClickListener {
                    openUrl(getString(R.string.app_terms_of_service_url), R.color.colorSurface)
                }
            }
        }
    }

    private fun setThemeMode(themeMode: Int) {
        AppCompatDelegate.setDefaultNightMode(themeMode)
        AppPref.isLightThemeEnabled = themeMode == AppCompatDelegate.MODE_NIGHT_NO
    }
}
