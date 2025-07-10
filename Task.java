public class Task {
    private String title;
    private String description;
    private String dueDate;
   private String dueTime;
    private boolean isDone;

    //Constructor with args
    public Task (String title, String description, String dueDate, String dueTime, boolean isDone){
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.isDone = isDone;
    }

    //Constructor with no args
    public Task (){
        title = "Meal Prep";
        description = "Make your next meal";
        dueDate = "02.07.2025";
        dueTime = "15.00";
        isDone = false;
    }

    //Copy connstrctor
    public Task (Task other){
        this.title = other.title;
        this.description = other.description;
        this.dueDate = other.dueDate;
        this.dueTime = other.dueTime;
        this.isDone = other.isDone;
    }

    //Get
    public String getTitle(String title){
        return title;
    }

    public String getDescription (String description){
        return description;
    }

    public String getDueDate (String dueDate){
        return dueDate;
    }

    public String getDueTime (String dueTime){
        return dueTime;
    }

    public boolean getIsDone (boolean isDone){
        return isDone;
    }

    //Set
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

    @Override
    public String toString(){
        return "The task " + title + " that requires " + description + " and has to be done by " + dueDate + " at " + dueTime + " is currently marked as done " + isDone;
    }


}
