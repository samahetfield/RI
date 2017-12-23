package Search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;

/**
 * Created by sergi on 19/11/2017.
 */
public class AuthorAnalyzer extends Analyzer {
    @Override
    protected Analyzer.TokenStreamComponents createComponents(String string){
        //This method create the tokens that we need
        final Tokenizer source = new AuthorTokenizer(); //Use CustomTokenizer
        TokenStream pipeline = source;

        pipeline = new LowerCaseFilter(pipeline); //All the tokens in LowerCase

        return new Analyzer.TokenStreamComponents(source, pipeline);

    }
}


