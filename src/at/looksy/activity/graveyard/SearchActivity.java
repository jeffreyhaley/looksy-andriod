package at.looksy.activity.graveyard;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import at.looksy.R;
import at.looksy.activity.base.HomeBaseActivity;
import at.looksy.core.Constants;
import at.looksy.dataitem.LocationDataItem;
import at.looksy.service.WebService;
import at.looksy.service.consumer.IWebServiceAsyncConsumer;
import at.looksy.service.data.WebServiceRequest;
import at.looksy.service.data.WebServiceResponse;
import at.looksy.service.data.WebServiceResponse.StatusCode;
import at.looksy.tile.SearchResultTile;
import at.looksy.util.DataItemUtil;

@Deprecated
public class SearchActivity extends HomeBaseActivity implements IWebServiceAsyncConsumer {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		initActivity();
	}
	
	private String currentSearchTerm = null;
	private String lastSearchTerm = null;
	
	private Timer searchTimer = null;
	private LinearLayout searchResultLayout = null;
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	private void initActivity()
	{
		// Initialize menu items
		initNavbar(this);

		// init ui components
		searchResultLayout = (LinearLayout) findViewById(R.id.searchResultList);

		// init search box
		EditText searchBox = (EditText) findViewById(R.id.searchBox);
		searchBox.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				currentSearchTerm = s.toString();
				progressBarOn();
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			@Override
			public void afterTextChanged(Editable s) { }
		});

		// timer setup
		searchTimer = new Timer(true);
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				
				if (lastSearchTerm != null && 
						currentSearchTerm != null && 
						lastSearchTerm.equalsIgnoreCase(currentSearchTerm)) {
					progressBarOff();
					return;
				}
				
				clearSearchResults();
				
				if (currentSearchTerm != null && !currentSearchTerm.isEmpty())
					new WebService(SearchActivity.this).findLocations(currentSearchTerm);
				else
					progressBarOff();
				
				lastSearchTerm = currentSearchTerm;
			}
		};
		searchTimer.scheduleAtFixedRate(task, 0, TimeUnit.SECONDS.toMillis(2));
	}
	
	// clear search results
	private void clearSearchResultsInternal() {
		searchResultLayout.removeAllViews();
	}
	private final Handler clearSearchResultsHandler = new Handler();
	public void clearSearchResults() {		
		clearSearchResultsHandler.post(clearSearchResultsRunnable);
	}
	final Runnable clearSearchResultsRunnable = new Runnable() {
		@Override
		public void run() {
			clearSearchResultsInternal();
		}
	};
	
	// progress bar ON
	private void progressBarOnInternal() {
		findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
	}
	private final Handler progressBarOnHandler = new Handler();
	public void progressBarOn() {		
		progressBarOnHandler.post(progressBarOnRunnable);
	}
	final Runnable progressBarOnRunnable = new Runnable() {
		@Override
		public void run() {
			progressBarOnInternal();
		}
	};
	
	// progress bar OFF
	private void progressBarOffInternal() {
		findViewById(R.id.progressBar).setVisibility(View.GONE);
	}
	private final Handler progressBarOffHandler = new Handler();
	public void progressBarOff() {		
		progressBarOffHandler.post(progressBarOffRunnable);
	}
	final Runnable progressBarOffRunnable = new Runnable() {
		@Override
		public void run() {
			progressBarOffInternal();
		}
	};

	protected void endView()
	{
//		Class<?> callingClass = ActivityManager.getInstance().pop2();
//
//		Intent intent = new Intent(this, callingClass);
//		startActivity(intent);

		finish();

		overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
	}

	@Override
	public void onBackPressed() {
		endView();
	}

	@Override
	public void consumeWSExchange(WebServiceRequest wsRequest,
			WebServiceResponse wsResponse) {

		if (!receiveWSCallback() || wsResponse == null)
			return;

		String method = wsRequest.getRequestAction();
		try {
			if (wsResponse.getStatus() == StatusCode.OK) {
				if (method.equals(Constants.JSON_METHOD_GET_LOCATIONS_FOR_SEARCH_TERM)) {
					
					List<LocationDataItem> dataList = 
							DataItemUtil.decodeJSONLocations(
									wsResponse.getJSONArray(Constants.WEB_SERVICE_DATA));
					
					LinearLayout searchResultLayout = 
							(LinearLayout) findViewById(R.id.searchResultList);
					searchResultLayout.removeAllViews();
					
					for (LocationDataItem item : dataList) {
						searchResultLayout.addView(
								new SearchResultTile(this, item).getLayout());
					}
					progressBarOff();
				}
			}
		} catch (JSONException e) {	e.printStackTrace(); }
	}

	@Override
	public boolean receiveWSCallback() {
		return !isFinishing();
	}
	
}
