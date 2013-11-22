package com.wincor.bcon.bookingtool.server.db.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * The persistent class for the booking_template database table.
 * 
 */
@Entity
@Table(name="budget_plan_item")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "BudgetPlanItem.findAll", query = "SELECT i FROM BudgetPlanItem i"),
    @NamedQuery(name = "BudgetPlanItem.findByBudgetId", query = "SELECT i FROM BudgetPlanItem i WHERE i.budgetId = :budgetId"),
    @NamedQuery(name = "BudgetPlanItem.findByBudgetIdAndPeriod", query = "SELECT i FROM BudgetPlanItem i WHERE i.budgetId = :budgetId AND i.period = :period"),
    @NamedQuery(name = "BudgetPlanItem.deleteByBudgetId", query = "DELETE FROM BudgetPlanItem i WHERE i.budgetId = :budgetId")})

public class BudgetPlanItem implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column
	private Integer id;

	@Column(name="budget_id")
	@NotNull
	private Integer budgetId;

	@Column
	@NotNull
	private Integer period;

	@Column
	@NotNull
	private Integer minutes;


	public BudgetPlanItem() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

    public Integer getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Integer budgetId) {
        this.budgetId = budgetId;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

	
	@Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof BudgetPlanItem)) {
            return false;
        }
        BudgetPlanItem other = (BudgetPlanItem) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

}