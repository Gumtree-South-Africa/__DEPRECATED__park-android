package com.ebay.park.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.interfaces.FilterableActivity;
import com.ebay.park.model.CategoryModel;
import com.ebay.park.utils.FontsUtil;
import com.ebay.park.utils.PreferencesUtil;
import com.ebay.park.utils.SwrveEvents;
import com.swrve.sdk.SwrveSDK;

import java.util.*;

public class GroupFilterFragment extends BaseFragment {

	public static final String TAG = GroupFilterFragment.class.getSimpleName();
	private FilterableActivity mActivity;

	private Spinner mOrder;
	private EditText mMaxDistance;
    private CustomArrayAdapter mOrderAdapter;

	private static final int ORDER_ATOZ = 1;
	private static final int ORDER_NEAR = 2;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			this.mActivity = (FilterableActivity) activity;
		} catch (ClassCastException ex) {
			throw new ClassCastException("GroupFilterFragment must be attached to FilterableActivity");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onStart() {
		super.onStart();
		setTitle(R.string.group_filters_title);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.menu_filter, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_ok) {
			ParkApplication.sResetPageFromGroupFilter = true;
			filter();
			getBaseActivity().onBackPressed();
			return true;
		} else if (item.getItemId() == android.R.id.home){
			getBaseActivity().onBackPressed();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View baseview = inflater.inflate(R.layout.fragment_group_filter,
				(ViewGroup) super.onCreateView(inflater, container, savedInstanceState), true);

		mOrder = (Spinner) baseview.findViewById(R.id.spinner_order);
		mMaxDistance = (EditText) baseview.findViewById(R.id.max_distance);

        updateOrderAdapter();

		return baseview;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		disableRefreshSwipe();
	}

	@Override
	public void onResume() {
		super.onResume();
		fillFilters();
	}

	private void filter() {

		HashMap<String, String> params = new HashMap<String, String>();
		Map<String, String> swrveAttributes = new HashMap<String, String>();

		params.put(FilterableActivity.MAX_DISTANCE, String.valueOf(mMaxDistance.getText().toString()));

		if (!TextUtils.isEmpty(PreferencesUtil.getCurrentUserLocationName(getBaseActivity()))) {
			Map<String, String> attributes = new HashMap<String, String>();

			if (TextUtils.isEmpty(mMaxDistance.getText().toString())) {
				swrveAttributes.put(SwrveEvents.FILTERED_LOCATION, "N/A");

			} else {

				if (!mMaxDistance.getText().toString().equals("20")){
					swrveAttributes.put(SwrveEvents.FILTERED_LOCATION, PreferencesUtil.getCurrentUserLocationName(getBaseActivity()));
				}
			}
		}

		if (swrveAttributes.size() > 0) {
			SwrveSDK.userUpdate(swrveAttributes);
		}

		int orderId = (int)((CategoryModel) mOrder.getItemAtPosition(mOrder.getSelectedItemPosition())).getId();
		switch (orderId) {
			case ORDER_ATOZ:
				params.put(FilterableActivity.ORDER_BY, "name");
				break;
			case ORDER_NEAR:
				params.put(FilterableActivity.ORDER_BY, "nearest");
				break;
			default:
				break;
		}
		ParkApplication.sSelectedGroupFilters = params;
		ParkApplication.sComesFromFilter = true;
		mActivity.onFiltersConfirmed(params);
	}

	private void fillFilters() {

		HashMap<String, String> filters = ParkApplication.sSelectedGroupFilters;

		if (filters.containsKey(FilterableActivity.ORDER_BY)) {
			switch (filters.get(FilterableActivity.ORDER_BY)) {
				case "name":
					mOrder.setSelection(ORDER_ATOZ - 1, true);
					break;
				case "nearest":
					mOrder.setSelection(ORDER_NEAR - 1, true);
					break;
				default:
					break;
			}
		} else {
			mOrder.setSelection(ORDER_NEAR - 1, true);
		}
		if (filters.containsKey(FilterableActivity.MAX_DISTANCE)) {
			mMaxDistance.setText(filters.get(FilterableActivity.MAX_DISTANCE));
		} else {
			mMaxDistance.setText("20");
		}

	}

	@Override
	public void onRefresh() {
	}

    private void updateOrderAdapter() {

        ArrayList<CategoryModel> orderList = new ArrayList<>();
        orderList.add(new CategoryModel(ORDER_ATOZ, getString(R.string.a_to_z)));
        orderList.add(new CategoryModel(ORDER_NEAR, getString(R.string.nearest)));

        mOrderAdapter = new CustomArrayAdapter(getActivity(), orderList);
        mOrder.setAdapter(mOrderAdapter);
        mOrder.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                TextView firstCat = (TextView) parent.getChildAt(0);
                if (firstCat != null){
                    firstCat.setGravity(Gravity.RIGHT);
                    firstCat.setTypeface(FontsUtil.getFSBook(getBaseActivity()));
                    firstCat.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_arrow_down, 0);
                    firstCat.setCompoundDrawablePadding(35);
                    firstCat.setIncludeFontPadding(false);
                    firstCat.setTextColor(getResources().getColor(R.color.system_notification));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

	@Override
	public void onBackPressed() {
	}


	private class CustomArrayAdapter extends ArrayAdapter<CategoryModel> {
        public CustomArrayAdapter(Context ctx, List<CategoryModel> objects) {
            super(ctx, android.R.layout.simple_spinner_item, objects);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View view = null;
            TextView text = null;

            view = super.getView(position, null, parent);
            text = (TextView) view.findViewById(android.R.id.text1);
            text.setPadding((int) getResources().getDimension(R.dimen.padding_left_spinner), (int) getResources().getDimension(R.dimen.padding_top_spinner), (int) getResources().getDimension(R.dimen.padding_right_spinner), (int) getResources().getDimension(R.dimen.padding_bottom_spinner));
            text.setTypeface(FontsUtil.getFSBook(getBaseActivity()));
            text.setTextColor(view.getResources().getColor(R.color.system_notification));
            text.setCompoundDrawablePadding(45);

            if (position == 0) {
                text.setPadding((int) getResources().getDimension(R.dimen.padding_left_spinner), (int) getResources().getDimension(R.dimen.padding_first_top_spinner), (int) getResources().getDimension(R.dimen.padding_right_spinner), (int) getResources().getDimension(R.dimen.padding_bottom_spinner));
            }
            if (position == getCount()-1) {
                text.setPadding((int) getResources().getDimension(R.dimen.padding_left_spinner), (int) getResources().getDimension(R.dimen.padding_top_spinner), (int) getResources().getDimension(R.dimen.padding_right_spinner), (int) getResources().getDimension(R.dimen.padding_last_bottom_spinner));
            }

            if (position == mOrder.getSelectedItemPosition()) {
                text.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_check, 0);
            } else {
                text.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

            return view;
        }
    }

}
