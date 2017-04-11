package com.ebay.park.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.ebay.park.R;
import com.ebay.park.base.BaseListFragment;
import com.ebay.park.flow.ScreenManager;
import com.ebay.park.interfaces.OnShowListenerLollipop;
import com.ebay.park.interfaces.OnShowListenerPreLollipop;
import com.ebay.park.interfaces.Tabeable;
import com.ebay.park.model.PendingRateModel;
import com.ebay.park.model.RateModel;
import com.ebay.park.model.RateModel.Ranking;
import com.ebay.park.requests.PendingRatesDeleteRequest;
import com.ebay.park.requests.PendingRatesListRequest;
import com.ebay.park.requests.PendingRatesListRequest.PendingRatesResponse;
import com.ebay.park.requests.RateUserRequest;
import com.ebay.park.utils.DeviceUtils;
import com.ebay.park.utils.DialogUtils;
import com.ebay.park.utils.KeyboardHelper;
import com.ebay.park.utils.Logger;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.views.TextViewBook;
import com.ebay.park.views.TextViewDemi;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.squareup.picasso.Picasso;

import java.util.*;

public class PendingRatesFragment extends BaseListFragment<PendingRatesResponse> implements Tabeable {

	private ListView mPendingList;
	private PendingRatesAdapter mAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_pending_rates,
				getContainerLayout(inflater, container, savedInstanceState), true);
	}

	@Override
	public void onViewCreated(View rootView, Bundle savedInstanceState) {
		super.onViewCreated(rootView, savedInstanceState);
		loadEmptyView(getBaseActivity(), (ViewGroup) rootView.findViewById(R.id.list_container));
		mPendingList = (ListView) rootView.findViewById(R.id.pending_rates_list);
		mPendingList.setAdapter(mAdapter);
		mPendingList.setOnScrollListener(new ListScroll());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		disableRefreshSwipe();
	}

	@Override
	public void onBackPressed() {
	}

	private class PendingRatesAdapter extends ModelAdapter<RateModel> {

		public void mergePending(List<PendingRateModel> aList) {
			ArrayList<RateModel> aRateList = new ArrayList<>();
			Iterator<PendingRateModel> aIterator = aList.iterator();
			PendingRateModel aItem;
			RateModel aRate;
			while (aIterator.hasNext()) {
				aItem = aIterator.next();
				aRate = new RateModel();
				aRate.setItemImageUrl(aItem.getItemImageUrl());
				aRate.setUserImageUrl(aItem.getUserImageUrl());
				aRate.setItemName(aItem.getItemName());
				aRate.setRatingId(aItem.getRatingId());
				aRate.setItemId(aItem.getItemId());
				aRate.setUserId(aItem.getUserIdToRate());
				aRate.setUsername(aItem.getUsernameToRate());
				aRateList.add(aRate);
			}
			super.merge(aRateList);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(getBaseActivity(), R.layout.item_pending_rate, null);
				convertView.setTag(new ViewHolder(convertView));
			}
			ViewHolder aViewHolder = (ViewHolder) convertView.getTag();
			aViewHolder.load(getItem(position), position);
			return convertView;
		}

		class ViewHolder {
			private ImageView mItemImage;
			private ImageView mProfileImage;
			private TextViewBook mRateUsername;
			private TextViewDemi mItemName;
			private ImageView mRatePositive;
			private ImageView mRateNeutral;
			private ImageView mRateNegative;
			private TextViewBook mRateMessage;
			private View mBtnRateSubmit;
			private View mBtnRateDiscart;

			public ViewHolder(View aView) {
				mItemImage = (ImageView) aView.findViewById(R.id.rate_item_image);
				mProfileImage = (ImageView) aView.findViewById(R.id.rate_profile_img);
				mRateUsername = (TextViewBook) aView.findViewById(R.id.rate_username);
				mItemName = (TextViewDemi) aView.findViewById(R.id.rate_item_name);
				mRatePositive = (ImageView) aView.findViewById(R.id.rate_positive);
				mRateNeutral = (ImageView) aView.findViewById(R.id.rate_neutral);
				mRateNegative = (ImageView) aView.findViewById(R.id.rate_negative);
				mRateMessage = (TextViewBook) aView.findViewById(R.id.rate_message);
				mBtnRateSubmit = aView.findViewById(R.id.rate_submit);
				mBtnRateDiscart = aView.findViewById(R.id.rate_discart);
			}

			public void load(final RateModel aPendingRate, int position) {
				if (TextUtils.isEmpty(aPendingRate.getItemImageUrl())) {
					aPendingRate.setItemImageUrl(null);
				}
				Picasso.with(getBaseActivity()).load(aPendingRate.getItemImageUrl()).placeholder(R.drawable.img_placeholder)
						.fit().into(mItemImage);

				if (TextUtils.isEmpty(aPendingRate.getUserImageUrl())) {
					aPendingRate.setUserImageUrl(null);
				}
				Picasso.with(getBaseActivity()).load(aPendingRate.getUserImageUrl()).placeholder(R.drawable.avatar_tiny_ph_image_fit)
						.fit().centerCrop().into(mProfileImage);

				mRateUsername.setText(aPendingRate.getUsername());
				mRateUsername.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (aPendingRate != null) {
							if (aPendingRate.getUsername() != null) {
								ScreenManager.showProfileActivity(getBaseActivity(), aPendingRate.getUsername());
							}
						}
					}
				});
				mItemName.setText(aPendingRate.getItemName());

				mRateMessage.setOnClickListener(new RateClickListener(position));

				mRatePositive.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (aPendingRate.getRatingStatus()!=null) {
							if (aPendingRate.getRatingStatus()==Ranking.POSITIVE) {
								aPendingRate.setRatingStatus(null);
							} else {
								aPendingRate.setRatingStatus(Ranking.POSITIVE);
							}
						} else {
							aPendingRate.setRatingStatus(Ranking.POSITIVE);
						}
						mAdapter.notifyDataSetChanged();
					}
				});

				mRateNeutral.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (aPendingRate.getRatingStatus()!=null) {
							if (aPendingRate.getRatingStatus()==Ranking.NEUTRAL) {
								aPendingRate.setRatingStatus(null);
							} else {
								aPendingRate.setRatingStatus(Ranking.NEUTRAL);
							}
						} else {
							aPendingRate.setRatingStatus(Ranking.NEUTRAL);
						}
						mAdapter.notifyDataSetChanged();
					}
				});

				mRateNegative.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (aPendingRate.getRatingStatus()!=null) {
							if (aPendingRate.getRatingStatus()==Ranking.NEGATIVE) {
								aPendingRate.setRatingStatus(null);
							} else {
								aPendingRate.setRatingStatus(Ranking.NEGATIVE);
							}
						} else {
							aPendingRate.setRatingStatus(Ranking.NEGATIVE);
						}
						mAdapter.notifyDataSetChanged();
					}
				});

				updateRates(aPendingRate);

				mBtnRateSubmit.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (checkRateSubmit(aPendingRate)) {
							mPendingList.setEnabled(false);
							mSpiceManager.execute(
									new RateUserRequest(aPendingRate.getComment(), aPendingRate.getUserId(),
											aPendingRate.getItemId(), aPendingRate.getRatingStatus()),
									new RateUserRequestListener(aPendingRate));
							KeyboardHelper.hide(getBaseActivity(),getBaseActivity().getCurrentFocus());
						} else {
							final AlertDialog builder = new AlertDialog.Builder(getBaseActivity())
									.setMessage(R.string.rate_warning)
									.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											dialog.dismiss();
										}
									}).create();

							builder.setOnShowListener(new DialogInterface.OnShowListener() {
								@Override
								public void onShow(DialogInterface arg0) {
									builder.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
											getResources().getColor(R.color.IndicatorOrange));
								}
							});

							builder.show();
						}
					}
				});

				mBtnRateDiscart.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						showConfirmDismissDialog(aPendingRate);
						KeyboardHelper.hide(getBaseActivity(),getBaseActivity().getCurrentFocus());
					}
				});
			}

			private void updateRates(final RateModel aPendingRate) {
				if (aPendingRate.getRatingStatus() != null) {
					switch (aPendingRate.getRatingStatus()) {
						case POSITIVE:
							mRatePositive.setImageDrawable(getResources().getDrawable(R.drawable.btn_rate_positive_selected));
							mRateNeutral.setImageDrawable(getResources().getDrawable(R.drawable.btn_rate_neutral_unselected));
							mRateNegative.setImageDrawable(getResources().getDrawable(R.drawable.btn_rate_negative_unselected));
							break;
						case NEUTRAL:
							mRatePositive.setImageDrawable(getResources().getDrawable(R.drawable.btn_rate_positive_unselected));
							mRateNeutral.setImageDrawable(getResources().getDrawable(R.drawable.btn_rate_neutral_selected));
							mRateNegative.setImageDrawable(getResources().getDrawable(R.drawable.btn_rate_negative_unselected));
							break;
						case NEGATIVE:
							mRatePositive.setImageDrawable(getResources().getDrawable(R.drawable.btn_rate_positive_unselected));
							mRateNeutral.setImageDrawable(getResources().getDrawable(R.drawable.btn_rate_neutral_unselected));
							mRateNegative.setImageDrawable(getResources().getDrawable(R.drawable.btn_rate_negative_selected));
							break;
						default:
							break;
					}
				} else {
					mRatePositive.setImageDrawable(getResources().getDrawable(R.drawable.btn_rate_positive_default));
					mRateNeutral.setImageDrawable(getResources().getDrawable(R.drawable.btn_rate_neutral_default));
					mRateNegative.setImageDrawable(getResources().getDrawable(R.drawable.btn_rate_negative_default));
				}

				mRateMessage.setText((aPendingRate.getComment() != null) ? aPendingRate.getComment() : "");
			}

			private void showCommentDialog(final int position){
				final AlertDialog dialog;

				if (!TextUtils.isEmpty(mRateMessage.getText().toString())){
					dialog = DialogUtils.getDialogWithField(getBaseActivity(),
							R.string.dialog_rate_hint, R.string.rate_message_hint,
							mRateMessage.getText().toString())
							.setPositiveButton(R.string.accept, null)
							.setNegativeButton(R.string.cancel, null).create();
				} else {
					dialog = DialogUtils.getDialogWithField(getBaseActivity(),
							R.string.dialog_rate_hint, R.string.rate_message_hint)
							.setPositiveButton(R.string.accept, null)
							.setNegativeButton(R.string.cancel, null).create();
				}

				if (DeviceUtils.isDeviceLollipopOrHigher()){
					dialog.setOnShowListener(new OnShowListenerLollipop(dialog, getBaseActivity()));
				} else {
					dialog.setOnShowListener(new OnShowListenerPreLollipop(dialog, getBaseActivity()));
				}

				dialog.setCancelable(false);
				dialog.show();

				dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						EditText editCancelReason = DialogUtils.getDialogField(dialog);
						String rateMessage = editCancelReason.getText().toString();
						if (!TextUtils.isEmpty(rateMessage)){
							mRateMessage.setText(rateMessage);
							mAdapter.getItem(position).setComment(rateMessage);
						}
						KeyboardHelper.hide(getBaseActivity(),editCancelReason);
						dialog.dismiss();
					}
				});

				dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						EditText editCancelReason = DialogUtils.getDialogField(dialog);
						KeyboardHelper.hide(getBaseActivity(),editCancelReason);
						dialog.cancel();
					}
				});
			}

			private class RateClickListener implements OnClickListener {

				private int mPosition;

				public RateClickListener(int position){
					this.mPosition = position;
				}

				@Override
				public void onClick(View v) {
					showCommentDialog(mPosition);
				}
			}

		}

		@Override
		public long getItemId(int position) {
			return getItem(position).getRatingId();
		}
	}

	private static boolean checkRateSubmit(RateModel aPendingRate) {
		return isRateCommentSet(aPendingRate) && isRateSelectionSet(aPendingRate);
	}

	private static boolean isRateCommentSet(RateModel aPendingRate) {
		return !TextUtils.isEmpty(aPendingRate.getComment());
	}

	private static boolean isRateSelectionSet(RateModel aPendingRate) {
		return aPendingRate.getRatingStatus() != null;
	}

	private class PendingRatesListener extends ListListener {
	}

	private class RateUserRequestListener extends BaseNeckRequestListener<Boolean> {

		private RateModel mSelectedPendingRate;

		public RateUserRequestListener(RateModel selectedPendingRate) {
			super();
			this.mSelectedPendingRate = selectedPendingRate;
		}

		@Override
		public void onRequestError(com.globant.roboneck.requests.BaseNeckRequestException.Error error) {
			hideProgress();
			mPendingList.setEnabled(true);
			MessageUtil.showError(getBaseActivity(), error.getMessage(), getBaseActivity().getCroutonsHolder());
			Logger.warn(error.getMessage());
		}

		@Override
		public void onRequestSuccessfull(Boolean success) {
			hideProgress();
			mPendingList.setEnabled(true);
			if (success) {
				mAdapter.remove(mSelectedPendingRate);
				if (mPendingList.getAdapter().isEmpty()) {
					onRefresh();
				}
				MessageUtil.showSuccess(getBaseActivity(), getString(R.string.pending_rate_successfull), getBaseActivity().getCroutonsHolder());
			}
		}

		@Override
		public void onRequestException(SpiceException exception) {
			hideProgress();
			mPendingList.setEnabled(true);
			MessageUtil.showError(getBaseActivity(), exception.getMessage(), getBaseActivity().getCroutonsHolder());
			Logger.warn(exception.getMessage());
		}

	}

	@Override
	public void onTabSelected() {
	}

	@Override
	protected ModelAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	protected void initializeAdapter() {
		mAdapter = new PendingRatesAdapter();
	}

	@Override
	protected void getDataFromServer() {
		showProgress();
		mSpiceManager.executeCacheRequest(new PendingRatesListRequest(page, PAGE_SIZE), new PendingRatesListener());
	}

	@Override
	protected void clearData() {
		mAdapter.clear();
	}

	@Override
	protected void loadData(PendingRatesResponse aResponse) {
		mIsLoading = false;
		if (!aResponse.getPendingRatings().isEmpty()) {
			long fetchedItems = mAdapter.getCount() + aResponse.getPendingRatings().size();
			mLoadedAllItems = aResponse.getTotalElements() <= fetchedItems;
			mAdapter.mergePending(aResponse.getPendingRatings());
		}
		if (mAdapter.isEmpty()) {
			mPendingList.setEmptyView(mEmptyView);
			mEmptyMessage.setText(aResponse.getNoResultsMessage());
			mEmptyHint.setText(aResponse.getNoResultsHint());
		}
	}

	private void showConfirmDismissDialog(final RateModel aPendingRate) {
		final AlertDialog dialog = DialogUtils.getDialogWithLabel(getBaseActivity(), R.string.pending_rate_dismiss_title,
				R.string.pending_rate_dismiss_message, R.drawable.delete)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						showProgress();
						mSpiceManager.execute(new PendingRatesDeleteRequest(aPendingRate.getRatingId()),
								new BaseNeckRequestListener<PendingRateModel>() {

									@Override
									public void onRequestError(Error error) {
										Logger.verb("onRequestError");
										MessageUtil.showError(getBaseActivity(), error.getMessage(),
												getBaseActivity().getCroutonsHolder());
										hideProgress();
									}

									@Override
									public void onRequestSuccessfull(PendingRateModel model) {
										hideProgress();
										showError(getString(R.string.delete_successfully));
										mAdapter.remove(aPendingRate);
										if (mAdapter.isEmpty()) {
											onRefresh();
										}
									}

									@Override
									public void onRequestException(SpiceException exception) {
										hideProgress();
										showError(exception.getMessage());
										Logger.error(exception.getMessage());
									}
								});
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
		MessageUtil.showError(getBaseActivity(), message,
				getBaseActivity().getCroutonsHolder());
	}

}