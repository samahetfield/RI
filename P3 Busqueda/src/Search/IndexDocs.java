package Search;

public class IndexDocs {
    protected String Author;
    protected String Title;
    protected String Year;
    protected String Source_Title;
    protected String Link;
    protected String Resume;
    protected String AuthorKeywords;
    protected String IndexKeywords;
    protected String Eid;



    public IndexDocs(String author, String title, String year, String source_title, String link, String Aukey, String Indexkey){
        Author = author;
        Title = title;
        Year = year;
        Source_Title = source_title;
        Link = link;
       // Resume = resume;
        AuthorKeywords = Aukey;
        IndexKeywords = Indexkey;
       // Eid = eid;

    }
    public String getAuthor() {
        return Author;
    }

    public String getTitle() {
        return Title;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getYear() {
        return Year;
    }

    public void setYear(String year) {
        Year = year;
    }

    public String getSource_Title() {
        return Source_Title;
    }

    public void setSource_Title(String source_Title) {
        Source_Title = source_Title;
    }

    public String getLink() {
        return Link;
    }

    public void setLink(String link) {
        Link = link;
    }

    public String getAuthorKeywords() {
        return AuthorKeywords;
    }

    public void setAuthorKeywords(String authorKeywords) {
        AuthorKeywords = authorKeywords;
    }

    public String getIndexKeywords() {
        return IndexKeywords;
    }

    public void setIndexKeywords(String indexKeywords) {
        IndexKeywords = indexKeywords;
    }
}
