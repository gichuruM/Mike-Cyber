package com.example.Cyber.Model;

import java.io.Serializable;
import java.util.Date;

public class NoteModel implements Serializable {

    private String noteHeading;
    private String noteContent;
    private String id;
    private Date time;
    private boolean standAloneNote;

    public NoteModel() {
    }

    public NoteModel(String noteHeading, String noteContent, String id, Date time, boolean standAloneNote) {
        this.noteHeading = noteHeading;
        this.noteContent = noteContent;
        this.id = id;
        this.time = time;
        this.standAloneNote = standAloneNote;
    }

    public boolean isStandAloneNote() {
        return standAloneNote;
    }
    public void setStandAloneNote(boolean standAloneNote) {
        this.standAloneNote = standAloneNote;
    }
    public Date getTime() {
        return time;
    }
    public void setTime(Date time) {
        this.time = time;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getNoteHeading() {
        return noteHeading;
    }
    public void setNoteHeading(String noteHeading) {
        this.noteHeading = noteHeading;
    }
    public String getNoteContent() {
        return noteContent;
    }
    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }
}
