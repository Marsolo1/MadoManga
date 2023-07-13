package org.madomanga;

import com.scalar.db.exception.transaction.TransactionException;

import java.io.File;
import java.io.IOException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import org.madomanga.DBManager.BookData;
import org.madomanga.DBManager.LoanData;
import static org.madomanga.CommonUI.*;

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
        libraryMode.addActionListener(e -> {
            try {
                new LibraryUI(db).showLibraryList(frame);
            } catch (TransactionException ex) {
                throw new RuntimeException(ex);
            }
        });
        JButton userMode = new JButton("User mode");
        userMode.addActionListener(e -> {
            try {
                new UserUI(db).showUserList(frame);
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




}