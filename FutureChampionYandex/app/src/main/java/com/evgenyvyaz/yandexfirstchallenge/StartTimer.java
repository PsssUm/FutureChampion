package com.evgenyvyaz.yandexfirstchallenge;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

/**
 * Created by X550V on 27.08.2016.
 */
public class StartTimer {
    private Handler handler = new Handler(Looper.getMainLooper());
    private Long time;
    private TextView textView;
    private String result = "";

    OnStopListener onStopListener;

    public interface OnStopListener {
        public void onStop();
    }


    public StartTimer(Long time, TextView textView, OnStopListener onStopListener) {
        this.time = time;
        this.textView = textView;

        this.onStopListener = onStopListener;
    }

    private boolean opened = true;

    public void startTimer() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (opened) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }

                    long sec = time;
                    time --;


                    long minutes;
                    long seconds = sec % 60;
                    if (sec >= 3600) {

                        long hours = Math.round(sec / 3600);
                        minutes = (sec % 3600) / 60;
                        result = (hours < 10 ? "0" + hours : hours) + ":" + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
                    } else {
                        minutes = Math.round(sec / 60);
                        result = (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds < 10 ? "0" + seconds : seconds);
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (textView.getVisibility() == View.GONE && opened) {
                                textView.setVisibility(View.VISIBLE);
                            }
                            textView.setText(result);
                        }
                    });

                    if (sec == 0){
                        opened = false;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onStopListener.onStop();
                                textView.setText("");
                                textView.setVisibility(View.GONE);
                            }
                        });

                    }
                }


            }


        });
        thread.start();
    }


}