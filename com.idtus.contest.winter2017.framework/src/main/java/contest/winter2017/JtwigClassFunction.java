package contest.winter2017;

import org.jtwig.functions.FunctionRequest;
import org.jtwig.functions.SimpleJtwigFunction;

public class JtwigClassFunction extends SimpleJtwigFunction {

	@Override
	public Object execute(FunctionRequest arg0) {
		String val = arg0.get(0).toString();
		int sep = val.lastIndexOf("/");
		if (sep < 0) {
			return new String[] { val, "" };
		}
		return new String[] { val.substring(sep + 1), val.substring(0, sep) };
	}

	@Override
	public String name() {
		return "class";
	}

}
