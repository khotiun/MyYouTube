package com.khotiun.myyoutube.fragments;

import android.os.Bundle;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.khotiun.myyoutube.R;

/**
 * Created by hotun on 18.07.2017.
 */

public class FragmentVideo extends YouTubePlayerFragment implements YouTubePlayer.OnInitializedListener {

   private YouTubePlayer mPlayer;
   private String mVideoId;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        initialize(getResources().getString(R.string.youtube_apikey), this);
    }

    @Override
    public void onDestroy() {
        if (mPlayer != null) {
            mPlayer.release();
        }
        super.onDestroy();
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean restored) {

        this.mPlayer = youTubePlayer;
        mPlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE);
        mPlayer.setOnFullscreenListener((YouTubePlayer.OnFullscreenListener) getActivity());

        if (!restored && mVideoId != null) {
            mPlayer.cueVideo(mVideoId);
        }

    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

        this.mPlayer = null;

    }

    public void backnormal() {
        mPlayer.setFullscreen(false);
    }
}
