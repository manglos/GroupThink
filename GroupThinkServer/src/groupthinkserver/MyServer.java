package groupthinkserver;



import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.DatagramSocket;

public class MyServer{ 
    static int PORT;
    FileInputStream fis = null;
    ObjectInputStream ois = null;
    static ArrayList<String> output;
    static PrintStream os;
    static int clientNumber = 0;
    ServerSocket echoServer;
    public static Long RCV=(long)-1;
    DatagramSocket udpSocket;
    
    MyServer(int p){
    	PORT = p;
    	echoServer = null;
    }
    
    public void listen(){
        try {
            openSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openSocket() throws IOException{     

        UDPWorker uw = new UDPWorker(PORT, clientNumber++);
        System.out.println("Ready for UDP on PORT: " + PORT);
        
	try{	
            Thread t = new Thread(uw);
	    t.start();
            t.join();
	}
        catch (InterruptedException e) {
            System.out.println("Accept failed: " + PORT);
            echoServer.close();
        }
    }

}
