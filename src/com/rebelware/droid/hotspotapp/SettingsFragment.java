package com.rebelware.droid.hotspotapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SettingsFragment extends Fragment
{
	private View view;
	
	private String ssid = "";
	private String passphrase = "";
	
	private TextView statusView;
	private EditText ssidView;
	private EditText passphraseView;
	private CheckBox broadcastCheckbox;
	
	public static SettingsFragment newInstance(Bundle bundle)
	{
		SettingsFragment fragment = new SettingsFragment();
		if (bundle != null)
		{
			fragment.setConfig(bundle);			
		}
		
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		view = inflater.inflate(R.layout.settings, container, false);
		return view;
	}
	
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		
		setupViews();
		setupData();
	}
	
	private void setupViews()
	{
		statusView = (TextView)view.findViewById(R.id.settings_status);
		ssidView = (EditText)view.findViewById(R.id.settings_ssid);
		passphraseView = (EditText)view.findViewById(R.id.settings_passphrase);
		
		CheckBox cb = (CheckBox)view.findViewById(R.id.settings_show_passphrase);
		cb.setOnCheckedChangeListener(new OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton button, boolean checked)
			{
				// TODO Auto-generated method stub
				if (checked)
					passphraseView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				else
					passphraseView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
			}			
		});
		
		broadcastCheckbox = (CheckBox)view.findViewById(R.id.settings_broadcast);		
	}
	
	private void setupData()
	{
		ssidView.setText(ssid);
		passphraseView.setText(passphrase);
	}
	
	public void setConfig(Bundle config)
	{			
		if (config != null)
		{
			ssid = config.getString("ssid", "");
			passphrase = config.getString("passphrase", "");
			//ssidView.setText();		
			//passphraseView.setText();		
			//broadcastCheckbox.setChecked(config.getBoolean("hide", false));
		}
	}
	
	public Bundle getConfig()
	{		
		if (ssidView.getText().toString().length() < 3 || passphraseView.getText().toString().length() < 8)
		{
			//Toast.makeText(view.getContext(), "invalid configuration", Toast.LENGTH_SHORT).show();
			return new Bundle();
		}
	
		Bundle bundle = new Bundle();
		
		bundle.putString("ssid", ssidView.getText().toString());
		bundle.putString("passphrase", passphraseView.getText().toString());
		bundle.putBoolean("hide", broadcastCheckbox.isChecked());
		
		return bundle;		
	}
	
	public void setStatusText(String text)
	{
		if (statusView != null)
			statusView.setText(text);
	}
}
