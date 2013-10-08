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
import java.util.regex.Pattern;

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
 * The final class that sorts the answers based on their score
 * @author Kartik Goyal
 *
 */
public class Evaluator extends JCasAnnotator_ImplBase {
  private Integer id;
  private ArrayList<Double> preclist;
  /**
   * Global list for average precision
   */
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    // Get config. parameter values
    id = 0;
    preclist= new ArrayList<Double>();
  }
  /**
   * Calculating average precision
   */
  public void destroy(){
    super.destroy();
    Double tot=0.0;
    for (Double prec: preclist){
      tot += prec;
    }
    Double avg_prec= tot/id.doubleValue();
    System.out.println("Average Precision: "+avg_prec.toString());
  }
  /**
   * sort the answers
   */
  public void process(JCas jCas) throws AnalysisEngineProcessException {
    id +=1;
    FSIndex scoreIdx = jCas.getAnnotationIndex(AnswerScore.type);
    FSIndex qIdx = jCas.getAnnotationIndex(Question.type);
    Iterator scr = scoreIdx.iterator();
    Iterator q = qIdx.iterator();
    ArrayList<Question> ql = new ArrayList<Question>();
    ArrayList<AnswerScore> answers = new ArrayList<AnswerScore>();
    Integer correctAns = 0;
    while (q.hasNext()) {
      Question que = (Question) q.next();
      ql.add(que);
    }
    while (scr.hasNext()) {
      AnswerScore asc = (AnswerScore) scr.next();
      if (asc.getAnswer().getIsCorrect()) {
        correctAns += 1;
      }
      answers.add(asc);
    }
    Collections.sort(answers, new Comparator<AnswerScore>() {
      @Override
      public int compare(AnswerScore a1, AnswerScore a2) {
        Double difference = a2.getScore() - a1.getScore();
        if (Math.abs(difference) < 1)
          difference = 1 / difference;
        return difference.intValue();
      }
    });
    Question ques= ql.get(0);
    Integer predicted = 0;
    for (int cnt = 0; cnt < correctAns; cnt++) {
      if (answers.get(cnt).getAnswer().getIsCorrect()) {
        predicted += 1;
      }
    }
    Double precision = predicted.doubleValue() / correctAns.doubleValue();
    preclist.add(precision);
    System.out.println("Question: "+ques.getCoveredText());
    for (AnswerScore answ:answers){
      String label = answ.getAnswer().getIsCorrect() ? "+":"-";
      System.out.println(label+" "+answ.getScore()+" "+answ.getAnswer().getCoveredText());
      
    }
    System.out.println("Precision at "+correctAns.toString()+": "+precision.toString()+"\n");
  }
}
