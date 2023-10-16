package net.trellisframework.plugin.intellij;

import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.*;
import java.awt.*;

public class FindReplaceDialog extends DialogWrapper {
    private JTextField oldStringTextField;
    private JTextField newStringTextField;

    protected FindReplaceDialog() {
        super(true);
        init();
        setTitle("Find and Replace");
        setSize(400, 150);
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Old String:"));
        oldStringTextField = new JTextField();
        oldStringTextField.setPreferredSize(new Dimension(200, 30));
        panel.add(oldStringTextField);
        panel.add(new JLabel("New String:"));
        newStringTextField = new JTextField();
        newStringTextField.setPreferredSize(new Dimension(200, 30));
        panel.add(newStringTextField);
        oldStringTextField.setFocusable(true);
        oldStringTextField.requestFocusInWindow();
        return panel;
    }

    public String getOldString() {
        return oldStringTextField.getText();
    }

    public String getNewString() {
        return newStringTextField.getText();
    }
}