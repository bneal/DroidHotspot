package com.rebelware.droid.hotspotapp;

import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.rebelware.droid.hotspotapp.util.HotspotManager;
import com.rebelware.droid.hotspotapp.util.Preferences;

public class DroidHotspotActivity extends FragmentActivity
{
	final static String TAG = "hotspotapp";
	
	final static String INITIALIZED = "initialized";
	
	private WifiManager wifi;
	private HotspotManager hotspotManager;
	
	private ImageView statusImage;
	
	private MenuItem actionMenuItem;
	private SettingsFragment settingsFragment = SettingsFragment.newInstance(null);
	
	private boolean isApEnabled = false;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		setupViews();
		setupData();		
	}
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		Log.d(TAG, "onCreateOptionsMenu");
		/*
		actionMenuItem = menu.add("toggle");
		
		actionMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		actionMenuItem.setIcon(getResources().getDrawable(R.drawable.wifi_off24));
		*/
		menu.add("Settings");
		menu.add("Help");
		menu.add("About");
		
		updateActionIcon();
		
		return super.onCreateOptionsMenu(menu);
	}
	
	protected void onStart ()
	{
		Log.d(TAG, "onStart");
		super.onStart();
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{	
		if (item.getTitle() == "toggle")
		{
			Log.d(TAG, "WifiAPState enabled: [" + isApEnabled + "] current state: " + hotspotManager.getStateString(hotspotManager.getWifiApState()));
			enableAccessPoint(!isApEnabled);
		}
		else if (item.getTitle() == "Settings")
		{
			Intent i = new Intent(this, SettingsActivity.class);
			startActivity(i);
		}
		else
			Toast.makeText(this, "selected: " + item.getTitle(), Toast.LENGTH_SHORT).show();
		
		return super.onOptionsItemSelected(item);
	}
	
	private void setupViews()
	{
		statusImage = (ImageView) this.findViewById(R.id.status_image);
		
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();		
		ft.replace(R.id.fragment_view, (android.support.v4.app.Fragment)settingsFragment);		
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
		
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		statusImage.setOnClickListener(new OnClickListener()
		{
			public void onClick(View arg0)
			{
				enableAccessPoint(!isApEnabled);
			}			
		});
		
	}

	private void setupData()
	{
		wifi = (WifiManager)this.getSystemService(WIFI_SERVICE);
		
		if (wifi != null)
		{
			setupPreferences();
			hotspotManager = new HotspotManager(this);
			final Integer apState = hotspotManager.getWifiApState();
			if (hotspotManager != null)
			{
				isApEnabled = hotspotManager.isWifiApEnabled();
				Log.d(TAG, "setupData -> wifiApState: " + apState);
			}
			Log.d(TAG, "setupData -> done setting wifi state:" + wifi.getWifiState());
			
			updateActionIcon();
			
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					Log.d(TAG, "started loading thread ...");
					
					boolean enabled = wifi.isWifiEnabled();
					
					if (!enabled && !isApEnabled)
					{
						wifi.setWifiEnabled(true);
						while(!wifi.isWifiEnabled())
						{
							try
							{
								Log.d(TAG, "waiting for wifi to enable ...");
								Thread.sleep(100);								
							} 
							catch (InterruptedException e)
							{
								Log.w(TAG, "loading thread", e);
							}
						}
					}
					
					loadConfigurations();					
					Log.d(TAG, "ending loading thread");
				}				
			}).start();
		}			
		
	}
	
	private void enableAccessPoint(boolean enable)
	{
		Log.d(TAG, "enableAccessPoint: " + enable);
		
		Bundle bundle = settingsFragment.getConfig();
		Log.d(TAG, "config:" + bundle.toString());
		
		if (bundle.size() > 0)
		{
			Log.d(TAG, "enableAccessPoint: settings: " + bundle);
		}
		
		WifiConfiguration config = new WifiConfiguration();
		config.SSID = "MyWifiTether";
		config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
		config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		config.preSharedKey = "superman";
		
		if (hotspotManager != null && wifi != null)
		{
			String message = "Starting ...";
			
			if (!enable)
				message = "Stopping ...";
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
			
			hotspotManager.setWifiApEnabled(config, enable, new Handler()
			{
				public void handleMessage(Message msg)
				{
					Log.d(TAG, "enableAccessPoint -> done setting AP state:" + hotspotManager.getWifiApState());
					Log.d(TAG, "enableAccessPoint -> done setting wifi state:" + wifi.getWifiState());
					if (msg != null && msg.obj == Boolean.FALSE)
					{
						Log.e(TAG, "failure enabling hotspot code:" + hotspotManager.getWifiApState() + " msg:" + hotspotManager.getStateString(hotspotManager.getWifiApState()));						
					}
					
					WifiInfo wifiInfo = wifi.getConnectionInfo();
					Log.d(TAG, "connectInfo: " + wifiInfo.toString());
					
					isApEnabled = hotspotManager.isWifiApEnabled();
					updateActionIcon();
				}				
			});
		}	
		else 
			Toast.makeText(this, "wifi not enabled", Toast.LENGTH_SHORT).show();
	}
	
	private void setupPreferences()
	{		
		Log.d(TAG, "setupPreferences");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (!prefs.getBoolean(INITIALIZED, false))
		{			
			Editor editor = prefs.edit();
			editor.putBoolean(INITIALIZED, true);
			editor.putString(getResources().getString(R.string.settings_ssid), "MyWifiTether");
			editor.putString(getResources().getString(R.string.settings_passphrase), "superman");
			editor.commit();
		}
		
		String ssid = prefs.getString(getResources().getString(R.string.settings_ssid), "");
		String passphrase = prefs.getString(getResources().getString(R.string.settings_passphrase), "");		
		
		if (settingsFragment != null)
		{
			Log.d(TAG, "loading preferences");
			Bundle bundle = new Bundle();
			bundle.putString("ssid", ssid);
			bundle.putString("passphrase", passphrase);
			settingsFragment.setConfig(bundle);
		}	
	}
	
	private void loadConfigurations()
	{
		Log.d(TAG, "loadConfigurations");
		
		Log.d(TAG, "WifiState:" + hotspotManager.getStateString(wifi.getWifiState()));
		Log.d(TAG, "WifiApState:" + hotspotManager.getStateString(hotspotManager.getWifiApState()));
		
		List<WifiConfiguration> configs = wifi.getConfiguredNetworks();
		if (configs != null)
		{
			StringBuilder sb = new StringBuilder();
			for (WifiConfiguration c : configs)
			{
				Log.d(TAG, "loadConfigurations -> SSID: [" + c.SSID + "] status:" + c.status);
				sb.append("config:").append(c.toString()).append("\n\n");				
			}
			
			Message msg = Message.obtain();
			msg.obj = sb.toString();
			handler.sendMessage(msg);
		}
	}
	
	private void updateActionIcon()
	{
		Log.d(TAG, "updateActionIcon isApEnabled:" + isApEnabled);
		
		if (statusImage != null)
		{
			Log.d(TAG, "updateActionIcon -> updating actionMenu");
			if (isApEnabled)
			{
				//actionMenuItem.setIcon(getResources().getDrawable(R.drawable.wifi_on24));
				statusImage.setImageDrawable(getResources().getDrawable(R.drawable.wifi_on96));
			}
			else
			{
				//actionMenuItem.setIcon(getResources().getDrawable(R.drawable.wifi_off24));
				statusImage.setImageDrawable(getResources().getDrawable(R.drawable.wifi_off96));
			}
		}
		else
			Log.d(TAG, "updateActionIcon -> actionMenu is null");
	}
	
	private Handler handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if (settingsFragment != null)
				settingsFragment.setStatusText((String)msg.obj);
			
			updateActionIcon();
		}
	};
}