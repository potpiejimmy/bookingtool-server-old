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
@Table(name="project_manager")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ProjectManager.findByProjectId", query = "SELECT f FROM ProjectManager f WHERE f.projectId=:projectId ORDER BY f.userName"),
    @NamedQuery(name = "ProjectManager.findByUserName", query = "SELECT f FROM ProjectManager f,Project p WHERE f.projectId=p.id AND f.userName=:userName ORDER BY p.name"),
    @NamedQuery(name = "ProjectManager.deleteByProjectId", query = "DELETE FROM ProjectManager f WHERE f.projectId=:projectId")})
public class ProjectManager implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@NotNull
	@Column(name="project_id")
	private Integer projectId;

        @Id
	@NotNull
	@Column(name="user_name")
	private String userName;

	public ProjectManager() {
	}

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

        
}