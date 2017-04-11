package com.ebay.park.fragments;

import android.app.Activity;
import android.content.Intent;
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
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.interfaces.PhotoSourceOptionsListener;
import com.ebay.park.model.MyProfileModel;
import com.ebay.park.model.ProfileModel;
import com.ebay.park.model.ZipCodeLocationModel;
import com.ebay.park.requests.ProfileImgUploadRequest;
import com.ebay.park.requests.ProfileRequest;
import com.ebay.park.requests.ProfileUpdateRequest;
import com.ebay.park.requests.ProfileUpdateRequest.Builder;
import com.ebay.park.requests.ZipCodeLocationRequest;
import com.ebay.park.responses.ProfileImgResponse;
import com.ebay.park.responses.ZipCodesResponse.Result.Geometry.Location;
import com.ebay.park.utils.Constants;
import com.ebay.park.utils.FacebookUtil;
import com.ebay.park.utils.FileUtils;
import com.ebay.park.utils.KeyboardHelper;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.views.EditTextMedium;
import com.ebay.park.views.TextViewBook;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

/**
 * Edit Profile Fragment
 *
 * @author federico.perez
 * @author Nicol�s Mat�as Fern�ndez
 * @author Jonatan Collard Bovy
 *
 */

public class EditProfileFragment extends BaseFragment implements PhotoSourceOptionsListener {
	private static final int REQUEST_IMAGE_CAPTURE = 1;
	private static final int REQUEST_IMAGE_GALLERY = 2;

	private View mLayout;
	private ImageView mProfileImage;
	private EditTextMedium mEtZipCode;
	private ProgressBar mPbZipCode;
	private TextViewBook mTvLocationName;
	private TextViewBook mLocationError;

	private ProfileModel mProfileInfo;

	private String mImagePath;
	private String mLocationName;
	private String mCoordinates;
	private String mZipCode;
	private TextViewBook mUsername;
	private TextViewBook mEmail;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("imagePath", mImagePath);
		outState.putString("mLocationName", mLocationName);
		outState.putString("mCoordinates", mCoordinates);
		outState.putString("mZipCode", (mEtZipCode.getText() != null) ? mEtZipCode.getText().toString() : "");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mImagePath = savedInstanceState.getString("imagePath");
			mLocationName = savedInstanceState.getString("mLocationName");
			mCoordinates = savedInstanceState.getString("mCoordinates");
			mZipCode = savedInstanceState.getString("mZipCode");
		}

		setTitle(R.string.edit_profile);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.menu_update_profile, menu);
		if (menu.findItem(R.id.action_ok) != null) {
			menu.findItem(R.id.action_ok).setVisible(false);
		}
		if (menu.findItem(R.id.action_cancel) != null) {
			menu.findItem(R.id.action_cancel).setVisible(true);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (menu.findItem(R.id.action_ok) != null){
			menu.findItem(R.id.action_ok).setVisible(
					!TextUtils.isEmpty(mZipCode) && !TextUtils.isEmpty(mLocationName)
							&& mZipCode.length() == Constants.ZIPCODE_LENGTH && mTvLocationName.getVisibility() == View.VISIBLE);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_ok) {
			Toast toast = Toast.makeText(getContext(), getString(R.string.processing), Toast.LENGTH_SHORT);
			View view = toast.getView();
			view.setBackgroundResource(R.drawable.custom_toast);
			toast.show();
			updateProfile();
			return true;
		}
		if (item.getItemId() == R.id.action_cancel) {
			getBaseActivity().onSupportNavigateUp();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View baseview = inflater.inflate(R.layout.fragment_profile_edit,
				(ViewGroup) super.onCreateView(inflater, container, savedInstanceState), true);
		return baseview;
	}

	@Override
	public void onViewCreated(View rootView, Bundle savedInstanceState) {
		super.onViewCreated(rootView, savedInstanceState);

		mLayout = rootView;
		mLayout.setVisibility(View.INVISIBLE);

		mProfileImage = (ImageView) rootView.findViewById(R.id.iv_profile_edit_picture);
		mUsername = (TextViewBook) rootView.findViewById(R.id.tv_profile_edit_username);
		mEtZipCode = (EditTextMedium) rootView.findViewById(R.id.et_profile_edit_zipcode);
		mPbZipCode = (ProgressBar) rootView.findViewById(R.id.pb_profile_edit_zip_code);
		mTvLocationName = (TextViewBook) rootView.findViewById(R.id.tv_profile_edit_location);
		mLocationError = (TextViewBook) rootView.findViewById(R.id.tv_profile_edit_error);
		mEmail = (TextViewBook) rootView.findViewById(R.id.tv_profile_edit_mail);

		mProfileImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				promptForPictureOrigin();
			}
		});

		mEtZipCode.addTextChangedListener(new ZipCodeWatcher());

		if (savedInstanceState != null) {
			updatePhotosViewOnReCreation();
		}
	}

	private void updatePhotosViewOnReCreation() {
		if (mImagePath != null) {
			try {
				File file = FileUtils.resizeImageFromPath(getActivity(), mImagePath);
				if (file != null && file.exists()) {
					Picasso.with(getBaseActivity()).load(file).placeholder(R.drawable.avatar_big_ph_image_fit_gray).fit().centerCrop()
							.into(mProfileImage);
				}
			} catch (NullPointerException e) {
				MessageUtil.showError(getBaseActivity(), R.string.image_loading_unsoported,
						getBaseActivity().getCroutonsHolder());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		disableRefreshSwipe();
		showProgress();
		onRefresh();
	}

	@Override
	public void onRefresh() {
		mSpiceManager
				.execute(new ProfileRequest(ParkApplication.getInstance().getUsername()), new ProfileInfoListener());
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			File file = null;
			if (requestCode == REQUEST_IMAGE_CAPTURE) {
				try {
					file = FileUtils.resizeImageFromPath(getActivity(), mImagePath);
					mImagePath = file.getAbsolutePath();
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
					if (file != null) {
						mImagePath = file.getAbsolutePath();
					}
				} catch (Exception e) {
					MessageUtil.showError(getBaseActivity(), R.string.image_loading_unsoported,
							getBaseActivity().getCroutonsHolder());
				}
			}

			if (file != null && file.exists()) {
				Picasso.with(getBaseActivity()).load(file).placeholder(R.drawable.avatar_big_ph_image_fit_gray).fit().centerCrop()
						.into(mProfileImage);
			}
		} else {
			mImagePath = null;
		}
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
				Logger.error(ex.getMessage());
			}
			if (photoFile != null) {
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
				startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
			}
		}
	}

	private void selectPictureFromGallery() {
		Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, REQUEST_IMAGE_GALLERY);
	}

	private void showError(String message) {
		mLocationError.setText(message);
		mLocationError.setVisibility(View.VISIBLE);
	}

	private void updateProfile() {
		if (mImagePath != null) {
			mSpiceManager.execute(new ProfileImgUploadRequest(mImagePath, "profilePicture"),
					new BaseNeckRequestListener<ProfileImgResponse>() {

						@Override
						public void onRequestError(Error error) {
							MessageUtil.showError(getBaseActivity(), error.getMessage(),
									getBaseActivity().getCroutonsHolder());
						}

						@Override
						public void onRequestSuccessfull(ProfileImgResponse response) {
							mSpiceManager.execute(getUpdateRequest().withPicture(response.getUrl()).build(),
									new ProfileUpdateListener(response.getUrl()));
						}

						@Override
						public void onRequestException(SpiceException exception) {
							hideProgress();
							MessageUtil.showError(getBaseActivity(), exception.getMessage(),
									getBaseActivity().getCroutonsHolder());
							Logger.error(exception.getMessage());
						}
					});
		} else {
			mSpiceManager.execute(getUpdateRequest().build(), new ProfileUpdateListener());
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

	private class ZipCodeWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			mZipCode = s.toString();
			getActivity().invalidateOptionsMenu();
			if (mZipCode.length() >= Constants.ZIPCODE_LENGTH) {
				mTvLocationName.setVisibility(View.GONE);
				mSpiceManager.executeCacheRequest(new ZipCodeLocationRequest(mZipCode), new ZipCodeRequestListener());
				mPbZipCode.setVisibility(View.VISIBLE);
			} else {
				mLocationError.setVisibility(View.GONE);
				mTvLocationName.setVisibility(View.GONE);
				mPbZipCode.setVisibility(View.GONE);
			}
		}
	}

	private Builder getUpdateRequest() {
		ProfileUpdateRequest.Builder builder = new ProfileUpdateRequest.Builder(mProfileInfo.getName(),
				mProfileInfo.getLastname());
		if (!TextUtils.isEmpty(mCoordinates) && !TextUtils.isEmpty(mLocationName) && !TextUtils.isEmpty(mZipCode)) {
			builder.withLocation(mCoordinates, mLocationName);
			builder.withZipCode(mZipCode);
		}
		return builder;
	}

	private class ZipCodeRequestListener extends BaseNeckRequestListener<ZipCodeLocationModel> {

		@Override
		public void onRequestError(Error error) {
			mPbZipCode.setVisibility(View.GONE);
			mLocationName = null;
			getActivity().invalidateOptionsMenu();
			if (error.getMessage().equals("ZERO_RESULTS") || error.getMessage().equals("UNKNOWN_ERROR")) {
				showError(getString(R.string.error_no_locations_for_this_zip_code));
			} else {
				showError(error.getMessage());
			}
		}

		@Override
		public void onRequestSuccessfull(ZipCodeLocationModel zipCodeLocationModel) {
			if (zipCodeLocationModel != null) {
				if (zipCodeLocationModel.getResults() != null && !zipCodeLocationModel.getResults().isEmpty()) {
					mTvLocationName.setVisibility(View.VISIBLE);
					mLocationName = zipCodeLocationModel.getResults().get(0).toString();
					mCoordinates = getCoordinates(zipCodeLocationModel.getResults().get(0).getGeometry().getLocation());
					mTvLocationName.setText(mLocationName);
					mPbZipCode.setVisibility(View.GONE);
				}
			}
			getActivity().invalidateOptionsMenu();
		}

		@Override
		public void onRequestException(SpiceException exception) {
			mPbZipCode.setVisibility(View.GONE);
			mLocationName = null;
			getActivity().invalidateOptionsMenu();
			Logger.verb(exception.getLocalizedMessage());
			showError(getString(R.string.error_cannot_get_address));
		}
	}

	private class ProfileUpdateListener extends BaseNeckRequestListener<MyProfileModel> {

		String mProfilePicture;

		ProfileUpdateListener(String profilePicture) {
			this.mProfilePicture = profilePicture;
		}

		ProfileUpdateListener() {
		}

		@Override
		public void onRequestError(Error error) {
			hideProgress();
			MessageUtil.showError(getBaseActivity(), error.getMessage(),
					getBaseActivity().getCroutonsHolder());
		}

		@Override
		public void onRequestSuccessfull(MyProfileModel profile) {
			MessageUtil.showSuccess(getBaseActivity(), getString(R.string.profile_updated),
					getBaseActivity().getCroutonsHolder());
			if (!TextUtils.isEmpty(mProfilePicture)) {
				ParkApplication.getInstance().setUserProfilePicture(mProfilePicture);
			}
			ParkApplication.getInstance().getEventsLogger().logEvent(FacebookUtil.EVENT_EDIT_ZIP_CODE);
			KeyboardHelper.hide(getBaseActivity(),getBaseActivity().getCurrentFocus());
			getBaseActivity().onBackPressed();
		}

		@Override
		public void onRequestException(SpiceException exception) {
			hideProgress();
			MessageUtil.showError(getBaseActivity(), exception.getMessage(),
					getBaseActivity().getCroutonsHolder());
			Logger.error(exception.getMessage());
		}

	}

	private class ProfileInfoListener extends BaseNeckRequestListener<ProfileModel> {

		@Override
		public void onRequestError(Error error) {
			MessageUtil.showError(getBaseActivity(), error.getMessage(),
					getBaseActivity().getCroutonsHolder());
			hideProgress();
		}

		@Override
		public void onRequestSuccessfull(ProfileModel profile) {
			mProfileInfo = profile;
			mUsername.setText(profile.getUsername());
			mEmail.setText(profile.getEmail());
			updateProfileLocation(profile);
			if (!TextUtils.isEmpty(profile.getProfilePicture()) && mImagePath == null) {
				Picasso.with(getBaseActivity()).load(profile.getProfilePicture()).placeholder(R.drawable.avatar_big_ph_image_fit_gray)
						.fit().centerCrop().into(mProfileImage);
			}
			hideProgress();
			getActivity().invalidateOptionsMenu();
			mLayout.setVisibility(View.VISIBLE);
		}

		@Override
		public void onRequestException(SpiceException exception) {
			Logger.error(exception.getMessage());
			hideProgress();
			MessageUtil.showError(getBaseActivity(), exception.getMessage(),
					getBaseActivity().getCroutonsHolder());
		}
	}

	public static final String getCoordinates(Location aLocation) {
		return aLocation.getLat() + "," + aLocation.getLng();
	}

	private void updateProfileLocation(ProfileModel profile) {
		if (!TextUtils.isEmpty(profile.getZipCode())) {
			mZipCode = profile.getZipCode();
			mEtZipCode.setText(mZipCode);
		} else {
			mEtZipCode.setHint(R.string.current_zip_code);
		}
		mTvLocationName.setVisibility(View.VISIBLE);
		mLocationName = profile.getLocationName();
		mTvLocationName.setText(mLocationName);
	}

}
