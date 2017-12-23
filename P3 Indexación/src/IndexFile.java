import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.UAX29URLEmailAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sergi on 17/11/2017.
 */
public class IndexFile{

    static HashMap<String, Analyzer> analyzers = new HashMap<>();
    static PerFieldAnalyzerWrapper analyzer = null;

    Directory dir = null;

    private DirectoryTaxonomyWriter taxoWriter;
    private IndexWriter writer;
    private FacetsConfig fconfig = new FacetsConfig();



    public static void main(String[] args) throws IOException, ParseException {
        System.out.println("Direccion de directorio de indexacion: (e.g. /tmp/index or c:\\temp\\index)");
        String indexLocation = null;
        BufferedReader br = new BufferedReader(
                new InputStreamReader(System.in));
        String s = br.readLine();

        System.out.println("Direccion de directorio de facetas: (e.g. /tmp/index or c:\\temp\\index)");
        String facetLocation = null;
        BufferedReader br2 = new BufferedReader(
                new InputStreamReader(System.in));
        String s2 = br2.readLine();
        IndexFile indexer = null;
            try {
                indexLocation = s;
                indexer = new IndexFile(s,s2);
        } catch (Exception ex) {
            System.out.println("No puedo crear indice..." + ex.getMessage());
            System.exit(-1);
        }

        //===================================================
        //read input from user until he enters q for quit
        //===================================================
        while (!s.equalsIgnoreCase("q")) {
            try {
                System.out.println("Fichero/Direcorio a incluir en el indice (q=quit): (e.g. /home/ron/mydir or c:\\Users\\ron\\mydir)");
                System.out.println("Tipo de ficheros aceptados: .csv]");
                s = br.readLine();
                if (s.equalsIgnoreCase("q")) {
                    break;
                }

                //try to add file into the index
                indexer.addFiles(new File(s));
            } catch (Exception e) {
                System.out.println("Error indexando " + s + " : " + e.getMessage());
            }

        }

        //===================================================
        //after adding, we always have to call the
        //closeIndex, otherwise the index is not created
        //===================================================
        indexer.closeIndex();

    }

    /**
     * Constructor
     * @param indexDir the name of the folder in which the index should be created
     * @throws java.io.IOException when exception creating index.
     */
    IndexFile(String indexDir, String facetDir) throws IOException {

        //Create all the analyzers for each field
        analyzers.put("author", new AuthorAnalyzer());  //This analyzer split from each character ","
        analyzers.put("content", new EnglishAnalyzer());
        analyzers.put("resume", new EnglishAnalyzer());
        analyzers.put("AuthorKeywords", new KeyAnalyzer()); //KeyAnalyzer split the tokens when find ";"
        analyzers.put("IndexKeywords", new KeyAnalyzer());
        analyzers.put("title", new SimpleAnalyzer());
        analyzers.put("source_title", new SimpleAnalyzer());

        analyzer = new PerFieldAnalyzerWrapper(new WhitespaceAnalyzer(), analyzers);

        Path p1 = Paths.get(indexDir);
        FSDirectory dir = FSDirectory.open(p1);
        Path p2 = Paths.get(facetDir);
        FSDirectory dirFacet = FSDirectory.open(p2);
        this.dir = dir;
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE); //Lo abrimos para crear
        writer = new IndexWriter(dir, config);

        fconfig.setMultiValued("Year", true);
        fconfig.setHierarchical("Year", true);
        fconfig.setMultiValued("Authors", true);
        fconfig.setMultiValued("Author Keywords", true);
        fconfig.setMultiValued("Index Keywords", true);

        taxoWriter = new DirectoryTaxonomyWriter(dirFacet);


    }


    private void addFiles(File file) throws IOException {
        if (!file.exists()) {
            System.out.println(file + " does not exist.");
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                addFiles(f);
            }
        } else {
            String cadena;
            Boolean cabeceras = true;
            String filename = file.getName().toLowerCase();
            FileReader fReader = new FileReader(file.toString());
            BufferedReader b = new BufferedReader(fReader);
            while((cadena = b.readLine())!=null) {
                if(cabeceras == false)
                    this.indexDoc(cadena, file);
                else
                    cabeceras = false;
            }
        }
    }

    private void indexDoc(String cadena, File file) throws IOException {
        FileReader fr = null;
        try {
            Document doc = new Document();
            fr = new FileReader(file);

            ArrayList<String> campos = this.getFields(cadena);

            // Index fields
            doc.add(new TextField("content", cadena, Field.Store.NO));
            doc.add(new TextField("author", campos.get(0), Field.Store.YES));
            doc.add(new TextField("title", campos.get(1) , Field.Store.YES));
            doc.add(new IntPoint("year", Integer.parseInt(campos.get(2))));
            doc.add(new StoredField("year_s", campos.get(2)));
            doc.add(new TextField("source_title", campos.get(3) , Field.Store.YES));
            doc.add(new StringField("link", campos.get(5), Field.Store.YES));
            doc.add(new TextField("resume", campos.get(6) , Field.Store.NO));
            doc.add(new TextField("AuthorKeywords", campos.get(7) , Field.Store.YES));
            doc.add(new TextField("IndexKeywords", campos.get(8) , Field.Store.YES));
            doc.add(new TextField("eid", campos.get(9) , Field.Store.NO));

            //Facets fields
            ArrayList<String> Authors = getAuthors(campos.get(0));
            ArrayList<String> AuthorKeys= getKeyWords(campos.get(7));
            ArrayList<String> IndexKeys = getKeyWords(campos.get(8));


            doc.add(new FacetField("Year", campos.get(2)));

            for(int i=0; i<Authors.size(); i++)
                doc.add(new FacetField("Authors", Authors.get(i)));

            for(int i=0; i<AuthorKeys.size(); i++)
                doc.add(new FacetField("Author Keywords", AuthorKeys.get(i)));

            for(int i=0; i<IndexKeys.size(); i++)
                doc.add(new FacetField("Index Keywords", IndexKeys.get(i)));

            writer.addDocument(fconfig.build(taxoWriter, doc));
        } catch (Exception e) {
            System.out.println("Could not add doc from: " + file);
        } finally {
            fr.close();
        }

    }


    /**
     * Close the index.
     * @throws java.io.IOException when exception closing
     */
    public void closeIndex() throws IOException, ParseException {
        writer.close();
        taxoWriter.close();
    }



    /*
        This method receive a String and separate the fields of this String

        For each field, the method identificate if begin with (") or not
        If this field begin with ("), iterate until it find other (") and the next character
        is a ",", beacause, it could appear the character (") in the text.
        If the field doesn't begin with ("), the method should find the next character ",".

     */
    private ArrayList getFields(String cadena) {
        ArrayList<String> fields = new ArrayList<>();

        int i=0, j=0;
        boolean no_cambio = false;
        while(i < cadena.length()){
            if(String.valueOf(cadena.charAt(i)).equals("\"") ){
                i++;
                if(!no_cambio)
                    j=i;
                while(!String.valueOf(cadena.charAt(i)).equals("\"")){
                    i++;
                }

                String a = String.valueOf(cadena.charAt(i+1));
                if(String.valueOf(cadena.charAt(i+1)).equals(",")) {
                    fields.add(cadena.substring(j, i));
                    i += 2;
                    no_cambio=false;
                }
                else{
                    no_cambio=true;
                }
            }
            else{
                if(fields.size() != 9) {
                    j = i;
                    while (!String.valueOf(cadena.charAt(i)).equals(",")) {
                        i++;
                    }
                    fields.add(cadena.substring(j, i));
                    i++;
                }
                else{ //This is for the last field
                    j=i;
                    while(i<cadena.length()){
                        i++;
                    }
                    fields.add(cadena.substring(j, i));
                    i++;
                }

            }
        }

        return fields;

    }

    private ArrayList getKeyWords(String s){
        ArrayList<String> array = new ArrayList<>();

        String aux="";

        if(s.length() > 0) {
            for (int i = 0; i < s.length(); i++) {
                if (!String.valueOf(s.charAt(i)).equals(";")) {
                    aux += String.valueOf(s.charAt(i));
                } else {
                    array.add(aux.trim().toLowerCase());
                    aux = "";
                }
            }
            array.add(aux.trim().toLowerCase());
        }
        else{
            array.add("no-keys");
        }

        return array;

    }


    private ArrayList getAuthors(String s){
        ArrayList array = new ArrayList();

        String aux = "";

        for(int i=0; i<s.length(); i++){
            if(!String.valueOf(s.charAt(i)).equals(",")){
                aux += String.valueOf(s.charAt(i));
            }
            else{
                array.add(aux.trim().toLowerCase());
                aux = "";
            }
        }

        array.add(aux.trim().toLowerCase());

        return array;
    }

}
