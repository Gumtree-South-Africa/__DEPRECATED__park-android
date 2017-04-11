package com.ebay.park.utils;

import android.widget.EditText;

import org.apache.commons.lang3.StringUtils;

/**
 * Validates user input.
 * 
 * @author federico.perez
 * 
 */
public abstract class Validations {

	public static boolean validateEmail(EditText v) {
		return android.util.Patterns.EMAIL_ADDRESS.matcher(v.getText().toString()).matches();
	}

	public static boolean validateEmailMatchConfirmation(EditText vEmail, EditText vConfirmation){
		return vEmail.getText().toString().equals(vConfirmation.getText().toString());
	}

	public static boolean validateNameLastname(EditText v) {
		return v.getText().toString().matches(Constants.NAME_LASTNAME_REGEX);
	}

	public static boolean validateItemName(EditText v) {
		String itemName = v.getText().toString().trim();
		return itemName.toString().matches(Constants.ITEMNAME_REGEX);
	}
	
	public static boolean validateGroupName(EditText v) {
		String groupName = v.getText().toString().trim();
		return groupName.toString().matches(Constants.GROUPNAME_REGEX);
	}

	public static boolean validateItemDescription(EditText v) {
		return v.getText().toString().matches(Constants.ITEMDESCRIPTION_REGEX);
	}
	
	public static boolean validateGroupDescription(EditText v) {
		return v.getText().toString().matches(Constants.GROUPDESCRIPTION_REGEX);
	}


	public static boolean validateZipCode(EditText v) {
		return v.getText().toString().matches(Constants.ZIPCODE_REGEX);
	}

	public static boolean validatePassword(EditText v) {
		String password = v.getText().toString();
		if (password.length() < 6 || password.length() > 15) {
			return false;
		}
		return true;
	}

	public static boolean validatePrice(EditText v) {
		return v.getText().toString().replace("$", "").matches(Constants.PRICE_REGEX);
	}

	public static boolean validateChatMessage(EditText v) {
		return v.getText().toString().length() >= Constants.CHAT_MIN_LENGHT
				&& v.getText().toString().length() <= Constants.CHAT_MAX_LENGHT;
	}

	public static boolean validateUsernameleght(EditText v) {
		return v.getText().toString().length() >= Constants.ITEM_NAME_MIN_LENGTH
				&& v.getText().toString().length() <= Constants.NAME_LASTNAME_MAX_LENGTH;
	}

	public static boolean isNotEmpty(EditText v) {
		return v != null && StringUtils.isNotBlank(v.getText());
	}

}
