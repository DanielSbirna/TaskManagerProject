public class Folder {
    private int id;
    private String folderName;
    private static int componentsCount; // how many tasks has in it
    private int userId;

    // Constructor with args 
    public Folder (int id, String folderName, int userId, int componentsCount){
        this.id = id;
        this.folderName = folderName;
        this.userId = userId;
        this.componentsCount = componentsCount;
    }

    //Constructor with no args
    public Folder () {
        id = -1;
        folderName = "Unnamed";
        userId = -1;
        componentsCount = 0;
    }

    //Copy constructor
    public Folder (Folder other) {
        this.id = other.id;
        this.folderName = other.folderName;
        this.userId = other.userId;
        this.componentsCount = other.componentsCount;
    }

    //Get
    public int getId() {
        return id;
    }
    
    public String getFolderName(){
        return folderName;
    }

    public int getComponentsCount(){
        return componentsCount;
    }

    //Set
     public void setId(int id) {
        this.id = id;
    }
    
    public void setFolderName(String folderName){
        this.folderName = folderName;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setComponents(int components) {
        this.components = components;
    }
}
