// This file implements several audio pre-processing functions.
//
// Author: 			Bochen Li
// Last modified: 	07/11/2016


package functions;


public class AudioMatrix {
	
	public static AudioInfo readAudio(String fileName){
		WaveFileReader reader = new WaveFileReader(fileName);  
        int[] wavDataRaw = null;
        if(reader.isSuccess()){  
            wavDataRaw = reader.getData()[0]; 
        }  
        else{  
            System.err.println(fileName + "not a normal wav file");  
        }
        int L = wavDataRaw.length;

        double[] wavData = new double[L];
        double rms = 0;
        double mean = 0;
        for ( int i = 0; i < L; i ++ ){
        	rms = rms + Math.pow(wavDataRaw[i], 2);
        	mean = mean + wavDataRaw[i];
        }
        rms = Math.pow(rms / L, 0.5);
        mean = mean / L;

        for ( int i = 0; i < L; i ++ ){
        	wavData[i] = (wavDataRaw[i]+mean) / rms;
        }

        int fs = (int) reader.getSampleRate();

        AudioInfo info = new AudioInfo();
        info.fs = fs;
        info.L = L;
        info.wavData = wavData;
        return info;
        
	}
	
	
	public static int getAudioFrameNumber(double[] wavData, int frameLen, int frameHop){
		int L = wavData.length;
		int frameNum = (L - frameLen) / frameHop + 1;
		return frameNum;
	}
	
	
	public static double[][] getAudioSpectrogram(double[] wavData, int frameLen, int frameHop, int frameNum){
		
        double[] win = new double[frameLen];
        for (int i = 0; i < frameLen; i++ )
            win[i] = 0.53836 - 0.46164*Math.cos(2*Math.PI*i/(frameLen-1));
        
        double[][] Spectrogram = new double[frameLen][frameNum];
        
		for (int fnum = 0; fnum < frameNum; fnum++){
            Complex[] data_complex = new Complex[frameLen];
            for (int i = 0; i < frameLen; i++) 
                data_complex[i] = new Complex(wavData[frameHop*fnum+i] * win[i], 0);
            Complex[] spec = FFT.fft(data_complex);
            for (int i = 0; i < frameLen; i++)
            	Spectrogram[i][fnum] = spec[i].abs();
        }
		return Spectrogram;
		
	}
	

	

}
	

