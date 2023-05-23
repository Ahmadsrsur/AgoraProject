package com.app.interactiveclass.Model;

public class Question {

    public String question;
    public String opt1;
    public String opt2;
    public String opt3;
    public int answer;
    public int timer;
    public  int userAnswer=0;
    public String question_id;
    public String host_id;
    public long create_time;
    public long duration;

    public Question(){}
    public Question(String question, String opt1, String opt2, String opt3, int answer,int timer) {
        this.question = question;
        this.opt1 = opt1;
        this.opt2 = opt2;
        this.opt3 = opt3;
        this.answer = answer;
        this.timer=timer;
    }


}
