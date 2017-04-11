package com.ebay.park.requests;

import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.GroupDetailResponse;
import com.globant.roboneck.common.Multipart;
import com.globant.roboneck.common.Part;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.io.File;

/**
 * Group picture upload request.
 * 
 * @author Nicolás Matias Fernández
 * 
 */
public class GroupPictureUploadRequest extends BaseParkMultipartRequest<GroupDetailResponse> {

	private String mFileLocation;
	private long mGroupId;

	/**
	 * 
	 * @param fileLocation
	 *            Path of the file to upload..
	 */
	public GroupPictureUploadRequest(String fileLocation, long id) {
		super(GroupDetailResponse.class);
		this.mFileLocation = fileLocation;
		this.mGroupId = id;
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
				.addPart(
						new Part.Builder()
								.contentDisposition("form-data; name=\"photo\"; filename=\"" + f.getName() + "\"")
								.contentType("image/jpeg").body(f).build()).build();
		return multi;
	}

	@Override
	public long getCacheExpirationTime() {
		return DurationInMillis.ALWAYS_EXPIRED;
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.UPLOAD_UPDATE_GROUP_PICTURE, getApiUri(), mGroupId);
	}

	@Override
	protected GroupDetailResponse getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		return getGson().fromJson(json, GroupDetailResponse.class);
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
