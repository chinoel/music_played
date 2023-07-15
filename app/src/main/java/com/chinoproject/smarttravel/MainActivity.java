package com.chinoproject.smarttravel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaDrm;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    ArrayList<File> music = new ArrayList<>();
    MediaPlayer mMediaPlayer = new MediaPlayer();
    SeekBar progress;

    int musiclist = 0;
    private Context mContext;
    private NativeAd mNativeAd;


    private boolean music_parse;

    TextView nowtime;
    TextView maxtime;

    TextView Musicname;
    TextView MusicArtist;
    TextView MusicAll;

    String mut;
    String sec;

    ImageView play;

    MediaMetadataRetriever source = new MediaMetadataRetriever();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        loadNativeAd();

        int permission1 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission2 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);


        nowtime = findViewById(R.id.nowtime);
        maxtime = findViewById(R.id.maxtime);

        Musicname = findViewById(R.id.music_name);
        MusicArtist = findViewById(R.id.music_artist);
        MusicAll = findViewById(R.id.music_all);

        progress = findViewById(R.id.progress);
        progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    mMediaPlayer.seekTo(i);
                    timeset(i, 1);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(mMediaPlayer.isPlaying()) {

                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });




        // Data User Permission By load & send
        if (permission1 == PackageManager.PERMISSION_DENIED || permission2 == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        new String[]
                                {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
                return;
            }
        }



        // Music Play check Local
        ImageView btn = findViewById(R.id.start);
        btn.setOnClickListener(v -> {
                music = test();
                for (int i = 0; i < music.size(); i++) {
                    Log.d(i + "", music.get(i) + "");
                }
        });




        play = findViewById(R.id.played);
        play.setOnClickListener(v -> {
            isPlaying();

            mMediaPlayer.setOnCompletionListener(mp -> {
                        nextmusic();
            });
        });



        ImageView block = findViewById(R.id.left);
        block.setOnClickListener(v -> {
            if (3000 < mMediaPlayer.getCurrentPosition()) {
                mMediaPlayer.seekTo(0);
                progress.setProgress(0);
            }
            else {
                if (musiclist == 1) {
                    musiclist = music.size() - 1;
                }
                else {
                    musiclist = musiclist - 2;
                }
                nextmusic();
            }
        });



        ImageView next_r = findViewById(R.id.right);
        next_r.setOnClickListener(v -> {
            if (musiclist >= music.size()) {
                musiclist = 0;
            }
            nextmusic();
        });

    }


    public void nextmusic() {
        try {
            if (musiclist >= music.size()) {
                musiclist = 0;
            } else {
                mMediaPlayer.pause();
                mMediaPlayer.reset();
                String filepath = music.get(musiclist) + "";

                source.setDataSource(filepath);
                Musicname.setText(source.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
                MusicArtist.setText(source.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));

                String[] splits = filepath.split("/");
                MusicAll.setText(splits[splits.length - 1]);

                mMediaPlayer.setDataSource(filepath);
                mMediaPlayer.prepare();
                mMediaPlayer.start();


                musiclist++;
                play.setImageResource(R.drawable.btn_parse);
                progress.setProgress(0);
                progress_thread();
                mMediaPlayer.setOnCompletionListener(mp -> {
                    nextmusic();
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void isPlaying() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            music_parse = true;
            play.setImageResource(R.drawable.btn_play);
        } else if (music_parse) {
            mMediaPlayer.start();
            music_parse = false;
            play.setImageResource(R.drawable.btn_parse);
            progress_thread();
        } else {
            if (music.size() > musiclist) {
                try {
                    String filepath = music.get(musiclist) + "";
                    mMediaPlayer.setDataSource(filepath);

                    source.setDataSource(filepath);
                    Musicname.setText(source.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
                    MusicArtist.setText(source.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));

                    String[] splits = filepath.split("/");
                    MusicAll.setText(splits[splits.length - 1]);

                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                    musiclist++;
                    progress_thread();
                    music_parse = false;
                    play.setImageResource(R.drawable.btn_parse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "재생 목록이 없음", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void progress_thread() {
        // Progress Setting Max Length
        progress.setMax(mMediaPlayer.getDuration());
        timeset(mMediaPlayer.getDuration(), 0);
        thread_progress();
    }

    public void thread_progress() {
        new Thread(() -> {
            while (mMediaPlayer.isPlaying()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(() -> progress.setProgress(mMediaPlayer.getCurrentPosition()));
                runOnUiThread(() -> timeset(mMediaPlayer.getCurrentPosition(), 1));
            }

        }).start();
    }

    public void timeset(int i, int v) {
        if ((i / 60000) < 10) {
            mut = (i / 60000) + "";
        }
        else {
            mut = "0" + (i / 60000);
        }
        if ((i % 60000) < 10000) {
            sec = "0" + (i % 60000) / 1000;
        }
        else {
            sec = (i % 60000) / 1000 + "";
        }
        if (v == 0) {
            maxtime.setText(mut + ":" + sec);
        }
        else if (v == 1) {
            nowtime.setText(mut + ":" + sec);
        }
    }

    public ArrayList<File> test() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        ArrayList<File> arrayList = new ArrayList<>();
        File[] files = dir.listFiles();

        if (files != null) {
            for (File singlefile : files) {
                if (singlefile.getName().endsWith(".mp3") || singlefile.getName().endsWith(".wav")) {
                    arrayList.add(singlefile);
                }
            }
        }
        return arrayList;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1000) {
            boolean check_rewsult = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_rewsult = false;
                    break;
                }
            }

            if (check_rewsult == true) {

            }
            else {
                Toast.makeText(this, "권한에 동의하지 않는 경우 앱을 이용하실 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // User Native Ads C-2
    private void loadNativeAd() {
        AdLoader.Builder adBuilder = new AdLoader.Builder(mContext, getResources().getString(R.string.admob_native_ad_id));

        adBuilder.forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
            @Override
            public void onNativeAdLoaded(@NonNull NativeAd nativeAd) {
                if (isDestroyed() || isFinishing() || isChangingConfigurations()) {
                    nativeAd.destroy();
                    return;
                }

                if (mNativeAd != null) {
                    mNativeAd.destroy();
                }

                mNativeAd = nativeAd;

                FrameLayout adFrameLayout = findViewById(R.id.frameNativeAd);
                NativeAdView adView =
                        (NativeAdView) getLayoutInflater().inflate(R.layout.ad_native, null);
                populateNativeAdView(nativeAd, adView);
                adFrameLayout.removeAllViews();
                adFrameLayout.addView(adView);
            }
        });

        VideoOptions videoOptions =
                new VideoOptions.Builder().setStartMuted(true).build();
        NativeAdOptions nativeAdOptions =
                new NativeAdOptions.Builder().setVideoOptions(videoOptions).build();

        AdLoader adLoader = adBuilder.withAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                super.onAdClicked();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Toast.makeText(mContext, "ad load err: " + loadAdError, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Toast.makeText(mContext, "ad loaded", Toast.LENGTH_SHORT).show();
            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        // Set the media view.
        adView.setMediaView(adView.findViewById(R.id.ad_media));

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) Objects.requireNonNull(adView.getAdvertiserView())).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd);
    }
}