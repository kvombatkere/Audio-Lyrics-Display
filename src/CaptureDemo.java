//Karan Vombatkere
//June-July 2016, University of Rochester
//Real Time Audio Lyrics Display System

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Arrays;

import javax.sound.sampled.*;

import functions.*;
import Jama.*;


@SuppressWarnings("serial")
public class CaptureDemo extends JFrame {
	
	//Create new object
	static CaptureDemo demo = new CaptureDemo();
	
	//Specify Global variables used between both threads
	static int numBytesRead;
	//Buffer set up
	static int bufferLen = 4096;
	static byte[] targetData = new byte[bufferLen];
	
	static int readStatus = 0;//flag to coordinate thread reading and writing from the buffer
	static int suspend = 0;//Flag to coordinate pause/resume for threads
	
	public static String volString;
	public static JTextArea tField;
	
	static double inf = Double.POSITIVE_INFINITY; //define infinity

	
	int J = 0;
	int T = 0;
	int c = 300;
	int maxRunCount = 2;
	int runCount = 0;
	int previous = 0;
	
	double[][] chroma = new double [12][1];
	double[][] chromaBuffer = new double[12][c];
	
	static int frameNum1 = 1;
	static double[][] refChroma = null;
	
	
	class Capture implements Runnable{
	//Class to capture the audio as a real time stream
	//Uses the 'Audio Recorder Thread' and writes to the common buffer targetData
		public void captureAudio() {
			try {
				//Initialize TargetDataLine to capture audio
				final AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
				DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
				
				TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
				targetLine.open(format);
				targetLine.start();
				
				
				while (true) {
					Thread.currentThread().getName();
				  
					//Condition to read audio data into the buffer
					if(readStatus == 0){
						numBytesRead = targetLine.read(targetData, 0, targetData.length);
						if (numBytesRead == -1)	break;
						
						readStatus = 1;
					}
				}
				
				  
			}	catch (Exception e) {
			  	System.err.println(e);
		}
	}
		
		public void run(){
			this.captureAudio();
		}
	}


	class playBack implements Runnable{
	//Class to play back the audio from the buffer in real time
	//Uses the 'Play back Thread' and reads from the common buffer targetData	
		public void playAudio(){
			try {
				//Initialize SourceLine to play back audio
				final AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
				DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);
				
				SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
				sourceLine.open(format);
				sourceLine.start();
				
				//Initialize d and D arrays
				double[][] d = new double[frameNum1][frameNum1];
				double[][] D = new double[frameNum1][frameNum1];
				
				for (int fnum = 0; fnum < frameNum1; fnum++){
					for (int k = 0; k < frameNum1; k++){
						d[k][fnum] = inf;
						D[k][fnum] = inf;
					}
				}
			
				d[0][0] = 0;
				for (int j = 0; j < 12; j++){
					d[0][0] += Math.pow((-refChroma[j][0]), 2);
				}
				D[0][0] = d[0][0];
				
				double t_val = 0;
				
				while(true){					
					//Condition to play the audio back
					if (readStatus == 1){
						
						sourceLine.write(targetData, 0, numBytesRead);
						
						volString = printVolume(targetData);
				        tField.setText("\nLive Volume is: " + volString);
				        
				        //Get Spectrogram data for each buffer
				        double[][] spec = getDoubleSpectra(targetData);
				        //Get Chroma Matrix for each buffer
				        chroma = getChromaMatrix(spec);
				        
				        //Normalize Real-Time Chroma Vector
				        double chromaAbs = 0;
				        for(int i = 0; i < 12; i++)
				        	chromaAbs += Math.pow(chroma[i][0], 2);				        
				        for(int i = 0; i < 12; i++){
				        	chroma[i][0] = chroma[i][0] / Math.pow(chromaAbs, 0.5);
				        	System.out.printf("%.2f\t", chroma[i][0]); //Display chroma vector
				        }
				        System.out.println();
				        
				        //The following MATLAB code is implemented in Java
				        //chromaBuffer(:,1:end-1) = chromaBuffer(:,2:end);
				        for (int i = 1; i < c; i++){
				        	for (int r = 0; r < 12; r++)
					        	chromaBuffer[r][i-1] = chromaBuffer[r][i];
				        }
				        
				        //chromaBuffer(:,end) = chroma;
				        for (int p = 0; p < 12; p++){
				        	chromaBuffer[p][c-1] = chroma[p][0];
				        }
				        
				        //Call DTW algorithm here
				        t_val = DTW_Online.findPath(c, runCount, maxRunCount, T, J, D, d, previous, refChroma, chromaBuffer, chroma);
				        System.out.println("t_val = " + t_val);

						readStatus = 0;
					}
					Thread.currentThread().getName();

				}
				
			}catch (Exception e) {
			 System.err.println(e);
			}
	}
		
		public void run(){
			this.playAudio();
		}
	}

	
	//Code to get double sample points from byte buffer
    private static double readDouble(byte buf1, byte buf2){ 
    //Method to extract sample points from byte data	
    	double res = 0;
        res = (buf2&0x000000FF) | (((int)buf1)<<8);  
        return res;  
    }
    
    //Chroma vector for Real Time Audio
    private static double[][] getDoubleSpectra(byte[] buffer){
    	//Method to return double array with Spectra for Real Time audio
    	int frameLen = bufferLen/2;
    	int fftLen = frameLen * 4;
    	double[] data = new double[fftLen];
    	
    	//Generate Window function array
    	double[] win = new double[fftLen];
        for (int i = 0; i < frameLen; i++ )
            win[i] = 0.53836 - 0.46164*Math.cos(2*Math.PI*i/(frameLen-1));
    	
        //Generate double array with the sample points
    	for (int i = 0; i < frameLen; i++){
    		data[i] = readDouble(buffer[2*i], buffer[2*i+1]);	
    	}
    	
    	double[][] Spectrogram = new double [fftLen][1];
    	Complex[] data_complex = new Complex[fftLen];
    	
    	for (int i = 0; i < fftLen; i++) 
            data_complex[i] = new Complex(data[i] * win[i], 0);
    	
    	Complex[] spec = FFT.fft(data_complex);
    	for (int i = 0; i < fftLen; i++)
        	Spectrogram[i][0] = spec[i].abs();
    			
    	return Spectrogram;
    }
    
	
    //Get chroma matrix for Reference Audio File
	private static void ChromaRefAudio(String filename){
        // read audio
		AudioInfo audioInfo = AudioMatrix.readAudio(filename);
		int fs = audioInfo.fs;
		
		int audioL = audioInfo.L;
        double[] wavData = audioInfo.wavData;
        
        int frameLen = bufferLen/2;
    	int fftLen = frameLen * 4;

        int frameHop = 448;
        int frameNum = AudioMatrix.getAudioFrameNumber(wavData, frameLen, frameHop);
        
        System.out.println("Now the audio is successfully read!");
        System.out.println("Sample rate:" + fs + " Hz");
        System.out.printf("Total lengh: %.2f sec\n", (double)audioL/fs);
        System.out.println("Frame number:" + frameNum);
        
        double[] win = new double[fftLen];
        for (int i = 0; i < frameLen; i++ )
            win[i] = 0.53836 - 0.46164*Math.cos(2*Math.PI*i/(frameLen-1));
        
        double[][] Audio_Spectrogram = new double[fftLen][frameNum];
        
		for (int fnum = 0; fnum < frameNum; fnum++){
			double[] data = new double [fftLen];
            for (int i = 0; i < frameLen; i++) 	data[i] = wavData[frameHop*fnum+i];

            Complex[] data_complex = new Complex[fftLen];
            for (int i = 0; i < fftLen; i++) 
                data_complex[i] = new Complex(data[i] *win[i], 0);
            Complex[] spec = FFT.fft(data_complex);
            
            for (int i = 0; i < fftLen; i++)
            	Audio_Spectrogram[i][fnum] = spec[i].abs();
		}
        
		//Reference Audio chromagram
        refChroma = getChromaMatrix(Audio_Spectrogram);
		for (int fnum = 0; fnum < frameNum; fnum++){
	        double chromaAbs = 0;
	        for(int i = 0; i < 12; i++){
	        	chromaAbs += Math.pow(refChroma[i][fnum], 2);
	        }
	        
	        for (int i = 0; i < 12; i++){
	        	refChroma[i][fnum] = refChroma[i][fnum] / Math.pow(chromaAbs, 0.5);
	        }
		}
		frameNum1 = frameNum; //save the frame number to be used to initialize d and D arrays
        
        System.out.println("Reference Audio Chroma successfully Imported");
	}
	
	
	//Method to generate chroma matrix for input spectrogram data
	private static double[][] getChromaMatrix(double[][] Spectrogram_data){
		Matrix F2CM_J = null;
		try
	     {
	         File file = new File("source/F2CM");
	         FileReader fr = new FileReader(file.getAbsoluteFile());
	         BufferedReader br = new BufferedReader(fr);
	         F2CM_J =  Matrix.read(br);
	     }
	     catch (Exception e) { e.printStackTrace(); }
		
		Matrix Audio_Spectra = new Matrix(Spectrogram_data);
		Matrix ChromaA = F2CM_J.times(Audio_Spectra);
        double[][] ChromaMatrix = ChromaA.getArray();
        
		return ChromaMatrix;
	}
	
	
	//Method to print volume
	private String printVolume(byte[] buffer){
		double[] data = new double [buffer.length/2]; 
		for (int i = 0; i < buffer.length/2; i++)
        {
			data[i] = readDouble(buffer[2*i], buffer[2*i+1]);
        }
		double RMS = 0;
		for (int i = 0; i < buffer.length/2; i++)
		{
			RMS = RMS + Math.pow(data[i],2);
		}
		RMS = RMS / buffer.length*2;
		RMS = Math.pow(RMS, 0.5);
//		System.out.printf("Volume: %.2f\n", RMS);

		return String.format("Volume: %.2f",RMS);
	}
	
	
    
	public CaptureDemo() {
	    //Code for GUI panel
	    super("Automatic Lyrics Display System");
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    Container content = getContentPane();
	 
	    final JButton capture = new JButton("Capture/Play Audio");
	    final JButton pause = new JButton("Pause Stream");
	    final JButton stop = new JButton("Stop/Exit");
	    
	    final JButton getaudio = new JButton("Import Reference Audio");
	    final JButton getlyrics = new JButton("Import Lyrics");

	    capture.setEnabled(false);
	    pause.setEnabled(false);
	    stop.setEnabled(true);
	    getaudio.setEnabled(true);
	    getlyrics.setEnabled(true);
	    
	    final JFileChooser fcAudio = new JFileChooser("C:\\Users/Karan Vombatkere/workspace/audio_recorder/source");
	    final JFileChooser fcLyric = new JFileChooser("C:\\Users/Karan Vombatkere/workspace/audio_recorder");
	    
	    //Functionality after pressing Import Lyrics Button
	    ActionListener getlyricListener = new ActionListener(){
	    	public void actionPerformed(ActionEvent e) {
				capture.setEnabled(false);
		        pause.setEnabled(false);
		        stop.setEnabled(true);
			    getlyrics.setEnabled(false);
				getaudio.setEnabled(true);
				
				File refLyrics = new File("refLyrics");
				int Rval = fcLyric.showOpenDialog(content);
				if (Rval == JFileChooser.APPROVE_OPTION){
					refLyrics = fcLyric.getSelectedFile();
				}
				String refLyricName = refLyrics.toString();
				//Code to use text file name...
	    	}
	    };
	    getlyrics.addActionListener(getlyricListener);
	    content.add(getlyrics, BorderLayout.EAST);
	    
	    //Functionality after pressing Import Audio Button
	    ActionListener getaudioListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				capture.setEnabled(true);
		        pause.setEnabled(false);
		        stop.setEnabled(true);
				getaudio.setEnabled(false);
			    getlyrics.setEnabled(false);
				
				File refAudio = new File("refAudio");
				//Set up Reference Audio File chroma
		        tField.setText("Lyrics will display here in real time.\n\nImport Reference Audio and Lyrics to Start.");
		        int fcVal = fcAudio.showOpenDialog(content);
		        if (fcVal == JFileChooser.APPROVE_OPTION){
		        	refAudio = fcAudio.getSelectedFile();
		        }
		        //Call the ChromaRefAudio method with correct file name
		        String refAudioName = refAudio.toString();
				ChromaRefAudio(refAudioName);
			}
	    };
	    getaudio.addActionListener(getaudioListener);
	    content.add(getaudio, BorderLayout.NORTH);
	    
	    //Functionality after pressing Capture/Play
	    ActionListener captureListener = new ActionListener() {
	    	@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
		        capture.setEnabled(false);
		        pause.setEnabled(true);
		        stop.setEnabled(true);
				getaudio.setEnabled(false);
			    getlyrics.setEnabled(false);

		        
		        if (suspend == 0){
		        	//Start both threads when play is pressed
					captureThread.start();
			        playThread.start();
		        }
		        else if (suspend == 1){
		        	captureThread.resume();
			        playThread.resume();
		        }
		        
	    	}
	    };
	    capture.addActionListener(captureListener);
	    content.add(capture, BorderLayout.NORTH);
	    
	    //Functionality after pressing pause
	    ActionListener pauseListener = new ActionListener() {
	    	@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
		        capture.setEnabled(true);
		        pause.setEnabled(false);
		        stop.setEnabled(true);
				getaudio.setEnabled(false);
			    getlyrics.setEnabled(false);

		        
		        captureThread.suspend();
		        playThread.suspend();
		        suspend = 1;
		        
		        tField.setText("Stream is Paused! Press Capture/Play to continue...");

	    	}
	    };
	    pause.addActionListener(pauseListener);
	    content.add(pause, BorderLayout.EAST);
	 
	    ActionListener stopListener = new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		System.exit(0);
	    	}
	    };
	    stop.addActionListener(stopListener);
	    content.add(stop, BorderLayout.SOUTH);
	    
	    tField = new JTextArea("Lyrics will display here in real time.\n\nImport Reference Audio and Lyrics to Start.", 30, 40);
	    tField.setEditable(false);
	    tField.setLineWrap(true);
	    content.add(tField, BorderLayout.CENTER);
	    
	}
	
	//Set up required threads
	
	//Initialize Recording Thread
	static Capture runner2 = demo.new Capture();
	static Thread captureThread = new Thread(runner2, "Audio Recording Thread");
				
	//Initialize Play back Thread
	static playBack runner3 = demo.new playBack();
	static Thread playThread = new Thread(runner3, "Playback Thread");
    
	
	//Main thread
	public static void main(String[] args){			
		//Set up GUI window
		JFrame frame = new CaptureDemo();
	    frame.setLayout(new FlowLayout());	
	    frame.setVisible(true);
	    frame.setSize(800, 600);
	    
	}	
	
	
//	private void saveBuffer(String fileName, byte[] buffer){
//		//Method to save the buffer frame by frame
//		//It saves frames from buffer to file
//		try 
//        {
//            File file = new File(fileName);
//            
//            // create the file if doesn't exists
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            FileWriter fw = new FileWriter(file.getAbsoluteFile());
//            BufferedWriter bw = new BufferedWriter(fw);
//            for (int i = 0; i < buffer.length/2; i++)
//            {
//    			int x = readInt(buffer[2*i], buffer[2*i+1]);
//                bw.write(x + ", ");       
//            }
//
//            bw.close();
//        }catch(IOException e) 
//        {
//            System.out.println("IO Problem");
//        }
//
//	}
//	
//	private static int readInt(byte buf1, byte buf2){ 
//	    //Method to extract sample points from byte data	
//	    	int res = 0;
//	        res = (buf2&0x000000FF) | (((int)buf1)<<8);  
//	        return res;  
//	    }
		
}

