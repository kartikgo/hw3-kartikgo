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
 * Class to annotate unigrams of type Ngram.
 * 
 * 
 * @author Kartik Goyal
 */
public class UnigramAnnotator extends JCasAnnotator_ImplBase {
  /**
   * It uses casProcessorId of tokens to form relevant unigrams
   */
  public void process(JCas jCas) throws AnalysisEngineProcessException {

    FSIndex tokIdx = jCas.getAnnotationIndex(Token.type);
    Iterator tok = tokIdx.iterator();
    
    while (tok.hasNext()){
      Token token = (Token)tok.next();
      FSArray tok_arr= new FSArray(jCas,1);
      tok_arr.set(0, token);
      NGram unigram= new NGram(jCas, token.getBegin(), token.getEnd());
      unigram.setCasProcessorId(token.getCasProcessorId());
      unigram.setConfidence(1.0);
      unigram.setElementType("edu.cmu.deiis.types.Token");
      unigram.setElements(tok_arr);
      unigram.addToIndexes();
    }
  }
}
