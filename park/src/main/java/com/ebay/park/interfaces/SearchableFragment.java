package com.ebay.park.interfaces;

import java.util.Map;

/**
 * Classes implementing this interface should be able to search for a given
 * query.
 * 
 * @author federico.perez
 * @author Nicolás Matias Fernández
 * 
 */
public interface SearchableFragment extends AttachableFragment {

	/**
	 * Search for a query showing the results to the user.
	 * 
	 * @param query
	 *            The query entered by the user.
	 */
	public void searchQuery(String query);

	/**
	 * Callback when the user taps the filter action.
	 */
	public void onFilterSelected(Map<String, String> filters, String query);

	public boolean fragmentIsLoading();
}