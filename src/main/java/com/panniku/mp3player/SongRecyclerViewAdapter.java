package com.panniku.mp3player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class SongRecyclerViewAdapter extends RecyclerView.Adapter<SongRecyclerViewAdapter.SongViewHolder>{

    private Context context;
    private ArrayList<MusicAdapter> musicAdapter;

    private OnRecyclerViewClick mOnRecyclerViewClick;
    private OnRecyclerViewLongClick mOnRecyclerViewLongClick;

    public interface OnRecyclerViewClick {
        void OnItemClick(View view, int position);
    }

    public interface OnRecyclerViewLongClick {
        void OnItemLongClick(View view, int position);
    }

    public SongRecyclerViewAdapter(Context context, ArrayList<MusicAdapter> musicAdapter, OnRecyclerViewClick listener, OnRecyclerViewLongClick longListener){

        this.context = context;
        this.musicAdapter = musicAdapter;
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
        Log.d("onCreateViewHolder: ", "CREATED");
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
        holder.songName.setText(musicAdapter.get(position).getTitle());
        holder.songArtist.setText(musicAdapter.get(position).getArtist());
        holder.songDuration.setText(getTimeFormatted(musicAdapter.get(position).getDuration()));

        byte[] image = getAlbumArt(musicAdapter.get(position).getPath());
        if(image != null) {
            holder.songThumb.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
            Log.d("onBindViewHolder()", "Loading Artwork.");
        } else {
            holder.songThumb.setImageResource(R.drawable.song_image);
            Log.d("onBindViewHolder()", "Loading -Placeholder.");
        }
    }

    @Override
    public int getItemCount() {
        //Log.d("getItemCount()", "size: " + musicAdapter.size());
        return musicAdapter.size();
    }

    private byte[] getAlbumArt(String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        byte[] art = null;
        try{
            retriever.setDataSource(uri.toString());
            if(retriever.getEmbeddedPicture() != null) {
                art = retriever.getEmbeddedPicture();
            }
            retriever.release();
            return art;
        } catch (Exception e){
            Drawable d = this.context.getDrawable(R.drawable.song_image);
            Bitmap bitmap = ((BitmapDrawable)d).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        }
    }

    private String getTimeFormatted(long milliSeconds) {
        String finalTimerString = "";
        String secondsString;

        int hours = (int) (milliSeconds / 3600000);
        int minutes = (int) (milliSeconds % 3600000) / 60000;
        int seconds = (int) ((milliSeconds % 3600000) % 60000 / 1000);

        if (hours > 0)
            finalTimerString = hours + ":";

        if (seconds < 10)
            secondsString = "0" + seconds;
        else
            secondsString = "" + seconds;

        finalTimerString = finalTimerString + minutes + ":" + secondsString;
        return finalTimerString;
    }
}