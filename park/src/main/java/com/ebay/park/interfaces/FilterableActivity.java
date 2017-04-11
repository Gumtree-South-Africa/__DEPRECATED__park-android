package com.ebay.park.interfaces;

import java.util.Map;

public interface FilterableActivity {
	public static final String PRICE_FROM = "pirceFrom";
	public static final String PRICE_TO = "priceTo";
	public static final String CATEGORY = "category";
	public static final String MAX_DISTANCE = "radius";
	public static final String ORDER_BY = "order";

	public void onFiltersConfirmed(Map<String, String> filters);
}
