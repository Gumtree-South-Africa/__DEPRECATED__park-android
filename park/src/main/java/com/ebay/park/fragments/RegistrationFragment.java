package com.ebay.park.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.activities.LoginActivity;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.flow.IntentFactory;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.model.CheckValueModel;
import com.ebay.park.model.SignupModel;
import com.ebay.park.model.ZipCodeLocationModel;
import com.ebay.park.requests.CheckValueRequest;
import com.ebay.park.requests.SignupRequest;
import com.ebay.park.requests.ZipCodeLocationRequest;
import com.ebay.park.responses.ZipCodesResponse.Result;
import com.ebay.park.responses.ZipCodesResponse.Result.Geometry.Location;
import com.ebay.park.utils.Constants;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.GCMUtils;
import com.ebay.park.utils.KeyboardHelper;
import com.ebay.park.utils.LocationHelper;
import com.ebay.park.utils.LocationHelper.LocationResponseCallback;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.SwrveEvents;
import com.ebay.park.utils.Validations;
import com.ebay.park.views.ButtonDemi;
import com.ebay.park.views.EditTextBook;
import com.ebay.park.views.TextViewBook;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.swrve.sdk.SwrveSDK;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class RegistrationFragment extends BaseFragment {

    public static final String TAG = "REGISTRATION_FRAGMENT_TAG";

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "email";
    private static final String LOCATION = "location";
    private static final String LOCATION_NAME = "locationName";
    private static final String ZIP_CODE = "zipCode";
    private static final String REG_ID = "regId";
    private static final String NAME = "name";
    private static final String LAST_NAME = "lastName";
    private static final String FB_TOKEN = "fbToken";
    private static final String FB_USER_ID = "fbUserId";
    private static final String regexLocation = "%1$s";
    private static final String regexLocationZipCode = "(%1$s)";
    private static final String DEVICE_ID = "uniqueDeviceId";

    private View mLocationLayout;
    private View mZipCodeLayout;
    private EditTextBook mEmail;
    private EditTextBook mEmailConfirmation;
    private EditTextBook mPassword;
    private EditTextBook mName;
    private EditTextBook mLastname;
    private EditTextBook mPostalCode;
    private TextViewBook mLocation;
    private TextViewBook mLocationZipCodeView;
    private TextViewBook mLocationEdit;
    private ProgressBar mZipCodeProgress;
    private ButtonDemi mBtnContinue;
    private LocationHelper mLocationHelper;
    private TextViewBook mTermsLink;
    private Boolean mPasswordVisible = false;
    private Drawable mErrorDrawable;
    private TextViewBook mErrorEmail;
    private TextViewBook mErrorEmailConfirmation;
    private TextViewBook mErrorPass;
    private TextViewBook mErrorName;
    private TextViewBook mErrorLastName;
    private TextViewBook mErrorZipCode;
    private ImageView mPassVisibility;

    private Map<String, String> mValues;
    Tracker mGAnalyticsTracker;

    private String mRegid;
    private String mUniqueDeviceId;

    private boolean mIsDeterminedByGPS;
    private String mCoordinates;
    private String mLocationName;
    private String mLocationZipCode;
    private String mUsername = "";
    private int mUsernameIncrement = 0;

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("regid", mRegid);
        outState.putBoolean("isDeterminedByGPS", mIsDeterminedByGPS);
        outState.putString("coordinates", mCoordinates);
        outState.putString("locationName", mLocationName);
        outState.putString("locationZipCode", mLocationZipCode);
        outState.putString("uniqueDeviceId", mUniqueDeviceId);
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.registration_title);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mRegid = savedInstanceState.getString("regid");
            mIsDeterminedByGPS = savedInstanceState.getBoolean("isDeterminedByGPS");
            mCoordinates = savedInstanceState.getString("coordinates");
            mLocationName = savedInstanceState.getString("locationName");
            mLocationZipCode = savedInstanceState.getString("locationZipCode");
            mUniqueDeviceId = savedInstanceState.getString("uniqueDeviceId");
        }

        if (GCMUtils.checkPlayServices(getBaseActivity())) {
            mRegid = GCMUtils.getRegistrationId(getBaseActivity());

            if (mRegid.isEmpty()) {
                new Thread(new Runnable() {
                    public void run() {
                        mRegid = GCMUtils.registerOnGCM(getBaseActivity());
                    }
                }).start();
            }
        } else {
            Logger.info("No valid Google Play Services APK found.");
        }

        mUniqueDeviceId = DeviceUtils.getUniqueDeviceId(getBaseActivity());

        SwrveSDK.event(SwrveEvents.USER_REGISTRATION_BEGIN);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        boolean isEventConsumed = false;
        if (item.getItemId() == android.R.id.home) {
            mBtnContinue.requestFocus();
            KeyboardHelper.hide(getBaseActivity(),getView());
            SwrveSDK.event(SwrveEvents.USER_REGISTRATION_CANCEL);
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ViewGroup parentView = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        final View baseview = inflater.inflate(R.layout.fragment_user_register, parentView, true);

        getBaseActivity().getSupportActionBar().show();

        mErrorEmail = (TextViewBook) baseview.findViewById(R.id.tv_error_email);
        mErrorEmailConfirmation = (TextViewBook) baseview.findViewById(R.id.tv_error_confirmation);
        mErrorPass = (TextViewBook) baseview.findViewById(R.id.tv_error_pass);
        mErrorName = (TextViewBook) baseview.findViewById(R.id.tv_error_name);
        mErrorLastName = (TextViewBook) baseview.findViewById(R.id.tv_error_lastname);
        mErrorZipCode = (TextViewBook) baseview.findViewById(R.id.tv_error_zip_code);

        mErrorDrawable = getResources().getDrawable(R.drawable.icon_validation_error);
        mErrorDrawable.setBounds(0, 0, mErrorDrawable.getIntrinsicWidth(), mErrorDrawable.getIntrinsicHeight());

        mEmail = (EditTextBook) baseview.findViewById(R.id.et_regist_email);
        mEmail.setFilters(new InputFilter[]{Constants.EMAIL_FILTER});
        mEmail.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    removeTick();
                    removeError(mEmail);
                    removeError(mEmailConfirmation);
                    if (!hasFocus && !Validations.validateEmail(mEmail)) {
                        setEmailError(getString(R.string.enter_valid_email));
                    } else {
                        removeError(mEmail);
                        mSpiceManager.executeCacheRequest(
                                CheckValueRequest.validateEmailRequest(mEmail.getText().toString()),
                                new FieldAvailabiltyListener(EMAIL, null, null, null));
                    }
                }
            }
        });

        mEmailConfirmation = (EditTextBook) baseview.findViewById(R.id.et_regist_email_confirm);
        mEmailConfirmation.setFilters(new InputFilter[]{Constants.EMAIL_FILTER});
        mEmailConfirmation.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    removeError(mEmailConfirmation);
                    if (!StringUtils.isBlank(mEmailConfirmation.getText())) {
                        if (!Validations.validateEmailMatchConfirmation(mEmail, mEmailConfirmation)) {
                            removeTick();
                            // Set the exclamation sign
                            setEmailError(getString(R.string.email_match_confirmation));
                            setError(mEmailConfirmation);
                        } else {
                            if (mErrorEmail.getVisibility() == View.GONE) {
                                removeError(mEmail);
                                removeError(mEmailConfirmation);
                                setTick();
                            }
                        }
                    }
                }
            }
        });

        mPassword = (EditTextBook) baseview.findViewById(R.id.et_regist_password);
        mPassVisibility = (ImageView) baseview.findViewById(R.id.iv_show_hide_pass);
        mPassword.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    removeError(mPassword);
                    if (!Validations.validatePassword(mPassword)) {
                        setError(mPassword);
                    }
                }
            }
        });
        mPassVisibility.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPasswordVisible) {
                    mPassVisibility.setImageDrawable(getResources().getDrawable(R.drawable.icon_eye_show));
                    mPassword.setTransformationMethod(new PasswordTransformationMethod());
                    mPasswordVisible = false;
                } else {
                    mPassVisibility.setImageDrawable(getResources().getDrawable(R.drawable.icon_eye_hide));
                    mPassword.setTransformationMethod(null);
                    mPasswordVisible = true;
                }
            }
        });

        mName = (EditTextBook) baseview.findViewById(R.id.et_regist_name);
        mName.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    removeError(mName);
                    if (!Validations.validateNameLastname(mName)) {
                        setError(mName);
                    }
                }
            }
        });

        mLastname = (EditTextBook) baseview.findViewById(R.id.et_regist_lastname);
        mLastname.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    removeError(mLastname);
                    if (!Validations.validateNameLastname(mLastname)) {
                        setError(mLastname);
                    }
                }
            }
        });

        mBtnContinue = (ButtonDemi) baseview.findViewById(R.id.btn_continue_regist);
        mBtnContinue.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    showProgress();
                    SwrveSDK.event(SwrveEvents.USER_REGISTRATION_ATTEMPT);
                    makeUserName(mName.getText().toString(),
                            String.valueOf(mLastname.getText().toString().charAt(0)), false);
                }
            }
        });

        mPostalCode = (EditTextBook) baseview.findViewById(R.id.et_regist_zip_code);
        mPostalCode.addTextChangedListener(new ZipCodeWatcher());
        mPostalCode.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    removeError(mPostalCode);
                    if (!Validations.validateZipCode(mPostalCode)) {
                        setError(mPostalCode);
                    }
                }
            }
        });

        mZipCodeProgress = (ProgressBar) baseview.findViewById(R.id.progress_regist_zip_code);

        mLocationLayout = baseview.findViewById(R.id.ly_regist_edit_location);
        mZipCodeLayout = baseview.findViewById(R.id.ly_regist_zip_code);

        mLocation = (TextViewBook) baseview.findViewById(R.id.tv_regist_location);
        mLocationZipCodeView = (TextViewBook) baseview.findViewById(R.id.tv_regist_location_zc);
        mLocationEdit = (TextViewBook) baseview.findViewById(R.id.tv_regist_edit_location);
        mLocationEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mLocationLayout.setVisibility(View.GONE);
                mLocation.setVisibility(View.GONE);
                mLocationZipCodeView.setVisibility(View.GONE);
                mPostalCode.setText("");
                mLocation.setText("");
                mLocationZipCodeView.setText("");
                mPostalCode.requestFocus();
                KeyboardHelper.show(getBaseActivity(), mPostalCode);
                mZipCodeLayout.setVisibility(View.VISIBLE);
            }
        });

        mLocationHelper = new LocationHelper(getBaseActivity().getApplicationContext(), new LocationResponseCallback() {

            @Override
            public void setZipCode(String zipCode) {

                // if didn't set any zip code manually yet
                if (TextUtils.isEmpty(mPostalCode.getText())) {
                    mIsDeterminedByGPS = true;
                    if (zipCode != null) {
                        if (!zipCode.equals(LocationHelper.ZIP_CODE_NOT_USA)) {
                            mLocationZipCode = zipCode;
                            mPostalCode.setText(zipCode);
                        } else {
                            mIsDeterminedByGPS = false;
                        }
                    } else {
                        final String message = zipCode;
                        if (!TextUtils.isEmpty(message)) {
                            mPostalCode.setText(message);
                        } else {
                            mIsDeterminedByGPS = false;
                        }
                    }
                }
            }

            @Override
            public void setAddress(String address) {
                if (!address.equals(LocationHelper.ZIP_CODE_NOT_USA)) {
                    if (mIsDeterminedByGPS) {
                        if (!TextUtils.isEmpty(mPostalCode.getText())) {
                            mBtnContinue.requestFocus();
                            KeyboardHelper.hide(getBaseActivity(),getView());
                            mIsDeterminedByGPS = true;
                            mCoordinates = mLocationHelper.getLatlng();
                            mLocationName = address;
                            mLocation.setText(String.format(regexLocation, mLocationName));
                            mLocationZipCodeView.setText(String.format(regexLocationZipCode, mPostalCode.getText()));
                            mLocationLayout.setVisibility(View.VISIBLE);
                            mLocation.setVisibility(View.VISIBLE);
                            mLocationZipCodeView.setVisibility(View.VISIBLE);
                            mZipCodeProgress.setVisibility(View.GONE);
                            mZipCodeLayout.setVisibility(View.GONE);
                        }
                    }
                }
                mIsDeterminedByGPS = false; // Reset value
            }
        });

        mTermsLink = (TextViewBook) baseview.findViewById(R.id.tvTermsLink);
        mTermsLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentTerms = IntentFactory.getTermsConditions();
                if (intentTerms.resolveActivity(getBaseActivity().getPackageManager()) != null) {
                    startActivity(intentTerms);
                }
            }
        });

        setHasOptionsMenu(true);

        return baseview;
    }

    private void setTick() {
        mEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_validation_check, 0);
        mEmailConfirmation.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_validation_check, 0);
    }

    private void removeTick() {
        mEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        mEmailConfirmation.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    private void setError(EditText et) {
        et.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_validation_error, 0);

        int id = et.getId();

        if (id == R.id.et_regist_email_confirm) {
            mErrorEmailConfirmation.setVisibility(View.VISIBLE);
        } else if (id == R.id.et_regist_password) {
            mErrorPass.setVisibility(View.VISIBLE);
        } else if (id == R.id.et_regist_name) {
            mErrorName.setText(String.format(getString(R.string.invalid_name),
                    Constants.NAME_LASTNAME_MIN_LENGTH, Constants.NAME_LASTNAME_MAX_LENGTH));
            mErrorName.setVisibility(View.VISIBLE);
        } else if (id == R.id.et_regist_lastname) {
            mErrorLastName.setText(String.format(getString(R.string.invalid_lastname),
                    Constants.NAME_LASTNAME_MIN_LENGTH, Constants.NAME_LASTNAME_MAX_LENGTH));
            mErrorLastName.setVisibility(View.VISIBLE);
        } else {
            mErrorZipCode.setVisibility(View.VISIBLE);
        }
    }

    private void setEmailError(String emailError) {
        mEmail.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_validation_error, 0);
        if (!emailError.equals(getString(R.string.email_match_confirmation))) {
            mErrorEmail.setText(emailError);
            mErrorEmail.setVisibility(View.VISIBLE);
        }
    }

    private void removeError(EditText et) {
        et.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        int id = et.getId();

        if (id == R.id.et_regist_email) {
            mErrorEmail.setVisibility(View.GONE);
        } else if (id == R.id.et_regist_email_confirm) {
            mErrorEmailConfirmation.setVisibility(View.GONE);
        } else if (id == R.id.et_regist_password) {
            mErrorPass.setVisibility(View.GONE);
        } else if (id == R.id.et_regist_name) {
            mErrorName.setVisibility(View.GONE);
        } else if (id == R.id.et_regist_lastname) {
            mErrorLastName.setVisibility(View.GONE);
        } else {
            mErrorZipCode.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRefresh() {
    }

    private void toggleViews(boolean enabled) {
        mLocationEdit.setEnabled(enabled);
        mBtnContinue.setEnabled(enabled);
        mEmail.setEnabled(enabled);
        mEmailConfirmation.setEnabled(enabled);
        mPassword.setEnabled(enabled);
        mName.setEnabled(enabled);
        mLastname.setEnabled(enabled);
        mPostalCode.setEnabled(enabled);
    }

    private void makeUserName(String _firstName, String _nextInitials, Boolean duplicated) {

        String finalUserName = "";
        String firstName = _firstName;
        String nextInitials = _nextInitials;

        if (duplicated){
            mUsernameIncrement++;
            nextInitials += mUsernameIncrement;
        }

        int extraNamesCount = nextInitials.length();
        int neededPlaces = 15 - extraNamesCount;
        if (firstName.length() > neededPlaces) {
            firstName = firstName.substring(0, (neededPlaces));
        }

        finalUserName = firstName + nextInitials;

        mSpiceManager.executeCacheRequest(CheckValueRequest.validateUsernameRequest(finalUserName),
                new FieldAvailabiltyListener(USERNAME, finalUserName, firstName, _nextInitials));

    }

    private String getUserInput(EditText field) {
        return field.getText().toString().trim();
    }

    private boolean validateInput() {
        boolean isInputValid = true;
        boolean missingField = false;

        if (TextUtils.isEmpty(mEmail.getText())) {
            isInputValid = false;
            missingField = true;
            removeTick();
            setEmailError(getString(R.string.enter_valid_email));
        } else {
            if (!Validations.validateEmail(mEmail)) {
                removeTick();
                setEmailError(getString(R.string.enter_valid_email));
                isInputValid = false;
            } else {
                removeError(mEmail);
                if (!StringUtils.isBlank(mEmailConfirmation.getText())) {
                    if (!Validations.validateEmailMatchConfirmation(mEmail, mEmailConfirmation)) {
                        isInputValid = false;
                        removeTick();
                        setEmailError(getString(R.string.email_match_confirmation));
                        setError(mEmailConfirmation);
                    } else {
                        removeError(mEmail);
                        removeError(mEmailConfirmation);
                        setTick();
                    }
                } else {
                    isInputValid = false;
                    missingField = true;
                }
            }
        }
        if (TextUtils.isEmpty(mEmailConfirmation.getText())) {
            missingField = true;
            isInputValid = false;
        }
        if (TextUtils.isEmpty(mPassword.getText())) {
            missingField = true;
            isInputValid = false;
            setError(mPassword);
        } else {
            if (!Validations.validatePassword(mPassword)) {
                isInputValid = false;
                setError(mPassword);
            } else {
                removeError(mPassword);
            }
        }
        if (TextUtils.isEmpty(mName.getText())) {
            missingField = true;
            isInputValid = false;
            setError(mName);
        } else {
            if (!Validations.validateNameLastname(mName)) {
                setError(mName);
                isInputValid = false;
            } else {
                removeError(mName);
            }
        }
        if (TextUtils.isEmpty(mLastname.getText())) {
            missingField = true;
            isInputValid = false;
            setError(mLastname);
        } else {
            if (!Validations.validateNameLastname(mLastname)) {
                setError(mLastname);
                isInputValid = false;
            } else {
                removeError(mLastname);
            }
        }
        if (TextUtils.isEmpty(mPostalCode.getText())) {
            missingField = true;
            isInputValid = false;
            setError(mPostalCode);
        } else {
            if (!mIsDeterminedByGPS) {
                if (!Validations.validateZipCode(mPostalCode)) {
                    setError(mPostalCode);
                    isInputValid = false;
                } else {
                    removeError(mPostalCode);
                }
            }
        }
        if (StringUtils.isBlank(mLocation.getText())) {
            setError(mPostalCode);
            isInputValid = false;
        }

        if (missingField) {
            showError(getString(R.string.check_registration_info));
        }

        return isInputValid;
    }

    @Override
    protected void showProgress() {
        super.showProgress();
        toggleViews(false);
    }

    @Override
    protected void hideProgress() {
        super.hideProgress();
        toggleViews(true);
    }

    private void completeStep() {
        mValues = new HashMap<String, String>();
        if (!mUsername.equals("")) {
            mValues.put(USERNAME, mUsername);
        }
        mValues.put(EMAIL, getUserInput(mEmail));
        mValues.put(PASSWORD, getUserInput(mPassword));
        mValues.put(LOCATION, mCoordinates);
        mValues.put(LOCATION_NAME, mLocationName);
        if (mLocationZipCode != null) {
            mValues.put(ZIP_CODE, mLocationZipCode);
        } else {
            mValues.put(ZIP_CODE, getUserInput(mPostalCode));
        }
        if (!TextUtils.isEmpty(mRegid)) {
            mValues.put(REG_ID, mRegid);
        }
        if (!TextUtils.isEmpty(mUniqueDeviceId)) {
            mValues.put(DEVICE_ID, mUniqueDeviceId);
        }

        // @formatter:off
        SignupRequest.Builder builder = new SignupRequest.Builder(mValues.get(USERNAME), mValues.get(PASSWORD),
                mValues.get(EMAIL), mValues.get(LOCATION), mValues.get(LOCATION_NAME), mValues.get(ZIP_CODE));

        if (mValues.containsKey(REG_ID) && mValues.containsKey(DEVICE_ID)) {
            builder.withGCM(mValues.get(REG_ID), mValues.get(DEVICE_ID));
        } else {
            if (mValues.containsKey(DEVICE_ID)) {
                builder.withoutGCM(mValues.get(DEVICE_ID));
            }
        }

        builder.withPersonalData(mValues.get(NAME), mValues.get(LAST_NAME));
        if (mValues.containsKey(FB_TOKEN)) {
            builder.withFacebook(mValues.get(FB_TOKEN), mValues.get(FB_USER_ID));
        }
        // @formatter:on'
        mSpiceManager.execute(builder.build(), new SignupRequestListener());

    }

    @Override
    public void onBackPressed() {
        SwrveSDK.event(SwrveEvents.USER_REGISTRATION_CANCEL);
    }

    private class SignupRequestListener extends BaseNeckRequestListener<SignupModel> {

        @Override
        public void onRequestError(Error error) {
            if (error.getErrorCode() == 204) {
                removeTick();
                setEmailError(getString(R.string.already_taken_email));
            }
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, error.getMessage());
            SwrveSDK.event(SwrveEvents.USER_REGISTRATION_FAIL, payload);
            hideProgress();
            Logger.warn(error.getMessage());
        }

        @Override
        public void onRequestSuccessfull(SignupModel signupModel) {
            if (signupModel != null) {
                SwrveSDK.event(SwrveEvents.USER_REGISTRATION_SUCCESS);
                ParkApplication.getInstance().setSessionToken(signupModel.getToken());
                ParkApplication.getInstance().setUsername(signupModel.getUsername());
                trackInGAnalytics(signupModel);
                ParkApplication.sJustLogged = true;
                ParkApplication.sJustLoggedSecondLevel = true;
                ParkApplication.sJustLoggedThirdLevel = true;
                getBaseActivity().finish();
            }
        }

        @Override
        public void onRequestException(SpiceException exception) {
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, exception.getMessage());
            SwrveSDK.event(SwrveEvents.USER_REGISTRATION_FAIL, payload);
            hideProgress();
            Logger.error(exception.getMessage());
        }
    }

    private void trackInGAnalytics(SignupModel signupModel) {
        // Build and send "User registration completed" Event.
        mGAnalyticsTracker.send(new HitBuilders.EventBuilder().setCategory("User registration")
                .setAction("User registration completed").setLabel(signupModel.getUsername() + " has been created!")
                .build());

    }

    private void showError(String message) {
        MessageUtil.showError(getBaseActivity(), message,
                getBaseActivity().getCroutonsHolder());
    }

    private class FieldAvailabiltyListener extends BaseNeckRequestListener<CheckValueModel> {

        private String mField;
        private String mUserName;
        private String mFirstName;
        private String mNextInitials;

        public FieldAvailabiltyListener(String field, String userName, String firstName, String nextInitials) {
            this.mField = field;
            this.mUserName = userName;
            this.mFirstName = firstName;
            this.mNextInitials = nextInitials;
        }

        @Override
        public void onRequestError(Error error) {
            Logger.warn(error.getMessage());
        }

        @Override
        public void onRequestSuccessfull(CheckValueModel value) {
            if (!value.isAvailable()) {
                if (mField.equals(EMAIL)) {
                    removeTick();
                    setEmailError(getString(R.string.already_taken_email));
                } else {
                    // username duplicated
                    makeUserName(mFirstName, mNextInitials, true);
                }
            } else {
                if (mField.equals(USERNAME)) {
                    mUsername = mUserName;
                    completeStep();
                }
            }
        }

        @Override
        public void onRequestException(SpiceException exception) {
            showError(exception.getMessage());
            Logger.warn(exception.getLocalizedMessage());
        }
    }

    private class ZipCodeWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!mIsDeterminedByGPS) {
                if (isAdded()) {
                    mPostalCode.setHint(getResources().getString(R.string.type_zip_code));
                    mIsDeterminedByGPS = false;
                    final String zipCode = s.toString();
                    if (zipCode.length() >= Constants.ZIPCODE_LENGTH) {
                        getLocationFromZipCode();
                        removeError(mPostalCode);
                        mBtnContinue.requestFocus();
                        KeyboardHelper.hide(getBaseActivity(),getView());
                        mZipCodeProgress.setVisibility(View.VISIBLE);
                    } else {
                        mLocationLayout.setVisibility(View.GONE);
                        mZipCodeProgress.setVisibility(View.GONE);
                    }
                }
            }
        }
    }

    private void getLocationFromZipCode() {
        mSpiceManager.executeCacheRequest(buildZipCodeRequest(), new ZipCodeRequestListener());
    }

    private ZipCodeLocationRequest buildZipCodeRequest() {
        String zipCode = getUserInput(mPostalCode);
        return new ZipCodeLocationRequest(zipCode);
    }

    private class ZipCodeRequestListener extends BaseNeckRequestListener<ZipCodeLocationModel> {

        @Override
        public void onRequestError(Error error) {
            mZipCodeProgress.setVisibility(View.GONE);
            if (error.getMessage().equals("ZERO_RESULTS") || error.getMessage().equals("UNKNOWN_ERROR")) {
                setError(mPostalCode);
            } else {
                showError(error.getMessage());
            }
        }

        @Override
        public void onRequestSuccessfull(ZipCodeLocationModel zipCodeLocationModel) {
            if (zipCodeLocationModel != null) {
                if (zipCodeLocationModel.getResults() != null && !zipCodeLocationModel.getResults().isEmpty()) {
                    mBtnContinue.requestFocus();
                    KeyboardHelper.hide(getBaseActivity(),getView());
                    ArrayList<String> results = new ArrayList<String>();
                    for (Result res : zipCodeLocationModel.getResults()) {
                        results.add(res.toString());
                    }
                    String strLocName = zipCodeLocationModel.getResults().get(0).toString();
                    Location loc = zipCodeLocationModel.getResults().get(0).getGeometry().getLocation();
                    mCoordinates = loc.getLat() + "," + loc.getLng();
                    mLocationName = strLocName;
                    mLocation.setText(String.format(regexLocation, mLocationName));
                    mLocationZipCodeView.setText(String.format(regexLocationZipCode, mPostalCode.getText()));
                    mLocationLayout.setVisibility(View.VISIBLE);
                    mLocation.setVisibility(View.VISIBLE);
                    mLocationZipCodeView.setVisibility(View.VISIBLE);
                    mZipCodeProgress.setVisibility(View.GONE);
                    mZipCodeLayout.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onRequestException(SpiceException exception) {
            mZipCodeProgress.setVisibility(View.GONE);
            Logger.verb(exception.getLocalizedMessage());
            showError(getString(R.string.error_cannot_get_address));
        }
    }

}