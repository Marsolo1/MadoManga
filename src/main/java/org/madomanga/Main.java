package org.madomanga;

import com.scalar.db.exception.transaction.TransactionException;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, TransactionException {
        final String scalarDBProperties = System.getProperty("user.dir") + File.separator + "scalardb.properties";
        DBManager db = new DBManager(scalarDBProperties);
        db.create_library("Animate", 10);
    }
}