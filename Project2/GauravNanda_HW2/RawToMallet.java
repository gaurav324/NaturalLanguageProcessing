import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.*;

public class RawToMallet {
	
	public static final ArrayList<String> SUFFIXES = new ArrayList<String>(Arrays.asList("s", "ed", "ing", "ly", "er", "or", "ion", "ble"));

	public static final ArrayList<String> PREFIXES =  new ArrayList<String>(Arrays.asList("un", "re", "in", "im", "dis", "en", "non", "over", "mis", "sub"));
	
	public static HashMap<String, Boolean> featureMap = new HashMap<String, Boolean>();
	
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
					String actualWord = tWord.substring(0, tWord.indexOf("/"));
					output.append(actualWord);
					output.append(" ");
					for (String feature : featureMap.keySet()) {
						// If first charcter is CAPS, then add CAPS feature.
						if (feature.equals("CAPS") && Character.isUpperCase(actualWord.charAt(0))) {
							output.append("CAPS");
							output.append(" ");
						}
						
						// IF word contains HYPHEN, add hypen feature.
						if (feature.equals("HYPHEN") && actualWord.contains("-")) {
							output.append("HYPHEN");
							output.append(" ");
						}
						
						// IF word starts with a number.
						if (feature.equals("START_NUMBER") && Character.isDigit(actualWord.charAt(0))) {
							output.append("START_NUMBER");
							output.append(" ");
						}
						
						// If we have any of these suffixes, lets append that.
						if (feature.equals("SUFFIX")) {
							for (String suffix : SUFFIXES) {
								if (actualWord.endsWith(suffix)) {
									output.append(suffix);
									output.append(" ");
									break;
								}
							}
						}
						
						// If we have any of these prefixes, lets append that.
						if (feature.equals("PREFIX")) {
							for (String prefix : PREFIXES) {
								if (actualWord.startsWith(prefix)) {
									output.append(prefix);
									output.append(" ");
									break;
								}
							}
						}
						
					}
					output.append(tWord.substring(tWord.indexOf("/") + 1));
					output.append(eol);
				}
			}
		} else if (line.startsWith("==") || line.length() == 0) {
			output.append(eol);
		} else {
			String[] taggedWords = line.split("\\s+");
			for (String tWord : taggedWords) {
                String actualWord = tWord.substring(0, tWord.indexOf("/"));
			    output.append(actualWord);
				output.append(" ");
				for (String feature : featureMap.keySet()) {
					// If first charcter is CAPS, then add CAPS feature.
					if (feature.equals("CAPS") && Character.isUpperCase(actualWord.charAt(0))) {
						output.append("CAPS");
						output.append(" ");
					}
					
					// IF word contains HYPHEN, add hypen feature.
					if (feature.equals("HYPHEN") && actualWord.contains("-")) {
						output.append("HYPHEN");
						output.append(" ");
					}
					
					// IF word starts with a number.
					if (feature.equals("START_NUMBER") && Character.isDigit(actualWord.charAt(0))) {
						output.append("START_NUMBER");
						output.append(" ");
					}
					
					// If we have any of these suffixes, lets append that.
					if (feature.equals("SUFFIX")) {
						for (String suffix : SUFFIXES) {
							if (actualWord.endsWith(suffix) && !actualWord.equals(suffix)) {
								output.append(suffix);
								output.append(" ");
								break;
							}
						}
					}
					
					// If we have any of these prefixes, lets append that.
					if (feature.equals("PREFIX")) {
						for (String prefix : PREFIXES) {
							if (actualWord.startsWith(prefix) && !actualWord.equals(prefix)) {
								output.append(prefix);
								output.append(" ");
								break;
							}
						}
					}
					
				}
				output.append(tWord.substring(tWord.indexOf("/") + 1));
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
                pw.flush();
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
		String featureFile = null;
		if (args.length >= 2) {
			outputFile = args[1];
		} 
        
        if(args.length == 3) {
			featureFile = args[2];
			
            try {
    			FileReader fileReader = new FileReader(featureFile);
	    		BufferedReader br = new BufferedReader(fileReader);
		    	
                String currentLine = "";
			    while ((currentLine = br.readLine()) != null) {
				    String[] feature_switch = currentLine.split(" ");
    				boolean is_true = Boolean.parseBoolean(feature_switch[1]);
                    //System.out.println(is_true);
	    			if (is_true) {
		    			featureMap.put(feature_switch[0], true);
			    	}
			    }
                for (String feature : featureMap.keySet()) {
                    //System.out.println(feature);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
		}	
		
		try {
			pw = new PrintWriter(new FileWriter(outputFile));
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
