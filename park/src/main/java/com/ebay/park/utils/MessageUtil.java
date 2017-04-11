package com.ebay.park.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.ebay.park.ParkApplication;
import com.ebay.park.ParkApplication.UnloggedNavigations;
import com.ebay.park.R;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.flow.ScreenManager;

import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class MessageUtil {

	private MessageUtil() {
	}

	/**
	 * Shows an error to the user.
	 *
	 * @param activity
	 * @param error
	 * 			The error message.
	 * @param view
	 * 			The view which the crouton will get added.
	 */
	public static void showError(BaseActivity activity, String error, ViewGroup view) {
		if (activity != null) {
			if (TextUtils.isEmpty(error)) {
				return;
			}
			switch (error) {
				case "Network is not available":
					error = ParkApplication.getInstance().getString(R.string.error_no_internet);
					break;
				case "Exception occurred during invocation of web service.":
					error = ParkApplication.getInstance().getString(R.string.error_timeout);
					break;
				default:
					break;
			}
			Crouton.makeText(activity, error, Style.ALERT, view).show();
		}
	}

	/**
	 * Shows an error to the user.
	 *
	 * @param activity
	 * @param error
	 *            The error message resource.
	 * @param view
	 */
	public static void showError(BaseActivity activity, int error, ViewGroup view) {
		if (activity != null && error != 0) {
			Crouton.makeText(activity, error, Style.ALERT, view).show();
		}
	}

	/**
	 * Shows a long error to the user until tap.
	 *
	 * @param activity
	 * @param error
	 * 			The error message resource.
	 * @param view
	 */
	public static void showLongError(BaseActivity activity, int error, ViewGroup view) {
		if (activity != null && error != 0) {
			Crouton.makeText(activity, error, Style.ALERT, view)
					.setConfiguration(new Configuration.Builder().setDuration(Configuration.DURATION_LONG).build()).show();

		}
	}

	/**
	 * Shows positive message to the user.
	 *
	 * @param activity
	 * @param success
	 * 			The success message.
	 * @param view
	 */
	public static void showSuccess(BaseActivity activity, String success, ViewGroup view) {
		if (activity != null && !TextUtils.isEmpty(success)) {
			Crouton.makeText(activity, success, Style.CONFIRM, view).show();
		}
	}

	public static void showLoginMsg(final Activity activity, final UnloggedNavigations whereToGo) {
		if (activity != null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setTitle(R.string.need_login);
			builder.setMessage(R.string.not_logged);
			builder.setPositiveButton(R.string.title_navdrawer_login, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ParkApplication.sFgmtOrAct_toGo = whereToGo;
					ScreenManager.showLoginScreen((BaseActivity) activity);
				}
			});
			builder.setNegativeButton(R.string.account_not_verified_negative, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			builder.create().show();
		}
	}
}
