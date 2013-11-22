/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wincor.bcon.bookingtool.webapp.mbean.vo;

import com.wincor.bcon.bookingtool.server.db.entity.Budget;
import java.util.HashMap;
import java.util.Map;

public class BudgetPlanVo
{
    private final Budget budget;
    private final Map<Integer,Number> values;

    public BudgetPlanVo(Budget b) {
        this.budget = b;
        this.values = new HashMap<Integer,Number>();
    }

    public Budget getBudget() {
        return budget;
    }
    
    public Map<Integer,Number> getValues() {
        return values;
    }
}
