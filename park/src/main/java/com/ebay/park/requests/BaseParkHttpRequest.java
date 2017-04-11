package com.ebay.park.requests;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.base.BaseSessionActivity;
import com.ebay.park.errors.GenericError;
import com.ebay.park.responses.BaseParkResponse;
import com.ebay.park.responses.ResponseCodes;
import com.ebay.park.utils.Logger;
import com.globant.roboneck.requests.BaseNeckHttpRequest;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseParkHttpRequest<T> extends BaseNeckHttpRequest<T, BaseParkResponse> {

    public BaseParkHttpRequest(Class<T> clazz) {
        super(clazz);
    }

    /**
     * Gets default {@link Gson} that must be used for all requests. If
     * additional configuration is required override this method.
     *
     * @return {@link Gson} object.
     */
    protected Gson getGson() {
        return new Gson();
    }

    @Override
    protected Map<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", getContentType());
        headers.put("Accept-Language", "es");// TEMPORAL HARDCODED TO FORCE
        return headers;
    }

    @Override
    protected BaseParkResponse processContent(String responseBody) {
        Logger.info(responseBody);
        return getGson().fromJson(responseBody, BaseParkResponse.class);
    }

    @Override
    protected boolean isLogicError(BaseParkResponse response) {
        return response.getStatusCode() != ResponseCodes.SUCCESS_CODE;
    }

    @Override
    protected Error processError(int httpStatus, BaseParkResponse response, String responseBody) {
        if (response != null) {
            if (response.getErrorCode() == ResponseCodes.Signout.USER_UNAUTHORIZED) {
                if (!ParkApplication.sMessageSessionExpiredAlreadyShown) {
                    ParkApplication.sMessageSessionExpiredAlreadyShown = true;
                    new Logout(response.getStatusMessage()).start();
                }
                return new GenericError(response.getStatusCode(), response.getErrorCode(), "");

            } else if (response.getErrorCode() == ResponseCodes.Signout.APP_DEPRECATED) {
                if (!ParkApplication.sMessageAppDeprecatedAlreadyShown) {
                    ParkApplication.sMessageAppDeprecatedAlreadyShown = true;
                    new ShowConfirmDeprecatedAppDialog(response.getStatusMessage()).start();
                }
                return new GenericError(response.getStatusCode(), response.getErrorCode(), response.getStatusMessage());
            } else {
                return new GenericError(response.getStatusCode(), response.getErrorCode(), response.getStatusMessage());
            }
        }

        return new GenericError(httpStatus, httpStatus, ParkApplication.sCurrentContext.getString(R.string.request_generic_error));
    }

    public class Logout extends Thread {

        private String message;

        public Logout(String message) {
            this.message = message;
        }

        public void run() {
            ((BaseActivity) ParkApplication.sCurrentContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (ParkApplication.sCurrentContext != null) {
                            try {
                                if (!((BaseSessionActivity) ParkApplication.sCurrentContext).isFinishing()) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(
                                            ParkApplication.sCurrentContext);
                                    builder.setTitle(R.string.title_session_expired);
                                    builder.setMessage(message);
                                    builder.setPositiveButton(android.R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    try {
                                                        ((BaseSessionActivity) ParkApplication.sCurrentContext).logout(false);
                                                    } catch (ClassCastException e) {
                                                    } catch (NullPointerException e){
                                                    }
                                                }
                                            });
                                    builder.setCancelable(false);
                                    builder.create().show();
                                }
                            } catch (Exception e) {
                            }
                        }
                    } catch (NullPointerException e) {
                    }
                }
            });
        }
    }

    private void openPlayStore() {

        final String appPackageName = ParkApplication.sCurrentContext.getPackageName();
        try {
            ParkApplication.sCurrentContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                    .parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            ParkApplication.sCurrentContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri
                    .parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public class ShowConfirmDeprecatedAppDialog extends Thread {

        String message;

        public ShowConfirmDeprecatedAppDialog(String message) {
            this.message = message;
        }

        public void run() {
            ((BaseActivity) ParkApplication.sCurrentContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (ParkApplication.sCurrentContext != null) {
                            try {
                                if (!((BaseActivity) ParkApplication.sCurrentContext).isFinishing()) {

                                    AlertDialog.Builder builder = new AlertDialog.Builder(
                                            ParkApplication.sCurrentContext);
                                    builder.setTitle(R.string.aviso);
                                    builder.setMessage(message);
                                    builder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            ParkApplication.sMessageAppDeprecatedAlreadyShown = false;
                                            ((BaseActivity) ParkApplication.sCurrentContext).finish();
                                            openPlayStore();
                                        }
                                    });

                                    builder.setOnCancelListener(new OnCancelListener() {
                                        @Override
                                        public void onCancel(DialogInterface dialog) {
                                            ParkApplication.sMessageAppDeprecatedAlreadyShown = false;
                                            ((BaseActivity) ParkApplication.sCurrentContext).finish();
                                        }
                                    });
                                    builder.create().show();
                                }
                            } catch (ClassCastException e) {
                            } catch (NullPointerException e) {
                            }
                        }
                    } catch (NullPointerException e) {
                    }
                }
            });
        }
    }

    /**
     * Gets the Content-type request header value.
     *
     * @return The request header value. Example: application/json.
     */
    protected abstract String getContentType();

    protected Integer getApiVersion() {
        return 3;
    }

    protected Integer getApiVersion4() {
        return 4;
    }

    protected Integer getNewApiVersion() {
        return 5;
    }

    protected Integer getApiMinorVersion() {
        return 0;
    }

    private static final String API_URL_FORMAT = "v%1$d.%2$d";

    protected final String getApiUri() {
        return String.format(API_URL_FORMAT, getApiVersion(), getApiMinorVersion());
    }
    protected final String getApiUri4() {
        return String.format(API_URL_FORMAT, getApiVersion4(), getApiMinorVersion());
    }
    protected final String getNewApiUri() {
        return String.format(API_URL_FORMAT, getNewApiVersion(), getApiMinorVersion());
    }

    @Override
    protected String getUrl() {
        return ParkUrls.SERVER + getUrlFormat();
    }

    protected abstract String getUrlFormat();

}
