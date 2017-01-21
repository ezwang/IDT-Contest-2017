package contest.winter2017;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DependentParameterFactory extends ParameterFactory {
	// key: regex, value: parameter
	private Map<String, List<Parameter>> dependentParametersMap;

	public DependentParameterFactory(Map<String, List<Parameter>> dependentParametersMap) {
		this.dependentParametersMap = dependentParametersMap;
	}

	@Override
	public boolean isBounded() {
		return false;
	}

	@Override
	public List<Parameter> getNext(List<String> previousParameterValues) {
		String currentParamsString = getCurrentParamsString(previousParameterValues);
		List<Parameter> possibleParamsList = new ArrayList<Parameter>();

		for (String regex : dependentParametersMap.keySet()) {
			List<Parameter> obj = dependentParametersMap.get(regex);
			boolean matches = currentParamsString.matches(regex) ||
					regex.isEmpty() && currentParamsString.isEmpty();
			if (matches) {
				possibleParamsList.addAll(obj);
			}
		}
		return possibleParamsList;
	}

	private static String getCurrentParamsString(List<String> previousParameterValues) {
		StringBuffer sb = new StringBuffer();
		for (String paramString : previousParameterValues) {
			sb.append(" " + paramString);
		}
		return sb.toString().trim();
	}
}
