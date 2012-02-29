package validation;

import java.util.HashMap;
import java.util.Map;

import net.sf.oval.Validator;
import net.sf.oval.context.OValContext;
import net.sf.oval.exception.OValException;

public class CCNumberLengthCheck extends net.sf.oval.configuration.annotation.AbstractAnnotationCheck<CCNumber>{
	/** Error message key. */
    public final static String message = "validation.ccnum_minsz";
    int minSize;
    
    @Override
    public void configure(CCNumber annotation) {
        this.minSize = annotation.value();
        setMessage(annotation.message());
    }
    
	@Override
	public boolean isSatisfied(Object validatedObject, Object value1, OValContext context,Validator validator) throws OValException {
		requireMessageVariablesRecreation();
		String param = value1.toString();
		System.out.println("CCNumberLengthCheck::isSatisfied():: param="+param+"="+param.length());
		String trimmed=param.trim();
		System.out.println("CCNumberLengthCheck::minSize="+minSize);
		if(trimmed.length()< minSize) {
			System.out.println("CCNumberLengthCheck::trimmed.length()< minSize");
			return false;
		}
		return isNumber(trimmed);
	}
	
	@Override
    public Map<String, String> createMessageVariables() {
        Map<String, String> messageVariables = new HashMap<String, String>();
        messageVariables.put("minSize", Integer.toString(minSize));
        return messageVariables;
    }
	
	public static boolean isNumber(String value) { return value.matches("\\d+"); }

}
