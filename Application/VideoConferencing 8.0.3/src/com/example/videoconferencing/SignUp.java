package com.example.videoconferencing;

import com.example.videoconferencing.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class SignUp extends Activity implements OnClickListener
{

	@Override
	public void onClick(View arg0) 
	{
		setContentView(R.layout.sign_up);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

}
