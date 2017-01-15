package main;


import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.mllib.linalg.DenseMatrix;
import org.apache.spark.mllib.linalg.Matrices;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.linalg.distributed.RowMatrix;
import org.apache.spark.rdd.RDD;
import org.netlib.util.doubleW;


import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import com.clearspring.analytics.util.Lists;

import DocTermBuilder.CSVFile;
import Jama.Matrix;
import Jama.Node;
import Jama.SparseMatrix;
import Jama.SparseVector;
import breeze.linalg.DenseVector;
import main.ReadData.DATA_ID;
import scala.Function1;
import scala.Tuple2;
import scala.Tuple3;
import scala.runtime.BoxedUnit;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
//import java.io.IOException;
import java.io.ObjectOutputStream;


public class main {

//	private Node<Integer, Double> st;

	public static void wordCountJava7Spark(String filename, String output) throws IOException {
		// Define a configuration to use to interact with Spark
		SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("Work Count App");

		// Create a Java version of the Spark Context from the configuration
		JavaSparkContext sc = new JavaSparkContext(conf);

		// Load the input data, which is a text file read from the command line
		JavaRDD<String> input = sc.textFile(filename);

		// Java 7 and earlier
		JavaRDD<String> words = input.flatMap(new FlatMapFunction<String, String>() {
			public Iterable<String> call(String s) {
				return Arrays.asList(s.split(" "));
			}
		});

		// Java 7 and earlier: transform the collection of words into pairs
		// (word and1)
		JavaPairRDD<String, Integer> counts = words.mapToPair(new PairFunction<String, String, Integer>() {
			public Tuple2<String, Integer> call(String s) {
				return new Tuple2(s, 1);
			}
		});

		// Java 7 and earlier: count the words
		JavaPairRDD<String, Integer> reducedCounts = counts.reduceByKey(new Function2<Integer, Integer, Integer>() {
			public Integer call(Integer x, Integer y) {
				return x + y;
			}
		});

		// Save the word count back out to a text file, causing evaluation.
		reducedCounts.saveAsTextFile(output+"/"+ Long.toString(System.currentTimeMillis()));
	     
	}

	
	
		


    
    
	public static void matrixpro(String filename, String output) throws IOException {


		Logger.getLogger("org").setLevel(Level.OFF);
		Logger.getLogger("akka").setLevel(Level.OFF);
		// Define a configuration to use to interact with Spark
		SparkConf conf = new SparkConf().setMaster("local[*]").setAppName("LSA");

		
		// Create a Java version of the Spark Context from the configuration
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		System.out.println("matrixpro");
		// Save the word count back out to a text file, causing evaluation.
		
		double Dataset1[][] =
	         {{7,	26,	6,	60},
	         {1,	29,	15,	52},
	         {11,	56,	8,	20},
	         {11,	31,	8,	47},
	         {7,	52,	6,	33},
	         {11,	55,	9,	22},
	         {3,	71,	17,	6},
	         {1,	31,	22,	44},
	         {2,	54,	18,	22},
	         {21,	47,	4,	26},
	         {1,	40,	23,	34},
	         {11,	66,	9,	12},
	         {10,	68,	8,	12},
//	         {0,	0,	0,	0},
//	         {0,	0,	0,	0},
	         };
	         double Dataset1b[][] = {{78.5, 74.3, 104.3, 87.6, 95.9, 109.2, 102.7, 72.5, 93.1, 115.9, 83.3, 113.3, 109.4}};
         double Dataset1E[][] =
        	 {
        		 {1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 0, 0, 1},
        		 {0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1},
        		 {0, 0, 1, 1, 0, 1, 0, 0, 1, 0, 0, 0, 1},
        		 {1, 0, 1, 1, 0, 1, 0, 1, 1, 0, 0, 0, 1},
        		 {0, 0, 0, 1, 1, 1, 0, 0, 1, 0, 1, 0, 0},
        		 {1, 0, 1, 1, 0, 1, 1, 0, 1, 0, 0, 0, 1},
        		 {0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 1, 0, 1},
        		 {0, 0, 0, 1, 0, 1, 0, 1, 1, 0, 0, 0, 0},
        		 {1, 0, 1, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1},
        		 {0, 0, 1, 1, 0, 1, 0, 0, 1, 1, 1, 0, 1},
        		 {1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0},
        		 {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1},
        		 {1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 0, 0, 1},
        	 };

	         //Dataset2:  n=14, m=8
	         double Dataset2[][] =
	         {{8,	6.5,	7.7,	26,	16,	43.0,	6,	60},
	         {2,	8.0,	6.7,	29,	22,	40.5,	15,	52},
	         {12,	9.5,	11.0,	31,	20,	39.0,	8,	47},
	         {7,	6.5,	7.0,	52,	29,	42.5,	6,	33},
	         {12,	10.0,	11.0,	55,	32,	38.5,	9,	22},
	         {4,	10.0,	8.7,	71,	44,	38.5,	17,	6},
	         {1,	11.5,	8.3,	31,	27,	37.5,	22,	44},
	         {2,	10.0,	7.7,	54,	36,	38.0,	18,	22},
	         {12,	11.3,	11.8,	51,	31,	37.3,	11,	24},
	         {22,	12.5,	16.0,	47,	26,	36.5,	4,	26},
	         {2,	12.0,	9.0,	40,	32,	37.0,	23,	34},
	         {7,	11.0,	9.8,	53,	35,	38.0,	16,	23},
	         {11,	10.0,	10.7,	66,	38,	39.0,	9,	12},
	         {11,	9.0,	10.0,	68,	38,	40.0,	8,	12}};
	         double Dataset2b[][] = {{81.5, 76.3, 90.6, 98.9, 111.2, 104.7, 74.5, 95.1, 106.5, 117.9, 85.3, 100.8, 116.3, 112.4}};
	            
	   int testRun = 8;
       SparseMatrix sA = null;
       SparseMatrix sB = null;
       switch (testRun)
	    {
	        case 0:
	        {
	        	sA = new SparseMatrix(new Matrix(Dataset1));//.transpose();
		        sB = new SparseMatrix(new Matrix(Dataset1b));
		        RankingMatrix.ADMMMatrixN1(sc, sA,sB.transpose(), 0.06, 2.0, 6.0E-2,1.0E-3, output); //.transpose()
		        break;
	        }
	        
	        case 1:
	        {
	        	sA = new SparseMatrix(new Matrix(Dataset2)).transpose();
		        sB = new SparseMatrix(new Matrix(Dataset2b));
		        RankingMatrix.ADMMMatrixN(sc, sA,sB.transpose(), 0.06, 2.0, 6.0E-2,1.0E-3, output); //.transpose()
		        break;
	        }
	        case 2:
	        {
	        	JavaRDD<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>> A = ReadData.readRCVMatrix2RDD(sc, 5000, 100,output+"/"+ Long.toString(System.currentTimeMillis()));
		        
		        System.out.println("Read data finished "+A.count());
		        
		        RankingMatrix.ADMMMatrixN2(sc, 100, 5000, A,0.06, 2.0, 6.0E-2,1.0E-3, output);
		        break;
	        }
	        case 3:
	        {
//	        	ReadData.readHousingMatrix(sc, output+"/"+ Long.toString(System.currentTimeMillis()));
	        	SparseMatrix housingA = ReadData.readData(DATA_ID.HOUSING);
//	        	SparseMatrix housingVE = ADMM.buildSetVEGraph(housingA);
//	        	housingVE.print(false, "housingVE");
	        	sA = housingA;
		        sB = housingA.getRow(0).toSpaceMatrix();
		        RankingMatrix.ADMMMatrixN1(sc, sA,sB.transpose(), 0.06, 2.0, 6.0E-2,1.0E-3, output); //.transpose()
		        break;
	        }
	        case 4:
	        {
//	        	ReadData.readZooMatrix(sc, output+"/"+ Long.toString(System.currentTimeMillis()));//.multiply(B);
	        	SparseMatrix zooA = ReadData.readData(DATA_ID.ZOO);
	        	sA = zooA;
		        sB = zooA.getRow(0).toSpaceMatrix();
		        RankingMatrix.ADMMMatrixN(sc, sA,sB.transpose(), 0.06, 2.0, 6.0E-2,1.0E-3, output); //.transpose()
		        break;
	        }
	        case 5:
	        {
//	        	ReadData.readRCVMatrix(sc, output+"/"+ Long.toString(System.currentTimeMillis()));
//	        	SparseMatrix RCVA = ReadData.readData(DATA_ID.RCV).scale(1.0E2); //.getMatrix(0, 15000, 0, 18000)
//	        	sA = RCVA;
//		        sB = RCVA.getRow(0).toSpaceMatrix();
		        
	        	JavaRDD<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>> A = ReadData.readRCVMatrix2RDD(sc, 5000, 800,output+"/"+ Long.toString(System.currentTimeMillis()));
		        
		        System.out.println("Read data finished "+A.count());
		        
		        RankingMatrix.ADMMMatrixN2(sc, 800, 5000, A,0.06, 2.0, 6.0E-2,1.0E-3, output);
//		        JavaPairRDD<Integer, Iterable<Tuple2<Integer, Double>>>  x= toMatrixJ(sc,sA);
//		        x.saveAsTextFile(output+"/origil"+ Long.toString(System.currentTimeMillis()));
//		        transpose2(sc, x).saveAsTextFile(output+"/trans"+ Long.toString(System.currentTimeMillis()));
		        
		        break;
	        }
	        case 6:
	        {
	        	JavaRDD<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>> A = ReadData.readRCVMatrix2RDD(sc, 5000, 800,output+"/"+ Long.toString(System.currentTimeMillis()));
		        
		        System.out.println("Read data finished "+A.count());
		        
		        RankingMatrix.ADMMMatrixN3(sc, 800, 5000, A,0.06, 2.0, 6.0E-2,1.0E-3, output);
		        break;
	        }
	        case 7:
	        {
	        	JavaRDD<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>> A = ReadData.readRCVMatrix2RDD(sc, 5000, 800,output+"/"+ Long.toString(System.currentTimeMillis()));
		        
		        System.out.println("Read data finished "+A.count());
		        
		        RankingMatrix.ADMMMatrixN4(sc, 800, 5000, A,0.06, 2.0, 6.0E-2,1.0E-3, output);
		        break;
	        }
	        case 8:
	        {
	        	JavaRDD<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>> A = ReadData.readRCVMatrix2RDD(sc, 4000, 200,output+"/"+ Long.toString(System.currentTimeMillis()));
		        
		        System.out.println("Read data finished "+A.count());
		        
		        RankingMatrix.ADMMMatrixN5(sc, 200, 4000, A,0.06, 2.0, 6.0E-2,1.0E-3, output);
		        break;
	        }
	    }
       
       sc.close();
       System.out.println("================================DONE====================================== ");
	}
	
	public static void matrixproWOSpark(String filename, String output) throws IOException {



		
		System.out.println("matrixpro");
		// Save the word count back out to a text file, causing evaluation.
		
		double Dataset1[][] =
	         {{7,	26,	6,	60},
	         {1,	29,	15,	52},
	         {11,	56,	8,	20},
	         {11,	31,	8,	47},
	         {7,	52,	6,	33},
	         {11,	55,	9,	22},
	         {3,	71,	17,	6},
	         {1,	31,	22,	44},
	         {2,	54,	18,	22},
	         {21,	47,	4,	26},
	         {1,	40,	23,	34},
	         {11,	66,	9,	12},
	         {10,	68,	8,	12},
	         };
	         double Dataset1b[][] = {{78.5, 74.3, 104.3, 87.6, 95.9, 109.2, 102.7, 72.5, 93.1, 115.9, 83.3, 113.3, 109.4}};
         double Dataset1E[][] =
        	 {
        		 {1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 0, 0, 1},
        		 {0, 1, 0, 1, 0, 1, 0, 0, 1, 0, 1, 0, 1},
        		 {0, 0, 1, 1, 0, 1, 0, 0, 1, 0, 0, 0, 1},
        		 {1, 0, 1, 1, 0, 1, 0, 1, 1, 0, 0, 0, 1},
        		 {0, 0, 0, 1, 1, 1, 0, 0, 1, 0, 1, 0, 0},
        		 {1, 0, 1, 1, 0, 1, 1, 0, 1, 0, 0, 0, 1},
        		 {0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 1, 0, 1},
        		 {0, 0, 0, 1, 0, 1, 0, 1, 1, 0, 0, 0, 0},
        		 {1, 0, 1, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1},
        		 {0, 0, 1, 1, 0, 1, 0, 0, 1, 1, 1, 0, 1},
        		 {1, 0, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0},
        		 {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 1},
        		 {1, 0, 1, 1, 0, 1, 0, 0, 1, 0, 0, 0, 1},
        	 };
/*
         0:   0.61404	 2.28070	 0.52632	 5.26316	
         1:   0.08772	 2.54386	 1.31579	 4.56140	
         2:   0.96491	 4.91228	 0.70175	 1.75439	
         3:   0.96491	 2.71930	 0.70175	 4.12281	
         4:   0.61404	 4.56140	 0.52632	 2.89474	
         5:   0.96491	 4.82456	 0.78947	 1.92982	
         6:   0.26316	 6.22807	 1.49123	 0.52632	
         7:   0.08772	 2.71930	 1.92982	 3.85965	
         8:   0.17544	 4.73684	 1.57895	 1.92982	
         9:   1.84211	 4.12281	 0.35088	 2.28070	
         10:   0.08772	 3.50877	 2.01754	 2.98246	
         11:   0.96491	 5.78947	 0.78947	 1.05263	
         12:   0.87719	 5.96491	 0.70175	 1.05263
*/
	            
	   
//       SparseMatrix sA = null;
//       SparseMatrix sB = null;
	   SparseMatrix input =  new SparseMatrix(new Matrix(Dataset1)).centered();// othorStandeder2();
//	   CSVFile.saveMatrixData("test.csv", input, "input");
//	   input.print(true, "input");
	   SparseMatrix E_Set = new SparseMatrix(new Matrix(Dataset1E));
	   double lambda1 = 2.5/*2.44*/, lambda2 = 15e-2 /*0.1*/, rho = 0.8, stop1 = 0.9, stop2 = 0.9;
   		/*
   		 * TODO: Build E set from doc
   		 */
		File fileut;
		FileWriter fw;
		
		String fileOUname = "Output/" + Long.toString(System.currentTimeMillis()) + "/"; //
		new File(fileOUname).mkdir();
		String nfilename = fileOUname+ "/log.txt";

//		String nfilename = "Output/1483605081139/log"+ Long.toString(System.currentTimeMillis())  + ".txt"; 
		
		fileut = new File(nfilename);
		fw = new FileWriter(fileut);
		
		int testRun = 3;
       switch (testRun)
	    {
	        case 1:
	        {
	        	Clustering.mainADMMProcess(input, E_Set, lambda1, lambda2, rho, stop1, stop2, 20);
	        	break;
	        }
	        
	        case 2:
	        {
	        	SparseMatrix ret = Clustering.mainAMAProcess(input, E_Set, lambda1, lambda2, rho, stop1, stop2, 50, fw);
	        	CSVFile.saveMatrixData("Output/ret.csv", ret, "ret");
	        	
	        	SparseMatrix ret2 =CSVFile.readMatrixData("Output/ret.csv");
	        	
	        	ret2.print(true, "read from file");
	        	break;
	        }
	        case 3:
	        {
	        	input = ReadData.generateERandom(20, 50, 12, 1, 5, true).centered();//.othorStandeder2();
	        	CSVFile.saveMatrixData("Output/input.csv", input, "input");
	        	E_Set = ReadData.generateERandom(50, 50, 10, 0.5, 2.5, true);
	        	CSVFile.saveMatrixData("Output/E_Set.csv", E_Set, "E_Set");
	        	
//	        	input = CSVFile.readMatrixData("Output/1483605081139/input.csv");
//	        	E_Set = CSVFile.readMatrixData("Output/1483605081139/E_Set.csv");
	        	
	        	System.out.println(input.getm()+" "+input.getn());
	        	SparseMatrix ret = Clustering.mainAMAProcess(input,
	        			E_Set,
	        			10,//lambda1,
	        			5e-2,
	        			rho,
	        			stop1,
	        			stop2,
	        			50,
	        			fw);
	        	
	        	ret.print(false, "ret");
	        	CSVFile.saveMatrixData("Output/ret.csv", ret, "ret");
	        }
	        	
	    }
       fw.close();
       System.out.println("================================DONE====================================== ");
	}
	 public static void main( String[] args ) throws IOException
	 {
	 if( args.length == 0 )
	 {
		 System.out.println( "Usage: WordCount <file>" );
		 System.exit( 0 );
	 }
	 matrixproWOSpark(args[ 0 ] ,args[ 1 ] );
//	 matrixpro(args[ 0 ] ,args[ 1 ] );
//	 wordCountJava7Spark( args[ 0 ] ,args[ 1 ] );
	 }
}
