import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.AttributeFactory;

import java.io.IOException;

/**
 * Created by sergi on 19/11/2017.
 */
public class AuthorTokenizer extends CharTokenizer {
    public AuthorTokenizer() {
        super();
    }

    public AuthorTokenizer(AttributeFactory factory) {
        super(factory);
    }

    @Override
    protected boolean isTokenChar(int c) {
        //This method compare if a character is a WhiteSpace or a Letter or Digit
        char[] c1 = Character.toChars(c);
        if(Character.compare(c1[0], ',') != 0) {
            return true;
        }
        else {
            return false;
        }

    }

}



