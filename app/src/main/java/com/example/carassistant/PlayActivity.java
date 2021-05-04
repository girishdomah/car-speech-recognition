package com.example.carassistant;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.concurrent.TimeUnit;

public class PlayActivity extends AppCompatActivity {
    private ImageView imageViewSong;
    private TextView textViewName;
    private TextView textViewStart;
    private SeekBar seekBar;
    private TextView textViewEnd;
    private ImageView imageViewRewind;
    private ImageView imageViewPlay;
    private ImageView imageViewPause;
    private ImageView imageViewForward;

    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        imageViewSong = findViewById(R.id.image_view_song);
        textViewName = findViewById(R.id.text_view_name);
        textViewStart = findViewById(R.id.text_view_start);
        seekBar = findViewById(R.id.seek_bar);
        textViewEnd = findViewById(R.id.text_view_end);
        imageViewRewind = findViewById(R.id.image_view_rewind);
        imageViewPlay = findViewById(R.id.image_view_play);
        imageViewPause = findViewById(R.id.image_view_pause);
        imageViewForward = findViewById(R.id.image_view_forward);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String imageUrl = intent.getStringExtra("imageUrl");
        String fileUrl = intent.getStringExtra("fileUrl");

        textViewName.setText(name);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference gsImageRef = storage.getReferenceFromUrl(imageUrl);
        gsImageRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(imageViewSong);
                    }
                });

        StorageReference gsFileRef = storage.getReferenceFromUrl(fileUrl);
        gsFileRef.getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        mediaPlayer = MediaPlayer.create(PlayActivity.this, uri);

                        int duration = mediaPlayer.getDuration();
                        String stringDuration = convert(duration);
                        textViewEnd.setText(stringDuration);

                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                imageViewPause.setVisibility(View.GONE);
                                imageViewPlay.setVisibility(View.VISIBLE);
                                mediaPlayer.seekTo(0);
                            }
                        });

                        // AutoPlay
                        imageViewPlay.setVisibility(View.GONE);
                        imageViewPause.setVisibility(View.VISIBLE);
                        mediaPlayer.start();
                        seekBar.setMax(duration);
                        handler.postDelayed(runnable, 0);
                    }
                });

        runnable = new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 500);
            }
        };

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }

                textViewStart.setText(convert(mediaPlayer.getCurrentPosition()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        imageViewRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = mediaPlayer.getCurrentPosition();

                if (mediaPlayer.isPlaying() && currentPosition > 5000) {
                    currentPosition -= 5000;
                    textViewStart.setText(convert(currentPosition));
                    mediaPlayer.seekTo(currentPosition);
                }
            }
        });

        imageViewPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewPlay.setVisibility(View.GONE);
                imageViewPause.setVisibility(View.VISIBLE);
                mediaPlayer.start();
                seekBar.setMax(mediaPlayer.getDuration());
                handler.postDelayed(runnable, 0);
            }
        });

        imageViewPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewPlay.setVisibility(View.VISIBLE);
                imageViewPause.setVisibility(View.GONE);
                mediaPlayer.pause();
                handler.removeCallbacks(runnable);
            }
        });

        imageViewForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int duration = mediaPlayer.getDuration();

                if (mediaPlayer.isPlaying() && duration != currentPosition) {
                    currentPosition += 5000;
                    textViewStart.setText(convert(currentPosition));
                    mediaPlayer.seekTo(currentPosition);
                }
            }
        });
    }

    // Convert milliseconds to minutes and seconds
    @SuppressLint("DefaultLocale")
    private String convert(int duration) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }
}