package com.example.videoconferencing;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;

import com.example.videoconferencing.R;

import android.app.AlertDialog;

import android.app.ListActivity;

import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;

import android.view.View;

import android.widget.ArrayAdapter;

import android.widget.ListView;

import android.widget.TextView;

public class Explorer extends ListActivity {

	private List<String> item = null;
	private List<String> path = null;
	private String root = "/";
	private TextView myPath;

	/** Called when the activity is first created. */

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.explorer);
		myPath = (TextView) findViewById(R.id.path);
		getDir(root);
	}

	private void getDir(String dirPath)

	{
		myPath.setText("Location: " + dirPath);
		item = new ArrayList<String>();
		path = new ArrayList<String>();
		File f = new File(dirPath);
		File[] files = f.listFiles();
		if (!dirPath.equals(root))
		{
			item.add(root);
			path.add(root);
			item.add("../");
			path.add(f.getParent());
		}
		for (int i = 0; i < files.length; i++)
		{
			File file = files[i];
			path.add(file.getPath());
			if (file.isDirectory())
				item.add(file.getName() + "/");
			else
				item.add(file.getName());
		}
		Arrays.sort(files);
		ArrayAdapter<String> fileList =
		new ArrayAdapter<String>(this, R.layout.explorer_row, item);
		setListAdapter(fileList);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String file_path;
		File file = new File(path.get(position));
		if (file.isDirectory())
		{
			if (file.canRead()){
				getDir(path.get(position));
				
			}
			else
			{
				new AlertDialog.Builder(this)
				.setIcon(R.drawable.ic_launcher)
				.setTitle("[" + file.getName() + "] Access Denied!!!")
				.setPositiveButton("Back",
				new DialogInterface.OnClickListener() {
					@Override
				public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				}).show();
			}
		}
		else
		{
			
			file_path=file.getPath();
			Intent data=new Intent(this,FileShare.class);
			data.putExtra("filePath", file_path);
			
			setResult(RESULT_OK,data);
			this.finish();		
			
			/*
			new AlertDialog.Builder(this)
			.setIcon(R.drawable.ic_launcher)
			.setTitle("[" + file.getName() + "]")
			.setPositiveButton("OK",
			new DialogInterface.OnClickListener() {
				@Override
			public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					
					
				}
			}).show();*/
		}
	}
}