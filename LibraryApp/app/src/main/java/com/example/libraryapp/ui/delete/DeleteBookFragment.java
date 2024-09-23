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
import com.example.libraryapp.database.Book;
import com.example.libraryapp.databinding.FragmentDeleteBookBinding;

import java.util.ArrayList;
import java.util.List;

public class DeleteBookFragment extends Fragment {

    private FragmentDeleteBookBinding binding;
    Button deletebutton;
    List<Integer> bookIDs;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout using the binding
        binding = FragmentDeleteBookBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize the spinner
        initSpinner();

        deletebutton = view.findViewById(R.id.deleteButton);
        deletebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Get the selected book ID from the spinner
                    int selectedPosition = binding.spinnerOptions.getSelectedItemPosition();

                    // Check if there are books available
                    if (bookIDs.isEmpty()) {
                        Toast.makeText(getActivity(), "No books available to delete!", Toast.LENGTH_LONG).show();
                        return; // Exit the method
                    }

                    int selectedBookId = bookIDs.get(selectedPosition);

                    // Create a new Book object with the updated details
                    Book deletedBook = new Book();
                    deletedBook.setId(selectedBookId);

                    // Update the book in the database
                    Drawer.database.bookDao().deleteBook(deletedBook);

                    initSpinner();

                    Toast.makeText(getActivity(), "Book deleted!", Toast.LENGTH_LONG).show();
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
            List<Book> books = Drawer.database.bookDao().getAllBooks();

            // Extract the titles from the list of books
            List<String> bookTitles = new ArrayList<>();
            bookIDs = new ArrayList<>();
            for (Book book : books) {
                bookTitles.add(book.getTitle());
                bookIDs.add(book.getId());
            }

            // Create an adapter and set it to the spinner
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, bookTitles);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerOptions.setAdapter(adapter);


        }
    }

}