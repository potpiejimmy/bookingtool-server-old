package com.wincor.bcon.bookingtool.server.ejb;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

import com.wincor.bcon.bookingtool.server.db.entity.Booking;
import com.wincor.bcon.bookingtool.server.db.entity.BookingTemplate;
import com.wincor.bcon.bookingtool.server.db.entity.Budget;
import com.wincor.bcon.bookingtool.server.util.Utils;
import com.wincor.bcon.bookingtool.server.vo.BudgetInfoVo;
import com.wincor.bcon.bookingtool.server.vo.TimePeriod;

@Stateless
public class BookingsEJB {

	@PersistenceContext(unitName = "EJBsPU")
	EntityManager em;
	
        @EJB
        private BudgetsEJB budgetsEjb;
        
	/**
	 * Returns the list of all bookings in the system (admin only)
	 * @return list of bookings
	 */
	@RolesAllowed({"admin"})
	public List<Booking> getBookings() {
		return em.createNamedQuery("Booking.findAll", Booking.class).getResultList();
	}

	/**
	 * Returns the list of all bookings for the given person name
	 * @param person a person name
	 * @return list of bookings
	 */
	@RolesAllowed({"admin", "user"})
	public List<Booking> getBookings(String person) {
		return em.createNamedQuery("Booking.findByPerson", Booking.class).setParameter("person", person).getResultList();
	}

	/**
	 * Returns the list of all bookings for the given person name and last day
	 * @param person a person name
	 * @param day - the last export day
	 * @return list of bookings
	 */
	@RolesAllowed({"admin", "user"})
	public List<Booking> getBookingsByLastExportDay(String person, Date day) {
		TypedQuery<Booking> tq = em.createNamedQuery("Booking.findByLastExportDayForPerson", Booking.class);
		tq.setParameter("person", person);
		tq.setParameter("day", day);
		
		return tq.getResultList();
	}
	
	/**
	 * Returns the list of all bookings for the given last day
	 * @param day - the last export day
	 * @return list of bookings
	 */
	@RolesAllowed({"superuser"})
	public List<Booking> getBookingsByLastExportDayForSuperuser(Date day) {
		TypedQuery<Booking> tq = em.createNamedQuery("Booking.findByLastExportDayForSuperuser", Booking.class).setParameter("day", day);
		
		return tq.getResultList();
	}
        
        /**
         * Returns the sum of booked minutes per BookingTemplate.type (NP,0W,1T)
         * for the given month
         * @param person a person name
         * @param year the year, e.g. 2013
         * @param month a month constant as defined by Calendar.JANUARY to Calendar.DECEMBER
         * @param chartType chart type
         * @return Map of booked minutes per booking type
         */
	@RolesAllowed({"admin", "user"})
        public Map<String,Number> getBookingSumsForMonth(String person, int year, int month, int chartType) {
            TimePeriod timePeriod = Utils.timePeriodForMonth(year, month);
            String chartStmt = null;
            switch (chartType) {
                case 0: chartStmt = "Booking.sumsByTypeForPersonAndTimePeriod"; break;
                case 1: chartStmt = "Booking.sumsByProjectForPersonAndTimePeriod"; break;
                case 2: chartStmt = "Booking.sumAndCountDayForPersonAndTimePeriod"; break;
            }
            List<Object[]> sums = (List<Object[]>)em.createNamedQuery(chartStmt).
                    setParameter("person", person).
                    setParameter("from", new java.sql.Date(timePeriod.getFrom()), TemporalType.DATE).
                    setParameter("to", new java.sql.Date(timePeriod.getTo()), TemporalType.DATE).
                    getResultList();
            Map<String,Number> result = new HashMap<String,Number>();
            if (chartType == 2) {
                result.put("sum",  (Number)sums.get(0)[0]);
                result.put("days", (Number)sums.get(0)[1]);
            } else {
                for (Object[] o : sums) {
                    result.put((String)o[0], (Number)o[1]);
                }
            }
            return result;
        }

	/**
	 * Returns the list of all booking for the given person and day
	 * @param person a person name
	 * @param day a date specifying a day of the year
	 * @return list of bookings
	 */
	@RolesAllowed({"admin", "user"})
	public List<Booking> getBookings(String person, Date day) {
		TypedQuery<Booking> tq = em.createNamedQuery("Booking.findByPersonAndDay", Booking.class);
		tq.setParameter("person", person);
		tq.setParameter("day", day);
		
		return tq.getResultList();
	}

	/**
	 * Returns all bookings (directly) booked on the given budget
	 * @param budgetId a budget id
	 * @return list of bookings
	 */
	public List<Booking> getBookingsForBudget(int budgetId) {
		return em.createNamedQuery("Booking.findByBudgetId", Booking.class).setParameter("budgetId", budgetId).getResultList();
	}
        
	/**
	 * Returns all bookings for the given project
	 * @param projectId a project id
	 * @return list of bookings
	 */
	@RolesAllowed({"admin"})
	public List<Booking> getBookingsForProject(int projectId) {
		return em.createNamedQuery("Booking.findByProjectId", Booking.class).setParameter("projectId", projectId).getResultList();
        }
        
	/**
	 * Returns the booking with the given booking template ID
	 * @param bookingTemplateId a booking template ID
	 * @return the booking
	 */
        public List<Booking> getBookingsByTemplateId(int bookingTemplateId) {
		return em.createNamedQuery("Booking.findByTemplateId", Booking.class).setParameter("bookingTemplateId", bookingTemplateId).getResultList();
	}

	/**
	 * Returns the booking with the given ID
	 * @param bookingId a booking ID
	 * @return the booking
	 */
	@RolesAllowed({"admin", "user"})
	public Booking getBooking(int bookingId) {
		return em.find(Booking.class, bookingId);
	}

	/**
	 * Inserts or updates the given booking in the database.
	 * @param booking a booking
	 */
	@RolesAllowed({"admin", "user"})
	public void saveBooking(Booking booking) {
                assertNoOverrun(booking);
                booking.setModifiedDate(new Date());
		if (booking.getId() != null)
                {
                    if (booking.getExportState() == 1)
                        booking.setExportState((byte)2);                    
                    em.merge(booking);		// update the booking                 
                }
		else
			em.persist(booking); 	//insert a new booking

	}
        
        protected void assertNoOverrun(Booking booking) {
            // get associated budget:
            BookingTemplate t = em.find(BookingTemplate.class, booking.getBookingTemplateId());
            Budget budget = em.find(Budget.class, t.getBudgetId());
            if (budget.getAllowOverrun() == 1) return; // overrun allowed, everything is fine
            
            // check whether budget is about to overrun:
            BudgetInfoVo budgetInfo = budgetsEjb.getBudgetInfo(budget.getId());
            int usedMinutes = budgetInfo.getBookedMinutesRecursive();
            // subtract old value in DB if editing:
            if (booking.getId() != null) usedMinutes -= em.find(Booking.class, booking.getId()).getMinutes();
            
            // now check whether inserted or edited value will fit into the remaining budget:
            if (usedMinutes + booking.getMinutes() > Math.abs(budgetInfo.getBudget().getMinutes()))
                throw new IllegalStateException("Sorry, the remaining budget is insufficient and budget overrun is not allowed. Please contact your project manager to resolve this issue.");
        }

	/**
	 * Removes the booking with the given ID
	 * @param bookingId a booking ID
	 */
	@RolesAllowed({"admin", "user"})
	public void deleteBooking(int bookingId) {
		em.remove(getBooking(bookingId));
	}
        
        /**
         * Copies all person's bookings for a specified date to the given date.
         * @param person a person name
         * @param from date to copy from
         * @param to date to copy to
         */
	@RolesAllowed({"admin", "user"})
        public void copyBookings(String person, Date from, Date to) {
            for (Booking b : getBookings(person, from)) {
                Booking copy = new Booking(b);
                copy.setDay(to);
                saveBooking(copy);
            }
        }
}
