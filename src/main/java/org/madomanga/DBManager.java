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

    public void close() {
        manager.close();
    }

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

    public void create_book(String name, int library_id, String author, int chapter, String genre, int qty)
            throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            tx.put(Put.newBuilder()
                    .namespace(LIB_NAMESPACE)
                    .table(BOOKS_TABLE)
                    .partitionKey(Key.ofText("book_name", name))
                    .clusteringKey(Key.of("library_id", library_id, "chapter", chapter))
                    .textValue("author", author)
                    .textValue("genre", genre)
                    .intValue("qty_available", qty)
                    .build());

            tx.commit();
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }

    public List<String> getBooks() throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            List<Result> res = tx.scan(Scan.newBuilder()
                    .namespace(LIB_NAMESPACE)
                    .table(BOOKS_TABLE)
                    .all()
                    .projection("book_name")
                    .build());

            List<String> resStr = res.stream()
                    .map(record -> record.getText("book_name"))
                    .toList();

            tx.commit();
            return resStr;
        } catch (Exception e) {
            tx.abort();
            throw e;
        }

    }

    public void create_loan(int user_id, int loan_id, String start_date, String limit_date, String return_date,
            boolean loaned)
            throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            tx.put(Put.newBuilder()
                    .namespace(LIB_NAMESPACE)
                    .table(LOANS_TABLE)
                    .partitionKey(Key.ofInt("user_id", user_id))
                    .intValue("loan_id", loan_id)
                    .textValue("start_date", start_date)
                    .textValue("limit_date", limit_date)
                    .textValue("return_date", return_date)
                    .booleanValue("loaned", loaned)
                    .build());

            tx.commit();
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }
}
