package com.example.mskan.bookcheckv2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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

public class BarcodeActivity extends AppCompatActivity {
	private ImageView imageView;
	private TextView textView;
	private String Json;
	private String URL;
	private String UserID;
	private String UserToken;
	private String Title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_barcode);
		Title = getIntent().getStringExtra("Title");
		UserID = getIntent().getStringExtra("UserID");
		UserToken = getIntent().getStringExtra("UserToken");
		URL = getIntent().getStringExtra("URL");
		imageView = (ImageView) findViewById(R.id.barcode);
		textView = (TextView) findViewById(R.id.barcodeTEXT);
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
		integrator.setPrompt("책의 바코드가 보이도록 찍어주세요");
		integrator.setCameraId(0);
		integrator.setBeepEnabled(false);
		integrator.setBarcodeImageEnabled(true);
		integrator.initiateScan();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		final IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (result != null) {
			if (result.getContents() == null) {
				Toast.makeText(this, "스캔을 취소하셨습니다.", Toast.LENGTH_SHORT).show();
				finish();
			} else {
				Thread thread = new Thread() {
					@Override
					public void run() {
						String urlString = URL;

						try {
							URL url = new URL(urlString);

							HttpURLConnection connection = (HttpURLConnection) url.openConnection();
							connection.setRequestMethod("POST");
							connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
							connection.setDoInput(true);
							connection.setDoOutput(true);

							StringBuffer stringBuffer = new StringBuffer();
							stringBuffer.append("ID").append("=").append(UserID).append("&");
							stringBuffer.append("Token").append("=").append(UserToken).append("&");
							stringBuffer.append("BookID").append("=").append(result.getContents());
							OutputStream outputStream = connection.getOutputStream();
							BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
							bufferedWriter.write(stringBuffer.toString());

							bufferedWriter.flush();
							bufferedWriter.close();
							outputStream.close();
							connection.connect();

							StringBuilder responseStringBuilder = new StringBuilder();
							if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
								BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
								for (; ; ) {
									String stringline = bufferedReader.readLine();
									if (stringline == null) break;
									responseStringBuilder.append(stringline).append('\n');
								}
								bufferedReader.close();
							}

							connection.disconnect();
							Json = responseStringBuilder.toString();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				};
				JSONObject jsonObject = null;
				try {
					jsonObject = new JSONObject(Json);
				} catch (JSONException e) {
					e.printStackTrace();
				}

				String success = null;
				try {
					success = jsonObject.getString("success");
				} catch (JSONException e) {
					e.printStackTrace();
				}

				if (Boolean.getBoolean(success)) {
					imageView.setImageResource(R.drawable.success);
					textView.setText(Title + "이 완료되었습니다.");
				} else {
					imageView.setImageResource(R.drawable.warning);
					textView.setText(Title + "도중 오류가 발생하였습니다.");
				}
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
}
