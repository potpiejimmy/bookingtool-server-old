package com.wincor.bcon.bookingtool.webapp.mbean;

import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.wincor.bcon.bookingtool.server.db.entity.Domain;
import com.wincor.bcon.bookingtool.server.ejb.DomainsEJBLocal;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;
import java.util.ArrayList;
import org.primefaces.model.DualListModel;

@Named
@SessionScoped
public class DomainsBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	private DomainsEJBLocal ejb;
	
	private Domain currentDomain = null;

        private DualListModel<String> assignedUsers = new DualListModel<String>();
    
	public void clear() {
                newDomain();
	}
        
        public void newDomain() {
		currentDomain = new Domain();
                assignedUsers.setSource(ejb.getAllUsers());
                assignedUsers.setTarget(new ArrayList<String>());
        }

	public Domain getCurrentDomain() {
		return currentDomain;
	}
	
	public List<Domain> getDomains() {
		return ejb.getDomains();
	}
	
        public DualListModel<String> getAssignedUsers() {
                if (currentDomain == null) newDomain();
                return assignedUsers;
        }

        public void setAssignedUsers(DualListModel<String> assignedUsers) {
                this.assignedUsers = assignedUsers;
        }

        public String getAssignedUsersAsString(Domain domain) {
            StringBuilder stb = new StringBuilder();
            for (String user : ejb.getAssignedUsers(domain.getId())) {
                if (stb.length() > 0) stb.append(", ");
                stb.append(user);
            }
            return stb.toString();
        }
        
	public void save() {
		ejb.saveDomain(currentDomain, assignedUsers.getTarget());
		clear();
	}
	
	public void edit(Domain p) {
		currentDomain = p;

                List<String> source = new ArrayList<String>(ejb.getAllUsers());
                List<String> target = ejb.getAssignedUsers(currentDomain.getId());
                source.removeAll(target);
                assignedUsers.setSource(source);
                assignedUsers.setTarget(target);
	}
	
	public void delete(Domain p) {
		try {
			ejb.deleteDomain(p.getId());
                        clear();
		} catch (Exception ex) {
			WebUtils.addFacesMessage(ex);
		}
	}
}
