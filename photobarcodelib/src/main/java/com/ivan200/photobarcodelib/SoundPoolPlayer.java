package com.ivan200.photobarcodelib;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.SparseIntArray;

public class SoundPoolPlayer {
    private SoundPool mShortPlayer;
    private SparseIntArray mSounds = new SparseIntArray();

    public SoundPoolPlayer(Context pContext) {
        this.mShortPlayer = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        mSounds.put(R.raw.bleep, this.mShortPlayer.load(pContext, R.raw.bleep, 1));
    }

    public void playShortResource(int piResource) {
        int iSoundId = mSounds.get(piResource);
        this.mShortPlayer.play(iSoundId, 0.99f, 0.99f, 0, 0, 1);
    }

    public void release() {
        this.mShortPlayer.release();
    }
}
