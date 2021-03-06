
// by huanglibo 2017
package com.record.amrRecord;

import java.io.File;
import java.io.IOException;

import org.cocos2dx.lib.Cocos2dxGLSurfaceView;
import org.cocos2dx.lib.Cocos2dxLuaJavaBridge;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

public class AmrRecordAndPlay {
	
	private static final AmrRecordAndPlay mInstance = new AmrRecordAndPlay();
	
	private MediaRecorder mRecorder = null;	
	private MediaPlayer mPlayer = null;
	private static String mFilepath = "";
	private static int mLuaRecordResultFuncId = 0;
	private static int mLuaPlayResultFuncId = 0;

	//开始录音
	public static void startRecord(final String fullpath,final int luaRecordResultFuncId) 
	{
		Log.d("AmrRecord", "startRecord...");
		
		//stop record and play ...
		mInstance.stopAll();
		
		mFilepath = fullpath; //Environment.getExternalStorageDirectory()+"/hlb.amr";
		mLuaRecordResultFuncId = luaRecordResultFuncId;				
		mInstance.initRecorder();

    	try {
    		mInstance.mRecorder.prepare();
    		mInstance.mRecorder.start(); 
		} 			
    	catch (IOException e) {
			e.printStackTrace();
			Log.d("AmrRecord", " has error !!!");
			mInstance.handleRecordResult("fail");
		}     	
	} 
	
	//停止录音
	public static void stopRecord(final int luaRecordResultFuncId) 
	{
		Log.d("AmrRecord", "stopRecord");
		mLuaRecordResultFuncId = luaRecordResultFuncId;		
    	if(mInstance.mRecorder != null)
    	{
    		try {
	    		mInstance.mRecorder.stop();
	    		mInstance.mRecorder.release();// 释放资源
	    		mInstance.mRecorder = null;
	    		
	    		mInstance.handleRecordResult("success");
	    	}
            catch(Exception e){
                Log.d("AmrRecord", " has error in stopRecord...");
                e.printStackTrace();
                mInstance.mRecorder.release();
                mInstance.mRecorder = null; 
            } 
    	} 
	} 	
	
	//取消录音
	public static void cancelRecord() 
	{
		Log.d("AmrRecord", "cancelRecord");
    	if(mInstance.mRecorder != null){
    		mInstance.mRecorder.stop();
    		mInstance.mRecorder.release();// 释放资源
    		mInstance.mRecorder = null;
    	} 
    	
		File file = new File(mFilepath);
        if (file.exists())
            file.delete();     	
	} 		
	
	
	
	/*=====================================================================================================================*/
	
	//播放语音
	public static void startPlaying(final String fullpath, final int luaPlayResultFuncId) 	
	{	
		Log.d("AmrRecord", "startPlaying...");
		
		mInstance.stopAll();
		
		mFilepath = fullpath;
		mLuaPlayResultFuncId = luaPlayResultFuncId;
		

		mInstance.mPlayer = new MediaPlayer();
		
		//设置一个error监听器
		mInstance.mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
						                public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
						                	mInstance.stopAll();
						        			
						                	mInstance.handlePlayResult("error");
						                    return false;
						                }
    							  });
		
		mInstance.mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
	        public void onCompletion(MediaPlayer mp) {
	        	Log.d("AmrRecord", "play finish...");
	        	mInstance.stopAll();

	        	if (mInstance != null) {
	        		mInstance.handlePlayResult("finish");    	        		    	        		
            	}
	        }
		});					

		

		
		
        try {
        	mInstance.mPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
        	mInstance.mPlayer.setDataSource(mFilepath);
        	//mInstance.mPlayer.setVolume(1.0f, 1.0f);
        	//mInstance.mPlayer.prepare();
        	mInstance.mPlayer.prepareAsync();
        	mInstance.mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {				
						@Override
						public void onPrepared(MediaPlayer mp) {
							// TODO Auto-generated method stub
							mp.start();
						}
			});
        } 
        catch (Exception e) 
        {
        	mInstance.handlePlayResult("error");
        	mInstance.stopAll();
        }		
	}
	
	//停止播放
	public static void stopPlaying()
	{
		Log.d("AmrRecord", "stopPlaying...");

		mInstance.stopAll();
	}
	
	
	
	private void stopAll()
	{
    	if(mRecorder != null){
    		mRecorder.stop();
    		mRecorder.reset();
    		mRecorder.release();// 释放资源
    		mRecorder = null;
    	} 

		if (mPlayer != null) {
			if (mPlayer.isPlaying()){
				mPlayer.stop();
			}
			mPlayer.reset();
			mPlayer.release();
			mPlayer = null;
		}
	}
	
    private void initRecorder()
    {
    	if (mInstance != null)
    	{
    		
    		Log.d("AmrRecord", "initRecorder");

	        File file = new File(mFilepath);
	        if(file.exists()) {
	            if(file.delete()){
	                try {
	                    file.createNewFile();
	                }
	                catch(IOException e) {
	                    e.printStackTrace();
	                }
	            }
	            else {
	                try {
	                    file.createNewFile();
	                }
	                catch(IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }

	        try{
	            mInstance.mRecorder = new MediaRecorder();
	            mInstance.mRecorder.reset();
	            mInstance.mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	            mInstance.mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
	            mInstance.mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
	            mInstance.mRecorder.setOutputFile(mFilepath);
	            //如下两个设置很重要!否则即使录制2秒钟,某些机器录制出来的音频文件会非常大!!
	            mInstance.mRecorder.setMaxDuration(20000); 
	            mInstance.mRecorder.setMaxFileSize(100*1024); 
	        }
	        catch(Exception e){                
	            Log.d("AmrRecord", "has error in initRecorder...");
	            e.printStackTrace();
	        } 
    	}
    } 

    
	public void handleRecordResult(final String result) 
	{
		Log.d("AmrRecord", "handleRecordResult =" + result);
		if (mLuaRecordResultFuncId != 0)
		{
			
			Cocos2dxGLSurfaceView.getInstance().queueEvent(
					new Runnable() {
						@Override
						public void run() {
							Cocos2dxLuaJavaBridge.callLuaFunctionWithString(mLuaRecordResultFuncId, result);
						}
					});
		}
	}
	
	public void handlePlayResult(final String result) 
	{
		Log.d("AmrRecord", "handlePlayResult = "+ result);
		if (mLuaPlayResultFuncId != 0)
		{			
			Cocos2dxGLSurfaceView.getInstance().queueEvent(
					new Runnable() {
						@Override
						public void run() {
							Cocos2dxLuaJavaBridge.callLuaFunctionWithString(mLuaPlayResultFuncId, result);
						}
					});
		}
	}	
	
	
}