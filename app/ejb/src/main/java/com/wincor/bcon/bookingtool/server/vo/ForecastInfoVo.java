/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.server.vo;

/**
 * Holds the forecast information for one specific forecast cell
 */
public class ForecastInfoVo implements java.io.Serializable {

    private int period = 0;
    private int plannedMinutes = 0;
    private int bookedMinutes = 0;

    public ForecastInfoVo() {
    }
    
    public ForecastInfoVo(int period, int plannedMinutes, int bookedMinutes) {
        this.period = period;
        this.plannedMinutes = plannedMinutes;
        this.bookedMinutes = bookedMinutes;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public int getPlannedMinutes() {
        return plannedMinutes;
    }

    public void setPlannedMinutes(int plannedMinutes) {
        this.plannedMinutes = plannedMinutes;
    }

    public int getBookedMinutes() {
        return bookedMinutes;
    }

    public void setBookedMinutes(int bookedMinutes) {
        this.bookedMinutes = bookedMinutes;
    }
}
