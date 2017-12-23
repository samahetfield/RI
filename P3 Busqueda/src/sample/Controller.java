package sample;


import Search.*;
import Search.AuthorAnalyzer;
import Search.KeyAnalyzer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.facet.*;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * FXML Controller class
 *
 * @author sergi
 */
public class Controller {

    /**
     * Initializes the controller class.
     */
    @FXML
    private TextField textfield;
    @FXML
    private TextField textfieldbool;
    @FXML
    private TextField textfieldint;
    @FXML
    private ComboBox fieldbox;
    @FXML
    private ComboBox fieldboxbool;
    @FXML
    private ComboBox fieldboxint;
    @FXML
    private javafx.scene.control.TableView tabledocs;
    @FXML
    private TableColumn<String, String> author_col;
    @FXML
    private TableColumn<String, String> title_col;
    @FXML
    private TableColumn<String, String> year_col;
    @FXML
    private TableColumn<String, String> source_col;
    @FXML
    private TableColumn<String, String> link_col;
    @FXML
    private TableColumn<String, String> authorkey_col;
    @FXML
    private TableColumn<String, String> indexkey_col;
    @FXML
    private ComboBox AuthorComboBox;
    @FXML
    private ComboBox YearComboBox;
    @FXML
    private ComboBox AuKeyComboBox;
    @FXML
    private ComboBox InKeyComboBox;
    @FXML
    private Label totaldocs;

    private String search;
    private String field_select;
    private String field;

    private FacetsConfig fconfig = new FacetsConfig();
    BooleanQuery bq;
    final ObservableList<IndexDocs> data = FXCollections.observableArrayList();


    private String index_path="/home/sergio/RI/indices";
    private String facets_path = "/home/sergio/RI/indices/facetas";




    @FXML
    void btn1handle(ActionEvent event) throws IOException {
        tabledocs.setEditable(true);

        //Obtenemos el id del botón que ha lanzado el evento
        String clicked = ((Control)event.getSource()).getId();

        author_col.setCellValueFactory(new PropertyValueFactory<>("Author"));
        title_col.setCellValueFactory(new PropertyValueFactory<>("Title"));
        year_col.setCellValueFactory(new PropertyValueFactory<>("Year"));
        source_col.setCellValueFactory(new PropertyValueFactory<>("Source_Title"));
        link_col.setCellValueFactory(new PropertyValueFactory<>("Link"));
        authorkey_col.setCellValueFactory(new PropertyValueFactory<>("AuthorKeywords"));
        indexkey_col.setCellValueFactory(new PropertyValueFactory<>("IndexKeywords"));


        //Dependiendo del botón que haya sido clickado realizaremos una función determinada

        if(clicked.equals("btn")) {

            search = textfield.getText();
            if (fieldbox.getValue() == null)
                field_select = "All";
            else
                field_select = fieldbox.getValue().toString();

            doSearch();
        }else if(clicked.equals("btnbool")) {
            search = textfieldbool.getText();
            if (fieldboxbool.getValue() == null)
                field_select = "All";
            else
                field_select = fieldboxbool.getValue().toString();
            doSearchBoolean();
        }else {
            search = textfieldint.getText();
            if (fieldboxint.getValue() == null)
                field_select = "All";
            else
                field_select = fieldboxint.getValue().toString();
            doIntSearch();
        }
    }

    private void doSearchBoolean() throws IOException {
        Analyzer analyzer = null;
        String search_aux = "";
        List<String> result = new ArrayList<>();
        ArrayList<IndexDocs> array = new ArrayList<>();
        ArrayList<String> booleans = new ArrayList<>();



        /*
            Dependiendo del campo sobre el que queramos buscar, tendremos un analizador diferente, que tratará los OR, AND y NOT
            de una determinada manera.
            Es decir, por ejemplo el WhiteSpaceAnalyzer cogerá los OR como un token mientras que SimpleAnalyzer no,
            por ello, tenemos que ir diferenciando el analizador que se utiliza para tratar la consulta de determinadas formas.
         */
        switch (field_select){
            case "Author":
                //Obtenemos una consulta auxiliar que nos tenga los tokens separados por ","
                search_aux = trasformsearch(search);
                //Obtenemos los operadores booleanos de la consulta
                booleans = getoperators(search);
                analyzer = new AuthorAnalyzer();
                field = "author";

                try {
                    TokenStream stream  = analyzer.tokenStream(null,  new StringReader(search_aux));
                    OffsetAttribute offsetAtt = stream.addAttribute(OffsetAttribute.class);
                    CharTermAttribute cAtt= stream.addAttribute(CharTermAttribute.class);
                    stream.reset();

                    while (stream.incrementToken()) {
                        //cad = stream.getAttribute(CharTermAttribute.class).toString();!+
                        result.add( cAtt.toString()); // +" : ("+ offsetAtt.startOffset()+"," + offsetAtt.endOffset()+")");
                    }
                    stream.end();
                } catch (IOException e) {
                    // not thrown b/c we're using a string reader...
                    throw new RuntimeException(e);
                }
                break;
            case "Title":
                analyzer = new SimpleAnalyzer();
                field = "title";

                try {
                    TokenStream stream  = analyzer.tokenStream(null,  new StringReader(search));
                    OffsetAttribute offsetAtt = stream.addAttribute(OffsetAttribute.class);
                    CharTermAttribute cAtt= stream.addAttribute(CharTermAttribute.class);
                    stream.reset();
                    String cad ="";
                    Integer offsetfinal = 0;
                    //En esta consulta debemos ir comprobando los offSet para saber si entre cada token existe algun operador
                    while (stream.incrementToken()) {

                        if(cAtt.toString().toLowerCase().equals("and")){
                            booleans.add("AND");
                            result.add(cad);
                            cad = "";
                        }
                        else if(cAtt.toString().toLowerCase().equals("or")){
                            booleans.add("OR");
                            result.add(cad);
                            cad = "";
                        }
                        else if(cAtt.toString().toLowerCase().equals("not")){
                            booleans.add("NOT");
                            result.add(cad);
                            cad = "";
                        }
                        else{
                            cad += cAtt.toString()+" ";
                        }

                    }

                    result.add(cad);
                    stream.end();
                } catch (IOException e) {
                    // not thrown b/c we're using a string reader...
                    throw new RuntimeException(e);
                }


                break;
            case "Year":
                analyzer = new WhitespaceAnalyzer();
                field = "year";

                //Comprobamos los tokens y si son operadores booleanos no se introducen
                try {
                    TokenStream stream  = analyzer.tokenStream(null,  new StringReader(search));
                    OffsetAttribute offsetAtt = stream.addAttribute(OffsetAttribute.class);
                    CharTermAttribute cAtt= stream.addAttribute(CharTermAttribute.class);
                    stream.reset();
                    String cad ="";
                    Integer offsetfinal = 0;
                    while (stream.incrementToken()) {

                        if(cAtt.toString().toLowerCase().equals("and")){
                            booleans.add("AND");
                            result.add(cad);
                            cad = "";
                        }
                        else if(cAtt.toString().toLowerCase().equals("or")){
                            booleans.add("OR");
                            result.add(cad);
                            cad = "";
                        }
                        else if(cAtt.toString().toLowerCase().equals("not")){
                            booleans.add("NOT");
                            result.add(cad);
                            cad = "";
                        }
                        else{
                            cad += cAtt.toString()+" ";
                        }

                    }

                    result.add(cad);
                    stream.end();
                } catch (IOException e) {
                    // not thrown b/c we're using a string reader...
                    throw new RuntimeException(e);
                }


                break;
            case "Source Title":
                analyzer = new SimpleAnalyzer();
                field = "source_title";

                try {
                    TokenStream stream  = analyzer.tokenStream(null,  new StringReader(search));
                    OffsetAttribute offsetAtt = stream.addAttribute(OffsetAttribute.class);
                    CharTermAttribute cAtt= stream.addAttribute(CharTermAttribute.class);
                    stream.reset();
                    String cad ="";
                    Integer offsetfinal = 0;
                    while (stream.incrementToken()) {

                        if(cAtt.toString().toLowerCase().equals("and")){
                            booleans.add("AND");
                            result.add(cad);
                            cad = "";
                        }
                        else if(cAtt.toString().toLowerCase().equals("or")){
                            booleans.add("OR");
                            result.add(cad);
                            cad = "";
                        }
                        else if(cAtt.toString().toLowerCase().equals("not")){
                            booleans.add("NOT");
                            result.add(cad);
                            cad = "";
                        }
                        else{
                            cad += cAtt.toString()+" ";
                        }

                    }

                    result.add(cad);
                    stream.end();
                } catch (IOException e) {
                    // not thrown b/c we're using a string reader...
                    throw new RuntimeException(e);
                }


                break;
            case "Link":
                analyzer = new WhitespaceAnalyzer();
                field = "link";


                try {
                    TokenStream stream  = analyzer.tokenStream(null,  new StringReader(search));
                    OffsetAttribute offsetAtt = stream.addAttribute(OffsetAttribute.class);
                    CharTermAttribute cAtt= stream.addAttribute(CharTermAttribute.class);
                    stream.reset();
                    String cad ="";
                    Integer offsetfinal = 0;
                    while (stream.incrementToken()) {

                        if(cAtt.toString().toLowerCase().equals("and")){
                            booleans.add("AND");
                            result.add(cad);
                            cad = "";
                        }
                        else if(cAtt.toString().toLowerCase().equals("or")){
                            booleans.add("OR");
                            result.add(cad);
                            cad = "";
                        }
                        else if(cAtt.toString().toLowerCase().equals("not")){
                            booleans.add("NOT");
                            result.add(cad);
                            cad = "";
                        }
                        else{
                            cad += cAtt.toString()+" ";
                        }

                    }

                    result.add(cad);
                    stream.end();
                } catch (IOException e) {
                    // not thrown b/c we're using a string reader...
                    throw new RuntimeException(e);
                }


                break;
            case "Resume":
                analyzer = new EnglishAnalyzer();
                field = "resume";

                try {
                    TokenStream stream  = analyzer.tokenStream(null,  new StringReader(search));
                    OffsetAttribute offsetAtt = stream.addAttribute(OffsetAttribute.class);
                    CharTermAttribute cAtt= stream.addAttribute(CharTermAttribute.class);
                    stream.reset();
                    String cad ="";
                    Integer offsetfinal = 0;
                    while (stream.incrementToken()) {
                        if(offsetAtt.startOffset() != 0){
                            cad = search.substring(offsetfinal, offsetAtt.startOffset());

                            cad = cad.trim();

                            if(cad.trim().contains(" ")){
                                Integer i1 = cad.indexOf(" ");
                                Integer i2 = cad.length();
                                cad = cad.substring(cad.indexOf(" "), cad.length());
                                cad = cad.trim();
                            }

                        }
                        else{
                            booleans.add("AND");
                        }

                        if(cad.equals("and") || cad.equals("AND")){
                            booleans.add("AND");
                        }
                        else if(cad.equals("or") || cad.equals("OR")){
                            booleans.add("OR");
                        }
                        else if(cad.equals("not") || cad.equals("NOT")){
                            booleans.add("NOT");
                        }

                        //cad = stream.getAttribute(CharTermAttribute.class).toString();!+
                        result.add( cAtt.toString()); // +" : ("+ offsetAtt.startOffset()+"," + offsetAtt.endOffset()+")");
                        offsetfinal = offsetAtt.endOffset();
                    }
                    stream.end();
                } catch (IOException e) {
                    // not thrown b/c we're using a string reader...
                    throw new RuntimeException(e);
                }



                break;
            case "AuthorKeywords":
                analyzer = new KeyAnalyzer();
                field = "AuthorKeywords";

                search_aux = trasformsearchKey(search);
                booleans = getoperators(search);

                try {
                    TokenStream stream  = analyzer.tokenStream(null,  new StringReader(search_aux));
                    OffsetAttribute offsetAtt = stream.addAttribute(OffsetAttribute.class);
                    CharTermAttribute cAtt= stream.addAttribute(CharTermAttribute.class);
                    stream.reset();

                    while (stream.incrementToken()) {
                        //cad = stream.getAttribute(CharTermAttribute.class).toString();!+
                        result.add( cAtt.toString()); // +" : ("+ offsetAtt.startOffset()+"," + offsetAtt.endOffset()+")");
                    }
                    stream.end();
                } catch (IOException e) {
                    // not thrown b/c we're using a string reader...
                    throw new RuntimeException(e);
                }

                break;
            case "IndexKeywords":
                analyzer = new KeyAnalyzer();
                field = "IndexKeywords";

                search_aux = trasformsearchKey(search);
                booleans = getoperators(search);

                try {
                    TokenStream stream  = analyzer.tokenStream(null,  new StringReader(search_aux));
                    OffsetAttribute offsetAtt = stream.addAttribute(OffsetAttribute.class);
                    CharTermAttribute cAtt= stream.addAttribute(CharTermAttribute.class);
                    stream.reset();

                    while (stream.incrementToken()) {
                        //cad = stream.getAttribute(CharTermAttribute.class).toString();!+
                        result.add( cAtt.toString()); // +" : ("+ offsetAtt.startOffset()+"," + offsetAtt.endOffset()+")");
                    }
                    stream.end();
                } catch (IOException e) {
                    // not thrown b/c we're using a string reader...
                    throw new RuntimeException(e);
                }


                break;
            case "EID":
                analyzer = new WhitespaceAnalyzer();
                field = "eid";


                try {
                    TokenStream stream  = analyzer.tokenStream(null,  new StringReader(search));
                    OffsetAttribute offsetAtt = stream.addAttribute(OffsetAttribute.class);
                    CharTermAttribute cAtt= stream.addAttribute(CharTermAttribute.class);
                    stream.reset();
                    String cad ="";
                    Integer offsetfinal = 0;
                    while (stream.incrementToken()) {

                        if(cAtt.toString().toLowerCase().equals("and")){
                            booleans.add("AND");
                            result.add(cad);
                            cad = "";
                        }
                        else if(cAtt.toString().toLowerCase().equals("or")){
                            booleans.add("OR");
                            result.add(cad);
                            cad = "";
                        }
                        else if(cAtt.toString().toLowerCase().equals("not")){
                            booleans.add("NOT");
                            result.add(cad);
                            cad = "";
                        }
                        else{
                            cad += cAtt.toString()+" ";
                        }

                    }

                    result.add(cad);
                    stream.end();
                } catch (IOException e) {
                    // not thrown b/c we're using a string reader...
                    throw new RuntimeException(e);
                }



                break;
            case "All":
                analyzer = new EnglishAnalyzer();
                field = "content";


                try {
                    TokenStream stream  = analyzer.tokenStream(null,  new StringReader(search));
                    OffsetAttribute offsetAtt = stream.addAttribute(OffsetAttribute.class);
                    CharTermAttribute cAtt= stream.addAttribute(CharTermAttribute.class);
                    stream.reset();
                    String cad ="";
                    Integer offsetfinal = 0;
                    while (stream.incrementToken()) {
                        if(offsetAtt.startOffset() != 0){
                            cad = search.substring(offsetfinal, offsetAtt.startOffset());

                            cad = cad.trim();

                            if(cad.trim().contains(" ")){
                                Integer i1 = cad.indexOf(" ");
                                Integer i2 = cad.length();
                                cad = cad.substring(cad.indexOf(" "), cad.length());
                                cad = cad.trim();
                            }

                        }
                        else{
                            booleans.add("AND");
                        }

                        if(cad.equals("and") || cad.equals("AND")){
                            booleans.add("AND");
                        }
                        else if(cad.equals("or") || cad.equals("OR")){
                            booleans.add("OR");
                        }
                        else if(cad.equals("not") || cad.equals("NOT")){
                            booleans.add("NOT");
                        }

                        //cad = stream.getAttribute(CharTermAttribute.class).toString();!+
                        result.add( cAtt.toString()); // +" : ("+ offsetAtt.startOffset()+"," + offsetAtt.endOffset()+")");
                        offsetfinal = offsetAtt.endOffset();
                    }
                    stream.end();
                } catch (IOException e) {
                    // not thrown b/c we're using a string reader...
                    throw new RuntimeException(e);
                }


                break;
        }


        //Con la consulta tokenizada, pasamos a realizar las consultas
        try {
            Path p1 = Paths.get(index_path);
            FSDirectory dir = FSDirectory.open(p1);
            IndexReader ireader = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(ireader);

            BooleanQuery.Builder bqbuilder = new BooleanQuery.Builder();

            //Como tenemos almacenados los operadores utilizados, dependiendo del operador que estemos leyendo
            // usaremos un BooleanQuery.Occur diferente
            for(int i=0; i<result.size(); i++) {
                TermQuery q1 = new TermQuery(new Term(field, result.get(i).trim()));
                BooleanClause bc1;

                if(i == 0){
                    bc1 = new BooleanClause(q1, BooleanClause.Occur.MUST);
                }
                else {
                    if ((booleans.get(i-1)).equals("AND"))
                        bc1 = new BooleanClause(q1, BooleanClause.Occur.MUST);
                    else if ((booleans.get(i-1)).equals("OR"))
                        bc1 = new BooleanClause(q1, BooleanClause.Occur.SHOULD);
                    else
                        bc1 = new BooleanClause(q1, BooleanClause.Occur.MUST_NOT);
                }

                bqbuilder.add(bc1);

            }
            BooleanQuery bq = bqbuilder.build();
            this.bq = bq;

            TopDocs tdocs = searcher.search(bq, 50);

            System.out.println("Hay "+tdocs.totalHits+" docs");
            totaldocs.setText("Hay "+tdocs.totalHits+" documentos");

            ScoreDoc[] hits = tdocs.scoreDocs;

            data.clear();

            for(int i=0; i<hits.length; i++){
                org.apache.lucene.document.Document hitDoc = searcher.doc(hits[i].doc);

                data.add(new IndexDocs(hitDoc.get("author").toString(),
                        hitDoc.get("title").toString(),
                        hitDoc.get("year_s").toString(),
                        hitDoc.get("source_title").toString(),
                        hitDoc.get("link").toString(),
                        hitDoc.get("AuthorKeywords").toString(),
                        hitDoc.get("IndexKeywords").toString()));

                //System.out.println(hitDoc.get("title").toString());
            }

            tabledocs.setItems(data);

            addFacets(bq);

        }
        catch (IndexNotFoundException e){
            System.out.println("No se ha podido encontrar el índice en el directorio indicado");
        }



    }

    private void doSearch() throws IOException {
        Analyzer analyzer = null;
        List<String> result = new ArrayList<>();
        ArrayList<IndexDocs> array = new ArrayList<>();

        switch (field_select){
            case "Author":
                analyzer = new AuthorAnalyzer();
                field = "author";
                break;
            case "Title":
                analyzer = new SimpleAnalyzer();
                field = "title";
                break;
            case "Year":
                analyzer = new WhitespaceAnalyzer();
                field = "year";
                break;
            case "Source Title":
                analyzer = new SimpleAnalyzer();
                field = "source_title";
                break;
            case "Link":
                analyzer = new WhitespaceAnalyzer();
                field = "link";
                break;
            case "Resume":
                analyzer = new EnglishAnalyzer();
                field = "resume";
                break;
            case "AuthorKeywords":
                analyzer = new KeyAnalyzer();
                field = "AuthorKeywords";
                break;
            case "IndexKeywords":
                analyzer = new KeyAnalyzer();
                field = "IndexKeywords";
                break;
            case "EID":
                analyzer = new WhitespaceAnalyzer();
                field = "eid";
                break;
            case "All":
                analyzer = new EnglishAnalyzer();
                field = "content";
                break;
        }


        try {
            TokenStream stream  = analyzer.tokenStream(null,  new StringReader(search));
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

        try {
            Path p1 = Paths.get(index_path);
            FSDirectory dir = FSDirectory.open(p1);
            IndexReader ireader = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(ireader);


            //Al ser una consulta libre, lo que hacemos es que todos los tokens deben aparecer

            BooleanQuery.Builder bqbuilder = new BooleanQuery.Builder();
            for(int i=0; i<result.size(); i++) {
                TermQuery q1 = new TermQuery(new Term(field, result.get(i)));
                BooleanClause bc1 = new BooleanClause(q1, BooleanClause.Occur.MUST);

                bqbuilder.add(bc1);

            }
            BooleanQuery bq = bqbuilder.build();
            this.bq = bq;

            TopDocs tdocs = searcher.search(bq, 50);

            System.out.println("Hay "+tdocs.totalHits+" docs");
            totaldocs.setText("Hay "+tdocs.totalHits+" documentos");


            ScoreDoc[] hits = tdocs.scoreDocs;

            data.clear();

            for(int i=0; i<hits.length; i++){
                org.apache.lucene.document.Document hitDoc = searcher.doc(hits[i].doc);

                data.add(new IndexDocs(hitDoc.get("author").toString(),
                        hitDoc.get("title").toString(),
                        hitDoc.get("year_s").toString(),
                        hitDoc.get("source_title").toString(),
                        hitDoc.get("link").toString(),
                        hitDoc.get("AuthorKeywords").toString(),
                        hitDoc.get("IndexKeywords").toString()));

                //System.out.println(hitDoc.get("title").toString());
            }

            tabledocs.setItems(data);

            addFacets(bq);

        }
        catch (IndexNotFoundException e){
            System.out.println("No se ha podido encontrar el índice en el directorio indicado");
        }
    }

    private void doIntSearch() throws IOException {
        Analyzer analyzer = null;
        List<String> result = new ArrayList<>();
        ArrayList<IndexDocs> array = new ArrayList<>();


        /*
        * Como podemos ver, para hacer la búsqueda por rango debemos separar los dos extremos por ";"
        * Ya que estamos haciendo uso del KeyAnalyzer()
        * */
        switch (field_select){
            case "Year":
                analyzer = new KeyAnalyzer();
                field = "year";
                break;
            case "All":
                analyzer = new KeyAnalyzer();
                field = "year";
                break;
        }

        try {
            TokenStream stream  = analyzer.tokenStream(null,  new StringReader(search));
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

        try {
            Path p1 = Paths.get(index_path);
            FSDirectory dir = FSDirectory.open(p1);
            IndexReader ireader = DirectoryReader.open(dir);
            IndexSearcher searcher = new IndexSearcher(ireader);
            Integer startpoint = Integer.valueOf(result.get(0));
            Integer endpoint = Integer.valueOf(result.get(result.size()-1));

            if(startpoint > endpoint){
                Integer aux = startpoint;
                startpoint = endpoint;
                endpoint = aux;
            }

            Query bq = IntPoint.newRangeQuery(field, startpoint, endpoint);


            TopDocs tdocs = searcher.search(bq, 50);

            System.out.println("Hay "+tdocs.totalHits+" docs");
            totaldocs.setText("Hay "+tdocs.totalHits+" documentos");


            ScoreDoc[] hits = tdocs.scoreDocs;

            data.clear();

            for(int i=0; i<hits.length; i++){
                org.apache.lucene.document.Document hitDoc = searcher.doc(hits[i].doc);

                data.add(new IndexDocs(hitDoc.get("author").toString(),
                        hitDoc.get("title").toString(),
                        hitDoc.get("year_s").toString(),
                        hitDoc.get("source_title").toString(),
                        hitDoc.get("link").toString(),
                        hitDoc.get("AuthorKeywords").toString(),
                        hitDoc.get("IndexKeywords").toString()));

                //System.out.println(hitDoc.get("title").toString());
            }

            tabledocs.setItems(data);


        }
        catch (IndexNotFoundException e){
            System.out.println("No se ha podido encontrar el índice en el directorio indicado");
        }
    }




    /*
    *  Con la consulta construida y realizada, pasamos a buscar las facetas de esa consulta
    * */
    private void addFacets(BooleanQuery bq) throws IOException {
        Path p = Paths.get(index_path);
        FSDirectory indexDir = FSDirectory.open(p);
        DirectoryReader indexReader = DirectoryReader.open(indexDir);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        Path p1 = Paths.get(facets_path);
        FSDirectory taxoDir = FSDirectory.open(p1);
        TaxonomyReader taxoReader = new DirectoryTaxonomyReader(taxoDir);
        fconfig.setMultiValued("Year", true);
        fconfig.setHierarchical("Year", true);
        fconfig.setMultiValued("Authors", true);
        fconfig.setMultiValued("Author Keywords", true);
        fconfig.setMultiValued("Index Keywords", true);


        //Creamos FacetsCollector que encontrará las facetas de la búsqueda
        FacetsCollector fc = new FacetsCollector();
        TopDocs tdc = FacetsCollector.search(searcher, bq, 10, fc);

        for (ScoreDoc sd : tdc.scoreDocs){
            Document d = searcher.doc(sd.doc);
            //System.out.println(sd.score + d.get("title"));
        }


        Facets facetas = new FastTaxonomyFacetCounts(taxoReader, fconfig, fc);

        List<FacetResult> TodasDims = facetas.getAllDims(100);
        //System.out.println("Categorias totales " + TodasDims.size());

        AuKeyComboBox.getItems().clear();
        AuthorComboBox.getItems().clear();
        InKeyComboBox.getItems().clear();
        YearComboBox.getItems().clear();

        //Cada una de las dimensiones de la faceta se almacenará en un combobox que nos permitirá el filtrado posterior
        for(FacetResult fr : TodasDims){
            //System.out.println("Categoria " + fr.dim);
            LabelAndValue[] lv = fr.labelValues;

            switch (fr.dim){
                case "Author Keywords":
                    for(int i=0; (i<20 && i< fr.labelValues.length); i++){
                        AuKeyComboBox.getItems().add(lv[i].label);
                    }
                    break;
                case "Authors":
                    for(int i=0; (i<20 && i< fr.labelValues.length); i++){
                        AuthorComboBox.getItems().add(lv[i].label);
                    }
                    break;
                case "Index Keywords":
                    for(int i=0; (i<20 && i< fr.labelValues.length); i++){
                        InKeyComboBox.getItems().add(lv[i].label);
                    }
                    break;
                case "Year":
                    for(int i=0; (i<20 && i< fr.labelValues.length); i++){
                        YearComboBox.getItems().add(lv[i].label);
                    }
                    break;
            }
        }

        FacetResult fresult = facetas.getTopChildren(10, "title");

    }



    public void filterAction() throws IOException {
        Path p = Paths.get(index_path);
        FSDirectory indexDir = FSDirectory.open(p);
        DirectoryReader indexReader = DirectoryReader.open(indexDir);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        Path p1 = Paths.get(facets_path);
        FSDirectory taxoDir = FSDirectory.open(p1);
        TaxonomyReader taxoReader = new DirectoryTaxonomyReader(taxoDir);

        DrillDownQuery dq = new DrillDownQuery(fconfig, bq);

        //Obtenemos las facetas seleccionadas de cada categoría

        if(AuthorComboBox.getValue() != null){
            dq.add("Authors", AuthorComboBox.getValue().toString());
        }
        if(AuKeyComboBox.getValue() != null){
            dq.add("Author Keywords", AuKeyComboBox.getValue().toString());
        }
        if(InKeyComboBox.getValue() != null){
            dq.add("Index Keywords", InKeyComboBox.getValue().toString());
        }
        if(YearComboBox.getValue() != null){
            dq.add("Year", YearComboBox.getValue().toString());
        }


        // Con las facetas seleccionadas y almacenadas en DrillDownQuery filtramos los resultados
        DrillSideways ds = new DrillSideways(searcher, fconfig, taxoReader);
        DrillSideways.DrillSidewaysResult dsresult = ds.search(dq, 100);
        //System.out.println("dsw hits "+dsresult.hits.totalHits);
        //System.out.println(dsresult.facets.getAllDims(100).toString());

        data.clear();
        for(ScoreDoc scoreDoc:dsresult.hits.scoreDocs){
            //Document doc = searcher.doc(scoreDoc.doc);
            //System.out.println("    Docs Score -> " + scoreDoc.score + "::" + doc.get("title"));

            org.apache.lucene.document.Document hitDoc = searcher.doc(scoreDoc.doc);
            data.add(new IndexDocs(hitDoc.get("author").toString(),
                    hitDoc.get("title").toString(),
                    hitDoc.get("year_s").toString(),
                    hitDoc.get("source_title").toString(),
                    hitDoc.get("link").toString(),
                    hitDoc.get("AuthorKeywords").toString(),
                    hitDoc.get("IndexKeywords").toString()));

        }

        tabledocs.setItems(data);
        addFacets(bq);

    }


    // Función que nos permitirá transformar la consulta que escriba el usuario, de forma que cada vez que encuentre
    // un operador booleano significará quetodo lo anterior era un token y por lo tanto introducirá un ";" para separar
    // dichos tokens
    private String trasformsearchKey(String search){
        String aux = "";
        String[] tokens = search.split(" ");
        ArrayList<String> search_good = new ArrayList();
        int ultimo = 0;

        for(int i = ultimo; i<tokens.length; i++){
            String word = tokens[i];
            if(word.toLowerCase().equals("or") || word.toLowerCase().equals("and") || word.toLowerCase().equals("not")){
                String token = "";
                for(int j=ultimo; j<i; j++){
                    token += " " + tokens[j];
                }

                token += ";";
                search_good.add(token);
                ultimo = i+1;
            }
        }

        String token = "";
        for (int i=ultimo; i<tokens.length; i++){
            token += " " + tokens[i];

        }
        token += ";";
        search_good.add(token);

        for(int i=0; i<search_good.size(); i++){
            aux += search_good.get(i);
        }

        return aux;

    }

    // Función que nos permitirá transformar la consulta que escriba el usuario, de forma que cada vez que encuentre
    // un operador booleano significará quetodo lo anterior era un token y por lo tanto introducirá una "," para separar
    // dichos tokens
    private String trasformsearch(String search){
        String aux = "";
        String[] tokens = search.split(" ");
        ArrayList<String> search_good = new ArrayList();
        int ultimo = 0;

        for(int i = ultimo; i<tokens.length; i++){
            String word = tokens[i];
            if(word.toLowerCase().equals("or") || word.toLowerCase().equals("and") || word.toLowerCase().equals("not")){
                String token = "";
                for(int j=ultimo; j<i; j++){
                    token += " " + tokens[j];
                }

                token += ",";
                search_good.add(token);
                ultimo = i+1;
            }
        }

        String token = "";
        for (int i=ultimo; i<tokens.length; i++){
            token += " " + tokens[i];

        }
        token += ",";
        search_good.add(token);

        for(int i=0; i<search_good.size(); i++){
            aux += search_good.get(i);
        }

        return aux;

    }


    //Recorremos el String de la consulta para obtener cada uno de los operadores booleanos que se introduzcan
    private ArrayList getoperators(String search){
        ArrayList<String> salida = new ArrayList<>();
        String[] tokens = search.split(" ");

        for(int i = 0; i<tokens.length; i++){
            String word = tokens[i];
            if(word.toLowerCase().equals("or") || word.toLowerCase().equals("and") || word.toLowerCase().equals("not")){
                salida.add(word.toUpperCase());
            }
        }

        return salida;

    }


}