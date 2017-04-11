package com.ebay.park.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint.FontMetrics;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import com.ebay.park.R;
import com.ebay.park.utils.FacebookUtil;
import com.ebay.park.utils.FontsUtil;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookButtonBase;
import com.facebook.FacebookCallback;
import com.facebook.R.string;
import com.facebook.R.style;
import com.facebook.R.styleable;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.internal.CallbackManagerImpl.RequestCodeOffset;
import com.facebook.login.DefaultAudience;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.ToolTipPopup;

import java.util.Collections;
import java.util.List;

public class CustomFbLoginButton extends FacebookButtonBase {
    private static final String TAG = CustomFbLoginButton.class.getName();
    private String loginText;
    private CustomFbLoginButton.LoginButtonProperties properties = new CustomFbLoginButton.LoginButtonProperties();
    private String loginLogoutEventName = "fb_login_view_usage";
    private boolean toolTipChecked;
    private ToolTipPopup toolTipPopup;
    private AccessTokenTracker accessTokenTracker;
    private LoginManager loginManager;

    public CustomFbLoginButton(Context context) {
        super(context, (AttributeSet)null, 0, 0, "fb_login_button_create", "fb_login_button_did_tap");
    }

    public CustomFbLoginButton(Context context, AttributeSet attrs) {
        super(context, attrs, 0, 0, "fb_login_button_create", "fb_login_button_did_tap");
        this.setTypeface(FontsUtil.getFSDemi(context));
    }

    public CustomFbLoginButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle, 0, "fb_login_button_create", "fb_login_button_did_tap");
    }

    public DefaultAudience getDefaultAudience() {
        return this.properties.getDefaultAudience();
    }

    public LoginBehavior getLoginBehavior() {
        return this.properties.getLoginBehavior();
    }

    public void dismissToolTip() {
        if(this.toolTipPopup != null) {
            this.toolTipPopup.dismiss();
            this.toolTipPopup = null;
        }
    }

    public void registerCallback(CallbackManager callbackManager, FacebookCallback<LoginResult> callback) {
        this.getLoginManager().registerCallback(callbackManager, callback);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(this.accessTokenTracker != null && !this.accessTokenTracker.isTracking()) {
            this.accessTokenTracker.startTracking();
            this.setButtonText();
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(!this.toolTipChecked && !this.isInEditMode()) {
            this.toolTipChecked = true;
        }

    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.setButtonText();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(this.accessTokenTracker != null) {
            this.accessTokenTracker.stopTracking();
        }
        this.dismissToolTip();
    }

    protected void configureButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.configureButton(context, attrs, defStyleAttr, defStyleRes);
        this.setInternalOnClickListener(new CustomFbLoginButton.LoginClickListener());
        this.parseLoginButtonAttributes(context, attrs, defStyleAttr, defStyleRes);
        if(this.isInEditMode()) {
            this.loginText = getActivity().getString(R.string.facebook_login);
        } else {
            this.accessTokenTracker = new AccessTokenTracker() {
                protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                    CustomFbLoginButton.this.setButtonText();
                }
            };
        }
        this.setButtonText();
    }

    protected int getDefaultStyleResource() {
        return style.com_facebook_loginview_default_style;
    }

    private void parseLoginButtonAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, styleable.com_facebook_login_view, defStyleAttr, defStyleRes);

        try {
            this.loginText = a.getString(styleable.com_facebook_login_view_com_facebook_login_text);
        } finally {
            a.recycle();
        }

    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        FontMetrics fontMetrics = this.getPaint().getFontMetrics();
        int height = this.getCompoundPaddingTop() + (int)Math.ceil((double)(Math.abs(fontMetrics.top) + Math.abs(fontMetrics.bottom))) + this.getCompoundPaddingBottom();
        Resources resources = this.getResources();
        String text = this.loginText;
        int logInWidth;
        int width;
        if(text == null) {
            text = resources.getString(string.com_facebook_loginview_log_in_button_long);
            logInWidth = this.measureButtonWidth(text);
            width = resolveSize(logInWidth, widthMeasureSpec);
            if(width < logInWidth) {
                text = resources.getString(string.com_facebook_loginview_log_in_button);
            }
        }

        logInWidth = this.measureButtonWidth(text);
        width = resolveSize(logInWidth, widthMeasureSpec);
        this.setMeasuredDimension(width, height);
    }

    private int measureButtonWidth(String text) {
        int textWidth = this.measureTextWidth(text);
        int width = this.getCompoundPaddingLeft() + this.getCompoundDrawablePadding() + textWidth + this.getCompoundPaddingRight();
        return width;
    }

    public void setReadPermissions(List<String> permissions) {
        this.properties.setReadPermissions(permissions);
    }

    static class LoginButtonProperties {
        private DefaultAudience defaultAudience;
        private List<String> readPermissions;
        private List<String> publishPermissions;
        private LoginBehavior loginBehavior;

        LoginButtonProperties() {
            this.defaultAudience = DefaultAudience.FRIENDS;
            this.readPermissions = Collections.emptyList();
            this.publishPermissions = Collections.emptyList();
            this.loginBehavior = LoginBehavior.NATIVE_WITH_FALLBACK;
        }

        public DefaultAudience getDefaultAudience() {
            return this.defaultAudience;
        }

        public void setReadPermissions(List<String> permissions) {
            this.readPermissions = permissions;
        }

        public LoginBehavior getLoginBehavior() {
            return this.loginBehavior;
        }
    }

    protected void setButtonText() {
        this.setText(getActivity().getString(R.string.facebook_login));
    }

    protected int getDefaultRequestCode() {
        return RequestCodeOffset.Login.toRequestCode();
    }

    LoginManager getLoginManager() {
        if(this.loginManager == null) {
            this.loginManager = LoginManager.getInstance();
        }
        return this.loginManager;
    }

    private class LoginClickListener implements OnClickListener {
        private LoginClickListener() {
        }

        public void onClick(View v) {
            CustomFbLoginButton.this.callExternalOnClickListener(v);
            if (!FacebookUtil.getIsFacebookLoggedInAlready(CustomFbLoginButton.this.getActivity())) {
                AccessToken accessToken = AccessToken.getCurrentAccessToken();

                LoginManager logger1 = CustomFbLoginButton.this.getLoginManager();
                logger1.setDefaultAudience(CustomFbLoginButton.this.getDefaultAudience());
                logger1.setLoginBehavior(CustomFbLoginButton.this.getLoginBehavior());
                if (CustomFbLoginButton.this.getFragment() != null) {
                    if (properties.publishPermissions.size() > 0) {
                        logger1.logInWithPublishPermissions(CustomFbLoginButton.this.getFragment(), CustomFbLoginButton.this.properties.publishPermissions);
                    } else {
                        logger1.logInWithReadPermissions(CustomFbLoginButton.this.getFragment(), CustomFbLoginButton.this.properties.readPermissions);
                    }
                } else {
                    if (properties.publishPermissions.size() > 0) {
                        logger1.logInWithPublishPermissions(CustomFbLoginButton.this.getActivity(), CustomFbLoginButton.this.properties.publishPermissions);
                    } else {
                        logger1.logInWithReadPermissions(CustomFbLoginButton.this.getActivity(), CustomFbLoginButton.this.properties.readPermissions);
                    }
                }

                AppEventsLogger logger2 = AppEventsLogger.newLogger(CustomFbLoginButton.this.getContext());
                Bundle parameters1 = new Bundle();
                parameters1.putInt("logging_in", accessToken != null ? 0 : 1);
                logger2.logSdkEvent(CustomFbLoginButton.this.loginLogoutEventName, (Double) null, parameters1);
            }
        }
    }

    public static enum ToolTipMode {
        AUTOMATIC("automatic", 0),
        DISPLAY_ALWAYS("display_always", 1),
        NEVER_DISPLAY("never_display", 2);

        public static CustomFbLoginButton.ToolTipMode DEFAULT;
        private String stringValue;
        private int intValue;

        public static CustomFbLoginButton.ToolTipMode fromInt(int enumValue) {
            CustomFbLoginButton.ToolTipMode[] var1 = values();
            int var2 = var1.length;

            for(int var3 = 0; var3 < var2; ++var3) {
                CustomFbLoginButton.ToolTipMode mode = var1[var3];
                if(mode.getValue() == enumValue) {
                    return mode;
                }
            }

            return null;
        }

        private ToolTipMode(String stringValue, int value) {
            this.stringValue = stringValue;
            this.intValue = value;
        }

        public String toString() {
            return this.stringValue;
        }

        public int getValue() {
            return this.intValue;
        }

        static {
            DEFAULT = AUTOMATIC;
        }
    }
}
