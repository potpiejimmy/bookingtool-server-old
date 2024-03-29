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
 * The persistent class for the project database table.
 * 
 */
@Entity
@Table(name="project")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Project.findAll", query = "SELECT p FROM Project p ORDER BY p.name"),
    @NamedQuery(name = "Project.findByName", query = "SELECT p FROM Project p WHERE p.name = :name"),
    @NamedQuery(name = "Project.findByDomainId", query = "SELECT p FROM Project p WHERE p.domainId = :domainId ORDER BY p.name"),
    @NamedQuery(name = "Project.findByDomainUser", query = "SELECT p FROM Project p,Domain d,DomainUser u WHERE p.domainId = d.id AND d.id = u.domainId AND u.userName=:userName ORDER BY p.name"),
    @NamedQuery(name = "Project.findByDomainUserActive", query = "SELECT p FROM Project p,Domain d,DomainUser u WHERE p.domainId = d.id AND d.id = u.domainId AND u.userName=:userName AND p.status=0 ORDER BY p.name")})
public class Project implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column
	private Integer id;

	@Column
	@NotNull
	@Size(min = 1, max=64)
	private String name;

	@Column(name="domain_id")
	@NotNull
	private Integer domainId;

        @Column
	@NotNull
	private byte status;
        
	@Column
	@Size(min = 0, max=45)
	private String psp;

	public Project() {
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

    public Integer getDomainId() {
        return domainId;
    }

    public void setDomainId(Integer domainId) {
        this.domainId = domainId;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public String getPsp() {
        return psp;
    }

    public void setPsp(String psp) {
        this.psp = psp;
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
        if (!(object instanceof Project)) {
            return false;
        }
        Project other = (Project) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
	
	

}