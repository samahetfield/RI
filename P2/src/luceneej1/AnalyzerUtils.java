/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package luceneej1;


import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.ClassicAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.UAX29URLEmailAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;


import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;


import org.apache.lucene.util.Version;
import org.apache.tika.Tika;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.apache.lucene.analysis.standard.StandardTokenizer;
/**
 *
 * @author jhg
 */



public class AnalyzerUtils {
	

    public static final Analyzer [] analizadores = {
            //new WhitespaceAnalyzer(),
            //new SimpleAnalyzer(),
            //new StopAnalyzer(),
            //new StandardAnalyzer( ),
            //new SpanishAnalyzer(),
            //new UAX29URLEmailAnalyzer(),
            //new AnalyzerNuevo(),
    		new SourceAnalyzer()
            };

   public static List<String> tokenizeString(Analyzer analyzer, String string) {
    List<String> result = new ArrayList<String>();
    
    String cad;
    try {
      TokenStream stream  = analyzer.tokenStream(null,  new StringReader(string));
      OffsetAttribute offsetAtt = stream.addAttribute(OffsetAttribute.class);
      CharTermAttribute cAtt= stream.addAttribute(CharTermAttribute.class);
      stream.reset();
      while (stream.incrementToken()) {
     
        //cad = stream.getAttribute(CharTermAttribute.class).toString();
        result.add( cAtt.toString()); // +" : ("+ offsetAtt.startOffset()+"," + offsetAtt.endOffset()+")");
      }
      stream.end();
    } catch (IOException e) {
      // not thrown b/c we're using a string reader...
      throw new RuntimeException(e);
    }
   
    return result;
  }

public static void testLuceneStandardTokenizer() throws Exception {

  StandardTokenizer tokenizer=new StandardTokenizer();
  List<String> result=new ArrayList<String>();
  tokenizer.reset();
  while (tokenizer.incrementToken()) {
      
    result.add(((CharTermAttribute)tokenizer.getAttribute(CharTermAttribute.class)).toString());
  }
  System.out.println(result.toString());
 
 
}

  public static void displayTokens(String text) throws IOException {

    List<String> tokens;
	HashMap<String, Integer> tokens_frequency = new HashMap<>();


    for (Analyzer an : analizadores){
        System.out.println("Analizador "+an.getClass());
        
        tokens = tokenizeString(an,text); //With this method get all the tokens
        for (String tk : tokens) {
            //System.out.println("[" + tk + "] ");
        	//Here I'm saving all the tokens in a HashMap and the frequency of each token
        	Integer default_value = tokens_frequency.getOrDefault(tk, 0);
		      if(default_value == 0) {
		    	  tokens_frequency.put(tk, 1);   
		      }
		      else {
		    	tokens_frequency.put(tk, ( default_value )+1);
		      }
         }
        
        //Finally print the token and it's frequency
	    for (Entry<String, Integer> entry : tokens_frequency.entrySet()) {
			System.out.println(entry.getValue()+" -> "+entry.getKey());
			
		}
		
     }
   
    }

   public static void main(String[] args) throws IOException, Exception {
       
	   //Save path directory in a File variable
	   File Directory = new File(args[0]);
	   String[] files = Directory.list(); //each file's name
	   ArrayList<File> Files_Array = new ArrayList<>();
	
	   
		for(String file_auxiliar : files) {
			
			/*this.Files_Array[aux_index]*/
			//Get each file name and save it in a File variable
			File aux = new File(Directory.toString() +"\\"+ file_auxiliar); //assign the path for each file
			Files_Array.add(aux);
			Tika tika = new Tika();
			
			//Call method tika.parseToString because Lucene works with plain text, such a String
			String fileToString = tika.parseToString(aux);
			//Call this method to analyze the the text
			displayTokens(fileToString);
			//testLuceneStandardTokenizer();
		}

    }

}


 
