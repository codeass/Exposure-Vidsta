package nz.co.delacour.exposurevideoplayersample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import nz.co.delacour.exposurevideoplayer.ExposureVideoPlayer;
import nz.co.delacour.exposurevideoplayer.VideoListeners;

public class MainActivity extends AppCompatActivity implements VideoListeners {

    ExposureVideoPlayer evp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        evp = (ExposureVideoPlayer) findViewById(R.id.evp);
        evp.init(this);

        //evp.setSource("http://trailers.apple.com/movies/wb/live-by-night/live-by-night-trailer-1_h720p.mov");//You can set the video source here or back in the layout xml.
        evp.setSource("android.resource://" + getPackageName() + "/"+R.raw.big_buck_bunny);
        evp.setFullScreen(false);
        evp.setOnVideoListeners(this);

        //If you haven't set autoplay to true you can with start the video with one of these,
        //evp.start();
        //Or you can wait for the user to click the play button on screen.

    }



    @Override
    public void OnVideoStarted(ExposureVideoPlayer evp) {
        Log.e("Video ", "Started");

    }

    @Override
    public void OnVideoPaused(ExposureVideoPlayer evp) {
        Log.e("Video ", "Paused");

    }

    @Override
    public void OnVideoStopped(ExposureVideoPlayer evp) {
        Log.e("Video ", "Stopped");

    }

    @Override
    public void OnVideoFinished(ExposureVideoPlayer evp) {
        Log.e("Video ", "Completed");
    }

    @Override
    public void OnVideoBuffering(ExposureVideoPlayer evp, int buffPercent) {

    }

    @Override
    public void OnVideoError(Exception err) {

    }

    @Override
    public void OnVideoRestart(ExposureVideoPlayer player) {

    }
}