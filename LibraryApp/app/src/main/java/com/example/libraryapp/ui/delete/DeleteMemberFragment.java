package com.example.libraryapp.ui.delete;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.libraryapp.Drawer;
import com.example.libraryapp.R;
import com.example.libraryapp.database.Member;
import com.example.libraryapp.databinding.FragmentDeleteMemberBinding;

import java.util.ArrayList;
import java.util.List;

public class DeleteMemberFragment extends Fragment {

    private FragmentDeleteMemberBinding binding;
    Button deletebutton;
    List<Integer> memberIDs;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout using the binding
        binding = FragmentDeleteMemberBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize the spinner
        initSpinner();

        deletebutton = view.findViewById(R.id.deleteButton);
        deletebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Get the selected member ID from the spinner
                    int selectedPosition = binding.spinnerOptions.getSelectedItemPosition();

                    // Check if there are members available
                    if (memberIDs.isEmpty()) {
                        Toast.makeText(getActivity(), "No members available to delete!", Toast.LENGTH_LONG).show();
                        return; // Exit the method
                    }

                    int selectedMemberId = memberIDs.get(selectedPosition);

                    // Create a new Member object with the updated details
                    Member deletedMember = new Member();
                    deletedMember.setId(selectedMemberId);

                    // Update the member in the database
                    Drawer.database.memberDao().deleteMember(deletedMember);

                    initSpinner();

                    Toast.makeText(getActivity(), "Member deleted!", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    String message = e.getMessage();
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initSpinner() {
        if (binding != null) {
            // Get the list of books from the database
            List<Member> members = Drawer.database.memberDao().getAllMembers();

            // Extract the titles from the list of books
            List<String> membersTitles = new ArrayList<>();
            memberIDs = new ArrayList<>();
            for (Member member : members) {
                membersTitles.add(member.getName());
                memberIDs.add(member.getId());
            }

            // Create an adapter and set it to the spinner
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, membersTitles);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerOptions.setAdapter(adapter);


        }
    }

}