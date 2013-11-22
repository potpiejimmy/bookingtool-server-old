package com.wincor.bcon.bookingtool.server.ejb;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.wincor.bcon.bookingtool.server.db.entity.BookingTemplate;

@Stateless
public class BookingTemplatesEJB implements BookingTemplatesEJBLocal {

	@PersistenceContext(unitName = "EJBsPU")
	EntityManager em;
	
	@Override
	@RolesAllowed({"admin", "user"})
	public List<BookingTemplate> getBookingTemplates() {
		return em.createNamedQuery("BookingTemplate.findAll", BookingTemplate.class).getResultList();
	}

	@Override
	public List<BookingTemplate> getBookingTemplatesByProjectId(int projectId) {
		return em.createNamedQuery("BookingTemplate.findByProjectId", BookingTemplate.class).setParameter("projectId", projectId).getResultList();
	}

	@Override
	@RolesAllowed({"admin", "user"})
	public BookingTemplate getBookingTemplate(int bookingTemplateId) {
		return em.find(BookingTemplate.class, bookingTemplateId);
	}

	@Override
	@RolesAllowed("admin")
	public void saveBookingTemplate(BookingTemplate bt) {
		//update the searchString
		bt.setSearchString(bt.getPsp() + " " + bt.getName() + " " + bt.getSalesRepresentative() + " " + bt.getSubproject() + " " + bt.getAdditionalInfo() + " " + bt.getDescription());
		
		if (bt.getId() != null)
			em.merge(bt);  // update the bookingTemplate
		else
			em.persist(bt);  // insert a new bookingTemplate
	}

	@Override
	@RolesAllowed("admin")
	public void deleteBookingTemplate(int bookingTemplateId) {
		em.remove(getBookingTemplate(bookingTemplateId));
	}

	@Override
	@RolesAllowed({"admin", "user"})
	public List<BookingTemplate> findBookingTemplates(String searchString) {
		
		TypedQuery<BookingTemplate> tq = em.createNamedQuery("BookingTemplate.findBySearchString", BookingTemplate.class);
		tq.setParameter("searchString", "%"+searchString.replace("*", "%").replace(" ", "%") +"%");
		tq.setParameter("active", (byte)1);
		
		return tq.getResultList();
	}
}
