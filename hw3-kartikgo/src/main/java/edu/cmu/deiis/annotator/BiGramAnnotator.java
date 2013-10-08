package edu.cmu.deiis.annotator;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cmu.deiis.types.*;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

/**
 * 
 * Class to annotate bigrams of type Ngram.
 * 
 * 
 * @author Kartik Goyal
 */
public class BiGramAnnotator extends JCasAnnotator_ImplBase{
  /**
   * It uses casProcessorId of tokens to form relevant bigrams
   */
  public void process(JCas jCas) throws AnalysisEngineProcessException {

    FSIndex tokIdx = jCas.getAnnotationIndex(Token.type);
    Iterator tok = tokIdx.iterator();
    Token mem = new Token(jCas, -1, -1);
    while (tok.hasNext()) {
      Token token = (Token) tok.next();
      if (!(mem.getBegin() == -1)) {
        if (token.getCasProcessorId().equals(mem.getCasProcessorId())) {
          FSArray tok_arr = new FSArray(jCas, 2);
          tok_arr.set(0, mem);
          tok_arr.set(1, token);
          NGram bigram = new NGram(jCas, mem.getBegin(), token.getEnd());
          bigram.setCasProcessorId(token.getCasProcessorId());
          bigram.setConfidence(1.0);
          bigram.setElementType("edu.cmu.deiis.types.Token");
          bigram.setElements(tok_arr);
          bigram.addToIndexes();

        }

      }
      mem = token;
    }
  }
}
