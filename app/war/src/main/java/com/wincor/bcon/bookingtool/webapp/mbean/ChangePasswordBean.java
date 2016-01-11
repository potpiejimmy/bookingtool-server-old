package com.wincor.bcon.bookingtool.webapp.mbean;

import com.wincor.bcon.bookingtool.server.ejb.UsersEJB;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import com.wincor.bcon.bookingtool.webapp.util.WebUtils;

@Named
@RequestScoped
public class ChangePasswordBean {


    @EJB
    private UsersEJB ejb;

    private String oldPassword;
    private String newPassword;

    public void checkChangePasswordNeeded() {
        try {
            if (isChangePasswordNeeded()) WebUtils.redirect("/restricted/changepassword.xhtml");
        } catch (Exception ex) {
        	// redirect failed? ignore, just redirect the next time.
        }
    }
    
    public boolean isChangePasswordNeeded() {
        return (ejb.getCurrentUser().getPwStatus() == 0);
    }
    
    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
        
    public String save() {
        try {
            ejb.changePassword(oldPassword, newPassword);
            WebUtils.addFacesMessage("Password was changed successfully.");
            HttpServletRequest req = WebUtils.getHttpServletRequest();
            String userName = WebUtils.getCurrentPerson(req);
            req.logout();
            req.login(userName, newPassword);
            
            return "maininputpanel";
        } catch (Exception ex) {
            WebUtils.addFacesMessage(ex);
            return null;
        }
    }

}
