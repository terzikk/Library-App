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
import com.example.libraryapp.database.LibrarySection;
import com.example.libraryapp.databinding.FragmentInsertMemberBinding;

public class InsertSectionFragment extends Fragment {

    private FragmentInsertMemberBinding binding;

    EditText nametext, locationtext;
    Button insertbutton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insert_section, container, false);
        nametext = view.findViewById(R.id.editTextName);
        locationtext = view.findViewById(R.id.editTextLocation);
        insertbutton = view.findViewById(R.id.insertButton);
        insertbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertSection();
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void insertSection() {
        String name = nametext.getText().toString().trim();
        String location = locationtext.getText().toString().trim();

        // Check if any of the fields are empty
        if (name.isEmpty() || location.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            LibrarySection section = new LibrarySection();
            section.setName(name);
            section.setLocation(location);
            Drawer.database.sectionDao().insertSection(section);
            Toast.makeText(getActivity(), "Section added!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            String message = e.getMessage();
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        }

        // Clear input fields
        nametext.setText("");
        locationtext.setText("");
    }
}
