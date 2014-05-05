package com.mohammadag.htcgesturemodifier;

import java.util.Map;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import de.robv.android.xposed.XSharedPreferences;

public class SettingsHelper {

	private static final String PACKAGE_NAME = "com.mohammadag.htcgesturemodifier";
	private static String INTENT_SETTINGS_CHANGED = "com.mohammadag.htcgesturemodifier.SETTINGS_UPDATED";
	private static final String PREFS_NAME = PACKAGE_NAME + "_preferences";
	private BroadcastReceiver mBroadcastReceiver;
	private XSharedPreferences mXPreferences;
	private static SharedPreferences mPreferences;

	private Context mContext;

	public static final String DEFAULT_ACTION = "default";
	public static final String VOICE_SEARCH_ACTION = "voice_search";
	public static final String NEXT_SONG_ACTION = "next_song";
	public static final String PREV_SONG_ACTION = "prev_song";
	public static final String PLAY_PAUSE_ACTION = "play_pause";

	public static final String SWIPE_UP_KEY = "swipe_up_action";;
	public static final String SWIPE_LEFT_KEY = "swipe_left_action";
	public static final String SWIPE_RIGHT_KEY = "swipe_right_action";
	public static final String SWIPE_DOWN_KEY = "swipe_down_action";
	public static final String DOUBLE_TAP_KEY = "double_tap_action";
	public static final String CAMERA_KEY = "camera_action";

	public SettingsHelper() {
		mXPreferences = new XSharedPreferences(PACKAGE_NAME);
		mXPreferences.makeWorldReadable();

		reloadSettings();
	}

	public SettingsHelper(Context context) {
		mContext = context;
		mPreferences = getWritablePreferences(context);
	}

	public Context getContext() {
		return mContext;
	}

	public void reloadSettings() {
		mXPreferences.reload();
	}

	public static void emitSettingsChanged(Context context) {
		context.sendBroadcast(new Intent(INTENT_SETTINGS_CHANGED));
	}

	public Editor edit() {
		return mPreferences.edit();
	}

	@SuppressLint("WorldReadableFiles")
	@SuppressWarnings("deprecation")
	public static SharedPreferences getWritablePreferences(Context context) {
		if (mPreferences == null)
			mPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_READABLE);

		return mPreferences;
	}

	public void registerSettingsChangedListener(Context context) {
		mBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				reloadSettings();
			}
		};
		context.registerReceiver(mBroadcastReceiver, new IntentFilter(INTENT_SETTINGS_CHANGED));
	}

	public void unregisterSettingsChangedListener(Context context) {
		context.unregisterReceiver(mBroadcastReceiver);
		mBroadcastReceiver = null;
	}


	public String getString(String key, String defaultValue) {
		String returnResult = defaultValue;
		if (mPreferences != null) {
			returnResult = mPreferences.getString(key, defaultValue);
		} else if (mXPreferences != null) {
			returnResult = mXPreferences.getString(key, defaultValue);
		}
		return returnResult;
	}

	public Map<String, ?> getAll() {
		if (mPreferences != null) {
			return mPreferences.getAll();
		} else if (mXPreferences != null) {
			return mXPreferences.getAll();
		}

		return null;
	}

	public float getFloat(String key, float defaultValue) {
		float returnResult = defaultValue;
		if (mPreferences != null) {
			returnResult = mPreferences.getFloat(key, defaultValue);
		} else if (mXPreferences != null) {
			returnResult = mXPreferences.getFloat(key, defaultValue);
		}
		return returnResult;
	}

	public int getInt(String key, int defaultValue) {
		int returnResult = defaultValue;
		if (mPreferences != null) {
			returnResult = mPreferences.getInt(key, defaultValue);
		} else if (mXPreferences != null) {
			returnResult = mXPreferences.getInt(key, defaultValue);
		}
		return returnResult;
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		boolean returnResult = defaultValue;
		if (mPreferences != null) {
			returnResult = mPreferences.getBoolean(key, defaultValue);
		} else if (mXPreferences != null) {
			returnResult = mXPreferences.getBoolean(key, defaultValue);
		}
		return returnResult;
	}

	public Set<String> getStringSet(String key, Set<String> defaultValue) {
		Set<String> returnResult = defaultValue;
		if (mPreferences != null) {
			returnResult = mPreferences.getStringSet(key, defaultValue);
		} else if (mXPreferences != null) {
			returnResult = mXPreferences.getStringSet(key, defaultValue);
		}
		return returnResult;
	}

	public boolean contains(String key) {
		if (mPreferences != null)
			return mPreferences.contains(key);
		else if (mXPreferences != null)
			return mXPreferences.contains(key);

		return false;
	}

	public String getActionForGesture(int type) {
		switch (type) {
		case GestureType.BLINKFEED_ACTION:
			return getString(SWIPE_LEFT_KEY, DEFAULT_ACTION);
		case GestureType.HOMESCREEN_ACTION:
			return getString(SWIPE_RIGHT_KEY, DEFAULT_ACTION);
		case GestureType.QUICK_CALL_ACTION:
			return getString(SWIPE_DOWN_KEY, DEFAULT_ACTION);
		case GestureType.UNLOCK_ACTION:
			return getString(SWIPE_UP_KEY, DEFAULT_ACTION);
		case GestureType.CAMERA_ACTION:
			return getString(CAMERA_KEY, DEFAULT_ACTION);
		case GestureType.DOUBLE_TAP_ACTION:
			return getString(DOUBLE_TAP_KEY, DEFAULT_ACTION);
		default:
			return DEFAULT_ACTION;
		}
	}
}
