package edu.cmu.deiis.annotator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.cmu.deiis.types.*;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
//import org.apache.uima.tutorial.RoomNumber;
import org.apache.uima.util.Level;

/**
 * Annotator to annotate questions and answers
 * @author Kartik Goyal
 */

public class TestElementAnnotator extends JCasAnnotator_ImplBase {

  private Pattern[] mPatterns;

  private String[] mType;

  /**
   * Passing patterns as paramenters in the xml file
   */
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    // Get config. parameter values
    String[] patternStrings = (String[]) aContext.getConfigParameterValue("Patterns");
    mType = (String[]) aContext.getConfigParameterValue("Types");

    // compile regular expressions
    mPatterns = new Pattern[patternStrings.length];
    for (int i = 0; i < patternStrings.length; i++) {
      mPatterns[i] = Pattern.compile(patternStrings[i]);
    }
  }

  /**
   * Process to compile regex patterns for questions and answers.
   */
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    // get document text
    String docText = aJCas.getDocumentText();

    // loop over patterns
    for (int i = 0; i < mPatterns.length; i++) {
      Matcher matcher = mPatterns[i].matcher(docText);
      while (matcher.find()) {
        // found one - create annotation
        if (mType[i].equals("Que")) {
          Question annotation = new Question(aJCas);
          annotation.setBegin(matcher.start() + 2);
          annotation.setEnd(matcher.end());
          annotation.setConfidence(1.0);
          annotation.setCasProcessorId("QuestionRegex");
          annotation.addToIndexes();
          getContext().getLogger().log(Level.FINEST, "Found: " + annotation);
        }

        else if (mType[i].equals("Ans")) {
          Answer annotation = new Answer(aJCas);
          annotation.setBegin(matcher.start() + 4);
          annotation.setEnd(matcher.end());
          if (matcher.group(1).equals("1")) {
            annotation.setIsCorrect(true);
          } else {
            annotation.setIsCorrect(false);
          }
          // System.out.println(docText.charAt(matcher.start()));
          annotation.setConfidence(1.0);
          annotation.setCasProcessorId("AnswerRegex");
          annotation.addToIndexes();
          getContext().getLogger().log(Level.FINEST, "Found: " + annotation);
        }
      }
    }
  }

}
