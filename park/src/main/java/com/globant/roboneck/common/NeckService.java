package com.globant.roboneck.common;

import android.app.Application;

import com.globant.roboneck.NeckApp;
import com.octo.android.robospice.okhttp.OkHttpSpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;

public class NeckService extends OkHttpSpiceService {

	@Override
	public CacheManager createCacheManager(Application application) throws CacheCreationException {
		CacheManager manager = new CacheManager();
		manager.addPersister(((NeckApp) application).getPersister());
		return manager;
	}

}