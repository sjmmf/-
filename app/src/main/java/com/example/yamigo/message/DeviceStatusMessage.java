package com.example.yamigo.message;

public class DeviceStatusMessage {
    boolean temp_status;
    boolean humi_status;
    boolean smok_status;

    public DeviceStatusMessage() {
    }

    public DeviceStatusMessage(boolean temp_status, boolean humi_status, boolean smok_status) {
        this.temp_status = temp_status;
        this.humi_status = humi_status;
        this.smok_status = smok_status;
    }

    public boolean isTemp_status() {
        return temp_status;
    }

    public void setTemp_status(boolean temp_status) {
        this.temp_status = temp_status;
    }

    public boolean isHumi_status() {
        return humi_status;
    }

    public void setHumi_status(boolean humi_status) {
        this.humi_status = humi_status;
    }

    public boolean isSmok_status() {
        return smok_status;
    }

    public void setSmok_status(boolean smok_status) {
        this.smok_status = smok_status;
    }

    @Override
    public String toString() {
        return "DeviceStatusMessage{" +
                "temp_status=" + temp_status +
                ", humi_status=" + humi_status +
                ", smok_status=" + smok_status +
                '}';
    }
}
