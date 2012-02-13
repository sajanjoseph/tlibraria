package models;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;

import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Publisher extends Model {
	@Required
	@Column(unique = true)
	public String code;
	@Required
	@MinSize(3)
	public String name;	
	
	public String address;

	public Publisher() {
		super();
	}

	public Publisher(String code, String name, String address) {
		super();
		this.code = code;
		this.name = name;
		this.address = address;
	}
	
	public static Publisher getPublisherByCode(String code) {
		return Publisher.find("byCode", code).first();
	}
	
	public String toString() {
		return this.name;
	}

	public static  List<Publisher> getPublishersByName(String name) {
		
		return Publisher.find("byName", name).fetch();
	}

}
