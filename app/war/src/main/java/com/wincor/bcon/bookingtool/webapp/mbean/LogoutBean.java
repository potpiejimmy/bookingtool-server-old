package com.wincor.bcon.bookingtool.webapp.mbean;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import com.wincor.bcon.bookingtool.webapp.util.WebUtils;
import java.io.IOException;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;

@Named
@RequestScoped
public class LogoutBean {

    public void logout() {
        try {
            WebUtils.getHttpServletRequest().logout();
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            WebUtils.redirect("/");
        } catch (ServletException e) {
        } catch (IOException e) {
        }
    }
}
