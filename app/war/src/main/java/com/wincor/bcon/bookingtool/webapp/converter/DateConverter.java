package com.wincor.bcon.bookingtool.webapp.converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

@FacesConverter("dateConverter")
public class DateConverter implements Converter {
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd. MMM yyyy",Locale.ENGLISH);

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String submittedValue) {
		if (submittedValue.trim().equals("")) {  
            return null;  
        } else {  
            try {
            	
            	return dateFormat.parse(submittedValue);
            	
            } catch(ParseException exception) {  
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not a valid player"));  
            }  
        }  
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object value) {
		if (value == null || value.equals("") || !(value instanceof Date)) {  
            return "";  
        } else {
            return dateFormat.format((Date)value) ; 
        }  
	}

}
