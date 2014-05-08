/*
 * Filters text going into the editor (makes packets and queues/sends them).
 * Filtering can be avoided (e.g. for programmably inserted text) by using
 * "setActivation(boolean)"; however, make sure to synchronize.
 */
package groupthinkclient;

import GroupThink.GTP.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 *
 * @author angie
 */
public class ChangeLogger extends DocumentFilter {
    // Hold onto the client so that you have access to all datastructures:
    private final GroupThinkClient client;
    private boolean active;

    public ChangeLogger(GroupThinkClient client) {
        this.client = client;
        active = true;
    }

    // Toggle if logging is activated or not:
    public void setActivation(boolean active) {
        this.active = active;
    }
    
    //-------------------------FILTER MEHTODS---------------------------------//
    
    @Override
    // "Invoked prior to insertion of text into the specified Document."
    public void insertString(DocumentFilter.FilterBypass fb, int offset, String text,
            AttributeSet attr) throws BadLocationException {
        System.out.println("insert string " + text); // testing
        // if you are the leader, put the change in the global log and multicast
        if (this.client.leader.get()) {
            addGlobally((short) offset, text);
        } 
        // otherwise, buffer the change in the local log and request leadership
        else {
            addLocally(offset, text);
        }
        // display the change in the GUI 
        super.insertString(fb, offset, text, attr);
        test(); // testing
    }
    
    @Override
    // "Invoked prior to removal of the specified region in the specified Document."
    public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text,
            AttributeSet attrs) throws BadLocationException {
        System.out.println("replace " + text); // testing
        // if you are the leader, put the change in the global log and multicast
        if (this.client.leader.get()) {
            addGlobally((short) offset, text);
        } 
        // otherwise, buffer the change in the local log and request leadership
        else {
            addLocally(offset, text);
        }
        // display the change in the GUI 
        super.replace(fb, offset, length, text, attrs);
        test(); // testing
    }

    @Override
    // "Invoked prior to replacing a region of text in the specified Document."
    public void remove(DocumentFilter.FilterBypass fb, int offset, int length)
            throws BadLocationException {
        System.out.println("remove"); // testing
        // if you are the leader, put the change in the global log and multicast
        if (this.client.leader.get()) {
            removeGlobally(offset, length);
        } 
        // otherwise, buffer the change in the local log and request leadership
        else {
            removeLocally(offset, length);
        }
        // display the change in the GUI 
        super.remove(fb, offset, length);
        test(); // testing
    }

    //-------------------------LOGGER MEHTODS---------------------------------//

    private void addGlobally(short offset, String text) {
        WCP newPacket;
        short id = (short) client.myID.get();
        for (int i=0; i<text.length(); i++) {
            try {
                System.out.println("=>SENDING PACKET (" + text.charAt(i) + ")");
                newPacket = new WCP((short) -1,(short) id, (short) 1, offset,
                        (short) 0, text.charAt(i));
                GroupThinkClient.UDPMultiCaster.sendPacket(newPacket);
            }
            catch (IOException ex) {
                Logger.getLogger(ChangeLogger.class.getName()).log(Level.SEVERE, null, ex);
            }
            // to do : add to global queue
        }
    }

    private void addLocally(int offset, String text) {
        // to do
    }
    
    private void removeGlobally(int offset, int length) {
        // to do
    }

    private void removeLocally(int offset, int length) {
        // to do
    }
    
    //--------------------------DEBUG MEHTODS---------------------------------//
    
    private void test() {
        System.out.println("->ACTIVE:" + this.active);
        System.out.println("->LEADER:" + this.client.leader.get());
    }
} 
