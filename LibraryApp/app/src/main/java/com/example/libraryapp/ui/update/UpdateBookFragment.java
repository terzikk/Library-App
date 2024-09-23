package com.example.libraryapp.ui.update;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.libraryapp.Drawer;
import com.example.libraryapp.R;
import com.example.libraryapp.database.Book;
import com.example.libraryapp.database.LibrarySection;
import com.example.libraryapp.databinding.FragmentUpdateBookBinding;

import java.util.ArrayList;
import java.util.List;

public class UpdateBookFragment extends Fragment {

    private FragmentUpdateBookBinding binding;
    EditText titletext, authortext, pagestext;
    Spinner bookSpinner, sectionSpinner;
    Button updatebutton;
    List<Integer> bookIDs;
    List<Integer> sectionIDs;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout using the binding
        binding = FragmentUpdateBookBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize the spinners
        initSpinners();

        titletext = view.findViewById(R.id.editTextTitle);
        authortext = view.findViewById(R.id.editTextAuthor);
        pagestext = view.findViewById(R.id.editTextPages);
        updatebutton = view.findViewById(R.id.updateButton);

        updatebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBook();
            }
        });


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initSpinners() {
        // Initialize book spinner
        bookSpinner = binding.spinnerOptions;
        List<Book> books = Drawer.database.bookDao().getAllBooks();
        List<String> bookTitles = new ArrayList<>();
        bookIDs = new ArrayList<>();
        for (Book book : books) {
            bookTitles.add(book.getTitle());
            bookIDs.add(book.getId());
        }
        ArrayAdapter<String> bookAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, bookTitles);
        bookAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bookSpinner.setAdapter(bookAdapter);

        // Initialize section spinner
        sectionSpinner = binding.spinnerOptions1;
        List<LibrarySection> sections = Drawer.database.sectionDao().getAllSections();
        List<String> sectionNames = new ArrayList<>();
        sectionIDs = new ArrayList<>();
        for (LibrarySection section : sections) {
            sectionNames.add(section.getName());
            sectionIDs.add(section.getId());
        }
        ArrayAdapter<String> sectionAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, sectionNames);
        sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sectionSpinner.setAdapter(sectionAdapter);
    }

    private void updateBook() {
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

        // Get selected book ID and section ID
        int selectedBookPosition = bookSpinner.getSelectedItemPosition();
        if (selectedBookPosition == AdapterView.INVALID_POSITION) {
            Toast.makeText(getActivity(), "Please select a book", Toast.LENGTH_LONG).show();
            return;
        }
        int selectedBookId = bookIDs.get(selectedBookPosition);

        int selectedSectionPosition = sectionSpinner.getSelectedItemPosition();
        if (selectedSectionPosition == AdapterView.INVALID_POSITION) {
            Toast.makeText(getActivity(), "Please select a section", Toast.LENGTH_LONG).show();
            return;
        }
        int selectedSectionId = sectionIDs.get(selectedSectionPosition);

        // Create and update the book
        Book updatedBook = new Book();
        updatedBook.setId(selectedBookId);
        updatedBook.setTitle(title);
        updatedBook.setAuthor(author);
        updatedBook.setPages(pages);
        updatedBook.setSectionId(selectedSectionId);

        try {
            Drawer.database.bookDao().updateBook(updatedBook);
            initSpinners();
            Toast.makeText(getActivity(), "Book updated!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            String message = e.getMessage();
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        }

        // Clear input fields
        titletext.setText("");
        authortext.setText("");
        pagestext.setText("");
    }
}
