import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Arrays;

import javax.sound.sampled.*;
 
public class Capture extends JFrame {
	
	protected boolean running;
	ByteArrayOutputStream out;
	int numBytesRead;
	
	public Capture() {
	    super("Capture Sound Demo");
	    setDefaultCloseOperation(EXIT_ON_CLOSE);
	    Container content = getContentPane();
	 
	    final JButton capture = new JButton("Capture");
	    final JButton stop = new JButton("Stop");
	    final JButton play = new JButton("Play");
	 
	    capture.setEnabled(true);
	    stop.setEnabled(false);
	    play.setEnabled(false);
	 
	    ActionListener captureListener = new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
		        capture.setEnabled(false);
		        stop.setEnabled(true);
		        play.setEnabled(false);
		        captureAudio();
		        
	      }
	    };
	    capture.addActionListener(captureListener);
	    content.add(capture, BorderLayout.NORTH);
	 
	    ActionListener stopListener = new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
		        capture.setEnabled(true);
		        stop.setEnabled(false);
		        play.setEnabled(true);
		        running = false;
	    	}
	    };
	    stop.addActionListener(stopListener);
	    content.add(stop, BorderLayout.CENTER);
	 
	    ActionListener playListener = new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    		playAudio();
	    	}
	    };
	    play.addActionListener(playListener);
	    content.add(play, BorderLayout.SOUTH);
}
 
  public void captureAudio() {
    try {
    //Initialize TargetDataLine to capture audio
	  final AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
	  DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
	  TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
	  targetLine.open(format);
	  targetLine.start();
      
	  //int numSamples = 1000000;
	  //int SampleArray[] = new int [numSamples];
		
      Runnable runner = new Runnable() {
        byte targetData[] = new byte[targetLine.getBufferSize() / 10];
  
        public void run() {
          out = new ByteArrayOutputStream();
          running = true;
          try {
            while (running) {
            	numBytesRead = targetLine.read(targetData, 0, targetData.length);
              
            	if (numBytesRead > 0) {
              		out.write(targetData, 0, numBytesRead);
            	}
            }
            out.close();
          } catch (IOException e) {
            System.err.println("I/O problems: " + e);
            System.exit(-1);
          }
        }
      };
      Thread captureThread = new Thread(runner);
      captureThread.start();
      
    } catch (LineUnavailableException e) {
      System.err.println("Line unavailable: " + e);
      System.exit(-2);
    }
  }
 
  
  public void playAudio() {
    try {  
  	  //Initialize SourceDataLine to playback audio
  	  final AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
  	  DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);
      SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
      sourceLine.open(format);
      sourceLine.start();
      
      byte audio[] = out.toByteArray();
      InputStream input = new ByteArrayInputStream(audio);
      final AudioInputStream ais = new AudioInputStream(input, format, audio.length / format.getFrameSize());
      
      Runnable runner = new Runnable() {
          //int bufferSize = (int) format.getSampleRate()* format.getFrameSize();
          byte[] buffer = new byte[sourceLine.getBufferSize()/5];
    
          public void run() {
            try {
              int count;
              while ((count = ais.read(
                  buffer, 0, buffer.length)) != -1) {
                if (count > 0) {
                  sourceLine.write(buffer, 0, count);
                }
              }
              sourceLine.drain();
              sourceLine.close();
            } catch (IOException e) {
              System.err.println("I/O problems: " + e);
              System.exit(-3);
            }
          }
        };
      
      Thread playThread = new Thread(runner);
      playThread.start();
      
    } catch (LineUnavailableException e) {
      System.err.println("Line unavailable: " + e);
      System.exit(-4);
    } 
  }
 
  @SuppressWarnings("deprecation")
public static void main(String args[]) {
    JFrame frame = new Capture();
    frame.pack();
    frame.show();
  }
}