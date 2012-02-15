package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"cctype","creditCardNumber"}))
public class Payment extends Model {
	
	//specify constraints for ccards
	@Required
	public String name;
	@MinSize(14)
	@Required
	public String creditCardNumber;
	@Required
	public String month;
	@Required
	public String year;
	@Required
	public String cctype;

	public Payment() {
		super();
	}
	public Payment(String name,String creditCardNumber, String month, String year,String cctype) {
		super();
		this.name = name;
		this.creditCardNumber = creditCardNumber;
		this.month = month;
		this.year = year;
		this.cctype = cctype;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((cctype == null) ? 0 : cctype.hashCode());
		result = prime
				* result
				+ ((creditCardNumber == null) ? 0 : creditCardNumber.hashCode());
		result = prime * result + ((month == null) ? 0 : month.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((year == null) ? 0 : year.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		Payment other = (Payment) obj;
		if (cctype == null) {
			if (other.cctype != null)
				return false;
		} else if (!cctype.equals(other.cctype))
			return false;
		if (creditCardNumber == null) {
			if (other.creditCardNumber != null)
				return false;
		} else if (!creditCardNumber.equals(other.creditCardNumber))
			return false;
		if (month == null) {
			if (other.month != null)
				return false;
		} else if (!month.equals(other.month))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (year == null) {
			if (other.year != null)
				return false;
		} else if (!year.equals(other.year))
			return false;
		return true;
	}
	
	public static List<Payment> findByName(String name){
		return Payment.find("byName",name).fetch();
	}

}
