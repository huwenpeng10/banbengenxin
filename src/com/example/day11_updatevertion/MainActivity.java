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

//	作为一个Splash界面， 市面上这个界面有透明度变化的动画效果
//	还有版本号， 公司logo等信息
//	功能：1.可以从服务端获取最新版本，给用户更新
//	    2.只能公司信息
//		3.splash作为缓冲页给主页面的加载获得时间

	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case UPDATE:
				showDialogue();
				break;
			case NOUPDATE:
				enterHome();
				Toast.makeText(MainActivity.this, "已经是最新版本", 1).show();
				break;
			case NETERROR:
				enterHome();
				Toast.makeText(MainActivity.this, "网络出小差了。。。", 1).show();
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
//		0.0 透明 1.0实体  0.0--1.0
		AlphaAnimation animation = new AlphaAnimation(0.2f, 1.0f);
		animation.setDuration(3000);
//		动画的作用域
		findViewById(R.id.root).startAnimation(animation);
		
		try {
//		获取本地程序的版本号和版本名 PackageMannager
			
			PackageManager manager = this.getPackageManager();
//		得到该包名下所有包的信息 packageName
			PackageInfo info = manager.getPackageInfo("com.example.day11_updatevertion", 0);
			localVersionCode = info.versionCode;
			String versionName = info.versionName;
			System.out.println("versionCode"+localVersionCode+"versionName"+versionName);
			
			tv_version.setText("当前版本："+localVersionCode);
			
//			去服务端获取最新的版本号和本地做对比   联网
//			serverVersionCode > versionCode 有新版本，
//			弹出下载对话框，确定--进入到下载页面，取消--进入主页面
//			，<=  不用更新，直接进入主页面

		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
//		联网，服务端获取数据 获取服务端的版本信息
//		和本地的版本作对比
		
		new Thread(new Runnable() {
			
			private int versioncode;
			private String versionName;
			

			@Override
			public void run() {
				try {
					String path = "http://192.168.119.178:8080/update1/jsoninfo";
					String result = HttpUtil.getData(path);
//				result就是返回的json字符串， 解析Json数据
//				JsonObject   JsonArray————数组
					JSONObject jo = new JSONObject(result);
					versioncode = (Integer) jo.get("versioncode");
					versionName = (String) jo.get("versionName");
					url = (String) jo.get("url");
					String desc = (String) jo.get("desc");
					
					System.out.println("versionCode=="+versioncode+" versionName"+versionName
							+"url"+url);
					
//					和本地版本作对比
					if (localVersionCode < versioncode ) {
//						当前有更新的版本，需要弹出一个下载的对话框
//						对话框
//						显示对话框是涉及到页面更新（show都算页面更新），在子线程中调用，会报异常：
//						can't show dialogue inside thread that cannot call Loop.prepare();
						
//						子主线程用Handler
						Message message = new Message();
						message.what=UPDATE;
						handler.sendMessage(message);
//						showDialogue();
					}else{
//						没有更新版本，直接进入主界面
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
		builder.setTitle("当前最新版本");
		builder.setMessage("抢先版优惠多多，亲快快快下载。。");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
//	
				download("http://192.168.119.178:8080" + url);
				System.out.println("进入下载页面。。"+"http://192.168.119.178:8080" + url);
			}
		});
		builder.setNegativeButton("CANCLE", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
//				不下载新版本，直接进入主界面
				enterHome();
			}
		});
		
		builder.show();
		
	}
//  进入主界面
	public void enterHome(){
		startActivity(new Intent(MainActivity.this, HomeActivity.class));
	}
//	下载。。
	private void download(String url) {
		String savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/xxx.apk";
		// 创建核心类，联网操作封装了 HttpURlConnection();
		HttpUtils httputils = new HttpUtils(5000);
		// httputils.download(文件下载地址, 保存路径, 获取下载参数的回调);
		// RequestCallBack获取下载过程中的参数
		httputils.download(url, savePath, new RequestCallBack<File>() {
			@Override
			public void onLoading(long total, long current, boolean isUploading) {
				super.onLoading(total, current, isUploading);
				System.out.println(current + "/" + total);
				// 显示到界面
				progressBar1.setText(current + "/" + total);
//				progress_text.setText(current + "/" + total);
			}

			// 成功
			@Override
			public void onSuccess(ResponseInfo<File> info) {
				System.out.println("onSuccess" + info.result.getAbsolutePath());
				// 弹出安装
				showInstallDialog(MainActivity.this, info.result.getAbsolutePath());
			}
			

			// 失败
			@Override
			public void onFailure(HttpException arg0, String arg1) {
				System.out.println("onFailure");

			}
		});
	}

	private void showInstallDialog(Context context, final String savePath) {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		// 设置标题
		builder.setTitle("安装提示");
		// 更新提示
		builder.setMessage("当前是最新版本最强功能,请求安装哦...");
		// Positive 确定
		builder.setPositiveButton("确定安装 ", new OnClickListener() {// 监听器为了响应用户操作
					@Override
					public void onClick(DialogInterface dialog, int which) {
						System.out.println("安装....");
						Intent intent = new Intent();
						intent.setAction("android.intent.action.VIEW");// 动作
						intent.addCategory("android.intent.category.DEFAULT");// 类别
						Uri data = Uri.parse("file://" + savePath);
						intent.setDataAndType(data, "application/vnd.android.package-archive");
						// startActivity(intent); 不能调用onActivityResult在处理完毕之后
						startActivityForResult(intent, 0);
					}
				});
		builder.setNegativeButton("残忍拒绝", new OnClickListener() {// 监听器为了响应用户操作
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						System.out.println("残忍拒绝");
						enterHome();
					}
				});
		Dialog dialog = builder.create();
		dialog.show();

	}
	
	/**
	 * 调用onActivityResult在处理完毕之后 在请求页面关闭之后
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		enterHome();
	}
}
