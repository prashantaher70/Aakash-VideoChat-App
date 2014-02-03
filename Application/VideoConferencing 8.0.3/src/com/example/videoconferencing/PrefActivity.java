package com.example.videoconferencing;

import com.example.videoconferencing.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PrefActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener 
{
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		//add preferences from settings/xml file 
		addPreferencesFromResource(R.xml.preferences);
		
		//set the default preferences when app is first installed
		PreferenceManager.setDefaultValues(PrefActivity.this,R.xml.preferences, false);

		// Initialize the summaries displayed
		initSummaryForEditP(getPreferenceScreen().getPreference(0));
		initSummaryForListP(getPreferenceScreen().getPreference(1));
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() 
	{
		super.onPause();
		
		//Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) 
	{
		if(key.equalsIgnoreCase("serverIP"))
			initSummaryForEditP(findPreference(key));
		else
			initSummaryForListP(findPreference(key));
	}

	// initialize the summary displayed
	private void initSummaryForEditP(Preference p) 
	{
		EditTextPreference editTextPref = (EditTextPreference) p;
		p.setSummary(editTextPref.getText());
	}

	// initialize the summary displayed
	private void initSummaryForListP(Preference p) 
	{
		ListPreference listPreference = (ListPreference)p;
		p.setSummary(listPreference.getValue() + " Hz");
	}
}
