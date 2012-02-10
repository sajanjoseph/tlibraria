package models;

import javax.persistence.Entity;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Author extends Model {
	@Required
	private String name;
	
	@Required
	private String address;

	public Author() {
		super();
	}

	public Author(String name, String address) {
		super();
		this.name = name;
		this.address = address;
	}
	
	public String toString() {
		return this.name;
	}
	
	
	
	

}
