import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.trees.MemoryTreebank;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.Treebank;


public class SplitBrown {
	/*
	 * args[0] is supposed to be the name of the folder in which all brown data is present.
	 * 
	 * args[1] Name of the folder in which both training and test files are to be kept.
	 */
	public static void main(String[] args) {
		Options op = new Options();
		op.doDep = false;
		op.doPCFG = true;
		op.setOptions("-goodPCFG", "-evals", "tsv");

		File f = new File(args[0]);
		List<Tree> train_trees = new ArrayList<Tree>();
		List<Tree> test_trees = new ArrayList<Tree>();
		for (File folder : f.listFiles()) {
			//Treebank trainTreeBank = DomainAdapterParser.makeTreebank(folder.getAbsolutePath(), op);
			MemoryTreebank trainTreeBank = new MemoryTreebank();
			trainTreeBank.loadPath(folder.getAbsolutePath());
			
			int count = 0;
			int total_trees = (int)(.9 * trainTreeBank.size());
			for (Tree tree : trainTreeBank) {
				if (count <= total_trees) {
					train_trees.add(tree);
				} else {
					test_trees.add(tree);
				}
				++count;
			}
		}
		
		File train_file = new File(args[1] + "/" + "train_file");
		File test_file = new File(args[1] + "/" + "test_file");
		try {
			FileWriter trainFileWriter = new FileWriter(train_file);
			FileWriter testFileWriter = new FileWriter(test_file);
			
			for (Tree tree : train_trees) {
				trainFileWriter.write(tree.pennString());
				trainFileWriter.write("\n");
				trainFileWriter.flush();
			}
			trainFileWriter.close();
			System.out.println("Train file written.");
			
			for (Tree tree : test_trees) {
				testFileWriter.write(tree.pennString());
				testFileWriter.write("\n");
				testFileWriter.flush();
			}
			testFileWriter.close();
			System.out.println("Test file written.");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
}
