/**
 *
 * @author Sumantra
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/*
 * this class sets the basic layout of the desktop server 
 */
public class Layout extends JFrame implements ActionListener
{
	//tabs shown in GUI
	private JTabbedPane pane;
	//shows all registered clients
	private ListUsers listUsers;
        //tab to view databse
       // private ViewDatabase VDb;
	//setup the server
	private ServerSetup setupServer;
	//register a new user
	private UserRegistration registerUsers;
	//panel to hold buttons and log area
	private JPanel Server;
	//panel to hold log area
	private JPanel logPanel;
	//panel to hold start server button and setup server button
	private JPanel buttonPanel;
	//start the server
	private JButton start;
	//setup the server
	private JButton setup;
        //setup the server
	private JButton clear;
	//server log
	private JTextArea log;
	//scroll bars
	private JScrollPane scrollPane;
	//server log label
	private JLabel logLabel;
	//Nimbus look and feel
    private VDOServer vd;
	
	
	public Layout()
	{
            super("Video Conferencing Server");
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
               
		 
		pane = new JTabbedPane();
		
		listUsers = new ListUsers();
		
		logPanel = new JPanel();
		logPanel.setLayout(new BorderLayout());
		
		buttonPanel = new JPanel();
		start = new JButton("Start Server");
		buttonPanel.add(start);
		setup = new JButton("Setup Server");
		buttonPanel.add(setup);
                clear = new JButton("Save and Clear Log");
		buttonPanel.add(clear);
                
		
		Server = new JPanel();
		Server.setLayout(new BorderLayout());
		Server.add(buttonPanel,BorderLayout.NORTH);
		
		log = new JTextArea(getWidth(), getHeight());
                log.setEditable(false);
		logLabel = new JLabel("Server Log : ");
		logPanel.add(logLabel,BorderLayout.NORTH);
		
		registerUsers = new UserRegistration(log);
		
		scrollPane = new JScrollPane(log);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		logPanel.add(scrollPane,BorderLayout.CENTER);
		
		Server.add(logPanel,BorderLayout.CENTER);
                
               // VDb = new ViewDatabase(log);
		
		//add all the tabs
		pane.addTab("Server", Server);
		pane.addTab("Status", listUsers);
		pane.addTab("Clients", registerUsers);
                //pane.addTab("Database", VDb);
		add(pane);
		
		setupServer = new ServerSetup(log);
		
		start.addActionListener(this);
		setup.addActionListener(this);
		clear.addActionListener(this);
                
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(650,450);
		setLocation(getWidth()/2,getHeight()/2);
		setResizable(false);
		setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent v) 
	{
		//if start button is pressed
		if(v.getSource() == start)
		{
			//if button text is start server
			if(start.getText().equals("Start Server"))
			{
				//initiate a thread to reply to the requests for current time of server 
				//st = new ServerTime(log);
				//start the server and wait for request from clients
				vd = new VDOServer(log,listUsers);
				//change the button text
				start.setText("Exit Server");
			}
			//if button text is exit server
                        else
			{                            
                            log.append("\n@SavingLog");
                            logwriter();
                            JOptionPane.showMessageDialog(null, "Exiting Server; Saving Log");
                          /*  vd.socket.close();
                            setVisible(false);
                            new AdminLogin();
                            */
                        System.exit(0);
			}
                            
                                    
		}
		//if setup button is pressed
                else if(v.getSource() == setup)
		{
                        //show the setup window
			setupServer.setVisible(true);     
		}
                else if(v.getSource() == clear)
                {
                    log.append("\n@SavingLog"); 
                    logwriter();
            JOptionPane.showMessageDialog(this,"Saved");
        
                    log.setText("");
            }
	}

    private void logwriter() {
        int i = 0;
        try {
                         File file = new File("log.txt");
                         if(!file.exists())
                             file.createNewFile();
                             FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
                             BufferedWriter outfile = new BufferedWriter(fw);
                             outfile.append(new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss").format(Calendar.getInstance().getTime()));
                             outfile.newLine();
                             String[] lines = log.getText().split("\\n");
                             while(!(lines[i].equals("@SavingLog")))
                             {
                             outfile.append(lines[i++]);
                             outfile.newLine();
                             }
                             outfile.close();
                         
                         }
            catch(FileNotFoundException e) {
                System.out.println("File not found.");
            }
            catch(NullPointerException j){
                System.out.println("Null.");
            }
            catch(IOException k){
                System.out.println("IO Exception.");            
            }
    }
}


