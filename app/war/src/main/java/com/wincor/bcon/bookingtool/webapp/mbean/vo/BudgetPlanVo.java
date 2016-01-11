/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.webapp.mbean.vo;

import com.wincor.bcon.bookingtool.server.vo.BudgetInfoVo;
import java.util.HashMap;
import java.util.Map;

public class BudgetPlanVo
{
    private final BudgetInfoVo budget;
    private final Map<Integer,Number> planValues; // as entered, person days
    private final Map<Integer,Integer> usageValues; // minutes

    public BudgetPlanVo(BudgetInfoVo b) {
        this.budget = b;
        this.planValues = new HashMap<Integer,Number>();
        this.usageValues = new HashMap<Integer,Integer>();
    }

    public BudgetInfoVo getBudgetInfo() {
        return budget;
    }
    
    public Map<Integer,Number> getPlanValues() {
        return planValues;
    }

    public Map<Integer,Integer> getUsageValues() {
        return usageValues;
    }
}
