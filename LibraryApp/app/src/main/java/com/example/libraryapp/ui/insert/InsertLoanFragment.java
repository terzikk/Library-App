package com.example.libraryapp.ui.insert;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.libraryapp.Drawer;
import com.example.libraryapp.R;
import com.example.libraryapp.database.Book;
import com.example.libraryapp.database.Member;
import com.example.libraryapp.databinding.FragmentInsertLoanBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class InsertLoanFragment extends Fragment {

    private FragmentInsertLoanBinding binding;

    EditText duedatetext;
    Spinner bookSpinner, memberSpinner;
    Button insertbutton;
    List<Integer> bookIDs, memberIDs;
    List<Book> books;
    List<Member> members;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference loanCollectionRef = db.collection("Loan");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insert_loan, container, false);

        bookSpinner = view.findViewById(R.id.spinnerOptionsforbook);
        memberSpinner = view.findViewById(R.id.spinnerOptionsformember);
        duedatetext = view.findViewById(R.id.editTextDueDate);
        insertbutton = view.findViewById(R.id.insertButton);

        // Populate spinner with library section names
        populateSpinner();
        duedatetext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDatePickerDialog();
                }
            }
        });

        insertbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertLoan();
            }
        });


        return view;
    }

    private void populateSpinner() {
        // Retrieve all books from the database
        books = Drawer.database.bookDao().getAllBooks();

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

                // Populate the spinner with available book titles
                List<String> bookTitles = new ArrayList<>();
                bookIDs = new ArrayList<>();
                for (Book book : availableBooks) {
                    bookTitles.add(book.getTitle());
                    bookIDs.add(book.getId());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, bookTitles);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                bookSpinner.setAdapter(adapter);
            }
        });


        members = Drawer.database.memberDao().getAllMembers();
        List<String> memberNames = new ArrayList<>();
        memberIDs = new ArrayList<>();
        for (Member member : members) {
            memberNames.add(member.getName());
            memberIDs.add(member.getId());
        }

        ArrayAdapter<String> memberAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, memberNames);
        memberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        memberSpinner.setAdapter(memberAdapter);
    }

    private void showDatePickerDialog() {
        // Get current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create DatePickerDialog and set minimum date
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // Set the selected date to the EditText
                duedatetext.setText(String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, monthOfYear + 1, year));
            }
        }, year, month, dayOfMonth);

        // Set minimum date
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Disable past dates
        datePickerDialog.show();
    }


    private void insertLoan() {
        // Get selected book and member IDs
        int selectedBookId = bookIDs.isEmpty() ? 0 : bookIDs.get(bookSpinner.getSelectedItemPosition());
        int selectedMemberId = memberIDs.get(memberSpinner.getSelectedItemPosition());

        // Get due date from EditText
        String dueDate = duedatetext.getText().toString().trim();

        // Check if there are available books in the spinner
        if (selectedBookId == 0) {
            Toast.makeText(getActivity(), "No available books to select", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the due date is empty
        if (dueDate.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter the due date", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the due date has the correct format (dd/MM/yyyy)
        if (!isValidDateFormat(dueDate)) {
            Toast.makeText(getActivity(), "Please enter the due date in the format dd/MM/yyyy", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if a member is selected
        if (selectedMemberId == 0) {
            Toast.makeText(getActivity(), "Please select a member", Toast.LENGTH_SHORT).show();
            return;
        }

        // If all checks pass, proceed with loan insertion
        Map<String, Object> loanData = new HashMap<>();
        loanData.put("bookId", selectedBookId);
        loanData.put("memberId", selectedMemberId);
        loanData.put("dueDate", dueDate);

        loanCollectionRef
                .document()
                .set(loanData, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getActivity(), "Loan added successfully", Toast.LENGTH_SHORT).show();
                        populateSpinner();
                        duedatetext.setText("");
                        duedatetext.clearFocus();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Failed to add loan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }




    private boolean isValidDateFormat(String date) {
        try {
            // Parse the date using the specified format (dd/MM/yyyy)
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sdf.setLenient(false);
            sdf.parse(date);
            return true; // If parsing is successful, return true
        } catch (ParseException e) {
            return false; // If parsing fails, return false
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

