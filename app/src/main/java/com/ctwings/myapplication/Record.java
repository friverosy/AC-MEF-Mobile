package com.ctwings.myapplication;

/**
 * Created by cristtopher on 16-02-16.
 */
public class Record {
    private String run;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRun() {
        return run;
    }

    public void setRun(String run) {
        this.run = run;
    }

    public boolean is_permitted() {
        return is_permitted;
    }

    public void setIs_permitted(boolean is_permitted) {
        this.is_permitted = is_permitted;
    }

    private String fullName;
    private boolean is_permitted;
}
