package com.ebay.park.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ebay.park.R;
import com.tokenautocomplete.TokenCompleteTextView;

public class GroupsLabelView extends TokenCompleteTextView<String> {
	public GroupsLabelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		allowCollapse(false);
	}

	@Override
	protected View getViewForObject(String groupName) {
		String g = groupName;

		LayoutInflater l = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		LinearLayout view = (LinearLayout) l.inflate(R.layout.group_label_token, (ViewGroup) GroupsLabelView.this.getParent(),
				false);
		TextView textView = (TextView) view.findViewById(R.id.name);
		textView.setText(g);

		return view;
	}

	@Override
	protected String defaultObject(String completionText) {
		return new String();
	}

	public Parcelable onSaveInstanceState() {
		// This code is necessary to fix: EPA001-3813 please do not remove.
		super.onSaveInstanceState();
		return null;
	}

}