package contest.winter2017;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DependentParameterFactory extends ParameterFactory {
	// key: regex, value: parameter
	private Map<String, Object> dependentParametersMap;

	public DependentParameterFactory(Map<String, Object> dependentParametersMap) {
		this.dependentParametersMap = dependentParametersMap;
	}

	@Override
	public boolean isBounded() {
		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<Parameter> getNext(List<String> previousParameterValues) {
		String currentParamsString = getCurrentParamsString(previousParameterValues);
		List<Parameter> possibleParamsList = new ArrayList<Parameter>();

		for (Map.Entry<String, Object> mapEntry : dependentParametersMap.entrySet()) {
			String regex = mapEntry.getKey();
			Object obj = mapEntry.getValue();

			if (currentParamsString.matches(regex)|| (regex.isEmpty() && currentParamsString.isEmpty())) {
				if (obj instanceof Map) {
					possibleParamsList.add(new Parameter((Map) obj));
				} else {
					for (Map paramMap : (List<Map>) obj) {
						possibleParamsList.add(new Parameter(paramMap));
					}
				}
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
