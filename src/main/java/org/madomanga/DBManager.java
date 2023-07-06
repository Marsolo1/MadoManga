package org.madomanga;

import com.scalar.db.api.*;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.io.Key;
import com.scalar.db.service.TransactionFactory;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import static java.util.Arrays.stream;

public class DBManager {

    private static final String USER_NAMESPACE = "users";
    private static final String LIB_NAMESPACE = "libraries";
    private static final String LIB_TABLE = "libraries";
    private static final String BOOKS_TABLE = "books";
    private static final String LOANS_TABLE = "loans";
    private static final String USERS_TABLE = "users";

    private final DistributedTransactionManager manager;

    public DBManager(String scalarDBProperties) throws IOException {
        TransactionFactory factory = TransactionFactory.create(scalarDBProperties);
        manager = factory.getTransactionManager();
    }

    public int create_library(String name, int returnDelay) throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            List<Result> res = tx.scan(Scan.newBuilder()
                    .namespace(LIB_NAMESPACE)
                    .table(LIB_TABLE)
                    .all()
                    .projection("library_id")
                    .build());

            int id = res.stream()
                    .mapToInt(record -> record.getInt("library_id"))
                    .max().orElse(-1) + 1;

            tx.put(Put.newBuilder()
                    .namespace(LIB_NAMESPACE)
                    .table(LIB_TABLE)
                    .partitionKey(Key.ofInt("library_id", id))
                    .textValue("library_name", name)
                    .intValue("return_delay", returnDelay)
                    .build());

            tx.commit();
            return id;
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }

    public int create_book(String name, int library_id, String author, int chapter, String genre, int qty)
            throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            tx.put(Put.newBuilder()
                    .namespace(LIB_NAMESPACE)
                    .table(BOOKS_TABLE)
                    .partitionKey(Key.ofText("book_name", name))
                    .intValue("library_id", name)
                    .textValue("author", author)
                    .intValue("chapter", chapter)
                    .textValue("genre", genre)
                    .intValue("qty_available", qty)
                    .build());

            tx.commit();
            return id;
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }
}
