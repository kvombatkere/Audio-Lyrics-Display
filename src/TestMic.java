import java.util.Arrays;
import javax.sound.sampled.*;


public class TestMic {
	
//	private static int Get16BitSample(int high, int low){
//		//Method to extract the sample from the 16 bit audio data byte array
//		//This is used to generate the array/points that is used to plot the actual waveform
//		return (high << 8) + (low & 0x00ff);
//	}

	public static void main(String[] args) {
		AudioFormat format = new AudioFormat(44100, 16, 2, true, true);

		DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
		DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);

		try {
			TargetDataLine targetLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
			targetLine.open(format);
			targetLine.start();
			
			
			SourceDataLine sourceLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
			sourceLine.open(format);
			sourceLine.start();

			int numBytesRead;
			byte[] targetData = new byte[targetLine.getBufferSize()/10];
			
//			int numSamples = 1000000;
//			int SampleArray[] = new int [numSamples];
			
			//This loop does the actual realtime recording and playback
			while (true) {
				//Capture audio and store in targetData as byte array
				numBytesRead = targetLine.read(targetData, 0, targetData.length);

				if (numBytesRead == -1)	break;
				
				//Print values stored in the targetData variable
				//System.out.println(Arrays.toString(targetData));
				
				//Code to extract data points and plot waveform in real time 
//				for(int i=0, j=0; i < targetData.length;){
//					
//					int iHigh = targetData[i];
//					i++;
//					int iLow = targetData[i];
//					i++;
//					
//					SampleArray[j] = Get16BitSample(iHigh, iLow);
//					j++;
//				}

				//Playback the audio captured in real-time
				sourceLine.write(targetData, 0, numBytesRead);
			}
			
		}
		catch (Exception e) {
			System.err.println(e);
		}
	}

}
