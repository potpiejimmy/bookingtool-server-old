package com.wincor.bcon.bookingtool.server.db.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * The persistent class for the booking_template database table.
 * 
 */
@Entity
@Table(name="booking_template")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "BookingTemplate.findAll", query = "SELECT b FROM BookingTemplate b"),
    @NamedQuery(name = "BookingTemplate.findByProjectId", query = "SELECT bt FROM BookingTemplate bt, Budget bu WHERE bt.budgetId = bu.id AND bu.projectId = :projectId ORDER BY bt.salesRepresentative,bt.psp"),
    @NamedQuery(name = "BookingTemplate.findByBudgetId", query = "SELECT bt FROM BookingTemplate bt WHERE bt.budgetId = :budgetId ORDER BY bt.salesRepresentative,bt.psp"),
    @NamedQuery(name = "BookingTemplate.findLastUsedByPerson", query = "SELECT bt FROM BookingTemplate bt,Booking b WHERE b.bookingTemplateId = bt.id AND b.person = :person GROUP BY bt ORDER BY MAX(b.modifiedDate) DESC, bt.id"),
    @NamedQuery(name = "BookingTemplate.findBySearchString", query = "SELECT bt FROM BookingTemplate bt,Budget b,Project p WHERE bt.budgetId=b.id AND b.projectId=p.id AND p.domainId=:domainId AND bt.searchString LIKE :searchString AND bt.active = :active AND p.status=0")})

public class BookingTemplate implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column
	private Integer id;

	@Column
	@NotNull
	private byte active;

	@Column
	@Size(min = 1, max=255)
	private String description;

	@Column
	@NotNull
	@Size(min = 1, max=64)
	private String name;

	@Column
	@NotNull
	@Size(min = 1, max=45)
	private String psp;

	@Column(name="sales_representative")
	@Size(min = 0, max=45)
	private String salesRepresentative;

	@Column
	@Size(min = 0, max=45)
	private String subproject;

	@Column
	@NotNull
	private String type;
	
	@Column(name="additional_info")
	@Size(min=0, max = 64)
	private String additionalInfo;
	
	@Column(name="search_string")
	@Size(min = 0, max = 512)
	@NotNull
	private String searchString;

	@Column(name="budget_id")
	@NotNull
	private Integer budgetId;

	public BookingTemplate() {
	}

        /* copy constructor */
	public BookingTemplate(BookingTemplate copy) {
            this.active = copy.getActive();
            this.description = copy.getDescription();
            this.name = copy.getName();
            this.psp = copy.getPsp();
            this.salesRepresentative = copy.getSalesRepresentative();
            this.subproject = copy.getSubproject();
            this.type = copy.getType();
            this.additionalInfo = copy.getAdditionalInfo();
            this.searchString = copy.getSearchString();
            this.budgetId = copy.getBudgetId();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public byte getActive() {
		return this.active;
	}

	public void setActive(byte active) {
		this.active = active;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPsp() {
		return this.psp;
	}

	public void setPsp(String psp) {
		this.psp = psp;
	}

	public String getSalesRepresentative() {
		return this.salesRepresentative;
	}

	public void setSalesRepresentative(String salesRepresentative) {
		this.salesRepresentative = salesRepresentative;
	}

	public String getSubproject() {
		return this.subproject;
	}

	public void setSubproject(String subproject) {
		this.subproject = subproject;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getBudgetId() {
		return budgetId;
	}

	public void setBudgetId(Integer budgetId) {
		this.budgetId = budgetId;
	}
	
	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public String getSearchString() {
		return this.searchString;
	}
	
	public void setSearchString(String searchString) {
		this.searchString = searchString;  
	}
	
	@Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof BookingTemplate)) {
            return false;
        }
        BookingTemplate other = (BookingTemplate) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

}