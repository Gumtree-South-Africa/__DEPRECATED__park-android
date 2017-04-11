package com.ebay.park.fragments;

import android.view.View;
import android.widget.TextView;

import com.ebay.park.ParkApplication;
import com.ebay.park.R;
import com.ebay.park.base.BaseGroupListFragment;
import com.ebay.park.model.GroupModel;
import com.ebay.park.responses.GroupListResponse;
import com.ebay.park.utils.FontsUtil;

/**
 * Created by jonatan on 19/08/15.
 */
public abstract class BoldableGroupFragment extends BaseGroupListFragment{

    private BoldableCallback mCallback;
    private BoldableGroupAdapter mAdapter;

    public void setBoldableCallback(BoldableCallback aCallback){
        mCallback = aCallback;
    }

    @Override
    protected void loadData(GroupListResponse aResponse) {
        super.loadData(aResponse);
        if (mCallback != null) {
            mCallback.onUpdate();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetInvalidated();
    }

    @Override
    protected void initializeAdapter() {
        mAdapter = new BoldableGroupAdapter();
    }

    public interface BoldableCallback{

        void onUpdate();

    }

    public static BoldableGroupFragment addCallback(BoldableGroupFragment aFragment, BoldableGroupFragment.BoldableCallback aCallback){
        aFragment.setBoldableCallback(aCallback);
        return aFragment;
    }

    @Override
    protected GroupAdapter getAdapter() {
        return mAdapter;
    }

    protected class BoldableGroupAdapter extends GroupAdapter{

        @Override
        protected ViewHolder getViewHolder(View aView) {
            return new BoldableViewHolder(aView);
        }

        protected class BoldableViewHolder extends ViewHolder{

            private final TextView mGroupNotificationCounter;

            public BoldableViewHolder(View vItem) {
                super(vItem);
                mGroupNotificationCounter = (TextView) vItem.findViewById(R.id.tv_notification_counter);
            }

            @Override
            public void load(GroupModel aGroup) {
                super.load(aGroup);
                if (BoldableGroupFragment.isBoldable(aGroup)){
                    mGroupLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_rectangular_border_unread));
                    mRoundedShape.setImageDrawable(getResources().getDrawable(R.drawable.mask_list_item_left_shadow));
                    mGroupTitle.setTypeface(FontsUtil.getFSDemi(getBaseActivity()));
                    mGroupNotificationCounter.setVisibility(View.VISIBLE);
                    mGroupNotificationCounter.setText(getNotificationAmmount(aGroup));
                }else{
                    mGroupLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_rectangular_border));
                    mGroupLayout.setPadding(-1,-1,-1,-1);
                    mRoundedShape.setImageDrawable(getResources().getDrawable(R.drawable.mask_list_item_left));
                    mGroupTitle.setTypeface(FontsUtil.getFSBook(getBaseActivity()));
                    mGroupNotificationCounter.setVisibility(View.GONE);
                }
            }
        }
    }


    private String getNotificationAmmount(GroupModel aGroup) {
        if (aGroup.getNewItems() <= 20){
            return String.valueOf(aGroup.getNewItems());
        }else{
            return "20+";
        }
    }

    protected static final boolean isBoldable(GroupModel aGroup){
        return aGroup.getNewItems() != 0 && !ParkApplication.getInstance().getGroupsWithResetedNotifications().contains(Long.valueOf(aGroup.getId()));
    }

}
