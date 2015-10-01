package org.rhhsstuco.ets;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	TextView statusText;
	Button b_scanqr, b_typeqr, b_typetid, b_undo;
	TextView textView_barcode;
	TextView textView_tid;
	
	protected static final String URLPREF = "http://ets.rhhsstuco.org/app.php?key=90j1x2i0gf30indjid238&";
	String http_url;
	String http_result;
	int curr_task;
	
	String lastGuest = null;
	String lastCode = null;
	
	protected static final int TASK_QRSCAN = 0;
	protected static final int TASK_QRENTER = 1;
	protected static final int TASK_TIDENTER = 2;
	protected static final int TASK_UNDOLAST = 3;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		statusText = (TextView) findViewById(R.id.textView_status);
		textView_barcode = (TextView) findViewById(R.id.editText_barcode);
		textView_tid = (TextView) findViewById(R.id.editText_tid);
		
		b_scanqr = (Button) findViewById(R.id.button_scannow);
		b_scanqr.setOnClickListener(new OnClickListener() {
			public void onClick (View view)
			{
				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
				intent.putExtra("SCAN_MODE", "QR_CODE_MODE");

				curr_task = TASK_QRSCAN;
				startActivityForResult(intent, 0);
			}
		});
		
		b_typeqr = (Button) findViewById(R.id.button_barcodetyped);
		b_typeqr.setOnClickListener(new OnClickListener() {
			public void onClick (View view)
			{
				String code = textView_barcode.getText().toString().trim();
				curr_task = TASK_QRENTER;
				performCheckInByCode(code);
			}
		});
		
		b_typetid = (Button) findViewById(R.id.button_tidtyped);
		b_typetid.setOnClickListener(new OnClickListener() {
			public void onClick (View view)
			{
				String tid = textView_tid.getText().toString().trim();
				performCheckInByTid(tid);
			}
		});
		
		b_undo = (Button) findViewById(R.id.button_unchecklast);
		b_undo.setOnClickListener(new OnClickListener() {
			public void onClick (View view)
			{
				performCheckOutByCode();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void LockUI() {
		statusText.setText("Status: Waiting for server...");
		b_scanqr.setEnabled(false);
		b_typeqr.setEnabled(false);
		b_typetid.setEnabled(false);
		b_undo.setEnabled(false);
	}
	
	public void UnlockUI() {
		statusText.setText("Status: Ready");
		b_scanqr.setEnabled(true);
		b_typeqr.setEnabled(true);
		b_typetid.setEnabled(true);
		if (lastCode != null)
			b_undo.setEnabled(true);
		textView_barcode.setText("");
		textView_tid.setText("");
	}
	
	public void performCheckInByCode (String code) {
		LockUI();
		http_url = URLPREF + "barcode=" + code;
		new TicketCmd().execute(new Void[0]);
	}
	
	public void performCheckInByTid (String id) {
		LockUI();
		http_url = URLPREF + "tid=" + id;
		curr_task = TASK_TIDENTER;
		new TicketCmd().execute(new Void[0]);
	}
	
	public void performCheckOutByCode () {
		if (lastCode != null) {
			new AlertDialog.Builder(MainActivity.this)
			.setMessage("Confirm undo check-in for " + lastGuest + "?")
			.setCancelable(false)
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {  
				public void onClick(DialogInterface dialog, int which) {
					LockUI();
					http_url = URLPREF + "barcode_uncheckin=" + lastCode;
					curr_task = TASK_UNDOLAST;
					new TicketCmd().execute(new Void[0]);
					dialog.dismiss();
				}  
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {  
				public void onClick(DialogInterface dialog, int which) {  
					dialog.dismiss();
				}  
			})
			.show();
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				// Handle successful scan
				if (curr_task == TASK_QRSCAN) {
					performCheckInByCode(contents);
				}
			} else if (resultCode == RESULT_CANCELED) {
				// Do nothing
			}
		}
	}
	
	public void PopMessageBox (String text) {
		new AlertDialog.Builder(MainActivity.this)
		.setMessage(text)
		.setTitle("Server Response")
		.setCancelable(false)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() {  
			public void onClick(DialogInterface dialog, int which) {  
				dialog.dismiss();
			}
		})
		.show();
	}

	protected class TicketCmd extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... urls) {
			HttpURLConnection urlConnection = null;
			try {
				URL url = new URL(http_url);
				urlConnection = (HttpURLConnection) url.openConnection();
				InputStream in = new BufferedInputStream(urlConnection.getInputStream());
				BufferedReader r = new BufferedReader(new InputStreamReader(in));

				Scanner s = new Scanner(r);
				http_result = "";
				while (s.hasNextLine())
					http_result += s.nextLine() + "\n";
			} catch (Exception e) {
				http_result = "Error";
			} finally {
				if (urlConnection != null)
					urlConnection.disconnect();
			}
			return null;
		}

		protected void onPostExecute(Void __R) {
			Scanner result = new Scanner(http_result);
			if (result.hasNextInt()) {
				int rcode = result.nextInt();
				result.nextLine(); // Move onto line after number
				
				if (rcode == 1) {
					PopMessageBox(result.nextLine());
				} else if (rcode == 0) {
					lastGuest = result.nextLine();
					lastCode = result.nextLine();
					PopMessageBox(result.nextLine());
				} else {
					PopMessageBox(result.nextLine());
				}
			} else {
				PopMessageBox(result.nextLine());
			}
			UnlockUI();
			//if (curr_task == TASK_QRSCAN)
			//	b_scanqr.performClick();
		}
	}
}
