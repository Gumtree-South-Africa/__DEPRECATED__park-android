package com.globant.roboneck.common;

import com.octo.android.robospice.persistence.memory.CacheItem;
import com.octo.android.robospice.persistence.memory.LruCache;
import com.octo.android.robospice.persistence.memory.LruCacheObjectPersister;

public class NeckPersister extends LruCacheObjectPersister<Object> {

	public NeckPersister(int cacheSize) {
		super(Object.class, new LruCache<Object, CacheItem<Object>>(cacheSize));
	}

	@Override
	public boolean canHandleClass(Class<?> clazz) {
		return true;
	}

}