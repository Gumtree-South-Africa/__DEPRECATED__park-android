package com.ebay.park.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseListFragment;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.interfaces.Tabeable;
import com.ebay.park.model.RateModel;
import com.ebay.park.requests.RatesListRequest;
import com.ebay.park.requests.RatesListRequest.RatesListResponse;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.views.TextViewBook;
import com.ebay.park.views.TextViewDemi;
import com.squareup.picasso.Picasso;

public class RateListFragment extends BaseListFragment<RatesListResponse> implements Tabeable {

    private static final String ROLE = "role";
    private static final String USERNAME = "username";

    private ListView mRatesList;
    private RatesAdapter mAdapter;

    private String mUsername;
    private int mRole;

    public static RateListFragment getSellerRates(String username) {
        RateListFragment fragment = new RateListFragment();
        Bundle args = new Bundle();
        args.putString(USERNAME, username);
        args.putInt(ROLE, RatesListRequest.ROLE_SELLER);
        fragment.setArguments(args);
        return fragment;
    }

    public static RateListFragment getBuyerRates(String username) {
        RateListFragment fragment = new RateListFragment();
        Bundle args = new Bundle();
        args.putString(USERNAME, username);
        args.putInt(ROLE, RatesListRequest.ROLE_BUYER);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            if (getArguments() == null || !getArguments().containsKey(ROLE) || !getArguments().containsKey(USERNAME)) {
                throw new IllegalStateException("RateListFragment called with no user or role");
            }
            mRole = getArguments().getInt(ROLE);
            mUsername = getArguments().getString(USERNAME);

        } catch (IllegalStateException e) {
            getBaseActivity().finish();
        } catch (Exception e) {
            MessageUtil.showError(getBaseActivity(), getString(R.string.error_generic),
                    getBaseActivity().getCroutonsHolder());
            getBaseActivity().finish();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rates, getContainerLayout(inflater, container, savedInstanceState),
                true);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);
        if (isOwnProfile()) {
            setTitle(R.string.title_activity_my_ratelist);
        } else {
            setTitle(String.format(getString(R.string.title_activity_ratelist), mUsername));
        }
        loadEmptyView(getBaseActivity(), (ViewGroup) rootView);
        mRatesList = (ListView) rootView.findViewById(R.id.rates_list);
        mRatesList.setAdapter(mAdapter);
        mRatesList.setOnScrollListener(new ListScroll());
    }

    private boolean isOwnProfile() {
        if (!TextUtils.isEmpty(ParkApplication.getInstance().getUsername())) {
            return ParkApplication.getInstance().getUsername().equals(mUsername);
        } else {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
    }

    private class RatesAdapter extends ModelAdapter<RateModel> {

        @Override
        public long getItemId(int position) {
            return getItem(position).getRatingId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getBaseActivity(), R.layout.item_rate, null);
                convertView.setTag(new ViewHolder(convertView));
            }
            ViewHolder aViewHolder = (ViewHolder) convertView.getTag();
            aViewHolder.load(getItem(position));
            return convertView;
        }

        class ViewHolder {
            private ImageView mProfileImage;
            private TextViewBook mRateUsername;
            private TextViewDemi mItemName;
            private TextViewBook mRateComment;
            private TextViewBook mRateDate;
            private ImageView mRatingIcon;

            public ViewHolder(View aView) {
                mProfileImage = (ImageView) aView.findViewById(R.id.rate_profile_img);
                mRateUsername = (TextViewBook) aView.findViewById(R.id.rate_username);
                mItemName = (TextViewDemi) aView.findViewById(R.id.rate_item_name);
                mRateComment = (TextViewBook) aView.findViewById(R.id.rate_comment);
                mRateDate = (TextViewBook) aView.findViewById(R.id.rate_date);
                mRatingIcon = (ImageView) aView.findViewById(R.id.rate_icon);
            }

            public void load(final RateModel aRate) {
                if (TextUtils.isEmpty(aRate.getUserImageUrl())) {
                    aRate.setUserImageUrl(null);
                }
                Picasso.with(getBaseActivity()).load(aRate.getUserImageUrl()).placeholder(R.drawable.avatar_medium_ph_image_fit).fit()
                        .centerCrop().into(mProfileImage);

                mRateUsername.setText(aRate.getUsername());
                mRateUsername.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (aRate != null) {
                            if (aRate.getUsername() != null) {
                                ScreenManager.showProfileActivity(getBaseActivity(), aRate.getUsername());
                            }
                        }
                    }
                });
                mItemName.setText(aRate.getItemName());
                mRateComment.setText(aRate.getComment());

                mRateDate.setText(formatDate(aRate.getRateDate()));

                switch (aRate.getRatingStatus()) {
                    case NEGATIVE:
                        Picasso.with(getBaseActivity()).load(R.drawable.icon_rating_negative).into(mRatingIcon);
                        break;
                    case NEUTRAL:
                        Picasso.with(getBaseActivity()).load(R.drawable.icon_rating_neutral).into(mRatingIcon);
                        break;
                    case POSITIVE:
                        Picasso.with(getBaseActivity()).load(R.drawable.icon_rating_positive).into(mRatingIcon);
                        break;
                    default:
                        break;
                }

            }
        }
    }

    private String formatDate(String date){
        String formated = date.substring(5,7)+"/"+date.substring(8,10)+"/"+date.substring(0,4);
        return formated;
    }

    @Override
    protected ModelAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    protected void initializeAdapter() {
        mAdapter = new RatesAdapter();
    }

    @Override
    protected void getDataFromServer() {
        showProgress();
        mSpiceManager.executeCacheRequest(new RatesListRequest(mUsername, mRole, page, PAGE_SIZE), new RateListener());
    }

    private class RateListener extends ListListener {
    }

    @Override
    protected void clearData() {
        mAdapter.clear();
    }

    @Override
    protected void loadData(RatesListResponse aResponse) {
        mIsLoading = false;
        if (!aResponse.getRatings().isEmpty()) {
            long fetchedItems = mAdapter.getCount() + aResponse.getRatings().size();
            mLoadedAllItems = aResponse.getAmountDataFound() <= fetchedItems;
            mAdapter.merge(aResponse.getRatings());
        }
        if (mAdapter.isEmpty()) {
            mRatesList.setEmptyView(mEmptyView);
            mEmptyMessage.setText(aResponse.getNoResultsMessage());
            mEmptyHint.setText(aResponse.getNoResultsHint());
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onTabSelected() {
    }
}
