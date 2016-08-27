package com.ctwings.myapplication;

/**
 * Created by Cristtopher Quintana on 12-08-16.
 */
public class Record {

    int record_id;
    String person_fullname;
    String person_run;
    int record_is_input;
    int record_bus;
    int person_is_permitted;
    String person_company;
    String person_location;
    String person_company_code;
    String record_input_datetime;
    String record_output_datetime;
    int record_sync;

    //Constructors
    public Record(){

    }

    public Record(Integer record_id, String person_fullname, String person_run, Integer record_is_input, Integer record_bus, Integer person_is_permitted, String person_company, String person_location, String person_company_code, String record_input_datetime, String record_output_datetime, Integer record_sync){

        this.record_id = record_id;
        this.person_fullname = person_fullname;
        this.person_run = person_run;
        this.record_is_input = record_is_input;
        this.record_bus = record_bus;
        this.person_is_permitted = person_is_permitted;
        this.person_company = person_company;
        this.person_location = person_location;
        this.person_company_code = person_company_code;
        this.record_input_datetime = record_input_datetime;
        this.record_output_datetime = record_output_datetime;
        this.record_sync = record_sync;
    }

    public int getRecord_id() {
        return record_id;
    }

    public void setRecord_id(int record_id) {
        this.record_id = record_id;
    }

    public String getPerson_fullname() {
        return person_fullname;
    }

    public void setPerson_fullname(String person_fullname) {
        this.person_fullname = person_fullname;
    }

    public String getPerson_run() {
        return person_run;
    }

    public void setPerson_run(String person_run) {
        this.person_run = person_run;
    }

    public int getRecord_is_input() {
        return record_is_input;
    }

    public void setRecord_is_input(int record_is_input) {
        this.record_is_input = record_is_input;
    }

    public int getRecord_bus() {
        return record_bus;
    }

    public void setRecord_bus(int record_bus) {
        this.record_bus = record_bus;
    }

    public int getPerson_is_permitted() {
        return person_is_permitted;
    }

    public void setPerson_is_permitted(int person_is_permitted) {
        this.person_is_permitted = person_is_permitted;
    }

    public String getPerson_company() {
        return person_company;
    }

    public void setPerson_company(String person_company) {
        this.person_company = person_company;
    }

    public String getPerson_location() {
        return person_location;
    }

    public void setPerson_location(String person_location) {
        this.person_location = person_location;
    }

    public String getPerson_company_code() {
        return person_company_code;
    }

    public void setPerson_company_code(String person_company_code) {
        this.person_company_code = person_company_code;
    }

    public String getRecord_input_datetime() {
        return record_input_datetime;
    }

    public void setRecord_input_datetime(String record_input_datetime) {
        this.record_input_datetime = record_input_datetime;
    }

    public String getRecord_output_datetime() {
        return record_output_datetime;
    }

    public void setRecord_output_datetime(String record_output_datetime) {
        this.record_output_datetime = record_output_datetime;
    }

    public int getRecord_sync() {
        return record_sync;
    }

    @Override
    public String toString() {
        return "Record [record_id=" + record_id + ", person_fullname=" + person_fullname + ", person_run=" +
                person_run + ", person_is_permitted=" + person_is_permitted +
                ", person_company=" + person_company + ", person_location=" + person_location + ", person_company_code="+ person_company_code + "]";
    }
}