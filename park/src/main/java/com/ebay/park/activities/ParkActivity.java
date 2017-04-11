package com.ebay.park.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseActivity;
import com.ebay.park.base.BaseSessionActivity;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.fragments.LeftMenuFragment;
import com.ebay.park.fragments.LeftMenuFragment.LeftMenuOption;
import com.ebay.park.fragments.LeftMenuFragment.OnLeftMenuInteractionListener;
import com.ebay.park.fragments.MyGroupsFragment;
import com.ebay.park.fragments.MyLists.Whishlist;
import com.ebay.park.fragments.MyListsTab;
import com.ebay.park.interfaces.Subscribable;
import com.ebay.park.model.ProfileModel;
import com.ebay.park.model.UnreadBadgeCountModel;
import com.ebay.park.requests.DeviceRegistrationRequest;
import com.ebay.park.requests.ProfileRequest;
import com.ebay.park.requests.UnReadBadgeCountRequest;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.FacebookUtil;
import com.ebay.park.utils.GCMUtils;
import com.ebay.park.utils.KeyboardHelper;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.PreferencesUtil;
import com.ebay.park.utils.SwrveEvents;
import com.ebay.park.utils.TwitterUtil;
import com.facebook.appevents.AppEventsLogger;
import com.globant.roboneck.requests.BaseNeckRequestException;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.swrve.sdk.SwrveSDK;

import java.util.HashMap;
import java.util.Map;

import me.leolin.shortcutbadger.ShortcutBadger;

import static com.ebay.park.services.WearCallListenerService.ITEM_ID;

public class ParkActivity extends BaseSessionActivity implements OnLeftMenuInteractionListener,
        Subscribable, Whishlist {

    public static final String MENU_OPTION_EXTRA = "MENU_OPTION";
    public static final String EXTRA_FROM_ERROR = "FROM_ERROR";
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Context mContext;
    private String mDeviceId;
    private String mUniqueDeviceId;
    public static ParkActivity parkActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park);

        mContext = this;

        if (savedInstanceState == null) {
            if (getIntent().getBooleanExtra(EXTRA_FROM_ERROR, false)) {
                showOOMMessage();
            }
        }

        if (DeviceUtils.isDeviceLollipopOrHigher()) {
            Toolbar parkToolbar = (Toolbar) findViewById(R.id.park_toolbar);
            parkToolbar.setPadding(0, DeviceUtils.getStatusBarHeight(this), 0, 0);
            setSupportActionBar(parkToolbar);
        } else {
            getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            getSupportActionBar().setCustomView(R.layout.actionbar);
        }

        moveDrawerToTop();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setElevation(0);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                try {
                    LeftMenuFragment menuFragment = (LeftMenuFragment) getSupportFragmentManager().findFragmentById(
                            R.id.left_menu_fragment);
                    if (menuFragment != null) {
                        menuFragment.unReadCountersHandler();
                        menuFragment.updateProfileIconPic();
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (ClassCastException e){
                    e.printStackTrace();
                }
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mDrawerToggle.setHomeAsUpIndicator(R.drawable.icon_white_hamburger);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        registerDevice();

        Map<String, String> swrveAttributes = new HashMap<String, String>();
        if (PreferencesUtil.getParkToken(this) == null) {
            swrveAttributes.put(SwrveEvents.STATUS, "guest");
        } else {
            swrveAttributes.put(SwrveEvents.STATUS, "registered");
        }
        SwrveSDK.userUpdate(swrveAttributes);

    }

    public static ParkActivity getInstance() {
        return parkActivity;
    }

    private void moveDrawerToTop() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        DrawerLayout drawer = (DrawerLayout) inflater.inflate(R.layout.navigation_drawer, null); // "null" is important.

        // Remove the first child of decor view
        ViewGroup decor = (ViewGroup) getWindow().getDecorView();
        View child = decor.getChildAt(0);
        decor.removeView(child);
        FrameLayout container = (FrameLayout) drawer.findViewById(R.id.container); // This is the container we defined just now.
        container.addView(child, 0);
        drawer.findViewById(R.id.left_menu_fragment).setPadding(0, DeviceUtils.getStatusBarHeight(this), 0, 0);

        // Make the drawer replace the first child
        decor.addView(drawer);
    }

    private void registerDevice() {
        if (GCMUtils.checkPlayServices(this)) {
            mUniqueDeviceId = DeviceUtils.getUniqueDeviceId(this);
            mDeviceId = GCMUtils.getRegistrationId(this);
            if (mDeviceId.isEmpty()) {
                new getDeviceId().execute();
            }
        } else {
            Logger.info("No valid Google Play Services APK found.");
        }
    }

    private class getDeviceId extends AsyncTask<Void, Void, String> {

        protected String doInBackground(Void... arg0) {
            return GCMUtils.registerOnGCM((BaseActivity) mContext);
        }

        protected void onPostExecute(String deviceId) {
            mSpiceManager.execute(new DeviceRegistrationRequest(deviceId, mUniqueDeviceId), new DeviceRegistrationRequestListener());
        }
    }

    /**
     * Listener for device registration responses from Park server.
     */
    private class DeviceRegistrationRequestListener extends BaseNeckRequestListener<String> {

        @Override
        public void onRequestSuccessfull(String response) {
            com.ebay.park.utils.Logger.verb("onRequestSuccessfull");
//            MessageUtil.showSuccess((BaseActivity) mContext, "Registrado");
        }

        @Override
        public void onRequestError(BaseNeckRequestException.Error error) {
            com.ebay.park.utils.Logger.verb("onRequestError");
//            MessageUtil.showError((BaseActivity) mContext, error.getMessage());
        }

        @Override
        public void onRequestException(SpiceException ex) {
            com.ebay.park.utils.Logger.verb(ex.getLocalizedMessage());
//            MessageUtil.showError((BaseActivity) mContext, ex.getLocalizedMessage());
        }

    }

    private void showOOMMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_recover);
        builder.setMessage(R.string.app_recover_info);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_park, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                KeyboardHelper.hide(this,getCurrentFocus());
                if (mDrawerLayout.isDrawerOpen(findViewById(R.id.left_menu_fragment))) {
                    mDrawerLayout.closeDrawer(findViewById(R.id.left_menu_fragment));
                } else {
                    mDrawerLayout.openDrawer(findViewById(R.id.left_menu_fragment));
                }
                return true;
            case R.id.action_search:
                ScreenManager.showGlobalSearch(this);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void hideLeftmenu() {
        mDrawerLayout.closeDrawer(findViewById(R.id.left_menu_fragment));
    }

    @Override
    protected void onStart() {
        super.onStart();
        ParkApplication.sSelectedItemFilters.clear();
        ParkApplication.sSelectedUserFilters.clear();
        ParkApplication.sSelectedGroupFilters.clear();
        PreferencesUtil.setParkActivityActive(this,true);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                handleIntent(getIntent());
            }
        });
    }

    private void handleIntent(Intent intent) {
        if (intent.hasExtra(MENU_OPTION_EXTRA)) {
            int menuOptionIndex = getIntent().getIntExtra(MENU_OPTION_EXTRA,
                    LeftMenuFragment.DEFAULT_MENU_OPTION.ordinal());
            LeftMenuOption option = LeftMenuOption.values()[menuOptionIndex];
            try {
                LeftMenuFragment menuFragment = (LeftMenuFragment) getSupportFragmentManager().findFragmentById(
                        R.id.left_menu_fragment);
                if (menuFragment != null) {
                    menuFragment.handleMenuOptionSelected(option);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (ClassCastException e){
                e.printStackTrace();
            }

            intent.removeExtra(MENU_OPTION_EXTRA);
            setIntent(intent);
        }
        if (intent.hasExtra(ITEM_ID)){
            Long itemId = intent.getLongExtra(ITEM_ID, -1L);
            if (!itemId.equals(-1L)){
                ScreenManager.showItemDetailActivity(this,itemId, null, null);
            }
            intent.removeExtra(ITEM_ID);
            setIntent(intent);
        }
    }

    public void doLogout() {
        super.logout(false);
    }

    private class BannedListener extends BaseNeckRequestListener<ProfileModel> {

        @Override
        public void onRequestError(Error error) {
        }

        @Override
        public void onRequestSuccessfull(ProfileModel profile) {

            PreferencesUtil.saveCurrentUserLocationName(mContext, profile.getLocationName());

            if (profile.getStatus().equals("BANNED")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ParkActivity.this);
                builder.setTitle(R.string.account_banned);
                builder.setMessage(R.string.account_banned_text);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doLogout();
                    }
                });
                builder.setCancelable(false);
                builder.create().show();
            } else {

                for (String social : profile.getUserSocials()) {
                    if (social.equals("facebook")) {
                        FacebookUtil.saveIsFacebookLoggedInAlready(getApplicationContext());
                    } else if (social.equals("twitter")) {
                        TwitterUtil.saveIsTwitterLoggedInAlready(getApplicationContext());
                    }
                }
            }
        }

        @Override
        public void onRequestException(SpiceException exception) {
        }

    }

    public void notifyGroupSuscribed() {
        Fragment myGroupsFragment = getSupportFragmentManager().findFragmentByTag(MyGroupsFragment.TAG);
        if (myGroupsFragment != null) {
            ((Subscribable) myGroupsFragment).notifyGroupSuscribed();
        }
    }

    @Override
    public void notifyLiked() {
        Fragment myWhishList = getSupportFragmentManager().findFragmentByTag(MyListsTab.TAG);
        if (myWhishList != null) {
            ((Whishlist) myWhishList).notifyLiked();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
    }

    public void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);

        if (PreferencesUtil.getParkToken(this) != null) {
            // Logout if the user is banned:
            mSpiceManager.execute(new ProfileRequest(ParkApplication.getInstance().getUsername()), new BannedListener());

            // Update badge counter if an user is logged
            unReadBadgeCountersHandler();
        }
    }

    @Override
    public ViewGroup getCroutonsHolder() {
        ViewGroup view = (ViewGroup) findViewById(R.id.crouton_handle);
        if (view != null) {
            view.setPadding(0, DeviceUtils.getStatusBarHeight(this), 0, 0);
        }
        return view;
    }

    public void unReadBadgeCountersHandler()
    {
        mSpiceManager.execute(new UnReadBadgeCountRequest(), new BaseNeckRequestListener<UnreadBadgeCountModel>() {

            @Override
            public void onRequestError(Error error) {
            }

            @Override
            public void onRequestSuccessfull(UnreadBadgeCountModel count) {
                if (count != null) {
                    if (count.getUnreadFeedsCounter() > 0) {
                        ShortcutBadger.applyCount(mContext, count.getUnreadFeedsCounter());
                    } else {
                        ShortcutBadger.removeCount(mContext);
                    }
                }
            }

            @Override
            public void onRequestException(SpiceException exception) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        PreferencesUtil.setParkActivityActive(this,false);
        super.onDestroy();
    }
}
