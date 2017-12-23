package Search;

import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.AttributeFactory;

/**
 * Created by sergi on 19/11/2017.
 */
public class KeyTokenizer extends CharTokenizer{
    public KeyTokenizer() {
        super();
    }

    public KeyTokenizer(AttributeFactory factory) {
        super(factory);
    }

    @Override
    protected boolean isTokenChar(int c) {
        //This method compare if a character is a WhiteSpace or a Letter or Digit
        char[] c1 = Character.toChars(c);
        if(Character.compare(c1[0], ';') != 0) {
            return true;
        }
        else {
            return false;
        }

    }
}



