public class Task {
    private int id;
    private String title;
    private String description;
    private String dueDate;
    private String dueTime;
    private boolean isDone;
    private int userId;
    private Long folderId; // folderId is Long - warper class for long - so it can be also 'null'

    // Constructor with args with ID
    public Task (int id, String title, String description, String dueDate, String dueTime, boolean isDone, int userId, Long folderId){
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.isDone = isDone;
        this.userId = userId;
        this.folderId = folderId;
    }

    // Constructor with args and no ID
    public Task(String title, String description, String dueDate, String dueTime, boolean isDone, int userId, Long folderId) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.isDone = isDone;
        this.userId = userId;
        this.folderId = folderId;
    }

    //Copy connstrctor
    public Task (Task other){
        this.id = other.id;
        this.title = other.title;
        this.description = other.description;
        this.dueDate = other.dueDate;
        this.dueTime = other.dueTime;
        this.isDone = other.isDone;
        this.userId = other.userId;
        this.folderId = other.folderId;
    }

    //Get
    public long getId() {
        return id;
    }
    
    public String getTitle(){
        return title;
    }

    public String getDescription (){
        return description;
    }

    public String getDueDate (){
        return dueDate;
    }

    public String getDueTime (){
        return dueTime;
    }

    public boolean getIsDone (){
        return isDone;
    }

    //Set
    public void setId(int id) {
        this.id = id;
    }
    
    public void setTitle (String title){
        this.title = title;
    }

    public void setDescription (String description){
        this.description = description;
    }

    public void setDueDate (String dueDate){
        this.dueDate = dueDate;
    }

    public void setDueTime (String dueTime){
        this.dueTime = dueTime;
    }

    public void setIsDone (String isDone){
        this.isDone = isDone;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    } 
}
