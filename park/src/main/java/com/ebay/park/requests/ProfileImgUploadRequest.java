package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.ProfileImgResponse;
import com.globant.roboneck.common.Multipart;
import com.globant.roboneck.common.Part;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.io.File;

/**
 * User profile picture upload request.
 * 
 * @author federico.perez
 * 
 */
public class ProfileImgUploadRequest extends BaseParkMultipartRequest<ProfileImgResponse> {

	private String mFileLocation;
	private String mFilename;

	/**
	 * 
	 * @param fileLocation
	 *            Path of the file to upload.
	 * @param filename
	 *            Name to give to the file.
	 */
	public ProfileImgUploadRequest(String fileLocation, String filename) {
		super(ProfileImgResponse.class);
		this.mFileLocation = fileLocation;
		this.mFilename = filename;
	}

	@Override
	public Object getCachekey() {
		return "";
	}

	@Override
	protected Multipart getMultipartBody(String boundary) {
		File f = new File(mFileLocation);
		Multipart multi = new Multipart.Builder(boundary)
				.type(Multipart.Type.FORM)
				.addPart(new Part.Builder().contentDisposition("form-data; name=\"name\"").body(mFilename).build())
				.addPart(
						new Part.Builder()
								.contentDisposition("form-data; name=\"file\"; filename=\"" + f.getName() + "\"")
								.contentType("application/octet-stream").body(f).build()).build();
		return multi;
	}

	@Override
	public long getCacheExpirationTime() {
		return DurationInMillis.ALWAYS_EXPIRED;
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.ASSET_UPLOAD, getApiUri());
	}

	@Override
	protected ProfileImgResponse getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		return getGson().fromJson(json, ProfileImgResponse.class);
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
