package com.cmpe277.mediastreaming;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

/***** This Activity / Class is responsible to display Home Screen with 2 buttons: 
 * 				1. Live Stream : Will pass control to Main Activity Intent and will create a video stream using RTSP protocol.
 * 				2. Upload Media : Will upload local media ( video files ) to AWS instance, which will intern connected to Adobe Media Encoder and Media Server. This will Broadcast Video to a particular web site.
 * @author malik
 *
 */
public class Home extends Activity {
	
	static final public String TAG = "MediaStreaming - HomeActivity";

	private ImageButton btnLiveStream, btnUpload;
	private static final int SELECT_VIDEO = 3;
	private String URL = "http://54.225.246.29:8080/RESTfulExample/rest/file/upload";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		btnLiveStream = (ImageButton) findViewById(R.id.btnLiveStream);
		btnUpload = (ImageButton) findViewById(R.id.btnUpload);

		btnLiveStream.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				if(view == btnLiveStream)
				{
					Intent intentLiveStream = new Intent(Home.this,MainActivity.class);
					startActivity(intentLiveStream);
				}
			}
		});

		btnUpload.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				if(view == btnUpload)
				{
					Intent intent = new Intent();
					intent.setType("video/*");
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(Intent.createChooser(intent,"Select a Video "), SELECT_VIDEO);
				}

			}
		});
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_VIDEO) {
				//Uri selectedVideoUri = data.getData();
				//String selectedPath = getPath(selectedVideoUri);
				new uploadMedia().execute(getPath(data.getData()));
			}      
		}
	}


	private class uploadMedia extends AsyncTask<String, Void, String> {

		protected String doInBackground(String... params) {
			String mediaFilePath = params[0];

			HttpClient httpClientObject = new DefaultHttpClient();
			HttpPost httpPostCall = new HttpPost(URL);
			FileBody fileParamRequestBody = new FileBody(new File(mediaFilePath));			

			MultipartEntity requestParamEntity = new MultipartEntity();
			requestParamEntity.addPart("file", fileParamRequestBody);
			httpPostCall.setEntity(requestParamEntity);

			String httpResult = null;

			try {

				HttpEntity httpEntityObject = httpClientObject.execute(httpPostCall).getEntity();

				if (httpEntityObject != null) {
					InputStream inputStreamObject = httpEntityObject.getContent();
					Reader readerObject = new InputStreamReader(inputStreamObject);
					BufferedReader bufferedReaderObject = new BufferedReader(readerObject);

					StringBuilder resultStringBuilder = new StringBuilder();
					String readLine = null;

					while ((readLine = bufferedReaderObject.readLine()) != null) {
						resultStringBuilder.append(readLine + "\n");
					}

					httpResult = resultStringBuilder.toString();
				}

			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return httpResult;
		}

		protected void onPostExecute(String response) {
			try {
				Toast.makeText(getApplicationContext(),response, Toast.LENGTH_LONG).show();
				//toast website name and url
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String getPath(Uri uri) {
		String[] videoDetails = { MediaStore.Video.Media.DATA, MediaStore.Video.Media.SIZE, MediaStore.Video.Media.DURATION}; 
		Cursor videoResultCursor = managedQuery(uri, videoDetails, null, null, null);
		videoResultCursor.moveToFirst(); 
		String filePath = videoResultCursor.getString(videoResultCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
		int fileSize = videoResultCursor.getInt(videoResultCursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
		long duration = TimeUnit.MILLISECONDS.toSeconds(videoResultCursor.getInt(videoResultCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)));

		return filePath;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.home, menu);
		return true;
	}

}
