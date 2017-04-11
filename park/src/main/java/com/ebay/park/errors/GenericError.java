package com.ebay.park.errors;

import com.globant.roboneck.requests.BaseNeckRequestException.Error;

public class GenericError implements Error {

	private String mMessage;
	private int mCode;
	private int mStatus;

	public GenericError(int status, int code, String message) {
		this.mMessage = message;
		this.mCode = code;
		this.mStatus = status;
	}

	@Override
	public String getMessage() {
		return this.mMessage;
	}

	@Override
	public int getErrorCode() {
		return this.mCode;
	}

	@Override
	public int getStatus() {
		return mStatus;
	}

}
