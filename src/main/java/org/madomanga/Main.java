package org.madomanga;

import com.scalar.db.exception.transaction.TransactionException;

import java.io.File;
import java.io.IOException;

import org.madomanga.DBManager.BookData;
import org.madomanga.DBManager.LoanData;

public class Main {
    public static void main(String[] args) throws IOException, TransactionException, Exception {
        final String scalarDBProperties = System.getProperty("user.dir") + File.separator + "scalardb.properties";
        DBManager db = new DBManager(scalarDBProperties);
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


        for (LoanData l : db.getLoans())
            System.out.println(l);

        db.close();
    }
}