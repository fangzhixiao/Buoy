package edu.neu.madcourse.buoy.listobjects;

/*
Documentation:
LocalDateTime
https://docs.oracle.com/javase/8/docs/api/java/time/LocalDateTime.html
 */
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;

/**
 * This represents a task object. A task is part of list and has a category (badge purposes), title,
 * due date, completion status, and buoys associated with it.
 */
public class Task {
    String taskTitle;
    AchievementCategory achievementCategory;
    String subCategory;
    boolean completed;
    LocalDateTime dueDate;
    ArrayList<Buoys> buoys;
    int likes;

    public Task(String title, AchievementCategory achievementCategory, String subCategory, int year,
                int month, int day, int hour, int min){
        this.taskTitle = title;
        this.dueDate = LocalDateTime.of(year, month, day, hour, min);
        this.achievementCategory = achievementCategory;
        this.subCategory = subCategory;
        this.completed = false;
        this.buoys = new ArrayList<>();
        this.likes = 0;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {

        if (this.completed != completed){
            //if completed = true, add 1 task completed to parent category, else category -1 task.
        }
        this.completed = completed;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public ArrayList<Buoys> getBuoys() {
        return buoys;
    }

    public void addBuoy(Buoys buoy){
        this.buoys.add(buoy);
    }

    public void addLike(){
        this.likes ++;
    }

    public AchievementCategory getAchievementCategory() {
        return achievementCategory;
    }

    public void setAchievementCategory(AchievementCategory achievementCategory) {
        this.achievementCategory = achievementCategory;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }
}