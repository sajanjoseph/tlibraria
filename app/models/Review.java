package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Type;

import play.data.validation.Required;
import play.data.validation.MaxSize;
import play.db.jpa.Model;

@Entity
public class Review extends Model {
	
	@Required
	public String author;
	
	@Required
	//@MaxSize(10000)
	//lets find out if search inside lob is possible
	@Lob
	@Type(type="org.hibernate.type.TextType")
	public String content;
	
	@Required
	public Date postedAt;
	
	@ManyToOne
	@Required
	public Book book;

	public Review() {
		super();
	}
	
	public Review(Book book,String author, String content) {
		super();
		this.author = author;
		this.content = content;
		this.book = book;
		this.postedAt = new Date();
	}
	
	public String toString() {
		return this.author+"-"+this.postedAt.toString();
	}
	

}
