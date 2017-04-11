package com.ebay.park.configuration;

public class ConfigurationQA extends ParkConfiguration {

	@Override
	public String getParkRestUrl() {
		return "http://qarest.newapptest.com:8080/";
	}

	@Override
	public String getParkWebUrl() {
		return "http://qa-web.newapptest.com/";
	}

	@Override
	public String getFacebookAppId() {
		return "660832997326314";
	}

	@Override
	public String getTwitterConsumerKey() {
		return "bqxsFGsFaUCsyemxCmrPWGA8Y";
	}

	@Override
	public String getTwitterConsumerSecret() {
		return "AekjV52btB3IHD139ZTj3PFxN1MXoUdVJm3oONQ87uvclvalBv";
	}

	@Override
	public String getGlobalTrackerEnv() {
		return "QA";
	}

	@Override
	public Boolean logRequests() {
		return true;
	}

	@Override
	public String getCrittercismAppId() {
		return "55a50d0a63235a0f00ad8ee4";
	}

	@Override
	public String getAppLink() {
		return "https://fb.me/949919525084325";
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
