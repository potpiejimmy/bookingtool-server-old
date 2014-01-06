package com.wincor.bcon.bookingtool.server.db.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name="domain_user")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DomainUser.findByDomainId", query = "SELECT f FROM DomainUser f WHERE f.domainId=:domainId ORDER BY f.userName"),
    @NamedQuery(name = "DomainUser.findByUserName", query = "SELECT f FROM DomainUser f WHERE f.userName=:userName"),
    @NamedQuery(name = "DomainUser.deleteByDomainId", query = "DELETE FROM DomainUser f WHERE f.domainId=:domainId")})
public class DomainUser implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
	@Column(name="domain_id")
	private Integer domainId;

        @Id
	@NotNull
	@Column(name="user_name")
	private String userName;

	public DomainUser() {
	}

    public Integer getDomainId() {
        return domainId;
    }

    public void setDomainId(Integer domainId) {
        this.domainId = domainId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

        
}