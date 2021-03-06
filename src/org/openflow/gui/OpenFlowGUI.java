package org.openflow.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.*;

import org.openflow.gui.drawables.OpenFlowSwitch;
import org.openflow.nchc.restful.GetRadioButtonData;
import org.openflow.nchc.restful.MessageHandler;
import org.openflow.nchc.wunyuan.*;
import org.openflow.util.Pair;
import org.pzgui.PZManager;
import org.pzgui.layout.Edge;
import org.pzgui.layout.PZLayoutManager;
import org.pzgui.layout.Vertex;
/**
 * Provides static methods for running the GUI.
 * 
 * @author David Underhill
 */
public final class OpenFlowGUI {
    private OpenFlowGUI() { /* this class may not be instantiated */ }
    
    /** 
     * Run a simple version of the GUI by starting a single connection which 
     * will populate a single topology drawn by a PZLayoutManager.
     */
    public static void main(String args[]) {

    	
    	
    	MessageHandler mh = new MessageHandler();
    	Thread test = new Thread(mh);
    	test.start();
    	
    	String server ="";
    	short port = 0;
    	
        Pair<String, Short> serverPort = getServer(args);
        
        if(GetRadioButtonData.defaultString.equals("ByteData")) {
        	GetRadioButtonData.restful_IP = serverPort.a;
            server = serverPort.a;
            port = serverPort.b;
        }
        else {
        	GetRadioButtonData.restful_IP = serverPort.a;
        	GetRadioButtonData.defaultRestful_IP = serverPort.a;
            server = "127.0.0.1";
            port = serverPort.b;
        }     

        
        // create a manager to handle drawing the topology info received by the connection
        
        
        
        PZLayoutManager gm = new PZLayoutManager();
        CallRedirectedFrame crf = new CallRedirectedFrame();
        crf.go();
        // layout the nodes with the spring algorithm by default
        gm.setLayout(new edu.uci.ics.jung.algorithms.layout.SpringLayout2<Vertex, Edge>(gm.getGraph()));
        
        
        // leave a small 10-pixel border around the edge of the screen
        gm.setBorderSize(OpenFlowSwitch.DEFAULT_SIZE/2+10);
        
        // create a manager to handle the connection itself
        ConnectionHandler cm = makeDefaultConnection(gm, server, port, true, true);
        
        // start our managers
        gm.start();
        cm.getConnection().start();
        
        /*
        JFrame frame = new JFrame("HelloSwing");
        
        Container cp = frame.getContentPane();
        JButton button = new JButton("Hello Swing!");
        
        cp.add(button);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        */
        

        
    }

    
    /**
     * Creates a connection which will populate a new topology.  The connection
     * handler is registered as a closing listener with manager so that it can
     * be cleanly torn down when the GUI closes.
     * 
     * @param manager            the manager of the GUI elements
     * @param server             the IP or hostname where the back-end is located
     * @param port               the port the back-end is listening on
     * @param subscribeSwitches  whether to subscribe to switch changes
     * @param subscribeLinks     whether to subscribe to link changes
     */
    public static ConnectionHandler makeDefaultConnection(PZManager manager,
                                                          String server, Short port,
                                                          boolean subscribeSwitches,
                                                          boolean subscribeLinks) {
        ConnectionHandler ch = new ConnectionHandler(new Topology(manager), server, port, subscribeSwitches, subscribeLinks);
       // manager.addClosingListener(ch);
        return ch;
    }
    
    /**
     * Gets the IP[:PORT] to connect to.
     * 
     * @param args  the command-line arguments to extract a server from; if
     *              one is not provided then the user will be prompted for one
     * 
     * @return  the server to connect to
     */
    public static Pair<String, Short> getServer(String args[]) {
        ArrayList<Pair<String, Short>> servers = getServers(args, true);
        return servers.get(0);
    }
    
    /**
     * Gets the IP[:PORT](s) to connect to.  If no servers are specified, the 
     * user is prompted to specify one.  If the user does not specify anything
     * when prompted, then the program terminates.
     * 
     * 
     * @param args  the command-line arguments to extract server(s) from; if
     *              none are provided then the user will be prompted for one
     * 
     * @return  the server to connect to
     */
    public static ArrayList<Pair<String, Short>> getServers(String args[]) {
        return getServers(args, false);
    }
    
    private static ArrayList<Pair<String, Short>> getServers(String args[], boolean limitToOne) {
        // get the server(s) to connect to
        ArrayList<Pair<String, Short>> servers = new ArrayList<Pair<String, Short>>();
        
        if(args.length == 0) {
            // if none are specified, prompt the user like the base gui
            servers.add(promptForServer());
        }
        else {
            // each argument is a server to connect to
            for(String arg : args) {
                servers.add(parseServerIdentifier(arg));
                if(limitToOne)
                    break;
            }
        }
        
        if(servers.size() == 0) {
            System.out.println("Goodbye");
            System.exit(0);
        }
        
        return servers;
    }
    
    /**
     * Returns the parse of a IP[:PORT].  If PORT is omitted, the 
     * Options.DEFAULT_PORT is returned for the port value.
     * 
     * @return IP-port pair
     */
    public static Pair<String, Short> parseServerIdentifier(String s) {
        int indexOfColon = s.indexOf(':');
        
        String server;
        Short port = Options.DEFAULT_PORT;
        if(indexOfColon > 0) {
            server = s.substring(0, indexOfColon);
            String strPort = s.substring(indexOfColon + 1);
            try {
                port = Short.valueOf(strPort);
            }
            catch(NumberFormatException e) {
                throw new Error("Error: invalid port number: " + strPort);
            }
        }
        else
            server = s;
        
        return new Pair<String, Short>(server, port);
    }
    
    /**
     * Ask the user for the backend's IP[:PORT] in dialog box.
     */
    public static Pair<String, Short> promptForServer() {
        JPanel panel = new JPanel(); 
        JPanel optionPanel = new JPanel();
        JPanel delayTimePanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        optionPanel.setLayout(new BorderLayout());
        delayTimePanel.setLayout(new GridBagLayout());
        
        
        delayTimePanel.setAlignmentX(delayTimePanel.LEFT_ALIGNMENT);
        buttonPanel.setAlignmentX(buttonPanel.LEFT_ALIGNMENT);
        optionPanel.setAlignmentX(optionPanel.LEFT_ALIGNMENT);
        panel.setAlignmentX(panel.LEFT_ALIGNMENT);
        
        //radio button
        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButton r1 = new JRadioButton("Read Byte Data");
        JRadioButton r2 = new JRadioButton("Read Restful Data");
        r1.addActionListener(new GetRadioButtonData());
        r2.addActionListener(new GetRadioButtonData());
        buttonGroup.add(r1);
        buttonGroup.add(r2);
        r2.setSelected(true); 
        buttonPanel.add(r2);
        buttonPanel.add(r1);
        

        optionPanel.add(buttonPanel,BorderLayout.NORTH);


        //delay time input field
        JLabel delayTimeLabel = new JLabel("retry time. Please input bigger than 500ms (default:500ms) : ");
    //    delayTimeLabel.setText("Set delay time for retry load data");     
        JTextField delayTimeField = new JTextField("500");     
        JLabel msLabel = new JLabel("ms");
        
        GridBagConstraints ct = new GridBagConstraints();
        ct.gridx = 0;
        ct.gridy = 0;
        delayTimePanel.add(delayTimeLabel, ct);
        ct.gridx = 1;
        ct.gridy = 0; 
        delayTimePanel.add(delayTimeField, ct);
        ct.gridx = 2;
        ct.gridy = 0; 
        delayTimePanel.add(msLabel, ct);
        
        
        optionPanel.add(delayTimePanel,BorderLayout.SOUTH);
        panel.add(optionPanel);
        
        
        //IP input dialog
        String server = (String) JOptionPane.showInputDialog(null, panel,  
            "Input data",  
            JOptionPane.PLAIN_MESSAGE, null, null, Options.DEFAULT_SERVER_IP);  
        if(server==null)
        	System.exit(0);
       
       // panel.dispatchEvent(arg0);
      
              
    	
       // String server = DialogHelper.getInput("What is the IP or hostname of the backend?", 
        //                                      Options.DEFAULT_SERVER_IP);
        
        try {
        	Options.RETRY_LOAD_DATA_TIME = Integer.parseInt(delayTimeField.getText());
        	if(Options.RETRY_LOAD_DATA_TIME < 500)
        		Options.RETRY_LOAD_DATA_TIME =500;
        	} catch(Exception e) {
        		System.out.println("error input...using default delay time.");
        		Options.RETRY_LOAD_DATA_TIME = 500;
        	}
        
        return parseServerIdentifier(server);
    }
     
}
