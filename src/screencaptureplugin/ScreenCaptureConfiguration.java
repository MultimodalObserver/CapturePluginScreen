package screencaptureplugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mo.capture.RecordableConfiguration;
import mo.organization.Configuration;
import mo.organization.Participant;
import mo.organization.ProjectOrganization;

public class ScreenCaptureConfiguration implements RecordableConfiguration {
    
    private String id;
    private int fps;
    private int dim;
    ScreenRecorder sr;    
    private static final Logger logger = Logger.getLogger(ScreenRecorder.class.getName());

    ScreenCaptureConfiguration(String id, int fps, int dim) {
        this.id = id;
        this.fps = fps;
        this.dim = dim;
    }
    
    ScreenCaptureConfiguration(){
        
    }

    @Override
    public void setupRecording(File stageFolder, ProjectOrganization org, Participant p) {
         sr = new ScreenRecorder(stageFolder, org, p,fps,dim, this);
    }

    @Override
    public void startRecording() {
            sr.StartRecord();
    }

    @Override
    public void stopRecording() {
        sr.StopRecord();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public File toFile(File parent) {
        try {
            File f = new File(parent, "screen_"+id+"-"+fps+"_"+dim+".xml");
            f.createNewFile();
            return f;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Configuration fromFile(File file) {
        String fileName = file.getName();
        if (fileName.contains("_") && fileName.contains(".") && fileName.contains("-")){
            String newId = fileName.substring(fileName.indexOf('_') + 1, fileName.indexOf("-"));
            String newfps = fileName.substring(fileName.indexOf('-') + 1, fileName.lastIndexOf("_"));
            String newdim = fileName.substring(fileName.lastIndexOf("_")+1,fileName.lastIndexOf("."));
            ScreenCaptureConfiguration c = new ScreenCaptureConfiguration(newId,Integer.parseInt(newfps),Integer.parseInt(newdim));
            return c;
        }
        return null;
    }

    @Override
    public void cancelRecording() {
        sr.CandelRecord();
    }

    @Override
    public void pauseRecording() {
        sr.PauseRecord();
    }

    @Override
    public void resumeRecording() {
        sr.ResumeRecod();
    }
    
}
