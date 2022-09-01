package com.panniku.mp3player;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.panniku.mp3player.Overlay.OverlayPlayer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class OverlayPlaylistsEditor extends AppCompatActivity {

    EditText editorInput;
    ImageView playlistThumb;
    Button editorConfirm;

    Bitmap toSetThumb;

    static String ifSetTitle;

    public static final int PICK_IMAGE = 1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_editor_activity);

        if(ifSetTitle != null){
            getSupportActionBar().setTitle(ifSetTitle);
        }

        editorInput = findViewById(R.id.editorEnterText);
        playlistThumb = findViewById(R.id.editorOpenGallery);
        editorConfirm = findViewById(R.id.editorConfirm);

        playlistThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        editorConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editorInput.getText().toString();
                if(editorInput.getText().toString().length() == 0){
                    name = "My Playlist";
                }
                Date date = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                String formattedDate = df.format(date);
                OverlayPlayer.addPlaylist(toSetThumb, name, formattedDate);
                finish();
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            Log.d("onActivityResult: ", "Result is OK");
            if(data != null){
                Uri image = data.getData();
                playlistThumb.setImageURI(image);
                // TODO make this compress files
                try {
                    toSetThumb = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);
                } catch (IOException e) {
                    Toast.makeText(this, "File size is too big!", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

            } else {
                toSetThumb = BitmapFactory.decodeResource(this.getResources(), R.drawable.song_image);
            }
        }
    }

    private void openGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    public static void setAction(String name){
        ifSetTitle = name;
    }

}
