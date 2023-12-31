package org.madomanga;

import com.scalar.db.exception.transaction.TransactionException;

import javax.swing.*;

import org.madomanga.CommonUI.LoanColumns;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.function.Consumer;

import static org.madomanga.CommonUI.*;

public class LibraryUI {
    public static DBManager.BookData addBookDialog(JFrame owner, int library, DBManager db) {
        AtomicBoolean newBook = new AtomicBoolean(true);
        JTextField bookName = new JTextField();
        JTextField author = new JTextField();
        JTextField genre = new JTextField();
        JTextArea summary = new JTextArea();
        summary.setLineWrap(true);
        summary.setRows(3);
        JSpinner chapter = new JSpinner(new SpinnerNumberModel(1, 1, null, 1));
        JSpinner quantity = new JSpinner(new SpinnerNumberModel(1, 1, null, 1));
        author.setEnabled(false);
        genre.setEnabled(false);
        summary.setEnabled(false);

        bookName.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {

            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                DBManager.BookData data;
                try {
                    data = db.getBookData(bookName.getText());
                } catch (TransactionException ex) {
                    throw new RuntimeException(ex);
                }
                // New book
                if (data == null) {
                    newBook.set(true);
                    author.setEnabled(true);
                    genre.setEnabled(true);
                    summary.setEnabled(true);
                } else {
                    // Book already exists
                    newBook.set(false);
                    author.setEnabled(false);
                    genre.setEnabled(false);
                    summary.setEnabled(false);
                    author.setText(data.author());
                    genre.setText(data.genre());
                    summary.setText(data.summary());
                }
            }
        });

        int result = JOptionPane.showConfirmDialog(owner, new Object[] {
                "Book name", bookName,
                "Author", author,
                "Genre", genre,
                "Summary", new JScrollPane(summary),
                "Chapter #", chapter,
                "Quantity", quantity
        }, "New book", JOptionPane.OK_CANCEL_OPTION);
        try {
            if (result == JOptionPane.OK_OPTION) {
                DBManager.BookData data = null;
                if (newBook.get()) {
                    data = new DBManager.BookData(bookName.getText(), author.getText(), genre.getText(),
                            summary.getText());
                    db.create_book(data);
                }
                db.add_books_available(library, bookName.getText(), (int) chapter.getValue(),
                        (int) quantity.getValue());
                return data;
            }
        } catch (TransactionException ex) {
            throw new RuntimeException(ex);
        }
        return null;
    }

    private DBManager db;
    private int libraryId;
    private String libraryName;

    public LibraryUI(DBManager db) {
        this.db = db;
    }

    public void showLibraryList(Frame owner) throws TransactionException {
        showGenericList(owner, "Select library", db.getLibraries(),
                (i, s) -> {
                    try {
                        libraryId = i;
                        libraryName = s;
                        showLibraryMainInterface();
                    } catch (TransactionException e) {
                        throw new RuntimeException(e);
                    }
                },
                parent -> newGenericListEntry(parent, "New library", "Please enter library name",
                        (s) -> {
                            try {
                                int i = Integer.parseInt((String) JOptionPane.showInputDialog(
                                        parent,
                                        "Please enter return delay (days)",
                                        "Return delay",
                                        JOptionPane.PLAIN_MESSAGE,
                                        null,
                                        null,
                                        null));
                                return db.create_library(s, i);
                            } catch (TransactionException e) {
                                throw new RuntimeException(e);
                            }
                        }));
    }

    private void showLibraryMainInterface() throws TransactionException {
        JFrame frame = new JFrame("MadoManga: Library mode (" + libraryName + ")");
        frame.setSize(900, 700);
        JTabbedPane tabbedPane = new JTabbedPane();

        frame.getContentPane().add(BorderLayout.CENTER, tabbedPane);
        frame.setVisible(true);
        tabbedPane.addTab("Books", null, CommonUI.booksList(libraryId, db),
                "Display current books of the library");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        // TODO: Replace this... thing with a better way of updating, see the todo in
        // CommonUI
        class ReturnBook implements Consumer<DBManager.LoanData> {

            @Override
            public void accept(DBManager.LoanData loanData) {
                try {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDateTime now = LocalDateTime.now();
                    db.end_loan(loanData.user_id(), loanData.loan_id(), dtf.format(now).toString());
                    tabbedPane.setComponentAt(1, CommonUI.loansList(
                            db.getLoans().stream().filter(loan -> loan.library_id() == libraryId).toList(), "Loans",
                            db.getUsers(), null, new CommonUI.LoanColumns[] { LoanColumns.LIBRARY }, this));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        tabbedPane.addTab("Loans", null,
                CommonUI.loansList(db.getLoans().stream().filter(loan -> loan.library_id() == libraryId).toList(),
                        "Loans", db.getUsers(), null, new CommonUI.LoanColumns[] { LoanColumns.LIBRARY },
                        new ReturnBook()));
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        frame.getContentPane().add(BorderLayout.CENTER, tabbedPane);
        frame.setVisible(true);
    }

}
