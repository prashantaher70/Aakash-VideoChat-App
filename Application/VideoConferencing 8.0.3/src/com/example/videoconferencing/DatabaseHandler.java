package com.example.videoconferencing;

import java.util.ArrayList;
import java.util.List;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper 
{

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "contactsManager";

	// Contacts table name
	private static final String TABLE_CONTACTS = "contacts";

	// Contacts Table Columns names
	private static final String KEY_ID = "uid";
	private static final String KEY_NAME = "name";
	private static final String KEY_OWNER = "owner";
	
	public DatabaseHandler(Context context) 
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	
	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		/*String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
				+ KEY_ID + " TEXT PRIMARY KEY," + KEY_NAME + " TEXT," + KEY_OWNER + " TEXT)";
		db.execSQL(CREATE_CONTACTS_TABLE);*/
		String CREATE_CONTACTS_TABLE = "CREATE TABLE contacts ( uid TEXT, name TEXT, owner TEXT, PRIMARY KEY (uid, owner))";
		db.execSQL(CREATE_CONTACTS_TABLE);
	}

	
	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

		// Create tables again
		onCreate(db);
	}

	
	// Adding new contact
	// Input :a contact class type object
	// Output:NOne
	void addContact(Contact contact) 
	{
		//open the database
		SQLiteDatabase db = this.getWritableDatabase();
		
		//create a bundle
		ContentValues values = new ContentValues();
		values.put(KEY_ID, contact.getuid()); // Contact uid
		values.put(KEY_NAME, contact.getName()); // Contact Name
		values.put(KEY_OWNER, Login.uname);

		// Inserting Row
		db.insert(TABLE_CONTACTS, null, values);
		db.close(); // Closing database connection
	}

	// Getting single contact
	// Input: uid of the contact sought
	// Output:contact class type object
	Contact getContact(String id) 
	{
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,
				KEY_NAME}, KEY_ID + "=?",
				new String[] {id }, null, null, null, null);//Query to find the contact with specified uid
		if (cursor != null)
			cursor.moveToFirst();

		Contact contact = new Contact((cursor.getString(0)),
				cursor.getString(1));//Creating the contact type object to return
		
		cursor.close();
		db.close();//Closing database connection
		return contact;
	}
	
	// Getting All Contacts
	// Input : None
	// Output: contact type list containing all the contacts
	public List<Contact> getAllContacts() 
	{
		List<Contact> contactList = new ArrayList<Contact>();
		
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS + " WHERE " + KEY_OWNER + " = '"+Login.uname+"'";

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);//Executing the query

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				Contact contact = new Contact();
				contact.setuid((cursor.getString(0)));
				contact.setName(cursor.getString(1));
				
				// Adding contact to list
				contactList.add(contact);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();//Closing database connection
		// return contact list
		return contactList;
	}

	// Updating single contact
	// Input :contact type object
	// Output:Number of rows affected
	public int updateContact(Contact contact) 
	{
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_ID, contact.getuid());
		values.put(KEY_NAME, contact.getName());
		values.put(KEY_OWNER, Login.uname);

		// updating row
		return db.update(TABLE_CONTACTS, values, KEY_ID + " = ?",
				new String[] { contact.getuid() });
	}

	// Deleting single contact
	// Input : uid of that contact which should be deleted
	// Output: 1 if successful 
	//     	   0 if the contact does not exist
	public int deleteContact(String x) 
	{
		SQLiteDatabase db = this.getWritableDatabase();
		//String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS +" WHERE "+KEY_ID+" = '"+x+"'";
		
		String selectQuery = "SELECT  * FROM contacts WHERE uid = '"+x+"' AND owner = '"+Login.uname+"'";
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor.getCount() == 0)//checking whether contact exist or not
		{
			db.close();
			return(0);//returns 0 if contact does not exist
		}
		
		String deleteQuery = "DELETE FROM contacts WHERE uid = '"+x+"' AND owner = '"+Login.uname+"'";
		db.execSQL(deleteQuery);
		db.close();
		return(1);//returns 1 if deletion is successful
	}


	// Getting contacts Count
	//Input : none
	//Output : total number of contacts
	public int getContactsCount() 
	{
		String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int i;
		i= cursor.getCount();
		cursor.close();
		// return count
		return i;
	}
	
	//Checks whether a particular contact exists or not
	//Input : uid number
	//Output: 0 if does not exist
	//        1 if exists
	int uidexist(String id) 
	{
		SQLiteDatabase db = this.getReadableDatabase();

		/*Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID,
				KEY_NAME}, KEY_ID + "=?",
				new String[] {id }, null, null, null, null);*/
		String selectQuery = "SELECT  * FROM contacts WHERE uid = '"+id+"' AND owner = '"+Login.uname+"'";
		Cursor cursor = db.rawQuery(selectQuery, null);
		if (cursor.getCount() == 0)
		{
			cursor.close();
			db.close();
			return(0);
		}
		else
		{
			cursor.close();
			db.close();
			return(1);
		}
	}

}
