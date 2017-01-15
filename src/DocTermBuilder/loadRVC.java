package DocTermBuilder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

public class loadRVC {

	public static double [][] matrx;
	public static void load() throws IOException
	{
		String input = "/home/hduser/workspace/Java_prj/20160728/data/rcv1_train.multiclass";
		String s, temp ;
		StringTokenizer st;
		 BufferedReader br = new BufferedReader(new FileReader(input));
		 while ((s = br.readLine()) != null) {
			 
			 st = new StringTokenizer(s, " ", false);
             while (st.hasMoreTokens()) {
                 temp = st.nextToken();
                 System.out.print(temp+"---");
             } // while ends 
             System.out.print("\n");
		 }
	}
}
