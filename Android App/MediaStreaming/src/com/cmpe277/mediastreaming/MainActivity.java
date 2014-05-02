package com.cmpe277.mediastreaming;

import com.cmpe277.mediastreaming.api.AppRtspServer;
import com.cmpe277.mediastreaming.rtsp.AppRTSPServer;
import com.cmpe277.mediastreaming.utility.SessionBuilder;

import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class MainActivity extends Activity {

	static final public String TAG = "MediaStreaming - MainActivity";

	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private AppRTSPServer mRtspServer;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.livestream);
		
			mSurfaceView = (SurfaceView)findViewById(R.id.handset_camera_view);
			mSurfaceHolder = mSurfaceView.getHolder();
			mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			SessionBuilder.getInstance().setSurfaceHolder(mSurfaceHolder);
			
			this.startService(new Intent(this,AppRtspServer.class));
	}

	public void onStart() {
		super.onStart();
		bindService(new Intent(this,AppRtspServer.class), mRtspServiceConnection, Context.BIND_AUTO_CREATE);
		Log.d(TAG,"Bind and Start Service");
	}

	@Override
	public void onStop() {
		super.onStop();
	
		if (mRtspServer != null) mRtspServer.removeCallbackListener(mRtspCallbackListener);
		unbindService(mRtspServiceConnection);
		finish();
		
		Log.d(TAG,"On Stop - Unbind & Stop Service");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG,"On Resume");
	}

	@Override
	public void onPause() {
		super.onPause();
		
		if (mRtspServer != null) mRtspServer.removeCallbackListener(mRtspCallbackListener);
		unbindService(mRtspServiceConnection);
		finish();
		
		Log.d(TAG,"on Pause");
	}

	@Override
	public void onDestroy() {
		Log.d(TAG,"Destroyed");
		super.onDestroy();
		if (mRtspServer != null) mRtspServer.removeCallbackListener(mRtspCallbackListener);
		unbindService(mRtspServiceConnection);
		finish();
	}

	@Override    
	public void onBackPressed() {
		Log.d(TAG,"Back Button Pressed");
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	private ServiceConnection mRtspServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mRtspServer = (AppRtspServer) ((AppRTSPServer.LocalBinder)service).getService();
			mRtspServer.addCallbackListener(mRtspCallbackListener);
			mRtspServer.start();
			
			Toast.makeText(getApplicationContext(),"Service Started", Toast.LENGTH_LONG).show();
			Log.d(TAG,"Service Started");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Toast.makeText(getApplicationContext(),"Service Disconnected", Toast.LENGTH_LONG).show();
			Log.d(TAG,"Service Disconnected");
		}

	};

	private AppRTSPServer.CallbackListener mRtspCallbackListener = new AppRTSPServer.CallbackListener() {

		@Override
		public void onError(AppRTSPServer server, Exception e, int error) {
			Toast.makeText(getApplicationContext(),"Exception. Please read Log file for more details", Toast.LENGTH_LONG).show();
			Log.d(TAG,"Callback Error");
		}

		@Override
		public void onMessage(AppRTSPServer server, int message) {
			Log.d(TAG,"Callback on Message received");
			
		}


	};	
}
