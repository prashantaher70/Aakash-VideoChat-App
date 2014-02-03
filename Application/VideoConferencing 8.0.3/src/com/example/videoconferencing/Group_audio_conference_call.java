package com.example.videoconferencing;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.videoconferencing.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

public class Group_audio_conference_call extends Fragment implements OnClickListener {
	// address book class

	private DatabaseHandler db;
	String selected[] = new String[10];
	// contact list for the address book
	private ArrayList<Contact> contactList;
	String UIDself;
	String[] ip_list = new String[10];
	int j = 0;
	private ListView groupList;
	public Group_chat_list current;
	private int serverPort;
	public String serverIP = "";
	String rdata;
	byte data2[] = new byte[100];
	Button bGroupsend, bGroup_close, bresumeChat;

	// to fetch the server IP and sampling rate saved in the device
	private SharedPreferences settings;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		UIDself = Login.uname;
		groupList = (ListView) this.getView()
				.findViewById(R.id.groupList_audio);
		bGroupsend = (Button) this.getView().findViewById(R.id.bAudio_start);
		bGroup_close = (Button) this.getView().findViewById(R.id.bAudio_stop);

		bGroup_close.setOnClickListener(this);
		// bresumeChat.setOnClickListener(this);
		bGroupsend.setOnClickListener(this);
		serverPort = 6500;
		contactList = new ArrayList<Contact>();
		db = new DatabaseHandler(this.getActivity());

		// retrieve default settings saved in device
		settings = PreferenceManager.getDefaultSharedPreferences(this
				.getActivity());

		List<Contact> contacts = db.getAllContacts();// Fetching all the
														// contacts from the
														// database
		// get the server IP saved in device
		serverIP = settings.getString("serverIP", "10.105.14.225");

		if (db.getContactsCount() == 0)// If there is no contact
		{

		} else {
			for (Contact cn : contacts)// Looping through all the contacts
			{
				contactList.add(cn);// Adding to the contact list
			}
		}

		contactAdapter ad = new contactAdapter(this.getActivity(),
				R.layout.group_row_conf_call, contactList);
		groupList.setAdapter(ad);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.group_contacts_conf_call,
				container, false);

		return view;
	}

	public class contactAdapter extends ArrayAdapter<Contact> {
		Context context; // Stores the context where the list is to be shown
		int layoutResourceId;// Stores the layout ID of the custom layout of the
								// row
		ArrayList<Contact> data = null;// stores the data for the list

		// Constructor for the class
		public contactAdapter(Context context, int layoutResourceId,
				ArrayList<Contact> data) {
			super(context, layoutResourceId, data);// Calls the super class
													// constructor
			this.layoutResourceId = layoutResourceId;
			this.context = context;
			this.data = data;
		}

		@Override
		// Over riding the get view function so the appearance looks as we want
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			ContactHolder holder = null;

			if (row == null) {
				LayoutInflater inflater = ((Activity) context)
						.getLayoutInflater();
				row = inflater.inflate(layoutResourceId, parent, false);

				holder = new ContactHolder();
				holder.chk = (CheckBox) row.findViewById(R.id.checkBox_audio);
				// as row is null therefore sets the view with the text box id's
				row.setTag(holder);

				holder.chk.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						CheckBox cb = (CheckBox) v;
						Contact contact = (Contact) cb.getTag();
						contact.setSelected(cb.isChecked());
					}
				});
			} else {
				// as row is already set , therefore getting back the row's
				// holder
				holder = (ContactHolder) row.getTag();
			}

			// setting the holder with data
			Contact contact = data.get(position);
			holder.chk.setText(contact.name);
			holder.chk.setChecked(contact.isSelected());
			holder.chk.setTag(contact);
			View colorStrip=(View) row.findViewById(R.id.colorStrip);
			if(position%6 ==0){
	          	colorStrip.setBackgroundResource(R.color.color3);
	          }
	          else if(position%6 ==1){
	          	colorStrip.setBackgroundResource(R.color.color4);
	          }
	          else if(position%6 ==2){
	          	colorStrip.setBackgroundResource(R.color.color5);
	          }
	          else if(position%6 ==3){
	            	colorStrip.setBackgroundResource(R.color.color6);
	            }
	          else if(position%6 ==4){
	            	colorStrip.setBackgroundResource(R.color.color2);
	            }
	          else{
	          	colorStrip.setBackgroundResource(R.color.color1);
	          }
	        
			return row;
		}
	}

	// Holder class
	static class ContactHolder {
		CheckBox chk;
	}

	private class FetchAsyncIP extends AsyncTask<String, String, Void> {
		ProgressDialog dialog;

		// ObjectOutputStream oos;
		// ObjectInputStream ois;

		protected void onPreExecute() {
			dialog = new ProgressDialog(getActivity());
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.setIndeterminate(false);
			dialog.setMessage("Please wait fetching IPs' & Starting Audio Conference Call");
			dialog.show();

		}

		@Override
		protected Void doInBackground(String... id) {
			String temp_ip_uid = "";

			try {
				Log.i("GroupChat", "1");

				DatagramSocket socket = new DatagramSocket();
				DatagramPacket sendP, receiveP;
				String send;
				j = 0;
				Log.i("GroupChat", "2");
				/*for (int i = 0; i < id.length; i++)
					Log.i("GIDS", " " + id[i]);
				*/Home.group_chat_code = ""; // blank code initialise
				for (int i = 0; id[i] != null; i++) {
					// send the login message to server and set device's IP in
					// the server
					Log.i("GroupChat", "3" + id[i]);
					send = "group"+" "+Login.uname+" "+id[i];
					sendP = new DatagramPacket(send.getBytes(),send.getBytes().length,InetAddress.getByName(serverIP), serverPort);
					socket.send(sendP);
					Log.i("GroupChat", "4");
					Home.group_chat_code += id[i].charAt(0); // generating a
																// code for
																// group
					// check whether user is registered onto the server or not
					receiveP = new DatagramPacket(data2, data2.length);
					socket.receive(receiveP);
					Log.i("GroupChat", "5");
					// get the string from received bytes
					rdata = new String(receiveP.getData(), 0,
							receiveP.getLength()); // getting ip
					/*
					 * String[] pdata = new String[10]; pdata =
					 * rdata.split(" ");
					 */

					// if user not online
					if (rdata.equalsIgnoreCase("dull")) {
						Log.d("test", "offline");
					}

					// if the roll number given by user is not in server
					// database
					else if (rdata.equalsIgnoreCase("not")) {
						Log.d("test", "not registered");
						publishProgress("not");
					}// if called person is online
					else {
						// Storing the received ip in a map
						Home.UID_IP.put(id[i], rdata);
						Home.IP_Accept_Flag.put(rdata, "false");
					}
					// if called person is online

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Home.group_chat_code += Login.uname.charAt(0);
			Log.i("GCList", " " + Home.group_chat_code);
			for (Map.Entry<String, String> entry : Home.UID_IP.entrySet()) {
				Log.i("GCLIST",
						"  " + entry.getKey() + "    " + entry.getValue());
			}
			for (Map.Entry<String, String> entry : Home.IP_Accept_Flag.entrySet()) {
				Log.i("GCLIST ",
						" IP accept Flag " + entry.getKey() + "    " + entry.getValue());
			}

			for (Map.Entry<String, String> entry : Home.UID_IP.entrySet()) {
				temp_ip_uid = "";
				for (Map.Entry<String, String> entry1 : Home.UID_IP.entrySet()) {
					if (!entry1.getKey().equals(entry.getKey()))
						temp_ip_uid += entry1.getKey() + " "
								+ entry1.getValue() + " ";
				}
				temp_ip_uid += Login.uname + " " + Login.ip_self;
				temp_ip_uid = "LIST " + Home.group_chat_code + " "
						+ temp_ip_uid;

				Log.i("GroupChat", "Tmp Value : ------" + temp_ip_uid);

				SendIP si = new SendIP(temp_ip_uid, entry.getValue(),
						entry.getKey(), 35015);
				Thread th_si = new Thread(si);
				th_si.start();
				try {
					th_si.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.i("GCC","Join Error");
				}
				
				// Sending the Audio message to each thread to deliver to
				// online user
				if (!entry.getKey().equals(Login.uname)) {
					Log.i("Inside FetchIP SendAudio Message", "GCC");
						SendAudioMessage outoing_messages = new SendAudioMessage(
								entry.getValue(), "AUDIO" + " "
										+ Home.group_chat_code + " "
										+ Login.uname, 35015);
						Thread y = new Thread(outoing_messages);
						y.start();
						Log.i("GCC","Inside FetchIP SendAudio Message Thread Started");

					} else {
						Log.i("GroupChat", "UID null!!!!!");
					}
				}
				
			publishProgress("OK"); // id[0] is receiver rdata is ip of receiver
			return null;
		}

		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			if (values[0].equals("OK")) {
				// intent=new Intent(getActivity(),groupchat_activity.class);
				// startActivity(intent);
				dialog.dismiss();
				Log.i("GroupChat", values[0]);
				//Toast.makeText(getActivity(), "IP received. Conf Starting",
						//Toast.LENGTH_SHORT).show();
			}
		}
	}

	public class SendIP implements Runnable {
		String uid_ip_list, to_send_ip, to_send_uid;
		Socket socket;
		int SERVERPORT;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;

		public SendIP(String u_i, String t_s_i, String t_s_u, int port) {
			this.uid_ip_list = u_i;
			this.to_send_ip = t_s_i;
			this.SERVERPORT = port;
			this.to_send_uid = t_s_u;
		}

		@Override
		public void run() {
			socket = null;
			try {
				socket = new Socket(to_send_ip, SERVERPORT);
			} catch (UnknownHostException e) {
				Log.i("Object", "oos and ois not created");
			} catch (IOException e) {
				e.printStackTrace();
				Log.i("GCL", "SOCKET EXCEPTION " + to_send_uid);
				Home.UID_IP.remove(to_send_uid);
			}
			Log.i("GCLIST", "---------" + socket);
			if (socket != null) {
				try {
					oos = new ObjectOutputStream(socket.getOutputStream());
					ois = new ObjectInputStream(socket.getInputStream());
					Log.i("GRoup_CHAT_LIST", " Passing Data into oos"
							+ uid_ip_list + " with IP:" + to_send_ip);
					oos.writeObject(uid_ip_list);
					Log.i("GRoup_CHAT_LIST", " Passed Data into oos"
							+ uid_ip_list + " with IP:" + to_send_ip);
					oos.flush();
					oos.close();
					ois.close();
				} catch (IOException e) {
					Log.i("GCList", "Failed");
					e.printStackTrace();
				} finally {
					if (!socket.isClosed()) {
						try {
							Log.i("groupchat_list", "Closing socket");
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} else {
				Log.i("GroupChatList", "Socket Not Connected!!!!!!");
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.bAudio_start) {
			if (!Home.IP_UID_SET_FLAG) {
				// bresumeChat.setClickable(true);
				int j = 0;
				for (int i = 0; i < contactList.size(); i++) {
					Contact cont = contactList.get(i);
					if (cont.isSelected()) {
						selected[j] = cont.uid;
						j++;
						Log.i("GroupChat", cont.uid);
					}
				}

				if (j > 0) {
					Home.IP_UID_SET_FLAG = true;
					Home.record = true;
					new FetchAsyncIP().execute(selected);
					Home.initialize_udp_sockets();
					Home.ini();
					Log.i("GCC",
							"After sending AUDIO message Starting RecordSend");
					Home.RecordSend rs = new Home.RecordSend();
					Thread hrs = new Thread(rs);
					hrs.start();
					Log.i("GCC",
							"After sending AUDIO message Started RecordSend");

					Log.i("GCC",
							"After sending AUDIO message Starting PlayAudio");
					Home.PlayAudio pa = new Home.PlayAudio();
					Thread hpa = new Thread(pa);
					hpa.start();
					Log.i("GCC",
							"After sending AUDIO message Started PlayAudio");

				} else {
					Toast.makeText(getActivity(),
							"Select Some Contacts to Start Audio Conference Call",
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(getActivity(),
						"Please Close Previous Audio Conference Call First.",
						Toast.LENGTH_SHORT).show();
			}
		} else if (v.getId() == R.id.bAudio_stop) {
			if (Home.IP_UID_SET_FLAG) {
				Home.IP_UID_SET_FLAG = false;
				Home.record = false;
				/// changes
				Log.i("ON STop Click ", " Group CHat Code:"+Home.group_chat_code);
				for (Map.Entry<String, String> entry : Home.IP_Accept_Flag.entrySet()) {
					if(entry.getValue() == "true")
					{
						Log.i("GCL EXIT", " " + entry.getKey());
						Send_exit_message s_e_m = new Send_exit_message(
								entry.getKey(), Login.ip_self, Login.uname,
								35015);
						Thread sem = new Thread(s_e_m);
						sem.start();
					}
				}
				
				Home.UID_IP.clear();
				Home.IP_Accept_Flag.clear();
				GroupchatActivity.grp_message = "";
				
				//Home.group_chat_code="";
				
				Home.audioRecord.stop();
				Home.audioRecord.release();
				Home.audioTrack.stop();
				Home.audioTrack.release();
				Home.socketS.close();
				Home.sockR.close();
				
				Toast.makeText(getActivity(), "Audio Conference Ended.",
						Toast.LENGTH_SHORT).show();
							}
			else{
				Toast.makeText(getActivity(), "First Start Audio Conference Call.", Toast.LENGTH_SHORT).show();
			}
		}
	}

	public class SendAudioMessage implements Runnable {
		String ip_address;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		Socket socket;
		int SENDMESSAGEPORT;
		String message;

		// CONSTRUCTOR FOR THREAD
		public SendAudioMessage(String ip, String msg, int port) {
			this.ip_address = ip;
			this.message = msg;
			this.SENDMESSAGEPORT = port;
		}

		public void run() {
			socket = null;
			try {
				Log.i("GroupChatActivity Thread", " Inside Socket");
				socket = new Socket(ip_address, SENDMESSAGEPORT);
				Log.i("GroupChatActivity Thread",
						" Socket Created oos and ois " + socket);
			} catch (UnknownHostException e) {
				Log.i("Object", "oos and ois not created");
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (socket != null) {
				try {
					oos = new ObjectOutputStream(socket.getOutputStream());
					ois = new ObjectInputStream(socket.getInputStream());
					Log.i("ChatGroupActivity Thread", " OOS" + message
							+ " with IP: " + ip_address);
					oos.writeObject(message);
					oos.flush();
					oos.close();
					ois.close();
				}

				catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (!socket.isClosed()) {
						try {
							Log.i("groupchat_activity", "Closing socket");
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}// FINALLY
			}// IF SOCKET NOT NULL CLOSED
		}// RUN METHOD CLOSED
	}// CLASS CLOSED FOR SENDING MESSAGE

	public class Send_exit_message implements Runnable 
	{
		String self_uid, self_ip, to_send_ip;
		Socket socket;
		int SERVERPORT;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;

		public Send_exit_message(String t_s_ip, String self_ip,
				String self_uid, int port) {
			this.self_ip = self_ip;
			this.to_send_ip = t_s_ip;
			this.self_uid = self_uid;
			this.SERVERPORT = port;
		}

		@Override
		public void run() {
			socket = null;
			try {
				socket = new Socket(to_send_ip, SERVERPORT);
			} catch (UnknownHostException e) {
				Log.i("Object", "oos and ois not created");
			} catch (IOException e) {
				Log.i("GCL EXIT Socket error", " " + socket);
				e.printStackTrace();

			}
			Log.i("GCLIST", "---------" + socket);
			if (socket != null) {
				try {
					
					oos = new ObjectOutputStream(socket.getOutputStream());
					ois = new ObjectInputStream(socket.getInputStream());
					Log.i("GACC", "SENd to "+ to_send_ip+" ");
					oos.writeObject("EXIT" + " " + Home.group_chat_code + " "
							+ self_uid + " " + self_ip);
					Log.i("GACC", "Message "+" "+"EXIT" + " " + Home.group_chat_code + " "
							+ self_uid + " " + self_ip);
					oos.flush();
					oos.close();
					ois.close();

					// closing connections of conferencing

					/*Home.audioRecord.stop();
					Home.audioRecord.release();
					Home.audioTrack.stop();
					Home.audioTrack.release();
*/
				} catch (IOException e) {
					Log.i("GCList", "Failed");
					e.printStackTrace();
				} finally {
					if (!socket.isClosed()) {
						try {
							Log.i("groupchat_list", "Closing socket");
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					if (!Home.socketS.isClosed()) {
						try {
							Log.i("groupchat_list", "Closing socket");
							Home.socketS.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (!Home.sockR.isClosed()) {
						try {
							Log.i("groupchat_list", "Closing socket");
							Home.sockR.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} else {
				Log.i("GroupChatList", "Socket Not Connected!!!!!!");
			}
		}
	}
}
