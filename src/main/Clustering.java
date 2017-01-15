package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFlatMapFunction;
import org.netlib.util.booleanW;

import Jama.SparseMatrix;
import Jama.SparseVector;
import scala.Tuple2;

public class Clustering {
	
	/*
	 * TODO
	 * min(X-A)
	 * NOTE: U and V calculated base on row of A, not column
	 * should check again, result not same as expected, should related to lambda
	 */
	
	static SparseVector proc_N1(double sic, SparseVector V)
	{
		SparseVector ret = new SparseVector(V.size());
//		double sic = lambda*we/rho;
//		double ve = V.norm();
		
		for(int i = 0; i< V.size(); i++)
		{
			if(V.get(i)>sic)
			{
				ret.put(i, V.get(i) - sic);
			}
			else if(V.get(i) < -sic)
			{
				ret.put(i, V.get(i) + sic);
			}
			else
			{
				ret.put(i, 0);
			}
		}
		return ret;
	};
//	
//	double[] prox_l1(double x[], int n, double rho) 
//	{
//	    int i;
//	    for (i = 0; i < n; i++) {
//	        x[i] = ( 0 > ((x[i] - 1.0/rho) - (0 > (-x[i] - 1.0/rho)? 0: (-x[i] - 1.0/rho))) )? 0 : ((x[i] - 1.0/rho) - (0 > (-x[i] - 1.0/rho)? 0: (-x[i] - 1.0/rho)));
//	    }
//	    
//	    return x;
//	}
	
//	double[] prox_norm2(double x[],  double rho, int n) 
//	{
//	    double norm_square = 0;
//	    int i;
//
//	    for (i = 0; i < n; i++) {
//	        norm_square += x[i]*x[i];
//	    }
//
//	    double norm = Math.sqrt(norm_square);
//
//	    for (i = 0; i < n; i++) {
//	        if (norm >= 1./rho)   x[i] *= (1. - 1./(rho*norm));
//	        else                  x[i] = 0;
//	    }
//	    
//	    return x;
//	}
	
	static SparseVector proc_N2(double sic, SparseVector Z )//, int time, FileWriter fw) throws IOException
	{
		SparseVector ret = Z.copy();// new SparseVector(Z.size());
//		double  rho = u ;//*100; //lambda;//*
		
		double norm = Z.norm();
		
//		System.out.println("Z norm " + norm + " - "+ lambda+" - "+u + " - "+ 1/rho);
		
	    for (int i = 0; i < Z.size(); i++) {
	    	
	        if (norm >= 1./sic)
        	{
//	        	if((ret.get(i)* (1. - 1./(rho*norm))< -10) || (ret.get(i)* (1. - 1./(rho*norm))>10))
//	        	{
////		        	System.out.println(time + "ret  " + ret.get(i) + "-"+rho+"-"+norm);
//		        	fw.write(time + " ret " +i+" :"+ ret.get(i) + " - "+rho+" - "+norm +"--"+(1. - 1./(rho*norm))+"---"+(ret.get(i)* (1. - 1./(rho*norm)))+"\n");
//	        	}
	        	ret.put(i, ret.get(i)* (1. - 1./(sic*norm)));// x[i] *= (1. - 1./(rho*norm));

        	}
	        else
        	{
//	        	System.out.println("ret  0 " + (1./rho));
	        	ret.put(i, 0);
        	}
	    }
	    
		return ret;
	};
	static SparseMatrix T_Set(int n, double rho)
	{
		return new SparseMatrix(n, n, 1).scale(-(Math.sqrt(1+ n*rho)-1)/n).plus(SparseMatrix.IMtx(n).scale(Math.sqrt(1+ n*rho)));
	}
	
	static SparseMatrix T_InvertSet(int n, double rho)
	{
		double scale1 = (Math.sqrt(1+ n*rho)-1)/n;
		double scale2 = 1/Math.sqrt(1+ n*rho);
//		System.out.println("T_InvertSet, scale 1:" + scale1 + "-" + scale2);
		SparseMatrix IMt =  SparseMatrix.IMtx(n);
		SparseMatrix _m11 =  new SparseMatrix(n, n, 1);
		SparseMatrix ret = _m11.scale(scale1).plus(IMt).scale(scale2);
		return ret;
	}
	
	/*
	 * r= Ax-Bz-c phần lồi tiến về 0
	 * s= rhoAtB(z1-z)
	 * A,B = 11
	 */

	
	static boolean[] KTT( SparseMatrix A, SparseMatrix x_Pre, NodeList z_Pre, NodeList u_Pre, NodeList z_Cur, double rho, double epsilonA, double epsilonR)
	{
		boolean ret [] = new boolean[x_Pre.getn()];
		for(int i =0; i < x_Pre.getn(); i++)
		{
			SparseVector x_pre = x_Pre.getRow(i);
			SparseVector z_pre = z_Pre.getV(i); //TODO
			SparseVector u_pre = u_Pre.getV(i);
			SparseVector z = z_Cur.getV(i);
			
			double r = x_pre.plus(z_pre.scale(-1)).norm();
	    	double s =  z.plus(z_pre.scale(-1.0)).scale(rho).norm();
	
//	    	z.print("Z");
//	    	z_pre.print("Z_P");
	    	
	    	double eP = epsilonA * Math.sqrt(A.getn()) + epsilonR*((x_pre.norm()>z_pre.norm())?x_pre.norm():z_pre.norm());
	    	double eD = epsilonA * Math.sqrt(A.getm()) + epsilonR*(u_pre.scale(rho).norm());
	
//	    	System.out.println(i+ " x.norm(): ("+ x_pre.norm() +")---z.norm(): ("+z_pre.norm()+")");
//	    	System.out.println(i+ " r: ("+ r +")---eP: ("+eP+")");
//	    	System.out.println(i+ " s: ("+ s +")---eD: ("+eD+")");
	    	
	    	if((r<= eP) && (s<=eD))
	    	{
//	    		System.out.println(i+ " r<= eP) && (s<=eD) ---- STOP LOOP");
	    		ret[i]= true;
	    	}
	    	if(s<=eD)
	    	{
//	    		System.out.println(i+ " s<=eD");
	    		ret[i]= true;
	    	}
	    	if(r<= eP)
	    	{
//	    		System.out.println(i+ " r<= eP");
	//    		ret[i]= true;
	    	}
	    	ret[i]= false;
		}
		return ret;
	}
	
	static NodeList solveV(SparseMatrix E, SparseMatrix X0, NodeList U, NodeList V0, double lambda1, double rho, boolean[] stop) {
//		NodeList ret = null;
		for(int i = 0; i < V0.getn(); i++)
		{
			for(int j = 0; j < V0.getn(); j++)
			{
				double  we = E.get(i, j);
				if(we > 0)
				{
//					System.out.println(V0.getn()+ " solveV "+i+"-"+j);
					SparseVector Ve= X0.getRow(i).plus(X0.getRow(j).scale(-1)).plus(U.getE(i,j).scale(-1));
//					Ve.toSpaceMatrix().print(true,"Ve");
//					U.getE(i,j).toSpaceMatrix().print(true, "Ue");
//					Ve = Ve.plus(U.getE(i,j).scale(-1));
					double sic = lambda1*we/rho;
					SparseVector proc = proc_N1(sic,Ve);
					
					V0.setE(i, j, proc);
				}
			}
		}
//		V0.EPrint("solve V");
		return V0;
	}
	/* 
	 * E include node(node is 2 doc related) and edge and weigh of edge
	 * e is index of relationship bw j and j', Ve = V.get(j,j') V is square matrix with size m*m
	 *	
	*/
	
	//////////////////////////////
	// Ue = Ue- + rho (Ve - Aj + Aj')
	
	static NodeList solveU( SparseMatrix E, SparseMatrix X0, NodeList U0, NodeList V, double rho, boolean[] stop) {
//		NodeList ret = null;
		for(int i = 0; i < U0.getn(); i++)
		{
			for(int j = 0; j < U0.getn(); j++)
			{
//				System.out.println("solveU "+i+"-"+j);
				if(E.get(i, j)>0)
					U0.setE(i, j, U0.getE(i, j).plus(V.getE(i, j).plus(X0.getRow(i).plus(X0.getRow(j).scale(-1)).scale(-1)).scale(rho)));
			}
		}
//		U0.EPrint("solve U");
		return U0;
	}
	
	
	static SparseVector Z_SetADMM(double rho, SparseVector X, SparseMatrix E, NodeList U, NodeList V, SparseMatrix T_InvertSet, int indexA)
	{
		SparseVector sum = new SparseVector(E.getn());//(X.size());
		/*
		 * Z = T^-1 ( Xj + rho sum_e((Uej+Vej)*(Ẹj - Ẹj,)))
		 */
		/*
		 * get X j, get list j'
		 */
//		System.out.println("size "+ X.size());
		for(int j = 0; j< E.getm(); j++)
		{
			//all e, get element j and j' related j
			if(E.get(indexA, j)> 0 )
			{
				double tmp = U.getE(indexA, j).get(indexA) + V.getE(indexA, j).get(indexA);
//					System.out.println("Z_set, sum of e: ("+indexA+"-"+ i+")" + tmp);
				sum = sum.plus(SparseVector.E(E.getn(), indexA).plus(SparseVector.E(E.getn(), j).scale(-1)).scale(tmp));
			}
		}
		
		sum = X.plus(sum.scale(rho));
//		sum.print("SUM");
		SparseMatrix ret = T_InvertSet.mulMatrix(sum.toSpaceMatrix().transpose());
//		ret .print(true, "Z_Set");
		return ret.getColumn(0);
	}
	
	//////////////////////////////
	// Ue = Ue- + rho (Ve - Aj + Aj')
	
	static NodeList solveU_Slaver( SparseMatrix E, SparseMatrix X0, NodeList U0, NodeList V, double rho, boolean[] stop) {
//		NodeList ret = null;
		for(int i = 0; i < U0.getn(); i++)
		{
			for(int j = 0; j < U0.getn(); j++)
			{
//				System.out.println("solveU "+i+"-"+j);
				if(E.get(i, j)>0)
					U0.setE(i, j, U0.getE(i, j).plus(V.getE(i, j).plus(X0.getRow(i).plus(X0.getRow(j).scale(-1)).scale(-1)).scale(rho)));
			}
		}
//		U0.EPrint("solve U");
		return U0;
	}
	
	
	static SparseVector Z_SetADMM_Slaver(double rho, SparseVector X, SparseMatrix E, NodeList U, NodeList V, SparseMatrix T_InvertSet, int indexA)
	{
		SparseVector sum = new SparseVector(E.getn());//(X.size());
		/*
		 * Z = T^-1 ( Xj + rho sum_e((Uej+Vej)*(Ẹj - Ẹj,)))
		 */
		/*
		 * get X j, get list j'
		 */
//		System.out.println("size "+ X.size());
		for(int j = 0; j< E.getm(); j++)
		{
			//all e, get element j and j' related j
			if(E.get(indexA, j)> 0 )
			{
				double tmp = U.getE(indexA, j).get(indexA) + V.getE(indexA, j).get(indexA);
//					System.out.println("Z_set, sum of e: ("+indexA+"-"+ i+")" + tmp);
				sum = sum.plus(SparseVector.E(E.getn(), indexA).plus(SparseVector.E(E.getn(), j).scale(-1)).scale(tmp));
			}
		}
		
		sum = X.plus(sum.scale(rho));
//		sum.print("SUM");
		SparseMatrix ret = T_InvertSet.mulMatrix(sum.toSpaceMatrix().transpose());
//		ret .print(true, "Z_Set");
		return ret.getColumn(0);
	}
		
	
	// each node i +  weigh function for all node in input. each weigh function of node with related node of that node
	// ex: calculate x for xi, x in m, should calculate m-1 time (for couble xi and (m-xi)) in set (m- xi) each node has k related node.
	
	// X = 1/2 (Z- TX) + lamb* u* X
	// Z = T^-1 (X*j) + rho * sigma_e((Ue+Ve)(Ej-Ej'))

	/*
	 * TODO: X is calculated from column of X pre, base on column
	 */
static SparseMatrix solveXADMM(SparseMatrix X0, SparseMatrix avgX, SparseMatrix A, SparseMatrix E, NodeList U, NodeList V, SparseVector u,  SparseMatrix T_InvertSet, SparseMatrix T_Set, double lambda2, double rho, boolean[] stop) {
		
		SparseMatrix X = new SparseMatrix(X0.getm(),X0.getn());
		SparseMatrix Y = new SparseMatrix(X0.getm(),X0.getn());
		
		/*
		 *  for i: 0->n
		 *  	yi= xi + sum_l1(v+ rho*u) - sum_l2(v+ rho*u) 
		 *  l = l1 and l2???
		 *  
		 *  A = (1/(1+n*rho)) + ((n*rho/)(1+n*rho))avg(X)
		 */
		
		/*
		 * sum l1 = i + sum l2 = i
		 */
		for(int i = 0; i < X0.getm(); i++)
		{			
			SparseVector row = X0.getRow(i);
			
			SparseVector l1 = new SparseVector(X0.getn());
			SparseVector l2 = new SparseVector(X0.getn());
				// get all node (i,*) and (*,i)
			for(int k = 0; k< E.getn(); k++)
			{
				if(E.get(i, k)>0)
				{
					l1 = l1.plus( U.getE(i, k).plus(V.getE(i, k).scale(rho)));
					l2 = l2.plus( U.getE(i,k).plus(V.getE(i,k).scale(rho)));
				}
			}	
//			rowl1.toSpaceMatrix().print(true,"Row1");
//			rowl2.toSpaceMatrix().print(true,"Row2");
			row = row.plus(l1.plus(l2.scale(-1)));
//			row.toSpaceMatrix().print(true,"Row");
//			row.print("Row");
			Y.setRow(i, row);
			
//			Y.print(true, "Y "+i);
		}
		
//		Y.scale(1/(1+X0.getm()*rho)).plus(avgX).print(true, "Y");
		
		
		X = Y.scale(1/(1+X0.getm()*rho)).plus(avgX);
//		X.print(true, "solve X");
		
		return X;
	}
	//////////////////////////////

	
	
	// 1 time, all node
	/*
	 * Update all X in A acordingly with Xi
	 * update all V in E
	 * update all U in E
	 */
	static SparseMatrix ADMM(SparseMatrix input, SparseMatrix E, SparseMatrix T_InvertSet, SparseMatrix T_Set, double lambda_1, double lambda_2, double rho, SparseMatrix A0, double epsilonA, double epsilonR, int loop,boolean fisrtProcess) {
		
		SparseMatrix X = new SparseMatrix(input.getm(),input.getn());
		NodeList U = new NodeList(input.getm(), input.getn());//,input.getm());
		NodeList V = new NodeList(input.getm(), input.getn());//,input.getm());
		
		SparseMatrix X0 = new SparseMatrix(input.getm(),input.getn());
//		SparseVector A2 = new SparseVector(input.getn());
		NodeList U0 = new NodeList(input.getm(), input.getn());//input.getm());
		NodeList V0 = new NodeList(input.getm(), input.getn());//,input.getm());
		
		boolean[] stop = new boolean[input.getn()];
		
		SparseMatrix avgX = new SparseMatrix(input.getm(), input.getn());
		
//		for(int i =0; i< input.getm(); i++)
		{
			SparseVector avr = new SparseVector(input.getm());
			for(int j =0; j< input.getn(); j++)
			{
				avr = avr.plus(input.getColumn(j));
//				avgX.put(i,j, input.getColumn(j).norm());
			}	
			avr = avr.scale(1/input.getm());
			for(int j =0; j< input.getn(); j++)
			{
//				System.out.println(j+" "+input.getn()+" "+input.getm());
//				System.out.println(j+" "+avgX.getn()+" "+avgX.getm());
//				avr = avr.plus(input.getColumn(j));
				avgX.setColumn(j, avr); //(i,j, input.getColumn(j).norm());
			}
		}
		
		SparseVector u = new SparseVector(A0.getm());
		
		for(int i = 0 ; i < A0.getm(); i++)
		{
			if(! fisrtProcess)
			{
				double a = 1/A0.getColumn(i).norm();
				System.out.println();
				u.put(i, a);
			}
			else
				u.put(i, 0);
		}
//		avgX.print(true, "avgX");
		
		int i = 0;
//		int indexE = 0;
		
		// stop condition
		while(i<loop)
		{
			i++;
//			System.out.println("Time "+i);
//			X.print(true, "solve X");
//			V.EPrint("solve V");
//			U.EPrint("solve U");
//			int jArray[] = getAllJIndex(E, indexE);
			X = solveXADMM(input, avgX, X, E, U, V, u, T_InvertSet, T_Set, lambda_2, rho, stop);	
			
//			System.out.println("U.getn "+U.getn());
//			System.out.println("V.getn "+V.getn());
			V = solveV(E, X, U, V0, lambda_1, rho, stop);
			
			U = solveU(E, X, U0, V, rho, stop);
			

			
			/*
			 * TODO
	    	 * Check stop condition
	    	 */
			stop = KTT(input, X0, V0, U0, V, rho, epsilonA, epsilonR);
			
			/*
	    	double r = X0.getRow(0).plus(U0.getE(0, 0).scale(-1)).norm();
	    	double s =  U0.getE(0, 0).plus(V0.getE(0, 0).scale(-1.0)).scale(rho).norm();

	    	
	    	double eP = (X0.getRow(0).norm()>U0.getE(0, 0).norm())?(epsilonA * Math.sqrt(input.getn()) + epsilonR*X0.getRow(0).norm()):(epsilonA * Math.sqrt(input.getn()) + epsilonR*U0.getE(0, 0).norm());
	    	double eD = epsilonA * Math.sqrt(input.getm()) + epsilonR*(X.scale(rho).getRow(0).norm());
	    	
	    	System.out.println("x.norm(): ("+ X0.getColumn(0).norm() +")---z.norm(): ("+U0.getE(0, 0).norm()+")");
	    	System.out.println("r: ("+ r +")---eP: ("+eP+")");
	    	System.out.println("s: ("+ s +")---eD: ("+eD+")");
	    	
	    	if(i>1)
	    	{
		    	if((r<= eP) && (s<=eD))
		    	{
		    		System.out.println("r<= eP) && (s<=eD) ---- STOP LOOP");
		//    		return x;
		    	}
		    	if(s<=eD)
		    	{
		    		System.out.println("s<=eD");
//		    		n++;
//		    		break;
		//    		return x;
		    	}
		    	if(r<= eP)
		    	{
		    		System.out.println("r<= eP");
		//    		return x;
		    	}
	    	}
	    	
	    	if(r>(s*10))
	    		rho = rho*2;
	    	else if(s>(r*10))
	    		rho = rho/2;
	    	else
	    		rho = rho;
	    	*/
			
			
			X0 = X.copy();
			V0 = V;
			U0=  U;
			
		}
		return X;
		
	}
	
	public static SparseMatrix mainADMMProcess(SparseMatrix input, SparseMatrix E_Set, double lambda1, double lambda2, double rho, double stop1, double stop2, int loop) {
		
		SparseMatrix A = new SparseMatrix(input.getm(), input.getn());
		SparseMatrix T_InvertSet = T_InvertSet(A.getm(), rho);
//		T_InvertSet.print(true, "T_InvertSet");
		SparseMatrix T_Set = T_Set(A.getm(), rho);
//		T_Set.print(true, "T_Set");
		/*
		 * 
		 */
		System.out.println("mainADMMProcess");
		
		SparseMatrix u1 = new SparseMatrix(input.getm(),input.getn());
		
//		int i = 0;
		// Process for all node, loop untill reach stop condition
//		while(i<A.getn())
		{
			System.out.println("mainADMMProcess 2");
			u1 = ADMM(input, E_Set, T_InvertSet, T_Set, lambda1, 0, rho, u1, stop1, stop2, loop, true);
//			i++;
		}
//		i = 0;
//		while(i<A.getn())
		{
			System.out.println("mainADMMProcess 3");
			A = ADMM(input, E_Set, T_InvertSet, T_Set, lambda1, lambda2, rho, u1, stop1, stop2, loop, false);
//			i++;
		}
		return A;
		
	}

	/*
	 * TODO
	 */
	static SparseVector Solve_ADMM_Slaver(JavaSparkContext sc, int w, int h,
			JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> A, 
			JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> E,
			SparseMatrix X0, 
			SparseMatrix avgX, 
			NodeList U, 
			NodeList V, 
			SparseVector u, 
			SparseMatrix T_InvertSet, 
			SparseMatrix T_Set,
			double lambda2, double rho, boolean[] stop, 
			String output) throws IOException {
		SparseVector ret = null;

		SparseMatrix A1 = util.toSparceMatrix3(A.collect(), h, w);
		SparseMatrix E1 = util.toSparceMatrix3(E.collect(), h, h);
		
		SparseVector x = new SparseVector(A1.getn());
		SparseVector x1 = new SparseVector(A1.getn());
		SparseVector x2 = new SparseVector(A1.getn());
		SparseVector x3 = new SparseVector(A1.getn());
		SparseVector x4 = new SparseVector(A1.getn());
		SparseVector z = new SparseVector(A1.getn());
//		SparseVector u = new SparseVector(A1.getn());
		SparseVector u1 = new SparseVector(A1.getn());
		SparseVector u2 = new SparseVector(A1.getn());
		SparseVector u3 = new SparseVector(A1.getn());
		SparseVector u4 = new SparseVector(A1.getn());
		
		return ret;
	}
	static SparseMatrix ClusteringADMM1Parallel(JavaSparkContext sc, int w, int h,
			JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> A,JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> E, double lambda, double lambda2, double rho, double epsilonA,
			double epsilonR, String output) throws IOException {
		SparseMatrix ret = null;

		SparseMatrix A1 = util.toSparceMatrix3(A.collect(), h, w);
		SparseMatrix E1 = util.toSparceMatrix3(E.collect(), h, h);
		
		SparseVector x = new SparseVector(A1.getn());
		SparseVector x1 = new SparseVector(A1.getn());
		SparseVector x2 = new SparseVector(A1.getn());
		SparseVector x3 = new SparseVector(A1.getn());
		SparseVector x4 = new SparseVector(A1.getn());
		SparseVector z = new SparseVector(A1.getn());
		SparseVector u = new SparseVector(A1.getn());
		SparseVector u1 = new SparseVector(A1.getn());
		SparseVector u2 = new SparseVector(A1.getn());
		SparseVector u3 = new SparseVector(A1.getn());
		SparseVector u4 = new SparseVector(A1.getn());
		
		int n = 1;
		
		z = A1.getRow(n);
		u = z.scale(0.5);
		
		
		int i = 0;
//		int indexE = 0;
		
		// stop condition
		while(i<50)
		{
//			Solve_ADMM_Slaver(sc, w, h, A, E, lambda, lambda2, rho, epsilonA, epsilonR, output);
//			solveU(A, E, X0, U0, V, rho, epsilonA);
//			solveV(A, E, X0, U, V0, lambda, rho, epsilonR);
		}
		
		return ret;
		
	}
	
	public static SparseMatrix mainADMMProcess(JavaSparkContext sc, int w, int h, SparseMatrix input,SparseMatrix E_Set, double lambda1, double lambda2, double rho, double stop1, double stop2) throws IOException {
		
		SparseMatrix A = null;//new SparseMatrix(input.getm(), input.getn());

//		input.print(true, "input");
//		E_Set.print(true, "eset");
//		SparseMatrix T_InvertSet = ADMM.T_InvertSet(A.getm(), rho);
//		T_InvertSet.print(true, "T_InvertSet");
//		SparseMatrix T_Set = ADMM.T_Set(A.getm(), rho);
//		T_Set.print(true, "T_Set");
		
		/*
		 * 
		 */
		JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> inputRDD = SparkMatrix.ToSparkMatrix(sc, input);
		JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> E_SetRDD = SparkMatrix.ToSparkMatrix(sc, E_Set);
		
		System.out.println("mainAMAProcess");
		
		SparseMatrix u1 = null;//new SparseMatrix(input.getm(),input.getn());
		
//		int i = 0;
		// Process for all node, loop untill reach stop condition
//		while(i<A.getn())
		{
			System.out.println("mainAMAProcess 2");
			SparseMatrix T_InvertSet = T_InvertSet(A.getm(), rho);
//			T_InvertSet.print(true, "T_InvertSet");
			SparseMatrix T_Set = T_Set(A.getm(), rho);
//			u1 = AMA(input, E_Set, lambda1, 0, rho, u1, stop1, stop2, true);
//			u1 = AMA(input, E_Set, lambda1, lambda2, rho, u1, stop1, stop2, true);
//			u1 = 
					ClusteringADMM1Parallel(sc, w, h, inputRDD, E_SetRDD, lambda1, lambda2, rho, stop1, stop2, "output");
			
//			i++;
		}
//		i = 0;
//		while(i<A.getn())
		{
			System.out.println("mainAMAProcess 3");
//			A = 
			ClusteringADMM1Parallel(sc, w, h, inputRDD, E_SetRDD, lambda1, lambda2, rho, stop1, stop2, "output");
//			i++;
		}
		return A;
		
//	}
//
//	public static SparseVector convexClusteringAMA2(JavaSparkContext sc, int w, int h,
//			JavaRDD<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> A1, double lambda, double rho, double epsilonA,
//			double epsilonR, String output) throws IOException {
//		SparseVector ret = null;
//
//		return ret;
//	}
}
//
//	public static SparseVector convexClusteringADMM2(JavaSparkContext sc, int w, int h,
//			JavaRDD<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> A1, double lambda, double rho, double epsilonA,
//			double epsilonR, String output) throws IOException {
//		SparseVector ret = null;
//
//		return ret;
//	}

	
	
	// each node i +  weigh function for all node in input. each weigh function of node with related node of that node
		// ex: calculate x for xi, x in m, should calculate m-1 time (for couble xi and (m-xi)) in set (m- xi) each node has k related node.
		
		// X = 1/2 (Z- TX) + lamb* u* X
		// Z = X*j + rho * sigma_e(Ue(Ej-Ej'))
		
		static SparseVector Z_SetAMA(double rho, SparseVector X, SparseMatrix E, NodeList U, int indexA, FileWriter fw) throws IOException
		{
			SparseVector sum = new SparseVector(X.size());
			/*
			 * Z =  Xj + rho sum_e((Uej)*(Ẹj - Ẹj,))
			 */
			/*
			 * get X j, get list j'
			 */
//			System.out.println(E.getm()+"--"+ E.getn());
			for(int j = 0; j< E.getn(); j++)
			{
				//all e, get element j and j' related j
				if(E.get(indexA, j)> 0 )
				{
//						System.out.println(" i, j"+ indexA + " - "+ j +"-"+ U.getE(indexA, j).size());
					double tmp = U.getE(indexA, j).get(indexA);
					
					//System.out.println(" i, j: "+ indexA + " - "+ j +" "+ tmp);
//					if(tmp >5)
//						fw.write("\n"+indexA+"-"+j+": "+tmp+"-"+sum.toString()+"\n");
					sum = sum.plus(SparseVector.E(E.getm(), indexA).plus(SparseVector.E(E.getm(), j).scale(-1)).scale(tmp));
				}
			}
			//fw.write("\n"+sum.toString()+"\n");
//			sum.print("sumE");
			sum = X.plus(sum.scale(rho));
//			sum.print("Z");
			return sum;
		}
		
		static SparseVector Z_SetAMA_Slaver(double rho, 
				SparseVector X, 
				SparseMatrix E, 
				NodeList U,
				int indexA, FileWriter fw) throws IOException
		{
			SparseVector sum = new SparseVector(X.size());
			/*
			 * Z =  Xj + rho sum_e((Uej)*(Ẹj - Ẹj,))
			 */
			/*
			 * get X j, get list j'
			 */
//			System.out.println(E.getm()+"--"+ E.getn());
			for(int j = 0; j< E.getn(); j++)
			{
				//all e, get element j and j' related j
				if(E.get(indexA, j)> 0 )
				{
//						System.out.println(" i, j"+ indexA + " - "+ j +"-"+ U.getE(indexA, j).size());
					double tmp = U.getE(indexA, j).get(indexA);
					
					//System.out.println(" i, j: "+ indexA + " - "+ j +" "+ tmp);
//					if(tmp >5)
//						fw.write("\n"+indexA+"-"+j+": "+tmp+"-"+sum.toString()+"\n");
					sum = sum.plus(SparseVector.E(E.getm(), indexA).plus(SparseVector.E(E.getm(), j).scale(-1)).scale(tmp));
				}
			}
			//fw.write("\n"+sum.toString()+"\n");
//			sum.print("sumE");
			sum = X.plus(sum.scale(rho));
//			sum.print("Z");
			return sum;
		}

		static SparseMatrix solveXAMA(SparseMatrix A, SparseMatrix E, SparseMatrix X0, NodeList U, SparseVector u1, double lambda2, double rho, boolean[] stop, FileWriter fw) throws IOException {
						
			SparseMatrix X = new SparseMatrix(X0.getm(),X0.getn());
			SparseMatrix Y = new SparseMatrix(X0.getm(),X0.getn());
			
			/*
			 *  for i: 0->n
			 *  	yi= xi + sum_l1(v+ rho*u) - sum_l2(v+ rho*u) 
			 *  l = l1 and l2???
			 *  
			 *  A = (1/(1+n*rho)) + ((n*rho/)(1+n*rho))avg(X)
			 */
/*			
			for(int i = 0; i < X0.getm(); i++)
			{
				
				SparseVector row = X0.getRow(i);
				
				SparseVector rowl1 = new SparseVector(X0.getn());
				SparseVector rowl2 = new SparseVector(X0.getn());
//				for(int j = 0; j< E.getn(); j++)
				{
					for(int k = 0; k< E.getn(); k++)
					{
						if(E.get(i, k)>0)
						{
							rowl1 = rowl1.plus( U.getE(i, k));
							rowl2 = rowl2.plus( U.getE(k, i));
						}
					}	
				}
//				rowl1.toSpaceMatrix().print(true,"Row1");
//				rowl2.toSpaceMatrix().print(true,"Row2");
//				row.toSpaceMatrix().print(true,"Row");
				row = row.plus(rowl1.plus(rowl2.scale(-1)));
//				row.toSpaceMatrix().print(true,"Row");
//				row.print("Row " +i);
				 Y.setRow(i, row);
				
//				Y.print(true, "Y "+i);
			}
//			Y.print(true, "Y ");
//
 */
			// from indexE, should know index j and j' to calc Aj, Aj', calc with column of matrix input
			for(int i = 0; i < X0.getn(); i++)
			{	
				SparseVector Z = Z_SetAMA(rho, A.getColumn(i), E, U, i,fw);

				double sic = u1.get(i) * lambda2;//*100; //
//				System.out.println(" sic: "+ u1.get(i));
//				fw.write(i+" Z\n"+Z.toString()+"\n");
				SparseVector procR = proc_N2(sic, Z );//, i, fw);
//				procR.print("line "+ i);
				X.setColumn(i, procR);//Row(i,Z);
			}
			
//			X = Y;
			return X;
		}
				
		
		// 1 time, all node
		/*
		 * Update all X in A acordingly with Xi
		 * update all V in E
		 * update all U in E
		 */
		static SparseMatrix AMA(SparseMatrix input, SparseMatrix E, double lambda_1, double lambda_2, double rho, SparseMatrix A0, double epsilonA, double epsilonR, boolean fisrtProcess, int loop, FileWriter fw) throws IOException {

			
			fw.write("======================A_Input matrix=========================\n");
			fw.write(input.toString());
			fw.write("======================related doc matrix=========================\n");
			fw.write(E.toString());
			fw.write("======================A0========================\n");
			fw.write(A0.toString());
			fw.write("===============================================\n");
			
			SparseMatrix X = new SparseMatrix(input.getm(),input.getn());
			NodeList U = new NodeList(input.getm(),input.getn());
			NodeList V = new NodeList(input.getm(),input.getn());
			
			SparseMatrix X0 = new SparseMatrix(input.getm(),input.getn());
//			SparseVector A2 = new SparseVector(input.getn());
			NodeList U0 = new NodeList(input.getm(),input.getn());
			NodeList V0 = new NodeList(input.getm(),input.getn());
			
			boolean[] stop = new boolean[input.getm()];
			
//			SparseMatrix avgX = new SparseMatrix(input.getm(),input.getn());
//			
//			for(int i =0; i< input.getm(); i++)
//			{
//				for(int j =0; j< input.getn(); j++)
//				{
//					avgX.put(i,j, input.getColumn(j).norm());
//				}	
//			}
			
//			avgX.print(true, "avgX");
			
			SparseVector u = new SparseVector(A0.getn());
			
			for(int i = 0 ; i < A0.getn(); i++)
			{
				if(! fisrtProcess)
				{
					double a = 1/A0.getColumn(i).norm();
//					System.out.println();
					u.put(i, a);
				}
				else
					u.put(i, 0);
			}
			
			
			int i = 0;
//			int indexE = 0;
			
			// stop condition
			while(i<loop)
			{
				i++;
//				System.out.println("Time "+i);
//				u.print(true, "U");
//				V.EPrint("V");
//				U.EPrint("U");
				//X.print(true, "solve X");
				fw.write("\nTime "+i+"=========================================================================\n");
				fw.write(X.toString()+"\n");
				
//				fw.write("\nV "+i+"=========================================================================\n");
//				fw.write(V.toString()+"\n");
				
//				fw.write("\nU "+i+"=========================================================================\n");
//				fw.write(U.toString()+"\n");
//				int jArray[] = getAllJIndex(E, indexE);
				X = solveXAMA(input, E, X, U, u, lambda_2, rho, stop,fw);	

				V = solveV(E, X, U, V0, lambda_1, rho, stop);
				
				U = solveU(E, X, U0, V, rho, stop);
				

				
				/*
				 * TODO
		    	 * Check stop condition
		    	 */
				
				stop = KTT(input, X0, V0, U0, V, rho, epsilonA, epsilonR);
				
				/*
				SparseVector R = new SparseVector(X0.getm());//, X0.getm());
				SparseVector S = new SparseVector(X0.getm());//, X0.getm());
				SparseVector EP = new SparseVector(X0.getm());//, X0.getm());
				SparseVector ED = new SparseVector(X0.getm());//, X0.getm());

				double r = 0;
				double s = 0;
				double eP = 0;
				double eD = 0;		
				for(int k = 0; k< X0.getm(); k++)
				{
					SparseVector aU = new SparseVector(X0.getn());
					SparseVector aV = new SparseVector(X0.getn());
					for(int l = 0; l< X0.getm(); l++)
					{
						if(E.get(k, l)>0)
						{
							aU = aU.plus(U0.getE(k, l));
							aV = aV.plus(V0.getE(k, l));
							
//							r = X0.getRow(k).plus(U0.getE(k, l).scale(-1)).norm();
//					    	s = U0.getE(k, l).plus(V0.getE(k, l).scale(-1.0)).scale(rho).norm();
					    	
//					    	eP = (X0.getRow(k).norm()>U0.getE(k, l).norm())?(epsilonA * Math.sqrt(input.getn()) + epsilonR*X0.getRow(k).norm()):(epsilonA * Math.sqrt(input.getn()) + epsilonR*U0.getE(k, l).norm());
//					    	eD = epsilonA * Math.sqrt(input.getm()) + epsilonR*(X.scale(rho).getRow(k).norm());    	
						}
					}
					aU = aU.scale(X0.getm());
					aV = aV.scale(X0.getm());
					
					r = X0.getRow(k).plus(aU.scale(-1)).norm();
			    	s = aU.plus(aV.scale(-1.0)).scale(rho).norm();
			    	
//			    	eP = (X0.getRow(k).norm()>aU.norm())?(epsilonA * Math.sqrt(input.getn()) + epsilonR*X0.getRow(k).norm()):(epsilonA * Math.sqrt(input.getn()) + epsilonR*aU.norm());
			    	
			    	eP = epsilonA * Math.sqrt(input.getn()) + epsilonR* ((X0.getRow(k).norm()>aU.norm())? X0.getRow(k).norm(): aU.norm());
			    	eD = epsilonA * Math.sqrt(input.getm()) + epsilonR* (X.scale(rho).getRow(k).norm());
			    	
			    	R.put(k, r);
			    	S.put(k, s);
			    	EP.put(k, eP);
			    	ED.put(k, eD);
//			    	System.out.println("x.norm(): ("+ X0.getRow(k).norm() +")---z.norm(): ("+U0.getE(k, l).norm()+")");
//			    	System.out.println(k);
			    	
//			    	System.out.println(k+" r: ("+ r +")---eP: ("+eP+") # s: ("+ s +")---eD: ("+eD+")");

				}
				
		    	r = R.norm();
		    	s =  S.norm();

		    	eP = EP.norm();
		    	eD = ED.norm();
				
//		    	r = X0.getRow(0).plus(U0.getE(0, 0).scale(-1)).norm();
//		    	s =  U0.getE(0, 0).plus(V0.getE(0, 0).scale(-1.0)).scale(rho).norm();

		    	
//		    	eP = (X0.getRow(0).norm()>U0.getE(0, 0).norm())?(epsilonA * Math.sqrt(input.getn()) + epsilonR*X0.getRow(0).norm()):(epsilonA * Math.sqrt(input.getn()) + epsilonR*U0.getE(0, 0).norm());
//		    	eD = epsilonA * Math.sqrt(input.getm()) + epsilonR*(X.scale(rho).getRow(0).norm());
				
//				R.print(false, "R");
//				EP.print(false, "EP");
//				S.print(false, "S");
//				ED.print(false, "ED");
				
		    	System.out.println("x.norm(): ("+ X0.getColumn(0).norm() +")---z.norm(): ("+U0.getE(0, 0).norm()+")");
		    	System.out.println("r: ("+ r +")---eP: ("+eP+") ### s: ("+ s +")---eD: ("+eD+")");
		    	
		    	if(i>1)
		    	{
			    	if((r<= eP) && (s<=eD))
			    	{
			    		System.out.println("r<= eP) && (s<=eD) ---- STOP LOOP");
			//    		return x;
			    	}
			    	if(s<=eD)
			    	{
			    		System.out.println("s<=eD");
//			    		n++;
//			    		break;
			//    		return x;
			    	}
			    	if(r<= eP)
			    	{
			    		System.out.println("r<= eP");
			//    		return x;
			    	}
		    	}
		    	
		    	if(r>(s*10))
		    		rho = rho*2;
		    	else if(s>(r*10))
		    		rho = rho/2;
		    	else
		    		rho = rho;
				*/
				
				
				X0 = X.copy();
				V0 = V;
				U0 = U;
				
			}
			return X;
			
		}
		
		/*
		 * input: quy tâm theo côt, sum_i( x(ij))) = 0
		 */
		public static SparseMatrix mainAMAProcess(SparseMatrix input, SparseMatrix E_Set, double lambda1, double lambda2, double rho, double stop1, double stop2, int loop, FileWriter fw) throws IOException {
			
			SparseMatrix A = new SparseMatrix(input.getm(), input.getn());

//			input.print(true, "input");
//			E_Set.print(true, "eset");
//			SparseMatrix T_InvertSet = ADMM.T_InvertSet(A.getm(), rho);
//			T_InvertSet.print(true, "T_InvertSet");
//			SparseMatrix T_Set = ADMM.T_Set(A.getm(), rho);
//			T_Set.print(true, "T_Set");
			
			/*
			 * 
			 */
			
//			System.out.println("mainAMAProcess");
			
			SparseMatrix u1 = new SparseMatrix(input.getm(),input.getn());
			
//			int i = 0;
			// Process for all node, loop untill reach stop condition
//			while(i<A.getn())
			{
//				System.out.println("mainAMAProcess 2");
				SparseMatrix T_InvertSet = T_InvertSet(A.getm(), rho);
//				T_InvertSet.print(true, "T_InvertSet");
				SparseMatrix T_Set = T_Set(A.getm(), rho);
//				u1 = AMA(input, E_Set, lambda1, 0, rho, u1, stop1, stop2, true);
//				u1 = AMA(input, E_Set, lambda1, lambda2, rho, u1, stop1, stop2, true);
				u1 = ADMM(input, E_Set, T_InvertSet, T_Set, lambda1+1, 0, rho, u1, stop1, stop2, loop,true);
				
//				i++;
			}
//			i = 0;
//			while(i<A.getn())
			{
				System.out.println("mainAMAProcess 3");
				A = AMA(input, E_Set, lambda1, lambda2, rho, u1, stop1, stop2, false, loop,fw);
//				i++;
			}
			return A;
			
		}

		
		/*
		 * TODO
		 * input: 
		 * - SparseMatrix input, 
		 * - SparseMatrix E_Set,
		 * - double lambda1,
		 * - double lambda2,
		 * - double rho,
		 * - double stop1,
		 * - double stop2
		 */

//		static SparseMatrix solveXAMA(SparseMatrix A, SparseMatrix E, SparseMatrix X0, NodeList U, SparseVector u1, double lambda2, double rho, boolean[] stop, FileWriter fw) throws IOException {
		static SparseMatrix SolveX_AMA_Slaver(JavaSparkContext sc, int w, int h,
				JavaRDD<Tuple2< String,Tuple2<Tuple2<Integer, Integer>, Double>>> A, 
				JavaRDD<Tuple2< String,Tuple2<Tuple2<Integer, Integer>, Double>>> E, 
				JavaRDD<Tuple2< String,Tuple2<Tuple2<Integer, Integer>, Double>>> X0, 
				JavaRDD<Tuple2< String,Tuple2<Tuple2<Integer, Integer>, Iterable<Double>>>> U, //NodeList  
				SparseVector u1, 
				double lambda2, double rho, boolean[] stop, String output) throws IOException {

			SparseMatrix X = new SparseMatrix(h,w);
			FileWriter fw;
			// from indexE, should know index j and j' to calc Aj, Aj', calc with column of matrix input
			for(int i = 0; i < w; i++)
			{	
				SparseVector Z = null;//Z_SetAMA(rho, A.getColumn(i), E, U, i,fw);

				double sic = u1.get(i) * lambda2;//*100; //
//				System.out.println(" sic: "+ u1.get(i));
//				fw.write(i+" Z\n"+Z.toString()+"\n");
				SparseVector procR = proc_N2(sic, Z );//, i, fw);
//				procR.print("line "+ i);
				X.setColumn(i, procR);//Row(i,Z);
			}
			
//			X = Y;
			return X;
			
		}
		
		
		static SparseMatrix ClusteringAMA1Parallel(JavaSparkContext sc, int w, int h,
				JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> A,JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> E, double lambda, double lambda2, double rho, double epsilonA,
				double epsilonR, String output) throws IOException {
			SparseMatrix ret = null;

			int part = 4;
			
			NodeList U = null;
			NodeList[] U_cluster = new NodeList[part];
			NodeList V = null;
			NodeList[] V_cluster = new NodeList[part];
			
			
			
			SparseMatrix A1 = util.toSparceMatrix3(A.collect(), h, w);
			SparseMatrix E1 = util.toSparceMatrix3(E.collect(), h, h);
			
			SparseVector x = new SparseVector(A1.getn());
			SparseVector x1 = new SparseVector(A1.getn());
			SparseVector x2 = new SparseVector(A1.getn());
			SparseVector x3 = new SparseVector(A1.getn());
			SparseVector x4 = new SparseVector(A1.getn());
			SparseVector z = new SparseVector(A1.getn());
			SparseVector u = new SparseVector(A1.getn());
			SparseVector u1 = new SparseVector(A1.getn());
			SparseVector u2 = new SparseVector(A1.getn());
			SparseVector u3 = new SparseVector(A1.getn());
			SparseVector u4 = new SparseVector(A1.getn());
			
			int n = 1;
			
			z = A1.getRow(n);
			u = z.scale(0.5);
			
			
			int i = 0;
//			int indexE = 0;
			
			// stop condition
			while(i<50)
			{
//				SolveX_AMA_Slaver(sc, w, h, A, E, lambda, lambda2, rho, epsilonA, epsilonR, output);
//				solveU(A, E, X0, U0, V, rho, epsilonA);
//				solveV(A, E, X0, U, V0, lambda, rho, epsilonR);
			}
			
			return ret;
		}
		/*
		 * TODO:
		 * split matrix to n part -> after init-> centroied matrix.
		 * Init data
		 * loop
		 * - map n part data(include X,U,Z, note that U,Z in here are node list), each part call X. (slaver)
		 * - reduce X (master)
		 * - cal U,Z (master)
		 * - check KTT (master)
		 */
		
public static SparseMatrix mainAMAProcess(JavaSparkContext sc, int w, int h, SparseMatrix input, SparseMatrix E_Set, double lambda1, double lambda2, double rho, double stop1, double stop2) throws IOException {
			
			SparseMatrix A = null;//new SparseMatrix(input.getm(), input.getn());

//			input.print(true, "input");
//			E_Set.print(true, "eset");
//			SparseMatrix T_InvertSet = ADMM.T_InvertSet(A.getm(), rho);
//			T_InvertSet.print(true, "T_InvertSet");
//			SparseMatrix T_Set = ADMM.T_Set(A.getm(), rho);
//			T_Set.print(true, "T_Set");
			
			/*
			 * 
			 */
			
			JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> inputRDD = SparkMatrix.ToSparkMatrix(sc, input);
			JavaRDD<Tuple2<Tuple2<Integer, Integer>, Double>> E_SetRDD = SparkMatrix.ToSparkMatrix(sc, E_Set);
			System.out.println("mainAMAProcess");
			
			
			inputRDD.flatMapToPair(new PairFlatMapFunction<Tuple2<Tuple2<Integer,Integer>,Double>, Integer, Tuple2<Tuple2<Integer,Integer>,Double>>() {

				@Override
				public Iterable<Tuple2<Integer, Tuple2<Tuple2<Integer,Integer>,Double>>> call(Tuple2<Tuple2<Integer, Integer>, Double> t) throws Exception {
					// TODO Auto-generated method stub
					return null;
				}
			});
			
			SparseMatrix u1 = null;//new SparseMatrix(input.getm(),input.getn());
			
//			int i = 0;
			// Process for all node, loop untill reach stop condition
//			while(i<A.getn())
			{
				System.out.println("mainAMAProcess 2");
				SparseMatrix T_InvertSet = T_InvertSet(A.getm(), rho);
//				T_InvertSet.print(true, "T_InvertSet");
				SparseMatrix T_Set = T_Set(A.getm(), rho);
//				u1 = AMA(input, E_Set, lambda1, 0, rho, u1, stop1, stop2, true);
//				u1 = AMA(input, E_Set, lambda1, lambda2, rho, u1, stop1, stop2, true);
//				u1 = 
						ClusteringADMM1Parallel(sc, w, h, inputRDD, E_SetRDD, lambda1, lambda2, rho, stop1, stop2, "output");
				
//				i++;
			}
//			i = 0;
//			while(i<A.getn())
			{
				System.out.println("mainAMAProcess 3");
//				A = 
						ClusteringAMA1Parallel(sc, w, h, inputRDD, E_SetRDD, lambda1, lambda2, rho, stop1, stop2, "output");
//				i++;
			}
			return A;
			
		}
//
//		public static SparseVector convexClusteringAMA2(JavaSparkContext sc, int w, int h,
//				JavaRDD<Tuple2<Integer, Iterable<Tuple2<Integer, Double>>>> A1, double lambda, double rho, double epsilonA,
//				double epsilonR, String output) throws IOException {
//			SparseVector ret = null;
//
//			return ret;
//		}
}


