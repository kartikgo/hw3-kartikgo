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
 * Class to annotate trigrams of type Ngram.
 * 
 * 
 * @author Kartik Goyal
 */
public class TriGramAnnotator extends JCasAnnotator_ImplBase {
  /**
   * It uses casProcessorId of tokens to form relevant trigrams
   */
  public void process(JCas jCas) throws AnalysisEngineProcessException {

    FSIndex tokIdx = jCas.getAnnotationIndex(Token.type);
    Iterator tok = tokIdx.iterator();
    Token mem1 = new Token(jCas, -1, -1);
    Token mem2 = new Token(jCas, -1, -1);
    while (tok.hasNext()) {
      Token token = (Token) tok.next();
      if ((!(mem1.getBegin() == -1)) && (!(mem2.getBegin() == -1))) {
        if ((token.getCasProcessorId().equals(mem1.getCasProcessorId()))
                && (token.getCasProcessorId().equals(mem2.getCasProcessorId()))) {
          FSArray tok_arr = new FSArray(jCas, 3);
          tok_arr.set(0, mem2);
          tok_arr.set(1, mem1);
          tok_arr.set(2, token);
          NGram trigram = new NGram(jCas, mem2.getBegin(), token.getEnd());
          trigram.setCasProcessorId(token.getCasProcessorId());
          trigram.setConfidence(1.0);
          trigram.setElementType("edu.cmu.deiis.types.Token");
          trigram.setElements(tok_arr);
          trigram.addToIndexes();

        }

      }
      mem2 = mem1;
      mem1 = token;
    }
  }
}
