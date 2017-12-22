package com.example.mskan.bookcheckv2;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import static android.os.Build.ID;

public class RFIDActivity extends AppCompatActivity {
	private NfcAdapter nfcAdapter;
	PendingIntent pendingIntent;
	String hex;
	String UUID = "";
	String mode;
	private String userToken;
	private String URL;
	TextView textView;
	ImageView imageView;
	String library;
	Tag tag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rfid);
		userToken = getIntent().getStringExtra("UserToken");
		URL = getIntent().getStringExtra("URL");
		mode = getIntent().getStringExtra("mode");
		textView = (TextView) findViewById(R.id.RFIDtext);
		imageView = (ImageView) findViewById(R.id.RFIDImage);
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		library = getIntent().getStringExtra("Libraries");

		if (nfcAdapter == null) {
			Toast.makeText(this, "NFC가 지원되지 않는 기기입니다. 사용에 불편을 드려 죄송합니다.", Toast.LENGTH_SHORT).show();
			finish();
		}

		if (!nfcAdapter.isEnabled()) {
			Toast.makeText(this, "이 서비스는 NFC기능이 필요한 서비스입니다.", Toast.LENGTH_SHORT).show();
			Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
			startActivity(i);
		}

		Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (nfcAdapter != null) {
			nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
		}
	}

	@Override
	protected void onPause() {
		if (nfcAdapter != null) {
			nfcAdapter.disableForegroundDispatch(this);
		}
		super.onPause();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (mode.equals("borrow"))
			new BorrowTask().execute(URL, userToken);
	}

	private class BorrowTask extends AsyncTask<String, Void, String> {
		int requestCode = 0;

		@Override
		protected String doInBackground(String... strings) {
			String rawString = "";
			String urlString = strings[0];
			String token = strings[1];
			HttpURLConnection connection = null;
			try {
				StringBuilder stringBuffer = new StringBuilder(urlString);
				stringBuffer.append("?rfid").append("=").append(UUID);

				URL url = new URL(stringBuffer.toString());
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.setRequestProperty("Authorization", "JWT " + token);
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setDoInput(true);
				connection.setDoOutput(false);

				StringBuilder responseStringBuilder = new StringBuilder();
				if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
					while (true) {
						String stringline = bufferedReader.readLine();
						if (stringline == null) break;
						responseStringBuilder.append(stringline).append('\n');
					}
					bufferedReader.close();
				}
				requestCode = connection.getResponseCode();
				connection.disconnect();
				rawString = responseStringBuilder.toString();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (connection != null)
					connection.disconnect();
			}
			return rawString;
		}

		@Override
		protected void onPostExecute(String result) {
			switch (requestCode) {
				case HttpURLConnection.HTTP_OK:
					textView.setText("대출이 완료되었습니다.");
					imageView.setImageResource(R.drawable.success);
					break;
				case HttpURLConnection.HTTP_NO_CONTENT:
					textView.setText("존재하지 않는 RFID입니다!");
					imageView.setImageResource(R.drawable.warning);
					break;
				case 208:
					textView.setText("대출이 불가능한 책입니다.");
					imageView.setImageResource(R.drawable.warning);
			}

			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			if (tag != null) {
				byte[] tagID = tag.getId();
				for (int i = 0; i < 4; i++) {
					if ((tagID[i] & 0xFF) < (byte) 16) {
						hex = "0" + Integer.toHexString(tagID[i] & 0xFF) + "_";
					} else {
						hex = Integer.toHexString(tagID[i] & 0xFF) + "_";
					}

					UUID += hex;
				}
			}

			UUID = UUID.substring(0, UUID.length() - 1);
			UUID = UUID.toUpperCase();
			super.onPreExecute();
		}
	}
}

