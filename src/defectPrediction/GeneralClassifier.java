package defectPrediction;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.LibSVM;
import weka.core.Instances;

public class GeneralClassifier {
	public static final String CLASSIFIER_RANDOM_FOREST = "randForest";
	public static final String CLASSIFIER_NAIVE_BAYES = "naiveBayes";
	public static final String CLASSIFIER_LOGISTIC = "logistic";
	public static final String CLASSIFIER_SVM = "svm";
	
	private Classifier classifier;
	private Instances trainData;
	
	public GeneralClassifier(Instances data, String model, String[] params) {
		try {
			trainData = data;
			switch (model) {
				case CLASSIFIER_RANDOM_FOREST: {
					RandomForest rf  = new RandomForest();	
					classifier = rf;
					break;
				}
				case CLASSIFIER_NAIVE_BAYES: {
					NaiveBayes nb = new NaiveBayes();
					classifier = nb;
					break;
				}
				case CLASSIFIER_LOGISTIC: {
					Logistic log = new Logistic();					
					classifier = log;
					break;
				}
				case CLASSIFIER_SVM: {
					LibSVM svm = new LibSVM();
					classifier = svm;
					break;
				}
				default:
					break;
			}
			if (params != null) {
				classifier.setOptions(params);
			}
			classifier.buildClassifier(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public Evaluation evalutate(Instances testData) {
		Evaluation eval = null;
		try {
			//System.out.println("********" + trainData.numAttributes());
			eval = new Evaluation(trainData);
			eval.evaluateModel(classifier, testData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return eval;
	}
	
	public static String toDetailResult(Evaluation eval, String title) {
		String res = null;
		try {
			String detail = eval.toClassDetailsString(title);
			String matrix =  eval.toMatrixString();
			res = detail + matrix;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}
	
}
