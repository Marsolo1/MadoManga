package org.madomanga;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
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

        public LoanTableModel(List<DBManager.LoanData> data) {
            this(data, new LoanColumns[0]);
        }

        public LoanTableModel(List<DBManager.LoanData> data, LoanColumns[] hiddenColumns) {
            this.data=data;
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
                    return rowData.user_id();
                }
                case LIBRARY -> {
                    return rowData.library_id();
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

    public static JComponent loansList(List<DBManager.LoanData> list, String header) {
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        JTable table = new JTable(new LoanTableModel(list));
        table.setFillsViewportHeight(true);
        JLabel label = new JLabel(header);
        label.setLabelFor(table);
        listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        return new JScrollPane(table);
    }
}
