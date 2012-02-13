package models;

import javax.persistence.Entity;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Address extends Model {
	@Required
	public String addressLine1;
	
	public String addressLine2;
	
	public String city;
	
	@Required
	public String state;
	
	public String pincode;
	
	public String phoneNumber;
	
	@Required
	public String country; 

	public Address() {
		super();
	}
	
	public Address(String addressLine1, String addressLine2, String city,
			String state, String pincode, String phoneNumber, String country) {
		super();
		this.addressLine1 = addressLine1;
		this.addressLine2 = addressLine2;
		this.city = city;
		this.state = state;
		this.pincode = pincode;
		this.phoneNumber = phoneNumber;
		this.country = country;
	}
	
	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = result
				+ ((addressLine1 == null) ? 0 : addressLine1.hashCode());
		result = result
				+ ((addressLine2 == null) ? 0 : addressLine2.hashCode());
		result = result + ((city == null) ? 0 : city.hashCode());
		result = result + ((country == null) ? 0 : country.hashCode());
		result = result
				+ ((phoneNumber == null) ? 0 : phoneNumber.hashCode());
		result = result + ((pincode == null) ? 0 : pincode.hashCode());
		result = result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		System.out.println("equals("+obj);
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		Address other = (Address) obj;
		if (addressLine1 == null) {
			if (other.addressLine1 != null)
				return false;
		} else if (!addressLine1.equals(other.addressLine1))
			return false;
		if (addressLine2 == null) {
			if (other.addressLine2 != null)
				return false;
		} else if (!addressLine2.equals(other.addressLine2))
			return false;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.equals(other.country))
			return false;
		if (phoneNumber == null) {
			if (other.phoneNumber != null)
				return false;
		} else if (!phoneNumber.equals(other.phoneNumber))
			return false;
		if (pincode == null) {
			if (other.pincode != null)
				return false;
		} else if (!pincode.equals(other.pincode))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}
	
	public String toString() {
		return this.addressLine1+","+this.addressLine2+","+this.city+","+this.state+","+this.pincode+","+this.country+","+this.phoneNumber;
	}

}
