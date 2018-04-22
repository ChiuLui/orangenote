package com.orange.orangenote;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.orange.orangenote.db.Note;
import com.orange.orangenote.util.ContentUtil;
import com.orange.orangenote.util.DateUtil;

import java.util.List;

/**
 * @author 神经大条蕾弟
 * @version 1.0
 * @date 2018/04/21 17:13
 * @copyright 赵蕾
 */

class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder>{

    private Context mContext;

    private List<Note> mNoteList;

    public NoteAdapter(Context mContext, List<Note> mNoteList) {
        this.mContext = mContext;
        this.mNoteList = mNoteList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView textView_title;
        TextView textView_content;
        TextView textView_time;
    public ViewHolder(View itemView) {
        super(itemView);
        cardView = (CardView) itemView;
        textView_title = cardView.findViewById(R.id.text_item_title);
        textView_content = cardView.findViewById(R.id.text_item_content);
        textView_time = cardView.findViewById(R.id.text_item_tiem);
    }
}
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.note_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                Note note = mNoteList.get(position);
                Intent intent = new Intent(mContext, NewNote.class);
                int nowId = note.getId();
                String nowYear = note.getYear();
                String nowDate = note.getDate();
                String nowTime = note.getTime();
                String nowContent = note.getContent();
                boolean nowState = true;
                intent.putExtra("nowId", nowId);
                intent.putExtra("nowYear", nowYear);
                intent.putExtra("nowDate", nowDate);
                intent.putExtra("nowTime", nowTime);
                intent.putExtra("nowContent", nowContent);
                intent.putExtra("nowState", nowState);
                mContext.startActivity(intent);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Note note = mNoteList.get(position);
        String temp = note.getContent();
        holder.textView_title.setText(ContentUtil.getTitle(temp));
        holder.textView_content.setText(ContentUtil.getContent(temp));
        if (holder.textView_content.getText().length() <= 0 || holder.textView_content.getText().equals("") || holder.textView_content.getText() == null || holder.textView_content.getText().equals(" ") || holder.textView_content.getText().equals("\n")){
            holder.textView_content.setVisibility(View.GONE);
        } else {
            holder.textView_content.setVisibility(View.VISIBLE);
        }
        holder.textView_time.setText(note.getYear() + "  " + note.getDate() + "  " + note.getTime());
    }

    @Override
    public int getItemCount() {
        return mNoteList.size();
    }


}
