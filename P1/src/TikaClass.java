import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ContentHandler;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.tika.*;
import org.apache.tika.exception.TikaException;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.LinkContentHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.apache.tika.sax.ToHTMLContentHandler;
import org.xml.sax.SAXException;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.html.HtmlParser;

import com.optimaize.langdetect.LanguageDetector;



public class TikaClass {
	private Tika tika = new Tika(); //Tika object so we can use and apply their functions to our documents 
	private File Directory; //The directory which contains our documents
	private String Directory_String;
	//private File[] Files_Array;
	private ArrayList<File> Files_Array = new ArrayList<>();
	private String[] files;//The list of our documents

	Integer aux_ind = 0;

    
//Tika Functions's results will be stored in the next list of variables
	private Parser parser = new AutoDetectParser(); 	
	private BodyContentHandler handler = new BodyContentHandler(-1); 
	private Metadata metadata = new Metadata();
	private FileInputStream content;
	private String document_type = "";
	LanguageIdentifier object;
	
	//variables for link extraction
	private LinkContentHandler Link_handler = new LinkContentHandler();
	private Parser parser_links = new AutoDetectParser();
	private BodyContentHandler text_handler = new BodyContentHandler(-1);
	private ToHTMLContentHandler toHTML = new ToHTMLContentHandler();
	TeeContentHandler teeHandle;
	ParseContext parseContext = new ParseContext();
	
	
	
	
		
//****
	
//Data structured used into the process	
	private ArrayList<Pair<Integer, String>> Ordered_List = new ArrayList(); //Ordered_list will be used to have the terms ordered by their frecuency
	private HashMap<String, Integer> Words_Frequency = new HashMap<>(); //store the text's words frequency
// ****
	
	private File f; //file which represents a document output, of our search documents
	private String file_in_string;//store the file's text in String format 
	private Scanner fileScanner; //used for scanning over the file's text
	
	//delimeters it's a variable which store a regular expression 
	private String delimeters = "\\s(http|ftp|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?\\s|\\s[^0-9]\\s|\\s*[*{]*\\s|\\s[*}]*\\s|\\s[:]\\s|\\s*[(]*\\s|\\s[…]\\s|\\s \\s|\\s*[>]*\\s|\\s*[<]*\\s|\\s*[•]*\\s|\\s*[-]*\\s|\\s[(]*\\s|\\s*[)]\\s|\\s[*?]\\s|\\s[*.]\\s|\\s*[,]*\\s|\\s*[;]*\\s|\\s*[:]*\\s|\\s[+]\\s|\\s*[=]*\\s|\\s[*0-9]+[.]?[*0-9]\\s";
			
//Functions
	
	//Storing Files's path
	private void Store_Files_Path() {
		this.files = (this.Directory).list(); //each file's name
		
		
		for(String file_auxiliar : files) {
			
			/*this.Files_Array[aux_index]*/
			File aux = new File(Directory_String+"\\"+file_auxiliar); //assign the path for each file
			this.Files_Array.add(aux);
		
			//System.out.print(file_auxiliar);
		}
	}
	
	//apply tika functions to documents
	private void Generate_Tika_Functions_Results(File example) throws IOException, SAXException, TikaException {
		content = new FileInputStream(example);
	      document_type = tika.detect(example);
	      parser = new AutoDetectParser(); 				//Parser method parameters
	      handler = new BodyContentHandler(-1);
	      metadata = new Metadata();

	      //Parsing the given document
	      parser.parse(content, handler, metadata, new ParseContext());
	      object = new LanguageIdentifier(handler.toString());
	}
	
	private void Export_All_Links_From_Documents(File example) throws IOException, SAXException, TikaException {
		content = new FileInputStream(example);
		teeHandle = new TeeContentHandler(Link_handler,text_handler,toHTML);
		parser_links.parse(content, teeHandle, metadata, parseContext);
		System.out.println("links:\n" + Link_handler.getLinks());
	}
	
	private void Get_Metadata(File example) {
		System.out.println("Title: "+ metadata.get("title"));
		System.out.println("Type: "+ document_type);
		System.out.println("Codificación: "+ metadata.get("Content-Type"));
		System.out.println("Language name: "+ object.getLanguage());
		System.out.println("\n");

		System.out.println("----------------------------------------------");

	}
	
	
	
	
	//Once we get the terms into the hash, we order them by the frequency in ascendent form
	private void Oder_Terms_by_Frecuency() {
	    //Change the order Key-> Value to Value -> Key in new ArrayList  
		for (Entry<String, Integer> entry : Words_Frequency.entrySet()) {
			Ordered_List.add(new Pair(entry.getValue(), entry.getKey()));
			
		}
		System.out.println(Ordered_List.size());

		//Override the method compare to compare the first value of our Pair
		Collections.sort(Ordered_List, new Comparator<Pair>() {
			@Override public int compare(Pair x, Pair y) {
				//return (Integer) x.getL() - (Integer) y.getL();
				return (Integer) y.getL() - (Integer) x.getL() ; //Orden Ascendent
			}
		});
	}
	private void save_result_to_file_output() throws FileNotFoundException {
		
		PrintStream out;

		out = new PrintStream(new FileOutputStream("book_"+aux_ind+".dat",false));
		System.setOut(out);
		
		for(int i=0; i<Ordered_List.size(); i++) {
			//System.out.print(Ordered_List.get(i).getR());

			System.out.println(Math.log(i)  + " " + Math.log( Ordered_List.get(i).getL() ) );
			//System.out.println( Ordered_List.get(i).getR()  + " " +  Ordered_List.get(i).getL()  );
		}
		aux_ind++;
		Ordered_List = new ArrayList<>();
	}
	
	//this method will be used to "Tokenizing" our document text into terms by different conditions set by us 
	private void Tokenize_Doument() throws IOException, TikaException, SAXException{
		String temp="";
		 Integer default_value = 0;
		
		for(File f : Files_Array) {
		  
			this.Export_All_Links_From_Documents(f);
			this.Generate_Tika_Functions_Results(f);
			this.Get_Metadata(f);
	      file_in_string = tika.parseToString(f);//storing the file's text in 
	      fileScanner = new Scanner(file_in_string);//storing the string file into scanner variable for "tokenizing"
	      fileScanner.useDelimiter(delimeters);//assigning our Delimeters for the Scanner Delimeters
		  Words_Frequency = new HashMap<>();

		  while(fileScanner.hasNext()) { //iterating over the the "Tokens" extracted by Scanner file and its functions 
			  temp = fileScanner.next();
			 // System.out.println(temp);
		      default_value = Words_Frequency.getOrDefault(temp, 0);
		      if(default_value == 0) {
		    	  Words_Frequency.put(temp, 1);   
		      }
		      else {
		    	Words_Frequency.put(temp, ( default_value )+1);
		      }
		      //System.out.println(Words_Frequency.get(temp));

		  }
		  this.Oder_Terms_by_Frecuency();
		  this.save_result_to_file_output();
		  //Ordered_List = new ArrayList<>();

		}
	} 
	



	
	//Do_it_all a function which will be called ffrom the main to do all the required process
	public void Do_it_all(String Directory_path) throws IOException, TikaException, SAXException {
		this.Directory_String = Directory_path;
		this.Directory = new File(Directory_path);
		System.out.println(Directory_String);
		
		this.Store_Files_Path();
		this.Tokenize_Doument();
	}


//***
	
	
	public static void main(String[] args) throws Exception, IOException{
		Tika tika = new Tika();
		TikaClass example = new TikaClass();
		example.Do_it_all(args[0]);
		
	}
}
