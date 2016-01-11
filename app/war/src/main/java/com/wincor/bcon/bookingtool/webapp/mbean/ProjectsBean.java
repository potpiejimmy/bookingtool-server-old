package com.wincor.bcon.bookingtool.webapp.mbean;

import com.wincor.bcon.bookingtool.server.db.entity.Domain;
import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.wincor.bcon.bookingtool.server.db.entity.Project;
import com.wincor.bcon.bookingtool.server.ejb.DomainsEJB;
import com.wincor.bcon.bookingtool.server.ejb.ProjectsEJB;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;
import java.util.ArrayList;
import javax.faces.model.SelectItem;
import org.primefaces.model.DualListModel;

@Named
@SessionScoped
public class ProjectsBean implements Serializable {

	private static final long serialVersionUID = 1L;

        @EJB
        private ProjectsEJB ejb;
    
	@EJB
	private DomainsEJB domainsEjb;
	
	private Project currentProject = null;
        private Project selectedForDrop = null;

        private DualListModel<String> assignedManagers = new DualListModel<String>();
    
	public void clear() {
		newProject();
	}

        public void newProject() {
		currentProject = new Project();
                updateManagersPickList();
        }
        
        public void updateManagersPickList() {
                List<String> users = currentProject.getDomainId() != null ?
                        new ArrayList<String>(domainsEjb.getAssignedUsersWithAdminRole(currentProject.getDomainId())) :
                        new ArrayList<String>();
                List<String> managers = new ArrayList<String>();
                if (currentProject.getId() != null) {
                    managers.addAll(ejb.getAssignedManagers(currentProject.getId()));
                } else {
                    managers.add(WebUtils.getCurrentPerson()); // add myself as default mgr
                }
                users.removeAll(managers);
                assignedManagers.setSource(users);
                assignedManagers.setTarget(managers);
        }
        
        public boolean isProjectManagedByMe(Project p) {
            return (WebUtils.getHttpServletRequest().isUserInRole("superuser") ||
                    ejb.getAssignedManagers(p.getId()).contains(WebUtils.getCurrentPerson()));
        }

	public Project getCurrentProject() {
                if (currentProject == null) newProject();
		return currentProject;
	}
	
        public DualListModel<String> getAssignedManagers() {
                if (currentProject == null) newProject();
                return assignedManagers;
        }

        public void setAssignedManagers(DualListModel<String> assignedManagers) {
                this.assignedManagers = assignedManagers;
        }

        public String getAssignedManagerList(Integer projectId) {
                String mgrList = ejb.getAssignedManagers(projectId).toString(); 
                return mgrList.substring(1, mgrList.indexOf(']'));
        }

	public List<Project> getProjects() {
		return ejb.getProjectsForAdmin();
	}
        
        public List<SelectItem> getStatusItems() {
		List<SelectItem> result = new ArrayList<SelectItem>();
                result.add(new SelectItem((byte)0, "Active"));
                result.add(new SelectItem((byte)1, "Hidden"));
		return result;
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
		ejb.saveProject(currentProject, assignedManagers.getTarget());
		newProject();
	}
	
	public void edit(Project p) {
		currentProject = p;
                updateManagersPickList();
	}
	
	public void delete(Project p) {
		try {
			ejb.deleteProject(p.getId());
		} catch (Exception ex) {
			WebUtils.addFacesMessage(ex);
		}
	}

        public Project getSelectedForDrop() {
            return selectedForDrop;
        }

        public void setSelectedForDrop(Project selectedForDrop) {
            this.selectedForDrop = selectedForDrop;
        }
        
	public void dropProject() {
            try {
                if (selectedForDrop != null) {
                    ejb.dropProject(selectedForDrop.getId());
                }
            } catch (Exception ex) {
                    WebUtils.addFacesMessage(ex);
            }
            selectedForDrop = null;
	}
}
