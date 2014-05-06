package groupthinkclient;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by wilhelmi on 4/29/14.
 */
public class UserColorGenerator {
    private ArrayList<Color> colors;
    private int nextColor;

    public UserColorGenerator(){
        nextColor = 0;

        colors = new ArrayList<Color>();

        colors.add(Color.blue.darker());
        colors.add(Color.cyan.darker());
        colors.add(Color.gray.darker());
        colors.add(Color.green.darker());
        colors.add(Color.magenta.darker());
        colors.add(Color.orange.darker());
        colors.add(Color.pink.darker());
        colors.add(Color.red.darker());
        colors.add(Color.yellow.darker());

        for(int i=0;i<9;i++){
            colors.add(colors.get(i).darker());
        }
    }

//    public static UserColorGenerator getInstance(){
//        if(instance == null){
//            instance = new UserColorGenerator();
//        }
//        return instance;
//    }

    public Color getNextUserColor(){
        Color c = colors.get(nextColor);
        nextColor++;
        if(nextColor >= colors.size()){
            nextColor = 0;
        }
        return c;
    }

//    public static void main(String[] args){
//        UserColorGenerator ucg = new UserColorGenerator();
//
//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
//
//                JFrame f = new JFrame();
//                JPanel p = new JPanel(new FlowLayout());
//                for(Color c : colors){
//                    JLabel l = new JLabel("[][][][]");
//                    l.setForeground(Color.white);
//                    l.setBackground(c);
//                    l.setOpaque(true);
//                    p.add(l);
//                }
//                f.setContentPane(p);
//                f.pack();
//                f.setVisible(true);
//
//            }
//        });
//    }
}