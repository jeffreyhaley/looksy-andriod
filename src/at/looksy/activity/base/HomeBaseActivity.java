package at.looksy.activity.base;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import at.looksy.R;
import at.looksy.activity.HomeActivity;
import at.looksy.activity.FavoriteLocationsActivity;
import at.looksy.activity.WifiListingActivity;


public abstract class HomeBaseActivity extends Activity {
	
	protected void initNavbar(final Activity activity) {
		
		final ImageView followIconView = (ImageView) findViewById(R.id.btnFollow);
		final ImageView logoIconView = (ImageView) findViewById(R.id.imgLogo);
		
		followIconView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, FavoriteLocationsActivity.class);
				startActivity(intent);
				
				if (activity instanceof HomeActivity)
					overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
			}
		});
		
		
		logoIconView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(activity, HomeActivity.class);
				startActivity(intent);
				
				if (activity instanceof FavoriteLocationsActivity)
					overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
			}
		});
		
		// reset the nav
		followIconView.setImageResource(R.drawable.ico_heart_off);
		logoIconView.setImageResource(R.drawable.app_logo);
		
		// then select which one is enabled		
		if (activity instanceof HomeActivity) {
			logoIconView.setImageResource(R.drawable.app_logo);
			
		} else if (activity instanceof FavoriteLocationsActivity) {
			followIconView.setImageResource(R.drawable.ico_heart_on);
			
		} else if (activity instanceof WifiListingActivity) {
			
		}
	}

}
