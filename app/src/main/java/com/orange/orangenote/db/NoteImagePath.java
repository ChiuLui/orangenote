package com.orange.orangenote.db;

import org.litepal.crud.DataSupport;

/**
 * @author 神经大条蕾弟
 * @version 1.0
 * @date 2018/05/23 16:24
 * @copyright 赵蕾
 */

public class NoteImagePath extends DataSupport {

    private int id;

    private int noteId;

    private String imagePath;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
