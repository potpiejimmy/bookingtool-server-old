package com.wincor.bcon.bookingtool.server.ejb;

import java.util.Date;
import java.util.List;

import javax.ejb.Local;

import com.wincor.bcon.bookingtool.server.db.entity.Booking;
import java.util.Map;

@Local
public interface BookingsEJBLocal {

	/**
	 * Returns the list of all bookings in the system (admin only)
	 * @return list of bookings
	 */
	public List<Booking> getBookings();

	/**
	 * Returns the list of all bookings for the given person name
	 * @param person a person name
	 * @return list of bookings
	 */
	public List<Booking> getBookings(String person);
	
	/**
	 * Returns the list of all bookings for the given person name and last day
	 * @param person a person name
	 * @param lastDay - the last export day
	 * @return list of bookings
	 */
	public List<Booking> getBookingsByLastExportDay(String person, Date lastDay);
	
	/**
	 * Returns the list of all bookings for the given last day
	 * @param day - the last export day
	 * @return list of bookings
	 */
	public List<Booking> getBookingsByLastExportDayForSuperuser(Date day);

    /**
     * Returns the sum of booked minutes per BookingTemplate.type (NP,0W,1T)
     * for the given month
     * @param person a person name
     * @param year the year, e.g. 2013
     * @param month a month constant as defined by Calendar.JANUARY to Calendar.DECEMBER
     * @param chartType chart type
     * @return Map of booked minutes per booking type
     */
    public Map<String,Number> getBookingSumsForMonth(String person, int year, int month, int chartType);
        
	/**
	 * Returns the list of all booking for the given person and day
	 * @param person a person name
	 * @param day a date specifying a day of the year
	 * @return list of bookings
	 */
	public List<Booking> getBookings(String person, Date day);
	
	/**
	 * Returns all bookings (directly) booked on the given budget
	 * @param budgetId a budget id
	 * @return list of bookings
	 */
	public List<Booking> getBookingsForBudget(int budgetId);
	
	/**
	 * Returns the booking with the given ID
	 * @param bookingId a booking ID
	 * @return the booking
	 */
	public Booking getBooking(int bookingId);
	
	/**
	 * Inserts or updates the given booking in the database.
	 * @param booking a booking
	 */
	public void saveBooking(Booking booking);
	
	/**
	 * Removes the booking with the given ID
	 * @param bookingId a booking ID
	 */
	public void deleteBooking(int bookingId);
}
