package com.ebay.park.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import com.ebay.park.base.BaseFragment;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.interfaces.OnShowListenerLollipop;
import com.ebay.park.interfaces.OnShowListenerPreLollipop;
import com.ebay.park.interfaces.PhotoSourceOptionsListener;
import com.ebay.park.model.GroupModel;
import com.ebay.park.model.ZipCodeLocationModel;
import com.ebay.park.requests.GroupCreateRequest;
import com.ebay.park.requests.GroupDeleteRequest;
import com.ebay.park.requests.GroupDetailRequest;
import com.ebay.park.requests.GroupPictureUploadRequest;
import com.ebay.park.requests.GroupShareRequest;
import com.ebay.park.requests.GroupUpdateRequest;
import com.ebay.park.requests.ZipCodeLocationRequest;
import com.ebay.park.responses.GroupDetailResponse;
import com.ebay.park.responses.GroupShareResponse;
import com.ebay.park.responses.ResponseCodes;
import com.ebay.park.responses.ZipCodesResponse.Result;
import com.ebay.park.responses.ZipCodesResponse.Result.Geometry.Location;
import com.ebay.park.utils.Constants;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.DialogUtils;
import com.ebay.park.utils.FacebookUtil;
import com.ebay.park.utils.FileUtils;
import com.ebay.park.utils.KeyboardHelper;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.TwitterUtil;
import com.ebay.park.utils.Validations;
import com.ebay.park.views.ButtonDemi;
import com.ebay.park.views.EditTextBook;
import com.ebay.park.views.TextViewBook;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Create Group Fragment
 *
 * @author Nicol�s Mat�as Fern�ndez
 *
 */

public class GroupCreateFragment extends BaseFragment implements PhotoSourceOptionsListener {

	//TODO: Alternativa para Croutons

	private static final String ID_PARAM = "group_id";

	private static final int REQUEST_IMAGE_CAPTURE = 1;
	private static final int REQUEST_IMAGE_GALLERY = 2;

	private String mImagePath = "";
	private ImageView mPhoto;
	private EditTextBook mName;
	private EditTextBook mDescription;
	private EditTextBook mZipCode;
	private TextViewBook mLocation;
	private ProgressBar mZipCodeProgress;
	private MenuItem mConfirmCreate;
	private boolean mDeterminedByGPS;
	private String mLocationName;
	private String mCoordinates;
	private View mBtnShareFacebook;
	private View mBtnShareTwitter;
	private ButtonDemi mBtnFinish;
	private CallbackManager mCallbackManager;
	private TextViewBook mPhotoError;
	private TextViewBook mNameError;
	private TextViewBook mDescriptionError;
	private TextViewBook mZipcodeError;
	private View mGroupImage;
	private View mGroupImageUpload;
	private ImageView mIvPhotoError;
	private ImageView mAddPhoto;
	private Drawable mErrorDrawable;

	private long mGroupId = -1;
	private boolean mIsEditMode = false;

	private GroupModel mGroup;

	public static GroupCreateFragment forGroup(long groupId) {
		GroupCreateFragment fragment = new GroupCreateFragment();
		Bundle args = new Bundle();
		args.putLong(ID_PARAM, groupId);
		fragment.setArguments(args);
		return fragment;
	}

	public void onSaveInstanceState(Bundle outState) {
		if (outState != null) {
			outState.putString("imagePath", mImagePath);
			outState.putString("locationName", mLocationName);
			outState.putString("coordinates", mCoordinates);
			outState.putBoolean("determinedByGPS", mDeterminedByGPS);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		this.mGroupId = this.getArguments().getLong(ID_PARAM, -1);
		this.mIsEditMode = mGroupId != -1;
		mCallbackManager = CallbackManager.Factory.create();
	}

	@Override
	public void onRefresh() {
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!FacebookSdk.isInitialized()){
			FacebookSdk.sdkInitialize(getActivity());
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		disableRefreshSwipe();
		if (mIsEditMode) {
			setTitle(R.string.edit_group);
			showProgress();
			mSpiceManager.execute(new GroupDetailRequest(mGroupId), new GroupDetailListener());
		} else {
			setTitle(R.string.create_group);
		}
		onRefresh();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup parentView = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
		View baseview = inflater.inflate(R.layout.fragment_group_create, parentView, true);

		mPhoto = (ImageView) baseview.findViewById(R.id.iv_photo);
		mName = (EditTextBook) baseview.findViewById(R.id.et_name);
		mDescription = (EditTextBook) baseview.findViewById(R.id.et_description);
		mZipCode = (EditTextBook) baseview.findViewById(R.id.et_zip_code);
		mZipCode.addTextChangedListener(new ZipCodeWatcher());
		mLocation = (TextViewBook) baseview.findViewById(R.id.tv_group_location);
		mZipCodeProgress = (ProgressBar) baseview.findViewById(R.id.progress_regist_zip_code);
		mPhotoError = (TextViewBook) baseview.findViewById(R.id.error_image_group_label);
		mNameError = (TextViewBook) baseview.findViewById(R.id.error_name_group_label);
		mDescriptionError = (TextViewBook) baseview.findViewById(R.id.error_description_group_label);
		mZipcodeError = (TextViewBook) baseview.findViewById(R.id.error_zc_group_label);
		mGroupImage = baseview.findViewById(R.id.ly_group_image);
		mGroupImageUpload = baseview.findViewById(R.id.ly_group_image_upload);
		mIvPhotoError = (ImageView) baseview.findViewById(R.id.error_image_group);
		mAddPhoto = (ImageView) baseview.findViewById(R.id.iv_add_photo);

		OnClickListener enableButtonClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				v.setSelected(!v.isSelected());
			}
		};

		mBtnShareFacebook = baseview.findViewById(R.id.btn_facebook);
		mBtnShareTwitter = baseview.findViewById(R.id.btn_twitter);
		mBtnShareFacebook.setOnClickListener(enableButtonClickListener);
		mBtnShareTwitter.setOnClickListener(enableButtonClickListener);

		mBtnFinish = (ButtonDemi) baseview.findViewById(R.id.btn_finish);

		mErrorDrawable = getResources().getDrawable(R.drawable.icon_validation_error);
		mErrorDrawable.setBounds(0, 0, mErrorDrawable.getIntrinsicWidth(), mErrorDrawable.getIntrinsicHeight());

		setTwitterCheckboxInitialStatus();
		setFacebookCheckboxInitialStatus();
		View tvAutopublishMsg = baseview.findViewById(R.id.tv_autopublish_message);
		if (!mBtnShareTwitter.isEnabled() || !mBtnShareFacebook.isEnabled()) {
			tvAutopublishMsg.setVisibility(View.VISIBLE);
		} else {
			tvAutopublishMsg.setVisibility(View.GONE);
		}

		mName.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus && !Validations.validateGroupName(mName)) {
					setError(mName, String.format(getString(R.string.invalid_group_name),
							Constants.ITEM_NAME_MIN_LENGTH, Constants.GROUP_NAME_MAX_LENGTH));
				}
				if (!hasFocus && Validations.validateGroupName(mName)) {
					removeError(mName);
				}
			}
		});

		mDescription.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus && !Validations.validateGroupDescription(mDescription)) {
					setError(mDescription, getString(R.string.invalid_group_description));
				}
				if (!hasFocus && Validations.validateGroupDescription(mDescription)) {
					removeError(mDescription);
				}

			}
		});

		mZipCode.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus && !Validations.validateZipCode(mZipCode)) {
					setError(mZipCode, getString(R.string.invalid_zip_code));
				}
			}
		});

		mPhoto.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				promptForPictureOrigin();
			}
		});

		mAddPhoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				promptForPictureOrigin();
			}
		});

		mBtnFinish.setText(mIsEditMode ? R.string.group_delete : R.string.group_create);
		mBtnFinish.setBackgroundResource(mIsEditMode ? R.drawable.btn_delete_group : R.drawable.btn_create_group);
		mBtnFinish.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsEditMode) {
					confirmGroupDeletion();
				} else {
					doCreateGroup();
				}
			}
		});
		if (mIsEditMode){
			mBtnFinish.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_trash_left_margin, 0, 0, 0);
			mBtnFinish.setPadding(15, 0, 0, 0);
		}

		return baseview;
	}

	private void setError(EditText et, String error) {
		et.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_validation_error, 0);

		int id = et.getId();

		if (id == R.id.et_name) {
			mNameError.setText(error);
			mNameError.setVisibility(View.VISIBLE);
		} else if (id == R.id.et_description) {
			mDescriptionError.setText(error);
			mDescriptionError.setVisibility(View.VISIBLE);
		} else if (id == R.id.et_zip_code) {
			mZipcodeError.setText(error);
			mZipcodeError.setVisibility(View.VISIBLE);
		}
	}

	private void removeError(EditText et) {
		et.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

		int id = et.getId();

		if (id == R.id.et_name) {
			mNameError.setVisibility(View.INVISIBLE);
		} else if (id == R.id.et_description) {
			mDescriptionError.setVisibility(View.INVISIBLE);
		} else if (id == R.id.et_zip_code) {
			mZipcodeError.setVisibility(View.INVISIBLE);
		}
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if (savedInstanceState != null) {
			mImagePath = savedInstanceState.getString("imagePath");
			mDeterminedByGPS = savedInstanceState.getBoolean("determinedByGPS");
			mCoordinates = savedInstanceState.getString("coordinates");
			mLocationName = savedInstanceState.getString("locationName");
		}
		createCallback();
	}

	private void createCallback() {
		LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(LoginResult loginResult) {
				FacebookUtil.saveIsPublishPermissionGranted(getContext(), loginResult
						.getAccessToken().getPermissions().contains("publish_actions"));
				if (!mIsEditMode) {
					showProgress();
					mSpiceManager.executeCacheRequestWithProgress(buildCreateGroupRequest(), new GroupCreateRequestListener());
				} else {
					showProgress();
					mSpiceManager.executeCacheRequestWithProgress(buildUpdateGroupRequest(), new GroupUpdateRequestListener());
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.menu_group_create, menu);
		mConfirmCreate = menu.findItem(R.id.action_edit);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (menu.findItem(R.id.action_edit) != null){
			menu.findItem(R.id.action_edit).setVisible(mIsEditMode);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_edit:
				doUpdateGroup();
				break;
			default:
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void showProgress() {
		super.showProgress();
		enableViews(false);
		if (mConfirmCreate != null) {
			mConfirmCreate.setVisible(false);
		}
	}

	private void showError(String message){
		MessageUtil.showError(getBaseActivity(), message, getBaseActivity().getCroutonsHolder());
	}

	private void checkRequest(boolean success) {
		hideProgress();
		if (mConfirmCreate != null){
			mConfirmCreate.setVisible(mIsEditMode || success);
		}
	}

	@Override
	protected void hideProgress() {
		super.hideProgress();
		enableViews(true);
	}

	private void confirmGroupDeletion() {
		final AlertDialog dialog = DialogUtils.getDialogWithLabel(getBaseActivity(), R.string.delete,
				R.string.group_delete_confirm, R.drawable.delete_xs)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						doDeleteGroup();
					}
				})
				.setNegativeButton(R.string.no, null).create();

		if (DeviceUtils.isDeviceLollipopOrHigher()){
			dialog.setOnShowListener(new OnShowListenerLollipop(dialog, getBaseActivity()));
		} else {
			dialog.setOnShowListener(new OnShowListenerPreLollipop(dialog, getBaseActivity()));
		}

		dialog.show();
	}

	private void doCreateGroup() {
		removeImageError();
		if (areFieldsOk()) {
			if (mBtnShareFacebook.isSelected() && !FacebookUtil.getIsPublishPermissionGranted(getActivity())){
				FacebookUtil.requestPublishPermissions(getActivity(),getContext());
			} else {
				showProgress();
				mSpiceManager.executeCacheRequestWithProgress(buildCreateGroupRequest(), new GroupCreateRequestListener());
			}
		}
	}

	private void doUpdateGroup() {
		removeImageError();
		if (areFieldsOk()) {
			if (mBtnShareFacebook.isSelected() && !FacebookUtil.getIsPublishPermissionGranted(getActivity())){
				FacebookUtil.requestPublishPermissions(getActivity(),getContext());
			} else {
				showProgress();
				mSpiceManager.executeCacheRequestWithProgress(buildUpdateGroupRequest(), new GroupUpdateRequestListener());
			}
		}
	}

	private void doDeleteGroup() {
		mSpiceManager.executeCacheRequestWithProgress(new GroupDeleteRequest(mGroupId), new GroupDeleteRequestListener());
	}

	private void enableViews(boolean enabled) {
		mName.setEnabled(enabled);
		mDescription.setEnabled(enabled);
		mZipCode.setEnabled(enabled);
		mPhoto.setEnabled(enabled);
		mBtnFinish.setEnabled(enabled);
	}

	private boolean areFieldsOk() {
		boolean areFieldsOk = true;
		if (!mIsEditMode && TextUtils.isEmpty(mImagePath)) {
			mIvPhotoError.setVisibility(View.VISIBLE);
			mPhotoError.setVisibility(View.VISIBLE);
			areFieldsOk = false;
		}
		if (!Validations.validateGroupName(mName)) {
			setError(mName, String.format(getString(R.string.invalid_group_name),
					Constants.ITEM_NAME_MIN_LENGTH, Constants.GROUP_NAME_MAX_LENGTH));
			areFieldsOk = false;
		}
		if (!Validations.validateGroupDescription(mDescription)) {
			setError(mDescription, getString(R.string.invalid_group_description));
			areFieldsOk = false;
		}
		if (!Validations.validateZipCode(mZipCode) && !mIsEditMode) {
			setError(mZipCode, getString(R.string.invalid_zip_code));
			areFieldsOk = false;
		}
		if (StringUtils.isBlank(mLocation.getText())) {
			setError(mZipCode, getString(R.string.invalid_zip_code));
			areFieldsOk = false;
		}
		return areFieldsOk;
	}

	private void promptForPictureOrigin() {
		PhotoSourceDialogFragment optionsDialog = PhotoSourceDialogFragment.newInstance();
		optionsDialog.setTargetFragment(this, 0);
		optionsDialog.show(getActivity().getSupportFragmentManager(), PhotoSourceDialogFragment.TAG);
	}

	private void takePicture() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(getBaseActivity().getPackageManager()) != null) {
			File photoFile = null;
			try {
				photoFile = FileUtils.createProfileImageFile();
				mImagePath = photoFile.getAbsolutePath();
			} catch (IOException ex) {
				MessageUtil.showError(getBaseActivity(),getString(R.string.error_save_image),
						getBaseActivity().getCroutonsHolder());
				Logger.error(ex.getMessage());
			}
			if (photoFile != null) {
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
				getBaseActivity().startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
			}
		}
	}

	@Override
	public void onCameraClick() {
		takePicture();
	}

	@Override
	public void onGalleryClick() {
		selectPictureFromGallery();
	}

	@Override
	public void onCancelClick() {
	}

	@Override
	public void onBackPressed() {
	}

	private class GroupDetailListener extends BaseNeckRequestListener<GroupModel> {

		@Override
		public void onRequestError(Error error) {
			checkRequest(false);
			Logger.error(error.getMessage());
		}

		@Override
		public void onRequestSuccessfull(GroupModel g) {
			getActivity().supportInvalidateOptionsMenu();
			mGroup = g;

			if (!TextUtils.isEmpty(mGroup.getPictureUrl())) {
				mGroupImage.setVisibility(View.VISIBLE);
				mGroupImageUpload.setVisibility(View.GONE);
				Picasso.with(getBaseActivity()).load(mGroup.getPictureUrl())
						.placeholder(R.drawable.add_image_placeholder).fit().centerCrop().into(mPhoto);
			} else {
				mGroupImage.setVisibility(View.GONE);
				mGroupImageUpload.setVisibility(View.VISIBLE);
			}

			mZipCode.setText(g.getZipCode());

			mName.setText(mGroup.getName());
			if (!TextUtils.isEmpty(mGroup.getDescription())) {
				mDescription.setText(mGroup.getDescription());
			}
			mLocation.setText(mGroup.getLocationName());
			mLocation.setVisibility(View.VISIBLE);
			mZipcodeError.setVisibility(View.GONE);

			checkRequest(true);
		}

		@Override
		public void onRequestException(SpiceException exception) {
			checkRequest(false);
			Logger.error(exception.getMessage());
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			File file = null;
			if (requestCode == REQUEST_IMAGE_CAPTURE) {
				try {
					file = FileUtils.resizeImageFromPath(getActivity(), mImagePath);
					if(file!=null){
						mImagePath = file.getAbsolutePath();
					}
				} catch (IOException e) {
					MessageUtil.showError(getBaseActivity(), R.string.image_loading_unsoported,
							getBaseActivity().getCroutonsHolder());
				}

			} else if (requestCode == REQUEST_IMAGE_GALLERY) {
				Uri selectedImage = data.getData();
				try {
					FileUtils.sErrorSizeShown = false;
					FileUtils.sIsEqorBiggerthan200 = false;
					file = FileUtils.resizeImage(getBaseActivity(), selectedImage);
					if(file!=null){
						mImagePath = file.getAbsolutePath();
					}
				} catch (IOException e) {
					MessageUtil.showError(getBaseActivity(), R.string.image_loading_unsoported,
							getBaseActivity().getCroutonsHolder());
				}
			}  else if (requestCode == FacebookUtil.REQUEST_PUBLISH_FB) {
				mCallbackManager.onActivityResult(requestCode,resultCode,data);
			}
			if (file != null && file.exists()) {
				Picasso.with(getBaseActivity()).load(file).fit().centerCrop().into(mPhoto);
				mGroupImage.setVisibility(View.VISIBLE);
				mGroupImageUpload.setVisibility(View.GONE);
				removeImageError();
			}
		}
	}

	private void removeImageError() {
		mIvPhotoError.setVisibility(View.GONE);
		mPhotoError.setVisibility(View.GONE);
	}

	private void selectPictureFromGallery() {
		Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		getBaseActivity().startActivityForResult(i, REQUEST_IMAGE_GALLERY);
	}

	private String getUserInput(EditText field) {
		return field.getText().toString().trim();
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
			if (!mDeterminedByGPS) {
				final String zipCode = s.toString();
				if (zipCode.length() >= Constants.ZIPCODE_LENGTH) {
					getLocationFromZipCode();
					removeError(mZipCode);
					KeyboardHelper.hide(getBaseActivity(),getView());
					mZipCodeProgress.setVisibility(View.VISIBLE);
				} else {
					mZipcodeError.setVisibility(View.INVISIBLE);
					mLocation.setVisibility(View.GONE);
					mZipCodeProgress.setVisibility(View.GONE);
					mLocation.setText("");
				}
			}
		}

		private void getLocationFromZipCode() {
			mSpiceManager.executeCacheRequest(buildZipCodeRequest(), new ZipCodeRequestListener());
		}

		private ZipCodeLocationRequest buildZipCodeRequest() {
			String zipCode = getUserInput(mZipCode);
			return new ZipCodeLocationRequest(zipCode);
		}

		/**
		 * Listener for zip code responses from Google api
		 */
		private class ZipCodeRequestListener extends BaseNeckRequestListener<ZipCodeLocationModel> {

			@Override
			public void onRequestError(Error error) {
				mZipCodeProgress.setVisibility(View.GONE);
				mLocation.setText("");
				if (error.getMessage().equals("ZERO_RESULTS") || error.getMessage().equals("UNKNOWN_ERROR")) {
					setError(mZipCode, getString(R.string.invalid_zip_code));
				}
			}

			@Override
			public void onRequestSuccessfull(ZipCodeLocationModel zipCodeLocationModel) {
				if (zipCodeLocationModel != null) {
					if (zipCodeLocationModel.getResults() != null && !zipCodeLocationModel.getResults().isEmpty()) {
						ArrayList<String> results = new ArrayList<String>();
						for (Result res : zipCodeLocationModel.getResults()) {
							results.add(res.toString());
						}
						String strLocName = zipCodeLocationModel.getResults().get(0).toString();
						Location loc = zipCodeLocationModel.getResults().get(0).getGeometry().getLocation();
						mLocation.setText(strLocName);
						mCoordinates = loc.getLat() + "," + loc.getLng();
						mLocationName = strLocName;
						mZipcodeError.setVisibility(View.GONE);
						mLocation.setVisibility(View.VISIBLE);
						mZipCodeProgress.setVisibility(View.GONE);
					}
				}
			}

			@Override
			public void onRequestException(SpiceException exception) {
				mZipCodeProgress.setVisibility(View.GONE);
				mLocation.setText("");
				Logger.verb(exception.getLocalizedMessage());
			}
		}
	}

	private class GroupPicUploadListener extends BaseNeckRequestListener<GroupDetailResponse> {

		@Override
		public void onRequestSuccessfull(GroupDetailResponse response) {
			if (response != null) {
				if (mBtnShareFacebook.isSelected() || mBtnShareTwitter.isSelected()) {
					mSpiceManager
							.execute(
									new GroupShareRequest(mGroupId, mBtnShareFacebook.isSelected(), mBtnShareTwitter
											.isSelected()), new GroupShareListener());
				} else {
					MessageUtil.showSuccess(getBaseActivity(),
							getString(mIsEditMode ? R.string.update_group_successfully
									: R.string.create_group_successfully),
							getBaseActivity().getCroutonsHolder());
					if (!mIsEditMode) {
						ParkApplication.sGroupJustCreated = true;
						ScreenManager.showGroupDetailActivity(getBaseActivity(), mGroupId);
					}
					getBaseActivity().finish();
					checkRequest(true);
				}
			}
		}

		@Override
		public void onRequestError(Error error) {
			checkRequest(false);
			Logger.error(error.getMessage());
		}

		@Override
		public void onRequestException(SpiceException exception) {
			checkRequest(false);
			showError(getString(R.string.error_img_upload));
			Logger.error(exception.getMessage());
		}
	}

	private class GroupShareListener extends BaseNeckRequestListener<GroupShareResponse> {

		@Override
		public void onRequestError(Error error) {
			MessageUtil.showSuccess(getBaseActivity(), getString(mIsEditMode ? R.string.update_group_successfully
					: R.string.create_group_successfully), getBaseActivity().getCroutonsHolder());
			getBaseActivity().finish();
		}

		@Override
		public void onRequestSuccessfull(GroupShareResponse t) {
			MessageUtil.showSuccess(getBaseActivity(), getString(mIsEditMode ? R.string.update_group_successfully
					: R.string.create_group_successfully), getBaseActivity().getCroutonsHolder());
			if (!mIsEditMode) {
				ParkApplication.sGroupJustCreated = true;
				ScreenManager.showGroupDetailActivity(getBaseActivity(), mGroupId);
			}
			getBaseActivity().finish();
			checkRequest(true);
		}

		@Override
		public void onRequestException(SpiceException exception) {
			MessageUtil.showSuccess(getBaseActivity(), getString(mIsEditMode ? R.string.update_group_successfully
					: R.string.create_group_successfully), getBaseActivity().getCroutonsHolder());
			getBaseActivity().finish();
		}

	}

	private GroupCreateRequest buildCreateGroupRequest() {
		return new GroupCreateRequest(mName.getText().toString().trim(), mDescription.getText().toString().trim(),
				mCoordinates, mLocationName, mZipCode.getText().toString().trim());
	}

	private GroupUpdateRequest buildUpdateGroupRequest() {
		return new GroupUpdateRequest(mGroupId, mName.getText().toString().trim(), mDescription.getText().toString()
				.trim(), mCoordinates, mLocationName, mZipCode.getText().toString().trim());
	}

	/**
	 * Listener to Create a group.
	 */
	private class GroupCreateRequestListener extends BaseNeckRequestListener<GroupDetailResponse> {

		@Override
		public void onRequestSuccessfull(GroupDetailResponse group) {
			if (group != null) {
				mGroupId = group.getId();
				if (!TextUtils.isEmpty(mImagePath)) {
					mSpiceManager.execute(new GroupPictureUploadRequest(mImagePath, group.getId()),
							new GroupPicUploadListener());
				} else if (mBtnShareFacebook.isSelected() || mBtnShareTwitter.isSelected()) {
					mSpiceManager
							.execute(
									new GroupShareRequest(mGroupId, mBtnShareFacebook.isSelected(), mBtnShareTwitter
											.isSelected()), new GroupShareListener());
				} else {
					MessageUtil.showSuccess(getBaseActivity(), getString(R.string.create_group_successfully),
							getBaseActivity().getCroutonsHolder());
					ParkApplication.sGroupJustCreated = true;
					ScreenManager.showGroupDetailActivity(getBaseActivity(), mGroupId);
					getBaseActivity().finish();
				}
			}
		}

		@Override
		public void onRequestError(Error error) {
			Logger.verb("onRequestError");
			if (error != null) {
				switch (error.getStatus()) {
					case ResponseCodes.FAIL_CODE:
					showError(error.getMessage());
						break;
					default:
						break;
				}
				switch (error.getErrorCode()){
					case ResponseCodes.GroupManagement.GROUP_ALREADY_EXISTS:
						setError(mName, error.getMessage());
						break;
					case ResponseCodes.GroupManagement.INVALID_GROUP_NAME_CHARACTER:
						setError(mName, String.format(getString(R.string.invalid_group_name),
								Constants.ITEM_NAME_MIN_LENGTH, Constants.GROUP_NAME_MAX_LENGTH));
						break;
				}
			}
			checkRequest(false);
		}

		@Override
		public void onRequestNotFound() {
			mSpiceManager.getResultFromCache(buildCreateGroupRequest(), this);
		}

		@Override
		public void onRequestException(SpiceException ex) {
			checkRequest(false);
			showError(ex.getMessage());
			Logger.verb(ex.getLocalizedMessage());
		}
	}

	/**
	 * Listener to update a group.
	 */
	private class GroupUpdateRequestListener extends BaseNeckRequestListener<GroupDetailResponse> {

		@Override
		public void onRequestSuccessfull(GroupDetailResponse group) {
			if (group != null) {
				if (!TextUtils.isEmpty(mImagePath)) {
					mSpiceManager.execute(new GroupPictureUploadRequest(mImagePath, mGroupId),
							new GroupPicUploadListener());
				} else if (mBtnShareFacebook.isSelected() || mBtnShareTwitter.isSelected()) {
					mSpiceManager
							.execute(
									new GroupShareRequest(mGroupId, mBtnShareFacebook.isSelected(), mBtnShareTwitter
											.isSelected()), new GroupShareListener());
				} else {
					MessageUtil.showSuccess(getBaseActivity(), getString(R.string.update_group_successfully),
							getBaseActivity().getCroutonsHolder());
					getBaseActivity().finish();
				}
			}
		}

		@Override
		public void onRequestError(Error error) {
			Logger.verb("onRequestError");
			if (error != null) {
				switch (error.getStatus()) {
					case ResponseCodes.FAIL_CODE:
					showError(error.getMessage());
						break;
					default:
						break;
				}
			}
			switch (error.getErrorCode()){
				case ResponseCodes.GroupManagement.GROUP_ALREADY_EXISTS:
					setError(mName, error.getMessage());
					break;
				case ResponseCodes.GroupManagement.INVALID_GROUP_NAME_CHARACTER:
					setError(mName, String.format(getString(R.string.invalid_group_name),
							Constants.ITEM_NAME_MIN_LENGTH, Constants.GROUP_NAME_MAX_LENGTH));
					break;
			}
			checkRequest(false);
		}

		@Override
		public void onRequestNotFound() {
			mSpiceManager.getResultFromCache(buildCreateGroupRequest(), this);
		}

		@Override
		public void onRequestException(SpiceException ex) {
			checkRequest(false);
			showError(ex.getMessage());
			Logger.verb(ex.getLocalizedMessage());
		}

	}

	private class GroupDeleteRequestListener extends BaseNeckRequestListener<GroupDetailResponse> {

		@Override
		public void onRequestError(Error error) {
			checkRequest(false);
			showError(error.getMessage());
			Logger.verb(error.getMessage());
		}

		@Override
		public void onRequestSuccessfull(GroupDetailResponse g) {
			if (g != null) {
				MessageUtil.showSuccess(getBaseActivity(), getString(R.string.delete_group_successfully),
						getBaseActivity().getCroutonsHolder());
				getBaseActivity().setResult(GroupDetailFragment.RESULT_DELETED);
				getBaseActivity().finish();
			}
			checkRequest(true);
		}

		@Override
		public void onRequestException(SpiceException exception) {
			checkRequest(false);
			showError(exception.getMessage());
			Logger.verb(exception.getMessage());
		}

	}

	private void setTwitterCheckboxInitialStatus() {
		mBtnShareTwitter.setEnabled(TwitterUtil.getIsTwitterLoggedInAlready(getBaseActivity()));
		mBtnShareTwitter.setSelected(false);
	}

	private void setFacebookCheckboxInitialStatus() {
		mBtnShareFacebook.setEnabled(FacebookUtil.getIsFacebookLoggedInAlready(getBaseActivity()));
		mBtnShareFacebook.setSelected(false);
	}

}
