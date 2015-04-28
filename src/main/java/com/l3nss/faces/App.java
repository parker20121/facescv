package com.l3nss.faces;

import java.io.Console;
import java.io.File;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import static org.bytedeco.javacpp.opencv_contrib.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;

/**
 * This application wraps the OpenCV libraries, providing a facial recognition
 * capability. The user can train the system through a command line interface
 * and submit images to integrate the knowledgebase for matches. If matches 
 * are recognized, the system will return the associated identifiers.
 * 
 * The following commands are supported:
 * 
 * <ul>
 *    <li>create [facial recognition model] [path to store model]</li>
 *    <li>train [training file]</li>
 *    <li>load [model location]</li>
 *    <li>save [model location]</li>
 *    <li>find [image path]</li>
 * </ul>
 * 
 * Running the application under Apache Maven
 * 
 * <code>
 *   mvn clean package exec:java -Dplatform.dependencies=true -Dexec.mainClass=com.l3nss.faces.App
 * 
 *   At the commoand line, create a new database with the Eigen facial recoginition class. Store
 *   the database at the path /opt/pervs/database
 * 
 *   Enter command: create EIGEN /opt/pervs/database
 *   
 *   > EigenFaceRecognizer loaded. 
 * 
 *   Traing the system with a collection of png and jpg files. The system will look for 
 *   image files starting at the parent directory /opt/familywatchdog
 * 
 *   Enter command: train /opt/sexoffenders
 *   
 *   > 
 * 
 * </code>
 * 
 * @author <a href="mailto:matthew.parker@l3-com.com">Matt Parker</a>
 * @see <a href="http://docs.opencv.org/trunk/doc/tutorials/introduction/java_eclipse/java_eclipse.html">Using OpenCV with Eclipse and Java</a>
 * @see <a href="https://github.com/bytedeco/javacv">JavaCV</a>
 *
 * <code>
 * 
 * Copyright 2015 Matt Parker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * </code>
 * 
 */
public class App {
    
    public static final String CMD_CREATE = "create";
    public static final String CMD_EXIT = "exit";
    public static final String CMD_LOAD = "load";
    public static final String CMD_QUIT = "quit";
    public static final String CMD_SAVE = "save";
    public static final String CMD_SEARCH = "search";
    public static final String CMD_TRAIN = "train";
    
    public static final String RECOGNIZER_EIGEN = "EIGEN";
    public static final String RECOGNIZER_FISHER = "FISHER";
    public static final String RECOGNIZER_LBPH = "LBPH";
       
    Pattern labelPattern = Pattern.compile("\\-(\\d+)");
    
    FaceRecognizer model;
    
    String modelPath = null;
    
    Size templateSize = new Size(512, 512);
        
    String database = "";
    
    /**
     * 
     * @param args 
     */
    public static final void main( String args[] ){
        
        FaceRecognizer model = createFisherFaceRecognizer();
        
        App facialRecognition = new App();
         
        while( true ){
            
            Console console = System.console();
            String input = console.readLine("Enter command: ");
            
            String[] tokens = input.split(" ");
            
            if ( input.startsWith( CMD_CREATE ) ){
                
                if ( tokens[1].equalsIgnoreCase( RECOGNIZER_EIGEN )){
                    facialRecognition.setModel( createEigenFaceRecognizer(300, 10d) );
                    System.out.println("EigenFaceRecognizer loaded. ");
                    
                } else if ( tokens[1].equalsIgnoreCase( RECOGNIZER_FISHER )){
                    facialRecognition.setModel( createFisherFaceRecognizer() );
                    System.out.println("FisherFaceRecognizer loaded. ");
                    
                } else if ( tokens[1].equalsIgnoreCase( RECOGNIZER_LBPH )){
                    facialRecognition.setModel( createLBPHFaceRecognizer() );
                    System.out.println("LBPHFaceRecgonizer loaded.");
                }

                    //Make the output directory.
                if ( tokens[2] != null ){
                    
                    File directory = new File( tokens[2] );
                    
                    if (!directory.exists()){
                        directory.mkdirs();
                    }
                    
                }
                
            } else if ( input.startsWith( CMD_TRAIN ) ){
                facialRecognition.train( tokens[1] );  
                
            } else if ( input.startsWith( CMD_LOAD ) ){
                facialRecognition.load( tokens[1] );
                
            } else if ( input.startsWith( CMD_SAVE ) ){
                if ( tokens.length == 1 ){
                    facialRecognition.save();
                } else {
                    facialRecognition.save(tokens[1]);
                }
                
            } else if ( input.startsWith( CMD_SEARCH ) ){
                facialRecognition.search(tokens[1]);
                
            } else if ( input.startsWith( CMD_EXIT ) || input.startsWith( CMD_QUIT ) ){
                System.out.println("Exiting..");
                System.exit(0);
            } else {
                System.out.println("Don't recognize command: " + tokens[0] );
                System.out.println("commands: " + CMD_CREATE + ", " + CMD_EXIT + ", " + CMD_LOAD + ", " + CMD_SAVE + ", " + CMD_SEARCH + "\n");
            }
            
        }
    }
    
    /**
     * 
     * @param model 
     */
    public void setModel( FaceRecognizer model ){
        this.model = model;
    }
    
    /**
     * 
     * @param directoryPath 
     */
    public void train( String directoryPath ){      
        
        File directory = new File(directoryPath);
        
        if ( directory.exists() ){
            
            try { 
                
                String[] extensions = {"png","jpg"};
                
                Collection<File> imageFiles = FileUtils.listFiles(directory, extensions, true);
                        
                int fileCount = imageFiles.size();
                System.out.println("Training system on " + fileCount + " files...");
                
                MatVector images = new MatVector( fileCount );
                //Mat labels = new Mat(0, 1, CV_32SC1);
                CvMat labels = CvMat.create( fileCount, 1, CV_32SC1);
                
                //int[] labels = new int[imageFiles.length];
                //CvMat labels = cvCreateMat(1, imageFiles.length, CV_32SC1);
                
                int counter = 0;

                for (File image : imageFiles) {                      

                    System.out.println("Processing " + image.getName() );

                    Mat grayImage = imread( image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE );

                    Mat template = new Mat( templateSize );            
                    resize(grayImage, template, templateSize, 0d, 0d, INTER_CUBIC );

                    System.out.println("   Image size width: " + grayImage.size().width() + 
                                       " height: " + grayImage.size().height() + 
                                       " scaled to " + template.size().width() + 
                                       " " + template.size().height() );

                    imwrite( database + "/resized/" + image.getName(), template );
                  
                    Matcher m = labelPattern.matcher(image.getName());

                    long label = -1;

                    if ( !m.find() ){
                        System.out.println("Image label format wasn't followed for image: " + image.getName() );
                    } else {
                        label = Long.parseLong( m.group(1) );
                        System.out.println("   Label: " + label );
                    }

                    images.put( counter, template );
                    labels.put( counter++, label);
                    
                }
      
                Mat l = new Mat(labels);
                model.train(images, l);
                
            } catch ( Exception e ){
                System.out.println("Error: " + e.toString() );
                e.printStackTrace();
            }
            
        } else {
            System.out.println("Directory doesn't exist: " + directoryPath );
        }
        
    }   
        
    /**
     * 
     * @param imagePath 
     */
    public void search( String imagePath ){
      
        File file = new File(imagePath);
        
        if ( file.exists() ){
            
            try {
                
           
                Mat greyImage = imread( imagePath, CV_LOAD_IMAGE_GRAYSCALE );    
                
                int predictedLabel = -1;
                        
                if ( greyImage.size().width() != templateSize.width() ||
                     greyImage.size().height() != templateSize.height() ){
                
                    Mat testImage = new Mat( templateSize );
                    resize( greyImage, testImage, templateSize, 0d, 0d, INTER_CUBIC );
                    
                    predictedLabel = model.predict( testImage );
                    
                } else {
                    predictedLabel = model.predict( greyImage );
                }
                           
                System.out.println("Predicted label: " + predictedLabel);
                
            } catch ( Exception e ){
                System.out.println("Error: " + e.toString() );
                e.printStackTrace();
            }
            
        } else {
            System.out.println("Can't find image at " + imagePath );
        }
        
    }
    
    /**
     * 
     * @param modelPath 
     */
    public void load( String modelPath ){
        
        if ( model != null ){
            
            System.out.println("Trying to load " + modelPath );
            
            try {
                
                File file = new File(modelPath);
                
                if ( file.exists() ){                    
                    model.load( modelPath );
                } else {
                    System.out.println("Cannot find model: " + modelPath );
                }
                
            } catch ( Exception e ){
                System.out.println("Error: " + e.toString() );
                e.printStackTrace();
            }
            
        } else {
            System.out.println("Model doesn't exist. Please create a model first.");
        }
                
    }
    
    /**
     * 
     * @param modelPath 
     */
    public void save( String modelPath ){
        
        this.modelPath = modelPath;
        
        if ( model != null ){
            model.save(modelPath);
        } else {
            System.out.println("Model doesn't exist. Plead load or create a model.");
        }
        
    }
    
    /**
     * 
     */
    public void save(){
        
        if ( modelPath != null ){
            save(modelPath);
        } else {
            System.out.println("Please provide file path to save model.");
        }
        
    }
    
}
