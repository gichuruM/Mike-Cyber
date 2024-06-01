package com.example.mabei_poa.ExtraClasses;

import static com.example.mabei_poa.ProductsActivity.TAG;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.mabei_poa.HomeActivity;
import com.example.mabei_poa.Model.NoteModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class SyncNotesRunnable implements Runnable {

    private Context context;
    private NoteModel note;
    private View view;

    public SyncNotesRunnable(Context context, NoteModel note, View view) {
        this.context = context;
        this.note = note;
        this.view = view;
    }

    @Override
    public void run() {
        FirebaseFirestore.getInstance()
                .collection("Notes")
                .document(note.getId())
                .set(note)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        InternalDataBase.getInstance(context).removeFromUnsavedNotes(note);
                        if(InternalDataBase.getInstance(context).getUnsavedNotes().size() == 0 &&
                                InternalDataBase.getInstance(context).getOfflineTransactions().size() == 0 &&
                                InternalDataBase.getInstance(context).getOfflineDebtUpdates().size() == 0){

                            InternalDataBase.getInstance(context).setSyncStatus(false);
                            view.clearAnimation();
                            view.setVisibility(View.GONE);
                            Toast.makeText(context, "Synced successfully", Toast.LENGTH_SHORT).show();
                        }

                        Log.d(TAG, "onSuccess: note heading "+note.getNoteHeading()+" synced");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+e.getMessage());
                    }
                });
    }
}
