public class Folder {
    private long id;
    private String folderName;
    private static int components; // how many tasks has in it

    // Constructor with args including ID
    public Folder (long id, String folderName, int components){
        this.id = id;
        this.folderName = folderName;
        this.components = components;
    }

    // Constructor with args (for new folders before DB assignment)
    public Folder (String folderName, int components){
        this.folderName = folderName;
        this.components = components;
    }

    //Constructor with no args
    public Folder (){
        id = -1; // Default invalid ID
        folderName = "Unnamed";
        components = 0;
    }

    //Copy constructor
    public Folder(Folder other){
        this.id = other.id;
        this.folderName = other.folderName;
        this.components = other.components;
    }

    //Get
    public long getId() {
        return id;
    }
    
    public String getFolderName(){
        return folderName;
    }

    public int getComponents(){
        return components;
    }

    //Set
     public void setId(long id) {
        this.id = id;
    }
    
    public void setFolderName(String folderName){
        this.folderName = folderName;
    }

    public void setComponents(int components) {
        this.components = components;
    }
}
