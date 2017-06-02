
package com.example.day11_updatevertion;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

public class HomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		TextView view = new TextView(this);
		view.setText("大家好，我是主页面");
		view.setTextSize(25);
		view.setTextColor(Color.RED);
		
//		是页面展示视图
		setContentView(view);
	}
}
