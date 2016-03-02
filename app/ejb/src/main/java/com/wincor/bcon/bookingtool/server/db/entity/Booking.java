package com.wincor.bcon.bookingtool.server.db.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * The persistent class for the booking database table.
 * 
 */
@Entity
@Table(name="booking")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Booking.findAll", query = "SELECT b FROM Booking b ORDER BY b.day DESC, b.person"),
    @NamedQuery(name = "Booking.findByLastExportDayForPerson", query = "SELECT b FROM Booking b WHERE b.person = :person AND b.day >= :day ORDER BY b.day DESC, b.person"),
    @NamedQuery(name = "Booking.findByLastExportDayForSuperuser", query = "SELECT b FROM Booking b WHERE b.day >= :day ORDER BY b.day DESC, b.person"),
    @NamedQuery(name = "Booking.findByDay", query = "SELECT b FROM Booking b WHERE b.day = :day"),
    @NamedQuery(name = "Booking.findByBudgetId", query = "SELECT b FROM Booking b,BookingTemplate t WHERE b.bookingTemplateId=t.id AND t.budgetId=:budgetId ORDER BY b.day DESC, b.person"),
    @NamedQuery(name = "Booking.findByProjectId", query = "SELECT b FROM Booking b,BookingTemplate t,Budget bu WHERE b.bookingTemplateId=t.id AND t.budgetId=bu.id AND bu.projectId=:projectId ORDER BY b.day DESC, b.person"),
    @NamedQuery(name = "Booking.findByTemplateId", query = "SELECT b FROM Booking b WHERE b.bookingTemplateId=:bookingTemplateId"),
    @NamedQuery(name = "Booking.findByPerson", query = "SELECT b FROM Booking b WHERE b.person = :person ORDER BY b.day DESC"),
    @NamedQuery(name = "Booking.findByPersonAndDay", query = "SELECT b FROM Booking b WHERE b.person = :person AND b.day = :day"),
    @NamedQuery(name = "Booking.sumsByTypeForPersonAndTimePeriod", query = "SELECT t.type,SUM(b.minutes) FROM Booking b,BookingTemplate t WHERE b.bookingTemplateId=t.id AND b.person=:person AND b.day>=:from AND b.day<:to GROUP BY t.type"),
    @NamedQuery(name = "Booking.sumsByProjectForPersonAndTimePeriod", query = "SELECT p.name,SUM(b.minutes) FROM Booking b,BookingTemplate t,Budget bu,Project p WHERE b.bookingTemplateId=t.id AND t.budgetId=bu.id AND bu.projectId=p.id AND b.person=:person AND b.day>=:from AND b.day<:to GROUP BY p.name"),
    @NamedQuery(name = "Booking.sumAndCountDayForPersonAndTimePeriod", query = "SELECT SUM(b.minutes),COUNT(DISTINCT day) FROM Booking b WHERE b.person=:person AND b.day>=:from AND b.day<:to")})

public class Booking implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;

	@Column
	@NotNull
	@Temporal(TemporalType.DATE)
	private Date day;

	@Column
	@NotNull
	@Size(min = 1, max=255)
	private String description;

	@Column(name="sales_representative")
	@NotNull
	@Size(min = 1, max=45)
	private String salesRepresentative;

	@Column
	@NotNull
	private Integer minutes;

	@Column
	@NotNull
	@Size(min = 1, max=45)
	private String person;

	@Column(name="booking_template_id")
	@NotNull
	private Integer bookingTemplateId;
        
        @Column(name="export_state")
	@NotNull
	private byte exportState;
        
        @Column(name="modified_date")
        @Temporal(TemporalType.TIMESTAMP)
        @NotNull
        private Date modifiedDate;

        @Transient
        private BookingTemplate bookingTemplate = null;
        
	public Booking() {
	}
        
        public Booking(Booking copy) {
            this.day = copy.day;
            this.description = copy.description;
            this.salesRepresentative = copy.salesRepresentative;
            this.minutes = copy.minutes;
            this.person = copy.person;
            this.bookingTemplateId = copy.bookingTemplateId;
        }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getDay() {
		return this.day;
	}

	public void setDay(Date day) {
		this.day = day;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSalesRepresentative() {
		return salesRepresentative;
	}

	public void setSalesRepresentative(String salesRepresentative) {
		this.salesRepresentative = salesRepresentative;
	}

	public Integer getMinutes() {
		return minutes;
	}

	public void setMinutes(Integer minutes) {
		this.minutes = minutes;
	}

	public String getPerson() {
		return this.person;
	}

	public void setPerson(String person) {
		this.person = person;
	}
	
	public Integer getBookingTemplateId() {
		return bookingTemplateId;
	}

	public void setBookingTemplateId(Integer bookingTemplateId) {
		this.bookingTemplateId = bookingTemplateId;
	}
        
        public byte getExportState() {
		return this.exportState;
	}
        public void setExportState(byte exportState) {
		this.exportState = exportState;
	}

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public BookingTemplate getBookingTemplate() {
        return bookingTemplate;
    }

    public void setBookingTemplate(BookingTemplate bookingTemplate) {
        this.bookingTemplate = bookingTemplate;
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
        if (!(object instanceof Booking)) {
            return false;
        }
        Booking other = (Booking) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

}