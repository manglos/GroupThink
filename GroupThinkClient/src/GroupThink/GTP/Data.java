package GroupThink.GTP;


import java.nio.ByteBuffer;
import java.util.Arrays;

public class Data extends GTPPacket {

    int blocknum;
    byte[] data;
    byte[] bytes;
    boolean isLast;

    public Data(int ir, int bn, byte[] d) {
        super(9, ir);
        data = d;
        blocknum=bn;

        byte[] b = new byte[calcBytesLength()];

        short num = (short) super.opCode;

        ByteBuffer dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(num);
        byte[] opBytes = dbuf.array();

        b[0] = opBytes[0];
        b[1] = opBytes[1];

        short iu = (short) super.intendedRecipient;

        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(iu);
        byte[] n = dbuf.array();

        b[2] = n[0];
        b[3] = n[1];

        num = (short)blocknum;

        dbuf = ByteBuffer.allocate(2);
        dbuf.putShort(num);
        byte[] bnBytes = dbuf.array();

        b[4] = bnBytes[0];
        b[5] = bnBytes[1];

        //data=trim(data);
        b=trim(b);

        for (int i = 0; i < data.length; i++) {
            b[6 + i] = data[i];
        }
        this.bytes = b;
        setBytes();
    }

    public Data(byte[] b) throws WrongPacketTypeException{
        super(b);
        if(super.getOP()!=9)
            throw new WrongPacketTypeException("Invalid DATA Packet.");

        isLast = false;

        byte[] ir = new byte[2];
        ir[0] = b[2];
        ir[1] = b[3];

        ByteBuffer bb = ByteBuffer.wrap(ir);
        intendedRecipient = bb.getShort();


        byte[] bn = new byte[2];
        bn[0] = b[4];
        bn[1] = b[5];

        bb = ByteBuffer.wrap(bn);
        blocknum = (int)bb.getShort();

//        for(int i=4;i<b.length;i++){
//            if(b[i]==0){
//                isLast=true;
//            }
//        }

        if(b[b.length-1]==0)
            isLast=true;

        //data=trim(data);
        b=trim(b);

        data = new byte[b.length-6];
        for(int i=0;i<data.length;i++)
            data[i] = b[i+6];

        this.bytes=b;
        setBytes();
    }

    public int getBlockNum(){
        return blocknum;
    }

    public boolean isLastPacket(){
        return isLast;
    }

    public int calcBytesLength() {
        return 6 + data.length;
    }

    public void setBytes() {

        super.bytes = this.bytes;
    }

    public byte[] getBytes() {
        return super.bytes;
    }



    private byte[] trim(byte[] bytes){

        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0)
        {
            --i;
        }

        return Arrays.copyOf(bytes, i + 1);
    }
}