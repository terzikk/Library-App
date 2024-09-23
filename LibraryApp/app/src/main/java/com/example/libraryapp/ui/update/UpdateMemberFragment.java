package com.example.libraryapp.ui.update;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.libraryapp.Drawer;
import com.example.libraryapp.R;
import com.example.libraryapp.database.Member;
import com.example.libraryapp.databinding.FragmentUpdateMemberBinding;

import java.util.ArrayList;
import java.util.List;


public class UpdateMemberFragment extends Fragment {

    private FragmentUpdateMemberBinding binding;
    EditText nametext, emailtext;
    Button updatebutton;
    List<Integer> memberIDs;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout using the binding
        binding = FragmentUpdateMemberBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize the spinner
        initSpinner();

        nametext = view.findViewById(R.id.editTextName);
        emailtext = view.findViewById(R.id.editTextEmail);
        updatebutton = view.findViewById(R.id.updateButton);
        updatebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMember();
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
            List<String> memberNames = new ArrayList<>();
            memberIDs = new ArrayList<>();
            for (Member member : members) {
                memberNames.add(member.getName());
                memberIDs.add(member.getId());
            }

            // Create an adapter and set it to the spinner
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, memberNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerOptions.setAdapter(adapter);
        }
    }

    private void updateMember() {
        String name = nametext.getText().toString().trim();
        String email = emailtext.getText().toString().trim();

        // Check if any of the fields are empty
        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            int selectedPosition = binding.spinnerOptions.getSelectedItemPosition();
            int selectedMemberId = memberIDs.get(selectedPosition);

            Member updatedMember = new Member();
            updatedMember.setId(selectedMemberId);
            updatedMember.setName(name);
            updatedMember.setEmail(email);

            // Update the member in the database
            Drawer.database.memberDao().updateMember(updatedMember);

            initSpinner();

            Toast.makeText(getActivity(), "Member updated!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            String message = e.getMessage();
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        }

        // Clear input fields
        nametext.setText("");
        emailtext.setText("");
    }
}
