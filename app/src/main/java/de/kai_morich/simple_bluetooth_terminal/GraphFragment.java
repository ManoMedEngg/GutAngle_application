package de.kai_morich.simple_bluetooth_terminal;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class GraphFragment extends Fragment implements ServiceConnection, SerialListener {

    private SerialService service;
    private LineChart chart;
    private long xValue = 0;
    private StringBuilder sessionBuilder = new StringBuilder();

    private final int[] colors = {
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(service != null)
            service.attach(this);
        else
            getActivity().startService(new Intent(getActivity(), SerialService.class));
    }

    @Override
    public void onStop() {
        if(service != null && !getActivity().isChangingConfigurations())
            service.detach();
        super.onStop();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        getActivity().bindService(new Intent(getActivity(), SerialService.class), this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        try { getActivity().unbindService(this); } catch(Exception ignored) {}
        super.onDetach();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graph, container, false);
        chart = view.findViewById(R.id.chart);
        setupChart();

        view.findViewById(R.id.btn_clear_graph).setOnClickListener(v -> clearChart());
        view.findViewById(R.id.btn_export).setOnClickListener(v -> exportSession());

        return view;
    }

    private void setupChart() {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setDrawGridBackground(false);
        chart.setPinchZoom(true);
        chart.setBackgroundColor(Color.BLACK);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);
        chart.setData(data);

        Legend l = chart.getLegend();
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = chart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisRight().setEnabled(false);
    }

    private void addEntry(float[] values) {
        LineData data = chart.getData();

        if (data != null) {
            for (int i = 0; i < values.length; i++) {
                ILineDataSet set = data.getDataSetByIndex(i);
                if (set == null) {
                    set = createSet(i);
                    data.addDataSet(set);
                }
                data.addEntry(new Entry(xValue, values[i]), i);
            }
            xValue++;
            data.notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.setVisibleXRangeMaximum(50);
            chart.moveViewToX(data.getEntryCount());
        }
    }

    private LineDataSet createSet(int index) {
        LineDataSet set = new LineDataSet(null, "Data " + (index + 1));
        set.setAxisDependency(com.github.mikephil.charting.components.YAxis.AxisDependency.LEFT);
        set.setColor(colors[index % colors.length]);
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(0f);
        set.setFillAlpha(65);
        set.setFillColor(colors[index % colors.length]);
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private void clearChart() {
        chart.clearValues();
        if (chart.getData() != null) chart.getData().notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
        xValue = 0;
        sessionBuilder.setLength(0);
    }

    private void exportSession() {
        if (sessionBuilder.length() == 0) {
            Toast.makeText(getActivity(), "No data to export", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "gutangle_session_" + System.currentTimeMillis() + ".csv");
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == getActivity().RESULT_OK && data != null) {
            Uri uri = data.getData();
            try (OutputStream os = getActivity().getContentResolver().openOutputStream(uri)) {
                os.write(sessionBuilder.toString().getBytes());
                Toast.makeText(getActivity(), "Exported successfully", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((SerialService.SerialBinder) binder).getService();
        service.attach(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

    @Override
    public void onSerialConnect() {}

    @Override
    public void onSerialConnectError(Exception e) {}

    @Override
    public void onSerialRead(byte[] data) {
        String message = new String(data);
        sessionBuilder.append(message);
        parseAndPlot(message);
    }

    @Override
    public void onSerialRead(ArrayDeque<byte[]> datas) {
        for (byte[] data : datas) {
            onSerialRead(data);
        }
    }

    private void parseAndPlot(String message) {
        String[] lines = message.split("\n");
        for (String line : lines) {
            String[] parts = line.split(",");
            List<Float> floatValues = new ArrayList<>();
            for (String part : parts) {
                try {
                    floatValues.add(Float.parseFloat(part.trim()));
                } catch (NumberFormatException ignored) {}
            }
            if (!floatValues.isEmpty()) {
                float[] values = new float[floatValues.size()];
                for (int i = 0; i < floatValues.size(); i++) values[i] = floatValues.get(i);
                getActivity().runOnUiThread(() -> addEntry(values));
            }
        }
    }

    @Override
    public void onSerialIoError(Exception e) {}
}
