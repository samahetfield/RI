package luceneej1;

import java.io.IOException;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.CharTokenizer;
import org.apache.lucene.util.AttributeFactory;
import org.apache.lucene.util.PagedBytes.Reader;

public class MyTokenizer extends CharTokenizer{
	public MyTokenizer() {
		super();
	}
	
	public MyTokenizer(AttributeFactory factory) {
		super(factory);
	}
	
	@Override
	protected boolean isTokenChar(int c) {
		//This method compare if a character is a WhiteSpace or a Letter or Digit
		if(!Character.isSpaceChar(c) && Character.isLetterOrDigit(c)) {
			return true;
		}
		else {
			return false;
		}
		
	}
	
	
}
