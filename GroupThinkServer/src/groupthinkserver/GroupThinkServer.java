package groupthinkserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class GroupThinkServer {
    static final String ANSI_RED = "\u001B[31m";
    static final String ANSI_WHITE = "\u001B[0m";
    static final String ANSI_GREEN = "\u001B[32m";
    static final String ANSI_BLUE = "\u001B[34m";
    static final String ANSI_YELLOW = "\u001B[33m";
    static final String ANSI_CYAN = "\u001B[36m";
    static final String ANSI_PURPLE = "\u001B[35m";
    
    public static final double COMMIT_RATIO = 0.66;
    
    public static boolean voting = false;
    public static boolean commitable = false;
    public static boolean debug = false;
    public static final int PORT = 2606;
    public static ArrayList<String> clients;
    
    
    public static void main(String[] args){
        clients = new ArrayList<String>();
        clients.add("server");
        
        try{
            DocumentSerializer.DeSerialize();
        }catch(Exception ex){
            System.out.println("Could not read document.");
        }
        
        
        MyServer myServer = new MyServer(PORT);
        myServer.listen();
        
    }
    
    public static class DocumentSerializer{
        public static String document;
        
        static void Serialize() throws FileNotFoundException, IOException{
            String filename = System.getProperty("user.home")+System.getProperty("file.separator")+"session.gt";
            File file = new File(filename);
            FileOutputStream fileOut =
            new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(document);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in " + filename);            
        }
        
        static void DeSerialize() throws FileNotFoundException, IOException, ClassNotFoundException{
            String filename = System.getProperty("user.home")+System.getProperty("file.separator")+"session.gt";
            File file = new File(filename);
            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            document = (String)in.readObject();
            in.close();
            fileIn.close();
        }
        
    }
    
}
