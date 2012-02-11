package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity
public class Category extends Model implements Comparable<Category> {
	/*
	 * A book can belong to one or more Categories.
	 * This helps retrieving books easier.
	 */
	public String name;
	
	public Category(String name) {
		super();
		this.name = name;
	}

	public Category() {
		super();
	}

	@Override
	public int compareTo(Category other) {
		return this.name.compareTo(other.name);
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	public static Category findOrCreateByName(String name) {
		Category cat = Category.find("byName", name).first();
		if (cat == null) {
			cat = new Category();
		}
		return cat;
	}

}
