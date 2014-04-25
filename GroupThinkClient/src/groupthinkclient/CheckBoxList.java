package groupthinkclient;

/**
 * Created by wilhelmi on 4/23/14.
 * Based on:
 *  http://www.devx.com/tips/Tip/5342
 */

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Vector;

public class CheckBoxList extends JList{
    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
    protected static Vector<JCheckBox> checkboxes;
    protected static DefaultListModel dflm;

    public CheckBoxList(Object[] items){
        dflm = new DefaultListModel();
        setModel(dflm);
        checkboxes = new Vector<JCheckBox>();
        for(Object o : items){
            checkboxes.add(new JCheckBox(o.toString()));
        }
        setListData(checkboxes.toArray());


        setCellRenderer(new CellRenderer());
        addMouseListener(new MouseAdapter(){
            public void mousePressed(MouseEvent e){
                 int index = locationToIndex(e.getPoint());
                 if (index != -1) {
                     JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
                     checkbox.setSelected(!checkbox.isSelected());
                     repaint();
                 }
             }
         });

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public boolean allChecked(){
        boolean allChecked = true;

        for(JCheckBox b : checkboxes){
            if(!b.isSelected()){
                allChecked = false;
                break;
            }
        }
        return allChecked;
    }

    public ArrayList<String> getCheckedItemNames(){
        ArrayList<String> checked = new ArrayList<String>();

        for(JCheckBox b : checkboxes){
            if(b.isSelected()){
                checked.add(b.getText());
            }
        }

        return checked;
    }

    public void addName(String name){
        dflm.addElement(new JCheckBox(name));
    }

    protected class CellRenderer implements ListCellRenderer{
        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus){
            Color selectedBGColor = new Color(32, 255, 0);
            Color selectedFGColor = new Color(10, 212, 0);

            JCheckBox checkbox = new JCheckBox();
            if(value instanceof JCheckBox) {
               checkbox = (JCheckBox) value;
            } else if(value instanceof String) {
                checkbox = new JCheckBox((String) value);
            }
            checkbox.setBackground(isSelected ? selectedBGColor : getBackground());
            checkbox.setForeground(isSelected ? selectedFGColor : getForeground());
            checkbox.setEnabled(isEnabled());
            checkbox.setFont(getFont());
            checkbox.setFocusPainted(false);
            checkbox.setBorderPainted(true);
            checkbox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
            return checkbox;
        }
    }
}
