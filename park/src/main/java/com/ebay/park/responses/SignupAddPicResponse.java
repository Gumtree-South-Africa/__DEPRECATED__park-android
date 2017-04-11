package com.ebay.park.responses;

import com.ebay.park.requests.SignupAddPicRequest;

/**
 * {@link SignupAddPicRequest} response.
 * 
 * @author federico.perez
 * 
 */
public class SignupAddPicResponse {

	private boolean success;

	public SignupAddPicResponse() {
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public boolean isSuccess() {
		return success;
	}
}
