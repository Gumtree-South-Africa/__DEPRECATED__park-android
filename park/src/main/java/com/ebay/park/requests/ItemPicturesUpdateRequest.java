package com.ebay.park.requests;

import android.text.TextUtils;
import android.util.Pair;

import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.ItemPicturesUpdateResponse;
import com.globant.roboneck.common.Multipart;
import com.globant.roboneck.common.Part;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.io.File;
import java.util.List;

/**
 * Item pictures update request.
 * 
 * @author Nicolás Matías Fernández
 * 
 */
public class ItemPicturesUpdateRequest extends BaseParkMultipartRequest<ItemPicturesUpdateResponse> {

	private long mId;
	private String mPhoto1;
	private String mPhoto2;
	private String mPhoto3;
	private String mPhoto4;

	/**
	 * 
	 * @param itemId
	 *            The id of the item that has the pictures to be updated.
	 */
	public ItemPicturesUpdateRequest(Boolean editMode, long itemId, List<String> itemPictureLocalPathList) {
		super(ItemPicturesUpdateResponse.class);
		this.mId = itemId;
		this.mPhoto1 = itemPictureLocalPathList.size() < 1 ? "" : itemPictureLocalPathList.get(0);
		this.mPhoto2 = itemPictureLocalPathList.size() < 2 ? "" : itemPictureLocalPathList.get(1);
		this.mPhoto3 = itemPictureLocalPathList.size() < 3 ? "" : itemPictureLocalPathList.get(2);
		this.mPhoto4 = itemPictureLocalPathList.size() < 4 ? "" : itemPictureLocalPathList.get(3);
	}

	@Override
	public Object getCachekey() {
		return "";
	}

	@Override
	protected Multipart getMultipartBody(String boundary) {

		Multipart.Builder builder = new Multipart.Builder(boundary).type(Multipart.Type.FORM);

		if (!TextUtils.isEmpty(mPhoto1)) {
			File f1 = new File(mPhoto1);
			builder.addPart(new Part.Builder()
					.contentDisposition("form-data; name=\"photo1\"; filename=\"" + f1.getName() + "\"")
					.contentType("image/jpeg").body(f1).build());
		}
		if (!TextUtils.isEmpty(mPhoto2)) {
			File f2 = new File(mPhoto2);
			builder.addPart(new Part.Builder()
					.contentDisposition("form-data; name=\"photo2\"; filename=\"" + f2.getName() + "\"")
					.contentType("image/jpeg").body(f2).build());
		}
		if (!TextUtils.isEmpty(mPhoto3)) {
			File f3 = new File(mPhoto3);
			builder.addPart(new Part.Builder()
					.contentDisposition("form-data; name=\"photo3\"; filename=\"" + f3.getName() + "\"")
					.contentType("image/jpeg").body(f3).build());
		}
		if (!TextUtils.isEmpty(mPhoto4)) {
			File f4 = new File(mPhoto4);
			builder.addPart(new Part.Builder()
					.contentDisposition("form-data; name=\"photo4\"; filename=\"" + f4.getName() + "\"")
					.contentType("image/jpeg").body(f4).build());
		}

		Multipart multi = builder.build();

		return multi;
	}

	@Override
	public long getCacheExpirationTime() {
		return DurationInMillis.ALWAYS_EXPIRED;
	}

	@Override
	protected String getUrlFormat() {
		return String.format(ParkUrls.UPLOAD_UPDATE_ITEM_PICTURES, getApiUri(), mId);

	}

	@Override
	protected ItemPicturesUpdateResponse getRequestModel(BaseParkResponse response) {
		String json = getGson().toJson(response.getData());
		return getGson().fromJson(json, ItemPicturesUpdateResponse.class);
	}

	@Override
	protected Pair<String, String>[] getQueryParameters() {
		return null;
	}

}
