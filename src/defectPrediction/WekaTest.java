package defectPrediction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;

public class WekaTest {
	
	public static void attrSelectionTest() {
		String trainFilePath = "./train/geode_metrics_1.1.0.arff";
		try {
			BufferedReader reader1 = new BufferedReader(new FileReader(trainFilePath));
			Instances trainSet = new Instances(reader1);
 			reader1.close();
 			trainSet.setClassIndex(trainSet.numAttributes() - 1);
 			
 			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		String trainFilePath = "./train/geode_metrics_1.1.0.arff";
		String testFilePath = "./train/geode_metrics_1.2.0.arff";
		String[] options = {
				"-I", "20",
				"-K", "7"
		};
//		ArffLoader loader = new ArffLoader();
		try {
//			loader.setFile(new File(trainFilePath));
//			Instances structure = loader.getStructure();
//			structure.setClassIndex(structure.numAttributes() -  1);
//			RandomForest rf = new RandomForest();
//			rf.buildClassifier(structure);
			
			BufferedReader reader1 = new BufferedReader(new FileReader(trainFilePath));
			BufferedReader reader2 = new BufferedReader(new FileReader(testFilePath));
			Instances trainSet = new Instances(reader1);
 			Instances testSet = new Instances(reader2);
 			
 			reader1.close();
 			reader2.close();
 			trainSet.setClassIndex(trainSet.numAttributes() - 1);
 			testSet.setClassIndex(testSet.numAttributes() - 1);
 			RandomForest rf = new RandomForest();
 			rf.setOptions(options);
 			rf.buildClassifier(trainSet);
 			
 			
 			Evaluation eval = new Evaluation(trainSet);
 			eval.evaluateModel(rf, testSet);
 			double f = eval.fMeasure(0);
 			System.out.println(eval.toSummaryString("====results====\n", false));
 			System.out.println("fmeasure: " + f);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
}
