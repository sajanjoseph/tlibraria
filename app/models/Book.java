package models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.junit.Test;

import play.cache.Cache;
import play.data.validation.Required;
import play.data.validation.MaxSize;
import play.db.jpa.Model;

@Entity
public class Book extends Model implements Comparable<Book>{
	
	@Required
	@Column(unique = true)
	public String isbn;
	
	@Required
	//@Field
	public String name;
	
	@Required
	@ManyToOne
	public Author author;
	
	@Required
	@ManyToOne
	public Publisher publisher;
	
	@Required
	public Date publishDate;
	
	@Required
	public BigDecimal price;
	
	@MaxSize(10000)
	@Column(name="description",length=10000)
	//@Field //search field
	public String description;
	
	/*in Review table ,the book will be a foreign key
	 * So, Review is the owner of relation, Book is the inverse side
	 */
	@OneToMany(mappedBy="book",cascade=CascadeType.ALL)
	public List<Review> reviews;
	
	@ManyToMany(cascade=CascadeType.PERSIST,fetch=FetchType.EAGER)
	public Set<Category> categories;

	public Book() {
		super();
		this.reviews = new ArrayList<Review>();
	}
	
	public Book(String isbn, String name, Date publishDate, BigDecimal price,
			String description, Author author, Publisher publisher) {
		super();
		
		this.isbn = isbn;
		this.name = name;
		this.publishDate = publishDate;
		this.price = price;
		this.description = description;
		this.author = author;
		this.publisher = publisher;
		
		this.reviews = new ArrayList<Review>();
		this.categories = new TreeSet<Category>();//natural ordering kept
	}

	@Override
	public int compareTo(Book other) {
		return this.isbn.compareTo(other.isbn);
	}
	
	public boolean hasSameIsbn(Book book) {
		return (this.isbn.equals( book.isbn));
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if (!(o instanceof Book)){
		    return false;
		}
		Book book = (Book)o;
		if(hasSameIsbn(book)){
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public int hashCode(){
		return this.isbn.hashCode();
	}
	
	@Override
	public String toString() {
		return this.name;
	}

	/*
	 * signature diff from prev version..
	 * doesn't return Review obj
	 */
	public void addReview(String author,String content) {
		Review review = new Review(this,author,content).save();
		this.reviews.add(review);
		this.save();
	}

	public Book tagItWith(String categoryName) {
		this.categories.add(Category.findOrCreateByName(categoryName));
		return this;
		//we return an instance so that the method invocations can be chained
	}

	public static List<Book> findTaggedWith(String categoryName) {
		Map<String,List<Book>> tagMap = (Map<String, List<Book>>) Cache.get("tagmap");
		if(tagMap==null) {
			tagMap= new HashMap<String,List<Book>>();
		}
		List<Book> books = Book.find("select distinct book from Book book join book.categories as cat where cat.name=:name").bind("name", categoryName).fetch();
		tagMap.put(categoryName, books);
		Cache.add("tagmap", tagMap,"20mn");
		return books;
	}
	
	public static List<Book> findTaggedWith(String... categories){
		return Book.find("select distinct b from Book b join b.categories as cat where cat.name in (:categories) group by b.id, b.author,b.publishDate, b.name,b.isbn,b.price, b.description having count(cat.id)=:size").bind("categories",categories).bind("size", categories.length).fetch();
		
	}
	
	

}
