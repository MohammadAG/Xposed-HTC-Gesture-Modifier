package com.mohammadag.htcgesturemodifier;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {
	private static final String URL_MY_MODULES = "http://repo.xposed.info/users/mohammadag";
	private static final String URL_MY_APPS = "market://search?q=pub:Mohammad Abu-Garbeyyeh";

	@Override
	public SharedPreferences getSharedPreferences(String name, int mode) {
		return SettingsHelper.getWritablePreferences(getApplicationContext());
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		initCopyright();
	}

	@Override
	protected void onPause() {
		SettingsHelper.emitSettingsChanged(getApplicationContext());
		super.onPause();
	}

	@SuppressWarnings("deprecation")
	private void initCopyright() {
		Preference copyrightPreference = findPreference("copyright_key");
		copyrightPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
				builder.setTitle("")
				.setItems(R.array.my_apps, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Uri uri = null;
						Intent intent = new Intent(Intent.ACTION_VIEW);
						switch (which) {
						case 0:
							uri = Uri.parse(URL_MY_APPS);
							intent.setPackage("com.android.vending");
							break;
						case 1:
							uri = Uri.parse(URL_MY_MODULES);
							break;
						}
						try {
							startActivity(intent.setData(uri));
						} catch (ActivityNotFoundException e) {
							Toast.makeText(SettingsActivity.this, "Play Store not found", Toast.LENGTH_SHORT).show();
						}
					}
				});
				builder.create().show();
				return false;
			}
		});
	}
}
