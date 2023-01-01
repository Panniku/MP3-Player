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

public class SelectorRecyclerViewAdapter extends RecyclerView.Adapter<SelectorRecyclerViewAdapter.SelectorViewHolder>{

    private Context context;
    private ArrayList<SongsConstructor> songsConstructor;

    private static ArrayList<SongsConstructor> selectedSongs = new ArrayList<>();
    private static ArrayList<String> selectedSongsString = new ArrayList<>();

    private OnRecyclerViewClick mOnRecyclerViewClick;

    static ImageView toSendIcon;
    static ImageView toSetIcon;

    public interface OnRecyclerViewClick {
        void OnItemClick(View view, int position);
    }


    public SelectorRecyclerViewAdapter(Context context, ArrayList<SongsConstructor> songsConstructor, OnRecyclerViewClick listener){

        this.context = context;
        this.songsConstructor = songsConstructor;
        mOnRecyclerViewClick = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    public class SelectorViewHolder extends RecyclerView.ViewHolder {

        private ImageView songSelectIcon;

        private TextView songName, songArtist, songDuration;
        private ImageView songThumb;

        public SelectorViewHolder(View itemView) {
            super(itemView);
            songName = itemView.findViewById(R.id.songSelectorName);
            songArtist = itemView.findViewById(R.id.songSelectorArtist);
            songDuration = itemView.findViewById(R.id.songSelectorDuration);
            songThumb = itemView.findViewById(R.id.songSelectorThumb);

            songSelectIcon = itemView.findViewById(R.id.songSelectorSelectedIcon);

            //Log.d("viewHolder()", "viewHolder: mListener:" + listener);

        }
    }

    @NonNull
    @Override
    public SelectorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("onCreateViewHolder: ", "CREATED");
        View view = LayoutInflater.from(context).inflate(R.layout.song_selector_item, parent, false);

        SelectorViewHolder viewHolder = new SelectorViewHolder(view);

        viewHolder.songSelectIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnRecyclerViewClick.OnItemClick(view, viewHolder.getAdapterPosition());
                if(selectedSongs.contains(songsConstructor.get(viewHolder.getAdapterPosition()))){
                    viewHolder.songSelectIcon.setImageResource(R.drawable.selector_unchecked);
                    //selectedSongsString.remove(viewHolder.getAdapterPosition());
                    selectedSongs.remove(songsConstructor.get(viewHolder.getAdapterPosition()));
                } else {
                    viewHolder.songSelectIcon.setImageResource(R.drawable.selector_checked);
                    //selectedSongsString.add(songsConstructor.get(viewHolder.getAdapterPosition()).getTitle());
                    selectedSongs.add(songsConstructor.get(viewHolder.getAdapterPosition()));
                }

            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull SelectorViewHolder holder, int position) {
        holder.songName.setText(songsConstructor.get(position).getTitle());
        holder.songArtist.setText(songsConstructor.get(position).getArtist());
        holder.songDuration.setText(Utils.getTimeFormatted(songsConstructor.get(position).getDuration()));

//        Uri songArt = Utils.getAlbumArt(position);
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
        //Log.d("getItemCount()", "size: "  + musicAdapter.size());
        return songsConstructor.size();
    }

    public static ArrayList<SongsConstructor> getSelectedSongs() {
        return selectedSongs;
    }

    public static ArrayList<String> getSelectedSongsDEBUG() {
        return selectedSongsString;
    }

    public static ImageView getIcon(){
        return toSendIcon;
    }

    public static void setIcon(int res){
        //
    }
}