package com.ebay.park.configuration;

public abstract class ParkConfiguration {

	public static ParkConfiguration getInstance(String env) {

		switch (env) {
		case "QA":
			return new ConfigurationQA();
		case "PROD":
			return new ConfigurationProd();
		case "STG":
			return new ConfigurationSTG();
		case "DEV":
			return new ConfigurationDEV();
		default:
			return new ConfigurationQA();
		}

	}

	public abstract String getParkRestUrl();

	public abstract String getParkWebUrl();

	public abstract String getFacebookAppId();

	public abstract String getTwitterConsumerKey();

	public abstract String getTwitterConsumerSecret();

	public abstract String getGlobalTrackerEnv();

	public abstract Boolean logRequests();

	public abstract String getCrittercismAppId();

	public abstract String getAppLink();

    public abstract int getSwrveAppId();

    public abstract String getSwrveApiKey();
}
