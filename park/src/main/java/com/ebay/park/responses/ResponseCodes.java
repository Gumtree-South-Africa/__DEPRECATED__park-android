package com.ebay.park.responses;

/**
 * Response Codes returned by park api.
 * 
 * @author federico.perez
 * @author Nicolás Matias Fernández
 * 
 */
public class ResponseCodes {

	/**
	 * Successful request code.
	 */
	public static final int SUCCESS_CODE = 1;

	/**
	 * Failed request error code.
	 */
	public static final int FAIL_CODE = 2;

	public static class Login {

		public static final int USER_NOT_FOUND = 100;

		public static final int ACCOUNT_LOCKED = 215;

		public static final int INVALID_PASSWORD = 101;

		public static final int WARNING_INVALID_PASSWORD = 107;

		public static final int NO_FACEBOOK = 105;

		public static final int IO_ERROR = 1000;

		public static final int INVALID_FB_TOKEN = 102;

		public static final int USER_BANNED = 220;

		public static final int NON_EXISTENT_EMAIL = 112;
	}

	public static class Categories {

	}

	public static class SignUp {

		/**
		 * error.empty_username
		 */
		public static final int EMPTY_USER_NAME = 200;

		/**
		 * error.empty_password
		 */
		public static final int EMPTY_PASSWORD = 201;

		/**
		 * error.empty_email.
		 */
		public static final int EMPTY_EMAIL = 202;

		/**
		 * error.empty_location.
		 */
		public static final int EMPTY_LOCATION = 203;

		/**
		 * error.duplicated_data.
		 */
		public static final int DUPLICATED_DATA = 204;

		/**
		 * error.invalid_signup_req.
		 */
		public static final int INVALID_SIGNUP_REQ = 205;

		/**
		 * error.invalid_facebook_info.
		 */
		public static final int INVALID_FACEBOOK_INFO = 206;
	}

	public static class Signout {

		public static final int USER_NOT_FOUND = 100;

		public static final int EMPTY_TOKEN = 301;

		public static final int USER_UNAUTHORIZED = 302;
		
		public static final int APP_DEPRECATED = 108;

	}

	public static class PublishItem {

		/**
		 * error.empty_data_createitem
		 */
		public static final int EMPTY_DATA_CREATE_ITEM = 700;

		/**
		 * error.no_pictures
		 */
		public static final int NO_PICTURES = 500;

		/**
		 * error.no_marketplaces
		 */
		public static final int NO_MARKET_PLACES = 501;

	}

	public static class UpdateItem {

		/**
		 * error.empty_data_update
		 */
		public static final int EMPTY_DATA_UPDATE = 712;

		/**
		 * error.category_not_found
		 */
		public static final int CATEORY_NOT_FOUND = 708;

		/**
		 * error.invalid_group
		 */
		public static final int INVALID_GROUP = 709;

		/**
		 * error.error_deleting_comment
		 */
		public static final int DELETING_COMMENT = 710;

		/**
		 * error.error_updating_item
		 */
		public static final int UPDATING_ITEM = 711;

	}

	public static class UpdateItemPictures {

		/**
		 * error.user_unauthorized
		 */
		public static final int USER_UNAUTHORIZED = 302;

		/**
		 * error.item_not_found
		 */
		public static final int ITEM_NOT_FOUND = 701;

		/**
		 * error.item_doesnt_belong_to_user
		 */
		public static final int ITEM_DOESNT_BELONG_TO_USER = 716;

	}

	public static class DeleteItemPictures {

		/**
		 * error.error_deleting_picture
		 */
		public static final int DELETING_PICTURE = 717;

		/**
		 * error.picture_not_found
		 */
		public static final int PICTURE_NOT_FOUND = 718;

		/**
		 * error.cannot_delete_picture
		 */
		public static final int CANNOT_DELETE_PICTURE = 719;

	}

	public static class DeleteItem {

		/**
		 * error.user_unauthorized
		 */
		public static final int USER_UNAUTHORIZED = 302;

		/**
		 * error.item_not_found
		 */
		public static final int ITEM_NOT_FOUND = 701;

		/**
		 * error.item_deletion_error
		 */
		public static final int ITEM_DELETION_ERROR = 702;

		/**
		 * error.item_already_deleted
		 */
		public static final int ITEM_ALREADY_DELETED = 703;

	}

	public static class GroupManagement{

		/**
		 * error.group_already_exists
		 */
		public static final int GROUP_ALREADY_EXISTS = 1709;

		/**
		 * error.invalid_group_name_character
		 */
		public static final int INVALID_GROUP_NAME_CHARACTER = 1719;

	}

}
