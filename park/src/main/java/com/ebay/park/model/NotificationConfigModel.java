package com.ebay.park.model;

/**
 * Notifications Configuration Model
 * 
 * @author Nicolás Matias Fernández
 * 
 */
public class NotificationConfigModel {

	private String actionDisplayName;
	private String emailConfig;
	private String pushConfig;

	public NotificationConfigModel(String name, Boolean email, Boolean push) {
		actionDisplayName = name;
		if (email == null) {
			emailConfig = "DISABLED";
		} else {
			if (email) {
				emailConfig = "TRUE";
			} else {
				emailConfig = "FALSE";
			}
		}
		if (push == null) {
			pushConfig = "DISABLED";
		} else {
			if (push) {
				pushConfig = "TRUE";
			} else {
				pushConfig = "FALSE";
			}
		}
	}

	public String getActionDisplayName() {
		return actionDisplayName;
	}

	public void setActionDisplayName(String actionDisplayName) {
		this.actionDisplayName = actionDisplayName;
	}

	public Boolean getEmailConfig() {
		switch (emailConfig) {
		case "TRUE":
			return true;
		case "FALSE":
			return false;
		case "DISABLED":
			return false;
		}
		return false;
	}

	public void setEmailConfig(String emailConfig) {
		this.emailConfig = emailConfig;
	}

	public Boolean getPushConfig() {
		switch (pushConfig) {
		case "TRUE":
			return true;
		case "FALSE":
			return false;
		case "DISABLED":
			return false;
		}
		return false;
	}

	public void setPushConfig(String pushConfig) {
		this.pushConfig = pushConfig;
	}

}
