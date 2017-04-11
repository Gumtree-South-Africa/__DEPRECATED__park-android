package com.ebay.park.fragments;

import android.location.Location;

import com.ebay.park.activities.ParkActivity;
import com.ebay.park.base.BaseItemListFragment;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.model.ItemModel;
import com.ebay.park.model.OwnerModel;
import com.ebay.park.requests.ItemListRequest;
import com.ebay.park.utils.LocationUtil;
import com.ebay.park.utils.LocationUtil.LocationResolverCallback;

public abstract class MyLists extends BaseItemListFragment {

	@Override
	protected void doFollow(ItemModel aItem) {
		super.doFollow(aItem);
		try {
			((ParkActivity) getActivity()).notifyLiked();
		} catch (NullPointerException e) {
		} catch (ClassCastException e) {
		}
	}

	public static class MyItemWhishlist extends MyLists implements Whishlist {
		@Override
		protected void getDataFromServer() {
			showProgress();
			Location location = LocationUtil.getLocation(getBaseActivity());
			if (location != null) {
				new LocationUtil.LocationResolverTask(getBaseActivity(), location, new LocationResolverCallback() {
					@Override
					public void onLocationResolved(Location location) {
						startRequest(location);
					}
				}).execute();
			} else {
				startRequest(location);
			}
		}

		private void startRequest(Location location) {

			ItemListRequest.Builder builder = new ItemListRequest.Builder().page(page.longValue()).pageSize(
					((Integer) PAGE_SIZE).longValue());
			if (location != null) {
				builder.withPos(location.getLatitude(), location.getLongitude());
			}
			mSpiceManager.execute(builder.fromMyWishlist(true).build(), new ItemListListener());
		}

		@Override
		public void notifyLiked() {
			onRefresh();
		}

		@Override
		public void onBackPressed() {
		}
	}

	public static class MyFavoritePeopleItemList extends MyLists implements Whishlist {

		@Override
		protected void getDataFromServer() {
			showProgress();
			Location location = LocationUtil.getLocation(getBaseActivity());
			if (location != null) {
				new LocationUtil.LocationResolverTask(getBaseActivity(), location, new LocationResolverCallback() {
					@Override
					public void onLocationResolved(Location location) {
						startRequest(location);
					}
				}).execute();
			} else {
				startRequest(location);
			}
		}

		private void startRequest(Location location) {

			ItemListRequest.Builder builder = new ItemListRequest.Builder().page(page.longValue()).pageSize(
					((Integer) PAGE_SIZE).longValue());
			if (location != null) {
				builder.withPos(location.getLatitude(), location.getLongitude());
			}
			mSpiceManager.execute(builder.fromPeopleIfollow(true).build(), new ItemListListener());
		}

		@Override
		public void notifyLiked() {
			onRefresh();
		}

		@Override
		public void onBackPressed() {
		}
	}

	public static class MyGroupsItemList extends MyLists implements Whishlist {

		@Override
		protected void getDataFromServer() {

			showProgress();
			Location location = LocationUtil.getLocation(getBaseActivity());

			ItemListRequest.Builder builder = new ItemListRequest.Builder().page(page.longValue()).pageSize(
					((Integer) PAGE_SIZE).longValue());
			if (location != null) {
				builder.withPos(location.getLatitude(), location.getLongitude());
			}
			mSpiceManager.execute(builder.fromSubscribedGroups(true).build(), new ItemListListener());
		}

		@Override
		public void notifyLiked() {
			onRefresh();
		}

		@Override
		public void onBackPressed() {
		}
	}

	private class ItemListListener extends ListListener {
	}

	@Override
	protected void onItemClicked(ItemModel aItem, int pos) {
		if (aItem != null){
			if (aItem.getId() != null){
				ScreenManager.showItemDetailActivity(getBaseActivity(), aItem.getId(),
						null, null);
			}
		}
	}

	@Override
	protected void onUsenameClicked(OwnerModel aUser) {
		if (aUser!=null){
			if (aUser.getUsername()!=null){
				ScreenManager.showProfileActivity(getBaseActivity(), aUser.getUsername());
			}
		}
	}

	public interface Whishlist {

		public void notifyLiked();
	}

}
