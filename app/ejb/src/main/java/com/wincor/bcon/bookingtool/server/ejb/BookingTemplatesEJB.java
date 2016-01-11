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
public class BookingTemplatesEJB {

	@PersistenceContext(unitName = "EJBsPU")
	EntityManager em;

        @EJB
        private DomainsEJB domainsEjb;
       
        @EJB
        private BookingsEJB bookingsEjb;
        
	/**
	 * Returns all booking templates in the "booking_template" table
	 * @return list of booking templates
	 */
	@RolesAllowed({"admin", "user"})
	public List<BookingTemplate> getBookingTemplates() {
		return em.createNamedQuery("BookingTemplate.findAll", BookingTemplate.class).getResultList();
	}

	/**
	 * Returns the booking templates associated to the given project ID
	 * @param projectId a project ID
	 * @return a list of booking templates
	 */
	public List<BookingTemplate> getBookingTemplatesByProjectId(int projectId) {
		return em.createNamedQuery("BookingTemplate.findByProjectId", BookingTemplate.class).setParameter("projectId", projectId).getResultList();
	}

	/**
	 * Returns the booking templates associated with the given budget ID
	 * @param budgetId a budget ID
	 * @return a list of booking templates
	 */
	public List<BookingTemplate> getBookingTemplatesByBudgetId(int budgetId) {
		return em.createNamedQuery("BookingTemplate.findByBudgetId", BookingTemplate.class).setParameter("budgetId", budgetId).getResultList();
	}

	/**
	 * Returns the booking template with the given ID
	 * @param bookingTemplateId a booking template ID
	 * @return the booking template
	 */
	@RolesAllowed({"admin", "user"})
	public BookingTemplate getBookingTemplate(int bookingTemplateId) {
		return em.find(BookingTemplate.class, bookingTemplateId);
	}
        
        /**
         * Returns the list of last used booking templates for the given person.
         * @param person a person name
         * @param num number of templates to return
         * @return list of last used templates
         */
	@RolesAllowed({"admin", "user"})
        public List<BookingTemplate> getLastUsedByPerson(String person, int num) {
            return em.createNamedQuery("BookingTemplate.findLastUsedByPerson", BookingTemplate.class).setParameter("person", person).setMaxResults(num).getResultList();
        }

	/**
	 * Saves or updates the given booking template
	 * @param bt a booking template
	 */
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

        /**
         * Sets the given template's active flag.
         * @param bookingTemplateId a booking template
         * @param active the active field to be set
         */
	@RolesAllowed("admin")
        public void setTemplateActive(int bookingTemplateId, byte active) {
            getBookingTemplate(bookingTemplateId).setActive(active);
        }

	/**
	 * Removes the booking template with the given ID
	 * @param bookingTemplateId a booking template ID
	 */
	@RolesAllowed("admin")
	public void deleteBookingTemplate(int bookingTemplateId) {
		em.remove(getBookingTemplate(bookingTemplateId));
	}

	/**
	 * Returns the list of templates matching the given search criteria
	 * @param searchString a search string
         * @return list of matching templates
	 */
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
