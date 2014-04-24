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
import java.util.Vector;

public class CheckBoxList extends JList{
    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
    protected static Vector checkboxes;

    public CheckBoxList(Object[] items){
        checkboxes = new Vector();
        for(Object o : items){
            checkboxes.add(new JCheckBox(o.toString()));
        }
        setListData(checkboxes);


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