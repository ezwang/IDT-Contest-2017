package contest.winter2017;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that represents a single parameter for an executable jar. 
 * 
 * @author IDT
 */
public class Parameter {

	@SuppressWarnings("rawtypes")
	final private Class type;
	final private String format;
	final private Object min;
	final private Object max;
	final private boolean optional;


	/**
	 * Pattern to replace if the parameter is formatted
	 */
	private Pattern replaceMePattern = null;


	/**
	 * Ctr for Parameter
	 */
	@SuppressWarnings("rawtypes")
	public Parameter(Class type, String format, Object min, Object max, boolean optional) {
		this.type = type;
		this.format = format;
		this.min = min;
		this.max = max;
		this.optional = optional;
	}

	/**
	 * Getter for type of parameter (integer, long, double, float, String, etc)
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Class getType() {
		return type;
	}

	/**
	 * Getter for the min value associated with this parameter (if one exists)
	 * @return Object representing the minimum value associated with this parameter
	 */
	public Object getMin() {
		return min;
	}

	/**
	 * Getter for the max value associated with this parameter (if one exists)
	 * @return Object representing the maximum value associated with this parameter
	 */
	public Object getMax() {
		return max;
	}

	/**
	 * Getter for the optionality of the parameter
	 * @return boolean true indicates the parameter is optional
	 */
	public boolean isOptional() {
		return optional;
	}


	/**
	 * Getter for the flag indicating whether or not the parameter has a specific format
	 * @return boolean true indicates the parameter has a specific format
	 */
	public boolean isFormatted() {
		return format != null;
	}


	/**
	 * Getter for the format string the parameter has if it has a specific one
	 * @return String with the parameters format <<REPLACE_ME_...>> are included
	 */
	public String getFormat() {
		return format;
	}


	/**
	 * Getter for the types of the variables (eg. <<REPLACE_ME_STRING>>) in the format string... useful for formatted enumerated values
	 * @return List<Class> that contains the types of each of the <<REPLACE_ME_...>> in the format string
	 */
	@SuppressWarnings("rawtypes")
	public List<Class> getFormatVariables() {
		if (format == null) {
			return null;
		}

		List<Class> typeList = new ArrayList<Class>();

		this.replaceMePattern = Pattern.compile("<<REPLACE_ME_(STRING|INT)>>");
		Matcher replaceMeMatcher = replaceMePattern.matcher(format);
		while(replaceMeMatcher.find()) {
			switch(replaceMeMatcher.group()) {
			case "<<REPLACE_ME_STRING>>":
				typeList.add(String.class);
				break;
			case "<<REPLACE_ME_INT>>":
				typeList.add(Integer.class);
				break;
			default:
				//NOP
				break;
			}
		}

		return typeList;
	}


	/**
	 * Utility method to build a valid formatted parameter by replacing all of the <<REPLACE_ME_...>> in the format parameter string
	 * @param List<Object> - containing the values that will replace the format for <<REPLACE_ME_...>> placeholders of this formatted parameter
	 * @return String containing the parameter with <<REPLACE_ME_...>> placeholders replaced with the passed in values
	 */
	public String getFormattedParameter(List<Object> formatVariableValues) {
		Matcher replaceMeMatcher = replaceMePattern.matcher(format);
		StringBuffer sb = new StringBuffer();
		for(Object variable : formatVariableValues) {
			if(replaceMeMatcher.find()) {
				replaceMeMatcher.appendReplacement(sb, variable.toString());
			}
		}
		replaceMeMatcher.appendTail(sb);

		return sb.toString();
	}

}
