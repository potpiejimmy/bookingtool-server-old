package com.wincor.bcon.bookingtool.server.vo;

import com.wincor.bcon.bookingtool.server.db.entity.Budget;

public class BudgetInfoVo {

	private Budget budget = null;
	
        private String fullBudgetName = null;
        
	private int bookedMinutes = 0;
	private int bookedMinutesRecursive = 0;
        
        private int numberOfTemplates = 0;

	public BudgetInfoVo(Budget budget, int bookedMinutes) {
		this.budget = budget;
		this.bookedMinutes = bookedMinutes;
	}
	
	public Budget getBudget() {
		return budget;
	}

	public void setBudget(Budget budget) {
		this.budget = budget;
	}

	public int getBookedMinutes() {
		return bookedMinutes;
	}

	public void setBookedMinutes(int bookedMinutes) {
		this.bookedMinutes = bookedMinutes;
	}

	public int getBookedMinutesRecursive() {
		return bookedMinutesRecursive;
	}

	public void setBookedMinutesRecursive(int bookedMinutesRecursive) {
		this.bookedMinutesRecursive = bookedMinutesRecursive;
	}

        public int getNumberOfTemplates() {
            return numberOfTemplates;
        }

        public void setNumberOfTemplates(int numberOfTemplates) {
            this.numberOfTemplates = numberOfTemplates;
        }

        public String getFullBudgetName() {
            return fullBudgetName;
        }

        public void setFullBudgetName(String fullBudgetName) {
            this.fullBudgetName = fullBudgetName;
        }
}
