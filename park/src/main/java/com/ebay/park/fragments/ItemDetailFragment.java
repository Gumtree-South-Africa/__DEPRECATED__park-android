package com.ebay.park.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ebay.park.ParkApplication;
import com.ebay.park.ParkApplication.UnloggedNavigations;
import com.ebay.park.R;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.flow.IntentFactory;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.interfaces.OnShowListenerLollipop;
import com.ebay.park.interfaces.OnShowListenerPreLollipop;
import com.ebay.park.interfaces.OnSwipeTouchListener;
import com.ebay.park.model.ItemModel;
import com.ebay.park.model.ItemsListParamsModel;
import com.ebay.park.model.PublishItemModel;
import com.ebay.park.requests.ItemDeleteRequest;
import com.ebay.park.requests.ItemDetailRequest;
import com.ebay.park.requests.ItemFollowRequest;
import com.ebay.park.requests.ItemIdsListRequest;
import com.ebay.park.requests.ItemRepublishRequest;
import com.ebay.park.requests.ItemSoldRequest;
import com.ebay.park.requests.ItemUnFollowRequest;
import com.ebay.park.requests.ReportItemRequest;
import com.ebay.park.responses.ItemIdsListResponse;
import com.ebay.park.responses.ItemRepublishResponse;
import com.ebay.park.responses.ResponseCodes;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.DialogUtils;
import com.ebay.park.utils.FacebookUtil;
import com.ebay.park.utils.FormatUtils;
import com.ebay.park.utils.GroupsLabelView;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.PreferencesUtil;
import com.ebay.park.utils.SwrveEvents;
import com.ebay.park.views.StickyScrollView;
import com.github.clans.fab.FloatingActionButton;
import com.globant.roboneck.requests.BaseNeckRequestException;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.swrve.sdk.SwrveSDK;
import com.viewpagerindicator.CirclePageIndicator;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shows item detail screen.
 *
 * @author federico.perez
 * @author Nicol�s Mat�as Fern�ndez
 */
public class ItemDetailFragment extends BaseFragment implements OnMapReadyCallback {

	public static final String ID_PARAM = "id";
	public static final String ITEMS_LIST_PARAMS_PARAM = "items_list_params";
	public static final String ITEM_IDS_LIST = "items_ids_list";
	public static final String TEST_TAG = ItemDetailFragment.class.getSimpleName();
	private static final String STATE_SOLD = "SOLD";
	private static final String STATE_EXPIRED = "EXPIRED";
	private static final String STATE_PENDING = "PENDING";
	private static final String STATE_ACTIVE = "ACTIVE";
	private static final long NO_ID = -123L;
	private static final int NO_LIKES = 0;

	private static final int MAX_GROUPS_SHOWN = 5;

	private View mLayout;

	private ViewPager mItemImages;
	private CirclePageIndicator mItemImagesIndicator;

	private ImageView mOwnerImage;
	private View mItemDelete;
	private TextView mItemState;
	private View mItemEdit;
	private TextView mLikeCount;
	private TextView mItemStatus;
	private TextView mItemName;
	private GroupsLabelView mItemGroups;
	private TextView mItemDescription;
	private TextView mOwnerUsername;
	private TextView mOwnerLocation;
	private TextView mItemReport;
	private TextView mItemLocation;
	private TextView mPostTime;
	private Tracker mGAnalyticsTracker;
	private Animation mAnimZoomin;
	private Animation mAnimZoomout;
	private AnimationSet mAnimSet;
	private RelativeLayout mItemActionsLayout;
	protected FloatingActionButton mFabNegotiate;
	private ImageView mItemStatusImage;
	private GoogleMap mMap;
	private boolean mFlagLikeIcon = false;
	private boolean mFlagCanLike = false;
	private boolean mFlagCanShare = false;
	private boolean mFlagLikeVisible = false;
	private boolean mFlagShareVisible = false;

	private static final float MAP_ZOOM = 12;
	private static final int MAP_ANIMATION_DURATION = 2000;

	private ItemModel mItem;
	private long itemId;
	private ItemsListParamsModel itemsListParams;
	private boolean nextItem;

	public static LinkedHashMap<String, Integer> sChkBoxList;
	private TextView mListAllGroups;
	private Boolean mItemDeleted = false;
	private static View sView;
	private int mOldScrollY;
	private ArrayList<Long> itemIds = null;
	private Boolean mViewPagerDragging = false;

	/**
	 * Get the item detail for a given item id.
	 *
	 * @param id
	 *            The id of the item.
	 */
	public static ItemDetailFragment forItem(long id, ItemsListParamsModel itemsParams, ArrayList<Long> itemIds) {
		ItemDetailFragment fragment = new ItemDetailFragment();
		Bundle args = new Bundle();
		args.putLong(ID_PARAM, id);
		args.putSerializable(ITEMS_LIST_PARAMS_PARAM, itemsParams);
		args.putSerializable(ITEM_IDS_LIST, itemIds);
		fragment.setArguments(args);
		return fragment;
	}

	private void negotiationHandler() {
		if (PreferencesUtil.getParkToken(getActivity()) != null) {
			// Build and send "Clicks on Negotiation" Tracking Event.
			mGAnalyticsTracker.send(new HitBuilders.EventBuilder().setCategory("Clicks on Negotiation")
					.setAction("Clicks on Negotiation").setLabel("The user entered to negociate").build());
			if (isActiveUserOwner()) {
				mFabNegotiate.setVisibility(View.GONE);
				ScreenManager.showConversationListForItem(getBaseActivity(), itemId);
			} else if (this.mItem.hasConversations() || STATE_ACTIVE.equals(this.mItem.getStatus())) {
				mFabNegotiate.setVisibility(View.GONE);
				ScreenManager.showNewChatFragment(getBaseActivity(), itemId);
			} else {
				new AlertDialog.Builder(getBaseActivity()).setTitle(getString(R.string.item_negociate_unable_title))
						.setMessage(getString(R.string.item_negociate_unable_message))
						.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
							}

						}).show();
			}
		} else {
			MessageUtil.showLoginMsg(getActivity(), UnloggedNavigations.MAKE_OFFER);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if (getArguments() == null || !getArguments().containsKey(ID_PARAM)) {
			throw new IllegalStateException("Item detail fragment called with no id args.");
		}
		itemId = getArguments().getLong(ID_PARAM, NO_ID);
		itemsListParams = (ItemsListParamsModel) getArguments().getSerializable(ITEMS_LIST_PARAMS_PARAM);
		itemIds = (ArrayList<Long>) getArguments().getSerializable(ITEM_IDS_LIST);
		setUpAnimations();
	}

	private void startItemsIdsRequest() {

		ItemIdsListRequest.Builder builder = new ItemIdsListRequest.Builder().query("").page(itemsListParams.getPage()+1)
				.pageSize(((Integer) 24).longValue()).categoryId(itemsListParams.getCategoryId()).priceFrom(null)
				.priceTo(null).order("published").forGroup(-123L).forUser("")
				.maxDistance("20");

		mSpiceManager.execute(builder.build(), new ItemIdsListListener());
	}

	private class ItemIdsListListener extends BaseNeckRequestListener<ItemIdsListResponse> {
		@Override
		public void onRequestError(BaseNeckRequestException.Error error) {
		}

		@Override
		public void onRequestSuccessfull(ItemIdsListResponse aResponse) {
			long id = itemIds.get(itemsListParams.getCurrentPos());//test it is equal to itemId

			if (aResponse.getItemsIds().size() != 0) {
				itemIds.addAll(aResponse.getItemsIds());
				itemsListParams.setPage(itemsListParams.getPage() + 1);

				showNextItemVip();
			}
		}

		@Override
		public void onRequestException(SpiceException exception) {
		}
	}

	private void showNextItemVip() {
		if(nextItem){
			itemsListParams.setCurrentPos(itemsListParams.getCurrentPos()+1);
			ScreenManager.showItemDetailActivity(getBaseActivity(), itemIds.get(itemsListParams.getCurrentPos()),
					itemsListParams, itemIds);
			getBaseActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
		} else {
			itemsListParams.setCurrentPos(itemsListParams.getCurrentPos()-1);
			ScreenManager.showItemDetailActivity(getBaseActivity(), itemIds.get(itemsListParams.getCurrentPos()),
					itemsListParams, itemIds);
			getBaseActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		}

		getBaseActivity().finish();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		try {
			sView = inflater.inflate(R.layout.fragment_item_detail,
					(ViewGroup) super.onCreateView(inflater, container, savedInstanceState), true);
			hideProgress();
		} catch (InflateException e) {
			// map is already there, just return view as it is
		}
		return sView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_item_detail, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		if (menu.findItem(R.id.like) != null) {
			if(mFlagLikeIcon){
				menu.findItem(R.id.like).setIcon(R.drawable.icon_white_like_fill_115);
			} else {
				menu.findItem(R.id.like).setIcon(R.drawable.icon_white_like_stroke_115);
			}
			menu.findItem(R.id.like).setVisible(mFlagLikeVisible);
		}

		if (menu.findItem(R.id.like) != null) {
			menu.findItem(R.id.like).setEnabled(mFlagCanLike);
		}

		if (menu.findItem(R.id.share) != null) {
			menu.findItem(R.id.share).setEnabled(mFlagCanShare);
			menu.findItem(R.id.share).setVisible(mFlagShareVisible);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem aItem) {
		switch (aItem.getItemId()) {
			case R.id.share:
				if (mItemDeleted){
					MessageUtil.showError(getBaseActivity(), getString(R.string.item_deleted),
							getBaseActivity().getCroutonsHolder());
				} else {
					startActivity(IntentFactory.getShareItemIntent(getBaseActivity(), mItem.getUrl()));
				}
				break;
			case R.id.like:
				if (PreferencesUtil.getParkToken(getActivity()) != null) {
					if (mItemDeleted){
						MessageUtil.showError(getBaseActivity(), getString(R.string.item_deleted),
								getBaseActivity().getCroutonsHolder());
					} else {
						showProgress();
						mLikeCount.startAnimation(mAnimSet);
						setCanLike(false);
						setMenuItemLikeIcon(!mItem.isFollowedByUser());
						if (mItem.isFollowedByUser()) {
							SwrveSDK.event(SwrveEvents.WATCHLIST_REMOVE_ATTEMPT);
							mSpiceManager.execute(new ItemUnFollowRequest(mItem.getId()), new UnfollowListener());
						} else {
							SwrveSDK.event(SwrveEvents.WATCHLIST_ADD_ATTEMPT);
							Map<String, String> attributes = new HashMap<>();
							attributes.put(SwrveEvents.FAVORITE_ITEM, mItem.getCategory().getName());
							attributes.put(SwrveEvents.FAVORITE_ITEM + "." + mItem.getCategory().getName()
									.replace(" ", "-"), mItem.getName());
							SwrveSDK.userUpdate(attributes);
							mSpiceManager.execute(new ItemFollowRequest(mItem.getId()), new FollowListener());
						}
					}
				} else {
					MessageUtil.showLoginMsg(getActivity(), UnloggedNavigations.CATEGORIES);
				}
				break;
			default:
				break;
		}
		return super.onOptionsItemSelected(aItem);
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {

		if(mMap == null && googleMap != null) {
			mMap = googleMap;
			UiSettings uiSettings = googleMap.getUiSettings();
			uiSettings.setAllGesturesEnabled(false);
		}

		if (mItem != null) {
			if (mItem.getLatitude() != 0 && mItem.getLongitude() != 0) {
				LatLng marker = new LatLng(mItem.getLatitude(), mItem.getLongitude());
				CameraPosition cameraPosition = CameraPosition.builder().target(marker).zoom(MAP_ZOOM).build();
				googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), MAP_ANIMATION_DURATION, null);
				googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
					@Override
					public void onMapClick(LatLng point) {
						if (mItemDeleted) {
							MessageUtil.showError(getBaseActivity(), getString(R.string.item_deleted),
									getBaseActivity().getCroutonsHolder());
						} else {
							try {
								startActivity(IntentFactory.getMapIntent(getBaseActivity(), mItem.getLatitude(), mItem.getLongitude(), mItem.getLocationName()));
							} catch (Exception e) {
							}
						}
					}
				});
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mMap != null){
			mMap.setMyLocationEnabled(false);
		}
	}

	protected void showNegotiateOffersButton(boolean isOwner) {
		if(isOwner) {
			mFabNegotiate.setImageDrawable(getResources().getDrawable(R.drawable.floating_action_button_negotiation_selector));
		} else {
			mFabNegotiate.setImageDrawable(getResources().getDrawable(R.drawable.floating_action_button_offers_selector));
		}
		mFabNegotiate.setVisibility(View.VISIBLE);
	}

	private void setUpFab(View aView) {
		mFabNegotiate = (FloatingActionButton) aView.findViewById(R.id.fab_publish);
		final StickyScrollView scrollView = (StickyScrollView) aView.findViewById(R.id.item_detail_scroll);
		mFabNegotiate.setEnabled(false);
		if (mFabNegotiate != null) {
			mFabNegotiate.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mItemDeleted) {
						MessageUtil.showError(getBaseActivity(), getString(R.string.item_deleted),
								getBaseActivity().getCroutonsHolder());
					} else {
						negotiationHandler();
					}
				}
			});
			scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
				@Override
				public void onScrollChanged() {
					if (mFabNegotiate.getVisibility() != View.GONE) {
						int scrollY = scrollView.getScrollY();
						if (scrollY > mOldScrollY+18) {
							mFabNegotiate.hide(true);
						} else if (scrollY < mOldScrollY-18) {
							mFabNegotiate.show(true);
						}

						mOldScrollY = scrollY;
					}
				}
			});
		}
	}

	@Override
	public void onViewCreated(View rootView, Bundle savedInstanceState) {
		super.onViewCreated(rootView, savedInstanceState);
		mLayout = rootView.findViewById(R.id.item_detail_layout);
		mLayout.setVisibility(View.INVISIBLE);

		mItemImages = (ViewPager) rootView.findViewById(R.id.vp_item_detail_images);
		mItemImages.setPageTransformer(true, new ZoomPageTransformer());
		mItemImagesIndicator = (CirclePageIndicator) rootView.findViewById(R.id.cp_item_detail_image_indicator);
		mItemStatus = (TextView) rootView.findViewById(R.id.tv_item_detail_status);

		mItemEdit = rootView.findViewById(R.id.iv_item_edit);
		mItemState = (TextView) rootView.findViewById(R.id.iv_item_sold);
		mItemDelete = rootView.findViewById(R.id.iv_item_delete);

		mLikeCount = (TextView) rootView.findViewById(R.id.tv_item_detail_likes);
		mItemName = (TextView) rootView.findViewById(R.id.tv_item_detail_title);

		mOwnerImage = (ImageView) rootView.findViewById(R.id.iv_item_detail_owner_image);
		mOwnerUsername = (TextView) rootView.findViewById(R.id.tv_item_detail_owner_username);
		mOwnerLocation = (TextView) rootView.findViewById(R.id.tv_item_detail_owner_location);
		mPostTime = (TextView) rootView.findViewById(R.id.tv_item_detail_post_time);

		mItemGroups = (GroupsLabelView) rootView.findViewById(R.id.tv_item_detail_groups);
		mListAllGroups = (TextView) rootView.findViewById(R.id.tv_item_detail_groups_more);
		mItemDescription = (TextView) rootView.findViewById(R.id.tv_item_detail_description);
		mItemLocation = (TextView) rootView.findViewById(R.id.item_detail_location);
		mItemReport = (TextView) rootView.findViewById(R.id.tv_item_detail_report);

		mItemStatusImage = (ImageView) rootView.findViewById(R.id.iv_status_item);

		mItemActionsLayout = (RelativeLayout) rootView.findViewById(R.id.item_detail_sticky_actions);

		mItemEdit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mItemDeleted){
					MessageUtil.showError(getBaseActivity(), getString(R.string.item_deleted),
							getBaseActivity().getCroutonsHolder());
				} else {
					getBaseActivity().finish();
					ScreenManager.showPublishActivity(getBaseActivity(), itemId);
				}
			}
		});

		mItemDelete.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mItemDeleted){
					MessageUtil.showError(getBaseActivity(), getString(R.string.item_deleted),
							getBaseActivity().getCroutonsHolder());
				} else {
					SwrveSDK.event(SwrveEvents.DELETE_AD_BEGIN);
					showConfirmDeleteDialog();
				}
			}
		});

		mItemState.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mItemDeleted){
					MessageUtil.showError(getBaseActivity(), getString(R.string.item_deleted),
							getBaseActivity().getCroutonsHolder());
				} else {
					if (STATE_ACTIVE.equals(mItem.getStatus())) {
						showConfirmSellDialog();
					} else if (STATE_SOLD.equals(mItem.getStatus()) || STATE_EXPIRED.equals(mItem.getStatus())) {
						SwrveSDK.event(SwrveEvents.REPOST_AD_BEGIN);
						showConfirmRepublishDialog();
					}
				}
			}
		});

		mOwnerImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mItem !=null){
					if (mItem.getUser()!= null){
						if (mItem.getUser().getUsername()!=null){
							ScreenManager.showProfileActivity(getBaseActivity(), mItem.getUser().getUsername());
						}
					}
				}
			}
		});

		mOwnerUsername.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mItem !=null){
					if (mItem.getUser()!= null){
						if (mItem.getUser().getUsername()!=null){
							ScreenManager.showProfileActivity(getBaseActivity(), mItem.getUser().getUsername());
						}
					}
				}
			}
		});

		mItemGroups.setBackgroundDrawable(null);

		mListAllGroups.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mItemDeleted) {
					MessageUtil.showError(getBaseActivity(), getString(R.string.item_deleted),
							getBaseActivity().getCroutonsHolder());
				} else {
					mFabNegotiate.setVisibility(View.GONE);
					ScreenManager.showItemGroupsFragment(getBaseActivity(), mItem.getId());
				}
			}
		});

		SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		mItemReport.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mItemDeleted){
					MessageUtil.showError(getBaseActivity(), getString(R.string.item_deleted),
							getBaseActivity().getCroutonsHolder());
				} else {
					if (PreferencesUtil.getParkToken(getActivity()) != null) {
						showProgress();
						mSpiceManager.execute(new ReportItemRequest(mItem.getId()), new ItemReportListener());
					} else {
						MessageUtil.showLoginMsg(getActivity(), UnloggedNavigations.CATEGORIES);
					}
				}
			}
		});

		setUpFab(rootView);

		//Set listener only when it comes from "Comprar" screen
		if (itemIds != null) {
			mLayout.setOnTouchListener(itemGestureListener);
			mItemGroups.setOnTouchListener(itemGestureListener);

			View mapView = rootView.findViewById(R.id.swipe_map);
			mapView.setOnTouchListener(itemMapGestureListener);
		}
	}

	private ViewPager.OnPageChangeListener imagePageChangeListener = new ViewPager.OnPageChangeListener(){

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			if (mViewPagerDragging && positionOffset == 0.0 && positionOffsetPixels == 0){
				//When user tries to scroll out of bounds
				if (position == 0){
					nextItem = false; // GO TO THE PREVIOUS ITEM
					if (itemsListParams.getCurrentPos() != 0){
						showNextItemVip();
					}
					mViewPagerDragging = false;
				} else if (position == (mItemImages.getAdapter().getCount()-1)) {
					nextItem = true; // GO TO THE NEXT ITEM
					if (itemsListParams.getCurrentPos() == (itemIds.size()-1)) {
						startItemsIdsRequest();
					} else {
						showNextItemVip();
					}
					mViewPagerDragging = false;
				}
			}
		}

		@Override
		public void onPageSelected(int position) {
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			//User is swiping
			mViewPagerDragging = (state == ViewPager.SCROLL_STATE_DRAGGING);
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		disableRefreshSwipe();
		mGAnalyticsTracker = ParkApplication.getInstance().getTracker(ParkApplication.TrackerName.GLOBAL_TRACKER);
	}

	@Override
	public void onResume() {
		super.onResume();
		setTitle(R.string.details);
		showProgress();
		onRefresh();
		if (ParkApplication.sJustLoggedSecondLevel) {
			navigationFlow();
		}
	}

	private void navigationFlow() {

		ParkApplication.sJustLoggedSecondLevel = false;

		if (ParkApplication.sFgmtOrAct_toGo != null) {
			switch (ParkApplication.sFgmtOrAct_toGo) {
				case MAKE_OFFER:
					negotiationHandler();
					break;
				default:
					break;
			}
		}
	}

	@Override
	public void onRefresh() {
		getActivity().supportInvalidateOptionsMenu();
		mSpiceManager.execute(new ItemDetailRequest(itemId), new ItemDetailListener());
	}

	private void setUpAnimations() {
		mAnimZoomin = AnimationUtils.loadAnimation(getBaseActivity(), R.anim.zoom_in);
		mAnimZoomout = AnimationUtils.loadAnimation(getBaseActivity(), R.anim.zoom_out);
		mAnimSet = new AnimationSet(true);
		mAnimSet.addAnimation(mAnimZoomin);
		mAnimSet.addAnimation(mAnimZoomout);
	}

	private boolean isActiveUserOwner() {
		if (mItem != null && mItem.getUser() != null && !TextUtils.isEmpty(ParkApplication.getInstance().getUsername())) {
			return ParkApplication.getInstance().getUsername().equals(mItem.getUser().getUsername());
		} else {
			return false;
		}
	}

	private void updateStatus(ItemModel aItem) {
		switch (aItem.getStatus().toUpperCase()) {
			case STATE_ACTIVE:
				updateStatus(FormatUtils.formatPrice(aItem.getPrice()).replace("$",""), R.string.item_set_as_sold);
				mItemStatusImage.setVisibility(View.GONE);
				break;
			case STATE_PENDING:
				updateStatus(FormatUtils.formatPrice(aItem.getPrice()).replace("$", ""), R.string.item_set_as_sold);
				Picasso.with(getBaseActivity()).load(R.drawable.flag_pending_big).into(mItemStatusImage);
				mItemStatusImage.setVisibility(View.VISIBLE);
				break;
			case STATE_EXPIRED:
				updateStatus(FormatUtils.formatPrice(aItem.getPrice()).replace("$", ""), R.string.republish_item_confirm_title);
				Picasso.with(getBaseActivity()).load(R.drawable.flag_expired_big).into(mItemStatusImage);
				mItemStatusImage.setVisibility(View.VISIBLE);
				break;
			case STATE_SOLD:
				updateStatus(FormatUtils.formatPrice(aItem.getPrice()).replace("$", ""), R.string.republish_item_confirm_title);
				Picasso.with(getBaseActivity()).load(R.drawable.flag_sold_big).into(mItemStatusImage);
				mItemStatusImage.setVisibility(View.VISIBLE);
				break;
			default:
				updateStatus(FormatUtils.formatPrice(aItem.getPrice()).replace("$", ""), R.string.item_set_as_sold);
		}
		mItemEdit.setEnabled(STATE_ACTIVE.equals(mItem.getStatus()) || STATE_PENDING.equals(mItem.getStatus())
				|| STATE_EXPIRED.equals(mItem.getStatus()));
		mItemState.setEnabled(STATE_ACTIVE.equals(aItem.getStatus()) || STATE_SOLD.equals(mItem.getStatus())
				|| STATE_EXPIRED.equals(mItem.getStatus()));

	}

	private void updateStatus(String aStatus, int aState) {
		mItemStatus.setText(aStatus);
		mItemState.setText(aState);
	}

	private void deleteItem(){
		showProgress();
		SwrveSDK.event(SwrveEvents.DELETE_AD_ATTEMPT);
		mSpiceManager.execute(new ItemDeleteRequest(itemId), new DeleteItemListener());
	}

	private void showConfirmDeleteDialog() {
		final AlertDialog dialog = DialogUtils.getDialogWithLabel(getBaseActivity(), R.string.delete,
				R.string.delete_item_confirm)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteItem();
					}
				})
				.setNegativeButton(R.string.no, null).create();

		if (DeviceUtils.isDeviceLollipopOrHigher()){
			dialog.setOnShowListener(new OnShowListenerLollipop(dialog, getBaseActivity()));
		} else {
			dialog.setOnShowListener(new OnShowListenerPreLollipop(dialog, getBaseActivity()));
		}

		dialog.show();
	}

	protected void showConfirmRepublishDialog() {
		final AlertDialog dialog = DialogUtils.getDialogWithLabel(getBaseActivity(),
				R.string.republish_item_confirm_title,
				R.string.republish_item_confirm_message, R.drawable.sold_item)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						showProgress();
						SwrveSDK.event(SwrveEvents.REPOST_AD_ATTEMPT);
						mSpiceManager.execute(new ItemRepublishRequest(mItem.getId()), new ItemRepublishListener());
					}
				})
				.setNegativeButton(R.string.no, null).create();

		if (DeviceUtils.isDeviceLollipopOrHigher()){
			dialog.setOnShowListener(new OnShowListenerLollipop(dialog, getBaseActivity()));
		} else {
			dialog.setOnShowListener(new OnShowListenerPreLollipop(dialog, getBaseActivity()));
		}

		dialog.show();
	}

	private void showConfirmSellDialog() {
		final AlertDialog dialog = DialogUtils.getDialogWithLabel(getBaseActivity(), R.string.item_set_as_sold,
				R.string.sell_item_confirm_message)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						showProgress();
						mSpiceManager.execute(new ItemSoldRequest(mItem.getId()), new MarkAsSoldListener());
					}
				})
				.setNegativeButton(R.string.no, null).create();

		if (DeviceUtils.isDeviceLollipopOrHigher()){
			dialog.setOnShowListener(new OnShowListenerLollipop(dialog, getBaseActivity()));
		} else {
			dialog.setOnShowListener(new OnShowListenerPreLollipop(dialog, getBaseActivity()));
		}

		dialog.show();
	}

	private void showError(String message) {
		MessageUtil.showError(getBaseActivity(), message, getBaseActivity().getCroutonsHolder());
	}

	@Override
	public void onBackPressed() {
	}

	private class DeleteItemListener extends BaseNeckRequestListener<PublishItemModel> {

		@Override
		public void onRequestError(Error error) {
			Logger.verb("onRequestError");
			Map<String,String> payload = new HashMap<>();
			payload.put(SwrveEvents.EVENT_FAIL_KEY, error.getMessage());
			SwrveSDK.event(SwrveEvents.DELETE_AD_FAIL, payload);
			if (error.getErrorCode() == ResponseCodes.UpdateItemPictures.ITEM_NOT_FOUND) {
				MessageUtil.showError(getBaseActivity(),
						getString(R.string.item_negociate_unable_message),
						getBaseActivity().getCroutonsHolder());
			} else {
				MessageUtil.showError(getBaseActivity(), error.getMessage(),
						getBaseActivity().getCroutonsHolder());
			}
			hideProgress();
		}

		@Override
		public void onRequestSuccessfull(PublishItemModel model) {
			SwrveSDK.event(SwrveEvents.DELETE_AD_SUCCESS);
			hideProgress();
			ParkApplication.getInstance().getEventsLogger()
					.logEvent(FacebookUtil.EVENT_ITEM_DELETED_BY_OWNER);
			showError(getString(R.string.delete_successfully));
			getBaseActivity().finish();
		}

		@Override
		public void onRequestException(SpiceException exception) {
			Map<String,String> payload = new HashMap<>();
			payload.put(SwrveEvents.EVENT_FAIL_KEY, exception.getMessage());
			SwrveSDK.event(SwrveEvents.DELETE_AD_FAIL, payload);
			hideProgress();
			showError(exception.getMessage());
			Logger.error(exception.getMessage());
		}

	}

	private class ItemDetailListener extends BaseNeckRequestListener<ItemModel> {

		@Override
		public void onRequestError(Error error) {
			hideProgress();
			// Show item from list info
			if (error.getErrorCode() == ResponseCodes.UpdateItemPictures.ITEM_NOT_FOUND) {
				if (ParkApplication.sItemTapped != null) {
					ItemDetailFragment.this.mItem = ParkApplication.sItemTapped;
					ParkApplication.sItemTapped = null;
					preparePreloadedItem();
					showItem();
					mItemDeleted = true;
				}
			}
			MessageUtil.showError(getBaseActivity(), error.getMessage(), getBaseActivity().getCroutonsHolder());
			Logger.error(error.getMessage());
		}

		@Override
		public void onRequestSuccessfull(ItemModel item) {
			ItemDetailFragment.this.mItem = item;
			showItem();
		}

		@Override
		public void onRequestException(SpiceException exception) {
			hideProgress();
			// Show item from list info
			if (ParkApplication.sItemTapped != null){
				ItemDetailFragment.this.mItem = ParkApplication.sItemTapped;
				ParkApplication.sItemTapped = null;
				preparePreloadedItem();
				showItem();
			}
			MessageUtil.showError(getBaseActivity(), exception.getMessage(), getBaseActivity().getCroutonsHolder());
			Logger.error(exception.getMessage());
		}

	}

	private void preparePreloadedItem(){
		// pictures list is null in the list
		if (mItem.getPictureUrl() == null){ // Item without picture
			mItem.setPictures(new ArrayList<String>());
		} else { // Add the principal picture to the preloaded pictures array
			List<String> pictures = new ArrayList<>();
			pictures.add(mItem.getPictureUrl());
			mItem.setPictures(pictures);
		}

		// Location name is null in the list
		// Set owner's location to the preloaded item
		mItem.setLocationName(mItem.getUser().getLocationName());
	}

	private void showItem(){
		if (getActivity() != null) {
			getActivity().supportInvalidateOptionsMenu();
		}

		mLayout.setVisibility(View.VISIBLE);

		mItemImages.setAdapter(new ImageAdapter(mItem.getPictures()));
		mItemImagesIndicator.setViewPager(mItemImages);
		if (mItem.getPictures().size() <= 1) {
			mItemImagesIndicator.setVisibility(View.GONE);
		}

		//Only set the proper listener is the VIP comes from "Comprar" screen
		if (itemIds != null){
			if (mItem.getPictures().size() == 1){
				mItemImages.setOnTouchListener(itemImageGestureListener);
			} else {
				mItemImages.addOnPageChangeListener(imagePageChangeListener);
			}
		}

		updateStatus(mItem);

		if (mItem.getPrice().equals(0D)){ // Comes from a list with incomplete info
			mItemStatus.setVisibility(View.GONE);
			// When it comes from a list with incomplete info, comes with the date formated
			mPostTime.setText(mItem.getPublishedTime());
		} else {
			mPostTime.setText(FormatUtils.timeAgo(mItem.getPublishedTime()));
		}

		if (isActiveUserOwner()) {
			showNegotiateOffersButton(true);
			mItemActionsLayout.setVisibility(View.VISIBLE);
			mItemEdit.setVisibility(View.VISIBLE);
			mItemDelete.setVisibility(View.VISIBLE);
			mItemState.setVisibility(View.VISIBLE);
		} else {
			showNegotiateOffersButton(false);
			mItemActionsLayout.setVisibility(View.GONE);
			mItemEdit.setVisibility(View.GONE);
			mItemDelete.setVisibility(View.GONE);
			mItemState.setVisibility(View.GONE);
		}
		getActivity().supportInvalidateOptionsMenu();

		setCanLike(STATE_ACTIVE.equals(mItem.getStatus()));
		mLikeCount.setVisibility((mItem.getTotalOfFollowers() > NO_LIKES) ? View.VISIBLE : View.INVISIBLE);
		mLikeCount.setText(String.valueOf(mItem.getTotalOfFollowers()));
		setMenuItemLikeIcon(mItem.isFollowedByUser());
		setLikeVisible(true);
		setShareVisible(true);

		mItemName.setText(mItem.getName());

		setCanShare(STATE_ACTIVE.equals(mItem.getStatus()));

		if (TextUtils.isEmpty(mItem.getUser().getPictureUrl())) {
			mItem.getUser().setPictureUrl(null);
		}
		Picasso.with(getBaseActivity()).load(mItem.getUser().getPictureUrl()).placeholder(R.drawable.avatar_big_ph_image_fit_orange)
				.fit().centerCrop().into(mOwnerImage);


		mOwnerUsername.setText(mItem.getUser().getUsername());
		if (mItem.getUser().getLocationName().trim().endsWith(","))
			mOwnerLocation.setText(mItem.getUser().getLocationName().trim()
					.substring(0, mItem.getUser().getLocationName().trim().length() - 1));
		else
			mOwnerLocation.setText(mItem.getUser().getLocationName());

		if (mItem.getGroups() != null) {
			if (mItem.getGroups().size() > 0) {
				sChkBoxList = new LinkedHashMap<String, Integer>();
				for (int i = 0; i < mItem.getGroups().size(); i++) {
					sChkBoxList.put(mItem.getGroups().get(i).getName(), mItem.getGroups().get(i).getId());
				}
				fillGroupsBox();
			} else {
				sChkBoxList = null;
				fillGroupsBox();
			}
		}
		mListAllGroups.setVisibility((mItem.getGroups() != null && mItem.getGroups().size() > 0) ? View.VISIBLE : View.GONE);

		if (!TextUtils.isEmpty(mItem.getDescription())) {
			mItemDescription.setVisibility(View.VISIBLE);
			mItemDescription.setText(mItem.getDescription());
		} else {
			mItemDescription.setVisibility(View.GONE);
		}

		mItemLocation.setText(mItem.getLocationName());
		if (mItem.getLatitude() != 0 && mItem.getLongitude() != 0) {
			onMapReady(mMap);
		}
		if (mItem.isReported()) {
			mItemReport.setText(getString(R.string.item_reported));
		}
		mItemReport.setSelected(mItem.isReported());
		mItemReport.setEnabled(STATE_ACTIVE.equals(mItem.getStatus()) && !isActiveUserOwner());
	}

	private void fillGroupsBox() {
		if (mItemGroups != null) {
			if (sChkBoxList != null) {
				if (sChkBoxList.size() > 0) {
					ArrayList<String> selectedGroups = new ArrayList<String>(sChkBoxList.keySet());
					for (int i = 0; i < Math.min(selectedGroups.size(), MAX_GROUPS_SHOWN); i++) {
						mItemGroups.setText("");
						mItemGroups.addObject(selectedGroups.get(i));
					}
				} else {
					mItemGroups.setText("");
				}
			} else {
				mItemGroups.setText("");
				mItemGroups.addObject(getString(R.string.no_group));
			}
		}
	}

	private class ImageAdapter extends PagerAdapter {

		private List<String> mUrls;

		public ImageAdapter(List<String> pictures) {
			this.mUrls = pictures;
		}

		@Override
		public int getCount() {
			return mUrls.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((ImageView) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			ImageView imgView = new ImageView(getBaseActivity());
			imgView.setPadding(0, 0, 0, 0);

			Picasso.with(getBaseActivity()).load(mUrls.get(position)).placeholder(R.drawable.img_placeholder).fit()
					.centerCrop().into(imgView, new Callback() {
				@Override
				public void onSuccess() {
					hideProgress();
					mFabNegotiate.setEnabled(true);
				}

				@Override
				public void onError() {
					hideProgress();
					mFabNegotiate.setEnabled(true);
				}
			});
			((ViewPager) container).addView(imgView, 0);
			return imgView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((ImageView) object);
		}

	}

	private class ZoomPageTransformer implements ViewPager.PageTransformer {
		private static final float MIN_SCALE = 0.85f;
		private static final float MIN_ALPHA = 0.5f;

		public void transformPage(View view, float position) {
			int pageWidth = view.getWidth();
			int pageHeight = view.getHeight();

			if (position < -1) {
				view.setAlpha(0);

			} else if (position <= 1) {
				float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
				float vertMargin = pageHeight * (1 - scaleFactor) / 2;
				float horzMargin = pageWidth * (1 - scaleFactor) / 2;
				if (position < 0) {
					view.setTranslationX(horzMargin - vertMargin / 2);
				} else {
					view.setTranslationX(-horzMargin + vertMargin / 2);
				}

				view.setScaleX(scaleFactor);
				view.setScaleY(scaleFactor);

				view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));

			} else {
				view.setAlpha(0);
			}
		}
	}

	private class ItemReportListener extends BaseNeckRequestListener<Boolean> {

		@Override
		public void onRequestError(Error error) {
			hideProgress();
			showMessage(error.getMessage());
		}

		@Override
		public void onRequestSuccessfull(Boolean success) {
			hideProgress();
			if (success) {
				MessageUtil.showSuccess(getBaseActivity(), getString(R.string.item_reported),
						getBaseActivity().getCroutonsHolder());
				mItemReport.setText(getString(R.string.item_reported));
				mItemReport.setSelected(success);
			}
		}

		@Override
		public void onRequestException(SpiceException exception) {
			hideProgress();
			Logger.error(exception.getMessage());
			showMessage(getString(R.string.error_reporting_item));
		}

		private void showMessage(String message) {
			MessageUtil.showError(getBaseActivity(), message,
					getBaseActivity().getCroutonsHolder());
		}
	}

	private class FollowListener extends BaseNeckRequestListener<Boolean> {

		@Override
		public void onRequestError(Error error) {
			Map<String,String> payload = new HashMap<>();
			payload.put(SwrveEvents.EVENT_FAIL_KEY, error.getMessage());
			SwrveSDK.event(SwrveEvents.WATCHLIST_ADD_FAIL, payload);
			hideProgress();
			setCanLike(true);
			setMenuItemLikeIcon(false);
			MessageUtil.showError(getBaseActivity(), error.getMessage(), getBaseActivity().getCroutonsHolder());
		}

		@Override
		public void onRequestSuccessfull(Boolean success) {
			SwrveSDK.event(SwrveEvents.WATCHLIST_ADD_SUCCESS);
			hideProgress();
			if (success) {
				int incFollows = NumberUtils.toInt(mLikeCount.getText().toString()) + 1;
				mLikeCount.setVisibility((incFollows > NO_LIKES) ? View.VISIBLE : View.INVISIBLE);
				mLikeCount.setText(String.valueOf(incFollows));
				mItem.setFollowedByUser(true);
				setCanLike(true);
			}
		}

		@Override
		public void onRequestException(SpiceException exception) {
			Map<String,String> payload = new HashMap<>();
			payload.put(SwrveEvents.EVENT_FAIL_KEY, exception.getMessage());
			SwrveSDK.event(SwrveEvents.WATCHLIST_ADD_FAIL, payload);
			hideProgress();
			setCanLike(true);
			setMenuItemLikeIcon(false);
			Logger.error(exception.getMessage());
			MessageUtil.showError(getBaseActivity(), exception.getMessage(), getBaseActivity().getCroutonsHolder());
		}
	}

	private class UnfollowListener extends FollowListener {

		@Override
		public void onRequestException(SpiceException exception) {
			Map<String,String> payload = new HashMap<>();
			payload.put(SwrveEvents.EVENT_FAIL_KEY, exception.getMessage());
			SwrveSDK.event(SwrveEvents.WATCHLIST_REMOVE_FAIL, payload);
			setMenuItemLikeIcon(true);
		}

		@Override
		public void onRequestSuccessfull(Boolean success) {
			hideProgress();
			if (success) {
				SwrveSDK.event(SwrveEvents.WATCHLIST_REMOVE_SUCCESS);
				int incFollows = NumberUtils.toInt(mLikeCount.getText().toString()) - 1;
				mLikeCount.setVisibility((incFollows > NO_LIKES) ? View.VISIBLE : View.INVISIBLE);
				mLikeCount.setText(String.valueOf(incFollows));
				mItem.setFollowedByUser(false);
				setCanLike(true);
			}
		}

		@Override
		public void onRequestError(Error error) {
			Map<String,String> payload = new HashMap<>();
			payload.put(SwrveEvents.EVENT_FAIL_KEY, error.getMessage());
			SwrveSDK.event(SwrveEvents.WATCHLIST_REMOVE_FAIL, payload);
			setMenuItemLikeIcon(true);
		}
	}

	private class MarkAsSoldListener extends BaseNeckRequestListener<Boolean> {

		@Override
		public void onRequestError(Error error) {
			hideProgress();
			MessageUtil.showError(getBaseActivity(), error.getMessage(), getBaseActivity().getCroutonsHolder());
		}

		@Override
		public void onRequestSuccessfull(Boolean success) {
			if (success) {
				MessageUtil.showSuccess(getBaseActivity(), getString(R.string.item_sold), getBaseActivity().getCroutonsHolder());
				ParkApplication.getInstance().getEventsLogger().logEvent(FacebookUtil.EVENT_ITEM_MARKED_AS_SOLD);
				mItemEdit.setEnabled(!success);
				onRefresh();
			} else {
				hideProgress();
			}
		}

		@Override
		public void onRequestException(SpiceException exception) {
			hideProgress();
			Logger.error(exception.getMessage());
			MessageUtil.showError(getBaseActivity(), exception.getMessage(),
					getBaseActivity().getCroutonsHolder());
		}
	}

	private class ItemRepublishListener extends BaseNeckRequestListener<ItemRepublishResponse> {

		@Override
		public void onRequestError(Error error) {
			Map<String,String> payload = new HashMap<>();
			payload.put(SwrveEvents.EVENT_FAIL_KEY, error.getMessage());
			SwrveSDK.event(SwrveEvents.REPOST_AD_FAIL, payload);
			hideProgress();
			MessageUtil.showError(getBaseActivity(), error.getMessage(),
					getBaseActivity().getCroutonsHolder());
		}

		@Override
		public void onRequestSuccessfull(ItemRepublishResponse t) {
			if (t != null) {
				SwrveSDK.event(SwrveEvents.REPOST_AD_SUCCESS);
				MessageUtil.showSuccess(getBaseActivity(), getString(R.string.item_republished),
						getBaseActivity().getCroutonsHolder());
				mItem.setId(t.getRepublishedItemId());
				itemId = t.getRepublishedItemId();
				onRefresh();
			} else {
				hideProgress();
			}
		}

		@Override
		public void onRequestException(SpiceException exception) {
			Map<String,String> payload = new HashMap<>();
			payload.put(SwrveEvents.EVENT_FAIL_KEY, exception.getMessage());
			SwrveSDK.event(SwrveEvents.REPOST_AD_FAIL, payload);
			hideProgress();
			Logger.error(exception.getMessage());
			MessageUtil.showError(getBaseActivity(), exception.getMessage(), getBaseActivity().getCroutonsHolder());
		}

	}

	private void setLikeVisible(boolean visible) {
		mFlagLikeVisible = visible;
		if (getBaseActivity() != null) {
			getBaseActivity().supportInvalidateOptionsMenu();
		}
	}

	private void setCanLike(boolean canLike) {
		mFlagCanLike = canLike;
		if (getBaseActivity() != null) {
			getBaseActivity().supportInvalidateOptionsMenu();
		}
	}

	private void setMenuItemLikeIcon(boolean like) {
		mFlagLikeIcon = like;
		if (getBaseActivity() != null) {
			getBaseActivity().supportInvalidateOptionsMenu();
		}
	}

	private void setShareVisible(boolean visible) {
		mFlagShareVisible = visible;
		if (getBaseActivity() != null) {
			getBaseActivity().supportInvalidateOptionsMenu();
		}
	}

	private void setCanShare(boolean canShare) {
		mFlagCanShare = canShare;
		if (getBaseActivity() != null) {
			getBaseActivity().supportInvalidateOptionsMenu();
		}
	}

	private OnSwipeTouchListener itemGestureListener = new OnSwipeTouchListener(getBaseActivity()){

		@Override
		public void onSwipeLeft() {
			nextItem = true; // GO TO THE NEXT ITEM
			if (itemsListParams.getCurrentPos() == (itemIds.size()-1)) {
				startItemsIdsRequest();
			} else {
				showNextItemVip();
			}
		}

		@Override
		public void onSwipeRight() {
			super.onSwipeRight();
			nextItem = false; // GO TO THE PREVIOUS ITEM
			if (itemsListParams.getCurrentPos() != 0){
				showNextItemVip();
			}
		}
	};

	private OnSwipeTouchListener itemImageGestureListener = new OnSwipeTouchListener(getBaseActivity()){

		@Override
		public void onSwipeRight() {
			nextItem = false; // GO TO THE PREVIOUS ITEM
			if (itemsListParams.getCurrentPos() != 0){
				showNextItemVip();
			}
		}

		@Override
		public void onSwipeLeft() {
			nextItem = true; // GO TO THE NEXT ITEM
			if (itemsListParams.getCurrentPos() == (itemIds.size()-1)) {
				startItemsIdsRequest();
			} else {
				showNextItemVip();
			}
		}
	};

	private OnSwipeTouchListener itemMapGestureListener = new OnSwipeTouchListener(getBaseActivity()){

		@Override
		public void onSwipeRight() {
			nextItem = false; // GO TO THE PREVIOUS ITEM
			if (itemsListParams.getCurrentPos() != 0){
				showNextItemVip();
			}
		}

		@Override
		public void onSwipeLeft() {
			nextItem = true; // GO TO THE NEXT ITEM
			if (itemsListParams.getCurrentPos() == (itemIds.size()-1)) {
				startItemsIdsRequest();
			} else {
				showNextItemVip();
			}
		}

		@Override
		public void onClick() {
			if (mItemDeleted) {
				MessageUtil.showError(getBaseActivity(), getString(R.string.item_deleted),
						getBaseActivity().getCroutonsHolder());
			} else {
				try {
					startActivity(IntentFactory.getMapIntent(getBaseActivity(), mItem.getLatitude(), mItem.getLongitude(), mItem.getLocationName()));
				} catch (Exception e) {
				}
			}
		}
	};

}