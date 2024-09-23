package com.example.libraryapp.ui.update;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UpdateLoanFragment extends Fragment {

    EditText duedatetext;
    Spinner loanSpinner;
    Button updatebutton;
    List<String> loanTexts;
    List<String> loanIDs;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference loanCollectionRef = db.collection("Loan");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_update_loan, container, false);
        loanSpinner = view.findViewById(R.id.spinnerOptionsforloan);
        duedatetext = view.findViewById(R.id.editTextDueDate);
        updatebutton = view.findViewById(R.id.updateButton);

        // Populate spinners
        populateSpinners();

        duedatetext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDatePickerDialog();
                }
            }
        });
        updatebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loanIDs != null && !loanIDs.isEmpty()) {
                    updateLoan(loanIDs.get(loanSpinner.getSelectedItemPosition()));
                } else {
                    Toast.makeText(getActivity(), "No loans available", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void populateSpinners() {
        loanTexts = new ArrayList<>();
        loanIDs = new ArrayList<>();

        loanCollectionRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Integer bookId = documentSnapshot.getLong("bookId").intValue();
                    Integer memberId = documentSnapshot.getLong("memberId").intValue();

                    // Retrieve book title and member name from local database
                    String bookTitle = Drawer.database.bookDao().getBookTitle(bookId);
                    String memberName = Drawer.database.memberDao().getMemberName(memberId);


                    String loanText = bookTitle + " for " + memberName;
                    String loanId = documentSnapshot.getId();

                    loanTexts.add(loanText);
                    loanIDs.add(loanId);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, loanTexts);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                loanSpinner.setAdapter(adapter);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Failed to load loans: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLoan(String loanId) {
        String dueDate = duedatetext.getText().toString().trim();

        // Check if the due date is empty
        if (dueDate.isEmpty()) {
            Toast.makeText(getActivity(), "Please enter the due date", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a Loan object
        Map<String, Object> loanData = new HashMap<>();
        loanData.put("dueDate", dueDate);

        // Update the loan data in Firestore
        loanCollectionRef.document(loanId) // Use the loanId to update the existing loan
                .set(loanData, SetOptions.merge()) // Use merge() to merge new data with existing data
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Handle successful update
                        Toast.makeText(getActivity(), "Loan updated successfully", Toast.LENGTH_SHORT).show();
                        // Refresh the loan spinner
                        populateSpinners();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Toast.makeText(getActivity(), "Failed to update loan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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

        duedatetext.setText("");
        duedatetext.clearFocus();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
