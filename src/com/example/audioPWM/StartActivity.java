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
        EditText durTime;
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.main);
                play = (Button) findViewById(R.id.button);
                dutyl = (EditText) findViewById(R.id.editText);
                dutyr = (EditText) findViewById(R.id.editText2);
                durTime = (EditText) findViewById(R.id.editText3);
                play.setOnClickListener(new View.OnClickListener()
                {
                        @Override
                        public void onClick(View view)
                        {
                                String strL = String.valueOf(dutyl.getText());
                                String strR = String.valueOf(dutyr.getText());
                                String strDur = String.valueOf(durTime.getText());
                                duration = Integer.parseInt(strDur);
                                if(running)
                                {
                                        sayToast("wait");
                                        return;
                                }
                                if(duration > 5000 || duration < 1)
                                {
                                        sayToast("invalid duration, setting to 1000");
                                        duration = 1000;
                                        durTime.setText("1000");
                                }
                                final int dutyL = Integer.parseInt(strL);
                                final int dutyR = Integer.parseInt(strR);
                                if (dutyL <= 100 && dutyL >= 0 && dutyR <= 100 && dutyR >= 0)
                                {
                                        play.setText("playing");
                                        runPWM(dutyL, dutyR);
                                } else
                                {
                                        sayToast("pwm out of range");
                                }
                        }
                });
        }

        public void runPWM(int dutyL, int dutyR)
        {
                running = true;
                new backgroungSound().execute(dutyL, dutyR);
        }

        public void generatePWM(int dutyL, int dutyR)
        {
                int cycles = (int)(sampleRate*((double)duration/1000))/100;
                short dataL[] = new short[cycles*100];
                short dataR[] = new short[cycles*100];
                short dataStereo[] = new short[2*cycles*100];
                short singleSampleL[] = new short[100];
                short singleSampleR[] = new short[100];
                for (int iii = 0; iii < 100; iii++)
                {
                        singleSampleL[iii] = 0;
                        singleSampleR[iii] = 0;
                }
                for(int iii = 0; iii < dutyL; iii++ )
                {
                        singleSampleL[iii] = 32767;
                }
                for(int iii = 0; iii < dutyR; iii++ )
                {
                        singleSampleR[iii] = 32767;
                }
                for (int iii = 0; iii < cycles*100; iii++)
                {
                        dataL[iii] = singleSampleL[iii % 100];
                        dataR[iii] = singleSampleR[iii % 100];
                }

                /* for stereo, if dataL is say {2, 18, 52}
                *             and dataR is say {5, 25, 35}
                *       then complete dataStereo will be
                *       {2, 5, 18, 25, 52, 35}
                */
                for (int iii = 0; iii < 2*cycles*100;)
                {
                        dataStereo[iii] = dataL[(iii++)/2];
                        dataStereo[iii] = dataR[(iii++)/2];
                }
                AudioTrack pwm = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, dataStereo.length, AudioTrack.MODE_STATIC);
                pwm.write(dataStereo, 0, dataStereo.length);
                pwm.play();
                while (pwm.getPlaybackHeadPosition() < dataStereo.length/4)  // one short 2 bytes, 2 channel, 2*2 = 4
                {
                }
        }

        private void sendLog(String err)
        {
                Log.d(TAG, err);
        }
        public void sayToast(String txt)
        {
                Toast.makeText(this, txt, Toast.LENGTH_SHORT).show();
        }

        private class backgroungSound extends AsyncTask<Integer, Void, Void>
        {
                @Override
                protected Void doInBackground(Integer... a)
                {
                        generatePWM(a[0], a[1]);
                        return null;
                }
                protected void onPostExecute(Void v)
                {
                        running = false;
                        play.setText("play");
                }
        }
}
