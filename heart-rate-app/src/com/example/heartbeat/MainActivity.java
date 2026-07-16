package com.example.heartbeat;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Lay the phone flat on your bare chest, screen up, and hold still.
 * Chest-wall movement from each heartbeat shows up as a tiny periodic
 * blip in the accelerometer signal (seismocardiography). We band-pass
 * that signal with two exponential moving averages, pick peaks against
 * a noise-adaptive threshold, and turn the last few beat-to-beat
 * intervals into a BPM estimate once enough beats have been seen.
 */
public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensor;

    private TextView bpmView;
    private TextView statusView;

    private double emaFast;
    private double emaSlow;
    private double emaVariance;
    private boolean emaInit;

    private long lastBeatNanos;
    private static final long MIN_BEAT_INTERVAL_NANOS = 300_000_000L; // refractory period, caps ~200 BPM
    private static final long MAX_BEAT_INTERVAL_NANOS = 2_000_000_000L; // gap this long resets the run

    private final Deque<Long> beatIntervalsMs = new ArrayDeque<>();
    private static final int MAX_INTERVALS = 8;
    private static final int MIN_BEATS_FOR_BPM = 4;

    private double smoothedBpm = -1;
    private long lastMovementWarningNanos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(Color.BLACK);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        bpmView = new TextView(this);
        bpmView.setTextColor(Color.WHITE);
        bpmView.setTextSize(72);
        bpmView.setGravity(Gravity.CENTER);
        bpmView.setText("--");
        root.addView(bpmView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        statusView = new TextView(this);
        statusView.setTextColor(Color.LTGRAY);
        statusView.setTextSize(18);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(60, 40, 60, 40);
        statusView.setText("Place the phone flat on your bare chest, screen up, and stay still.");
        root.addView(statusView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(root);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if (sensor == null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetDetection();
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
            statusView.setText("Place the phone flat on your bare chest, screen up, and stay still.");
        } else {
            statusView.setText("No motion sensor available on this device.");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void resetDetection() {
        emaInit = false;
        emaFast = 0;
        emaSlow = 0;
        emaVariance = 0;
        lastBeatNanos = 0;
        beatIntervalsMs.clear();
        smoothedBpm = -1;
        bpmView.setText("--");
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        double x = event.values[0];
        double y = event.values[1];
        double z = event.values[2];
        double magnitude = Math.sqrt(x * x + y * y + z * z);

        if (!emaInit) {
            emaFast = magnitude;
            emaSlow = magnitude;
            emaVariance = 0.01;
            emaInit = true;
            return;
        }

        // Fast EMA tracks the raw signal, slow EMA tracks the drifting baseline
        // (breathing, posture shift); their difference is a crude band-pass filter
        // tuned to pull out the quick heartbeat blip.
        double alphaFast = 0.35;
        double alphaSlow = 0.02;
        emaFast = alphaFast * magnitude + (1 - alphaFast) * emaFast;
        emaSlow = alphaSlow * magnitude + (1 - alphaSlow) * emaSlow;

        double signal = emaFast - emaSlow;

        double alphaVar = 0.05;
        emaVariance = alphaVar * (signal * signal) + (1 - alphaVar) * emaVariance;
        double dynamicThreshold = Math.sqrt(emaVariance) * 1.2;

        long now = event.timestamp;

        // A spike many times larger than the recent noise floor is the phone
        // being moved/repositioned, not a heartbeat - flag it and skip.
        if (dynamicThreshold > 0 && Math.abs(signal) > dynamicThreshold * 8) {
            if (now - lastMovementWarningNanos > 2_000_000_000L) {
                statusView.setText("Too much movement — hold still for a steady reading.");
                lastMovementWarningNanos = now;
            }
            return;
        }

        boolean refractoryOk = lastBeatNanos == 0 || (now - lastBeatNanos) > MIN_BEAT_INTERVAL_NANOS;

        if (dynamicThreshold > 1e-6 && signal > dynamicThreshold && refractoryOk) {
            if (lastBeatNanos != 0) {
                long intervalNanos = now - lastBeatNanos;
                if (intervalNanos < MAX_BEAT_INTERVAL_NANOS) {
                    long intervalMs = intervalNanos / 1_000_000L;
                    beatIntervalsMs.addLast(intervalMs);
                    if (beatIntervalsMs.size() > MAX_INTERVALS) {
                        beatIntervalsMs.removeFirst();
                    }
                } else {
                    beatIntervalsMs.clear();
                }
            }
            lastBeatNanos = now;
            updateBpmDisplay();
        }
    }

    private void updateBpmDisplay() {
        int count = beatIntervalsMs.size();
        if (count < MIN_BEATS_FOR_BPM) {
            statusView.setText("Calibrating… " + count + "/" + MIN_BEATS_FOR_BPM + " beats detected");
            return;
        }

        List<Long> sorted = new ArrayList<>(beatIntervalsMs);
        Collections.sort(sorted);
        long median = sorted.get(sorted.size() / 2);

        long sum = 0;
        int used = 0;
        for (long v : beatIntervalsMs) {
            if (v > median * 0.6 && v < median * 1.4) {
                sum += v;
                used++;
            }
        }
        if (used == 0) {
            return;
        }
        double avgIntervalMs = (double) sum / used;
        double instantBpm = 60000.0 / avgIntervalMs;

        // Exponential smoothing so the displayed number doesn't jitter beat to beat.
        smoothedBpm = (smoothedBpm < 0) ? instantBpm : (0.3 * instantBpm + 0.7 * smoothedBpm);

        bpmView.setText(String.valueOf(Math.round(smoothedBpm)));
        statusView.setText("BPM (live) — keep the phone still on your chest");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
