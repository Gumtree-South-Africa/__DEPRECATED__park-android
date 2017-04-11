package com.ebay.park.model;

public class BannerModel {
	private String message;
	private Type type;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public enum Type {
		SYSTEM, TIP, FACEBOOK, TWITTER
	}
}
