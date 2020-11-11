// This file exports a matrix data into a plain-text file.
//
// Author: 			Bochen Li
// Last modified: 	07/11/2016


package functions;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MyFileIO {

	public static void saveToCSV(double[][] x, String fileName){
	    try 
	    {
	        File file = new File(fileName);
	        
	        // create the file if doesn't exists
	        if (!file.exists()) {
	            file.createNewFile();
	        }
	        FileWriter fw = new FileWriter(file.getAbsoluteFile());
	        BufferedWriter bw = new BufferedWriter(fw);
	        for (int i = 0; i < x.length-1; i++)
	        {
	            for (int j = 0; j < x[0].length-1; j++)
	            {
	                bw.write(x[i][j] + ", ");
	            }
	            int j = x[0].length-1;
	            bw.write(x[i][j] + "\n");         
	        }
	        int i = x.length-1;
	        for (int j = 0; j < x[0].length-1; j++)
	        {
	            bw.write(x[i][j] + ", ");
	        }
	        int j = x[0].length-1; 
	        bw.write(x[i][j] + "");
	
	        bw.close();
	    }
	
	    catch(IOException e) 
	    {
	        System.out.println("IO Problem");
	    }
	}
    
}
