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


/**
 * The persistent class for the budget database table.
 * 
 */
@Entity
@Table(name="budget")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Budget.findAll", query = "SELECT b FROM Budget b ORDER BY b.name"),
    @NamedQuery(name = "Budget.findByProjectId", query = "SELECT b FROM Budget b WHERE b.projectId = :projectId ORDER BY b.name"),
    @NamedQuery(name = "Budget.findRoots", query = "SELECT b FROM Budget b WHERE b.parentId IS NULL AND b.projectId = :projectId ORDER BY b.name"),
    @NamedQuery(name = "Budget.findByParentId", query = "SELECT b FROM Budget b WHERE b.parentId = :parentId ORDER BY b.name"),
    @NamedQuery(name = "Budget.getBookedMinutes", query = "SELECT SUM(b.minutes) FROM Booking b, BookingTemplate t WHERE b.bookingTemplateId=t.id AND t.budgetId=:budgetId"),
    @NamedQuery(name = "Budget.getBookedMinutesInPeriod", query = "SELECT SUM(b.minutes) FROM Booking b, BookingTemplate t WHERE b.bookingTemplateId=t.id AND t.budgetId=:budgetId AND b.day>=:from AND b.day<:to")})
public class Budget implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;

	@Column
	@NotNull
	private Integer minutes;

	@Column
	@NotNull
	@Size(min = 1, max=255)
	private String name;

	@Column(name="parent_id")
	private Integer parentId;

	@Column(name="project_id")
	@NotNull
	private Integer projectId;

	public Budget() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getMinutes() {
		return minutes;
	}

	public void setMinutes(Integer minutes) {
		this.minutes = minutes;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public void setProjectId(Integer projectId) {
		this.projectId = projectId;
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
        if (!(object instanceof Budget)) {
            return false;
        }
        Budget other = (Budget) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

}