package org.rhhsstuco.ets;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;

public class SaleInfoActivity extends Activity {

	String http_url;
	String http_result;
	
	String tid;
	String seller;
	String sellerid;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sale_info);
		// Show the Up button in the action bar.
		setupActionBar();
		
		Intent intent = getIntent();
		tid = intent.getStringExtra("tid");
		seller = intent.getStringExtra("seller");
		sellerid = intent.getStringExtra("sellerid");
		TextView tview = (TextView) findViewById(R.id.textView_tid);
		tview.setText("Ticket ID: " + tid.substring(0, 8));
		TextView snamev = (TextView) findViewById(R.id.textView_sname);
		snamev.setText(seller);
		
		Button submit = (Button) findViewById(R.id.buttonsubmit);
		submit.setOnClickListener(new OnClickListener() {
			public void onClick (View view)
			{
				Button submit = (Button) findViewById(R.id.buttonsubmit);
				TextView resulttext = (TextView) findViewById(R.id.submitrtext);
				EditText nametext = (EditText)findViewById(R.id.editText_gname);
				EditText gradetext = (EditText)findViewById(R.id.editText_grade);
				
				http_url = MainActivity.HOST + "purchase_ticket?ticket_id=" + tid + "&user_id="
							+ sellerid + "&buyer_name=" + nametext.getText().toString().trim().replace(" ", "%20")
							+ "&grade=" + gradetext.getText().toString().trim();
				
				resulttext.setText("Submitting...");
				
				submit.setEnabled(false);
				
				new SubmitThread().execute(new Void[0]);
			}
		});
	}

	protected class SubmitThread extends AsyncTask<Void, Void, Void> {
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
			TextView resulttext = (TextView) findViewById(R.id.submitrtext);

			int index = result.indexOf("42:");
			if (index != -1) {
				resulttext.setText("#"+result.substring(index+3)+" sold.");
			} else {
				resulttext.setText(result);
			}
		}
	}
	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sale_info, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
