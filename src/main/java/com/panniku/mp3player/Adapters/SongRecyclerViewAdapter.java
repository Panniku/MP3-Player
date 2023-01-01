package com.panniku.mp3player.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.panniku.mp3player.Constructors.SongsConstructor;
import com.panniku.mp3player.MainActivity;
import com.panniku.mp3player.Overlay.OverlayPlayer;
import com.panniku.mp3player.R;
import com.panniku.mp3player.Utils.Utils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class SongRecyclerViewAdapter extends RecyclerView.Adapter<SongRecyclerViewAdapter.SongViewHolder>{

    private Context context;
    private ArrayList<SongsConstructor> songsConstructor;

    private OnRecyclerViewClick mOnRecyclerViewClick;
    private OnRecyclerViewLongClick mOnRecyclerViewLongClick;

    public interface OnRecyclerViewClick {
        void OnItemClick(View view, int position);
    }

    public interface OnRecyclerViewLongClick {
        void OnItemLongClick(View view, int position);
    }

    public SongRecyclerViewAdapter(Context context, ArrayList<SongsConstructor> songsConstructor, OnRecyclerViewClick listener, OnRecyclerViewLongClick longListener){

        this.context = context;
        this.songsConstructor = songsConstructor;
        mOnRecyclerViewClick = listener;
        mOnRecyclerViewLongClick = longListener;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class SongViewHolder extends RecyclerView.ViewHolder {

        private TextView songName, songArtist, songDuration;
        private ImageView songThumb;

        public SongViewHolder(View itemView) {
            super(itemView);
            songName = itemView.findViewById(R.id.songName);
            songArtist = itemView.findViewById(R.id.songArtist);
            songDuration = itemView.findViewById(R.id.songDuration);
            songThumb = itemView.findViewById(R.id.songThumb);

            //Log.d("viewHolder()", "viewHolder: mListener:" + listener);

        }
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("onCreateViewHolder: ", "Created ViewHolder");
        View view = LayoutInflater.from(context).inflate(R.layout.song_list_item, parent, false);

        SongViewHolder viewHolder = new SongViewHolder(view);

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
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Log.d("onBindViewHolder: ", "onBindViewHolder()");

        holder.songName.setText(songsConstructor.get(position).getTitle());
        holder.songArtist.setText(songsConstructor.get(position).getArtist());
        holder.songDuration.setText(Utils.getTimeFormatted(songsConstructor.get(position).getDuration()));

        File f = new File(OverlayPlayer.cacheDir);
        if(f.exists()){
            File filePath = new File(f + "/" + songsConstructor.get(position).getOgName() + ".png");
            Picasso.get().load(filePath).error(R.drawable.song_image).into(holder.songThumb, new Callback() {
                @Override
                public void onSuccess() {
//                    Log.d("onSuccess: ", "SUCCESS");
//                    Log.d("songsConstructor", "LOADED PICASSO!");
                }

                @Override
                public void onError(Exception e) {
                    //Log.d("onError: ", "FAILED.");
                }
            });
            Log.d("songsConstructor", "path: " + filePath);
        } else {
            Bitmap bitmap = Utils.getAlbumArt(songsConstructor.get(position).getPath());
            if(bitmap != null){
                holder.songThumb.setImageBitmap(bitmap);
            } else {
                holder.songThumb.setImageResource(R.drawable.song_image);
            }
            Log.d("songsConstructor", "LOADED BITMAP");
        }

    }

    @Override
    public int getItemCount() {
        //Log.d("getItemCount()", "size: "  + musicAdapter.size());
        return songsConstructor.size();
    }
}