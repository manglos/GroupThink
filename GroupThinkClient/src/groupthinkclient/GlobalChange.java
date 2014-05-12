/*
 * This class encapsulates information about a change that has been added
 * to the global logs of all users.
 */

package groupthinkclient;

/**
 *
 * @author angie
 */
public class GlobalChange {
    private final long id;       // unique sequential id for the log
    private int caretX;          // x-position of caret
    private int caretY;          // y-position of caret
    private final char payload;  // the character being added or deleted
    private final boolean write; // write or delete?
    
    public GlobalChange(long id, int x, int y, char c, boolean write) {
        this.id = id;
        this.caretX = x;
        this.caretY = y;
        this.write = write;
        this.payload = c;
    }
    
    // offsets for a local delete/insert if this change occurs after:
    public void offsetIfApplicable(int localX, int localY, int direction) {
        if (effectedByLocalChange(localX, localY)) {
            offsetX(direction); // +1 for write -1 for delete
        }
    }
    
    // See if this character is effected by a local change:
    public boolean effectedByLocalChange(int changeX, int changeY) {
        return ((this.caretY > changeY) || ((this.caretY == changeY) && (this.caretX >= changeX)));
    }
    
    // offset the caret x position if local changes effect it
    public void offsetX(int xOffset) {
        caretX += xOffset;
    }
    
    // offset the caret y position if local changes effect it
    public void offsetY(int yOffset) {
        caretY += yOffset;
    }
    
    // get this change's id:
    public long getChangeID() {
        return id;
    }
}
