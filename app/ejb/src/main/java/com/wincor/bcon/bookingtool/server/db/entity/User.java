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
@Table(name="user")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "User.findAll", query = "SELECT p FROM User p ORDER BY p.name"),
    @NamedQuery(name = "User.findByName", query = "SELECT p FROM User p WHERE p.name = :name")})
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column
	@NotNull
	@Size(min = 5, max=45)
	private String name;

	@Column
	@NotNull
	@Size(min = 4, max=32)
	private String password;

	@Column(name="pw_status")
	@NotNull
	private byte pwStatus;

	public User() {
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public byte getPwStatus() {
        return pwStatus;
    }

    public void setPwStatus(byte pwStatus) {
        this.pwStatus = pwStatus;
    }

	@Override
    public int hashCode() {
        int hash = 0;
        hash += (name != null ? name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the name fields are not set
        if (!(object instanceof User)) {
            return false;
        }
        User other = (User) object;
        if ((this.name == null && other.name != null) || (this.name != null && !this.name.equals(other.name))) {
            return false;
        }
        return true;
    }

}