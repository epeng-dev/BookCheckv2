package com.example.mskan.bookcheckv2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class SplashActivity extends AppCompatActivity {
	String Token;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		ImageView imageView = (ImageView) findViewById(R.id.SplashImage);
		final SharedPreferences pref = getSharedPreferences("user", MODE_PRIVATE);
		Glide.with(this).load(R.raw.splash).into(imageView);
		Handler handler = new Handler();
		Token = pref.getString("refreshToken", "");
		if (Token != "") {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					new AutoLoginTask().execute(Token);
				}
			}, 7000);
		}
		else{
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					finish();
					startActivity(new Intent(SplashActivity.this, LoginActivity.class));
				}
			}, 7800);
		}
	}

	@Override
	public void finish() {
		super.finish();
		this.overridePendingTransition(R.anim.anim_stop, R.anim.anim_translate_right);
	}

	private class AutoLoginTask extends AsyncTask<String, Void, String> {
		int requestCode = 0;

		@Override
		protected String doInBackground(String... strings) {
			String rawString = "";
			String urlString = "http://52.79.134.200:3004/refresh";
			String token = strings[0];
			HttpURLConnection connection = null;
			try {
				URL url = new URL(urlString);
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("POST");
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
			if (requestCode == HttpURLConnection.HTTP_OK) {
				Log.d("resultString", result);
				JSONObject jsonObject = null;
				try {
					jsonObject = new JSONObject(result);
					String userToken = jsonObject.getString("access_token");
					SharedPreferences pref = getSharedPreferences("user", MODE_PRIVATE);
					SharedPreferences.Editor editor = pref.edit();
					editor.putString("Token", userToken);
					editor.apply();
					finish();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else if(requestCode == HttpURLConnection.HTTP_RESET){
				Toast.makeText(SplashActivity.this, "다른 기기에서 비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show();
				startActivity(new Intent(SplashActivity.this, LoginActivity.class));
			} else if (requestCode == HttpURLConnection.HTTP_FORBIDDEN){
				Toast.makeText(SplashActivity.this, "다시 로그인 해주시기 바랍니다.", Toast.LENGTH_SHORT).show();
				startActivity(new Intent(SplashActivity.this, LoginActivity.class));
			}

			super.onPostExecute(result);
		}

	}
}
