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

import edu.cmu.deiis.types.*;
/**
 * This calculates overlaps of tokens, ngrams, part of speech tags(both tokens and ngrams), lemma(bith tokens and ngrams) 
 * Finally a score which is a weighted sum of these attributes is calculated.
 * @author Kartik Goyal
 *
 */
public class ScoreAnnotator extends JCasAnnotator_ImplBase {
  
  public void process(JCas jCas) throws AnalysisEngineProcessException {

    FSIndex quesIdx = jCas.getAnnotationIndex(Question.type);
    FSIndex ansIdx = jCas.getAnnotationIndex(Answer.type);
    FSIndex tokenIdx= jCas.getAnnotationIndex(Token.type);
    FSIndex ngIdx= jCas.getAnnotationIndex(NGram.type);
    Iterator que = quesIdx.iterator();
    Iterator ans = ansIdx.iterator();
    Iterator tok= tokenIdx.iterator();
    Iterator ng= ngIdx.iterator();
    ArrayList<Token> questionTok= new ArrayList<Token>();
    ArrayList<NGram> questionGram= new ArrayList<NGram>();
    while(que.hasNext()){
      Question q=(Question)que.next();
      int qbeg = q.getBegin();
      int qend = q.getEnd();
      while (tok.hasNext()){
        Token t= (Token)tok.next();
        if ((t.getBegin()>=qbeg)&&(t.getEnd()<=qend)){
          questionTok.add(t);
        }
      }
      while(ng.hasNext()){
        NGram n= (NGram)ng.next();
        if ((n.getBegin()>=qbeg)&&(n.getEnd()<=qend)){
          questionGram.add(n);
        }
      }
    }
    
    while(ans.hasNext()){
      tok= tokenIdx.iterator();
      ng= ngIdx.iterator();
      ArrayList<Token> answerTok= new ArrayList<Token>();
      ArrayList<NGram> answerGram= new ArrayList<NGram>();
      Answer a=(Answer)ans.next();
      int abeg = a.getBegin();
      int aend = a.getEnd();
      while (tok.hasNext()){
        Token t= (Token)tok.next();
        if ((t.getBegin()>=abeg)&&(t.getEnd()<=aend)){
          answerTok.add(t);
        }
      }
      
      
      double totTok = (double)answerTok.size();
      double tokOl=0;
      double POSOl=0;
      double lemmaOl=0;
      for(Token qtok: questionTok){
        for (Token atok:answerTok){
          if (atok.getCoveredText().equals(qtok.getCoveredText())){
            tokOl +=1;
          }
          if (atok.getPOS().equals(qtok.getPOS())){
            POSOl +=1;
          }
          if (atok.getPOS().equals(qtok.getPOS())){
            lemmaOl +=1;
          }
        }
      }
      double tokScore= tokOl/totTok;
      double POSScore= POSOl/totTok;
      double lemmaScore= lemmaOl/totTok;
      
      
      while (ng.hasNext()){
        NGram n= (NGram)ng.next();
        if ((n.getBegin()>=abeg)&&(n.getEnd()<=aend)){
          answerGram.add(n);
        }
      }
      
      double totgram = (double)answerGram.size();
      double gramOl=0;
      double gramPOSOl=0;
      double gramlemmaOl=0;
      for(NGram qn: questionGram){
        for (NGram an:answerGram){
          if(an.getElements().size()==qn.getElements().size()){
            FSArray aElements = an.getElements();
            FSArray qElements = qn.getElements();
            if(an.getElements().size()==1){
              Token aFirst = (Token)aElements.get(0);
              Token qFirst= (Token)qElements.get(0);
              if(aFirst.getCoveredText().equals(qFirst.getCoveredText())){
                gramOl +=1;
              }
              if(aFirst.getLemma().equals(qFirst.getLemma())){
                gramlemmaOl +=1;
              }
              if(aFirst.getPOS().equals(qFirst.getPOS())){
                gramPOSOl +=1;
              }
            }
            if(an.getElements().size()==2){
              Token aFirst = (Token)aElements.get(0);
              Token qFirst= (Token)qElements.get(0);
              Token aSecond = (Token)aElements.get(1);
              Token qSecond= (Token)qElements.get(1);
              if(aFirst.getCoveredText().equals(qFirst.getCoveredText())&&(aSecond.getCoveredText().equals(qSecond.getCoveredText()))){
                gramOl +=1;
              }
              if(aFirst.getLemma().equals(qFirst.getLemma())&&(aSecond.getLemma().equals(qSecond.getLemma()))){
                gramlemmaOl +=1;
              }
              if(aFirst.getPOS().equals(qFirst.getPOS())&&(aSecond.getPOS().equals(qSecond.getPOS()))){
                gramPOSOl +=1;
              }
            }
            if(an.getElements().size()==3){
              Token aFirst = (Token)aElements.get(0);
              Token qFirst= (Token)qElements.get(0);
              Token aSecond = (Token)aElements.get(1);
              Token qSecond= (Token)qElements.get(1);
              Token aThird = (Token)aElements.get(2);
              Token qThird= (Token)qElements.get(2);
              if(aFirst.getCoveredText().equals(qFirst.getCoveredText())&&(aSecond.getCoveredText().equals(qSecond.getCoveredText()))&&(aThird.getCoveredText().equals(qThird.getCoveredText()))){
                gramOl +=1;
              }
              if(aFirst.getLemma().equals(qFirst.getLemma())&&(aSecond.getLemma().equals(qSecond.getLemma()))&&(aThird.getLemma().equals(qThird.getLemma()))){
                gramlemmaOl +=1;
              }
              if(aFirst.getPOS().equals(qFirst.getPOS())&&(aSecond.getPOS().equals(qSecond.getPOS()))&&(aThird.getPOS().equals(qThird.getPOS()))){
                gramPOSOl +=1;
              }
            }
          }
          
        }
      }
      double gramScore= gramOl/totgram;
      double gramPOSScore= gramPOSOl/totgram;
      double gramlemmaScore= gramlemmaOl/totgram;
      //Setting answerscore
      double combinedScore=((0.3*tokScore)+(0.15*lemmaScore)+(0.15*gramlemmaScore)+(0.05*POSScore)+(0.05*gramPOSScore)+(0.3*gramScore));
      AnswerScore tokLevelScore= new AnswerScore(jCas);
      tokLevelScore.setAnswer(a);
      tokLevelScore.setScore(combinedScore);
      tokLevelScore.setCasProcessorId("Token-NGram-Score");
      tokLevelScore.setBegin(abeg);
      tokLevelScore.setEnd(aend);
      tokLevelScore.setConfidence(1.0);
      tokLevelScore.addToIndexes();
    }
  }
}
