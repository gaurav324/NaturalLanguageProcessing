import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class RawToMallet {
	
	// This is where we parse a line from pos file 
	// and return the tags found in the line.
	public static String parseLine(String line) {
		// Get line separator.
		String eol = System.getProperty("line.separator");  
		
		StringBuilder output = new StringBuilder();
		
		// First trim to remove trailing characters.
		line = line.trim();
		
		//System.out.println("Parsing line: " + line);
		// Check if it starts with "[" 
		if (line.startsWith("[")) {
			// Remove "[ " and " ]"
			line = line.replace("[", " ").replace("]", " ").trim();

			// If line looks something like.
			if (line.startsWith(("@"))) {
				output.append(eol);
			} else {
				String[] taggedWords = line.split("\\s+");
				for (String tWord : taggedWords) {
					output.append(tWord.substring(0, tWord.indexOf("/")));
					output.append(" ");
;;					output.append(tWord.substring(tWord.indexOf("/") + 1));
					output.append(eol);
				}
			}
		} else if (line.startsWith("==") || line.length() == 0) {
			output.append(eol);
		} else {
			String[] taggedWords = line.split("\\s+");
			for (String tWord : taggedWords) {
				output.append(tWord.substring(0, tWord.indexOf("/")));
				output.append(tWord.substring(tWord.indexOf("/")));
				output.append(eol);
			}
		}
		return output.toString();
	}
	
	// This function would parse a file and write tagged
	// data using printWriter handle.
	public static void parseFileAndWrite(String filename, PrintWriter pw) {
		System.out.println("Parsing file: " + filename);
		BufferedReader br = null;
		try {
			String currentLine;
			FileReader fileReader = new FileReader(filename);
			br = new BufferedReader(fileReader);
			
			// Read in each file line by line and keep writing
			// output using PrintWriter.
			while ((currentLine = br.readLine()) != null) {
				String result = parseLine(currentLine);
				pw.print(result);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public static void parseDirectoryAndWrite(File filename, PrintWriter pw) {
		for (File file : filename.listFiles()) {
			if (file.isDirectory()) {
				parseDirectoryAndWrite(file, pw);
			} else {
				if (file.getName().endsWith(".pos")) {
					parseFileAndWrite(file.getAbsolutePath(), pw);
				}
			}
		}
	}
	
	// args[0] can be a file or folder. For folders, we would parse each *.pos file.
	// arg[1] would be the name of the output folder.		
	public static void main(String[] args) {		
		// Create printWriter object.
		PrintWriter pw = null;
		String outputFile = "output";
		if (args.length == 2) {
			outputFile = args[1];
		}
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		File f = new File(args[0]);
		// args[0] would a filename for ATIS and folder name for WSJ.
		if (f.isFile()) {
			parseFileAndWrite(f.getAbsolutePath(), pw);
		} else {
			parseDirectoryAndWrite(f, pw);
		}
		
	}
}
