/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package luceneej1;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jhg
 */


import org.apache.lucene.analysis.Analyzer; 
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.util.AttributeFactory;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.Version;

public class AnalyzerNuevo extends Analyzer { 
  /** Tokens longer than this length are discarded. Defaults to 50 chars. */
    public int maxTokenLength = 50;
    private static final List<String> stopwords = Arrays.asList("a", "an", "and", "are", "as", "at", "be", "but", "by", 
        "for", "if", "in", "into", "is", "it", "no", "not", "of", 
        "on", "or", "such", "that", "the", "their", "then", "there", "these", 
        "they", "this", "to", "was", "will", "with" ); 

 
    public AnalyzerNuevo() {
    	
    }
    
    @Override
    protected TokenStreamComponents createComponents(String string)   {
        
            //To change body of generated methods, choose Tools | Templates.
            final Tokenizer source = new StandardTokenizer();
            SynonymMap.Builder builder = new SynonymMap.Builder(true);
            builder.add(new CharsRef("text"), new CharsRef("documento"), true);
            
            SynonymMap synonymMap;
        
        
            TokenStream pipeline = source;
            pipeline = new StandardFilter( pipeline);
          
            pipeline = new EnglishPossessiveFilter(pipeline);
            try {
            synonymMap = builder.build();
            pipeline = new SynonymFilter(pipeline,synonymMap,true);
            } catch (IOException ex) {
               Logger.getLogger(AnalyzerNuevo.class.getName()).log(Level.SEVERE, null, ex);
            }
            
         
        
            
            
            pipeline = new ASCIIFoldingFilter(pipeline);
            pipeline = new LowerCaseFilter(pipeline);
            pipeline = new StopFilter( pipeline, new CharArraySet(stopwords,true));
            pipeline = new PorterStemFilter(pipeline);
          
        
          return new TokenStreamComponents(source, pipeline);
    }


} 
