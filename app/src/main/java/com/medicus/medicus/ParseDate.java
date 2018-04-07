package com.medicus.medicus;

/**
 * Created by utsav on 29-03-2018.
 */

public class ParseDate {
    private String date;
    private String parsedDate;
    private String parsedTime;
    private String stampedDate;
    private String stampedMonth;
    private String stampedYear;
    private String[] dateArray = new String[3];
//    private String stampedTime;
    public ParseDate(String date){
        this.date = date;
        parsedDate = date.split("T")[0];
        parsedTime = date.split("T")[1];
        dateArray[0] = parsedDate.split("-")[0];
        dateArray[1] = parsedDate.split("-")[1];
        dateArray[2] = parsedDate.split("-")[2];
    }

    public String getParsedDate(){
        return parsedDate;
    }
    public String getParsedTime(){
        String wTime = parsedTime.substring(0,5);
        String[] sTime = wTime.split(":");
        String hour;
        String mins = sTime[1];
        if(Integer.parseInt(sTime[0]) > 12){
            int i = Integer.parseInt(sTime[0]) - 12;
            if(i<10){
                hour = "0"+Integer.toString(i);
            } else{
                hour = Integer.toString(i);
            }
            return hour+":"+mins+"_pm";
        } else{
            hour = sTime[0];
            return hour+":"+mins+"_am";
        }
    }
    public String getStampedDate(){
        return dateArray[2];
    }
    public String getStampedMonth(){
        return dateArray[1];
    }
    public String getStampedYear(){
        return dateArray[0];
    }
}
