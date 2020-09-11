package com.willowtreeapps.signinwithapplebutton.view

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.willowtreeapps.signinwithapplebutton.*

class SignInWithAppleButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    internal companion object {
        const val SIGN_IN_WITH_APPLE_LOG_TAG = "SIGN_IN_WITH_APPLE"
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.sign_in_with_apple_button, this, true)
    }

    private val imageView: ImageView = findViewById(R.id.imageView)
    private val textView: TextView = findViewById(R.id.textView)

    init {
        val attributes =
            context.theme.obtainStyledAttributes(attrs, R.styleable.SignInWithAppleButton, 0, R.style.SignInWithAppleButton)

        // Style
        val background = attributes.getDrawable(R.styleable.SignInWithAppleButton_android_background)
        val icon = attributes.getResourceId(R.styleable.SignInWithAppleButton_android_drawableLeft, -1)
        val iconSize = attributes.getDimensionPixelSize(R.styleable.SignInWithAppleButton_sign_in_with_apple_button_iconSize, -1)
        val textColor = attributes.getColorStateList(R.styleable.SignInWithAppleButton_android_textColor)
        val textSize = attributes.getDimensionPixelSize(R.styleable.SignInWithAppleButton_android_textSize, -1)
        val textStyle = attributes.getInt(R.styleable.SignInWithAppleButton_android_textStyle, 0)
        val fontFamily = attributes.getString(R.styleable.SignInWithAppleButton_android_fontFamily)
        val textAppearance = attributes.getResourceId(R.styleable.SignInWithAppleButton_android_textAppearance, -1)

        // Text type
        val text = attributes.getInt(
            R.styleable.SignInWithAppleButton_sign_in_with_apple_button_textType,
            SignInTextType.SIGN_IN.ordinal
        )

        // Hide or show icon
        val enableIcon = attributes.getBoolean(
            R.styleable.SignInWithAppleButton_sign_in_with_apple_button_enableIcon,
            true
        )

        // Corner radius
        val cornerRadius = attributes.getDimension(
            R.styleable.SignInWithAppleButton_sign_in_with_apple_button_cornerRadius,
            resources.getDimension(R.dimen.sign_in_with_apple_button_cornerRadius_default)
        )

        attributes.recycle()

        this.background = background?.mutate()
        (background as? GradientDrawable)?.cornerRadius = cornerRadius

        if (icon != -1 && enableIcon) {
            imageView.setImageResource(icon)

            if (iconSize != -1) {
                imageView.layoutParams.width = iconSize
                imageView.layoutParams.height = iconSize
            }
        } else {
            imageView.visibility = View.GONE
        }

        textView.setTextColor(textColor)

        if (textSize != -1) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
        }

        // set default typeface
        val typeface = if (fontFamily == null) {
            Typeface.create(textView.typeface, textStyle)
        } else {
            Typeface.create(fontFamily, textStyle)
        }

        // textView.typeface = typeface
        if (textAppearance != -1) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                textView.setTextAppearance(textAppearance)
            } else {
                // not compatible with lollipop below
                setTypeFace(typeface)
            }
        } else {
            // default
            setTypeFace(typeface)
        }

        textView.text = resources.getString(SignInTextType.values()[text].text)
    }

    fun setTypeFace(typeface: Typeface) {
        textView.typeface = typeface
    }

    fun setUpSignInWithAppleOnClick(
        fragmentManager: FragmentManager,
        configuration: SignInWithAppleConfiguration,
        callback: (SignInWithAppleResult) -> Unit
    ) {
        val fragmentTag = "SignInWithAppleButton-$id-SignInWebViewDialogFragment"
        val service = SignInWithAppleService(context, fragmentManager, fragmentTag, configuration, callback)
        setOnClickListener { service.show() }
    }

    fun setUpSignInWithAppleOnClick(
        fragmentManager: FragmentManager,
        configuration: SignInWithAppleConfiguration,
        callback: SignInWithAppleCallback
    ) {
        setUpSignInWithAppleOnClick(fragmentManager, configuration, callback.toFunction())
    }
}
