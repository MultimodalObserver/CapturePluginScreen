package screencaptureplugin;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.video.ConverterFactory;
import com.xuggle.xuggler.video.IConverter;
import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
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
    
    public int fps_op;
    public int sw=1;
    public int IMG_WIDTH;
    public int IMG_HEIGHT;
    public int screen_op;
    
    public ScreenRecorder(File stageFolder, ProjectOrganization org, Participant p,int FPS,int dim,int screen,ScreenCaptureConfiguration c){
        participant = p;
        this.org = org;
        this.config = c;
        this.fps_op=FPS;
        this.screen_op=screen;
        createFile(stageFolder);
        switch(dim){
            case 0:
                IMG_WIDTH = 800;
                IMG_HEIGHT = 600;
            case 1:
                IMG_WIDTH = 1024;
                IMG_HEIGHT = 768;
            case 2:
                IMG_WIDTH = 1280;
                IMG_HEIGHT = 720;
            case 3:
                IMG_WIDTH = 1366;
                IMG_HEIGHT = 768;
        }
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
                    Rectangle screensize = null;
                    int count = 1;
                for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
                    
                    Rectangle bounds = gd.getDefaultConfiguration().getBounds();
                    if(screen_op==0){
                        screensize = new Rectangle(0, 0, 0, 0);
                        screensize = screensize.union(gd.getDefaultConfiguration().getBounds());                        
                    }
                    else{
                        if(count==screen_op){                            
                            screensize = new Rectangle((int) bounds.getMinX(),
                    (int) bounds.getMinY(), (int) bounds.getWidth(), (int) bounds.getHeight());    
                        }
                    }
                    count++;
                }

                writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_H264,IMG_WIDTH,IMG_HEIGHT);
                start = System.currentTimeMillis();
                inicio = System.currentTimeMillis();   
		while(sw!=0) {
                        BufferedImage image = new Robot().createScreenCapture(screensize);
                        int type = image.getType() == 0? BufferedImage.TYPE_INT_ARGB : image.getType();
                        image = resizeImage(image,type);
                        image = ConverterFactory.convertToType(image, BufferedImage.TYPE_3BYTE_BGR);
                        IConverter converter = ConverterFactory.createConverter(image, IPixelFormat.Type.YUV420P);            
                        IVideoPicture frame = converter.toPicture(image, (System.currentTimeMillis() - start) * 1000);
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
        
        private BufferedImage resizeImage(BufferedImage originalImage, int type){
            BufferedImage resizedImage = new BufferedImage(IMG_WIDTH,IMG_HEIGHT,type);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage,0,0,IMG_WIDTH,IMG_HEIGHT,null);
            g.dispose();
            return resizedImage;
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