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
        public int duration = 1000; // in ms
        boolean running = false;
        Button play;
        EditText dutyl;
        EditText dutyr;
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.main);
                play = (Button) findViewById(R.id.button);
                dutyl = (EditText) findViewById(R.id.editText);
                dutyr = (EditText) findViewById(R.id.editText2);
                play.setOnClickListener(new View.OnClickListener()
                {
                        @Override
                        public void onClick(View view)
                        {
                                String strL = String.valueOf(dutyl.getText());
                                String strR = String.valueOf(dutyr.getText());
                                final int dutyL = Integer.parseInt(strL);
                                final int dutyR = Integer.parseInt(strR);
                                runPWM(dutyL,dutyR);
                        }
                });
        }

        public void runPWM(int dutyL, int dutyR)
        {
                running = true;
                //generatePWM();
                generatePWM(dutyL, dutyR);
        }

        public void generatePWM(int dutyL, int dutyR)
        {
                int cycles = (int)(sampleRate*((double)duration/1000))/100;
                int  L = dutyL, R = dutyR;
                byte samplesbyteR[] = new byte[2*100];
                byte samplesbyteL[] = new byte[2*100];
                final byte dataL[] = new byte[2*cycles*100];
                byte dataR[] = new byte[2*cycles*100];
                byte dataStereo[] = new byte[2*2*cycles*100];
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
                for (int iii = 0; iii < 2*cycles*100; iii++)
                {
                        dataL[iii] = samplesbyteL[iii % 200];
                        dataR[iii] = samplesbyteR[iii % 200];
                }

                /* for stereo, if dataL is say {2, 18, 52}
                *             and dataR is say {5, 25, 35}
                *       then complete dataStereo will be
                *       {2, 5, 18, 25, 52, 35}
                */
                int lll = 0, rrr = 0;
                for (int iii = 0; iii < 2*2*cycles*100;)
                {
                        dataStereo[iii++] = dataL[lll++];
                        dataStereo[iii++] = dataL[lll++];
                        dataStereo[iii++] = dataR[rrr++];
                        dataStereo[iii++] = dataR[rrr++];
                }
                AudioTrack pwm = new AudioTrack(AudioManager.STREAM_MUSIC, 48000, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, dataStereo.length, AudioTrack.MODE_STATIC);
//                int nativeSampleRate = pwm.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
//                Log.d(TAG, String.valueOf(nativeSampleRate));
                pwm.write(dataStereo, 0, dataStereo.length);
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
