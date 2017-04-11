package com.ebay.park.configuration;

public class ConfigurationSTG extends ParkConfiguration {

	@Override
	public String getParkRestUrl() {
		return "http://stgrest.newapptest.com:8080/";
	}

	@Override
	public String getParkWebUrl() {
		return "http://stg-web.newapptest.com/";
	}

	@Override
	public String getFacebookAppId() {
		return "676839865725627";
	}

	@Override
	public String getTwitterConsumerKey() {
		return "r1oVdPNrZfM6XpD3TrXxm7k1K";
	}

	@Override
	public String getTwitterConsumerSecret() {
		return "9J3OJ2K3wD43eOJ37xLlD5MU8ZyuvrXa02N6Wc65CbKg6BbccX";
	}

	@Override
	public String getGlobalTrackerEnv() {
		return "STG";
	}

	@Override
	public Boolean logRequests() {
		return true;
	}

	@Override
	public String getCrittercismAppId() {
		return "55cb63ab251f530b00e0dc65";
	}

	@Override
	public String getAppLink() {
		return "https://fb.me/949920191750925";
	}

    @Override
    public int getSwrveAppId() {
        return 3894;
    }

    @Override
    public String getSwrveApiKey() {
        return "ttPpYE3aEtnI1HnL0f3e";
    }

}
