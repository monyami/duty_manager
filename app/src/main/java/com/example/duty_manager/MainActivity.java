package com.example.duty_manager;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Single–file implementation: keeps data in memory, shows a RecyclerView, and handles dialogs.
 */
public class MainActivity extends AppCompatActivity {

    // In-memory list of notes (not persisted)
    private final List<Note> notes = new ArrayList<>();
    private NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Add an initial placeholder note
        notes.add(new Note("Welcome", "Tap the + button to add a note"));

        setupRecycler(); // Setup RecyclerView
        setupFab();      // Setup Floating Action Button
    }

    // Initializes the RecyclerView with vertical layout and connects adapter
    private void setupRecycler() {
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter(notes);
        recycler.setAdapter(adapter);
    }

    // Connects the FAB and its click action
    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showAddDialog());
    }

    /**
     * Displays a dialog allowing the user to create a new note.
     */
    private void showAddDialog() {
        // Create two EditText fields for title and description
        EditText etTitle = new EditText(this);
        etTitle.setHint("Title");

        EditText etBody = new EditText(this);
        etBody.setHint("Description");
        etBody.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        etBody.setMinLines(3);

        // Wrap the fields in a vertical layout with padding
        View dialogView = new LinearLayoutCompat(this);
        ((LinearLayoutCompat) dialogView).setOrientation(LinearLayoutCompat.VERTICAL);
        int pad = getResources().getDimensionPixelSize(R.dimen.padding_standard);
        ((LinearLayoutCompat) dialogView).setPadding(pad, pad, pad, pad);
        ((LinearLayoutCompat) dialogView).addView(etTitle);
        ((LinearLayoutCompat) dialogView).addView(etBody);

        // Show a dialog with Save/Cancel options
        new AlertDialog.Builder(this)
                .setTitle("New Note")
                .setView(dialogView)
                .setPositiveButton("Save", (d, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String body = etBody.getText().toString().trim();
                    if (!title.isEmpty()) {
                        // Add new note and update the adapter
                        notes.add(new Note(title, body));
                        adapter.notifyItemInserted(notes.size() - 1);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Basic data model representing a Note.
     */
    private static class Note {
        final String title;
        final String body;
        boolean done = false;

        Note(String title, String body) {
            this.title = title;
            this.body = body;
        }
    }

    /**
     * Adapter class for displaying notes in the RecyclerView.
     */
    private class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.VH> {

        private final List<Note> data;

        NoteAdapter(List<Note> data) {
            this.data = data;
        }

        // Creates new ViewHolder (called only when no reusable view is available)
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_note, parent, false);
            return new VH(v);
        }

        // Binds data to the view (called every time a view becomes visible)
        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Note n = data.get(pos);
            h.title.setText(n.title);
            h.status.setText(n.done ? "Done" : "Not done");
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        // ViewHolder holds references to views and handles click events
        class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView title = itemView.findViewById(R.id.tvTitle);
            final TextView status = itemView.findViewById(R.id.tvDone);

            VH(@NonNull View itemView) {
                super(itemView);
                itemView.setOnClickListener(this); // Handle click on entire item
            }

            @Override
            public void onClick(View v) {
                int pos = getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                showViewDialog(data.get(pos), pos);
            }
        }
    }

    /**
     * Displays a note and allows the user to toggle its "done" state.
     */
    private void showViewDialog(Note note, int position) {
        // Inflate the note layout again for the dialog
        View dialog = LayoutInflater.from(this).inflate(R.layout.item_note, null);
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvStatus = dialog.findViewById(R.id.tvDone);

        // Combine title and body into one block
        tvTitle.setText(note.title + "\n\n" + note.body);
        tvStatus.setText(note.done ? "Done" : "Not done");

        // Dialog for viewing/toggling note status
        new AlertDialog.Builder(this)
                .setTitle("Note")
                .setView(dialog)
                .setPositiveButton(note.done ? "Mark as Undone" : "Mark as Done", (d, w) -> {
                    // Toggle the done flag and update the view
                    note.done = !note.done;
                    adapter.notifyItemChanged(position);
                })
                .setNegativeButton("Close", null)
                .show();
    }
}
