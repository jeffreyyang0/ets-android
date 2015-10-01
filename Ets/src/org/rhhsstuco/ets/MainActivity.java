package org.rhhsstuco.ets;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public static final int QR_LOGIN = 1;
	public static final int QR_SELL_TICKET = 2;
	public static final int QR_CHECK_IN = 3;
	public static final int QR_CHECK_TKT = 4;
	
	// public static final String HOST = "https://rhhs-carnival.appspot.com/";
	public static final String HOST = "https://rhhsstuco-org.appspot.com/";
	
	// final static MediaPlayer mp = new MediaPlayer();
	
	protected int qr_task;
	protected int prev_qr_task;
	
	protected String user_id;
	protected String user_name;
	protected boolean isLoggedIn;
	
	TextView welcometext;
	
	static String http_url;
	static String http_result;
	
	Button b_sell;
	Button b_test;
	Button b_login;
	Button b_checkin;
	Button b_checkst;
	Button b_logoff;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		qr_task = 0;
		isLoggedIn = false;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		/*
		try{
			AssetFileDescriptor descriptor = MainActivity.this.getAssets().openFd("scanner beep.mp3");
			mp.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
			descriptor.close();
		} catch(Exception e){
			e.printStackTrace();
		}
		
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 20, 0);
		*/

		welcometext = (TextView) findViewById(R.id.textView_welcome);
		
		b_sell = (Button) findViewById(R.id.button_sell);
		b_sell.setOnClickListener(new OnClickListener() {
			public void onClick (View view)
			{
				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				qr_task = QR_SELL_TICKET;
				startActivityForResult(intent, 0);
			}
		});
		
		/*
		b_test = (Button) findViewById(R.id.buttontest);
		b_test.setOnClickListener(new OnClickListener() {
			public void onClick (View view)
			{
				// test stuff
				http_url = HOST + "get_status?ticket_id=e8d7cd0aa0e76e744cb08e336e220377fb30e8771b2686082a4139d5";
				prev_qr_task = QR_CHECK_IN;
				new TicketCmd().execute(new Void[0]);
			}
		});*/
		
		b_login = (Button) findViewById(R.id.button_signon);
		b_login.setOnClickListener(new OnClickListener() {
			public void onClick (View view)
			{
				// playBeep();
				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				qr_task = QR_LOGIN;
				startActivityForResult(intent, 0);
			}
		});
		
		b_checkin = (Button) findViewById(R.id.button_checkin);
		b_checkin.setOnClickListener(new OnClickListener() {
			public void onClick (View view)
			{
				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				qr_task = QR_CHECK_IN;
				startActivityForResult(intent, 0);
			}
		});
		
		b_checkst = (Button) findViewById(R.id.button_checktinfo);
		b_checkst.setOnClickListener(new OnClickListener() {
			public void onClick (View view)
			{
				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
				qr_task = QR_CHECK_TKT;
				startActivityForResult(intent, 0);
			}
		});
		
		b_logoff = (Button) findViewById(R.id.button_logoff);
		b_logoff.setOnClickListener(new OnClickListener() {
			public void onClick (View view)
			{
				b_logoff.setEnabled(false);
				b_login.setEnabled(true);
				b_checkin.setEnabled(false);
				b_checkst.setEnabled(false);
				b_sell.setEnabled(false);
				welcometext.setText("Hello, guest!");
				isLoggedIn = false;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	protected class LoginThread extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... urls) {
			HttpsURLConnection urlConnection = null;
			try {
				URL url = new URL(http_url);
				urlConnection = (HttpsURLConnection) url.openConnection();
				InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				BufferedReader r = new BufferedReader(new InputStreamReader(in));

				http_result = r.readLine();
			} catch (Exception e) {
				http_result = "Error";
			} finally {
				if (urlConnection != null)
					urlConnection.disconnect();
			}
			return null;
		}

		protected void onPostExecute(Void __R) {
			String result = http_result;

			if (result.length() > 7 && result.substring(0, 7).equals("code42:")) {
				user_name = result.substring(7);
				welcometext.setText("Hello, " + user_name + "!");
				Toast.makeText(getApplicationContext(), "Hello, " + user_name + "!", Toast.LENGTH_SHORT).show();
				isLoggedIn = true;
				b_sell.setEnabled(true);
				b_login.setEnabled(false);
				b_logoff.setEnabled(true);
				b_checkst.setEnabled(true);
				b_checkin.setEnabled(true);
			} else {
				Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
				isLoggedIn = false;
			}
		}
	}
	
	protected class TicketCmd extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... urls) {
			HttpsURLConnection urlConnection = null;
			try {
				URL url = new URL(http_url);
				urlConnection = (HttpsURLConnection) url.openConnection();
				InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				BufferedReader r = new BufferedReader(new InputStreamReader(in));

				http_result = r.readLine();
			} catch (Exception e) {
				http_result = "Error";
			} finally {
				if (urlConnection != null)
					urlConnection.disconnect();
			}
			return null;
		}

		protected void onPostExecute(Void __R) {
			String result = http_result;

			if (result.substring(0, 7).equals("code42:")) {
				int comma1 = result.indexOf(',');
				int comma2 = result.indexOf(',', comma1+1);
				String guestname = result.substring(7, comma1);
				String guestgrade = result.substring(comma1+1, comma2);
				String guesttid = result.substring(comma2+1);
				
				if (prev_qr_task == QR_CHECK_IN) {
					new AlertDialog.Builder(MainActivity.this)
				      .setMessage("Guest: " + guestname + "\n"
				    		  	+ "Grade: " + guestgrade + "\n"
				    		  	+ "Ticket ID: " + guesttid)
				      .setTitle("Checked-In Successfully")
				      .setCancelable(false)
				      .setPositiveButton("OK", new DialogInterface.OnClickListener() {  
				    	  public void onClick(DialogInterface dialog, int which) {  
				    		  dialog.dismiss();
				    	  }  
				      })
				      .setNeutralButton("Check-in another", new DialogInterface.OnClickListener() {  
				    	  public void onClick(DialogInterface dialog, int which) {  
				    		  dialog.dismiss();
				    		  b_checkin.performClick();
				    	  }  
				      })
				      .show();
				} else if (prev_qr_task == QR_CHECK_TKT) {
					new AlertDialog.Builder(MainActivity.this)
				      .setMessage("Sold to: " + guestname + "\n"
				    		  	+ "Grade: " + guestgrade + "\n"
				    		  	+ "Ticket ID: " + guesttid)
				      .setTitle("Ticket Information")
				      .setCancelable(false)
				      .setPositiveButton("OK", new DialogInterface.OnClickListener() {  
				    	  public void onClick(DialogInterface dialog, int which) {  
				    		  dialog.dismiss();
				    	  }  
				      })
				      .show();
				}
			} else {
				new AlertDialog.Builder(MainActivity.this)
			      .setMessage(result)
			      .setTitle("Error")
			      .setCancelable(false)
			      .setPositiveButton("OK", new DialogInterface.OnClickListener() {  
			    	  public void onClick(DialogInterface dialog, int which) {  
			    		  dialog.dismiss();
			    	  }  
			      })
			      .show();
			}
		}
	}

	/*
	public void playBeep()
	{
		try {
			mp.stop();
			mp.prepare();
			mp.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
	public void performLogin(String code)
	{
		http_url = HOST + "auth_user?user_id=" + code;
		user_id = code;
		new LoginThread().execute(new Void[0]);
	}
	
	public void checkTicketStatus(String code)
	{
		http_url = HOST + "get_status?ticket_id=" + code;
		prev_qr_task = QR_CHECK_TKT;
		new TicketCmd().execute(new Void[0]);
	}
	
	public void performCheckIn(String code)
	{
		http_url = HOST + "checkin?ticket_id=" + code + "&user_id=";
		// Trim user id
		http_url += user_id.substring(user_id.indexOf(':')+1);
		prev_qr_task = QR_CHECK_IN;
		new TicketCmd().execute(new Void[0]);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		   if (requestCode == 0) {
		      if (resultCode == RESULT_OK) {
		         String contents = intent.getStringExtra("SCAN_RESULT");
		         String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
		         // Handle successful scan
		         
		         switch (qr_task) {
		         case QR_LOGIN:
		        	 performLogin(contents);
		        	 break;
		         case QR_SELL_TICKET:
		        	 Intent i = new Intent(MainActivity.this, SaleInfoActivity.class);
		        	 i.putExtra("tid", contents);
		        	 i.putExtra("seller", user_name);
		        	 i.putExtra("sellerid", user_id);
			         startActivityForResult(i, 10);
		        	 break;
		         case QR_CHECK_IN:
		        	 performCheckIn(contents);
		        	 break;
		         case QR_CHECK_TKT:
		        	 checkTicketStatus(contents);
		        	 break;
		         }
		         
		      } else if (resultCode == RESULT_CANCELED) {
//		         // Handle cancel
//		    	  Intent i = new Intent(MainActivity.this, SaleInfoActivity.class);
//			      startActivityForResult(i, 10);
		      }
		   }
		}

}
