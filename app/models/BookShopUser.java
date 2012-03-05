package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class BookShopUser extends Model {
	
	@Email
	@Required
	public String email;
	
	@Required
	public String password;
	
	@Required
	public String fullName;
	
	@OneToMany(mappedBy="bookshopuser",  cascade=CascadeType.ALL)
	public List<Address> addresses;
	
	@OneToMany(mappedBy="bookshopuser", cascade=CascadeType.ALL)
	public Set<Payment> payments;
	
	@OneToMany(mappedBy="customer", cascade=CascadeType.ALL)
	public List<BookOrder> bookOrders;
	
	@OneToOne
	public Payment currentPayment;
	
	public boolean isAdmin;
	
	public boolean isCustomer;

	public BookShopUser() {
		super();
		this.payments = new HashSet<Payment>();
		this.addresses = new ArrayList<Address>();
		this.bookOrders = new ArrayList<BookOrder>();
		
	}
	
	public BookShopUser(String email, String password, String fullname) {
		super();
        this.email = email;
        this.password = password;
        this.fullName = fullname;
        this.payments = new HashSet<Payment>();
        this.addresses = new ArrayList<Address>();
        this.bookOrders = new ArrayList<BookOrder>();
    }
	
	public String toString() {
		return email;
	}
	
	/*
	 * queries the db for a user with this Email and this Password and gets the first match
	 */
	public static  BookShopUser connect(String email,String password) {
		return BookShopUser.find("byEmailAndPassword",email,password).first();
	}

}
