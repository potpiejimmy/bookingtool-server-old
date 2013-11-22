package com.wincor.bcon.bookingtool.server.vo;

import java.io.Serializable;

public class AutoCreateInfoVo implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private int projectId;
	private int parentBudgetId;
	private int specBudgetId;
	private String pspTemplate;
	private String pspNameTemplate;
	private String mksNo;
	private String mksName;
	private int minutesDev;
	private int minutesQA;
	
	public int getProjectId() {
		return projectId;
	}
	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}
	public int getParentBudgetId() {
		return parentBudgetId;
	}
	public void setParentBudgetId(int parentBudgetId) {
		this.parentBudgetId = parentBudgetId;
	}
	public int getSpecBudgetId() {
		return specBudgetId;
	}
	public void setSpecBudgetId(int specBudgetId) {
		this.specBudgetId = specBudgetId;
	}
	public String getPspTemplate() {
		return pspTemplate;
	}
	public void setPspTemplate(String pspTemplate) {
		this.pspTemplate = pspTemplate;
	}
	public String getPspNameTemplate() {
		return pspNameTemplate;
	}
	public void setPspNameTemplate(String pspNameTemplate) {
		this.pspNameTemplate = pspNameTemplate;
	}
	public String getMksNo() {
		return mksNo;
	}
	public void setMksNo(String mksNo) {
		this.mksNo = mksNo;
	}
	public String getMksName() {
		return mksName;
	}
	public void setMksName(String mksName) {
		this.mksName = mksName;
	}
	public int getMinutesDev() {
		return minutesDev;
	}
	public void setMinutesDev(int minutesDev) {
		this.minutesDev = minutesDev;
	}
	public int getMinutesQA() {
		return minutesQA;
	}
	public void setMinutesQA(int minutesQA) {
		this.minutesQA = minutesQA;
	}
	
}
