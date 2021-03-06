/*
 * Filters text going into the editor (makes packets and queues/sends them).
 * Filtering can be avoided (e.g. for programmably inserted text) by using
 * "setActivation(boolean)"; however, make sure to synchronize.
 */
package groupthinkclient;

import GroupThink.GTP.*;
import static groupthinkclient.GroupThinkClient.editor;
import static groupthinkclient.GroupThinkClient.logger;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;

/**
 *
 * @author angie
 */
public class ChangeLogger extends DocumentFilter {
    // Hold onto the client so that you have access to all datastructures:
    private final GroupThinkClient client;
    private boolean active;
    DocumentFilter.FilterBypass myBypass;
    AttributeSet myAttributes;

    public ChangeLogger(GroupThinkClient client) {
        this.client = client;
        active = true;
    }

    // Toggle if logging is activated or not:
    public void setActivation(boolean active) {
        this.active = active;
    }
    
    public void doChange(GlobalChange gc){
        if(gc.isWrite()){
            try{
                active=false;
                editor.getDocument().insertString(gc.getPosition(), gc.getChar()+"", null);
                active=true;
            }catch(BadLocationException ex){}

        }
        else{
            try{
                active=false;
                editor.getDocument().remove(gc.getPosition(), 1);
                active=true;
            }catch(BadLocationException ex){}
        }
    }

    //-------------------------FILTER MEHTODS---------------------------------//
    @Override
    // "Invoked prior to insertion of text into the specified Document."
    public void insertString(DocumentFilter.FilterBypass fb, int offset, String text,
            AttributeSet attr) throws BadLocationException {
        myBypass=fb;
        myAttributes=attr;
        // If the user wrote it, add to the local queue
        if (active) {
            for (int i = 0; i < text.length(); i++) {
                GroupThinkClient.lChanges.add(new LocalChange(offset, text.charAt(i)));
                synchronized (GroupThinkClient.lChanges) {
                    GroupThinkClient.lChanges.notifyAll();
                }
            }
        }
        // display the change in the GUI 
        super.insertString(fb, offset, text, attr);
    }

    @Override
    // "Invoked prior to removal of the specified region in the specified Document."
    public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text,
            AttributeSet attrs) throws BadLocationException {
        myBypass=fb;
        myAttributes=attrs;
        // If the user wrote it, add to the local queue
        if (active) {
            for (int i = 0; i < text.length(); i++) {
                GroupThinkClient.lChanges.add(new LocalChange(offset, text.charAt(i)));
                synchronized (GroupThinkClient.lChanges) {
                    GroupThinkClient.lChanges.notifyAll();
                }
            }
        }
        // display the change in the GUI 
        super.replace(fb, offset, length, text, attrs);
    }

    @Override
    // "Invoked prior to replacing a region of text in the specified Document."
    public void remove(DocumentFilter.FilterBypass fb, int offset, int length)
            throws BadLocationException {
        myBypass=fb;
        // If the user wrote it, add to the local queue
        if (active) {
            for (int i = 0; i < length; i++) {
                GroupThinkClient.lChanges.add(new LocalChange(offset));
                synchronized (GroupThinkClient.lChanges) {
                    GroupThinkClient.lChanges.notifyAll();
                }
            }
        }
        // display the change in the GUI 
        super.remove(fb, offset, length);
    }

    //------------OLD METHODS -- DO NOT USE (JUST FOR REFERENCE)--------------//

    
    public void addGlobally(int offset, String text) {
        WCP newPacket;
        short id = (short) client.myID.get();
        for (int i=0; i<text.length(); i++) {
            try {
                System.out.println("=>SENDING PACKET (" + text.charAt(i) + ")");
                newPacket = new WCP((short) -1,(short) id, (short) 1, offset,
                        text.charAt(i));
                GroupThinkClient.UDPMultiCaster.sendPacket(newPacket);
            }
            catch (IOException ex) {
                Logger.getLogger(ChangeLogger.class.getName()).log(Level.SEVERE, null, ex);
            }
            // to do : add to global queue
        }
    }
    
    private void removeGlobally(int offset, int length) {
        DCP newPacket;
        short id = (short) client.myID.get();
        for (int i=0; i<length; i++) {
            try {
                System.out.println("=>SENDING PACKET");
                newPacket = new DCP((short) -1,(short) id, (short) 1, offset);
                GroupThinkClient.UDPMultiCaster.sendPacket(newPacket);
            }
            catch (IOException ex) {
                Logger.getLogger(ChangeLogger.class.getName()).log(Level.SEVERE, null, ex);
            }
            // to do : add to global queue
        }
    }
    
    //--------------------------DEBUG MEHTODS---------------------------------//
} 
