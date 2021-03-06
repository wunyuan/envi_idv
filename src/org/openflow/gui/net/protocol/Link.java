package org.openflow.gui.net.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Structure to specify a link.
 * 
 * @author David Underhill
 */
public class Link {
    public static final int SIZEOF = 2 + 2 + 2 * (Node.SIZEOF + 2);

    /** type of the link */
    public final LinkType linkType;
    
    /** source node */
    public final Node srcNode;

    /** port number on the source switch */
    public final short srcPort;
    
    /** destination node */
    public final Node dstNode;
    
    /** port number of the link is connected to on the destination switch */
    public final short dstPort;   
 

    
    //add by wunyuan
    
    public final boolean interDomainLink;
    
    public Link(DataInput in) throws IOException {
        this(LinkType.typeValToMessageType(in.readShort()), 
             new Node(in, false), in.readShort(),
             new Node(in, false), in.readShort());
    }
    
    public Link(DataInput in, boolean interDomainLink) throws IOException {
        this(LinkType.typeValToMessageType(in.readShort()), interDomainLink, 
             new Node(in, false), in.readShort(),
             new Node(in, false), in.readShort());
    }
    //end
    
    
    /*original
    public Link(DataInput in) throws IOException {
        this(LinkType.typeValToMessageType(in.readShort()), 
             new Node(in), in.readShort(),
             new Node(in), in.readShort());
    }
    */
    
    public Link(LinkType linkType, Node srcNode, short srcPort, Node dstNode, short dstPort) {
        this.linkType = linkType;
        this.srcNode = srcNode;
        this.srcPort = srcPort;
        this.dstNode = dstNode;
        this.dstPort = dstPort;
        this.interDomainLink = false;
    }
    
    //add by wunyuan
    public Link(LinkType linkType, boolean interDomainLink ,Node srcNode, short srcPort, Node dstNode, short dstPort) {
        this.linkType = linkType;
        this.srcNode = srcNode;
        this.srcPort = srcPort;
        this.dstNode = dstNode;
        this.dstPort = dstPort;
        this.interDomainLink = interDomainLink;
    }
    //end
    
    public void write(DataOutput out) throws IOException {
        out.writeShort(linkType.getTypeID());
        srcNode.write(out);
        out.writeShort(srcPort);
        dstNode.write(out);
        out.writeShort(dstPort);
    }
    
    public String toString() {
        return "Link{" + srcNode + "/" + srcPort  + 
                         " -- " + linkType.toString() + " --> " + 
                         dstNode + "/" + dstPort + "}";
    }

    public int hashCode() {
        int ret = 7*srcNode.hashCode() + 15*srcPort;
        ret += 31*dstNode.hashCode() + 31*dstPort;
        return ret + 15*linkType.getTypeID();
    }
    
    public boolean equals(Object o) {
        if(o == null) return false;
        if(!(o instanceof Node)) return false;
        Link l = (Link)o;
        return linkType.getTypeID()==l.linkType.getTypeID() && 
               srcPort==l.srcPort &&
               dstPort==l.dstPort &&
               srcNode.equals(l.srcNode) &&
               dstNode.equals(l.dstNode);
    }
}
