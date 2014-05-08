/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package groupthinkclient;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 *
 * @author angie
 */
public class ChangeLogger extends DocumentFilter {
    // Hold onto the client so that you have access to all datastructures:
    private final GroupThinkClient client;

    public ChangeLogger(GroupThinkClient client) {
        this.client = client;
    }

    @Override
    public void insertString(DocumentFilter.FilterBypass fb, int offset, String text,
            AttributeSet attr) throws BadLocationException {
        System.out.println("insert string " + text);
        super.insertString(fb, offset, text, attr);
    }
    
    @Override
    public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text,
            AttributeSet attrs) throws BadLocationException {
        System.out.println("replace " + text);
        super.replace(fb, offset, length, text, attrs);
    }

    @Override
    public void remove(DocumentFilter.FilterBypass fb, int offset, int length)
            throws BadLocationException {
        System.out.println("remove");
        super.remove(fb, offset, length);
    }
} 
