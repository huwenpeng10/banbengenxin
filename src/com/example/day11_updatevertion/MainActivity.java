package com.example.day11_updatevertion;

import java.io.File;

import org.json.JSONObject;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

protected static final int UPDATE = 1;
protected static final int NOUPDATE = 2;
protected static final int NETERROR = 3;
private String url;
private int localVersionCode;

//	��Ϊһ��Splash���棬 ���������������͸���ȱ仯�Ķ���Ч��
//	���а汾�ţ� ��˾logo����Ϣ
//	���ܣ�1.���Դӷ���˻�ȡ���°汾�����û�����
//	    2.ֻ�ܹ�˾��Ϣ
//		3.splash��Ϊ����ҳ����ҳ��ļ��ػ��ʱ��

	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case UPDATE:
				showDialogue();
				break;
			case NOUPDATE:
				enterHome();
				Toast.makeText(MainActivity.this, "�Ѿ������°汾", 1).show();
				break;
			case NETERROR:
				enterHome();
				Toast.makeText(MainActivity.this, "�����С���ˡ�����", 1).show();
				break;

			default:
				break;
			}
			
		};
	};

	private TextView progressBar1;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		progressBar1 = (TextView) findViewById(R.id.textView2);
		TextView tv_version = (TextView) findViewById(R.id.textView1);
//		0.0 ͸�� 1.0ʵ��  0.0--1.0
		AlphaAnimation animation = new AlphaAnimation(0.2f, 1.0f);
		animation.setDuration(3000);
//		������������
		findViewById(R.id.root).startAnimation(animation);
		
		try {
//		��ȡ���س���İ汾�źͰ汾�� PackageMannager
			
			PackageManager manager = this.getPackageManager();
//		�õ��ð��������а�����Ϣ packageName
			PackageInfo info = manager.getPackageInfo("com.example.day11_updatevertion", 0);
			localVersionCode = info.versionCode;
			String versionName = info.versionName;
			System.out.println("versionCode"+localVersionCode+"versionName"+versionName);
			
			tv_version.setText("��ǰ�汾��"+localVersionCode);
			
//			ȥ����˻�ȡ���µİ汾�źͱ������Ա�   ����
//			serverVersionCode > versionCode ���°汾��
//			�������ضԻ���ȷ��--���뵽����ҳ�棬ȡ��--������ҳ��
//			��<=  ���ø��£�ֱ�ӽ�����ҳ��

		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
//		����������˻�ȡ���� ��ȡ����˵İ汾��Ϣ
//		�ͱ��صİ汾���Ա�
		
		new Thread(new Runnable() {
			
			private int versioncode;
			private String versionName;
			

			@Override
			public void run() {
				try {
					String path = "http://192.168.119.178:8080/update1/jsoninfo";
					String result = HttpUtil.getData(path);
//				result���Ƿ��ص�json�ַ����� ����Json����
//				JsonObject   JsonArray������������
					JSONObject jo = new JSONObject(result);
					versioncode = (Integer) jo.get("versioncode");
					versionName = (String) jo.get("versionName");
					url = (String) jo.get("url");
					String desc = (String) jo.get("desc");
					
					System.out.println("versionCode=="+versioncode+" versionName"+versionName
							+"url"+url);
					
//					�ͱ��ذ汾���Ա�
					if (localVersionCode < versioncode ) {
//						��ǰ�и��µİ汾����Ҫ����һ�����صĶԻ���
//						�Ի���
//						��ʾ�Ի������漰��ҳ����£�show����ҳ����£��������߳��е��ã��ᱨ�쳣��
//						can't show dialogue inside thread that cannot call Loop.prepare();
						
//						�����߳���Handler
						Message message = new Message();
						message.what=UPDATE;
						handler.sendMessage(message);
//						showDialogue();
					}else{
//						û�и��°汾��ֱ�ӽ���������
						Message message = new Message();
						message.what=NOUPDATE;
						handler.sendMessage(message);
//						enterHome();
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Message message = new Message();
					message.what=NETERROR;
					handler.sendMessage(message);
					e.printStackTrace();
				}
				
				
			}
		}).start();
		
	}

	public void showDialogue(){
		Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle("��ǰ���°汾");
		builder.setMessage("���Ȱ��Żݶ�࣬�׿������ء���");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
//	
				download("http://192.168.119.178:8080" + url);
				System.out.println("��������ҳ�档��"+"http://192.168.119.178:8080" + url);
			}
		});
		builder.setNegativeButton("CANCLE", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
//				�������°汾��ֱ�ӽ���������
				enterHome();
			}
		});
		
		builder.show();
		
	}
//  ����������
	public void enterHome(){
		startActivity(new Intent(MainActivity.this, HomeActivity.class));
	}
//	���ء���
	private void download(String url) {
		String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/xxx.apk";
		// ���������࣬����������װ�� HttpURlConnection();
		HttpUtils httputils = new HttpUtils(5000);
		// httputils.download(�ļ����ص�ַ, ����·��, ��ȡ���ز����Ļص�);
		// RequestCallBack��ȡ���ع����еĲ���
		httputils.download(url, savePath, new RequestCallBack<File>() {
			@Override
			public void onLoading(long total, long current, boolean isUploading) {
				super.onLoading(total, current, isUploading);
				System.out.println(current + "/" + total);
				// ��ʾ������
				progressBar1.setText(current + "/" + total);
//				progress_text.setText(current + "/" + total);
			}

			// �ɹ�
			@Override
			public void onSuccess(ResponseInfo<File> info) {
				System.out.println("onSuccess" + info.result.getAbsolutePath());
				// ������װ
				showInstallDialog(MainActivity.this, info.result.getAbsolutePath());
			}
			

			// ʧ��
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				System.out.println("onFailure");

			}
		});
	}

	private void showInstallDialog(Context context, final String savePath) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		// ���ñ���
		builder.setTitle("��װ��ʾ");
		// ������ʾ
		builder.setMessage("��ǰ�����°汾��ǿ����,����װŶ...");
		// Positive ȷ��
		builder.setPositiveButton("ȷ����װ ", new OnClickListener() {// ������Ϊ����Ӧ�û�����
					@Override
					public void onClick(DialogInterface dialog, int which) {
						System.out.println("��װ....");
						Intent intent = new Intent();
						intent.setAction("android.intent.action.VIEW");// ����
						intent.addCategory("android.intent.category.DEFAULT");// ���
						Uri data = Uri.parse("file://" + savePath);
						intent.setDataAndType(data, "application/vnd.android.package-archive");
						// startActivity(intent); ���ܵ���onActivityResult�ڴ������֮��
						startActivityForResult(intent, 0);
					}
				});
		builder.setNegativeButton("���ܾ̾�", new OnClickListener() {// ������Ϊ����Ӧ�û�����
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						System.out.println("���ܾ̾�");
						enterHome();
					}
				});
		Dialog dialog = builder.create();
		dialog.show();

	}
	
	/**
	 * ����onActivityResult�ڴ������֮�� ������ҳ��ر�֮��
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		enterHome();
	}
}
