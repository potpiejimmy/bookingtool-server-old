package com.wincor.bcon.bookingtool.server.ejb;

import java.util.Date;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.wincor.bcon.bookingtool.server.db.entity.Booking;

@Stateless
public class BookingsEJB implements BookingsEJBLocal {

	@PersistenceContext(unitName = "EJBsPU")
	EntityManager em;
	
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
		if (booking.getId() != null)
			em.merge(booking);		// update the booking
		else
			em.persist(booking); 	//insert a new booking

	}

	@Override
	@RolesAllowed({"admin", "user"})
	public void deleteBooking(int bookingId) {
		em.remove(getBooking(bookingId));
	}
}
