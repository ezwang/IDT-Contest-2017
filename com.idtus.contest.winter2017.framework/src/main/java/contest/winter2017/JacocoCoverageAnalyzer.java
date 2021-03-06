package contest.winter2017;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import org.jacoco.core.tools.ExecFileLoader;

import contest.winter2017.Tester.TesterOptions;

class JacocoCoverageAnalyzer {
	private final String jacocoOutputFilePath;
	private final String jarToTestPath;

	private CoverageBuilder coverageBuilder = null;

	public JacocoCoverageAnalyzer(TesterOptions options) throws IOException {
		this.jacocoOutputFilePath = options.jacocoOutputFilePath;
		this.jarToTestPath = options.jarToTestPath;
	}

	private void loadCoverage() throws IOException {
		// creating a new file for output in the jacoco output directory (one of the application arguments)
		File executionDataFile = new File(this.jacocoOutputFilePath);
		ExecFileLoader execFileLoader = new ExecFileLoader();
		execFileLoader.load(executionDataFile);

		// use CoverageBuilder and Analyzer to assess code coverage from jacoco output file
		coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(
				execFileLoader.getExecutionDataStore(), coverageBuilder);

		// analyzeAll is the way to go to analyze all classes inside a container (jar or zip or directory)
		File jarToTest = new File(jarToTestPath);
		analyzer.analyzeAll(jarToTest);
	}


	/**
	 * Method used to print raw code coverage stats including hits/probes
	 * @throws IOException
	 */
	public void printRawCoverageStats()  {
		System.out.printf("exec file: %s%n", this.jacocoOutputFilePath);
		System.out.println("CLASS ID         HITS/PROBES   CLASS NAME");

		try {
			File executionDataFile = new File(this.jacocoOutputFilePath);
			final FileInputStream in = new FileInputStream(executionDataFile);
			final ExecutionDataReader reader = new ExecutionDataReader(in);
			reader.setSessionInfoVisitor(new ISessionInfoVisitor() {
				public void visitSessionInfo(final SessionInfo info) {
					System.out.printf("Session \"%s\": %s - %s%n",
							info.getId(),
							new Date(info.getStartTimeStamp()),
							new Date(info.getDumpTimeStamp()));
				}
			});
			reader.setExecutionDataVisitor(new IExecutionDataVisitor() {
				public void visitClassExecution(final ExecutionData data) {
					System.out.printf("%016x  %3d of %3d   %s%n",
							Long.valueOf(data.getId()),
							Integer.valueOf(getHitCount(data.getProbes())),
							Integer.valueOf(data.getProbes().length),
							data.getName());
				}
			});
			reader.read();
			in.close();
		} catch (IOException e) {
			System.out.println("Unable to display raw coverage stats due to IOException related to "
					+ this.jacocoOutputFilePath);
		}
		System.out.println();
	}


	/**
	 * Method used to get hit count from the code coverage metrics
	 * @param data - boolean array of coverage data where true indicates hits
	 * @return int representation of count of total hits from supplied data
	 */
	private int getHitCount(final boolean[] data) {
		int count = 0;
		for (final boolean hit : data) {
			if (hit) {
				count++;
			}
		}
		return count;
	}


	/**
	 * Method for generating code coverage metrics including instructions, branches, lines,
	 * methods and complexity.
	 * 
	 * @return double representation of the percentage of code covered during testing
	 */
	public double generateSummaryCodeCoverageResults() {
		try {
			loadCoverage();
		} catch (IOException e) {
			e.printStackTrace();
			return 0.0;
		}

		long total = 0;
		long covered = 0;

		for (final IClassCoverage cc : coverageBuilder.getClasses()) {
			// ignore the TestBounds class within the jar
			if (cc.getName().endsWith("TestBounds")) {
				continue;
			}

			total += cc.getInstructionCounter().getTotalCount();
			total += cc.getBranchCounter().getTotalCount();
			total += cc.getLineCounter().getTotalCount();
			total += cc.getMethodCounter().getTotalCount();
			total += cc.getComplexityCounter().getTotalCount();

			covered += cc.getInstructionCounter().getCoveredCount();
			covered += cc.getBranchCounter().getCoveredCount();
			covered += cc.getLineCounter().getCoveredCount();
			covered += cc.getMethodCounter().getCoveredCount();
			covered += cc.getComplexityCounter().getCoveredCount();
		}

		double percentCovered = ((double)covered / (double)total) * 100.0;
		return percentCovered;
	}


	/**
	 * This method shows an example of how to generate code coverage metrics from Jacoco
	 * 
	 * @return Map<String, Map<String, String>> representing code coverage results
	 */
	@SuppressWarnings("rawtypes")
	public Map<String, Map> generateDetailedCodeCoverageResults() {
		try {
			loadCoverage();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		Map<String, Map> executionResults = new TreeMap<String, Map>();

		for (final IClassCoverage cc : coverageBuilder.getClasses()) {
			// ignore the TestBounds class within the jar
			if (cc.getName().endsWith("TestBounds")) {
				continue;
			}

			Map<String, Map> coverage = new TreeMap<String, Map>();

			coverage.put("instructions", getMetricResultMap(cc.getInstructionCounter()));
			coverage.put("branches", getMetricResultMap(cc.getBranchCounter()));
			coverage.put("lines", getMetricResultMap(cc.getLineCounter()));
			coverage.put("methods", getMetricResultMap(cc.getMethodCounter()));
			coverage.put("complexity", getMetricResultMap(cc.getComplexityCounter()));

			executionResults.put(cc.getName(), coverage);

			// adding this to a string is a little impractical with the size of some of the files,
			// so we are commenting it out, but it shows that you can get the coverage status of each line
			// if you wanted to add debug argument to display this level of detail at command line level....
			/*
			for (int i = cc.getFirstLine(); i <= cc.getLastLine(); i++) {
				coverage.put("Line " + Integer.valueOf(i) + ": " + getStatusString(cc.getLine(i).getStatus()) + "\n");
			}
			 */
		}

		return executionResults;
	}


	/**
	 * Method to translate the Jacoco line coverage status integers to Strings.
	 * 
	 * @param status - integer representation of line coverage status provided by Jacoco
	 * @return String representation of line coverage status (not covered, partially covered, fully covered)
	 */
	@SuppressWarnings("unused")
	private String getStatusString(final int status) {
		switch (status) {
		case ICounter.NOT_COVERED:
			return "not covered";
		case ICounter.PARTLY_COVERED:
			return "partially covered";
		case ICounter.FULLY_COVERED:
			return "fully covered";
		}
		return "";
	}


	/**
	 * Method to translate the counter data and units into a human readable metric result String
	 * 
	 * @param unit
	 * @param counter
	 * @return
	 */

	private Map<String, Integer> getMetricResultMap(final ICounter counter) {
		Map<String, Integer> res = new HashMap<String, Integer>();
		res.put("missed", counter.getMissedCount());
		res.put("covered", counter.getCoveredCount());
		res.put("total", counter.getTotalCount());
		return res;
	}
}
