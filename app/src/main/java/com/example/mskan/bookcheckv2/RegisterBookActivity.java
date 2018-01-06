
package com.example.mskan.bookcheckv2;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegisterBookActivity extends AppCompatActivity {
	private final int camera = 0;
	private final int album = 1;
	private final int crop = 2;

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
	Button imageSelect;
	ImageView bookImage;

	String date;
	String absoultePath;
	String imagepath;
	Uri imageUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_book);

		String uuid = getIntent().getStringExtra("UUID");
		final String token = getIntent().getStringExtra("Token");

		title = (EditText) findViewById(R.id.etTitle);
		author = (EditText) findViewById(R.id.etAuthor);
		area = (EditText) findViewById(R.id.etArea);
		libraryID = (EditText) findViewById(R.id.etLibraryId);
		summary = (EditText) findViewById(R.id.etSummary);
		detail = (EditText) findViewById(R.id.etDetail);
		rfid = (TextView) findViewById(R.id.tvRFID);
		publication = (TextView) findViewById(R.id.tvPublication);
		error = (TextView) findViewById(R.id.tvError);
		register = (Button) findViewById(R.id.btRegister);
		imageSelect = (Button) findViewById(R.id.btImageButton);
		bookImage = (ImageView) findViewById(R.id.bookImage);
		rfid.setText(uuid);

		imageSelect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						doTakePhotoAction();
					}
				};

				DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						doTakeAlbumAction();
					}
				};

				DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				};
				int permissionCheck = ContextCompat.checkSelfPermission(RegisterBookActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
				if (permissionCheck == PackageManager.PERMISSION_DENIED) {
					ActivityCompat.requestPermissions(RegisterBookActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
				} else {
					new AlertDialog.Builder(RegisterBookActivity.this)
							.setTitle("업로드할 이미지 선택")
							.setPositiveButton("사진촬영", cameraListener)
							.setNeutralButton("앨범선택", albumListener)
							.setNegativeButton(" 취소 ", cancelListener)
							.show();
				}
			}
		});

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
						Integer.parseInt(array[1]) - 1,
						Integer.parseInt(array[2]));
				dialog.show();
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
						strDetail,
						imagepath);
			}
		});
	}

	private void doTakePhotoAction() {
		int permissionCheck = ContextCompat.checkSelfPermission(RegisterBookActivity.this, Manifest.permission.CAMERA);
		if (permissionCheck == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(RegisterBookActivity.this, new String[]{Manifest.permission.CAMERA}, 0);
		} else {
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";

			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
				imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));
			else {
				imageUri = FileProvider.getUriForFile(RegisterBookActivity.this,
						BuildConfig.APPLICATION_ID + ".provider",
						new File(Environment.getExternalStorageDirectory(), url));
			}
			intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			startActivityForResult(intent, camera);
		}
	}

	private void doTakeAlbumAction() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
		startActivityForResult(intent, album);
	}

	private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
			date = year + "-";

			if (month + 1 < 10) {
				date = date + "0" + (month + 1) + "-";
			} else {
				date = date + (month + 1) + "-";
			}

			if (dayOfMonth < 10) {
				date = date + "0" + dayOfMonth;
			} else {
				date = date + dayOfMonth;
			}

			publication.setText(date);
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
			case album: {
				imageUri = data.getData();
			}
			case camera: {
				Intent intent = new Intent("com.android.camera.action.CROP");
				intent.setDataAndType(imageUri, "image/*");

				intent.putExtra("outputX", 200);
				intent.putExtra("outputY", 300);
				intent.putExtra("aspectX", 1);
				intent.putExtra("aspectY", 1.5);
				intent.putExtra("scale", true);
				intent.putExtra("return-data", true);
				startActivityForResult(intent, crop);
				break;
			}
			case crop: {
				Bundle bundle = data.getExtras();
				String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
						"/BookCheck/" + System.currentTimeMillis() + ".jpg";
				imagepath = filePath;
				if (bundle != null) {
					Bitmap bitmap = bundle.getParcelable("data");
					bookImage.setImageBitmap(bitmap);

					storeCropImage(bitmap, filePath);
					absoultePath = filePath;
					break;
				}

				File file = new File(imageUri.getPath());
				if (file.exists()) {
					file.delete();
				}
			}


		}
	}

	private void storeCropImage(Bitmap bitmap, String filePath) {
		String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BookCheck";
		BufferedOutputStream out = null;
		try {
			File directory_BookCheck = new File(dirPath);

			if (!directory_BookCheck.exists()) {
				if (!directory_BookCheck.mkdirs()) {
					Toast.makeText(RegisterBookActivity.this, "심각한 오류 발생", Toast.LENGTH_SHORT).show();
				}
			}

			File copyFile = new File(filePath);

			copyFile.createNewFile();
			out = new BufferedOutputStream(new FileOutputStream(copyFile));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			byte[] byteArray = stream.toByteArray();

			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(copyFile)));

			out.flush();
			out.close();
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == 0) {
			if (grantResults[0] != 0) {
				Toast.makeText(this, "카메라 권한이 거절되었습니다. 카메라를 이용하려면 권한을 승낙하여야합니다.", Toast.LENGTH_SHORT).show();
			}
		}
	}

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
			String filepath = strings[9];
			String boundary = "boundary";
			String delimiter = "\r\n--" + boundary + "\r\n";
			HttpURLConnection connection = null;
			try {
				URL url = new URL("http://52.79.134.200:3004/book");
				connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Authorization", "JWT " + token);
				connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
				connection.setDoInput(true);
				connection.setDoOutput(true);

				StringBuilder stringBuffer = new StringBuilder();
				stringBuffer.append(delimiter);
				stringBuffer.append(setValue("library_id", libraryId));
				stringBuffer.append(delimiter);
				stringBuffer.append(setValue("rfid", rfid));
				stringBuffer.append(delimiter);
				stringBuffer.append(setValue("area", area));
				stringBuffer.append(delimiter);
				stringBuffer.append(setValue("title", title));
				stringBuffer.append(delimiter);
				stringBuffer.append(setValue("author", author));
				stringBuffer.append(delimiter);
				stringBuffer.append(setValue("publication_date", publication));
				stringBuffer.append(delimiter);
				stringBuffer.append(setValue("summary", summary));
				stringBuffer.append(delimiter);
				stringBuffer.append(setValue("detail", detail));
				stringBuffer.append(delimiter);
				stringBuffer.append(setFile("image", title + ".jpg"));
				stringBuffer.append("Content-Type: image/jpeg\r\n\r\n");


				FileInputStream fileInputStream = new FileInputStream(filepath);
				DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(connection.getOutputStream()));
				outputStream.write(stringBuffer.toString().getBytes("utf-8"));
				Log.d("httpSend", stringBuffer.toString());

				int maxBufferSize = 1024;
				int bufferSize = Math.min(fileInputStream.available(), maxBufferSize);
				byte[] buffer = new byte[bufferSize];

				int byteRead = fileInputStream.read(buffer, 0, bufferSize);

				while (byteRead > 0) {
					outputStream.write(buffer, 0, bufferSize);
					bufferSize = Math.min(fileInputStream.available(), maxBufferSize);
					byteRead = fileInputStream.read(buffer, 0, bufferSize);
				}

				outputStream.writeBytes("\r\n--" + boundary + "--\r\n");
				outputStream.flush();
				outputStream.close();
				fileInputStream.close();

				requestCode = connection.getResponseCode();

				connection.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e){
				error.setText("책 사진을 넣어주세요.");
			} finally{
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

	public static String setValue(String key, String value) {
		return "Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n"
				+ value;
	}

	public static String setFile(String key, String fileName) {
		return "Content-Disposition: form-data; name=\"" + key
				+ "\"; filename=\"" + fileName + "\"\r\n";
	}
}
