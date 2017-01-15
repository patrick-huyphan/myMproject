package main;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.PairFlatMapFunction;

import com.clearspring.analytics.util.Lists;

import Jama.SparseMatrix;
import Jama.SparseVector;
import scala.Tuple2;
import scala.Tuple3;

public class util {

	public static JavaPairRDD<Integer, Iterable<Tuple2<Integer, Double>>> scale(JavaSparkContext sc, JavaPairRDD<Integer, Iterable<Tuple2<Integer, Double>>> A, final double B)
	{
		JavaPairRDD<Integer, Iterable<Tuple2<Integer, Double>>> ret = null;
		
		A.flatMapToPair(new PairFlatMapFunction<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>, Integer, Iterable<Tuple2<Integer, Double>>>() {

			public Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> call(
					Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> t) throws Exception {
				// TODO Auto-generated method stub
				List<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> ret = new ArrayList<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>();
				List<Tuple2<Integer, Double>> emo = new ArrayList<Tuple2<Integer,Double>>();
				for (Tuple2<Integer, Double> element : t._2()) {
					emo.add(new Tuple2<Integer, Double>(element._1, element._2*B));
				}
				ret.add(new Tuple2<Integer, Iterable<Tuple2<Integer,Double>>>(t._1, emo));
				return null;
			}
		});
		
		return ret;
	}


	
	public static JavaRDD<Iterable<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>>> toListArray2D(JavaSparkContext sc, SparseMatrix input)
	{
		List<Iterable<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>>> ret = new ArrayList<Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>();
		List<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>> matrix = new ArrayList<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>();
		int i = 0;
		for (SparseVector d : input.getRows()) {
			List<Tuple2<Integer, Double>> row = new ArrayList<Tuple2<Integer,Double>>();
			for(int j = 0; j< d.size(); j++)
			{
				row.add(new Tuple2<Integer, Double>(j, d.get(j)));
			}
			matrix.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> (i, row));
			i++;
//			System.out.println("toListArray2D " + input.getm() + " - "+input.getn() + " i "+i);
		}
		ret.add(matrix);
		return sc.parallelize(ret);
	}
	
	public static SparseMatrix[] splitMatrix(SparseMatrix input, final int total,final int outputPart)
	{
		SparseMatrix[] ret = new SparseMatrix[outputPart];
		int i = 0;
		int k = 0;
		int numOfLine = total/outputPart;
		for (SparseVector d : input.getRows()) {
			if(i == 0)
			{
//				System.out.println(numOfLine);
				ret[k] = new SparseMatrix(numOfLine, input.getn());
			}
			ret[k].setRow(i, d);
			i++;
			
			if(i == numOfLine)
			{
//				System.out.println(i);
				k++;
				i = 0;
				if(k == outputPart-1)
				{
					numOfLine = total - (numOfLine*(outputPart-1));
				}
			}
		}
		
		return ret;
	}
	
	public static JavaRDD<Iterable<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>>> splitMatrix1(JavaSparkContext sc, SparseMatrix input, final int total,final int outputPart)
	{
		List<Iterable<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>>> ret = new ArrayList<Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>();
		List<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>> matrix = new ArrayList<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>();
		int i = 0;
		for (SparseVector d : input.getRows()) {
			List<Tuple2<Integer, Double>> row = new ArrayList<Tuple2<Integer,Double>>();
			for(int j = 0; j< d.size(); j++)
			{
				row.add(new Tuple2<Integer, Double>(j, d.get(j)));
			}
//			d.print(i%((total/outputPart)+1)+": ");
			matrix.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> (i%((total/outputPart)+1), row));
			i++;
			if(i%((total/outputPart)+1)==0 || i== total-1)
			{
				ret.add(matrix);
				matrix = new ArrayList<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>();
//				matrix.clear(); //
//				i = 0;
			}
//			System.out.println("toListArray2D " + input.getm() + " - "+input.getn() + " i "+i);
		}
		
		return sc.parallelize(ret);
	}
	
	public static JavaPairRDD<Integer,Iterable<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>>> splitMatrix2(JavaSparkContext sc, SparseMatrix input, final int total,final int outputPart)
	{
		System.out.println("START splitMatrix2");
		List<Tuple2<Integer,Iterable<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>>>> ret = new ArrayList<Tuple2<Integer,Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>>();
		List<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>> matrix = new ArrayList<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>();
		int i = 0;
		int k = 0;
		for (SparseVector d : input.getRows()) {
			List<Tuple2<Integer, Double>> row = new ArrayList<Tuple2<Integer,Double>>();
			for(int j = 0; j< d.size(); j++)
			{
				row.add(new Tuple2<Integer, Double>(j, d.get(j)));
			}
//			d.print(i%((total/outputPart)+1)+": ");
			matrix.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> (i%((total/outputPart)+1), row));
//			System.out.println("toListArray2D " + input.getm() + " - "+input.getn() + " i "+i);
			i++;
			if(i%((total/outputPart)+1)==0 || i== total-1)
			{
//				System.out.println("slaver index: "+k);
				ret.add(new Tuple2<Integer, Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>(k, matrix));
				matrix = new ArrayList<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>();
				k++;
//				matrix.clear(); //
//				i = 0;
			}
			
		}
		JavaRDD<Tuple2<Integer,Iterable<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>>>> a1 = sc.parallelize(ret);
		
//		System.out.println("START splitMatrix2 2");
		
		return a1.flatMapToPair(new PairFlatMapFunction<Tuple2<Integer,Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>, Integer, Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>() {

			public Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> call(
					Tuple2<Integer,Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> t) throws Exception {
				// TODO Auto-generated method stub
				List<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> ret = new ArrayList<Tuple2<Integer,Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>>();
				ret.add(new Tuple2<Integer, Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>(t._1, t._2));
				return ret;
			}
		});
	}
	public static JavaPairRDD<String,Iterable<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>>> splitMatrix3(JavaSparkContext sc, SparseMatrix input, final int total,final int outputPart)
	{
		System.out.println("START splitMatrix2");
		List<Tuple2<String,Iterable<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>>>> ret = new ArrayList<Tuple2<String,Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>>();
		List<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>> matrix = new ArrayList<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>();
		int i = 0;
		int k = 0;
		for (SparseVector d : input.getRows()) {
			List<Tuple2<Integer, Double>> row = new ArrayList<Tuple2<Integer,Double>>();
			for(int j = 0; j< d.size(); j++)
			{
				row.add(new Tuple2<Integer, Double>(j, d.get(j)));
			}
//			d.print(i%((total/outputPart)+1)+": ");
			matrix.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> (i%((total/outputPart)+1), row));
//			System.out.println("toListArray2D " + input.getm() + " - "+input.getn() + " i "+i);
			i++;
			if(i%((total/outputPart)+1)==0 || i== total-1)
			{
//				System.out.println("slaver index: "+k);
				ret.add(new Tuple2<String, Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>(Integer.toString(k), matrix));
				matrix = new ArrayList<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>();
				k++;
//				matrix.clear(); //
//				i = 0;
			}
			
		}
		JavaRDD<Tuple2<String,Iterable<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>>>> a1 = sc.parallelize(ret);
		
//		System.out.println("START splitMatrix2 2");
		
		return a1.flatMapToPair(new PairFlatMapFunction<Tuple2<String,Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>, String, Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>() {

			public Iterable<Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> call(
					Tuple2<String,Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> t) throws Exception {
				// TODO Auto-generated method stub
				List<Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> ret = new ArrayList<Tuple2<String,Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>>();
				ret.add(new Tuple2<String, Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>(t._1, t._2));
				return ret;
			}
		});
	}
	public static SparseMatrix toSparceMatrix(Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> input)
	{
//		SparseMatrix ret = null;
		
		List<List<Double>> arrayM = new ArrayList<List<Double>>();
		
		for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> double1 : input) {
			System.out.println("convert "+double1.toString());
			List<Double> row = new ArrayList<Double>();
			
			for(Tuple2<Integer, Double> ele : double1._2)
			{
				row.add(ele._2);
			}
//			SparseVector rowx = new SparseVector(row) ;
			arrayM.add(row);
		}
//		arrayM.size();
		SparseMatrix ret = new SparseMatrix(arrayM);
		return ret;
	}
	
	public static SparseMatrix toSparceMatrix2(Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> input, int m, int n)
	{
		int type = 1;
		if(type == 0)
		{
			SparseVector arrayM[] = new SparseVector[m];
			int i = 0;
			for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> double1 : input) {
	//			System.out.println("convert "+double1._1());
				SparseVector rowx = new SparseVector(n) ;
				for(Tuple2<Integer, Double> ele : double1._2)
				{
					rowx.put(ele._1, ele._2);
				}
				arrayM[i]= rowx; //double1._1()
				i++;
			}
			
			SparseMatrix ret = new SparseMatrix(arrayM, m,n);
//			ret.print(false, "matrix");
			return ret;
		}
		else
		{
			List<SparseVector> arrayM = new ArrayList<SparseVector>();
			for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> double1 : input) {
				SparseVector rowx = new SparseVector(n) ;
				for(Tuple2<Integer, Double> ele : double1._2)
				{
					rowx.put(ele._1, ele._2);
				}
				arrayM.add(rowx);
			}
			
			SparseMatrix ret = new SparseMatrix(arrayM, m);
//			ret.print(false, "matrix");
			return ret;			
		}
	}
	public static SparseMatrix toSparceMatrix3(Iterable<Tuple2<Tuple2<Integer, Integer>, Double>> input, int m, int n)
	{
		int type = 1;
		if(type == 0)
		{
			SparseVector arrayM[] = new SparseVector[m];
			int i = 0;
//			for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> double1 : input) {
//	//			System.out.println("convert "+double1._1());
//				SparseVector rowx = new SparseVector(n) ;
//				for(Tuple2<Integer, Double> ele : double1._2)
//				{
//					rowx.put(ele._1, ele._2);
//				}
//				arrayM[i]= rowx; //double1._1()
//				i++;
//			}
			
			SparseMatrix ret = new SparseMatrix(arrayM, m,n);
//			ret.print(false, "matrix");
			return ret;
		}
		else
		{
			List<SparseVector> arrayM = new ArrayList<SparseVector>();
//			for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> double1 : input) {
//				SparseVector rowx = new SparseVector(n) ;
//				for(Tuple2<Integer, Double> ele : double1._2)
//				{
//					rowx.put(ele._1, ele._2);
//				}
//				arrayM.add(rowx);
//			}
			
			SparseMatrix ret = new SparseMatrix(arrayM, m);
//			ret.print(false, "matrix");
			return ret;			
		}
	}
	public static JavaPairRDD<Integer, Iterable<Tuple2<Integer, Double>>>  toPairRDDMatrix(JavaSparkContext sc, SparseMatrix input)
	{
		List<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> list = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
		
		int i = 0;
		for (SparseVector d : input.getRows()) {
			List<Tuple2<Integer, Double>> row = new ArrayList<Tuple2<Integer,Double>>();
			for(int j = 0; j< d.size(); j++)
			{
				row.add(new Tuple2<Integer, Double>(j, d.get(j)));
			}
			list.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> (i, row));
			i++;
		}
		
		JavaPairRDD<Integer, Iterable<Tuple2<Integer, Double>>> ret = sc.parallelize(list).flatMapToPair(new PairFlatMapFunction<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>, Integer, Iterable<Tuple2<Integer, Double>>>() {

			public Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> call(
					Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> t) throws Exception {
				// TODO Auto-generated method stub
				List<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> list = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
				list.add(new Tuple2<Integer, Iterable<Tuple2<Integer,Double>>>(t._1, t._2));
				return list;
			}
		});
		return ret;
	}
	
	
	public static JavaRDD<Tuple2<Integer, Double>> getRow(JavaSparkContext sc, JavaPairRDD<Integer, Iterable<Tuple2<Integer, Double>>> input, final int rowIndex)
	{
		return sc.parallelize(Lists.newArrayList(input.collect().get(rowIndex)._2()));//sc.parallelize(list);
	}

	
	public static JavaRDD<Tuple2<Integer, Double>> getColumn(JavaSparkContext sc, JavaPairRDD<Integer, Iterable<Tuple2<Integer, Double>>> input, final int colIndex)
	{
		return getRow(sc, transpose(sc, input), colIndex);
	}
	
	public static JavaPairRDD<Integer, Iterable<Tuple2<Integer, Double>>> transpose(JavaSparkContext sc, JavaPairRDD<Integer, Iterable<Tuple2<Integer, Double>>> input)
	{
		return input.flatMap(new FlatMapFunction<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>, Tuple3<Integer, Integer, Double>>() {

			public Iterable<Tuple3<Integer, Integer, Double>> call(Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> t)
					throws Exception {
				// TODO Auto-generated method stub
				List <Tuple3<Integer, Integer, Double>> ret = new ArrayList<Tuple3<Integer,Integer,Double>>();
				for (Tuple2<Integer, Double> tuple2 : t._2) {
					ret.add(new Tuple3<Integer, Integer, Double>(t._1, tuple2._1, tuple2._2));
				}
				return ret;
			}
		}).flatMapToPair(new PairFlatMapFunction<Tuple3<Integer,Integer,Double>, Integer, Tuple2<Integer, Double>>() {

			public Iterable<Tuple2<Integer, Tuple2<Integer, Double>>> call(Tuple3<Integer, Integer, Double> t)
					throws Exception {
				// TODO Auto-generated method stub
				List<Tuple2<Integer, Tuple2<Integer, Double>>> ret = new ArrayList<Tuple2<Integer,Tuple2<Integer,Double>>>();
				ret.add(new Tuple2<Integer, Tuple2<Integer,Double>>(t._2(), new Tuple2<Integer, Double>(t._1(), t._3())));
				return ret;
			}
		}).groupByKey();
		
	}
		
	
	public static JavaPairRDD<Integer, Iterable<Tuple2<Integer, Double>>> mull2(JavaSparkContext sc, JavaPairRDD<Integer, Iterable<Tuple2<Integer, Double>>> m1, JavaPairRDD<Integer, Iterable<Tuple2<Integer, Double>>> m2)
	{
		
		return m1.cartesian(transpose(sc, m2)).flatMapToPair(new PairFlatMapFunction<Tuple2<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>,Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>, Integer, Tuple2<Integer,Double>>() {
			public Iterable<Tuple2<Integer, Tuple2<Integer, Double>>> call(
					Tuple2<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>, Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> t)
					throws Exception {
				// TODO Auto-generated method stub
				List<Tuple2<Integer, Tuple2<Integer, Double>>> ret = new ArrayList<Tuple2<Integer,Tuple2<Integer,Double>>>();
				double sum = 0;
				for (Tuple2<Integer, Double> tuple21 : t._1()._2) {
					for (Tuple2<Integer, Double> tuple22 : t._2()._2) {
						 sum += tuple21._2*tuple22._2;
					}
				}
				ret.add(new Tuple2<Integer, Tuple2<Integer,Double>>(t._1._1, new Tuple2<Integer,Double>(t._1._1, sum)));
				return ret;
			}
		}).groupByKey();
//		 null;
	}

	
	public static JavaPairRDD<Integer, Iterable<Tuple2<Integer, Double>>> add2(JavaSparkContext sc, JavaPairRDD<Integer, Iterable<Tuple2<Integer, Double>>> m1,JavaPairRDD<Integer, Iterable<Tuple2<Integer, Double>>> m2)
	{
		return m1.cartesian( m2).flatMapToPair(new PairFlatMapFunction<Tuple2<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>,Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>, Integer, Tuple2<Integer,Double>>() {
			public Iterable<Tuple2<Integer, Tuple2<Integer, Double>>> call(
					Tuple2<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>, Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> t)
					throws Exception {
				// TODO Auto-generated method stub
				List<Tuple2<Integer, Tuple2<Integer, Double>>> ret = new ArrayList<Tuple2<Integer,Tuple2<Integer,Double>>>();
				double sum = 0;
				for (Tuple2<Integer, Double> tuple21 : t._1()._2) {
//					t._2()._2.iterator().
					for (Tuple2<Integer, Double> tuple22 : t._2()._2) {
						if(tuple21._1 == tuple22._1)
						{
							sum += tuple21._2*tuple22._2;
							break;
						}
					}
				}
				ret.add(new Tuple2<Integer, Tuple2<Integer,Double>>(t._1._1, new Tuple2<Integer,Double>(t._1._1, sum)));
				return ret;
			}
		}).groupByKey();
	}
	
	

	
//	public static JavaPairRDD<Integer, Double> split(JavaSparkContext sc, JavaPairRDD<Integer, Iterable<Tuple2<Integer, Double>>> input)
//	{
////		input.collect()
//		JavaPairRDD<Integer, Double>  ret =   input.flatMapToPair(new PairFlatMapFunction<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>, Integer, Double>() {
//
//			public Iterable<Tuple2<Integer, Double>> call(Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> t)
//					throws Exception {
//				// TODO Auto-generated method stub
//				
//				List<Tuple2<Integer, Double>> ret = new ArrayList<Tuple2<Integer,Double>>();
////				System.out.println(" key 1: "+t._1 );
//				
//				double sum  = 0;
//				for(Tuple2<Integer,Double> in: t._2)
//				{
////					System.out.println(" key 1: "+t._1 +" key 2: "+in._1 +" value: "+in._2 );
//					sum += in._2;
////					row.add(new Tuple2<Integer, Double>(in._1, in._2+3));
//				}
//				
//				ret.add(new Tuple2<Integer, Double>(t._1, sum));
//				return ret;
//			}
//		});
//		
//		for(Tuple2<Integer, Double> node :ret.collect())
//		{
//			System.out.println("no fgsf: "+node._1+"----" + node._2());
//		}
//		return ret;
//	}
}
