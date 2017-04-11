package com.ebay.park.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.ebay.park.ParkApplication;
import com.ebay.park.ParkApplication.UnloggedNavigations;
import com.ebay.park.R;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.base.BaseTabPagerFragment.EditManager;
import com.ebay.park.flow.IntentFactory;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.model.GroupModel;
import com.ebay.park.model.SubscriberModel;
import com.ebay.park.requests.BaseParkSessionRequest;
import com.ebay.park.requests.GroupDetailRequest;
import com.ebay.park.requests.GroupNotificationResetRequest;
import com.ebay.park.requests.GroupSubscribeRequest;
import com.ebay.park.requests.GroupUnsubscribeRequest;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.utils.PreferencesUtil;
import com.ebay.park.views.ButtonDemi;
import com.ebay.park.views.RoundImageView;
import com.ebay.park.views.TextViewBook;
import com.ebay.park.views.TextViewDemi;
import com.globant.roboneck.requests.BaseNeckRequestException;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Shows group info screen.
 *
 * @author Nicol�s Mat�as Fern�ndez
 */
public class GroupInfoFragment extends BaseFragment {

    public static boolean sSubscribedGroup;
    private GroupModel mGroup = null;
    private static final String ID_PARAM = "id";
    private static final long NO_ID = -123L;
    private long mGroupId;
    private View mLayout;
    private ImageView mGroupImage;
    private TextViewDemi mGroupName;
    private TextViewBook mGroupLocation;
    private ButtonDemi mBtnSubscribed;
    private ImageView mBtnShare;
    private TextViewBook mGroupDescription;
    private RoundImageView mGroupAdminImage;
    private TextViewDemi mGroupAdminName;
    private TextViewBook mGroupAdminLocation;
    private TextViewBook mGroupPeople;
    private TextViewBook mGroupItems;
    private View mAdmin;
    private View mSuscribers;
    private EditManager mEditManager;

    private int mOldScrollY;

    /**
     * Get the group info for a given group id.
     *
     * @param id The id of the group.
     */
    public static GroupInfoFragment forGroup(EditManager manager, long id) {
        GroupInfoFragment fragment = new GroupInfoFragment();
        Bundle args = new Bundle();
        args.putLong(ID_PARAM, id);
        fragment.setArguments(args);
        fragment.mEditManager = manager;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setHasOptionsMenu(true);
            if (getArguments() == null || !getArguments().containsKey(ID_PARAM)) {
                throw new IllegalStateException("Group info fragment called with no id args.");
            }
            mGroupId = getArguments().getLong(ID_PARAM, NO_ID);

        } catch (IllegalStateException e) {
            getBaseActivity().finish();
        } catch (Exception e) {
            MessageUtil.showError(getBaseActivity(), getString(R.string.error_generic),
                    getBaseActivity().getCroutonsHolder());
            getBaseActivity().finish();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (ParkApplication.sFgmtOrAct_toGo == UnloggedNavigations.GROUPS){
            ParkApplication.sFgmtOrAct_toGo = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ViewGroup parentView = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        final View baseview = inflater.inflate(R.layout.fragment_group_info, parentView, true);

        mLayout = baseview.findViewById(R.id.main_layout);
        mLayout.setVisibility(View.INVISIBLE);
        mGroupImage = (ImageView) baseview.findViewById(R.id.iv_group_photo);
        mGroupName = (TextViewDemi) baseview.findViewById(R.id.tv_group_name);
        mGroupLocation = (TextViewBook) baseview.findViewById(R.id.tv_group_location);
        mGroupPeople = (TextViewBook) baseview.findViewById(R.id.tv_group_users);
        mGroupItems = (TextViewBook) baseview.findViewById(R.id.tv_group_items);
        mBtnShare = (ImageView) baseview.findViewById(R.id.share_btn);
        mBtnSubscribed = (ButtonDemi) baseview.findViewById(R.id.btn_subscribe);
        mGroupDescription = (TextViewBook) baseview.findViewById(R.id.tv_group_description);
        mAdmin = baseview.findViewById(R.id.group_admin);
        mGroupAdminImage = (RoundImageView) baseview.findViewById(R.id.iv_group_admin_image);
        mGroupAdminName = (TextViewDemi) baseview.findViewById(R.id.tv_group_admin);
        mGroupAdminLocation = (TextViewBook) baseview.findViewById(R.id.tv_group_admin_location);
        mSuscribers = baseview.findViewById(R.id.group_subscribers_list);
        mBtnSubscribed.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (PreferencesUtil.getParkToken(getActivity()) != null) {
                    showProgress();
                    BaseParkSessionRequest<Boolean> aRequest;
                    if (mGroup.getSubscribed()) {
                        aRequest = new GroupUnsubscribeRequest(mGroup.getId());
                    } else {
                        aRequest = new GroupSubscribeRequest(mGroup.getId());
                    }
                    mSpiceManager.execute(aRequest, new SubscribeRequestListener(mBtnSubscribed, mGroup));
                } else {
                    MessageUtil.showLoginMsg(getActivity(), UnloggedNavigations.GROUPS);
                }

            }
        });

        mGroupAdminName.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mGroup != null) {
                    if (mGroup.getOwner() != null) {
                        if (mGroup.getOwner().getUsername() != null) {
                            ScreenManager.showProfileActivity(getBaseActivity(), mGroup.getOwner().getUsername());
                        }
                    }
                }
            }
        });

        mBtnShare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(IntentFactory.getShareGroupIntent(getBaseActivity(), mGroup.getUrl()));
            }
        });

        return baseview;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.edit:
                ScreenManager.showCreateGroupActivity(getBaseActivity(), mGroupId);
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        disableRefreshSwipe();
        showProgress();
        onRefresh();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onRefresh() {
        getActivity().supportInvalidateOptionsMenu();
        mSpiceManager.execute(new GroupDetailRequest(mGroupId), new GroupDetailListener());
    }

    private boolean isGroupOwner() {
        return mGroup != null && mGroup.getOwner() != null && mGroup.getOwner().getUsername() != null
                && TextUtils.equals(mGroup.getOwner().getUsername(), ParkApplication.getInstance().getUsername());
    }

    @Override
    public void onBackPressed() {
    }

    private class GroupDetailListener extends BaseNeckRequestListener<GroupModel> {

        @Override
        public void onRequestError(Error error) {
            hideProgress();
            MessageUtil.showError(getBaseActivity(), error.getMessage(),
                    getBaseActivity().getCroutonsHolder());
            Logger.error(error.getMessage());
        }

        @Override
        public void onRequestSuccessfull(GroupModel group) {

            getBaseActivity().supportInvalidateOptionsMenu();
            mGroup = group;
            sSubscribedGroup = mGroup.getSubscribed();

            mLayout.setVisibility(View.VISIBLE);

            if (!TextUtils.isEmpty(group.getPictureUrl())) {
                Picasso.with(getBaseActivity()).load(group.getPictureUrl()).placeholder(R.drawable.group_placeholder).fit()
                        .centerCrop().into(mGroupImage);
            } else {
                Picasso.with(getBaseActivity()).load(R.drawable.group_placeholder)
                        .placeholder(R.drawable.group_placeholder).fit().centerCrop().into(mGroupImage);
            }

            mGroupName.setText(group.getName());
            mGroupLocation.setText(group.getLocationName());
            mGroupPeople.setText(Integer.toString(group.getTotalSubscribers()));
            mGroupItems.setText(Integer.toString(group.getTotalItems()));
            setUpSubscribeBtn(mBtnSubscribed, group.getSubscribed());
            if (!TextUtils.isEmpty(group.getDescription())) {
                mGroupDescription.setText(group.getDescription());
            } else {
                mGroupDescription.setVisibility(View.GONE);
            }
            if (group.getOwner() != null) {
                if (!TextUtils.isEmpty(group.getOwner().getPictureUrl())) {
                    Picasso.with(getBaseActivity()).load(group.getOwner().getPictureUrl()).placeholder(R.drawable.avatar_big_ph_image_fit)
                            .fit().centerCrop().into(mGroupAdminImage);
                }
                mGroupAdminName.setText(group.getOwner().getUsername());
                mGroupAdminLocation.setText(group.getOwner().getLocationName());
            } else {
                mAdmin.setVisibility(View.GONE);
            }

            insertFollowingSubscribers(group.getSubscribers());

            mBtnSubscribed.setVisibility(isGroupOwner() ? View.GONE : View.VISIBLE);
            if (mEditManager != null) {
                mEditManager.setCanEdit(isGroupOwner());
            }

            if (!isGroupOwner()){
                GroupDetailFragment.mIsOwner = false;
                GroupDetailFragment parentFragment = (GroupDetailFragment) getBaseActivity().getSupportFragmentManager()
                        .findFragmentByTag(GroupDetailFragment.TEST_TAG);
                parentFragment.showCreateGroupButton();
            } else {
                GroupDetailFragment.mIsOwner = true;
            }

            if (mGroup.getSubscribed()){
                ParkApplication.getInstance().resetGroupNotification(mGroupId);
                mSpiceManager.execute(new GroupNotificationResetRequest(mGroupId), new ResetCounterListener());
            }

            hideProgress();
        }

        @Override
        public void onRequestException(SpiceException exception) {
            hideProgress();
            MessageUtil.showError(getBaseActivity(), exception.getMessage(),
                    getBaseActivity().getCroutonsHolder());
            Logger.error(exception.getMessage());
        }
    }

    private static final int MAX_FOLLOWING_COUNT = 6;

    private void insertFollowingSubscribers(List<SubscriberModel> subscribers) {
        if (subscribers == null || subscribers.isEmpty() || PreferencesUtil.getParkToken(getActivity()) == null) {
            mSuscribers.setVisibility(View.GONE);
            return;
        } else {
            mSuscribers.setVisibility(View.VISIBLE);
        }

        LayoutInflater inflater = LayoutInflater.from(getBaseActivity());
        ViewGroup container = (ViewGroup) mSuscribers.findViewById(R.id.ly_following_container);
        container.removeAllViews();
        for (int i = 0; i < subscribers.size() && i < MAX_FOLLOWING_COUNT; i++) {
            View subscriberView = inflater.inflate(R.layout.layout_following_subs,
                    container, false);
            ImageView suscriberImage = (ImageView) subscriberView.findViewById(R.id.iv_group_user);
            if (TextUtils.isEmpty(subscribers.get(i).getPicture())) {
                subscribers.get(i).setPicture(null);
            }
            Picasso.with(getBaseActivity()).load(subscribers.get(i).getPicture()).placeholder(R.drawable.avatar_small_ph_image_fit)
                    .error(R.drawable.avatar_small_ph_image_fit).fit().centerCrop().into(suscriberImage);

            container.addView(subscriberView);
        }
        if (subscribers.size() > MAX_FOLLOWING_COUNT){
            View subscriberView = inflater.inflate(R.layout.layout_following_subs,
                    container, false);
            ImageView suscriberImage = (ImageView) subscriberView.findViewById(R.id.iv_group_user);
            ImageView suscriberMask = (ImageView) subscriberView.findViewById(R.id.iv_group_user_mask);
            suscriberImage.setVisibility(View.GONE);
            Picasso.with(getBaseActivity()).load(R.drawable.btn_more).fit().centerCrop().into(suscriberMask);

            subscriberView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    GroupDetailFragment parentFragment = (GroupDetailFragment) getBaseActivity().getSupportFragmentManager()
                            .findFragmentByTag(GroupDetailFragment.TEST_TAG);
                    parentFragment.showSuscribersTab();
                }
            });

            container.addView(subscriberView);
        }
    }

    private void setUpSubscribeBtn(Button btnSubscribe, boolean isSubscriptedActiveUser) {
        if (isSubscriptedActiveUser) {
            btnSubscribe.setActivated(true);
            btnSubscribe.setText(R.string.unsubscribe);
        } else {
            btnSubscribe.setActivated(false);
            btnSubscribe.setText(R.string.subscribe);
        }
    }

    private class SubscribeRequestListener extends BaseNeckRequestListener<Boolean> {
        private GroupModel group;
        private Button subscribeButton;

        public SubscribeRequestListener(Button subscribeBtn, GroupModel group) {
            this.group = group;
            this.subscribeButton = subscribeBtn;
        }

        @Override
        public void onRequestError(Error error) {
            subscribeButton.setEnabled(true);
            hideProgress();
            MessageUtil.showError(getBaseActivity(), error.getMessage(),
                    getBaseActivity().getCroutonsHolder());
        }

        @Override
        public void onRequestSuccessfull(Boolean isSuccesful) {
            hideProgress();
            subscribeButton.setEnabled(true);
            group.setSubscribed(!group.getSubscribed());
            setUpSubscribeBtn(subscribeButton, group.getSubscribed());
        }

        @Override
        public void onRequestException(SpiceException exception) {
            hideProgress();
            MessageUtil.showError(getBaseActivity(), exception.getMessage(),
                    getBaseActivity().getCroutonsHolder());
            subscribeButton.setEnabled(true);
            Logger.error(exception.getMessage());
        }
    }

    private class ResetCounterListener extends BaseNeckRequestListener<String> {

        @Override
        public void onRequestError(BaseNeckRequestException.Error error) {
        }

        @Override
        public void onRequestSuccessfull(String s) {
        }

        @Override
        public void onRequestException(SpiceException exception) {
        }
    }
}