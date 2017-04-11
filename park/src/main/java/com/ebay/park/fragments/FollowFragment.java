package com.ebay.park.fragments;

import android.os.Bundle;

import com.ebay.park.R;
import com.ebay.park.base.BaseUserListFragment;
import com.ebay.park.requests.BaseParkSessionRequest;
import com.ebay.park.requests.FollowedRequest;
import com.ebay.park.requests.FollowersRequest;
import com.ebay.park.responses.UserListResponse;

/**
 * Followers/following users list screen.
 * 
 * @author federico.perez
 * 
 */
public class FollowFragment extends BaseUserListFragment {
	private static final String TYPE_KEY = "type";
	private static final String USER_KEY = "user";
	private static final int TYPE_FOLLOWERS = 1;
	private static final int TYPE_FOLLOWING = 2;

	private String mUsername;
	private int mListType;

	/**
	 * Get followers screen instance.
	 */
	public static FollowFragment getFollowersInstance(String username) {
		FollowFragment fragment = new FollowFragment();
		Bundle args = new Bundle();
		args.putString(USER_KEY, username);
		args.putInt(TYPE_KEY, TYPE_FOLLOWERS);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Get following screen instance.
	 */
	public static FollowFragment getFollowingInstance(String username) {
		FollowFragment fragment = new FollowFragment();
		Bundle args = new Bundle();
		args.putString(USER_KEY, username);
		args.putInt(TYPE_KEY, TYPE_FOLLOWING);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mListType == TYPE_FOLLOWERS) {
			setTitle(R.string.followersUp);
		} else {
			setTitle(R.string.followingUp);
		}
		onRefresh();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mUsername = getArguments().getString(USER_KEY);
		mListType = getArguments().getInt(TYPE_KEY);
	}

	@Override
	protected void getDataFromServer() {
		showProgress();
		BaseParkSessionRequest<UserListResponse> request;
		if (mListType == TYPE_FOLLOWERS) {
			request = new FollowersRequest(mUsername);
		} else {
			request = new FollowedRequest(mUsername);
		}
		mSpiceManager.execute(request, new UserListener());
	}

	@Override
	public void onBackPressed() {
	}

	private class UserListener extends ListListener {
	};
}