package com.ebay.park.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.activities.LoginActivity;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.flow.IntentFactory;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.interfaces.OnShowListenerLollipop;
import com.ebay.park.interfaces.OnShowListenerPreLollipop;
import com.ebay.park.model.LoginModel;
import com.ebay.park.requests.BaseParkHttpRequest;
import com.ebay.park.requests.LoginRequest;
import com.ebay.park.responses.ResponseCodes;
import com.ebay.park.utils.Constants;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.DialogUtils;
import com.ebay.park.utils.GCMUtils;
import com.ebay.park.utils.KeyboardHelper;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.PreferencesUtil;
import com.ebay.park.utils.SwrveEvents;
import com.ebay.park.utils.Validations;
import com.ebay.park.views.ButtonDemi;
import com.ebay.park.views.EditTextBook;
import com.ebay.park.views.TextViewBook;
import com.globant.roboneck.common.NeckSpiceManager;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvingResultCallbacks;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.swrve.sdk.SwrveSDK;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles standard app login.
 *
 * @author Nicolás Matias Fernández
 */
public class StandardLoginFragment extends BaseFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "STANDARD_LOGIN_FRAGMENT_TAG";
    public static final String TEST_TAG = StandardLoginFragment.class.getSimpleName();
    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final int RC_SAVE = 1;
    private static final int RC_HINT = 2;
    private static final int RC_READ = 3;

    private EditTextBook mUsernameEditText;
    private EditText mPasswordEditText;
    private ButtonDemi mLoginButton;
    private TextViewBook mLoginFailedView;
    private TextViewBook mInvalidMailView;
    private String mDeviceId;
    private String mUniqueDeviceId;
    private TextView mSetPassword;
    private TextView mRegister;
    private TextViewBook mTermsLink;
    private ImageView mPassVisibility;
    private Boolean mPasswordVisible = false;
    private GoogleApiClient mCredentialsApiClient;
    private Credential mCurrentCredential;
    private boolean mIsResolving = false;
    Tracker mGAnalyticsTracker;

    @Override
    public void onStart() {
        super.onStart();
        mSpiceManager.addListenerIfPending(buildLoginRequest(), new LoginRequestListener());
        // Attempt auto-sign in.
        if (!mIsResolving) {
            requestCredentials();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.login_with_cred);
        mCredentialsApiClient.connect();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_RESOLVING, mIsResolving);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (GCMUtils.checkPlayServices(getBaseActivity())) {
            mDeviceId = GCMUtils.getRegistrationId(getBaseActivity());

            if (mDeviceId.isEmpty()) {
                new Thread(new Runnable() {
                    public void run() {
                        mDeviceId = GCMUtils.registerOnGCM(getBaseActivity());
                    }
                }).start();
            }
        } else {
            Logger.info("No valid Google Play Services APK found.");
        }

        // Instance state
        if (savedInstanceState != null) {
            mIsResolving = savedInstanceState.getBoolean(KEY_IS_RESOLVING);
        }

        mCredentialsApiClient = new GoogleApiClient.Builder(getBaseActivity())
                .addConnectionCallbacks(this)
                .enableAutoManage(getBaseActivity(), this)
                .addApi(Auth.CREDENTIALS_API)
                .build();

        mUniqueDeviceId = DeviceUtils.getUniqueDeviceId(getBaseActivity());
        SwrveSDK.event(SwrveEvents.LOGIN_BEGIN);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsResolving = true;
        mCredentialsApiClient.stopAutoManage(getBaseActivity());
        mCredentialsApiClient.disconnect();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup parentView = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        View baseview = inflater.inflate(R.layout.fragment_standard_login, parentView, true);

        getBaseActivity().getSupportActionBar().show();

        mUsernameEditText = (EditTextBook) baseview.findViewById(R.id.et_username);
        mPasswordEditText = (EditText) baseview.findViewById(R.id.et_password);
        mLoginFailedView = (TextViewBook) baseview.findViewById(R.id.tv_login_failed);
        mInvalidMailView = (TextViewBook) baseview.findViewById(R.id.tv_email_invalid);
        mSetPassword = (TextViewBook) baseview.findViewById(R.id.tv_set_password);
        mRegister = (TextViewBook) baseview.findViewById(R.id.tv_register);
        mPassVisibility = (ImageView) baseview.findViewById(R.id.iv_show_hide_pass);
        mTermsLink = (TextViewBook) baseview.findViewById(R.id.tvTermsLink);

        mPassVisibility.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPasswordVisible) {
                    mPassVisibility.setImageDrawable(getResources().getDrawable(R.drawable.icon_eye_show));
                    mPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());
                    mPasswordVisible = false;
                } else {
                    mPassVisibility.setImageDrawable(getResources().getDrawable(R.drawable.icon_eye_hide));
                    mPasswordEditText.setTransformationMethod(null);
                    mPasswordVisible = true;
                }
            }
        });

        ClicksListener listener = new ClicksListener();
        mSetPassword.setOnClickListener(listener);
        mRegister.setOnClickListener(listener);

        mLoginButton = (ButtonDemi) baseview.findViewById(R.id.btn_login);
        mLoginButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                SwrveSDK.event(SwrveEvents.LOGIN_ATTEMPT);
                doParkLogin();
            }
        });


        mTermsLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(IntentFactory.getTermsConditions());
            }
        });

        mUsernameEditText.setFilters(new InputFilter[]{Constants.EMAIL_FILTER});
        mUsernameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    mInvalidMailView.setVisibility(View.GONE);
                    if (!hasFocus && !Validations.validateEmail(mUsernameEditText)) {
                        mInvalidMailView.setVisibility(View.VISIBLE);
                    } else {
                        mInvalidMailView.setVisibility(View.GONE);
                    }
                }
            }
        });

        mPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mLoginButton.performClick();
                    return true;
                }
                return false;
            }
        });

        mPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    clearError();
                }
            }
        });

        setHasOptionsMenu(true);

        return baseview;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean isEventConsumed = false;
        if (item.getItemId() == android.R.id.home) {
            KeyboardHelper.hide(getBaseActivity(), getView());
            SwrveSDK.event(SwrveEvents.LOGIN_CANCEL);
            isEventConsumed = backToLogin();
        }
        return isEventConsumed;
    }

    private boolean backToLogin() {
        boolean hasStackedFragments = !LoginActivity.hasStackedFragments(getFragmentManager());
        if (hasStackedFragments) {
            ScreenManager.showLoginScreen(getBaseActivity());
            getActivity().finish();
        }
        return hasStackedFragments;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        disableRefreshSwipe();
        mGAnalyticsTracker = ParkApplication.getInstance().getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER);
        // Build and send "Did begin log in with credentials" Event.
        mGAnalyticsTracker.send(new HitBuilders.EventBuilder().setCategory("User action")
                .setAction("Did begin log in with credentials").setLabel("The user entered in log in with credentials screen").build());
    }

    private boolean isCredentialsFormatOk() {

        final String username = mUsernameEditText.getText().toString().trim();
        final String password = mPasswordEditText.getText().toString().trim();

        boolean areCredentialsValid = true;
        boolean showErrorMessage = false;

        if (TextUtils.isEmpty(username)) {
            areCredentialsValid = false;
            mInvalidMailView.setVisibility(View.VISIBLE);
            showErrorMessage = true;
        } else {
            if (!Validations.validateEmail(mUsernameEditText)) {
                areCredentialsValid = false;
                mInvalidMailView.setVisibility(View.VISIBLE);
                showErrorMessage = false;
            } else {
                mInvalidMailView.setVisibility(View.GONE);
            }
        }
        if (TextUtils.isEmpty(password)) {
            areCredentialsValid = false;
            showErrorMessage = true;
        }

        if (showErrorMessage) {
            MessageUtil.showError(getBaseActivity(), R.string.all_mandatory_fields,
                    getBaseActivity().getCroutonsHolder());
        }

        return areCredentialsValid;
    }

    private BaseParkHttpRequest<LoginModel> buildLoginRequest() {

        String usernameInput = mUsernameEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();
        String email = "";
        String username = "";
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(usernameInput).matches()) {
            email = usernameInput;
        } else {
            username = usernameInput;
        }
        return new LoginRequest(username, password, email, mDeviceId, mUniqueDeviceId);

    }

    private void showError(String message) {
        mLoginFailedView.setText(message);
        mLoginFailedView.setVisibility(View.VISIBLE);
    }

    private void clearError() {
        mLoginFailedView.setText("");
        mLoginFailedView.setVisibility(View.GONE);
    }

    private void doParkLogin() {
        if (isCredentialsFormatOk()) {
            clearError();
            if (NeckSpiceManager.isDeviceOnline(getBaseActivity())) {
                mSpiceManager.executeCacheRequestWithProgress(buildLoginRequest(), new LoginRequestListener());
            } else {
                MessageUtil.showError(getBaseActivity(), getBaseActivity().getString(R.string.no_internet),
                        getBaseActivity().getCroutonsHolder());
            }
        }
    }

    @Override
    protected void showProgress() {
        try {
            super.showProgress();
            mUsernameEditText.setEnabled(false);
            mPasswordEditText.setEnabled(false);
            mLoginButton.setEnabled(false);
        } catch (NullPointerException e) {
        }
    }

    @Override
    protected void hideProgress() {
        try {
            super.hideProgress();
            mUsernameEditText.setEnabled(true);
            mPasswordEditText.setEnabled(true);
            mLoginButton.setEnabled(true);
        } catch (NullPointerException e) {
        }
    }

    @Override
    public void onBackPressed() {
        SwrveSDK.event(SwrveEvents.LOGIN_CANCEL);
    }


    //SMART LOCK CODE:******************************************************************************
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        hideProgress();

        switch (requestCode) {
            case RC_HINT:
                // Drop into handling for RC_READ
            case RC_READ:
                if (resultCode == getBaseActivity().RESULT_OK) {
                    boolean isHint = (requestCode == RC_HINT);
                    Credential credential = data.getParcelableExtra(Credential.EXTRA_KEY);
                    processRetrievedCredential(credential, isHint, true);
                } else {
                    mUsernameEditText.setText("");
                    mPasswordEditText.setText("");
                }

                mIsResolving = false;
                break;
            case RC_SAVE:
                if (resultCode == getBaseActivity().RESULT_OK) {
                    getBaseActivity().finish();
                } else {
                    getBaseActivity().finish();
                }

                mIsResolving = false;
                break;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    /**
     * Called when the save button is clicked.  Reads the entries in the email and password
     * fields and attempts to save a new Credential to the Credentials API.
     */
    private void saveCredentialClicked() {
        String email = mUsernameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();

        // Create a Credential with the user's email as the ID and storing the password.  We
        // could also add 'Name' and 'ProfilePictureURL' but that is outside the scope of this
        // minimal sample.
        final Credential credential = new Credential.Builder(email)
                .setPassword(password)
                .build();

        // NOTE: this method unconditionally saves the Credential built, even if all the fields
        // are blank or it is invalid in some other way.  In a real application you should contact
        // your app's back end and determine that the credential is valid before saving it to the
        // Credentials backend.
        if(mCredentialsApiClient.isConnected()){
            showProgress();
            Auth.CredentialsApi.save(mCredentialsApiClient, credential).setResultCallback(
                    new ResolvingResultCallbacks<Status>(getBaseActivity(), RC_SAVE) {
                        @Override
                        public void onSuccess(Status status) {
                            getBaseActivity().finish();
                            hideProgress();
                        }

                        @Override
                        public void onUnresolvableFailure(Status status) {
                            getBaseActivity().finish();
                            hideProgress();
                        }
                    });
        }
    }

    private class ClicksListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_set_password:
                    ScreenManager.showSetPassword((BaseActivity) getActivity());
                    break;
                case R.id.tv_register:
                    showRegistrationScreen();
                    break;
                default:
                    Logger.warn("Unknown click");
                    break;
            }
        }
    }

    /**
     * Request Credentials from the Credentials API.
     */
    private void requestCredentials() {
        // Request all of the user's saved username/password credentials.  We are not using
        // setAccountTypes so we will not load any credentials from other Identity Providers.
        CredentialRequest request = new CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .build();

        showProgress();

        Auth.CredentialsApi.request(mCredentialsApiClient, request).setResultCallback(
                new ResultCallback<CredentialRequestResult>() {
                    @Override
                    public void onResult(CredentialRequestResult credentialRequestResult) {
                        hideProgress();
                        Status status = credentialRequestResult.getStatus();
                        if (status.isSuccess()) {
                            // Successfully read the credential without any user interaction, this
                            // means there was only a single credential and the user has auto
                            // sign-in enabled.
                            processRetrievedCredential(credentialRequestResult.getCredential(), false, false);
                        } else if (status.getStatusCode() == CommonStatusCodes.RESOLUTION_REQUIRED) {
                            // This is most likely the case where the user has multiple saved
                            // credentials and needs to pick one
                            resolveResult(status, RC_READ);
                        } else if (status.getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED) {
                            // This means only a hint is available, but we are handling that
                            // elsewhere so no need to act here.
                        }
                    }
                });
    }

    /**
     * Attempt to resolve a non-successful Status from an asynchronous request.
     *
     * @param status      the Status to resolve.
     * @param requestCode the request code to use when starting an Activity for result,
     *                    this will be passed back to onActivityResult.
     */
    private void resolveResult(Status status, int requestCode) {
        // We don't want to fire multiple resolutions at once since that can result
        // in stacked dialogs after rotation or another similar event.
        if (mIsResolving) {
            return;
        }

        if (status.hasResolution()) {
            try {
                status.startResolutionForResult(getBaseActivity(), requestCode);
                mIsResolving = true;
            } catch (IntentSender.SendIntentException e) {
                hideProgress();
            }
        } else {
            hideProgress();
        }
    }

    /**
     * Process a Credential object retrieved from a successful request.
     *
     * @param credential the Credential to process.
     * @param isHint     true if the Credential is hint-only, false otherwise.
     */
    private void processRetrievedCredential(Credential credential, boolean isHint, boolean multipleAcc) {

        // If the Credential is not a hint, we should store
        if (!isHint) {
            mCurrentCredential = credential;
        }

        mUsernameEditText.setText(credential.getId());
        mPasswordEditText.setText(credential.getPassword());

        if(multipleAcc){
            doParkLogin();
        }

    }
    //END SMART LOCK CODE:**************************************************************************

    /**
     * Listener for login responses from Park server.
     */
    private class LoginRequestListener extends BaseNeckRequestListener<LoginModel> {

        @Override
        public void onRequestSuccessfull(LoginModel login) {
            if (login != null) {
                SwrveSDK.event(SwrveEvents.LOGIN_SUCCESS);
                ParkApplication.getInstance().setSessionToken(login.getSessionToken());
                ParkApplication.getInstance().setUsername(login.getUsername());
                ParkApplication.getInstance().setUserProfilePicture(login.getProfilePicture());

                ParkApplication.sJustLogged = true;
                ParkApplication.sJustLoggedSecondLevel = true;
                ParkApplication.sJustLoggedThirdLevel = true;
                saveCredentialClicked();
                PreferencesUtil.saveHasEmail(getBaseActivity(),true);

                // Build and send "User logged with credentials" Event.
                mGAnalyticsTracker.send(new HitBuilders.EventBuilder().setCategory("Server action")
                        .setAction("User logged with credentials").setLabel("The login call succeed").build());
            }
            hideProgress();
        }

        @Override
        public void onRequestError(Error error) {
            Logger.verb("onRequestError");
            Map<String, String> payload = new HashMap<>();
            if (error != null) {

                switch (error.getErrorCode()) {
                    case ResponseCodes.Login.INVALID_PASSWORD:
                        showError(getString(R.string.invalid_user_pass));
                    case ResponseCodes.Login.USER_NOT_FOUND:
                        showError(error.getMessage());
                        break;
                    case ResponseCodes.Login.IO_ERROR:
                        showError(getString(R.string.error_generic));
                        break;
                    case ResponseCodes.Login.NON_EXISTENT_EMAIL:
                        // Build and send "Popup: E-mail not registered" Event.
                        mGAnalyticsTracker.send(new HitBuilders.EventBuilder().setCategory("User action")
                                .setAction("Popup: E-mail not registered").setLabel("The e-mail is not registered").build());
                        String[] messageArray = error.getMessage().split("\\.");
                        showResultsDialog(messageArray[0].substring(7) + "\n\n" + messageArray[1]);
                        break;
                    case ResponseCodes.Signout.APP_DEPRECATED:
                        break;
                    default:
                        showError(error.getMessage());
                        break;
                }
                payload.put(SwrveEvents.EVENT_FAIL_KEY, error.getMessage());
            }
            SwrveSDK.event(SwrveEvents.LOGIN_FAIL, payload);
            hideProgress();
        }

        @Override
        public void onRequestException(SpiceException ex) {
            hideProgress();
            Map<String, String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, ex.getMessage());
            SwrveSDK.event(SwrveEvents.LOGIN_FAIL, payload);
            showError(ex.getMessage());
            Logger.verb(ex.getLocalizedMessage());
        }

    }

    @Override
    public void onRefresh() {
        ;// No refresh
    }

    private void showRegistrationScreen() {
        getFragmentManager().popBackStack();
        LoginFragment fragment = (LoginFragment) getBaseActivity().getSupportFragmentManager().findFragmentByTag(LoginFragment.TEST_TAG);
        fragment.onLoginPhone(false, mUsernameEditText.getText().toString());
    }

    private void showResultsDialog(String message) {
        final AlertDialog dialog = DialogUtils.getDialogWithLabel(getBaseActivity(), R.string.ooops,
                message)
                .setPositiveButton(R.string.register_user, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showRegistrationScreen();
                    }
                })
                .setNegativeButton(R.string.no_thanks, null).create();

        if (DeviceUtils.isDeviceLollipopOrHigher()) {
            dialog.setOnShowListener(new OnShowListenerLollipop(dialog, getBaseActivity()));
        } else {
            dialog.setOnShowListener(new OnShowListenerPreLollipop(dialog, getBaseActivity()));
        }

        dialog.show();
    }
}
