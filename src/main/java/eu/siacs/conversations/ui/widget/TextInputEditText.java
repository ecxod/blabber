package eu.siacs.conversations.ui.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * A wrapper class to fix some weird fuck ups on Meizu devices
 * credit goes to the people in this thread https://github.com/android-in-china/Compatibility/issues/11
 */
public class TextInputEditText extends com.google.android.material.textfield.TextInputEditText {

    public TextInputEditText(Context context) {
        super(context);
    }

    public TextInputEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextInputEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

}
