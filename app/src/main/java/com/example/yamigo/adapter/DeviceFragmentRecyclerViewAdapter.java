package com.example.yamigo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yamigo.R;
import com.example.yamigo.bean.DeviceBlockData;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeviceFragmentRecyclerViewAdapter extends RecyclerView.Adapter<DeviceFragmentRecyclerViewAdapter.DragHolder> implements DeviceDragItemAdapter {
    private Context context;
    private List<DeviceBlockData> contentList;

    private static final int[] colorClassArray = new int[] {Color.RED, Color.BLUE, Color.GRAY};
    private static final String[] legendName = new String[] {"温度", "湿度", "烟雾"};
    private static final String[] LineName = new String[] {"温度", "湿度", "烟雾"};
    private static final String[] BarName = new String[] {"平均温度区间", "平均湿度区间", "平均烟雾区间"};
    private static final String[] deviceName = new String[] {"温度传感器", "湿度传感器", "烟雾传感器"};
    private static final String[] AxisName = new String[] {"0:00-4:00", "4:00-8:00", "8:00-12:00", "12:00-16:00", "16:00-20:00", "20:00-24:00"};

    public DeviceFragmentRecyclerViewAdapter(Context context, List<DeviceBlockData> contentList) {
        this.context = context;
        this.contentList = contentList;
    }

    @NonNull
    @Override
    public DeviceFragmentRecyclerViewAdapter.DragHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.device_drag_item, parent, false);
        return new DragHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceFragmentRecyclerViewAdapter.DragHolder holder, int position) {
        // 拿到对应数据
        DeviceBlockData deviceBlockData = contentList.get(position);
        String name = deviceBlockData.getName();
        ArrayList<Entry> line_values = deviceBlockData.getLine_values();
        ArrayList<BarEntry> bar_values = deviceBlockData.getBar_values();

        // 折线图将要用到的数据类
        LineDataSet lineDataSet;
        ArrayList<ILineDataSet> lineDataSets;
        LineData lineData;

        // 柱状图将要用到的数据类
        BarDataSet barDataSet;
        ArrayList<IBarDataSet> barDataSets;
        BarData barData;

        switch (name) {
            case "temp":
                lineDataSets = new ArrayList<>();
                barDataSets = new ArrayList<>();

                // 设置折线图和柱状图的标题
                holder.device_line_chart_title.setText(LineName[0]);
                holder.device_name.setText(deviceName[0]);
                holder.device_bar_chart_title.setText(BarName[0]);

                // 设置折线图数据
                lineDataSet = new LineDataSet(line_values, legendName[0]);
                lineDataSet.setColor(colorClassArray[0]);
                lineDataSets.add(lineDataSet);
                lineData = new LineData(lineDataSets);

                // 设置柱状图数据
                barDataSet = new BarDataSet(bar_values, legendName[0]);
                barDataSet.setColor(colorClassArray[0]);
                barDataSets.add(barDataSet);
                barData = new BarData(barDataSets);

                // 设置折线图图例
                holder.device_line_chart.getLegend().setForm(Legend.LegendForm.LINE);
                holder.device_line_chart.getLegend().setXEntrySpace(10);
                holder.device_line_chart.getLegend().setFormToTextSpace(10);
                LegendEntry line_entry = new LegendEntry();
                LegendEntry[] line_entries = new LegendEntry[1];
                line_entry.formColor = colorClassArray[0];
                line_entry.label = String.valueOf(legendName[0]);
                line_entries[0] = line_entry;
                holder.device_line_chart.getLegend().setCustom(line_entries);
                
                // 设置柱状图图例
                holder.device_bar_chart.getLegend().setXEntrySpace(10);
                holder.device_bar_chart.getLegend().setFormToTextSpace(10);
                LegendEntry bar_entry = new LegendEntry();
                LegendEntry[] bar_entries = new LegendEntry[1];
                bar_entry.formColor = colorClassArray[0];
                bar_entry.label = String.valueOf(legendName[0]);
                bar_entries[0] = bar_entry;
                holder.device_bar_chart.getLegend().setCustom(bar_entries);

                // 设置折线图轴线
                holder.device_line_chart.getXAxis().setValueFormatter(new TempLineXAxisValueFormatter());
                holder.device_line_chart.getAxisLeft().setAxisMaximum(0f);
                holder.device_line_chart.getAxisLeft().setAxisMaximum(100f);
                holder.device_line_chart.getAxisLeft().setValueFormatter(new TempLineYAxisValueFormatter());
                holder.device_line_chart.getAxisLeft().setDrawAxisLine(false);
                holder.device_line_chart.getAxisRight().setEnabled(false);

                // 设置柱状图轴线
                holder.device_bar_chart.getXAxis().setValueFormatter(new TempBarXAxisValueFormatter());
                holder.device_bar_chart.getAxisLeft().setAxisMinimum(0f);
                holder.device_bar_chart.getAxisLeft().setEnabled(false);
                holder.device_bar_chart.getAxisRight().setAxisMinimum(0f);
                holder.device_bar_chart.getAxisRight().setEnabled(false);

                // 绘制折线图
                holder.device_line_chart.setData(lineData);
                holder.device_line_chart.invalidate();

                // 绘制柱状图
                holder.device_bar_chart.setData(barData);
                holder.device_bar_chart.invalidate();
                break;
            case "humi":
                lineDataSets = new ArrayList<>();
                barDataSets = new ArrayList<>();

                // 设置折线图和柱状图的标题
                holder.device_line_chart_title.setText(LineName[1]);
                holder.device_name.setText(deviceName[1]);
                holder.device_bar_chart_title.setText(BarName[1]);

                // 设置折线图数据
                lineDataSet = new LineDataSet(line_values, legendName[1]);
                lineDataSet.setColor(colorClassArray[1]);
                lineDataSets.add(lineDataSet);
                lineData = new LineData(lineDataSets);

                // 设置柱状图数据
                barDataSet = new BarDataSet(bar_values, legendName[1]);
                barDataSet.setColor(colorClassArray[1]);
                barDataSets.add(barDataSet);
                barData = new BarData(barDataSets);

                // 设置折线图图例
                holder.device_line_chart.getLegend().setForm(Legend.LegendForm.LINE);
                holder.device_line_chart.getLegend().setXEntrySpace(10);
                holder.device_line_chart.getLegend().setFormToTextSpace(10);
                LegendEntry line_entry_1 = new LegendEntry();
                LegendEntry[] line_entries_1 = new LegendEntry[1];
                line_entry_1.formColor = colorClassArray[1];
                line_entry_1.label = String.valueOf(legendName[1]);
                line_entries_1[0] = line_entry_1;
                holder.device_line_chart.getLegend().setCustom(line_entries_1);

                // 设置柱状图图例
                holder.device_bar_chart.getLegend().setXEntrySpace(10);
                holder.device_bar_chart.getLegend().setFormToTextSpace(10);
                LegendEntry bar_entry_1 = new LegendEntry();
                LegendEntry[] bar_entries_1 = new LegendEntry[1];
                bar_entry_1.formColor = colorClassArray[1];
                bar_entry_1.label = String.valueOf(legendName[1]);
                bar_entries_1[0] = bar_entry_1;
                holder.device_bar_chart.getLegend().setCustom(bar_entries_1);

                // 设置折线图轴线
                holder.device_line_chart.getXAxis().setValueFormatter(new HumiLineXAxisValueFormatter());
                holder.device_line_chart.getAxisLeft().setAxisMaximum(0f);
                holder.device_line_chart.getAxisLeft().setAxisMaximum(100f);
                holder.device_line_chart.getAxisLeft().setValueFormatter(new HumiLineYAxisValueFormatter());
                holder.device_line_chart.getAxisLeft().setDrawAxisLine(false);
                holder.device_line_chart.getAxisRight().setEnabled(false);

                // 设置柱状图轴线
                holder.device_bar_chart.getXAxis().setValueFormatter(new HumiBarXAxisValueFormatter());
                holder.device_bar_chart.getAxisLeft().setAxisMinimum(0f);
                holder.device_bar_chart.getAxisLeft().setEnabled(false);
                holder.device_bar_chart.getAxisRight().setAxisMinimum(0f);
                holder.device_bar_chart.getAxisRight().setEnabled(false);

                // 绘制折线图
                holder.device_line_chart.setData(lineData);
                holder.device_line_chart.invalidate();

                // 绘制柱状图
                holder.device_bar_chart.setData(barData);
                holder.device_bar_chart.invalidate();
                break;
            case "smok":
                lineDataSets = new ArrayList<>();
                barDataSets = new ArrayList<>();

                // 设置折线图和柱状图的标题
                holder.device_line_chart_title.setText(LineName[2]);
                holder.device_name.setText(deviceName[2]);
                holder.device_bar_chart_title.setText(BarName[2]);

                // 设置折线图数据
                lineDataSet = new LineDataSet(line_values, legendName[2]);
                lineDataSet.setColor(colorClassArray[2]);
                lineDataSets.add(lineDataSet);
                lineData = new LineData(lineDataSets);

                // 设置柱状图数据
                barDataSet = new BarDataSet(bar_values, legendName[2]);
                barDataSet.setColor(colorClassArray[2]);
                barDataSets.add(barDataSet);
                barData = new BarData(barDataSets);

                // 设置折线图图例
                holder.device_line_chart.getLegend().setForm(Legend.LegendForm.LINE);
                holder.device_line_chart.getLegend().setXEntrySpace(10);
                holder.device_line_chart.getLegend().setFormToTextSpace(10);
                LegendEntry line_entry_2 = new LegendEntry();
                LegendEntry[] line_entries_2 = new LegendEntry[1];
                line_entry_2.formColor = colorClassArray[2];
                line_entry_2.label = String.valueOf(legendName[2]);
                line_entries_2[0] = line_entry_2;
                holder.device_line_chart.getLegend().setCustom(line_entries_2);

                // 设置柱状图图例
                holder.device_bar_chart.getLegend().setXEntrySpace(10);
                holder.device_bar_chart.getLegend().setFormToTextSpace(10);
                LegendEntry bar_entry_2 = new LegendEntry();
                LegendEntry[] bar_entries_2 = new LegendEntry[1];
                bar_entry_2.formColor = colorClassArray[2];
                bar_entry_2.label = String.valueOf(legendName[2]);
                bar_entries_2[0] = bar_entry_2;
                holder.device_bar_chart.getLegend().setCustom(bar_entries_2);

                // 设置折线图轴线
                holder.device_line_chart.getXAxis().setValueFormatter(new SmokLineXAxisValueFormatter());
                holder.device_line_chart.getAxisLeft().setAxisMaximum(0f);
                holder.device_line_chart.getAxisLeft().setAxisMaximum(100f);
                holder.device_line_chart.getAxisLeft().setValueFormatter(new SmokLineYAxisValueFormatter());
                holder.device_line_chart.getAxisLeft().setDrawAxisLine(false);
                holder.device_line_chart.getAxisRight().setEnabled(false);

                // 设置柱状图轴线
                holder.device_bar_chart.getXAxis().setValueFormatter(new SmokBarXAxisValueFormatter());
                holder.device_bar_chart.getAxisLeft().setAxisMinimum(0f);
                holder.device_bar_chart.getAxisLeft().setEnabled(false);
                holder.device_bar_chart.getAxisRight().setAxisMinimum(0f);
                holder.device_bar_chart.getAxisRight().setEnabled(false);

                // 绘制折线图
                holder.device_line_chart.setData(lineData);
                holder.device_line_chart.invalidate();

                // 绘制柱状图
                holder.device_bar_chart.setData(barData);
                holder.device_bar_chart.invalidate();
                break;
        }
    }

    @Override
    public int getItemCount() {
        return contentList.size();
    }

    @Override
    public void onItemMove(RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
        int fromPosition = source.getAdapterPosition();
        int toPosition = target.getAdapterPosition();
        if (fromPosition < contentList.size() && toPosition < contentList.size()) {
            // 交换数据位置
            Collections.swap(contentList, fromPosition, toPosition);
            // 刷新位置交换
            notifyItemMoved(fromPosition, toPosition);
        }
    }

    @Override
    public void onItemSelect(RecyclerView.ViewHolder source) {
        // 当拖拽选中时放大选中的view
        source.itemView.setScaleX(1.05f);
        source.itemView.setScaleY(1.05f);
    }

    @Override
    public void onItemClear(RecyclerView.ViewHolder source) {
        // 拖拽结束后恢复view的状态
        source.itemView.setScaleX(1.0f);
        source.itemView.setScaleY(1.0f);
    }

    public void setContentList(Context context, List<DeviceBlockData> contentList) {
        this.context = context;
        this.contentList = contentList;
    }

    public List<DeviceBlockData> getContentList() {
        return this.contentList;
    }

    public static class DragHolder extends RecyclerView.ViewHolder {
        private TextView device_line_chart_title;
        private TextView device_name;
        private View device_status;
        private LineChart device_line_chart;
        private TextView device_bar_chart_title;
        private HorizontalBarChart device_bar_chart;

        public DragHolder(@NonNull View itemView) {
            super(itemView);

            // 初始化组件
            init_view(itemView);
        }

        private void init_view(View itemView) {
            device_line_chart_title = itemView.findViewById(R.id.device_line_chart_title);
            device_name = itemView.findViewById(R.id.device_name);
            device_status = itemView.findViewById(R.id.device_status);
            device_line_chart = itemView.findViewById(R.id.device_line_chart);
            device_bar_chart_title = itemView.findViewById(R.id.device_bar_chart_title);
            device_bar_chart = itemView.findViewById(R.id.device_bar_chart);
            
            init_line_chart();
            
            init_bar_chart();
        }

        private void init_line_chart() {
            device_line_chart.setTouchEnabled(false);
            device_line_chart.setPinchZoom(true);
            device_line_chart.setDescription(null);
            device_line_chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            device_line_chart.getXAxis().setDrawGridLines(false);
        }

        private void init_bar_chart() {
            device_bar_chart.setTouchEnabled(false);
            device_bar_chart.setPinchZoom(true);
            device_bar_chart.setDescription(null);
            device_bar_chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
            device_bar_chart.getAxisLeft().setDrawGridLines(false);
            device_bar_chart.getAxisRight().setDrawGridLines(false);
        }
    }

    private class TempLineXAxisValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return (int)value + ":00";
        }
    }

    private class TempLineYAxisValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return value + "℃";
        }
    }

    private class TempBarXAxisValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return AxisName[(int)value];
        }
    }

    private class HumiLineXAxisValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return (int)value + ":00";
        }
    }

    private class HumiLineYAxisValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return value + "%";
        }
    }

    private class HumiBarXAxisValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return AxisName[(int)value];
        }
    }

    private class SmokLineXAxisValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return (int)value + ":00";
        }
    }

    private class SmokLineYAxisValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return value + "%";
        }
    }

    private class SmokBarXAxisValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return AxisName[(int)value];
        }
    }
}
