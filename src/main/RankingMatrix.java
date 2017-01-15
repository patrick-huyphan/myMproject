package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.spark.Accumulator;
import org.apache.spark.AccumulatorParam;
import org.apache.spark.api.java.function.FlatMapFunction;
//import org.apache.spark.api.java.JavaPairRDD;
//import org.apache.spark.api.java.JavaRDD;
//import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;

import com.clearspring.analytics.util.Lists;

//import akka.event.slf4j.Logger;

import org.apache.spark.api.java.*;
import org.apache.spark.mllib.linalg.distributed.RowMatrix;
import org.apache.spark.mllib.linalg.Matrix;
import org.apache.spark.mllib.linalg.SingularValueDecomposition;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.rdd.RDD;
import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;

import Jama.Node;
import Jama.SparseMatrix;
import Jama.SparseVector;
import scala.Tuple2;
import scala.Tuple3;


/*
 * TODO: 
 * A = Uz, A (n,m) U (n,p) column orthonomal matrix, X(p,m)
 * - data structure? node include mat name, index row, index column and value.
 * - example: transpose mat A -> mul ATA, plus (ATA,rho_Im)-> inverse (plus (ATA,rho_Im))
 * -> loop(X, U, V) 
 * - device doc into some part and process parallel-> data structure? 
 * - Parallel processing 1 doc in n part
 * - dynamic
 * - Process for all doc.
 */
public class RankingMatrix {

	/*
	 * min(A-UX)
	 */

	public static List<Double> x_slaver(JavaSparkContext sc,
			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input,// final SparseMatrix b,
			final SparseVector u, final SparseVector z, final double rho, String ouput) {
		final double[] inZA = z.toArrayDouble();
		final double[] inUA = u.toArrayDouble();
		// final double[] inB = b.toArrayDouble();

		JavaRDD<Double> ret = input
				.flatMap(new FlatMapFunction<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>, Double>() {

					/*
					 * 
					 */
					// private static final long serialVersionUID =
					// -8417815831721385215L;

					public Iterable<Double> call(Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> t)
							throws Exception {
						// TODO Auto-generated method stub
						List<List<Double>> arrayM = new ArrayList<List<Double>>();
						//
						for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> double1 : t) {
							List<Double> row = new ArrayList<Double>();
							// System.out.println("line "+double1._1);
							for (Tuple2<Integer, Double> ele : double1._2) {
								row.add(ele._2);
							}
							arrayM.add(row);
						}

						// arrayM.size();
						SparseMatrix ret = new SparseMatrix(arrayM);
						// SparseMatrix ret2 = new SparseMatrix(arrayM);
						// ret.print(true, "test");

						SparseMatrix ATA = ret.getMatrix(0, ret.getm() / 2 - 1, 0, ret.getn() - 1);// transpose().mulMatrix(ret);
						// ATA.print(true, "UTU");
						// fw.write("\nUTU:\n" + ATA.toString());
						// SparseMatrix c = new SparseMatrix(new
						// Matrix(arr)).transpose();
						// SparseMatrix ATb = A2.transpose().mulMatrix(c);
						// b.print(true, "b: ");
						// SparseMatrix ATb =
						// ret.transpose().mulMatrix(b);//.transpose();
						// ATb.print(true, "UTb");

						// 2(A^TA + \rho I_m)^-1
						// SparseMatrix IM = IMtx(A2.n);
						// IMtx(A2.n).scale(rho).print(true,
						// "IMtx(A2.n).scale(rho)");
						SparseMatrix ATA_rho_Im = ret.getMatrix(ret.getm() / 2, ret.getm() - 1, 0, ret.getn() - 1);// ATA.plus(SparseMatrix.IMtx(ret.getn()).scale(rho)).invert().scale(2);
						// ATA_rho_Im.print(true, "ATA_rho_Im");

						SparseVector inZ = new SparseVector(inZA);

						SparseVector inU = new SparseVector(inUA);
						/*
						 * x^{k+1} = 2(A^TA + \rho I_m)^-1 [ A^Tb - \rho (z^k -
						 * u^k)]
						 */
						// z.plus(u.scale(-1)).print("z-u");
						inZ.print("z - " + inZ.size());
						inU.print("u - " + inU.size());
						SparseVector rho_zk_uk = inZ.plus(inU.scale(-1)).scale(rho * (-1.0)); // -
																								// \rho
																								// (z^k
																								// -
																								// u^k)
						rho_zk_uk.print("rho_zk_uk - " + rho_zk_uk.size());

						// SparseVector ATb_rho_zATbk_uk =
						// ATb.getColumn(0).plus(rho_zk_uk); //A^Tb - \rho (z^k
						// - u^k)
						SparseVector ATb_rho_zATbk_uk = ATA.getRow(0).plus(rho_zk_uk); // A^Tb
																						// -
																						// \rho
																						// (z^k
																						// -
																						// u^k)
						// ATb_rho_zATbk_uk.print("x");
						SparseVector tmp1[] = new SparseVector[1];
						tmp1[0] = ATb_rho_zATbk_uk;
						SparseMatrix tmpM = new SparseMatrix(tmp1, 1, ATb_rho_zATbk_uk.size()).transpose();
						// fw.write("tmpm: "+tmpM.toString());
						// tmpM.print(true, "x");
						SparseMatrix Xall = ATA_rho_Im.mulMatrix(tmpM);
						Xall.getColumn(0).print("x");
						List<Double> retAr1 = new ArrayList<Double>();
						for (Double in : Xall.getColumn(0).toArrayDouble()) {
							retAr1.add(in);
						}

						System.out.println("xout1 " + retAr1.toString());

						// List<Double> retAr2 = new ArrayList<Double>();
						// for(Double in : Xall.getColumn(0).toArrayDouble())
						// {
						// retAr2.add(in);
						// }

						// List<Iterable<Double>> retAr = new
						// ArrayList<Iterable<Double>>();
						// retAr.add(retAr1);
						// retAr.add(retAr2);

						return retAr1;
					}
				});
		// ret.saveAsTextFile(ouput);

		return ret.collect();
	}

	public static List<List<Double>> x_slaver2(JavaSparkContext sc,
			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input,// final SparseMatrix b,
			final SparseVector u, final SparseVector z, final double rho, String ouput) {
		final double[] inZA = z.toArrayDouble();
		final double[] inUA = u.toArrayDouble();
		// final double[] inB = b.toArrayDouble();

		JavaRDD<List<Double>> ret = input.flatMap(
				new FlatMapFunction<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>, List<Double>>() {

					/*
					 * 
					 */
					// private static final long serialVersionUID =
					// -8417815831721385215L;

					public Iterable<List<Double>> call(Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> t)
							throws Exception {
						// TODO Auto-generated method stub
						List<List<Double>> arrayM = new ArrayList<List<Double>>();
						List<List<Double>> retM = new ArrayList<List<Double>>();
						//
						for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> double1 : t) {
							List<Double> row = new ArrayList<Double>();
							// System.out.println("line "+double1._1);
							for (Tuple2<Integer, Double> ele : double1._2) {
								row.add(ele._2);
							}
							arrayM.add(row);
						}

						// arrayM.size();
						SparseMatrix inM = new SparseMatrix(arrayM);
						// SparseMatrix ret2 = new SparseMatrix(arrayM);
						// ret.print(true, "test");

						SparseMatrix ATA = inM.getMatrix(0, inM.getm() / 2 - 1, 0, inM.getn() - 1);// transpose().mulMatrix(ret);
						// ATA.print(true, "UTU");
						// fw.write("\nUTU:\n" + ATA.toString());
						// b.print(true, "b: ");
						// ATb.print(true, "UTb");

						// 2(A^TA + \rho I_m)^-1
						// SparseMatrix IM = IMtx(A2.n);
						// IMtx(A2.n).scale(rho).print(true,
						// "IMtx(A2.n).scale(rho)");
						SparseMatrix ATA_rho_Im = inM.getMatrix(inM.getm() / 2, inM.getm() - 1, 0, inM.getn() - 1);// ATA.plus(SparseMatrix.IMtx(ret.getn()).scale(rho)).invert().scale(2);
						// ATA_rho_Im.print(true, "ATA_rho_Im");

						SparseVector inZ = new SparseVector(inZA);

						SparseVector inU = new SparseVector(inUA);
						/*
						 * x^{k+1} = 2(A^TA + \rho I_m)^-1 [ A^Tb - \rho (z^k -
						 * u^k)]
						 */
						// z.plus(u.scale(-1)).print("z-u");
						inZ.print("z - " + inZ.size());
						inU.print("u - " + inU.size());
						SparseVector rho_zk_uk = inZ.plus(inU.scale(-1)).scale(rho * (-1.0)); // -
																								// \rho
																								// (z^k
																								// -
																								// u^k)
						rho_zk_uk.print("rho_zk_uk - " + rho_zk_uk.size());

						// SparseVector ATb_rho_zATbk_uk =
						// ATb.getColumn(0).plus(rho_zk_uk); //A^Tb - \rho (z^k
						// - u^k)
						SparseVector ATb_rho_zATbk_uk = ATA.getRow(0).plus(rho_zk_uk); // A^Tb
																						// -
																						// \rho
																						// (z^k
																						// -
																						// u^k)
						// ATb_rho_zATbk_uk.print("x");
						SparseVector tmp1[] = new SparseVector[1];
						tmp1[0] = ATb_rho_zATbk_uk;
						SparseMatrix tmpM = new SparseMatrix(tmp1, 1, ATb_rho_zATbk_uk.size()).transpose();
						// fw.write("tmpm: "+tmpM.toString());
						// tmpM.print(true, "x");
						SparseMatrix Xall = ATA_rho_Im.mulMatrix(tmpM);
						Xall.getColumn(0).print("x");
						List<Double> retAr1 = new ArrayList<Double>();
						for (Double in : Xall.getColumn(0).toArrayDouble()) {
							retAr1.add(in);
						}

						System.out.println("xout 1 " + retAr1.toString());
						retM.add(retAr1);
						return retM;
					}
				});
		// ret.saveAsTextFile(ouput);
		// List<List<Double>> ret2 = new ArrayList<List<Double>>();
		// ret2.add(ret.collect());

		return ret.collect();
	}

	public static List<Tuple2<Integer, List<Double>>> x_slaver3(JavaSparkContext sc,
			JavaPairRDD<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input,
			//final SparseMatrix b, final SparseVector u, final SparseVector z, final double rho, String ouput) {
			final SparseVector z, final double rho, String ouput) {
		final double[] inZA = z.toArrayDouble();
		// final double[] inUA = u.toArrayDouble();
		// final double[] inB = b.toArrayDouble();

		JavaPairRDD<Integer, List<Double>> ret = input.flatMapToPair(
				new PairFlatMapFunction<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>, Integer, List<Double>>() {

					public Iterable<Tuple2<Integer, List<Double>>> call(
							Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> t)
							throws Exception {
						List<Tuple2<Integer, List<Double>>> retP = new ArrayList<Tuple2<Integer, List<Double>>>();
						List<List<Double>> arrayM = new ArrayList<List<Double>>();
						// List<List<Double>> retM = new
						// ArrayList<List<Double>>();
						// System.out.println("x_slaver3 " + t._1);
						for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> double1 : t._2) {
							List<Double> row = new ArrayList<Double>();
							// System.out.println("line "+double1._1);
							for (Tuple2<Integer, Double> ele : double1._2) {
								row.add(ele._2);
							}
							arrayM.add(row);
						}

						// arrayM.size();
						SparseMatrix inM = new SparseMatrix(arrayM);
						// SparseMatrix ret2 = new SparseMatrix(arrayM);
						 inM.print(false, "inM "+t._1);
						arrayM.clear();
						arrayM = null;
						System.out.println(t._1 + "  inM: " + inM.getm() + " " + inM.getn());
						SparseMatrix ATb = inM.getMatrix(0, (inM.getm() - 1) / 2 - 1, 0, inM.getn() - 1);// transpose().mulMatrix(ret);
						System.out.println(t._1 + "  ATb: " + ATb.getm() + " " + ATb.getn());

						// ATA.print(true, "UTU");
						// fw.write("\nUTU:\n" + ATA.toString());
						// b.print(true, "b: ");
						// ATb.print(true, "UTb");

						// 2(A^TA + \rho I_m)^-1
						// SparseMatrix IM = IMtx(A2.n);
						// IMtx(A2.n).scale(rho).print(true,
						// "IMtx(A2.n).scale(rho)");
						SparseMatrix ATA_rho_Im = inM.getMatrix((inM.getm() - 1) / 2, inM.getm() - 2, 0,
								inM.getn() - 1);// ATA.plus(SparseMatrix.IMtx(ret.getn()).scale(rho)).invert().scale(2);
						System.out.println(t._1 + "  ATA_rho_Im: " + ATA_rho_Im.getm() + " " + ATA_rho_Im.getn());

						// ATA_rho_Im.print(true, "ATA_rho_Im "+t._1);

						SparseVector inZ = new SparseVector(inZA);

						SparseVector inU = new SparseVector(inM.getRow(inM.getm() - 1).toArrayDouble());
						/*
						 * x^{k+1} = 2(A^TA + \rho I_m)^-1 [ A^Tb - \rho (z^k -
						 * u^k)]
						 */
						// z.plus(u.scale(-1)).print("z-u");
						// inZ.print("z - " + inZ.size());
						inU.print("u - " + t._1 + ":" + inU.size());
						SparseVector rho_zk_uk = inZ.plus(inU.scale(-1)).scale(rho * (-1.0)); // -
																								// \rho
																								// (z^k
																								// -
																								// u^k)
						// rho_zk_uk.print("rho_zk_uk " + t._1);
						// rho_zk_uk.print("rho_zk_uk - " + rho_zk_uk.size());

						// ATA.getRow(0).print("ATA.getRow(0)");
						// SparseVector ATb_rho_zATbk_uk =
						// ATb.getColumn(0).plus(rho_zk_uk); //A^Tb - \rho (z^k
						// - u^k)
						// ATA.print(true, "ATA " + t._1);
						SparseVector ATb_rho_zATbk_uk = ATb.getRow(0).plus(rho_zk_uk); // A^Tb
																						// -
																						// \rho
																						// (z^k
																						// -
																						// u^k)
						// ATb_rho_zATbk_uk.print("x");
						SparseVector tmp1[] = new SparseVector[1];
						tmp1[0] = ATb_rho_zATbk_uk;
						SparseMatrix tmpM = new SparseMatrix(tmp1, 1, ATb_rho_zATbk_uk.size()).transpose();
						// fw.write("tmpm: "+tmpM.toString());
						// tmpM.print(true, "x");
						SparseMatrix Xall = ATA_rho_Im.mulMatrix(tmpM);
						// Xall.getColumn(0).print("x");
						List<Double> retAr1 = new ArrayList<Double>();
						for (Double in : Xall.getColumn(0).toArrayDouble()) {
							retAr1.add(in);
						}

						// retM.add(retAr1);

						retP.add(new Tuple2<Integer, List<Double>>(t._1, retAr1));
						// System.out.println("xout 1 "+retAr1.toString());
						// System.out.println("end x_slaver "+retP.size());
						return retP;
					}

				});
		// System.out.println("x_slaver3 "+ret.count());
		// ret.saveAsTextFile(ouput);
		// List<List<Double>> ret2 = new ArrayList<List<Double>>();
		// ret2.add(ret.collect());

		return ret.collect();
	}

	public static List<Tuple2<String, List<Double>>> x_slaver4(JavaSparkContext sc,
			JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input,
			//final SparseMatrix b, final SparseVector u, final SparseVector z, final double rho, String ouput) {
//			final SparseVector z, 
			final Broadcast<SparseVector> avrZ, final int size, final double rho, String ouput) {
//		final double[] inZA = z.toArrayDouble();
		// final double[] inUA = u.toArrayDouble();
		// final double[] inB = b.toArrayDouble();

		JavaPairRDD<String, List<Double>> ret = input.flatMapToPair(
				new PairFlatMapFunction<Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>, String, List<Double>>() {

					public Iterable<Tuple2<String, List<Double>>> call(
							Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> t)
							throws Exception {
						SparseVector ATb =null;
						SparseVector inU = null;
						
						SparseVector inZ =avrZ.value();
						SparseMatrix ATA_rho_Im = new SparseMatrix(size, size);
						
						List<Tuple2<String, List<Double>>> retP = new ArrayList<Tuple2<String, List<Double>>>();
//						List<List<Double>> arrayM = new ArrayList<List<Double>>();
						// List<List<Double>> retM = new
						// ArrayList<List<Double>>();
						// System.out.println("x_slaver3 " + t._1);
						int i = 0;
						for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> double1 : t._2) {
//							List<Double> row = new ArrayList<Double>();
							// System.out.println("line "+double1._1);
							SparseVector row = new SparseVector(size);
							for (Tuple2<Integer, Double> ele : double1._2) {
								row.put(ele._1, ele._2);
							}
//							row.print(t._1 + " Row "+"- i "+i+"-"+ double1._1());
							if(i< size)
							{
								ATA_rho_Im.setRow(i, row);
							}
							else if(i > size)
							{
//								row.print(t._1 + " Row U "+"-"+ double1._1());
								inU = row;
							}
							else
							{
//								row.print(t._1 + " Row ATB"+"-"+ double1._1());
								ATb =row;	
							}
							i++;
						}
						
						/*
						 * x^{k+1} = 2(A^TA + \rho I_m)^-1 [ A^Tb - \rho (z^k -
						 * u^k)]
						 */
						SparseVector rho_zk_uk = inZ.plus(inU.scale(-1)).scale(rho * (-1.0)); // -

						SparseVector ATb_rho_zATbk_uk = ATb.plus(rho_zk_uk); // A^Tb

						// ATb_rho_zATbk_uk.print("x");
						SparseVector tmp1[] = new SparseVector[1];
						tmp1[0] = ATb_rho_zATbk_uk;
						SparseMatrix tmpM = new SparseMatrix(tmp1, 1, ATb_rho_zATbk_uk.size()).transpose();
						// fw.write("tmpm: "+tmpM.toString());
						// tmpM.print(true, "x");
						SparseMatrix Xall = ATA_rho_Im.mulMatrix(tmpM);
						// Xall.getColumn(0).print("x");
						List<Double> retAr1 = new ArrayList<Double>();
						for (Double in : Xall.getColumn(0).toArrayDouble()) {
							retAr1.add(in);
						}

						// retM.add(retAr1);

						retP.add(new Tuple2<String, List<Double>>(t._1, retAr1));
						// System.out.println("xout 1 "+retAr1.toString());
						// System.out.println("end x_slaver "+retP.size());
						return retP;
					}

				});
		// System.out.println("x_slaver3 "+ret.count());
		// ret.saveAsTextFile(ouput);
		// List<List<Double>> ret2 = new ArrayList<List<Double>>();
		// ret2.add(ret.collect());

		return ret.collect();
	}
	
	public static JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_rho_Im_slaver(
			final JavaSparkContext sc, JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input,
			final double rho, String ouput) {
		JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ret = input.flatMap(
				new FlatMapFunction<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>() {
					public Iterable<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> call(
							Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> t) throws Exception {

						// System.out.println("ATA_rho_Im_slaver");
						// TODO Auto-generated method stub
						List<List<Double>> arrayM = new ArrayList<List<Double>>();

						for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> double1 : t) {
							List<Double> row = new ArrayList<Double>();
							for (Tuple2<Integer, Double> ele : double1._2) {
								row.add(ele._2);
							}
							arrayM.add(row);
						}
						// arrayM.size();
						SparseMatrix ret = new SparseMatrix(arrayM);
						// ret.print(true, "test");
						System.out.println("transpose " + ret.getm() + " " + ret.getn());
						SparseMatrix ATA = ret.transpose().mulMatrix(ret);
						System.out.println("utu");
						// ATA.print(true, "UTU");
						// fw.write("\nUTU:\n" + ATA.toString());
						// SparseMatrix c = new SparseMatrix(new
						// Matrix(arr)).transpose();
						// SparseMatrix ATb = A2.transpose().mulMatrix(c);
						// b.print(true, "b: ");
						// SparseMatrix ATb =
						// ret.transpose().mulMatrix(b);//.transpose();
						// ATb.print(true, "UTb");

						// 2(A^TA + \rho I_m)^-1
						// SparseMatrix IM = IMtx(A2.n);
						// IMtx(A2.n).scale(rho).print(true,
						// "IMtx(A2.n).scale(rho)");
						SparseMatrix ATA_rho_Im = ATA.plus(SparseMatrix.IMtx(ret.getn()).scale(rho)).invert().scale(2);
						// ATA_rho_Im.print(true, "ATA_rho_Im");
						// System.out.println("ATA");
						List<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> retM = new ArrayList<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>();
						// List<Tuple2<Integer,Iterable<Tuple2<Integer,
						// Double>>>> matrix = new
						// ArrayList<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>();
						// int i = 0;
						// for (SparseVector d : ATA_rho_Im.getRows()) {
						// List<Tuple2<Integer, Double>> row = new
						// ArrayList<Tuple2<Integer,Double>>();
						// for(int j = 0; j< d.size(); j++)
						// {
						// row.add(new Tuple2<Integer, Double>(j, d.get(j)));
						// }
						// matrix.add(new Tuple2<Integer,
						// Iterable<Tuple2<Integer, Double>>> (i, row));
						// i++;
						// }
						retM.add(SparkMatrix.toRDDMatrix(ATA_rho_Im));

						// retM.add(toRDDMatrix(ATA));
						return retM;
					}
				});
		return ret;//
	}

	public static JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA(final JavaSparkContext sc,
			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input, String ouput) {
		JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ret = input.flatMap(
				new FlatMapFunction<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>() {
					public Iterable<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> call(
							Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> t) throws Exception {

						// System.out.println("ATA");
						// TODO Auto-generated method stub
						List<List<Double>> arrayM = new ArrayList<List<Double>>();

						for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> double1 : t) {
							List<Double> row = new ArrayList<Double>();
							for (Tuple2<Integer, Double> ele : double1._2) {
								row.add(ele._2);
							}
							arrayM.add(row);
						}
						// arrayM.size();
						SparseMatrix ret = new SparseMatrix(arrayM);
						// ret.print(true, "test");

						SparseMatrix ATA = ret.transpose().mulMatrix(ret);

						List<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> retM = new ArrayList<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>();
						// List<Tuple2<Integer,Iterable<Tuple2<Integer,
						// Double>>>> matrix = new
						// ArrayList<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>();
						// int i = 0;
						// for (SparseVector d : ATA.getRows()) {
						// List<Tuple2<Integer, Double>> row = new
						// ArrayList<Tuple2<Integer,Double>>();
						// for(int j = 0; j< d.size(); j++)
						// {
						// row.add(new Tuple2<Integer, Double>(j, d.get(j)));
						// }
						// matrix.add(new Tuple2<Integer,
						// Iterable<Tuple2<Integer, Double>>> (i, row));
						// i++;
						// }
						retM.add(SparkMatrix.toRDDMatrix(ATA));
						return retM;
					}
				});
		return ret;//
	}

	
	public static JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> invert(
			final JavaSparkContext sc, JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input,
			final double rho, String ouput) {
		JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ret = input.flatMap(
				new FlatMapFunction<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>() {
					public Iterable<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> call(
							Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> t) throws Exception {

						// TODO Auto-generated method stub
						List<List<Double>> arrayM = new ArrayList<List<Double>>();

						for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> double1 : t) {
							List<Double> row = new ArrayList<Double>();
							for (Tuple2<Integer, Double> ele : double1._2) {
								row.add(ele._2);
							}
							arrayM.add(row);
						}
						// arrayM.size();
						SparseMatrix ret = new SparseMatrix(arrayM);
						// ret.print(true, "test");

						SparseMatrix ATA = ret.transpose().mulMatrix(ret);

						// "IMtx(A2.n).scale(rho)");
						SparseMatrix ATA_rho_Im = ATA.plus(SparseMatrix.IMtx(ret.getn()).scale(rho)).invert().scale(2);
						// ATA_rho_Im.print(true, "ATA_rho_Im");

						List<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> retM = new ArrayList<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>();

						retM.add(SparkMatrix.MergeMatrixtoRDDMatrix(ATA, ATA_rho_Im));
						// retM.add(toRDDMatrix(ATA_rho_Im));

						return retM;
						// return sc.parallelize(retM);

					}
				});
		// toMatrixJ(sc, ATA_rho_Im);
		return ret;//
	}

	public static JavaPairRDD<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> invert2(
			final JavaSparkContext sc,
			JavaPairRDD<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input, final double rho,
			String ouput) {

		JavaPairRDD<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ret = input.flatMapToPair(
				new PairFlatMapFunction<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>, Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>() {

					public Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> call(
							Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> t)
							throws Exception {
						// TODO Auto-generated method stub
//						System.out.println("Start Invert slaver " + Long.toString(System.currentTimeMillis()));
						List<List<Double>> arrayM = new ArrayList<List<Double>>();

						for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> double1 : t._2) {
							List<Double> row = new ArrayList<Double>();
							for (Tuple2<Integer, Double> ele : double1._2) {
								row.add(ele._2);
							}
							arrayM.add(row);
						}
						// arrayM.size();
						SparseMatrix ret = new SparseMatrix(arrayM);
						// ret.print(true, "test " + t._1);
						// System.out.println("invert2");
						// TODO: mul and invert by parallel
//						System.out.println("Start Invert slaver 1 " + Long.toString(System.currentTimeMillis()));
						SparseMatrix ATA = ret.transpose().mulMatrix(ret);
//						System.out.println("Start Invert slaver 2 " + Long.toString(System.currentTimeMillis()));

						SparseMatrix ATA_rho_Im = ATA.plus(SparseMatrix.IMtx(ret.getn()).scale(rho)).invert().scale(2);
						// ATA_rho_Im.print(true, "ATA_rho_Im");
//						System.out.println("Start Invert slaver 3 " + Long.toString(System.currentTimeMillis()));
						List<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> retP = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>>();
						retP.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>(t._1,
								SparkMatrix.MergeMatrixtoRDDMatrix(ATA, ATA_rho_Im)));
//						System.out.println("Start Invert slaver 4 " + Long.toString(System.currentTimeMillis()));
						return retP;
					}
				});

		return ret;//
	}

	public static JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATb_ATA_rho_Im_invert(
			final JavaSparkContext sc,
			JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input, final int indB , final double rho ) {

		JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ret = input.flatMapToPair(
				new PairFlatMapFunction<Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>, String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>() {

					public Iterable<Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> call(
							Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> t)
							throws Exception {
						// TODO Auto-generated method stub
//						System.out.println("Start Invert slaver " + Long.toString(System.currentTimeMillis()));
						List<List<Double>> arrayM = new ArrayList<List<Double>>();

						for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> double1 : t._2) {
							List<Double> row = new ArrayList<Double>();
							for (Tuple2<Integer, Double> ele : double1._2) {
								row.add(ele._2);
							}
							arrayM.add(row);
						}
						// arrayM.size();
						SparseMatrix ret = new SparseMatrix(arrayM);
						// ret.print(true, "test " + t._1);
						// System.out.println("invert2");
						// TODO: mul and invert by parallel
//						System.out.println("Start Invert slaver 1 " + Long.toString(System.currentTimeMillis()));
						SparseMatrix ATb = ret.transpose().mulMatrix(ret.getColumn(indB).toSpaceMatrix().transpose());
//						ATb.print(false, "ATb");
						SparseMatrix ATA = ret.transpose().mulMatrix(ret);
						
//						System.out.println("Start Invert slaver 2 " + Long.toString(System.currentTimeMillis()));
						// IMtx(A2.n).scale(rho).print(true,
						// "IMtx(A2.n).scale(rho)");
						SparseMatrix ATA_rho_Im = ATA.plus(SparseMatrix.IMtx(ret.getn()).scale(rho)).invert().scale(2);
						// ATA_rho_Im.print(true, "ATA_rho_Im");
//						System.out.println("Start Invert slaver 3 ("+ATA_rho_Im.getm()+ "-"+ATA_rho_Im.getn()+") " + Long.toString(System.currentTimeMillis()));
						List<Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> retP = new ArrayList<Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>>();
						/*
						 * This inculde ATA_rho_Im and ATA = n(ATA[i]) = nATbi
						 */
						retP.add(new Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>(t._1,
								SparkMatrix.MergeMatrixtoRDDMatrix(ATA_rho_Im, ATb.transpose())));
//						System.out.println("Start Invert slaver 4 " + Long.toString(System.currentTimeMillis()));
						return retP;
					}
				});

		return ret;//
	}
	
	public static JavaPairRDD<String, Tuple2<Tuple2<Integer, Integer>, Double>> ATb_ATA_rho_Im_invert2(
			final JavaSparkContext sc,
			final JavaPairRDD<String, Tuple2<Tuple2<Integer, Integer>, Double>> input, final double rho ) {

		final List<String> matName = input.keys().collect();
		
		JavaPairRDD<String, Tuple2<Tuple2<Integer, Integer>, Double>> ret = input.flatMapToPair(
				new PairFlatMapFunction<Tuple2<String, Tuple2<Tuple2<Integer, Integer>, Double>>, String, Tuple2<Tuple2<Integer, Integer>, Double>>() {

					public Iterable<Tuple2<String, Tuple2<Tuple2<Integer, Integer>, Double>>> call(
							Tuple2<String, Tuple2<Tuple2<Integer, Integer>, Double>> t)
							throws Exception {
						// TODO Auto-generated method stub
//						System.out.println("Start Invert slaver ("+matName.size()+")---" + Long.toString(System.currentTimeMillis()));
						List<List<Double>> arrayM = new ArrayList<List<Double>>();

//						for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> double1 : t._2) {
//							List<Double> row = new ArrayList<Double>();
//							for (Tuple2<Integer, Double> ele : double1._2) {
//								row.add(ele._2);
//							}
//							arrayM.add(row);
//						}
						
						// arrayM.size();
						SparseMatrix ret = new SparseMatrix(arrayM);
						
//						for(String mat: matName)
						{
							if(t._1.equals(matName))
							{
								ret.put(t._2._1._1, t._2._1._2, t._2._2);
							}
						}
						
						// ret.print(true, "test " + t._1);
						// System.out.println("invert2");
						// TODO: mul and invert by parallel
//						System.out.println("Start Invert slaver 1 " + Long.toString(System.currentTimeMillis()));
						SparseMatrix ATb = ret.transpose().mulMatrix(ret);

						SparseMatrix ATA_rho_Im = ATb.plus(SparseMatrix.IMtx(ret.getn()).scale(rho)).invert().scale(2);
						// ATA_rho_Im.print(true, "ATA_rho_Im");
//						System.out.println("Start Invert slaver 3 ("+ATA_rho_Im.getm()+ "-"+ATA_rho_Im.getn()+") " + Long.toString(System.currentTimeMillis()));
						List<Tuple2<String, Tuple2<Tuple2<Integer, Integer>, Double>>> retP = new ArrayList<Tuple2<String, Tuple2<Tuple2<Integer, Integer>, Double>>>();
						/*
						 * This inculde ATA_rho_Im and ATA = n(ATA[i]) = nATbi
						 */
//						retP.add(new Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>(t._1,
//								SparkMatrix.MergeMatrixtoRDDMatrix(ATb, ATA_rho_Im)));
//						System.out.println("Start Invert slaver 4 " + Long.toString(System.currentTimeMillis()));
						return retP;
					}
				}).reduceByKey(new Function2<Tuple2<Tuple2<Integer,Integer>,Double>, Tuple2<Tuple2<Integer,Integer>,Double>, Tuple2<Tuple2<Integer,Integer>,Double>>() {
					
					@Override
					public Tuple2<Tuple2<Integer, Integer>, Double> call(Tuple2<Tuple2<Integer, Integer>, Double> v1,
							Tuple2<Tuple2<Integer, Integer>, Double> v2) throws Exception {
						// TODO Auto-generated method stub
						
						return null;
					}
				});

		return ret;//
	}
	public static SparseVector ADMM_1(SparseMatrix A, int ib, double lambda, double rho, double epsilonA,
			double epsilonR) throws IOException {
		SparseMatrix b = A.getColumn(ib).toSpaceMatrix().transpose();
		return ADMM_1(A, b, lambda, rho, epsilonA, epsilonR);
	}
	/*
	 * m is doc, n is term, n>>m A = U (m row- n column) b = a (m row- 1 column)
	 * z0 = x0 u0 = b rho = 0.1 ->10 lambda = 10% -> 50%
	 * 
	 */
	public static SparseVector ADMM_1(SparseMatrix A, SparseMatrix b, double lambda, double rho, double epsilonA,
			double epsilonR) throws IOException {
		SparseVector ret = null;
		File fileut;
		FileWriter fw;
		// double arr[][] = {{2,1,3,1}};
		
		System.out.println("Am: " + A.getm() + " An: " + A.getn() + "---- bm: " + b.getm() + " bn: " + b.getn());
		if (A.getm() != b.getm())
			throw new UnsupportedOperationException("Not supported yet Am=! b.m");
		String filename = "Output/" + Long.toString(System.currentTimeMillis()) + "/"; //
		new File(filename).mkdir();
		String nfilename = filename + "log.txt"; // Long.toString(System.currentTimeMillis()),
													// +
													// post.getCreatedTime().toString().trim()
		fileut = new File(nfilename);
		fw = new FileWriter(fileut);

		// A.print(true,"A " +A.m+"-"+A.n);
		SparseMatrix A2 = A.copy();// echolonConvert();
		// A2.print(true,"U (echelon of A) " +A2.m+"-"+A2.n);

		fw.write("U (echelon of A) \n" + A2.toString());

		SparseVector x = new SparseVector(A.getn());
		SparseVector z = new SparseVector(A.getm());
		SparseVector u = new SparseVector(A.getm());

		// IMtx(A2.n).print(true, "IMTX");
		// A2.mulMatrix(A2.transpose()).plus(IMtx(A2.m)).print(true, "AAT");

		SparseMatrix ATA = A2.transpose().mulMatrix(A2);
		// ATA.print(true, "UTU");
		// fw.write("\nUTU:\n" + ATA.toString());
		// SparseMatrix c = new SparseMatrix(new Matrix(arr)).transpose();
		// SparseMatrix ATb = A2.transpose().mulMatrix(c);
		// b.print(true, "b: ");
		SparseMatrix ATb = A2.transpose().mulMatrix(b);// .transpose();
		// ATb.print(true, "UTb");

		// 2(A^TA + \rho I_m)^-1
		// SparseMatrix IM = IMtx(A2.n);
		// IMtx(A2.n).scale(rho).print(true, "IMtx(A2.n).scale(rho)");
		SparseMatrix ATA_rho_Im = ATA.plus(SparseMatrix.IMtx(A2.getn()).scale(rho)).invert().scale(2);
		// ATA_rho_Im.print(true, "UTU+ rho*Im");
		// fw.write("\n UTU+ rho*Im:\n" + ATA_rho_Im.toString());
		int n = 0;
		// while(n<A.n)
		{
			fw.write(
					"\n================================================================================================"
							+ n);
			z = ATA.getColumn(n);
			// z.print("z0");
			fw.write("\nzo:\n" + z.toString());
			u = z.scale(0.5);
			// u.print("u0");
			fw.write("\nuo:\n" + u.toString());

			// System.out.println("============================================================");

			fw.write(
					"\n================================================================================================"
							+ n);
			int i = 0;
			while (true) {
				i++;
				System.out.println("loop time: " + i);
				fw.write("\nloop time: " + i + "\n");
				if (i > 200)
					break;

				SparseVector x_pre = x.copy();
				SparseVector z_pre = z.copy();
				SparseVector u_pre = u.copy();
				/*
				 * x^{k+1} = 2(A^TA + \rho I_m)^-1 [ A^Tb - \rho (z^k - u^k)]
				 */
				// z.plus(u.scale(-1)).print("z-u");
				SparseVector rho_zk_uk = z.plus(u.scale(-1)).scale(rho * (-1.0)); // -
																					// \rho
																					// (z^k
																					// -
																					// u^k)
				// in2.print("in2");

				// SparseVector ATb_rho_zATbk_uk =
				// ATb.getColumn(0).plus(rho_zk_uk); //A^Tb - \rho (z^k - u^k)
				SparseVector ATb_rho_zATbk_uk = ATA.getRow(n).plus(rho_zk_uk); // A^Tb
																				// -
																				// \rho
																				// (z^k
																				// -
																				// u^k)

				SparseVector tmp1[] = new SparseVector[1];
				tmp1[0] = ATb_rho_zATbk_uk;
				SparseMatrix tmpM = new SparseMatrix(tmp1, 1, ATb_rho_zATbk_uk.size()).transpose();
				// fw.write("tmpm: "+tmpM.toString());
				// tmpM.print(true, "tmpm");
				SparseMatrix Xall = ATA_rho_Im.mulMatrix(tmpM);

				// ATA_rho_Im.print(true, "ATA_rho_Im");
				// ATb_rho_zATbk_uk.print("ATb_rho_zATbk_uk");

				x = Xall.getColumn(0);// .print(true, "Xxx");// ;
				// fw.write("\nrho_zk_uk: "+rho_zk_uk.toString());
				// rho_zk_uk.print("rho_zk_uk");
				// fw.write("\nXall: "+Xall.toString());
				// Xall.print(true, "Xall");

				// ATb_rho_zATbk_uk.print("ATb_rho_zATbk_uk");
				// fw.write("\nATb_rho_zATbk_uk: "+ATb_rho_zATbk_uk.toString());
				// x.print("Xk "+i);
				fw.write("\nXk " + i + ":" + x.toString());
				/*
				 * z^{k+1} = S_{\frag{\lambda}{\rho}}(x^{k+1} + u^k) = x^{k+1}
				 * +u^k - frag{\lambda}{\rho} if > = 0 if || \leq = x^{k+1} +u^k
				 * + frag{\lambda}{\rho} if <
				 */
				SparseVector tmp = x.plus(u);
				// tmp.print("x+u ");
				// fw.write("\n x+u :"+tmp.toString());
				// SparseVector zTmp = z.copy();
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						// System.out.println(tmp.get(j) - lambda/(rho/2));
						z.put(j, tmp.get(j) - lambda / (rho), false);
						u.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						// System.out.println(tmp.get(j)+"<"+ lambda/rho);
						z.put(j, tmp.get(j) + lambda / (rho), false);
						u.put(j, -lambda / (rho), false);
					} else {
						// System.out.println(tmp.get(j)+"***"+ lambda/rho);
						z.put(j, 0, false);
						u.put(j, x.get(j) + u.get(j), false);
					}
					// z.print("Zk "+i);
				}

				// z.print("Zk "+i);
				fw.write("\nZk " + i + ":" + z.toString());
				/*
				 * u^{k+1} = u^k + x^{k+1} - z^{k+1}
				 */
				// u.print("uk "+i);
				fw.write("\nuk " + i + ":" + u.toString());

				/*
				 * Check stop condition
				 */
				double r = x_pre.plus(z_pre.scale(-1)).norm();
				double s = z_pre.plus(z.scale(-1.0)).scale(rho).norm();

				double eP = (x_pre.norm() > z_pre.norm()) ? (epsilonA * Math.sqrt(A.getn()) + epsilonR * x_pre.norm())
						: (epsilonA * Math.sqrt(A.getn()) + epsilonR * z_pre.norm());
				double eD = epsilonA * Math.sqrt(A.getm()) + epsilonR * (u_pre.scale(rho).norm());
				// System.out.println("x.norm(): ("+ x_pre.norm()
				// +")---z.norm(): ("+z_pre.norm()+")");
				// System.out.println("r: ("+ r +")---eP: ("+eP+")");
				// System.out.println("s: ("+ s +")---eD: ("+eD+")");
				fw.write("\nx.norm(): (" + x_pre.norm() + ")---z.norm(): (" + z_pre.norm() + ")\n" + "r: (" + r
						+ ")---eP: (" + eP + ")\n" + "s: (" + s + ")---eD: (" + eD + ")\n");

				if ((r <= eP) && (s <= eD)) {
					// System.out.println("r<= eP) && (s<=eD) ---- STOP LOOP");
					fw.write("(r<= eP) && (s<=eD) ---- STOP LOOP\n");
					// fw.close();
					// return x;
				}
				if (s <= eD) {
					// System.out.println("s<=eD");
					fw.write("(s<=eD)");
					// fw.close();
					n++;
					break;
					// return x;
				}
				if (r <= eP) {
					// System.out.println("r<= eP");
					fw.write("r<= eP)");
					// fw.close();
					// return x;
				}

				if (r > (s * 10))
					rho = rho * 2;
				else if (s > (r * 10))
					rho = rho / 2;
				else
					rho = rho;

				// fw.write("rho "+rho);
				// System.out.println("rho "+rho);
				fw.write("rho " + rho + "\n============================================================");
				// System.out.println("============================================================");
			}
		}
		fw.close();
		return x;

	}

	public static SparseMatrix ADMM_FullMatrix(SparseMatrix A, double lambda, double rho, double epsilonA, double epsilonR)
			throws IOException {
		SparseMatrix ret = null;

		SparseMatrix X = new SparseMatrix(A.getn(), A.getm());
		SparseMatrix Z = new SparseMatrix(A.getm(), A.getm());
		SparseMatrix U = new SparseMatrix(A.getm(), A.getm());

		SparseMatrix ATA = A.transpose().mulMatrix(A);
		SparseMatrix ATb = A.transpose().mulMatrix(A.getColumn(0).toSpaceMatrix());// .transpose();
																					// //TODO
																					// with
																					// m
																					// column
		SparseMatrix ATA_rho_Im = ATA.plus(SparseMatrix.IMtx(A.getn()).scale(rho)).invert().scale(2);
		int n = 0;

		while (n < A.getn()) {
			SparseVector row = ADMM_1(A, A.getColumn(n).toSpaceMatrix().transpose(), lambda, rho, epsilonA, epsilonR);
			n++;
			X.setRow(n, row);
			//// fw.write("\n================================================================================================"+n);
			// z = ATA.getColumn(n);
			// u = z.scale(0.5);
			//// u.print("u0");
			//
			//
			// int i =0;
			// while(true)
			// {
			// i++;
			// System.out.println("loop time: "+i);
			// if(i> 500)
			// break;
			//
			// SparseVector x_pre = x.copy();
			// SparseVector z_pre = z.copy();
			// SparseVector u_pre = u.copy();
			// /*
			// * x^{k+1} = 2(A^TA + \rho I_m)^-1 [ A^Tb - \rho (z^k - u^k)]
			// */
			//// z.plus(u.scale(-1)).print("z-u");
			// SparseVector rho_zk_uk = z.plus(u.scale(-1)).scale(rho*(-1.0));
			//// //- \rho (z^k - u^k)
			//// in2.print("in2");
			//
			//// SparseVector ATb_rho_zATbk_uk =
			//// ATb.getColumn(0).plus(rho_zk_uk); //A^Tb - \rho (z^k - u^k)
			// SparseVector ATb_rho_zATbk_uk = ATA.getRow(n).plus(rho_zk_uk);
			//// //A^Tb - \rho (z^k - u^k)
			//
			// SparseVector tmp1[]= new SparseVector[1];
			// tmp1[0] = ATb_rho_zATbk_uk;
			// SparseMatrix tmpM = new
			//// SparseMatrix(tmp1,1,ATb_rho_zATbk_uk.size()).transpose();
			//// fw.write("tmpm: "+tmpM.toString());
			//// tmpM.print(true, "tmpm");
			// SparseMatrix Xall = ATA_rho_Im.mulMatrix(tmpM);
			//
			//// ATA_rho_Im.print(true, "ATA_rho_Im");
			//// ATb_rho_zATbk_uk.print("ATb_rho_zATbk_uk");
			//
			// x= Xall.getColumn(0);//.print(true, "Xxx");// ;
			// fw.write("\nXk "+i+":"+x.toString());
			// /*
			// * z^{k+1} = S_{\frag{\lambda}{\rho}}(x^{k+1} + u^k)
			// * = x^{k+1} +u^k - frag{\lambda}{\rho} if >
			// * = 0 if || \leq
			// * = x^{k+1} +u^k + frag{\lambda}{\rho} if <
			// */
			// SparseVector tmp = x.plus(u);
			// for(int j =0; j<tmp.size(); j++)
			// {
			//
			// if(tmp.get(j) > lambda/rho)
			// {
			//// System.out.println(tmp.get(j) - lambda/(rho/2));
			// z.put(j, tmp.get(j) - lambda/(rho), false);
			// u.put(j, lambda/(rho), false);
			// }
			// else if (tmp.get(j) < -1.0*(lambda/rho))
			// {
			//// System.out.println(tmp.get(j)+"<"+ lambda/rho);
			// z.put(j, tmp.get(j) + lambda/(rho), false);
			// u.put(j, -lambda/(rho), false);
			// }
			// else
			// {
			//// System.out.println(tmp.get(j)+"***"+ lambda/rho);
			// z.put(j, 0, false);
			// u.put(j, x.get(j)+u.get(j) , false);
			// }
			// }
			//
			// /*
			// * u^{k+1} = u^k + x^{k+1} - z^{k+1}
			// */
			//
			// /*
			// * Check stop condition
			// */
			// double r = x_pre.plus(z_pre.scale(-1)).norm();
			// double s = z_pre.plus(z.scale(-1.0)).scale(rho).norm();
			//
			//
			// double eP = (x_pre.norm()>z_pre.norm())?(epsilonA *
			//// Math.sqrt(A.getn()) + epsilonR*x_pre.norm()):(epsilonA *
			//// Math.sqrt(A.getn()) + epsilonR*z_pre.norm());
			// double eD = epsilonA * Math.sqrt(A.getm()) +
			//// epsilonR*(u_pre.scale(rho).norm());
			//
			// if((r<= eP) && (s<=eD))
			// {
			//// return x;
			// }
			// if(s<=eD)
			// {
			//// System.out.println("s<=eD");
			// n++;
			// break;
			//// return x;
			// }
			// if(r<= eP)
			// {
			//// System.out.println("r<= eP");
			//// return x;
			// }
			//
			//
			// if(r>(s*10))
			// rho = rho*2;
			// else if(s>(r*10))
			// rho = rho/2;
			// else
			// rho = rho;
			//
			// }
		}
		return ret;
	}

	public static SparseVector ADMMMatrixN(JavaSparkContext sc, SparseMatrix A, SparseMatrix b, double lambda,
			double rho, double epsilonA, double epsilonR, String output) throws IOException {

		System.out.println("ADMMMatrixN");
		SparseVector ret = null;
		File fileut;
		FileWriter fw;
		// double arr[][] = {{2,1,3,1}};
		int part = A.getm();
		System.out.println("Am: " + A.getm() + " An: " + A.getn() + "---- bm: " + b.getm() + " bn: " + b.getn());
		// if(A.getm() != b.getm())
		// throw new UnsupportedOperationException("Not supported yet Am=!
		// b.m");
		String filename = output + Long.toString(System.currentTimeMillis()) + "/"; //
		new File(filename).mkdir();
		String nfilename = filename + "log.txt"; // Long.toString(System.currentTimeMillis()),
													// +
													// post.getCreatedTime().toString().trim()
		fileut = new File(nfilename);
		fw = new FileWriter(fileut);

		// A.print(true,"A " +A.m+"-"+A.n);
		// SparseMatrix A2 = A.copy();// echolonConvert();
		// A2.print(true,"U (echelon of A) " +A2.m+"-"+A2.n);

		fw.write("U ( of A) \n" + A.toString());

		SparseVector x = new SparseVector(A.getn());
		SparseVector x1 = new SparseVector(A.getn());
		SparseVector x2 = new SparseVector(A.getn());
		SparseVector x3 = new SparseVector(A.getn());
		SparseVector x4 = new SparseVector(A.getn());
		SparseVector z = new SparseVector(A.getn());
		SparseVector u = new SparseVector(A.getn());
		SparseVector u1 = new SparseVector(A.getn());
		SparseVector u2 = new SparseVector(A.getn());
		SparseVector u3 = new SparseVector(A.getn());
		SparseVector u4 = new SparseVector(A.getn());

		// IMtx(A2.n).print(true, "IMTX");
		// A2.mulMatrix(A2.transpose()).plus(IMtx(A2.m)).print(true, "AAT");

		// SparseMatrix ATA = A2.transpose().mulMatrix(A2);
		// ATA.print(true, "UTU");
		// fw.write("\nUTU:\n" + ATA.toString());
		// SparseMatrix c = new SparseMatrix(new Matrix(arr)).transpose();
		// SparseMatrix ATb = A2.transpose().mulMatrix(c);
		// b.print(true, "b: ");
		// SparseMatrix ATb = A2.transpose().mulMatrix(b);//.transpose();
		// ATb.print(true, "UTb");

		// 2(A^TA + \rho I_m)^-1
		// SparseMatrix IM = IMtx(A2.n);
		// IMtx(A2.n).scale(rho).print(true, "IMtx(A2.n).scale(rho)");
		// SparseMatrix ATA_rho_Im =
		// ATA.plus(SparseMatrix.IMtx(A2.getn()).scale(rho)).invert().scale(2);
		// ATA_rho_Im.print(true, "UTU+ rho*Im");
		// fw.write("\n UTU+ rho*Im:\n" + ATA_rho_Im.toString());
		int n = 0;
		// while(n<A.n)
		{
			fw.write(
					"\n================================================================================================"
							+ n);
			z = A.getRow(1);// Column(1);
			z.print("z0");
			fw.write("\nzo:\n" + z.toString());
			// u = z.scale(0.5);
			// u.print("u0");
			fw.write("\nuo:\n" + u.toString());

			// System.out.println("============================================================");

			fw.write(
					"\n================================================================================================"
							+ n);
			int i = 0;
			System.out.println(" n:" + A.getn() + " - " + part / 4 + "- " + part / 2 + " - " + part * 3 / 2);

			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input1 = util.toListArray2D(sc,
					A.getMatrix(0, (part / 4) - 1, 0, A.getn() - 1));
			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input2 = util.toListArray2D(sc,
					A.getMatrix(part / 4, (part / 2) - 1, 0, A.getn() - 1));
			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input3 = util.toListArray2D(sc,
					A.getMatrix(part / 2, (3 * part / 4) - 1, 0, A.getn() - 1));
			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input4 = util.toListArray2D(sc,
					A.getMatrix(3 * part / 4, part - 1, 0, A.getn() - 1));

			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_1 = ATA(sc, input1,
					output + "/ATA_rho_Im_slaver" + Long.toString(System.currentTimeMillis()));
			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_2 = ATA(sc, input2,
					output + "/ATA_rho_Im_slaver" + Long.toString(System.currentTimeMillis()));
			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_3 = ATA(sc, input3,
					output + "/ATA_rho_Im_slaver" + Long.toString(System.currentTimeMillis()));
			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_4 = ATA(sc, input4,
					output + "/ATA_rho_Im_slaver" + Long.toString(System.currentTimeMillis()));

			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_rho_1 = ATA_rho_Im_slaver(sc,
					input1, rho, output + "/ATA_rho_Im_slaver" + Long.toString(System.currentTimeMillis()));
			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_rho_2 = ATA_rho_Im_slaver(sc,
					input2, rho, output + "/ATA_rho_Im_slaver" + Long.toString(System.currentTimeMillis()));
			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_rho_3 = ATA_rho_Im_slaver(sc,
					input3, rho, output + "/ATA_rho_Im_slaver" + Long.toString(System.currentTimeMillis()));
			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_rho_4 = ATA_rho_Im_slaver(sc,
					input4, rho, output + "/ATA_rho_Im_slaver" + Long.toString(System.currentTimeMillis()));

			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_ATA_rho_1 = ATA_1
					.union(ATA_rho_1);

			List<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> rowall = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
			for (Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> row : ATA_ATA_rho_1.collect()) {
				for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> tupe : row) {
					rowall.add(tupe);
				}
			}
			List<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> babdfb = new ArrayList<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>();
			babdfb.add(rowall);
			ATA_ATA_rho_1 = sc.parallelize(babdfb);

			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_ATA_rho_2 = ATA_2
					.union(ATA_rho_2);
			rowall = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
			for (Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> row : ATA_ATA_rho_2.collect()) {
				for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> tupe : row) {
					rowall.add(tupe);
				}
			}
			babdfb = new ArrayList<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>();
			babdfb.add(rowall);
			ATA_ATA_rho_2 = sc.parallelize(babdfb);

			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_ATA_rho_3 = ATA_3
					.union(ATA_rho_3);
			rowall = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
			for (Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> row : ATA_ATA_rho_3.collect()) {
				for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> tupe : row) {
					rowall.add(tupe);
				}
			}
			babdfb = new ArrayList<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>();
			babdfb.add(rowall);
			ATA_ATA_rho_3 = sc.parallelize(babdfb);

			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_ATA_rho_4 = ATA_4
					.union(ATA_rho_4);
			rowall = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
			for (Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> row : ATA_ATA_rho_4.collect()) {
				for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> tupe : row) {
					rowall.add(tupe);
				}
			}
			babdfb = new ArrayList<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>();
			babdfb.add(rowall);
			ATA_ATA_rho_4 = sc.parallelize(babdfb);

			// ATA_ATA_rho_1.collect();//
			// saveAsTextFile(output+"/ATA_ATA_rho_1_slaver"+
			// Long.toString(System.currentTimeMillis()));

			while (true) {
				i++;
				System.out.println("============================================================");
				System.out.println("loop time: " + i);

				fw.write("\nloop time: " + i + "\n");
				if (i > 500)
					break;

				SparseVector x_pre = x.copy();
				SparseVector z_pre = z.copy();
				SparseVector u_pre = u.copy();
				/*
				 * x^{k+1} = 2(A^TA + \rho I_m)^-1 [ A^Tb - \rho (z^k - u^k)]
				 */

				x1 = new SparseVector(x_slaver(sc, ATA_ATA_rho_1,/* b,*/ u1, z, rho,
						output + "/x1testNew" + Long.toString(System.currentTimeMillis())));

				x2 = new SparseVector(x_slaver(sc, ATA_ATA_rho_2,/* b,*/ u2, z, rho,
						output + "/x2testNew" + Long.toString(System.currentTimeMillis())));

				x3 = new SparseVector(x_slaver(sc, ATA_ATA_rho_3,/* b,*/ u3, z, rho,
						output + "/x3testNew" + Long.toString(System.currentTimeMillis())));

				x4 = new SparseVector(x_slaver(sc, ATA_ATA_rho_4,/* b,*/ u4, z, rho,
						output + "/x4testNew" + Long.toString(System.currentTimeMillis())));

				// x1 = new SparseVector(x_slaver(sc, toListArray2D(sc,
				// A.getMatrix(0, 13, 0, part/4).transpose()),b,u1,z,rho,
				// output+"/x1testNew"+
				// Long.toString(System.currentTimeMillis())));
				// x2 = new SparseVector(x_slaver(sc, toListArray2D(sc,
				// A.getMatrix(0, 13,part/4,
				// part/2).transpose()),b,u2,z,rho,output+"/x2testNew"+
				// Long.toString(System.currentTimeMillis())));
				// x3 = new SparseVector(x_slaver(sc, toListArray2D(sc,
				// A.getMatrix(0, 13, part/2, 3*part/4).transpose()),b,u3,z,rho,
				// output+"/x3testNew"+
				// Long.toString(System.currentTimeMillis())));
				// x4 = new SparseVector(x_slaver(sc, toListArray2D(sc,
				// A.getMatrix(0, 13, 3*part/4,
				// part-1).transpose()),b,u4,z,rho,output+"/x4testNew"+
				// Long.toString(System.currentTimeMillis())));

				x = x1.plus(x2).plus(x3).plus(x4).scale(0.25);//
				x1.print("X1");
				x2.print("X2");
				x3.print("X3");
				x4.print("X4");
				x.print("Xxx");// ;

				fw.write("\nu1k " + i + ":" + u1.toString());
				fw.write("\nu2k " + i + ":" + u2.toString());
				fw.write("\nu3k " + i + ":" + u3.toString());
				fw.write("\nu4k " + i + ":" + u4.toString());
				/*
				 * u^{k+1} = u^k + x^{k+1} - z^{k+1}
				 */
				// u.print("uk "+i);
				fw.write("\nuk " + i + ":" + u.toString());

				fw.write("\nx1: " + x1.toString());
				fw.write("\nx2: " + x2.toString());
				fw.write("\nx3: " + x3.toString());
				fw.write("\nx4: " + x4.toString());
				// fw.write("x: "+x.toString());
				// u= u1.plus(u2).plus(u3).plus(u4).scale(4);//
				// u.print("Xxx");// ;

				// fw.write("\nrho_zk_uk: "+rho_zk_uk.toString());
				// rho_zk_uk.print("rho_zk_uk");
				// fw.write("\nXall: "+Xall.toString());
				// Xall.print(true, "Xall");

				// ATb_rho_zATbk_uk.print("ATb_rho_zATbk_uk");
				// fw.write("\nATb_rho_zATbk_uk: "+ATb_rho_zATbk_uk.toString());
				// x.print("Xk "+i);
				fw.write("\nXk " + i + ":" + x.toString());
				/*
				 * z^{k+1} = S_{\frag{\lambda}{\rho}}(x^{k+1} + u^k) = x^{k+1}
				 * +u^k - frag{\lambda}{\rho} if > = 0 if || \leq = x^{k+1} +u^k
				 * + frag{\lambda}{\rho} if <
				 */
				SparseVector tmp = x.plus(u);
				// tmp.print("x+u ");
				// fw.write("\n x+u :"+tmp.toString());
				// SparseVector zTmp = z.copy();
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						z.put(j, tmp.get(j) - lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						z.put(j, tmp.get(j) + lambda / (rho), false);
					} else {
						z.put(j, 0, false);
					}
				}

				tmp = x1.plus(u1);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u1.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u1.put(j, -lambda / (rho), false);
					} else {
						u1.put(j, x1.get(j) + u1.get(j), false);
					}
				}

				tmp = x2.plus(u2);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u2.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u2.put(j, -lambda / (rho), false);
					} else {
						u2.put(j, x2.get(j) + u2.get(j), false);
					}
				}

				tmp = x3.plus(u3);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u3.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u3.put(j, -lambda / (rho), false);
					} else {
						u3.put(j, x3.get(j) + u3.get(j), false);
					}
				}

				tmp = x4.plus(u4);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u4.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u4.put(j, -lambda / (rho), false);
					} else {
						u4.put(j, x4.get(j) + u4.get(j), false);
					}
				}

				u = u1.plus(u2).plus(u3).plus(u4).scale(0.25);//

				z.print("Zk " + i);
				fw.write("\nZk " + i + ":" + z.toString());
				// fw.write("\nu1k "+i+":"+u1.toString());
				// fw.write("\nu2k "+i+":"+u2.toString());
				// fw.write("\nu3k "+i+":"+u3.toString());
				// fw.write("\nu4k "+i+":"+u4.toString());
				// /*
				// * u^{k+1} = u^k + x^{k+1} - z^{k+1}
				// */
				//// u.print("uk "+i);
				// fw.write("\nuk "+i+":"+u.toString());

				/*
				 * Check stop condition
				 */
				double r = x_pre.plus(z_pre.scale(-1)).norm();
				double s = z_pre.plus(z.scale(-1.0)).scale(rho).norm();

				double eP = (x_pre.norm() > z_pre.norm()) ? (epsilonA * Math.sqrt(A.getn()) + epsilonR * x_pre.norm())
						: (epsilonA * Math.sqrt(A.getn()) + epsilonR * z_pre.norm());
				double eD = epsilonA * Math.sqrt(A.getm()) + epsilonR * (u_pre.scale(rho).norm());
				// System.out.println("x.norm(): ("+ x_pre.norm()
				// +")---z.norm(): ("+z_pre.norm()+")");
				// System.out.println("r: ("+ r +")---eP: ("+eP+")");
				// System.out.println("s: ("+ s +")---eD: ("+eD+")");
				fw.write("\nx.norm(): (" + x_pre.norm() + ")---z.norm(): (" + z_pre.norm() + ")\n" + "r: (" + r
						+ ")---eP: (" + eP + ")\n" + "s: (" + s + ")---eD: (" + eD + ")\n");

				if ((r <= eP) && (s <= eD)) {
					System.out.println("r<= eP) && (s<=eD) ---- STOP LOOP");
					fw.write("(r<= eP) && (s<=eD) ---- STOP LOOP\n");
					// fw.close();
					// return x;
				}
				if (s <= eD) {
					System.out.println("s<=eD");
					fw.write("(s<=eD)");
					// fw.close();
					n++;
					break;
					// return x;
				}
				if (r <= eP) {
					System.out.println("r<= eP");
					fw.write("r<= eP)");
					// fw.close();
					// return x;
				}

				if (r > (s * 10))
					rho = rho * 2;
				else if (s > (r * 10))
					rho = rho / 2;
				else
					rho = rho;

				// fw.write("rho "+rho);
				// System.out.println("rho "+rho);
				fw.write("rho " + rho + "\n============================================================");
				// System.out.println("============================================================");
			}
		}
		fw.close();
		return x;

	}

	public static SparseVector ADMMMatrixN1(JavaSparkContext sc, SparseMatrix A, SparseMatrix b, double lambda,
			double rho, double epsilonA, double epsilonR, String output) throws IOException {

		System.out.println("ADMMMatrixN");
		SparseVector ret = null;
		File fileut;
		FileWriter fw;
		// double arr[][] = {{2,1,3,1}};
		int part = A.getm();
		System.out.println("Am: " + A.getm() + " An: " + A.getn() + "---- bm: " + b.getm() + " bn: " + b.getn());
		// if(A.getm() != b.getm())
		// throw new UnsupportedOperationException("Not supported yet Am=!
		// b.m");
		String filename = output + Long.toString(System.currentTimeMillis()) + "/"; //
		new File(filename).mkdir();
		String nfilename = filename + "log.txt"; // Long.toString(System.currentTimeMillis()),
													// +
													// post.getCreatedTime().toString().trim()
		fileut = new File(nfilename);
		fw = new FileWriter(fileut);

		// A.print(true,"A " +A.m+"-"+A.n);
		// SparseMatrix A2 = A.copy();// echolonConvert();
		// A2.print(true,"U (echelon of A) " +A2.m+"-"+A2.n);

		fw.write("U ( of A) \n" + A.toString());

		SparseVector x = new SparseVector(A.getn());
		SparseVector x1 = new SparseVector(A.getn());
		SparseVector x2 = new SparseVector(A.getn());
		SparseVector x3 = new SparseVector(A.getn());
		SparseVector x4 = new SparseVector(A.getn());
		SparseVector z = new SparseVector(A.getn());
		SparseVector u = new SparseVector(A.getn());
		SparseVector u1 = new SparseVector(A.getn());
		SparseVector u2 = new SparseVector(A.getn());
		SparseVector u3 = new SparseVector(A.getn());
		SparseVector u4 = new SparseVector(A.getn());

		int n = 0;
		// while(n<A.n)
		{
			fw.write(
					"\n================================================================================================"
							+ n);
			z = A.getRow(1);// Column(1);
			z.print("z0");
			fw.write("\nzo:\n" + z.toString());
			// u = z.scale(0.5);
			// u.print("u0");
			fw.write("\nuo:\n" + u.toString());

			// System.out.println("============================================================");

			fw.write(
					"\n================================================================================================"
							+ n);
			int i = 0;
			JavaPairRDD<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input = util
					.splitMatrix2(sc, A, A.getm(), 4);

			input.saveAsTextFile(output + "/input" + Long.toString(System.currentTimeMillis()));

			JavaPairRDD<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_rho_1 = invert2(sc,
					input, rho, output + "/ATA_rho_Im_slaver" + Long.toString(System.currentTimeMillis()));

			ATA_rho_1.saveAsTextFile(output + "/ATA_rho" + Long.toString(System.currentTimeMillis()));

			while (true) {
				i++;
				System.out.println("============================================================loop time: " + i);
				fw.write("\nloop time: " + i + "\n");
				if (i > 500)
					break;

				SparseVector x_pre = x.copy();
				SparseVector z_pre = z.copy();
				SparseVector u_pre = u.copy();

				List<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> listU = new ArrayList<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>();
				// Tuple2<Integer,Iterable<Tuple2<Integer, Double>>> rowU = new
				// Tuple2<Integer, Iterable<Tuple2<Integer,Double>>>(_1, _2);
				// List
				List<Tuple2<Integer, Double>> rowU1 = new ArrayList<Tuple2<Integer, Double>>();
				List<Tuple2<Integer, Double>> rowU2 = new ArrayList<Tuple2<Integer, Double>>();
				List<Tuple2<Integer, Double>> rowU3 = new ArrayList<Tuple2<Integer, Double>>();
				List<Tuple2<Integer, Double>> rowU4 = new ArrayList<Tuple2<Integer, Double>>();
				for (int k = 0; k < u.size(); k++) {
					rowU1.add(new Tuple2<Integer, Double>(k, u1.get(k)));
					rowU2.add(new Tuple2<Integer, Double>(k, u2.get(k)));
					rowU3.add(new Tuple2<Integer, Double>(k, u3.get(k)));
					rowU4.add(new Tuple2<Integer, Double>(k, u4.get(k)));
				}
				ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> uArr = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
				uArr.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(0, rowU1));
				listU.add(uArr);
				uArr = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
				uArr.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(1, rowU2));
				listU.add(uArr);
				uArr = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
				uArr.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(2, rowU3));
				listU.add(uArr);
				uArr = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
				uArr.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(3, rowU4));
				listU.add(uArr);
				JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> uRDD = sc.parallelize(listU);

				uRDD.saveAsTextFile(output + "/listU" + Long.toString(System.currentTimeMillis()));
				JavaPairRDD<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> uPRDD = uRDD
						.flatMapToPair(
								new PairFlatMapFunction<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>, Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>() {

									public Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> call(
											Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> t)
											throws Exception {
										// TODO Auto-generated method stub
										int index = t.iterator().next()._1;
										// System.out.println("index of u:
										// "+index);
										List<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> ret = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>>();

										ret.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>(
												index, t));
										return ret;
									}
								});
				uPRDD.saveAsTextFile(output + "/uPRDD" + Long.toString(System.currentTimeMillis()));
				JavaPairRDD<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_rho_U = ATA_rho_1
						.union(uPRDD).groupByKey().flatMapToPair(
								new PairFlatMapFunction<Tuple2<Integer, Iterable<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>>, Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>() {

									public Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> call(
											Tuple2<Integer, Iterable<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> t)
											throws Exception {
										// TODO Auto-generated method stub
										List<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> ret = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>>();
										List<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> ret2 = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
										for (Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> x : t._2) {
											for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> x2 : x) {
												ret2.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(x2._1,
														x2._2));
											}

										}
										ret.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>(
												t._1, ret2));
										return ret;
									}
								});
				ATA_rho_U.saveAsTextFile(output + "/ATA_rho_U" + Long.toString(System.currentTimeMillis()));

				u = u1.plus(u2).plus(u3).plus(u4).scale(0.25);//

				/*
				 * x^{k+1} = 2(A^TA + \rho I_m)^-1 [ A^Tb - \rho (z^k - u^k)]
				 */
				List<Tuple2<Integer, List<Double>>> x_slaver = x_slaver3(sc, ATA_rho_U,/* null, u1,*/ z, rho,
						output + "/x_slaver3" + Long.toString(System.currentTimeMillis()));
				x1 = new SparseVector(x_slaver.get(0)._2);
				x2 = new SparseVector(x_slaver.get(1)._2);
				x3 = new SparseVector(x_slaver.get(2)._2);
				x4 = new SparseVector(x_slaver.get(3)._2);

				x = x1.plus(x2).plus(x3).plus(x4).scale(0.25);//
				x1.print("X1 " + x1.size());
				x2.print("X2 " + x2.size());
				x3.print("X3 " + x3.size());
				x4.print("X4 " + x4.size());
				x.print("Xxx " + x.size());// ;

				fw.write("\nu1k " + i + ":" + u1.toString());
				fw.write("\nu2k " + i + ":" + u2.toString());
				fw.write("\nu3k " + i + ":" + u3.toString());
				fw.write("\nu4k " + i + ":" + u4.toString());
				/*
				 * u^{k+1} = u^k + x^{k+1} - z^{k+1}
				 */
				// u.print("uk "+i);
				fw.write("\nuk " + i + ":" + u.toString());

				fw.write("\nx1: " + x1.toString());
				fw.write("\nx2: " + x2.toString());
				fw.write("\nx3: " + x3.toString());
				fw.write("\nx4: " + x4.toString());
				// fw.write("x: "+x.toString());
				// u= u1.plus(u2).plus(u3).plus(u4).scale(4);//
				// u.print("Xxx");// ;

				// fw.write("\nrho_zk_uk: "+rho_zk_uk.toString());
				// rho_zk_uk.print("rho_zk_uk");
				// fw.write("\nXall: "+Xall.toString());
				// Xall.print(true, "Xall");

				// ATb_rho_zATbk_uk.print("ATb_rho_zATbk_uk");
				// fw.write("\nATb_rho_zATbk_uk: "+ATb_rho_zATbk_uk.toString());
				// x.print("Xk "+i);
				fw.write("\nXk " + i + "-" + x.size() + ":" + x.toString());
				/*
				 * z^{k+1} = S_{\frag{\lambda}{\rho}}(x^{k+1} + u^k) = x^{k+1}
				 * +u^k - frag{\lambda}{\rho} if > = 0 if || \leq = x^{k+1} +u^k
				 * + frag{\lambda}{\rho} if <
				 */
				SparseVector tmp = x.plus(u);
				// tmp.print("x+u ");
				// fw.write("\n x+u :"+tmp.toString());
				// SparseVector zTmp = z.copy();
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						z.put(j, tmp.get(j) - lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						z.put(j, tmp.get(j) + lambda / (rho), false);
					} else {
						z.put(j, 0, false);
					}
				}

				tmp = x1.plus(u1);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u1.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u1.put(j, -lambda / (rho), false);
					} else {
						u1.put(j, x1.get(j) + u1.get(j), false);
					}
				}

				tmp = x2.plus(u2);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u2.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u2.put(j, -lambda / (rho), false);
					} else {
						u2.put(j, x2.get(j) + u2.get(j), false);
					}
				}

				tmp = x3.plus(u3);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u3.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u3.put(j, -lambda / (rho), false);
					} else {
						u3.put(j, x3.get(j) + u3.get(j), false);
					}
				}

				tmp = x4.plus(u4);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u4.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u4.put(j, -lambda / (rho), false);
					} else {
						u4.put(j, x4.get(j) + u4.get(j), false);
					}
				}

				u = u1.plus(u2).plus(u3).plus(u4).scale(0.25);//

				z.print("Zk " + i);
				fw.write("\nZk " + i + ":" + z.toString());
				// fw.write("\nu1k "+i+":"+u1.toString());
				// fw.write("\nu2k "+i+":"+u2.toString());
				// fw.write("\nu3k "+i+":"+u3.toString());
				// fw.write("\nu4k "+i+":"+u4.toString());
				// /*
				// * u^{k+1} = u^k + x^{k+1} - z^{k+1}
				// */
				//// u.print("uk "+i);
				// fw.write("\nuk "+i+":"+u.toString());

				/*
				 * Check stop condition
				 */
				double r = x_pre.plus(z_pre.scale(-1)).norm();
				double s = z_pre.plus(z.scale(-1.0)).scale(rho).norm();

				double eP = (x_pre.norm() > z_pre.norm()) ? (epsilonA * Math.sqrt(A.getn()) + epsilonR * x_pre.norm())
						: (epsilonA * Math.sqrt(A.getn()) + epsilonR * z_pre.norm());
				double eD = epsilonA * Math.sqrt(A.getm()) + epsilonR * (u_pre.scale(rho).norm());
				// System.out.println("x.norm(): ("+ x_pre.norm()
				// +")---z.norm(): ("+z_pre.norm()+")");
				// System.out.println("r: ("+ r +")---eP: ("+eP+")");
				// System.out.println("s: ("+ s +")---eD: ("+eD+")");
				fw.write("\nx.norm(): (" + x_pre.norm() + ")---z.norm(): (" + z_pre.norm() + ")\n" + "r: (" + r
						+ ")---eP: (" + eP + ")\n" + "s: (" + s + ")---eD: (" + eD + ")\n");

				if ((r <= eP) && (s <= eD)) {
					// System.out.println("r<= eP) && (s<=eD) ---- STOP LOOP");
					fw.write("(r<= eP) && (s<=eD) ---- STOP LOOP\n");
					// fw.close();
					// return x;
				}
				if (s <= eD) {
					// System.out.println("s<=eD");
					fw.write("(s<=eD)");
					// fw.close();
					n++;
					break;
					// return x;
				}
				if (r <= eP) {
					// System.out.println("r<= eP");
					fw.write("r<= eP)");
					// fw.close();
					// return x;
				}

				if (r > (s * 10))
					rho = rho * 2;
				else if (s > (r * 10))
					rho = rho / 2;
				else
					rho = rho;

				// fw.write("rho "+rho);
				// System.out.println("rho "+rho);
				fw.write("rho " + rho + "\n============================================================");
				// System.out.println("============================================================");
			}
		}
		fw.close();
		System.out.println("=========================END===================================");
		return x;

	}

	public static SparseVector ADMMMatrixN2(JavaSparkContext sc, int w, int h,
			JavaRDD<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> A1, double lambda, double rho, double epsilonA,
			double epsilonR, String output) throws IOException {

		System.out.println("ADMMMatrixN2");
		SparseVector ret = null;
		File fileut;
		FileWriter fw;
		// double arr[][] = {{2,1,3,1}};

		SparseMatrix A = util.toSparceMatrix2(A1.collect(), h, w);
		int part = A.getm();
		System.out.println("Am: " + A.getm() + " An: " + A.getn() + "---- bm: ");// +b.getm()+
																					// "
																					// bn:
																					// "+b.getn());
		// if(A.getm() != b.getm())
		// throw new UnsupportedOperationException("Not supported yet Am=!
		// b.m");
		String filename = output + Long.toString(System.currentTimeMillis()) + "/"; //
		new File(filename).mkdir();
		String nfilename = filename + "log.txt"; // Long.toString(System.currentTimeMillis()),
													// +
													// post.getCreatedTime().toString().trim()
		fileut = new File(nfilename);
		fw = new FileWriter(fileut);
		// toSparceMatrix(A1.collect());
		// A.print(true,"A " +A.m+"-"+A.n);
		// SparseMatrix A2 = toSparceMatrix(A1.collect());//A.copy();//
		// echolonConvert();
		// A2.print(true,"U (echelon of A) " +A2.m+"-"+A2.n);

		// fw.write("U ( of A) \n" + A.toString());

		SparseVector x = new SparseVector(A.getn());
		SparseVector x1 = new SparseVector(A.getn());
		SparseVector x2 = new SparseVector(A.getn());
		SparseVector x3 = new SparseVector(A.getn());
		SparseVector x4 = new SparseVector(A.getn());
		SparseVector z = new SparseVector(A.getn());
		SparseVector u = new SparseVector(A.getn());
		SparseVector u1 = new SparseVector(A.getn());
		SparseVector u2 = new SparseVector(A.getn());
		SparseVector u3 = new SparseVector(A.getn());
		SparseVector u4 = new SparseVector(A.getn());

		int n = 0;
		// while(n<A.n)
		{
			fw.write(
					"\n================================================================================================"
							+ n);
			z = A.getRow(1);
			z.print("z0");
			fw.write("\nzo:\n" + z.toString());
			u = z.scale(0.5);
			u.print("u0");
			fw.write("\nuo:\n" + u.toString());

			// System.out.println("============================================================");

			fw.write(
					"\n================================================================================================"
							+ n);
			int i = 0;
			System.out.println(
					" n:" + A.getn() + " - " + part / 4 + "- " + part / 2 + " - " + (part * 3 / 4) + " - " + (part));

			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input1 = util.toListArray2D(sc,
					A.getMatrix(0, (part / 4) - 1, 0, A.getn() - 1));
			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input2 = util.toListArray2D(sc,
					A.getMatrix(part / 4, (part / 2) - 1, 0, A.getn() - 1));
			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input3 = util.toListArray2D(sc,
					A.getMatrix(part / 2, (3 * part / 4) - 1, 0, A.getn() - 1));
			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input4 = util.toListArray2D(sc,
					A.getMatrix(3 * part / 4, part - 1, 0, A.getn() - 1));

			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_1 = ATA(sc, input1,
					output + "/ATA_Im_slaver" + Long.toString(System.currentTimeMillis()));
			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_2 = ATA(sc, input2,
					output + "/ATA_Im_slaver" + Long.toString(System.currentTimeMillis()));
			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_3 = ATA(sc, input3,
					output + "/ATA_Im_slaver" + Long.toString(System.currentTimeMillis()));
			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_4 = ATA(sc, input4,
					output + "/ATA_Im_slaver" + Long.toString(System.currentTimeMillis()));

			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_rho_1 = ATA_rho_Im_slaver(sc,
					input1, rho, output + "/ATA_rho_Im_slaver" + Long.toString(System.currentTimeMillis()));
			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_rho_2 = ATA_rho_Im_slaver(sc,
					input2, rho, output + "/ATA_rho_Im_slaver" + Long.toString(System.currentTimeMillis()));
			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_rho_3 = ATA_rho_Im_slaver(sc,
					input3, rho, output + "/ATA_rho_Im_slaver" + Long.toString(System.currentTimeMillis()));
			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_rho_4 = ATA_rho_Im_slaver(sc,
					input4, rho, output + "/ATA_rho_Im_slaver" + Long.toString(System.currentTimeMillis()));

			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_ATA_rho_1 = ATA_1
					.union(ATA_rho_1);

			List<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> rowall = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
			for (Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> row : ATA_ATA_rho_1.collect()) {
				for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> tupe : row) {
					rowall.add(tupe);
				}
			}
			List<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> babdfb = new ArrayList<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>();
			babdfb.add(rowall);
			ATA_ATA_rho_1 = sc.parallelize(babdfb);

			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_ATA_rho_2 = ATA_2
					.union(ATA_rho_2);
			rowall = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
			for (Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> row : ATA_ATA_rho_2.collect()) {
				for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> tupe : row) {
					rowall.add(tupe);
				}
			}
			babdfb = new ArrayList<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>();
			babdfb.add(rowall);
			ATA_ATA_rho_2 = sc.parallelize(babdfb);

			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_ATA_rho_3 = ATA_3
					.union(ATA_rho_3);
			rowall = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
			for (Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> row : ATA_ATA_rho_3.collect()) {
				for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> tupe : row) {
					rowall.add(tupe);
				}
			}
			babdfb = new ArrayList<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>();
			babdfb.add(rowall);
			ATA_ATA_rho_3 = sc.parallelize(babdfb);

			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_ATA_rho_4 = ATA_4
					.union(ATA_rho_4);
			rowall = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
			for (Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> row : ATA_ATA_rho_4.collect()) {
				for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> tupe : row) {
					rowall.add(tupe);
				}
			}
			babdfb = new ArrayList<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>();
			babdfb.add(rowall);
			ATA_ATA_rho_4 = sc.parallelize(babdfb);

			while (true) {
				i++;
				System.out.println("loop time: " + i);
				fw.write("\nloop time: " + i + "\n");
				if (i > 500)
					break;

				SparseVector x_pre = x.copy();
				SparseVector z_pre = z.copy();
				SparseVector u_pre = u.copy();
				/*
				 * x^{k+1} = 2(A^TA + \rho I_m)^-1 [ A^Tb - \rho (z^k - u^k)]
				 */
				x1 = new SparseVector(x_slaver(sc, ATA_ATA_rho_1,/* null,*/ u1, z, rho,
						output + "/x1testNew" + Long.toString(System.currentTimeMillis())));
				x2 = new SparseVector(x_slaver(sc, ATA_ATA_rho_2,/* null,*/ u2, z, rho,
						output + "/x2testNew" + Long.toString(System.currentTimeMillis())));
				x3 = new SparseVector(x_slaver(sc, ATA_ATA_rho_3,/* null,*/ u3, z, rho,
						output + "/x3testNew" + Long.toString(System.currentTimeMillis())));
				x4 = new SparseVector(x_slaver(sc, ATA_ATA_rho_4,/* null,*/ u4, z, rho,
						output + "/x4testNew" + Long.toString(System.currentTimeMillis())));

				x = x1.plus(x2).plus(x3).plus(x4).scale(0.25);//
				// x1.print("X1");
				// x2.print("X2");
				// x3.print("X3");
				// x4.print("X4");
				x.print("Xxx");// ;

				// fw.write("\nx1: "+x1.toString());
				// fw.write("\nx2: "+x2.toString());
				// fw.write("\nx3: "+x3.toString());
				// fw.write("\nx4: "+x4.toString());
				// fw.write("x: "+x.toString());
				// u= u1.plus(u2).plus(u3).plus(u4).scale(4);//
				// u.print("Xxx");// ;

				// fw.write("\nrho_zk_uk: "+rho_zk_uk.toString());
				// rho_zk_uk.print("rho_zk_uk");
				// fw.write("\nXall: "+Xall.toString());
				// Xall.print(true, "Xall");

				// ATb_rho_zATbk_uk.print("ATb_rho_zATbk_uk");
				// fw.write("\nATb_rho_zATbk_uk: "+ATb_rho_zATbk_uk.toString());
				// x.print("Xk "+i);
				fw.write("\nXk " + i + ":" + x.toString());
				/*
				 * z^{k+1} = S_{\frag{\lambda}{\rho}}(x^{k+1} + u^k) = x^{k+1}
				 * +u^k - frag{\lambda}{\rho} if > = 0 if || \leq = x^{k+1} +u^k
				 * + frag{\lambda}{\rho} if <
				 */
				SparseVector tmp = x.plus(u);
				tmp.print("x+u ");
				// fw.write("\n x+u :"+tmp.toString());
				// SparseVector zTmp = z.copy();
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						z.put(j, tmp.get(j) - lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						z.put(j, tmp.get(j) + lambda / (rho), false);
					} else {
						z.put(j, 0, false);
					}
				}

				tmp = x1.plus(u1);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u1.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u1.put(j, -lambda / (rho), false);
					} else {
						u1.put(j, x1.get(j) + u1.get(j), false);
					}
				}

				tmp = x2.plus(u2);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u2.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u2.put(j, -lambda / (rho), false);
					} else {
						u2.put(j, x2.get(j) + u2.get(j), false);
					}
				}

				tmp = x3.plus(u3);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u3.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u3.put(j, -lambda / (rho), false);
					} else {
						u3.put(j, x3.get(j) + u3.get(j), false);
					}
				}

				tmp = x4.plus(u4);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u4.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u4.put(j, -lambda / (rho), false);
					} else {
						u4.put(j, x4.get(j) + u4.get(j), false);
					}
				}

				u = u1.plus(u2).plus(u3).plus(u4).scale(0.25);//

				z.print("Zk " + i);
				fw.write("\nZk " + i + ":" + z.toString());
				// fw.write("\nu1k "+i+":"+u1.toString());
				// fw.write("\nu2k "+i+":"+u2.toString());
				// fw.write("\nu3k "+i+":"+u3.toString());
				// fw.write("\nu4k "+i+":"+u4.toString());
				/*
				 * u^{k+1} = u^k + x^{k+1} - z^{k+1}
				 */
				// u.print("uk "+i);
				fw.write("\nuk " + i + ":" + u.toString());

				/*
				 * Check stop condition
				 */
				double r = x_pre.plus(z_pre.scale(-1)).norm();
				double s = z_pre.plus(z.scale(-1.0)).scale(rho).norm();

				double eP = (x_pre.norm() > z_pre.norm()) ? (epsilonA * Math.sqrt(A.getn()) + epsilonR * x_pre.norm())
						: (epsilonA * Math.sqrt(A.getn()) + epsilonR * z_pre.norm());
				double eD = epsilonA * Math.sqrt(A.getm()) + epsilonR * (u_pre.scale(rho).norm());
				// System.out.println("x.norm(): ("+ x_pre.norm()
				// +")---z.norm(): ("+z_pre.norm()+")");
				// System.out.println("r: ("+ r +")---eP: ("+eP+")");
				// System.out.println("s: ("+ s +")---eD: ("+eD+")");
				fw.write("\nx.norm(): (" + x_pre.norm() + ")---z.norm(): (" + z_pre.norm() + ")\n" + "r: (" + r
						+ ")---eP: (" + eP + ")\n" + "s: (" + s + ")---eD: (" + eD + ")\n");

				if ((r <= eP) && (s <= eD)) {
					// System.out.println("r<= eP) && (s<=eD) ---- STOP LOOP");
					fw.write("(r<= eP) && (s<=eD) ---- STOP LOOP\n");
					// fw.close();
					// return x;
				}
				if (s <= eD) {
					// System.out.println("s<=eD");
					fw.write("(s<=eD)");
					// fw.close();
					n++;
					break;
					// return x;
				}
				if (r <= eP) {
					// System.out.println("r<= eP");
					fw.write("r<= eP)");
					// fw.close();
					// return x;
				}

				if (r > (s * 10))
					rho = rho * 2;
				else if (s > (r * 10))
					rho = rho / 2;
				else
					rho = rho;

				// fw.write("rho "+rho);
				// System.out.println("rho "+rho);
				fw.write("rho " + rho + "\n============================================================");
				// System.out.println("============================================================");
			}
		}
		fw.close();
		return x;

	}

	public static SparseVector ADMMMatrixN3(JavaSparkContext sc, int w, int h,
			JavaRDD<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> A1, double lambda, double rho, double epsilonA,
			double epsilonR, String output) throws IOException {

		System.out.println("ADMMMatrixN2");
		SparseVector ret = null;
		File fileut;
		FileWriter fw;
		// double arr[][] = {{2,1,3,1}};

		SparseMatrix A = util.toSparceMatrix2(A1.collect(), h, w);
		int part = A.getm();
		System.out.println("Am: " + A.getm() + " An: " + A.getn() + "---- bm: ");// +b.getm()+
																					// "
																					// bn:
																					// "+b.getn());
		// if(A.getm() != b.getm())
		// throw new UnsupportedOperationException("Not supported yet Am=!
		// b.m");
		String filename = output + Long.toString(System.currentTimeMillis()) + "/"; //
		new File(filename).mkdir();
		String nfilename = filename + "log.txt"; // Long.toString(System.currentTimeMillis()),
													// +
													// post.getCreatedTime().toString().trim()
		fileut = new File(nfilename);
		fw = new FileWriter(fileut);
		// toSparceMatrix(A1.collect());
		// A.print(true,"A " +A.m+"-"+A.n);
		// SparseMatrix A2 = toSparceMatrix(A1.collect());//A.copy();//
		// echolonConvert();
		// A2.print(true,"U (echelon of A) " +A2.m+"-"+A2.n);

		// fw.write("U ( of A) \n" + A.toString());

		SparseVector x = new SparseVector(A.getn());
		SparseVector x1 = new SparseVector(A.getn());
		SparseVector x2 = new SparseVector(A.getn());
		SparseVector x3 = new SparseVector(A.getn());
		SparseVector x4 = new SparseVector(A.getn());
		SparseVector z = new SparseVector(A.getn());
		SparseVector u = new SparseVector(A.getn());
		SparseVector u1 = new SparseVector(A.getn());
		SparseVector u2 = new SparseVector(A.getn());
		SparseVector u3 = new SparseVector(A.getn());
		SparseVector u4 = new SparseVector(A.getn());

		int n = 0;
		// while(n<A.n)
		{
			fw.write(
					"\n================================================================================================"
							+ n);
			z = A.getRow(1);
			z.print("z0");
			fw.write("\nzo:\n" + z.toString());
			u = z.scale(0.5);
			u.print("u0");
			fw.write("\nuo:\n" + u.toString());

			// System.out.println("============================================================");

			fw.write(
					"\n================================================================================================"
							+ n);
			int i = 0;
			// System.out.println(" n:"+ A.getn()+ " - "+ part/4 + "- "+ part/2
			// + " - "+(part*3/4)+ " - "+(part));

			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input = util.splitMatrix1(sc, A,
					A.getm(), 4);

			input.saveAsTextFile(output + "/input" + Long.toString(System.currentTimeMillis()));

			JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_rho_1 = invert(sc, input, rho,
					output + "/ATA_rho_Im_slaver" + Long.toString(System.currentTimeMillis()));

			ATA_rho_1.saveAsTextFile(output + "/ATA_rho" + Long.toString(System.currentTimeMillis()));

			while (true) {
				i++;
				System.out.println("loop time: " + i);
				fw.write("\nloop time: " + i + "\n");
				if (i > 500)
					break;

				SparseVector x_pre = x.copy();
				SparseVector z_pre = z.copy();
				SparseVector u_pre = u.copy();

				List<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> listU = new ArrayList<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>();
				// Tuple2<Integer,Iterable<Tuple2<Integer, Double>>> rowU = new
				// Tuple2<Integer, Iterable<Tuple2<Integer,Double>>>(_1, _2);
				// List
				List<Tuple2<Integer, Double>> rowU1 = new ArrayList<Tuple2<Integer, Double>>();
				List<Tuple2<Integer, Double>> rowU2 = new ArrayList<Tuple2<Integer, Double>>();
				List<Tuple2<Integer, Double>> rowU3 = new ArrayList<Tuple2<Integer, Double>>();
				List<Tuple2<Integer, Double>> rowU4 = new ArrayList<Tuple2<Integer, Double>>();
				for (int k = 0; k < u.size(); k++) {
					rowU1.add(new Tuple2<Integer, Double>(k, u1.get(k)));
					rowU2.add(new Tuple2<Integer, Double>(k, u2.get(k)));
					rowU3.add(new Tuple2<Integer, Double>(k, u3.get(k)));
					rowU4.add(new Tuple2<Integer, Double>(k, u4.get(k)));
				}
				ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> uArr = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
				uArr.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(0, rowU1));
				listU.add(uArr);
				uArr = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
				uArr.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(2, rowU2));
				listU.add(uArr);
				uArr = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
				uArr.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(3, rowU3));
				listU.add(uArr);
				uArr = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
				uArr.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(4, rowU4));
				listU.add(uArr);
				JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> uRDD = sc.parallelize(listU);

				uRDD.saveAsTextFile(output + "/listU" + Long.toString(System.currentTimeMillis()));

				u = u1.plus(u2).plus(u3).plus(u4).scale(0.25);//

				/*
				 * x^{k+1} = 2(A^TA + \rho I_m)^-1 [ A^Tb - \rho (z^k - u^k)]
				 */
				List<List<Double>> x_slaver = x_slaver2(sc, ATA_rho_1,/* null,*/ u1, z, rho,
						output + "/x1testNew" + Long.toString(System.currentTimeMillis()));
				x1 = new SparseVector(x_slaver.get(0));
				x2 = new SparseVector(x_slaver.get(1));
				x3 = new SparseVector(x_slaver.get(2));
				x4 = new SparseVector(x_slaver.get(3));

				// ATA_ATA_rho_1.
				x = x1.plus(x2).plus(x3).plus(x4).scale(0.25);//
				// x1.print("X1");
				// x2.print("X2");
				// x3.print("X3");
				// x4.print("X4");
				x.print("Xxx");// ;

				// fw.write("\nx1: "+x1.toString());
				// fw.write("\nx2: "+x2.toString());
				// fw.write("\nx3: "+x3.toString());
				// fw.write("\nx4: "+x4.toString());
				// fw.write("x: "+x.toString());
				// u= u1.plus(u2).plus(u3).plus(u4).scale(4);//
				// u.print("Xxx");// ;

				// fw.write("\nrho_zk_uk: "+rho_zk_uk.toString());
				// rho_zk_uk.print("rho_zk_uk");
				// fw.write("\nXall: "+Xall.toString());
				// Xall.print(true, "Xall");

				// ATb_rho_zATbk_uk.print("ATb_rho_zATbk_uk");
				// fw.write("\nATb_rho_zATbk_uk: "+ATb_rho_zATbk_uk.toString());
				// x.print("Xk "+i);
				fw.write("\nXk " + i + ":" + x.toString());
				/*
				 * z^{k+1} = S_{\frag{\lambda}{\rho}}(x^{k+1} + u^k) = x^{k+1}
				 * +u^k - frag{\lambda}{\rho} if > = 0 if || \leq = x^{k+1} +u^k
				 * + frag{\lambda}{\rho} if <
				 */
				SparseVector tmp = x.plus(u);
				tmp.print("x+u ");
				// fw.write("\n x+u :"+tmp.toString());
				// SparseVector zTmp = z.copy();
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						z.put(j, tmp.get(j) - lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						z.put(j, tmp.get(j) + lambda / (rho), false);
					} else {
						z.put(j, 0, false);
					}
				}

				tmp = x1.plus(u1);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u1.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u1.put(j, -lambda / (rho), false);
					} else {
						u1.put(j, x1.get(j) + u1.get(j), false);
					}
				}

				tmp = x2.plus(u2);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u2.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u2.put(j, -lambda / (rho), false);
					} else {
						u2.put(j, x2.get(j) + u2.get(j), false);
					}
				}

				tmp = x3.plus(u3);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u3.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u3.put(j, -lambda / (rho), false);
					} else {
						u3.put(j, x3.get(j) + u3.get(j), false);
					}
				}

				tmp = x4.plus(u4);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u4.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u4.put(j, -lambda / (rho), false);
					} else {
						u4.put(j, x4.get(j) + u4.get(j), false);
					}
				}

				z.print("Zk " + i);
				fw.write("\nZk " + i + ":" + z.toString());
				// fw.write("\nu1k "+i+":"+u1.toString());
				// fw.write("\nu2k "+i+":"+u2.toString());
				// fw.write("\nu3k "+i+":"+u3.toString());
				// fw.write("\nu4k "+i+":"+u4.toString());
				/*
				 * u^{k+1} = u^k + x^{k+1} - z^{k+1}
				 */
				// u.print("uk "+i);
				fw.write("\nuk " + i + ":" + u.toString());

				/*
				 * Check stop condition
				 */
				double r = x_pre.plus(z_pre.scale(-1)).norm();
				double s = z_pre.plus(z.scale(-1.0)).scale(rho).norm();

				double eP = (x_pre.norm() > z_pre.norm()) ? (epsilonA * Math.sqrt(A.getn()) + epsilonR * x_pre.norm())
						: (epsilonA * Math.sqrt(A.getn()) + epsilonR * z_pre.norm());
				double eD = epsilonA * Math.sqrt(A.getm()) + epsilonR * (u_pre.scale(rho).norm());
				// System.out.println("x.norm(): ("+ x_pre.norm()
				// +")---z.norm(): ("+z_pre.norm()+")");
				// System.out.println("r: ("+ r +")---eP: ("+eP+")");
				// System.out.println("s: ("+ s +")---eD: ("+eD+")");
				fw.write("\nx.norm(): (" + x_pre.norm() + ")---z.norm(): (" + z_pre.norm() + ")\n" + "r: (" + r
						+ ")---eP: (" + eP + ")\n" + "s: (" + s + ")---eD: (" + eD + ")\n");

				if ((r <= eP) && (s <= eD)) {
					// System.out.println("r<= eP) && (s<=eD) ---- STOP LOOP");
					fw.write("(r<= eP) && (s<=eD) ---- STOP LOOP\n");
					// fw.close();
					// return x;
				}
				if (s <= eD) {
					// System.out.println("s<=eD");
					fw.write("(s<=eD)");
					// fw.close();
					n++;
					break;
					// return x;
				}
				if (r <= eP) {
					// System.out.println("r<= eP");
					fw.write("r<= eP)");
					// fw.close();
					// return x;
				}

				if (r > (s * 10))
					rho = rho * 2;
				else if (s > (r * 10))
					rho = rho / 2;
				else
					rho = rho;

				// fw.write("rho "+rho);
				// System.out.println("rho "+rho);
				fw.write("rho " + rho + "\n============================================================");
				// System.out.println("============================================================");
			}
		}
		fw.close();
		return x;

	}
	/*
	 * TODO : 
	 * - re-check again
	 * - re-design, more flexible
	 */
	public static SparseVector ADMMMatrixN4(JavaSparkContext sc, int w, int h,
			JavaRDD<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> A1, double lambda, double rho, double epsilonA,
			double epsilonR, String output) throws IOException {

		System.out.println("ADMMMatrixN4");
		// SparseVector ret = null;
		File fileut;
		FileWriter fw;

		SparseMatrix A = util.toSparceMatrix2(A1.collect(), h, w);

		// int part =A.getm();
		System.out.println("Am: " + A.getm() + " An: " + A.getn() + "---- bm: ");// +b.getm()+
																					// "
																					// bn:
																					// "+b.getn());
		// if(A.getm() != b.getm())
		// throw new UnsupportedOperationException("Not supported yet Am=!
		// b.m");
		String filename = output + Long.toString(System.currentTimeMillis()) + "/"; //
		new File(filename).mkdir();
		String nfilename = filename + "log.txt"; // Long.toString(System.currentTimeMillis()),
													// +
													// post.getCreatedTime().toString().trim()
		fileut = new File(nfilename);
		fw = new FileWriter(fileut);
		// toSparceMatrix(A1.collect());
		// A.print(true,"A " +A.m+"-"+A.n);
		// SparseMatrix A2 = toSparceMatrix(A1.collect());//A.copy();//
		// echolonConvert();
		// A2.print(true,"U (echelon of A) " +A2.m+"-"+A2.n);

		// fw.write("U ( of A) \n" + A.toString());

		SparseVector x = new SparseVector(A.getn());
		SparseVector x1 = new SparseVector(A.getn());
		SparseVector x2 = new SparseVector(A.getn());
		SparseVector x3 = new SparseVector(A.getn());
		SparseVector x4 = new SparseVector(A.getn());
		SparseVector z = new SparseVector(A.getn());
		SparseVector u = new SparseVector(A.getn());
		SparseVector u1 = new SparseVector(A.getn());
		SparseVector u2 = new SparseVector(A.getn());
		SparseVector u3 = new SparseVector(A.getn());
		SparseVector u4 = new SparseVector(A.getn());

//		int n = 0;
		int n = 1;
		// while(n<A.n)
		{
			fw.write(
					"\n================================================================================================"
							+ n);
			z = A.getRow(n);
			z.print("z0");
			fw.write("\nzo:\n" + z.toString());
			u = z.scale(0.5);
			u.print("u0");
			fw.write("\nuo:\n" + u.toString());

			System.out.println("============================================================");

			fw.write(
					"\n================================================================================================"
							+ n);
			int i = 0;
			// System.out.println(" n:"+ A.getn()+ " - "+ part/4 + "- "+ part/2
			// + " - "+(part*3/4)+ " - "+(part));

			JavaPairRDD<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input = util
					.splitMatrix2(sc, A, A.getm(), 4);
			//TODO: (ATA +rho)^-1 not A
			System.out.println(" After splitMatrix2============================================================");
			input.saveAsTextFile(output + "/input" + Long.toString(System.currentTimeMillis()));
			
			//(A^TA + \rho I_m)^-1
			JavaPairRDD<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_rho_1 = invert2(sc,
					input, rho, output + "/ATA_rho_Im_slaver" + Long.toString(System.currentTimeMillis()));

			ATA_rho_1.saveAsTextFile(output + "/ATA_rho" + Long.toString(System.currentTimeMillis()));

			while (true) {
				i++;
				System.out.println("============================================================loop time: " + i);
				fw.write("\n============================================================loop time: " + i + "\n");
				if (i > 200)
					break;

				SparseVector x_pre = x.copy();
				SparseVector z_pre = z.copy();
				SparseVector u_pre = u.copy();

				List<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> listU = new ArrayList<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>();
				// Tuple2<Integer,Iterable<Tuple2<Integer, Double>>> rowU = new
				// Tuple2<Integer, Iterable<Tuple2<Integer,Double>>>(_1, _2);
				// List
				List<Tuple2<Integer, Double>> rowU1 = new ArrayList<Tuple2<Integer, Double>>();
				List<Tuple2<Integer, Double>> rowU2 = new ArrayList<Tuple2<Integer, Double>>();
				List<Tuple2<Integer, Double>> rowU3 = new ArrayList<Tuple2<Integer, Double>>();
				List<Tuple2<Integer, Double>> rowU4 = new ArrayList<Tuple2<Integer, Double>>();
				for (int k = 0; k < u.size(); k++) {
					// System.out.println("k u:"+k);
					rowU1.add(new Tuple2<Integer, Double>(k, u1.get(k)));
					rowU2.add(new Tuple2<Integer, Double>(k, u2.get(k)));
					rowU3.add(new Tuple2<Integer, Double>(k, u3.get(k)));
					rowU4.add(new Tuple2<Integer, Double>(k, u4.get(k)));
				}
				ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> uArr = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
				uArr.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(0, rowU1));
				listU.add(uArr);
				uArr = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
				uArr.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(1, rowU2));
				listU.add(uArr);
				uArr = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
				uArr.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(2, rowU3));
				listU.add(uArr);
				uArr = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
				uArr.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(3, rowU4));
				listU.add(uArr);
				JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> uRDD = sc.parallelize(listU);

				// uRDD.saveAsTextFile(output+"/listU"+
				// Long.toString(System.currentTimeMillis()));
				//A^Tb with b is vector of A
				JavaPairRDD<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> uPRDD = uRDD
						.flatMapToPair(
								new PairFlatMapFunction<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>, Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>() {

									public Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> call(
											Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> t)
											throws Exception {
										// TODO Auto-generated method stub
										int index = t.iterator().next()._1;
										// System.out.println("index of u:
										// "+index);
										List<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> ret = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>>();

										ret.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>(
												index, t));
										return ret;
									}
								});
				// uPRDD.saveAsTextFile(output+"/uPRDD"+
				// Long.toString(System.currentTimeMillis()));
				JavaPairRDD<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_rho_ATb = ATA_rho_1
						.union(uPRDD).groupByKey().flatMapToPair(
								new PairFlatMapFunction<Tuple2<Integer, Iterable<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>>, Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>() {

									public Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> call(
											Tuple2<Integer, Iterable<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> t)
											throws Exception {
										// TODO Auto-generated method stub
										List<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> ret = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>>();
										List<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> ret2 = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
										for (Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> x : t._2) {
											for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> x2 : x) {
												ret2.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(x2._1,
														x2._2));
											}

										}
										ret.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>(
												t._1, ret2));
										return ret;
									}
								});
				// ATA_rho_U.saveAsTextFile(output+"/ATA_rho_U"+
				// Long.toString(System.currentTimeMillis()));

				u = u1.plus(u2).plus(u3).plus(u4).scale(0.25);//
				// System.out.println("before x_slaver3");
				/*
				 * x^{k+1} = 2(A^TA + \rho I_m)^-1 [ A^Tb - \rho (z^k - u^k)]
				 */
				List<Tuple2<Integer, List<Double>>> x_slaver = x_slaver3(sc, ATA_rho_ATb, /*null, u1,*/ z, rho,//x_slaver3(sc, ATA_rho_U, null, u1, z, rho,
						output + "/x_slaver3" + Long.toString(System.currentTimeMillis()));
				x1 = new SparseVector(x_slaver.get(0)._2);
				x2 = new SparseVector(x_slaver.get(1)._2);
				x3 = new SparseVector(x_slaver.get(2)._2);
				x4 = new SparseVector(x_slaver.get(3)._2);

				// ATA_ATA_rho_1.
				x = x1.plus(x2).plus(x3).plus(x4).scale(0.25);//
				// x1.print("X1");
				// x2.print("X2");
				// x3.print("X3");
				// x4.print("X4");
				x.print("\nXk " + i);// ;

				// fw.write("\nx1: "+x1.toString());
				// fw.write("\nx2: "+x2.toString());
				// fw.write("\nx3: "+x3.toString());
				// fw.write("\nx4: "+x4.toString());
				// fw.write("x: "+x.toString());
				// u= u1.plus(u2).plus(u3).plus(u4).scale(4);//
				// u.print("Xxx");// ;

				// fw.write("\nrho_zk_uk: "+rho_zk_uk.toString());
				// rho_zk_uk.print("rho_zk_uk");
				// fw.write("\nXall: "+Xall.toString());
				// Xall.print(true, "Xall");

				// ATb_rho_zATbk_uk.print("ATb_rho_zATbk_uk");
				// fw.write("\nATb_rho_zATbk_uk: "+ATb_rho_zATbk_uk.toString());
				// x.print("Xk "+i);
				fw.write("\nXk " + i + ":" + x.toString());
				/*
				 * z^{k+1} = S_{\frag{\lambda}{\rho}}(x^{k+1} + u^k) = x^{k+1}
				 * +u^k - frag{\lambda}{\rho} if > = 0 if || \leq = x^{k+1} +u^k
				 * + frag{\lambda}{\rho} if <
				 */
				SparseVector tmp = x.plus(u);
				tmp.print("x+u ");
				// fw.write("\n x+u :"+tmp.toString());
				// SparseVector zTmp = z.copy();
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						z.put(j, tmp.get(j) - lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						z.put(j, tmp.get(j) + lambda / (rho), false);
					} else {
						z.put(j, 0, false);
					}
				}

				tmp = x1.plus(u1);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u1.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u1.put(j, -lambda / (rho), false);
					} else {
						u1.put(j, x1.get(j) + u1.get(j), false);
					}
				}

				tmp = x2.plus(u2);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u2.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u2.put(j, -lambda / (rho), false);
					} else {
						u2.put(j, x2.get(j) + u2.get(j), false);
					}
				}

				tmp = x3.plus(u3);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u3.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u3.put(j, -lambda / (rho), false);
					} else {
						u3.put(j, x3.get(j) + u3.get(j), false);
					}
				}

				tmp = x4.plus(u4);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u4.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u4.put(j, -lambda / (rho), false);
					} else {
						u4.put(j, x4.get(j) + u4.get(j), false);
					}
				}

				z.print("Zk " + i);
				fw.write("\nZk " + i + ":" + z.toString());
				// fw.write("\nu1k "+i+":"+u1.toString());
				// fw.write("\nu2k "+i+":"+u2.toString());
				// fw.write("\nu3k "+i+":"+u3.toString());
				// fw.write("\nu4k "+i+":"+u4.toString());
				/*
				 * u^{k+1} = u^k + x^{k+1} - z^{k+1}
				 */
				// u.print("uk "+i);
				fw.write("\nuk " + i + ":" + u.toString());

				/*
				 * Check stop condition
				 */
				double r = x_pre.plus(z_pre.scale(-1)).norm();
				double s = z.plus(z_pre.scale(-1.0)).scale(rho).norm();

				double eP = (x_pre.norm() > z_pre.norm()) ? (epsilonA * Math.sqrt(A.getn()) + epsilonR * x_pre.norm())
						: (epsilonA * Math.sqrt(A.getn()) + epsilonR * z_pre.norm());
				double eD = epsilonA * Math.sqrt(A.getm()) + epsilonR * (u_pre.scale(rho).norm());
				System.out.println("x.norm(): (" + x_pre.norm() + ")---z.norm(): (" + z_pre.norm() + ")");
				System.out.println("r: (" + r + ")---eP: (" + eP + ")");
				System.out.println("s: (" + s + ")---eD: (" + eD + ")");
				fw.write("\nx.norm(): (" + x_pre.norm() + ")---z.norm(): (" + z_pre.norm() + ")\n" + "r: (" + r
						+ ")---eP: (" + eP + ")\n" + "s: (" + s + ")---eD: (" + eD + ")\n");
				if (i > 1) {
					if ((r <= eP) && (s <= eD)) {
						System.out.println("r<= eP) && (s<=eD) ---- STOP LOOP");
						fw.write("(r<= eP) && (s<=eD) ---- STOP LOOP\n");
						// fw.close();
						// return x;
					}
					if (s <= eD) {
						System.out.println("s<=eD");
						fw.write("(s<=eD)");
						// fw.close();
						n++;
						break;
						// return x;
					}
					if (r <= eP) {
						System.out.println("r<= eP");
						fw.write("r<= eP)");
						// fw.close();
						// return x;
					}
				}

				if (r > (s * 10))
					rho = rho * 2;
				else if (s > (r * 10))
					rho = rho / 2;
				else
					rho = rho;

				// fw.write("rho "+rho);
				// System.out.println("rho "+rho);
				fw.write("rho " + rho + "\n============================================================");
				// System.out.println("============================================================");
			}
		}
		fw.close();
		return x;

	}
	
	static JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> toRDD(JavaSparkContext sc, SparseMatrix[] input)
	{
		int index = input.length;
		
		List<Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> ret = new ArrayList<Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>>();
		
		for(int i = 0; i< index; i++)
		{
			List<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> mat = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
			for(int j = 0; j< input[i].getm(); j++)
			{
				List<Tuple2<Integer,Double>> rowData = new ArrayList<Tuple2<Integer,Double>>();
				for(int k = 0; k<input[i].getm(); k++)
				{
					rowData.add(new Tuple2<Integer, Double>(k, input[i].get(j, k)));
				}
				mat.add(new Tuple2<Integer, Iterable<Tuple2<Integer,Double>>>(j, rowData));
			}
			ret.add(new Tuple2<String, Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>(Integer.toString(i), mat));
		}

		return sc.parallelize(ret).flatMapToPair(new PairFlatMapFunction<Tuple2<String,Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>, String,Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>() {

			@Override
			public Iterable<Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> call(
					Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> t) throws Exception {

				List<Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> ret = new ArrayList<Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>>();

				ret.add(new Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>(
						t._1, t._2));
				return ret;
			}
		});
	}

	/*
	 * Split int put to n part
	 * map to system
	 * cal AtA
	 * ATA + rhoI
	 * inverse (AtA+rhoI) 	(1)
	 * AtA[n]				(2)
	 * for(!end)
	 * {
	 * 		x= XSlave(1,2, u, z)
	 * 		u= avr(u)
	 * 		z= avr(z)
	 * }
	 */
	
	public static SparseMatrix ADMMMatrixN5(JavaSparkContext sc, int w, int h,
			JavaRDD<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> A1, double lambda, double rho, double epsilonA,
			double epsilonR, String output) throws IOException {

		System.out.println("ADMMMatrixN5");
		// SparseVector ret = null;
		File fileut;
		FileWriter fw;

//		JavaRDD<Integer> rdd = sc.parallelize(Arrays.asList(1, 2, 3, 4));
		
//		rdd.mapPartitions
		
//		rdd.saveAsHadoopFile
//		rdd.fold(zeroValue, f)
		
//		final Accumulator<Integer> count = sc.accumulator(0);
		
		SparseMatrix A = util.toSparceMatrix2(A1.collect(), h, w);

		SparseMatrix ret = new SparseMatrix(h, w); 
		// int part =A.getm();
		System.out.println("Am: " + A.getm() + " An: " + A.getn() + "---- bm: ");// +b.getm()+
																					// "
																					// bn:
																					// "+b.getn());
		// if(A.getm() != b.getm())
		// throw new UnsupportedOperationException("Not supported yet Am=!
		// b.m");
		String filename = output + Long.toString(System.currentTimeMillis()) + "/"; //
		new File(filename).mkdir();
		String nfilename = filename + "log.txt"; // Long.toString(System.currentTimeMillis()),
													// +
													// post.getCreatedTime().toString().trim()
		fileut = new File(nfilename);
		fw = new FileWriter(fileut);
		// toSparceMatrix(A1.collect());
		// A.print(true,"A " +A.m+"-"+A.n);
		// SparseMatrix A2 = toSparceMatrix(A1.collect());//A.copy();//
		// echolonConvert();
		// A2.print(true,"U (echelon of A) " +A2.m+"-"+A2.n);

		// fw.write("U ( of A) \n" + A.toString());

		SparseVector x = new SparseVector(A.getn());
		SparseVector x1 = new SparseVector(A.getn());
		SparseVector x2 = new SparseVector(A.getn());
		SparseVector x3 = new SparseVector(A.getn());
		SparseVector x4 = new SparseVector(A.getn());
		SparseVector z = new SparseVector(A.getn());
		SparseVector u = new SparseVector(A.getn());
		SparseVector u1 = new SparseVector(A.getn());
		SparseVector u2 = new SparseVector(A.getn());
		SparseVector u3 = new SparseVector(A.getn());
		SparseVector u4 = new SparseVector(A.getn());

		int n = 0;
//		int n = 1;
		 while(n<A.getn())
		{
			fw.write(
					"\n================================================================================================"
							+ n);
			z = A.getRow(n);
			z.print("z0");
			fw.write("\nzo:\n" + z.toString());
			u = z.scale(0.5);
			u.print("u0");
			fw.write("\nuo:\n" + u.toString());

			System.out.println("============================================================"+ n);

			fw.write(
					"\n================================================================================================"
							+ n);
			int i = 0;
			// System.out.println(" n:"+ A.getn()+ " - "+ part/4 + "- "+ part/2
			// + " - "+(part*3/4)+ " - "+(part));
			
			JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input = 
					//toRDD(sc, A4AT);
					util.splitMatrix3(sc, A, A.getm(), 4);
			//TODO: (ATA +rho)^-1 not A
			System.out.println(" After splitMatrix2============================================================");
//			input.saveAsTextFile(filename + "/input" + Long.toString(System.currentTimeMillis()));
			
			
			
			//(A^TA + \rho I_m)^-1
			/**
			 * Not correct, should Ai Bi, not AB
			 */
			JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> _ATA_rho_Im_invert = ATb_ATA_rho_Im_invert(sc,
					input,n, rho);
//			_ATA_rho_Im_invert.saveAsTextFile(filename + "/ATb_ATA_rho_Im_invert" + Long.toString(System.currentTimeMillis()));

//			JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> tmp1 = null;
//			JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> tmp2 = null;
//			int indexRdd = 0;
//			for(String a:_ATA_rho_Im_invert.keys().collect())
//			{
////				System.out.println("Matrix "+a);
//				try
//				{
//					if(a.equals("1"))
//						tmp1=SparkMatrix.getMatFromList2(sc, input, a);//.saveAsTextFile(filename+ "/_out_"+a);
//					if(a.equals("2"))
//						tmp2=SparkMatrix.getMatFromList2(sc, input, a);//.saveAsTextFile(filename+ "/_out_"+a);
//				}
//				catch(Exception e)
//				{
//					System.out.println(e.getMessage());
//				}
//			}
			
//			SparkMatrix.mulMat2(sc, tmp1, tmp2).saveAsTextFile(filename+"/mullmath"+ Long.toString(System.currentTimeMillis()));
			
			while (true) {
				i++;
				System.out.println("============================================================loop time: " + i);
				fw.write("\n============================================================loop time: " + i + "\n");
				if (i > 200)
					break;

				SparseVector x_pre = x.copy();
				SparseVector z_pre = z.copy();
				SparseVector u_pre = u.copy();

				List<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> listU = new ArrayList<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>();
				// Tuple2<Integer,Iterable<Tuple2<Integer, Double>>> rowU = new
				// Tuple2<Integer, Iterable<Tuple2<Integer,Double>>>(_1, _2);
				// List
				List<Tuple2<Integer, Double>> rowU1 = new ArrayList<Tuple2<Integer, Double>>();
				List<Tuple2<Integer, Double>> rowU2 = new ArrayList<Tuple2<Integer, Double>>();
				List<Tuple2<Integer, Double>> rowU3 = new ArrayList<Tuple2<Integer, Double>>();
				List<Tuple2<Integer, Double>> rowU4 = new ArrayList<Tuple2<Integer, Double>>();
				for (int k = 0; k < u.size(); k++) {
					// System.out.println("k u:"+k);
					rowU1.add(new Tuple2<Integer, Double>(k, u1.get(k)));
					rowU2.add(new Tuple2<Integer, Double>(k, u2.get(k)));
					rowU3.add(new Tuple2<Integer, Double>(k, u3.get(k)));
					rowU4.add(new Tuple2<Integer, Double>(k, u4.get(k)));
				}
				ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> uArr = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
				uArr.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(0, rowU1));
				listU.add(uArr);
				uArr = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
				uArr.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(1, rowU2));
				listU.add(uArr);
				uArr = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
				uArr.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(2, rowU3));
				listU.add(uArr);
				uArr = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
				uArr.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(3, rowU4));
				listU.add(uArr);
				JavaRDD<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> uRDD = sc.parallelize(listU);

				// uRDD.saveAsTextFile(output+"/listU"+
				// Long.toString(System.currentTimeMillis()));
								
				JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> uPRDD = uRDD
						.flatMapToPair(
								new PairFlatMapFunction<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>, String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>() {

									public Iterable<Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> call(
											Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> t)
											throws Exception {
										// TODO Auto-generated method stub
										int index = t.iterator().next()._1;
										// System.out.println("index of u:
										// "+index);
										List<Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> ret = new ArrayList<Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>>();

										ret.add(new Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>(
												Integer.toString(index), t));
										return ret;
									}
								});
//				 uPRDD.saveAsTextFile(filename+"/uPRDD"+
//				 Long.toString(System.currentTimeMillis()));
				JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ATA_rho_ATb_U = _ATA_rho_Im_invert
						.union(uPRDD).groupByKey().flatMapToPair(
								new PairFlatMapFunction<Tuple2<String, Iterable<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>>, String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>() {

									public Iterable<Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> call(
											Tuple2<String, Iterable<Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> t)
											throws Exception {
										// TODO Auto-generated method stub
										List<Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> ret = new ArrayList<Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>>();
										List<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> ret2 = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
										for (Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> x : t._2) {
											for (Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> x2 : x) {
												ret2.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(x2._1,
														x2._2));
											}

										}
										ret.add(new Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>(
												t._1, ret2));
										return ret;
									}
								});
//				ATA_rho_ATb_U.saveAsTextFile(filename+"/ATA_rho_U"+
//				 Long.toString(System.currentTimeMillis()));

				u = u1.plus(u2).plus(u3).plus(u4).scale(0.25);//
				// System.out.println("before x_slaver3");
				/*
				 * x^{k+1} = 2(A^TA + \rho I_m)^-1 [ A^Tb - \rho (z^k - u^k)]
				 */
				final Broadcast<SparseVector> avrZ = sc.broadcast(z);
				final Broadcast<SparseVector> avrU = sc.broadcast(u);
				
				List<Tuple2<String, List<Double>>> x_slaver = x_slaver4(sc, ATA_rho_ATb_U, avrZ, z.size(), rho,
						output + "/x_slaver3" + Long.toString(System.currentTimeMillis()));
				x1 = new SparseVector(x_slaver.get(0)._2);
				x2 = new SparseVector(x_slaver.get(1)._2);
				x3 = new SparseVector(x_slaver.get(2)._2);
				x4 = new SparseVector(x_slaver.get(3)._2);

				// ATA_ATA_rho_1.
				x = x1.plus(x2).plus(x3).plus(x4).scale(0.25);//
				// x1.print("X1");
				// x2.print("X2");
				// x3.print("X3");
				// x4.print("X4");
				x.print("\nXk " + i);// ;

				// fw.write("\nx1: "+x1.toString());
				// fw.write("\nx2: "+x2.toString());
				// fw.write("\nx3: "+x3.toString());
				// fw.write("\nx4: "+x4.toString());
				// fw.write("x: "+x.toString());
				// u= u1.plus(u2).plus(u3).plus(u4).scale(4);//
				// u.print("Xxx");// ;

				// fw.write("\nrho_zk_uk: "+rho_zk_uk.toString());
				// rho_zk_uk.print("rho_zk_uk");
				// fw.write("\nXall: "+Xall.toString());
				// Xall.print(true, "Xall");

				// ATb_rho_zATbk_uk.print("ATb_rho_zATbk_uk");
				// fw.write("\nATb_rho_zATbk_uk: "+ATb_rho_zATbk_uk.toString());
				// x.print("Xk "+i);
				fw.write("\nXk " + i + ":" + x.toString());
				/*
				 * z^{k+1} = S_{\frag{\lambda}{\rho}}(x^{k+1} + u^k) = x^{k+1}
				 * +u^k - frag{\lambda}{\rho} if > = 0 if || \leq = x^{k+1} +u^k
				 * + frag{\lambda}{\rho} if <
				 */
				SparseVector tmp = x.plus(u);
				tmp.print("x+u ");
				// fw.write("\n x+u :"+tmp.toString());
				// SparseVector zTmp = z.copy();
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						z.put(j, tmp.get(j) - lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						z.put(j, tmp.get(j) + lambda / (rho), false);
					} else {
						z.put(j, 0, false);
					}
				}

				tmp = x1.plus(u1);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u1.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u1.put(j, -lambda / (rho), false);
					} else {
						u1.put(j, x1.get(j) + u1.get(j), false);
					}
				}

				tmp = x2.plus(u2);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u2.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u2.put(j, -lambda / (rho), false);
					} else {
						u2.put(j, x2.get(j) + u2.get(j), false);
					}
				}

				tmp = x3.plus(u3);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u3.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u3.put(j, -lambda / (rho), false);
					} else {
						u3.put(j, x3.get(j) + u3.get(j), false);
					}
				}

				tmp = x4.plus(u4);
				for (int j = 0; j < tmp.size(); j++) {

					if (tmp.get(j) > lambda / rho) {
						u4.put(j, lambda / (rho), false);
					} else if (tmp.get(j) < -1.0 * (lambda / rho)) {
						u4.put(j, -lambda / (rho), false);
					} else {
						u4.put(j, x4.get(j) + u4.get(j), false);
					}
				}

				z.print("Zk " + i);
				fw.write("\nZk " + i + ":" + z.toString());
				// fw.write("\nu1k "+i+":"+u1.toString());
				// fw.write("\nu2k "+i+":"+u2.toString());
				// fw.write("\nu3k "+i+":"+u3.toString());
				// fw.write("\nu4k "+i+":"+u4.toString());
				/*
				 * u^{k+1} = u^k + x^{k+1} - z^{k+1}
				 */
				// u.print("uk "+i);
				fw.write("\nuk " + i + ":" + u.toString());

				/*
				 * Check stop condition
				 */
				double r = x_pre.plus(z_pre.scale(-1)).norm();
				double s = z.plus(z_pre.scale(-1.0)).scale(rho).norm();

				double eP = (x_pre.norm() > z_pre.norm()) ? (epsilonA * Math.sqrt(A.getn()) + epsilonR * x_pre.norm())
						: (epsilonA * Math.sqrt(A.getn()) + epsilonR * z_pre.norm());
				double eD = epsilonA * Math.sqrt(A.getm()) + epsilonR * (u_pre.scale(rho).norm());
				System.out.println("x.norm(): (" + x_pre.norm() + ")---z.norm(): (" + z_pre.norm() + ")");
				System.out.println("r: (" + r + ")---eP: (" + eP + ")");
				System.out.println("s: (" + s + ")---eD: (" + eD + ")");
				fw.write("\nx.norm(): (" + x_pre.norm() + ")---z.norm(): (" + z_pre.norm() + ")\n" + "r: (" + r
						+ ")---eP: (" + eP + ")\n" + "s: (" + s + ")---eD: (" + eD + ")\n");
				if (i > 1) {
					if ((r <= eP) && (s <= eD)) {
						System.out.println("r<= eP) && (s<=eD) ---- STOP LOOP");
						fw.write("(r<= eP) && (s<=eD) ---- STOP LOOP\n");
						// fw.close();
						// return x;
					}
					if (s <= eD) {
						System.out.println("s<=eD");
						fw.write("(s<=eD)");
						// fw.close();
						ret.setRow(n, x);
						n++;
						break;
						// return x;
					}
					if (r <= eP) {
						System.out.println("r<= eP");
						fw.write("r<= eP)");
						// fw.close();
						// return x;
					}
				}

				if (r > (s * 10))
					rho = rho * 2;
				else if (s > (r * 10))
					rho = rho / 2;
				else
					rho = rho;

				// fw.write("rho "+rho);
				// System.out.println("rho "+rho);
				fw.write("rho " + rho + "\n============================================================");
				// System.out.println("============================================================");
			}
			
//			n++;
		}
		fw.write(ret.toString());
		fw.close();
		ret.print(false, "ret mat");
		return ret;

	}
	/*
	 * TODO
	 * From reduce matr, re-calc mat, mul Q to re-cal mat.
	 * return doc ID and sim rank
	 */
	public static SparseMatrix query(SparseMatrix A, SparseVector Q)
	{
		SparseMatrix ret = A;
		
		return A;
	}
}
