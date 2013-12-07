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
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name="forecast")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Forecast.findAll", query = "SELECT f FROM Forecast f ORDER BY f.name")})
public class Forecast implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column
	private Integer id;

	@Column
	@NotNull
	@Size(min = 1, max=64)
	private String name;

	@Column(name="fiscal_year")
	@NotNull
	private Integer fiscalYear;

	@Column(name="fc_budget_cents")
	@NotNull
	private Integer fcBudgetCents;

	@Column(name="cents_per_hour")
	@NotNull
	private Integer centsPerHour;

	@Column(name="cents_per_hour_ifrs")
	@NotNull
	private Integer centsPerHourIfrs;

	public Forecast() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public Integer getFiscalYear() {
        return fiscalYear;
    }

    public void setFiscalYear(Integer fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    public Integer getFcBudgetCents() {
        return fcBudgetCents;
    }

    public void setFcBudgetCents(Integer fcBudgetCents) {
        this.fcBudgetCents = fcBudgetCents;
    }

    public Integer getCentsPerHour() {
        return centsPerHour;
    }

    public void setCentsPerHour(Integer centsPerHour) {
        this.centsPerHour = centsPerHour;
    }

    public Integer getCentsPerHourIfrs() {
        return centsPerHourIfrs;
    }

    public void setCentsPerHourIfrs(Integer centsPerHourIfrs) {
        this.centsPerHourIfrs = centsPerHourIfrs;
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
        if (!(object instanceof Forecast)) {
            return false;
        }
        Forecast other = (Forecast) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
}