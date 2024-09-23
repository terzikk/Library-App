package com.example.libraryapp.ui.insert;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.libraryapp.Drawer;
import com.example.libraryapp.R;
import com.example.libraryapp.database.Member;
import com.example.libraryapp.databinding.FragmentInsertMemberBinding;

public class InsertMemberFragment extends Fragment {

    private FragmentInsertMemberBinding binding;

    EditText nametext, emailtext;
    Button insertbutton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insert_member, container, false);
        nametext = view.findViewById(R.id.editTextName);
        emailtext = view.findViewById(R.id.editTextEmail);
        insertbutton = view.findViewById(R.id.insertButton);
        insertbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertMember();
            }
        });
        return view;
    }

    private void insertMember() {
        String name = nametext.getText().toString().trim();
        String email = emailtext.getText().toString().trim();

        // Check if any of the fields are empty
        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            Member member = new Member();
            member.setName(name);
            member.setEmail(email);
            Drawer.database.memberDao().insertMember(member);
            Toast.makeText(getActivity(), "Member added!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            String message = e.getMessage();
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        }

        // Clear input fields
        nametext.setText("");
        emailtext.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
