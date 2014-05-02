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

public class MainActivity extends Activity {

	static final public String TAG = "MediaStreaming - MainActivity";

	public final int HANDSET = 0x01;
	public int device = HANDSET;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private AppRTSPServer mRtspServer;
	
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

	}

	@Override
	public void onStop() {
		super.onStop();
	
		if (mRtspServer != null) mRtspServer.removeCallbackListener(mRtspCallbackListener);
		unbindService(mRtspServiceConnection);
		finish();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		
		if (mRtspServer != null) mRtspServer.removeCallbackListener(mRtspCallbackListener);
		unbindService(mRtspServiceConnection);
		finish();
	}

	@Override
	public void onDestroy() {
		Log.d(TAG,"SpydroidActivity destroyed");
		super.onDestroy();
		if (mRtspServer != null) mRtspServer.removeCallbackListener(mRtspCallbackListener);
		unbindService(mRtspServiceConnection);
		finish();
	}

	@Override    
	public void onBackPressed() {
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
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {}

	};

	private AppRTSPServer.CallbackListener mRtspCallbackListener = new AppRTSPServer.CallbackListener() {

		@Override
		public void onError(AppRTSPServer server, Exception e, int error) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onMessage(AppRTSPServer server, int message) {
			// TODO Auto-generated method stub
			
		}


	};	

//	private ServiceConnection mHttpServiceConnection = new ServiceConnection() {
//
//		@Override
//		public void onServiceConnected(ComponentName name, IBinder service) {
//			mHttpServer = (CustomHttpServer) ((TinyHttpServer.LocalBinder)service).getService();
//			mHttpServer.addCallbackListener(mHttpCallbackListener);
//			mHttpServer.start();
//		}
//
//		@Override
//		public void onServiceDisconnected(ComponentName name) {}
//
//	};

//	private TinyHttpServer.CallbackListener mHttpCallbackListener = new TinyHttpServer.CallbackListener() {
//
//		@Override
//		public void onError(TinyHttpServer server, Exception e, int error) {
//			// We alert the user that the port is already used by another app.
//			if (error == TinyHttpServer.ERROR_HTTP_BIND_FAILED ||
//					error == TinyHttpServer.ERROR_HTTPS_BIND_FAILED) {
//				String str = error==TinyHttpServer.ERROR_HTTP_BIND_FAILED?"HTTP":"HTTPS";
//				new AlertDialog.Builder(SpydroidActivity.this)
//				.setTitle(R.string.port_used)
//				.setMessage(getString(R.string.bind_failed, str))
//				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//					public void onClick(final DialogInterface dialog, final int id) {
//						startActivityForResult(new Intent(SpydroidActivity.this, OptionsActivity.class),0);
//					}
//				})
//				.show();
//			}
//		}
//
//		@Override
//		public void onMessage(TinyHttpServer server, int message) {
//			if (message==CustomHttpServer.MESSAGE_STREAMING_STARTED) {
//				if (mAdapter != null && mAdapter.getHandsetFragment() != null) 
//					mAdapter.getHandsetFragment().update();
//				if (mAdapter != null && mAdapter.getPreviewFragment() != null)	
//					mAdapter.getPreviewFragment().update();
//			} else if (message==CustomHttpServer.MESSAGE_STREAMING_STOPPED) {
//				if (mAdapter != null && mAdapter.getHandsetFragment() != null) 
//					mAdapter.getHandsetFragment().update();
//				if (mAdapter != null && mAdapter.getPreviewFragment() != null)	
//					mAdapter.getPreviewFragment().update();
//			}
//		}
//
//	};

}
