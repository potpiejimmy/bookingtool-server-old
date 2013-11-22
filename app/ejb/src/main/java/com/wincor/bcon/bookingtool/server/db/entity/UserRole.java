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
@Table(name="user_role")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UserRole.findByUserName", query = "SELECT p FROM UserRole p WHERE p.userName = :userName"),
    @NamedQuery(name = "UserRole.deleteByUserName", query = "DELETE FROM UserRole r WHERE r.userName = :userName")})
public class UserRole implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column
	@NotNull
	@Size(min = 1, max=45)
	private String role;

	@Id
	@Column(name="user_name")
	@NotNull
	@Size(min = 5, max=45)
	private String userName;

	public UserRole() {
	}

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}