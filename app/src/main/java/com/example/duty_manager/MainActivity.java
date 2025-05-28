/*
 * MainActivity.java — drop-in replacement.
 * Uses Room for simple persistence while keeping UI logic almost untouched.
 * All comments are in English as requested.
 */
package com.example.duty_manager;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /** DAO reference for all DB operations. */
    private NoteDao noteDao;

    /** In-memory cache mirroring DB content (used by the adapter). */
    private final List<Note> notes = new ArrayList<>();

    private NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Build a Room DB. allowMainThreadQueries() is *only* to keep the example short.
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                        AppDatabase.class,
                        "notes.db")
                .allowMainThreadQueries()
                .build();
        noteDao = db.noteDao();

        // Load persisted notes.
        notes.addAll(noteDao.getAll());

        // First run → add a welcome note.
        if (notes.isEmpty()) {
            Note welcome = new Note("Welcome", "Tap the + button to add a note");
            welcome.id = (int) noteDao.insert(welcome);
            notes.add(welcome);
        }

        setupRecycler();
        setupFab();
    }

    /** Initializes RecyclerView with a vertical LinearLayoutManager. */
    private void setupRecycler() {
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter(notes);
        recycler.setAdapter(adapter);
    }

    /** Sets up Floating Action Button for adding notes. */
    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showAddDialog());
    }

    /** Dialog for creating a new note. */
    private void showAddDialog() {
        EditText etTitle = new EditText(this);
        etTitle.setHint("Title");

        EditText etBody = new EditText(this);
        etBody.setHint("Description");
        etBody.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        etBody.setMinLines(3);

        // Simple vertical container with padding.
        LinearLayoutCompat container = new LinearLayoutCompat(this);
        container.setOrientation(LinearLayoutCompat.VERTICAL);
        int pad = getResources().getDimensionPixelSize(R.dimen.padding_standard);
        container.setPadding(pad, pad, pad, pad);
        container.addView(etTitle);
        container.addView(etBody);

        new AlertDialog.Builder(this)
                .setTitle("New Note")
                .setView(container)
                .setPositiveButton("Save", (d, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String body  = etBody.getText().toString().trim();
                    if (!title.isEmpty()) {
                        // Persist to DB first.
                        Note n = new Note(title, body);
                        n.id = (int) noteDao.insert(n);
                        notes.add(n);
                        sortNotes();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Shows a note and lets user toggle “done” or delete it. */
    private void showViewDialog(Note note, int position) {
        View dialog = LayoutInflater.from(this).inflate(R.layout.item_note, null);
        TextView tvTitle  = dialog.findViewById(R.id.tvTitle);
        TextView tvStatus = dialog.findViewById(R.id.tvDone);

        tvTitle.setText(note.title + "\n\n" + note.body);
        tvStatus.setText(note.done ? "Done" : "Not done");

        new AlertDialog.Builder(this)
                .setTitle("Note")
                .setView(dialog)
                .setPositiveButton(note.done ? "Mark as Undone" : "Mark as Done", (d, w) -> {
                    note.done = !note.done;
                    noteDao.update(note);      // persist change
                    sortNotes();
                })
                .setNeutralButton("Delete", (d, w) -> {
                    notes.remove(position);
                    noteDao.delete(note);       // remove from DB
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Close", null)
                .show();
    }

    /** Sorts notes (undone first) and refreshes adapter. */
    private void sortNotes() {
        notes.sort((a, b) -> Boolean.compare(a.done, b.done)); // false < true
        adapter.notifyDataSetChanged();
    }

    /* --------------------------------------------------------------------- */
    /* RecyclerView adapter & view-holder.                                   */
    /* --------------------------------------------------------------------- */

    private class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.VH> {

        private final List<Note> data;

        NoteAdapter(List<Note> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_note, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            Note n = data.get(pos);
            h.title.setText(n.title);
            h.status.setText(n.done ? "Done" : "Not done");
            h.itemView.setAlpha(n.done ? 0.5f : 1.0f); // subtle opacity for completed items
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        /** View-holder that handles item clicks. */
        class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView title  = itemView.findViewById(R.id.tvTitle);
            final TextView status = itemView.findViewById(R.id.tvDone);

            VH(@NonNull View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    showViewDialog(data.get(pos), pos);
                }
            }
        }
    }
}
