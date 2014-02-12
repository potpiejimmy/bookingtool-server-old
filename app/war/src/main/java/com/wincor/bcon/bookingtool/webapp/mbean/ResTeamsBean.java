package com.wincor.bcon.bookingtool.webapp.mbean;

import com.wincor.bcon.bookingtool.server.db.entity.Domain;
import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.wincor.bcon.bookingtool.server.db.entity.ResourceTeam;
import com.wincor.bcon.bookingtool.server.ejb.DomainsEJBLocal;
import com.wincor.bcon.bookingtool.server.ejb.ResourceTeamsEJBLocal;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;
import java.util.ArrayList;
import javax.faces.model.SelectItem;
import org.primefaces.model.DualListModel;

@Named
@SessionScoped
public class ResTeamsBean implements Serializable {

	private static final long serialVersionUID = 1L;

        @EJB
        private ResourceTeamsEJBLocal ejb;
    
	@EJB
	private DomainsEJBLocal domainsEjb;
	
	private ResourceTeam currentTeam = null;

        private DualListModel<String> assignedMembers = new DualListModel<String>();
    
	public void clear() {
		newResourceTeam();
	}

        public void newResourceTeam() {
		currentTeam = new ResourceTeam();
                updateMembersPickList();
        }
        
        public void updateMembersPickList() {
                List<String> users = currentTeam.getDomainId() != null ?
                        new ArrayList<String>(domainsEjb.getAssignedUsers(currentTeam.getDomainId())) :
                        new ArrayList<String>();
                List<String> members = new ArrayList<String>();
                if (currentTeam.getId() != null) {
                    members.addAll(ejb.getAssignedUsers(currentTeam.getId()));
                }
                users.removeAll(members);
                assignedMembers.setSource(users);
                assignedMembers.setTarget(members);
        }
        
	public ResourceTeam getCurrentResourceTeam() {
                if (currentTeam == null) newResourceTeam();
		return currentTeam;
	}
	
        public DualListModel<String> getAssignedMembers() {
                if (currentTeam == null) newResourceTeam();
                return assignedMembers;
        }

        public void setAssignedMembers(DualListModel<String> assignedMembers) {
                this.assignedMembers = assignedMembers;
        }

        public String getAssignedMemberList(Integer projectId) {
                String mgrList = ejb.getAssignedUsers(projectId).toString(); 
                return mgrList.substring(1, mgrList.indexOf(']'));
        }

	public List<ResourceTeam> getResourceTeams() {
		return ejb.getResourceTeams();
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
	
	public void save() {
            try {
		ejb.saveResourceTeam(currentTeam, assignedMembers.getTarget());
		newResourceTeam();
            } catch (Exception ex) {
                WebUtils.addFacesMessage(ex);
            }
	}
	
	public void edit(ResourceTeam p) {
		currentTeam = p;
                updateMembersPickList();
	}
	
	public void delete(ResourceTeam p) {
		try {
			ejb.deleteResourceTeam(p.getId());
		} catch (Exception ex) {
			WebUtils.addFacesMessage(ex);
		}
	}
}
