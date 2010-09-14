package com.synthable.wifispy;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class HomeActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	public void onClickListener(View target) {
    	switch(target.getId()) {
    		case R.id.home_scan_button:
    			break;
    		case R.id.home_map_button:
    			break;
    		case R.id.home_profiles_button:
    			break;
    		case R.id.home_settings_button:
    			break;
    		default:
    			break;
    	}
    }
}
