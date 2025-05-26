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

    private final List<Note> notes = new ArrayList<>();
    private NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initial "Welcome" note
        notes.add(new Note("Welcome", "Tap the + button to add a note"));

        setupRecycler();
        setupFab();
    }

    private void setupRecycler() {
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter   = new NoteAdapter(notes);
        recycler.setAdapter(adapter);
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showAddDialog());
    }

    /**
     * Shows a dialog to create a new note.
     */
    private void showAddDialog() {
        // Programmatically build two text fields
        EditText etTitle = new EditText(this);
        etTitle.setHint("Title");
        EditText etBody = new EditText(this);
        etBody.setHint("Description");
        etBody.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        etBody.setMinLines(3);

        // Wrap them in a simple layout
        View dialogView = new LinearLayoutCompat(this);
        ((LinearLayoutCompat) dialogView).setOrientation(LinearLayoutCompat.VERTICAL);
        int pad = getResources().getDimensionPixelSize(R.dimen.padding_standard);
        ((LinearLayoutCompat) dialogView).setPadding(pad, pad, pad, pad);
        ((LinearLayoutCompat) dialogView).addView(etTitle);
        ((LinearLayoutCompat) dialogView).addView(etBody);

        new AlertDialog.Builder(this)
                .setTitle("New Note")
                .setView(dialogView)
                .setPositiveButton("Save", (d, which) -> {
                    String title = etTitle.getText().toString().trim();
                    String body  = etBody.getText().toString().trim();
                    if (!title.isEmpty()) {
                        notes.add(new Note(title, body));
                        adapter.notifyItemInserted(notes.size() - 1);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Inner data class – keeps the note in memory.
     */
    private static class Note {
        final String title;
        final String body;
        boolean done = false;

        Note(String title, String body) {
            this.title = title;
            this.body  = body;
        }
    }

    /**
     * RecyclerView Adapter and ViewHolder combined in one inner class for brevity.
     */
    private class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.VH> {

        private final List<Note> data;

        NoteAdapter(List<Note> data) {
            this.data = data;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_note, parent, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            Note n = data.get(pos);
            h.title.setText(n.title);
            h.status.setText(n.done ? "Done" : "Not done");
        }

        @Override public int getItemCount() { return data.size(); }

        class VH extends RecyclerView.ViewHolder implements View.OnClickListener {
            final TextView title  = itemView.findViewById(R.id.tvTitle);
            final TextView status = itemView.findViewById(R.id.tvDone);
            VH(@NonNull View itemView) {
                super(itemView);
                itemView.setOnClickListener(this);
            }
            @Override public void onClick(View v) {
                int pos = getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                showViewDialog(data.get(pos), pos);
            }
        }
    }

    /**
     * Dialog that displays a note and allows toggling its "done" state.
     */
    private void showViewDialog(Note note, int position) {
        View dialog = LayoutInflater.from(this).inflate(R.layout.item_note, null);
        TextView tvTitle = dialog.findViewById(R.id.tvTitle);
        TextView tvStatus = dialog.findViewById(R.id.tvDone);
        tvTitle.setText(note.title + "\n\n" + note.body);
        tvStatus.setText(note.done ? "Done" : "Not done");

        new AlertDialog.Builder(this)
                .setTitle("Note")
                .setView(dialog)
                .setPositiveButton(note.done ? "Mark as Undone" : "Mark as Done", (d, w) -> {
                    note.done = !note.done;
                    adapter.notifyItemChanged(position);
                })
                .setNegativeButton("Close", null)
                .show();
    }
}
