/**
 * 
 */
package com.ebay.park.utils;

import android.util.Log;

public class Logger {

	public static boolean DEBUG = true;

	/**
	 * TAG to be used for logging purposes
	 */
	private static final String TAG = "PARK";

	/**
	 * Perform logging with Verbose level of verbosity.
	 * 
	 * @param message
	 *            Message to log.
	 */
	public static void verb(String message) {
		if (DEBUG) {
			Log.v(TAG, message);
		}
	}

	/**
	 * Perform logging with Info level of verbosity
	 * 
	 * @param message
	 *            Message to log.
	 */
	public static void info(String message) {
		if (DEBUG) {
			Log.i(TAG, message);
		}
	}

	/**
	 * Perform logging with Error level of verbosity
	 * 
	 * @param message
	 *            Message to log.
	 */
	public static void error(String message) {
		if (DEBUG) {
			Log.e(TAG, message);
		}
	}

	public static void error(String message, Exception e) {
		if (DEBUG) {
			Log.e(TAG, message, e);
		}
	}

	public static void warn(String message) {
		if (DEBUG) {
			Log.w(TAG, message);
		}
	}
}
