package com.ebay.park.configuration;

public class ConfigurationDEV extends ParkConfiguration {

	@Override
	public String getParkRestUrl() {
		return "http://devrest.newapptest.com:8080/";
	}

	@Override
	public String getParkWebUrl() {
		return "http://dev-web.newapptest.com/";
	}

	@Override
	public String getFacebookAppId() {
		return "647236968685917";
	}

	@Override
	public String getTwitterConsumerKey() {
		return "PVY6ysfTbmB0EReltT6bg37BC";
	}

	@Override
	public String getTwitterConsumerSecret() {
		return "u6dN8ijpoFqKBH8LZwFCSJ4x5u1Xzq3jDL9lJIlf461BCzto4l";
	}

	@Override
	public String getGlobalTrackerEnv() {
		return "DEV";
	}

	@Override
	public Boolean logRequests() {
		return true;
	}

	@Override
	public String getCrittercismAppId() {
		return "55cb6391985ec40d0002c553";
	}

	@Override
	public String getAppLink() {
		return "https://fb.me/949758171767127";
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
