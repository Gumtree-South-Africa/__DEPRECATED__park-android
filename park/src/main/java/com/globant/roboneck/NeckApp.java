package com.globant.roboneck;

import android.app.Application;

import com.globant.roboneck.common.NeckPersister;

public abstract class NeckApp extends Application {

	private NeckPersister persister;

	@Override
	public void onCreate() {
		super.onCreate();
		persister = createPersister();
	}

	public NeckPersister getPersister() {
		return persister;
	}

	protected NeckPersister createPersister() {
		return new NeckPersister(2048);
	}

}
