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
    private int position;          // position in document                                                                                                     
    private final char payload;  // the character being added or deleted                                                                                       
    private final boolean write; // write or delete?                                                                                                           

    public GlobalChange(long id, int pos, char c, boolean write) {
        this.id = id;
        this.position = pos;
        this.write = write;
        this.payload = c;
    }
    
    public GlobalChange(long id, LocalChange lc){
        this.id = id;
        this.position = lc.getPosition();
        this.payload = lc.getChar();
        this.write = lc.isWrite();                
    }

    public int getPosition(){
        return position;
    }

    public char getChar(){
        return payload;
    }

    public boolean isWrite(){
        return write;
    }

    // get this change's id:                                                                                                                                   
    public long getChangeID() {
        return id;
    }
}

