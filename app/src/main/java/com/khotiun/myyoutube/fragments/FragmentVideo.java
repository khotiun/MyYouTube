package com.khotiun.myyoutube.fragments;

import android.os.Bundle;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.khotiun.myyoutube.R;

/**
 * Created by hotun on 18.07.2017.
 */
//класс нужен для отображения видео стандартно и в полноэкранном режиме
public class FragmentVideo extends YouTubePlayerFragment implements YouTubePlayer.OnInitializedListener {

   private YouTubePlayer mPlayer;//создание плеера
   private String mVideoId;//строковый параметр для видео id

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        initialize(getResources().getString(R.string.youtube_apikey), this);//инициализация апи кей
    }

    @Override
    public void onDestroy() {
        if (mPlayer != null) {//если плееер существует, то метод release() - оставит воспроизведение или загрузку видео
            //и освобит системные ресурсы используемые ютуб плеером
            mPlayer.release();
        }
        super.onDestroy();
    }
    //метод для установки видео id
    public void setVideoId(String videoId) {
        if (videoId != null && !videoId.equals(this.mVideoId)) {
            this.mVideoId = videoId;
            if(mPlayer != null) {
                mPlayer.cueVideo(videoId);//cueVideo - загружает видео эскиз и готовит плеер к воспроизведению видео
                //но не загружает видео поток до вызова метода play
            }
        }
    }
    //вызывается при успешной инициализации плеера
    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean restored) {

        this.mPlayer = youTubePlayer;
        mPlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE);//устанавливаем полноэкранный флаг
        mPlayer.setOnFullscreenListener((YouTubePlayer.OnFullscreenListener) getActivity());

        if (!restored && mVideoId != null) {
            mPlayer.cueVideo(mVideoId);//готовит плеер к воспроизведению
        }

    }
    //вызывается при ошибки инициализации плеера
    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

        this.mPlayer = null;//обнуляем ссылку на плеер

    }
    //возвращает окно плеера из полноекранного режима в номальный
    public void backnormal() {
        mPlayer.setFullscreen(false);
    }
}
