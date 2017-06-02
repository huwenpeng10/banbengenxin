package com.example.day11_updatevertion;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {

	private static int code;
	private static String result;

	public static String getData(String path){
		
		try {
			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);
			
			code = conn.getResponseCode();
			
			if (code == 200) {
				InputStream in = conn.getInputStream();
				
				int len;
				byte[] buffer = new byte[1024];
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				while ((len=in.read(buffer))!= -1) {
					out.write(buffer, 0, len);
				}
				out.flush();
				
				byte[] data = out.toByteArray();
				
				result = new String(data);
				
				return result;
			}
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
}
