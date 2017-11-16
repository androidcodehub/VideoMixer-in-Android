package com.androidcodehub.demo.ui.record.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidcodehub.demo.R;
import com.androidcodehub.demo.po.POThemeSingle;
import com.androidcodehub.demo.utils.IsUtils;


import java.util.Observable;
import java.util.Observer;


public class ThemeView extends RelativeLayout implements Observer {


	private ImageView mSelectedIcon;
    private BitmapImageView mIcon;

	private TextView mTitle;
	private POThemeSingle mTheme;

	public ThemeView(Context context, POThemeSingle theme) {
		super(context);
		this.mTheme = theme;

		LayoutInflater.from(context).inflate(R.layout.view_theme_item, this);
		mIcon = (BitmapImageView) findViewById(R.id.icon);
		mSelectedIcon = (ImageView) findViewById(R.id.selected);
		mTitle = (TextView) findViewById(R.id.title);

		mTitle.setText(mTheme.themeDisplayName);

		if (!mTheme.isMV()) {

			mSelectedIcon.setImageResource(R.drawable.record_theme_square_selected);
		}
		if (mTheme.isEmpty()) {
			mSelectedIcon.setVisibility(View.VISIBLE);
		}
	}


	public BitmapImageView getIcon() {
		return mIcon;
	}

	@Override
	public void update(Observable observable, Object data) {
		if (data != null && mTheme != null) {
			if (IsUtils.equals(mTheme.themeName, data.toString())) {
				mSelectedIcon.setVisibility(View.VISIBLE);
			} else {
				mSelectedIcon.setVisibility(View.GONE);
			}
		}
	}
}
