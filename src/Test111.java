import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import Jama.Matrix;

public class Test111 {

	public static void main(String[] args) {
		
		double[] chroma = {1,2,3,4,5};
        double chromaAbs = 0;
        for(int i = 0; i < 5; i++)
        	chromaAbs += Math.pow(chroma[i], 2);
        
//        chromaAbs = chromaAbs/5;
        chromaAbs = Math.pow(chromaAbs, 0.5);
        
        
        
        for(int i = 0; i < 5; i++)
        	chroma[i] = chroma[i] / chromaAbs;

        for(int i = 0; i < 5; i++)
        	System.out.printf("%.2f\t", chroma[i]);
        System.out.println();
		
		
		
		
	}

}
