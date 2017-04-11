package com.ebay.park.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.activities.LoginActivity;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.flow.IntentFactory;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.interfaces.BackPressable;
import com.ebay.park.model.SignupModel;
import com.ebay.park.model.ZipCodeLocationModel;
import com.ebay.park.requests.SignupAKRequest;
import com.ebay.park.requests.ZipCodeLocationRequest;
import com.ebay.park.responses.ResponseCodes;
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
import com.ebay.park.utils.PreferencesUtil;
import com.ebay.park.utils.SwrveEvents;
import com.ebay.park.utils.Validations;
import com.ebay.park.views.ButtonDemi;
import com.ebay.park.views.EditTextBook;
import com.ebay.park.views.TextViewBook;
import com.facebook.accountkit.AccessToken;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.swrve.sdk.SwrveSDK;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class RegistrationAKFragment extends BaseFragment implements BackPressable {

    public static final String TAG = "REGISTRATION_AK_FRAGMENT_TAG";

    private static final String USERNAME = "username";
    private static final String MOBILE_PHONE = "mobilePhone";
    private static final String EMAIL = "email";
    private static final String LOCATION = "location";
    private static final String LOCATION_NAME = "locationName";
    private static final String ZIP_CODE = "zipCode";
    private static final String REG_ID = "regId";
    private static final String regexLocation = "%1$s";
    private static final String regexLocationZipCode = "(%1$s)";
    private static final String DEVICE_ID = "uniqueDeviceId";
    private static final String WITH_SMS = "withSms";

    private View mLocationLayout;
    private View mZipCodeLayout;
    private EditTextBook mPostalCode;
    private TextViewBook mLocation;
    private TextViewBook mLocationZipCodeView;
    private TextViewBook mLocationEdit;
    private ProgressBar mZipCodeProgress;
    private ButtonDemi mBtnContinue;
    private LocationHelper mLocationhelper;
    private TextViewBook mTermsLink;
    private Drawable mErrorDrawable;
    private TextViewBook mErrorZipCode;
    private EditText mUsername;
    private TextViewBook mErrorUserName;

    private Map<String, String> mValues;
    Tracker mGAnalyticsTracker;

    private boolean mWithSms;
    private String mRegid;
    private String mUniqueDeviceId;

    private boolean mIsDeterminedByGPS;
    private String mCoordinates;
    private String mLocationName;
    private String mLocationZipCode;

    private String mAccountId;
    private String mPhoneNumber;
    private String mEmail;

    public static RegistrationAKFragment initWith(boolean sms) {
        RegistrationAKFragment fragment = new RegistrationAKFragment();
        Bundle args = new Bundle();
        args.putBoolean(WITH_SMS, sms);
        fragment.setArguments(args);
        return fragment;
    }

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
        if (mWithSms) {
            setTitle(R.string.login_with_sms);
        } else {
            setTitle(R.string.login_with_email);
        }

        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(final Account account) {
                mAccountId = account.getId();
                mPhoneNumber = account.getPhoneNumber() == null ? null : account.getPhoneNumber().toString();
                mEmail = account.getEmail();
            }

            @Override
            public void onError(final AccountKitError error) {
            }
        });
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

        if (getArguments() != null && getArguments().containsKey(WITH_SMS)) {
            mWithSms = getArguments().getBoolean(WITH_SMS);
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

        getBaseActivity().getSupportActionBar().show();

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
        final View baseview = inflater.inflate(R.layout.fragment_user_register_acc_kit, parentView, true);

        mUsername = (EditText) baseview.findViewById(R.id.et_regist_username);
        mErrorUserName = (TextViewBook) baseview.findViewById(R.id.tv_error_username);

        mErrorZipCode = (TextViewBook) baseview.findViewById(R.id.tv_error_zip_code);

        mErrorDrawable = getResources().getDrawable(R.drawable.icon_validation_error);
        mErrorDrawable.setBounds(0, 0, mErrorDrawable.getIntrinsicWidth(), mErrorDrawable.getIntrinsicHeight());

        mBtnContinue = (ButtonDemi) baseview.findViewById(R.id.btn_continue_regist);
        mBtnContinue.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                completeStep();
            }
        });

        mPostalCode = (EditTextBook) baseview.findViewById(R.id.et_regist_zip_code);
        mPostalCode.addTextChangedListener(new ZipCodeWatcher());

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
                mLocation.setText("");
                mLocationZipCodeView.setVisibility(View.GONE);
                mPostalCode.setText("");
                mPostalCode.requestFocus();
                showKeyboard();
                mZipCodeLayout.setVisibility(View.VISIBLE);
            }
        });

        mPostalCode.setText("77001");
        mLocationhelper = new LocationHelper(getBaseActivity().getApplicationContext(), new LocationResponseCallback() {

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
                            hideKeyboard();
                            mIsDeterminedByGPS = true;
                            mCoordinates = mLocationhelper.getLatlng();
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

    private void setError(EditText et, String message) {
        et.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_validation_error, 0);

        int id = et.getId();

        if (id == R.id.et_regist_zip_code) {
            mErrorZipCode.setVisibility(View.VISIBLE);
        } else if (id == R.id.et_regist_username) {
            mErrorUserName.setText(message);
            mErrorUserName.setVisibility(View.VISIBLE);
        }
    }

    private void removeError(EditText et) {
        et.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        int id = et.getId();

        if (id == R.id.et_regist_zip_code) {
            mErrorZipCode.setVisibility(View.GONE);
        } else if (id == R.id.et_regist_username) {
            mErrorUserName.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRefresh() {
    }

    private void toggleViews(boolean enabled) {
        mLocationEdit.setEnabled(enabled);
        mBtnContinue.setEnabled(enabled);
        mPostalCode.setEnabled(enabled);
    }


    private String getUserInput(EditText field) {
        return field.getText().toString().trim();
    }

    private String getUserLabel(TextView field) {
        return field.getText().toString().trim();
    }

    private boolean validateInput() {
        boolean isInputValid = true;

        if (!TextUtils.isEmpty(mUsername.getText())) {
            if (!Validations.validateUsernameleght(mUsername)) {
                setError(mUsername, String.format(getString(R.string.invalid_username),
                        Constants.ITEM_NAME_MIN_LENGTH, Constants.NAME_LASTNAME_MAX_LENGTH));
                isInputValid = false;
            } else {
                removeError(mUsername);
            }
        } else {
            removeError(mUsername);
        }

        if (!mIsDeterminedByGPS) {
            if (!Validations.validateZipCode(mPostalCode)) {
                setError(mPostalCode, "");
                isInputValid = false;
            }
        }
        if (StringUtils.isBlank(mLocation.getText())) {
            setError(mPostalCode, "");
            isInputValid = false;
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
        if (validateInput()) {
            showProgress();
            SwrveSDK.event(SwrveEvents.USER_REGISTRATION_ATTEMPT);

            mValues = new HashMap<String, String>();
            mValues.put(USERNAME, getUserInput(mUsername).toLowerCase());
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
            mValues.put(MOBILE_PHONE, mPhoneNumber);
            mValues.put(EMAIL, mEmail);

            AccessToken accessToken = AccountKit.getCurrentAccessToken();
            String accToken = "";
            if (accessToken != null) {
                accToken = accessToken.getToken();
            }

            SignupAKRequest.Builder builder = new SignupAKRequest.Builder(accToken,
                    mValues.get(LOCATION), mValues.get(LOCATION_NAME), mValues.get(ZIP_CODE));

            if (mWithSms) {
                builder.withPhone(mValues.get(MOBILE_PHONE));
            } else {
                builder.withEmail(mValues.get(EMAIL));
            }

            if (!TextUtils.isEmpty(getUserInput(mUsername))) {
                builder.withUsername(mValues.get(USERNAME));
            }

            if (mValues.containsKey(REG_ID) && mValues.containsKey(DEVICE_ID)) {
                builder.withGCM(mValues.get(REG_ID), mValues.get(DEVICE_ID));
            } else {
                if (mValues.containsKey(DEVICE_ID)) {
                    builder.withoutGCM(mValues.get(DEVICE_ID));
                }
            }

            mSpiceManager.execute(builder.build(), new SignupRequestListener());

        }
    }

    @Override
    public void onBackPressed() {
        SwrveSDK.event(SwrveEvents.USER_REGISTRATION_CANCEL);
        backToLogin();
    }

    private class SignupRequestListener extends BaseNeckRequestListener<SignupModel> {

        @Override
        public void onRequestError(Error error) {
            hideProgress();
            if (error.getErrorCode() == ResponseCodes.SignUp.DUPLICATED_DATA){
                setError(mUsername, getString(R.string.username_duplicated));
            }

            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, error.getMessage());
            SwrveSDK.event(SwrveEvents.USER_REGISTRATION_FAIL, payload);
            Logger.warn(error.getMessage());
            MessageUtil.showError((BaseActivity) getActivity(),
                    getResources().getString(R.string.error_generic),
                    getBaseActivity().getCroutonsHolder());
        }

        @Override
        public void onRequestSuccessfull(SignupModel signupModel) {
            if (signupModel != null) {
                SwrveSDK.event(SwrveEvents.USER_REGISTRATION_SUCCESS);
                ParkApplication.getInstance().setSessionToken(signupModel.getToken());
                ParkApplication.getInstance().setUsername(signupModel.getUsername());
                if (mWithSms){
                    PreferencesUtil.saveIsSmsUser(getBaseActivity(), true);
                } else {
                    PreferencesUtil.saveHasEmail(getBaseActivity(), true);
                }
                trackInGAnalytics(signupModel);
                finishLogin();
            }
        }

        @Override
        public void onRequestException(SpiceException exception) {
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, exception.getMessage());
            SwrveSDK.event(SwrveEvents.USER_REGISTRATION_FAIL, payload);
            hideProgress();
            Logger.error(exception.getMessage());
            MessageUtil.showError((BaseActivity) getActivity(),
                    getResources().getString(R.string.error_generic),
                    getBaseActivity().getCroutonsHolder());
        }
    }

    private void finishLogin() {
        ParkApplication.sJustLogged = true;
        ParkApplication.sJustLoggedSecondLevel = true;
        ParkApplication.sJustLoggedThirdLevel = true;
        getBaseActivity().finish();
    }

    private void trackInGAnalytics(SignupModel signupModel) {
        // Build and send "User account kit registration completed" Event.
        mGAnalyticsTracker.send(new HitBuilders.EventBuilder().setCategory("User registration")
                .setAction("User account kit registration completed").setLabel(signupModel.getUsername() + " has been created!")
                .build());

    }

    private void showError(String message) {
        MessageUtil.showError(getBaseActivity(), message,
                getBaseActivity().getCroutonsHolder());
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

    private void hideKeyboard() {
        try {
            mBtnContinue.requestFocus();
            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        } catch (NullPointerException e) {
        } catch (ClassCastException e) {
        } catch (IllegalStateException e) {
        }
    }

    private void showKeyboard() {
        try {
            final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mPostalCode, 0);
        } catch (NullPointerException e) {
        } catch (ClassCastException e) {
        } catch (IllegalStateException e) {
        }
    }

    private class ZipCodeRequestListener extends BaseNeckRequestListener<ZipCodeLocationModel> {

        @Override
        public void onRequestError(Error error) {
            mZipCodeProgress.setVisibility(View.GONE);
            mLocation.setText("");
            if (error.getMessage().equals("ZERO_RESULTS") || error.getMessage().equals("UNKNOWN_ERROR")) {
                setError(mPostalCode, "");
            } else {
                showError(error.getMessage());
            }
        }

        @Override
        public void onRequestSuccessfull(ZipCodeLocationModel zipCodeLocationModel) {
            if (zipCodeLocationModel != null) {
                if (zipCodeLocationModel.getResults() != null && !zipCodeLocationModel.getResults().isEmpty()) {
                    hideKeyboard();
                    removeError(mPostalCode);
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
            mLocation.setText("");
            showError(getString(R.string.error_cannot_get_address));
        }
    }

}