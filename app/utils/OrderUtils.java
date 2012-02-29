package utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class OrderUtils {
	public static Date incrementDate(Date now,int weeks,int days) {
		Date incremented = null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		cal.add(Calendar.WEEK_OF_YEAR, weeks < 0 ?  0 :weeks);
		cal.add(Calendar.DAY_OF_YEAR, days < 0 ? 0 : days);
		incremented = cal.getTime();
		System.out.println("OrderUtils:incrementDate()::incremented="+incremented);
		return incremented;
	}
	public static String createOrderNumberString(String email,Date orderDate) {
		System.out.println("OrderUtils:createOrderNumberString()::email="+email);
		System.out.println("OrderUtils:createOrderNumberString()::email="+orderDate);
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		String datestring = df.format(orderDate).toString();
		System.out.println("OrderUtils:createOrderNumberString()::datestring="+datestring);
		StringBuffer strbuffer = new StringBuffer(datestring);
		strbuffer.append(email.replace("@", "-").replace(".", "-"));
		return strbuffer.toString();
	}

}
