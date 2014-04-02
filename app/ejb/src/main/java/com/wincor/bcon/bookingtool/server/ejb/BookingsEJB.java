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
public class BookingsEJB implements BookingsEJBLocal {

	@PersistenceContext(unitName = "EJBsPU")
	EntityManager em;
	
        @EJB
        private BudgetsEJBLocal budgetsEjb;
        
	@Override
	@RolesAllowed({"admin"})
	public List<Booking> getBookings() {
		return em.createNamedQuery("Booking.findAll", Booking.class).getResultList();
	}

	@Override
	@RolesAllowed({"admin", "user"})
	public List<Booking> getBookings(String person) {
		return em.createNamedQuery("Booking.findByPerson", Booking.class).setParameter("person", person).getResultList();
	}

	@Override
	@RolesAllowed({"admin", "user"})
	public List<Booking> getBookingsByLastExportDay(String person, Date day) {
		TypedQuery<Booking> tq = em.createNamedQuery("Booking.findByLastExportDayForPerson", Booking.class);
		tq.setParameter("person", person);
		tq.setParameter("day", day);
		
		return tq.getResultList();
	}
	
	@Override
	@RolesAllowed({"superuser"})
	public List<Booking> getBookingsByLastExportDayForSuperuser(Date day) {
		TypedQuery<Booking> tq = em.createNamedQuery("Booking.findByLastExportDayForSuperuser", Booking.class).setParameter("day", day);
		
		return tq.getResultList();
	}
        
        @Override
	@RolesAllowed({"admin", "user"})
        public Map<String,Number> getBookingSumsForMonth(String person, int year, int month, int chartType) {
            TimePeriod timePeriod = Utils.timePeriodForMonth(year, month);
            List<Object[]> sums = (List<Object[]>)em.createNamedQuery(
                    chartType == 0 ? "Booking.sumsByTypeForPersonAndTimePeriod" : "Booking.sumsByProjectForPersonAndTimePeriod").
                    setParameter("person", person).
                    setParameter("from", new java.sql.Date(timePeriod.getFrom()), TemporalType.DATE).
                    setParameter("to", new java.sql.Date(timePeriod.getTo()), TemporalType.DATE).
                    getResultList();
            Map<String,Number> result = new HashMap<String,Number>();
            for (Object[] o : sums) {
                result.put((String)o[0], (Number)o[1]);
            }
            return result;
        }

	@Override
	@RolesAllowed({"admin", "user"})
	public List<Booking> getBookings(String person, Date day) {
		TypedQuery<Booking> tq = em.createNamedQuery("Booking.findByPersonAndDay", Booking.class);
		tq.setParameter("person", person);
		tq.setParameter("day", day);
		
		return tq.getResultList();
	}

	@Override
	public List<Booking> getBookingsForBudget(int budgetId) {
		return em.createNamedQuery("Booking.findByBudgetId", Booking.class).setParameter("budgetId", budgetId).getResultList();
	}

	@Override
	@RolesAllowed({"admin", "user"})
	public Booking getBooking(int bookingId) {
		return em.find(Booking.class, bookingId);
	}

	@Override
	@RolesAllowed({"admin", "user"})
	public void saveBooking(Booking booking) {
                assertNoOverrun(booking);
		if (booking.getId() != null)
			em.merge(booking);		// update the booking
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

	@Override
	@RolesAllowed({"admin", "user"})
	public void deleteBooking(int bookingId) {
		em.remove(getBooking(bookingId));
	}
}
