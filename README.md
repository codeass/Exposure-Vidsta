<a href='https://bintray.com/delacour/maven/exposurevideoplayer/_latestVersion'><img src='https://api.bintray.com/packages/delacour/maven/exposurevideoplayer/images/download.svg'></a>

<a href='https://bintray.com/delacour/maven/exposurevideoplayer?source=watch' alt='Get automatic notifications about new "exposurevideoplayer" versions'><img src='https://www.bintray.com/docs/images/bintray_badge_color.png'></a>

####Note that this Repo is still in development. 
####So if you find any bugs or things that dont work the way they should please report them, 
####Thank you.

# Exposure-Video-Player
####Custom Android video player API library. Simple and integrate with your apps quickly and efficiently.


<p align="center">
<img src="https://github.com/UrbanChrisy/Exposure-Video-Player/blob/master/screenshots/screenshot_1.png" height="640px" width="360px">
<img src="https://github.com/UrbanChrisy/Exposure-Video-Player/blob/master/screenshots/screenshot_2.png" height="640px" width="360px"> </p>





#Feature List:
####-UI is very similar to what youtube uses as such.
####-Simple and easy to use and setup.
####-Allot of feature to be still implemented.
####-Made around the base MediaPlayer API. So it can play anything and on most devices.

#Module Dependency
####Add the following dependancy to your module build.gradle file, then your set to go.
```Gradle
compile 'nz.co.delacour.exposure-core:exposurevideoplayer:1.0.1'
```
#Code Basic setup
####Firstly add this basic layout to get the video player. 
####Without changing any settings. The basic video player will have theses settings.
#####-Autoplay set to false (Disabled).
#####-Auto Hide of control buttons on play set to true (Enabled).
#####-Auto Fullscreen and Fullscreen set to true (Enabled).
#####-Color tint of play and pause button set to white. (Default)


```XML
<nz.co.delacour.exposurevideoplayer.ExposureVideoPlayer
    android:id="@+id/evp"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

###Then now in you layout find the view.

```Android
 public class MainActivity extends AppCompatActivity implements VideoListeners {

    ExposureVideoPlayer evp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        evp = (ExposureVideoPlayer) findViewById(R.id.evp);
        evp.setSource("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
        //You can set the video source here or back in the layout xml.
        evp.setOnVideoListeners(this);
        //Video Listeners if you want them. Someof these are still being worked out so some of them dont work fully yet. My bad :)
        //Also if you do use the video listeners this I would highly recommend implmenting the VideoListeners to the base class. 
        //Just makes it a whole allot easier

        //If you haven't set autoplay to true you can with start the video with evp.start();
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
    public void OnVideoBuffering(ExposureVideoPlayer exposureVideoPlayer) {
        Log.e("Video ", "Buffering");
    }

}
```


#Things to be added and/or finished in given time.
#####-Video Listeners
#####-Autoplay
#####-Automatically resize, little broken at the moment.
#####-Thumbnail view.
#####-Batch preloading.
#####-Ability to load next video.
#####-Override mediaplayer buffer to sort out data saver mode.
#####-Ability to take away seekbar and timers.
#####-Allot more to come also.


