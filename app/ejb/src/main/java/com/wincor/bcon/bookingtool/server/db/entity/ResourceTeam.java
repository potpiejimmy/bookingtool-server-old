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
 * The persistent class for the resource_team database table.
 * 
 */
@Entity
@Table(name="resource_team")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ResourceTeam.findAll", query = "SELECT p FROM ResourceTeam p ORDER BY p.name"),
    @NamedQuery(name = "ResourceTeam.findByName", query = "SELECT p FROM ResourceTeam p WHERE p.name = :name"),
    @NamedQuery(name = "ResourceTeam.findByManager", query = "SELECT p FROM ResourceTeam p WHERE p.manager = :manager"),
    @NamedQuery(name = "ResourceTeam.findByDomainId", query = "SELECT p FROM ResourceTeam p WHERE p.domainId = :domainId ORDER BY p.name"),
    @NamedQuery(name = "ResourceTeam.findByDomainUser", query = "SELECT p FROM ResourceTeam p,Domain d,DomainUser u WHERE p.domainId = d.id AND d.id = u.domainId AND u.userName=:userName ORDER BY p.name")})
public class ResourceTeam implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column
	private Integer id;

	@Column(name="domain_id")
	@NotNull
	private Integer domainId;

	@Column
	@NotNull
	@Size(min = 1, max=64)
	private String name;

	@Column
	@NotNull
	@Size(min = 1, max=45)
	private String manager;

	public ResourceTeam() {
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

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
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
        if (!(object instanceof ResourceTeam)) {
            return false;
        }
        ResourceTeam other = (ResourceTeam) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
	
	

}