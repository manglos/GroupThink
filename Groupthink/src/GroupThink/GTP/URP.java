package GroupThink.GTP;


import java.nio.ByteBuffer;

public class URP extends Packet {

    String username;
    byte[] usernameBytes;
    byte[] bytes;

    public URP(String un) {
        super(5);
        username = un;
        usernameBytes = username.getBytes();
        
        byte[] b = new byte[calcBytesLength()];

        short num = (short) super.opCode;

        ByteBuffer dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(num);
        byte[] opBytes = dbuf.array();

        b[0] = opBytes[0];
        b[1] = opBytes[1];


        for (int i = 0; i < usernameBytes.length; i++) {
            b[2 + i] = usernameBytes[i];
        }

        b[b.length-1] = 0;

        this.bytes = b;
        setBytes();
    }


    public URP(byte[] b) throws WrongPacketTypeException{
        super(b);
        
        if(super.getOP()!=5){
            throw new WrongPacketTypeException("Not a valid URP Packet");
        }
        
        byte[] ub = new byte[b.length-3];
        for(int i=0;i<ub.length;i++){
            ub[i] = b[i+2];
        }
        
        username = new String(ub);

        this.bytes=b;
        setBytes();
    }
    
    public String getUsername() {
        return username;
    }

    public int calcBytesLength() {
        return 2 + usernameBytes.length + 1;
    }

    public void setBytes() {
        super.bytes = this.bytes;
    }

    public byte[] getBytes() {
        return super.bytes;
    }
}
