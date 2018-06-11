package gitDataProcess;

import java.util.Calendar;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class Commit {
	private String id;
	private Calendar date;
	private String message;
	private String author;
	private String dateString;
	private Map<String, String> files;
	 
	
	
	public void setId(String id) {
		this.id = id;
	}


	public void setMessage(String message) {
		this.message = message;
	}


	public void setAuthor(String author) {
		this.author = author;
	}


	public void setDateString(String dateString) {
		this.dateString = dateString;
		setDate();
	}

	public void setDate(){
		
		String[] dateStrs = dateString.split(" ")[0].split("-");
		date = Calendar.getInstance();
//		try {
			date.set(Integer.parseInt(dateStrs[0]), Integer.parseInt(dateStrs[1]), Integer.parseInt(dateStrs[2]));
//		}catch (Exception e) {
//			System.out.println(dateString);
//		}
		
	}

	public void setFiles(Map<String, String> files) {
		this.files = files;
	}


	public String getId() {
		return id;
	}


	public Calendar getDate() {
		return date;
	}


	public String getMessage() {
		return message;
	}


	public String getAuthor() {
		return author;
	}


	public String getDateString() {
		return dateString;
	}


	public Map<String, String> getFiles() {
		return files;
	}
	
	public void addFile(String file, String act) {
		files.put(file, act);
	}
	
	public Commit() {
		files = new TreeMap<String, String>();
	}

	public Commit(List<String>  lines) {
		files = new TreeMap<String, String>();
		//commit format: 
		/**
		 * a522561055da4636aadc84c952e66f61fe76b5ae
		 * wenshao
		 * 2018-03-21 23:53:36 +0800
         * 1.2.48
		 * :100755 100755 948296d4 32f57644 M	src/main/java/com/alibaba/fastjson/JSON.java
		 * :...
		 */
		setId(lines.get(0));
		setAuthor(lines.get(1));
		setDateString(lines.get(2));
		setMessage(lines.get(3));
		for (int i = 4; i < lines.size(); i++) {
			//System.out.println("*" + lines.get(i) + "*");
			String[] temp = lines.get(i).split("\\s");
//			System.out.println("---------");
//			for (String str : temp) {
//				System.out.print("*" + str + "*");
//			}
			files.put(temp[5], temp[4]);
//			CommitSpliter.pause();
		}
	}
	
	public LinkedList<String> toStringList() {
		LinkedList<String> res = new LinkedList<>();
		res.add(id);
		res.add(author);
		res.add(dateString);
		res.add(message);
		for (String file : files.keySet()) {
			res.add(file + " " + files.get(file));
		}
		return res;
	}
	
	
	public void printCommit() {
		System.out.println("id: " + id);
		System.out.println("author: " + author);
		System.out.println("data: " + dateString);
		System.out.println("message: " + message);
		System.out.println("files:");
		for (String file : files.keySet()) {
			System.out.println(file + " " + files.get(file));
		}
		System.out.println();
	}
	
	public static void main(String[] args) {
//		String commit = ":100755 100755 948296d4 32f57644 M	src/main/java/com/alibaba/fastjson/JSON.java";
//		for (String temp : commit.split("\\s")) {
//			System.out.println("*" + temp + "*");
//		}
		
		String test = "";
		System.out.println(test.matches("\\s*"));
	}
}
