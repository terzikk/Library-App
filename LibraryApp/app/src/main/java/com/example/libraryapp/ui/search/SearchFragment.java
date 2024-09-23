package com.example.libraryapp.ui.search;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.libraryapp.Drawer;
import com.example.libraryapp.R;
import com.example.libraryapp.database.Book;
import com.example.libraryapp.database.LibrarySection;
import com.example.libraryapp.database.Member;
import com.example.libraryapp.databinding.FragmentSearchBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private Spinner querySpinner;
    private TableLayout queryResultTable;
    List<Book> books;
    List<Member> members;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference loanCollectionRef = db.collection("Loan");

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        querySpinner = root.findViewById(R.id.spinnerOptions);
        queryResultTable = root.findViewById(R.id.queryResultTable);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.SpinnerOptions, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        querySpinner.setAdapter(spinnerAdapter);

        root.findViewById(R.id.searchbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedQuery = querySpinner.getSelectedItem().toString();
                executeQuery(selectedQuery);
            }
        });

        return root;
    }

    private void executeQuery(String query) {
        switch (query) {
            case "Books per Library Section":
                List<LibrarySection> sections = Drawer.database.sectionDao().getAllSectionsWithBookCount();
                displayCountResultsperSection(sections);
                break;
            case "All Members":
                List<Member> members = Drawer.database.memberDao().getAllMembers();
                displayMemberResultsInTable(members);
                break;
            case "Books above 300 Pages":
                List<Book> books = Drawer.database.bookDao().getBooksAbove300Pages();
                displayBooksAbove300Pages(books);
                break;
            case "Overdue Loans":
                displayOverdueLoans();
                break;
            case "Available Books":
                displayAvailableBooks();
                break;
            case "Days Remaining per Loaned Book":
                displayDaysRemainingperLoanedBook();
                break;
        }
    }

    private void displayDaysRemainingperLoanedBook() {
        queryResultTable.removeAllViews();

        // Add header row
        TableRow headerRow = new TableRow(requireContext());
        headerRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView bookTitleHeader = new TextView(requireContext());
        bookTitleHeader.setText("Book's\nTitle");
        bookTitleHeader.setPadding(5, 5, 5, 5);
        bookTitleHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
        bookTitleHeader.setBackgroundResource(R.drawable.table_row_bg);
        bookTitleHeader.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
        bookTitleHeader.setTypeface(null, Typeface.BOLD); // Make text bold
        bookTitleHeader.setLines(2);
        headerRow.addView(bookTitleHeader);

        TextView memberNameHeader = new TextView(requireContext());
        memberNameHeader.setText("Loaner's\nName");
        memberNameHeader.setPadding(5, 5, 5, 5);
        memberNameHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
        memberNameHeader.setBackgroundResource(R.drawable.table_row_bg);
        memberNameHeader.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
        memberNameHeader.setTypeface(null, Typeface.BOLD); // Make text bold
        memberNameHeader.setLines(2);
        headerRow.addView(memberNameHeader);

        TextView daysRemainingHeader = new TextView(requireContext());
        daysRemainingHeader.setText("Days\nRemaining");
        daysRemainingHeader.setPadding(5, 5, 5, 5);
        daysRemainingHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
        daysRemainingHeader.setBackgroundResource(R.drawable.table_row_bg);
        daysRemainingHeader.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
        daysRemainingHeader.setTypeface(null, Typeface.BOLD); // Make text bold
        daysRemainingHeader.setLines(2);
        headerRow.addView(daysRemainingHeader);

        queryResultTable.addView(headerRow);

        // Retrieve the loaned books and their due dates
        loanCollectionRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                // Get the bookId and dueDate fields from each loan document
                Integer bookId = documentSnapshot.getLong("bookId").intValue();
                Integer memberId = documentSnapshot.getLong("memberId").intValue();
                String dueDateString = documentSnapshot.getString("dueDate");

                Book book = Drawer.database.bookDao().getBookById(bookId);
                String name = Drawer.database.memberDao().getMemberName(memberId);


                // Parse due date string to Date object
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date dueDate;
                try {
                    dueDate = dateFormat.parse(dueDateString);
                } catch (ParseException e) {
                    Log.e("TAG", "Error parsing due date: " + e.getMessage());
                    return;
                }

                // Calculate days remaining until due date
                long currentTimeMillis = System.currentTimeMillis();
                long dueDateTimeMillis = dueDate.getTime();
                long timeDiffMillis = dueDateTimeMillis - currentTimeMillis;
                int daysRemaining = (int) TimeUnit.MILLISECONDS.toDays(timeDiffMillis);

                // Update daysRemaining text if negative
                String daysRemainingText;
                if (daysRemaining < 0) {
                    daysRemainingText = "Expired";
                } else {
                    daysRemainingText = String.valueOf(daysRemaining);
                }

                if (book != null) {
                    String title = book.getTitle();

                    // Add row for each loaned book
                    TableRow row = new TableRow(requireContext());
                    row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                    TextView bookTitleTextView = new TextView(requireContext());
                    bookTitleTextView.setText(title);
                    bookTitleTextView.setPadding(5, 5, 5, 5);
                    bookTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
                    bookTitleTextView.setBackgroundResource(R.drawable.table_row_bg);
                    bookTitleTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
                    row.addView(bookTitleTextView);

                    TextView memberNameTextView = new TextView(requireContext());
                    memberNameTextView.setText(name);
                    memberNameTextView.setPadding(5, 5, 5, 5);
                    memberNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
                    memberNameTextView.setBackgroundResource(R.drawable.table_row_bg);
                    memberNameTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
                    row.addView(memberNameTextView);

                    TextView daysRemainingTextView = new TextView(requireContext());
                    daysRemainingTextView.setText(daysRemainingText);
                    daysRemainingTextView.setPadding(5, 5, 5, 5);
                    daysRemainingTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
                    daysRemainingTextView.setBackgroundResource(R.drawable.table_row_bg);
                    daysRemainingTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
                    row.addView(daysRemainingTextView);

                    // Add row to the table
                    queryResultTable.addView(row);
                } else {
                    Log.e("TAG", "Book object is null");
                }
            }
        }).addOnFailureListener(e -> {
            // Handle failure
            Toast.makeText(requireContext(), "Failed to retrieve loaned books: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    private void displayOverdueLoans() {
        queryResultTable.removeAllViews();

        // Add header row
        TableRow headerRow = new TableRow(requireContext());
        headerRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView bookTitleHeader = new TextView(requireContext());
        bookTitleHeader.setText("Book's Title");
        bookTitleHeader.setPadding(5, 5, 5, 5);
        bookTitleHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
        bookTitleHeader.setBackgroundResource(R.drawable.table_row_bg);
        bookTitleHeader.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
        bookTitleHeader.setTypeface(null, Typeface.BOLD); // Make text bold
        headerRow.addView(bookTitleHeader);

        TextView memberNameHeader = new TextView(requireContext());
        memberNameHeader.setText("Loaner's Name");
        memberNameHeader.setPadding(5, 5, 5, 5);
        memberNameHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
        memberNameHeader.setBackgroundResource(R.drawable.table_row_bg);
        memberNameHeader.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
        memberNameHeader.setTypeface(null, Typeface.BOLD); // Make text bold
        headerRow.addView(memberNameHeader);

        TextView dueDateHeader = new TextView(requireContext());
        dueDateHeader.setText("Due Date");
        dueDateHeader.setPadding(5, 5, 5, 5);
        dueDateHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
        dueDateHeader.setBackgroundResource(R.drawable.table_row_bg);
        dueDateHeader.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
        dueDateHeader.setTypeface(null, Typeface.BOLD); // Make text bold
        headerRow.addView(dueDateHeader);

        queryResultTable.addView(headerRow);

        // Retrieve the IDs of books that are overdue
        loanCollectionRef.whereLessThan("dueDate", getCurrentDate()).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                // Get the bookId and dueDate fields from each loan document
                Integer bookId = documentSnapshot.getLong("bookId").intValue();
                Integer memberId = documentSnapshot.getLong("memberId").intValue();
                String dueDate = documentSnapshot.getString("dueDate");

                Book book = Drawer.database.bookDao().getBookById(bookId);
                String name = Drawer.database.memberDao().getMemberName(memberId);

                if (book != null) {
                    String title = book.getTitle();

                    TableRow row = new TableRow(requireContext());
                    row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                    TextView bookTitleTextView = new TextView(requireContext());
                    bookTitleTextView.setText(title);
                    bookTitleTextView.setPadding(5, 5, 5, 5);
                    bookTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
                    bookTitleTextView.setBackgroundResource(R.drawable.table_row_bg);
                    bookTitleTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
                    row.addView(bookTitleTextView);

                    TextView memberNameTextView = new TextView(requireContext());
                    memberNameTextView.setText(name);
                    memberNameTextView.setPadding(5, 5, 5, 5);
                    memberNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
                    memberNameTextView.setBackgroundResource(R.drawable.table_row_bg);
                    memberNameTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
                    row.addView(memberNameTextView);

                    TextView dueDateTextView = new TextView(requireContext());
                    dueDateTextView.setText(dueDate);
                    dueDateTextView.setPadding(5, 5, 5, 5);
                    dueDateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
                    dueDateTextView.setBackgroundResource(R.drawable.table_row_bg);
                    dueDateTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
                    row.addView(dueDateTextView);

                    // Add divider line
                    View divider = new View(requireContext());
                    divider.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
                    divider.setBackgroundColor(Color.BLACK);
                    queryResultTable.addView(divider);

                    queryResultTable.addView(row);
                } else {
                    Log.e("TAG", "Book object is null");
                }
            }

        }).addOnFailureListener(e -> {
            // Handle failure
            Toast.makeText(requireContext(), "Failed to retrieve overdue loans: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    // Helper method to get current date in the format expected by Firestore
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(new Date());
    }


    private void displayAvailableBooks() {
        books = Drawer.database.bookDao().getAllBooks();
        members = Drawer.database.memberDao().getAllMembers();

        // Retrieve the IDs of all loaned books
        List<Integer> loanedBookIds = new ArrayList<>();
        loanCollectionRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    // Get the bookId field from each loan document
                    Integer bookId = documentSnapshot.getLong("bookId").intValue();
                    loanedBookIds.add(bookId);
                }

                // Remove loaned books from the list of all books
                List<Book> availableBooks = new ArrayList<>();
                for (Book book : books) {
                    if (!loanedBookIds.contains(book.getId())) {
                        availableBooks.add(book);
                    }
                }

                queryResultTable.removeAllViews();

                // Add header row
                TableRow headerRow = new TableRow(requireContext());
                headerRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));


                TextView bookTitleHeader = new TextView(requireContext());
                bookTitleHeader.setText("Title");
                bookTitleHeader.setPadding(5, 5, 5, 5);
                bookTitleHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
                bookTitleHeader.setBackgroundResource(R.drawable.table_row_bg);
                bookTitleHeader.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
                bookTitleHeader.setTypeface(null, Typeface.BOLD); // Make text bold
                headerRow.addView(bookTitleHeader);

                TextView bookAuthorHeader = new TextView(requireContext());
                bookAuthorHeader.setText("Author");
                bookAuthorHeader.setPadding(5, 5, 5, 5);
                bookAuthorHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
                bookAuthorHeader.setBackgroundResource(R.drawable.table_row_bg);
                bookAuthorHeader.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
                bookAuthorHeader.setTypeface(null, Typeface.BOLD); // Make text bold
                headerRow.addView(bookAuthorHeader);

                TextView bookPagesHeader = new TextView(requireContext());
                bookPagesHeader.setText("Pages");
                bookPagesHeader.setPadding(5, 5, 5, 5);
                bookPagesHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
                bookPagesHeader.setBackgroundResource(R.drawable.table_row_bg);
                bookPagesHeader.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
                bookPagesHeader.setTypeface(null, Typeface.BOLD); // Make text bold
                headerRow.addView(bookPagesHeader);

                queryResultTable.addView(headerRow);

                // Add data rows for available books
                for (Book book : availableBooks) {
                    TableRow row = new TableRow(requireContext());
                    row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));


                    TextView bookTitleTextView = new TextView(requireContext());
                    bookTitleTextView.setText(book.getTitle());
                    bookTitleTextView.setPadding(5, 5, 5, 5);
                    bookTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
                    bookTitleTextView.setBackgroundResource(R.drawable.table_row_bg);
                    bookTitleTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
                    row.addView(bookTitleTextView);

                    TextView bookAuthorTextView = new TextView(requireContext());
                    bookAuthorTextView.setText(book.getAuthor());
                    bookAuthorTextView.setPadding(5, 5, 5, 5);
                    bookAuthorTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
                    bookAuthorTextView.setBackgroundResource(R.drawable.table_row_bg);
                    bookAuthorTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
                    row.addView(bookAuthorTextView);

                    TextView bookPagesTextView = new TextView(requireContext());
                    bookPagesTextView.setText(String.valueOf(book.getPages()));
                    bookPagesTextView.setPadding(5, 5, 5, 5);
                    bookPagesTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
                    bookPagesTextView.setBackgroundResource(R.drawable.table_row_bg);
                    bookPagesTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
                    row.addView(bookPagesTextView);

                    // Add divider line
                    View divider = new View(requireContext());
                    divider.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
                    divider.setBackgroundColor(Color.BLACK);
                    queryResultTable.addView(divider);

                    queryResultTable.addView(row);
                }
            }
        });
    }

    private void displayCountResultsperSection(List<LibrarySection> sections) {
        queryResultTable.removeAllViews();

        // Add header row
        TableRow headerRow = new TableRow(requireContext());
        headerRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView sectionNameHeader = new TextView(requireContext());
        sectionNameHeader.setText("Library Section");
        sectionNameHeader.setPadding(5, 5, 5, 5);
        sectionNameHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
        sectionNameHeader.setBackgroundResource(R.drawable.table_row_bg);
        sectionNameHeader.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
        sectionNameHeader.setTypeface(null, Typeface.BOLD); // Make text bold
        headerRow.addView(sectionNameHeader);

        TextView bookCountHeader = new TextView(requireContext());
        bookCountHeader.setText("Number of Books");
        bookCountHeader.setPadding(5, 5, 5, 5);
        bookCountHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
        bookCountHeader.setBackgroundResource(R.drawable.table_row_bg);
        bookCountHeader.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
        bookCountHeader.setTypeface(null, Typeface.BOLD); // Make text bold
        headerRow.addView(bookCountHeader);

        queryResultTable.addView(headerRow);

        // Add data rows
        for (LibrarySection section : sections) {
            TableRow row = new TableRow(requireContext());
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            TextView sectionNameTextView = new TextView(requireContext());
            sectionNameTextView.setText(section.getName());
            sectionNameTextView.setPadding(5, 5, 5, 5);
            sectionNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
            sectionNameTextView.setBackgroundResource(R.drawable.table_row_bg);
            sectionNameTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
            row.addView(sectionNameTextView);

            TextView bookCountTextView = new TextView(requireContext());
            bookCountTextView.setText(String.valueOf(section.getBookCount()));
            bookCountTextView.setPadding(5, 5, 5, 5);
            bookCountTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
            bookCountTextView.setBackgroundResource(R.drawable.table_row_bg);
            bookCountTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
            row.addView(bookCountTextView);

            // Add divider line
            View divider = new View(requireContext());
            divider.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(Color.BLACK);
            queryResultTable.addView(divider);

            queryResultTable.addView(row);
        }
    }

    private void displayMemberResultsInTable(List<Member> members) {
        queryResultTable.removeAllViews();

        // Add header row
        TableRow headerRow = new TableRow(requireContext());
        headerRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView memberNameHeader = new TextView(requireContext());
        memberNameHeader.setText("Name");
        memberNameHeader.setPadding(5, 5, 5, 5);
        memberNameHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
        memberNameHeader.setBackgroundResource(R.drawable.table_row_bg);
        memberNameHeader.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
        memberNameHeader.setTypeface(null, Typeface.BOLD); // Make text bold
        headerRow.addView(memberNameHeader);

        TextView memberEmailHeader = new TextView(requireContext());
        memberEmailHeader.setText("Email");
        memberEmailHeader.setPadding(5, 5, 5, 5);
        memberEmailHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
        memberEmailHeader.setBackgroundResource(R.drawable.table_row_bg);
        memberEmailHeader.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
        memberEmailHeader.setTypeface(null, Typeface.BOLD); // Make text bold
        headerRow.addView(memberEmailHeader);

        queryResultTable.addView(headerRow);

        // Add data rows
        for (Member member : members) {
            TableRow row = new TableRow(requireContext());
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            TextView memberNameTextView = new TextView(requireContext());
            memberNameTextView.setText(member.getName());
            memberNameTextView.setPadding(5, 5, 5, 5);
            memberNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
            memberNameTextView.setBackgroundResource(R.drawable.table_row_bg);
            memberNameTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
            row.addView(memberNameTextView);

            TextView memberEmailTextView = new TextView(requireContext());
            memberEmailTextView.setText(member.getEmail());
            memberEmailTextView.setPadding(5, 5, 5, 5);
            memberEmailTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
            memberEmailTextView.setBackgroundResource(R.drawable.table_row_bg);
            memberEmailTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
            row.addView(memberEmailTextView);

            // Add divider line
            View divider = new View(requireContext());
            divider.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(Color.BLACK);
            queryResultTable.addView(divider);

            queryResultTable.addView(row);
        }
    }

    private void displayBooksAbove300Pages(List<Book> books) {
        queryResultTable.removeAllViews();

        // Add header row
        TableRow headerRow = new TableRow(requireContext());
        headerRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView bookTitleHeader = new TextView(requireContext());
        bookTitleHeader.setText("Title");
        bookTitleHeader.setPadding(5, 5, 5, 5);
        bookTitleHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
        bookTitleHeader.setBackgroundResource(R.drawable.table_row_bg);
        bookTitleHeader.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
        bookTitleHeader.setTypeface(null, Typeface.BOLD); // Make text bold
        headerRow.addView(bookTitleHeader);

        TextView bookAuthorHeader = new TextView(requireContext());
        bookAuthorHeader.setText("Author");
        bookAuthorHeader.setPadding(5, 5, 5, 5);
        bookAuthorHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
        bookAuthorHeader.setBackgroundResource(R.drawable.table_row_bg);
        bookAuthorHeader.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
        bookAuthorHeader.setTypeface(null, Typeface.BOLD); // Make text bold
        headerRow.addView(bookAuthorHeader);

        TextView bookPagesHeader = new TextView(requireContext());
        bookPagesHeader.setText("Pages");
        bookPagesHeader.setPadding(5, 5, 5, 5);
        bookPagesHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
        bookPagesHeader.setBackgroundResource(R.drawable.table_row_bg);
        bookPagesHeader.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
        bookPagesHeader.setTypeface(null, Typeface.BOLD); // Make text bold
        headerRow.addView(bookPagesHeader);

        queryResultTable.addView(headerRow);

        // Add data rows
        for (Book book : books) {
            TableRow row = new TableRow(requireContext());
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            TextView bookTitleTextView = new TextView(requireContext());
            bookTitleTextView.setText(book.getTitle());
            bookTitleTextView.setPadding(5, 5, 5, 5);
            bookTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
            bookTitleTextView.setBackgroundResource(R.drawable.table_row_bg);
            bookTitleTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
            row.addView(bookTitleTextView);

            TextView bookAuthorTextView = new TextView(requireContext());
            bookAuthorTextView.setText(book.getAuthor());
            bookAuthorTextView.setPadding(5, 5, 5, 5);
            bookAuthorTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
            bookAuthorTextView.setBackgroundResource(R.drawable.table_row_bg);
            bookAuthorTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
            row.addView(bookAuthorTextView);

            TextView bookPagesTextView = new TextView(requireContext());
            bookPagesTextView.setText(String.valueOf(book.getPages()));
            bookPagesTextView.setPadding(5, 5, 5, 5);
            bookPagesTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18); // Set text size
            bookPagesTextView.setBackgroundResource(R.drawable.table_row_bg);
            bookPagesTextView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)); // Adjust width to take full space
            row.addView(bookPagesTextView);

            // Add divider line
            View divider = new View(requireContext());
            divider.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(Color.BLACK);
            queryResultTable.addView(divider);

            queryResultTable.addView(row);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        FloatingActionButton fab = requireActivity().findViewById(R.id.mailbutton);
        if (fab != null) {
            fab.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        FloatingActionButton fab = requireActivity().findViewById(R.id.mailbutton);
        if (fab != null) {
            fab.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
