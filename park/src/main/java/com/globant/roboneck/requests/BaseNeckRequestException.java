package com.globant.roboneck.requests;

public class BaseNeckRequestException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Error error;

	public BaseNeckRequestException(Exception original) {
		super(original);
	}

	public BaseNeckRequestException(Error error) {
		this.error = error;
	}

	public Error getError() {
		return error;
	}

	public interface Error {

		/**
		 * A message returned by the API giving details about the response.
		 * 
		 * @return The message returned by the API.
		 */
		public String getMessage();

		/**
		 * The error code returned by the API.
		 * 
		 * @return The code.
		 */
		public int getErrorCode();

		/**
		 * The status code returned by the API.
		 * 
		 * @return The code.
		 */
		public int getStatus();

	}

}
