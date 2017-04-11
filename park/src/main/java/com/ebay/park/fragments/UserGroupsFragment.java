package com.ebay.park.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.model.GroupModel;
import com.ebay.park.requests.GroupListRequest;
import com.ebay.park.responses.GroupListResponse;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.squareup.picasso.Picasso;

import java.util.*;

/**
 * User groups list screen.
 *
 * @author Nicol�s Mat�as Fern�ndez
 *
 */
public class UserGroupsFragment extends BaseFragment {

	private ListView mUserGroupsList;
	private LinearLayout mHeader;
	private View mEmptyView;
	private TextView mEmptyMessage;
	private TextView mEmptyHint;
	private ImageView mImageViewSelectAll;
	private CheckboxState mSelectGroupsState = CheckboxState.NONE;
	public static Boolean sShowAcceptGroupsSelectionMenuItem = true;

	@Override
	public void onBackPressed() {
	}

	public enum CheckboxState {
		ALL, SOME, NONE
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final ViewGroup parentView = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
		final View baseview = inflater.inflate(R.layout.fragment_user_groups_list, parentView, true);

		mHeader = (LinearLayout) baseview.findViewById(R.id.ly_header);
		mHeader.setVisibility(View.GONE);
		mUserGroupsList = (ListView) baseview.findViewById(R.id.user_groups_list);
		mUserGroupsList.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				int topRowVerticalPos = (mUserGroupsList == null || mUserGroupsList.getChildCount() == 0) ? 0
						: mUserGroupsList.getChildAt(0).getTop();
				mSyncProgressBar.setEnabled(topRowVerticalPos >= 0 && firstVisibleItem == 0);
			}
		});

		mEmptyView = inflater
				.inflate(R.layout.empty_view, (ViewGroup) baseview.findViewById(R.id.list_container), true);
		mEmptyMessage = (TextView) mEmptyView.findViewById(R.id.empty_message);
		mEmptyHint = (TextView) mEmptyView.findViewById(R.id.empty_hint);
		mImageViewSelectAll = (ImageView) baseview.findViewById(R.id.iv_select_all);
		mImageViewSelectAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				try {
					UserGroupsAdapter adapter = (UserGroupsAdapter) mUserGroupsList.getAdapter();
					if (mSelectGroupsState == CheckboxState.SOME || mSelectGroupsState == CheckboxState.NONE) {

						for (int i = 0; i < adapter.getCount(); i++) {
							mUserGroupsList.setItemChecked(i, true);

							String name = adapter.getItem(i).getName();
							int id = adapter.getItem(i).getId();
							ItemCreateEditFragment.sChkBoxList.put(name, id);
						}

						mSelectGroupsState = CheckboxState.ALL;
						mImageViewSelectAll.setImageDrawable(getResources().getDrawable(R.drawable.checkbox_checked_turquoise));

					} else {

						for (int i = 0; i < adapter.getCount(); i++) {
							mUserGroupsList.setItemChecked(i, false);
						}
						ItemCreateEditFragment.sChkBoxList.clear();
						mSelectGroupsState = CheckboxState.NONE;
						mImageViewSelectAll.setImageDrawable(getResources().getDrawable(R.drawable.checkbox_unchecked));

					}
					adapter.notifyDataSetChanged();
				} catch (ClassCastException e) {
					MessageUtil.showError(getBaseActivity(), getString(R.string.error_generic),
							getBaseActivity().getCroutonsHolder());
				} catch (NullPointerException e) {
					MessageUtil.showError(getBaseActivity(), getString(R.string.error_generic),
							getBaseActivity().getCroutonsHolder());
				} catch (IndexOutOfBoundsException e) {
					MessageUtil.showError(getBaseActivity(), getString(R.string.error_generic),
							getBaseActivity().getCroutonsHolder());
				}
			}
		});

		return baseview;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		sShowAcceptGroupsSelectionMenuItem = false;
		getActivity().supportInvalidateOptionsMenu();
		showProgress();
		onRefresh();
	}

	@Override
	public void onResume() {
		super.onResume();
		setTitle(R.string.title_user_groups);
	}

	@Override
	public void onRefresh() {
		Integer page = 0;
		Integer pageSize = 100000;
		mSpiceManager.execute(new GroupListRequest.Builder().page(page.longValue()).pageSize(pageSize.longValue())
				.build(ParkApplication.getInstance().getUsername()), new ListGroupsRequestListener());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_user_groups, menu);
		if (menu.findItem(R.id.action_accept_groups_selection) != null){
			menu.findItem(R.id.action_accept_groups_selection).setVisible(sShowAcceptGroupsSelectionMenuItem);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.action_accept_groups_selection:
				getBaseActivity().finish();
				break;
			default:
				break;
		}

		return super.onOptionsItemSelected(item);
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
				viewHolder.mChbGroup = (CheckBox) convertView.findViewById(R.id.chbGroup);
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
				if (ItemCreateEditFragment.sChkBoxList.get(group.getName()) == null) {
					viewHolder.mChbGroup.setChecked(false);
					mImageViewSelectAll.setImageDrawable(getResources().getDrawable(R.drawable.checkbox_unchecked));
					mSelectGroupsState = CheckboxState.NONE;
				} else {
					viewHolder.mChbGroup.setChecked(true);

					if (ItemCreateEditFragment.sChkBoxList.size() > 0) {
						if (ItemCreateEditFragment.sChkBoxList.size() == mUserGroupsList.getAdapter().getCount()){
							mImageViewSelectAll.setImageDrawable(getResources().getDrawable(R.drawable.checkbox_checked_turquoise));
							mSelectGroupsState = CheckboxState.ALL;
						} else {
							mImageViewSelectAll.setImageDrawable(getResources().getDrawable(R.drawable.checkbox_mixed));
							mSelectGroupsState = CheckboxState.SOME;
						}
					}
				}
			}

			viewHolder.mChbGroup.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					CheckBox chk = (CheckBox) v;
					if (chk.isChecked()) {
						ItemCreateEditFragment.sChkBoxList.put(group.getName(), group.getId());
						if (ItemCreateEditFragment.sChkBoxList.size() == mUserGroupsList.getAdapter().getCount()){
							mImageViewSelectAll.setImageDrawable(getResources().getDrawable(R.drawable.checkbox_checked_turquoise));
							mSelectGroupsState = CheckboxState.ALL;
						} else {
							mImageViewSelectAll.setImageDrawable(getResources().getDrawable(R.drawable.checkbox_mixed));
							mSelectGroupsState = CheckboxState.SOME;
						}
					} else {
						ItemCreateEditFragment.sChkBoxList.remove(group.getName());
						if (ItemCreateEditFragment.sChkBoxList.size() == 0){
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
			CheckBox mChbGroup;
			ImageView mGroupImg;
			TextView mGroupLocation;
			TextView mGroupSubscs;
			TextView mGroupItems;
		}

	}

	private class ListGroupsRequestListener extends BaseNeckRequestListener<GroupListResponse> {

		@Override
		public void onRequestError(Error error) {
			mUserGroupsList.setEmptyView(mEmptyView);
			mEmptyMessage.setText(error.getMessage());
			mUserGroupsList.setAdapter(new UserGroupsAdapter(getBaseActivity(), new ArrayList<GroupModel>()));
			hideProgress();
		}

		@Override
		public void onRequestSuccessfull(GroupListResponse response) {
			hideProgress();
			sShowAcceptGroupsSelectionMenuItem = true;
			getActivity().supportInvalidateOptionsMenu();

			if (response.getGroups().size() == 0) {
				mUserGroupsList.setEmptyView(mEmptyView);
				mEmptyMessage.setText(response.getNoResultsMessage());
				mEmptyHint.setText(response.getNoResultsHint());
				mHeader.setVisibility(View.GONE);
			} else{
				mHeader.setVisibility(View.VISIBLE);
			}

			if (ItemCreateEditFragment.sChkBoxList == null) {
				ItemCreateEditFragment.sChkBoxList = new LinkedHashMap<String, Integer>();
			}

			for (int i = 0; i < response.getGroups().size(); i++) {
				String name = response.getGroups().get(i).getName();
				if (ItemCreateEditFragment.sChkBoxList.get(name) == null) {
					mImageViewSelectAll.setImageDrawable(getResources().getDrawable(R.drawable.checkbox_unchecked));
					mSelectGroupsState = CheckboxState.NONE;
				}
			}

			mUserGroupsList.setAdapter(new UserGroupsAdapter(getBaseActivity(), response.getGroups()));

			if (response.getGroups().size() > 0) {
				if (ItemCreateEditFragment.sChkBoxList.size() == mUserGroupsList.getAdapter().getCount()){
					mImageViewSelectAll.setImageDrawable(getResources().getDrawable(R.drawable.checkbox_checked_turquoise));
					mSelectGroupsState = CheckboxState.ALL;
				} else {
					mImageViewSelectAll.setImageDrawable(getResources().getDrawable(R.drawable.checkbox_mixed));
					mSelectGroupsState = CheckboxState.SOME;
				}
			}

			UserGroupsAdapter adapter = (UserGroupsAdapter) mUserGroupsList.getAdapter();
			adapter.notifyDataSetChanged();

		}

		@Override
		public void onRequestException(SpiceException exception) {
			Logger.error(exception.getMessage());
			mUserGroupsList.setEmptyView(mEmptyView);
			mEmptyMessage.setText(R.string.unable_to_load_groups);
			mHeader.setVisibility(View.GONE);
			MessageUtil.showError(getBaseActivity(), exception.getMessage(),
					getBaseActivity().getCroutonsHolder());
			mUserGroupsList.setAdapter(new UserGroupsAdapter(getBaseActivity(), new ArrayList<GroupModel>()));
			hideProgress();
		}
	}

}