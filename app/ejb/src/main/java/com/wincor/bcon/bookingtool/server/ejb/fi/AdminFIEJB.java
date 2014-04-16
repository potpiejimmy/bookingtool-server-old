package com.wincor.bcon.bookingtool.server.ejb.fi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.wincor.bcon.bookingtool.server.db.entity.BookingTemplate;
import com.wincor.bcon.bookingtool.server.db.entity.Budget;
import com.wincor.bcon.bookingtool.server.ejb.BookingTemplatesEJBLocal;
import com.wincor.bcon.bookingtool.server.vo.AutoCreateInfoVo;
import javax.ejb.EJB;

@Stateless
public class AdminFIEJB implements AdminFIEJBLocal {

	@PersistenceContext(unitName = "EJBsPU")
	private EntityManager em;
        
        @EJB
        private BookingTemplatesEJBLocal templatesEJB;
	
	@Override
	@RolesAllowed({"admin"})
	public Map<Class<?>, List<?>> autoCreateBudgetsAndTemplates(AutoCreateInfoVo createVo) {
		
		List<Budget> createdBudgets = new ArrayList<Budget>(3);
		
		// First, create CR budget:
		Budget crBudget = new Budget();
		crBudget.setName(createVo.getMksNo() + " " + createVo.getMksName());
		crBudget.setMinutes(createVo.getMinutesDev() + createVo.getMinutesQA()); // not really necessary because super budget is sum of sub budgets
		crBudget.setProjectId(createVo.getProjectId());
		crBudget.setParentId(createVo.getParentBudgetId());
		em.persist(crBudget);
		em.flush(); // fetch new ID
		createdBudgets.add(crBudget);
		
		// Create sub-budget for QA and Dev
		Budget crBudgetDev = new Budget();
		crBudgetDev.setName("Entwicklung");
		crBudgetDev.setMinutes(createVo.getMinutesDev());
		crBudgetDev.setProjectId(createVo.getProjectId());
		crBudgetDev.setParentId(crBudget.getId());
                crBudgetDev.setAllowOverrun((byte)1);

		Budget crBudgetQA = new Budget();
		crBudgetQA.setName("QA");
		crBudgetQA.setMinutes(createVo.getMinutesQA());
		crBudgetQA.setProjectId(createVo.getProjectId());
		crBudgetQA.setParentId(crBudget.getId());
                crBudgetQA.setAllowOverrun((byte)1);
		
		em.persist(crBudgetDev);
		em.persist(crBudgetQA);
		em.flush(); // fetch new IDs
		createdBudgets.add(crBudgetDev);
		createdBudgets.add(crBudgetQA);
		
		List<BookingTemplate> createdTemplates = new ArrayList<BookingTemplate>(10);

		createdTemplates.add(createTemplate(createVo.getSpecBudgetId(),
				createVo.getPspTemplate() + "-02",
				createVo.getPspNameTemplate() + " Pflichtenheft",
				"Analyse FachSpez " + createVo.getMksName(),
				createVo.getMksNo()));
		
		createdTemplates.add(createTemplate(createVo.getSpecBudgetId(),
				createVo.getPspTemplate() + "-03",
				createVo.getPspNameTemplate() + " Spezifikation",
				"Feinspezifikation " + createVo.getMksName(),
				createVo.getMksNo()));
		
		createdTemplates.add(createTemplate(crBudgetDev.getId(),
				createVo.getPspTemplate() + "-05",
				createVo.getPspNameTemplate() + " Entwicklung",
				"Umsetzung CR " + createVo.getMksName(),
				createVo.getMksNo()));
		
		createdTemplates.add(createTemplate(crBudgetQA.getId(),
				createVo.getPspTemplate() + "-06",
				createVo.getPspNameTemplate() + " QA",
				"QA " + createVo.getMksName(),
				createVo.getMksNo()));
                
                em.flush(); // fetch IDs
		
		Map<Class<?>, List<?>> result = new HashMap<Class<?>, List<?>>();
		result.put(Budget.class, createdBudgets);
		result.put(BookingTemplate.class, createdTemplates);
		
		return result;
		
	}

	protected BookingTemplate createTemplate(int budgetId, String psp, String pspName, String description, String salesRepresentative) {
		BookingTemplate t = new BookingTemplate();
		t.setActive((byte)1);
		t.setBudgetId(budgetId);
		t.setDescription(description);
		t.setName(pspName);
		t.setPsp(psp);
		t.setSalesRepresentative(salesRepresentative);
		t.setType("0W");
                t.setSubproject("");
                t.setAdditionalInfo("");
                templatesEJB.saveBookingTemplate(t);
		return t;
	}
}
