package nz.co.delacour.exposurevideoplayersample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import nz.co.delacour.exposurevideoplayer.ExposureThumbnailView;
import nz.co.delacour.exposurevideoplayer.ExposureVideoPlayer;
import nz.co.delacour.exposurevideoplayer.OnThumbnailClickListener;

public class MainActivity extends AppCompatActivity {

    ExposureVideoPlayer evp;
    ExposureThumbnailView etv;
    String videoSource = "http://www.quirksmode.org/html5/videos/big_buck_bunny.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //evp = (ExposureVideoPlayer) findViewById(R.id.evp);
        //evp.init(this);
        // This allows for a better fullscreen setting, If you don't have this when the user clicks the notification bar it pops out of fullscreen.
        // This will be fixed later but currently its going to stay like this.
        //evp.setVideoSource(videoSource);
        // Set video source from raw source, evp.setVideoSource("android.resource://" + getPackageName() + "/"+R.raw.big_buck_bunny);
        //evp.setOnVideoListeners(this);
        // If you haven't set autoplay to true you can with start the video with one of these,
        // evp.start();
        // Or you can wait for the user to click the play button on screen.


        // If you want to use just the thumbnail which uses the first frame of the video as an place holder thumbnail image.
        etv = (ExposureThumbnailView) findViewById(R.id.etv);
        etv.setVideoSource(videoSource);
        etv.setAutoPlay(true);
        etv.setFullScreen(false);
        etv.disableStandalonePlayer(true);
        etv.setOnThumbnailClickListener(new OnThumbnailClickListener() {
            @Override
            public void onClick() {
                Log.e("Thumbnail: ", "Clicked");
                // Add your own on clicks methods here.
                // Start activity with video player etc.
            }
        });
    }

}