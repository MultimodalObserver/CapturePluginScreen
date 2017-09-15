package screencaptureplugin;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import mo.organization.FileDescription;
import mo.organization.Participant;
import mo.organization.ProjectOrganization;



public class ScreenRecorder {
    
    Participant participant;
    ProjectOrganization org;
    private ScreenCaptureConfiguration config;
    private File output;
    private File arInicio;
    private String path;
    private String file_name;
    private FileOutputStream outputStream;    
    private FileDescription desc;    
    static long start;
    static long pause;
    private static long inicio;
    private static long fin;
    
    private static final Logger logger = Logger.getLogger(ScreenRecorder.class.getName());
    
    public int id_camera;
    public int fps_op;
    public int sw=1;
    
    public ScreenRecorder(File stageFolder, ProjectOrganization org, Participant p,int FPS,ScreenCaptureConfiguration c){
        participant = p;
        this.org = org;
        this.config = c;
        this.fps_op=FPS;
        createFile(stageFolder);
    }

    private void createFile(File parent) {

        Date now = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss.SSS");

        String reportDate = df.format(now);

        output = new File(parent, reportDate + "_" + config.getId() + ".mp4");
        arInicio = new File(parent, reportDate + "_" + config.getId() + "-temp.txt");
        path = parent.getAbsolutePath();
        file_name = reportDate + "_"+config.getId();
        try {
            output.createNewFile();
            outputStream = new FileOutputStream(output);
            desc = new FileDescription(output, ScreenRecorder.class.getName());
        } catch (FileNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        

    }
    
    private void deleteFile() {
        if (output.isFile()) {
            output.delete();
        }
        if (desc.getDescriptionFile().isFile()) {
            desc.deleteFileDescription();
        }
    }

	private class Record implements Runnable{
            @Override
            public void run() {

		IMediaWriter writer = ToolFactory.makeWriter(path+"\\"+file_name+".mp4");
                Robot r;
                try {
                    r = new Robot();
                    Rectangle screensize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

                writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_H264, screensize.width, screensize.height);
                start = System.currentTimeMillis();
                inicio = System.currentTimeMillis();   
		while(sw!=0) {

			BufferedImage image = r.createScreenCapture(screensize);
                        BufferedImage image2 = ConverterFactory.convertToType(image, BufferedImage.TYPE_3BYTE_BGR);
                        IConverter converter = ConverterFactory.createConverter(image2, IPixelFormat.Type.YUV420P);            
                        IVideoPicture frame = converter.toPicture(image2, (System.currentTimeMillis() - start) * 1000);
                        writer.encodeVideo(0, frame);
                        switch(fps_op){
                                case 0:
                            {
                                try {
                                    Thread.sleep(25);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(ScreenRecorder.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                                case 1:
                            {
                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(ScreenRecorder.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                                case 2:
                            {
                                try {
                                    Thread.sleep(4);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(ScreenRecorder.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                                case 3: 
                            }
                        while(sw==2){                           
                            try {       
                                Thread.sleep(2);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(ScreenRecorder.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
		}
                fin = System.currentTimeMillis();
		writer.close();
                BufferedWriter bw;
                try {
                    bw = new BufferedWriter(new FileWriter(arInicio));
                    bw.write(inicio+"\n");
                    bw.write(fin+"");
                    bw.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
                try {
                    arInicio.createNewFile();
                } catch (IOException ex) {
                   logger.log(Level.SEVERE, null, ex);
                }
            }   catch (AWTException ex) {
                    Logger.getLogger(ScreenRecorder.class.getName()).log(Level.SEVERE, null, ex);
                }
           }              
	}
        
        public void StartRecord(){            
                Thread t=new Thread(new Record());
                t.start();  
        }
        
        public void StopRecord(){
            sw=0;
        }
        
        public void PauseRecord(){
             sw=2;
            pause = System.currentTimeMillis()-start;
        }
        
        public void ResumeRecod(){
            sw=1;
            start=System.currentTimeMillis()-pause+1000;
        }
        public void CandelRecord(){
            StopRecord();
            deleteFile();
        }
}