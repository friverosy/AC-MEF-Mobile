package com.ctwings.myapplication;

public class Record {

    int record_id;
    String person_fullname;
    String person_run;
    String person_profile;
    int record_is_input;
    int record_bus;
    int person_is_permitted;
    int person_card;
    String person_company;
    String person_place;
    String person_company_code;
    String record_input_datetime;
    String record_output_datetime;
    String person_truck_patent;
    String person_rampla_patent;
    String type;
    int pda_number;
    int record_sync;

    //Constructors
    public Record(){

    }

    public Record(int record_id, String person_fullname, String person_run, String person_profile, int record_is_input, int record_bus, int person_is_permitted, int person_card, String person_company, String person_place, String person_company_code, String record_input_datetime, String record_output_datetime, String person_truck_patent, String person_rampla_patent, String type, int pda_number, int record_sync) {
        this.record_id = record_id;
        this.person_fullname = person_fullname;
        this.person_run = person_run;
        this.person_profile = person_profile;
        this.record_is_input = record_is_input;
        this.record_bus = record_bus;
        this.person_is_permitted = person_is_permitted;
        this.person_card = person_card;
        this.person_company = person_company;
        this.person_place = person_place;
        this.person_company_code = person_company_code;
        this.record_input_datetime = record_input_datetime;
        this.record_output_datetime = record_output_datetime;
        this.person_truck_patent = person_truck_patent;
        this.person_rampla_patent = person_rampla_patent;
        this.type = type;
        this.pda_number = pda_number;
        this.record_sync = record_sync;
    }

    public String getPerson_truck_patent() {
        return person_truck_patent;
    }

    public void setPerson_truck_patent(String person_truck_patent) {
        this.person_truck_patent = person_truck_patent;
    }

    public String getPerson_rampla_patent() {
        return person_rampla_patent;
    }

    public void setPerson_rampla_patent(String person_rampla_patent) {
        this.person_rampla_patent = person_rampla_patent;
    }

    public int getPerson_card() { return person_card; }

    public void setPerson_card(int person_card) { this.person_card = person_card; }

    public String getPerson_profile() {
        return person_profile;
    }

    public void setPerson_profile(String person_profile) {
        this.person_profile = person_profile;
    }

    public int getRecord_id() {
        return record_id;
    }

    public void setRecord_id(int record_id) { this.record_id = record_id; }

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

    public String getPerson_place() {
        return person_place;
    }

    public void setPerson_place(String person_place) {
        this.person_place = person_place;
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

    public void setRecord_sync(int record_sync) {
        this.record_sync = record_sync;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getPda_number() {
        return pda_number;
    }

    public void setPda_number(int pda_number) {
        this.pda_number = pda_number;
    }

    @Override
    public String toString() {
        return "Record [record_id=" + record_id +
                ", person_fullname=" + person_fullname +
                ", person_run=" + person_run +
                ", record_is_input=" + record_is_input +
                ", record_bus=" + record_bus +
                ", person_is_permitted=" + person_is_permitted +
                ", person_company=" + person_company +
                ", person_place=" + person_place +
                ", person_company_code="+ person_company_code +
                ", record_input_datetime=" + record_input_datetime +
                ", record_output_datetime=" + record_output_datetime +
                ", record_sync=" + record_sync +
                ", person_profile=" + person_profile +
                ", person_card=" + person_card +
                "]";
    }
}