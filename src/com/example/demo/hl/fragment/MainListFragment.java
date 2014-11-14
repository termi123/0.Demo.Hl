package com.example.demo.hl.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockListFragment;
import com.example.demo.hl.MainActivity;
import com.example.demo.hl.R;
import com.example.demo.hl.util.Constants;

public class MainListFragment extends SherlockFragment{
	private ViewFlipper mViewFlipper;
	private View view;
	public final static String INTENT_VAR_IS_RELATED = "intentVarIsRelated";
	private DoujinListFragment frmDoujinList;
	
	public void setMainActivity(MainActivity mainActivity) {
	}
	
	public void onStart() {
		super.onStart();
		init();
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater
				.inflate(R.layout.fragment_main_list, container, false);
		
		return view;
	}
	
	private void init() {
		mViewFlipper = (ViewFlipper) getView().findViewById(R.id.view_flipper);
		mViewFlipper.setAutoStart(true);
		mViewFlipper.setFlipInterval(6000);
		mViewFlipper.startFlipping();
		
//		frmMainList = new MainListFragment();
//		getActivity().getSupportFragmentManager().beginTransaction()
//		.replace(R.id.content_frame, frmMainList).commit();
//		setTitle("Ha Long Bay - Travel Helpers");
		
		
//		frmDoujinList = new DoujinListFragment();
//		frmDoujinList.setMainActivity(this);
//		frmDoujinList.setRelated(isRelated);
//		String url = Constants.SITEHOTEL;
//		frmDoujinList.setUrl(url);
//		String title = Constants.TITLEHOTEL;
//		getActivity().setTitle(title);
//		getActivity().getSupportFragmentManager().beginTransaction()
//				.replace(R.id.contenthot, frmDoujinList).addToBackStack(null).commit();
	}
	
}
