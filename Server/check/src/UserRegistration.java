import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
 // @author Sumantra
 
public class UserRegistration extends JPanel implements ActionListener{
        //username
	private JLabel UsrnmLabel;
        //Password
	private JLabel PswdLabel;
	
	//enter username here
	private JTextField UsrnmText;
        //enter password
        private JPasswordField PswdText;
	
	//save the changes
	private JButton save;
	//delete a user
	private JButton delete;
        //logout a particular user
        private JButton logout;
        //view database
        private JButton viewDB;
	
	//connection to SQL server
	private Connection con;
	//query to update DB
	private PreparedStatement stmt;            
	//to query the database
	private Statement stmt1;                    
	//result set of a query to database
	private ResultSet rs;                       
	
	//stores host name
	public String host;
	//stores database name
	public String dname;
	//stores user name
	public String username;
	//stores password
	public String password;
	
	//stores username
	private String usrnm;
        //stores the password
        private String pswd;
        
	//to use readXml() method from server
	private VDOServer server;
	
	//server log
	private JTextArea log;
	
	//initialize the database variables only one time
	private boolean flag; 
        
        //private ViewDatabase VDB;
        public UserRegistration()
        {
                server = new VDOServer();//y required a constructor...???
		//VDB = new ViewDatabase(this.log);
                //read the XML file
		server.readXml();
		//retrieve host name
		host = server.getHost();
		//retrieve database name
		dname = server.getDName();
		//retrieve user name
		username = server.getUsername();
		//retrieve password
		password = server.getPassword();
        }
	
	public UserRegistration(JTextArea log)
	{
		
		//set when application runs for first time
		flag = true;
		this.log = log;
		UsrnmLabel = new JLabel("USERNAME");
                UsrnmText = new JTextField(55);
		PswdLabel = new JLabel("PASSWORD");
		PswdText = new JPasswordField(55);
		save = new JButton("      SAVE      ");
		delete = new JButton("      DELETE      ");
                logout = new JButton("      KICK     ");
                viewDB = new JButton("  VIEW DATABASE  ");
                UsrnmLabel.setSize(100,100);
		add(UsrnmLabel);
                add(UsrnmText);
                add(PswdLabel);
                add(PswdText);
		add(save);
		add(delete);
                add(logout);
                add(viewDB);
                
                save.addActionListener(this);
                delete.addActionListener(this);
                logout.addActionListener(this);
                viewDB.addActionListener(this);
		server = new VDOServer();//y required a constructor...???
		//VDB = new ViewDatabase(this.log);
                //read the XML file
		server.readXml();
		//retrieve host name
		host = server.getHost();
		//retrieve database name
		dname = server.getDName();
		//retrieve user name
		username = server.getUsername();
		//retrieve password
		password = server.getPassword();
//                buttonStopper();
	}

	/*
	 * method called when user presses one of the button
	 */
	@Override
	public void actionPerformed(ActionEvent v) 
	{
		if(flag)
		{
			try
			{	
				//load MYSQL server JDBC driver
				Class.forName("com.mysql.jdbc.Driver");
				//obtaining a connection to SQL server
				con = DriverManager.getConnection("jdbc:mysql://"+host+":3306/"+dname+"?user="+username+"&password="+password);
				stmt1 = con.createStatement();
			}
			catch(ClassNotFoundException ce)
			{
				log.append("ERROR : " + ce.getMessage() + "\n");
			}
			catch(SQLException se)
			{
				log.append("ERROR : " + se.getMessage() + "\n");
			}
			flag = false;
		}
		
                        //read username from text field
			usrnm = UsrnmText.getText();
                        //read the password from text field
			pswd = PswdText.getText().toString();
		//if save button is clicked
		if(v.getSource() == save)
		{
			//if Username is already present
			if(usernameExist(usrnm) == 1)
				JOptionPane.showMessageDialog(null, "Username already present");
			//improper name
			else if(usrnm.length() < 4)
				JOptionPane.showMessageDialog(null, "Enter the Username");
			else if(pswd.length() == 0)
				JOptionPane.showMessageDialog(null, "No password given");
                        //register the user
			else
			{
				try 
				{
					//insert into database
					stmt = con.prepareStatement("insert into address values('" + usrnm + "',NULL,NULL,'" + pswd + "','N');");
                                        stmt.executeUpdate();
                                            JOptionPane.showMessageDialog(null, usrnm + "'s records successfully inserted");
                                        
					//set to default
					UsrnmText.setText("");
					PswdText.setText("");
                                        
                                            
				} 
				catch (SQLException e) 
				{
					log.append("ERROR : " + e.getMessage() + "\n");
				}
			}
		}
                else if(v.getSource() == logout)
                {
                    if(usrnm.length() < 4 )
				JOptionPane.showMessageDialog(null, "Username must be at least four Characters ");
                    else if((usernameExist(usrnm) == 0))
                               JOptionPane.showMessageDialog(null, "No such username present");
                    else    
                    {
                        logoutMethod(UsrnmText.getText());
                        JOptionPane.showMessageDialog(null, UsrnmText.getText()+"Logged out of Server");
                        UsrnmText.setText("");
                        PswdText.setText("");
                        
                    }
                }
                else if(v.getSource() == viewDB)
                {
                    new ViewDatabase();
                }
		//if delete button is clicked
                else 
		{
			try 
			{
				//delete from table
				stmt = con.prepareStatement("delete from address where username = '"+usrnm+"';");
				int i = stmt.executeUpdate();
				//if such a user exits
				if(i>0)
				{
				UsrnmText.setText("");
				PswdText.setText("");
				JOptionPane.showMessageDialog(null, usrnm + "'s records successfully deleted");
				}
				else
					JOptionPane.showMessageDialog(null, "No such username present");
			} 
			catch (SQLException e) 
			{
				log.append("ERROR : " + e.getMessage() + "\n");
			}
			}
               /* else if (v.getSource()== viewDB){
                    // VDB.setVisible(true);
                      Client cust = new Client();
                      try{
                          rs = stmt1.executeQuery("Select username,ip_add,mac_add,password from address");
                          while(rs.next())
                          {
                              cust.setUsername(rs.getString(1));
                              cust.setIp_add(rs.getString(2));
                              cust.setMac_add(rs.getString(3));
                              cust.setPassword(rs.getString(4));
                              new ClientDataStore().add(cust);
                          }
                      }catch(SQLException e)
                      {
                          log.append("Error, couldn't view database because of issue :"+ e.getMessage()+"\n");
                      }
                }
                */
		}
            
            
	
	/*
	 * utility method to check if username exists in database 
	 */
	/**
     *
     * @return
     */
    public int usernameExist(String usrnm)
	{
		try 
    	{
			//select all username from database
			rs = stmt1.executeQuery("Select username from address;");
	
			while(rs.next())
			{
				//if username already present
				if(rs.getString(1).equalsIgnoreCase(usrnm))
				{
					return 1;
				}
			}
		} 
    	catch (SQLException e) 
    	{
			log.append("ERROR : " + e.getMessage() + "\n");
		}	
		return 0;
	}

    private void logoutMethod(String user) {
            try
			{
                                //pdata1 me from
                                log.append("Logout Request from " +user+"\n");
                                stmt = con.prepareStatement("Update address set ip_add = null where username = '"+user+"';");
                                int i = stmt.executeUpdate();
                                stmt = con.prepareStatement("Update address set busy = 'N' where username = '"+user+"';");
                                stmt.executeUpdate();
                              
        }catch(SQLException sq)
        {
          log.append("ERROR: "+sq.getMessage());  
          
        }
    
}
}
