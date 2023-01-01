package com.panniku.mp3player.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.panniku.mp3player.Constructors.SongsConstructor;
import com.panniku.mp3player.R;
import com.panniku.mp3player.Utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SecondaryAdapter extends RecyclerView.Adapter<SecondaryAdapter.SecondaryViewHolder> {

    private Context context;
    private ArrayList<SongsConstructor> songsConstructor;

    private SelectorRecyclerViewAdapter.OnRecyclerViewClick mOnRecyclerViewClick;

    public SecondaryAdapter(Context context, ArrayList<SongsConstructor> songsConstructor, SelectorRecyclerViewAdapter.OnRecyclerViewClick listener){

        this.context = context;
        this.songsConstructor = songsConstructor;
        mOnRecyclerViewClick = listener;
    }

    public class SecondaryViewHolder extends RecyclerView.ViewHolder {

        private TextView songName, songArtist, songDuration;
        private ImageView songThumb;

        public SecondaryViewHolder(View itemView) {
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
    public SecondaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_list_item, parent, false);

        SecondaryViewHolder viewHolder = new SecondaryViewHolder(view);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnRecyclerViewClick.OnItemClick(view, viewHolder.getAdapterPosition());
            }
        });

        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull SecondaryViewHolder holder, int position) {
        holder.songName.setText(songsConstructor.get(position).getTitle());
        holder.songArtist.setText(songsConstructor.get(position).getArtist());
        holder.songDuration.setText(Utils.getTimeFormatted(songsConstructor.get(position).getDuration()));

        Log.d("onBingViewHolder POSITION", "" + Integer.getInteger(String.valueOf(songsConstructor.get(position))));

//        Uri songArt = Utils.getAlbumArt(Integer.getInteger(String.valueOf(songsConstructor.get(position))));
//        if(songArt != null){
//            //holder.songThumb.setImageBitmap(bitmap);
//            Picasso.get().load(songArt).into(holder.songThumb);
//        } else {
//            Picasso.get().load(R.drawable.song_image).into(holder.songThumb);
//        }

        Bitmap bitmap = Utils.getAlbumArt(songsConstructor.get(position).getPath());
        if(bitmap != null){
            holder.songThumb.setImageBitmap(bitmap);
        } else {
            holder.songThumb.setImageResource(R.drawable.song_image);
        }

        holder.songName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        holder.songName.setSelected(true);

        holder.songArtist.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        holder.songArtist.setSelected(true);
    }

    @Override
    public int getItemCount() {
        if(songsConstructor != null){
            return songsConstructor.size();
        } else{
            return 0;
        }
    }
}
