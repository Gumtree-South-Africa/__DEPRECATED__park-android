package com.ebay.park.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.activities.ItemCreateEditActivity;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.model.CategoryModel;
import com.ebay.park.model.ItemModel;
import com.ebay.park.model.PublishItemModel;
import com.ebay.park.model.ZipCodeLocationModel;
import com.ebay.park.requests.ItemCategoryRequest;
import com.ebay.park.requests.ItemCreateRequest;
import com.ebay.park.requests.ItemCreateRequest.PublishRequestPayload;
import com.ebay.park.requests.ItemDeleteRequest;
import com.ebay.park.requests.ItemDetailRequest;
import com.ebay.park.requests.ItemPicturesDeleteRequest;
import com.ebay.park.requests.ItemPicturesUpdateRequest;
import com.ebay.park.requests.ItemUpdateRequest;
import com.ebay.park.requests.ZipCodeLocationRequest;
import com.ebay.park.responses.ItemCategoryResponse;
import com.ebay.park.responses.ItemPicturesUpdateResponse;
import com.ebay.park.responses.ResponseCodes;
import com.ebay.park.responses.ZipCodesResponse;
import com.ebay.park.utils.Constants;
import com.ebay.park.utils.FacebookUtil;
import com.ebay.park.utils.FontsUtil;
import com.ebay.park.utils.FormatUtils;
import com.ebay.park.utils.GroupsLabelViewLight;
import com.ebay.park.utils.KeyboardHelper;
import com.ebay.park.utils.LocationHelper;
import com.ebay.park.utils.LocationHelper.LocationResponseCallback;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.SwrveEvents;
import com.ebay.park.utils.TwitterUtil;
import com.ebay.park.utils.Validations;
import com.ebay.park.views.TextViewBook;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.squareup.picasso.Picasso;
import com.swrve.sdk.SwrveSDK;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Create edit Item Fragment
 *
 * @author Nicol�s Mat�as Fern�ndez
 */

public class ItemCreateEditFragment extends BaseFragment {

    public static final int REQUEST_CAMERA = 1;

    private static final String ID_PARAM = "id";
    private static final String IMAGES_PATHS = "images_paths";

    private static final int MAX_IMAGE_COUNT = 4;
    private static final int MAX_ITEMS_PER_ROW = 2;

    private static final String ID_ARG = "ID";
    private static final String EDIT_MODE_ARG = "EDIT_MODE";
    private static final String CATEGORY_ID_ARG = "CATEGORY_ID";

    private View mLayout;
    private String mImagePath;

    private int mRequestingIndex;

    private ViewGroup mImagesEdition;
    private Spinner mCategories;
    private GroupsLabelViewLight mGroups;
    private View mAddGroup;
    private LinearLayout mMainDataLayout;
    private LinearLayout mGroupsLayout;
    private EditText mName;
    private EditText mDescription;
    private EditText mPrice;
    private EditText mPostalCode;
    private TextView mLocation;
    private TextView mLocationZc;
    private View mLocationView;
    private LinearLayout mLocationLayout;
    private View mZipCodeLayout;
    private ProgressBar mZipCodeProgress;
    private TextViewBook mErrorZipCode;
    private TextViewBook mNameError;
    private TextViewBook mPriceError;
    private TextViewBook mDescError;
    private TextViewBook mCateError;
    private ImageButton mBtnPublishFb;
    private ImageButton mBtnPublishTw;
    private Button mBtnPublish;
    private TextView mCategoryHint;
    private TextView mGroupsHint;
    private CallbackManager mCallbackManager;

    private static String sSelectedLocationId;
    private static String sSelectedLocation;
    private static String sSelectedLatitude;
    private static String sSelectedLongitude;
    private static String sSelectedZipCode;
    public static Boolean sShowPublishMenuItem = false;
    public static LinkedHashMap<String, Integer> sChkBoxList;

    private long mId;

    private List<String> mImagePaths = new ArrayList<String>();
    private String[] mImagesToBeUploaded = {"", "", "", ""};
    private String[] mImagesOnServer = {"", "", "", ""};

    private long mSelectedCategoryId;
    private int mSelectedCategoryPosition = 0;
    private List<CategoryModel> mCategoryList = new ArrayList<CategoryModel>();
    private Boolean mIsEditMode = false;
    private Boolean mAlreadyPublished = false;
    private long mItemPublished = -1;
    private Boolean mResultFromCallback = false;
    private MenuItem mUpdateItem;

    private CustomArrayAdapter mCategoriesAdapter;
    Tracker mGAnalyticsTracker;
    private LocationHelper mLocationHelper;

    /**
     * Get the item detail for a given item id to be edited.
     *
     * @param id The id of the item.
     */
    public static ItemCreateEditFragment forItem(long id) {
        ItemCreateEditFragment fragment = new ItemCreateEditFragment();
        Bundle args = new Bundle();
        args.putLong(ID_PARAM, id);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Get the item detail for a given item id to be edited.
     *
     * @param id
     * @param imagesPaths The id of the item.
     */
    public static ItemCreateEditFragment forItem(long id, String[] imagesPaths) {
        ItemCreateEditFragment fragment = new ItemCreateEditFragment();
        Bundle args = new Bundle();
        args.putLong(ID_PARAM, id);
        args.putStringArray(IMAGES_PATHS, imagesPaths);
        fragment.setArguments(args);
        return fragment;
    }

    TextWatcher tw = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {

            if (s.toString().contains("$")) {
                mPrice.removeTextChangedListener(this);
                s.delete(0, 1);
            }

            String text = s.toString();
            int length = text.length();

            if (!Pattern.matches("^(?!0[0-9])[0-9]{1,6}([.][0-9]{0,2})?$", text)) {
                if (length > 0)
                    s.delete(length - 1, length);
            }

            String userInput = "" + s.toString().replaceAll("[^\\d.]", "");
            StringBuilder cashAmountBuilder = new StringBuilder(userInput);
            mPrice.removeTextChangedListener(this);
            mPrice.setText(cashAmountBuilder.toString());
            mPrice.setTextKeepState("$" + cashAmountBuilder.toString());
            Selection.setSelection(mPrice.getText(), cashAmountBuilder.toString().length() + 1);
            mPrice.addTextChangedListener(this);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mId = getArguments().getLong(ID_PARAM, -1);
        if (getArguments() != null && getArguments().containsKey(IMAGES_PATHS)) {
            String[] paths = getArguments().getStringArray(IMAGES_PATHS);
            for (int i = 0; i < paths.length; i++) {
                mImagePaths.add(paths[i]);
                this.mImagesToBeUploaded[i] = paths[i];
            }
        }

        if (mId == -1) {
            mIsEditMode = false;
        } else {
            mIsEditMode = true;
        }

        if (savedInstanceState != null) {
            mRequestingIndex = savedInstanceState.getInt("requestingIndex");
            mImagePath = savedInstanceState.getString("imagePath");
            mId = savedInstanceState.getLong(ID_ARG);
            mSelectedCategoryId = savedInstanceState.getLong(CATEGORY_ID_ARG);
            mSelectedCategoryPosition = savedInstanceState.getInt("selectedCategoryPosition");
            mIsEditMode = savedInstanceState.getBoolean(EDIT_MODE_ARG);

            sSelectedLocation = savedInstanceState.getString("selectedLocation");
            sSelectedLatitude = savedInstanceState.getString("selectedLatitude");
            sSelectedLongitude = savedInstanceState.getString("selectedLongitude");

            mImagePaths = savedInstanceState.getStringArrayList("imagePaths");
            mImagesToBeUploaded = savedInstanceState.getStringArray("imagesToBeUploaded");
            mImagesOnServer = savedInstanceState.getStringArray("imagesOnServer");

            String json = savedInstanceState.getString("categoryList");
            if (json != null) {
                mCategoryList = new Gson().fromJson(json, new TypeToken<List<CategoryModel>>() {
                }.getType());
            }

        }
        mCallbackManager = CallbackManager.Factory.create();

        if (mIsEditMode){
            SwrveSDK.event(SwrveEvents.EDIT_AD_BEGIN);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("requestingIndex", mRequestingIndex);
        outState.putString("imagePath", mImagePath);

        outState.putLong(ID_ARG, mId);
        outState.putInt("selectedCategoryPosition", mCategories.getSelectedItemPosition());

        outState.putLong(CATEGORY_ID_ARG, mSelectedCategoryId);
        outState.putBoolean(EDIT_MODE_ARG, mIsEditMode);

        outState.putString("selectedLocation", sSelectedLocation);
        outState.putString("selectedLatitude", sSelectedLatitude);
        outState.putString("selectedLongitude", sSelectedLongitude);

        outState.putStringArrayList("imagePaths", (ArrayList<String>) mImagePaths);
        outState.putStringArray("imagesToBeUploaded", mImagesToBeUploaded);
        outState.putStringArray("imagesOnServer", mImagesOnServer);

        if (mCategoryList != null) {
            outState.putString("categoryList", new Gson().toJson(mCategoryList));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!FacebookSdk.isInitialized()) {
            FacebookSdk.sdkInitialize(getActivity());
        }
        fillGroupsBox();
        if (mAlreadyPublished) {
            getBaseActivity().finish();
            ScreenManager.showItemDetailActivity(getBaseActivity(), mItemPublished, null, null);
        }
    }

    private void fillGroupsBox() {
        if (mGroups.getObjects() != null) {
            mGroups.getObjects().clear();
        }
        if (sChkBoxList != null) {
            if (sChkBoxList.size() > 0) {
                ArrayList<String> selectedGroups = new ArrayList<String>(sChkBoxList.keySet());
                mGroups.setVisibility(View.VISIBLE);
                for (int i = 0; i < selectedGroups.size(); i++) {
                    mGroups.setText("");
                    mGroups.addObject(selectedGroups.get(i));
                }
            } else {
                mGroups.setText("");
                mGroups.setVisibility(View.GONE);
            }
        } else {
            mGroups.setText("");
            mGroups.setVisibility(View.GONE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_create_edit,
                (ViewGroup) super.onCreateView(inflater, container, savedInstanceState), true);
    }

    public void onViewCreated(View baseview, Bundle savedInstanceState) {
        super.onViewCreated(baseview, savedInstanceState);
        mLayout = baseview.findViewById(R.id.publish_main_layout);
        mImagesEdition = (ViewGroup) baseview.findViewById(R.id.ly_images_edition);
        mMainDataLayout = (LinearLayout) baseview.findViewById(R.id.ly_main_data);
        mLocationLayout = (LinearLayout) baseview.findViewById(R.id.location_layout);
        mGroupsLayout = (LinearLayout) baseview.findViewById(R.id.groups_layout);
        mName = (EditText) baseview.findViewById(R.id.et_name);
        mDescription = (EditText) baseview.findViewById(R.id.et_description);
        mLocation = (TextView) baseview.findViewById(R.id.tv_location);
        mLocationZc = (TextView) baseview.findViewById(R.id.tv_location_zc);
        mLocationView = baseview.findViewById(R.id.ly_edit_location);
        mZipCodeLayout = baseview.findViewById(R.id.ly_regist_zip_code);
        TextView tvEditLocation = (TextView) baseview.findViewById(R.id.tv_edit_location);
        mErrorZipCode = (TextViewBook) baseview.findViewById(R.id.tv_error_zip_code);
        mNameError = (TextViewBook) baseview.findViewById(R.id.error_name_item_label);
        mPriceError = (TextViewBook) baseview.findViewById(R.id.error_price_item_label);
        mDescError = (TextViewBook) baseview.findViewById(R.id.error_desc_item_label);
        mCateError = (TextViewBook) baseview.findViewById(R.id.error_cate_item_label);
        mPrice = (EditText) baseview.findViewById(R.id.et_price);
        mGroups = (GroupsLabelViewLight) baseview.findViewById(R.id.et_groups);
        mCategories = (Spinner) baseview.findViewById(R.id.spCategories);
        mAddGroup = baseview.findViewById(R.id.iv_add_group);
        mBtnPublishFb = (ImageButton) baseview.findViewById(R.id.btn_share_fb);
        mBtnPublishTw = (ImageButton) baseview.findViewById(R.id.btn_share_tw);
        mBtnPublish = (Button) baseview.findViewById(R.id.btn_publish);
        mCategoryHint = (TextView) baseview.findViewById(R.id.tv_category_hint);
        mGroupsHint = (TextView) baseview.findViewById(R.id.tv_groups_hint);

        setTwitterCheckboxInitialStatus();
        setFacebookCheckboxInitialStatus();

        if (mIsEditMode) {
            setTitle(getResources().getString(R.string.title_publish_edit));
        } else {
            setTitle(getResources().getString(R.string.title_publish));
        }

        View tvAutopublishMsg = baseview.findViewById(R.id.tv_autopublish_message);
        if (!mBtnPublishTw.isEnabled() || !mBtnPublishFb.isEnabled()) {
            tvAutopublishMsg.setVisibility(View.VISIBLE);
        } else {
            tvAutopublishMsg.setVisibility(View.GONE);
        }

        sSelectedLocationId = "";
        sSelectedLocation = "";
        sSelectedLatitude = "";
        sSelectedLongitude = "";
        sSelectedZipCode = "";

        mName.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!Validations.validateItemName(mName)) {
                        setError(mName);
                    } else {
                        removeError(mName);
                    }
                }
            }
        });

        mDescription.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (!Validations.validateItemDescription(mDescription)) {
                        setError(mDescription);
                    } else {
                        removeError(mDescription);
                    }
                }
            }
        });

        mPrice.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus && !Validations.validatePrice(mPrice)) {
                    setError(mPrice);
                }
                if (!hasFocus && Validations.validatePrice(mPrice)) {
                    removeError(mPrice);
                }
            }
        });

        mCategories.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                KeyboardHelper.hide(getBaseActivity(),getView());
                return false;
            }
        }) ;

        if (mIsEditMode) {

            mLocationLayout.setVisibility(View.VISIBLE);
            mLocation.setVisibility(View.VISIBLE);
            mLocationZc.setVisibility(View.VISIBLE);
            mLocationView.setVisibility(View.VISIBLE);

            mPostalCode = (EditText) baseview.findViewById(R.id.et_regist_zip_code);
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

            tvEditLocation.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    mLocationView.setVisibility(View.GONE);
                    mLocation.setVisibility(View.GONE);
                    mLocationZc.setVisibility(View.GONE);
                    mPostalCode.setText("");
                    mLocation.setText("");
                    mPostalCode.requestFocus();
                    KeyboardHelper.show(getBaseActivity(), mPostalCode);
                    mZipCodeLayout.setVisibility(View.VISIBLE);
                }
            });
        } else {
            mLocationHelper = new LocationHelper(getBaseActivity().getApplicationContext(), new LocationResponseCallback() {

                @Override
                public void setZipCode(String zipCode) {
                    if (!TextUtils.isEmpty(zipCode) && !LocationHelper.ZIP_CODE_NOT_USA.equals(zipCode)) {
                        sSelectedZipCode = zipCode;
                        sSelectedLocationId = "-1";
                    }
                }

                @Override
                public void setAddress(String address) {

                    if (!TextUtils.isEmpty(sSelectedZipCode) && !LocationHelper.ZIP_CODE_NOT_USA.equals(address)) {
                        if (!TextUtils.isEmpty(address)) {
                            sSelectedLocation = address;
                            String[] latLng = mLocationHelper.getLatlng().split(",");
                            sSelectedLatitude = latLng[0];
                            sSelectedLongitude = latLng[1];
                        }
                    }
                }

            });
        }

        mAddGroup.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ScreenManager.showUserGroupsActivity(getBaseActivity());
            }
        });

        mBtnPublishFb.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mBtnPublishFb.isSelected()) {
                    mBtnPublishFb.setSelected(false);
                } else {
                    mBtnPublishFb.setSelected(true);
                }
            }
        });

        mBtnPublishTw.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mBtnPublishTw.isSelected()) {
                    mBtnPublishTw.setSelected(false);
                } else {
                    mBtnPublishTw.setSelected(true);
                }
            }
        });

        mBtnPublish.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SwrveSDK.event(SwrveEvents.POST_AD_FREE_ATTEMPT);
                doPublishItem();
            }
        });

        mPrice.addTextChangedListener(tw);

        setupImageEditionContainer();

        if (savedInstanceState != null) {
            if (!mIsEditMode) {
                updateCategoriesAdapter();
            }
        }

        hideAndShowImagePlaceholders();
        createCallback();
    }

    private void setError(EditText et) {
        if (et != null) {
            et.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_validation_error, 0);
            int id = et.getId();
            if (id == R.id.et_name) {
                mNameError.setText(String.format(getString(R.string.invalid_item_name),
                        Constants.ITEM_NAME_MIN_LENGTH, Constants.ITEM_NAME_MAX_LENGTH));
                mNameError.setVisibility(View.VISIBLE);
            } else if (id == R.id.et_description) {
                mDescError.setVisibility(View.VISIBLE);
            } else if (id == R.id.et_price) {
                mPriceError.setVisibility(View.VISIBLE);
            } else if (id == R.id.et_regist_zip_code) {
                mErrorZipCode.setVisibility(View.VISIBLE);
            }
        } else {
            mCateError.setVisibility(View.VISIBLE);
        }
    }

    private void removeError(EditText et) {
        if (et != null) {
            et.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            int id = et.getId();
            if (id == R.id.et_name) {
                mNameError.setVisibility(View.GONE);
            } else if (id == R.id.et_description) {
                mDescError.setVisibility(View.GONE);
            } else if (id == R.id.et_price) {
                mPriceError.setVisibility(View.GONE);
            } else if (id == R.id.et_regist_zip_code) {
                mErrorZipCode.setVisibility(View.GONE);
            }
        } else {
            mCateError.setVisibility(View.GONE);
        }
    }

    private void createCallback() {
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                FacebookUtil.saveIsPublishPermissionGranted(getContext(), loginResult
                        .getAccessToken().getPermissions().contains("publish_actions"));
                if (!mIsEditMode) {
                    showProgress();
                    mSpiceManager.execute(buildPublishItemRequest(), new PublishItemRequestListener());
                } else {
                    showProgress();
                    mSpiceManager.execute(buildUpdateItemRequest(), new UpdateItemRequestListener());
                }
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {

            }
        });
    }

    @Override
    public void onBackPressed() {
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

            if (isAdded()) {
                mPostalCode.setHint(getResources().getString(R.string.enter_zip_code));
                final String zipCode = s.toString();
                if (zipCode.length() >= Constants.ZIPCODE_LENGTH) {
                    getLocationFromZipCode();
                    removeError(mPostalCode);
                    KeyboardHelper.hide(getBaseActivity(),getView());
                    mZipCodeProgress.setVisibility(View.VISIBLE);
                } else {
                    mLocationView.setVisibility(View.INVISIBLE);
                    mZipCodeProgress.setVisibility(View.GONE);
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

    private String getUserInput(EditText field) {
        return field.getText().toString().trim();
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
                    KeyboardHelper.hide(getBaseActivity(),getView());
                    ArrayList<String> results = new ArrayList<String>();
                    for (ZipCodesResponse.Result res : zipCodeLocationModel.getResults()) {
                        results.add(res.toString());
                    }
                    String strLocName = zipCodeLocationModel.getResults().get(0).toString();
                    ZipCodesResponse.Result.Geometry.Location loc = zipCodeLocationModel.getResults().get(0).getGeometry().getLocation();
                    sSelectedLongitude = String.valueOf(loc.getLng());
                    sSelectedLatitude = String.valueOf(loc.getLat());
                    sSelectedLocation = strLocName;
                    sSelectedZipCode = zipCodeLocationModel.getResults().get(0).getAddressComponents().get(0).getLong_name().toString();
                    mLocation.setText(sSelectedLocation);
                    mLocationZc.setText("(" + mPostalCode.getText() + ")");
                    mLocationView.setVisibility(View.VISIBLE);
                    mLocation.setVisibility(View.VISIBLE);
                    mLocationZc.setVisibility(View.VISIBLE);
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


    private void setupImageEditionContainer() {
        int reminder = MAX_IMAGE_COUNT % MAX_ITEMS_PER_ROW;

        int requiredRows = reminder > 0 ? MAX_IMAGE_COUNT / MAX_ITEMS_PER_ROW + 1 : MAX_IMAGE_COUNT / MAX_ITEMS_PER_ROW;

        OnClickListener onImageClickedListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsEditMode && !checkPicturesList()) {
                    ScreenManager.showCameraActivityEdition(getBaseActivity(), mImagePaths);
                } else {
                    ScreenManager.showCameraActivityCreation(getBaseActivity(), mImagePaths);
                }
            }
        };

        this.mImagesEdition.removeAllViews();
        for (int i = 0, index = 0; i < requiredRows; i++) {
            LinearLayout ll = new LinearLayout(getBaseActivity());
            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            lp.gravity = Gravity.CENTER_HORIZONTAL;

            ll.setLayoutParams(lp);

            for (int j = 0; j < MAX_ITEMS_PER_ROW; j++) {
                View placeholder = this.getPlaceholder(ll, onImageClickedListener, index);
                placeholder.setVisibility(index == 0 ? View.VISIBLE : View.INVISIBLE);
                ll.addView(placeholder);
                index++;
            }

            ll.setVisibility(i == 0 ? View.VISIBLE : View.GONE);
            this.mImagesEdition.addView(ll);
        }
    }

    private View getPlaceholder(ViewGroup parent, OnClickListener onImageClicked,
                                int index) {
        View placeholder = LayoutInflater.from(this.getBaseActivity()).inflate(R.layout.layout_image_placeholder,
                parent, false);

        View photo = placeholder.findViewById(R.id.iv_photo);
        View principal = placeholder.findViewById(R.id.iv_photo_principal);

        placeholder.setTag(index);
        photo.setTag(index);
        if (index == 0) {
            principal.setVisibility(View.VISIBLE);
        }
        photo.setOnClickListener(onImageClicked);

        return placeholder;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        disableRefreshSwipe();
        if (mIsEditMode) {
            showProgress();
            mBtnPublish.setVisibility(View.GONE);
            mAddGroup.setVisibility(View.VISIBLE);
            mGroupsHint.setVisibility(View.VISIBLE);
            mGroupsLayout.setVisibility(View.VISIBLE);
        } else {
            mBtnPublish.setVisibility(View.VISIBLE);
            mAddGroup.setVisibility(View.GONE);
            mGroupsHint.setVisibility(View.GONE);
            mGroupsLayout.setVisibility(View.GONE);
        }

        mSpiceManager.getResultFromCache(new ItemCategoryRequest(), new CategoriesListener());

        if (savedInstanceState == null) {
            sChkBoxList = null;
        }

        mGAnalyticsTracker = ParkApplication.getInstance().getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_done, menu);
        mUpdateItem = menu.findItem(R.id.action_done);
        if (mUpdateItem != null) {
            mUpdateItem.setVisible(mIsEditMode && sShowPublishMenuItem);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                SwrveSDK.event(SwrveEvents.EDIT_AD_ATTEMPT);
                doUpdateItem();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void showProgress() {
        super.showProgress();
        enableViews(false);
        sShowPublishMenuItem = false;
        if (mUpdateItem != null) {
            mUpdateItem.setVisible(false);
        }
        getBaseActivity().getSupportActionBar().setHomeButtonEnabled(false);
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    protected void hideProgress() {
        super.hideProgress();
        enableViews(true);
        sShowPublishMenuItem = true;
        if (getBaseActivity() != null && getBaseActivity().getSupportActionBar() != null) {
            getBaseActivity().getSupportActionBar().setHomeButtonEnabled(true);
        }
        if (getActivity() != null) {
            getActivity().supportInvalidateOptionsMenu();
        }
    }

    private void enableViews(boolean enabled) {
        for (int i = 0; i < MAX_IMAGE_COUNT; i++) {
            View placeholder = this.getPlaceholderForIndex(i);
            placeholder.findViewById(R.id.iv_photo).setEnabled(enabled);
        }
        for (int i = 0; i < mMainDataLayout.getChildCount(); i++) {
            View child = mMainDataLayout.getChildAt(i);
            child.setEnabled(enabled);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                mImagesToBeUploaded = data.getStringArrayExtra(ItemCreateEditActivity.EXTRA_IMAGES_PATHS);
                mImagePaths.clear();
                mImagePaths = new ArrayList<>(Arrays.asList(mImagesToBeUploaded));
            } else if (requestCode == FacebookUtil.REQUEST_PUBLISH_FB) {
                mResultFromCallback = true;
                mCallbackManager.onActivityResult(requestCode, resultCode, data);
            }

            this.hideAndShowImagePlaceholders();
        }
    }

    private void hideAndShowImagePlaceholders() {
        for (int i = 0; i < this.mImagePaths.size(); i++) {
            System.out.println("INDEX: " + i);
            this.getRowForIndex(i).setVisibility(View.VISIBLE);
            View placeholder = this.getPlaceholderForIndex(i);
            placeholder.setVisibility(View.VISIBLE);
            String path = mImagePaths.get(i);
            File imageFile = new File(path);

            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            mImagesEdition.setLayoutParams(lp);
            placeholder.getLayoutParams().height = dpToPx(168);
            placeholder.setPadding(0, 0, 0, 0);
            ((RelativeLayout) placeholder).setGravity(Gravity.CENTER);
            (placeholder.findViewById(R.id.iv_photo)).getLayoutParams().width = dpToPx(168);
            (placeholder.findViewById(R.id.iv_photo)).getLayoutParams().height = dpToPx(168);
            (placeholder.findViewById(R.id.iv_photo)).requestLayout();

            if (imageFile.exists()) {
                Picasso.with(getBaseActivity()).load(imageFile).resize(200, 200).centerCrop()
                        .into((ImageView) placeholder.findViewById(R.id.iv_photo));
            } else {
                if (TextUtils.isEmpty(path)) {
                    path = null;
                }
                Picasso.with(getBaseActivity()).load(path).resize(200, 200).centerCrop()
                        .into((ImageView) placeholder.findViewById(R.id.iv_photo));

            }

            (placeholder.findViewById(R.id.iv_rounded_shape)).setVisibility(View.VISIBLE);
        }

        for (int i = this.mImagePaths.size() + 1; i < MAX_IMAGE_COUNT; i++) {
            this.getRowForIndex(i).setVisibility(View.GONE);
            View placeholder = this.getPlaceholderForIndex(i);
            this.restorePlaceholder(placeholder, i);
            placeholder.setVisibility(View.INVISIBLE);
        }

        if (this.mImagePaths.size() < MAX_IMAGE_COUNT) {
            View placeholder = this.getPlaceholderForIndex(this.mImagePaths.size());
            this.getRowForIndex(this.mImagePaths.size()).setVisibility(View.VISIBLE);
            this.restorePlaceholder(placeholder, this.mImagePaths.size());
        }
    }

    public static int dpToPx(int dp) {
        return (int) (dp * (Resources.getSystem().getDisplayMetrics().densityDpi / 160f));
    }

    private View getPlaceholderForIndex(int index) {
        ViewGroup row = this.getRowForIndex(index);
        View placeholder = row.getChildAt(index % MAX_ITEMS_PER_ROW);
        return placeholder;
    }

    private ViewGroup getRowForIndex(int index) {
        if (index >= MAX_IMAGE_COUNT) {
            return null;
        }

        return (ViewGroup) this.mImagesEdition.getChildAt(index / MAX_ITEMS_PER_ROW);
    }

    private void restorePlaceholder(View placeholder, int position) {
        placeholder.setVisibility(View.VISIBLE);

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        mImagesEdition.setLayoutParams(lp);
        if (position == 2) {
            placeholder.getLayoutParams().height = dpToPx(70);
            placeholder.setPadding(45, 10, 0, 0);
            mImagesEdition.getLayoutParams().height = dpToPx(250);
            ((RelativeLayout) placeholder).setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        } else {
            ((RelativeLayout) placeholder).setGravity(Gravity.CENTER);
        }

        (placeholder.findViewById(R.id.iv_photo)).getLayoutParams().width = dpToPx(55);
        (placeholder.findViewById(R.id.iv_photo)).getLayoutParams().height = dpToPx(55);
        (placeholder.findViewById(R.id.iv_photo)).requestLayout();
        Picasso.with(getBaseActivity()).load(R.drawable.btn_add_picture)
                .into((ImageView) placeholder.findViewById(R.id.iv_photo));
        (placeholder.findViewById(R.id.iv_rounded_shape)).setVisibility(View.GONE);
    }

    private String capitalizeText(String text){
        return (text.length() > 1 ? text.substring(0,1).toUpperCase() + text.substring(1).toLowerCase() :
                text.toUpperCase());
    }

    /**
     * Spinner Array Adapter for the data of the list (Custom Row)
     *
     * @author Nicol�s Mat�as Fern�ndez
     */

    private class CustomArrayAdapter extends ArrayAdapter<CategoryModel> {
        public CustomArrayAdapter(Context ctx, List<CategoryModel> objects) {
            super(ctx, android.R.layout.simple_spinner_item, objects);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = null;
            TextView text = null;

            if (position == 0) {
                TextView tv = new TextView(getContext());
                tv.setHeight(0);
                tv.setVisibility(View.GONE);
                view = tv;
            } else {
                view = super.getView(position, null, parent);
                text = (TextView) view.findViewById(android.R.id.text1);
                text.setPadding(pxToDp(120), pxToDp(70), pxToDp(60), pxToDp(70));
                text.setTextSize(14);
                text.setTypeface(FontsUtil.getFSBook(getBaseActivity()));
                text.setTextColor(view.getResources().getColor(R.color.system_notification));
                if (position == mCategories.getSelectedItemPosition()) {
                    text.setBackgroundColor(view.getResources().getColor(R.color.SelectedCategory));
                } else {
                    text.setBackgroundColor(view.getResources().getColor(R.color.White));
                }
            }

            return view;
        }
    }

    public static int pxToDp(int px) {
        return (int) (px / (Resources.getSystem().getDisplayMetrics().densityDpi / 160f));
    }

    // Validate all required data:
    private boolean areFieldsOk() {
        boolean areFieldsOk = true;

        if (!Validations.validateItemName(mName)) {
            setError(mName);
            areFieldsOk = false;
        }
        if (!Validations.validateItemDescription(mDescription)) {
            setError(mDescription);
            areFieldsOk = false;
        }
        if (!Validations.validatePrice(mPrice)) {
            setError(mPrice);
            areFieldsOk = false;
        }

        if (!mIsEditMode) {
            if (mImagePaths.isEmpty()) {
                MessageUtil.showError(getBaseActivity(), R.string.publish_picture_not_selected,
                        getBaseActivity().getCroutonsHolder());
                areFieldsOk = false;
            }
        } else {
            if (StringUtils.isBlank(mLocation.getText())) {
                setError(mPostalCode);
                areFieldsOk = false;
            }
        }

        if (mSelectedCategoryId == 0) {
            setError(null);
            areFieldsOk = false;
        } else {
            removeError(null);
        }
        return areFieldsOk;
    }

    private void showError(String message) {
        MessageUtil.showError(getBaseActivity(), message,
                getBaseActivity().getCroutonsHolder());
    }

    // Checks that no picture requires upload, update or delete:
    private boolean checkPicturesList() {
        for (String path : mImagesToBeUploaded) {
            if (!TextUtils.isEmpty(path)) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<Integer> createGroupsArray() {

        ArrayList<Integer> selectedGroups = new ArrayList<Integer>();

        if (sChkBoxList != null) {
            if (sChkBoxList.size() > 0) {
                selectedGroups = new ArrayList<Integer>(sChkBoxList.values());
            }
        }
        return selectedGroups;
    }

    private void errorRequest() {
        if (mUpdateItem != null) {
            mUpdateItem.setVisible(mIsEditMode);
        }
    }

    // Builds the Body Request to Publish an Item:
    private ItemCreateRequest buildPublishItemRequest() {

        PublishRequestPayload oPublish = new PublishRequestPayload();

        oPublish.setName(capitalizeText(mName.getText().toString().trim()));
        oPublish.setDescription((TextUtils.isEmpty(mDescription.getText().toString())) ?
                mDescription.getText().toString().trim() : capitalizeText(mDescription.getText().toString().trim()));
        oPublish.setLocation(sSelectedLocationId);
        oPublish.setPrice(Double.valueOf(mPrice.getText().toString().trim().replace("$", "")));
        oPublish.setShareOnFacebook(mBtnPublishFb.isSelected());
        oPublish.setShareOnTwitter(mBtnPublishTw.isSelected());
        oPublish.setCategoryId(mSelectedCategoryId);
        oPublish.setGroups(createGroupsArray());
        oPublish.setLatitude(sSelectedLatitude);
        oPublish.setLongitude(sSelectedLongitude);
        oPublish.setLocationName(sSelectedLocation.trim());
        oPublish.setZipCode(sSelectedZipCode);

        return new ItemCreateRequest(oPublish);
    }

    // Builds the Body Request to Update an Item:
    private ItemUpdateRequest buildUpdateItemRequest() {

        Double _lat = -1000.0;
        Double _long = -1000.0;
        if (!TextUtils.isEmpty(sSelectedLatitude)) {
            _lat = Double.valueOf(sSelectedLatitude);
        }
        if (!TextUtils.isEmpty(sSelectedLongitude)) {
            _long = Double.valueOf(sSelectedLongitude);
        }

        return new ItemUpdateRequest.Builder(mId, true).name(capitalizeText(mName.getText().toString()))
                .description((TextUtils.isEmpty(mDescription.getText().toString())) ?
                        mDescription.getText().toString().trim() : capitalizeText(mDescription.getText().toString().trim()))
                .location(sSelectedLocationId)
                .price(Double.valueOf(mPrice.getText().toString().trim().replace("$", ""))).latitude(_lat)
                .longitude(_long).locationName(sSelectedLocation.trim()).zipCode(sSelectedZipCode)
                .category(mSelectedCategoryId).groups(createGroupsArray()).shareOnFacebook(mBtnPublishFb.isSelected())
                .shareOnTwitter(mBtnPublishTw.isSelected()).build();
    }

    // Start publish process to create a new item, if fields are ok:
    private void doPublishItem() {
        if (areFieldsOk()) {
            if (mBtnPublishFb.isSelected() && !FacebookUtil.getIsPublishPermissionGranted(getActivity())) {
                mResultFromCallback = true;
                FacebookUtil.requestPublishPermissions(getActivity(), getContext());
            } else {
                showProgress();
                mSpiceManager.execute(buildPublishItemRequest(), new PublishItemRequestListener());
            }
        }
    }

    // Start update process to edit an item, if fields are ok:
    private void doUpdateItem() {
        if (areFieldsOk()) {
            if (mBtnPublishFb.isSelected() && !FacebookUtil.getIsPublishPermissionGranted(getActivity())) {
                FacebookUtil.requestPublishPermissions(getActivity(), getContext());
            } else {
                showProgress();
                mSpiceManager.execute(buildUpdateItemRequest(), new UpdateItemRequestListener());
            }
        } else {
            MessageUtil.showError(getBaseActivity(), R.string.check_fields_with_errors,
                    getBaseActivity().getCroutonsHolder());
        }
    }

    /**
     * Listener for Publish item responses from Park server.
     */
    private class PublishItemRequestListener extends BaseNeckRequestListener<PublishItemModel> {

        @Override
        public void onRequestSuccessfull(PublishItemModel publish) {
            mAlreadyPublished = true;
            mItemPublished = publish.getId();
            SwrveSDK.event(SwrveEvents.POST_AD_FREE_SUCCESS);
            if (publish != null) {
                mId = publish.getId();

                ParkApplication.getInstance().getEventsLogger().logEvent(FacebookUtil.EVENT_CREATE_ITEM_COMPLETE);

                // Build and send "Item publication completed" Event.
                mGAnalyticsTracker.send(new HitBuilders.EventBuilder().setCategory("Item publication")
                        .setAction("Item publication completed").setLabel("The item has been created!").build());

                mSpiceManager.execute(new ItemPicturesUpdateRequest(mIsEditMode, mId, mImagePaths),
                        new ItemPictureUploadUpdateListener());
            }
        }

        @Override
        public void onRequestError(Error error) {
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, error.getMessage());
            SwrveSDK.event(SwrveEvents.POST_AD_FREE_FAIL, payload);
            Logger.verb("onRequestError");
            hideProgress();
            fullFocusDownScrollView();
            showError(error.getMessage());

        }

        @Override
        public void onRequestNotFound() {
            mSpiceManager.getResultFromCache(buildPublishItemRequest(), this);
        }

        @Override
        public void onRequestException(SpiceException ex) {
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, ex.getMessage());
            SwrveSDK.event(SwrveEvents.POST_AD_FREE_FAIL, payload);
            hideProgress();
            fullFocusDownScrollView();
            showError(ex.getMessage());
            Logger.verb(ex.getLocalizedMessage());
        }

    }

    private void deleteItemPictureFlow() {
        // Counts the amount of images on BE before any update was made
        int imagesOnServerCount = 0;
        for (String url : mImagesOnServer) {
            if (!TextUtils.isEmpty(url)) {
                imagesOnServerCount++;
            }
        }

        List imagesToDelete = new ArrayList<Integer>();
        // Will iterate over the paths to find the next to delete only if:
        // 1) Are less or equal to the max image count = 4, and
        // 2) Are less than the amount of images on the BE
        // Otherwhise there is no need to delete any image
        for (int i = mImagePaths.size(); i < MAX_IMAGE_COUNT && mImagePaths.size() < imagesOnServerCount; i++) {
            if (i >= mImagesOnServer.length) {
                break;
            } else if (!TextUtils.isEmpty(mImagesOnServer[i])) {
                mImagesOnServer[i] = "";
                imagesToDelete.add(i+1);
            }
        }

        if (imagesToDelete.size() > 0) {
            mSpiceManager.execute(new ItemPicturesDeleteRequest(mId, imagesToDelete), new ItemPictureDeleteListener());
        } else {
            completeUpdateStep();
        }
    }

    private void completeUpdateStep() {
        getBaseActivity().finish();
        if (mIsEditMode) {
            ScreenManager.showItemDetailActivity(getBaseActivity(), mId, null, null);
        } else {
            ScreenManager.showItemPublishedScreen(getBaseActivity(), mId);
        }
    }

    /**
     * Listener for Delete item responses from Park server.
     */
    private void deleteWrongItem(Long itemId) {
        mSpiceManager.execute(new ItemDeleteRequest(itemId),
                new BaseNeckRequestListener<PublishItemModel>() {

                    @Override
                    public void onRequestError(Error error) {
                        Logger.verb("onRequestError");
                    }

                    @Override
                    public void onRequestSuccessfull(PublishItemModel model) {
                        Logger.verb("onRequestSuccessfull");
                    }

                    @Override
                    public void onRequestException(SpiceException exception) {
                        Logger.error(exception.getMessage());
                    }
                });
    }

    /**
     * Listener for Update item responses from Park server.
     */
    private class UpdateItemRequestListener extends BaseNeckRequestListener<PublishItemModel> {

        @Override
        public void onRequestSuccessfull(PublishItemModel update) {
            if (update != null) {
                SwrveSDK.event(SwrveEvents.EDIT_AD_SUCCESS);
                if (checkPicturesList()) {
                    mSpiceManager.execute(
                            new ItemPicturesUpdateRequest(!mIsEditMode, mId, Arrays.asList(mImagesToBeUploaded)),
                            new ItemPictureUploadUpdateListener());
                } else {
                    deleteItemPictureFlow();
                }
            }
        }

        @Override
        public void onRequestError(Error error) {
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, error.getMessage());
            SwrveSDK.event(SwrveEvents.EDIT_AD_FAIL, payload);
            Logger.verb("onRequestError");
            showError(error.getMessage());
            hideProgress();
            errorRequest();
            fullFocusDownScrollView();
        }

        @Override
        public void onRequestNotFound() {
            mSpiceManager.getResultFromCache(buildPublishItemRequest(), this);
        }

        @Override
        public void onRequestException(SpiceException ex) {
            Map<String,String> payload = new HashMap<>();
            payload.put(SwrveEvents.EVENT_FAIL_KEY, ex.getMessage());
            SwrveSDK.event(SwrveEvents.EDIT_AD_FAIL, payload);
            hideProgress();
            errorRequest();
            fullFocusDownScrollView();
            showError(ex.getMessage());
            Logger.verb(ex.getLocalizedMessage());
        }

    }

    /**
     * Listener for update an item picture
     */
    private class ItemPictureUploadUpdateListener extends BaseNeckRequestListener<ItemPicturesUpdateResponse> {

        @Override
        public void onRequestSuccessfull(ItemPicturesUpdateResponse response) {
            if (response != null) {
                deleteItemPictureFlow();
            }
        }

        @Override
        public void onRequestError(Error error) {
            deleteWrongItem(mId);
            Logger.verb("onRequestError");
            showError(error.getMessage());
            errorRequest();
            hideProgress();
            fullFocusDownScrollView();

        }

        @Override
        public void onRequestException(SpiceException exception) {
            deleteWrongItem(mId);
            hideProgress();
            errorRequest();
            fullFocusDownScrollView();
            MessageUtil.showError(getBaseActivity(), exception.getMessage(), getBaseActivity().getCroutonsHolder());
            Logger.error(exception.getMessage());
        }
    }

    /**
     * Listener for delete an item picture
     */
    private class ItemPictureDeleteListener extends BaseNeckRequestListener<ItemPicturesUpdateResponse> {

        @Override
        public void onRequestSuccessfull(ItemPicturesUpdateResponse model) {
            completeUpdateStep();
        }

        @Override
        public void onRequestError(Error error) {
            Logger.verb("onRequestError");
            if (error != null) {
                switch (error.getStatus()) {
                    case ResponseCodes.FAIL_CODE:
                        showError(getString(R.string.delete_failed));
                        break;

                    case ResponseCodes.UpdateItemPictures.USER_UNAUTHORIZED:
                        showError(getString(R.string.update_user_unauthorized));
                        break;

                    case ResponseCodes.UpdateItemPictures.ITEM_NOT_FOUND:
                        showError(getString(R.string.update_item_not_found));
                        break;

                    case ResponseCodes.UpdateItemPictures.ITEM_DOESNT_BELONG_TO_USER:
                        showError(getString(R.string.update_item_doesnt_belong_to_user));
                        break;

                    case ResponseCodes.DeleteItemPictures.DELETING_PICTURE:
                        showError(getString(R.string.deleting_picture));
                        break;

                    case ResponseCodes.DeleteItemPictures.PICTURE_NOT_FOUND:
                        showError(getString(R.string.delete_picture_not_found));
                        break;

                    case ResponseCodes.DeleteItemPictures.CANNOT_DELETE_PICTURE:
                        showError(getString(R.string.delete_picture_cannot_be_deleted));
                        break;

                    default:
                        break;
                }
            }
            hideProgress();
            errorRequest();
            fullFocusDownScrollView();
        }

        @Override
        public void onRequestException(SpiceException exception) {
            hideProgress();
            errorRequest();
            fullFocusDownScrollView();
            showError(exception.getMessage());
            Logger.error(exception.getMessage());
        }
    }

    private void fullFocusDownScrollView() {
        final ScrollView scrollview = (ScrollView) mLayout;
        scrollview.post(new Runnable() {
            @Override
            public void run() {
                scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    /**
     * Listener for get the data of the item to edit
     */
    private class ItemDetailListener extends BaseNeckRequestListener<ItemModel> {

        @Override
        public void onRequestError(Error error) {
            hideProgress();
            errorRequest();
            fullFocusDownScrollView();
            showError(error.getMessage());
            Logger.error(error.getMessage());
        }

        @Override
        public void onRequestSuccessfull(ItemModel item) {
            hideProgress();
            mLayout.setVisibility(View.VISIBLE);

            mName.setText(item.getName());
            mDescription.setText(item.getDescription());
            mPrice.setText(FormatUtils.formatPriceWithoutCurrency(item.getPrice()));
            if (!TextUtils.isEmpty(item.getLocation())) {
                if (!TextUtils.isEmpty(item.getZipCode())) {
                    mLocation.setText(item.getLocationName());
                    mLocationZc.setText("(" + item.getZipCode() + ")");
                } else {
                    mLocation.setText(item.getLocationName());
                }
                mLocation.setVisibility(View.VISIBLE);
                mLocationZc.setVisibility(View.VISIBLE);
                sSelectedLocationId = item.getLocation();
                sSelectedLocation = item.getLocationName();
                sSelectedZipCode = item.getZipCode();
                sSelectedLatitude = String.valueOf(item.getLatitude());
                sSelectedLongitude = String.valueOf(item.getLongitude());
            }

            updateCategoriesAdapter();
            for (int i = 0; i < mCategories.getAdapter().getCount(); i++) {
                if (item.getCategory().getId() == (((CategoryModel) mCategories.getAdapter().getItem(i)).getId())) {
                    mCategories.setSelection(i);
                    break;
                }
            }

            if (item.getGroups() != null) {
                if (item.getGroups().size() > 0) {
                    sChkBoxList = new LinkedHashMap<String, Integer>();
                    for (int i = 0; i < item.getGroups().size(); i++) {
                        sChkBoxList.put(item.getGroups().get(i).getName(), item.getGroups().get(i).getId());
                    }
                    fillGroupsBox();
                }
            }

            mImagePaths.clear();
            if (item.getPictures().size() > 0) {
                for (int i = 0; i < item.getPictures().size() && i < MAX_IMAGE_COUNT; i++) {
                    if (!TextUtils.isEmpty(item.getPictures().get(i))) {
                        System.out.println("ADDING: " + i);
                        mImagePaths.add(item.getPictures().get(i));
                        mImagesOnServer[i] = item.getPictures().get(i);
                    }
                }
            }
            hideAndShowImagePlaceholders();
        }

        @Override
        public void onRequestException(SpiceException exception) {
            hideProgress();
            errorRequest();
            fullFocusDownScrollView();
            MessageUtil.showError(getBaseActivity(), exception.getMessage(), getBaseActivity().getCroutonsHolder());
            Logger.error(exception.getMessage());
        }
    }

    /**
     * Listener for get the category list
     */
    private class CategoriesListener extends BaseNeckRequestListener<ItemCategoryResponse> {

        @Override
        public void onRequestError(Error error) {
            hideProgress();
            errorRequest();
            Toast.makeText(getBaseActivity(), "No se pudieron cargar las categor�as", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRequestSuccessfull(ItemCategoryResponse response) {
            if (!mResultFromCallback) {
                hideProgress();
            }
            if (response == null) {
                return;
            }
            if (mCategoriesAdapter == null) {
                mCategoryList.clear();
                CategoryModel ghost = new CategoryModel(0, getResources().getString(R.string.select_category));
                ghost.setSelectable(true);
                mCategoryList.add(ghost);
                mCategoryList.addAll(response.getCategories());

                if (mIsEditMode) {
                    mSpiceManager.execute(new ItemDetailRequest(mId), new ItemDetailListener());
                } else {
                    updateCategoriesAdapter();
                }
            }
        }

        @Override
        public void onRequestNotFound() {
            mSpiceManager.getResultFromCache(new ItemCategoryRequest(), new CategoriesListener());
            if (!mResultFromCallback) {
                hideProgress();
                errorRequest();
            }
        }

        @Override
        public void onRequestException(SpiceException exception) {
            hideProgress();
            errorRequest();
            MessageUtil.showError(getBaseActivity(), exception.getMessage(), getBaseActivity().getCroutonsHolder());
        }
    }

    private ArrayList<CategoryModel> getCategories() {
        ArrayList<CategoryModel> cleanCategories = new ArrayList<>();
        Iterator<CategoryModel> aIterator = mCategoryList.iterator();
        while (aIterator.hasNext()) {
            CategoryModel oCategory = aIterator.next();
            if (oCategory.getSelectable() != null) {
                if (oCategory.getSelectable()) {
                    cleanCategories.add(oCategory);
                }
            }
        }
        return cleanCategories;
    }

    @Override
    public void onRefresh() {
    }

    @Override
    public void onStart() {
        super.onStart();
        mSpiceManager.addListenerIfPending(new ItemCategoryRequest(), new CategoriesListener());
    }

    private void setTwitterCheckboxInitialStatus() {
        if (!TwitterUtil.getIsTwitterLoggedInAlready(getBaseActivity())) {
            mBtnPublishTw.setEnabled(false);
        } else {
            mBtnPublishTw.setEnabled(true);
        }
    }

    private void setFacebookCheckboxInitialStatus() {
        if (!FacebookUtil.getIsFacebookLoggedInAlready(getBaseActivity())) {
            mBtnPublishFb.setEnabled(false);
        } else {
            mBtnPublishFb.setEnabled(true);
            mBtnPublishFb.setSelected(true);
        }
    }

    private void updateCategoriesAdapter() {
        mCategoriesAdapter = new CustomArrayAdapter(getActivity(), getCategories());

        mCategories.setAdapter(mCategoriesAdapter);
        mCategories.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                TextView firstCat = (TextView) parent.getChildAt(0);
                mSelectedCategoryId = mCategoriesAdapter.getItem(pos).getId();
                if (firstCat != null && mCategoryHint != null) {

                    firstCat.setTextSize(13);
                    firstCat.setTypeface(FontsUtil.getFSBook(getBaseActivity()));
                    firstCat.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.arrow_combo, 0);
                    if (!firstCat.getText().toString().equals(getString(R.string.select_category))) {
                        firstCat.setTextColor(getResources().getColor(R.color.system_notification));
                        mCateError.setVisibility(View.GONE);
                    } else {
                        firstCat.setTextColor(getResources().getColor(R.color.feed_filter_grey));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        if (mSelectedCategoryPosition != 0) {
            mCategories.setSelection(mSelectedCategoryPosition);
        }
    }
}