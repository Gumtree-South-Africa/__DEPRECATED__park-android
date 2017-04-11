package com.ebay.park.base;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ebay.park.ParkApplication;
import com.ebay.park.ParkApplication.UnloggedNavigations;
import com.ebay.park.R;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.model.FollowerModel;
import com.ebay.park.requests.FollowUserRequest;
import com.ebay.park.requests.UnfollowUserRequest;
import com.ebay.park.responses.UserListResponse;
import com.ebay.park.utils.FacebookUtil;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.PreferencesUtil;
import com.github.clans.fab.FloatingActionButton;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.google.android.gms.analytics.HitBuilders;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseUserListFragment extends BaseListFragment<UserListResponse> {

	protected ListView mUserList;
	private UserAdapter mAdapter;
	private List<FollowerModel> mToBeRemoved = new ArrayList<FollowerModel>();

	private int mPreviousVisibleItem;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_follow_list,
				getContainerLayout(inflater, container, savedInstanceState), true);
	}

	@Override
	protected void clearData() {
		if (mAdapter!=null){
			mAdapter.clear();
		} else {
			mAdapter = new UserAdapter();
		}
		page = START_PAGE;
	}

	@Override
	protected ModelAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	protected void initializeAdapter() {
		mAdapter = new UserAdapter();
	}

	protected boolean forEdition() {
		return false;
	}

	protected List<FollowerModel> getRemovedFollowers() {
		return this.mToBeRemoved;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (ParkApplication.sComesFromFilter && mAdapter == null){
			ParkApplication.sComesFromFilter = false;
			getActivity().finish();
		}
	}

	@Override
	public void onViewCreated(View rootView, Bundle savedInstanceState) {
		super.onViewCreated(rootView, savedInstanceState);
		loadEmptyView(getBaseActivity(), (ViewGroup) rootView);
		mUserList = (ListView) rootView.findViewById(R.id.follow_list);
		loadHeaderView();
		mUserList.setAdapter(mAdapter);
		mUserList.setOnScrollListener(new ListScroll());

		final FloatingActionButton fab = (FloatingActionButton) getBaseActivity().findViewById(R.id.fab_publish);
		if (fab != null){
			final BaseListFragment.ListScroll listScroll = new BaseListFragment.ListScroll();
			mUserList.setOnScrollListener(new AbsListView.OnScrollListener() {

				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
				}

				@Override
				public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
					listScroll.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

					if (fab.getVisibility() != View.GONE) {
						if (firstVisibleItem > mPreviousVisibleItem) {
							fab.hide(true);
						} else if (firstVisibleItem < mPreviousVisibleItem) {
							fab.show(true);
						}
						mPreviousVisibleItem = firstVisibleItem;
					}
				}
			});
		}
	}

	protected void loadHeaderView() {
	}

	protected void onUserClicked(FollowerModel aUser) {
		if (!mIsLoading) {
			ScreenManager.showUserProfileFragment(getBaseActivity(), aUser.getUsername(), true);
		}
	}

	protected void onUsernameClick(String username) {
		if (!mIsLoading) {
			ScreenManager.showUserProfileFragment(getBaseActivity(), username, true);
		}
	}

	protected class UserAdapter extends ModelAdapter<FollowerModel> {

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(getBaseActivity(), R.layout.item_user_follow, null);
				convertView.setTag(new ViewHolder(convertView));
			}
			ViewHolder aViewHolder = (ViewHolder) convertView.getTag();
			aViewHolder.load(getItem(position));
			return convertView;
		}

		private class ViewHolder {
			private View mContainer;
			private ImageView mUserImage;
			private TextView mUsername;
			private TextView mUserLocation;
			private TextView mFriendShip;
			private Button mBtnFollow;
			private Button mBtnFollowRemove;

			public ViewHolder(View vUser) {
				mContainer = vUser;
				mUserImage = (ImageView) vUser.findViewById(R.id.follow_list_profileimg);
				mUsername = (TextView) vUser.findViewById(R.id.follow_list_username);
				mUserLocation = (TextView) vUser.findViewById(R.id.follow_list_location);
				mFriendShip = (TextView) vUser.findViewById(R.id.follow_list_friendship);
				mBtnFollow = (Button) vUser.findViewById(R.id.follow_list_follow_btn);
				mBtnFollowRemove = (Button) vUser.findViewById(R.id.btn_remove_follower);
			}

			public void load(final FollowerModel aUser) {
				mContainer.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						onUserClicked(aUser);
					}
				});
				if (TextUtils.isEmpty(aUser.getProfilePicture())) {
					aUser.setProfilePicture(null);
				}
				Picasso.with(getBaseActivity()).load(aUser.getProfilePicture()).placeholder(R.drawable.avatar_medium_ph_image_fit).fit()
						.centerCrop().into(mUserImage);
				mUsername.setText(aUser.getUsername());
				mUsername.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						onUsernameClick(aUser.getUsername());
					}
				});

				if(TextUtils.isEmpty(aUser.getLocation())) {
					if(TextUtils.isEmpty(aUser.getLocationName())){
						mUserLocation.setText("");
					} else {
						mUserLocation.setText(aUser.getLocationName());
					}
				} else {
					mUserLocation.setText(aUser.getLocation());
					mFriendShip.setVisibility(View.VISIBLE);
					if(TextUtils.isEmpty(aUser.getFriendOf())) {
						mFriendShip.setText(getString(R.string.your_friendship));
					} else {
						mFriendShip.setText(getString(R.string.friendship_of) + " " + aUser.getFriendOf());
					}
				}

				if (aUser.getUsername().equals(ParkApplication.getInstance().getUsername())) {
					mBtnFollow.setVisibility(View.GONE);
					mBtnFollowRemove.setVisibility(View.GONE);
					if (forEdition()) {
						mContainer.setClickable(false);
						mUsername.setClickable(false);
					}
				} else {
					if (forEdition()) {
						mBtnFollow.setVisibility(View.GONE);
						mBtnFollowRemove.setVisibility(View.VISIBLE);
						mBtnFollowRemove.setTag(aUser);
						mUsername.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								updateRemoved(mBtnFollowRemove, (FollowerModel) mBtnFollowRemove.getTag(),
										mBtnFollowRemove.isSelected());
							}
						});
						mContainer.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								updateRemoved(mBtnFollowRemove, (FollowerModel) mBtnFollowRemove.getTag(),
										mBtnFollowRemove.isSelected());
							}
						});
						mBtnFollowRemove.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								updateRemoved(v, (FollowerModel) v.getTag(), v.isSelected());
							}
						});
						mBtnFollowRemove.setSelected(mToBeRemoved.contains(aUser));
					} else {
						mBtnFollowRemove.setVisibility(View.GONE);
						mBtnFollowRemove.setOnClickListener(null);
						mBtnFollow.setVisibility(View.VISIBLE);
						mBtnFollow.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								if (!mIsLoading) {
									if (PreferencesUtil.getParkToken(getActivity()) != null) {
										if (aUser.isFollowingActiveUser()) {
											showProgress();
											mSpiceManager.execute(new UnfollowUserRequest(aUser.getUsername()),
													new FollowRequestListener(mBtnFollow, aUser));
										} else {
											showProgress();
											// From FB friends list
											if(!TextUtils.isEmpty(aUser.getLocation())) {
												trackFollowUser(true);
											} else {
												trackFollowUser(false);
											}
											mSpiceManager.execute(new FollowUserRequest(aUser.getUsername()),
													new FollowRequestListener(mBtnFollow, aUser));
										}
									} else {
										MessageUtil.showLoginMsg(getActivity(), UnloggedNavigations.CATEGORIES);
									}
								}
							}
						});

						setUpFollowBtn(mBtnFollow, aUser.isFollowingActiveUser());

					}
				}

			}

		}

	}

	private void trackFollowUser (boolean findFBFriends) {
		if (findFBFriends) {
			ParkApplication
					.getInstance()
					.getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER)
					.send(new HitBuilders.EventBuilder().setCategory("Follow an user on Find FB Friends")
							.setAction("Clicks on Follow an user on Find FB Friends").setLabel("The user follows an user on Find FB Friends")
							.build());
			ParkApplication.getInstance().getEventsLogger().logEvent(FacebookUtil.EVENT_FOLLOW_USER_FIND_FB_FRIENDS);
		} else {
			ParkApplication
					.getInstance()
					.getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER)
					.send(new HitBuilders.EventBuilder().setCategory("Follow an user on Profile")
							.setAction("Clicks on Follow an user on Profile").setLabel("The user follows an user on Profile")
							.build());
			ParkApplication.getInstance().getEventsLogger().logEvent(FacebookUtil.EVENT_FOLLOW_USER_PROFILE_REST);
		}
	}

	private void updateRemoved(View aView, FollowerModel aItem, boolean isSelected) {
		aView.setSelected(!isSelected);
		if (!isSelected) {
			mToBeRemoved.add(aItem);
		} else {
			if (mToBeRemoved.contains(aItem)) {
				mToBeRemoved.remove(aItem);
			}
		}
	}

	private class FollowRequestListener extends BaseNeckRequestListener<Boolean> {
		private FollowerModel mFollower;
		private Button mFollowButton;

		public FollowRequestListener(Button followBtn, FollowerModel follower) {
			this.mFollower = follower;
			this.mFollowButton = followBtn;
		}

		@Override
		public void onRequestError(Error error) {
			hideProgress();
			MessageUtil.showError(getBaseActivity(), error.getMessage(), getBaseActivity().getCroutonsHolder());
		}

		@Override
		public void onRequestSuccessfull(Boolean t) {
			hideProgress();
			mFollower.setFollowsUser(!mFollower.isFollowingActiveUser());
			setUpFollowBtn(mFollowButton, mFollower.isFollowingActiveUser());
		}

		@Override
		public void onRequestException(SpiceException exception) {
			hideProgress();
			MessageUtil.showError(getBaseActivity(), exception.getMessage(), getBaseActivity().getCroutonsHolder());
			Logger.error(exception.getMessage());
		}

	}

	private void setUpFollowBtn(Button btnFollow, boolean userFollowsProfile) {
		if (userFollowsProfile) {
			btnFollow.setActivated(true);
			btnFollow.setText(R.string.following);
		} else {
			btnFollow.setActivated(false);
			btnFollow.setText(R.string.follow);
		}
	}

	@Override
	protected void loadData(UserListResponse aResponse) {
		mIsLoading = false;
		if (!aResponse.getUsers().isEmpty()) {
			long fetchedItems = mAdapter.getCount() + aResponse.getUsers().size();
			mLoadedAllItems = aResponse.getAmountItemsFound() <= fetchedItems;
			mAdapter.merge(aResponse.getUsers());
		}
		if (mAdapter.isEmpty()) {
			mUserList.setEmptyView(mEmptyView);
			mEmptyMessage.setText(aResponse.getNoResultsMessage());
			mEmptyHint.setText(aResponse.getNoResultsHint());
		} else {
			mUserList.setEmptyView(null);
		}
		mAdapter.notifyDataSetChanged();
	}
}
