package com.urbantamil.kalsee;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.util.Queue;

/*
 * This code is distributed under Apache License.
 * (c) 2016 Muthiah Annamalai
 * (C) 2016 Ezhil Language Foundation
 */
class SoundPlayNext implements MediaPlayer.OnCompletionListener {
	public interface OnLoopCompletionListener {
		void onLoopComplete(SoundPlayNext obj);
	}

	private Queue<ResIDObj> ref_soundNamesQ;
	private Context ref_ctx;
	private OnLoopCompletionListener m_loopComplete_listener;

	SoundPlayNext(Queue<ResIDObj> soundNamesQ,Context refCtx) {
		ref_soundNamesQ = soundNamesQ;
		ref_ctx = refCtx;
		m_loopComplete_listener = null;
	}

	public  void setOnLoopCompletionListener(OnLoopCompletionListener lpl) {
		m_loopComplete_listener = lpl;
	}

	public void onCompletion(MediaPlayer mp) {
		  if(mp.isPlaying()) {
	        	mp.stop();
		  }
         mp.reset();
         mp.release();
         mp=null;
         Log.d("Mplay","stopped player");

         if ( !ref_soundNamesQ.isEmpty() ) {
        	 ResIDObj obj = ref_soundNamesQ.remove();
        	 MediaPlayer mplayer = MediaPlayer.create(ref_ctx, obj.resID);
        	 mplayer.setOnCompletionListener( this );
        	 mplayer.start();
         } else if ( m_loopComplete_listener != null){
			 m_loopComplete_listener.onLoopComplete(this);
		 }
	}
}
