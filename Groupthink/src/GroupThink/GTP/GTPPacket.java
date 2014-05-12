package GroupThink.GTP;

import java.nio.ByteBuffer;

public abstract class Packet {

    int opCode;
    byte[] bytes;

    Packet(int o) {
        opCode = o;
    }
    
    Packet(byte[] b) {
        byte[] op = new byte[2];
        op[0] = b[0];
        op[1] = b[1];
        
        ByteBuffer bb = ByteBuffer.wrap(op);
        opCode = (int)bb.getShort();
    }

    public int getOP() {
        return opCode;
    }

    abstract byte[] getBytes();

    abstract void setBytes();

}
