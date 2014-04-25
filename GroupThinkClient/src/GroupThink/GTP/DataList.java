package GroupThink.GTP;

import java.util.ArrayList;

public class DataList{

    byte[] fileBytes;
    ArrayList<Data> data;
    int blocksize;
    int currentPiece;
    String file;

    public DataList(int bs){
        blocksize=bs;
        data=new ArrayList<Data>();
    }

    public String getFile() {

        int size=0;

        for(Data d : data){
            size+=d.getBytes().length-6;
        }

        fileBytes = new byte[size];
        System.out.println("filebyes is " + size);

        for(Data d : data){
            for(int i=6;i<d.getBytes().length;i++){
                fileBytes[(i+(d.blocknum*blocksize)-6)] = d.getBytes()[i];
            }
        }

        file = new String(fileBytes);


        return file;//.replaceAll("\u0000", "");
    }

    public void addData(Data d){
        data.add(d);
    }


}