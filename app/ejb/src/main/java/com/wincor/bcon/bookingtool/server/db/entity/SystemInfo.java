package com.wincor.bcon.bookingtool.server.db.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(name="system_info")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "SystemInfo.findAll", query = "SELECT i FROM SystemInfo i ORDER BY i.id")})
public class SystemInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column
	@NotNull
	@Size(min = 1, max=45)
	private String id;

	@Column
	@Size(min = 1, max=255)
	private String value;

	public SystemInfo() {
	}

	public SystemInfo(String id, String value) {
            this.id = id;
            this.value = value;
	}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
        
	@Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SystemInfo)) {
            return false;
        }
        SystemInfo other = (SystemInfo) object;
        return (this.id != null || other.id == null) && (this.id == null || this.id.equals(other.id));
    }
}