package defectPrediction;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import gitDataProcess.Util;
import weka.classifiers.Evaluation;
import weka.core.Instances;


public class ClassifiersRunner {
	public static String dirPath = "E:/data/metric_arff1/";
	public static String[] softwares = {
			"xalan", 
			"jmeter", 
			"camel", 
			"celery", 
			"kivy", 
			"tensorflow", 
//			"zulip",						
//			"geode", "beam","cloudstack", "isis", 			
//			"okhttp", "mahout"
	};
	
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
			if (fileName.endsWith(".arff")) {
				files.add(fileName);
			}
		}
		return files;
	}
	
	public static List<String[]> produceParams(String model) {
		List<String[]> optionsList = new ArrayList<>();
		if (GeneralClassifier.CLASSIFIER_RANDOM_FOREST.equals(model)) {
			int minTreeNum = 5;
			int maxTreeNum = 20;
			int minFeatureNum = 4;
			int maxFeatureNum = 10;
			for (int treeNum = minTreeNum; treeNum < maxTreeNum; treeNum++) {
				for (int featureNum  = minFeatureNum; featureNum < maxFeatureNum; featureNum++) {
					String[] options = {"-I", treeNum + "", "-K", featureNum + ""};
					optionsList.add(options);
				}
			}
		}		
		return optionsList;
	}
	
	public static void runPredictionWithParams(String name, String model, String featureSelection, String rebalance) {
		List<String[]> optionsList =  produceParams(model);
		
		for (String[] optsions : optionsList) {
			runAllCrossVersionPrediction(name, model, featureSelection, rebalance, optsions);
		}
	}
	
	public static void runAllCrossVersionPrediction(String name, String model, String featureSelection, String rebalance, String[] options) {
		List<String> files = getVerFileList(name);
		Util.sortFileName(files);	
		int verNum = files.size();		
		Evaluation[][] evals = new Evaluation[verNum+5][verNum-1];
		Instances[] allVersData = new Instances[files.size()];
		Instances[] allVerDataWithName = new Instances[files.size()];
		for (int sourIndex = 0; sourIndex < verNum - 1; sourIndex++) {
			//read data
			Instances trainData = null;
			if (allVersData[sourIndex] != null) {
				trainData = allVersData[sourIndex];
			}else {
				String testFilePath = dirPath + name + "/" + files.get(sourIndex);			
				trainData = DataBuilder.readData(testFilePath);
				allVerDataWithName[sourIndex] = trainData;
				trainData = FeatureSelection.delFilename(trainData);
				allVersData[sourIndex] = trainData;
			}			
			//select features
			int[] features = FeatureSelection.featureSelection(trainData, featureSelection);
			trainData = FeatureSelection.filterData(trainData, features);				
			//build a classifier
			GeneralClassifier classifier = new GeneralClassifier(trainData, model, options);		
			//evaluate on several version test data
			for (int targIndex = sourIndex + 1; targIndex < verNum; targIndex++) {
				Instances testData = null;
				if (allVersData[targIndex] != null) {
					testData = allVersData[targIndex];
				}else {
					String testFilePath = dirPath + name + "/" + files.get(targIndex);
					testData = DataBuilder.readData(testFilePath);
					allVerDataWithName[targIndex] = testData;
					testData = FeatureSelection.delFilename(testData);
					allVersData[targIndex] = testData;
				}
				//filter data
				Instances filteredTestData = FeatureSelection.filterData(testData, features);
				Evaluation eval = classifier.evalutate(filteredTestData);
				evals[sourIndex][targIndex-1] = eval;
			}
		}
		Instances merged = new Instances(allVersData[0]);
		int[] indexes = null;
		for (int index = 1; index < allVersData.length-1; index++) {
			//merged data as train dataset
			merged = DataBuilder.mergeData(merged, allVersData[index]);
			int[] features = FeatureSelection.featureSelection(merged, featureSelection);
			Instances trainData = FeatureSelection.filterData(merged, features); 
			GeneralClassifier classifier = new GeneralClassifier(trainData, model, options);
			Instances testData = allVersData[index+1];
			testData = FeatureSelection.filterData(testData, features);
			Evaluation eval = classifier.evalutate(testData);
			evals[evals.length-4][index] = eval;
			
			//filtered data as train dataset
			indexes = DataBuilder.mergeData(allVerDataWithName[0], allVerDataWithName[index], indexes);
			
			//used for majority rule
			Instances filtered = DataBuilder.filterConflictInstance(allVerDataWithName[0], indexes);
			
			//used for 28rule
			Instances filtered_28 = DataBuilder.filterConflictInstance_28(allVerDataWithName[0], indexes);
			
			//used for 37rule
			Instances filtered_37 = DataBuilder.filterConflictInstance_37(allVerDataWithName[0], indexes);
			
			//used for 46rule
			Instances filtered_46 = DataBuilder.filterConflictInstance_46(allVerDataWithName[0], indexes);
			
			//used for 19rule
			Instances filtered_19 = DataBuilder.filterConflictInstance_19(allVerDataWithName[0], indexes);
			
			filtered = new Instances(filtered);
			filtered = FeatureSelection.delFilename(filtered);
			int[] features2 = FeatureSelection.featureSelection(filtered, featureSelection);
			Instances trainData2 = FeatureSelection.filterData(filtered, features2);
			GeneralClassifier classifier2 = new GeneralClassifier(trainData2, model, null);
			testData = allVersData[index+1];
			testData = FeatureSelection.filterData(testData, features2);
			Evaluation eval2 = classifier2.evalutate(testData);
			evals[evals.length-3][index] = eval2;
			
			filtered_28 = new Instances(filtered_28);
			filtered_28 = FeatureSelection.delFilename(filtered_28);
			int[] features3 = FeatureSelection.featureSelection(filtered_28, featureSelection);
			Instances trainData3 = FeatureSelection.filterData(filtered_28, features3);
			GeneralClassifier classifier3 = new GeneralClassifier(trainData3, model, null);
			testData = allVersData[index+1];
			testData = FeatureSelection.filterData(testData, features3);
			Evaluation eval3 = classifier3.evalutate(testData);
			evals[evals.length-2][index] = eval3;
			
			filtered_37 = new Instances(filtered_37);
			filtered_37 = FeatureSelection.delFilename(filtered_37);
			int[] features4 = FeatureSelection.featureSelection(filtered_37, featureSelection);
			Instances trainData4 = FeatureSelection.filterData(filtered_37, features4);
			GeneralClassifier classifier4 = new GeneralClassifier(trainData4, model, null);
			testData = allVersData[index+1];
			testData = FeatureSelection.filterData(testData, features4);
			Evaluation eval4 = classifier4.evalutate(testData);
			evals[evals.length-1][index] = eval4;
					
			filtered_46 = new Instances(filtered_46);
			filtered_46 = FeatureSelection.delFilename(filtered_46);
			int[] features5 = FeatureSelection.featureSelection(filtered_46, featureSelection);
			Instances trainData5 = FeatureSelection.filterData(filtered_46, features5);
			GeneralClassifier classifier5 = new GeneralClassifier(trainData5, model, null);
			testData = allVersData[index+1];
			testData = FeatureSelection.filterData(testData, features5);
			Evaluation eval5 = classifier5.evalutate(testData);
			evals[evals.length-1][index] = eval5;
			
			filtered_19 = new Instances(filtered_19);
			filtered_19 = FeatureSelection.delFilename(filtered_19);
			int[] features6 = FeatureSelection.featureSelection(filtered_19, featureSelection);
			Instances trainData6 = FeatureSelection.filterData(filtered_19, features6);
			GeneralClassifier classifier6 = new GeneralClassifier(trainData6, model, null);
			testData = allVersData[index+1];
			testData = FeatureSelection.filterData(testData, features6);
			Evaluation eval6 = classifier6.evalutate(testData);
			evals[evals.length-1][index] = eval6;
		}
		formatResult(evals, name, model, featureSelection, rebalance, files);
	}
	
	public static void formatResultsWithParams(Evaluation[][] evals, String name, String model, 
			String featureSelection, String rebalance, List<String> files, String[] options) {
		
	}
	
	
	private static void formatResult(Evaluation[][] evals, String name, String model, 
			String featureSelection, String rebalance, List<String> files) {
		int rowNum = evals.length;
		int colNum = evals[0].length;
		Double[][] bugPrec = new Double[rowNum][colNum];
		Double[][] bugRecall = new Double[rowNum][colNum];
		Double[][] bugF = new Double[rowNum][colNum];
		Double[][] gMean = new Double[rowNum][colNum];
		Double[][] balance = new Double[rowNum][colNum];
		List<String> allResults = new ArrayList<>();
		List<String> tags = files.stream()
				.map(filename -> filename.substring(0, filename.length() - 5).split("_")[2])
				.collect(Collectors.toList());
		tags.add("allData");
		tags.add("filtered_majority");
		tags.add("filtered_28");
		tags.add("filtered_37");
		tags.add("filtered_46");
		tags.add("filtered_19");
		for (int sourIndex = 0; sourIndex < rowNum; sourIndex++) {
			for (int targIndex = 0; targIndex < colNum; targIndex++) {
				if (sourIndex <= targIndex || (sourIndex >= rowNum-6 && targIndex >= 1)) {
					Evaluation eval = evals[sourIndex][targIndex];
					if (eval == null) {
						continue;
					}
					String resultTitle = null;
					if (sourIndex == rowNum-6) {
						resultTitle = "=== " + name + "  allData  -->" + tags.get(targIndex) + " ===";
					} else if (sourIndex == rowNum-5) {
						resultTitle = "=== " + name + "  filteredData  -->" + tags.get(targIndex) + " ===";
					} else if (sourIndex == rowNum-4) {
						resultTitle = "=== " + name + "  filteredData_28  -->" + tags.get(targIndex) + " ===";
					} else if (sourIndex == rowNum-3) {
						resultTitle = "=== " + name + "  filteredData_37  -->" + tags.get(targIndex) + " ===";
					} else if (sourIndex == rowNum-2) {
						resultTitle = "=== " + name + "  filteredData_46  -->" + tags.get(targIndex) + " ===";
					} else if (sourIndex == rowNum-1) {
						resultTitle = "=== " + name + "  filteredData_19  -->" + tags.get(targIndex) + " ===";
					} else {
						resultTitle = "=== " + name + "  " + tags.get(sourIndex) + "-->" + tags.get(targIndex) + " ===";
					} 
					String resultStr = GeneralClassifier.toDetailResult(eval, resultTitle);
					//result process
					allResults.add(resultStr);
					bugPrec[sourIndex][targIndex] = eval.precision(0);
					bugRecall[sourIndex][targIndex] = eval.recall(0);
					bugF[sourIndex][targIndex] = eval.fMeasure(0);
					double tp = eval.numTruePositives(0);
					double tn = eval.numTrueNegatives(0);
					double fp = eval.numFalsePositives(0);
					double fn = eval.numFalseNegatives(0);
					gMean[sourIndex][targIndex] = ResultEvaluator.gMean(tp, tn, fp, fn);
					balance[sourIndex][targIndex] = ResultEvaluator.balance(tp, tn, fp, fn);
				}
			}
		}
		writeResults(name, model, featureSelection, tags, 
				bugPrec, bugRecall, bugF, gMean, balance, allResults);
	}
	


	private static void writeResults(String name, String model, String featureSelection, List<String> tags,
			Double[][] bugPrec, Double[][] bugRecall, Double[][] bugF, Double[][] gMean, Double[][] balance,
			List<String> allResults) {
		String detailResultPath = getFilePath(name, "detail", model, featureSelection, null, false);
		String summaryResultPath = getFilePath(name, "summary", model, featureSelection, null, false);
		Util.writeFile(detailResultPath, allResults);
		List<String> tables = new ArrayList<>();		
		tables.add(markdownTableFormatDouble(bugPrec, tags, "Precision", null));
		tables.add(markdownTableFormatDouble(bugRecall, tags, "Recall", null));
		tables.add(markdownTableFormatDouble(bugF, tags, "Fmeasure", null));
		tables.add(markdownTableFormatDouble(gMean, tags, "gMean", null));
		tables.add(markdownTableFormatDouble(balance, tags, "balance", null));
		Util.writeFile(summaryResultPath, tables);
	}

	public static String getFilePath(String name, String type, String model, String fSelection, String rebalance, boolean withParams) {
		String fs = fSelection == null ? "" : fSelection;
		String rb = rebalance == null ? "" : rebalance;
		String modelStr = "-" + model;
		//feature string 
		switch (fs) {
			case FeatureSelection.CFS_ATTRIBUTE_SELECTION:{
				fs = "-cfs";
				break;
			}case FeatureSelection.FILTER_ATTRIBUTE_SELECTION: {
				fs = "-filter";
				break;
			}default: {
				fs = "";
				break;
			}
		}
		//rebalance string
		switch (rb) {
			case Rebalance.REBALANCE_SMOTE: {
				rb = "-smote";
				break;
			}default: {
				rb = "";
				break;
			}	
		}
		String dir = dirPath + name + "/";
		String param = "";
		if (withParams) {
			dir += "paprams-out/";
			param = "";
		}
		String res = dir + name +  "-" + type + modelStr + fs + rb + param + ".txt";
		return res; 
	}
	
	
	public static String markdownTableFormatDouble(Double[][] data, List<String> tags, String title, DecimalFormat df) {
		int rowNum = data.length;
		int colNum = data[0].length;
		if (df == null) {
			df = new DecimalFormat("0.000");
		}
		String[][] dataStr = new String[rowNum][colNum];
		for (int i = 0; i < rowNum; i++) {
			for (int j = 0; j < colNum; j++) {
				dataStr[i][j] = data[i][j] == null ? "" : df.format(data[i][j]);
			}
		}
		return markdownTableFormatStr(dataStr, tags, title);
	}
	
	public static String markdownTableFormatStr(String[][] data, List<String> tags, String title) {
		String res = "";
		int rowNum = data.length;
		int colNum = data[0].length;
		res += "|" + title + "\t|";
		for (int i = 1; i <= colNum; i++) {
			res += tags.get(i) + "\t|";
		}
		res += "\n";
		for (int i = 0; i <= colNum; i++) {
			res += "|:---\t"; 
		}
		res += "|\n";
		for (int sourIndex = 0; sourIndex < rowNum; sourIndex++) {
			int tagIndex = sourIndex >= colNum ? sourIndex + 1 : sourIndex;
			String line = "|" + tags.get(tagIndex)  + "\t|";
			for (int targIndex = 0; targIndex < colNum; targIndex++) {
				String element = data[sourIndex][targIndex] +  "\t|";
				line += element;
			}
			res += line + "\n";
		}
		return res;
	}
	
	
		
	public static void main(String[] args) {
		String[] models = {
			GeneralClassifier.CLASSIFIER_RANDOM_FOREST,
			GeneralClassifier.CLASSIFIER_NAIVE_BAYES,
			GeneralClassifier.CLASSIFIER_LOGISTIC,
//			GeneralClassifier.CLASSIFIER_SVM
		};
		String[] attrSele = {
			null,
			FeatureSelection.CFS_ATTRIBUTE_SELECTION,
//			FeatureSelection.CLASSIFIER_ATTRIBUTE_SELECTION,
			FeatureSelection.FILTER_ATTRIBUTE_SELECTION
		};
		for (String name : softwares) {
			System.out.println("===========processing " + name + "===========");
			for (String model : models) {
				System.out.println("Using " + model + "...");
				for (String selec : attrSele) {
					runAllCrossVersionPrediction(name, model, selec, null, null);
				}
			}
			System.out.println(name + " finished");
		}
		System.out.println("all softwares are finished!");
		
//		mergeTest("geode");
	}
	
	
}
