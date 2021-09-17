package sample;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.util.Timer;
import java.util.TimerTask;

public class StopwatchTimer {
    private Label hourLabel, minuteLabel, secondLabel,  milliLabel;
    //@FXML private Label status;
    private int hour, minute, second, milli;

    Timer timer;
    TimerTask task;
    public TimerTask getTask(){
        TimerTask temp = new TimerTask(){
            @Override
            public void run() {
                updateMilli();
            }
        };
        return temp;
    }
    public Timer getTimer(){
        return new Timer();
    }
    public StopwatchTimer() {
        hour = 0;
        minute = 0;
        second = 0;
        milli = 0;
        this.hourLabel = new Label(timeToString(hour));
        this.minuteLabel = new Label(timeToString(minute));
        this.secondLabel = new Label(timeToString(second));
        this.milliLabel = new Label(timeToString(milli));


    }

    public StopwatchTimer(Label hourLabel, Label minuteLabel, Label secondLabel, Label milliLabel) {
        hour = 0;
        minute = 0;
        second = 0;
        milli = 0;
        this.hourLabel = hourLabel;
        this.minuteLabel = minuteLabel;
        this.secondLabel = secondLabel;
        this.milliLabel = milliLabel;
        this.hourLabel.setText(timeToString(hour));
        this.minuteLabel.setText(timeToString(minute));
        this.secondLabel.setText(timeToString(second));
        this.milliLabel.setText(timeToString(milli));
    }

    public void startTimer(){
        resetTime();
        resetLabel();
        timer = getTimer();
        task = getTask();
        timer.scheduleAtFixedRate(task, 0, 10);
    }

    public void stopTimer(){
        timer.cancel();
        timer.purge();
    }

    private void updateHour() {
        hour++;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                hourLabel.setText(timeToString(hour));
            }
        });
    }

    public void updateMinute() {
        minute++;
        if (minute == 60){
            minute = 0;
            updateHour();
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                minuteLabel.setText(timeToString(minute));
            }
        });
    }

    public void updateSecond() {
        second++;
        if(second == 60){
            second = 0;
            updateMinute();
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                secondLabel.setText(timeToString(second));
            }
        });

    }

    public void updateMilli() {
        milli++;
        if (milli == 100){
            milli = 0;
            updateSecond();
        }
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                milliLabel.setText(timeToString(milli));
            }
        });
    }
    public void resetTime(){
        hour = minute = second = milli = 0;

    }
    public void resetLabel(){
        this.hourLabel.setText(timeToString(hour));
        this.minuteLabel.setText(timeToString(minute));
        this.secondLabel.setText(timeToString(second));
        this.milliLabel.setText(timeToString(milli));
    }
    public String timeToString(int value){
        return String.format("%02d", value);
    }
}
