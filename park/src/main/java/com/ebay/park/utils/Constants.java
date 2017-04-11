package com.ebay.park.utils;

import android.text.InputFilter;
import android.text.Spanned;

public class Constants {

	/**
	 * User name and lastname minimum length. (FIXME yet to be defined.)
	 */
	public static final int NAME_LASTNAME_MIN_LENGTH = 2;

	/**
	 * User name and lastname maximum length. (FIXME yet to be defined.)
	 */
	public static final int NAME_LASTNAME_MAX_LENGTH = 15;

	/**
	 * Regular expression to validate user name and lastname.
	 */
	public static final String NAME_LASTNAME_REGEX = "^[A-Za-z]{" + NAME_LASTNAME_MIN_LENGTH + "," + NAME_LASTNAME_MAX_LENGTH
			+ "}$";

	/**
	 * Action to logout user.
	 */
	public static final String LOGOUT_BROADCAST = "com.ebay.park.LOGOUT";

	/**
	 * Zip code length. (FIXME yet to be defined.)
	 */
	public static final int ZIPCODE_LENGTH = 5;

	/**
	 * Regular expression for zip codes.
	 */
	public static final String ZIPCODE_REGEX = "[0-9]{5}$";

	/**
	 * Regular expression for price.
	 */
	public static final String PRICE_REGEX = "^\\s*(?=.*[1-9])\\d*(?:\\.\\d{1,2})?\\s*$";

	public static final String APPLICATION_JSON = "application/json";

	/**
	 * Item name minimum length. (FIXME yet to be defined.)
	 */
	public static final int ITEM_NAME_MIN_LENGTH = 3;

	/**
	 * Item name maximum length. (FIXME yet to be defined.)
	 */
	public static final int ITEM_NAME_MAX_LENGTH = 20;
	
	/**
	 * Group name maximum length. (FIXME yet to be defined.)
	 */
	public static final int GROUP_NAME_MAX_LENGTH = 25;

	/**
	 * Regular expression to validate item names.
	 */
	public static final String ITEMNAME_REGEX = "^[A-Za-z0-9 \\xC0-\\xFF ¿?¡! _-]{" + ITEM_NAME_MIN_LENGTH + ","
			+ ITEM_NAME_MAX_LENGTH + "}$";
	
	/**
	 * Regular expression to validate group names.
	 */
	public static final String GROUPNAME_REGEX = "^[A-Za-z0-9 \\xC0-\\xFF ¿?¡! _-]{" + ITEM_NAME_MIN_LENGTH + ","
			+ GROUP_NAME_MAX_LENGTH + "}$";	

	/**
	 * Regular expression to validate item descriptions.
	 */
	public static final String ITEMDESCRIPTION_REGEX = "[^:;]*$";
	
	/**
	 * Regular expression to validate group descriptions.
	 */
	public static final String GROUPDESCRIPTION_REGEX = "[^:;]*$";

	/**
	 * App supported languages.
	 */
	public static final String[] SUPPORTED_LANGUAGES = new String[] { "en", "es" };

	/**
	 * Seller role on negotiation.
	 */
	public static final String ROLE_SELLER = "seller";

	/**
	 * Buyer role on negotiation.
	 */
	public static final String ROLE_BUYER = "buyer";

	/**
	 * Chat update interval
	 */
	public static final long CHAT_UPDATE_INTERVAL = 15 * 1000;

	public static final int CHAT_MIN_LENGHT = 1;
	public static final int CHAT_MAX_LENGHT = 250;

	/**
	 * Device type to send to server.
	 */
	public static final String DEVICE_TYPE = "android";

	/**
	 * Input filter for e-mail fields.
	 */
	public static final InputFilter EMAIL_FILTER = new InputFilter() {
		public CharSequence filter(CharSequence source, int start, int end,
								   Spanned dest, int dstart, int dend) {
			for (int i = start; i < end; i++) {
				if (!Character.isLetterOrDigit(source.charAt(i)) &&
						source.charAt(i) != '@' && source.charAt(i) != '.' &&
						source.charAt(i) != '-' && source.charAt(i) != '_' &&
						source.charAt(i) != '+') {
					return "";
				}
			}
			return null;
		}
	};

}
