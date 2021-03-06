import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasTag;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.EvaluateTreebank;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.parser.lexparser.ParserQuery;
import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.MemoryTreebank;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;
import edu.stanford.nlp.trees.Treebank;
import edu.stanford.nlp.util.Timing;

class DomainAdapterParser {
	
	public static Treebank makeTreebank(String treebankPath, Options op) {
	    System.err.println("Training a parser from treebank dir: " + treebankPath);
	    Treebank trainTreebank = op.tlpParams.memoryTreebank();
	    System.err.print("Reading trees...");
	    
	    trainTreebank.loadPath(treebankPath);
	    
	    Timing.tick("done [read " + trainTreebank.size() + " trees].");
	    return trainTreebank;
	}
	  
	/**
	 * args[0] is expected to be the training folder/file.
	 * 
	 * args[1] is expected to the data to be self-trained.
	 * 
	 * args[2] is expected to be the date to be tested.
	 * 
	 * args[3] is expected to be the number of threads. Defaults to 1.
	 * @param args
	 */
	public static void main(String[] args) {
		Options op = new Options();
		op.doDep = false;
		op.doPCFG = true;
		op.setOptions("-goodPCFG", "-evals", "tsv");
		
		if (args.length < 3) {
			System.out.println("Please specify three arguments.");
			return;
		}
		
		String treebankPath = args[0];
		
		// Create Lexicalized parser using just the training data.
		Treebank trainTreeBank = DomainAdapterParser.makeTreebank(treebankPath, op);
		LexicalizedParser lp = LexicalizedParser.trainFromTreebank(trainTreeBank, null, op);
		System.out.println("Size of original Tree Bank: " + trainTreeBank.size());
		
		if (!args[1].equals("NOADAPTION")) {
			// Create memory tree bank for the self-training data.
			Treebank selfTrainTreebank = op.tlpParams.memoryTreebank();
			selfTrainTreebank.loadPath(args[1], null);
			System.out.println("Size of selfTrain Tree Bank: " + selfTrainTreebank.size());
			
			System.out.println("Update tree bank with self trained tree bank...");
			
			List<List<CoreLabel>> sentences = new ArrayList<List<CoreLabel>>();
			for (Tree tree : selfTrainTreebank) {
				//System.out.println("Old Tree: " + tree.toString());
				List<CoreLabel> sentence = Sentence.toCoreLabelList(tree.yieldWords());
				sentences.add(sentence);
			}	
			
			int threads = args.length > 3 ? Integer.parseInt(args[3]) : 1;
			List<Tree> new_trees = lp.parseMultiple(sentences, threads);
			
			for (Tree new_tree : new_trees) {
				if (new_tree.children()[0].label().toString().equals("X")) {
					System.out.println("New Tree: " + new_tree.toString());
					continue;
				}
				//System.out.println("New Tree: " + new_tree.toString());
				trainTreeBank.add(new_tree);
			}
			
			System.out.println("Done updating tree bank with self trained data..");	
			System.out.println("Size of updated Tree Bank: " + trainTreeBank.size());
			
			System.out.println("Retraining parser..");
			lp = LexicalizedParser.trainFromTreebank(trainTreeBank, null, op);
			System.out.println("Retraining done.");
		}
		
		Treebank testTreeBank = op.tlpParams.memoryTreebank();
		testTreeBank.loadPath(args[2], null);
		
		System.out.println("Evaluate tree bank of the size: " + testTreeBank.size());
		EvaluateTreebank evaluator = new EvaluateTreebank(lp);
	    evaluator.testOnTreebank(testTreeBank);	
	}
}
