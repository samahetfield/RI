package luceneej1;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer.Builder;
import org.apache.lucene.analysis.standard.StandardFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.util.CharTokenizer;

public class SourceAnalyzer extends Analyzer{
	public int maxTokenLength = 50;
	private static final List<String> stopwords = Arrays.asList("abstract", 
    		"assert", "boolean", "break", "byte", "case", "catch", "char",
    		"char", "class", "const", "continue", "default", "do", "double",
    		"else", "enum", "extends", "final", "finally", "float", "for",
    		"goto", "if", "implements", "import", "instanceof", "int", "interface",
    		"long", "native", "new", "package", "private", "protected", "public",
    		"return", "short", "static", "strictfp", "super", "switch", 
    		"synchronyzed", "this", "throw", "throws", "transient",
    		"try", "void", "volatile", "while", "this"); 

 
    public SourceAnalyzer() {

    	
    }
    
    @Override
    protected TokenStreamComponents createComponents(String string)   {
    	//This method create the tokens that we need
    	final Tokenizer source = new MyTokenizer(); //Use CustomTokenizer
        TokenStream pipeline = source;
        
        pipeline = new LowerCaseFilter(pipeline); //All the tokens in LowerCase
        pipeline = new StopFilter( pipeline, new CharArraySet(stopwords,true)); //Remove the words in the list stopwords
        
        
        return new TokenStreamComponents(source, pipeline); 

    }
    
    
}
