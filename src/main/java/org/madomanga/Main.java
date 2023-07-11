package org.madomanga;

import com.scalar.db.exception.transaction.TransactionException;

import java.io.File;
import java.io.IOException;

import org.madomanga.DBManager.BookData;
import org.madomanga.DBManager.LoanData;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Main {

    static DBManager db;
    public static void main(String[] args) throws IOException, TransactionException, Exception {
        final String scalarDBProperties = System.getProperty("user.dir") + File.separator + "scalardb.properties";
        db = new DBManager(scalarDBProperties);

        JFrame frame = new JFrame("MadoManga");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(600,300);
        JPanel panel = new JPanel();
        JButton libraryMode = new JButton("Library mode");
        JButton userMode = new JButton("User mode");
        userMode.addActionListener(e -> {
            try {
                showUserList(frame);
            } catch (TransactionException ex) {
                throw new RuntimeException(ex);
            }
        });
        panel.add(libraryMode);
        panel.add(userMode);
        frame.getContentPane().add(BorderLayout.CENTER, panel);
        frame.setVisible(true);

        // Action executed when we close the main window
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.setVisible(false);
                frame.dispose();
                System.out.println("Closing the database...");
                db.close();
            }
        });

        /*
        db.create_library("Animate", 10);
        db.create_book(new BookData("One Piece", "Eiichiro Oda", "Shonen", "blabla"));
        db.create_book(new BookData("Berserk", "Kentaro Miura", "Seinen", "blabla"));
        db.create_book(new BookData("Naruto", "Masashi Kishimoto", "Shonen", "blabla"));
        db.create_book(new BookData("Dragon Ball", "Akira Toriyama", "Shonen", "blabla"));
        db.create_book(new BookData("Jojo's Bizarre Adventure", "Hirohiko Araki", "Shonen", "blabla"));
        db.create_book(new BookData("Hunter x Hunter", "Yoshihiro Togashi", "Shonen", "blabla"));
        db.create_book(new BookData("My Hero Academia", "Kohei Horikoshi", "Shonen", "blabla"));
        db.create_book(new BookData("Fullmetal Alchemist", "Hiromu Arakawa", "Shonen", "blabla"));
        db.create_book(new BookData("Death Note", "Tsugumi Ohba", "Shonen", "blabla"));
        db.create_book(new BookData("Demon Slayer", "Koyoharu Gotouge", "Shonen", "blabla"));

        db.create_user("Martial");
        db.create_user("Dorian");

        db.add_books_available(0, "Berserk", 400, 5);
        db.add_books_available(0, "One Piece", 100, 5);

        for (String s : db.getBooks())
            System.out.println(s);

        System.out.println(db.getBookData("Berserk"));

        db.create_loan(0, 0, "Berserk", 400, "2023-01-01", "2023-03-01");
        db.create_loan(0, 0, "One Piece", 100, "2023-01-01", "2023-03-01");

        System.out.println(db.getBookAvailability("Berserk"));

        for (LoanData l : db.getLoans())
            System.out.println(l);

        db.close();*/
    }

    private static void showUserList(Frame owner) throws TransactionException {
        showGenericList(owner, "Select user",db.getUsers(),
                (i,s) -> System.out.println(s+" ("+i+")"),
                parent -> newGenericListEntry(parent, "New user", "Please enter user name:", s -> {
                    try {
                        return db.create_user(s);
                    } catch (TransactionException e) {
                        throw new RuntimeException(e);
                    }
                }));
    }

    // To display the name but keep the id, we can use this class
    static class cell {
        public String name;
        public int id;
        public cell(int i, String s) {
            id = i;
            name = s;
        }

        @Override
        public String toString() {
            return name+" ("+id+")";
        }
    }

    private static void showGenericList(Frame owner, String windowName, Map<Integer, String> content, BiConsumer<Integer, String> onSelect, Function<JDialog, cell> onNew) {

        JDialog dialog = new JDialog(owner, windowName);
        JPanel panel = new JPanel(new BorderLayout());
        dialog.setSize(400,500);

        // List
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        DefaultListModel listModel = new DefaultListModel();
        content.entrySet().stream().map(kv -> new cell(kv.getKey(), kv.getValue())).forEach(e->listModel.addElement(e));
        JList list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.VERTICAL);
        list.setVisibleRowCount(-1);
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(250, 80));
        listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.add(listScroller);
        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));


        // Buttons

        JButton cancelBtn = new JButton("Cancel");
        JButton newBtn = new JButton("Add new");
        JButton selectBtn = new JButton("Select");
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane,
                BoxLayout.LINE_AXIS));
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(cancelBtn);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(newBtn);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(selectBtn);
        selectBtn.setEnabled(false);

        // When we select a name, we make the select button clickable
        list.addListSelectionListener(e -> selectBtn.setEnabled(list.getSelectedValue()!=null));

        // Button functions
        selectBtn.addActionListener(e -> {
            dialog.setVisible(false);
            dialog.dispose();
            onSelect.accept(((cell)list.getSelectedValue()).id,((cell)list.getSelectedValue()).name );
        });
        newBtn.addActionListener(e -> {
            cell c = onNew.apply(dialog);
            if (c != null) {
                listModel.addElement(c);
                list.setVisibleRowCount(-1);
            }
        });
        cancelBtn.addActionListener(e -> {
            dialog.setVisible(false);
            dialog.dispose();
        });

        // Add to layout

        panel.add(listPane, BorderLayout.CENTER);
        panel.add(buttonPane, BorderLayout.PAGE_END);
        dialog.getContentPane().add(BorderLayout.CENTER, panel);
        dialog.setVisible(true);
    }

    private static cell newGenericListEntry(JDialog parent, String windowName, String prompt, Function<String, Integer> addMethod) {
        String s = (String)JOptionPane.showInputDialog(
                parent,
                prompt,
                windowName,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);
        // If we click on "cancel", s will be null. We also check if we didn't put anything
        if (s==null || s.equals(""))
            return null;
        return new cell(addMethod.apply(s), s);
    }
}