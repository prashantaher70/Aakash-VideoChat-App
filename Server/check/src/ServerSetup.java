import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*
 *this class will the set the server up for use on any desktop 
 */
public class ServerSetup extends JFrame 
{
	//save the changes to the file
	private JButton ok;
	//hide the window
	private JButton cancel;
	//creates the database
	private JButton createDatabase;
	//creates address table
	private JButton createTable;
	
	//host name
	public JTextField host;
	//database name
	public JTextField dname;
	//MySql user name
	public JTextField username;
	
	//MySql password
	public JPasswordField password;
	
	//host
	private JLabel hostLabel;
	//database
	private JLabel dnameLabel;
	//user name
	private JLabel usernameLabel;
	//password
	private JLabel passwordLabel;
	
	//server log
	private JTextArea log;
	
	//panel to hold create buttons
	private JPanel createPanel; 
	//panel to hold text boxes and buttons
	private JPanel remainPanel;
	
	//XML file
	private File xmlFile;
	//to parse the XML file
	private DocumentBuilderFactory docFactory;
	private DocumentBuilder docBuilder;
	private Document doc;
	
	//to point an element of XML file
	private Node database;
	private NodeList nList;
	
	//to reflect the changes to the XML file
	private Transformer transformer;
	private TransformerFactory transformerFactory;
	private DOMSource source;
	private StreamResult result;
	
	//Nimbus look and feel
	private UIManager.LookAndFeelInfo looks[];
	
        public ServerSetup()
        {
            
        }
	public ServerSetup(JTextArea log)
	{
		
		super("Video Conferencing Server Setup");
		//set the default to be flow layout
            //this is to set the image of the jpanel to our rquired image....    
               try
               {
               String dir = System.getProperty("user.dir");
               Image img= ImageIO.read(new File(dir+"\\"+"server.jpg"));
               setIconImage(img);
               }
        catch(Exception e)
        {
            System.out.println("Error image: " + e.getMessage());
        }
		setLayout(new BorderLayout());
		
		this.log = log;
		
		//get all installed look and feels
		looks = UIManager.getInstalledLookAndFeels();
			
		//set the Nimbus look and feel
		try 
		 {
			 for(int i=0;i<looks.length;i++)
			 {
				 if(looks[i].getName().equalsIgnoreCase("nimbus"))
				 {
					 UIManager.setLookAndFeel(
							 looks[ i ].getClassName() );
					 SwingUtilities.updateComponentTreeUI( this );
					 break;
				 }
			 }
		 }	
		 catch ( Exception exception ) 
		 {
			 log.append("ERRORgeneralinss : " + exception.getMessage() + "\n");
		 }
		
		hostLabel = new JLabel("  Host Name  ");
		dnameLabel = new JLabel("Database Name");
		usernameLabel = new JLabel("     User    ");
		passwordLabel = new JLabel("  Password   ");
		
		ok = new JButton("     SAVE     ");
		cancel = new JButton("     CANCEL    ");
		createDatabase = new JButton("     Create Database     ");
		createTable = new JButton("     Create Table     ");
		
		host = new JTextField("localhost",60);
		host.setHorizontalAlignment(JTextField.CENTER);
		dname = new JTextField("userdetails",60);
		dname.setHorizontalAlignment(JTextField.CENTER);
		username = new JTextField("root",60);
		username.setHorizontalAlignment(JTextField.CENTER);
		password = new JPasswordField("tutul",60);
		password.setHorizontalAlignment(JTextField.CENTER);
		
		createPanel = new JPanel(); 
		createPanel.setLayout(new FlowLayout());
		//createPanel.setLayout(new GridLayout(2,1,0,0));
		createPanel.add(createDatabase);
		createPanel.add(createTable);
		
		remainPanel = new JPanel();
		remainPanel.setLayout(new FlowLayout());
		
		remainPanel.add(createPanel);
		remainPanel.add(hostLabel);
		remainPanel.add(host);
		remainPanel.add(dnameLabel);
		remainPanel.add(dname);
		remainPanel.add(usernameLabel);
		remainPanel.add(username);
		remainPanel.add(passwordLabel);
		remainPanel.add(password);
		remainPanel.add(ok);
		remainPanel.add(cancel);
		
		add(remainPanel,BorderLayout.CENTER);
		add(createPanel,BorderLayout.SOUTH);
		
		xmlFile = new File("Server.xml");
		docFactory = DocumentBuilderFactory.newInstance();
		
		try 
    	{
			docBuilder = docFactory.newDocumentBuilder();
			//parse the XML file
			doc = docBuilder.parse(xmlFile);
			//get the parent tag
			database = doc.getElementsByTagName("database").item(0);
		} 
    	catch (ParserConfigurationException e) 
    	{
    		log.append("ERRORpceinss : " + e.getMessage() + "\n");
		}
    	catch (SAXException e) 
    	{
    		log.append("ERRORsaxinss : " + e.getMessage() + "\n");
		} 
		catch (IOException e) 
		{
			log.append("ERRORioinss : " + e.getMessage() + "\n");
		}
		
		//read Server.xml file
		readXml();
		
		//create a database
		createDatabase.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				database();
			}
		});
		
		//create  address table
		createTable.addActionListener(new ActionListener() {
					
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				table();
			}
		});
				
		//cancel button listener
		cancel.addActionListener(new ActionListener() 
		{	
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				//hide the window
				setVisible(false);
			}
		});
		
		//save button listener
		ok.addActionListener(new ActionListener() 
		{
			
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				//check for valid entries
				if(host.getText().length() == 0)
					JOptionPane.showMessageDialog(null, "Invalid host name");
				else if(dname.getText().length() == 0)
					JOptionPane.showMessageDialog(null, "Invalid database name");
				else if(username.getText().length() == 0)
					JOptionPane.showMessageDialog(null, "Invalid user name");
				else if(password.getPassword().length == 0)
					JOptionPane.showMessageDialog(null, "Invalid password");
				//edit the XML file
				else
					saveXml();
			}
		});
		
		//hide the window on closing
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		//size of window
		setSize(700,450);
		//disable maximize button
		setResizable(false);
		//position the window
		setLocation(getWidth()/2,getHeight()/2);
		
	}
	
	public void database()
    {
        Connection con;
                Connection con1;
        PreparedStatement stmt;
        
        try    
        {    
                        String database = "create database " + dname.getText().trim();
                    
                    
            //load MYSQL server JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            //obtaining a connection to SQL server
            con1 = DriverManager.getConnection("jdbc:mysql://"+host.getText()+":3306/"
                    +"test"+"?user="+username.getText()+"&password="+password.getText());                    
            
            stmt = con1.prepareStatement(database);
                        stmt.executeUpdate();
                        con1.close();
                        con = DriverManager.getConnection("jdbc:mysql://"+host.getText()+":3306/"
                    +dname.getText()+"?user="+username.getText()+"&password="+password.getText());                    
                  
            JOptionPane.showMessageDialog(null, "Database Successfully created");
        }    
        catch(ClassNotFoundException ce)
        {
                    JOptionPane.showMessageDialog(null, ce.getMessage());
                    log.append("ERRORcnfinss : " + ce.getMessage() + "\n");
        }
        catch(SQLException se)
        {
            JOptionPane.showMessageDialog(null, se.getMessage());
                    log.append("ERRORsqlinss : " + se.getMessage() + "\n");
        }
    }
	
	/*
	 * this method will create a table named address in database
	 */
	public void table()
	{
		Connection con;
		PreparedStatement stmt;
		String table = "create table address(username varchar(30) not null,ip_add varchar(30)," +
				"mac_add varchar(30),password varchar(30) not null,busy varchar(1) not null,primary key (username));";
		try	
		{	
			//load MYSQL server JDBC driver
			Class.forName("com.mysql.jdbc.Driver");
			//obtaining a connection to SQL server
			con = DriverManager.getConnection("jdbc:mysql://"+host.getText()+":3306/"
					+dname.getText()+"?user="+username.getText()+"&password="+password.getText());					
			
			stmt = con.prepareStatement(table);
			stmt.executeUpdate();
			JOptionPane.showMessageDialog(null, "Address Table Successfully created");
		}	
		catch(ClassNotFoundException ce)
		{
                        JOptionPane.showMessageDialog(null, ce.getMessage());
			log.append("ERRORCNF : " + ce.getMessage() + "\n");
		}
		catch(SQLException se)
		{
                    JOptionPane.showMessageDialog(null, se.getMessage());
                    log.append("ERRORSQL : " + se.getMessage() + "\n");
		}
	}
	
	/*
	 * parse the XML file and save the changes to it
	 */
	public void saveXml()
	{
		//get all the child tag of parent tag database
		NodeList list = database.getChildNodes();
		 
		//set the tag values with field values 
		for (int i = 0; i < list.getLength(); i++) 
		{
			
           Node node = list.item(i);
		   if ("host".equalsIgnoreCase(node.getNodeName())) 
		   {
			node.setTextContent(host.getText());
		   }
		   
		   else if("dname".equalsIgnoreCase(node.getNodeName()))
		   {
			   node.setTextContent(dname.getText());
		   }
		   
		   else if("username".equalsIgnoreCase(node.getNodeName()))
		   {
			   node.setTextContent(username.getText());
		   }
		   
		   else if("password".equalsIgnoreCase(node.getNodeName()))
		   {
			   node.setTextContent(password.getText());
		   }
		}
	
		//make these changes to XML file
		try 
		{
			transformerFactory = TransformerFactory.newInstance();
			transformer = transformerFactory.newTransformer();
			source = new DOMSource(doc);
			result = new StreamResult(xmlFile);
			transformer.transform(source, result);
		}
		catch (TransformerConfigurationException e) 
		{
			log.append("ERRORtce : " + e.getMessage() + "\n");
		}
	
		catch (TransformerException e) 
		{
			 log.append("ERRORte : " + e.getMessage() + "\n");
		}
		
		JOptionPane.showMessageDialog(null, "Changes successfully saved");
	}
	
	/*
	 * read the XML file and display the values in it
	 * in respective fields
	 */
	public void readXml()
	{
			nList = doc.getElementsByTagName("database");
		
			//read all tag values and set the text fields
			for (int temp = 0; temp < nList.getLength(); temp++) 
			{
	 
			   Node nNode = nList.item(temp);
			   if (nNode.getNodeType() == Node.ELEMENT_NODE) 
			   {
	 
			      Element eElement = (Element) nNode;
	 
			      host.setText(getTagValue("host", eElement));
			      dname.setText(getTagValue("dname", eElement));
		          username.setText(getTagValue("username", eElement));
			      password.setText(getTagValue("password", eElement));
			   }
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
}
