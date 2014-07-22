package com.wincor.bcon.bookingtool.server.ejb;

import com.wincor.bcon.bookingtool.server.db.entity.Booking;
import com.wincor.bcon.bookingtool.server.db.entity.BookingTemplate;
import com.wincor.bcon.bookingtool.server.db.entity.Domain;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

@Stateless
public class BookingTemplatesEJB implements BookingTemplatesEJBLocal {

	@PersistenceContext(unitName = "EJBsPU")
	EntityManager em;

        @EJB
        private DomainsEJBLocal domainsEjb;
       
        @EJB
        private BookingsEJBLocal bookingsEjb;
        
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
	public List<BookingTemplate> getBookingTemplatesByBudgetId(int budgetId) {
		return em.createNamedQuery("BookingTemplate.findByBudgetId", BookingTemplate.class).setParameter("budgetId", budgetId).getResultList();
	}

	@Override
	@RolesAllowed({"admin", "user"})
	public BookingTemplate getBookingTemplate(int bookingTemplateId) {
		return em.find(BookingTemplate.class, bookingTemplateId);
	}
        
	@Override
	@RolesAllowed({"admin", "user"})
        public List<BookingTemplate> getLastUsedByPerson(String person, int num) {
            return em.createNamedQuery("BookingTemplate.findLastUsedByPerson", BookingTemplate.class).setParameter("person", person).setMaxResults(num).getResultList();
        }

	@Override
	@RolesAllowed("admin")
	public void saveBookingTemplate(BookingTemplate bt) {
		// check whether booking relevant fields in the template have
		// changed and if so, re-save all bookings that have already
		// been exported so that they get marked as changed.
                if (bt.getId() != null) {
                    // template was edited
                    BookingTemplate btold = getBookingTemplate(bt.getId());
                    boolean hasRelevantChanges = !bt.getPsp().equals(btold.getPsp()) ||
                                                 !bt.getName().equals(btold.getName()) ||
                                                 !bt.getType().equals(btold.getType()) ||
                                                 !bt.getSubproject().equals(btold.getSubproject());
                    if (hasRelevantChanges) {
                        for(Booking b : bookingsEjb.getBookingsByTemplateId(bt.getId()))
                                if (b.getExportState()==1) b.setExportState((byte)2);
                    }
                }
                
                //update the searchString
		bt.setSearchString(bt.getPsp() + " " + bt.getName() + " " + bt.getSalesRepresentative() + " " + bt.getSubproject() + " " + bt.getAdditionalInfo() + " " + bt.getDescription());
		
		if (bt.getId() != null)
			em.merge(bt);  // update the bookingTemplate
		else
			em.persist(bt);  // insert a new bookingTemplate
	}

	@Override
	@RolesAllowed("admin")
        public void setTemplateActive(int bookingTemplateId, byte active) {
            getBookingTemplate(bookingTemplateId).setActive(active);
        }

	@Override
	@RolesAllowed("admin")
	public void deleteBookingTemplate(int bookingTemplateId) {
		em.remove(getBookingTemplate(bookingTemplateId));
	}

	@Override
	@RolesAllowed({"admin", "user"})
	public List<BookingTemplate> findBookingTemplates(String searchString) {
		List<BookingTemplate> result = new ArrayList<BookingTemplate>();
                for (Domain domain : domainsEjb.getDomains()) {
                    TypedQuery<BookingTemplate> tq = em.createNamedQuery("BookingTemplate.findBySearchString", BookingTemplate.class);
                    tq.setParameter("domainId", domain.getId());
                    tq.setParameter("searchString", "%"+searchString.replace("*", "%").replace(" ", "%") +"%");
                    tq.setParameter("active", (byte)1);
                    result.addAll(tq.getResultList());
                }		
		return result;
	}
}
