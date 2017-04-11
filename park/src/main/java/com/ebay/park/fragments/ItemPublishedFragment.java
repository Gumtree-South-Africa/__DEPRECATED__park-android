package com.ebay.park.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.model.GroupModel;
import com.ebay.park.model.ItemModel;
import com.ebay.park.model.PublishItemModel;
import com.ebay.park.model.ZipCodeLocationModel;
import com.ebay.park.requests.GroupListRequest;
import com.ebay.park.requests.ItemDetailRequest;
import com.ebay.park.requests.ItemUpdateRequest;
import com.ebay.park.requests.ZipCodeLocationRequest;
import com.ebay.park.responses.GroupListResponse;
import com.ebay.park.responses.ZipCodesResponse;
import com.ebay.park.utils.Constants;
import com.ebay.park.utils.KeyboardHelper;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.Validations;
import com.ebay.park.views.StickyScrollView;
import com.globant.roboneck.requests.BaseNeckRequestException;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class ItemPublishedFragment extends BaseFragment {

    public static final String ID_PARAM = "id";
    private static final long NO_ID = -123L;

    private TextView mTextViewEditLocation;
    private TextView mTextViewItemLocation;
    private ImageView mImageViewSelectAll;
    private ListView mListViewGroups;
    private EditText mEditTextEditZipCode;
    private TextView mTextViewErrorZipCode;
    private RelativeLayout mRelativeLayoutEditZipCode;
    private ProgressBar mProgressBarLoadZipCode;
    private View mEmptyView;
    private TextView mTextViewEmptyMessage;
    private TextView mTextViewEmptyHint;
    private LinearLayout mLinearLayoutLocation;
    private StickyScrollView mStickyView;
    private TextView mLocationZc;

    private long mItemId;
    private String mLocationName;
    private String mLocationZipCode;
    private String mLocationId;
    private String mLocationLatitude;
    private String mLocationLongitude;
    private ItemModel mItemPublished = new ItemModel();
    private MenuItem mUpdateItem;
    private CheckboxState mSelectGroupsState = CheckboxState.NONE;
    private static LinkedHashMap<String, Integer> sCheckedGroupsList;
    private Boolean mChanges = false;


    public enum CheckboxState {
        ALL, SOME, NONE
    }

    public ItemPublishedFragment() {
    }

    public static ItemPublishedFragment forItem(long id) {
        ItemPublishedFragment fragment = new ItemPublishedFragment();
        Bundle args = new Bundle();
        args.putLong(ID_PARAM, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null || !getArguments().containsKey(ID_PARAM)) {
            throw new IllegalStateException("Item published fragment called with no id args.");
        }
        mItemId = getArguments().getLong(ID_PARAM, NO_ID);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View baseView = inflater.inflate(R.layout.fragment_item_published,
                (ViewGroup) super.onCreateView(inflater, container, savedInstanceState), true);

        mEmptyView = inflater
                .inflate(R.layout.empty_view, (ViewGroup) baseView.findViewById(R.id.list_container), true);

        return baseView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_done, menu);
        mUpdateItem = menu.findItem(R.id.action_done);
        if (mUpdateItem != null){
            mUpdateItem.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_done:
                updateItem();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mStickyView = (StickyScrollView) view.findViewById(R.id.item_published_scroll);
        mTextViewEditLocation = (TextView) view.findViewById(R.id.tv_edit_published_location);
        mTextViewItemLocation = (TextView) view.findViewById(R.id.tv_item_location);
        mImageViewSelectAll = (ImageView) view.findViewById(R.id.iv_select_all);
        mListViewGroups = (ListView) view.findViewById(R.id.lv_groups);
        mEditTextEditZipCode = (EditText) view.findViewById(R.id.et_edit_zip_code);
        mTextViewErrorZipCode = (TextView) view.findViewById(R.id.tv_error_zip_code);
        mRelativeLayoutEditZipCode = (RelativeLayout) view.findViewById(R.id.ly_edit_zip_code);
        mProgressBarLoadZipCode = (ProgressBar) view.findViewById(R.id.progress_load_zip_code);
        mLinearLayoutLocation = (LinearLayout) view.findViewById(R.id.ly_location);
        mLocationZc = (TextView) view.findViewById(R.id.tv_location_zc);

        mTextViewEmptyMessage = (TextView) mEmptyView.findViewById(R.id.empty_message);
        mTextViewEmptyHint = (TextView) mEmptyView.findViewById(R.id.empty_hint);

        mEditTextEditZipCode.addTextChangedListener(new ZipCodeWatcher());
        mTextViewEditLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextViewItemLocation.setVisibility(View.GONE);
                mLocationZc.setVisibility(View.GONE);
                mLinearLayoutLocation.setVisibility(View.GONE);
                mTextViewItemLocation.setText("");
                mEditTextEditZipCode.setText("");
                mEditTextEditZipCode.requestFocus();
                KeyboardHelper.show(getBaseActivity(), mEditTextEditZipCode);
                mRelativeLayoutEditZipCode.setVisibility(View.VISIBLE);
                mTextViewErrorZipCode.setVisibility(View.INVISIBLE);
                mChanges = true;
            }
        });

        mImageViewSelectAll.setEnabled(false);
        mImageViewSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mChanges = true;
                    UserGroupsAdapter adapter = (UserGroupsAdapter) mListViewGroups.getAdapter();
                    if (mSelectGroupsState == CheckboxState.SOME || mSelectGroupsState == CheckboxState.NONE) {

                        for (int i = 0; i < adapter.getCount(); i++) {
                            mListViewGroups.setItemChecked(i, true);

                            String name = adapter.getItem(i).getName();
                            int id = adapter.getItem(i).getId();
                            sCheckedGroupsList.put(name, id);
                        }

                        mSelectGroupsState = CheckboxState.ALL;
                        mImageViewSelectAll.setImageDrawable(getResources().getDrawable(R.drawable.checkbox_checked_turquoise));

                    } else {
                        for (int i = 0; i < adapter.getCount(); i++) {
                            mListViewGroups.setItemChecked(i, false);
                        }
                        sCheckedGroupsList.clear();
                        mSelectGroupsState = CheckboxState.NONE;
                        mImageViewSelectAll.setImageDrawable(getResources().getDrawable(R.drawable.checkbox_unchecked));
                    }
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    MessageUtil.showError(getBaseActivity(), getString(R.string.error_generic),
                            getBaseActivity().getCroutonsHolder());
                }
            }
        });

        mLocationId = "";
        mLocationName = "";
        mLocationZipCode = "";
        mLocationLatitude = "";
        mLocationLongitude = "";
        sCheckedGroupsList = new LinkedHashMap<String, Integer>();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        disableRefreshSwipe();
        showProgress();
        onRefresh();
    }

    @Override
    public void onRefresh() {
        mSpiceManager.execute(new ItemDetailRequest(mItemId), new ItemDetailListener());
    }

    private void updateItem(){
        if (locationOk()) {
            if (mChanges){
                showProgress();
                mSpiceManager.execute(buildUpdateItemRequest(), new UpdateItemRequestListener());
            } else {
                getBaseActivity().finish();
                ScreenManager.showItemDetailActivity(getBaseActivity(), mItemPublished.getId(),
                        null, null);
            }
        }
    }

    private Boolean locationOk(){
        if (TextUtils.isEmpty(mEditTextEditZipCode.getText())) {
            if (mRelativeLayoutEditZipCode.getVisibility() == View.VISIBLE){
                setLocationError();
                return false;
            }
        } else {
            if (!Validations.validateZipCode(mEditTextEditZipCode)) {
                if (mRelativeLayoutEditZipCode.getVisibility() == View.VISIBLE){
                    setLocationError();
                    return false;
                }
            } else {
                if (mTextViewErrorZipCode.getVisibility() == View.VISIBLE){
                    return false;
                }
            }
        }
        return true;
    }

    private ItemUpdateRequest buildUpdateItemRequest() {

        Double _lat = -1000.0;
        Double _long = -1000.0;
        if (!TextUtils.isEmpty(mLocationLatitude)) {
            _lat = Double.valueOf(mLocationLatitude);
        }
        if (!TextUtils.isEmpty(mLocationLongitude)) {
            _long = Double.valueOf(mLocationLongitude);
        }

        ItemUpdateRequest.Builder updateRequest = new ItemUpdateRequest.Builder(mItemPublished.getId(), false)
                .location(mLocationId).latitude(_lat).longitude(_long).locationName(mLocationName.trim())
                .zipCode(mLocationZipCode).groups(createGroupsArray());

        return updateRequest.build();
    }

    private ArrayList<Integer> createGroupsArray() {

        ArrayList<Integer> selectedGroups = new ArrayList<Integer>();

        if (sCheckedGroupsList != null) {
            if (sCheckedGroupsList.size() > 0) {
                selectedGroups = new ArrayList<Integer>(sCheckedGroupsList.values());
            }
        }
        return selectedGroups;
    }

    private class UpdateItemRequestListener extends BaseNeckRequestListener<PublishItemModel> {

        @Override
        public void onRequestSuccessfull(PublishItemModel update) {
            if (update != null) {
                getBaseActivity().finish();
                ScreenManager.showItemDetailActivity(getBaseActivity(), mItemPublished.getId(),
                        null, null);
            }
        }

        @Override
        public void onRequestError(BaseNeckRequestException.Error error) {
            Logger.verb("onRequestError");
            MessageUtil.showError(getBaseActivity(), error.getMessage(),
                    getBaseActivity().getCroutonsHolder());
            hideProgress();
            fullFocusUpScrollView();
        }

        @Override
        public void onRequestNotFound() {
        }

        @Override
        public void onRequestException(SpiceException ex) {
            hideProgress();
            fullFocusUpScrollView();
            MessageUtil.showError(getBaseActivity(), ex.getMessage(), getBaseActivity().getCroutonsHolder());
            Logger.verb(ex.getLocalizedMessage());
        }

    }

    private void getUserGroups(){
        Integer page = 0;
        Integer pageSize = 100000;
        mSpiceManager.execute(new GroupListRequest.Builder().page(page.longValue()).pageSize(pageSize.longValue())
                .build(ParkApplication.getInstance().getUsername()), new ListGroupsRequestListener());
    }

    private class UserGroupsAdapter extends ArrayAdapter<GroupModel> {

        public UserGroupsAdapter(Context context, List<GroupModel> objects) {
            super(context, R.layout.item_user_group_creation, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            UserGroupViewHolder viewHolder;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_user_group_creation, parent, false);

                viewHolder = new UserGroupViewHolder();
                viewHolder.mUserGroupName = (TextView) convertView.findViewById(R.id.tv_group_name);
                viewHolder.mGroupCheck = (CheckBox) convertView.findViewById(R.id.chbGroup);
                viewHolder.mGroupImg = (ImageView) convertView.findViewById(R.id.iv_group_picture);
                viewHolder.mGroupLocation = (TextView) convertView.findViewById(R.id.tv_group_location);
                viewHolder.mGroupSubscs = (TextView) convertView.findViewById(R.id.group_subscrs);
                viewHolder.mGroupItems = (TextView) convertView.findViewById(R.id.group_items);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (UserGroupViewHolder) convertView.getTag();
            }

            final GroupModel group = getItem(position);

            if (!TextUtils.isEmpty(group.getPictureUrl())) {
                Picasso.with(getContext()).load(group.getPictureUrl()).placeholder(R.drawable.groups_placeholder_xs)
                        .fit().centerCrop().into(viewHolder.mGroupImg);
            } else {
                Picasso.with(getContext()).load(R.drawable.groups_placeholder_xs).into(viewHolder.mGroupImg);
            }

            viewHolder.mUserGroupName.setText(group.getName());
            viewHolder.mGroupLocation.setText(group.getLocationName());
            viewHolder.mGroupSubscs.setText(String.valueOf(group.getTotalSubscribers()));
            viewHolder.mGroupItems
                    .setText(getString(R.string.items_label) + " " + String.valueOf(group.getTotalItems()));

            if (group != null) {
                if (sCheckedGroupsList.get(group.getName()) == null) {
                    viewHolder.mGroupCheck.setChecked(false);
                } else {
                    viewHolder.mGroupCheck.setChecked(true);
                }
            }

            viewHolder.mGroupCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mChanges = true;
                    CheckBox chk = (CheckBox) v;
                    if (chk.isChecked()) {
                        sCheckedGroupsList.put(group.getName(), group.getId());
                        if (sCheckedGroupsList.size() == mListViewGroups.getAdapter().getCount()){
                            mImageViewSelectAll.setImageDrawable(getResources().getDrawable(R.drawable.checkbox_checked_turquoise));
                            mSelectGroupsState = CheckboxState.ALL;
                        } else {
                            mImageViewSelectAll.setImageDrawable(getResources().getDrawable(R.drawable.checkbox_mixed));
                            mSelectGroupsState = CheckboxState.SOME;
                        }
                    } else {
                        sCheckedGroupsList.remove(group.getName());
                        if (sCheckedGroupsList.size() == 0){
                            mImageViewSelectAll.setImageDrawable(getResources().getDrawable(R.drawable.checkbox_unchecked));
                            mSelectGroupsState = CheckboxState.NONE;
                        } else {
                            mImageViewSelectAll.setImageDrawable(getResources().getDrawable(R.drawable.checkbox_mixed));
                            mSelectGroupsState = CheckboxState.SOME;
                        }
                    }
                }
            });

            return convertView;
        }

        class UserGroupViewHolder {
            TextView mUserGroupName;
            CheckBox mGroupCheck;
            ImageView mGroupImg;
            TextView mGroupLocation;
            TextView mGroupSubscs;
            TextView mGroupItems;
        }

    }

    private class ListGroupsRequestListener extends BaseNeckRequestListener<GroupListResponse> {

        @Override
        public void onRequestError(BaseNeckRequestException.Error error) {
            mListViewGroups.setEmptyView(mEmptyView);
            mTextViewEmptyMessage.setText(error.getMessage());
            mListViewGroups.setAdapter(new UserGroupsAdapter(getBaseActivity(), new ArrayList<GroupModel>()));
            hideProgress();
        }

        @Override
        public void onRequestSuccessfull(GroupListResponse response) {
            hideProgress();
            getActivity().supportInvalidateOptionsMenu();

            if (response.getGroups().size() == 0) {
                mListViewGroups.setEmptyView(mEmptyView);
                mTextViewEmptyMessage.setText(response.getNoResultsMessage());
                mTextViewEmptyHint.setText(response.getNoResultsHint());
            }
            else {
                mImageViewSelectAll.setEnabled(true);
            }

            mListViewGroups.setAdapter(new UserGroupsAdapter(getBaseActivity(), response.getGroups()));
            setListViewHeightBasedOnChildren(mListViewGroups);

        }

        @Override
        public void onRequestException(SpiceException exception) {
            Logger.error(exception.getMessage());
            mListViewGroups.setEmptyView(mEmptyView);
            mTextViewEmptyMessage.setText(R.string.unable_to_load_groups);
            MessageUtil.showError(getBaseActivity(), exception.getMessage(), getBaseActivity().getCroutonsHolder());
            mListViewGroups.setAdapter(new UserGroupsAdapter(getBaseActivity(), new ArrayList<GroupModel>()));
            hideProgress();
        }
    }

    private class ZipCodeWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (isAdded()) {
                mEditTextEditZipCode.setHint(getResources().getString(R.string.enter_zip_code));
                final String zipCode = s.toString();
                if (zipCode.length() >= Constants.ZIPCODE_LENGTH) {
                    getLocationFromZipCode();
                    removeLocationError();
                    KeyboardHelper.hide(getBaseActivity(),getView());
                    mProgressBarLoadZipCode.setVisibility(View.VISIBLE);
                } else {
                    mProgressBarLoadZipCode.setVisibility(View.GONE);
                }
            }
        }
    }

    private void getLocationFromZipCode() {
        mSpiceManager.executeCacheRequest(buildZipCodeRequest(), new ZipCodeRequestListener());
    }

    private ZipCodeLocationRequest buildZipCodeRequest() {
        String zipCode = mEditTextEditZipCode.getText().toString();
        return new ZipCodeLocationRequest(zipCode);
    }

    private class ZipCodeRequestListener extends BaseNeckRequestListener<ZipCodeLocationModel> {

        @Override
        public void onRequestError(BaseNeckRequestException.Error error) {
            mProgressBarLoadZipCode.setVisibility(View.GONE);
            if (error.getMessage().equals("ZERO_RESULTS") || error.getMessage().equals("UNKNOWN_ERROR")) {
                setLocationError();
            } else {
                MessageUtil.showError(getBaseActivity(),error.getMessage(), getBaseActivity().getCroutonsHolder());
            }
        }

        @Override
        public void onRequestSuccessfull(ZipCodeLocationModel zipCodeLocationModel) {
            if (zipCodeLocationModel != null) {
                if (zipCodeLocationModel.getResults() != null && !zipCodeLocationModel.getResults().isEmpty()) {
                    ArrayList<String> results = new ArrayList<String>();
                    for (ZipCodesResponse.Result res : zipCodeLocationModel.getResults()) {
                        results.add(res.toString());
                    }
                    String strLocName = zipCodeLocationModel.getResults().get(0).toString();
                    ZipCodesResponse.Result.Geometry.Location loc = zipCodeLocationModel.getResults().get(0).getGeometry().getLocation();
                    mLocationName = strLocName;
                    mLocationZipCode = mEditTextEditZipCode.getText().toString();
                    mLocationLatitude = String.valueOf(loc.getLat());
                    mLocationLongitude = String.valueOf(loc.getLng());
                    mTextViewItemLocation.setText(mLocationName);
                    mLocationZc.setText("(" + mLocationZipCode + ")");
                    mLinearLayoutLocation.setVisibility(View.VISIBLE);
                    mTextViewItemLocation.setVisibility(View.VISIBLE);
                    mLocationZc.setVisibility(View.VISIBLE);
                    mProgressBarLoadZipCode.setVisibility(View.GONE);
                    mRelativeLayoutEditZipCode.setVisibility(View.GONE);
                    mEditTextEditZipCode.requestFocus();
                    mTextViewErrorZipCode.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onRequestException(SpiceException exception) {
            mProgressBarLoadZipCode.setVisibility(View.GONE);
            Logger.verb(exception.getLocalizedMessage());
            MessageUtil.showError(getBaseActivity(), getString(R.string.error_cannot_get_address),
                    getBaseActivity().getCroutonsHolder());
        }
    }

    private void setLocationError() {
        mEditTextEditZipCode.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_validation_error, 0);
        mTextViewErrorZipCode.setVisibility(View.VISIBLE);
    }

    private void removeLocationError() {
        mEditTextEditZipCode.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        mTextViewErrorZipCode.setVisibility(View.INVISIBLE);
    }

    private void fullFocusUpScrollView() {
        mStickyView.post(new Runnable() {
            @Override
            public void run() {
                mStickyView.fullScroll(ScrollView.FOCUS_UP);
            }
        });
    }

    private class ItemDetailListener extends BaseNeckRequestListener<ItemModel> {

        @Override
        public void onRequestError(BaseNeckRequestException.Error error) {
            hideProgress();
            MessageUtil.showError(getBaseActivity(), error.getMessage(), getBaseActivity().getCroutonsHolder());
            Logger.error(error.getMessage());
        }

        @Override
        public void onRequestSuccessfull(ItemModel item) {
            mItemPublished = item;
            if (!TextUtils.isEmpty(mItemPublished.getLocation())) {
                if (!TextUtils.isEmpty(mItemPublished.getZipCode())) {
                    mTextViewItemLocation.setText(item.getLocationName());
                    mLocationZc.setText("(" + item.getZipCode() + ")");
                } else {
                    mTextViewItemLocation.setText(mItemPublished.getLocationName());
                }
                mTextViewItemLocation.setVisibility(View.VISIBLE);
                mLocationZc.setVisibility(View.VISIBLE);
                mLocationId = item.getLocation();
                mLocationName = item.getLocationName();
                mLocationZipCode = item.getZipCode();
                mLocationLatitude = String.valueOf(item.getLatitude());
                mLocationLongitude = String.valueOf(item.getLongitude());
            }
            getUserGroups();
        }

        @Override
        public void onRequestException(SpiceException exception) {
            hideProgress();
            MessageUtil.showError(getBaseActivity(), exception.getMessage(), getBaseActivity().getCroutonsHolder());
            Logger.error(exception.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        getBaseActivity().finish();
        ScreenManager.showItemDetailActivity(getBaseActivity(), mItemPublished.getId(),
                null, null);
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        UserGroupsAdapter listAdapter = (UserGroupsAdapter) listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, AbsListView.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount()));
        listView.setLayoutParams(params);
    }
}
