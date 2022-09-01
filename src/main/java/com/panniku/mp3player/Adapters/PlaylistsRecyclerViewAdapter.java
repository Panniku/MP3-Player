package com.panniku.mp3player.Adapters;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.panniku.mp3player.Constructors.PlaylistsConstructor;
import com.panniku.mp3player.R;

import java.util.ArrayList;

public class PlaylistsRecyclerViewAdapter extends RecyclerView.Adapter<PlaylistsRecyclerViewAdapter.PlaylistsViewHolder>{

    private Context context;
    private ArrayList<PlaylistsConstructor> playlistsConstructor;

    private OnRecyclerViewClick mOnRecyclerViewClick;
    private OnRecyclerViewLongClick mOnRecyclerViewLongClick;

    public interface OnRecyclerViewClick {
        void OnItemClick(View view, int position);
    }

    public interface OnRecyclerViewLongClick {
        void OnItemLongClick(View view, int position);
    }

    public PlaylistsRecyclerViewAdapter(Context context, ArrayList<PlaylistsConstructor> playlistsConstructor, OnRecyclerViewClick listener, OnRecyclerViewLongClick longListener){

        this.context = context;
        this.playlistsConstructor = playlistsConstructor;
        mOnRecyclerViewClick = listener;
        mOnRecyclerViewLongClick = longListener;

    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class PlaylistsViewHolder extends RecyclerView.ViewHolder {

        private TextView playlistName, playlistCreationDate;
        private ImageView playlistThumb;

        public PlaylistsViewHolder(View itemView) {
            super(itemView);
            playlistName = itemView.findViewById(R.id.playlistName);
            playlistCreationDate = itemView.findViewById(R.id.playlistCreationDate);
            playlistThumb = itemView.findViewById(R.id.playlistThumb);

            //Log.d("viewHolder()", "viewHolder: mListener:" + listener);

        }
    }

    @NonNull
    @Override
    public PlaylistsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("onCreateViewHolder: ", "CREATED");
        View view = LayoutInflater.from(context).inflate(R.layout.playlist_list_item, parent, false);

        PlaylistsViewHolder viewHolder = new PlaylistsViewHolder(view);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnRecyclerViewClick.OnItemClick(view, viewHolder.getAdapterPosition());
            }
        });

        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mOnRecyclerViewLongClick.OnItemLongClick(view, viewHolder.getAdapterPosition());
                return true;
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistsViewHolder holder, int position) {
        holder.playlistName.setText(playlistsConstructor.get(position).getName());
        holder.playlistCreationDate.setText(playlistsConstructor.get(position).getDate());

        holder.playlistThumb.setImageBitmap(playlistsConstructor.get(position).getThumb());

        holder.playlistName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        holder.playlistName.setSelected(true);

        holder.playlistCreationDate.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        holder.playlistCreationDate.setSelected(true);
    }

    @Override
    public int getItemCount() {
        //Log.d("getItemCount()", "size: "  + musicAdapter.size());
        return playlistsConstructor.size();
    }
}