package contest.winter2017;

import java.util.ArrayList;
import java.util.List;

public class FixedParameterFactory extends ParameterFactory {
	private List<Parameter> fixedParameterList;

	public FixedParameterFactory(List<Parameter> fixedParameterList) {
		this.fixedParameterList = fixedParameterList;
	}

	@Override
	public boolean isBounded() {
		return true;
	}

	@Override
	public List<Parameter> getNext(List<String> previousParameterValues) {
		List<Parameter> possibleParamsList = new ArrayList<Parameter>();

		if (previousParameterValues.size() < fixedParameterList.size()) {
			Parameter param = fixedParameterList.get(previousParameterValues.size());
			possibleParamsList.add(param);
		}
		return possibleParamsList;
	}
}
