package gitDataProcess;


import java.util.LinkedList;


public class Runner {
	
	public static String[] softwareNames = {
		"xalan"
		, 
		"beam"	
		, 
		"camel"
		,
		"cloudstack"
		,
		"geode"
		,
		"isis"
		,
		"mahout"
		,
		"jmeter"
		,
		"okhttp"
		,	
		"scikit", "tensorflow", 
		"celery", 
		"ipython", "kivy", "zulip"
	};
	
	
		
	
	public static void main(String[] args) {
		for (String name : softwareNames) {
			String file = Util.getLogFilePath(name);
			String version = Util.getVersionFilePath(name);
			
//			//bug & fix logs
//			ParseLog pl = new ParseLog(file);
//			String filtFile = getFilteredFilePath(name, ParseLog.FILTER_BUG_AND_FIX);
//			LinkedList<Commit> commits = pl.parse(ParseLog.FILTER_BUG_AND_FIX, filtFile);
//			System.out.println(name + "-----bug&fix-----" + commits.size());
//			CommitSpliter cs = new CommitSpliter();
//			cs.readVersions(version);
//			System.out.println("---date after release---");
//			//split strategy: 0
//			cs.splitCommits(commits, CommitSpliter.DATE_AFTER_RELEASE);
//			String outFile1 = getGroupedFilePath(name, ParseLog.FILTER_BUG_AND_FIX, CommitSpliter.DATE_AFTER_RELEASE);
//			cs.writeFile(outFile1);
//			cs.writeBuggyFiles(name, ParseLog.FILTER_BUG_AND_FIX, CommitSpliter.DATE_AFTER_RELEASE);			
//			System.out.println("---closest release date");
//			//split strategy: 1
//			cs.splitCommits(commits, CommitSpliter.CLOSEST_RELEASE_DATE);
//			String outFile2 = getGroupedFilePath(name, ParseLog.FILTER_BUG_AND_FIX, CommitSpliter.CLOSEST_RELEASE_DATE);
//			cs.writeFile(outFile2);
//			cs.writeBuggyFiles(name, ParseLog.FILTER_BUG_AND_FIX, CommitSpliter.CLOSEST_RELEASE_DATE);
			
			//bug || fix
			ParseLog pl2 = new ParseLog(file);
			String filtFile2 = Util.getFilteredFilePath(name, ParseLog.FILTER_BUG_OR_FIX);
			LinkedList<Commit> commits2 = pl2.parse(ParseLog.FILTER_BUG_OR_FIX, filtFile2);
			System.out.println(name + "-----bug||fix-----" + commits2.size());
			//split strategy 0
			CommitSpliter cs = new CommitSpliter();
			cs.readVersions(version);
//			System.out.println("---date after release---");
//			cs.splitCommits(commits2, CommitSpliter.DATE_AFTER_RELEASE);
//			
//			String outFile3 = Util.getGroupedFilePath(name, ParseLog.FILTER_BUG_OR_FIX, CommitSpliter.DATE_AFTER_RELEASE);
//			cs.writeFile(outFile3);
//			cs.writeBuggyFiles(name, ParseLog.FILTER_BUG_OR_FIX, CommitSpliter.DATE_AFTER_RELEASE);
			
			//split strategy 1
			System.out.println("---closest release date");
			cs.splitCommits(commits2, CommitSpliter.CLOSEST_RELEASE_DATE);
//			String outFile4 = getGroupedFilePath(name, ParseLog.FILTER_BUG_OR_FIX, CommitSpliter.CLOSEST_RELEASE_DATE);
//			cs.writeFile(outFile4);
			cs.writeBuggyFiles(name, ParseLog.FILTER_BUG_OR_FIX, CommitSpliter.CLOSEST_RELEASE_DATE);
		
			FileScanner.writeRecords();
			System.out.println("===================================");
		}
	}
}
