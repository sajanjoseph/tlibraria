package utils;

public class PaymentUtils {
	public static String maskCCNumber(String ccnum){
		//long starttime = System.currentTimeMillis();
		int total = ccnum.length();
		int startlen=4,endlen = 4;
		int masklen = total-(startlen + endlen) ;
		StringBuffer maskedbuf = new StringBuffer(ccnum.substring(0,startlen));
		for(int i=0;i<masklen;i++) {
			maskedbuf.append('X');
		}
		maskedbuf.append(ccnum.substring(startlen+masklen, total));
		String masked = maskedbuf.toString();
		//long endtime = System.currentTimeMillis();
		//System.out.println("maskCCNumber:="+masked+" of :"+masked.length()+" size");
		//System.out.println("using StringBuffer="+ (endtime-starttime)+" millis");
		return masked;
	}

}
