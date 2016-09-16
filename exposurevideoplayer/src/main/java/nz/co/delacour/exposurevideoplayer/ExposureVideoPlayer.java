package nz.co.delacour.exposurevideoplayer;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rey.material.widget.LinearLayout;
import com.rey.material.widget.ProgressView;

import java.io.IOException;

/**
 * Created by Chris on 11-Sep-16.
 */
public class ExposureVideoPlayer extends FrameLayout implements TextureView.SurfaceTextureListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnVideoSizeChangedListener, View.OnClickListener,
        SeekBar.OnSeekBarChangeListener, MediaPlayer.OnErrorListener {

    private TextureView textureView;
    private MediaPlayer videoPlayer;
    //private View vcv;
    private View controlPlayPause;
    private View controlSeekBar;
    private View videoLoadingView;
    private SeekBar seekBarDuration;
    private ImageButton imgBtnPlayPause;
    private ImageButton imgBtnFullScreenToggle;
    private TextView tvPosition;
    private TextView tvDuration;
    private ProgressView proViewVideoLoading;

    private Drawable playVideoDrawable;
    private Drawable pauseVideoDrawable;
    private Drawable retryVideoDrawable;
    private Drawable nextVideoDrawable;
    private Drawable previousVideoDrawable;
    private Drawable enterFullScreenDrawable;
    private Drawable exitFullScreenDrawable;

    private VideoListeners videoListener;
    private LayoutStates.OnLayoutCreated onLayoutCreated;
    private LayoutStates.OnLayoutResumed onLayoutResumed;
    private LayoutStates.OnLayoutPaused onLayoutPaused;
    private LayoutStates.OnLayoutDestroyed onLayoutDestroyed;
    private FullScreenClickListener fullscreenToggleClickListener;

    private Surface surface;
    private Uri videoSource;
    //private boolean hideControlsOnPlay;
    private boolean autoPlay;
    private boolean wasPlaying;
    private int initialHeight;
    private int initialWidth;
    private int videoDuration;
    private boolean isFullScreen = false;
    private boolean isSetFullScreen;

    private Handler handler;
    private Configuration layoutConfig;
    private int buttonTintColor = 0;
    private Activity baseAct;
    private boolean setFullScreenButtonEnabled;
    private int orientationBeforeFullScreen;


    public ExposureVideoPlayer(Context context) {
        super(context);
        init(context, null);
    }

    public ExposureVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ExposureVideoPlayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setBackgroundColor(Color.BLACK);

        if (attrs != null) {
            TypedArray customAttr = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ExposureVideoPlayer, 0, 0);
            try {
                String s = customAttr.getString(R.styleable.ExposureVideoPlayer_videoSource);
                if (s != null && !s.trim().isEmpty()) videoSource = Uri.parse(s);

                //hideControlsOnPlay = customAttr.getBoolean(R.styleable.ExposureVideoPlayer_hideControlsOnPlay, true);
                autoPlay = customAttr.getBoolean(R.styleable.ExposureVideoPlayer_autoPlay, false);
                setFullScreenButtonEnabled = customAttr.getBoolean(R.styleable.ExposureVideoPlayer_fullScreenButtonEnabled, true);
                isSetFullScreen = customAttr.getBoolean(R.styleable.ExposureVideoPlayer_setFullScreen, false);
                isFullScreen = isSetFullScreen;

                buttonTintColor = customAttr.getColor(R.styleable.ExposureVideoPlayer_buttonTintColor, ContextCompat.getColor(getContext(), R.color.colorPrimaryText));

                playVideoDrawable = customAttr.getDrawable(R.styleable.ExposureVideoPlayer_playVideoDrawable);
                pauseVideoDrawable = customAttr.getDrawable(R.styleable.ExposureVideoPlayer_pauseVideoDrawable);
                retryVideoDrawable = customAttr.getDrawable(R.styleable.ExposureVideoPlayer_retryVideoDrawable);
                nextVideoDrawable = customAttr.getDrawable(R.styleable.ExposureVideoPlayer_nextVideoDrawable);
                previousVideoDrawable = customAttr.getDrawable(R.styleable.ExposureVideoPlayer_previousVideoDrawable);
            } finally {
                customAttr.recycle();
            }
        } else {
            autoPlay = false;
            isSetFullScreen = false;
            setFullScreenButtonEnabled = true;
            buttonTintColor = ContextCompat.getColor(getContext(), R.color.colorPrimaryText);
        }

        if (playVideoDrawable == null)
            playVideoDrawable = ContextCompat.getDrawable(context, R.drawable.video_play);
        if (pauseVideoDrawable == null)
            pauseVideoDrawable = ContextCompat.getDrawable(context, R.drawable.video_pause);
        if (retryVideoDrawable == null)
            retryVideoDrawable = ContextCompat.getDrawable(context, R.drawable.video_retry);
        //if (nextVideoDrawable == null)
        //    nextVideoDrawable = ContextCompat.getDrawable(context, R.drawable.video_next);
        //if (previousVideoDrawable == null)
        //    previousVideoDrawable = ContextCompat.getDrawable(context, R.drawable.video_previous);

        if (enterFullScreenDrawable == null)
            enterFullScreenDrawable = ContextCompat.getDrawable(context, R.drawable.video_screen_fullscreen_enter);
        if (exitFullScreenDrawable == null)
            exitFullScreenDrawable = ContextCompat.getDrawable(context, R.drawable.video_screen_fullscreen_exit);
    }

    public void start() {
        if (videoPlayer == null) return;
        videoPlayer.start();
        imgBtnPlayPause.setImageDrawable(pauseVideoDrawable);
        handler.post(seekBarProgress);
        videoListener.OnVideoStarted(this);
    }

    public void pause() {
        if (videoPlayer.isPlaying()) {
            videoPlayer.pause();
            imgBtnPlayPause.setImageDrawable(playVideoDrawable);
            handler.removeCallbacks(seekBarProgress);
        }
        videoListener.OnVideoPaused(this);
    }

    public void stop() {
        videoPlayer.stop();
        imgBtnPlayPause.setImageDrawable(playVideoDrawable);
        handler.removeCallbacks(seekBarProgress);
        videoListener.OnVideoStopped(this);
    }

    public void restart() {
        videoPlayer.stop();
        if (autoPlay) {
            videoPlayer.start();
            imgBtnPlayPause.setImageDrawable(pauseVideoDrawable);
        } else {
            imgBtnPlayPause.setImageDrawable(playVideoDrawable);

        }
        handler.removeCallbacks(seekBarProgress);
        videoListener.OnVideoRestart(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        handler = new Handler();
        videoPlayer = new MediaPlayer();
        videoPlayer.setOnPreparedListener(this);
        videoPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        textureView = new TextureView(getContext());
        addView(textureView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout rl = new LinearLayout(getContext());
        addView(rl, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rl.setId(R.id.touchId);
        rl.setOnClickListener(this);

        LayoutInflater li = LayoutInflater.from(getContext());
        videoLoadingView = li.inflate(R.layout.video_loading_view, this, false);
        addView(videoLoadingView);

        controlPlayPause = li.inflate(R.layout.video_img_button_play_pause, this, false);
        controlSeekBar = li.inflate(R.layout.video_seek_bar, this, false);
        FrameLayout.LayoutParams lp1 = (FrameLayout.LayoutParams) controlPlayPause.getLayoutParams();
        FrameLayout.LayoutParams lp2 = (FrameLayout.LayoutParams) controlSeekBar.getLayoutParams();

        lp1.gravity = Gravity.CENTER;
        lp2.gravity = Gravity.BOTTOM;

        addView(controlPlayPause, lp1);
        addView(controlSeekBar, lp2);

        imgBtnPlayPause = (ImageButton) controlPlayPause.findViewById(R.id.imageButtonPlayPauseRetry);
        imgBtnFullScreenToggle = (ImageButton) controlSeekBar.findViewById(R.id.imageButtonFullScreenToggle);
        tvPosition = (TextView) controlSeekBar.findViewById(R.id.textViewPosition);
        tvDuration = (TextView) controlSeekBar.findViewById(R.id.textViewDuration);
        proViewVideoLoading = (ProgressView) videoLoadingView.findViewById(R.id.proViewVideoLoading);
        seekBarDuration = (SeekBar) controlSeekBar.findViewById(R.id.seekBarDuration);
        imgBtnPlayPause.setImageDrawable(playVideoDrawable);

        imgBtnPlayPause.setOnClickListener(this);
        imgBtnFullScreenToggle.setOnClickListener(this);
        textureView.setSurfaceTextureListener(this);
        seekBarDuration.setOnSeekBarChangeListener(this);
        controlPlayPause.setVisibility(INVISIBLE);
        controlSeekBar.setVisibility(INVISIBLE);
        proViewVideoLoading.start();
        setUpVideoPlayer();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig == null) return;
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                Log.e("LANDSCAPE", "LANDSCAPE");
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                Log.e("PORTRAIT", "PORTRAIT");
                break;
        }
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        initialWidth = width;
        initialHeight = height;
        surface = new Surface(surfaceTexture);
        videoPlayer.setSurface(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
        adjustView(width, height, videoPlayer.getVideoWidth(), videoPlayer.getVideoHeight());
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        videoListener.OnVideoBuffering(this, i);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        videoListener.OnVideoFinished(this);
        handler.removeCallbacks(seekBarProgress);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        videoDuration = mediaPlayer.getDuration();
        seekBarDuration.setProgress(0);
        seekBarDuration.setMax(videoDuration);
        tvPosition.setText(ExposureVideoPlayerUtil.getTimeString(0, false));
        tvDuration.setText(ExposureVideoPlayerUtil.getTimeString(videoDuration, true));
        proViewVideoLoading.stop();
        proViewVideoLoading.setVisibility(INVISIBLE);
        removeView(videoLoadingView);
        videoPlayer.setOnBufferingUpdateListener(this);
        videoPlayer.setOnCompletionListener(this);
        videoPlayer.setOnErrorListener(this);
        videoPlayer.setOnVideoSizeChangedListener(this);

        if (autoPlay) {
            start();
        } else {
            controlPlayPause.setVisibility(VISIBLE);
            controlSeekBar.setVisibility(VISIBLE);
            start();
            pause();
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
        adjustView(initialWidth, initialHeight, width, height);
    }

    private void adjustView(int viewWidth, int viewHeight, int videoWidth, int videoHeight) {
        final double aspectRatio = (double) videoHeight / videoWidth;
        int newWidth, newHeight;

        if (viewHeight > (int) (viewWidth * aspectRatio)) {
            newWidth = viewWidth;
            newHeight = (int) (viewWidth * aspectRatio);
        } else {
            newWidth = (int) (viewHeight / aspectRatio);
            newHeight = viewHeight;
        }

        final int xoff = (viewWidth - newWidth) / 2;
        final int yoff = (viewHeight - newHeight) / 2;

        final Matrix txform = new Matrix();
        textureView.getTransform(txform);
        txform.setScale((float) newWidth / viewWidth, (float) newHeight / viewHeight);
        txform.postTranslate(xoff, yoff);
        textureView.setTransform(txform);
    }

    public void setSource(String str) {
        setSource(Uri.parse(str));
    }

    public void setSource(Uri uri) {
        videoSource = uri;
        setUpVideoPlayer();
    }

    private void setUpVideoPlayer() {
        if (videoPlayer == null || videoSource == null) return;
        videoPlayer.setSurface(surface);
        try {
            if (videoSource.getScheme() != null && (videoSource.getScheme().equals("http") || videoSource.getScheme().equals("https")))
                videoPlayer.setDataSource(videoSource.toString());
            else videoPlayer.setDataSource(getContext(), videoSource);
            videoPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (isSetFullScreen) {
            imgBtnFullScreenToggle.setVisibility(GONE);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tvDuration.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }

        if (buttonTintColor != ContextCompat.getColor(getContext(), R.color.colorPrimaryText)) {
            ColorDrawable dr = new ColorDrawable(buttonTintColor);
            playVideoDrawable.setColorFilter(dr.getColor(), PorterDuff.Mode.MULTIPLY);
            pauseVideoDrawable.setColorFilter(dr.getColor(), PorterDuff.Mode.MULTIPLY);
            enterFullScreenDrawable.setColorFilter(dr.getColor(), PorterDuff.Mode.MULTIPLY);
            exitFullScreenDrawable.setColorFilter(dr.getColor(), PorterDuff.Mode.MULTIPLY);
            imgBtnPlayPause.setImageDrawable(playVideoDrawable);
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.imageButtonPlayPauseRetry) {
            if (isPlaying()) {
                pause();
            } else {
                start();
                toggleControls();
            }
        } else if (id == R.id.touchId) {
            toggleControls();
        } else if (id == R.id.imageButtonFullScreenToggle) {
            if (isFullScreen) {
                onFullScreenToggleClick(false);
            } else {
                onFullScreenToggleClick(true);
            }
        }
    }

    public boolean isPlaying() {
        return (videoPlayer != null && videoPlayer.isPlaying());
    }

    public boolean controlsShowing() {
        return (controlPlayPause.getVisibility() == VISIBLE);
    }

    public void setOnVideoListeners(@NonNull VideoListeners callback) {
        videoListener = callback;
    }

    public void toggleControls() {
        if (videoPlayer == null) return;
        else if (controlsShowing()) {
            animateControls(controlPlayPause, 1f, 0f, INVISIBLE);
            animateControls(controlSeekBar, 1f, 0f, INVISIBLE);
        } else {
            animateControls(controlPlayPause, 0f, 1f, VISIBLE);
            animateControls(controlSeekBar, 0f, 1f, VISIBLE);
        }
    }


    public void animateControls(final View v, float f1, float f2, final int visibility) {
        v.animate().cancel();
        v.setAlpha(f1);
        v.setVisibility(VISIBLE);
        v.animate().alpha(f2)
                .setInterpolator(new DecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        v.setVisibility(visibility);
                    }
                }).start();
    }

    private void setFullScreenToggle(boolean isFullScreen) {
        this.isFullScreen = isFullScreen;
        if (isFullScreen) setFullScreen();
        else {
            exitFullScreen();
        }
    }

    public void setFullScreen(boolean isFullScreen) {
        this.isSetFullScreen = isFullScreen;
        this.isFullScreen = isFullScreen;
        if (isFullScreen) setFullScreen();
        else {
            exitFullScreen();
        }
    }


    private void exitFullScreen() {
        this.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        imgBtnFullScreenToggle.setImageDrawable(enterFullScreenDrawable);
        if (baseAct != null)
            if (orientationBeforeFullScreen == Configuration.ORIENTATION_PORTRAIT)
                baseAct.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            else baseAct.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    public void setFullScreen() {
        imgBtnFullScreenToggle.setImageDrawable(exitFullScreenDrawable);
        orientationBeforeFullScreen = getResources().getConfiguration().orientation;
        if (baseAct != null) {
            baseAct.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            baseAct.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            Toast.makeText(getContext(), "FullScreen will not work properly, as no Activity has been initialized.", Toast.LENGTH_LONG).show();
            this.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
        if (isSetFullScreen) {
            imgBtnFullScreenToggle.setVisibility(GONE);
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tvDuration.getLayoutParams();
            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        }
    }


    @Override
    public void onWindowSystemUiVisibilityChanged(int visible) {
        super.onWindowSystemUiVisibilityChanged(visible);

    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    public boolean autoPlayEnabled() {
        return autoPlay;
    }

    public void playVideoFrom(int i) {
        if (videoPlayer == null) return;
        videoPlayer.seekTo(i);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if (b) playVideoFrom(i);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        wasPlaying = isPlaying();
        if (wasPlaying) videoPlayer.pause();

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (wasPlaying) videoPlayer.start();
    }

    private final Runnable seekBarProgress = new Runnable() {
        @Override
        public void run() {
            if (videoPlayer == null || handler == null || seekBarDuration == null) return;
            int videoPos = videoPlayer.getCurrentPosition();
            int videoLength = videoPlayer.getDuration();

            tvPosition.setText(ExposureVideoPlayerUtil.getTimeString(videoPos, false));
            tvDuration.setText(ExposureVideoPlayerUtil.getTimeString((videoLength - videoPos), true));
            seekBarDuration.setProgress(videoPos);
            seekBarDuration.setMax(videoLength);
            handler.postDelayed(this, 100);
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            onLayoutResumed();
        } else {
            onLayoutPaused();
        }
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        onLayoutCreated();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        onLayoutDestroyed();
    }

    public void setOnLayoutCreatedListener(@NonNull LayoutStates.OnLayoutCreated onLayoutCreated) {
        this.onLayoutCreated = onLayoutCreated;
    }

    public void setOnLayoutResumedListener(@NonNull LayoutStates.OnLayoutResumed onLayoutResumed) {
        this.onLayoutResumed = onLayoutResumed;
    }

    public void setOnLayoutPauseListener(@NonNull LayoutStates.OnLayoutPaused onLayoutPaused) {
        this.onLayoutPaused = onLayoutPaused;
    }

    public void setOnLayoutDestroyedListener(@NonNull LayoutStates.OnLayoutDestroyed onLayoutDestroyed) {
        this.onLayoutDestroyed = onLayoutDestroyed;
    }

    public void onLayoutCreated() {
        if (onLayoutCreated != null) onLayoutCreated.onCreated();
    }

    public void onLayoutResumed() {
        if (onLayoutResumed != null) onLayoutResumed.onResume();
        if (isSetFullScreen) {
            if (baseAct != null)
                if (baseAct.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                    baseAct.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            this.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            this.getResources().getConfiguration().orientation = Configuration.ORIENTATION_LANDSCAPE;
        }


    }

    public void onLayoutPaused() {
        if (onLayoutPaused != null) onLayoutPaused.onPaused();
    }

    public void onLayoutDestroyed() {
        if (onLayoutDestroyed != null) onLayoutDestroyed.onDestroy();
        videoPlayer.stop();
        videoPlayer.release();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int ii) {
        Log.e("MEDIA PLAYER ERROR", "i" + i + " ,  " + ii);
        return false;
    }


    public void init(Activity act) {
        this.baseAct = act;
    }


    public void setOnFullScreenClickListener(@NonNull FullScreenClickListener listener) {
        this.fullscreenToggleClickListener = listener;
    }

    private void onFullScreenToggleClick(boolean isFullscreen) {
        if (fullscreenToggleClickListener != null)
            fullscreenToggleClickListener.onToggleClick(isFullscreen);
        setFullScreenToggle(isFullscreen);
    }
}
