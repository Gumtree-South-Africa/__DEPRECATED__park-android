package com.ebay.park;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crittercism.app.Crittercism;
import com.ebay.park.activities.ParkActivity;
import com.ebay.park.configuration.ParkConfiguration;
import com.ebay.park.model.ItemModel;
import com.ebay.park.utils.GCMUtils;
import com.ebay.park.utils.PreferencesUtil;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.accountkit.AccountKit;
import com.facebook.appevents.AppEventsLogger;
import com.globant.roboneck.NeckApp;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.code.microlog4android.Level;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.appender.FileAppender;
import com.google.code.microlog4android.format.Formatter;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.swrve.sdk.SwrveSDK;
import com.swrve.sdk.config.SwrveConfig;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import io.branch.referral.Branch;

public class ParkApplication extends NeckApp {

    public static final String DEV_BUILD_VARIANT = "dev";
    public static final String STG_BUILD_VARIANT = "stg";
    public static final String PROD_BUILD_VARIANT = "release";
    public static final String QA_BUILD_VARIANT = "debug";

    private static ParkApplication sInstance;
    public static ParkConfiguration sParkConfiguration;
    private String mSessionToken;
    private String mUsername;
    private String mUserProfilePicture;
    private RefWatcher mRefWatcher;
    public static Context sCurrentContext;
    public static Boolean sMessageSessionExpiredAlreadyShown = false;
    public static Boolean sMessageAppDeprecatedAlreadyShown = false;
    public static HashMap<String, String> sSelectedItemFilters = new HashMap<String, String>();
    public static HashMap<String, String> sSelectedUserFilters = new HashMap<String, String>();
    public static HashMap<String, String> sSelectedGroupFilters = new HashMap<String, String>();
    public static List<Long> sResetGroupNotification = new ArrayList<>();
    public static Boolean sResetPageFromItemFilter = false;
    public static Boolean sResetPageFromUserFilter = false;
    public static Boolean sResetPageFromGroupFilter = false;
    public static Boolean sComesFromFilter = false;
    public static final Logger LOGGER = LoggerFactory.getLogger();
    private static int sLogCount = 1;
    public static boolean sJustLogged = false;
    public static boolean sJustLoggedSecondLevel = false;
    public static boolean sJustLoggedThirdLevel = false;
    public static boolean sJustLoggedFromNavegar = false;
    public static int sNavegarTab_toGo = -1;
    public static UnloggedNavigations sFgmtOrAct_toGo;
    public static Boolean sGroupJustCreated = false;
    public static ItemModel sItemTapped = null;
    public static AppEventsLogger sAppEventsLogger;

    public static enum UnloggedNavigations {
        POST_AD, CREATE_GROUP, MAKE_OFFER, CATEGORIES, OFFERS, MY_LISTS, GROUPS, ACTIVITY, PREFERENCES;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AccountKit.initialize(getApplicationContext());
        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());

        sInstance = this;
        sParkConfiguration = getConfiguration();
//        Crittercism.initialize(getApplicationContext(), sParkConfiguration.getCrittercismAppId());
        FacebookSdk.setApplicationId(sParkConfiguration.getFacebookAppId());
        if (sParkConfiguration.getGlobalTrackerEnv().equals("QA") || sParkConfiguration.getGlobalTrackerEnv().equals("DEV")) {
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);
        }
        FacebookSdk.sdkInitialize(this);
        AppEventsLogger.activateApp(this);
        sAppEventsLogger = AppEventsLogger.newLogger(this);
        mRefWatcher = LeakCanary.install(this);
        initMicroLog();

        if (sParkConfiguration.getGlobalTrackerEnv().equals("PROD")){
            Branch.getAutoInstance(this);
        } else {
            Branch.getAutoTestInstance(this);
        }

        try {
            SwrveSDK.createInstance(this, sParkConfiguration.getSwrveAppId(), sParkConfiguration.getSwrveApiKey(), SwrveConfig.withPush((GCMUtils.SENDER_ID)));
        } catch (IllegalArgumentException exp) {
            Log.e("Swrve on Viva", "Could not initialize the Swrve SDK", exp);
        }
    }

    public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {

        private Thread.UncaughtExceptionHandler defaultUEH;

        public CustomExceptionHandler() {
            this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        }

        public void uncaughtException(Thread t, Throwable e) {
            Log.e("Tag", "uncaughtException", e);
            if (e.getClass().equals(OutOfMemoryError.class)) {
                handleUncaughtException(t, e);
            } else {
                defaultUEH.uncaughtException(t, e);
            }
        }
    }

    public void handleUncaughtException(Thread thread, Throwable e) {
        Crittercism.logHandledException(e);
        Intent intent = new Intent(getApplicationContext(), ParkActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        intent.putExtra(ParkActivity.EXTRA_FROM_ERROR, true);
        startActivity(intent);
        System.exit(1); // kill off the crashed app
        e.printStackTrace();
    }

    public static ParkConfiguration getConfiguration() {
        return getConfiguration(BuildConfig.BUILD_TYPE);
    }

    public static ParkConfiguration getConfiguration(String aEnviorment) {
        ParkConfiguration aConfiguration;
        switch (aEnviorment) {
            case DEV_BUILD_VARIANT:
                aConfiguration = ParkConfiguration.getInstance("DEV");
                break;
            case STG_BUILD_VARIANT:
                aConfiguration = ParkConfiguration.getInstance("STG");
                break;
            case PROD_BUILD_VARIANT:
                aConfiguration = ParkConfiguration.getInstance("PROD");
                break;
            case QA_BUILD_VARIANT:
            default:
                aConfiguration = ParkConfiguration.getInstance("QA");
                break;
        }
        return aConfiguration;
    }

    public static ParkApplication getInstance() {
        return sInstance;
    }

    public AppEventsLogger getEventsLogger(){
        return sAppEventsLogger;
    }

    public String getSessionToken() {
        if (mSessionToken == null) {
            mSessionToken = PreferencesUtil.getParkToken(getApplicationContext());
        }
        return mSessionToken;
    }

    public String getUsername() {
        if (mUsername == null) {
            mUsername = PreferencesUtil.getCurrentUsername(getApplicationContext());
        }
        return mUsername;
    }

    public void setUsername(String username) {
        this.mUsername = username;
        PreferencesUtil.saveCurrentUser(getApplicationContext(), username);
    }

    public void setSessionToken(String sessionToken) {
        this.mSessionToken = sessionToken;
        PreferencesUtil.saveParkToken(getApplicationContext(), sessionToken);
    }

    public void clearSession() {
        mSessionToken = null;
        mUsername = null;
        mUserProfilePicture = null;
    }

    public void setUserProfilePicture(String userProfilePicture) {
        this.mUserProfilePicture = userProfilePicture;
        PreferencesUtil.setCurrentUserProfilePic(getApplicationContext(), userProfilePicture);
    }

    public void resetGroupNotification(long groupId) {
        sResetGroupNotification.add(groupId);
    }

    public List<Long> getGroupsWithResetedNotifications() {
        return sResetGroupNotification;
    }

    // **** Google Analytics V4 ****
    public enum TrackerName {
        GLOBAL_TRACKER, OTHERS
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t;
            switch (ParkApplication.sParkConfiguration.getGlobalTrackerEnv()) {
                case "QA":
                    t = analytics.newTracker(R.xml.global_tracker_qa);
                    break;
                case "PROD":
                    t = analytics.newTracker(R.xml.global_tracker_prod);
                    break;
                case "STG":
                    t = analytics.newTracker(R.xml.global_tracker_stg);
                    break;
                case "DEV":
                    t = analytics.newTracker(R.xml.global_tracker_dev);
                    break;
                default:
                    t = analytics.newTracker(R.xml.global_tracker_qa);
                    break;
            }

            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }

    // **** MicroLog ****
    private void initMicroLog() {
        FileAppender appender = new FileAppender();
        Formatter formatter = new Formatter() {

            @Override
            public void setProperty(String arg0, String arg1) {
            }

            @Override
            public String[] getPropertyNames() {
                return null;
            }

            @SuppressLint("SimpleDateFormat")
            @Override
            public String format(String arg0, String arg1, long arg2, Level arg3, Object arg4, Throwable arg5) {

                String finalStr;
                DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Calendar cal = Calendar.getInstance();
                finalStr = "[" + (sLogCount++) + "] - " + dateFormat.format(cal.getTime()) + arg4;
                return finalStr;
            }
        };
        appender.setFormatter(formatter);
        appender.setFileName("/VivanunciosLog.txt");
        LOGGER.addAppender(appender);
    }

    public static RefWatcher getRefWatcher(Context context) {
        ParkApplication application = (ParkApplication) context.getApplicationContext();
        return application.mRefWatcher;
    }

}
