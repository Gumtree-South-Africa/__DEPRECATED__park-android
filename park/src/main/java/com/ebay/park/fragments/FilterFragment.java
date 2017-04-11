package com.ebay.park.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.interfaces.FilterableActivity;
import com.ebay.park.model.CategoryModel;
import com.ebay.park.requests.ItemCategoryRequest;
import com.ebay.park.responses.ItemCategoryResponse;
import com.ebay.park.utils.FontsUtil;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.PreferencesUtil;
import com.ebay.park.utils.SwrveEvents;
import com.ebay.park.utils.Validations;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.swrve.sdk.SwrveSDK;

import java.util.*;

public class FilterFragment extends BaseFragment {

	public static final String TAG = FilterFragment.class.getSimpleName();
	private static final int ALL_CATEGORIES = -123;

	private FilterableActivity mActivity;
	private Spinner mCategories;
	private Spinner mOrder;
	private EditText mMinPrice;
	private EditText mMaxPrice;
	private EditText mMaxDistance;
	private MenuItem mOkAction;
	private ArrayList<CategoryModel> mCategoryList = new ArrayList<>();
	private CustomArrayAdapter mCategoriesAdapter;
	private CustomArrayAdapter mOrderAdapter;

	private static final int ORDER_CAT = 1;
	private static final int ORDER_LOWP = 2;
	private static final int ORDER_HIGHP = 3;
	private static final int ORDER_NEAR = 4;
	private static final int ORDER_DATE = 5;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			this.mActivity = (FilterableActivity) activity;
		} catch (ClassCastException ex) {
			throw new ClassCastException("FilterFragment must be attached to FilterableActivity");
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
		setTitle(R.string.filter);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.menu_filter, menu);
		mOkAction = menu.findItem(R.id.action_ok);
		if (mOkAction != null) {
			mOkAction.setVisible(false);
		}
		mSpiceManager.executeCacheRequest(new ItemCategoryRequest(), new CategoriesRequestListener());
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_ok) {
			ParkApplication.sResetPageFromItemFilter = true;
			filter();
			goBack();
			mMinPrice.setInputType(EditorInfo.TYPE_NULL);
			mMaxPrice.setInputType(EditorInfo.TYPE_NULL);
			mMaxDistance.setInputType(EditorInfo.TYPE_NULL);
			return true;
		}else if (item.getItemId() == android.R.id.home){
			goBack();
		}
		return super.onOptionsItemSelected(item);
	}

	private void goBack(){
		if (getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
			getActivity().getSupportFragmentManager().popBackStack();
		}else{
			getBaseActivity().onBackPressed();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View baseview = inflater.inflate(R.layout.fragment_filter,
				(ViewGroup) super.onCreateView(inflater, container, savedInstanceState), true);

		mCategories = (Spinner) baseview.findViewById(R.id.spinner_categories);
		mOrder = (Spinner) baseview.findViewById(R.id.spinner_order);
		mMinPrice = (EditText) baseview.findViewById(R.id.filter_min_price);
		mMaxPrice = (EditText) baseview.findViewById(R.id.filter_max_price);
		mMaxDistance = (EditText) baseview.findViewById(R.id.max_distance);

		mMinPrice.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {

				if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					mMaxPrice.requestFocus();
					return true;
				} else {
					return false;
				}
			}
		});

		return baseview;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		disableRefreshSwipe();
		showProgress();
	}

	@Override
	public void onRefresh() {
	}

	private void filter() {

		HashMap<String, String> params = new HashMap<String, String>();
		Map<String, String> swrveAttributes = new HashMap<String, String>();

		long catId = ((CategoryModel) mCategories.getItemAtPosition(mCategories.getSelectedItemPosition())).getId();

		if (catId != ALL_CATEGORIES) {
			params.put(FilterableActivity.CATEGORY, String.valueOf(catId));
		}
		if (Validations.isNotEmpty(mMaxPrice)) {
			params.put(FilterableActivity.PRICE_TO, String.valueOf(mMaxPrice.getText().toString()));
		}
		if (Validations.isNotEmpty(mMinPrice)) {
			params.put(FilterableActivity.PRICE_FROM, String.valueOf(mMinPrice.getText().toString()));
		}

		params.put(FilterableActivity.MAX_DISTANCE, String.valueOf(mMaxDistance.getText().toString()));

		if (!TextUtils.isEmpty(PreferencesUtil.getCurrentUserLocationName(getBaseActivity()))) {

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
			case ORDER_CAT:
				params.put(FilterableActivity.ORDER_BY, "category");
				break;
			case ORDER_HIGHP:
				params.put(FilterableActivity.ORDER_BY, "-price");
				break;
			case ORDER_LOWP:
				params.put(FilterableActivity.ORDER_BY, "price");
				break;
			case ORDER_NEAR:
				params.put(FilterableActivity.ORDER_BY, "nearest");
				break;
			case ORDER_DATE:
				params.put(FilterableActivity.ORDER_BY, "published");
				break;
			default:
				break;
		}

		ParkApplication.sSelectedItemFilters = params;
		ParkApplication.sComesFromFilter = true;
		mActivity.onFiltersConfirmed(params);
	}

	@Override
	public void onBackPressed() {
	}

	private class CategoriesRequestListener extends BaseNeckRequestListener<ItemCategoryResponse> {

		@Override
		public void onRequestError(Error error) {
			hideProgress();
			MessageUtil.showError(getBaseActivity(), "Could not get categories list.",
					getBaseActivity().getCroutonsHolder());
		}

		@Override
		public void onRequestSuccessfull(ItemCategoryResponse response) {
			hideProgress();

			mCategoryList.clear();
			mCategoryList.addAll(response.getCategories());
			ArrayList<CategoryModel> finalCategoryList = getCategories();
			if (!finalCategoryList.get(0).getName().equals(getString(R.string.all))) {
				finalCategoryList.add(0, new CategoryModel(ALL_CATEGORIES, getString(R.string.all)));
			}
			updateCategoriesAdapter(finalCategoryList);
			updateOrderAdapter();


			fillFilters();
			if (mOkAction != null){
				mOkAction.setVisible(true);
			}
		}

		@Override
		public void onRequestException(SpiceException exception) {
			hideProgress();
			MessageUtil.showError(getBaseActivity(), exception.getMessage(),
					getBaseActivity().getCroutonsHolder());
			Logger.error(exception.getMessage());
		}
	}

	private ArrayList<CategoryModel> getCategories() {

		ArrayList<CategoryModel> cleanCategories = new ArrayList<>();
		Iterator<CategoryModel> aIterator = mCategoryList.iterator();
		while (aIterator.hasNext()) {
			CategoryModel oCategory = aIterator.next();
			if (oCategory.getSelectable() != null) {
				if (oCategory.getSelectable()) {
					cleanCategories.add(oCategory);
				}
			}
		}
		return cleanCategories;
	}

	private void fillFilters() {

		HashMap<String, String> filters = ParkApplication.sSelectedItemFilters;

		if (filters.containsKey(FilterableActivity.CATEGORY)) {

			for (int i = 0; i < mCategories.getAdapter().getCount(); i++) {
				if (Integer.parseInt(filters.get(FilterableActivity.CATEGORY)) == (((CategoryModel) mCategories
						.getAdapter().getItem(i)).getId())) {
					mCategories.setSelection(i, true);
					break;
				}
			}
		}

		if (filters.containsKey(FilterableActivity.PRICE_FROM)) {
			mMinPrice.setText(filters.get(FilterableActivity.PRICE_FROM));
		}
		if (filters.containsKey(FilterableActivity.PRICE_TO)) {
			mMaxPrice.setText(filters.get(FilterableActivity.PRICE_TO));
		}
		if (filters.containsKey(FilterableActivity.ORDER_BY)) {
			switch (filters.get(FilterableActivity.ORDER_BY)) {
				case "category":
					mOrder.setSelection(ORDER_CAT - 1, true);
					break;
				case "-price":
					mOrder.setSelection(ORDER_HIGHP - 1, true);
					break;
				case "price":
					mOrder.setSelection(ORDER_LOWP - 1, true);
					break;
				case "nearest":
					mOrder.setSelection(ORDER_NEAR - 1, true);
					break;
				case "published":
					mOrder.setSelection(ORDER_DATE - 1, true);
					break;
				default:
					break;
			}
		} else {
			mOrder.setSelection(ORDER_DATE - 1, true);
		}
		if (filters.containsKey(FilterableActivity.MAX_DISTANCE)) {
			mMaxDistance.setText(filters.get(FilterableActivity.MAX_DISTANCE));
		} else {
			mMaxDistance.setText("20");
		}

	}

	private void updateCategoriesAdapter(ArrayList<CategoryModel> categories) {

		mCategoriesAdapter = new CustomArrayAdapter(getActivity(), categories);
		mCategories.setTag("CAT");
		mCategories.setAdapter(mCategoriesAdapter);
		mCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

	private void updateOrderAdapter() {

		ArrayList<CategoryModel> orderList = new ArrayList<>();
		orderList.add(new CategoryModel(ORDER_CAT, getString(R.string.category)));
		orderList.add(new CategoryModel(ORDER_LOWP, getString(R.string.lowest_price)));
		orderList.add(new CategoryModel(ORDER_HIGHP, getString(R.string.highest_price)));
		orderList.add(new CategoryModel(ORDER_NEAR, getString(R.string.nearest)));
		orderList.add(new CategoryModel(ORDER_DATE, getString(R.string.published_date)));

		mOrderAdapter = new CustomArrayAdapter(getActivity(), orderList);
		mOrder.setTag("ORD");
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

			Spinner spCurrent;
			if (((ListView) parent).getAdapter().getCount() == 5) {
				spCurrent = mOrder;
			} else {
				spCurrent = mCategories;
			}

            if (position == 0) {
                text.setPadding((int) getResources().getDimension(R.dimen.padding_left_spinner), (int) getResources().getDimension(R.dimen.padding_first_top_spinner), (int) getResources().getDimension(R.dimen.padding_right_spinner), (int) getResources().getDimension(R.dimen.padding_bottom_spinner));
            }
            if (position == getCount()-1) {
                text.setPadding((int) getResources().getDimension(R.dimen.padding_left_spinner), (int) getResources().getDimension(R.dimen.padding_top_spinner), (int) getResources().getDimension(R.dimen.padding_right_spinner), (int) getResources().getDimension(R.dimen.padding_last_bottom_spinner));
            }

			if (position == spCurrent.getSelectedItemPosition()) {
				text.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_check, 0);
			} else {
				text.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			}

			return view;
		}
	}
    
}