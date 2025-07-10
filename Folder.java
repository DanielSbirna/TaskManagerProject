public class Folder {
    private String folderName;
    private int color;
    private static int components; // how many tasks has in it

    //Constructor with args + increments components to 1
    public Folder (String folderName, int color, int components){
        this.folderName = folderName;
        this.color = color;
        components++;
    }

    //Constructor with no args
    public Folder (){
        folderName = "Work";
        color = 2;
    }

    //Copy constructor
    public Folder(Folder other){
        this.folderName = other.folderName;
        this.color = other.color;
    }

    //Get
    public String getFolderName(String folderName){
        return folderName;
    }

    public int getColor(int color){
        return color;
    }

    public int getComponents(int components){
        return components;
    }

    //Set
    public void setFolderName(String folderName){
        this.folderName = folderName;
    }

    public void setColor(int color){
        this.color = color;
    }


}
