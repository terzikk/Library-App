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
import com.example.libraryapp.database.LibrarySection;
import com.example.libraryapp.databinding.FragmentUpdateSectionBinding;

import java.util.ArrayList;
import java.util.List;


public class UpdateSectionFragment extends Fragment {

    private FragmentUpdateSectionBinding binding;
    EditText nametext, locationtext;
    Button updatebutton;
    List<Integer> sectionIDs;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout using the binding
        binding = FragmentUpdateSectionBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize the spinner
        initSpinner();

        nametext = view.findViewById(R.id.editTextName);
        locationtext = view.findViewById(R.id.editTextLocation);
        updatebutton = view.findViewById(R.id.updateButton);
        updatebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nametext.getText().toString().isEmpty() || locationtext.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_LONG).show();
                } else {
                    String name = nametext.getText().toString();
                    String location = locationtext.getText().toString();

                    try {

                        int selectedPosition = binding.spinnerOptions.getSelectedItemPosition();
                        int selectedSectionId = sectionIDs.get(selectedPosition);

                        LibrarySection updatedSection = new LibrarySection();
                        updatedSection.setId(selectedSectionId);
                        updatedSection.setName(name);
                        updatedSection.setLocation(location);

                        // Update the book in the database
                        Drawer.database.sectionDao().updateSection(updatedSection);

                        initSpinner();

                        Toast.makeText(getActivity(), "Section updated!", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        String message = e.getMessage();
                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                    }
                }
                nametext.setText("");
                locationtext.setText("");
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
            List<LibrarySection> sections = Drawer.database.sectionDao().getAllSections();

            // Extract the titles from the list of books
            List<String> sectionNames = new ArrayList<>();
            sectionIDs = new ArrayList<>();
            for (LibrarySection section : sections) {
                sectionNames.add(section.getName());
                sectionIDs.add(section.getId());
            }

            // Create an adapter and set it to the spinner
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, sectionNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerOptions.setAdapter(adapter);


        }
    }

}
