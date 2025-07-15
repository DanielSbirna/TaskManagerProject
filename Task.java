public class Task {
    private long id;
    private String title;
    private String description;
    private String dueDate;
    private String dueTime;
    private boolean isDone;

    // Constructor with args with ID
    public Task (long id, String title, String description, String dueDate, String dueTime, boolean isDone){
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.isDone = isDone;
    }

    // Constructor with args
    public Task (String title, String description, String dueDate, String dueTime, boolean isDone){
        this.id=-1;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.isDone = isDone;
    }

    //Constructor with no args
    public Task (){
        id = -1;
        title = "Meal Prep";
        description = "Make your next meal";
        dueDate = "02.07.2025";
        dueTime = "15.00";
        isDone = false;
    }

    //Copy connstrctor
    public Task (Task other){
        this.id = other.id;
        this.title = other.title;
        this.description = other.description;
        this.dueDate = other.dueDate;
        this.dueTime = other.dueTime;
        this.isDone = other.isDone;
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
    public void setId(long id) {
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
}
