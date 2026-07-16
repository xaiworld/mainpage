package com.example.heartbeat;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
 *
 * The bottom of the screen keeps a history of measurement runs. The
 * current run shows a live beat count and time since its first beat;
 * when the detector has to recalibrate (a >2s gap between beats, or
 * the app is paused), the row freezes as-is and a new row starts.
 */
public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor sensor;

    private TextView bpmView;
    private TextView statusView;

    private LinearLayout historyLayout;
    private ScrollView historyScroll;
    private TextView currentRunView;
    private int runCount;
    private int beatCount;
    private long firstBeatElapsedMs; // SystemClock.elapsedRealtime() at the run's first beat

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            long nowMs = SystemClock.elapsedRealtime();
            if (sensor != null && nowMs >= suppressUntilMs
                    && nowMs - lastBeatWallMs > STALL_TIMEOUT_MS) {
                resetDetection();
                statusView.setText("No heartbeat detected — set the phone on your chest again.");
                suppressUntilMs = nowMs + SETTLE_AFTER_STALL_MS;
                lastBeatWallMs = nowMs;
            }
            if (currentRunView != null) {
                updateCurrentRunRow();
            }
            uiHandler.postDelayed(this, 1000);
        }
    };

    private double emaFast;
    private double emaSlow;
    private double emaVariance;
    private boolean emaInit;

    private long lastBeatNanos;
    private static final long MIN_BEAT_INTERVAL_NANOS = 300_000_000L; // absolute refractory floor, caps ~200 BPM
    private static final long MAX_BEAT_INTERVAL_NANOS = 2_000_000_000L; // gap this long resets the run
    // Each heartbeat produces two chest-wall thumps (S1 "lub" and S2 "dub",
    // ~300-350ms apart), so a fixed 300ms dead time lets S2 slip through and
    // get counted as its own beat - the displayed BPM then ramps toward 2x.
    // Once locked onto a rhythm, stretch the dead time to 55% of the median
    // beat spacing; capped so a genuinely fast rate is still detectable.
    private static final double ADAPTIVE_REFRACTORY_FRACTION = 0.55;
    private static final long MAX_ADAPTIVE_REFRACTORY_MS = 650;

    private final Deque<Long> beatIntervalsMs = new ArrayDeque<>();
    private static final int MAX_INTERVALS = 8;
    private static final int MIN_BEATS_FOR_BPM = 4;

    private double smoothedBpm = -1;
    private long lastMovementWarningNanos;

    // Stall handling: with no beat for this long, tell the user to reposition
    // the phone, then ignore the sensor briefly so the bump of repositioning
    // doesn't feed the fresh calibration.
    private static final long STALL_TIMEOUT_MS = 5000;
    private static final long SETTLE_AFTER_STALL_MS = 1000;
    private long lastBeatWallMs;
    private long suppressUntilMs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        float density = getResources().getDisplayMetrics().density;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout centerBlock = new LinearLayout(this);
        centerBlock.setOrientation(LinearLayout.VERTICAL);
        centerBlock.setGravity(Gravity.CENTER);
        root.addView(centerBlock, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        bpmView = new TextView(this);
        bpmView.setTextColor(Color.WHITE);
        bpmView.setTextSize(72);
        bpmView.setGravity(Gravity.CENTER);
        bpmView.setText("--");
        centerBlock.addView(bpmView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        statusView = new TextView(this);
        statusView.setTextColor(Color.LTGRAY);
        statusView.setTextSize(18);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(60, 40, 60, 40);
        statusView.setText("Place the phone flat on your bare chest, screen up, and stay still.");
        centerBlock.addView(statusView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        historyScroll = new ScrollView(this);
        historyLayout = new LinearLayout(this);
        historyLayout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * density);
        historyLayout.setPadding(pad, pad / 2, pad, pad);
        historyScroll.addView(historyLayout, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(historyScroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) (180 * density)));

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
        lastBeatWallMs = SystemClock.elapsedRealtime();
        suppressUntilMs = 0;
        uiHandler.postDelayed(ticker, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        uiHandler.removeCallbacks(ticker);
        freezeCurrentRun();
    }

    private void resetDetection() {
        emaInit = false;
        emaFast = 0;
        emaSlow = 0;
        emaVariance = 0.01;
        lastBeatNanos = 0;
        beatIntervalsMs.clear();
        smoothedBpm = -1;
        bpmView.setText("--");
        freezeCurrentRun();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (SystemClock.elapsedRealtime() < suppressUntilMs) {
            return; // settle window after a stall reset
        }
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

        long refractoryNanos = MIN_BEAT_INTERVAL_NANOS;
        if (beatIntervalsMs.size() >= MIN_BEATS_FOR_BPM) {
            long adaptiveMs = Math.min(
                    (long) (ADAPTIVE_REFRACTORY_FRACTION * medianIntervalMs()),
                    MAX_ADAPTIVE_REFRACTORY_MS);
            refractoryNanos = Math.max(refractoryNanos, adaptiveMs * 1_000_000L);
        }
        boolean refractoryOk = lastBeatNanos == 0 || (now - lastBeatNanos) > refractoryNanos;

        if (dynamicThreshold > 1e-6 && signal > dynamicThreshold && refractoryOk) {
            if (lastBeatNanos == 0) {
                startNewRun();
            } else {
                long intervalNanos = now - lastBeatNanos;
                if (intervalNanos < MAX_BEAT_INTERVAL_NANOS) {
                    beatCount++;
                    long intervalMs = intervalNanos / 1_000_000L;
                    beatIntervalsMs.addLast(intervalMs);
                    if (beatIntervalsMs.size() > MAX_INTERVALS) {
                        beatIntervalsMs.removeFirst();
                    }
                } else {
                    // Recalibration: freeze the finished run and start a fresh one.
                    beatIntervalsMs.clear();
                    startNewRun();
                }
            }
            lastBeatNanos = now;
            lastBeatWallMs = SystemClock.elapsedRealtime();
            // The run's row only materializes once calibration completes, so
            // aborted pre-calibration runs never clutter the history.
            if (currentRunView == null && beatIntervalsMs.size() >= MIN_BEATS_FOR_BPM) {
                createRunRow();
            }
            updateCurrentRunRow();
            updateBpmDisplay();
        }
    }

    private void startNewRun() {
        freezeCurrentRun();
        beatCount = 1;
        firstBeatElapsedMs = SystemClock.elapsedRealtime();
    }

    private void createRunRow() {
        runCount++;

        float density = getResources().getDisplayMetrics().density;
        currentRunView = new TextView(this);
        currentRunView.setTextColor(Color.WHITE);
        currentRunView.setTextSize(16);
        currentRunView.setPadding(0, (int) (4 * density), 0, (int) (4 * density));
        historyLayout.addView(currentRunView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        historyScroll.post(new Runnable() {
            @Override
            public void run() {
                historyScroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private void freezeCurrentRun() {
        if (currentRunView != null) {
            currentRunView.setText(runRowText(false));
            currentRunView.setTextColor(Color.GRAY);
            currentRunView = null;
        }
    }

    private void updateCurrentRunRow() {
        if (currentRunView != null) {
            currentRunView.setText(runRowText(true));
        }
    }

    private String runRowText(boolean live) {
        long elapsedMs = SystemClock.elapsedRealtime() - firstBeatElapsedMs;
        String row = "Run " + runCount + " — " + beatCount
                + (beatCount == 1 ? " beat · " : " beats · ") + formatElapsed(elapsedMs);
        return live ? row + "  ●" : row;
    }

    private static String formatElapsed(long ms) {
        long totalSec = ms / 1000;
        long h = totalSec / 3600;
        long m = (totalSec % 3600) / 60;
        long s = totalSec % 60;
        if (h > 0) {
            return h + ":" + (m < 10 ? "0" : "") + m + ":" + (s < 10 ? "0" : "") + s;
        }
        return m + ":" + (s < 10 ? "0" : "") + s;
    }

    private long medianIntervalMs() {
        List<Long> sorted = new ArrayList<>(beatIntervalsMs);
        Collections.sort(sorted);
        return sorted.get(sorted.size() / 2);
    }

    private void updateBpmDisplay() {
        int count = beatIntervalsMs.size();
        if (count < MIN_BEATS_FOR_BPM) {
            statusView.setText("Calibrating… " + count + "/" + MIN_BEATS_FOR_BPM + " beats detected");
            return;
        }

        long median = medianIntervalMs();

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
