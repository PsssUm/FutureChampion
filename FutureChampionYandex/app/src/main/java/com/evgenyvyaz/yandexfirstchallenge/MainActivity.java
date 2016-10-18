package com.evgenyvyaz.yandexfirstchallenge;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener, TextToSpeech.OnInitListener {
    private SensorManager sensorManager;
    private boolean color = false;

    private long lastUpdate;
    private MediaPlayer mp;
    private int count = 0;
    private int progress = 1;

    private int testModeCounter = 0;
    private Float averageX;
    private Float averageY;
    private Float averageZ;
    private Button startBTN;
    private int progressHeight;
    private boolean isTestMode = false;
    private Float testX[] = new Float[3];
    private Float testY[] = new Float[3];
    private Float testZ[] = new Float[3];
    private RelativeLayout progressBarRL;
    private RelativeLayout progressRL;
    private LinearLayout testSquadsLL;
    private TextView timerTV;
    private TextToSpeech speech;
    private boolean readyToSpeak = false;
    private TextView squatCountTV;
    private LinearLayout trainingLL;
    private Button stopTrainingBTN;
    private LinearLayout timerLL;
    private String motivations[];
    private Random random;
    private RelativeLayout.LayoutParams params;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startBTN = (Button) findViewById(R.id.startBTN);
        stopTrainingBTN = (Button) findViewById(R.id.stopTrainingBTN);
        timerTV = (TextView) findViewById(R.id.timerTV);
        squatCountTV = (TextView) findViewById(R.id.squatCountTV);
        progressBarRL = (RelativeLayout) findViewById(R.id.progressBarRL);
        progressRL = (RelativeLayout) findViewById(R.id.progressRL);
        testSquadsLL = (LinearLayout) findViewById(R.id.testSquadsLL);
        trainingLL = (LinearLayout) findViewById(R.id.trainingLL);
        timerLL = (LinearLayout) findViewById(R.id.timerLL);
        speech = new TextToSpeech(this, this);

        mp = MediaPlayer.create(getApplicationContext(), R.raw.wah);
        mp.setVolume(100, 100);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();
        motivations = getResources().getStringArray(R.array.motivation);
        random = new Random();

        startBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StartTimer startTimer = new StartTimer(10L, timerTV,new StartTimer.OnStopListener() {
                    @Override
                    public void onStop() {
                        isTestMode = true;

                        if (readyToSpeak) {
                            sayText(getString(R.string.do_test_squads));
                        }
                        timerLL.setVisibility(View.GONE);
                        testSquadsLL.setVisibility(View.VISIBLE);
                    }
                });
                startTimer.startTimer();
                timerLL.setVisibility(View.VISIBLE);
                startBTN.setVisibility(View.GONE);
            }
        });

        stopTrainingBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testModeCounter = 0;
                count = 0;
                progress = 1;
                squatCountTV.setText("");
                trainingLL.setVisibility(View.GONE);
                startBTN.setVisibility(View.VISIBLE);
                params = new RelativeLayout.LayoutParams(0, progressHeight);
                progressBarRL.setLayoutParams(params);

            }
        });

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }

    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];


        float accelationSquareRoot = (y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);

        long actualTime = System.currentTimeMillis();
        if (accelationSquareRoot >= 1.6f) {

            if (actualTime - lastUpdate < 300) {
                return;
            }
            lastUpdate = actualTime;
            if (isTestMode) {

                if (testModeCounter % 2 == 0) {
                    testX[testModeCounter / 2] = x;
                    testY[testModeCounter / 2] = y;
                    testZ[testModeCounter / 2] = z;
                    progressRL.post(new Runnable() {


                        @Override
                        public void run() {
                           progressHeight =  progressBarRL.getHeight();
                            params = new RelativeLayout.LayoutParams(progressRL.getWidth() / 3 * progress, progressHeight);
                            progressBarRL.setLayoutParams(params);
                            progress++;
                        }
                    });


                }

                if (testModeCounter == 5) {
                    isTestMode = false;
                    averageX = getAveragePoint(testX);
                    averageY = getAveragePoint(testY);
                    averageZ = getAveragePoint(testZ);
                    if (readyToSpeak) {
                        sayText(getString(R.string.start_training));
                    }

                }
                testModeCounter++;
                return;
            }
            if (averageX == null) {
                return;
            }

            if (x < (averageX - 5) || x > (averageX + 5) || y < (averageY - 5) || y > (averageY + 5) || z < (averageZ - 5) || z > (averageZ + 5)) {
                return;
            }
            if (trainingLL.getVisibility() != View.VISIBLE) {
                trainingLL.setVisibility(View.VISIBLE);
                testSquadsLL.setVisibility(View.GONE);

            }

            count++;
            if (count % 5 == 0){
                if (readyToSpeak) {
                    sayText(motivations[random.nextInt(motivations.length)]);
                }
            } else {
                mp.start();
            }
            squatCountTV.setText(count + "");




        }
    }

    private void sayText(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            speech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            speech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        // register this class as a listener for the orientation and
        // accelerometer sensors
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onDestroy() {

        if (speech != null) {
            speech.stop();
            speech.shutdown();
        }
        super.onDestroy();
    }

    private Float getAveragePoint(Float array[]) {
        Float sum = 0f;
        for (int i = 0; i < array.length; i++) {
            sum += array[i];
        }
        return sum / array.length;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            Locale locale = new Locale("ru");

            int result = speech.setLanguage(locale);
            //int result = speech.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Извините, этот язык не поддерживается");
            } else {
                readyToSpeak = true;
            }

        } else {
            Log.e("TTS", "Ошибка!");
        }

    }
}