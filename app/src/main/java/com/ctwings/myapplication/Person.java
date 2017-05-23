package com.ctwings.myapplication;

public class Person {
    int person_id;
    String person_fullname;
    String person_run;
    String person_is_permitted;
    String person_company;
    String person_place;
    String person_company_code;
    int person_card;
    String person_profile;
    String person_truck_patent;
    String person_rampla_patent;

    //Constructors
    public Person(){

    }

    public Person(int person_id, String person_fullname, String person_run, String person_is_permitted, String person_company, String person_place, String person_company_code, int person_card, String person_profile, String person_truck_patent, String person_rampla_patent) {
        this.person_id = person_id;
        this.person_fullname = person_fullname;
        this.person_run = person_run;
        this.person_is_permitted = person_is_permitted;
        this.person_company = person_company;
        this.person_place = person_place;
        this.person_company_code = person_company_code;
        this.person_card = person_card;
        this.person_profile = person_profile;
        this.person_truck_patent = person_truck_patent;
        this.person_rampla_patent = person_rampla_patent;
    }

    //Set
    public void set_person_id(int person_id) {
        this.person_id = person_id;
    }

    public void set_person_card(int person_card) {
        this.person_card = person_card;
    }

    public void set_person_profile(String person_profile) {
        this.person_profile = person_profile;
    }

    public void set_person_fullname(String person_fullname) {
        this.person_fullname = person_fullname;
    }

    public void set_person_run(String person_run) {
        this.person_run = person_run;
    }


    public void set_person_is_permitted(String person_is_permitted) {
        this.person_is_permitted = person_is_permitted;
    }

    public void set_person_company(String person_company) {
        this.person_company = person_company;
    }

    public void set_person_place(String person_place) {
        this.person_place = person_place;
    }

    public void set_person_company_code(String person_company_code) {
        this.person_company_code = person_company_code;
    }


    //Get
    public int get_person_id() {
        return this.person_id;
    }

    public int get_person_card() {
        return this.person_card;
    }

    public String get_person_fullname() {
        return this.person_fullname;
    }

    public String get_person_profile() {
        return this.person_profile;
    }

    public String get_person_run() {
        return this.person_run;
    }

    public String get_person_is_permitted() {
        return this.person_is_permitted;
    }

    public String get_person_company() {
        return this.person_company;
    }

    public String get_person_place() {
        return this.person_place;
    }

    public String get_person_company_code() {
        return this.person_company_code;
    }

    public String get_person_truck_patent() {
        return person_truck_patent;
    }

    public void set_person_truck_patent(String person_truck_patent) {
        this.person_truck_patent = person_truck_patent;
    }

    public String get_person_rampla_patent() {
        return person_rampla_patent;
    }

    public void set_person_rampla_patent(String person_rampla_patent) {
        this.person_rampla_patent = person_rampla_patent;
    }

    @Override
    public String toString() {
        return "Person{" +
                "person_id=" + person_id +
                ", person_fullname='" + person_fullname + '\'' +
                ", person_run='" + person_run + '\'' +
                ", person_is_permitted='" + person_is_permitted + '\'' +
                ", person_company='" + person_company + '\'' +
                ", person_place='" + person_place + '\'' +
                ", person_company_code='" + person_company_code + '\'' +
                ", person_card=" + person_card +
                ", person_profile='" + person_profile + '\'' +
                ", person_truck_patent='" + person_truck_patent + '\'' +
                ", person_rampla_patent='" + person_rampla_patent + '\'' +
                '}';
    }
}