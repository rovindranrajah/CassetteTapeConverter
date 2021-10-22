package sample;

import com.mpatric.mp3agic.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.io.FileUtils;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class ID3Tags implements Initializable {
    @FXML private TextField titleField, artistField, albumField, genreField, yearField, trackField;
    @FXML private Button playButton;
    @FXML private Label totalTracks, currentTrack;
    @FXML private ProgressBar musicProgress;
    private ID3v2 tag = new ID3v24Tag();
    private String folder = "Final";
    private MediaPlayer player;
    private boolean play = false;
    private File directory = new File("splitted");
    private File[] files;
    private ArrayList<File> songs = new ArrayList<File>();
    private int songNumber=0;
    private Timer timer;
    private TimerTask task;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        files = directory.listFiles();
        new File(folder).mkdir();
        if(files!=null){
            for(File file : files){
                songs.add(file);
            }
        }
        loadMusic();
        totalTracks.setText(Integer.toString(songs.size()));
        currentTrack.setText(Integer.toString(songNumber+1));
    }
    public void loadMusic(){
        try {
            player = AudioUtils.getRecording(songs.get(songNumber));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    public void playMusic(){
        if (player.getCurrentTime().toSeconds() == player.getTotalDuration().toSeconds()){
            player.seek(new Duration(0));
            resetProgress();
            stopTimer();
        }
        if(play){
            playButton.setText("Play");
            player.pause();
            play = false;
        }
        else{
            playButton.setText("Pause");
            player.play();
            beginProgress();
            play = true;
        }
    }

    public void saveTags() throws InvalidDataException, IOException, UnsupportedTagException, NotSupportedException {
        Mp3File file = new Mp3File(songs.get(songNumber));
        file.setId3v2Tag(tag);
        tag.setTitle(titleField.getText());
        tag.setAlbum(albumField.getText());
        tag.setArtist(artistField.getText());
        tag.setYear(yearField.getText());
        tag.setTrack(trackField.getText());
        tag.setGenreDescription(genreField.getText());
        file.save(folder+"/"+titleField.getText()+".mp3");
        if(nextTrack()){
            clearField();
        }
        else{
            Stage stage = (Stage) playButton.getScene().getWindow();
            stage.close();
        }
    }
    public boolean nextTrack(){
        if (songNumber == songs.size()-1){
            songNumber=-99;
            return false;
        }
        else {
            ++songNumber;
        }
        currentTrack.setText(Integer.toString(songNumber+1));
        loadMusic();
        return true;
    }
    public void clearField(){
        titleField.clear();
        //albumField.clear();
        //artistField.clear();
        //yearField.clear();
        trackField.clear();
        //genreField.clear();
        resetProgress();
        try{
            stopTimer();
        }catch(Exception e){

        }
    }
    public void beginProgress(){
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                double current = player.getCurrentTime().toSeconds();
                try {
                    Mp3File file = new Mp3File(songs.get(songNumber));
                    double total = file.getLengthInSeconds();
                    musicProgress.setProgress((current/total));
                    if((current/total)>=1){
                        stopTimer();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UnsupportedTagException e) {
                    e.printStackTrace();
                } catch (InvalidDataException e) {
                    e.printStackTrace();
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 10);
    }
    public void resetProgress() {
        musicProgress.setProgress(0);
    }
    public void stopTimer() {
        timer.cancel();
        timer.purge();
    }
}
