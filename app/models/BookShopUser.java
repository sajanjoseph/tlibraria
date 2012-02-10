package models;

import play.data.validation.Email;
import play.data.validation.Required;
import play.db.jpa.Model;

public class BookShopUser extends Model {
	
	@Email
	@Required
	public String email;
	
	@Required
	public String password;
	
	@Required
	public String fullName;
	
	public boolean isAdmin;
	
	public boolean isCustomer;

	public BookShopUser() {
		super();
		
	}
	
	public BookShopUser(String email, String password, String fullname) {
		super();
        this.email = email;
        this.password = password;
        this.fullName = fullname;
        
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
