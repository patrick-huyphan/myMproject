package main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFlatMapFunction;

import Jama.Node;
import Jama.SparseMatrix;
import Jama.SparseVector;
import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple4;

public class SparkMatrix {
	
	/*
	 * 
	 * 
	 * 
	 * 
	 */
    public static void gaussian(JavaSparkContext sc, JavaRDD<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> A1, SparseMatrix a, int index[]) 
    {
        int n = index.length;
        double c[] = new double[n];
        
 // Initialize the index
//        for (int i=0; i<n; ++i) 
//            index[i] = i;
 
 // Find the rescaling factors, one from each row
//        System.out.println("START gaussian  "+Long.toString(System.currentTimeMillis()));
        for (int i=0; i<n; ++i) 
        {
        	index[i] = i;
        	c[i] = a.getRow(i).getMaxABS();
        	
//            double c1 = 0;
//            for (int j=0; j<n; ++j) 
//            {
//                double c0 = Math.abs(a.get(i, j));
//                if (c0 > c1) c1 = c0;
//            }
//            c[i] = c1;
//            System.out.println("getMaxABS " +c[i]+" "+Long.toString(System.currentTimeMillis()));
        }
//        System.out.println("gaussian 2 "+Long.toString(System.currentTimeMillis()));
 // Search the pivoting element from each column
        int k = 0;
        for (int j=0; j<n-1; ++j) 
        {
            double pi1 = 0;
//          System.out.println(j+ " (row) " +Long.toString(System.currentTimeMillis()));
          Node<Integer, Double> node = a.getColumn(j).getNode();
          Iterator<Integer> list1 = node.iterator();
        	while(list1.hasNext())
        	{
        		int id = list1.next();
        		if(id < j)
        			continue;

        		double pi0 = Math.abs(node.get(id));
        		pi0 /= c[index[id]];
                if (pi0 > pi1) 
                {
                    pi1 = pi0;
                    k = id;
                }
        	}
            
//            for (int i=j; i<n; ++i) 
//            {
//                double pi0 = Math.abs(a.get(index[i],j));
//                pi0 /= c[index[i]];
//                if (pi0 > pi1) 
//                {
//                    pi1 = pi0;
//                    k = i;
//                }
//            }
 
   // Interchange rows according to the pivoting order
            int itmp = index[j];
            index[j] = index[k];
            index[k] = itmp;
                        
   //Update remain element         
            for (int i=j+1; i<n; ++i) 	
            {
                double pj = a.get(index[i],j)/a.get(index[j],j);
 // Record pivoting ratios below the diagonal
                a.put(index[i], j, pj);
 
 // Modify other elements accordingly
                for (int l=j+1; l<n; ++l)
                	a.put(index[i], l, a.get(index[i], l)-pj*a.get(index[j],l));
            }
            
//            for (int i=j+1; i<n; ++i) 	
//            {
//                double pj = a.get(index[i],j)/a.get(index[j],j);
// // Record pivoting ratios below the diagonal
//                a.put(index[i], j, pj);
// 
// // Modify other elements accordingly
//                for (int l=j+1; l<n; ++l)
//                	a.put(index[i], l, a.get(index[i], l)-pj*a.get(index[j],l));
//            }
        }
//        System.out.println("END gaussian " +Long.toString(System.currentTimeMillis()));
    }
    
    
    /*
	 * 
	 * 
	 * 
	 * 
	 */
    public static void gaussian1(JavaSparkContext sc, JavaRDD<Tuple2<Tuple2<Integer,Integer>, Double>> A1, SparseMatrix a, int index[]) 
    {
        int n = index.length;
        double c[] = new double[n];
        
 // Initialize the index
//        for (int i=0; i<n; ++i) 
//            index[i] = i;
 
 // Find the rescaling factors, one from each row
//        System.out.println("START gaussian  "+Long.toString(System.currentTimeMillis()));
        for (int i=0; i<n; ++i) 
        {
        	index[i] = i;
        	c[i] = a.getRow(i).getMaxABS();
        	
//            double c1 = 0;
//            for (int j=0; j<n; ++j) 
//            {
//                double c0 = Math.abs(a.get(i, j));
//                if (c0 > c1) c1 = c0;
//            }
//            c[i] = c1;
//            System.out.println("getMaxABS " +c[i]+" "+Long.toString(System.currentTimeMillis()));
        }
//        System.out.println("gaussian 2 "+Long.toString(System.currentTimeMillis()));
 // Search the pivoting element from each column
        int k = 0;
        for (int j=0; j<n-1; ++j) 
        {
            double pi1 = 0;
//          System.out.println(j+ " (row) " +Long.toString(System.currentTimeMillis()));
          Node<Integer, Double> node = a.getColumn(j).getNode();
          Iterator<Integer> list1 = node.iterator();
        	while(list1.hasNext())
        	{
        		int id = list1.next();
        		if(id < j)
        			continue;

        		double pi0 = Math.abs(node.get(id));
        		pi0 /= c[index[id]];
                if (pi0 > pi1) 
                {
                    pi1 = pi0;
                    k = id;
                }
        	}
            
//            for (int i=j; i<n; ++i) 
//            {
//                double pi0 = Math.abs(a.get(index[i],j));
//                pi0 /= c[index[i]];
//                if (pi0 > pi1) 
//                {
//                    pi1 = pi0;
//                    k = i;
//                }
//            }
 
   // Interchange rows according to the pivoting order
            int itmp = index[j];
            index[j] = index[k];
            index[k] = itmp;
                        
   //Update remain element         
            for (int i=j+1; i<n; ++i) 	
            {
                double pj = a.get(index[i],j)/a.get(index[j],j);
 // Record pivoting ratios below the diagonal
                a.put(index[i], j, pj);
 
 // Modify other elements accordingly
                for (int l=j+1; l<n; ++l)
                	a.put(index[i], l, a.get(index[i], l)-pj*a.get(index[j],l));
            }
            
//            for (int i=j+1; i<n; ++i) 	
//            {
//                double pj = a.get(index[i],j)/a.get(index[j],j);
// // Record pivoting ratios below the diagonal
//                a.put(index[i], j, pj);
// 
// // Modify other elements accordingly
//                for (int l=j+1; l<n; ++l)
//                	a.put(index[i], l, a.get(index[i], l)-pj*a.get(index[j],l));
//            }
        }
//        System.out.println("END gaussian " +Long.toString(System.currentTimeMillis()));
    }
    
    /*
	 * 
	 * 
	 * 
	 * 
	 */
    
    
    public static SparseMatrix invert(JavaSparkContext sc, JavaRDD<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> A1, SparseMatrix input) 
    {
//    	System.out.println("invert");
    	SparseMatrix a = input.copy();
    	if(input.getm()!= input.getn())
    		 throw new ArrayIndexOutOfBoundsException(" invert2: m !=n");
        int n = a.getm();
        SparseMatrix x = new SparseMatrix (n,n);
        double b[][] = new double[n][n];
        int index[] = new int[n];
        for (int i=0; i<n; ++i) 
            b[i][i] = 1;
 
 // Transform the matrix into an upper triangle
        System.out.println("START gaussian "+Long.toString(System.currentTimeMillis()));
        gaussian(sc, A1, a, index);
        System.out.println("END gaussian " +Long.toString(System.currentTimeMillis()));
 // Update the matrix b[i][j] with the ratios stored
        for (int i=0; i<n-1; ++i)
            for (int j=i+1; j<n; ++j)
                for (int k=0; k<n; ++k)
                    b[index[j]][k] -= a.get(index[j],i)*b[index[i]][k];
 
 // Perform backward substitutions
        for (int i=0; i<n; ++i) 
        {
            x.put(n-1, i, b[index[n-1]][i]/a.get(index[n-1],n-1));
            for (int j=n-2; j>=0; --j) 
            {
            	double tmp = b[index[j]][i];
//            	x.put(j,i, b[index[j]][i]);
                for (int k=j+1; k<n; ++k) 
                {
//                	x.put(j, i, x.get(j, i) - a.get(index[j],k)*x.get(k,i));
                	tmp = tmp - a.get(index[j],k)*x.get(k,i);
                }
//                x.put(j, i,  x.get(j,i) / a.get(index[j],j));
                x.put(j, i,  tmp/a.get(index[j],j));
            }
        }
        System.out.println("END Invert "+Long.toString(System.currentTimeMillis()));
        return x;
    }
    
    
    /*
	 * 
	 * 
	 * 
	 * 
	 */
    public static SparseMatrix invert1(JavaSparkContext sc, JavaRDD<Tuple2<Tuple2<Integer,Integer>, Double>> A1, SparseMatrix input) 
    {
//    	System.out.println("invert");
    	SparseMatrix a = input.copy();
    	if(input.getm()!= input.getn())
    		 throw new ArrayIndexOutOfBoundsException(" invert2: m !=n");
        int n = a.getm();
        SparseMatrix x = new SparseMatrix (n,n);
        double b[][] = new double[n][n];
        int index[] = new int[n];
        for (int i=0; i<n; ++i) 
            b[i][i] = 1;
 
 // Transform the matrix into an upper triangle
        System.out.println("START gaussian "+Long.toString(System.currentTimeMillis()));
        gaussian1(sc, A1, a, index);
        System.out.println("END gaussian " +Long.toString(System.currentTimeMillis()));
 // Update the matrix b[i][j] with the ratios stored
        for (int i=0; i<n-1; ++i)
            for (int j=i+1; j<n; ++j)
                for (int k=0; k<n; ++k)
                    b[index[j]][k] -= a.get(index[j],i)*b[index[i]][k];
 
 // Perform backward substitutions
        for (int i=0; i<n; ++i) 
        {
            x.put(n-1, i, b[index[n-1]][i]/a.get(index[n-1],n-1));
            for (int j=n-2; j>=0; --j) 
            {
            	double tmp = b[index[j]][i];
//            	x.put(j,i, b[index[j]][i]);
                for (int k=j+1; k<n; ++k) 
                {
//                	x.put(j, i, x.get(j, i) - a.get(index[j],k)*x.get(k,i));
                	tmp = tmp - a.get(index[j],k)*x.get(k,i);
                }
//                x.put(j, i,  x.get(j,i) / a.get(index[j],j));
                x.put(j, i,  tmp/a.get(index[j],j));
            }
        }
        System.out.println("END Invert "+Long.toString(System.currentTimeMillis()));
        return x;
    }
    
    
    /*
	 * 
	 * 
	 * 
	 * 
	 */
    public static SparseMatrix echolonConvert(JavaSparkContext sc, JavaRDD<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> A1, SparseMatrix input){ // throws IOException 
    	SparseMatrix X= input.copy();//new SparseMatrix(m, n);
        if(X.getm()>X.getn())
            throw new ArrayIndexOutOfBoundsException(" echolonConvert: m>n");
        if (X.getRows()[0].get(0) <= 0) {
            int j = 1;
            while (X.getRows()[0].get(0) <= 0 && j < X.getm()) {
                X = X.swapRow(0, j);
                j++;
            }
        }
//    	X.print("X0 ");
        
        int i = 0;
        int j = 0;
        int l = 0;
        while (true) {
            j = i+1;
            while (j < X.getm()) {
//    			System.out.println(j+": "+X.rows[j].get(0));
                if (X.getRows()[j].get(l) != 0) {
//        			System.out.println(j+"-"+i+": "+X.rows[j].get(i-1)+"==="+"-"+ X.rows[i-1].get(i-1));
                    double tmp = (-1.0) * X.getRows()[j].get(l) / X.getRows()[i].get(l);
//        			System.out.println(j+"-"+i+": "+tmp);
                    X.getRows()[j] = X.getRows()[j].plus(X.getRows()[i].scale(tmp));
//        			X.rows[j].print();
                }
                j++;
            }
//            X.print("X0 " + i);
            i++;
            l++;
//            System.err.println("else i " + i);
            if((i == X.getm()-1) || (l == X.getn() -1) )
                break;
            else
            {
                int k = i;
//                System.err.println("else i " +i + " l: "+l);
//                System.err.println("else i " +i + " l: "+l +" "+ X.rows[i].get(l));
                if (X.getRows()[i].get(l) == 0) {
                    while(X.getRows()[i].get(l) == 0 && i<X.getm())
                    {
//                        System.err.println("else i " +i + " L: "+l);
                        if(X.getRows()[i].isZeroVector())
                        {
                            System.err.println("X.rows[i].isZeroVector()");
                        }
                        j = i+1;
//                        System.err.println(" j " +j + " l: "+l +" "+ X.rows[j].get(l));
                        while (X.getRows()[j].get(l) == 0 && j < X.getm()-1) {
                            j++;
                            if( j == X.getm()-1)
                            {
                                l++;
                            }
                        }
                        X = X.swapRow(i, j);
//                        System.err.println("SWAP: "+ i+" - "+j);

                    }
                }
            }
        }
        X.print(true,"echelon U'");
        /*
        TODO: remove linear independent vector column 
        */
        SparseMatrix ret = input.copy();
        for(int row = X.getm()-1; row>0; row--)
        {
            if(X.getRow(row).nnz()==0){
                System.err.println("Zero row: "+ row);
//                X.removeColumn(row)
//                X.m = X.m--;
            }
        }
        for(int col = X.getn()-1; col>0; col--)
        {
            if(X.getColumn(col).nnz()==0){
                System.err.println("Zero col: "+ col);
//                ret = ret.removeColumn(col);
            }
        }
//        X.print("X");
        for(int col = X.getn()-1; col >0; col--)
        {
            for(int k = col-1; k>0; k--)
            {
                if(X.getColumn(col).isDepenVector(X.getColumn(k)))
                {
//                    X.getColumn(col).print("C "+col);
//                    X.getColumn(k).print("k "+k);

                	ret = ret.removeColumn(col);
                  col --;
//                  ret.setColumn(new SparseVector(X.n), col); //
//                    X.transpose().print("X A remove " +X.m+"-"+X.n);
                }
            }
        }
        return ret;
    }
    
    /*
	 * 
	 * 
	 * 
	 * 
	 */
    public static List<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> toRDDMatrix(SparseMatrix input) {
		// List<Iterable<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>>>
		// retM = new
		// ArrayList<Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>();
		List<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> matrix = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
		int i = 0;
		for (SparseVector d : input.getRows()) {
			List<Tuple2<Integer, Double>> row = new ArrayList<Tuple2<Integer, Double>>();
			for (int j = 0; j < d.size(); j++) {
				row.add(new Tuple2<Integer, Double>(j, d.get(j)));
			}
			matrix.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(i, row));
			i++;
		}
		// retM.add(matrix);
		return matrix;
	}
    /*
	 * 
	 * 
	 * 
	 * 
	 */
    public static List<Tuple2<Tuple2<Integer,Integer>, Double>> toRDDMatrix_2(SparseMatrix input) {
		// List<Iterable<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>>>
		// retM = new
		// ArrayList<Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>();
    	List<Tuple2<Tuple2<Integer,Integer>, Double>> matrix = new ArrayList<Tuple2<Tuple2<Integer,Integer>, Double>>();
		int i = 0;
		for (SparseVector d : input.getRows()) {
			for (int j = 0; j < d.size(); j++) {
				matrix.add(new Tuple2<Tuple2<Integer,Integer>, Double>(new Tuple2<Integer, Integer>(i, j), d.get(j)));
			}
			i++;
		}
		return matrix;
	}
    /*
	 * 
	 * 
	 * 
	 * 
	 */
	public static List<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> MergeMatrixtoRDDMatrix(SparseMatrix input,
			SparseMatrix input2) {
		// List<Iterable<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>>>
		// retM = new
		// ArrayList<Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>();
		List<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> matrix = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
		int i = 0;
		for (SparseVector d : input.getRows()) {
			List<Tuple2<Integer, Double>> row = new ArrayList<Tuple2<Integer, Double>>();
			for (int j = 0; j < d.size(); j++) {
				row.add(new Tuple2<Integer, Double>(j, d.get(j)));
			}
			matrix.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(i, row));
			i++;
		}
		i = 0;
		for (SparseVector d : input2.getRows()) {
			List<Tuple2<Integer, Double>> row = new ArrayList<Tuple2<Integer, Double>>();
			for (int j = 0; j < d.size(); j++) {
				row.add(new Tuple2<Integer, Double>(j, d.get(j)));
			}
			matrix.add(new Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>(i, row));
			i++;
		}
		return matrix;
	}
	/*
	 * 
	 * 
	 * 
	 * 
	 */
	public static List<Tuple2<Tuple2<Integer,Integer>, Double>> MergeMatrixtoRDDMatrix_2(SparseMatrix input, SparseMatrix input2) {
		// List<Iterable<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>>>
		// retM = new
		// ArrayList<Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>();
    	List<Tuple2<Tuple2<Integer,Integer>, Double>> matrix = new ArrayList<Tuple2<Tuple2<Integer,Integer>, Double>>();
		int i = 0;
		for (SparseVector d : input.getRows()) {
			for (int j = 0; j < d.size(); j++) {
				matrix.add(new Tuple2<Tuple2<Integer,Integer>, Double>(new Tuple2<Integer, Integer>(i, j), d.get(j)));
			}
			i++;
		}
		i = 0;
		for (SparseVector d : input2.getRows()) {
			for (int j = 0; j < d.size(); j++) {
				matrix.add(new Tuple2<Tuple2<Integer,Integer>, Double>(new Tuple2<Integer, Integer>(i, j), d.get(j)));
			}
			i++;
		}
		return matrix;
	}
	/*
	 * 
	 * 
	 * 
	 * 
	 */
    public static List<Tuple2<Tuple2<Integer,Integer>, Double>> tranpose(JavaSparkContext sc, JavaRDD<Tuple2<Tuple2<Integer,Integer>, Double>> input) {
		// List<Iterable<Tuple2<Integer,Iterable<Tuple2<Integer, Double>>>>>
		// retM = new
		// ArrayList<Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>();
    	List<Tuple2<Tuple2<Integer,Integer>, Double>> matrix = new ArrayList<Tuple2<Tuple2<Integer,Integer>, Double>>();

    	
    	
		return matrix;
	}
    /*
	 * 
	 * 
	 * 
	 * 
	 */
    public static void LUDecomposition(JavaSparkContext sc, JavaRDD<Tuple2<Tuple2<Integer,Integer>, Double>> A1) 
    {
    	
    }
    /*
	 * 
	 * 
	 * 
	 * 
	 */
//    public static void MultipleMatrix(JavaSparkContext sc, JavaRDD<Tuple2<Tuple2<Integer,Integer>, Double>> A1, JavaRDD<Tuple2<Tuple2<Integer,Integer>, Double>> A2) 
//    {
//    	
//    }
    /*
	 * 
	 * 
	 * 
	 * 
	 */
    
    public static JavaRDD<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> getMatFromList(
			final JavaSparkContext sc, JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input, final String mathID) {
		
		JavaRDD<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> ret = input.filter(new Function<Tuple2<String,Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>, Boolean>() {
			@Override
			public Boolean call(Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> v1)
					throws Exception {
				// TODO Auto-generated method stub
				return v1._1.equals(mathID);
			}
		}).flatMap(new FlatMapFunction<Tuple2<String,Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>, Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>() {

			@Override
			public Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> call(
					Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> t) throws Exception {
				// TODO Auto-generated method stub
				return t._2;
			}
		});

		return ret;
	}
    /*
	 * 
	 * 
	 * 
	 * 
	 */
    static List<Tuple2<Tuple2<Integer, Integer>, Double>> fromRow2ele(Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> t)
    {
		List<Tuple2<Tuple2<Integer, Integer>, Double>> ret = new ArrayList<Tuple2<Tuple2<Integer, Integer>, Double>>();
		{
			for(Tuple2<Integer, Double> ele: t._2)
			{
				if(ele._2 != 0)
				{
//					System.out.println("("+t._1+"-"+ele._1+") :"+ele._2);
					ret.add(new Tuple2<Tuple2<Integer,Integer>, Double>(new Tuple2<Integer,Integer>(t._1,ele._1) , ele._2));
				}
			}
		}
		return ret;
    }
    /*
	 * 
	 * 
	 * 
	 * 
	 */
	public static JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> getMatFromList2(
			final JavaSparkContext sc, JavaRDD<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> input)
	{
		JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> ret = input.flatMap(new FlatMapFunction<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>, Tuple2<Tuple2<Integer, Integer>, Double>>() {

			@Override
			public Iterable<Tuple2<Tuple2<Integer, Integer>, Double>> call(
					Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> t) throws Exception {
				// TODO Auto-generated method stub
//					List<Tuple2<Tuple2<Integer, Integer>, Double>> ret = new ArrayList<Tuple2<Tuple2<Integer, Integer>, Double>>();
//					{
//						for(Tuple2<Integer, Double> ele: t._2)
//						{
//							if(ele._2 != 0)
//							{
////								System.out.println("("+t._1+"-"+ele._1+") :"+ele._2);
//								ret.add(new Tuple2<Tuple2<Integer,Integer>, Double>(new Tuple2<Integer,Integer>(t._1,ele._1) , ele._2));
//							}
//						}
//					}
					return fromRow2ele(t);
			}
		});
		return ret;
	}
	/*
	 * 
	 * 
	 * 
	 * 
	 */
	public static JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> getMatFromList2(
			final JavaSparkContext sc, JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input, final String mathID) {
		return getMatFromList2(sc, getMatFromList(sc, input, mathID));
	}
	/*
	 * 
	 * 
	 * 
	 * 
	 */
	public static JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> union2Math(JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input1, JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input2)
	{
		return input1.union(input2);
	}
	/*
	 * 
	 * 
	 * 
	 * 
	 */
	public static JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> mulMat(
			final JavaSparkContext sc, 
			final JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input,
			final JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input2,
			final double rho, String ouput) {
		
		JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> mat = input.union(input2);
		JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ret = null;
		//get mat a, add key to row, get mat b add key to col
		mat.flatMapToPair(new PairFlatMapFunction<Tuple2<String,Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>, String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>() {

			@Override
			public Iterable<Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> call(
					Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> t) throws Exception {
				// TODO Auto-generated method stub
				if(t._1.equals(input2.keys().collect().get(0)))
				{
					
				}
				return null;
			}
		})
		//reduce by key
		;
		
		return ret;
	}
	/*
	 * 
	 * 
	 * 
	 * 
	 */
	static Iterable<Tuple2<Tuple3<String, Integer,Integer>,Double>> addKey(
			Tuple2<Tuple2<Integer, Integer>, Double> t, String mat1Name) throws Exception {
		// TODO Auto-generated method stub
		List<Tuple2<Tuple3<String,Integer, Integer>, Double>> ret = new ArrayList<Tuple2<Tuple3<String,Integer, Integer>, Double>>();
		ret.add(new Tuple2<Tuple3<String,Integer,Integer>, Double>(new Tuple3<String,Integer,Integer>(mat1Name, t._1._1, t._1._2), t._2));
		return ret;
	}
	/*
	 * 
	 * 
	 * 
	 * 
	 */
	public static JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> mulMat2(
			final JavaSparkContext sc, 
			final JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> input,
			final JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> input2) {
		final String mat1Name = "mat1";
		
//		JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> ret = null;
		
		return input.flatMapToPair(new PairFlatMapFunction<Tuple2<Tuple2<Integer,Integer>,Double>, Tuple3<String, Integer,Integer>,Double>() {

			@Override
			public Iterable<Tuple2<Tuple3<String, Integer,Integer>,Double>> call(
					Tuple2<Tuple2<Integer, Integer>, Double> t) throws Exception {
				return addKey(t, "mat1");
			}
		})
		.union(
		input2.flatMapToPair(new PairFlatMapFunction<Tuple2<Tuple2<Integer,Integer>,Double>, Tuple3<String, Integer,Integer>,Double>() {

			@Override
			public Iterable<Tuple2<Tuple3<String, Integer,Integer>,Double>> call(
					Tuple2<Tuple2<Integer, Integer>, Double> t) throws Exception {
				// TODO Auto-generated method stub				
				return addKey(t, "mat2");
			}
		}))
		/*
		 * TODO: from here, update key2, not need 2 index
		 */
		.flatMapToPair(new PairFlatMapFunction<Tuple2<Tuple3<String,Integer,Integer>,Double>, Tuple2<Integer,Integer>, Tuple3<Double, Tuple2<Integer,Integer>, Tuple2<Integer,Integer>>>() {

			@Override
			public Iterable<Tuple2<Tuple2<Integer,Integer>, Tuple3<Double, Tuple2<Integer,Integer>, Tuple2<Integer,Integer>>>> call(
					Tuple2<Tuple3<String, Integer, Integer>, Double> t) throws Exception {
				// TODO Auto-generated method stub
				List<Tuple2<Tuple2<Integer,Integer>,  Tuple3<Double, Tuple2<Integer,Integer>, Tuple2<Integer,Integer>>>> ret = new ArrayList<Tuple2<Tuple2<Integer,Integer>,  Tuple3<Double, Tuple2<Integer,Integer>, Tuple2<Integer,Integer>>>>();
				if(t._1._1().equals("mat1"))
				{
					ret.add(new Tuple2<Tuple2<Integer,Integer>,  Tuple3<Double, Tuple2<Integer,Integer>, Tuple2<Integer,Integer>>>(new Tuple2<Integer,Integer>(t._1._2(),t._1._3()), new Tuple3<Double, Tuple2<Integer,Integer>, Tuple2<Integer,Integer>>(t._2, new Tuple2<Integer,Integer>( t._1._2(),t._1._3()), new Tuple2<Integer,Integer>( t._1._2(),t._1._3()))));
				}
				else
				{
					ret.add(new Tuple2<Tuple2<Integer,Integer>,  Tuple3<Double, Tuple2<Integer,Integer>, Tuple2<Integer,Integer>>>(new Tuple2<Integer,Integer>(t._1._3(),t._1._2()), new Tuple3<Double, Tuple2<Integer,Integer>, Tuple2<Integer,Integer>>(t._2,new Tuple2<Integer,Integer>( t._1._2(),t._1._3()), new Tuple2<Integer,Integer>( t._1._2(),t._1._3()))));
				}
				return ret;
			}
		})
		.reduceByKey(new Function2<Tuple3<Double,Tuple2<Integer,Integer>,Tuple2<Integer,Integer>>, Tuple3<Double,Tuple2<Integer,Integer>,Tuple2<Integer,Integer>>, Tuple3<Double,Tuple2<Integer,Integer>,Tuple2<Integer,Integer>>>() {
			
			@Override
			public Tuple3<Double, Tuple2<Integer, Integer>, Tuple2<Integer, Integer>> call(
					Tuple3<Double, Tuple2<Integer, Integer>, Tuple2<Integer, Integer>> v1,
					Tuple3<Double, Tuple2<Integer, Integer>, Tuple2<Integer, Integer>> v2) throws Exception {
				// TODO Auto-generated method stub
				
				return new Tuple3<Double, Tuple2<Integer,Integer>, Tuple2<Integer,Integer>>(v1._1()*v2._1(), v1._2(), v2._2());
			}
		})
		.flatMapToPair(new PairFlatMapFunction<Tuple2<Tuple2<Integer,Integer>,Tuple3<Double,Tuple2<Integer,Integer>,Tuple2<Integer,Integer>>>, Tuple2<Integer,Integer>, Tuple2<Tuple2<Integer,Integer>,Double>>() {

			@Override
			public Iterable<Tuple2<Tuple2<Integer, Integer>, Tuple2<Tuple2<Integer, Integer>,Double>>> call(
					Tuple2<Tuple2<Integer, Integer>, Tuple3<Double, Tuple2<Integer, Integer>, Tuple2<Integer, Integer>>> t)
					throws Exception {
				// TODO Auto-generated method stub
				List<Tuple2<Tuple2<Integer, Integer>, Tuple2<Tuple2<Integer, Integer>,Double>>> ret = new ArrayList<Tuple2<Tuple2<Integer, Integer>, Tuple2<Tuple2<Integer, Integer>,Double>>>();
				{
					ret.add(new Tuple2<Tuple2<Integer,Integer>, Tuple2<Tuple2<Integer,Integer>,Double>>(new Tuple2<Integer,Integer>(t._2._2()._1, t._2._2()._1), new Tuple2<Tuple2<Integer,Integer>,Double>(new Tuple2<Integer,Integer>(t._2._2()._1, t._2._2()._1),t._2._1() )));
				}
				return ret;
			}
		})
		.reduceByKey(new Function2<Tuple2<Tuple2<Integer,Integer>,Double>, Tuple2<Tuple2<Integer,Integer>,Double>, Tuple2<Tuple2<Integer,Integer>,Double>>() {
			
			@Override
			public Tuple2<Tuple2<Integer, Integer>, Double> call(Tuple2<Tuple2<Integer, Integer>, Double> v1,
					Tuple2<Tuple2<Integer, Integer>, Double> v2) throws Exception {
				// TODO Auto-generated method stub
				List<Tuple2<Tuple2<Integer, Integer>, Double>> ret = new ArrayList<Tuple2<Tuple2<Integer, Integer>, Double>>();
				
				return new Tuple2<Tuple2<Integer,Integer>, Double>(v1._1, v1._2 + v2._2);
			}
		}).flatMap(new FlatMapFunction<Tuple2<Tuple2<Integer,Integer>,Tuple2<Tuple2<Integer,Integer>,Double>>, Tuple2<Tuple2<Integer, Integer>, Double>>() {

			@Override
			public Iterable<Tuple2<Tuple2<Integer, Integer>, Double>> call(
					Tuple2<Tuple2<Integer, Integer>, Tuple2<Tuple2<Integer, Integer>, Double>> t) throws Exception {
				// TODO Auto-generated method stub
				List<Tuple2<Tuple2<Integer, Integer>, Double>> ret = new ArrayList<Tuple2<Tuple2<Integer, Integer>, Double>>();
				
				ret.add(new Tuple2<Tuple2<Integer,Integer>, Double>(t._1, t._2._2));
				return ret;
			}
		});
		
//		return ret;
	}
	
	/*
	 * 
	 * 
	 * 
	 * 
	 */
	
	public static JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> plusMat(
			final JavaSparkContext sc, 
			JavaPairRDD<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input1,
			JavaPairRDD<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input2,
			String ouput) {
		JavaPairRDD<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input = input1.union(input2);
		JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ret = input.flatMapToPair(new PairFlatMapFunction<Tuple2<Integer,Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>, String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>() {

			@Override
			public Iterable<Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> call(
					Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> t) throws Exception {
				// TODO Auto-generated method stub
				return null;
			}
		});
		return ret;
	}
	/*
	 * 
	 * 
	 * 
	 * 
	 */
	public static JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> plusMat2(
			final JavaSparkContext sc, 
			final JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> input,
			final JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> input2,
			String ouput) {
		JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> ret = null;
		
		return 
		input.flatMapToPair(new PairFlatMapFunction<Tuple2<Tuple2<Integer,Integer>,Double>, Tuple3<String, Integer,Integer>,Double>() {

			@Override
			public Iterable<Tuple2<Tuple3<String, Integer,Integer>,Double>> call(
					Tuple2<Tuple2<Integer, Integer>, Double> t) throws Exception {
				return addKey(t, "mat1");
			}
		})
		.union(
		input2.flatMapToPair(new PairFlatMapFunction<Tuple2<Tuple2<Integer,Integer>,Double>, Tuple3<String, Integer,Integer>,Double>() {

			@Override
			public Iterable<Tuple2<Tuple3<String, Integer,Integer>,Double>> call(
					Tuple2<Tuple2<Integer, Integer>, Double> t) throws Exception {
				// TODO Auto-generated method stub				
				return addKey(t, "mat2");
			}
		}))
		/*
		 * TODO: from here, update key2, not need 2 index
		 */
		.flatMapToPair(new PairFlatMapFunction<Tuple2<Tuple3<String,Integer,Integer>,Double>, Tuple2<Integer,Integer>, Double>() {

			@Override
			public Iterable<Tuple2<Tuple2<Integer,Integer>, Double>> call(
					Tuple2<Tuple3<String, Integer, Integer>, Double> t) throws Exception {
				// TODO Auto-generated method stub
				List<Tuple2<Tuple2<Integer,Integer>, Double>> ret = new ArrayList<Tuple2<Tuple2<Integer,Integer>, Double>>();
//				if(t._1._1().equals("mat1"))
//				{
//					ret.add(new Tuple2<Tuple2<Integer,Integer>,  Tuple3<Double, Tuple2<Integer,Integer>, Tuple2<Integer,Integer>>>(new Tuple2<Integer,Integer>(t._1._2(),t._1._3()), new Tuple3<Double, Tuple2<Integer,Integer>, Tuple2<Integer,Integer>>(t._2, new Tuple2<Integer,Integer>( t._1._2(),t._1._3()), new Tuple2<Integer,Integer>( t._1._2(),t._1._3()))));
//				}
//				else
				{
					ret.add(new Tuple2<Tuple2<Integer,Integer>, Double>(new Tuple2<Integer,Integer>(t._1._2(),t._1._3()), t._2()));
				}
				
				return ret;
			}
		})
		.reduceByKey(new Function2<Double, Double, Double>() {
			
			@Override
			public Double call(Double v1, Double v2) throws Exception {
				// TODO Auto-generated method stub
				return v1+v2;
			}
		}).flatMap(new FlatMapFunction<Tuple2<Tuple2<Integer,Integer>,Double>, Tuple2<Tuple2<Integer,Integer>,Double>>() {

			@Override
			public Iterable<Tuple2<Tuple2<Integer, Integer>, Double>> call(Tuple2<Tuple2<Integer, Integer>, Double> t)
					throws Exception {
				// TODO Auto-generated method stub
				List<Tuple2<Tuple2<Integer, Integer>, Double>> ret = new ArrayList<Tuple2<Tuple2<Integer, Integer>, Double>>();
				ret.add(new Tuple2<Tuple2<Integer,Integer>, Double>(t._1, t._2));  
				return ret ;
			}
		});
	}
	/*
	 * 
	 * 
	 * 
	 * 
	 */
	public static JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> scaleMat(
			final JavaSparkContext sc, JavaPairRDD<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input,
			final double scale) {
		JavaPairRDD<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ret = input.flatMapToPair(new PairFlatMapFunction<Tuple2<Integer,Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>, String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>() {

			@Override
			public Iterable<Tuple2<String, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> call(
					Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> t) throws Exception {
				// TODO Auto-generated method stub
				return null;
			}
		});
		return ret;
	}
	/*
	 * 
	 * 
	 * 
	 * 
	 */
	public static JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> scaleMat2(
			final JavaSparkContext sc, JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> input, 
			final double scale) {
		JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> ret = input.flatMap(new FlatMapFunction<Tuple2<Tuple2<Integer,Integer>,Double>, Tuple2<Tuple2<Integer, Integer>, Double>>() {

			@Override
			public Iterable<Tuple2<Tuple2<Integer, Integer>, Double>> call(Tuple2<Tuple2<Integer, Integer>, Double> t)
					throws Exception {
				// TODO Auto-generated method stub
				List<Tuple2<Tuple2<Integer, Integer>, Double>> ret = new ArrayList<Tuple2<Tuple2<Integer, Integer>, Double>>();
				ret.add(new Tuple2<Tuple2<Integer,Integer>, Double>(t._1, t._2*scale));
				return ret;
			}
		});
		return ret;
	}
	/*
	 * 
	 * 
	 * 
	 * 
	 */
	public static JavaPairRDD<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> transpose(
			final JavaSparkContext sc, JavaPairRDD<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> input) {
//		JavaPairRDD<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ret = input.flatMapToPair(new PairFlatMapFunction<Tuple2<Integer,Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>, Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>() {
//
//			@Override
//			public Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> call(
//					Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> t) throws Exception {
//				// TODO Auto-generated method stub
//				List<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> ret = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>>();
//				List<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>> mat = new ArrayList<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>();
//				for(Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> row: t._2)
//				{
//					List<Tuple2<Integer,Double>> rows = new ArrayList<Tuple2<Integer,Double>>();
//					for(Tuple2<Integer, Double> ele: row._2)
//					{
//						rows.add(new Tuple2<Integer, Double>(t._1, ele._2));
//					}
//					mat.add(new Tuple2<Integer, Iterable<Tuple2<Integer,Double>>>(1, rows));
//				}
//				ret.add(new Tuple2<Integer, Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>(t._1, mat));
//				return ret;
//			}
//		});
		JavaPairRDD<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> ret = 
				input.flatMapToPair(new PairFlatMapFunction<Tuple2<Integer,Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>, Integer, Iterable<Tuple2<Tuple2<Integer, Integer>, Double>>>() {

					@Override
					public Iterable<Tuple2<Integer, Iterable<Tuple2<Tuple2<Integer, Integer>, Double>>>> call(
							Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>> t)
							throws Exception {
						// TODO Auto-generated method stub
						List<Tuple2<Integer, Iterable<Tuple2<Tuple2<Integer, Integer>, Double>>>> ret = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Tuple2<Integer, Integer>, Double>>>>();
						
						for(Tuple2<Integer, Iterable<Tuple2<Integer, Double>>> row: t._2)
						{
							ret.add(new Tuple2<Integer, Iterable<Tuple2<Tuple2<Integer,Integer>,Double>>>(t._1, fromRow2ele(row))) ;
						}
						
						return ret;
					}
		}).flatMapToPair(new PairFlatMapFunction<Tuple2<Integer,Iterable<Tuple2<Tuple2<Integer,Integer>,Double>>>, Integer,Iterable<Tuple2<Tuple2<Integer,Integer>,Double>>>() {
			@Override
			public Iterable<Tuple2<Integer, Iterable<Tuple2<Tuple2<Integer, Integer>, Double>>>> call(
					Tuple2<Integer, Iterable<Tuple2<Tuple2<Integer, Integer>, Double>>> t) throws Exception {
				// TODO Auto-generated method stub
				List<Tuple2<Integer, Iterable<Tuple2<Tuple2<Integer, Integer>, Double>>>> ret = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Tuple2<Integer, Integer>, Double>>>>();
				List<Tuple2<Tuple2<Integer, Integer>, Double>> ele = new ArrayList<Tuple2<Tuple2<Integer, Integer>, Double>>();
				for(Tuple2<Tuple2<Integer, Integer>, Double> mat : t._2)
				{
					ele.add(new Tuple2<Tuple2<Integer,Integer>, Double>(new Tuple2<Integer,Integer>(mat._1._2,mat._1._1), mat._2));
				}
				ret.add(new Tuple2<Integer, Iterable<Tuple2<Tuple2<Integer,Integer>,Double>>>(t._1, ele));
				return ret;
			}
		}).flatMapToPair(new PairFlatMapFunction<Tuple2<Integer,Iterable<Tuple2<Tuple2<Integer,Integer>,Double>>>, Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>() {

			@Override
			public Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> call(
					Tuple2<Integer, Iterable<Tuple2<Tuple2<Integer, Integer>, Double>>> t) throws Exception {
				// TODO Auto-generated method stub
				List<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>> ret = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>>>();
				List<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> mat = new ArrayList<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>>();
				for(Tuple2<Tuple2<Integer, Integer>, Double> ele : t._2)
				{
//					mat
				}
				ret.add(new Tuple2<Integer, Iterable<Tuple2<Integer,Iterable<Tuple2<Integer,Double>>>>>(t._1, mat));
				return ret;
			}
		});
		return ret;
	}
	/*
	 * 
	 * 
	 * 
	 * 
	 */
	public static JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> transpose2(
			final JavaSparkContext sc, JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> input) {
		JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> ret = input.flatMap(new FlatMapFunction<Tuple2<Tuple2<Integer,Integer>,Double>, Tuple2<Tuple2<Integer, Integer>, Double>>() {

			@Override
			public Iterable<Tuple2<Tuple2<Integer, Integer>, Double>> call(Tuple2<Tuple2<Integer, Integer>, Double> t)
					throws Exception {
				// TODO Auto-generated method stub
				List<Tuple2<Tuple2<Integer, Integer>, Double>> ret = new ArrayList<Tuple2<Tuple2<Integer, Integer>, Double>>();
					ret.add(new Tuple2<Tuple2<Integer,Integer>, Double>(new Tuple2<Integer,Integer>(t._1._2,t._1._1), t._2));
				return ret;
			}
		});
		return ret;
	}
	/*
	 * 
	 * 
	 * 
	 * 
	 */
	public static JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> ToSparkMatrix( JavaSparkContext sc, SparseMatrix input) {
		
		int with = input.getn();
		int high = input.getm();
		ArrayList<Tuple2<Tuple2<Integer, Integer>, Double>> row = new ArrayList<Tuple2<Tuple2<Integer, Integer>, Double>>();
		for (int i= 0; i< high; i++)
		{
			for (int j= 0; j< high; j++)
			{
				Tuple2<Tuple2<Integer, Integer>, Double> node = new Tuple2<Tuple2<Integer,Integer>, Double>(new Tuple2<Integer,Integer>(i, j), input.get(i, j));
				row.add(node);
			}
		}
		JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> ret = sc.parallelize(row);
		return ret;
	}

}
