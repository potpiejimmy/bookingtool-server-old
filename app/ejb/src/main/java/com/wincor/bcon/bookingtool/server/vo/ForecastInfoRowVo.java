/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.server.vo;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the forecast info objects for an entire forecast row
 */
public class ForecastInfoRowVo implements java.io.Serializable {
    
    private BudgetInfoVo budgetInfo = null;
    
    private Map<Integer,ForecastInfoVo> months = null;
    
    public ForecastInfoRowVo(BudgetInfoVo budgetInfo) {
        this.budgetInfo = budgetInfo;
        this.months = new HashMap<Integer,ForecastInfoVo>();
    }

    public BudgetInfoVo getBudgetInfo() {
        return budgetInfo;
    }

    public Map<Integer, ForecastInfoVo> getMonths() {
        return months;
    }
}
