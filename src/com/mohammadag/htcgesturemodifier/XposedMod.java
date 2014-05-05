package com.mohammadag.htcgesturemodifier;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.KeyEvent;
import android.view.WindowManager;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedMod implements IXposedHookLoadPackage {

	private SettingsHelper mSettingsHelper;

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (!lpparam.packageName.equals("com.htc.sense.easyaccessservice"))
			return;

		Class<?> SensorHubService = 
				XposedHelpers.findClass("com.htc.sense.easyaccessservice.SensorHubService", lpparam.classLoader);

		XC_MethodHook onCreateDestroyHook = new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				if ("onDestroy".equals(param.method.getName())) {
					mSettingsHelper = new SettingsHelper();
					Service service = (Service) param.thisObject;
					Context context = service.getApplicationContext();
					if (mSettingsHelper != null) {
						mSettingsHelper.unregisterSettingsChangedListener(context);
						mSettingsHelper = null;
					}
				}
			}

			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				if ("onCreate".equals(param.method.getName())) {
					mSettingsHelper = new SettingsHelper();
					Service service = (Service) param.thisObject;
					Context context = service.getApplicationContext();
					mSettingsHelper.registerSettingsChangedListener(context);
				}
			}
		};

		XposedHelpers.findAndHookMethod(SensorHubService, "onCreate", onCreateDestroyHook);
		XposedHelpers.findAndHookMethod(SensorHubService, "onDestroy", onCreateDestroyHook);
		XposedHelpers.findAndHookMethod(SensorHubService, "triggerLockscreenAction", int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				Service service = (Service) param.thisObject;
				Context context = service.getApplicationContext();
				int gesture = (Integer) param.args[0];
				String overridenAction = mSettingsHelper.getActionForGesture(gesture);
				if (SettingsHelper.DEFAULT_ACTION.equals(overridenAction))
					return;

				performAction(overridenAction, context);
				if (!overridenAction.equals(SettingsHelper.VOICE_SEARCH_ACTION)) {
					XposedHelpers.callMethod(param.thisObject, "onScreenOff");
				}
				param.setResult(null);
			}
		});
	}

	private void performAction(String action, Context context) {
		if (SettingsHelper.DEFAULT_ACTION.equals(action))
			return;

		if (SettingsHelper.NEXT_SONG_ACTION.equals(action)) {
			sendMediaButton(context, KeyEvent.KEYCODE_MEDIA_NEXT);

			wakeUpDevice(context);
		} else if (SettingsHelper.PREV_SONG_ACTION.equals(action)) {
			sendMediaButton(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS);

			wakeUpDevice(context);
		} else if (SettingsHelper.PLAY_PAUSE_ACTION.equals(action)) {
			sendMediaButton(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);

			wakeUpDevice(context);
		} else if (SettingsHelper.VOICE_SEARCH_ACTION.equals(action)) {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.setClassName("com.google.android.googlequicksearchbox",
					"com.google.android.googlequicksearchbox.VoiceSearchActivity");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
					WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
					WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
			try {
				context.startActivity(intent);
			} catch (ActivityNotFoundException anfe) {
				XposedBridge.log("Google Voice Search is not found");
			}  
		}
	}

	private void wakeUpDevice(Context context) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		@SuppressWarnings("deprecation")
		WakeLock wakelock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
				PowerManager.ACQUIRE_CAUSES_WAKEUP, 
				"HtcGestureModifier");

		wakelock.acquire();

		wakelock.release();
	}

	private void sendMediaButton(Context context, int keyCode) {
		KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
		Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);

		context.sendOrderedBroadcast(intent, null);

		keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
		intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);

		context.sendOrderedBroadcast(intent, null);
	}        
}
