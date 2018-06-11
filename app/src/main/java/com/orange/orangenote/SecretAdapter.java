package com.orange.orangenote;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.orange.orangenote.db.Note;
import com.orange.orangenote.db.NoteImagePath;
import com.orange.orangenote.util.ContentUtil;
import com.orange.orangenote.util.dp2px;

import org.litepal.LitePal;

import java.util.List;


/**
 * 私密便签页面自定义适配器
 *
 * @author 神经大条蕾弟
 * @version 1.0
 * @date 2018/04/21 17:13
 * @copyright 赵蕾
 */

class SecretAdapter extends RecyclerView.Adapter<SecretAdapter.ViewHolder> {

    private Context mContext;

    private List<Note> mNoteList;

    public SecretAdapter(Context mContext, List<Note> mNoteList) {
        this.mContext = mContext;
        this.mNoteList = mNoteList;
    }

    /**
     * 移除被选中对象
     *
     * @param note
     */
    public void deleteNote(Note note) {
        LitePal.deleteAll(Note.class, "id = ?", note.getId() + "");
        mNoteList.remove(note);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        LinearLayout linearLayout;
        TextView textView_title;
        TextView textView_content;
        TextView textView_itme;
        CheckBox checkBox_item;
        ImageView imageView_item;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            linearLayout = itemView.findViewById(R.id.linelayout_item);
            textView_title = itemView.findViewById(R.id.text_item_title);
            textView_content = itemView.findViewById(R.id.text_item_content);
            textView_itme = itemView.findViewById(R.id.text_item_tiem);
            checkBox_item = itemView.findViewById(R.id.checkbox_item);
            imageView_item = itemView.findViewById(R.id.imageview_item);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = null;
        //根据不同的视图选择不同的布局
        if (SecretActivity.isListView){
            //列表布局
            view = LayoutInflater.from(mContext).inflate(R.layout.note_item, parent, false);
        } else {
            //瀑布流布局
            view = LayoutInflater.from(mContext).inflate(R.layout.note_item2, parent, false);
        }

        final ViewHolder viewHolder = new ViewHolder(view);

        viewHolder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //不在删除模式下长按, 进入删除模式, 消耗事件
                if (!SecretActivity.isDelete) {
                    int position = viewHolder.getAdapterPosition();
                    Note note = mNoteList.get(position);
                    if (!SecretActivity.isDelete) {
                        SecretActivity.deleteNote.clear();
                        SecretActivity.isDelete = true;
                        //判断长按的对象是否置顶了
                        if (note.isTop()) {
                            SecretActivity.isTop = true;
                            SecretActivity.menu.findItem(R.id.top_toolbar).setIcon(R.drawable.download);
                        } else {
                            SecretActivity.isTop = false;
                            SecretActivity.menu.findItem(R.id.top_toolbar).setIcon(R.drawable.top);

                        }
                        //隐藏切换布局图标  显示删除图标和置顶图标
                        SecretActivity.menu.findItem(R.id.view_toolbar).setVisible(false);
                        SecretActivity.menu.findItem(R.id.delete_toolbar).setVisible(true);
                        SecretActivity.menu.findItem(R.id.secret_toolbar).setVisible(true);
                        SecretActivity.menu.findItem(R.id.top_toolbar).setVisible(true);
                        SecretActivity.menu.findItem(R.id.allcheck_toolbar).setVisible(true);
                    }
                    notifyDataSetChanged();
                    return true;
                } else {
                    //在删除模式下长按, 不消耗事件.
                    return false;
                }
            }
        });

        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                Note note = mNoteList.get(position);
                //不在退出模式下, 跳转到NewNote.class
                if (!SecretActivity.isDelete) {
                    Intent intent = new Intent(mContext, NewNote.class);
                    int nowId = note.getId();
                    String nowYear = note.getYear();
                    String nowDate = note.getDate();
                    String nowTime = note.getTime();
                    String nowContent = note.getContent();
                    boolean nowState = true;
                    boolean isRemind = note.isRemind();
                    intent.putExtra("nowId", nowId);
                    intent.putExtra("nowYear", nowYear);
                    intent.putExtra("nowDate", nowDate);
                    intent.putExtra("nowTime", nowTime);
                    intent.putExtra("nowContent", nowContent);
                    intent.putExtra("nowState", nowState);
                    intent.putExtra("isRemind", isRemind);
                    mContext.startActivity(intent);
                } else {
                    //在删除模式下
                    if (note.isTop() == SecretActivity.isTop) {
                        //点击item, 如果当前复选框被选中就显示未选中并且待删除列表中移除对象
                        if (viewHolder.checkBox_item.isChecked()) {
                            SecretActivity.deleteNote.remove(note);
                            viewHolder.checkBox_item.setChecked(false);
                        } else {
                            //如果当前复选框为未选中, 设置选中, 并且添加到待删除列表
                            SecretActivity.deleteNote.add(note);
                            viewHolder.checkBox_item.setChecked(true);
                        }
                    }
                }
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Note note = mNoteList.get(position);
        String temp = note.getContent();//得到内容
        temp = ContentUtil.getNoHtmlContent(temp);
        //根据不同的内容视图设置不同的长度的标题和内容
        if (SecretActivity.isListView) {
            holder.textView_title.setText(ContentUtil.getTitle(temp));
            holder.textView_content.setText(ContentUtil.getContent(temp));
        } else {
            holder.textView_title.setText(ContentUtil.getTitle(temp,8));
            holder.textView_content.setText(ContentUtil.getContent(temp, 8));
        }
        //当前内容长度是否为空内容, 隐藏或显示, 并且添加内边距补足高度
        if (holder.textView_content.getText().length() <= 0 || holder.textView_content.getText().equals("") || holder.textView_content.getText() == null || holder.textView_content.getText().equals(" ") || holder.textView_content.getText().equals("\n") || (holder.textView_content.equals("<br><br>")) || (holder.textView_content.equals("&nbsp;"))) {
            holder.textView_content.setVisibility(View.GONE);
            holder.linearLayout.setPadding(dp2px.dip2px(mContext, 15), dp2px.dip2px(mContext, 27), dp2px.dip2px(mContext, 15), dp2px.dip2px(mContext, 27));
        } else {
            holder.textView_content.setVisibility(View.VISIBLE);
            holder.linearLayout.setPadding(dp2px.dip2px(mContext, 15), dp2px.dip2px(mContext, 15), dp2px.dip2px(mContext, 15), dp2px.dip2px(mContext, 15));
        }
        holder.textView_itme.setText(note.getYear() + note.getDate() + "  " + note.getTime());
        //如果当前是否为删除模式, 显示或隐藏复选框
        if (SecretActivity.isDelete) {
            // 当前对象 的状态和 长按 的状态一样才能显示
            if (note.isTop() == SecretActivity.isTop) {
                holder.checkBox_item.setVisibility(View.VISIBLE);
            }
        } else {
            holder.checkBox_item.setVisibility(View.GONE);
        }
        //当待删除的列表为空时, 重置checkBok, 设置所有复选框为false
        if (SecretActivity.deleteNote == null || SecretActivity.deleteNote.size() <= 0) {
            holder.checkBox_item.setChecked(false);
            SecretActivity.deleteNote.clear();
        }
        //当前是否处于ListView视图, 动态改变内容显示的单行还是多行模式
        if (SecretActivity.isListView) {
            holder.textView_content.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            holder.textView_content.setSingleLine(true);
        } else {
            holder.textView_content.setEllipsize(null);
            holder.textView_content.setSingleLine(false);
        }
        //判断是全选状态还是取消全选状态.
        if (SecretActivity.isAllCheck == SecretActivity.isAllCheck_CHECK) {
            if (note.isTop() == SecretActivity.isTop) {
                holder.checkBox_item.setChecked(true);
            }
        } else if (SecretActivity.isAllCheck == SecretActivity.isAllCheck_UPCHECK) {
            if (note.isTop() == SecretActivity.isTop) {
                holder.checkBox_item.setChecked(false);
            }
        }
        //判断如果内容包含图片,则显示图片设置图片
        if (note.getContent().indexOf("/storage/emulated/0/Pictures/") != -1) {
            List<NoteImagePath> noteImagePaths = LitePal.where("noteId = ?", note.getId() + "").order("id desc").find(NoteImagePath.class);
            if (!(noteImagePaths.isEmpty())) {
                for (NoteImagePath noteImagePath : noteImagePaths) {
                    String path = noteImagePath.getImagePath();
                    if (note.getContent().indexOf(path) != -1) {
                        Glide.with(mContext)
                                .load(path)
                                .centerCrop()
                                .crossFade(500)
                                .error(R.drawable.loadfail)
                                .into(holder.imageView_item);
                        holder.imageView_item.setVisibility(View.VISIBLE);
                        return;
                /*centerInside()
                center()
                centerCrop() //缩放图片让图片充满整个ImageView的边框，然后裁掉超出的部分。
                fitCenter()  // ImageView会被完全填充满，但是图片可能不能完全显示出。*/
                    }
                }
            } else {
                holder.imageView_item.setVisibility(View.GONE);
            }
        } else {
            holder.imageView_item.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mNoteList.size();
    }
}
