package com.willowtreeapps.signinwithapplebutton.view

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.willowtreeapps.signinwithapplebutton.R
import com.willowtreeapps.signinwithapplebutton.SignInWithAppleConfiguration
import com.willowtreeapps.signinwithapplebutton.SignInWithAppleResult
import com.willowtreeapps.signinwithapplebutton.SignInWithAppleService
import com.willowtreeapps.signinwithapplebutton.constants.Strings
import com.willowtreeapps.signinwithapplebutton.view.SignInWithAppleButton.Companion.SIGN_IN_WITH_APPLE_LOG_TAG
import kotlinx.android.synthetic.main.sign_in_with_apple_button_dialog.*


@SuppressLint("SetJavaScriptEnabled")
internal class SignInWebViewDialogFragment : DialogFragment() {

    companion object {
        private const val AUTHENTICATION_ATTEMPT_KEY = "authenticationAttempt"
        private const val WEB_VIEW_KEY = "webView"

        fun newInstance(authenticationAttempt: SignInWithAppleService.AuthenticationAttempt): SignInWebViewDialogFragment {
            val fragment = SignInWebViewDialogFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(AUTHENTICATION_ATTEMPT_KEY, authenticationAttempt)
            }
            return fragment
        }
    }

    private lateinit var authenticationAttempt: SignInWithAppleService.AuthenticationAttempt
    private var callback: ((SignInWithAppleResult) -> Unit)? = null
    private var config: SignInWithAppleConfiguration? = null

    private val webViewIfCreated: WebView?
        get() = view as? WebView

    fun configureCallback(callback: (SignInWithAppleResult) -> Unit) {
        this.callback = callback
    }

    fun configure(config: SignInWithAppleConfiguration) {
        this.config = config
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        authenticationAttempt = arguments?.getParcelable(AUTHENTICATION_ATTEMPT_KEY)!!
        val signInWithAppleButtonDialogtheme = R.style.sign_in_with_apple_button_DialogTheme
        setStyle(STYLE_NORMAL, signInWithAppleButtonDialogtheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val v: View = inflater.inflate(R.layout.sign_in_with_apple_button_dialog, container, false)
        val webView = v.findViewById<View>(R.id.web_view) as WebView

        // val webView: WebView? = dialog?.window?.findViewById(R.id.web_view)
        webView.settings?.javaScriptEnabled  = true
        webView.settings?.javaScriptCanOpenWindowsAutomatically = true
        webView.webViewClient =
            config?.let { SignInWebViewClient(this, it, authenticationAttempt, ::onCallback) }

        // chromium, enable hardware acceleration
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        if (savedInstanceState != null) {
            savedInstanceState.getBundle(WEB_VIEW_KEY)?.run {
                webView.restoreState(this)
            }
        } else {
            webView.loadUrl(authenticationAttempt.authenticationUri)
        }

        return v;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // finish setup toolbar
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar)
        val actionBar: ActionBar? = (activity as AppCompatActivity?)?.supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = Strings.APPlEID_TITLE
            actionBar.subtitle = "Loading..."
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(
            WEB_VIEW_KEY,
            Bundle().apply {
                webViewIfCreated?.saveState(this)
            }
        )
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(MATCH_PARENT, MATCH_PARENT)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onCallback(SignInWithAppleResult.Cancel)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                dialog?.dismiss()
                super.onOptionsItemSelected(item)
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun showProgress() {
        progress_circular?.visibility = VISIBLE
    }

    fun hideProgress() {
        progress_circular?.visibility = GONE
    }

    fun updateSubtitle(string: String) {
        val actionBar: ActionBar? = (activity as AppCompatActivity?)?.supportActionBar
        if (actionBar != null) {
            actionBar.subtitle = string
        }
    }

    // SignInWithAppleCallback

    private fun onCallback(result: SignInWithAppleResult) {
        dialog?.dismiss()
        val callback = callback
        if (callback == null) {
            Log.e(SIGN_IN_WITH_APPLE_LOG_TAG, "Callback is not configured")
            return
        }
        callback(result)
    }
}
