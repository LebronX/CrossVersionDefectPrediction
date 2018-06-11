package gitDataProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class FileScanner {
	private static Map<String, Integer> fileCountMap;
	private static final String filePath = "./filecount.txt";
	private static int dirCount = 0;
	
	
	public static void readFileRecord() {
		fileCountMap = new HashMap<>();
		File file = new File(filePath);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			Object[] lines = reader.lines().toArray();
			reader.close();
			for (Object strObj : lines) {
				String line = ((String)strObj).trim();
				if ("".equals(line)) {
					continue;
				}
				String[] temp = line.split(" ");
				fileCountMap.put(temp[0], Integer.parseInt(temp[1]));
			}
			dirCount = fileCountMap.size();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static int getDirFileNum(String dir) {
		int res = 0;
		if (fileCountMap == null) {
			readFileRecord();
		}
		if (fileCountMap.containsKey(dir)) {
			res = fileCountMap.get(dir);
		}else {
			res = scanFiles(dir).size();
			if (res != 0) {
				fileCountMap.put(dir, res);
			}

		}
		return res;
	}
	
	public static void writeRecords() {
		if (fileCountMap.size() == dirCount) {
			return;
		}
		LinkedList<String> content = new LinkedList<>();
		for (String key : fileCountMap.keySet()) {
			String count = fileCountMap.get(key) + "";
			content.add(key + " " + count);
		}
		Util.writeFile(filePath, content);
	}
	
	
	public static Set<String> scanFiles(String dir) {
		File file =  new File(dir);
		Set<String> res = new HashSet<>();
		if (!file.exists()) {
			return res;
		}else {
			return res = scan(file, dir);
		}
	}
	
	public static Set<String> scan(File file, String prefix) {
		HashSet<String> res = new HashSet<String>();
		if (!file.isDirectory()) {
			String fileName = file.getAbsolutePath();
			if (fileName.endsWith(".java") || fileName.endsWith(".py")) {
				fileName = fileName.replace("\\", "/");
				fileName = fileName.substring(prefix.length());
				res.add(fileName);
				//System.out.println(fileName);
			}
		}else {
			for (File subFile : file.listFiles()) {
				res.addAll(scan(subFile, prefix));
			}
		}
		return res;
	}
	
	
	public static void main(String[] args) {
//		String dir = "E:/data/git/xalan/xalan-j/src";
//		int count = FileScanner.scanJavaFiles(dir);
//		System.out.println(count);
	}
}
