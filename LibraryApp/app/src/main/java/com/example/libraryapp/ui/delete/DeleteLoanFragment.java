package com.example.libraryapp.ui.delete;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

import java.util.ArrayList;
import java.util.List;

public class DeleteLoanFragment extends Fragment {

    Spinner loanSpinner;
    Button deleteButton;
    List<String> loanTexts;
    List<String> loanIDs;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference loanCollectionRef = db.collection("Loan");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delete_loan, container, false);
        loanSpinner = view.findViewById(R.id.spinnerOptionsforloan);
        deleteButton = view.findViewById(R.id.deleteButton);

        // Populate spinner
        populateSpinner();

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loanIDs != null && !loanIDs.isEmpty()) {
                    deleteLoan(loanIDs.get(loanSpinner.getSelectedItemPosition()));
                } else {
                    Toast.makeText(getActivity(), "No loans available to delete!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return view;
    }

    private void populateSpinner() {
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

    private void deleteLoan(String loanId) {
        loanCollectionRef.document(loanId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Handle successful deletion
                        Toast.makeText(getActivity(), "Loan deleted successfully", Toast.LENGTH_SHORT).show();
                        // Refresh the spinner
                        populateSpinner();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Toast.makeText(getActivity(), "Failed to delete loan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
