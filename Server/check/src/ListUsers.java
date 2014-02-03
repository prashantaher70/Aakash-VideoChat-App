
import java.awt.GridLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
 //@author Sumantra
 
public class ListUsers extends JPanel {
    
    //online users shown here
	private JTextArea online;
	//off line users shown here
	private JTextArea offline;
	//scroll these area
	private JScrollPane pane;
	
	public ListUsers()
	{
		//set the layout as grid layout with 2 rows and
		//1 column with out any spacing
		setLayout(new GridLayout(2,1,0,0));
		
		online = new JTextArea();
                online.setEditable(false);
		pane = new JScrollPane(online);
		//horizontal scroll bars as needed
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		//vertical scroll bars always needed
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		//add this text area
		add(pane);
		
		offline = new JTextArea();
                offline.setEditable(false);
		pane = new JScrollPane(offline);
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(pane);
	}
	
	/*
	 * return reference to online area
	 */
	public JTextArea getOnlineArea()
	{
		return online;
	}
	
	/*
	 * return reference to off line area
	 */
	public JTextArea getOfflineArea()
	{
		return offline;
	}
}
