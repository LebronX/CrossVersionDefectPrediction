package gitDataProcess;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class Util {
	
	public static String verDirPrefix = "E:/data/release/";
	
	public static void writeFile(String filepath, List<String> content) {
		File file = new File(filepath);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			for (String line : content) {
				writer.write(line + "\n");
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getLogFilePath(String name) {
		return name + "-log.txt";
	}
	
	public static String getFormatedFilePath(String name, int fileNum) {
		return "./out/" + name + "/"  + name + "-formated.txt";
	}
	
	public static String getFilteredFilePath(String name, int filStrategy) {
		String suf = filStrategy == ParseLog.FILTER_BUG_AND_FIX ? "andFB" : "orFB";
		String filepath = "./out/" + name + "/"  + name + "-filt-" + suf + ".txt";
		return filepath;
	}
	
	public static String getVersionFilePath(String name) {
		return name + "-versions.txt";
	}
	
	public static String getGroupedFilePath(String name, int filtStrategy, int splitStrategy) {
		String pre = filtStrategy == 0 ? "andFB" : "orFB";
		String post = splitStrategy == 0 ? "aft" : "cls";
		return "./out/" + name + "/"  + name + "-" + pre + "-" + post + ".txt";
	}
	
	public static String getVersBugFilePath(String name, int filtStrategy, int splitStrategy, String tag) {
		String pre = filtStrategy == 0 ? "andFB" : "orFB";
		String post = splitStrategy == 0 ? "aft" : "cls";
		String filePath = "./out/" + name + "/"  + name+ "-" + tag + "-" + pre + "-" + post + ".txt";
		return filePath;
	}

	public static void pause() {
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		scanner.close();
	}
	
	public static void sortFileName(List<String> files) {
		files.sort(new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				s1 = s1.split("_")[2];
				s2 = s2.split("_")[2];
				String[] parts1 = s1.trim().split("\\.");
				String[] parts2 = s2.trim().split("\\.");
				for (int i = 0;  i < parts1.length && i < parts2.length; i++) {
					int compare =Integer.parseInt(parts1[i]) - Integer.parseInt(parts2[i]);
					if (compare != 0) {
						return compare;						
					}
				}
				if (parts1.length != parts2.length) {
					return parts1.length - parts2.length;
				}
				return 0; 
			}			
		});
	}
 	
	public static String getVersReleaseDir(String name, String version) {
		String suffix = "";
		String dir = "";
		if ("xalan".equals(name)) {
			suffix = "xalan-j-" + version;
			dir = verDirPrefix + name + "/" + suffix;
		}else if ("beam".equals(name)) {
			suffix = "beam-" + version;
			dir = verDirPrefix + name + "/" + suffix;
		}else if ("camel".equals(name)) {
			suffix = "camel-" + version;
			dir = verDirPrefix + name + "/" + suffix;
		}else if ("isis".equals(name)) {
			version = "isis-rel-isis-" + version;
			dir = verDirPrefix + name + "/" + version + "/" + version; 
		}else if ("geode".equals(name)) {
			dir = verDirPrefix + name + "/" + name + "-rel-" + version;
		}else if ("cloudstack".equals(name)) {
			version = name + "-" + version;
			dir = verDirPrefix + name + "/" + version + "/" + version;
 		}else if ("mahout".equals(name)) {
			version = name + "-" + name + "-" + version;
			dir = verDirPrefix + name + "/" + version + "/" + version;
		}else if ("spark".equals(name)) {
			dir = verDirPrefix + name + "/" + name + "-" + version;
		}else if ("jmeter".equals(name)) {
			version = name + "-" + version;
			dir = verDirPrefix + name + "/" + version + "/" + version;
		}else if ("okhttp".equals(name)) {
			version = name + "-parent-" + version;
			dir = verDirPrefix + name + "/" + version + "/" + version;
		}else if ("scikit".equals(name)) {
			version = name + "-learn-" + version;
			dir = verDirPrefix + name + "/" + version + "/" + version;
		}else if ("tensorflow".equals(name)) {
			version = name + "-" + version;
			dir = verDirPrefix + name + "/" + version + "/" + version;
		}else if ("celery".equals(name)) {
			version = name + "-" + version;
			dir = verDirPrefix + name + "/" + version + "/" + version;
		}else if ("ipython".equals(name)) {
			version = name + "-" + version;
			dir = verDirPrefix + name + "/" + version + "/" + version;
		}else if ("kivy".equals(name)) {
			version = name + "-" + version;
			dir = verDirPrefix + name + "/" + version + "/" + version;
		}else if ("zulip".equals(name)) {
			version = name + "-" + version;
			dir = verDirPrefix + name + "/" + version + "/" + version;
		}
		return dir + "/";
		
	}
}
