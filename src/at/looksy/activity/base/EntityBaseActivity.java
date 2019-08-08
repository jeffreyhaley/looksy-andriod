package at.looksy.activity.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import at.looksy.R;
import at.looksy.activity.graveyard.EntityConnectActivity;
import at.looksy.activity.graveyard.EntityExploreActivity;
import at.looksy.activity.graveyard.EntityLearnActivity;
import at.looksy.activity.graveyard.EntityProfileActivity;
import at.looksy.core.Constants;
import at.looksy.dataitem.DataItem;
import at.looksy.manager.ActivityManager;

@Deprecated
public abstract class EntityBaseActivity extends Activity {
	
	private DataItem data = null;
	
	protected void initActivity() {
		Bundle bundle = getIntent().getExtras();
		data = (DataItem) bundle.getSerializable(Constants.BUNDLE_DATA);
	}
	
	protected void endView()
	{
		Class<?> callerClass = ActivityManager.getInstance().pop1();
		while (callerClass.getName().equals(EntityConnectActivity.class.getName()) ||
				callerClass.getName().equals(EntityExploreActivity.class.getName()) ||
				callerClass.getName().equals(EntityLearnActivity.class.getName())) {
			callerClass = ActivityManager.getInstance().pop1();
		}
		
		Bundle bundle = new Bundle();
		bundle.putSerializable(Constants.BUNDLE_DATA, data);

		Intent intent = new Intent(this, callerClass);
		intent.putExtras(bundle);
		startActivity(intent);

		finish();
		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}
	
	public DataItem getData() {
		return data;
	}
	
	@Override
	public void onBackPressed()
	{
		((EntityProfileActivity)getParent()).setBackButtonPressed();
		endView();
	}
	
	@Override
	public void finish()
	{
		super.finish();
	}

}
