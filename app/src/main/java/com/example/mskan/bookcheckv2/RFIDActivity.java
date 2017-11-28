package com.example.mskan.bookcheckv2;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.net.HttpURLConnection;
import java.net.URL;

public class RFIDActivity extends AppCompatActivity {
	private NfcAdapter nfcAdapter;
	PendingIntent pendingIntent;
	String hex;
	String UUID="";
	String Json;
	private String UserID;
	private String UserToken;
	private String URL;
	private String Title;
	TextView textView;
	ImageView imageView;
	String library;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rfid);
		Title = getIntent().getStringExtra("Title");
		UserID = getIntent().getStringExtra("UserID");
		UserToken = getIntent().getStringExtra("UserToken");
		URL = getIntent().getStringExtra("URL");
		textView = (TextView) findViewById(R.id.RFIDtext);
		imageView = (ImageView) findViewById(R.id.RFIDImage);
		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		library = getIntent().getStringExtra("Libraries");
		if(nfcAdapter == null){
			Toast.makeText(this, "NFC가 지원되지 않는 기기입니다. 사용에 불편을 드려 죄송합니다.", Toast.LENGTH_SHORT).show();
			finish();
		}

		if(!nfcAdapter.isEnabled()) {
			Toast.makeText(this, "이 서비스는 NFC기능이 필요한 서비스입니다.", Toast.LENGTH_SHORT).show();
			Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
			startActivity(i);
		}

		Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
	}

	@Override
	protected void onResume(){
		super.onResume();
		if(nfcAdapter != null){
			nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
		}
	}

	@Override
	protected void onPause(){
		if(nfcAdapter != null){
			nfcAdapter.disableForegroundDispatch(this);
		}
		super.onPause();
	}

	@Override
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		final Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

		Thread thread = new Thread(){
			@Override
			public void run() {
				String urlString = "http://henocks.dothome.co.kr/API/common/borrowBook";
				try {
					URL url = new URL(urlString);
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

					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("POST");
					connection.setRequestProperty("Content-Type", "application/json");
					connection.setRequestProperty("Accept", "application/json");
					connection.setDoInput(true);
					connection.setDoOutput(true);
					StringBuffer stringBuffer = new StringBuffer();
					stringBuffer.append("ID").append("=").append(UserID).append("&");
					stringBuffer.append("Token").append("=").append(UserToken).append("&");
					stringBuffer.append("BookID").append("=").append(UUID);
					OutputStream outputStream = connection.getOutputStream();
					BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
					bufferedWriter.write(stringBuffer.toString());

					bufferedWriter.flush();
					bufferedWriter.close();
					outputStream.close();
					connection.connect();
					UUID = "";
					StringBuilder responseStringBuilder = new StringBuilder();
					if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
						BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
						for(;;){
							String stringline = bufferedReader.readLine();
							if(stringline == null) break;
							responseStringBuilder.append(stringline + '\n');
						}
						bufferedReader.close();
					}

					connection.disconnect();
					Json = responseStringBuilder.toString();
				} catch (IOException e) {
					Toast.makeText(RFIDActivity.this, "전송 중 오류가 발생하였습니다. 네트워크 상태를 확인해주세요" , Toast.LENGTH_SHORT);
				}

			}
		};
		thread.start();
		try{
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		boolean success = Boolean.getBoolean(Json);

		if(success) {
			textView.setText(Title + "이 완료되었습니다.");
			imageView.setImageResource(R.drawable.success);
		}
		else{
			textView.setText(Title + "도중 오류가 발생하였습니다.");
			imageView.setImageResource(R.drawable.warning);
		}
	}
}
