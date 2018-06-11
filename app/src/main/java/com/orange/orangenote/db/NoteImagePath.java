package com.orange.orangenote.db;

import org.litepal.crud.DataSupport;
import org.litepal.crud.LitePalSupport;

/**
 * 插入的图片地址
 * @author 神经大条蕾弟
 * @version 1.0
 * @date 2018/05/23 16:24
 * @copyright 赵蕾
 */

public class NoteImagePath extends LitePalSupport {

    /** ID */
    private int id;

    /** 对应的便签ID */
    private int noteId;

    /** 图片地址 */
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
