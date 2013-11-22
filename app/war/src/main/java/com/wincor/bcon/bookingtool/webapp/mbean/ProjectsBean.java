package com.wincor.bcon.bookingtool.webapp.mbean;

import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.wincor.bcon.bookingtool.server.db.entity.Project;
import com.wincor.bcon.bookingtool.server.ejb.BudgetsEJBLocal;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;

@Named
@SessionScoped
public class ProjectsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	private BudgetsEJBLocal ejb;
	
	private Project currentProject = new Project();

	public void clear() {
		currentProject = new Project();
	}

	public Project getCurrentProject() {
		return currentProject;
	}
	
	public List<Project> getProjects() {
		return ejb.getProjects();
	}
	
	public void save() {
		ejb.saveProject(currentProject);
		currentProject = new Project();
	}
	
	public void edit(Project p) {
		currentProject = p;
	}
	
	public void delete(Project p) {
		try {
			ejb.deleteProject(p.getId());
		} catch (Exception ex) {
			WebUtils.addFacesMessage(ex);
		}
	}
}
