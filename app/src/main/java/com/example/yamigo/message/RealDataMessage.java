package com.example.yamigo.message;

public class RealDataMessage {
    private double temp;
    private double humi;
    private boolean smok;

    public RealDataMessage(double temp, double humi, boolean smok) {
        this.temp = temp;
        this.humi = humi;
        this.smok = smok;
    }

    public RealDataMessage() {
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getHumi() {
        return humi;
    }

    public void setHumi(double humi) {
        this.humi = humi;
    }

    public boolean isSmok() {
        return smok;
    }

    public void setSmok(boolean smok) {
        this.smok = smok;
    }

    @Override
    public String toString() {
        return "RealDataMessage{" +
                "temp=" + temp +
                ", humi=" + humi +
                ", smok=" + smok +
                '}';
    }
}
