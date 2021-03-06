package contest.winter2017;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

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
			boolean matches = currentParamsString.matches(regex);
			if (matches) {
				possibleParamsList.addAll(obj);
			}
		}
		return possibleParamsList;
	}

	public Map<String, List<Parameter>> getParameterMap() {
		return dependentParametersMap;
	}

	private static String getCurrentParamsString(List<String> previousParameterValues) {
		return StringUtils.join(previousParameterValues, ' ');
	}
}
