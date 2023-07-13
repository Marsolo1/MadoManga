package org.madomanga;

import com.scalar.db.exception.transaction.TransactionException;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommonUI {

    // To display the name but keep the id, we can use this class
    static class NameIdCell {
        public String name;
        public int id;
        public NameIdCell(int i, String s) {
            id = i;
            name = s;
        }

        @Override
        public String toString() {
            return name+" ("+id+")";
        }
    }

    public static void showGenericList(Frame owner, String windowName, Map<Integer, String> content, BiConsumer<Integer, String> onSelect, Function<JDialog, NameIdCell> onNew) {

        JDialog dialog = new JDialog(owner, windowName);
        JPanel panel = new JPanel(new BorderLayout());
        dialog.setSize(400,500);

        // List
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        DefaultListModel listModel = new DefaultListModel();
        content.entrySet().stream().map(kv -> new NameIdCell(kv.getKey(), kv.getValue())).forEach(e->listModel.addElement(e));
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
            onSelect.accept(((NameIdCell)list.getSelectedValue()).id,((NameIdCell)list.getSelectedValue()).name );
        });
        newBtn.addActionListener(e -> {
            NameIdCell c = onNew.apply(dialog);
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

    static NameIdCell newGenericListEntry(JDialog parent, String windowName, String prompt, Function<String, Integer> addMethod) {
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
        return new NameIdCell(addMethod.apply(s), s);
    }

    // Used for the loans list
    public enum LoanColumns{
        USERNAME,
        LIBRARY,
        BOOK_NAME,
        CHAPTER,
        START_DATE,
        LIMIT_DATE,
        RETURN_DATE,
        LOAN_STATUS
    }

    private static class LoanTableModel extends AbstractTableModel {
        private List<DBManager.LoanData> data;
        private LoanColumns[] columns;

        private Map<Integer, String> usernameMap;
        private Map<Integer, String> libraryMap;
        private static final Map<LoanColumns,String> columnNames = Stream.of(new Object[][]{
                {LoanColumns.USERNAME, "User"},
                {LoanColumns.LIBRARY, "Library"},
                {LoanColumns.BOOK_NAME, "Book name"},
                {LoanColumns.CHAPTER, "Chapter"},
                {LoanColumns.START_DATE, "Start date"},
                {LoanColumns.LIMIT_DATE, "Limit date"},
                {LoanColumns.RETURN_DATE, "Return date"},
                {LoanColumns.LOAN_STATUS, "Currently loaned"}
        }).collect(Collectors.toMap(data->(LoanColumns)data[0], data->(String)data[1]));

        public LoanTableModel(List<DBManager.LoanData> data, Map<Integer, String> usernameMap, Map<Integer, String> libraryMap) {
            this(data, usernameMap, libraryMap, new LoanColumns[0]);
        }

        public LoanTableModel(List<DBManager.LoanData> data,Map<Integer, String> usernameMap, Map<Integer, String> libraryMap, LoanColumns[] hiddenColumns) {
            this.data=data;
            this.usernameMap=usernameMap;
            this.libraryMap=libraryMap;
            List<LoanColumns> remove = Arrays.stream(hiddenColumns).toList();
            List<LoanColumns> remaining = Arrays.stream(LoanColumns.values()).filter(c -> !remove.contains(c)).toList();
            columns = remaining.toArray(new LoanColumns[remaining.size()]);
        }

        @Override
        public int getRowCount() {
            return data.size()  ;
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public Object getValueAt(int row, int col) {
            DBManager.LoanData rowData = data.get(row);
            switch (columns[col]) {
                case USERNAME -> {
                    if (usernameMap==null) return rowData.user_id();
                    return usernameMap.get(rowData.user_id());
                }
                case LIBRARY -> {
                    if (libraryMap==null) return rowData.library_id();
                    return libraryMap.get(rowData.library_id());
                }
                case BOOK_NAME -> {
                    return rowData.book_name();
                }
                case CHAPTER -> {
                    return rowData.chapter();
                }
                case START_DATE -> {
                    return rowData.start_date();
                }
                case LIMIT_DATE -> {
                    return rowData.limit_date();
                }
                case RETURN_DATE -> {
                    return rowData.return_date();
                }
                case LOAN_STATUS -> {
                    return rowData.loaned();
                }
            }
            return null;
        }
        @Override
        public String getColumnName(int col) {
            return columnNames.get(columns[col]);
        }
    }

    // TODO: Change the way this function works: we should connect to the database and dynamically update the content (like booksList)
    public static JComponent loansList(List<DBManager.LoanData> list, String header, Map<Integer,String> usernameMap, Map<Integer,String> libraryMap, LoanColumns[] hiddenColumns, Consumer<DBManager.LoanData> onReturn) {
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        JTable table = new JTable(new LoanTableModel(list, usernameMap, libraryMap, hiddenColumns));
        table.setFillsViewportHeight(true);
        JLabel label = new JLabel(header);
        JScrollPane scrollPane = new JScrollPane(table);
        label.setLabelFor(scrollPane);
        listPane.add(label);
        listPane.add(scrollPane);
        if (onReturn!=null) {
            JButton returnBtn = new JButton("Return book");
            returnBtn.addActionListener(e->onReturn.accept(list.get(table.getSelectedRow())));
            listPane.add(returnBtn);
        }
        listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        return listPane;
    }

    public static JComponent booksList(int libraryFilter, DBManager db) {
        // TODO: Makes the library filter work
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

        // Books list
        JPanel booksPanel = new JPanel();
        booksPanel.setLayout(new BoxLayout(booksPanel, BoxLayout.PAGE_AXIS));
        DefaultListModel listModel = new DefaultListModel();
        try {
            List<String> books = db.getBooks();
            books.stream().filter(b-> {
                try {
                    return !db.getBookAvailability(b).get(libraryFilter).isEmpty();
                } catch (TransactionException e) {
                    throw new RuntimeException(e);
                }
            });
            listModel.addAll(books);
        } catch (TransactionException e) {
            throw new RuntimeException(e);
        }
        JList booksList = new JList(listModel);
        booksList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        booksList.setLayoutOrientation(JList.VERTICAL);
        booksList.setVisibleRowCount(-1);
        JScrollPane booksListScroller = new JScrollPane(booksList);
        booksListScroller.setPreferredSize(new Dimension(250, 80));
        JLabel labelBooks = new JLabel("Books:");
        labelBooks.setLabelFor(booksListScroller);
        booksPanel.add(labelBooks);
        booksPanel.add(booksListScroller);

        if (libraryFilter!=-1) {
            JButton addBook = new JButton("Add new book");
            addBook.addActionListener(e -> {
                DBManager.BookData data = LibraryUI.addBookDialog((JFrame) SwingUtilities.getRoot(panel), libraryFilter, db);
                if (data!=null) {
                    listModel.addElement(data.name());
                    booksList.setSelectedValue(data.name(), true);
                } else
                    booksList.clearSelection();
            });
            booksPanel.add(addBook);
        }



        // Details panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.PAGE_AXIS));
        JLabel detailsBookName = new JLabel();
        JTextArea detailsSummary = new JTextArea();
        detailsSummary.setEditable(false);
        detailsSummary.setLineWrap(true);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        DefaultTreeModel chaptersModel = new DefaultTreeModel(root);
        JTree chaptersList = new JTree(chaptersModel);
        JScrollPane chaptersScroll = new JScrollPane(chaptersList);
        chaptersList.setRootVisible(false);
        detailsPanel.add(detailsBookName);
        detailsPanel.add(detailsSummary);
        detailsPanel.add(chaptersScroll);
        detailsPanel.setVisible(false);

        if (libraryFilter!=-1) {
            // TODO: Make the button non clickable until we click on a chapter
            JButton startLoan = new JButton("Start loan");
            startLoan.addActionListener(event -> {
                try {
                    CommonUI.showGenericList( (JFrame)SwingUtilities.getRoot(panel), "Select user", db.getUsers(),
                            (i,s) -> {
                                try {
                                    String chapterText = (String)((DefaultMutableTreeNode)chaptersList.getSelectionPath().getLastPathComponent()).getUserObject();
                                    Pattern pattern = Pattern.compile("Chapter (\\d+)"); // TODO: Use a better method, maybe?
                                    Matcher matcher = pattern.matcher(chapterText);
                                    if (matcher.find()) {
                                        // TODO Calculate the dates
                                        db.create_loan(i, libraryFilter, (String) booksList.getSelectedValue(), Integer.parseInt(matcher.group(1)), "2023-07-13", "2023-07-20");
                                    }

                                } catch (Exception e) {
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
                } catch (TransactionException e) {
                    throw new RuntimeException(e);
                }
            });
            detailsPanel.add(startLoan);
        }


        // When we click on a book, we load its details
        booksList.addListSelectionListener(e -> {
            String name = (String)((JList)e.getSource()).getSelectedValue();
            if (name==null) {
                detailsPanel.setVisible(false);
            }
            DBManager.BookData data;
            Map<Integer,Map<Integer,Integer>> availability;
            Map<Integer,String> libraryNames;
            try {
                data = db.getBookData(name);
                availability = db.getBookAvailability(name);
                libraryNames = db.getLibraries();
            } catch (TransactionException ex) {
                throw new RuntimeException(ex);
            }
            detailsBookName.setText(data.name() + " ["+data.genre()+"] - "+data.author());
            detailsSummary.setText(data.summary());
            chaptersModel.setRoot(buildChapterTree(availability,libraryNames));
            for (int i = 0; i < chaptersList.getRowCount(); i++) {
                chaptersList.expandRow(i);
            }
            detailsPanel.setVisible(true);
        });



        panel.add(booksPanel);
        panel.add(detailsPanel);
        return panel;

    }
    private static DefaultMutableTreeNode buildChapterTree(Map<Integer, Map<Integer,Integer>> availability, Map<Integer,String> libraryNames) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        for (int libId : availability.keySet()) {
            DefaultMutableTreeNode libraryNode = new DefaultMutableTreeNode(libraryNames.get(libId));
            root.add(libraryNode);
            for (Map.Entry<Integer,Integer> line: availability.get(libId).entrySet()) {
                libraryNode.add(new DefaultMutableTreeNode("Chapter "+line.getKey()+" - "+line.getValue()+" available"));
            }
        }
        return root;
    }
}
