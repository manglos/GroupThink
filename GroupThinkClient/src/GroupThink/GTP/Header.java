package GroupThink.GTP;


import java.nio.ByteBuffer;

public class Header extends GTPPacket {

    boolean ipv4, sequential;
    byte[] bytes;

    public Header(byte[] b) {
        super(b);

        if(b[2]==0)
            ipv4=false;
        else
            ipv4=true;
        
        if(b[3]==0)
            sequential=false;
        else
            sequential=true;
        
        this.bytes=b;
        setBytes();
    }

    public Header(boolean v4, boolean seq) {
        super(6);

        ipv4=v4;
        sequential=seq;
        
        byte[] b = new byte[calcBytesLength()];

        short num = (short) super.opCode;

        ByteBuffer dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(num);
        byte[] opBytes = dbuf.array();

        b[0] = opBytes[0];
        b[1] = opBytes[1];
        
        b[2]=0;
        b[3]=0;
        
        if(ipv4)
            b[2]=1;
        
        if(sequential)
            b[3]=1;
        

        this.bytes = b;
        setBytes();
    }

    public int calcBytesLength() {
        return 4;
    }

    public void setBytes() {
        super.bytes = this.bytes;
    }

    public byte[] getBytes() {
        return super.bytes;
    }
}
