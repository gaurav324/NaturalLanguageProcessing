package nlp.lm;

import java.io.*;
import java.util.*;

/** 
 * @author Gaurav Nanda
 * A bidirectional bigram language model that uses simple fixed-weight interpolation
 * with a unigram model for smoothing.
*/


public class BidirectionalBigramModel {
    
    public BigramModel bigramModel;
    public BackwardBigramModel backwardBigramModel;
    
    public double forwardRatio = 0.5;
    public double backwardRatio = 0.5;

    public boolean removeDots = false;

    public BidirectionalBigramModel(boolean removeDots, double forwardRatio) {
        bigramModel = new BigramModel(removeDots);
        backwardBigramModel = new BackwardBigramModel(removeDots);

        this.removeDots = removeDots;
        this.forwardRatio = forwardRatio;
        this.backwardRatio = 1 - forwardRatio;
    }

    public static void print(Object printIt) {
        System.out.println(printIt);
    }

    // This would train both the backward and forward models.
    public void train(List<List<String>> sentences) {
        print("Training bi-gram model.");
        bigramModel.train(sentences);

        print("Training Backward bi-gram model.");
        backwardBigramModel.train(sentences);
    }

    // This would test our model by taking contribution of both forward and backward models.
    public void test(List<List<String>> sentences) {
        // Compute log probability of sentence to avoid underflow
        double totalLogProb = 0;

        // Keep count of total number of tokens predicted
        double totalNumTokens = 0;

        // Accumulate log prob of all test sentences
        for (List<String> sentence : sentences) {
            if (removeDots) {
                sentence.remove(".");
            }

            totalNumTokens += sentence.size();
            
            // Compute log prob of sentence.
            //
            // This is where we would perform magic.
            double sentenceLogProb = sentenceLogProb(sentence);
            
            // Add to total log prob (since add logs to multiply probs)
            totalLogProb += sentenceLogProb;
        }
        // Given log prob compute perplexity
        double perplexity = Math.exp(-totalLogProb / totalNumTokens);
        System.out.println("Word Perplexity = " + perplexity ); 
    }

    public double sentenceLogProb (List<String> sentence) {
        
        double[] bigramProbs = bigramModel.sentenceTokenProbs(sentence);

        Collections.reverse(sentence);
        double[] backwardBigramProbs = backwardBigramModel.sentenceTokenProbs(sentence);
        Collections.reverse(sentence);
        
        double sentenceLogProb = 0;
        for (int i=0; i < bigramProbs.length; ++i) {
            double interpolatedProb = Math.log(forwardRatio * bigramProbs[i] + backwardRatio * backwardBigramProbs[bigramProbs.length - i - 1]);
            sentenceLogProb += interpolatedProb;
        }

        return sentenceLogProb;
    }

    /** Train and test a bidirectional model.
     *  Command Format "nlp.lm.BidirectionalModel [DIR]* [TestFrac] [removeDots] [forwardProb].
     * 0 < TestFrac < 1
     *  Uses the second last fraction of the data for testing and the first part
     *  for training.
     *
     *  removeDots {true, false}
     *  Uses last argument to control whether we want to remove "."
     *  from each sentence.
     *
     *  forwardProb {0 < forwardProb < 1}
     *  How much contribution of forward model would be there.
     */
    public static void main(String[] args) {
        // All but last arg is a file/directory of LDC tagged input data
        File[] files = new File[args.length - 3];
        for (int i = 0; i < files.length; i++) 
            files[i] = new File(args[i]);

        // Third last arg is the TestFrac
        double testFraction = Double.valueOf(args[args.length - 3]);

        // Second Last arg to control whether to remove "." from each sentence.
        boolean removeDots = Boolean.valueOf(args[args.length - 2]);

        // Last arg determines how much would forward model contribute.
        double forwardRatio = Double.valueOf(args[args.length - 1]);
        
        // Get list of sentences from the LDC POS tagged input files
        List<List<String>> sentences =  POSTaggedFile.convertToTokenLists(files);
        int numSentences = sentences.size();
        
        // Compute number of test sentences based on TestFrac
        int numTest = (int)Math.round(numSentences * testFraction);
        
        // Take test sentences from end of data
        List<List<String>> testSentences = sentences.subList(numSentences - numTest, numSentences);
        
        // Take training sentences from start of data
        List<List<String>> trainSentences = sentences.subList(0, numSentences - numTest);
        System.out.println("# Train Sentences = " + trainSentences.size() + 
                   " (# words = " + BigramModel.wordCount(trainSentences) + 
                   ") \n# Test Sentences = " + testSentences.size() +
                   " (# words = " + BigramModel.wordCount(testSentences) + ")");
        
        // Create a bigram model and train it.
        BidirectionalBigramModel model = new BidirectionalBigramModel(removeDots, forwardRatio);
        System.out.println("Training...");
        model.train(trainSentences); 

        // Test on the train data.
        model.test(trainSentences);
    
        System.out.println("Testing...");
        // Test on test data using test and test2
        model.test(testSentences);
    }
}
