package com.ebay.park.model;

import java.util.List;

import com.ebay.park.responses.ZipCodesResponse.Result;

public class ZipCodeLocationModel {

	private List<Result> results;

	public List<Result> getResults() {
		return results;
	}

	public void setResults(List<Result> results) {
		this.results = results;
	}
}
