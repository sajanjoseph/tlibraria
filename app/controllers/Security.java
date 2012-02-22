package controllers;

import models.BookShopUser;
import utils.HashUtils;

public class Security extends controllers.Secure.Security {
	//custom auth methods
	static boolean authenticate(String username, String password) {
    	System.out.println("controllers.Security.authenticate()");
    	String hashOfpassword = HashUtils.makeHash(password);
    	boolean authenticated = BookShopUser.connect(username, hashOfpassword) != null;
    	BookShopUser user = BookShopUser.find("byEmail", username).first();
    	if (user!=null) {
			session.put("loggedinuser", user.fullName);//to display loggedin user name in view
		}
    	System.out.println("controller.Security.authenticate():authenticated="+authenticated);
        return authenticated;
    }
	
	//logged out user shown the Home page
	static void onDisconnected() {
		System.out.println("controller.Security.onDisconnected()::");
	    Application.index();
	}
	
	/*
	 * if logged in user is an admin,show him admin page
	 * if customer,take him to the last url
	 * if last url is null ,go to Home page
	 */
	static void onAuthenticated() {
		System.out.println("controllers.Security.onAuthenticated()");
		BookShopUser user = BookShopUser.find("byEmail",Security.connected()).first();
		if(user.isAdmin) {
			Admin.index();
		}
		else {
			String url = flash.get("url");
			if(url!=null) {
				redirect(url);
			}else {
				redirect("/");
			}
		}
	}
	
	/*
	 * when the @Check(profile) annotated methods are called ,call this method to find out
	 * if access is allowed
	 */
	static boolean check(String profile) {
		System.out.println("controllers.Security.check("+profile+")");
		BookShopUser user = BookShopUser.find("byEmail", connected()).first();
		if("admin".equals(profile)) {
			//for check on "admin" if the logged in user is an admin return true else false
			return user.isAdmin;
			
		}else if("customer".equals(profile)) {
			//for check on "customer" if the logged in user is a customer return true else false
			return user.isCustomer;
		}else {
			return false;
		}
		
	}

}
