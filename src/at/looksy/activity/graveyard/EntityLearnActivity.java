package at.looksy.activity.graveyard;

import at.looksy.R;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import at.looksy.activity.base.EntityBaseActivity;
import at.looksy.dataitem.LocationDataItem;
import at.looksy.factory.ViewFactory;
import at.looksy.listener.OnSwipeTouchListener;
import at.looksy.manager.ActivityManager;

@Deprecated
public class EntityLearnActivity extends EntityBaseActivity {
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_entity_about);
		
		initActivity();
	}
	
	@Override
	protected void initActivity()
	{		
		super.initActivity();
		
		ViewFactory layoutFactory = new ViewFactory(this);
		LinearLayout parent = (LinearLayout) findViewById(R.id.aboutSections);
		
		final LocationDataItem entityDataItem = (LocationDataItem) getData();
		
		// description
		if (entityDataItem != null && entityDataItem.getDescription() != null) {
			LinearLayout descriptionGroup = layoutFactory.getAboutSection();
			TextView heading = (TextView) descriptionGroup.findViewById(R.id.heading);
			heading.setText("About");
			
			TextView body = (TextView) descriptionGroup.findViewById(R.id.body);
			body.setText(entityDataItem.getDescription());
			
			parent.addView(descriptionGroup);
		}
		
		// back swipe listener
		ScrollView root = (ScrollView) findViewById(R.id.viewScroller);
		root.setOnTouchListener(new OnSwipeTouchListener() {
			public void onSwipeRight() {
				endView();
			}
		});	
	}

}
