package com.wincor.bcon.bookingtool.server.db.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name="forecast_budget_plan")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ForecastBudgetPlan.findByForecastId", query = "SELECT f FROM ForecastBudgetPlan f WHERE f.forecastId=:forecastId ORDER BY f.position"),
    @NamedQuery(name = "ForecastBudgetPlan.deleteByForecastId", query = "DELETE FROM ForecastBudgetPlan f WHERE f.forecastId=:forecastId")})
public class ForecastBudgetPlan implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
	@Column(name="forecast_id")
	private Integer forecastId;

        @Id
	@NotNull
	@Column(name="budget_plan_id")
	private Integer budgetPlanId;

	@Column
	private Integer position;

	public ForecastBudgetPlan() {
	}

    public Integer getForecastId() {
        return forecastId;
    }

    public void setForecastId(Integer forecastId) {
        this.forecastId = forecastId;
    }

    public Integer getBudgetPlanId() {
        return budgetPlanId;
    }

    public void setBudgetPlanId(Integer budgetPlanId) {
        this.budgetPlanId = budgetPlanId;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }
}