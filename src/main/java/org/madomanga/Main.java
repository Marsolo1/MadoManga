package org.madomanga;

import com.scalar.db.exception.transaction.TransactionException;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, TransactionException {
        final String scalarDBProperties = System.getProperty("user.dir") + File.separator + "scalardb.properties";
        DBManager db = new DBManager(scalarDBProperties);
        db.create_library("Animate", 10);
        db.create_book("One Piece", 1, "Eiichiro Oda", 1000, "Shonen", 100);
        db.create_book("Naruto", 1, "Masashi Kishimoto", 700, "Shonen", 100);
        db.create_book("Bleach", 1, "Tite Kubo", 700, "Shonen", 100);
        db.create_book("Dragon Ball", 1, "Akira Toriyama", 500, "Shonen", 100);
        db.create_book("Death Note", 1, "Tsugumi Ohba", 100, "Shonen", 100);
        db.create_book("Berserk", 1, "Kentaro Miura", 400, "Seinen", 100);
        db.create_book("Vagabond", 1, "Takehiko Inoue", 300, "Seinen", 100);
        db.create_book("Gantz", 1, "Hiroya Oku", 300, "Seinen", 100);
        db.create_book("Monster", 1, "Naoki Urasawa", 200, "Seinen", 100);
    }
}