import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.Treebank;

public class SentenceExtractor {
	/**
	 * args[0] is expected to be the file/folder name.
	 * 
	 * args[1] is expected to be the location of the file to be written.
	 * 
	 * args[2] is expected to be the number of sentences to be extracted.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Options op = new Options();
		op.doDep = false;
		op.doPCFG = true;
		op.setOptions("-goodPCFG", "-evals", "tsv");
		Treebank trainTreeBank = DomainAdapterParser.makeTreebank(args[0], op);

		int numofSentences = Integer.parseInt(args[2]);
		int count = 0;
		List<Tree> trees = new ArrayList<Tree>();
		for (Tree tree : trainTreeBank) {
			if (count == numofSentences) {
				break;
			}
			trees.add(tree);
			++count;
		}
		
		File writeLocation = new File(args[1]);
		try {
			FileWriter fw = new FileWriter(writeLocation);
			
			for (Tree tree : trees) {
				fw.write(tree.pennString());
				fw.write("\n");
				fw.flush();
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}