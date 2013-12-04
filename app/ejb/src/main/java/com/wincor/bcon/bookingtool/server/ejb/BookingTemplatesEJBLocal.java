package com.wincor.bcon.bookingtool.server.ejb;

import java.util.List;

import javax.ejb.Local;

import com.wincor.bcon.bookingtool.server.db.entity.BookingTemplate;

 @Local
public interface BookingTemplatesEJBLocal {

	/**
	 * Returns all booking templates in the "booking_template" table
	 * @return list of booking templates
	 */
	public List<BookingTemplate> getBookingTemplates();
	
	/**
	 * Returns the booking template with the given ID
	 * @param bookingTemplateId a booking template ID
	 * @return the booking template
	 */
	public BookingTemplate getBookingTemplate(int bookingTemplateId);
	
	/**
	 * Returns the booking templates associated to the given project ID
	 * @param projectId a project ID
	 * @return a list of booking templates
	 */
	public List<BookingTemplate> getBookingTemplatesByProjectId(int projectId);
	
	/**
	 * Returns the booking templates associated with the given budget ID
	 * @param budgetId a budget ID
	 * @return a list of booking templates
	 */
	public List<BookingTemplate> getBookingTemplatesByBudgetId(int budgetId);
	
	/**
	 * Saves or updates the given booking template
	 * @param bookingTemplate a booking template
	 */
	public void saveBookingTemplate(BookingTemplate bookingTemplate);
	
	/**
	 * Removes the booking template with the given ID
	 * @param bookingTemplateId a booking template ID
	 */
	public void deleteBookingTemplate(int bookingTemplateId);

	/**
	 * Returns the list of templates matching the given search criteria
	 * @param searchString a search string
	 */
	public List<BookingTemplate> findBookingTemplates(String searchString);
}
