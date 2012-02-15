package models;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import play.db.jpa.Model;

@Entity
public class BookOrder extends Model {
	
	@OneToMany( cascade=CascadeType.ALL,orphanRemoval=true)
	public Set<CartItem> cartItems;

	public String status;
	
	public Date order_date;
	
	public String orderNumber;
	
	@ManyToOne
	public Payment paymentMethod;
	
	@ManyToOne
	public BookShopUser customer;
	
	public BookOrder() {
		this(new TreeSet<CartItem>(),null,"pending",new Date());
	}
	
	public BookOrder(Set<CartItem> cartItems,BookShopUser customer,String status,Date order_date) {
		super();
		this.cartItems = cartItems;
		this.customer = customer;
		this.status = status;
		this.order_date = order_date;
	}
	
	public BookOrder(BookShopUser customer) {
		this(new TreeSet<CartItem>(),customer,"pending",new Date());
	}
	
	public boolean addItem(CartItem item,int qty){
		return cartItems.add(item);
	}
	
	public boolean addItem(CartItem item){
		return cartItems.add(item);
	}
	
	public void removeItem(CartItem citem){
		String isbn = citem.book.isbn;
		removeItem(isbn);
	}
	
	
	public void removeItem(String bookIsbn){
		/*
		 * Iterator.remove is the only safe way to modify a collection during iteration; 
		 * the behavior is unspecified if the underlying collection is modified in any other way
		 * while the iteration is in progress.
		 * 
		 */
		Iterator<CartItem> cartitemsItr = this.cartItems.iterator();
		while(cartitemsItr.hasNext()){
			CartItem item = cartitemsItr.next();
			if(bookIsbn.equals(item.book.isbn)){
				cartitemsItr.remove();
			}
		}
		
	}
	
	public int cartItemsCount() {
		return this.cartItems.size();
	}
	
	public BigDecimal getTotalPrice() {
		BigDecimal total = BigDecimal.ZERO;
		for(CartItem item: this.cartItems) {
			BigDecimal lineTotal = new BigDecimal(item.quantity).multiply(item.book.price);
			total = total.add(lineTotal);
		}
		return total;
	}
	

}
