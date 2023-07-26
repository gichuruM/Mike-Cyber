package com.example.mabei_poa;

import static com.example.mabei_poa.ExtraClasses.ConnectivityReceiver.noConnectivity;
import static com.example.mabei_poa.HomeActivity.checkConnection;
import static com.example.mabei_poa.ProductsActivity.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.mabei_poa.ExtraClasses.InternalDataBase;
import com.example.mabei_poa.Model.NoteModel;
import com.example.mabei_poa.databinding.ActivityCreateNewNoteBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class CreateNewNoteActivity extends AppCompatActivity {

    public static final String NEW_NOTE = "new";
    public static final String EDIT_NOTE = "edit";
    ActivityCreateNewNoteBinding binding;
    String noteType = "";
    NoteModel note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateNewNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        noteType = getIntent().getStringExtra("noteType");

        if(noteType.equals(EDIT_NOTE)){
            note = (NoteModel) getIntent().getSerializableExtra("note");

            binding.newNoteHeading.setText(note.getNoteHeading());
            binding.newNoteContent.setText(note.getNoteContent());
        }

        binding.saveNewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String heading = binding.newNoteHeading.getText().toString();
                String content = binding.newNoteContent.getText().toString();

                if(heading.equals("") || content.equals("")){
                    Toast.makeText(CreateNewNoteActivity.this, "Enter all fields to proceed", Toast.LENGTH_SHORT).show();
                    return;
                }

                startActivity(new Intent(CreateNewNoteActivity.this,HomeActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(noteType.equals(NEW_NOTE)){

                            String noteId = UUID.randomUUID().toString();
                            NoteModel newNote = new NoteModel(heading,content,noteId,new Date(),true);

                            savingNote(newNote);
                        }
                        else if(noteType.equals(EDIT_NOTE)){

                            note.setNoteHeading(heading);
                            note.setNoteContent(content);

                            savingNote(note);
                        }
                    }

                    public void savingNote(NoteModel noteToSave){
                        if(checkConnection(CreateNewNoteActivity.this)){
                            FirebaseFirestore.getInstance()
                                    .collection("Notes")
                                    .document(noteToSave.getId())
                                    .set(noteToSave)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(CreateNewNoteActivity.this, "Note saved successfully", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(CreateNewNoteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                        }
                        else {
                            if(InternalDataBase.getInstance(CreateNewNoteActivity.this).addToUnsavedNotes(noteToSave)){
                                InternalDataBase.getInstance(CreateNewNoteActivity.this).setSyncStatus(true);
                                Log.d(TAG, "savingNote: To save "+noteToSave.getNoteHeading());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(CreateNewNoteActivity.this, "Note saved in offline mode", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }
                }).start();
            }
        });
//        InternalDataBase.getInstance(CreateNewNoteActivity.this).clearAllUnsavedNotes();
//        Log.d(TAG, "onCreate: unsaved notes "+InternalDataBase.getInstance(CreateNewNoteActivity.this).getUnsavedNotes().size()
//    +"status "+InternalDataBase.getInstance(CreateNewNoteActivity.this).getSyncStatus());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if(noteType.equals(EDIT_NOTE)){
            getMenuInflater().inflate(R.menu.deleteproductmenu,menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(noteType.equals(EDIT_NOTE)){
            if(item.getItemId() == R.id.deleteProduct){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Alert");
                builder.setMessage("Are you sure you want to delete this note?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean inCart = false;
                        ArrayList<NoteModel> notesList = InternalDataBase.getInstance(CreateNewNoteActivity.this).getUnsavedNotes();

                        for(NoteModel n : notesList){
                            if(n.getId().equals(note.getId())){
                                if(InternalDataBase.getInstance(CreateNewNoteActivity.this).removeFromUnsavedNotes(note)){
                                    Toast.makeText(CreateNewNoteActivity.this, "Successfully deleted offline note", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(CreateNewNoteActivity.this,HomeActivity.class));

                                    if(InternalDataBase.getInstance(CreateNewNoteActivity.this).getUnsavedNotes().size() == 0)
                                        InternalDataBase.getInstance(CreateNewNoteActivity.this).clearAllUnsavedNotes();
                                }
                                inCart = true;
                                break;
                            }
                        }

                        if(!inCart){
                            FirebaseFirestore.getInstance()
                                    .collection("Notes")
                                    .document(note.getId())
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(CreateNewNoteActivity.this, "Successfully deleted", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(CreateNewNoteActivity.this,HomeActivity.class));
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(CreateNewNoteActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });

                builder.show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

}