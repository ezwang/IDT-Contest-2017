package contest.winter2017;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Class that represents a single parameter for an executable jar. 
 * 
 * @author IDT
 */
public class Parameter {
	private static final String STRING_REPLACE = "<<REPLACE_ME_STRING>>";
	private static final String INTEGER_REPLACE = "<<REPLACE_ME_INT>>";
	private static final Pattern REPLACE_PATTERN = Pattern.compile("<<REPLACE_ME_(STRING|INT)>>");

	private final List<ParameterType<?>> typeList;
	private final String format;
	private final boolean optional;

	/**
	 * Ctr for Parameter
	 */
	public Parameter(ParameterType<?> type, boolean optional) {
		this.format = null;
		this.optional = optional;
		this.typeList = new ArrayList<>();
		typeList.add(type);
	}


	public Parameter(String format, boolean optional) {
		this.format = format;
		this.optional = optional;
		this.typeList = new ArrayList<>();

		Matcher m = REPLACE_PATTERN.matcher(format);
		while (m.find()) {
			switch (m.group()) {
			case STRING_REPLACE:
				typeList.add(new ParameterType.StringType());
				break;
			case INTEGER_REPLACE:
				typeList.add(new ParameterType.IntegerType());
				break;
			default:
				throw new IllegalStateException();
			}
		}
	}


	/**
	 * Getter for the types of the variables in the format string... useful for formatted enumerated values
	 * @return List<ParameterType<?>> that contains the types in the format string
	 */
	public List<ParameterType<?>> getTypeList() {
		return typeList;
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
	 * Utility method to build a valid formatted parameter by replacing all of the <<REPLACE_ME_...>> in the format parameter string
	 * @param List<Object> - containing the values that will replace the format for <<REPLACE_ME_...>> placeholders of this formatted parameter
	 * @return String containing the parameter with <<REPLACE_ME_...>> placeholders replaced with the passed in values
	 */
	public String formatParameterChecked(List<?> formatVariableValues) {
		if (formatVariableValues.size() != typeList.size()) {
			throw new IllegalArgumentException("wrong number of values");
		}

		ListIterator<ParameterType<?>> typeIterator = typeList.listIterator();
		for (Object variable : formatVariableValues) {
			// check that type of variable is correct
			if (!typeIterator.next().isInstance(variable)) {
				throw new IllegalArgumentException("type of " + variable + " is incorrect");
			}
		}

		// convert to list of strings
		List<String> stringValues = formatVariableValues.stream()
				.map(String::valueOf).collect(Collectors.toList());
		return formatParameterFromStrings(stringValues);
	}

	public String formatParameterFromStrings(List<String> formatVariableValues) {
		if (formatVariableValues.size() != typeList.size()) {
			throw new IllegalArgumentException("wrong number of values");
		}

		// if there's no format, just return as string
		if (format == null) {
			assert typeList.size() == 1;
			return formatVariableValues.get(0);
		}

		// find pattern to replace in string
		Matcher m = REPLACE_PATTERN.matcher(format);
		StringBuffer sb = new StringBuffer();

		for (String variable : formatVariableValues) {
			// append replacement to string
			if (m.find()) {
				variable = Matcher.quoteReplacement(variable);
				m.appendReplacement(sb, variable);
			} else {
				throw new IllegalStateException();
			}
		}

		m.appendTail(sb);
		return sb.toString();
	}
}
