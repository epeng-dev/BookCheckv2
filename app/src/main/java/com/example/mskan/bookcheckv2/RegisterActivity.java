package com.example.mskan.bookcheckv2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class RegisterActivity extends AppCompatActivity {
	EditText etId;
	EditText etPw;
	TextView errorText;
	Button button;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		etId = (EditText) findViewById(R.id.etID);
		etPw = (EditText) findViewById(R.id.etPW);
		errorText = (TextView) findViewById(R.id.errorText);
		button = (Button) findViewById(R.id.register);

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				button.setEnabled(false);
				String ID = etId.getText().toString();
				String PW = etPw.getText().toString();
				if(validate(ID, PW)){
					errorText.setText("ID나 PW를 정확히 입력해주십시오.");
					button.setEnabled(true);
					return;
				}
				register(ID, PW);
			}
		});
	}

	private boolean validate(String ID, String PW){
		if(ID.isEmpty() || PW.isEmpty()){
			return true;
		}
		return false;
	}

	private void register(String id, String pw){
		new RegisterTask().execute(id, pw);
	}

	private class RegisterTask extends AsyncTask<String, Void, String> {
		ProgressDialog progressDialog = new ProgressDialog(RegisterActivity.this, R.style.ProgressDialogTheme);
		String ID = null;
		String PW = null;
		int requestCode = 0;
		@Override
		protected String doInBackground(String... strings) {
			String rawString = "";
			String urlString = "http://52.79.134.200:3004/signup";
			ID = strings[0];
			PW = strings[1];
			HttpURLConnection connection = null;
			try {
				URL url = new URL(urlString);
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setDoInput(true);
				connection.setDoOutput(true);

				StringBuilder stringBuffer = new StringBuilder();
				stringBuffer.append("id").append("=").append(ID).append("&");
				stringBuffer.append("pw").append("=").append(PW);

				OutputStream outputStream = connection.getOutputStream();
				BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
				bufferedWriter.write(stringBuffer.toString());
				Log.d("stringbuffer", stringBuffer.toString());
				bufferedWriter.flush();
				bufferedWriter.close();
				outputStream.close();

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
			if (requestCode == HttpURLConnection.HTTP_CREATED) {
				Toast.makeText(RegisterActivity.this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
				onBackPressed();
			} else {
				Toast.makeText(RegisterActivity.this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(result);
			progressDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			progressDialog.setIndeterminate(true);
			progressDialog.setMessage("로그인중입니다.");
			progressDialog.show();
			super.onPreExecute();
		}
	}
}
