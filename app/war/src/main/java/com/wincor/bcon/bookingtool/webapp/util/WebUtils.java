/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wincor.bcon.bookingtool.webapp.util;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

/**
 * Contains static utility methods.
 */
public class WebUtils
{
	public static class SelectItemComparator implements Comparator<SelectItem>
	{
		@Override
		public int compare(SelectItem arg0, SelectItem arg1) {
			return arg0.getLabel().compareTo(arg1.getLabel());
		}
	}
	
    public static String getCurrentPerson(HttpServletRequest hsr)
    {
        Principal p = hsr.getUserPrincipal();
        return (p==null ? null : p.getName());
    }

    public static HttpServletRequest getHttpServletRequest()
    {
        return (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
    }
    
    public static String getCurrentPerson()
    {
        return getCurrentPerson(getHttpServletRequest());
    }
    
    public static void addFacesMessage(String msg)
    {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(msg));
    }

    public static void addFacesMessage(Throwable ex)
    {
    	FacesMessage facesMessage = handleCustomExceptions(ex);

    	if(facesMessage == null)
    	{
    		//no custom message found
    		while(ex.getCause() != null) ex = ex.getCause();
    		facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR,  ex.toString(), "");
    	}
    	FacesContext.getCurrentInstance().addMessage(null, facesMessage);
    }
    
    public static List<SelectItem> sortSelectItems(List<SelectItem> items)
    {
    	Collections.sort(items, new SelectItemComparator());
    	return items;
    }
    
    //print more meaningful error messages in some cases!
    private static FacesMessage handleCustomExceptions(Throwable ex) 
    {
    	FacesMessage customMessage = null;
    	if(ex != null)
    	{
	    	do
	    	{
	    		//cutom error message
	    		if(ex.getClass() != null && ex.getClass().getName().equals("org.hibernate.exception.ConstraintViolationException"))
	    		{
	    			customMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "ConstraintViolationException: ","Das Objekt kann nicht gelöscht werden, da noch Referenzen zu untergeordneten Objekten bestehen.");
	    		}
	    		
	    		//try to find the next cause!
	    		ex = ex.getCause();
	    	} while(ex != null);
    	}
    	return customMessage;
    }
    
    /**
     * Redirect to the given URL (context root relative)
     * @param relativeUrl url relative to context path, must start with a slash
     */
    public static void redirect(String relativeUrl) throws IOException
    {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        ctx.redirect(ctx.getRequestContextPath() + relativeUrl);
    }
    
    /**
     * Returns the default resource bundle of this application
     * @return default resource bundle
     */
    public static ResourceBundle getResBundle() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        return ctx.getApplication().getResourceBundle(ctx, "msg"); // "msg" specified in faces-config.xml
    }
}
