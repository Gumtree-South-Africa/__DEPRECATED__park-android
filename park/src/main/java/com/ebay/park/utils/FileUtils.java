package com.ebay.park.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.base.BaseSessionActivity;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileUtils {

	static final double sMaxWidth = 800;
	static final double sMaxHeight = 600;
	static final int sQuality = 50;
	public static boolean sIsEqorBiggerthan200 = false;
	public static boolean sErrorSizeShown = false;
	private static boolean sFromCropping = false;

	@SuppressLint("SimpleDateFormat")
	public static File createProfileImageFile() throws IOException {
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = "profile_" + timeStamp + "_";
		File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File image = File.createTempFile(imageFileName, ".jpg", storageDir);
		return image;
	}

	public static File resizeImage(Context aContext, Uri selectedImage) throws IOException {
		sFromCropping = false;
		File aFile = getFileFromCursor(aContext, selectedImage);
		if (aFile == null) {
			aFile = resizeImageFromStream(aContext, selectedImage);
		}
		return validateNullity(aFile);
	}

	public static File resizeImageFromStream(Context aContext, Uri selectedImage) throws IOException {
		Bitmap aBitmap = StreamFileUtils.getSampledBitmap(aContext, selectedImage);
		return validateNullity(getResizedBitmap(aContext, aBitmap, selectedImage.getPath()));
	}

	public static File resizeImageFromPath(Context aContext, String filePath) throws IOException {
		sFromCropping = true;
		return validateNullity(resizeFromPath(aContext, filePath));
	}

	private static File resizeFromPath(Context aContext, String aFilePath) throws IOException {
		Bitmap aBitmap = FileUtils.getSampledBitmap(aFilePath);
		return getResizedBitmap(aContext, aBitmap, aFilePath);
	}

	private static File getResizedBitmap(Context aContext, Bitmap aBitmap, String aPath) throws IOException{
		File aFile = null;
		if (aBitmap != null){
			if (isEqorBiggerthan200(aBitmap)){
				int desiredWidth = aBitmap.getWidth();
				int desiredHeight = aBitmap.getHeight();
				StreamFileUtils.getDesiredSize(desiredWidth, desiredHeight);
				Bitmap resizedBitmap = rotateBitmap(Bitmap.createScaledBitmap(aBitmap, desiredWidth, desiredHeight, false), StreamFileUtils.getRotation(aContext, aPath));
				aFile = File.createTempFile(StreamFileUtils.getFileName(), ".jpg", Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
				StreamFileUtils.writeFile(aFile, resizedBitmap);
				aBitmap.recycle();
			}else{
				if(!sErrorSizeShown) {
					sErrorSizeShown = true;
					if(!sFromCropping){
						MessageUtil.showLongError((BaseActivity) ParkApplication.sCurrentContext, R.string.image_too_small,
								((BaseActivity) ParkApplication.sCurrentContext).getCroutonsHolder());
					}
					else {
						MessageUtil.showLongError((BaseActivity) ParkApplication.sCurrentContext, R.string.image_too_small_simple,
								((BaseActivity) ParkApplication.sCurrentContext).getCroutonsHolder());
					}
				}
			}
		}
		return aFile;
	}

	private static Bitmap getSampledBitmap(String filePath) {
		Bitmap aBitmap = null;
		aBitmap = BitmapFactory.decodeFile(filePath, getBitmapOptions(filePath));
		return aBitmap;
	}

	private static Options getBitmapOptions(String aPath){
		Options aOptions = StreamFileUtils.initializeBitmapOptions();
		BitmapFactory.decodeFile(aPath, aOptions);
		return StreamFileUtils.processBitmapFactoryOptions(aOptions);
	}

	public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) throws IOException {
		Matrix matrix = new Matrix();
		switch (orientation) {
			case ExifInterface.ORIENTATION_NORMAL:
				return bitmap;
			case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
				matrix.setScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				matrix.setRotate(180);
				break;
			case ExifInterface.ORIENTATION_FLIP_VERTICAL:
				matrix.setRotate(180);
				matrix.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_TRANSPOSE:
				matrix.setRotate(90);
				matrix.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				matrix.setRotate(90);
				break;
			case ExifInterface.ORIENTATION_TRANSVERSE:
				matrix.setRotate(-90);
				matrix.postScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				matrix.setRotate(-90);
				break;
			default:
				return bitmap;
		}
		try {
			Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			bitmap.recycle();
			return bmRotated;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Boolean isEqorBiggerthan200(Bitmap b) {

		double height = (double) b.getHeight();
		double width = (double) b.getWidth();

		if (height < 200 || width < 200) {
			sIsEqorBiggerthan200 = false;
			return false;
		} else {
			sIsEqorBiggerthan200 = true;
			return true;
		}

	}

	private static File getFileFromCursor(Context aContext, Uri aSelectedImage){
		File imageFile = null;
		try{
			imageFile = FileUtils.resizeFromPath(aContext, getFilePathFromQuery(aContext, aSelectedImage));
		}catch (IOException fileNotFound){
		}
		return imageFile;
	}

	private static String getFilePathFromQuery(Context aContext, Uri aSelectedImage) {
		String result = "";
		final String[] filePathColumn = { MediaStore.Images.Media.DATA };
		Cursor cursor = aContext.getContentResolver().query(aSelectedImage, filePathColumn, null,
				null, null);
		if (cursor != null){
			if (cursor.moveToFirst()) {
				result = cursor.getString(0);
			}
			cursor.close();
		}
		return result;
	}

	private static File validateNullity(File aFile) {
		if (aFile == null){
			if(sIsEqorBiggerthan200) {
				MessageUtil.showLongError((BaseSessionActivity) ParkApplication.sCurrentContext, R.string.error_generic_image,
						((BaseActivity) ParkApplication.sCurrentContext).getCroutonsHolder());
			}
		}
		return aFile;
	}

	private static class StreamFileUtils{

		private static Bitmap getSampledBitmap(Context aContext, Uri selectedImage){
			Bitmap aBitmap = null;
			try {
				aBitmap = BitmapFactory.decodeStream(
						getInputStream(aContext, selectedImage),
						null,
						getBitmapOptions(getInputStream(aContext, selectedImage)));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return aBitmap;
		}

		public static int getOrientation(Context context, Uri photoUri) {
			int result = -1;
			try{
				Cursor cursor = context.getContentResolver().query(photoUri,
						new String[] { MediaStore.Images.ImageColumns.ORIENTATION },
						null, null, null);
				if (cursor != null){
					if (cursor.moveToFirst()) {
						result = cursor.getInt(0);
					}
					cursor.close();
				}
			}catch(IllegalArgumentException e){
			}
			return result;
		}

		private static Options getBitmapOptions(InputStream aInputStream){
			Options aOptions = initializeBitmapOptions();
			BitmapFactory.decodeStream(aInputStream, null, aOptions);
			return processBitmapFactoryOptions(aOptions);
		}

		private static Options processBitmapFactoryOptions(Options aOptions) {
			aOptions.inJustDecodeBounds = false;
			int mult = (int) Math.min((aOptions.outWidth / sMaxWidth), (aOptions.outHeight / sMaxHeight));
			if (mult > 1) {
				aOptions.inSampleSize = mult;
			}
			return aOptions;
		}

		private static Options initializeBitmapOptions(){
			Options aOptions = new BitmapFactory.Options();
			aOptions.inJustDecodeBounds = true;
			return aOptions;
		}

		private static InputStream getInputStream(Context aContext, Uri selectedImage) throws FileNotFoundException{
			return aContext.getContentResolver().openInputStream(selectedImage);
		}

		private static void writeFile(File aFile, Bitmap resizedBitmap) throws IOException {
			FileOutputStream fOut = new FileOutputStream(aFile);
			resizedBitmap.compress(Bitmap.CompressFormat.JPEG, sQuality, fOut);
			fOut.flush();
			fOut.close();
			resizedBitmap.recycle();
		}

		@SuppressLint("SimpleDateFormat")
		private static String getFileName(){
			return "image_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_";
		}

		private static int getRotation(Context aContext, String aPath) {
			int rotation = getOrientation(aContext, Uri.parse(aPath));
			if (rotation == -1) {
				try {
					rotation = new ExifInterface(aPath).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
				} catch (IOException e) {
				} catch (IllegalArgumentException e) {
				}
			}
			return rotation;
		}

		private static void getDesiredSize(double width, double height) {
			if (width > sMaxWidth || height > sMaxHeight) {
				double imgRatio = (double) width / (double) height;
				double maxRatio = sMaxWidth / sMaxHeight;
				if (imgRatio < maxRatio) {
					// adjust width according to sMaxHeight
					imgRatio = sMaxHeight / height;
					height = sMaxHeight;
					width = imgRatio * width;
				} else if (imgRatio > maxRatio) {
					// adjust height according to sMaxWidth
					imgRatio = sMaxWidth / width;
					height = imgRatio * height;
					width = sMaxWidth;

				} else {
					height = sMaxHeight;
					width = sMaxWidth;
				}
			}
		}

	}
}
