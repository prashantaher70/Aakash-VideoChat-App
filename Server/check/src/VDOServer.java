
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//@author Sumantra
public class VDOServer implements Runnable{

//variables required    
    //packet sent by server to clients
	private DatagramPacket sendPacket;         	
	//array of received data packets
	private DatagramPacket receivePacket[];     
	//UDP socket
	public	DatagramSocket socket;              
	//connection to SQL server
	private Connection con;                    
	// query to update DB
	private PreparedStatement stmt;            
	//to query the database
	private Statement stmt1;                    
	//result set of a query to database
	//private ResultSet rs;  
	//the data packet to be processed
	private ProcessPacket packet;
        //no. of threads executing from thread pool
	private int count;                                         
	//select a thread from a thread pool and execute
	private ExecutorService threadExecutor;
        
        //stores host name
	private String host;
	//stores database name
	private String dname;
	//stores user name
	private String username;
	//stores password
	private String password;
	
	//parse the XML file
	private DocumentBuilderFactory docFactory;
	private DocumentBuilder docBuilder;
	private Document doc;
	private NodeList nList;
        
        //this class thread
	private Thread th;
	//server log
	private JTextArea log;
   
        //the constructor of this class 
	public VDOServer(JTextArea log,ListUsers listUsers)
	{
                this.log = log;
		//create a thread pool
		threadExecutor = Executors.newCachedThreadPool();
                //read the values stored in XML file
		readXml();
                //will receive data gram requests from cleints 
		receivePacket = new DatagramPacket[200];
                count = 0;
		try	
			{	
				//load MYSQL server JDBC driver
				Class.forName("com.mysql.jdbc.Driver");
				//obtaining a connection to SQL server
				con = DriverManager.getConnection("jdbc:mysql://"+host+":3306/"+dname+"?user="+username+"&password="+password);		
				stmt1 = con.createStatement();
				
				//creates a socket and binds it to port 6500
				socket = new DatagramSocket(6500);
			}	
			catch(SocketException socketException)
			{	
				log.append("ERROR : " + socketException.getMessage() + "\nRestart the server or try to free the port : 6500\n");
			}
			catch(ClassNotFoundException ce)
			{
				log.append("ERROR : " + ce.getMessage() + "\n");
			}
			catch(SQLException se)
			{
				log.append("ERROR : " + se.getMessage() + "\n");
			}
                //check if any one is present in table address
                checktable();
                         //set all the users off line initially
			//setAllOffline();
                         //start servertime class with the constructor
                             new ServerTime(log);
       
                         //start pinging the online clients
                            new OnlineChecker(con,listUsers,log);
                            th = new Thread(this);
                            th.start();
	}

    public VDOServer() {
        
    }
        /*
	 * utility method to set all the users off line initially is server was
	 * closed abnormally last time
	 */
	public void setAllOffline()
	{
		//statement to query DB for list of online clients
		PreparedStatement stmt = null;            
		
		try 
		{
                    
			//set all IP's as null
			stmt = con.prepareStatement("update address set ip_add = NULL");
			stmt.executeUpdate();
                        //set busy field as 0
                        stmt = con.prepareStatement("update address set busy = 'N'");
			stmt.executeUpdate();
		} 
		catch (SQLException e) 
		{
			log.append("ERROR : " + e.getMessage() + "\n");
		}
	}
        
        public void stop()
	{	
		packet = null;
		count--;
	}

    public void readXml()
	{
		File xmlFile = new File("Server.xml");
		docFactory = DocumentBuilderFactory.newInstance();
		try 
		{
			docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.parse(xmlFile);
			nList = doc.getElementsByTagName("database");
				
			//read all tag values from XML file
			for (int temp = 0; temp < nList.getLength(); temp++) 
			{
				
				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) 
				{
					
					Element eElement = (Element) nNode;
					
					host = getTagValue("host", eElement);
					dname = getTagValue("dname", eElement);
					username = getTagValue("username", eElement);
					password = getTagValue("password", eElement);
				}	
			}	
				
		} 
		catch (ParserConfigurationException e) 
		{
			log.append("ERROR : " + e.getMessage() + "\n");
		}
		catch (SAXException e) 
		{
			log.append("ERROR : " + e.getMessage() + "\n");
		}
		catch (IOException e) 
		{
			log.append("ERROR : " + e.getMessage() + "\n");
		}
	}	
		
	/*
	 * utility method called by readXml() method
	 */
	private static String getTagValue(String sTag, Element eElement) 
	{
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = (Node) nlList.item(0);
		//return the tag value requested
		return nValue.getNodeValue();
	}
		
	/*
	 * utility method to return host name
	 */
	public String getHost()
	{
		return host;
	}
		
	/*
	 * utility method to return database name
	 */
	public String getDName()
	{
		return dname;
	}
		
	/*
	 * utility method to return user name
	 */
	public String getUsername()
	{
		return username;
	}
		
	/*
	 * utility method to return password
	 */
	public String getPassword()
	{
		return password;
	}

	@Override
	public void run() 
	{
		//buffer to store reply data
		byte data[] = new byte[100];        
			
		//continuously receive and process connection requests at port 6000
		while(true)
		{
			try
			{		
				receivePacket[count] = new DatagramPacket(data,data.length);	
				socket.receive(receivePacket[count]);
				packet = new ProcessPacket(receivePacket[count]);
				
				//call a thread to process the received packet
				threadExecutor.execute(packet);
				
				//update the no. of threads currently running
				count++;
				
				//abort the thread
				stop();
				
			}
			catch(IOException ioException)
			{
				log.append("ERRORinreadxml : " + ioException.getMessage() + "\n");
			}
		}
        }

    private void checktable() {
        try
        {
            stmt = con.prepareStatement("Select username from address");
            ResultSet rs = stmt.executeQuery();
            if(rs.next())
            {
             //means at least one is present
                setAllOffline();
            }
        }catch(SQLException sql)
        {
            log.append("ERROR : "+ sql.getMessage());
        }
    }
        
	//a sub class which is used to process the packet
	private class ProcessPacket implements Runnable
	{
		//message contained in received packet
		private String rdata;           
		//received data gram packet
		private DatagramPacket receivePacket;
		//store the constituent words in received message as array of strings
		private String pdata[];           
		//string of IP & MAC add of requested client
		private String ipmac;      
                //MAC address pattern
                private String pattern = "([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}";
        private boolean timestamp;
		
		public ProcessPacket(DatagramPacket receivePacket1)
		{
			receivePacket = receivePacket1;
			rdata = new String(receivePacket1.getData(),0,receivePacket1.getLength());
			pdata = rdata.split(" ");
		}
			
		/*
        *get the server current time
        *if the time stamp of received packet is before the server time-->message is discarded
        *else, depending on type of message , the client is 
        *logged-in
        *logged-out
        *sent the IP & MAC add of the requested client 
        */
            @Override
		public void run()
		{	    
			//get the current server-time in milliseconds
                        timestamp = true;
                    //long receivedTimeStamp=System.currentTimeMillis()+10000; 
			//log.append(String.valueOf(receivedTimeStamp));
			/*try
			{
				//get the time stamp of the received message
				receivedTimeStamp = Long.parseLong(pdata[pdata.length - 1]);
                                log.append(String.valueOf(receivedTimeStamp)+"\n");
			}
			catch (Exception e) 
			{
				log.append("ERROR : Time incorrect Exception: " + e.getMessage() + "\n");
			}
			*/
			//if server time is before time stamp of received message
			//if(System.currentTimeMillis()<receivedTimeStamp)
			if(timestamp)
                        {
				//client is requesting for login
				if(pdata[0].equalsIgnoreCase("login"))
				{	
                                  //login the client
                                    log.append("LOGIN REQUEST \n");
                                    //loginMethod();
                                    loginMethod1();
                                }
                                else if(pdata[0].equalsIgnoreCase("file"))
                                {
                                    //file transfer request
                                    log.append("File Transfer Request \n");
                                    ipSendMethod();
                                }
                                else if(pdata[0].equalsIgnoreCase("chat"))
                                {
                                    //file transfer request
                                    log.append("Chat Request \n");
                                    ipSendMethod();
                                }
                                else if(pdata[0].equalsIgnoreCase("password"))
                                {
                                    //password change request
                                    log.append("Change Password Request \n");
                                    changePasswordMethod();
                                }
                                else if(pdata[0].equalsIgnoreCase("audio"))
                                {
                                    //file transfer request
                                    log.append("Audio Call Request \n");
                                    ipSendMethod();
                                }
                                else if(pdata[0].equalsIgnoreCase("video"))
                                {
                                    //file transfer request
                                    log.append("Video Call Request \n");
                                    ipSendMethod();
                                }
                                else if(pdata[0].equalsIgnoreCase("logout"))
                                {
                                    //password change request
                                    log.append("Logout Request \n");
                                    logoutMethod();
                                }
                                //to free the user
                                else if(pdata[0].equalsIgnoreCase("free"))
                                {
                                    log.append(pdata[0]+" me Request \n");
                                    freeUserMethod();
                                }
                                else if(pdata[0].equalsIgnoreCase("group"))
                                {
                                    log.append(pdata[0]+" me Request \n");
                                    groupIpMethod();
                                }
                        }
                        else
                        {
                            log.append("Outdated message\n");
                        }
		}
		/*
		 *
         *mark the the client as online
         *update the IP add in DB to the current IP add of the client
         * 
         */
		public synchronized void loginMethod()
		{               
			try
			{
                            //checking wheter it is duplicate login or not
                                stmt = con.prepareStatement("Select ip_add from address where username= '"+pdata[3]+"';");
				ResultSet rs0 = stmt.executeQuery();
                               if(rs0.next())
                               {
                                    String ip = rs0.getString("ip_add");
                                        if(rs0.wasNull())
                                {
                                    log.append("LOGIN from " +pdata[3]+" ( " +pdata[1]+ " )\n");
                                    // stmnt to check the password for the username
                                stmt = con.prepareStatement("Select password from address where username= '"+pdata[3]+"';");
				ResultSet rs = stmt.executeQuery();
                                rs.next();
				//if the client is registered
                                                if(rs.getString(1).equals(pdata[4]))
                                                {
                                                    //update the IP add of the client
                                stmt = con.prepareStatement("Update address set ip_add = '"+pdata[1]+"' where username = '"+pdata[3]+"';");
				int i =stmt.executeUpdate();
                                //update the mac id of the client
                                stmt = con.prepareStatement("Update address set mac_add = '"+pdata[2]+"' where username = '"+pdata[3]+"';");
				int j =stmt.executeUpdate();
                                if(i>0&&j>0)
                                {
                                                    ipmac = "registered";
                                             
                                                    log.append(" user is registered\n");
                                 
                                }
                                else
				{
					ipmac = "not";
                                       
					log.append(" username not registered\n");
				}
                                                } 
                                                else{
                                                    ipmac ="wp";
                                                    
                                                    log.append("wrong password\n");
                                                }
                                
                                
                                
                                 }
                                        else
                                        {
                                           //duplicate login
                                            ipmac = "loggedin";
                                            log.append(" Duplicate Login request");
                                        }
                                        packetSendMethod(ipmac);
                        }
                        }
			catch(SQLException se)
			{
				log.append("ERROR : " + se.getMessage() + "\n");
			}
			catch(IOException ie)
			{
				log.append("ERROR : " + ie.getMessage() + "\n");
			}
			catch(Exception e)
                        {
                                log.append("ERROR : " + e.getMessage() + "\n");
                        }
			//notify that this thread has completed its task, and other can now access the DB
			notifyAll();
		}

        private synchronized void ipSendMethod()
        {  
                        try
			{       
                                log.append("Ip Request from " +pdata[1]+" to "+pdata[2]+"\n");
                                stmt = con.prepareStatement("Select username from address where username= '"+pdata[2]+"';");
				ResultSet rs1 = stmt.executeQuery();                          
                                if(!rs1.
                                        next())
                                {
                                    //invalid username
                                    log.append("invalid user \n");
                                    ipmac = "not";
                                }
                                else
                                {
                                    //valid username
                                    //check for online
                                    stmt = con.prepareStatement("Select ip_add from address where username= '"+pdata[2]+"';");
                                    ResultSet rs2 = stmt.executeQuery();
                                    //log.append(rs2.getString("ip_add"));
                                    if(rs2.next())
                                    {
                                        String ip = rs2.getString("ip_add");
                                        if(rs2.wasNull())
                                        {
                                            log.append("inside if rs.wasnull() ie user offline");
                                            ipmac = "dull";
                                            log.append(ipmac+ "\n");
                                        }
                                        else
                                        {
                                            log.append("inside else rs.wasnull() ie user online");
                                            //check whether he is busy or not
                                            stmt = con.prepareStatement("Select busy from address where username= '"+pdata[2]+"';");
                                            ResultSet rs3 = stmt.executeQuery();
                                            rs3.next();
                                            if(rs3.getString("busy").equals("N"))
                                            {
                                                ipmac = ip;
                                                log.append(ipmac+ "\n");  
                                                //make the callie and caller busy
                                                stmt = con.prepareStatement("Update address set busy = 'Y' where username = '"+pdata[1]+"';");
                                                stmt.executeUpdate();
                                                stmt = con.prepareStatement("Update address set busy = 'Y' where username = '"+pdata[2]+"';");
                                                stmt.executeUpdate();
                                            }
                                            else if(rs3.getString("busy").equals("Y"))
                                            {
                                                ipmac = "busy";
                                                log.append(ipmac+ "\n");
                                            }
                                        }
                                    }
                                 }
                                    packetSendMethod(ipmac);
                        }
                        catch(SQLException se)
			{
				log.append("ERRORsql: " + se.getMessage() + "\n");
			}
			catch(IOException ie)
			{
				log.append("ERRORio : " + ie.getMessage() + "\n");
			}
			catch(Exception e)
                        {
                                log.append("ERRORgen : " + e.getMessage() + "\n");
                        }
			//notify that this thread has completed its task, and other can now access the DB
			notifyAll();
			
        }
        
        void packetSendMethod(String ipmac) throws IOException
        {
            byte buffer[] = new byte[200];
            buffer = ipmac.getBytes();
            //from the data gram packet
            sendPacket = new DatagramPacket(buffer,
                                            buffer.length,
                        		receivePacket.getAddress(),
					receivePacket.getPort() );
						//send this packet
            socket.send(sendPacket);
            log.append(ipmac+ " this is the data sent\n");
        }
        private synchronized void changePasswordMethod() {
            		try
			{
                                //pdata1 me from, pdata2 me old password pdata3 me new password pdata4 me new password given again
				log.append("Password Change Request from " +pdata[1]+"\n");
                                stmt = con.prepareStatement("Select password from address where username= '"+pdata[1]+"';");
				ResultSet rs1 = stmt.executeQuery();  
                                rs1.next();
                                if(rs1.getString(1).equals(pdata[2]))
                                {
                                    //invalid username
                                    log.append("Given old password is correct\n");
                                    if(pdata[3].equals(pdata[4]))
                                    {
                                        stmt = con.prepareStatement("Update address set password = '"+pdata[3]+"' where username = '"+pdata[1]+"';");
                                        stmt.executeUpdate();
                                        ipmac = "change";
                                    }
                                    else
                                        ipmac = "mismatch";
                                }
                                else
                                    ipmac = "wrongold";
                                packetSendMethod(ipmac);
                          }
                        catch(SQLException se)
			{
				log.append("ERRORsql: " + se.getMessage() + "\n");
			}
			catch(IOException ie)
			{
				log.append("ERRORio : " + ie.getMessage() + "\n");
			}
			catch(Exception e)
                        {
                                log.append("ERRORgen : " + e.getMessage() + "\n");
                        }
			//notify that this thread has completed its task, and other can now access the DB
			notifyAll();
	
	
        }

        private synchronized void logoutMethod() {
            try
			{
                                //pdata1 me from
                                log.append("Logout Request from " +pdata[1]+"\n");
                                stmt = con.prepareStatement("Update address set ip_add = null where username = '"+pdata[1]+"';");
                                int i = stmt.executeUpdate();
                                stmt = con.prepareStatement("Update address set busy = 'N' where username = '"+pdata[1]+"';");
                                stmt.executeUpdate();
                                if(i>0){
                                    ipmac="logout"; 
                                    packetSendMethod(ipmac);
                                }
                          }
                        catch(SQLException se)
			{
				log.append("ERRORsql: " + se.getMessage() + "\n");
			}
			catch(IOException ie)
			{
				log.append("ERRORio : " + ie.getMessage() + "\n");
			}
			catch(Exception e)
                        {
                                log.append("ERRORgen : " + e.getMessage() + "\n");
                        }
			//notify that this thread has completed its task, and other can now access the DB
			notifyAll();
	
	
        }

        private void freeUserMethod() {
                                 log.append("Free me Request from " +pdata[1]+"\n");
                try {
                    stmt = con.prepareStatement("Update address set busy = 'N' where username = '"+pdata[1]+"';");
                    int i=stmt.executeUpdate();
                    if(i>0)
                        ipmac = "free";
                    packetSendMethod(ipmac);
                    log.append("User freed from server side");
                    
                } catch (SQLException ex) {
                    log.append("ERROR: "+ ex.getMessage());
                }
                catch (IOException ex) {
                    log.append("ERROR: "+ ex.getMessage());
                }
        }

        private void groupIpMethod() {
            try
			{       
                                log.append("Ip Request from " +pdata[1]+" to "+pdata[2]+"\n");
                                stmt = con.prepareStatement("Select username from address where username= '"+pdata[2]+"';");
				ResultSet rs1 = stmt.executeQuery();                          
                                if(!rs1.next())
                                {
                                    //invalid username
                                    log.append("invalid user \n");
                                    ipmac = "not";
                                }
                                else
                                {
                                    //valid username
                                    //check for online
                                    stmt = con.prepareStatement("Select ip_add from address where username= '"+pdata[2]+"';");
                                    ResultSet rs2 = stmt.executeQuery();
                                    //log.append(rs2.getString("ip_add"));
                                    if(rs2.next())
                                    {
                                        String ip = rs2.getString("ip_add");
                                        if(rs2.wasNull())
                                        {
                                            log.append("inside if rs.wasnull() ie user offline");
                                            ipmac = "dull";
                                            log.append(ipmac+ "\n");
                                        }
                                        else
                                        {
                                            log.append("inside else rs.wasnull() ie user online");
                                            ipmac = ip;
                                           /* //check whether he is busy or not
                                            stmt = con.prepareStatement("Select busy from address where username= '"+pdata[2]+"';");
                                            ResultSet rs3 = stmt.executeQuery();
                                            rs3.next();
                                            if(rs3.getString("busy").equals("N"))
                                            {
                                                ipmac = ip;
                                                log.append(ipmac+ "\n");  
                                                //make the callie and caller busy
                                                stmt = con.prepareStatement("Update address set busy = 'Y' where username = '"+pdata[1]+"';");
                                                stmt.executeUpdate();
                                                stmt = con.prepareStatement("Update address set busy = 'Y' where username = '"+pdata[2]+"';");
                                                stmt.executeUpdate();
                                            }
                                            else if(rs3.getString("busy").equals("Y"))
                                            {
                                                ipmac = "busy";
                                                log.append(ipmac+ "\n");
                                            }
                                            */
                                        }
                                    }
                                 }
                                    packetSendMethod(ipmac);
                        }
                        catch(SQLException se)
			{
				log.append("ERRORsql: " + se.getMessage() + "\n");
			}
			catch(IOException ie)
			{
				log.append("ERRORio : " + ie.getMessage() + "\n");
			}
			catch(Exception e)
                        {
                                log.append("ERRORgen : " + e.getMessage() + "\n");
                        }
			
			
        }
	
        public synchronized void loginMethod1()
        {
            try{
                log.append("from "+pdata[3]+"\n");
             stmt = con.prepareStatement("Select username from address where username = '"+pdata[3]+"';");
             ResultSet rs = stmt.executeQuery();
             
             if(rs.next())
                               {
                                   
                                    String un = rs.getString("username");
                                        if(rs.wasNull())
                                        {
                                            ipmac = "not";
                                            
                                        }
                                        else
                                        {
                                            stmt = con.prepareStatement("Select password from address where username = '"+pdata[3]+"';");
                                             ResultSet rs1 = stmt.executeQuery();
                                             rs1.next();
                                             
                                             if(!(rs1.getString("password").equals(pdata[4])))
                                             {
                                                 ipmac = "wp";
                                                 
                                             }
                                             else
                                             {
                                                 
                                                 stmt = con.prepareStatement("Select ip_add from address where username = '"+pdata[3]+"';");
                                             ResultSet rs2 = stmt.executeQuery();
                                             
                                             if(rs2.next())
                                             {
                                                 
                                                 String ip = rs2.getString("ip_add");
                                                         if(rs2.wasNull())
                                                         {
                                                                  ipmac = "registered";
                                                                  //update the IP add of the client
                                                                    stmt = con.prepareStatement("Update address set ip_add = '"+pdata[1]+"' where username = '"+pdata[3]+"';");
                                                                    stmt.executeUpdate();
                                                                    //update the mac id of the client
                                                                    stmt = con.prepareStatement("Update address set mac_add = '"+pdata[2]+"' where username = '"+pdata[3]+"';");
                                                                    stmt.executeUpdate();
                                                                    //update busy field
                                                                    stmt = con.prepareStatement("Update address set busy = 'N' where username = '"+pdata[3]+"';");
                                                                    stmt.executeUpdate();
                                                                  
                                                         }
                                                         else
                                                         {
                                                             stmt = con.prepareStatement("Select mac_add from address where username = '"+pdata[3]+"';");
                                                             ResultSet rs3 = stmt.executeQuery();
                                                             rs3.next();
                                                             if(rs3.getString("mac_add").equals(pdata[2]))
                                                             {
                                                                 ipmac = "registered";
                                                                 //update busy field
                                                                    stmt = con.prepareStatement("Update address set busy = 'N' where username = '"+pdata[3]+"';");
                                                                    stmt.executeUpdate();
                                                             }
                                                             else
                                                                 ipmac = "loggedin";
                                                                 
                                                         }
                                             }
                                                 
                                            }
                                        }
                                }
             else 
                 ipmac = "not";
             packetSendMethod(ipmac);
            }catch(SQLException sql)
            {
                JOptionPane.showMessageDialog(null,sql.getMessage());
            }
            catch(IOException io)
            {
                JOptionPane.showMessageDialog(null,io.getMessage());
            }
        }
	
}
}