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
@Table(name="budget_plan")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "BudgetPlan.findAll", query = "SELECT p FROM BudgetPlan p, Budget b WHERE p.budgetId=b.id ORDER BY b.projectId,b.name"),
    @NamedQuery(name = "BudgetPlan.findByProjectId", query = "SELECT bp FROM BudgetPlan bp,Budget b WHERE bp.budgetId=b.id AND b.projectId=:projectId ORDER BY b.name"),
    @NamedQuery(name = "BudgetPlan.findByDomainId", query = "SELECT bp FROM BudgetPlan bp,Budget b,Project p WHERE bp.budgetId=b.id AND b.projectId=p.id AND p.domainId=:domainId ORDER BY b.projectId,b.name")})
public class BudgetPlan implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column
	private Integer id;

	@Column(name="budget_id")
	@NotNull
	private Integer budgetId;

	@Column(name="plan_begin")
	@NotNull
	private Integer planBegin;

	@Column(name="plan_end")
	@NotNull
	private Integer planEnd;


	public BudgetPlan() {
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

    public Integer getPlanBegin() {
        return planBegin;
    }

    public void setPlanBegin(Integer planBegin) {
        this.planBegin = planBegin;
    }

    public Integer getPlanEnd() {
        return planEnd;
    }

    public void setPlanEnd(Integer planEnd) {
        this.planEnd = planEnd;
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
        if (!(object instanceof BudgetPlan)) {
            return false;
        }
        BudgetPlan other = (BudgetPlan) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

}