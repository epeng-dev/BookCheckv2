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

public class AdminLoginActivity extends AppCompatActivity {
	Button bLogin;
	TextView errorText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_admin_login);
		this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_translate_right);

		final EditText etID = (EditText) findViewById(R.id.etID);
		final EditText etPW = (EditText) findViewById(R.id.etPW);
		bLogin = (Button) findViewById(R.id.login);
		errorText = (TextView) findViewById(R.id.errorText);

		bLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bLogin.setEnabled(false);
				String ID = etID.getText().toString();
				String PW = etPW.getText().toString();
				if (validate(ID, PW)) {
					errorText.setText("ID나 PW를 정확히 입력해주십시오.");
					bLogin.setEnabled(true);
					return;
				}
				login(ID, PW);
			}
		});

	}

	private void login(final String ID, final String PW) {
		new LoginTask().execute(ID, PW);
	}

	private boolean validate(String ID, String PW) {
		if (ID.isEmpty() || PW.isEmpty()) {
			return true;
		}
		return false;
	}

	private class LoginTask extends AsyncTask<String, Void, String> {
		ProgressDialog progressDialog = new ProgressDialog(AdminLoginActivity.this, R.style.ProgressDialogTheme);
		String ID = null;
		String PW = null;
		int requestCode = 0;

		@Override
		protected String doInBackground(String... strings) {
			String rawString = "";
			String urlString = "http://52.79.134.200:3004/auth/admin";
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
			if (requestCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				errorText.setText("ID나 Password가 올바르지 않습니다");
				bLogin.setEnabled(true);
			} else {
				try {
					Log.d("resultString", result);
					JSONObject jsonObject = new JSONObject(result);
					String userToken = jsonObject.getString("access_token");
					Intent intent = new Intent();
					intent.putExtra("Token", userToken);
					intent.putExtra("ID", ID);
					intent.putExtra("isAdmin", false);
					setResult(1, intent);
					finish();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				bLogin.setEnabled(true);
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