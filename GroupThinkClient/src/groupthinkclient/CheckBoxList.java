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
import java.util.HashMap;
import java.util.Vector;

public class CheckBoxList extends JList{
    private final UserColorGenerator UCG;

    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
    protected static Vector<Component> items;
    protected static DefaultListModel dflm;
    protected static JLabel nameLabel;
    private static String clientName;
    private static HashMap<String, Color> nameToFontColorMap;
    private static Object[] myItems;

    public CheckBoxList(Object[] items){
        UCG = new UserColorGenerator();

        nameToFontColorMap = new HashMap<String, Color>();
        myItems=items;
        
        dflm = new DefaultListModel();
        setModel(dflm);
        CheckBoxList.items = new Vector<Component>();

        
        //the first entry in the list should be a JLabel of the user's name
        


        setCellRenderer(new CellRenderer());
        addMouseListener(new MouseAdapter(){
            public void mousePressed(MouseEvent e){
                 int index = locationToIndex(e.getPoint());
                 if (index != -1 && (getModel().getElementAt(index) instanceof JCheckBox)) {
                     JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
                     checkbox.setSelected(!checkbox.isSelected());
                     repaint();
                 }
             }
         });

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    
    public int getUserIndex(String name){
        for(Component c : items) {
            if (c instanceof JCheckBox) {
                JCheckBox b = (JCheckBox) c;
                if (b.getText().equalsIgnoreCase(name)) {
                    return items.indexOf(b);
                }
            }
        }
        
        return -1;
    }
    
    public void setInactive(String name){
        
        int index = getUserIndex(name);
        
        //System.out.println("setting inactive... " + index);
        if(index>=0){
            ((JCheckBox)items.get(index)).setText("("+name+")");
            setListData(items);
        }
    }
    
    public void setActive(String name){
        int index = getUserIndex("("+name+")");
        //System.out.println(index);
        if(index>=0){
            ((JCheckBox)items.get(index)).setText(name);
            setListData(items);
        }
    }

    public Color getUserColor(String name){
        return nameToFontColorMap.get(name);
    }
    
    public void setUsername(String un){
        clientName=un;
        
        nameLabel = new JLabel();
        nameLabel.setText("<html><b>" + clientName + "</b></html>");
        nameLabel.setOpaque(true);
        Color bg =  new Color(0, 0, 0);
        Color fg =  new Color(255, 255, 255);
        nameLabel.setBackground(bg);
        nameLabel.setForeground(fg);
        nameLabel.setHorizontalAlignment(JLabel.CENTER);


        CheckBoxList.items.add(0, nameLabel);


        for(Object o : myItems){
            
            if(!o.toString().equals(clientName)){
                nameToFontColorMap.put(o.toString(), UCG.getNextUserColor());
                JCheckBox box = new JCheckBox(o.toString());
                CheckBoxList.items.add(new JCheckBox(o.toString()));
            }
        }
        setListData(CheckBoxList.items.toArray());
    }

    public boolean allChecked(){
        boolean allChecked = true;

        for(Component c : items){
            if (c instanceof JCheckBox) {
                JCheckBox b = (JCheckBox) c;
                if(!b.isSelected()){
                    allChecked = false;
                    break;
                }
            }
        }
        return allChecked;
    }

    public ArrayList<String> getCheckedItemNames(){
        ArrayList<String> checked = new ArrayList<String>();

        for(Component c : items) {
            if (c instanceof JCheckBox) {
                JCheckBox b = (JCheckBox) c;
                if (b.isSelected()) {
                    checked.add(b.getText());
                }
            }
        }

        return checked;
    }

    public void removeName(String name){
        //TODO add code to remove names from the list
        Component toRemove = null;
        for(Component c : items) {
            if (c instanceof JCheckBox) {
                JCheckBox b = (JCheckBox) c;
                if (b.getText().equalsIgnoreCase(name)) {
                    toRemove = c;
                    break;
                }
            }
        }
        if(toRemove != null){
            items.remove(toRemove);
        }
        
        nameToFontColorMap.remove(name);
        setListData(items);
    }

    public void addName(String name){
//        dflm.addElement(new JCheckBox(name));

        //check if the name already exists in the list...
        boolean userExists = false;
        for(Component c : items) {
            if (c instanceof JCheckBox) {
                JCheckBox b = (JCheckBox) c;
                if (b.getText().equalsIgnoreCase(name)) {
                    userExists = true;
                    break;
                }
            }
        }
        if(!userExists){
            items.add(new JCheckBox(name));
            setListData(items);
            nameToFontColorMap.put(name, UCG.getNextUserColor());
        }
    }

    public boolean containsName(String name){
        boolean contains = false;
        String inactiveName = "("+name+")";

        for(Component c : items) {
            if (c instanceof JCheckBox) {
                JCheckBox b = (JCheckBox) c;
                if (b.getText().equalsIgnoreCase(name) || b.getText().equalsIgnoreCase(inactiveName)) {
                    contains = true;
                    break;
                }
            }
        }
        return contains;
    }

    protected class CellRenderer implements ListCellRenderer{
        public Component getListCellRendererComponent(JList list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus){
            if (value instanceof JCheckBox) {
                Color selectedBGColor = new Color(253, 253, 253);
                Color selectedFGColor = new Color(0, 0, 0);

                JCheckBox checkbox = (JCheckBox) value;
                checkbox.setOpaque(true);
                checkbox.setBackground(isSelected ? selectedBGColor : nameToFontColorMap.get(checkbox.getText()));
                checkbox.setForeground(isSelected ? selectedFGColor : Color.white);
                checkbox.setEnabled(isEnabled());
                checkbox.setFont(getFont());
                checkbox.setFocusPainted(false);
                checkbox.setBorderPainted(true);
                checkbox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);

                return checkbox;
            } else if(value instanceof JLabel){
                JLabel label = (JLabel)value;
                label.setFont(getFont());

                return label;
            } else{
                Color selectedBGColor = new Color(32, 255, 0);
                Color selectedFGColor = new Color(10, 212, 0);

                JCheckBox checkbox = new JCheckBox(value.toString());

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
}
