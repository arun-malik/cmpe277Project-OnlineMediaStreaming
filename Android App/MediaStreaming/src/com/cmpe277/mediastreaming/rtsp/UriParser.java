
package com.cmpe277.mediastreaming.rtsp;


import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.cmpe277.mediastreaming.utility.AudioQuality;
import com.cmpe277.mediastreaming.utility.Session;
import com.cmpe277.mediastreaming.utility.SessionBuilder;
import com.cmpe277.mediastreaming.utility.VideoQuality;

import android.hardware.Camera.CameraInfo;
import android.util.Log;

/**
 * This class parses URIs received by the RTSP server and configures a Session accordingly.
 */
public class UriParser {

	public final static String TAG = "UriParser";
	
	/**
	 * Configures a Session according to the given URI.
	 * Here are some examples of URIs that can be used to configure a Session:
	 * <ul><li>rtsp://xxx.xxx.xxx.xxx:8086?h264&flash=on</li>
	 * <li>rtsp://xxx.xxx.xxx.xxx:8086?h263&camera=front&flash=on</li>
	 * <li>rtsp://xxx.xxx.xxx.xxx:8086?h264=200-20-320-240</li>
	 * <li>rtsp://xxx.xxx.xxx.xxx:8086?aac</li></ul>
	 * @param uri The URI
	 * @throws IllegalStateException
	 * @throws IOException
	 * @return A Session configured according to the URI
	 */
	public static Session parse(String uri) throws IllegalStateException, IOException {		
		SessionBuilder builder = SessionBuilder.getInstance().clone();

		List<NameValuePair> params = URLEncodedUtils.parse(URI.create(uri),"UTF-8");
		if (params.size()>0) {

			builder.setAudioEncoder(SessionBuilder.AUDIO_NONE).setVideoEncoder(SessionBuilder.VIDEO_NONE);

			// Those parameters must be parsed first or else they won't necessarily be taken into account
			for (Iterator<NameValuePair> it = params.iterator();it.hasNext();) {
				NameValuePair param = it.next();

				// FLASH ON/OFF
				if (param.getName().equalsIgnoreCase("flash")) {
					if (param.getValue().equalsIgnoreCase("on")) 
						builder.setFlashEnabled(true);
					else 
						builder.setFlashEnabled(false);
				}

				// CAMERA -> the client can choose between the front facing camera and the back facing camera
				else if (param.getName().equalsIgnoreCase("camera")) {
					if (param.getValue().equalsIgnoreCase("back")) 
						builder.setCamera(CameraInfo.CAMERA_FACING_BACK);
					else if (param.getValue().equalsIgnoreCase("front")) 
						builder.setCamera(CameraInfo.CAMERA_FACING_FRONT);
				}

				// MULTICAST -> the stream will be sent to a multicast group
				// The default mutlicast address is 228.5.6.7, but the client can specify another
				else if (param.getName().equalsIgnoreCase("multicast")) {
					if (param.getValue()!=null) {
						try {
							InetAddress addr = InetAddress.getByName(param.getValue());
							if (!addr.isMulticastAddress()) {
								throw new IllegalStateException("Invalid multicast address !");
							}
							builder.setDestination(addr);
						} catch (UnknownHostException e) {
							throw new IllegalStateException("Invalid multicast address !");
						}
					}
					else {
						// Default multicast address
						builder.setDestination(InetAddress.getByName("228.5.6.7"));
					}
				}

				// UNICAST -> the client can use this to specify where he wants the stream to be sent
				else if (param.getName().equalsIgnoreCase("unicast")) {
					if (param.getValue()!=null) {
						try {
							InetAddress addr = InetAddress.getByName(param.getValue());
							builder.setDestination(addr);
						} catch (UnknownHostException e) {
							throw new IllegalStateException("Invalid destination address !");
						}
					}					
				}

				// TTL -> the client can modify the time to live of packets
				// By default ttl=64
				else if (param.getName().equalsIgnoreCase("ttl")) {
					if (param.getValue()!=null) {
						try {
							int ttl = Integer.parseInt(param.getValue());
							if (ttl<0) throw new IllegalStateException();
							builder.setTimeToLive(ttl);
						} catch (Exception e) {
							throw new IllegalStateException("The TTL must be a positive integer !");
						}
					}
				}

				// H.264
//				else if (param.getName().equalsIgnoreCase("h264")) {
//					VideoQuality quality = VideoQuality.parseQuality(param.getValue());
//					builder.setVideoQuality(quality).setVideoEncoder(VIDEO_H264);
//				}

				// H.263
				else if (param.getName().equalsIgnoreCase("h263")) {
					VideoQuality quality = VideoQuality.parseQuality(param.getValue());
					builder.setVideoQuality(quality).setVideoEncoder(SessionBuilder.VIDEO_H263);
				}

//				// AMR
//				else if (param.getName().equalsIgnoreCase("amrnb") || param.getName().equalsIgnoreCase("amr")) {
//					AudioQuality quality = AudioQuality.parseQuality(param.getValue());
//					builder.setAudioQuality(quality).setAudioEncoder(AUDIO_AMRNB);
//				}

				// AAC
				else if (param.getName().equalsIgnoreCase("aac")) {
					AudioQuality quality = AudioQuality.parseQuality(param.getValue());
					builder.setAudioQuality(quality).setAudioEncoder(SessionBuilder.AUDIO_AAC);
				}

			}

		}

		if (builder.getVideoEncoder()==SessionBuilder.VIDEO_NONE && builder.getAudioEncoder()==SessionBuilder.AUDIO_NONE) {
			SessionBuilder b = SessionBuilder.getInstance();
			builder.setVideoEncoder(b.getVideoEncoder());
			builder.setAudioEncoder(b.getAudioEncoder());
		}

		return builder.build();

	}

}
