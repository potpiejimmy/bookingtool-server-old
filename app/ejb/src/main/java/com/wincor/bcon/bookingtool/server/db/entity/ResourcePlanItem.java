package com.wincor.bcon.bookingtool.server.db.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The persistent class for the resource_plan_item database table.
 * 
 */
@Entity
@Table(name="resource_plan_item")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ResourcePlanItem.findByUserAndDateRange", query = "SELECT i FROM ResourcePlanItem i WHERE i.userName=:userName AND i.day>=:from AND i.day<=:to ORDER BY i.day"),
    @NamedQuery(name = "ResourcePlanItem.deleteByUserAndDateRange", query = "DELETE FROM ResourcePlanItem i WHERE i.userName=:userName AND i.day>=:from AND i.day<=:to")})
public class ResourcePlanItem implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;

	@Column(name="user_name")
	@NotNull
	@Size(min = 1, max=45)
	private String userName;

	@Column
	@NotNull
	@Temporal(TemporalType.DATE)
	private Date day;

	@Column
	@NotNull
	private char avail;

	@Column(name="project_id")
	@Null
	private Integer projectId;

	public ResourcePlanItem() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getDay() {
		return this.day;
	}

	public void setDay(Date day) {
		this.day = day;
	}

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public char getAvail() {
        return avail;
    }

    public void setAvail(char avail) {
        this.avail = avail;
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
        if (!(object instanceof ResourcePlanItem)) {
            return false;
        }
        ResourcePlanItem other = (ResourcePlanItem) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

}