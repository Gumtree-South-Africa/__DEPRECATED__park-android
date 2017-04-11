package com.ebay.park.configuration;

public class ConfigurationProd extends ParkConfiguration {

	@Override
	public String getParkRestUrl() {
		return "https://rest.vivanuncios.us/";
	}

	@Override
	public String getParkWebUrl() {
		return "http://www.vivanuncios.us/";
	}

	@Override
	public String getFacebookAppId() {
		return "633329433410004";
	}

	@Override
	public String getTwitterConsumerKey() {
		return "HuRLJWfZE5RE0Tv0dYsjYDMVf";
	}

	@Override
	public String getTwitterConsumerSecret() {
		return "HT8vg9Gau6j3AS2GbmvzqMtkZe3qQZrS3uo6C6Rw9CO3ur7AVP";
	}

	@Override
	public String getGlobalTrackerEnv() {
		return "PROD";
	}

	@Override
	public Boolean logRequests() {
		return false;
	}

	@Override
	public String getCrittercismAppId() {
		return "55a53fa94be3830b003a263c";
	}

	@Override
	public String getAppLink() {
		return "https://fb.me/949922018417409";
	}

    @Override
    public int getSwrveAppId() {
        return 3872;
    }

    @Override
    public String getSwrveApiKey() {
        return "6B5geSdhnkNNd942oGIZ";
    }

}
