package com.ebay.park.responses;

import com.ebay.park.requests.SignupAddPicRequest;

/**
 * {@link SignupAddPicRequest} response.
 * 
 * @author federico.perez
 * 
 */
public class GroupDetailResponse {

	private boolean success;
	private int id;

	public GroupDetailResponse() {
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public boolean isSuccess() {
		return success;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
