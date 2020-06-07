package aculix.core.extensions


import aculix.core.R
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import saschpe.android.customtabs.CustomTabsHelper
import saschpe.android.customtabs.WebViewFallback
import java.io.File


/**
 * Relaunches the current activity
 */
fun Context.relaunchActivity(context: Activity, intent: Intent) {
    context.finish()
    startActivity(intent)
}

/**
 * Shows the toast message
 */
fun Context.toast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

/**
 * Opens the passed URL in the Chrome Custom Tabs
 */
fun Context.openUrl(url: String, @ColorRes toolbarColor: Int) {
    val customTabsIntent = CustomTabsIntent.Builder()
        .addDefaultShareMenuItem()
        .setToolbarColor(ContextCompat.getColor(this, toolbarColor))
        .setShowTitle(true)
        .build()

    // This is optional but recommended
    CustomTabsHelper.addKeepAliveExtra(this, customTabsIntent.intent)

    CustomTabsHelper.openCustomTab(
        this,
        customTabsIntent,
        Uri.parse(url),
        WebViewFallback() // Opens in system browser if Chrome isn't installed on device
    )
}

fun Context.openAppInGooglePlay(appPackageName: String, @ColorRes toolbarColor: Int) {
    try {
        // Try to open in the Google Play app
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
    } catch (exception: android.content.ActivityNotFoundException) {
        // Google Play app is not installed. Open URL in the browser.
        openUrl("https://play.google.com/store/apps/details?id=$appPackageName", toolbarColor)
    }
}

fun Context.copyTextToClipboard(textToCopy: String, textCopiedMessage: String) {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("Copied Text", textToCopy)
    clipboardManager.setPrimaryClip(clipData)
    toast(textCopiedMessage)
}

fun Context.getTextFromClipboard(): CharSequence? {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return clipboardManager.primaryClip?.getItemAt(0)?.text
}

fun Context.startEmailIntent(toEmail: String, emailSubject: String?, emailBody: String = "") {
    // Open Email app with subject and to field pre-filled
    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
        type = "message/rfc822"
        val uriText = String.format(
            "mailto:%s?subject=%s&body=%s",
            toEmail, emailSubject, emailBody
        )
        data = Uri.parse(uriText)
    }

    // Check if an Email app is installed on the device
    if (emailIntent.resolveActivity(packageManager) != null) {
        startActivity(emailIntent)
    } else {
        toast(getString(R.string.text_no_email_app_found))
    }
}

/**
 * Starts an Intent for sharing text
 */
fun Context.startShareTextIntent(shareTitle: String, shareText: String) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    startActivity(Intent.createChooser(shareIntent, shareTitle))
}

/**
 * Starts an Intent for sharing an image
 */
fun Context.startShareImageIntent(uri: Uri) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_STREAM, uri)
        type = "image/*"
    }
    // Launch sharing dialog for image
    startActivity(Intent.createChooser(shareIntent, "Share Image"))
}

/**
 * Start an Intent for sharing multiple images
 */
fun Context.startShareImageIntent(uriList: ArrayList<Uri>) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND_MULTIPLE
        type = "image/*"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
    }

    startActivity(Intent.createChooser(shareIntent, getString(R.string.text_share_all_images)))
}
