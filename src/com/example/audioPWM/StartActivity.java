package com.example.audioPWM;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;


public class StartActivity extends Activity
{
        /**
         * Called when the activity is first created.
         */
        private final String TAG = "pwm";
        public final int sampleRate = 48000;
        public final int duration = 200; // in ms
        boolean running = false;
        Button play;
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.main);
                play = (Button) findViewById(R.id.button);
                play.setOnClickListener(new View.OnClickListener()
                {
                        @Override
                        public void onClick(View view)
                        {
                         runPWM(50,50);
                        }
                });
        }

        public void runPWM(int dutyL, int dutyR)
        {
                running = true;
                //generatePWM();
                generatePWM();
        }

        public void generatePWM()
        {
                int cycles = (int)(sampleRate*((double)duration/1000))/100;
                int  L = 50, R = 50;
                byte samplesbyteR[] = new byte[2*100];
                byte samplesbyteL[] = new byte[2*100];
                final byte dataL[] = new byte[2*cycles*100];
                byte dataR[] = new byte[2*cycles*100];

                short singleSampleL[] = new short[100];
                short singleSampleR[] = new short[100];
                for (int iii = 0; iii < 100; iii++)
                {
                        singleSampleL[iii] = singleSampleR[iii] = 0;
                }
                for(int iii = 0; iii < L; iii++ )
                {
                        singleSampleL[iii] = 32767;
                }
                for(int iii = 0; iii < R; iii++ )
                {
                        singleSampleR[iii] = 32767;
                }
                for (int iii  = 0; iii < 200;)
                {
                        samplesbyteL[iii] = (byte)( singleSampleL[(iii) /2] & 0x00ff );         // converting to bytes
                        samplesbyteR[iii] = (byte)( singleSampleR[(iii++) /2] & 0x00ff );       // lsb first
                        samplesbyteL[iii] = (byte)(( singleSampleL[(iii) /2] & 0xff00 ) >>> 8);
                        samplesbyteR[iii] = (byte)(( singleSampleR[(iii++) /2] & 0xff00 ) >>> 8);
                }
//                Log.d(TAG, String.valueOf(samplesbyteL[0]));
//                float LBuffer[] = new float[100*100];
//                float RBuffer[] = new float[100*100];
                for (int iii = 0; iii < 2*cycles*100; iii++)
                {
                        dataL[iii] = samplesbyteL[iii % 200];
                }
                AudioTrack pwm = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, dataL.length, AudioTrack.MODE_STATIC);
//                int nativeSampleRate = pwm.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
//                Log.d(TAG, String.valueOf(nativeSampleRate));
                pwm.write(dataL, 0, dataL.length);
                pwm.play();
                play.setText("played");
        }

        private void sendLog(String err)
        {
                Log.d(TAG, err);
        }

//        private class sound extends AsyncTask<Void, Void, Void>
//        {
//
//        }
}
