package app;

public class Relationship {
    private String status = "";
    private int numChanges = 0;
    
    public Relationship (String status) {
        this.status = status;
        numChanges = 0;
    }

    public void setStatus(String status) {
        if (!this.status.equalsIgnoreCase(status)) {
            this.status = status;
            numChanges++;
        }
    }

    public String getStatus() {
        return status;
    }

    public int getNumChanges() {
        return numChanges;
    }
}
