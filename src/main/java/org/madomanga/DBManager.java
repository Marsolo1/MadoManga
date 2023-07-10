package org.madomanga;

import com.scalar.db.api.*;
import com.scalar.db.exception.transaction.TransactionException;
import com.scalar.db.io.Key;
import com.scalar.db.service.TransactionFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public class DBManager {

    private static final String USER_NAMESPACE = "users";
    private static final String LIB_NAMESPACE = "libraries";
    private static final String LIB_TABLE = "libraries";
    private static final String BOOKS_AVAILABLE = "books_available";
    private static final String BOOKS_LIST = "books";
    private static final String LOANS_TABLE = "loans";
    private static final String USERS_TABLE = "users";

    public record BookData(
            String name,
            String author,
            String genre,
            String summary
    ){};

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
                    .table(BOOKS_AVAILABLE)
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
                    .table(BOOKS_AVAILABLE)
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

    // Gives a map, with the library ID as a key, and a map of (chapter, number of books available) as a value
    public Map<Integer, Map<Integer,Integer>> getBookAvailability(String bookName) throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            List<Result> res = tx.scan(Scan.newBuilder()
                    .namespace(LIB_NAMESPACE)
                    .table(BOOKS_AVAILABLE)
                    .partitionKey(Key.ofText("book_name", bookName))
                    .projections("library_id", "chapter", "qty_available")
                    .build());

            Map<Integer, Map<Integer,Integer>> resMap = res.stream()
                    .collect(Collectors.groupingBy(key -> key.getInt("library_id"), Collectors.toMap(key -> key.getInt("chapter"), key-> key.getInt("qty_available"))));

            tx.commit();
            return resMap;
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }

    public int create_loan(int user_id, String start_date, String limit_date, String return_date,
            boolean loaned)
            throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            List<Result> res = tx.scan(Scan.newBuilder()
                    .namespace(LIB_NAMESPACE)
                    .table(LOANS_TABLE)
                    .all()
                    .projection("loan_id")
                    .build());

            int id = res.stream()
                    .mapToInt(record -> record.getInt("loan_id"))
                    .max().orElse(-1) + 1;

            tx.put(Put.newBuilder()
                    .namespace(LIB_NAMESPACE)
                    .table(LOANS_TABLE)
                    .partitionKey(Key.ofInt("user_id", user_id))
                    .clusteringKey(Key.ofInt("loan_id", id))
                    .textValue("start_date", start_date)
                    .textValue("limit_date", limit_date)
                    .textValue("return_date", return_date)
                    .booleanValue("loaned", loaned)
                    .build());

            tx.commit();
            return id;
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }

    public int create_user(String name)
            throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            List<Result> res = tx.scan(Scan.newBuilder()
                    .namespace(USER_NAMESPACE)
                    .table(USERS_TABLE)
                    .all()
                    .projection("user_id")
                    .build());

            int id = res.stream()
                    .mapToInt(record -> record.getInt("user_id"))
                    .max().orElse(-1) + 1;

            tx.put(Put.newBuilder()
                    .namespace(USER_NAMESPACE)
                    .table(USERS_TABLE)
                    .partitionKey(Key.ofInt("user_id", id))
                    .textValue("user_name", name)
                    .build());

            tx.commit();
            return id;
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }

    public BookData getBookData(String book_name) throws TransactionException {
        DistributedTransaction tx = manager.start();
        try {
            Optional<Result> res = tx.get(
                    Get.newBuilder()
                    .namespace(USER_NAMESPACE)
                    .table(BOOKS_LIST)
                    .partitionKey(Key.ofText("book_name", book_name))
                    .build());

            tx.commit();

            if (res.isEmpty()) return null;

            return new BookData(res.get().getText("book_name"), res.get().getText("author"), res.get().getText("genre"), res.get().getText("summary"));
        } catch (Exception e) {
            tx.abort();
            throw e;
        }
    }

}
