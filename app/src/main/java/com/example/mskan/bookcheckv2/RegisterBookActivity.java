
package com.example.mskan.bookcheckv2;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegisterBookActivity extends AppCompatActivity {
	EditText title;
	EditText author;
	EditText area;
	EditText libraryID;
	EditText summary;
	EditText detail;
	TextView rfid;
	TextView publication;
	TextView error;
	Button register;

	String date;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_book);

		String uuid = getIntent().getStringExtra("UUID");
		final String token = getIntent().getStringExtra("Token");

		title = (EditText)findViewById(R.id.etTitle);
		author = (EditText)findViewById(R.id.etAuthor);
		area = (EditText)findViewById(R.id.etArea);
		libraryID = (EditText)findViewById(R.id.etLibraryId);
		summary = (EditText)findViewById(R.id.etSummary);
		detail = (EditText)findViewById(R.id.etDetail);
		rfid = (TextView)findViewById(R.id.tvRFID);
		publication = (TextView)findViewById(R.id.tvPublication);
		error = (TextView)findViewById(R.id.tvError);
		register = (Button)findViewById(R.id.btRegister);

		rfid.setText(uuid);
		publication.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Date date = new Date(System.currentTimeMillis());
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
				String formatDate = simpleDateFormat.format(date);
				String[] array = formatDate.split("/");
				DatePickerDialog dialog = new DatePickerDialog(RegisterBookActivity.this,
						listener,
						Integer.parseInt(array[0]),
						Integer.parseInt(array[1]),
						Integer.parseInt(array[2]));

			}
		});

		register.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String strLibrary = libraryID.getText().toString();
				String strRfid = rfid.getText().toString();
				String strArea = area.getText().toString();
				String strTitle = title.getText().toString();
				String strAuthor = author.getText().toString();
				String strPublication = publication.getText().toString();
				String strSummary = summary.getText().toString();
				String strDetail = detail.getText().toString();
				new RegisterTask().execute(token,
						strLibrary,
						strRfid,
						strArea,
						strTitle,
						strAuthor,
						strPublication,
						strSummary,
						strDetail);
			}
		});
	}

	private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
			date = year + "-" + month + "-" + dayOfMonth;
			publication.setText(date);
		}
	};

	private class RegisterTask extends AsyncTask<String, Void, String> {
		int requestCode = 0;

		@Override
		protected String doInBackground(String... strings) {
			String rawString = "";

			String token = strings[0];
			String libraryId = strings[1];
			String rfid = strings[2];
			String area = strings[3];
			String title = strings[4];
			String author = strings[5];
			String publication = strings[6];
			String summary = strings[7];
			String detail = strings[8];

			HttpURLConnection connection = null;
			try {
				URL url = new URL("http://52.79.134.200:3004/book");
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setDoInput(true);
				connection.setDoOutput(true);

				StringBuilder stringBuffer = new StringBuilder();
				stringBuffer.append("Authorization").append("=").append(token).append("&");
				stringBuffer.append("library_id").append("=").append(libraryId).append("&");
				stringBuffer.append("rfid").append("=").append(rfid).append("&");
				stringBuffer.append("area").append("=").append(area).append("&");
				stringBuffer.append("title").append("=").append(title).append("&");
				stringBuffer.append("author").append("=").append(author).append("&");
				stringBuffer.append("publication_date").append("=").append(publication).append("&");
				stringBuffer.append("summary").append("=").append(summary).append("&");
				stringBuffer.append("detail").append("=").append(detail);

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
			switch (requestCode) {
				case HttpURLConnection.HTTP_CREATED:
					Toast.makeText(RegisterBookActivity.this, "책 등록을 성공하였습니다.", Toast.LENGTH_SHORT).show();
					finish();
					break;
				case HttpURLConnection.HTTP_NO_CONTENT:
					error.setText("존재하지 않는 도서관 ID입니다!");
					break;
				case 208:
					error.setText("이미 등록된 책입니다.");
					break;
				case 403:
					error.setText("다른 도서관의 책이거나 권한이 없습니다.");
					break;
			}

			super.onPostExecute(result);
		}

	}
}
