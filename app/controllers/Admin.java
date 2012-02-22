package controllers;

import models.BookShopUser;
import play.Play;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;


@With(Secure.class)
@Check("admin")
public class Admin extends Controller {
	
	@Before
	static void setConnectedUser() {
		renderArgs.put("shopLogoText", Play.configuration.getProperty("bookshop.logotext"));
        if(Security.isConnected()) {
            BookShopUser user = BookShopUser.find("byEmail", Security.connected()).first();
            if(user.isAdmin) {
            	renderArgs.put("user", user.fullName);
            }else {
            	Application.index();
            }
        }
	}

	public static void index() {
		System.out.println("Admin::index()");
		render();
	}
	

}
