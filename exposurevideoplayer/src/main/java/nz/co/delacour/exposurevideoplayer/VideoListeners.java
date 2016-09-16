package nz.co.delacour.exposurevideoplayer;

/**
 * Created by Chris on 13-Sep-16.
 */

public interface VideoListeners {

    void OnVideoStarted(ExposureVideoPlayer evp);

    void OnVideoPaused(ExposureVideoPlayer evp);

    void OnVideoStopped(ExposureVideoPlayer evp);

    void OnVideoFinished(ExposureVideoPlayer evp);

    void OnVideoBuffering(ExposureVideoPlayer evp, int buffPercent);

    void OnVideoError(Exception err);//Todo: Add Error listener, not urgent tho.

    void OnVideoRestart(ExposureVideoPlayer player);
}


