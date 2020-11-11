// Dynamic Time Warping
// Author: 			Bochen Li
// Last Modified:  	04/01/2016

import Jama.*;

public class DTW {

	public static double[][] localDistMatrix( double[][] ChA, double[][] ChS ){

		int lU = ChA[0].length;
		int lV = ChS[0].length;

		double[][] d = new double[lV][lU];
		d[0][0] = 0;
		
		System.out.print("Current frame: ");
		for( int t = 0; t < lU; t ++ ){
			if ( t%500 == 0 )
				System.out.print(t + " ");
			for (int j = 0; j < lV; j ++ ){
				for ( int k = 0; k < ChA.length; k ++ ){
					d[j][t] = d[j][t] + Math.pow( ChA[k][t]-ChS[k][j] , 2);
				}
			}
		}
		
		return d;
	}
	
	
	public static double[][] globalDistMatrix( double[][] d ){

		int lU = d[0].length;
		int lV = d.length;
		
		double[][] D = new double[lV][lU];
		
		System.out.print("Current frame: ");
		for( int t = 0; t < lU; t ++ ){
			if ( t%500 == 0 )
				System.out.print(t + " ");
			for (int j = 0; j < lV; j ++ ){
				if ( j == 0 & t > 0 )
					D[j][t] = d[j][t] + D[j][t-1];
				else if (t == 0 & j > 0)
					D[j][t] = d[j][t] + D[j-1][t];
				else if (t > 0 & j > 0){
					D[j][t] = D[j-1][t] < D[j][t-1] ? D[j-1][t] : D[j][t-1];
					D[j][t] = D[j][t] < D[j-1][t-1] ? D[j][t] : D[j-1][t-1];
					D[j][t] = D[j][t] + d[j][t];
				}
			}
		}
		
		return D;
	}
	
	public static double[][] findPath ( double[][] D ){
		int n = D.length-1;
		int m = D[0].length-1;
		double[][] Path = new double[m+n][2];
		Path[0][0] = n;
		Path[0][1] = m;
		int pathLen = 0;
		while(n > 0 | m > 0){
			if (n == 0)
				m = m-1;
			else if (m == 0)
				n = n-1;
			else
				if ( D[n][m-1] < D[n-1][m] & D[n][m-1] < D[n-1][m-1] )
					m--;
				else if (D[n-1][m] < D[n][m-1] & D[n-1][m] < D[n-1][m-1] )
					n--;
				else{
					n--; m--;
				}
			pathLen ++;	
			Path[pathLen][0] = n;
			Path[pathLen][1] = m;		
//			System.out.println(Path[pathLen][0] + "  " + Path[pathLen][1]);
		}
		pathLen ++;
		System.out.println("The length of the path is: " + pathLen);
		
		// remove extra 0s
		double[][] PathNew = new double[pathLen][2];
		for (int i = 0; i < pathLen-1; i ++){
			PathNew[i][0] = Path[i][0];
			PathNew[i][1] = Path[i][1];
		}
		
		return PathNew;
		
	}
	
}