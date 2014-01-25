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
@Table(name="resource_team_member")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ResourceTeamMember.findByResourceTeamId", query = "SELECT f FROM ResourceTeamMember f WHERE f.resourceTeamId=:resourceTeamId ORDER BY f.userName"),
    @NamedQuery(name = "ResourceTeamMember.findByUserName", query = "SELECT f FROM ResourceTeamMember f WHERE f.userName=:userName"),
    @NamedQuery(name = "ResourceTeamMember.deleteByResourceTeamId", query = "DELETE FROM ResourceTeamMember f WHERE f.resourceTeamId=:resourceTeamId")})
public class ResourceTeamMember implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
	@Column(name="resource_team_id")
	private Integer resourceTeamId;

        @Id
	@NotNull
	@Column(name="user_name")
	private String userName;

	public ResourceTeamMember() {
	}

    public Integer getResourceTeamId() {
        return resourceTeamId;
    }

    public void setResourceTeamId(Integer resourceTeamId) {
        this.resourceTeamId = resourceTeamId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

        
}