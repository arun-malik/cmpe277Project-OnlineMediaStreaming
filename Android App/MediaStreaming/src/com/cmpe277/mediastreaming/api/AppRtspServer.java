package com.cmpe277.mediastreaming.api;

import com.cmpe277.mediastreaming.utility.*;
import com.cmpe277.mediastreaming.rtsp.*;

public class AppRtspServer extends AppRTSPServer  {

	public AppRtspServer() {
		super();
		// RTSP server disabled by default
		AppRTSPServer.mEnabled = true;
	}
}
