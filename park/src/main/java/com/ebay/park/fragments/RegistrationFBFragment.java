package com.ebay.park.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.activities.LoginActivity;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.flow.IntentFactory;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.model.CheckValueModel;
import com.ebay.park.model.SignupModel;
import com.ebay.park.model.ZipCodeLocationModel;
import com.ebay.park.requests.CheckValueRequest;
import com.ebay.park.requests.ProfileImgUploadRequest;
import com.ebay.park.requests.SignupAddPicRequest;
import com.ebay.park.requests.SignupRequest;
import com.ebay.park.requests.ZipCodeLocationRequest;
import com.ebay.park.responses.ProfileImgResponse;
import com.ebay.park.responses.SignupAddPicResponse;
import com.ebay.park.responses.ZipCodesResponse.Result;
import com.ebay.park.responses.ZipCodesResponse.Result.Geometry.Location;
import com.ebay.park.utils.Constants;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.FacebookUtil;
import com.ebay.park.utils.FileUtils;
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
import com.ebay.park.views.TextViewDemi;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.swrve.sdk.SwrveSDK;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

public class RegistrationFBFragment extends BaseFragment {

    public static final String TAG = "REGISTRATION_FB_FRAGMENT_TAG";

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
    private static final String IMAGE_PATH = "imagePath";
    private static final String regexLocation = "%1$s";
    private static final String regexLocationZipCode = "(%1$s)";
    private static final String DEVICE_ID = "uniqueDeviceId";

    private View mLocationLayout;
    private View mZipCodeLayout;
    private EditTextBook mPostalCode;
    private TextViewDemi mUsername;
    private TextViewBook mEmail;
    private TextViewBook mLocation;
    private TextViewBook mLocationZipCodeView;
    private TextViewBook mLocationEdit;
    private ProgressBar mZipCodeProgress;
    private ButtonDemi mBtnContinue;
    private LocationHelper mLocationHelper;
    private TextViewBook mTermsLink;
    private Drawable mErrorDrawable;
    private TextViewBook mErrorZipCode;
    private ImageView mProfileImage;
    private String mImagePath;

    private Map<String, String> mValues;
    Tracker mGAnalyticsTracker;

    private String mRegid;
    private String mFbToken;
    private String mFbUserId;
    private boolean mFbSessionOpened;
    private String mFbPictureUrl;
    private String mUniqueDeviceId;

    private boolean mIsDeterminedByGPS;
    private String mCoordinates;
    private String mLocationName;
    private String mLocationZipCode;
    private int mUsernameIncrement = 0;
    private String mUsernameFacebook;
    private Boolean mHasEmail = false;

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("imagePath", mImagePath);
        outState.putString("regid", mRegid);
        outState.putString("fbToken", mFbToken);
        outState.putString("fbUserId", mFbUserId);
        outState.putBoolean("isDeterminedByGPS", mIsDeterminedByGPS);
        outState.putString("coordinates", mCoordinates);
        outState.putString("locationName", mLocationName);
        outState.putString("locationZipCode", mLocationZipCode);
        outState.putString("uniqueDeviceId", mUniqueDeviceId);
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(R.string.register_fb_title);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mImagePath = savedInstanceState.getString("imagePath");
            mRegid = savedInstanceState.getString("regid");
            mFbToken = savedInstanceState.getString("fbToken");
            mFbUserId = savedInstanceState.getString("fbUserId");
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

        getBaseActivity().getSupportActionBar().show();

        mUniqueDeviceId = DeviceUtils.getUniqueDeviceId(getBaseActivity());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        boolean isEventConsumed = false;
        if (item.getItemId() == android.R.id.home) {
            if (mFbSessionOpened) {
                SwrveSDK.event(SwrveEvents.LOGIN_CANCEL);
                LoginManager.getInstance().logOut();
            }
            mBtnContinue.requestFocus();
            KeyboardHelper.hide(getBaseActivity(),getView());
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
        final View baseview = inflater.inflate(R.layout.fragment_user_register_fb, parentView, true);

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
                mLocationZipCodeView.setVisibility(View.GONE);
                mPostalCode.setText("");
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

        mUsername = (TextViewDemi) baseview.findViewById(R.id.tv_username);
        mEmail = (TextViewBook) baseview.findViewById(R.id.tv_email);
        mProfileImage = (ImageView) baseview.findViewById(R.id.iv_user_photo);

        retrieveFacebookData();
        setHasOptionsMenu(true);

        return baseview;
    }

    private void retrieveFacebookData() {

        if (AccessToken.getCurrentAccessToken() != null && Profile.getCurrentProfile() != null) {
            mFbSessionOpened = true;
            mFbToken = AccessToken.getCurrentAccessToken().getToken();

            Bundle fbParameters = new Bundle();
            fbParameters.putString("fields", "id,name,email,first_name,last_name,picture.type(large),permissions");

            GraphRequest graphRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject user, GraphResponse response) {
                    if (response.getError() != null) {
                        System.out.println("ERROR");
                        if (isAdded()) {
                            MessageUtil.showError((BaseActivity) getActivity(),
                                    getResources().getString(R.string.error_facebook_result),
                                    getBaseActivity().getCroutonsHolder());
                        }
                    } else {
                        System.out.println("Success");
                        try {
                            String jsonresult = String.valueOf(user);
                            System.out.println("JSON Result" + jsonresult);

                            mFbUserId = user.getString("id");

                            String fbUserEmail = user.getString("email");
                            if (!TextUtils.isEmpty(fbUserEmail)) {
                                if (mEmail != null) {
                                    mEmail.setText(fbUserEmail);
                                    mEmail.setEnabled(false);
                                }
                                mHasEmail = true;
                            }
                            makeUserName(user);
                            mUsernameFacebook = user.getString("name");

                            if (!user.getJSONObject("picture").getJSONObject("data").getBoolean("is_silhouette")) {
                                mFbPictureUrl = user.getJSONObject("picture").getJSONObject("data").getString("url");
                                downloadFacebookProfilePhoto(mFbPictureUrl);
                            }

                        } catch (NullPointerException e) {
                            if (isAdded()) {
                                MessageUtil.showError((BaseActivity) getActivity(),
                                        getResources().getString(R.string.error_facebook_result),
                                        getBaseActivity().getCroutonsHolder());
                            }
                        } catch (JSONException e) {
                            if (isAdded()) {
                                MessageUtil.showError((BaseActivity) getActivity(),
                                        getResources().getString(R.string.error_facebook_result),
                                        getBaseActivity().getCroutonsHolder());
                            }
                        }
                    }
                }

            });

            graphRequest.setParameters(fbParameters);
            showProgress();
            graphRequest.executeAsync();


        } else {
            mFbSessionOpened = false;
        }
    }

    private void downloadFacebookProfilePhoto(String picUrl) {

        Target target = new Target() {

            @Override
            public void onPrepareLoad(Drawable arg0) {
                return;
            }

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom arg1) {

                try {
                    File photoFile = null;
                    try {
                        photoFile = FileUtils.createProfileImageFile();
                        mImagePath = photoFile.getAbsolutePath();
                    } catch (IOException ex) {
                        MessageUtil.showError(getBaseActivity(), R.string.error_save_image,
                                getBaseActivity().getCroutonsHolder());
                        Logger.error(ex.getMessage());
                    }

                    FileOutputStream ostream = new FileOutputStream(photoFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                    ostream.close();

                    Picasso.with(getActivity().getApplicationContext()).load(photoFile).fit().centerCrop()
                            .into(mProfileImage);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onBitmapFailed(Drawable arg0) {
                return;
            }
        };

        Picasso.with(getActivity().getApplicationContext()).load(picUrl).into(target);
    }

    private void setError(EditText et) {
        et.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_validation_error, 0);

        int id = et.getId();

        if (id == R.id.et_regist_zip_code) {
            mErrorZipCode.setVisibility(View.VISIBLE);
        }
    }

    private void removeError(EditText et) {
        et.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        int id = et.getId();

        if (id == R.id.et_regist_zip_code) {
            mErrorZipCode.setVisibility(View.GONE);
        }
    }

    public static String randomPass() {
        return RandomStringUtils.randomAlphanumeric(8);
    }

    @Override
    public void onRefresh() {
    }

    private void toggleViews(boolean enabled) {
        mLocationEdit.setEnabled(enabled);
        mBtnContinue.setEnabled(enabled);
        mPostalCode.setEnabled(enabled);
    }

    private void makeUserName(JSONObject user) {

        try {
            String finalUserName = "";
            String firstName = "";
            String nextInitials = "";

            if (!TextUtils.isEmpty(user.getString("name"))) {
                String[] names = user.getString("name").split(" ");
                firstName = names[0];

                int extraNamesCount = names.length - 1;
                int neededPlaces = 15 - extraNamesCount;
                if (firstName.length() > neededPlaces) {
                    firstName = firstName.substring(0, (neededPlaces));
                }

                finalUserName = firstName;
                for (int i = 1; i < names.length; i++) {
                    nextInitials = nextInitials + names[i].charAt(0);
                }
                finalUserName = finalUserName + nextInitials;

            } else {
                firstName = user.getString("first_name");
                if (firstName.length() > 14) {
                    firstName = firstName.substring(0, 14);
                }
                nextInitials = String.valueOf(user.getString("last_name").charAt(0));
                finalUserName = firstName + nextInitials;
            }
            mSpiceManager.executeCacheRequest(CheckValueRequest.validateUsernameRequest(finalUserName),
                    new FieldAvailabiltyListener(mUsername, true, finalUserName, firstName, nextInitials));
        } catch (JSONException e) {
            MessageUtil.showError((BaseActivity) getActivity(), getResources()
                            .getString(R.string.error_facebook_result),
                    getBaseActivity().getCroutonsHolder());
        } catch (IndexOutOfBoundsException e) {
            MessageUtil.showError((BaseActivity) getActivity(), getResources()
                            .getString(R.string.error_facebook_result),
                    getBaseActivity().getCroutonsHolder());
        } catch (NullPointerException e) {
            MessageUtil.showError((BaseActivity) getActivity(), getResources()
                            .getString(R.string.error_facebook_result),
                    getBaseActivity().getCroutonsHolder());
        }

    }

    private void reMakeUserNameDueToDuplicated(String _firstName, String _nextInitials) {

        mUsernameIncrement++;
        String finalUserName = "";
        String firstName = _firstName;
        String nextInitials = _nextInitials + mUsernameIncrement;

        int extraNamesCount = nextInitials.length();
        int neededPlaces = 15 - extraNamesCount;
        if (firstName.length() > neededPlaces) {
            firstName = firstName.substring(0, (neededPlaces));
        }

        finalUserName = firstName + nextInitials;

        mSpiceManager.executeCacheRequest(CheckValueRequest.validateUsernameRequest(finalUserName),
                new FieldAvailabiltyListener(mUsername, true, finalUserName, firstName, _nextInitials));

    }

    private String getUserInput(EditText field) {
        return field.getText().toString().trim();
    }

    private String getUserLabel(TextView field) {
        return field.getText().toString().trim();
    }

    private boolean validateInput() {
        boolean isInputValid = true;

        if (!mIsDeterminedByGPS) {
            if (!Validations.validateZipCode(mPostalCode)) {
                setError(mPostalCode);
                isInputValid = false;
            }
        }
        if (StringUtils.isBlank(mLocation.getText())) {
            setError(mPostalCode);
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
        SwrveSDK.event(SwrveEvents.LOGIN_ATTEMPT);
        if (validateInput()) {
            showProgress();
            mValues = new HashMap<String, String>();
            mValues.put(USERNAME, getUserLabel(mUsername));
            mValues.put(EMAIL, getUserLabel(mEmail));
            mValues.put(PASSWORD, randomPass());
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
            if (!TextUtils.isEmpty(mFbToken)) {
                mValues.put(FB_TOKEN, mFbToken);
                mValues.put(FB_USER_ID, mFbUserId);
            }
            if (!TextUtils.isEmpty(mImagePath)) {
                mValues.put(IMAGE_PATH, mImagePath);
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

        } else {
            if (TextUtils.isEmpty(mPostalCode.getText().toString())) {
                showError(getString(R.string.check_registration_info));
            }
        }

    }

    @Override
    public void onBackPressed() {
        SwrveSDK.event(SwrveEvents.LOGIN_CANCEL);
    }

    private class SignupRequestListener extends BaseNeckRequestListener<SignupModel> {

        @Override
        public void onRequestError(Error error) {
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, error.getMessage());
            SwrveSDK.event(SwrveEvents.LOGIN_FAIL, payload);
            hideProgress();
            Logger.warn(error.getMessage());
        }

        @Override
        public void onRequestSuccessfull(SignupModel signupModel) {
            if (signupModel != null) {
                SwrveSDK.event(SwrveEvents.LOGIN_SUCCESS);
                ParkApplication.getInstance().setSessionToken(signupModel.getToken());
                ParkApplication.getInstance().setUsername(signupModel.getUsername());
                trackInGAnalytics(signupModel);
//                FacebookUtil.saveFacebookUsername(getBaseActivity(),mUsernameFacebook);
                FacebookUtil.saveIsFacebookLoggedInAlready(getBaseActivity());
                final String imagePath = mValues.get(IMAGE_PATH);
                if (!TextUtils.isEmpty(imagePath) && new
                        File(imagePath).exists()) {
                    mSpiceManager.execute(new ProfileImgUploadRequest(imagePath,
                                    "profilePicture"),
                            new ProfilePicUploadListener());
                } else {
                    finishLogin();
                }
            }
        }

        @Override
        public void onRequestException(SpiceException exception) {
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, exception.getMessage());
            SwrveSDK.event(SwrveEvents.LOGIN_FAIL, payload);
            hideProgress();
            Logger.error(exception.getMessage());
        }
    }

    private void finishLogin() {
        ParkApplication.sJustLogged = true;
        ParkApplication.sJustLoggedSecondLevel = true;
        ParkApplication.sJustLoggedThirdLevel = true;
        PreferencesUtil.saveHasEmail(getBaseActivity(),mHasEmail);
        getBaseActivity().finish();
    }

    private class ProfilePicUploadListener extends BaseNeckRequestListener<ProfileImgResponse> {

        @Override
        public void onRequestError(Error error) {
            Logger.error(error.getMessage());
            finishLogin();
        }

        @Override
        public void onRequestSuccessfull(ProfileImgResponse response) {
            if (response != null) {
                ParkApplication.getInstance().setUserProfilePicture(response.getUrl());
                mSpiceManager.execute(new SignupAddPicRequest(mValues.get(USERNAME), response.getUrl()),
                        new PictureUpdateListener());
            }
        }

        @Override
        public void onRequestException(SpiceException exception) {
            MessageUtil.showError(getBaseActivity(), exception.getMessage(),
                    getBaseActivity().getCroutonsHolder());
            Logger.error(exception.getMessage());
            finishLogin();
        }

        class PictureUpdateListener extends BaseNeckRequestListener<SignupAddPicResponse> {

            @Override
            public void onRequestError(Error error) {
                Logger.error(error.getMessage());
                finishLogin();
            }

            @Override
            public void onRequestSuccessfull(SignupAddPicResponse response) {
                if (response != null) {
                    if (!response.isSuccess()) {
                        MessageUtil.showError(getBaseActivity(), R.string.error_img_upload,
                                getBaseActivity().getCroutonsHolder());
                    }
                }
                finishLogin();
            }

            @Override
            public void onRequestException(SpiceException exception) {
                MessageUtil.showError(getBaseActivity(), R.string.error_img_upload,
                        getBaseActivity().getCroutonsHolder());
                Logger.error(exception.getMessage());
                finishLogin();
            }

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

        private TextView view;
        private String userName;
        private String firstName;
        private String nextInitials;

        public FieldAvailabiltyListener(TextView view, boolean FBmode, String userName, String firstName,
                                        String nextInitials) {
            this.view = view;
            this.userName = userName;
            this.firstName = firstName;
            this.nextInitials = nextInitials;
        }

        @Override
        public void onRequestError(Error error) {
            Logger.warn(error.getMessage());
        }

        @Override
        public void onRequestSuccessfull(CheckValueModel value) {
            if (!value.isAvailable()) {
                reMakeUserNameDueToDuplicated(firstName, nextInitials);
            } else {
                view.setText(userName);

                if (!TextUtils.isEmpty(mFbPictureUrl) && checkIfItsDefaultPlaceHolder()) {
                    downloadFacebookProfilePhoto(mFbPictureUrl);
                }
                hideProgress();
            }
        }

        @Override
        public void onRequestException(SpiceException exception) {
            showError(exception.getMessage());
            Logger.warn(exception.getLocalizedMessage());
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private boolean checkIfItsDefaultPlaceHolder() {
        boolean result = false;

        if (getBaseActivity() != null && mProfileImage != null && mProfileImage.getDrawable() != null) {

            Drawable.ConstantState constantState;
            if (DeviceUtils.isDeviceLollipopOrHigher()) {
                constantState = ContextCompat.getDrawable(getBaseActivity(), R.drawable.avatar_ph_image_fit_orange)
                        .getConstantState();
            } else {
                constantState = getResources().getDrawable(R.drawable.avatar_ph_image_fit_orange)
                        .getConstantState();
            }

            if (mProfileImage.getDrawable().getConstantState() == constantState) {
                result = true;
            }
        }
        return result;
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
            showError(getString(R.string.error_cannot_get_address));
        }
    }

}