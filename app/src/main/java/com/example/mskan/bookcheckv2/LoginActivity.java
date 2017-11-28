package com.example.mskan.bookcheckv2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.ContactsContract;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginActivity extends AppCompatActivity {
	Button bLogin;
	private String usertoken = "";
	SharedPreferences IDpref;
	SharedPreferences Tokenpref;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		this.overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_translate_right);

		final EditText etID = (EditText) findViewById(R.id.etID);
		final EditText etPW = (EditText) findViewById(R.id.etPW);
		bLogin = (Button) findViewById(R.id.login);
		final TextInputLayout IDWrapper = (TextInputLayout) findViewById(R.id.layout_LoginID);
		final TextInputLayout PWWrapper = (TextInputLayout) findViewById(R.id.layout_LoginPW);
		IDpref = getSharedPreferences("ID", MODE_PRIVATE);
		Tokenpref= getSharedPreferences("Token", MODE_PRIVATE);
		IDWrapper.setHint("ID");
		PWWrapper.setHint("PassWord");

		bLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bLogin.setEnabled(false);

				String ID = etID.getText().toString();
				String PW = etPW.getText().toString();
				if(validate(ID, PW)){
					Toast.makeText(LoginActivity.this, "ID나 PW를 정확히 입력해주십시오.", Toast.LENGTH_SHORT);
					bLogin.setEnabled(true);
					return;
				}

				final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this, R.style.ProgressDialogTheme);
				progressDialog.setIndeterminate(true);
				progressDialog.setMessage("로그인중입니다.");
				progressDialog.show();
				login(ID, PW);
				progressDialog.dismiss();
			}
		});
	}

	private void login(final String ID, final String PW){
		Thread thread = new Thread(){
			@Override
			public void run(){
				String urlString = "http://esplay.xyz:21221/API/common/login";
				HttpURLConnection connection = null;
				try{
					URL url = new URL(urlString);
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("POST");
					connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					connection.setDoInput(true);
					connection.setDoOutput(true);

					StringBuffer stringBuffer = new StringBuffer();
					stringBuffer.append("ID").append("=").append(ID).append("&");
					stringBuffer.append("PW").append("=").append(PW);
					OutputStream outputStream = connection.getOutputStream();
					BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
					bufferedWriter.write(stringBuffer.toString());
					bufferedWriter.flush();
					bufferedWriter.close();
					outputStream.close();

					StringBuilder responseStringBuilder = new StringBuilder();
					if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
						BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
						while(true){
							String stringline = bufferedReader.readLine();
							if(stringline == null) break;
							responseStringBuilder.append(stringline).append('\n');
						}
						bufferedReader.close();
					}

					connection.disconnect();
					usertoken = responseStringBuilder.toString();
				} catch (IOException e){
					Toast.makeText(LoginActivity.this, "로그인에 오류가 생겼습니다. 네트워크 상태를 확인해주세요", Toast.LENGTH_SHORT).show();
				} finally {
					if(connection != null)
						connection.disconnect();
				}
			}
		};

		if(usertoken.equals("")) {
			Toast.makeText(this, "ID나 Password가 올바르지 않습니다", Toast.LENGTH_SHORT).show();
			bLogin.setEnabled(true);
		}else{
			SharedPreferences.Editor editor = IDpref.edit();
			editor.putString("ID", ID);
			editor.apply();
			editor = Tokenpref.edit();
			editor.putString("Token", usertoken);
			editor.apply();
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			intent.putExtra("UserID", ID);
			intent.putExtra("UserToken", usertoken);
			startActivity(intent);
			finish();
			bLogin.setEnabled(true);
		}
	}


	private boolean validate(String ID, String PW){
		if(ID.isEmpty() || PW.isEmpty()){
			return true;
		}
		return false;
	}

	@Override
	public void finish() {
		super.finish();
		this.overridePendingTransition(R.anim.anim_stop, R.anim.anim_translate_right);
	}
}
