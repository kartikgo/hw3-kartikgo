package edu.cmu.deiis.annotator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.Question;
//import edu.stanford.nlp.dcoref.CorefChain;
//import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
//import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
//import edu.stanford.nlp.ling.CoreAnnotations.BeginIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.EndIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
//import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
//import edu.stanford.nlp.semgraph.SemanticGraph;
//import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
//import edu.stanford.nlp.semgraph.SemanticGraphEdge;
//import edu.stanford.nlp.trees.Tree;
//import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.cmu.deiis.types.*;

/**
 * 
 * Class to annotate tokens and relevant information.
 * 
 * Token Enhanced to have part of speech and lemma information
 * 
 * 
 * @author Kartik Goyal
 */
public class TokenAnnotator extends JCasAnnotator_ImplBase {

  private StanfordCoreNLP processor;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);

    Properties properties = new Properties();
    properties.put("annotators", "tokenize, ssplit, pos, lemma, ner");

    this.processor = new StanfordCoreNLP(properties);
  }

  /**
   * Process to identify tokens and their attributes.
   */
  @Override
  public void process(JCas jCas) throws AnalysisEngineProcessException {

    FSIndex quesIdx = jCas.getAnnotationIndex(Question.type);
    FSIndex ansIdx = jCas.getAnnotationIndex(Answer.type);
    Iterator que = quesIdx.iterator();
    Iterator ans = ansIdx.iterator();

    while (que.hasNext()) {
      Question ques = (Question) que.next();
      Annotation document = this.processor.process(ques.getCoveredText());
      
      for (CoreMap tokenAnn : document.get(TokensAnnotation.class)) {

        // create the token annotation
        int begin = ques.getBegin()+tokenAnn.get(CharacterOffsetBeginAnnotation.class);
        int end = ques.getBegin()+tokenAnn.get(CharacterOffsetEndAnnotation.class);
        String pos = tokenAnn.get(PartOfSpeechAnnotation.class);
        String lemma = tokenAnn.get(LemmaAnnotation.class);
        Token token = new Token(jCas, begin, end);
        token.setCasProcessorId("Stanford-Question");
        token.setPOS(pos);
        token.setLemma(lemma);
        token.setConfidence(1.0);
        token.addToIndexes();
      }
    }
    int count=0;
    while (ans.hasNext()) {
      count +=1;
      Answer answ = (Answer) ans.next();
      Annotation Adocument = this.processor.process(answ.getCoveredText());
      
      for (CoreMap tokenAnn : Adocument.get(TokensAnnotation.class)) {

        // create the token annotation
        int begin = answ.getBegin()+tokenAnn.get(CharacterOffsetBeginAnnotation.class);
        int end = answ.getBegin()+tokenAnn.get(CharacterOffsetEndAnnotation.class);
        String pos = tokenAnn.get(PartOfSpeechAnnotation.class);
        String lemma = tokenAnn.get(LemmaAnnotation.class);
        Token token = new Token(jCas, begin, end);
        token.setCasProcessorId("Stanford-Answer:"+Integer.toString(count));
        token.setPOS(pos);
        token.setLemma(lemma);
        token.setConfidence(1.0);
        token.addToIndexes();
      }
    }
  }
  // hackery to convert token-level named entity tag into phrase-level tag
  /*
   * String neTag = tokenAnn.get(NamedEntityTagAnnotation.class); if (neTag.equals("O") &&
   * !lastNETag.equals("O")) { NamedEntityMention ne = new NamedEntityMention(jCas, lastNEBegin,
   * lastNEEnd); ne.setMentionType(lastNETag); ne.addToIndexes(); } else { if
   * (lastNETag.equals("O")) { lastNEBegin = begin; } else if (lastNETag.equals(neTag)) { // do
   * nothing - begin was already set } else { NamedEntityMention ne = new NamedEntityMention(jCas,
   * lastNEBegin, lastNEEnd); ne.setMentionType(lastNETag); ne.addToIndexes(); lastNEBegin = begin;
   * } lastNEEnd = end; } lastNETag = neTag; } if (!lastNETag.equals("O")) { NamedEntityMention ne =
   * new NamedEntityMention(jCas, lastNEBegin, lastNEEnd); ne.setMentionType(lastNETag);
   * ne.addToIndexes(); }
   */
}