package com.ebay.park.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.ebay.park.R;
import com.ebay.park.base.BaseSessionActivity;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.interfaces.BackPressable;
import com.ebay.park.model.ItemsListParamsModel;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.KeyboardHelper;
import com.ebay.park.utils.MessageUtil;

import java.util.ArrayList;

/**
 * Item detail activity.
 *
 * @author federico.perez
 */
public class ItemDetailActivity extends BaseSessionActivity {

    public static final String EXTRA_ITEM_ID = "item_id";
    public static final String EXTRA_SHOW_COMMENTS = "showComments";
    public static final String EXTRA_ITEM_LIST_PARAMS = "items_list_params";
    public static final String EXTRA_ITEM_IDS_LIST = "items_ids_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_container);

            if (DeviceUtils.isDeviceLollipopOrHigher()) {
                Toolbar parkToolbar = (Toolbar) findViewById(R.id.park_toolbar);
                parkToolbar.setNavigationIcon(R.drawable.icon_white_back);
                setSupportActionBar(parkToolbar);
            } else {
                getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                getSupportActionBar().setCustomView(R.layout.actionbar);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.icon_white_back);
            }
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            if (savedInstanceState == null) {
                final long itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
                if (itemId == -1) {
                    throw new IllegalStateException("Item detail activity started with no item id");
                }

                Bundle bundle = getIntent().getExtras();
                ItemsListParamsModel itemsListParms =  (ItemsListParamsModel) bundle.getSerializable(ItemDetailActivity.EXTRA_ITEM_LIST_PARAMS);
                ArrayList<Long> itemIds = (ArrayList<Long>) bundle.getSerializable(EXTRA_ITEM_IDS_LIST);
                ScreenManager.showItemDetailFragment(this, itemId, itemsListParms, itemIds);
            }

        } catch (IllegalStateException e) {
            finish();
        } catch (Exception e) {
            MessageUtil.showError(this, getString(R.string.error_generic), getCroutonsHolder());
            finish();
        }
    }

    @Override
    public ViewGroup getCroutonsHolder() {
        return (ViewGroup) findViewById(R.id.crouton_handle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            switch (item.getItemId()) {
                case android.R.id.home:
                    KeyboardHelper.hide(this,getCurrentFocus());
                    onSupportNavigateUp();
                    return true;
                default:
                    break;
            }
        } catch (NullPointerException e) {
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        boolean fragmentBack = getSupportFragmentManager().getBackStackEntryCount() > 0;
        if (fragmentBack) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment!=null) {
            ((BackPressable) fragment).onBackPressed();
        }
        super.onBackPressed();
    }

}
