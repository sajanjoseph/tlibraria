package models;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

@Entity
public class CartItem extends Model implements Comparable<CartItem>{
	
	@ManyToOne
	public Book book;
	
	public int quantity;
	
	@ManyToOne
	public BookOrder order;
	
	public CartItem(Book book, int quantity) {
		super();
		this.book = book;
		this.quantity = quantity;
	}

	public CartItem() {
		super();
	}
	
	public boolean hasSameBook(CartItem cartitem) {
		return this.book.equals(cartitem.book);
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if (!(o instanceof CartItem)){
		    return false;
		}
		CartItem cartitem = (CartItem)o;
		if(hasSameBook(cartitem)){
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public int hashCode(){
		return this.book.hashCode();
	}
	
	public BigDecimal getCartItemPrice() {
		return new BigDecimal(this.quantity).multiply(this.book.price);
	}

	@Override
	public int compareTo(CartItem o) {
		return this.book.compareTo(o.book);
	}

}
