
import static java.lang.Math.*;


public class DTW_Online {

	public static Direction getInc(int c, int runCount, int maxRunCount, int T, int J, double[][] D, int previous){
		Direction Dir = new Direction();
		int x = 0, y = 0, direction = 0;
		Dir.x = x;
		Dir.y = y;
		Dir.direction = direction;
		//direction == 1: Column
		//direction == 2: Row
		//direction == 3: Both

		if (T < c){
			direction = 3;
			return Dir;
		}
		
		if (runCount > maxRunCount){
			if (previous == 2){
				direction = 1;
				return Dir;
			}
			else{
				direction = 2;
				return Dir;
			}	
		}
		
		int[] minRow = new int[2];
		minRow = getMinRowValue(D, J);
		int m1 = minRow[0]; x = minRow[1];
		
		int[] minCol = new int[2];
		minCol = getMinColValue(D, T);
		int m2 = minCol[0]; y = minCol[1];
		
		if (m1 < m2) y = J;
		else if (m1 > m2) x = T;
		else {
			x = T;
			y = J;
		}
		
		
		if (x < T) direction = 2;
		else if (y < J) direction = 1;
		else direction = 3;		
		
		return Dir;
	}
	
	
	public static int[] getMinRowValue(double[][] D, int K){
		int[] x = new int[2];
		int m1 = (int) D[K][0];
		int index = 0;
		
		for (int i = 0; i < D[K].length; i++){
			if (D[K][i] < m1 ){
				m1 = (int) D[K][i];
				index = i;
			}
		}
		x[0]=m1;
		x[1]=index;
		return x;
	}
	
	public static int[] getMinColValue(double[][] D, int K){
		int[] y = new int[2];
		int m2 = (int) D[0][K];
		int index = 0;
		
		for (int i = 0; i < D.length; i++){
			if (D[i][K] < m2 ){
				m2 = (int) D[i][K];
				index = i;
			}
		}
		y[0]=m2;
		y[1]=index;
		return y;
	}
		
	
	//Method to perform real time DTW to find path
	public static double findPath(int c, int runCount, int maxRunCount, int T, int J, double[][] D, double[][] d, 
			int previous, double[][] ChAudio1, double[][] chromaBuffer, double[][] chroma){
		
		Direction Dir1 = new Direction();
		Dir1 = getInc(c, runCount, maxRunCount, T, J, D, previous);
		
		double time = 0;
		
		int direction = Dir1.direction;
		int x = Dir1.x;
		int y = Dir1.y;
		
		if (direction == 1 || direction == 3){
			T = T+1;
			int needNewFrame = 1;
			for (int k = max(0,(J-c+1)); k < J; k++){
				//d[k][T] = sum( ( ChAudio1(:,k)-chroma ).^2 );
				d[k][T] = 0;
				for (int p = 0; p < 12; p++){
					d[k][T] += pow((ChAudio1[p][k] - chroma[p][0]), 2);
				}
			}
			
	        D[max(0,J-c+1)][T] = D[max(0,J-c+1)][T-1] + d[max(0,J-c+1)][T];
	        if (J > 0){
	        	for (int k = max(1,(J-c+2)); k < J; k++){
	        		double m = min(D[k-1][T], D[k-1][T-1]);
	        		D[k][T] = d[k][T] + min(m, D[k][T-1]);
	        	}
	        }
	    }
		
		if (direction == 2 || direction == 3){
			J = J+1;
			for (int k = max(0,(T-c+1)); k < T; k++){
				//d[J][k] = sum( ( ChAudio1(:,J)-chromaBuffer(:,k-T+c)).^2 );
				d[J][k] = 0;
				for (int p = 0; p < 12; p++){
					d[J][k] += pow((ChAudio1[p][J] - chromaBuffer[p][k-T+c]), 2);
				}
			}
	        D[J][max(0,T-c+1)] = D[J-1][max(0,T-c+1)] + d[J][max(0,T-c+1)];
	        if (T > 0){
	        	for (int k = max(1,(T-c+2)); k < J; k++){
	        		double m = min(D[J][k-1], D[J-1][k-1]);
	        		D[J][k] = d[J][k] + min(m,D[J-1][k]);
	        	}
	        }
	    }
		
		if (direction == previous) runCount += 1;
		else runCount = 1;
		
		if (direction != 3) previous = direction;
		
		int[] minRow = new int[2];
		minRow = getMinRowValue(D, J);
		int m1 = minRow[0]; x = minRow[1];
		
		int[] minCol = new int[2];
		minCol = getMinColValue(D, T);
		int m2 = minCol[0]; y = minCol[1];
		
		if (m1 < m2) y = J;
		else if (m1 > m2) x = T;
		else {
			x = T;
			y = J;
		}
		
		//Code to convert J value to time value needed here
		time = J;
		return time;
	}
	
}


class Direction{
	int direction;
	int x;
	int y;
}

