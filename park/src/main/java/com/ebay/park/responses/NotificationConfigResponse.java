package com.ebay.park.responses;

import com.ebay.park.model.NotificationConfigModel;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Notifications Configuration Response
 * 
 * @author Nicolás Matias Fernández
 * 
 */
public class NotificationConfigResponse {

	@SerializedName("GENERAL")
	private Map<String, NotificationConfigModel> generalSettings;

	@SerializedName("MY_PUBLICATION")
	private Map<String, NotificationConfigModel> myPublicationsSettings;

	@SerializedName("NEGOTIATION_CHAT")
	private Map<String, NotificationConfigModel> negotiationChatSetting;

	public Map<String, NotificationConfigModel> getGeneralSettings() {
		return generalSettings;
	}

	public void setGeneralSettings(Map<String, NotificationConfigModel> generalSettings) {
		this.generalSettings = generalSettings;
	}

	public Map<String, NotificationConfigModel> getMyPublicationsSettings() {
		return myPublicationsSettings;
	}

	public void setMyPublicationsSettings(Map<String, NotificationConfigModel> myPublicationsSettings) {
		this.myPublicationsSettings = myPublicationsSettings;
	}

	public Map<String, NotificationConfigModel> getNegotiationChatSetting() {
		return negotiationChatSetting;
	}

	public void setNegotiationChatSetting(Map<String, NotificationConfigModel> negotiationChatSetting) {
		this.negotiationChatSetting = negotiationChatSetting;
	}

}
