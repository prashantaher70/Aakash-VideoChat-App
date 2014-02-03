/**
 *
 * @author Sumantra
 */
import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JTextArea;

/*
ping the online clients to check if they are still connected to server
if not, update database and mark them as off line
*/	
public class OnlineChecker implements Runnable
{
	
	//result set of a query to database
	private ResultSet rr;
	//a new thread 
	private Thread tOl9chkr;
	//database connection
	private Connection con;
	//shows online and off line users
	private ListUsers list;
	//shows online users
	private JTextArea onlineArea;
	//shows off line users
	private JTextArea offlineArea;
	//server log
	private JTextArea log;
	
	public OnlineChecker(Connection con,ListUsers listUsers,JTextArea log)
	{
		//create and execute a new thread for polling
		tOl9chkr=new Thread(this);
		tOl9chkr.start();
		
		this.con = con;
		this.log = log;
                log.setEditable(false);
		
		list = listUsers;
		
		//get reference to online text area
		onlineArea = list.getOnlineArea();
		//get reference to off line text area
		offlineArea = list.getOfflineArea();
		//set text color to green
		onlineArea.setForeground(Color.green);
		//get reference to off line area
		offlineArea = list.getOfflineArea();
		//set text color to gray
		offlineArea.setForeground(Color.red);
	}
	
	public void run()
	{
		//shows the registered users all off line at this time
		refreshList();
		//statement to query DB for list of online clients
		Statement stmt11 = null;            
		//statement to update clients as off line
		PreparedStatement stmt12;           
		
		try
		{
			//Establish DB connection
			stmt11=con.createStatement();
		}
		catch(SQLException se)
		{
			log.append("ERROR : " + se.getMessage() + "\n");
		}
		
		//continuously get the IP add of logged-in clients and ping them every 5 secs
		while(true)
		{
			try
			{
				//wait for 1 second       
				
					tOl9chkr.sleep(1000);
				
				
				//Execute select IP address from table
				rr = stmt11.executeQuery("Select ip_add from address where ip_add LIKE '%.%';");

				//for each IP address in the result set
				while(rr.next())
				{
					//assign the value of result set to ipAddress
					String ipAddress = rr.getString(1); 
					//get the IP address from string
					InetAddress inet = InetAddress.getByName(ipAddress);
					//Timeout = 3 mins
					//boolean status = inet.isReachable(300000);
					
					//if User is off line
					//if (!status)
					/*{
						//remove IP Address from database
						stmt12 = con.prepareStatement("Update address set ip_add = NULL where ip_add = '"+ipAddress+"';");
						stmt12.executeUpdate();
                                                stmt12 = con.prepareStatement("Update address set busy = 'N' where ip_add = '"+ipAddress+"';");
						stmt12.executeUpdate();
                                                
					}*/
                                        
				}
				//refresh the text area
				refreshList();
			}
			catch(SQLException sq)
			{
				log.append("ERROR : " + sq.getMessage() + "\n");
			}
			catch(InterruptedException e)
			{
				log.append("ERROR : " + e.getMessage() + "\n");
			}
			catch (UnknownHostException e)
			{
				log.append("ERROR : Host does not exist\n");
			}
			catch (IOException e)
			{
				log.append("ERROR : Error in reaching the Host\n");
			}
		}
	}
	
	/*
	 * this method will set the registered users as online and off line
	 * it is called after every 5 SECS 
	 */
	public void refreshList()
	{
		//statement to query DB for list of online clients
		Statement stmt11 = null;            
		try
		{
			//Establish DB connection
			stmt11=con.createStatement();
		}
		catch(SQLException se)
		{
			log.append("ERROR : " + se.getMessage() + "\n");
		}
		
		//set the area as default
		offlineArea.setText("");
		onlineArea.setText("");
		
		try 
		{
			//select IP address and name from database
			rr = stmt11.executeQuery("Select username,ip_add from address;");
			//check for all IP addresses
			while(rr.next())
			{
				//if user is off line
				if(rr.getString(2)==null)
				{
					offlineArea.append(rr.getString(1) + " : OFFLINE\n");
				}
				//if user is online
				else
				{
					onlineArea.append(rr.getString(1) + " : ONLINE\n");
				}
			}
		} 
		catch (SQLException e) 
		{
			log.append("ERROR : " + e.getMessage() + "\n");
		}
	}
}
