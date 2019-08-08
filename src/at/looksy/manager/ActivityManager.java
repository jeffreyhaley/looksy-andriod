package at.looksy.manager;

import java.util.ArrayDeque;
import java.util.Deque;

import android.util.Log;
import at.looksy.BuildConfig;
import at.looksy.activity.HomeActivity;
import at.looksy.core.Constants;

public class ActivityManager {

	private Deque<Class<?>> activityStack = null;
	private static ActivityManager instance = null;

	private ActivityManager() {
		if (activityStack == null)
			activityStack = new ArrayDeque<Class<?>>();
	}

	public void pushMx(Class<?> clazz) {
		activityStack.push(clazz);
	}

	public Class<?> pop2x() {
		Class<?> returnClazz = HomeActivity.class;
		
		try {
			@SuppressWarnings("unused")
			Class<?> clazz1 = activityStack.pop();
			Class<?> clazz2 = activityStack.pop();
			if (clazz2 != null)
				returnClazz = clazz2;
			
		} catch (Exception e) {
			if (BuildConfig.DEBUG) {
				Log.d(Constants.DEBUG_TAG, "pop2 registered an exception");
			}
		}
		
		return returnClazz;
	}

	public Class<?> pop1() {
		try {
			Class<?> clazz = activityStack.pop();
			return clazz;
		} catch (Exception e) {
			return HomeActivity.class;
		}
	}

	public static ActivityManager getInstance()
	{
		if (instance == null)
			instance = new ActivityManager();
		return instance;
	}

}
