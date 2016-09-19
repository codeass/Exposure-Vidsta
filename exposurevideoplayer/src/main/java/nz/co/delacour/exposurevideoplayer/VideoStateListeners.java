package nz.co.delacour.exposurevideoplayer;

/**
 * Created by Chris on 13-Sep-16.
 */

public class VideoStateListeners {

    public interface OnVideoStartedListener {
        void OnVideoStarted(ExposureVideoPlayer evp);
    }

    public interface OnVideoPausedListener {
        void OnVideoPaused(ExposureVideoPlayer evp);
    }

    public interface OnVideoStoppedListener {
        void OnVideoStopped(ExposureVideoPlayer evp);
    }

    public interface OnVideoFinishedListener {
        void OnVideoFinished(ExposureVideoPlayer evp);
    }

    public interface OnVideoBufferingListener {
        void OnVideoBuffering(ExposureVideoPlayer evp, int buffPercent);
    }

    public interface OnVideoErrorListener {
        void OnVideoError(int i, int ii);
    }

    public interface OnVideoRestartListener {
        void OnVideoRestart(ExposureVideoPlayer player);
    }
}