package com.example.yamigo.bean;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class DeviceBlockData {
    private String name;
    private ArrayList<Entry> line_values;
    private ArrayList<BarEntry> bar_values;

    public DeviceBlockData() {
    }

    public DeviceBlockData(String name, ArrayList<Entry> line_values, ArrayList<BarEntry> bar_values) {
        this.name = name;
        this.line_values = line_values;
        this.bar_values = bar_values;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Entry> getLine_values() {
        return line_values;
    }

    public void setLine_values(ArrayList<Entry> line_values) {
        this.line_values = line_values;
    }

    public ArrayList<BarEntry> getBar_values() {
        return bar_values;
    }

    public void setBar_values(ArrayList<BarEntry> bar_values) {
        this.bar_values = bar_values;
    }

    @Override
    public String toString() {
        return "DeviceBlockData{" +
                "name='" + name + '\'' +
                ", line_values=" + line_values +
                ", bar_values=" + bar_values +
                ", device_status=" +
                '}';
    }
}
