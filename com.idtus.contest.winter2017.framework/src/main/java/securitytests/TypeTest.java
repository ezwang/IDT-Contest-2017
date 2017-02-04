package securitytests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import contest.winter2017.DependentParameterFactory;
import contest.winter2017.FixedParameterFactory;
import contest.winter2017.Parameter;
import contest.winter2017.ParameterFactory;
import contest.winter2017.ParameterType;

public class TypeTest implements SecurityTest {

	@Override
	public List<List<String>> getTests(ParameterFactory parameterFactory) {
		List<List<String>> out = new ArrayList<List<String>>();
		if (parameterFactory instanceof FixedParameterFactory) {
			FixedParameterFactory fixedParam = (FixedParameterFactory)parameterFactory;
			List<String> params = new ArrayList<String>();
			for (Parameter p : fixedParam.getParameterList()) {
				// add string where integers or doubles are supposed to be to check for error handling
				params.add("test");
			}
			out.add(params);
			List<String> params2 = new ArrayList<String>();
			for (Parameter p : fixedParam.getParameterList()) {
				// see what happens with very large numbers
				if (p.getTypeList().contains(new ParameterType.IntegerType()) || p.getTypeList().contains(new ParameterType.DoubleType())) {
					params2.add("99999999999999");
				}
				else {
					params2.add("test");
				}
			}
			out.add(params2);
			List<String> params3 = new ArrayList<String>();
			for (Parameter p : fixedParam.getParameterList()) {
				// see what happens with very small numbers
				if (p.getTypeList().contains(new ParameterType.IntegerType()) || p.getTypeList().contains(new ParameterType.DoubleType())) {
					params3.add("-99999999999999");
				}
				else {
					params3.add("test");
				}
			}
			out.add(params3);
		}
		else if (parameterFactory instanceof DependentParameterFactory) {
			DependentParameterFactory dependParam = (DependentParameterFactory) parameterFactory;
			Map<String, List<Parameter>> map = dependParam.getParameterMap();
			List<String> params = new ArrayList<String>();
			for (Entry<String, List<Parameter>> e : map.entrySet()) {
				if (!e.getKey().isEmpty()) {
					params.add(e.getKey().replaceAll("\\s*", ""));
				}
				params.add("test");
			}
			out.add(params);
		}
		return out;
	}

}
