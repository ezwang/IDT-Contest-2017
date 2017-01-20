package contest.winter2017;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FixedParameterFactory extends ParameterFactory {
	private List<Object> fixedParameterList;

	public FixedParameterFactory(List<Object> fixedParameterList) {
		this.fixedParameterList = fixedParameterList;
	}

	@Override
	public boolean isBounded() {
		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<Parameter> getNext(List<String> previousParameterValues) {
		List<Parameter> possibleParamsList = new ArrayList<Parameter>();

		if (previousParameterValues.size() < fixedParameterList.size()) {
			Map paramMap = (Map) fixedParameterList.get(previousParameterValues.size());
			possibleParamsList.add(new Parameter(paramMap));
		}
		return possibleParamsList;
	}
}
