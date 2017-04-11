package com.ebay.park.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ebay.park.R;
import com.ebay.park.base.BaseFragment;
import com.ebay.park.base.BaseSessionActivity;
import com.ebay.park.requests.ChangePassRequest;
import com.ebay.park.utils.MessageUtil;
import com.ebay.park.views.EditTextBook;
import com.ebay.park.views.TextViewBook;
import com.globant.roboneck.requests.BaseNeckRequestException.Error;
import com.globant.roboneck.requests.BaseNeckRequestListener;
import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.persistence.exception.SpiceException;

import static com.ebay.park.utils.Validations.isNotEmpty;

public class ChangePassFragment extends BaseFragment implements OnRefreshListener {

	public static Boolean processingPassChange = false;

	private TextView mSuccessView;
	private Button mSubmit;
	private EditTextBook mCurrentPassword;
	private EditTextBook mNewPassword;
	private EditTextBook mRepeatPassword;
	private TextViewBook mCurrentError;
	private TextViewBook mNewPassError;
	private TextViewBook mConfirmNewError;

	enum STATUS {
		FILLED, EMPTY
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View baseView = inflater.inflate(R.layout.fragment_change_password, (ViewGroup) super.onCreateView(inflater, container, savedInstanceState), true);
		mSuccessView = (TextView) baseView.findViewById(R.id.forgot_pass_success);
		mCurrentError = (TextViewBook) baseView.findViewById(R.id.tv_current_pass_error);
		mNewPassError = (TextViewBook) baseView.findViewById(R.id.tv_new_pass_error);
		mConfirmNewError = (TextViewBook) baseView.findViewById(R.id.tv_pass_error);

		final TextWatcher watcher = new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				getActivity().invalidateOptionsMenu();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		};

		mCurrentPassword = (EditTextBook) baseView.findViewById(R.id.change_pass_current);
		mCurrentPassword.addTextChangedListener(watcher);
		mCurrentPassword.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					passwordEmptyCheck(mCurrentPassword);
				} else {
					removeError(mCurrentPassword);
				}
			}
		});

		mNewPassword = (EditTextBook) baseView.findViewById(R.id.change_pass_new);
		mNewPassword.addTextChangedListener(watcher);
		mNewPassword.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus){
					passwordEmptyCheck(mNewPassword);
				} else {
					removeError(mNewPassword);
				}
			}
		});

		mRepeatPassword = (EditTextBook) baseView.findViewById(R.id.change_pass_repeat);
		mRepeatPassword.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				int lenght = mRepeatPassword.getText().toString().length();
				if (lenght > 0 && lenght >= mNewPassword.getText().length() && !passwordMatch()){
					setError(mNewPassword, STATUS.FILLED, null);
					setError(mRepeatPassword, STATUS.FILLED, null);
				} else {
					removeError(mNewPassword);
					removeError(mRepeatPassword);
				}
				getActivity().invalidateOptionsMenu();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		mRepeatPassword.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus){
					if(passwordEmptyCheck(mRepeatPassword) && !passwordMatch()){
						setError(mNewPassword, STATUS.FILLED, null);
						setError(mRepeatPassword, STATUS.FILLED, null);
					}
				}
			}
		});

		setTitle(R.string.change_password);

		return baseView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.menu_update_profile, menu);
	}

	private boolean passwordCheck() {
		return isNotEmpty(mCurrentPassword) && isNotEmpty(mNewPassword) && isNotEmpty(mRepeatPassword)
				&& passwordMatch();
	}

	private boolean passwordEmptyCheck(EditTextBook field){
		boolean isNotEmpty = isNotEmpty(field);
		if (!isNotEmpty){
			setError(field, STATUS.EMPTY, null);
		}
		return isNotEmpty;
	}

	private void setError(EditTextBook et, STATUS status, String errorBE){
		et.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_validation_error, 0);
		et.setCompoundDrawablePadding(8);

		int id = et.getId();

		switch (id){
			case R.id.change_pass_current:
				mCurrentError.setText(getString(R.string.update_empty));
				mCurrentError.setVisibility(View.VISIBLE);
				break;
			case R.id.change_pass_new:
				mNewPassError.setText(errorBE!=null ? errorBE : getString(R.string.update_empty));
				mNewPassError.setVisibility(((status == STATUS.EMPTY) || (errorBE != null)) ? View.VISIBLE : View.INVISIBLE);
				break;
			case R.id.change_pass_repeat:
				mConfirmNewError.setText((status == STATUS.EMPTY) ? getString(R.string.update_empty) : getString(R.string.password_no_match));
				mConfirmNewError.setVisibility(View.VISIBLE);
				break;
			default:
				break;
		}
	}

	private void setErrorRequest(String text){
		mCurrentPassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_validation_error, 0);
		mCurrentPassword.setCompoundDrawablePadding(8);
		mCurrentError.setText(text);
		mCurrentError.setVisibility(View.VISIBLE);
	}

	private void removeError(EditTextBook et){
		et.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

		int id = et.getId();

		switch (id){
			case R.id.change_pass_current:
				mCurrentError.setVisibility(View.INVISIBLE);
				break;
			case R.id.change_pass_new:
				mNewPassError.setVisibility(View.INVISIBLE);
				break;
			case R.id.change_pass_repeat:
				mConfirmNewError.setVisibility(View.INVISIBLE);
				break;
			default:
				break;
		}
	}

	private boolean passwordMatch(){
		return mNewPassword.getText().toString().equals(mRepeatPassword.getText().toString());
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.action_cancel).setVisible(false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.action_ok:
				removeError(mCurrentPassword);
				if (passwordCheck()) {
					removeError(mNewPassword);
					removeError(mRepeatPassword);
					showProgress();
					mSpiceManager.execute(new ChangePassRequest(mNewPassword.getText().toString(), mCurrentPassword.getText()
							.toString()), new ChangePasswordListener());

				} else {
					passwordEmptyCheck(mCurrentPassword);
					passwordEmptyCheck(mNewPassword);
					passwordEmptyCheck(mRepeatPassword);
					if (passwordEmptyCheck(mNewPassword) && passwordEmptyCheck(mRepeatPassword) && !passwordMatch()){
						setError(mNewPassword, STATUS.FILLED,null);
						setError(mRepeatPassword, STATUS.FILLED,null);
					}
				}
				break;
			case android.R.id.home:
				if (processingPassChange) {
					return true;
				}
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private class ChangePasswordListener extends BaseNeckRequestListener<String> {

		@Override
		public void onRequestError(Error error) {
			hideProgress();
			// 1301 - Password too short
			// 1305 - Password too similar
			if (error.getErrorCode() == 1301 || error.getErrorCode() == 1305) {
				setError(mNewPassword,STATUS.FILLED,error.getMessage());
			} else {
				setErrorRequest(error.getMessage());
			}
		}

		@Override
		public void onRequestSuccessfull(String token) {
			MessageUtil.showSuccess(getBaseActivity(), getString(R.string.change_password_succesful),
					getBaseActivity().getCroutonsHolder());
			hideProgress();
			if (mSuccessView != null && mSubmit != null) {
				mSuccessView.setVisibility(View.VISIBLE);
				mSuccessView.setText(getString(R.string.change_password_succesful));
				mSubmit.setVisibility(View.GONE);
			}
			processingPassChange = true;
			new Handler().postDelayed(back, 2 * 1000);
		}

		@Override
		public void onRequestException(SpiceException exception) {
			hideProgress();
			if (exception instanceof NoNetworkException){
				MessageUtil.showError(getBaseActivity(), getString(R.string.no_internet),
						getBaseActivity().getCroutonsHolder());
			} else {
				MessageUtil.showError(getBaseActivity(), exception.getMessage(),
						getBaseActivity().getCroutonsHolder());
			}
		}
	}

	private Runnable back = new Runnable() {

		@Override
		public void run() {
			((BaseSessionActivity) getActivity()).logout(true);
			processingPassChange = false;
		}
	};

	@Override
	public void onRefresh() {
	}

	@Override
	public void onBackPressed() {
	}

}
