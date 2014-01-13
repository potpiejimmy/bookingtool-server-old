package com.wincor.bcon.bookingtool.webapp.mbean;

import com.wincor.bcon.bookingtool.server.db.entity.Domain;
import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.wincor.bcon.bookingtool.server.db.entity.Project;
import com.wincor.bcon.bookingtool.server.ejb.DomainsEJBLocal;
import com.wincor.bcon.bookingtool.server.ejb.ProjectsEJBLocal;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;
import java.util.ArrayList;
import javax.faces.model.SelectItem;

@Named
@SessionScoped
public class ProjectsBean implements Serializable {

	private static final long serialVersionUID = 1L;

        @EJB
        private ProjectsEJBLocal ejb;
    
	@EJB
	private DomainsEJBLocal domainsEjb;
	
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
        
        public List<SelectItem> getDomainItems() {
		List<Domain> domains = domainsEjb.getDomains();
		List<SelectItem> result = new ArrayList<SelectItem>(domains.size() + 1);
                result.add(new SelectItem(0, "<Please choose>"));
		for (Domain d : domains) {
			result.add(new SelectItem(d.getId(), d.getName()));
		}
		return result;
	}
	
	public Domain getDomain(Integer domainId) {
		return domainsEjb.getDomain(domainId);
	}
	
	public void save() {
		ejb.saveProject(currentProject, new ArrayList<String>());
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
