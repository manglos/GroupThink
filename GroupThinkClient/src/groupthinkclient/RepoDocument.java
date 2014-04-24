package groupthinkclient;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wilhelmi on 4/23/14.
 */
public class RepoDocument {
    private final static double BYTES_TO_KB_FACTOR = 0.000976562;

    private String docName;
    private long sizeInBytes;
    private long lastModDate;

    public RepoDocument(String name, long size, long lastMod){
        this.docName = name;
        this.sizeInBytes = size;
        this.lastModDate = lastMod;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public Double getSizeInKb(){
        return getSizeInBytes()*BYTES_TO_KB_FACTOR;
    }

    public void setSizeInBytes(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    public long getLastModDate() {
        return lastModDate;
    }

    public void setLastModDate(long lastModDate) {
        this.lastModDate = lastModDate;
    }

    public String getMetaDataHTML(){
        DecimalFormat df = new DecimalFormat("#.##");
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        String time = sdf.format(new Date(lastModDate));
        String kb = df.format(getSizeInKb()).toString() + " Kb";

        String metaData = String.format("<html>%s<br>%s<br>%s</html>", docName, kb, time);

        return metaData;
    }

    public JLabel getFileIcon(){
        JLabel label;
        Border b = BorderFactory.createLineBorder(Color.black);
//        try{
//            ImageIcon img = new ImageIcon("doc2.png");
//            icon = new JLabel(getMetaDataHTML(), img, SwingConstants.CENTER);
//        } catch (IOException ioe){
//            ioe.printStackTrace();
//            icon = new JLabel(getMetaDataHTML(), SwingConstants.CENTER);
//        }
//
//        icon.setHorizontalAlignment(SwingConstants.CENTER);
//        icon.setHorizontalTextPosition(SwingConstants.CENTER);
//        icon = new JLabel(getMetaDataHTML(), SwingConstants.CENTER);
//        icon.setBorder(b);

        try {
            Image img = ImageIO.read(getClass().getResource("doc.png"));
            ImageIcon imgIcon = new ImageIcon(img);
            label = new JLabel(getMetaDataHTML(), imgIcon, SwingConstants.CENTER);
        } catch (IOException ioe){
            label = new JLabel(getMetaDataHTML());
        }

        return label;
    }
}
