package contest.winter2017;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;

import contest.winter2017.Tester.TesterOptions;

public class ProgramRunner {
	private final String jarToTestPath;
	private final String jacocoAgentJarPath;
	private final String jacocoOutputFilePath;
	private final int numThreads;
	private final boolean yamlOnly;
	private final boolean printDebug;
	
	public final int securityTestTime;
	public final int securityTestIterations;

	public ProgramRunner(TesterOptions options) {
		this.jarToTestPath = options.jarToTestPath;
		this.jacocoAgentJarPath = options.jacocoAgentJarPath;
		this.jacocoOutputFilePath = options.jacocoOutputFilePath;
		this.numThreads = options.numThreads;
		this.yamlOnly = options.yamlOnly;
		this.printDebug = options.verbose && !options.yamlOnly;
		
		this.securityTestTime = options.securityTestTime;
		this.securityTestIterations = options.securityTestIterations;
	}
	
	public List<Output> runTests(List<List<String>> testParametersList)
			throws InterruptedException, ExecutionException {
		return runTests(testParametersList, -1);
	}

	public List<Output> runTests(List<List<String>> testParametersList, int timeout)
			throws InterruptedException, ExecutionException {

		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		Timer t = new Timer();

		List<Future<Output>> futures = new ArrayList<>();
		if (timeout > 0) {
			t.schedule(new CancelFuturesTask(executor, futures), timeout * 1000);
		}

		// execute tests
		testParametersList.stream()
				.map(parameters -> new TestCallable(parameters))
				.map(callable -> executor.submit(callable))
				.forEachOrdered(futures::add);

		// collect results in list
		List<Output> results = new ArrayList<Output>();
		for (Future<Output> future : futures) {
			try {
				results.add(future.get());
			}
			catch (CancellationException e) { }
		}

		t.cancel();
		executor.shutdownNow();
		return results;
	}

	/**
	 * This method will instrument and execute the jar under test with the supplied parameters.
	 * This method should be used for both basic tests and security tests.
	 * 
	 * An assumption is made in this method that the word java is recognized on the command line
	 * because the user has already set the appropriate environment variable path. 
	 * 
	 * @param parameters - array of Objects that represents the parameter values to use for this 
	 *                     execution of the jar under test
	 * 
	 * @return Output representation of the standard out and standard error associated with the run
	 */
	public Output instrumentAndExecuteCode(List<String> parameters) {
		// we are building up a command line statement that will use java -jar to execute the jar
		// and uses jacoco to instrument that jar and collect code coverage metrics
		List<String> command = new ArrayList<String>();
		command.add("java");
		command.add("-javaagent:" + jacocoAgentJarPath + "=destfile=" + jacocoOutputFilePath);
		command.add("-jar");
		command.add(this.jarToTestPath);

		// add parameters for jar
		command.addAll(parameters);

		// show the user the command to run and prepare the process using the command
		if (printDebug) {
			System.out.println("command to run: " + command);
		}

		ProcessBuilder pb = new ProcessBuilder(command);
		String stdOutString;
		String stdErrString;

		// stdout and stderr should be read in separate threads, but whatever
		try {
			Process process = pb.start();
			InputStream isOut = process.getInputStream();
			InputStream isErr = process.getErrorStream();

			// await completion
			process.waitFor();

			stdOutString = IOUtils.toString(isOut, Charset.defaultCharset());
			stdErrString = IOUtils.toString(isErr, Charset.defaultCharset());
		}
		catch (IOException e) {
			if (!yamlOnly) {
				System.out.println("ERROR: Failed to execute test: " + command);
				e.printStackTrace();
			}
			return null;
		}
		catch (InterruptedException e) {
			// this occurs when the task is terminated due to a timeout
			return null;
		}

		// we now have the output as an object from the run of the black-box jar
		// this output object contains both the standard output and the standard error
		return new Output(stdOutString, stdErrString);
	}

	private class TestCallable implements Callable<Output> {
		private final List<String> parameters;

		public TestCallable(List<String> parameters) {
			this.parameters = parameters;
		}

		@Override
		public Output call() {
			return instrumentAndExecuteCode(parameters);
		}
	}

	private class CancelFuturesTask extends TimerTask {
		private ExecutorService executor;
		private List<Future<Output>> futures;

		public CancelFuturesTask(ExecutorService executor, List<Future<Output>> futures) {
			this.executor = executor;
			this.futures = futures;
		}

		@Override
		public void run() {
			if (!executor.isShutdown()) {
				if (!yamlOnly) {
					System.out.println("Time limit exceeded, terminating remaining tasks...");
				}
				executor.shutdownNow();
				for (Future<Output> future : futures) {
					if (!future.isDone()) {
						future.cancel(true);
					}
				}
			}
		}
	}
}
