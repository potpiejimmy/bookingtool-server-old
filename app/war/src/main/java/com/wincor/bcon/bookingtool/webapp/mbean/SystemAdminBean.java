package com.wincor.bcon.bookingtool.webapp.mbean;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.primefaces.event.FileUploadEvent;

import com.wincor.bcon.bookingtool.server.ejb.SystemEJBLocal;
import com.wincor.bcon.bookingtool.webapp.util.WebUtils;

@Named
@SessionScoped
public class SystemAdminBean implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@EJB
	private SystemEJBLocal ejb;
	
	public void handleFileUpload(FileUploadEvent event) {
		try {
			ejb.deployEar(event.getFile().getContents());
			WebUtils.addFacesMessage("File " + event.getFile().getFileName() + " uploaded successfully.");  
		} catch (Exception ex) {
			WebUtils.addFacesMessage(ex);
		}
	}
        
        public String getSystemMsg() {
            return ejb.getSystemWarning();
        }

        public boolean isMaintenanceWarning() {
            return ejb.getSystemWarning() != null;
        }

        public void setMaintenanceWarning(boolean maintenanceWarning) {
            ejb.setSystemWarning(maintenanceWarning ?
                    "System is going down for maintenance. Please finish your work and log out." :
                    null);
        }
}
