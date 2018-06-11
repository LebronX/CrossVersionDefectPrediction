package defectPrediction;

import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.ClassifierSubsetEval;
import weka.attributeSelection.FilteredSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.core.Instances;

public class FeatureSelection {
	
	public static final String CFS_ATTRIBUTE_SELECTION = "CFS";
	public static final String CLASSIFIER_ATTRIBUTE_SELECTION = "CLASSIFIER";
	public static final String FILTER_ATTRIBUTE_SELECTION = "FILTER";
	
	public static Instances delFilename(Instances data) {
		Instances res = new Instances(data);
		if (res.attribute(0).isNominal() || res.attribute(0).isString()) {
			res.deleteAttributeAt(0);
		}
		return res;
	}
	
	
	public static int[] featureSelection(Instances data, String selectStrategy) {
		int[] attrs = null;
		selectStrategy = selectStrategy == null ? "" : selectStrategy;
		switch (selectStrategy) {
			case CFS_ATTRIBUTE_SELECTION:{
				attrs = cfsFeatureSelection(data);
				break;
			}case CLASSIFIER_ATTRIBUTE_SELECTION: {
				attrs = classifierSubsetEval(data);
				break;
			}case FILTER_ATTRIBUTE_SELECTION: {
				attrs = filterSubsetEval(data);
				break;
			}default:
				break;
		}
		return attrs;
	}
	
	public static Instances filterData(Instances data, int[] attrs) {
		if (attrs == null || attrs.length == data.numAttributes()) {
			return data;
		}
		Instances res = new Instances(data);
		int[] toDelete = new int[data.numAttributes() - attrs.length];
		int toDeleteIndex = 0;
		int attrIndex = attrs.length - 1;
		int compareAttr = attrs[attrIndex];
		for (int i = data.numAttributes() - 1; i >= 0; i--) {
			if (i > compareAttr) {
				toDelete[toDeleteIndex] = i;
				toDeleteIndex++;
			}else if (i == compareAttr) {
				attrIndex--;
				compareAttr = attrIndex >= 0 ? attrs[attrIndex] : -1; 
			}
		}
		for (int i = 0; i < toDelete.length; i++) {
			res.deleteAttributeAt(toDelete[i]);
		}
		return res;
	}
	
	public static int[] classifierSubsetEval(Instances data) {
		int[] attrs = null;
		AttributeSelection as = new AttributeSelection();
		ClassifierSubsetEval cse = new ClassifierSubsetEval();
		GreedyStepwise search = new GreedyStepwise();
		search.setSearchBackwards(true);
		as.setEvaluator(cse);
		as.setSearch(search);
		try {
			as.SelectAttributes(data);
			attrs = as.selectedAttributes();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return attrs;
	}
	
	
	public static int[] filterSubsetEval(Instances data) {
		int[] attrs = null;
		AttributeSelection as = new AttributeSelection();
		FilteredSubsetEval fse = new FilteredSubsetEval();
		GreedyStepwise search = new GreedyStepwise();
		search.setSearchBackwards(true);
		as.setEvaluator(fse);
		as.setSearch(search);
		try {
			as.SelectAttributes(data);
			attrs = as.selectedAttributes();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return attrs;
	}
	
	
	public static int[] cfsFeatureSelection(Instances data) {
		int[] attrs = null;
		AttributeSelection as = new AttributeSelection();
		CfsSubsetEval cfs = new CfsSubsetEval();
		GreedyStepwise search = new GreedyStepwise();
		search.setSearchBackwards(true);
		as.setEvaluator(cfs);
		as.setSearch(search);
		try {
			as.SelectAttributes(data);
			attrs = as.selectedAttributes();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return attrs;
	}
	
}
