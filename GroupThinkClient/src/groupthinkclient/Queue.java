package groupthinkclient;

import java.util.concurrent.atomic.AtomicReference;

class Queue{
    boolean casHead(Node cap, Node val){
        return head.compareAndSet(cap, val);
    }
    
    void casTail(Node cap, Node val){
        tail = cap.next();
    }
    
    AtomicReference<Node> head, tail;
    
    Queue(){
        Node n = new Node(null, null);
        head=tail=new AtomicReference<Node>(n);
    }
    
    void add(Object x){
        
        Node node = new Node(x, null);
        
        for(;;){
            
            Node t = tail.get();
            Node l = t.next().get();
            
            if (l!=null)
                casTail(t,l);
            else if(t.casNext(null,node)){
                casTail(t, node);
                return;
            }
        }
    }
    
    Object pull(){
        for(;;){
            Node h = head.get();
            Node t = tail.get();
            Node l = t.next().get();
            
            if(l!=null)
                casTail(t, l);
            else if (head.get() != h)
                ;
            else if (h == t)
                return null;
            else{
                Node first = h.next().get();
                Object x = first.item;
                if (casHead(h, first))
                    return x;
            }
        }
    }
    
    
    
    static class Node{
        
        final Object item;
        AtomicReference<Node> next;
        
        boolean casNext(Node cap, Node val){
            return next.compareAndSet(cap, val);
        }
        
        Node(Object x, Node n){
            item = x;
            next = new AtomicReference<Node>(n);
        }
        
        AtomicReference<Node> next(){
            return next;
        }
        
    }
    
    
}