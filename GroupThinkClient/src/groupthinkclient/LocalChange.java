/*
 * Wrapper for information buffered in the local queue.
 */

package groupthinkclient;

//Just copied over from GlobalChange for my purposes, but
//in which way is a LocalChange object different from a Global one?
public class LocalChange {
    private final int position;
    private final char payload;  // the character being added or deleted
    private final boolean write; // write or delete?
    
    // Constructor if it is a write (needs a char)
    public LocalChange(int pos, char pl) {
        position = pos;
        payload = pl;
        write = true;
    }
    
    // Constructor if it is a delete (does not need a char)
    public LocalChange(int pos) {
        position = pos;
        payload = ' '; // should never be used
        write = false;
    }
}
