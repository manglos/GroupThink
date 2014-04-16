//test
package groupthinkserver;

public class GroupThinkServer {
    static final String ANSI_RED = "\u001B[31m";
    static final String ANSI_WHITE = "\u001B[0m";
    static final String ANSI_GREEN = "\u001B[32m";
    static final String ANSI_BLUE = "\u001B[34m";
    static final String ANSI_YELLOW = "\u001B[33m";
    static final String ANSI_CYAN = "\u001B[36m";
    static final String ANSI_PURPLE = "\u001B[35m";

    public static boolean debug = false;
    public static final int PORT = 2606;
    
    public static void main(String[] args){
        
        MyServer myServer = new MyServer(PORT);
        myServer.listen();
        
    }
    
}
