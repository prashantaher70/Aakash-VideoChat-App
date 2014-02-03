package com.example.videoconferencing;

public class Contact 
{
	
	//private variables
	 String uid;
	 String name;
	 boolean checked=false;
	
	// Empty constructor
	public Contact()
	{
		
	}
	// constructor
	public Contact(String id, String name)
	{
		this.uid = id;
		this.name = name;
	}
	
	
	// getting uid
	public String getuid()
	{
		return this.uid;
	}
	
	// setting uid
	public void setuid(String id)
	{
		this.uid = id;
	}
	
	// getting name
	public String getName()
	{
		return this.name;
	}
	
	// setting name
	public void setName(String name)
	{
		this.name = name;
	}
	public void setSelected(boolean selected)
	{
		this.checked=selected;
		
	}
	public boolean isSelected()
	{
		return checked;
	}
	
}
