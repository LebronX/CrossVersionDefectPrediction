package gitDataProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CommitSpliter {
	Map<String, String> versions;
	Map<String, LinkedList<Commit>> commitGroups;
	LinkedList<String> versionDates;
	
	public static final int DATE_AFTER_RELEASE = 0;
	public static final int CLOSEST_RELEASE_DATE = 1;
	
	public CommitSpliter() {
		versions = new HashMap<>();
		commitGroups = new HashMap<>();
		versionDates = new LinkedList<>();
	}
	
	public void readVersions(String filepath) {
		File file = new File(filepath);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			SimpleDateFormat parser = new SimpleDateFormat("dd MMM yyyy", Locale.US);
			SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd");
			while ((line = reader.readLine()) != null) {
				line.trim();
				String[] verInfo = line.split("#");
				try {
					Date date = parser.parse(verInfo[0]);
					String dateStr = formater.format(date);
					versions.put(dateStr, verInfo[1]);
					versionDates.add(dateStr);
					commitGroups.put(dateStr, new LinkedList<>());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		versionDates.sort(new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {				
				return s1.compareTo(s2);
			}
		});
//		for (String date : versionDates) {
//			System.out.println(date);
//		}
	}
	
	public void printList() {
		for (String date : versionDates) {
			System.out.println(date);
		}
	}
	
	
	public String dateSplit(String dateString) {
		for (int i = 0; i < versionDates.size(); i++) {
			int compare = dateString.compareTo(versionDates.get(i));
			if (compare <= 0) {
				return i == 0 ? versionDates.get(0) : versionDates.get(i-1);
			}
		}
		return versionDates.getLast();
	}
	
	public String dateSplit1(String dateString) {
		String ceil  = null;
		String floor = null;
		for (String verDate : versionDates) {
			int compare = dateString.compareTo(verDate);
			if (compare < 0) {
				ceil = verDate;
				break;
			}else if (compare > 0){
				floor = verDate;
			}else {
				return verDate;
			}
		}
		if (floor == null) {
			return ceil;
		}
		if (ceil == null) {
			return floor;
		}
		SimpleDateFormat parser = new SimpleDateFormat("yyyyMMdd");
		Date ceilDate =null;
		Date floorDate = null;
		Date date = null;
		try {
			ceilDate = parser.parse(ceil);
			floorDate = parser.parse(floor);
			date = parser.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		int diff1 = date.compareTo(floorDate);
		int diff2 = ceilDate.compareTo(date);	
		
		String res = diff1 < diff2 ? floor : ceil;
		return res;
	}
	

	
	public void clearVersionGroup() {
		for (LinkedList<Commit> list : commitGroups.values()) {
			list.clear();
		}
	}
	
	public void splitCommits(LinkedList<Commit> commits, int strategy) {
		clearVersionGroup();
		for (Commit commit : commits) {
			SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd");
		
			String dateStr = formater.format(commit.getDate().getTime());
			String version = null;
			if (strategy == CommitSpliter.DATE_AFTER_RELEASE) {
				version = dateSplit(dateStr);
			}else if (strategy == CommitSpliter.CLOSEST_RELEASE_DATE) {
				version = dateSplit1(dateStr);
			}
			commitGroups.get(version).add(commit);	
		}
		
//		for (String dateStr : versionDates) {
//			LinkedList<Commit> versionCommits = commitGroups.get(dateStr);
//			System.out.println(versions.get(dateStr) + "----------------------------" + versionCommits.size());
//			//pause();
//		}
	}
	
	
	public void writeBuggyFiles(String name, int filtStrategy, int splitStrategy) {
		for (String versionDate : versionDates) {
			String tag = versions.get(versionDate);
			String filePath = Util.getVersBugFilePath(name, filtStrategy, splitStrategy, tag);
			HashSet<String> buggyFiles = new HashSet<>();
			LinkedList<Commit> commits = commitGroups.get(versionDate);
			
			for (Commit commit : commits) {
				Map<String, String> files = commit.getFiles();
				for (String filename : files.keySet()) {
					if ((filename.endsWith(".java") || filename.endsWith(".py")) && files.get(filename).equals("M")) {
						//System.out.println(filename);
						buggyFiles.add(filename);
					}
				}
			}
			String versionDir = Util.getVersReleaseDir(name, tag);
			LinkedList<String> files = new LinkedList<>(buggyFiles);
			//int size = files.size();
			//System.out.println(versionDir);
			
			Set<String> allFiles = FileScanner.scanFiles(versionDir);
			int fileNum = FileScanner.getDirFileNum(versionDir);
			//System.out.println("***************************************");
			int count = 0;
			for (String file : files) {
				//System.out.println(file);
				if (allFiles.contains(file)) {
					count++;
				}
			}
			//Util.pause();
			
			
			Double rate = (double)count /(double)fileNum * 100;
			DecimalFormat df = new DecimalFormat("00.00");
			String titleLine = count + "/" + fileNum + "=" + df.format(rate);
			System.out.println(tag + "---" + titleLine);
			files.addFirst(titleLine);
			Util.writeFile(filePath, files);
		}
	}
	
	
	public void writeFile(String filepath) {
		LinkedList<String> content = new LinkedList<>();
		for (String verDate : versionDates) {
			String versionTag = versions.get(verDate);
			LinkedList<Commit> versionCommits = commitGroups.get(verDate);
			String titleLine = "#" + versionTag + "#" + verDate + "#" +versionCommits.size() + "\n";
			content.add(titleLine);
			for (Commit commit : versionCommits) {
				LinkedList<String> commitStrs = commit.toStringList();
				commitStrs.addFirst("---");
				for (String line : commitStrs) {
					content.add(line);
				}
			}
		}
		Util.writeFile(filepath, content);
	}
	
	
	public static void main(String[] args) {
		String date1 = "20170328";
		String date2 = "20180103";
		System.out.println(date1.compareTo(date2));
		
//		String filepath = "./xalan-versions.txt";
//		CommitSpliter cs = new CommitSpliter();
//		cs.readVersions(filepath);
//		LinkedList<Commit> commits = ParseLog.readFormatedCommits("./xalan-out.txt");
//		System.out.println("=========bug and fix===all after release========");
//		cs.splitCommits(commits, 0);
//		//cs.writeFile("./xalan2-splited-0.txt");
	}
}
