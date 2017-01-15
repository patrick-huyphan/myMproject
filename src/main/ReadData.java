package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.commons.lang.ArrayUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.mllib.linalg.Matrices;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.linalg.distributed.RowMatrix;
import org.omg.PortableInterceptor.HOLDING;

import Jama.SparseMatrix;
import scala.Tuple2;
import scala.Tuple3;

public class ReadData {
	public enum DATA_ID{HOUSING, ZOO,RCV};
	/*
	 * 5. Number of Instances: 506

		6. Number of Attributes: 13 continuous attributes (including "class"
		                         attribute "MEDV"), 1 binary-valued attribute.
		
		7. Attribute Information:
		
		    1. CRIM      per capita crime rate by town
		    2. ZN        proportion of residential land zoned for lots over 
		                 25,000 sq.ft.
		    3. INDUS     proportion of non-retail business acres per town
		    4. CHAS      Charles River dummy variable (= 1 if tract bounds 
		                 river; 0 otherwise)
		    5. NOX       nitric oxides concentration (parts per 10 million)
		    6. RM        average number of rooms per dwelling
		    7. AGE       proportion of owner-occupied units built prior to 1940
		    8. DIS       weighted distances to five Boston employment centres
		    9. RAD       index of accessibility to radial highways
		    10. TAX      full-value property-tax rate per $10,000
		    11. PTRATIO  pupil-teacher ratio by town
		    12. B        1000(Bk - 0.63)^2 where Bk is the proportion of blacks 
		                 by town
		    13. LSTAT    % lower status of the population
		    14. MEDV     Median value of owner-occupied homes in $1000's
	 */
	private static SparseMatrix readHousing() throws IOException
	{
		SparseMatrix ret = new SparseMatrix(506, 14);
		
		String input = "Input/housing.txt";
		String s, temp ;
		StringTokenizer st;
		 BufferedReader br = new BufferedReader(new FileReader(input));
		 int line = 0;
		 while ((s = br.readLine()) != null) {
			 
			 st = new StringTokenizer(s, " ", false);
			 int j = 0;
             while (st.hasMoreTokens()) {
                 temp = st.nextToken();
                 ret.put(line, j, Double.parseDouble(temp));
//                 System.out.print(temp+"\n");
                 j++;
             } // while ends 
//             System.out.print("=======\n");
             line++;
		 }
		 
		return ret;
	}
	/*
	 * 5. Number of Instances: 101
	
	6. Number of Attributes: 18 (animal name, 15 Boolean attributes, 2 numerics)
	
	7. Attribute Information: (name of attribute and type of value domain)
	   1. animal name:      Unique for each instance
	   2. hair		Boolean
	   3. feathers		Boolean
	   4. eggs		Boolean
	   5. milk		Boolean
	   6. airborne		Boolean
	   7. aquatic		Boolean
	   8. predator		Boolean
	   9. toothed		Boolean
	  10. backbone		Boolean
	  11. breathes		Boolean
	  12. venomous		Boolean
	  13. fins		Boolean
	  14. legs		Numeric (set of values: {0,2,4,5,6,8})
	  15. tail		Boolean
	  16. domestic		Boolean
	  17. catsize		Boolean
	  18. type		Numeric (integer values in range [1,7])
	 */
	private static SparseMatrix readZoo() throws IOException
	{
		SparseMatrix ret = new SparseMatrix(101,17);
		
		String input = "Input/zoo.txt";
		String s, temp ;
		StringTokenizer st;
		 BufferedReader br = new BufferedReader(new FileReader(input));
		 int line = 0;
		 while ((s = br.readLine()) != null) {
			 
			 st = new StringTokenizer(s, ",", false);
			 int j = 0;
             while (st.hasMoreTokens()) {
                 temp = st.nextToken();
                 if(j>0)
                 {
                	 ret.put(line, j-1, Double.parseDouble(temp));
//                	 System.out.print(temp+"\n");
                 }
                 j++;
             } // while ends 
//             System.out.print("=======\n");
             line++;
		 }
		 
		return ret;
	}
	
	private static SparseMatrix readRCV() throws IOException
	{
		SparseMatrix ret = new SparseMatrix(15565,47237);
		
		String input = "Input/rcv1_train.multiclass";
		String s, temp ;
		StringTokenizer st;
		 BufferedReader br = new BufferedReader(new FileReader(input));
		 int line = 0;
		 while ((s = br.readLine()) != null) {
			 
//			 System.out.print(s+"\n");
			 st = new StringTokenizer(s, " ", false);
			 int j = 0;
             while (st.hasMoreTokens()) {
                 temp = st.nextToken();
//                 if(j==0)
//                 {
//                	 System.out.print(line+", Doc ID : "+temp+"\n");
//                 }
                 if(j>0)
                 {
                	 String arr[]= temp.split(":");
//                	 System.out.print(temp.split(":")[0]+"---"+temp.split(":")[1]+"\n");
                	 
//                	 System.out.print(temp+"\n");
                	 ret.put(line, Integer.parseInt(arr[0]), Double.parseDouble(arr[1]));
                 }
                 j++;
             } // while ends 
//             System.out.print("=======\n");
             line++;
		 }
		 
		return ret;
	}
	
	private static SparseMatrix readxxx() throws IOException
	{
		SparseMatrix ret = new SparseMatrix(15565,47237);
		
		String input = "Input/rcv1_train.multiclass";
		String s, temp ;
		StringTokenizer st;
		 BufferedReader br = new BufferedReader(new FileReader(input));
		 int line = 0;
		 while ((s = br.readLine()) != null) {
			 
//			 System.out.print(s+"\n");
			 st = new StringTokenizer(s, " ", false);
			 int j = 0;
             while (st.hasMoreTokens()) {
                 temp = st.nextToken();
//                 if(j==0)
//                 {
//                	 System.out.print(line+", Doc ID : "+temp+"\n");
//                 }
                 if(j>0)
                 {
                	 String arr[]= temp.split(":");
//                	 System.out.print(temp.split(":")[0]+"---"+temp.split(":")[1]+"\n");
                	 
//                	 System.out.print(temp+"\n");
                	 ret.put(line, Integer.parseInt(arr[0]), Double.parseDouble(arr[1]));
                 }
                 j++;
             } // while ends 
//             System.out.print("=======\n");
             line++;
		 }
		 
		return ret;
	}
	
	public static SparseMatrix readData(DATA_ID dataID) throws IOException
	{
		SparseMatrix ret = null;
		switch(dataID)
		{
			case HOUSING:
			{
				ret = readHousing();
				break;
			}
			case ZOO:
			{
				ret = readZoo();
				break;
			}
			case RCV:
			{
				ret = readRCV();
				break;
			}
		}
		return ret;
	}
	
	public static SparseMatrix generateERandom(int w, int h, int nonz, double Srange, double Erange, boolean isInt)
	{
		SparseMatrix ret = new SparseMatrix(h, w);
		Random x = new Random();
		
		for(int i = 0; i< h; i++)
		{
			for(int j = 0; j< nonz; j++)
			{	
				if(isInt)
					ret.put(i, x.nextInt(w), x.nextInt(5));
				else
					ret.put(i, x.nextInt(w), Srange + (x.nextDouble() * (Erange - Srange)));
//				System.out.println(i+" "+j+": "+ret.get(i, j));
			}
		}
		return ret;
	}
	public static RowMatrix readZooMatrix(JavaSparkContext sc, String output) throws IOException {
//		String input = ;
//		RowMatrix U = null;
//		Vector S = null;
//		Matrix V = null;
//		JavaRDD<Vector> rows = null;
//		
//		double[] array = new double[100];
//		Vector currentRow = Vectors.dense(array);
//		List<Vector> ret = new ArrayList<Vector>();
//		ret.add(currentRow);
//		
//		RowMatrix mat = new RowMatrix(rows.rdd());
		
		
		JavaRDD<String> input = sc.textFile("Input/zoo.txt");

		JavaRDD<Vector> a = input.flatMap(new FlatMapFunction<String, Vector>() {

			public Iterable<Vector> call(String t) throws Exception {
				// TODO Auto-generated method stub
				List<Double> ret = new ArrayList<Double>();
				
				int i = 0;
				for(String s :t.split(","))
				{
					if(i != 0)
					{
						ret.add(Double.parseDouble(s));
//						System.out.println(i+"-"+Double.parseDouble(s));
					}
					i++;
				}
				Double[] arr = new Double[ret.size()];
				ret.toArray(arr);
				double[] d = ArrayUtils.toPrimitive(arr);
				Vector currentRow = Vectors.dense(d);
				List<Vector> retV = new ArrayList<Vector>();
				retV.add(currentRow);
				
				
				return retV;
			}
			
		});
//		a.toString();
		
		System.out.println(a.collect().toString());
		
		
//		Matrix SM = Matrices.ones(17, 560);//dense(reduceCount,reduceCount,ssdA).transpose();
		
		RowMatrix m =new RowMatrix(a.rdd());
		m.rows().saveAsObjectFile(output+"/zooOri");
		m.multiply(Matrices.ones(17, 560)).rows().saveAsTextFile(output+"/zoo");;
		
		//System.out.println(Matrices.ones(17, 560).numCols()+"--Matrices.ones(17, 560).numCols()--"+  Matrices.ones(17, 560).numRows());
		
//		Matrices.dense(10, 10 , new double[]{10,100,20}).
		
		return m;
	}
	
	public static JavaRDD<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>> readRCVMatrix2RDD(JavaSparkContext sc, int m, int n,String output) throws IOException {
		
		int a= 0;
		final int w = m;
		final int h = n;
		JavaRDD<String> input = sc.textFile("Input/rcv1_train.multiclass");
		if(a == 0 )
		{
		JavaRDD<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>> ret2 = 
				input.flatMap(new FlatMapFunction<String, Tuple3<Integer, Integer, Double>>() {
			int k = 0;
			public Iterable<Tuple3<Integer, Integer, Double>> call(String t) throws Exception {
				// TODO Auto-generated method stub
				List<Tuple3<Integer, Integer, Double>> ret = new ArrayList<Tuple3<Integer,Integer,Double>>();
				int i = 0;
				for(String s :t.split(" "))
				{
					if((i != 0) && (Integer.parseInt(s.split(":")[0]) <w) && (k<h))
					{
						ret.add(new Tuple3<Integer, Integer, Double>(k, Integer.parseInt(s.split(":")[0]), Double.parseDouble(s.split(":")[1])));
//						System.out.println(s);
//						B.add(new Tuple2<Integer, Double>(Integer.parseInt(s.split(":")[0]), Double.parseDouble(s.split(":")[1])));
//						System.out.println(i+"-"+Double.parseDouble(s.split(":")[1]));
//						System.out.println(i+"-"+k);
					}
					i++;
				}
				k++;
				return ret;
			}
		}).flatMapToPair(new PairFlatMapFunction<Tuple3<Integer,Integer,Double>, Integer, Tuple2<Integer,Double>>() {

			public Iterable<Tuple2<Integer, Tuple2<Integer, Double>>> call(Tuple3<Integer, Integer, Double> t)
					throws Exception {
				// TODO Auto-generated method stub
				List<Tuple2<Integer, Tuple2<Integer, Double>>> ret = new ArrayList<Tuple2<Integer,Tuple2<Integer,Double>>>();
				ret.add(new Tuple2<Integer, Tuple2<Integer,Double>>(t._2(), new Tuple2<Integer,Double>(t._1(),t._3())));
				return ret;
			}
			
		}).groupByKey().flatMap(new FlatMapFunction<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>, Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>() {

			public Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> call(
					Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> t) throws Exception {
				// TODO Auto-generated method stub
				List<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> ret = new ArrayList<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>(); 
				ret.add(t);
				return ret;
			}
		});

//		ret2.saveAsTextFile(output);
//		ret2.saveAsObjectFile(output);
		
		return ret2;//sc.parallelize(ret);

		}else{
		JavaRDD<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>> ret =
				input.flatMap(new FlatMapFunction<String, Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>>() {
			int k = 0;
			public Iterable<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>> call(String t) throws Exception {
				// TODO Auto-generated method stub
//				List<Iterable<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>>> ret = new ArrayList<Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>();
				List<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>> A = new ArrayList<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>();
 				List<Tuple2<Integer, Double>> B= new ArrayList<Tuple2<Integer,Double>>();
				int i = 0;
				for(String s :t.split(" "))
				{
					if(i != 0)
					{
//						System.out.println(s);
						B.add(new Tuple2<Integer, Double>(Integer.parseInt(s.split(":")[0]), Double.parseDouble(s.split(":")[1])));
//						System.out.println(i+"-"+Double.parseDouble(s.split(":")[1]));
					}
					i++;
				}
				
				A.add(new Tuple2<Integer, Iterable<Tuple2<Integer,Double>>>(k, B));
				k++;
//				System.out.println(k+" ====      "+B.size() +" - " +B.toString());
//				System.out.println(k+" ====      "+B.size());
//				ret.add(A);
				return A;
			}
			
		});

		ret.saveAsTextFile(output);
		
		return ret;//sc.parallelize(ret);
		}
		
	}
//	public static RowMatrix readRCVMatrix(JavaSparkContext sc, String output) throws IOException {
//
////		Matrix dm = Matrices.dense(3, 2, new double[] {1.0, 3.0, 5.0, 2.0, 4.0, 6.0});
////
////		// Create a sparse matrix ((9.0, 0.0), (0.0, 8.0), (0.0, 6.0))
////		Matrix sm = Matrices.sparse(3, 2, new int[] {0, 1, 3}, new int[] {0, 2, 1}, new double[] {9, 6, 8});
//		
//		
//		JavaRDD<String> input = sc.textFile("Input/rcv1_train.multiclass");
//			
//		JavaRDD<Vector> a = input.flatMap(new FlatMapFunction<String, Vector>() {
//
//			public Iterable<Vector> call(String t) throws Exception {
//				// TODO Auto-generated method stub
//				List<Double> ret = new ArrayList<Double>();
//				List<Integer> index = new ArrayList<Integer>();
//				
//				int i = 0;
//				for(String s :t.split(" "))
//				{
//					if(i != 0)
//					{
//						index.add(Integer.parseInt(s.split(":")[0]));
//						ret.add(Double.parseDouble(s.split(":")[1]));
////						System.out.println(i+"-"+Double.parseDouble(s.split(":")[1]));
//					}
//					i++;
//				}
//				Double[] arr = new Double[ret.size()];
//				ret.toArray(arr);
//				double[] d = ArrayUtils.toPrimitive(arr);
//				
//				Integer [] arrI = new Integer[index.size()];
//				index.toArray(arrI);
//				int[] id = ArrayUtils.toPrimitive(arrI);
//				
//				Vector currentRow = Vectors.sparse(15564, id, d);   //15,564 / 518,571 
////				Vector currentRow = Vectors.dense(d);
//				List<Vector> retV = new ArrayList<Vector>();
//				retV.add(currentRow);
//				
//				
//				return retV;
//			}
//			
//		});
////		a.toString();
//		
////		System.out.println(a.collect().toString());
//		a.saveAsTextFile(output);
//		
////		Matrix SM = Matrices.ones(17, 560);//dense(reduceCount,reduceCount,ssdA).transpose();
//		
//		RowMatrix m = new RowMatrix(a.rdd());
//		
//		m.rows().saveAsTextFile(output+"/rcv1_train");
////		m.multiply(Matrices.ones(17, 560));
//		
////		System.out.println(Matrices.ones(17, 560).numCols()+"--Matrices.ones(17, 560).numCols()--"+  Matrices.ones(17, 560).numRows());
//		
////		Matrices.dense(10, 10 , new double[]{10,100,20}).
//		
//		return m;
//	}
	

	
	public static RowMatrix readHousingMatrix(JavaSparkContext sc, String output) throws IOException {
//		String input = ;
//		RowMatrix U = null;
//		Vector S = null;
//		Matrix V = null;
//		JavaRDD<Vector> rows = null;
//		
//		double[] array = new double[100];
//		Vector currentRow = Vectors.dense(array);
//		List<Vector> ret = new ArrayList<Vector>();
//		ret.add(currentRow);
//		
//		RowMatrix mat = new RowMatrix(rows.rdd());
		
		
		JavaRDD<String> input = sc.textFile("Input/housing.txt");

		// Java 7 and earlier
//		JavaRDD<String> words = input.flatMap(new FlatMapFunction<String, String>() {
//			public Iterable<String> call(String s) {
//				return Arrays.asList(s.split(" "));
//			}
//		});
			
		JavaRDD<Vector> a = input.flatMap(new FlatMapFunction<String, Vector>() {

			public Iterable<Vector> call(String t) throws Exception {
				// TODO Auto-generated method stub
				List<Double> ret = new ArrayList<Double>();
				
				int i = 0;
				System.out.println("-"+(t));
				for(String s :t.split(" "))
				{
					String tmp = s.replace(" ", "");
					if(tmp !="")
					{
						System.out.println(i+"-"+(s));
//						ret.add(Double.parseDouble(s));
					}
					i++;
				}
				Double[] arr = new Double[ret.size()];
				ret.toArray(arr);
				double[] d = ArrayUtils.toPrimitive(arr);
				Vector currentRow = Vectors.dense(d);
				List<Vector> retV = new ArrayList<Vector>();
				retV.add(currentRow);
				
				
				return retV;
			}
			
		});
//		a.toString();
		
		System.out.println(a.collect().toString());
		a.saveAsTextFile(output);
		
//		Matrix SM = Matrices.ones(17, 560);//dense(reduceCount,reduceCount,ssdA).transpose();
		
		RowMatrix m =new RowMatrix(a.rdd());
		m.multiply(Matrices.ones(17, 560));
		
		System.out.println(Matrices.ones(17, 560).numCols()+"--Matrices.ones(17, 560).numCols()--"+  Matrices.ones(17, 560).numRows());
		
//		Matrices.dense(10, 10 , new double[]{10,100,20}).
		
		return m;
	}
	

}
