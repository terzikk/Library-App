package com.example.libraryapp.ui.insert;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.libraryapp.Drawer;
import com.example.libraryapp.R;
import com.example.libraryapp.database.Book;
import com.example.libraryapp.database.LibrarySection;
import com.example.libraryapp.databinding.FragmentInsertBookBinding;

import java.util.ArrayList;
import java.util.List;

public class InsertBookFragment extends Fragment {

    private FragmentInsertBookBinding binding;

    EditText titletext, authortext, pagestext;
    Spinner sectionSpinner;
    Button insertbutton;
    List<Integer> sectionIDs;
    List<LibrarySection> sections;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insert_book, container, false);

        titletext = view.findViewById(R.id.editTextTitle);
        authortext = view.findViewById(R.id.editTextAuthor);
        pagestext = view.findViewById(R.id.editTextPages);
        sectionSpinner = view.findViewById(R.id.spinnerOptions);
        insertbutton = view.findViewById(R.id.insertButton);

        // Populate spinner with library section names
        populateSpinner();

        // Set listener for insert button click
        insertbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertBook();
            }
        });

        return view;
    }

    private void populateSpinner() {
        sections = Drawer.database.sectionDao().getAllSections();
        List<String> sectionNames = new ArrayList<>();
        sectionIDs = new ArrayList<>();
        for (LibrarySection section : sections) {
            sectionNames.add(section.getName());
            sectionIDs.add(section.getId());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, sectionNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sectionSpinner.setAdapter(adapter);
    }

    private void insertBook() {
        String title = titletext.getText().toString().trim();
        String author = authortext.getText().toString().trim();
        String pagesString = pagestext.getText().toString().trim();

        // Check if any of the fields are empty
        if (title.isEmpty() || author.isEmpty() || pagesString.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_LONG).show();
            return;
        }

        // Parse pages to integer
        int pages;
        try {
            pages = Integer.parseInt(pagesString);
        } catch (NumberFormatException ex) {
            Toast.makeText(getActivity(), "Invalid number of pages", Toast.LENGTH_LONG).show();
            return;
        }

        // Check if a section is selected
        String selectedSectionName = (String) sectionSpinner.getSelectedItem();
        if (selectedSectionName == null) {
            Toast.makeText(getActivity(), "Please select a section", Toast.LENGTH_LONG).show();
            return;
        }

        // Find the selected section
        LibrarySection selectedSection = null;
        for (LibrarySection section : sections) {
            if (section.getName().equals(selectedSectionName)) {
                selectedSection = section;
                break;
            }
        }

        // Check if the selected section is valid
        if (selectedSection == null) {
            Toast.makeText(getActivity(), "Invalid section", Toast.LENGTH_LONG).show();
            return;
        }

        // Create and insert the book
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setPages(pages);
        book.setSectionId(selectedSection.getId());

        try {
            Drawer.database.bookDao().insertBook(book);
            Toast.makeText(getActivity(), "Book added!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            String message = e.getMessage();
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        }

        // Clear input fields
        titletext.setText("");
        authortext.setText("");
        pagestext.setText("");
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
