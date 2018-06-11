package DataAnalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import defectPrediction.ClassifiersRunner;
import gitDataProcess.Util;

public class CSVDataAnalysis {
	
	public static final String dirPath = "E:/data/metrics_csv/";
	
	public static Map<String, String> readFilesAndBuggy(String filename) {
		Map<String, List<String>> allData = readAllFileData(filename);
		Map<String, String> res = simplifyData(allData);
		return res;
	}
	
	private static Map<String, String> simplifyData(Map<String, List<String>> data) {
		Map<String, String> res = new HashMap<String, String>();
		for (String key : data.keySet()) {
			List<String> instance = data.get(key);
			res.put(key, instance.get(instance.size()-1));
		}
		return res;
	}
	
	
	public static Map<String, List<String>> readAllFileData(String filename) {
		BufferedReader reader = null;
		Map<String, List<String>> res = new HashMap<String,List<String>>();
		try {
			reader = new BufferedReader(new FileReader(filename));
			String line = reader.readLine();
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if ("".equals(line)) {
					continue;
				}
				String[] parts = line.split(",");
				List<String> instance = Arrays.asList(parts);
				res.put(instance.get(0), instance);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	public static List<String> getVerFileList(String name) {
		List<String> files = new LinkedList<String>();
		String arffDirPath = dirPath + name + "/";
		File dir = new File(arffDirPath);
		if (!dir.exists() || !dir.isDirectory()) {
			System.out.println("directory error!");
			return files;
		}
		for (File subFile : dir.listFiles()) {
			String fileName = subFile.getName();
			if (fileName.endsWith(".csv")) {
				files.add(fileName);
			}
		}
		Util.sortFileName(files);
		return files;
	}
	
	public static double cosSimilarityStr(List<String> vector1, List<String> verctor2) {
		List<Double> v1 = new ArrayList<>();
		List<Double> v2 = new ArrayList<>();
		for (int i = 1; i < vector1.size() - 1; i++) {
			v1.add(Double.parseDouble(vector1.get(i)));
			v2.add(Double.parseDouble(vector1.get(i)));
		}
		return cosSimilarity(v1, v2);
	}
	
	public static double cosSimilarity(List<Double> x, List<Double> y) {
		int len = x.size();
		double numerator = 0;
		double denominator = 0;
		double xDoubleSum = 0;
		double yDoubleSum = 0;
		for (int i = 0; i < len; i++) {
			double x_i = x.get(i);
			double y_i = y.get(i);
			numerator += x_i * y_i;
			xDoubleSum +=  x_i * x_i;
			yDoubleSum += y_i * y_i;
		}
		denominator = Math.sqrt(xDoubleSum) * Math.sqrt(yDoubleSum);
		double res = numerator / denominator;
		return res;
	}
	
	
	public static void conflictFileSimilarity(Map<String, List<String>> data1, Map<String, List<String>> data2) {
		Map<String, String> fileBuggy1 = simplifyData(data1);
		Map<String, String> fileBuggy2 = simplifyData(data2);
		List<String> conflictFiles = getConflictFileList(fileBuggy1, fileBuggy2);
		for (String file : conflictFiles) {
			List<String> instance1 = data1.get(file);
			List<String> instance2 = data2.get(file);
			double cos = cosSimilarityStr(instance1, instance2);
			System.out.println(file + ":  " + cos);
		}
	}
	
	public static List<String> getCommonFileList(Map<String, String> data1, Map<String, String> data2) {
		List<String> res = new ArrayList<>();
		for (String filename : data1.keySet()) {
			if (data2.containsKey(filename)) {
				res.add(filename);
			}
		}
		return res;
	}
	
	public static List<String> getConflictFileList(Map<String, String> data1, Map<String, String> data2) {
		List<String> res = new ArrayList<>();
		List<String> commonFils = getCommonFileList(data1, data2);
		for (String file : commonFils) {
			if (!data1.get(file).equals(data2.get(file))) {
				res.add(file);
			}
		}
		return res;
	}
	
	
	public static String[] commonFileAnalysis(Map<String, String> data1, Map<String, String> data2, String name1, String name2) {
		//Map<String, Boolean> commonFiles = new HashMap<>();
		int conflictFileCount = 0;
		int commonFileCount = 0;
		int bothBugCount = 0;
		int buggyCount1 = 0;
		int buggyCount2 = 0;
		List<String> commonFiles = getCommonFileList(data1, data2);
		for (String filename : commonFiles) {
			String buggyStr1 = data1.get(filename);
			String buggyStr2 = data2.get(filename);
			boolean buggy1 = "1".equals(buggyStr1);
			boolean buggy2 = "1".equals(buggyStr2);
			buggyCount1 += buggy1 ? 1 : 0;
			buggyCount2 += buggy2 ? 1 : 0;
			boolean conflict = buggy1 != buggy2;
			commonFileCount += 1;
			conflictFileCount +=  conflict ? 1 : 0;
			bothBugCount += (buggy1 && buggy2) ? 1 : 0;
		}

		DecimalFormat df = new DecimalFormat("00.0");
		double conflictRate = (double)conflictFileCount/(double)commonFileCount * 100;
		String conflictRateStr = df.format(conflictRate) + "%";
		double bothBuggyRate = (double)bothBugCount/(double)buggyCount1 * 100;
		String bothBuggyRateStr = df.format(bothBuggyRate) + "%";
		String out = name1 + " file num: " + data1.size() + "\n"
					+ name2 + " file num: " + data2.size() + "\n"
					+ "common file num: " + commonFileCount + "\n"
					+ "conflix file num: " + conflictFileCount + "\n"
					+ conflictRateStr + " of common files are conflix!!!\n"
					+ name1 + " buggy count: " + buggyCount1 + "\n"
					+ name2 + " buggy count: " + buggyCount2 + "\n"
					+ "both buggy file num: " + bothBugCount + "\n"
					+ bothBuggyRateStr + " of common files are both buggy!";		
		System.out.println(out);
		String[] res = {
				commonFileCount + "", conflictFileCount + "", 
				buggyCount1 + "", buggyCount2 + "", bothBugCount + "",
				conflictRateStr, bothBuggyRateStr
		};
		return res;
	}
	
	public static void simAnalysisRunner(String name) {
		List<String> files = getVerFileList(name);
		List<Map<String, List<String>>> allVerFiles = new ArrayList<>();
		for (String file : files) {
			String filePath = dirPath + name + "/" + file;
			Map<String, List<String>> verFiles = readAllFileData(filePath);
			allVerFiles.add(verFiles);
		}
		int len = allVerFiles.size();
		for (int i = 0; i < len - 1; i++) {
			for (int j = i + 1; j < len; j++) {
				Map<String, List<String>> data1 = allVerFiles.get(i);
				Map<String, List<String>> data2 = allVerFiles.get(j);
				System.out.println("========" + "============");
				conflictFileSimilarity(data1, data2);
			}
		}
	}
	
	public static void commonAnalysisRunner(String name) {
		List<String> files = getVerFileList(name);
		List<Map<String, String>> allVerFiles = new ArrayList<>();
		List<String> tags = files.stream()
				.map(filename -> filename.substring(0, filename.length() - 4).split("_")[2])
				.collect(Collectors.toList());
		for (String file : files) {
			String filePath = dirPath + name + "/" + file;
			Map<String, String> verFiles = readFilesAndBuggy(filePath);
			allVerFiles.add(verFiles);
		}
		int len = allVerFiles.size();
		String[][] commonFiles = new String[len-1][len-1];
		String[][] conflictFiles = new String[len-1][len-1];
		String[][] bothBuggyFiles = new String[len-1][len-1];
		String[][] conflictRates = new String[len-1][len-1];
		String[][] bothBuggyRates = new String[len-1][len-1];
		int[] buggyCount = new int[len];
		for (int i = 0; i < len - 1; i++) {
			for (int j = i + 1; j < len; j++) {
				Map<String, String> data1 = allVerFiles.get(i);
				Map<String, String> data2 = allVerFiles.get(j);
				String ver1 = tags.get(i);
				String ver2 = tags.get(j);
				String notice = "===================" + name + "  "  + ver1 + " ---" + ver2 + "===========================";
				System.out.println(notice);
				String[] res = commonFileAnalysis(data1, data2, tags.get(i), tags.get(j));
				commonFiles[i][j-1] = res[0];
				conflictFiles[i][j-1] = res[1];			
				buggyCount[i] = Integer.parseInt(res[2]);
				buggyCount[j] = Integer.parseInt(res[3]);
				bothBuggyFiles[i][j-1] = res[4];
				conflictRates[i][j-1] = res[5];
				bothBuggyRates[i][j-1] = res[6];
				//System.out.println(notice + res);
			}
		}
		writeResults(name, allVerFiles, tags, commonFiles, conflictFiles, buggyCount, bothBuggyFiles, conflictRates, bothBuggyRates);
	}

	private static void writeResults(String name, List<Map<String, String>> allVerFiles, List<String> tags, String[][] commonFiles, 
			 String[][] conflictFiles, int[] bugCount, String[][] bothBuggyFiles, String[][] conflictRates, String[][] bothBuggyRates) {
		int len = allVerFiles.size();
		List<String> countTags = new ArrayList<>();
		List<String> buggyTags = new ArrayList<>();
		for (int i = 0; i < len; i++) {
			String countTag = tags.get(i) + "#" + allVerFiles.get(i).size();
			countTags.add(countTag);
			String buggyTag = tags.get(i) + "#" + bugCount[i];
			buggyTags.add(buggyTag);
		}
		List<String> tables = new ArrayList<>();
		tables.add(ClassifiersRunner.markdownTableFormatStr(commonFiles, countTags, "CommonFiles"));
		tables.add(ClassifiersRunner.markdownTableFormatStr(conflictFiles, countTags, "ConflictFiles"));
		tables.add(ClassifiersRunner.markdownTableFormatStr(conflictRates, countTags, "ConflictRate"));
		tables.add(ClassifiersRunner.markdownTableFormatStr(bothBuggyFiles, buggyTags, "BothBuggyFiles"));
		tables.add(ClassifiersRunner.markdownTableFormatStr(bothBuggyRates, buggyTags, "BothBuggyRate"));
		String tablePath = dirPath + name + "/" + name + "-commonAnalysis.txt";
		Util.writeFile(tablePath, tables);
	}
	
	public static void main(String[] args) {
		String[] softwares = {
				"xalan",
//				"jmeter", "camel", 
//				"celery", "kivy", "tensorflow", "zulip",						
		};
		for (String name : softwares) {
			//commonAnalysisRunner(name);
			
			simAnalysisRunner(name);
		}
		
	}
	
}
