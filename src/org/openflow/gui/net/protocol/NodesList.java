package org.openflow.gui.net.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * A list of nodes.
 * 
 * @author David Underhill
 */
public abstract class NodesList extends OFGMessage {
    public final Node[] nodes;
    
    
    //add by wunyuan
    public static Node[] backupnodes;
    
    public static String getController(int index)
    {
    	String IpandPort = backupnodes[index].ipPortData;
    	int Position = IpandPort.indexOf(":");
    	if( Position != -1)
    		return IpandPort.substring(0, Position);
    	
		return IpandPort;
    }
    
    public static String getControllerType(int index)
    {
    	String controller = backupnodes[index].ipPortData;
    	int Position = controller.indexOf("#");
    	if( Position != -1)
    		controller = controller.substring(Position+1, controller.length());
    	switch(controller)
    	{
    	case "1":
    		controller = "Floodlight";
    		break;
    	case "2":
    		controller = "NOX";
    		break;
    	}
    	
		return controller;
    }
    //end

    
    public NodesList(OFGMessageType t, final Node[] nodes) {
        this(t, 0, nodes);
    }
    
    public NodesList(OFGMessageType t, int xid, final Node[] nodes) {
        super(t, xid);
        this.nodes = nodes;
    }
    
    public NodesList(final int len, final OFGMessageType t, final int xid, final DataInput in) throws IOException {
        super(t, xid);
        // make sure the number of bytes leftover makes sense
        int left = len - super.switchSize();
        left = left -super.ipDataLen();
        if(left % Node.SIZEOF != 0) {
            throw new IOException("Body of switch list is not a multiple of " + 
                                  Node.SIZEOF + 
                                  " (length of body is " + left + " bytes)");
        }
        
        // read in the DPIDs
        int index = 0;
        nodes = new Node[left / Node.SIZEOF];
        while(left >= Node.SIZEOF) {
            left -= Node.SIZEOF;
            //modify by wunyuan
            nodes[index++] = new Node(in, true);
            
            //add by wunyuan
            backupnodes = nodes;
           // original nodes[index++] = new Node(in);
            //end
        }
    }
    
    public int length() {
        return super.length() + nodes.length * Node.SIZEOF;
    }
    
    public void write(DataOutput out) throws IOException {
        super.write(out);
        for(Node n : nodes)
            n.write(out);
    }
    
    public String toString() {
        String ret;
        if(nodes.length > 0)
            ret = nodes[0].toString();
        else
            ret = "";
        
        for(int i=1; i<nodes.length; i++)
            ret += ", " + nodes[i].toString();
        
        return super.toString() + TSSEP + ret;
    }
}
