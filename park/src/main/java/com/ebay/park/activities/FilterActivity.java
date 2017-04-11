package com.ebay.park.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.ebay.park.R;
import com.ebay.park.base.BaseSessionActivity;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.fragments.GlobalSearchFragment;
import com.ebay.park.interfaces.FilterableActivity;
import com.ebay.park.interfaces.SearchableFragment;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.MessageUtil;

import java.util.Map;

public class FilterActivity extends BaseSessionActivity implements FilterableActivity {

    public static final String EXTRA_FILTER_ID = "filter_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_container);

            if (DeviceUtils.isDeviceLollipopOrHigher()) {
                Toolbar parkToolbar = (Toolbar) findViewById(R.id.park_toolbar);
                parkToolbar.setPadding(0, DeviceUtils.getStatusBarHeight(this), 0, 0);
                parkToolbar.setNavigationIcon(R.drawable.icon_white_back);
                setSupportActionBar(parkToolbar);
            } else {
                getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                getSupportActionBar().setCustomView(R.layout.actionbar);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon_white_back);
            }
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            if (savedInstanceState == null) {
                final int filterId = getIntent().getIntExtra(EXTRA_FILTER_ID, -1);
                if (filterId == -1) {
                    throw new IllegalStateException("Filter activity started with no filter id");
                }
                switch (filterId) {
                    case GlobalSearchFragment.ITEMS_POS:
                        ScreenManager.showFilterFragment(this);
                        break;
                    case GlobalSearchFragment.USERS_POS:
                        ScreenManager.showUserFilterFragment(this);
                        break;
                    case GlobalSearchFragment.GROUP_POS:
                        ScreenManager.showGroupFilterFragment(this);
                        break;
                }
            }

        } catch (IllegalStateException e) {
            finish();
        } catch (Exception e) {
            MessageUtil.showError(this, getString(R.string.error_generic),
                    getCroutonsHolder());
            finish();
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

    @Override
    public void onFiltersConfirmed(Map<String, String> filters) {
        Fragment aSearchableFragment = getSupportFragmentManager().findFragmentByTag(GlobalSearchFragment.TAG);
        if (aSearchableFragment != null) {
            ((SearchableFragment) aSearchableFragment).onFilterSelected(filters, "");
        }
    }
}
