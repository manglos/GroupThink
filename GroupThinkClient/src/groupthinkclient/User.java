package groupthinkclient;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

//Thread-safe User class
public class User {
    AtomicBoolean active;
    AtomicInteger id;
    AtomicLong lastHeartbeat;
    AtomicReference<String> username;
    AtomicBoolean isLeader;
    
    User(int i, String un){
        id = new AtomicInteger(i);
        lastHeartbeat = new AtomicLong(System.nanoTime());
        active = new AtomicBoolean(true);
        username = new AtomicReference(null);
        username.compareAndSet(null, un);
        isLeader = new AtomicBoolean(false);
    }
    
    public Long getLastHeartbeat(){
        return lastHeartbeat.get();
    }
    
    public boolean setLastHeartbeat(Long l){
        Long last = getLastHeartbeat();
        return lastHeartbeat.compareAndSet(last, l);
    }
    
    public boolean getActive(){
        return active.get();
    }
    
    public boolean setActive(boolean a){
        boolean isActive = getActive();
        return active.compareAndSet(isActive, a);
    }
    
    public boolean getIsLeader(){
        return isLeader.get();
    }
    
    public boolean setIsLeader(boolean a){
        boolean il = getActive();
        return isLeader.compareAndSet(il, a);
    }
    
    public String getUsername(){
        return username.get();
    }
    
    public boolean setUsername(String u){
        String un = getUsername();
        return username.compareAndSet(un, u);
    }
    
    public String toString(){
        return getUsername();
    }
    
}
