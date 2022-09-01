//package com.panniku.mp3player.Adapters;
//
//import android.annotation.SuppressLint;
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.os.AsyncTask;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.ImageView;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import com.panniku.mp3player.Constructors.SongsConstructor;
//import com.panniku.mp3player.R;
//import com.panniku.mp3player.Utils.Utils;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class SelectorListViewAdapter extends ArrayAdapter<SongsConstructor> {
//
//    Context mContext;
//    int mRes;
//
//    public SelectorListViewAdapter(Context context, int resource, ArrayList<SongsConstructor> objects) {
//        super(context, resource, objects);
//        this.mContext = context;
//        this.mRes = resource;
//    }
//
//
//    public class ViewHolder {
//        TextView songTitle, songArtist, songDuration;
//        ImageView songArtwork;
//    }
//
//    @SuppressLint("StaticFieldLeak")
//    @NonNull
//    @Override
//    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        String title = getItem(position).getTitle();
//        String artist = getItem(position).getArtist();
//        String duration = Utils.getTimeFormatted(getItem(position).getDuration());
//        String path = getItem(position).getPath();
//
//        LayoutInflater inflater = LayoutInflater.from(mContext);
//        convertView = inflater.inflate(mRes, parent, false);
//
//        ViewHolder holder = new ViewHolder();
//        holder.songTitle = convertView.findViewById(R.id.songSelectorName);
//        holder.songArtist = convertView.findViewById(R.id.songSelectorArtist);
//        holder.songDuration = convertView.findViewById(R.id.songSelectorDuration);
//        holder.songArtwork = convertView.findViewById(R.id.songSelectorThumb);
//        convertView.setTag(holder);
//
//        holder.songTitle.setText(title);
//        holder.songArtist.setText(artist);
//        holder.songDuration.setText(duration);
//
//        new AsyncTask<ViewHolder, Void, Bitmap>() {
//            private ViewHolder v;
//
//            @Override
//            protected Bitmap doInBackground(ViewHolder... params) {
//                v = params[0];
//                return mFakeImageLoader.getImage();
//            }
//
//            @Override
//            protected void onPostExecute(Bitmap result) {
//                super.onPostExecute(result);
//                if (v.position == position) {
//                    // If this item hasn't been recycled already, hide the
//                    // progress and set and show the image
//                    v.progress.setVisibility(View.GONE);
//                    v.icon.setVisibility(View.VISIBLE);
//                    v.icon.setImageBitmap(result);
//                }
//            }
//        }.execute(holder);
//
//        Bitmap bitmap = Utils.getAlbumArt(getItem(position).getPath());
//        if(bitmap != null){
//            holder.songArtwork.setImageBitmap(bitmap);
//        } else {
//            holder.songArtwork.setImageResource(R.drawable.song_image);
//        }
//
//        return convertView;
//    }
//}
