package com.panniku.mp3player;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
//import com.panniku.mp3player.Adapters.SelectorListViewAdapter;
import com.panniku.mp3player.Adapters.SelectorRecyclerViewAdapter;
import com.panniku.mp3player.Constructors.SongsConstructor;
import com.panniku.mp3player.Overlay.OverlayPlayer;

import java.util.ArrayList;

public class OverlaySongsSelector extends AppCompatActivity {

    private static ArrayList<SongsConstructor> selectorUriArrayList;
    private static SelectorRecyclerViewAdapter selectorRecyclerViewAdapter;
    //private static SelectorListViewAdapter selectorListViewAdapter;
    private RecyclerView selectorRecyclerView;

    static boolean isChecked = false;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songs_selector);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Select songs");

        selectorUriArrayList = OverlayPlayer.getAllSongs(this, false);
        selectorRecyclerView = findViewById(R.id.songSelectorRecyclerView);
        if(selectorUriArrayList.size() > 0){
//            selectorListViewAdapter = new SelectorListViewAdapter(this, R.layout.song_selector_item, selectorUriArrayList);
//            selectorListView.setAdapter(selectorListViewAdapter);

            selectorRecyclerViewAdapter = new SelectorRecyclerViewAdapter(this, selectorUriArrayList, new SelectorRecyclerViewAdapter.OnRecyclerViewClick() {
                @Override
                public void OnItemClick(View view, int position) {
                    if(!isChecked){
                        isChecked = true;
                    } else {
                        isChecked = false;
                    }
                }

            });
            //selectorRecyclerViewAdapter.setHasStableIds(true);
            selectorRecyclerView.setNestedScrollingEnabled(false);
            selectorRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            selectorRecyclerView.setAdapter(selectorRecyclerViewAdapter);
            selectorRecyclerViewAdapter.notifyDataSetChanged();



        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_select:
                Log.d("onOptionsItemSelected: ", "_: " + SelectorRecyclerViewAdapter.getSelectedSongsDEBUG());
                OverlayPlayer.updateSecondaryRecyclerView(this, SelectorRecyclerViewAdapter.getSelectedSongs());
                finish();
        }
        return super.onOptionsItemSelected(item);
    }



    public static boolean getIfChecked(){
        return isChecked;
    }
}