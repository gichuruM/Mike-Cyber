package com.example.Cyber.Adapter;

import static com.example.Cyber.CreateNewNoteActivity.EDIT_NOTE;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Cyber.CreateNewNoteActivity;
import com.example.Cyber.Model.NoteModel;
import com.example.Cyber.R;

import java.util.ArrayList;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.MyViewHolder>{

    private Context context;
    private ArrayList<NoteModel> noteModels;
    private int notSynced;

    public NotesAdapter(Context context, ArrayList<NoteModel> noteModels, int notSynced) {
        this.context = context;
        this.noteModels = noteModels;
        this.notSynced = notSynced;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.one_note_layout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        NoteModel note = noteModels.get(position);

        holder.notesHeading.setText(note.getNoteHeading());
        holder.notesContent.setText(note.getNoteContent());

        if(holder.getAdapterPosition() < notSynced)
            holder.noteNotSynced.setVisibility(View.VISIBLE);

        if(note.isStandAloneNote())
            holder.noteSource.setText("Source: Notes");
        else
            holder.noteSource.setText("Source: Transaction");

        holder.noteContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, CreateNewNoteActivity.class).putExtra("noteType",EDIT_NOTE).putExtra("note",note));
            }
        });
    }

    @Override
    public int getItemCount() {
        return noteModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        CardView noteContainer;
        TextView notesHeading, notesContent, noteSource;
        ImageView noteNotSynced;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            notesHeading = itemView.findViewById(R.id.notesHeading);
            notesContent = itemView.findViewById(R.id.notesContent);
            noteContainer = itemView.findViewById(R.id.noteContainer);
            noteSource = itemView.findViewById(R.id.noteSource);
            noteNotSynced = itemView.findViewById(R.id.noteNotSynced);
        }
    }
}
