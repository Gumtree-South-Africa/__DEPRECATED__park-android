package com.ebay.park.fragments;


import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.activities.ItemCreateEditActivity;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.interfaces.CameraOptionsListener;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.FacebookUtil;
import com.ebay.park.utils.FileUtils;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.SwrveEvents;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.swrve.sdk.SwrveSDK;

import java.io.*;
import java.util.*;

import eu.janmuller.android.simplecropimage.CropImage;

/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends BaseFragment implements CameraOptionsListener {

    public static final String TAG = "CameraFragment";
    private static final int PERMISSION_REQUEST_CAMERA = 23;
    private static final int REQUEST_IMAGE_GALLERY = 1;
    private static final int REQUEST_IMAGE_CROP = 2;
    private static final int IMAGE_MAX_WIDTH = 1280;
    private static final int IMAGE_MAX_HEIGHT = 1024;

    private static final String IMAGE_PATHS_PARAM = "IMAGE_PATHS_PARAM";
    private static final String IMAGE_PATHS_NOT_PUBLISHED = "IMAGE_PATHS_NOT_PUBLISHED";
    private static final String ACTION_DIALOG = "show_dialog";
    private static final String ACTION_PRINCIPAL = "set_principal";
    private static final String ACTION_NEXT = "next_step";

    private int mCameraId;
    private Camera mCamera;
    private String mFlashMode = Camera.Parameters.FLASH_MODE_AUTO;

    private View mTitle;
    private SurfaceView mPreview;
    private SurfaceHolder mSurfaceHolder;
    private ImageView mBtnSwitchCamera;
    private ImageView mBtnFlashMode;
    private ImageView mBtnGallery;
    private ImageView mBtnTakePhoto;
    private ImageView mClose;
    private ImageView mPhoto1;
    private ImageView mPhoto2;
    private ImageView mPhoto3;
    private ImageView mPhoto4;
    private ImageView mEdit1;
    private ImageView mEdit2;
    private ImageView mEdit3;
    private ImageView mEdit4;
    private ImageView mPhoto1mask;
    private ImageView mPhoto2mask;
    private ImageView mPhoto3mask;
    private ImageView mPhoto4mask;
    private ImageView mCompleteIcon;
    private TextView mCompleteHint;
    private TextView mCompleteInstruction;
    private LinearLayout mComplete;
    private TextView mNextStep;
    private TextView mDialogHint;
    private View mPreviewPlaceholder;

    private int mViewClicked = 0;
    private Boolean mFirstTime = true;
    private List<ImageCamera> mImagesList = new ArrayList<>();
    private int mPictureWidth = 0;
    private int mPictureHeight = 0;
    private Boolean mLoadedWithPreviousImages = false;
    private Boolean mTakePhotoButtonEnabled = false;
    private File mBkpPhoto;

    public enum ImageState {
        DOWNLOADED, PENDING
    }

    public CameraFragment() {
    }

    public static CameraFragment forEdition(String[] someImagePaths) {
        CameraFragment fragment = new CameraFragment();
        Bundle args = new Bundle();
        args.putStringArray(IMAGE_PATHS_PARAM, someImagePaths);
        fragment.setArguments(args);
        return fragment;
    }

    public static CameraFragment forCreation(String[] someImagePaths) {
        CameraFragment fragment = new CameraFragment();
        Bundle args = new Bundle();
        args.putStringArray(IMAGE_PATHS_NOT_PUBLISHED, someImagePaths);
        fragment.setArguments(args);
        return fragment;
    }

    private void loadEditionImages() {
        if (getArguments() != null && getArguments().containsKey(IMAGE_PATHS_PARAM)){
            mLoadedWithPreviousImages = true;
            mFirstTime = false;

            List<String> imageUrls = new ArrayList<>(Arrays.asList(getArguments().getStringArray(IMAGE_PATHS_PARAM)));
            imageUrls.removeAll(Collections.singleton(null));

            initializePendingImages(imageUrls,ImageState.PENDING);
            loadImagePreviews(imageUrls);

            for (int i = 0; i < imageUrls.size(); i++) {
                downloadImage(imageUrls.get(i), i, imageUrls.size());
            }
        }
    }

    private void initializePendingImages(List<String> path, ImageState pending){
        for (int i=0; i < path.size(); i++) {
            switch (pending){
                case PENDING:
                    mImagesList.add(new ImageCamera(pending, path.get(i), path.get(i)));
                    break;
                case DOWNLOADED:
                    mImagesList.add(new ImageCamera(pending, "", path.get(i)));
                    break;
                default:
                    break;
            }
        }
    }

    private void loadImagePreviews(List<String> urls){
        for (int i = 0; i < urls.size(); i++) {
            Picasso.with(getBaseActivity()).load(urls.get(i)).resize(200, 200).centerCrop()
                    .into(getProperImageView(i + 1));
            getProperEditImageView(i + 1).setVisibility(View.VISIBLE);

            setGrayBorder(getProperMaskImageView(i + 1));
        }
        setPrincipal();
        updateState();
        if (mImagesList.size() == 4){
            setAllLoadedStep();
        }
    }

    /**
     * Download image when entering the screen.
     *
     * @param aImagePath
     * @param pos
     * @param total
     */
    private void downloadImage(final String aImagePath, final int pos, final int total) {
        Picasso.with(getActivity().getApplicationContext()).load(aImagePath).into(new Target() {

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
                        mImagesList.remove(pos);
                        mImagesList.add(pos, new ImageCamera(ImageState.DOWNLOADED, aImagePath, photoFile.getAbsolutePath()));
                    } catch (IOException ex) {
                        MessageUtil.showError(getBaseActivity(), R.string.error_save_image,
                                getBaseActivity().getCroutonsHolder());
                        Logger.error(ex.getMessage());
                    }

                    FileOutputStream ostream = new FileOutputStream(photoFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                    ostream.close();

                    loadImage(pos, photoFile);

                    if (mImagesList.size() == total) {
                        if (mImagesList.size() < 4) {
                            updateState();
                        } else {
                            hideControls();
                            setAllLoadedStep();
                        }
                    }

                } catch (FileNotFoundException e) {
                    mFirstTime = true;
                } catch (IOException e) {
                    mFirstTime = true;
                } catch (NullPointerException e) {
                    mFirstTime = true;
                } catch (IllegalArgumentException e) {
                    mFirstTime = true;
                }
            }

            @Override
            public void onBitmapFailed(Drawable arg0) {
                Logger.error("Position not loaded: " + pos);
                return;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup parentView = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        if (getArguments() == null){
            ParkApplication.getInstance().getEventsLogger().logEvent(FacebookUtil.EVENT_CREATE_ITEM_BEGIN);
            SwrveSDK.event(SwrveEvents.POST_AD_BEGIN);
        }
        return inflater.inflate(R.layout.fragment_camera, parentView, true);
    }

    @Override
    public void onRefresh() {
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mCamera == null) {
            restartPreview();
        }
    }

    @Override
    public void onStop() {
        // Stop the preview
        if (mCamera != null) {
            stopCameraPreview();
            mCamera.release();
            mCamera = null;
        }

        super.onStop();
    }

    private void setPrincipal(){
        mPhoto1mask.setBackgroundDrawable(getResources().getDrawable(R.drawable.mask_cam_image_thumb_main));
        mPhoto1mask.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        disableRefreshSwipe();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            File imageFile = null;
            if (requestCode == REQUEST_IMAGE_GALLERY) {
                Uri selectedImage = data.getData();
                try {
                    FileUtils.sErrorSizeShown = false;
                    FileUtils.sIsEqorBiggerthan200 = false;
                    imageFile = FileUtils.resizeImage(getActivity(), selectedImage);
                } catch (IOException e) {
                    MessageUtil.showError(getBaseActivity(), R.string.image_loading_unsoported,
                            getBaseActivity().getCroutonsHolder());
                }

                if (imageFile != null && imageFile.exists()) {
                    runCropImage(imageFile.getPath());
                }
            } else if (requestCode == REQUEST_IMAGE_CROP){
                String path = data.getStringExtra(CropImage.IMAGE_PATH);

                // if image received
                if (path != null) {
                    try {
                        FileUtils.sErrorSizeShown = false;
                        FileUtils.sIsEqorBiggerthan200 = false;
                        imageFile = FileUtils.resizeImageFromPath(getBaseActivity(),path);
                        if(imageFile == null) {
                            imageFile = mBkpPhoto;
                        }
                        if (imageFile!=null && imageFile.exists()) {
                            if (mViewClicked != 0){ // Comes from dialog
                                mImagesList.set(mViewClicked-1, new ImageCamera(ImageState.DOWNLOADED,
                                        "", imageFile.getAbsolutePath()));
                                removeClicked();
                                mViewClicked = 0;
                            } else { // Comes from gallery
                                mImagesList.add(new ImageCamera(ImageState.DOWNLOADED, "", imageFile.getAbsolutePath()));
                            }
                        }
                        showImagesTaken();
                    } catch (IOException e) {
                        MessageUtil.showError(getBaseActivity(), R.string.image_loading_unsoported,
                                getBaseActivity().getCroutonsHolder());
                        if (mViewClicked != 0) {
                            removeImageClicked();
                        }
                    } catch (NullPointerException e) {
                        MessageUtil.showError(getBaseActivity(), R.string.image_loading_unsoported,
                                getBaseActivity().getCroutonsHolder());
                        if (mViewClicked != 0) {
                            removeImageClicked();
                        }
                    }
                }
            }
        } else {
            if (requestCode == REQUEST_IMAGE_CROP && mViewClicked != 0){
                removeClicked();
                if (mViewClicked == 1){
                    setPrincipal();
                }
                mViewClicked = 0;
            }
        }
    }

    private void removeImageClicked() {
        removeClicked();
        if (mImagesList.size() == 4){
            mComplete.setVisibility(View.GONE);
            mNextStep.setText(getString(R.string.next_step));
            showControls();
        }
        mImagesList.remove(mViewClicked - 1);
        mTakePhotoButtonEnabled = true;
        clearImagesViews();
        showImagesTaken();
        mViewClicked = 0;
    }

    private void runCropImage(String path) {

        try {
            mBkpPhoto = FileUtils.resizeImageFromPath(getBaseActivity(),path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(getBaseActivity(), CropImage.class);

        // Tell CropImage activity to look for image to crop
        intent.putExtra(CropImage.IMAGE_PATH, path);

        // Allow CropImage activity to rescale image
        intent.putExtra(CropImage.SCALE, true);

        // Fix aspect ratio to get a square area
        intent.putExtra(CropImage.ASPECT_X, 3);
        intent.putExtra(CropImage.ASPECT_Y, 3);

        getBaseActivity().startActivityForResult(intent, REQUEST_IMAGE_CROP);
    }

    View.OnClickListener imageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ImageView ivClicked;

            if (mPhoto1.getId() == v.getId()){
                mViewClicked = 1;
                ivClicked = mPhoto1mask;
                checkDialogVisibility();
            } else if (mPhoto2.getId() == v.getId()){
                mViewClicked = 2;
                ivClicked = mPhoto2mask;
            } else if (mPhoto3.getId() == v.getId()){
                mViewClicked = 3;
                ivClicked = mPhoto3mask;
            } else {
                mViewClicked = 4;
                ivClicked = mPhoto4mask;
            }

            if (!mLoadedWithPreviousImages) {
                if (mImagesList.size() >= mViewClicked) {
                    setClicked(ivClicked);
                    showOptionsDialog();
                } else {
                    mViewClicked = 0;
                }
            } else {
                if (mImagesList.size() >= mViewClicked){ // There are pending images
                    switch (mImagesList.get(mViewClicked-1).getPending()){
                        case PENDING:
                            downloadImageWithAction(mImagesList.get(mViewClicked-1).getUrl(), mViewClicked-1,
                                    ACTION_DIALOG);
                            break;
                        case DOWNLOADED:
                            setClicked(ivClicked);
                            showOptionsDialog();
                            break;
                        default:
                            break;
                    }
                } else {
                    mViewClicked = 0;
                }
            }
        }
    };

    private void setClicked(ImageView ivClicked) {
        ivClicked.setBackgroundDrawable(getResources().getDrawable(R.drawable.mask_cam_image_stroke_white_full));
        ivClicked.setVisibility(View.VISIBLE);
    }

    /**
     * Download image and then do an action
     *
     * @param url
     * @param pos
     * @param action
     */
    private void downloadImageWithAction(final String url, final int pos, final String action) {
        Picasso.with(getActivity().getApplicationContext()).load(url).into(new Target() {

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
                        mImagesList.remove(pos);
                        mImagesList.add(pos, new ImageCamera(ImageState.DOWNLOADED, url, photoFile.getAbsolutePath()));
                    } catch (IOException ex) {
                        MessageUtil.showError(getBaseActivity(), R.string.error_save_image,
                                getBaseActivity().getCroutonsHolder());
                        Logger.error(ex.getMessage());
                    }

                    FileOutputStream ostream = new FileOutputStream(photoFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
                    ostream.close();

                    switch (action) {
                        case ACTION_DIALOG:
                            showOptionsDialog();
                            break;
                        case ACTION_NEXT:
                            if (!checkPendingImages()) {
                                Intent result = new Intent();
                                List<String> paths = extractImagesPaths();
                                result.putExtra(ItemCreateEditActivity.EXTRA_IMAGES_PATHS,
                                        paths.toArray(new String[paths.size()]));
                                getBaseActivity().setResult(Activity.RESULT_OK, result);
                                getBaseActivity().finish();
                            }
                            break;
                        default:
                            break;
                    }

                } catch (FileNotFoundException e) {
                    mFirstTime = true;
                } catch (IOException e) {
                    mFirstTime = true;
                } catch (NullPointerException e) {
                    mFirstTime = true;
                } catch (IllegalArgumentException e) {
                    mFirstTime = true;
                }
            }

            @Override
            public void onBitmapFailed(Drawable arg0) {
                Logger.error("Position not loaded: " + pos);
                return;
            }
        });
    }

    private List<String> extractImagesPaths() {
        List<String> extraction = new ArrayList<>();
        for (ImageCamera image : mImagesList){
            extraction.add(image.getPath());
        }
        return extraction;
    }

    @Override
    public void onViewCreated(View rootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        mTitle = rootView.findViewById(R.id.fl_title);
        mPreview = (SurfaceView) rootView.findViewById(R.id.sv_camera_preview);
        mPreview.getHolder().addCallback(new PreviewCallback());

        mDialogHint = (TextView) rootView.findViewById(R.id.tv_dialog_hint);
        mDialogHint.setText(Html.fromHtml(getString(R.string.camera_dialog_hint)));
        mDialogHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogHint.setVisibility(View.GONE);
            }
        });

        mPreviewPlaceholder = rootView.findViewById(R.id.preview_placeholder);
        mPreviewPlaceholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDialogVisibility();
            }
        });

        mBtnSwitchCamera = (ImageView) rootView.findViewById(R.id.btn_switch_camera);
        mBtnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDialogVisibility();
                switchCamera();
            }
        });

        mBtnFlashMode = (ImageView) rootView.findViewById(R.id.btn_flash_mode);
        mBtnFlashMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDialogVisibility();
                changeFlashMode();
            }
        });

        mBtnGallery = (ImageView) rootView.findViewById(R.id.btn_gallery);
        mBtnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDialogVisibility();
                selectPictureFromGallery();
            }
        });

        mClose = (ImageView) rootView.findViewById(R.id.iv_close);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBaseActivity().onBackPressed();
            }
        });

        mBtnTakePhoto = (ImageView) rootView.findViewById(R.id.btn_shutter_camera);
        mBtnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTakePhotoButtonEnabled) {
                    takePhoto();
                    mTakePhotoButtonEnabled = false;
                }
            }
        });

        mPhoto1 = (ImageView) rootView.findViewById(R.id.iv_photo_1);
        mPhoto1.setOnClickListener(imageClickListener);

        mPhoto2 = (ImageView) rootView.findViewById(R.id.iv_photo_2);
        mPhoto2.setOnClickListener(imageClickListener);

        mPhoto3 = (ImageView) rootView.findViewById(R.id.iv_photo_3);
        mPhoto3.setOnClickListener(imageClickListener);

        mPhoto4 = (ImageView) rootView.findViewById(R.id.iv_photo_4);
        mPhoto4.setOnClickListener(imageClickListener);

        mEdit1 = (ImageView) rootView.findViewById(R.id.iv_edit_1);
        mEdit2 = (ImageView) rootView.findViewById(R.id.iv_edit_2);
        mEdit3 = (ImageView) rootView.findViewById(R.id.iv_edit_3);
        mEdit4 = (ImageView) rootView.findViewById(R.id.iv_edit_4);

        mPhoto1mask = (ImageView) rootView.findViewById(R.id.iv_photo_1_mask);
        mPhoto2mask = (ImageView) rootView.findViewById(R.id.iv_photo_2_mask);
        mPhoto3mask = (ImageView) rootView.findViewById(R.id.iv_photo_3_mask);
        mPhoto4mask = (ImageView) rootView.findViewById(R.id.iv_photo_4_mask);

        // Four photos loaded views
        mComplete = (LinearLayout) rootView.findViewById(R.id.ly_complete);
        mCompleteIcon = (ImageView) rootView.findViewById(R.id.iv_complete_icon);
        mCompleteHint = (TextView) rootView.findViewById(R.id.tv_complete_hint);
        mCompleteInstruction = (TextView) rootView.findViewById(R.id.tv_complete_instruction);

        mNextStep = (TextView) rootView.findViewById(R.id.tv_next_step);
        mNextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mLoadedWithPreviousImages) { // Camera First
                    ScreenManager.showPublishActivity(getBaseActivity(), -1, extractImagesPaths());
                    getBaseActivity().finish();
                } else {
                    if (checkPendingImages()) {
                        downloadPendingImages();
                    } else {
                        Intent result = new Intent();
                        List<String> paths = extractImagesPaths();
                        result.putExtra(ItemCreateEditActivity.EXTRA_IMAGES_PATHS,
                                paths.toArray(new String[paths.size()]));
                        getBaseActivity().setResult(Activity.RESULT_OK, result);
                        getBaseActivity().finish();
                    }
                }
            }
        });

        if (DeviceUtils.isDeviceLollipopOrHigher()) {
            mTitle.setPadding(0, DeviceUtils.getStatusBarHeight(getBaseActivity()), 0, 0);
        }

        loadEditionImages();
        loadNotPublishedImages();
    }

    private void downloadPendingImages(){
        for (int i = 0; i < mImagesList.size(); i++){
            if (mImagesList.get(i).getPending() == ImageState.PENDING){
                downloadImageWithAction(mImagesList.get(i).getUrl(), i, ACTION_NEXT);
            }
        }
    }

    private void orderPictures() {
        int posPrincipal = mViewClicked-1;
        if (posPrincipal!=0) {
            ImageCamera imagePrincipal = mImagesList.get(posPrincipal);
            mImagesList.remove(posPrincipal);
            mImagesList.add(0, imagePrincipal);
            if (mLoadedWithPreviousImages){
                if (imagePrincipal.getPending() == ImageState.PENDING){
                    downloadImageWithAction(imagePrincipal.getUrl(), 0, ACTION_PRINCIPAL);
                }
            }
        }
    }

    private Boolean checkPendingImages(){
        for (int i = 0; i < mImagesList.size(); i++){
            if (mImagesList.get(i).getPending() == ImageState.PENDING){
                return true;
            }
        }
        return false;
    }

    private void loadNotPublishedImages() {
        if (getArguments() != null && getArguments().containsKey(IMAGE_PATHS_NOT_PUBLISHED)) {
            mLoadedWithPreviousImages = true;
            mFirstTime = false;
            List<String> paths = new ArrayList<>(Arrays.asList(getArguments().getStringArray(IMAGE_PATHS_NOT_PUBLISHED)));
            initializePendingImages(paths,ImageState.DOWNLOADED);
            showImagesTaken();
            if (mImagesList.size() == 4){
                setAllLoadedStep();
            }
        }
    }

    private void checkDialogVisibility(){
        if (mDialogHint.getVisibility() == View.VISIBLE){
            mDialogHint.setVisibility(View.GONE);
        }
    }

    private void selectPictureFromGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        getBaseActivity().startActivityForResult(i, REQUEST_IMAGE_GALLERY);
    }

    /**
     * Show dialog setting CameraFragment as target to receive the option chose
     */
    private void showOptionsDialog() {
        CameraOptionsDialogFragment optionsDialog = CameraOptionsDialogFragment.newInstance();
        optionsDialog.setTargetFragment(this, 0);
        optionsDialog.show(getActivity().getSupportFragmentManager(), CameraOptionsDialogFragment.TAG);
    }

    /**
     * Hide controls to show the complete step
     */
    private void hideControls(){
        mPreview.setVisibility(View.GONE);
        mBtnFlashMode.setVisibility(View.GONE);
        mBtnSwitchCamera.setVisibility(View.GONE);
        mBtnTakePhoto.setVisibility(View.GONE);
        mBtnGallery.setVisibility(View.GONE);
    }

    /**
     * Show controls to show the camera
     */
    private void showControls(){
        mPreview.setVisibility(View.VISIBLE);
        mBtnFlashMode.setVisibility(View.VISIBLE);
//        mBtnSwitchCamera.setVisibility(View.VISIBLE);
        mBtnTakePhoto.setVisibility(View.VISIBLE);
        mBtnGallery.setVisibility(View.VISIBLE);
    }

    /**
     * Show the complete screen controls when the 4th photo is loaded
     */
    private void setCompleteStep() {
        mComplete.setVisibility(View.VISIBLE);
        mCompleteIcon.setImageDrawable(getResources().getDrawable(R.drawable.check_icon_big));
        mCompleteHint.setText(getString(R.string.incredible));
        // To get the bold word and the two lines text
        mCompleteInstruction.setText(Html.fromHtml(getString(R.string.camera_photos_hint)));
        mCompleteInstruction.setText(Html.fromHtml(getString(R.string.camera_photos_hint)));
    }

    /**
     * Show the complete screen controls when an item with 4 photos is loaded
     */
    private void setAllLoadedStep(){
        mComplete.setVisibility(View.VISIBLE);
        mNextStep.setVisibility(View.VISIBLE);
        mCompleteIcon.setImageDrawable(getResources().getDrawable(R.drawable.edit_icon_big));
        mCompleteHint.setText(getString(R.string.edit_photos));
        // To get the bold word and the two lines text
        mCompleteInstruction.setText(Html.fromHtml(getString(R.string.edit_photos_hint)));
        mNextStep.setText(getString(R.string.done_step));
    }

    @Override
    public void onSetPrincipalClick() {
        removeClicked();
        // Re order pictures to set the principal as the first one
        orderPictures();
        showImagesTaken();
        // Reset mViewClicked
        mViewClicked = 0;
    }

    @Override
    public void onCropClick() {
        runCropImage(mImagesList.get(mViewClicked - 1).getPath());
    }

    @Override
    public void onDeleteClick() {
        removeImageClicked();
    }

    @Override
    public void onCancelClick() {
        removeClicked();
        if (mViewClicked == 1){
            setPrincipal();
        }
        mViewClicked = 0;
    }

    /**
     * Removes all pictures and hide every edit icon
     */
    private void clearImagesViews() {
        mPhoto1.setImageDrawable(null);
        mPhoto2.setImageDrawable(null);
        mPhoto3.setImageDrawable(null);
        mPhoto4.setImageDrawable(null);
        mPhoto1.setBackgroundDrawable(getResources().getDrawable(R.drawable.cam_image_thumb_empty));
        mPhoto2.setBackgroundDrawable(getResources().getDrawable(R.drawable.cam_image_thumb_empty));
        mPhoto3.setBackgroundDrawable(getResources().getDrawable(R.drawable.cam_image_thumb_empty));
        mPhoto4.setBackgroundDrawable(getResources().getDrawable(R.drawable.cam_image_thumb_empty));
        mEdit1.setVisibility(View.GONE);
        mEdit2.setVisibility(View.GONE);
        mEdit3.setVisibility(View.GONE);
        mEdit4.setVisibility(View.GONE);
        mPhoto1mask.setVisibility(View.GONE);
        mPhoto2mask.setVisibility(View.GONE);
        mPhoto3mask.setVisibility(View.GONE);
        mPhoto4mask.setVisibility(View.GONE);

    }

    @Override
    public void onBackPressed() {
        checkDialogVisibility();
    }

    private class PreviewCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            getCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // The surface is destroyed with the visibility of the SurfaceView is set to View.Invisible
        }
    }

    private void getCamera() {
        initializeCamera();
    }

    private void initializeCamera(){
        checkPermission(Manifest.permission.CAMERA);
    }

    private void checkPermission(String aPermission) {
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), aPermission)) {
                //TODO inform the user why this permission is required
            }else{
                ActivityCompat.requestPermissions(getActivity(), new String[]{aPermission}, PERMISSION_REQUEST_CAMERA);
            }
        }else{
            retrieveCamera();
        }
    }

    private void retrieveCamera() {
        try{
            mCamera = Camera.open(mCameraId);
        } catch (RuntimeException ex){
        }
        onCameraRetrieved();
    }

    private void onCameraRetrieved() {
        startCameraPreview();
    }

    private void startCameraPreview() {
        setupCamera();
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
            if(mImagesList.size() < 4) {
                mTakePhotoButtonEnabled = true;
            }
        } catch (IOException e) {
            Log.d("CameraPreview", "Can't start camera preview due to IOException " + e);
        }
    }

    private void setupCamera() {
        // Never keep a global parameters
        Camera.Parameters parameters = mCamera.getParameters();

        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(mFlashMode)) {
            parameters.setFlashMode(mFlashMode);
        }
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        calculateBestSize();
        parameters.setPictureSize(mPictureWidth, mPictureHeight);

        // Lock in the changes
        mCamera.setParameters(parameters);
        updateOrientation();
    }

    // Picture size reduced to 1.3 MP
    private void calculateBestSize() {
        Camera.Parameters parameters = mCamera.getParameters();

        for (int i = parameters.getSupportedPictureSizes().size()-1; i >= 0; i--){
            if (parameters.getSupportedPictureSizes().get(i).width <= IMAGE_MAX_WIDTH &&
                    parameters.getSupportedPictureSizes().get(i).height <= IMAGE_MAX_HEIGHT){
                mPictureWidth = parameters.getSupportedPictureSizes().get(i).width;
                mPictureHeight = parameters.getSupportedPictureSizes().get(i).height;
            }
        }
    }

    private void updateOrientation(){
        int result;
        int degrees = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        Camera.CameraInfo info = new Camera.CameraInfo();
        mCamera.getCameraInfo(mCameraId, info);
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);
    }

    private void switchCamera() {
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mCameraId = getBackCameraID();
        } else {
            mCameraId = getFrontCameraID();
        }
        restartPreview();
    }

    private final int getFrontCameraID() {
        PackageManager pm = getActivity().getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return Camera.CameraInfo.CAMERA_FACING_FRONT;
        }

        // In case the device has not a front camera
        return getBackCameraID();
    }

    private final int getBackCameraID() {
        return Camera.CameraInfo.CAMERA_FACING_BACK;
    }

    private void restartPreview() {
        if (mCamera != null) {
            stopCameraPreview();
            mCamera.release();
            mCamera = null;
        }
        getCamera();
    }

    private void stopCameraPreview() {
        mCamera.stopPreview();
    }

    private void changeFlashMode() {
        switch (mFlashMode) {
            case Camera.Parameters.FLASH_MODE_ON:
                mFlashMode = Camera.Parameters.FLASH_MODE_OFF;
                mBtnFlashMode.setImageDrawable(getResources().getDrawable(R.drawable.flash_mode_icon_off));
                break;
            case Camera.Parameters.FLASH_MODE_OFF:
                mFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
                mBtnFlashMode.setImageDrawable(getResources().getDrawable(R.drawable.flash_mode_icon_auto));
                break;
            case Camera.Parameters.FLASH_MODE_AUTO:
            default:
                mFlashMode = Camera.Parameters.FLASH_MODE_ON;
                mBtnFlashMode.setImageDrawable(getResources().getDrawable(R.drawable.flash_mode_icon_on));
                break;
        }
        setupCamera();
    }

    private void takePhoto() {
        mCamera.takePicture(null, null, new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                // Convert byte[] to bitmap and rotate it
                Bitmap photoTaken = getBitmapPhoto(data);

                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/Pictures");
                myDir.mkdirs();
                Random generator = new Random();
                int n = 10000;
                n = generator.nextInt(n);
                String fname = "Image-" + n + ".jpg";
                File photoFile = new File(myDir, fname);
                if (photoFile.exists()) photoFile.delete();
                try {
                    FileOutputStream out = new FileOutputStream(photoFile);
                    photoTaken.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();
                    photoTaken.recycle();

                    if (photoFile != null && photoFile.exists()) {
                        mImagesList.add(new ImageCamera(ImageState.DOWNLOADED, "", photoFile.getAbsolutePath()));
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                checkDialogVisibility();
                showImagesTaken();
                restartPreview();
            }
        });
    }

    private void showImagesTaken() {
        for (int i = 0; i < this.mImagesList.size(); i++) {
            String path = mImagesList.get(i).getPath();
            File imageFile = new File(path);
            if (imageFile.exists()) {
                loadImage(i, imageFile);
            } else {
                if (mLoadedWithPreviousImages) {
                    loadImage(i, mImagesList.get(i).getUrl());
                }
            }
        }

        // The item must have at least one photo
        updateState();
    }

    /**
     * Load image from file
     *
     * @param pos
     * @param aImageFile
     */
    private void loadImage(int pos, File aImageFile) {
        // pos+1 because the methods are taking into account views numbers and not positions
        Picasso.with(getBaseActivity()).load(aImageFile).resize(200, 200).centerCrop()
                .into(getProperImageView(pos+1));
        getProperEditImageView(pos+1).setVisibility(View.VISIBLE);

        setGrayBorder(getProperMaskImageView(pos + 1));
    }

    /**
     * Load image from url.
     *
     * @param pos
     * @param url
     */
    private void loadImage(int pos, String url) {
        // pos+1 because the methods are taking into account views numbers and not positions
        Picasso.with(getBaseActivity()).load(url).resize(200, 200).centerCrop()
                .into(getProperImageView(pos + 1));
        getProperEditImageView(pos + 1).setVisibility(View.VISIBLE);

        setGrayBorder(getProperMaskImageView(pos + 1));
    }

    private void setGrayBorder(ImageView iv) {
        iv.setBackgroundDrawable(getResources().getDrawable(R.drawable.mask_cam_image_stroke_selected_full));
        iv.setVisibility(View.VISIBLE);
    }

    private void updateState() {
        if (mImagesList.size()>0){
            setPrincipal();
            mNextStep.setVisibility(View.VISIBLE);
            if (mImagesList.size() == 1 && mFirstTime){ // Show the dialog hint
                mDialogHint.setVisibility(View.VISIBLE);
                mFirstTime = false;
            } else if (mImagesList.size() == 4){ // All photos loaded
                hideControls();
                setCompleteStep();
            }
        } else {
            mNextStep.setVisibility(View.GONE);
            checkDialogVisibility();
        }
    }

    private ImageView getProperImageView(int view){
        switch (view){
            case 2:
                return mPhoto2;
            case 3:
                return mPhoto3;
            case 4:
                return mPhoto4;
            default:
                return mPhoto1;
        }
    }

    private ImageView getProperEditImageView(int view){
        switch (view){
            case 2:
                return mEdit2;
            case 3:
                return mEdit3;
            case 4:
                return mEdit4;
            default:
                return mEdit1;
        }
    }

    private ImageView getProperMaskImageView(int view){
        switch (view){
            case 2:
                return mPhoto2mask;
            case 3:
                return mPhoto3mask;
            case 4:
                return mPhoto4mask;
            default:
                return mPhoto1mask;
        }
    }

    private void removeClicked(){
        // Get proper ivPhoto and ivEdit to change colors
        ImageView ivClicked = getProperMaskImageView(mViewClicked);
        ImageView ivEditClicked = getProperEditImageView(mViewClicked);

        setGrayBorder(ivClicked);

        // Apply edit icon
        ivEditClicked.setVisibility(View.VISIBLE);
    }

    private Bitmap getBitmapPhoto(byte[] data) {
        // Convert byte[] to bitmap
        Bitmap photo = BitmapFactory.decodeByteArray(data, 0, data.length);

        // Create matrix to rotate bitmap
        Matrix matrix = new Matrix();

        // Set the proper rotation
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT){
            float[] mirrorY = { -1, 0, 0, 0, 1, 0, 0, 0, 1};
            Matrix matrixMirrorY = new Matrix();
            matrixMirrorY.setValues(mirrorY);

            matrix.postConcat(matrixMirrorY);
        }
        matrix.postRotate(90); // clockwise by 90 degrees

        // Create a new bitmap rotated using the matrix
        Bitmap rotatedPhoto = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);
        photo.recycle();

        int a = (int) (((float) mPreviewPlaceholder.getMeasuredHeight() / (float) mPreview.getMeasuredHeight()) * rotatedPhoto.getHeight());

        photo = Bitmap.createBitmap(rotatedPhoto, 0, mTitle.getHeight() - 50, rotatedPhoto.getWidth(), a);
        rotatedPhoto.recycle();

        return photo;
    }

    private class ImageCamera {

        private String mPath;
        private ImageState mPending;
        private String mUrl;

        public ImageCamera(ImageState pending, String url, String path) {
            this.mPending = pending;
            this.mUrl = url;
            this.mPath = path;
        }

        public String getPath() {
            return mPath;
        }

        public void setPath(String path) {
            this.mPath = path;
        }

        public String getUrl() {
            return mUrl;
        }

        public void setUrl(String url) {
            this.mUrl = url;
        }

        public ImageState getPending() {
            return mPending;
        }

        public void setPending(ImageState pending) {
            this.mPending = pending;
        }
    }

}
