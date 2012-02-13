package models;

import java.util.List;

import javax.persistence.Entity;


import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Author extends Model {
	@Required
	public String name;
	
	@Required
	public String address;

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

	public static Author getAuthor(String name, String address) {
		return Author.find("byNameAndAddress", name,address).first();
	}

	public static List<Author> getAuthorsByName(String name) {
		return Author.find("byName", name).fetch();
	}
	
	
	
	

}
