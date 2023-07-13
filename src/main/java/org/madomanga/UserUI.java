package org.madomanga;

import com.scalar.db.exception.transaction.TransactionException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import java.util.List;
import java.util.Map;

import static org.madomanga.CommonUI.*;

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
        frame.setSize(900,700);
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("My loans", null, loansList(),
                "Display current and past loans for the current user");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        tabbedPane.addTab("Search book", null, booksList(-1,db),
                "Search books in all libraries");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        frame.getContentPane().add(BorderLayout.CENTER, tabbedPane);
        frame.setVisible(true);
    }

    private JComponent loansList() {
        List<DBManager.LoanData> loans;
        Map<Integer,String> libraryMap;
        try {
            loans = db.getLoans().stream().filter(loan->loan.user_id()==userId).toList();
            libraryMap = db.getLibraries();
        } catch (TransactionException e) {
            throw new RuntimeException(e);
        }
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(CommonUI.loansList(loans.stream().filter(loan->loan.loaned()).toList(), "Current loans:", null, libraryMap, new CommonUI.LoanColumns[]{CommonUI.LoanColumns.USERNAME, CommonUI.LoanColumns.LOAN_STATUS, CommonUI.LoanColumns.RETURN_DATE}, null));
        panel.add(new JSeparator(SwingConstants.HORIZONTAL));
        panel.add(CommonUI.loansList(loans.stream().filter(loan->!loan.loaned()).toList(), "Past loans:", null, libraryMap, new CommonUI.LoanColumns[]{CommonUI.LoanColumns.USERNAME, CommonUI.LoanColumns.LOAN_STATUS}, null));
        return panel;
    }

}
