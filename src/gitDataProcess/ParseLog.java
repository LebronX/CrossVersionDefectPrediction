package gitDataProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

public class ParseLog {
	
	private String logFilePath;
	private boolean isFileOpened;
	private boolean isFileClosed;
	private BufferedReader reader;
	public static int readLinesPerTime = 1000;
	private LinkedList<Commit> fixCommits;	
	private LinkedList<String> buffer;
	
	public static final int FILTER_BUG_AND_FIX = 0;
	public static final int FILTER_BUG_OR_FIX = 1;
	

	
	public ParseLog(String filePath) {
		logFilePath = filePath;
		isFileOpened = false;
		isFileClosed = false;
		buffer = new LinkedList<String>();
		fixCommits = new LinkedList<Commit>();
	}	
	
	public boolean readFileLines() throws IOException {
		if (isFileClosed) {
			return false;
		}	
		if (!isFileOpened) {
			File file = new File(logFilePath);
			if (!file.exists() || file.isDirectory()) {
				throw new  FileNotFoundException();
			}
			reader = new BufferedReader(new FileReader(file));
			isFileOpened = true;
			isFileClosed = false;
		}
		Object[] lines  =  reader.lines().limit(readLinesPerTime).toArray();
		if (lines.length == 0) {
			reader.close();	
			isFileClosed = true;
			return false;
		}else {
			for (Object line : lines) {
				buffer.add((String)line);
			}
			return true;
		}		
	}
	
	public Commit nextCommit() {
		boolean started = false;
		LinkedList<String> commit = new LinkedList<>();
		while (true) {
			//read file when buffer is empty
			if (buffer.isEmpty()) {
				boolean hasLine = false;
				try {
					hasLine = readFileLines();
				} catch (IOException e) {
					e.printStackTrace();
				}
				//return when the whole file has been read
				if (!hasLine) {
					break;
				}
			}
			//read lines of one commit from buffer
			String line = buffer.removeFirst().trim();
			if (line.equals("") || line.matches("//s*")) {
				continue;
			}else if (line.trim().equals("---")) {
				if (started == false) {
					started = true;
					continue;
				}else {
					buffer.addFirst(line);
					break;
				}
			}else {
				commit.add(line);
			}
		}
		if (commit.size() == 0) {
			return null;
		}else {
			return new Commit(commit);
		}
	}
	
	
	public void printBuffer() {
		for (String line : buffer) {
			System.out.println(line);
		}
	}

	
	public static boolean filter(Commit commit, int strategy) {
		String message = commit.getMessage();
		message = message.toLowerCase();
		boolean bug = message.contains("bug");
		boolean fix = message.contains("fix");
//		boolean error = message.contains("error");
//		boolean issue = message.contains("issue");
//		boolean defect = message.contains("defect");
//		boolean problem = message.contains("problem");
//		boolean fail = message.contains("fail");
//		boolean patch = message.contains("patch");
		if (strategy == ParseLog.FILTER_BUG_AND_FIX) {
			return bug && fix;
		}else {
			return bug || fix;
			//return bug || fix || error || issue || defect;
			//return bug || fix || error || issue || defect || fail || problem || patch;
		}
	}
	
	public static void writeFile(LinkedList<Commit> commits, String filePath) {
		LinkedList<String> content = new LinkedList<>();
		for (Commit commit : commits) {
			LinkedList<String> commitStrs = commit.toStringList();
			content.add("---");
			content.addAll(commitStrs);
		}
		Util.writeFile(filePath, content);
	}
	
	public LinkedList<Commit> parse(int strategy, String outPath) {
		Commit commit;
		while ((commit = nextCommit()) != null) {
			if (filter(commit, strategy)) {
				fixCommits.add(commit);
			}
		}
		writeFile(fixCommits, outPath);
		return fixCommits;
	}
	
	public static LinkedList<Commit> readFormatedCommits(String path) {
		LinkedList<Commit> res = new LinkedList<>();
		File file = new File(path);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			Commit  commit= null;
			while (line != null) {
				line.trim();
				if (line.equals("---")) {
					if (commit != null) {
						res.add(commit);
					}
					commit = new Commit();
					String[] lines = new String[4];
					for (int i = 0; i < 4; i++) {
						lines[i] = reader.readLine();
					}
					commit.setId(lines[0]);
					commit.setAuthor(lines[1]);
					commit.setDateString(lines[2]);
					commit.setMessage(lines[3]);
				}else {
					String[] fileChange = line.split(" ");
					commit.addFile(fileChange[0], fileChange[1]);
				}
				line = reader.readLine();
			}
			reader.close();
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
	 
	
	public static void main(String[] args) {
		String filePath = "./xalan-log.txt";
		String outPath = "./xalan-out2.txt";
		ParseLog parser = new ParseLog(filePath);
		parser.parse(ParseLog.FILTER_BUG_OR_FIX, outPath);
		System.out.println("finished");
	}
}
