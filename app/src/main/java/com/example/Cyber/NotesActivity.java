package com.example.Cyber;

import static com.example.Cyber.CreateNewNoteActivity.NEW_NOTE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.Cyber.Adapter.NotesAdapter;
import com.example.Cyber.ExtraClasses.InternalDataBase;
import com.example.Cyber.Model.NoteModel;
import com.example.Cyber.databinding.ActivityNotesBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotesActivity extends AppCompatActivity {

    ActivityNotesBinding binding;
    ArrayList<NoteModel> noteModelsList;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("Notes to remember");

        noteModelsList = new ArrayList<>();

        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading...");
        dialog.setMessage("Notes");
        dialog.show();

        binding.createNewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(NotesActivity.this,CreateNewNoteActivity.class).putExtra("noteType",NEW_NOTE));
            }
        });

        getAllNotes();
    }

    private void getAllNotes() {
        FirebaseFirestore.getInstance()
                .collection("Notes")
                .orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> documentSnapshots = queryDocumentSnapshots.getDocuments();
                        int unSynced = -1;

                        //checking if there are any offline notes
                        if(InternalDataBase.getInstance(NotesActivity.this).getSyncStatus()){
                            ArrayList<NoteModel> unsavedNotes = InternalDataBase.getInstance(NotesActivity.this).getUnsavedNotes();
                            unSynced = unsavedNotes.size();
//                            noteModelsList.addAll(unsavedNotes);
                            for(int i = unsavedNotes.size()-1; i >= 0; i--){
                                noteModelsList.add(unsavedNotes.get(i));
                            }
                        }

                        for(DocumentSnapshot ds: documentSnapshots){
                            NoteModel noteModel = ds.toObject(NoteModel.class);

                            noteModelsList.add(noteModel);
                        }

                        //Creating the recycler view
                        binding.notesRecView.setHasFixedSize(true);
                        binding.notesRecView.setLayoutManager(new LinearLayoutManager(NotesActivity.this));
                        NotesAdapter notesAdapter = new NotesAdapter(NotesActivity.this,noteModelsList,unSynced);
                        binding.notesRecView.setAdapter(notesAdapter);
                        notesAdapter.notifyDataSetChanged();

                        dialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NotesActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}