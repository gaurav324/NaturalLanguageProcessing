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

    public BidirectionalBigramModel() {
        bigramModel = new BigramModel();
        backwardBigramModel = new BackwardBigramModel();
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
        //List<Double> bigramProbs = bigramModel.sentenceLogProbList(sentence);
        //List<Double> backwardBigramProbs = backwardBigramModel.sentenceLogProbList(sentence);

        //print("Sentence: " + sentence);
        
        double[] bigramProbs = bigramModel.sentenceTokenProbs(sentence);

        Collections.reverse(sentence);
        double[] backwardBigramProbs = backwardBigramModel.sentenceTokenProbs(sentence);
        Collections.reverse(sentence);
        
        double sentenceLogProb = 0;
        for (int i=0; i < bigramProbs.length; ++i) {
            //print("Word: " + sentence.get(i));
            //print("Forward Probability: " + bigramProbs[i]);
            //print("Backward Probability: " + backwardBigramProbs[bigramProbs.length - i - 1]);
            //print("Middle Probability: " + (forwardRatio * bigramProbs[i] + backwardRatio * backwardBigramProbs[bigramProbs.length - i - 1]));
            double interpolatedProb = Math.log(forwardRatio * bigramProbs[i] + backwardRatio * backwardBigramProbs[bigramProbs.length - i - 1]);
            sentenceLogProb += interpolatedProb;
            //print(forwardRatio * bigramProbs.get(i) + backwardRatio * backwardBigramProbs.get(i));
            //double interPolatedProb = Math.log(forwardRatio * bigramProbs.get(i) + backwardRatio * backwardBigramProbs.get(i));
            //print(interPolatedProb);
            //sentenceLogProb += interPolatedProb;
        }

        return sentenceLogProb;
    }

    public static void main(String[] args) {
        // All but last arg is a file/directory of LDC tagged input data
        File[] files = new File[args.length - 1];
        for (int i = 0; i < files.length; i++) 
            files[i] = new File(args[i]);

        // Last arg is the TestFrac
        double testFraction = Double.valueOf(args[args.length -1]);
        
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
        BidirectionalBigramModel model = new BidirectionalBigramModel();
        System.out.println("Training...");
        model.train(trainSentences); 

        // Test on the train data.
        model.test(trainSentences);

        // Test on test data using test and test2
        model.test(testSentences);
    }
}
