package contest.winter2017;

import java.util.List;

/**	
 *	 Parameters used to execute jars are tricky things (think command line flags), so we developed a ParameterFactory 
 * 	 class to help you get the parameter types needed to execute the given jar. Why are executable jar command line 
 *	 parameters/arguments tricky? Because they can be fixed (static) or dependent (dynamic).
 *	
 *	 Fixed parameters are fairly simple. When a jar simply takes a fixed number/type of parameters as inputs 
 *	 (e.g. java -jar isXDivisibleByY.jar 100 10), the order of those two inputs matters, and the types are fixed.
 *	 
 * 	 Dependent parameters occur when subsequent parameter types/values depend upon previous types/values. Often times, there
 *	 are multiple options at each of the levels. For example, take the following command:
 *	 java -jar randomGenerator.jar --randomrange start=100 stop=1000 step=0.5
 *	 The first argument (--randomrange) was one possibility from several options (--shuffle, --randomint, or --sample). 
 *	 The second, third, and fourth arguments are a result of selecting --randomrange (dependent upon the first parameter), 
 *	 and they could be in any order.
 *	
 *	 ParameterFactory is our attempt to reduce the complexity related to dependent parameters. Parameter definitions are built 
 *   in an iterative manner: on each iteration that it will return all of the potential options for that parameter index (each 
 *   time you call, you pass in the sum of previous selections to build up the parameter definition dynamically). The method 
 *   that we wrote to help is called getNext(List<String> previousParameterValues);
 *   
 *   YOU ARE WELCOME TO CHANGE THIS CLASS, INCLUDING THE APPROACH. KEEP IN MIND THAT YOU CAN'T CHANGE THE EXISTING FORMAT IN THE 
 *   BLACK-BOX JARS THOUGH. 
 *  
 *   @author IDT
 */
public abstract class ParameterFactory {

	/**
	 * Method to test if the parameters associated with this jar are fixed (aka bounded)
	 * @return true if the parameters are fixed (bounded) and false if they are not
	 */
	public abstract boolean isBounded();


	/**
	 * Method to deal with the complexity of dependent parameters. Also handles fixed parameters.
	 * For more information about dependent and fixed parameters, see explanation at the top of this
	 * class. We are essentially determining the potential parameters for a given index, and that index  
	 * is determined by the values in previous ParameterValues (hence, we call this iteratively and build
	 * the definition). This code is certainly fair game for change. 
	 * 
	 * @param previousParameterValues - since this method is used iteratively to build up the parameter
	 *        definitions, this is the accumulated parameters that have been passed in until now
	 * @return List of Parameter objects containing all metadata known about the each Parameter
	 */
	public abstract List<Parameter> getNext(List<String> previousParameterValues);

}
