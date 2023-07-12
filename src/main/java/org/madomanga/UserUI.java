package org.madomanga;

import com.scalar.db.exception.transaction.TransactionException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import static org.madomanga.CommonUI.newGenericListEntry;
import static org.madomanga.CommonUI.showGenericList;

public class UserUI {

    private DBManager db;
    private int userId;
    private String userName;

    public UserUI(DBManager db){
        this.db=db;
    }

    public void showUserList(Frame owner) throws TransactionException {
        showGenericList(owner, "Select user",db.getUsers(),
                (i,s) -> {
                    try {
                        userId=i;
                        userName=s;
                        showUserMainInterface();
                    } catch (TransactionException e) {
                        throw new RuntimeException(e);
                    }
                },
                parent -> newGenericListEntry(parent, "New user", "Please enter user name:", s -> {
                    try {
                        return db.create_user(s);
                    } catch (TransactionException e) {
                        throw new RuntimeException(e);
                    }
                }));
    }

    private void showUserMainInterface() throws TransactionException {
        JFrame frame = new JFrame("MadoManga: User mode ("+userName+")");
        frame.setSize(700,500);
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("My loans", null, loansList(),
                "Display current and past loans for the current user");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        JComponent panel2 = makeTextPanel("Panel #2");
        tabbedPane.addTab("Search book", null, panel2,
                "Search books in all libraries");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        frame.getContentPane().add(BorderLayout.CENTER, tabbedPane);
        frame.setVisible(true);
    }

    private JComponent loansList() {
        try {
            return CommonUI.loansList(db.getLoans(), "Current loans: ");
        } catch (TransactionException e) {
            throw new RuntimeException(e);
        }
    }

    protected JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }
}
