package gitDataProcess;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SVNLogParser {
	
	
	public static LinkedList<String> readRevisions(String filePath) {
		LinkedList<String> res = new LinkedList<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("Revision")) {
					res.add(line.split(": ")[1]);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	public static int[][] compare(List<String> files) {
		int len = files.size();
		int[][] compare = new int[len][len];
		LinkedList<LinkedList<String>> filesArray = new LinkedList<>();
		LinkedList<HashSet<String>> filesSets = new LinkedList<>();
		for (String filepath : files) {
			LinkedList<String> revisions = readRevisions(filepath);
			filesArray.add(revisions);
			HashSet<String> revisionSet = new HashSet<>(revisions);
			filesSets.add(revisionSet);
			System.out.println(revisionSet.size() + "\t/" + revisions.size());
		}
		
		for (int i  = 0; i < len - 1; i++) {
			HashSet<String> set1 = filesSets.get(i);
			for (String revision : set1) {
				for (int j = i+1; j < len; j++) {
					HashSet<String> set2 = filesSets.get(j);
					if (set2.contains(revision)) {
						compare[i][j] += 1;
						compare[j][i] = compare[i][j];
					}
				}
			}
		}
		return compare;
	}
	
	public static void print(int[][] res, String[] tags) {
		int len = res.length;
		for (int i = 0; i < len; i++) {
			System.out.print("\t" + tags[i]);
		}
		System.out.println();
		for (int i = 0; i < len; i++) {
			System.out.print(tags[i]);
			for (int j = 0; j < len; j++) {
				System.out.print("\t" + res[i][j]);
			}
			System.out.println();
		}
	}
	
	public static void main(String[] args) {
		
		String[] softwares = {
				"camel", "hadoop", "synapse", "tc", "xerces"
		};
		String[][] tags = {
				{"2.5.0", "2.6.0", "2.7.0", "2.8.0", "2.9.0"},
				{"2.1.0", "2.2.0", "2.3.0", "2.4.0", "2.5.0"},
				{"1.0", "1.1", "2.0.0", "2.1.0", "3.0.0"},
				{"6.0", "7.0", "8.0", "8.5", "9.0"},
				{"2.5", "2.6", "2.7", "2.8", "2.9"}
		};
		List<List<String>> fileListList = new ArrayList<List<String>>();
		String prefix = "E:/data/svn/";
		for (int i = 0; i < softwares.length; i++) {
			String name = softwares[i];
			List<String> tagList = Arrays.asList(tags[i]);
			List<String> filepaths = tagList.stream()
											.map(tag -> prefix + name + "/" + name + "_" + tag + ".txt")
											.collect(Collectors.toList());
			fileListList.add(filepaths);
		}
		for (int i = 0; i < softwares.length; i++) {
			List<String> files = fileListList.get(i);
			int[][] res = compare(files);
			System.out.println("==========================");
			print(res, tags[i]);
			
		}
		
		
		
		
//		String prefix = "E:/data/svn/camel/camel_";
//		List<String> tagList = Arrays.asList(tags);
//		List<String> filepaths = tagList.stream()
//				.map(tag -> prefix + tag + ".txt")
//				.collect(Collectors.toList());
//		int[][] res = compare(filepaths);
//		print(res, tags);
		
		
	}
}
