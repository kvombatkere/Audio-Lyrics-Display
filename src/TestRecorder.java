import javax.sound.sampled.*;
import java.io.*;

public class TestRecorder {

	static final int RECORD_TIME = 6000;
	
	File wavFile = new File("testRecording111.wav");
	
	AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
	
	TargetDataLine line;
	
	float sampleRate = 16000;
	int sampleSizeInBits = 8;
	int channels = 2;
	boolean signed = true;
	boolean bigEndian = true;

	void start(){
		try{
			AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
			
			DataLine.Info info = new DataLine.Info(TargetDataLine.class,  format);
			
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.open(format);
			line.start();
			
			System.out.println("Start recording...");
			
			AudioInputStream ais = new AudioInputStream(line);
			
			System.out.println("Start writing...");
			AudioSystem.write(ais,  fileType,  wavFile); // this line will block the main thread, so we need another thread to stop it
			
		}catch(Exception e){
			System.out.println("Error");
		}
	}
	
	void finish(){
		line.stop();
		line.close();
		System.out.println("Finished");
	}
	
	
	// this is main function
	public static void main(String[] args){
		
		TestRecorder recorder = new TestRecorder(); // create a TestRecorder object
		Thread stopper = new Thread(new Runnable(){ // create another thread to stop the recording after the specified recording time
			public void run() {
				try{
					Thread.sleep(RECORD_TIME);
				}catch(Exception e){
					System.out.println("Error");
				}
				recorder.finish();
			}
		});
		
		stopper.start();
		recorder.start();
	}
	
}