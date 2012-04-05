package com.rebelware.droid.hotspotapp.util;

import java.lang.reflect.Method;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HotspotManager
{
	private static String TAG = "HotspotManager";
	
	public static final int WIFI_AP_STATE_UNKNOWN = -1;
	public static final int WIFI_AP_STATE_DISABLING = 0;
	public static final int WIFI_AP_STATE_DISABLED = 1;
	public static final int WIFI_AP_STATE_ENABLING = 2;
	public static final int WIFI_AP_STATE_ENABLED = 3;
	public static final int WIFI_AP_STATE_FAILED = 4;
    
    private final String[] WIFI_STATE_TEXTSTATE = new String[] { "DISABLING","DISABLED","ENABLING","ENABLED","FAILED" };
    
	final private WifiManager mService;
	
	public HotspotManager(Context context)
	{
		mService = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}
	/**
	 * Start AccessPoint mode with the specified configuration. If the radio is
	 * already running in AP mode, update the new configuration Note that
	 * starting in access point mode disables station mode operation
	 * 
	 * @param wifiConfig
	 *            SSID, security and channel details as part of
	 *            WifiConfiguration
	 * @return {@code true} if the operation succeeds, {@code false} otherwise
	 * 
	 * @hide Dont open up yet
	 */
	public boolean setWifiApEnabled(WifiConfiguration wifiConfig, boolean enabled)
	{
		try
		{
			Method method;
			method = mService.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
			return (Boolean) method.invoke(mService, wifiConfig, enabled);	
		} 
		catch (Exception e)
		{
			Log.e(TAG, "setWifiApEnabled failure", e);	
		} 	
		
		return false;
	}
	
	public void setWifiApEnabled(final WifiConfiguration wifiConfig, final boolean enabled, final Handler handler)
	{		
			final boolean wifiEnabled = mService.isWifiEnabled();
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					if (wifiEnabled)
					{
						mService.setWifiEnabled(false);
						while(mService.isWifiEnabled())
						{
							try
							{
								Log.d(TAG, "waiting for wifi to disable ...");
								Thread.sleep(50);								
							} 
							catch (InterruptedException e)
							{
								Log.w(TAG, "disabling wifi thread", e);
							}
						}
					}
										
					try
					{
						Method method;
						method = mService.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
						Boolean result = (Boolean) method.invoke(mService, wifiConfig, enabled);
						if (result)
						{
							for(int i = 0; (getWifiApState() != 13) && i < 25; i++)
							{
								Log.d(TAG, "waiting for AP to enable ...");
								Thread.sleep(200);								
							}
							result = isWifiApEnabled();
						}
						
						Message msg = Message.obtain();
						msg.obj = result;
						handler.sendMessage(msg);
					} 
					catch (Exception e)
					{
						Log.e(TAG, "setWifiApEnabled failure", e);						
					} 							
				}				
			}).start();			
	}

	/**
	 * Gets the Wi-Fi enabled state.
	 * 
	 * @return One of {@link #WIFI_AP_STATE_DISABLED},
	 *         {@link #WIFI_AP_STATE_DISABLING}, {@link #WIFI_AP_STATE_ENABLED},
	 *         {@link #WIFI_AP_STATE_ENABLING}, {@link #WIFI_AP_STATE_FAILED}
	 * @see #isWifiApEnabled()
	 * 
	 * @hide Dont open yet
	 */
	public int getWifiApState()
	{
		try
		{
			Method method = mService.getClass().getMethod("getWifiApState");
			return (Integer) method.invoke(mService);
		} 
		catch (Exception e)
		{
			Log.e(TAG, "getWifiAPState failed", e);
			return WIFI_AP_STATE_FAILED;
		}
	}

	/**
	 * Return whether Wi-Fi AP is enabled or disabled.
	 * 
	 * @return {@code true} if Wi-Fi AP is enabled
	 * @see #getWifiApState()
	 * 
	 * @hide Dont open yet
	 */
	public boolean isWifiApEnabled()
	{
		//return getWifiApState() == WIFI_AP_STATE_ENABLED || getWifiApState() == WIFI_AP_STATE_ENABLING;
		return getWifiApState() == WIFI_AP_STATE_ENABLED || getWifiApState() == WIFI_AP_STATE_ENABLING || getWifiApState() == 13;
	}

	/**
	 * Gets the Wi-Fi AP Configuration.
	 * 
	 * @return AP details in WifiConfiguration
	 * 
	 * @hide Dont open yet
	 */
	public WifiConfiguration getWifiApConfiguration()
	{
		try
		{			
			Method method = mService.getClass().getMethod("getWifiApConfiguration");
			return (WifiConfiguration) method.invoke(mService);
		} 
		catch (Exception e)
		{
			Log.e(TAG, "getWifiApConfiguration failed", e);
			return null;
		}
	}

	/**
	 * Sets the Wi-Fi AP Configuration.
	 * 
	 * @return {@code true} if the operation succeeded, {@code false} otherwise
	 * 
	 * @hide Dont open yet
	 */
	public boolean setWifiApConfiguration(WifiConfiguration wifiConfig)
	{
		try
		{	
			Method method = mService.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
			return (Boolean) method.invoke(mService, wifiConfig);
		} 
		catch (Exception e)
		{
			Log.e(TAG, "setWifiApConfiguration failed", e);
			return false;
		}
	}
	
	public String getStateString(Integer errorCode)
	{
		Log.d(TAG, "getStateString code:" + errorCode);
		if (errorCode > WIFI_STATE_TEXTSTATE.length)
			return "UNKNOWN";
		
		return WIFI_STATE_TEXTSTATE[errorCode];
	}
}
