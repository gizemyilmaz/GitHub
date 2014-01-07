package com.gsm;

import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class GSMInformationsActivity extends Activity {

	TelephonyManager Tel;
	MyPhoneStateListener MyListener;
	ProgressBar progressBar1;
	ProgressBar progressBar2;
	ProgressBar progressBar3;
	TextView textView1;
	TextView textView2;
	TextView textView3;
	TextView textView4;
	TextView textView5;
	TextView textView6;
	TextView textView7;
	TextView textView8;
	TextView textView9;
	TextView textView10;
	SQLiteDatabase sldGSM;
	int iSgnStg, iSgnStgDbm, iNoise, iLat = 0, iLang = 0, iBatt;
	String strModel;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		progressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
		progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
		progressBar3 = (ProgressBar) findViewById(R.id.progressBar3);
		textView1 = (TextView) findViewById(R.id.textView1);
		textView2 = (TextView) findViewById(R.id.textView2);
		textView3 = (TextView) findViewById(R.id.textView3);
		textView4 = (TextView) findViewById(R.id.textView4);
		textView5 = (TextView) findViewById(R.id.textView5);
		textView6 = (TextView) findViewById(R.id.textView6);
		textView7 = (TextView) findViewById(R.id.textView7);
		textView8 = (TextView) findViewById(R.id.textView8);

		AddToDatabase atdGSMInfs = new AddToDatabase(getApplicationContext(),
				"GSM", null, 1);

		sldGSM = atdGSMInfs.getWritableDatabase();

		MyListener = new MyPhoneStateListener();
		Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		final LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		final LocationListener mlocListener = new MyLocationListener();

		((Button) findViewById(R.id.btnGetGSMInfs))
				.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {

						Tel.listen(MyListener,
								PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

						mlocManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER, 0, 0,
								mlocListener);

						GSMInformationsActivity.this
								.registerReceiver(
										GSMInformationsActivity.this.mBatInfoReceiver,
										new IntentFilter(
												Intent.ACTION_BATTERY_CHANGED));

						ContentValues cvTmp = new ContentValues();
						cvTmp.put("signal_strength", iSgnStg);
						cvTmp.put("signal_strength_dbm", iSgnStgDbm);
						cvTmp.put("noise", iNoise);
						cvTmp.put("latitude", iLat);
						cvTmp.put("langitude", iLang);
						cvTmp.put("model", strModel);
						cvTmp.put("battery", iBatt);
						cvTmp.put("process_date", new Date().toString());

						sldGSM.insert("gsm_information", null, cvTmp);

					}

				});

		((Button) findViewById(R.id.btnDispGoogleMaps))
				.setOnClickListener(new OnClickListener() {

					public void onClick(View v) {

						if ((iLat > 0) && (iLang > 0)) {

							Intent intGetMap = new Intent(Intent.ACTION_VIEW,
									Uri.parse("geo:" + String.valueOf(iLat)
											+ "," + String.valueOf(iLang)
											+ "?z=15"));

							startActivity(intGetMap);
						}
						else
						{
							
							Intent intGetMap = new Intent(Intent.ACTION_VIEW,
									Uri.parse("geo:" + String.valueOf(39.939365)
											+ "," + String.valueOf(32.855127)
											+ "?z=15"));

							startActivity(intGetMap);
							
						}

					}
				});

	}

	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {

			int level = intent.getIntExtra("level", 0);
			textView7.setText(String.valueOf(level) + "%");

			iBatt = level;

		}

	};

	private class MyPhoneStateListener extends PhoneStateListener {

		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);

			if (signalStrength.getCdmaDbm() > -30) {

				textView2.setTextColor(Color.GREEN);

			} else if (signalStrength.getCdmaDbm() > -81) {

				textView2.setTextColor(Color.YELLOW);

			} else if (signalStrength.getCdmaDbm() > -91) {

				textView2.setTextColor(Color.RED);

			}

			textView6.setText(android.os.Build.MODEL + " -"
					+ android.os.Build.VERSION.RELEASE);

			strModel = textView6.getText().toString();

			if (Tel.getNetworkType() == TelephonyManager.NETWORK_TYPE_EDGE) {

				textView7.setText("3G");

			} else if (Tel.getNetworkType() == TelephonyManager.NETWORK_TYPE_UNKNOWN) {

				textView7.setText("3G");

			}
			
			textView8.setText(Tel.getNetworkOperatorName());
			
			GsmCellLocation cl = (GsmCellLocation)Tel.getCellLocation();
			
			try {
				textView8.setText(textView8.getText() + " - " + String.valueOf(cl.getCid()));
			} catch (Exception e) {
				textView8.setText(textView8.getText()+" - " +"Deðer Alýnamadý");
			}
			

			progressBar1.incrementProgressBy(signalStrength
					.getGsmSignalStrength());
			textView1.setText(String.valueOf(signalStrength
					.getGsmSignalStrength()));

			iSgnStg = signalStrength.getGsmSignalStrength();

			progressBar2.incrementProgressBy(100 - signalStrength.getCdmaDbm());
			textView2.setText(String.valueOf(signalStrength.getCdmaDbm())
					+ " dBm");

			iSgnStgDbm = signalStrength.getCdmaDbm();

			progressBar3.incrementProgressBy(signalStrength.getEvdoSnr());
			textView3.setText(String.valueOf(signalStrength.getEvdoSnr()));

			iNoise = signalStrength.getEvdoSnr();

		}

	}

	private class MyLocationListener implements LocationListener {

		public void onLocationChanged(Location location) {

			textView4.setText(String.valueOf(location.getLatitude()) + " -"
					+ String.valueOf(location.getLongitude()));

			iLat = (int) location.getLatitude();
			iLang = (int) location.getLongitude();

		}

		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

	}

}