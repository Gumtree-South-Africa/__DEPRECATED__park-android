package com.globant.roboneck.requests;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;

public abstract class BaseNeckRequestListener<T> implements PendingRequestListener<T> {

	public final void onRequestFailure(SpiceException e) {
		onRequestException(e);
	}

	public abstract void onRequestError(BaseNeckRequestException.Error error);

	public abstract void onRequestSuccessfull(T t);

	public abstract void onRequestException(SpiceException exception);

	@Override
	public final void onRequestSuccess(T t) {
		if (t != null) {
			try {
				BaseNeckRequestException.Error e = (BaseNeckRequestException.Error) t;
				e.getErrorCode();
				onRequestError(e);
			} catch (ClassCastException e) {
				onRequestSuccessfull(t);
			}
		}
	}

	@Override
	public void onRequestNotFound() {

	}

}
