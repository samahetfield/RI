public class Pair<Integer,String> {
    private Integer l;
    private String r;
    public Pair(Integer l, String r){
        this.l = l;
        this.r = r;
    }
    public Integer getL(){ return l; }
    public String getR(){ return r; }
    public void setL(Integer l){ this.l = l; }
    public void setR(String r){ this.r = r; }

}
