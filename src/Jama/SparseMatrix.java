package Jama;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import Jama.util.Maths;
import scala.Tuple2;

public class SparseMatrix implements Cloneable, java.io.Serializable {

    private int m;           // n-by-n matrix final
    private int n;
    private SparseVector[] rows;   // the rows, each row is a sparse vector

    public int getm()
    {
    	return m;
    }
    public int getn()
    {
    	return n;
    }
    public SparseVector[] getRows()
    {
    	return rows;
    }
    // initialize an n-by-n matrix of all 0s
    public SparseMatrix(int m, int n) {
        this.m = m;
        this.n = n;
        rows = new SparseVector[m];
        for (int i = 0; i < m; i++) {
            rows[i] = new SparseVector(n);
        }
    }
    
    public SparseMatrix(int m, int n, double value) {
        this.m = m;
        this.n = n;
        rows = new SparseVector[m];
        for (int i = 0; i < m; i++) {
        	rows[i] = new SparseVector(n);
        	for (int j = 0; j < n; j++) {
        		rows[i].put(j, value);
        	}
//            rows[i] = new SparseVector(n);
        }
    }
    // init sparce matrix from denso

    public SparseMatrix(Matrix X) {
        this.m = X.getRowDimension();
        this.n = X.getColumnDimension();
        rows = new SparseVector[m];
        for (int i = 0; i < m; i++) {
            double rowN[] = X.getRow(i).toArrayDouble();
            rows[i] = new SparseVector(rowN);
        }
//        rows = new SparseVector[n];
//        for (int i = 0; i < n; i++)
//            rows[i] = new SparseVector(n);
    }

    public SparseMatrix(double[] X, int m, int n) {
        this.m = m;
        this.n = n;
        
//        rows = X;
        
        for(int i = 0; i < m; i++)
        {
        	for(int j = 0; j < n; j++)
        	{
        		rows[i].put(j, X[i*j]);
        	}
//        	X[i].print("fbagdf");
//        	rows[i] = new SparseVector(X[i].toArrayDouble());
        }
        
//        for (int i = 0; i < m; i++) {
//            double rowN[] = X.getRow(i).toArrayDouble();
//            rows[i] = new SparseVector(rowN);
//        }
//        rows = new SparseVector[n];
//        for (int i = 0; i < n; i++)
//            rows[i] = new SparseVector(n);
    }
    
    public SparseMatrix(SparseVector X, int m) {
        this.m = m;
        this.n = 1;
        
        rows[0] = X;
        
//        for(int i = 0; i < m; i++)
        {
//        	X[i].print("fbagdf");
//        	rows[i] = new SparseVector(X[i].toArrayDouble());
        }
        
//        for (int i = 0; i < m; i++) {
//            double rowN[] = X.getRow(i).toArrayDouble();
//            rows[i] = new SparseVector(rowN);
//        }
//        rows = new SparseVector[n];
//        for (int i = 0; i < n; i++)
//            rows[i] = new SparseVector(n);
    }
    
    public SparseMatrix(SparseVector[] X, int m, int n) {
        this.m = m;
        this.n = n;
        
        rows = X;
        
//        for(int i = 0; i < m; i++)
        {
//        	X[i].print("fbagdf");
//        	rows[i] = new SparseVector(X[i].toArrayDouble());
        }
        
//        for (int i = 0; i < m; i++) {
//            double rowN[] = X.getRow(i).toArrayDouble();
//            rows[i] = new SparseVector(rowN);
//        }
//        rows = new SparseVector[n];
//        for (int i = 0; i < n; i++)
//            rows[i] = new SparseVector(n);
    }
    
    public SparseMatrix(List<List<Double>> input) {
        this.m = input.size();
        this.n = input.get(0).size();
        rows = new SparseVector[m];
        for (int i = 0; i < m; i++) {
			Double[] arr = new Double[n];
            input.get(i).toArray(arr);
            double rowN[] = ArrayUtils.toPrimitive(arr);
            rows[i] = new SparseVector(rowN);
        }
    }
    
    
    public SparseMatrix(List<SparseVector> input, int tmp) {
        this.m = input.size();
        this.n = input.get(0).size();
        rows = new SparseVector[m];
        for (int i = 0; i < m; i++) {
            rows[i] = input.get(i);
        }
    }
    
    /**
     * Get row dimension.
     *
     * @return m, the number of rows.
     */
    public int getRowDimension() {
        return m;
    }

    /**
     * Get column dimension.
     *
     * @return n, the number of columns.
     */
    public int getColumnDimension() {
        return n;
    }

    // put A[i][j] = value
    public void put(int i, int j, double value) {
        if (i < 0 || i >= m) {
            throw new RuntimeException("Illegal index");
        }
        if (j < 0 || j >= n) {
            throw new RuntimeException("Illegal index");
        }
        rows[i].put(j, value);
    }

    // return A[i][j]
    public double get(int i, int j) {
        if (i < 0 || i >= m) {
            throw new RuntimeException("Illegal index i < 0 || i >= m");
        }
        if (j < 0 || j >= n) {
            throw new RuntimeException("Illegal index j < 0 || j >= n");
        }
        return rows[i].get(j);
    }

    public Matrix toDensoMatrix() {
        Matrix ret = new Matrix(m, n);
        for(int i = 0; i<m; i++)
            ret.setRow(i, this.getRow(i).toArrayDouble());
        return ret;
    }

    // return the number of nonzero entries (not the most efficient implementation)
    public int nnz() {
        int sum = 0;
        for (int i = 0; i < m; i++) {
            sum += rows[i].nnz();
        }
        return sum;
    }

    // return the matrix-vector product b = Ax
    public SparseVector times(SparseVector x) {
        if (m != x.size()) {
            throw new IllegalArgumentException("Dimensions disagree");
        }
        SparseVector b = new SparseVector(m);
        for (int i = 0; i < m; i++) {
            b.put(i, this.rows[i].dot(x));
        }
        return b;
    }

    // return the matrix-vector product b = Ax
    public SparseMatrix scale(double x) {
    	SparseMatrix b = this.copy();
        for (int i = 0; i < m; i++) {
//        	b.rows[i].print("row "+i);
        	b.rows[i] = b.rows[i].scale(x);
//            b.rows[i].print("rowA "+i);
        }
//        System.out.println("scale "+ Long.toString(System.currentTimeMillis()));
        return b;
    }
    // return this + that
    public SparseMatrix plus(SparseMatrix that) {
//    	System.out.println("plus SparseMatrix "+ this.n +"-"+ this.m + " "+ this.n +"-"+ this.m);
        if (this.m != that.m) {
            throw new IllegalArgumentException("Dimensions disagree m "+ this.m +"-"+ that.m);
        }
        if (this.n != that.n) {
            throw new IllegalArgumentException("Dimensions disagree n "+ this.n +"-"+ that.n);
        }
        SparseMatrix result = new SparseMatrix(m, n);
//        System.out.println("plus");
        for (int i = 0; i < m; i++) {
            result.rows[i] = this.rows[i].plus(that.rows[i]);
        }
//        System.out.println("PLUS "+ Long.toString(System.currentTimeMillis()));
        return result;
    }

    public SparseMatrix mulMatrix(SparseMatrix that) {
//        System.out.println(that.m + " that n--" + that.n);
//        System.out.println(m + " this n--" + n);
//        if (this.m != that.n) throw new IllegalArgumentException("Dimensions disagree");
//    	System.out.println("START MUL");
        if (this.n != that.m) {
            throw new IllegalArgumentException("Dimensions disagree n: "+this.n +" m: " +that.m);
        }

        SparseMatrix result = new SparseMatrix(this.m, that.n);
        for (int j = 0; j < this.m; j++) {
            for (int i = 0; i < that.n; i++) {
//                System.out.println("row " + j + " col " + i);
//            	this.rows[j].print("this.rows "+ j);
//            	that.getColumn(i).print("that.getColumn "+i);
//            	System.out.println(i+"-"+j+ ": "+this.rows[j].dot(that.getColumn(i)));
                result.rows[j].put(i, this.rows[j].dot(that.getColumn(i)));
//                result.rows[j].print("row "+j);
            }
        }
        
//        System.out.println("END MUL");
        return result;
    }
    
    // return this + that
    public static SparseMatrix IMtx(int size) {        
        SparseMatrix result = new SparseMatrix(size, size);
        for (int i = 0; i < size; i++) {
        	result.rows[i].put(i, 1.0);
        }
        return result;
    }

    public static SparseMatrix InitMtx(int size) {        
        SparseMatrix result = new SparseMatrix(size, size);
        for (int i = 0; i < size; i++) {
        	result.rows[i].put(i, 1.0);
        }
        return result;
    }
    
    public SparseVector getRow(int i) {
        return rows[i];
    }

    public void setRow(int i, SparseVector input) {
//        SparseMatrix X = this.copy();
        this.rows[i] = input;
//        X.rows[i].print("X");
//        X.print(true, "Xm");
//        return X;
    }

    public SparseVector getColumn(int i) {
        SparseVector col = new SparseVector(m);
        for(int j = 0; j<m; j++)
            col.put(j, this.get(j,i));
        return col;
    }

    public void setColumn(int ind, SparseVector col) {
//        SparseVector col = new SparseVector(m);

        int j = 0;
        for (SparseVector row : rows) {
            row.put(ind, col.get(j));
//            col.put(j, co.get(i));
            j++;
        }
//        return col;
    }

    public double[] getArrayData()
    {
    	double[] ret = new double[m*n];
    	for(int i = 0; i< m; i++)
    	{
    		for(int j = 0; j< n; j++)
    		{
    			ret[i*j] = rows[i].get(j);
    		}
    	}
    	return ret;
    }
    
    // return a string representation
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("n = " + m + ", nonzeros = " + nnz() + "\n");
        for (int i = 0; i < m; i++) {
        	if(rows[i].nnz()>0)
        	{
        		s.append(i + ": \t");
        		for(int j = 0; j< n; j++)
        			s.append(j + ":" + rows[i].get(j)+"\t");
        		s.append("\n");
        	}
        }
        return s.toString();
    }

    public void print(boolean full,String mes) {
        
        if(full)
        {
        	System.out.println(this.m+"-"+this.n);
            this.toDensoMatrix().print(true, mes);
        }
        else
        {
        	System.out.println(mes);
            System.out.println(this.toString());
            
        }
    }

    public void save(String fileName) throws IOException {
        FileWriter fw = new FileWriter(fileName);
        fw.write(this.toString());
        fw.close();
    }

    /**
     * Clone the Matrix object.
     */
    public Object clone() {
        return this.copy();
    }

    public SparseMatrix copy() {
        SparseMatrix X = new SparseMatrix(m, n);
        for (int i = 0; i < m; i++) {
            X.rows[i] = this.rows[i];
        }
        return X;
    }

    public SparseMatrix transpose() {
//    	System.out.println("START TRAN");
    	SparseMatrix X = new SparseMatrix(n, m);
        for (int i = 0; i < n; i++) {
            X.rows[i] = this.getColumn(i);
        }
//        System.out.println("END TRAN");
        return X;
    }

    public double detA()
    {
    	double ret = 0.0;
    	if(m!=n)
    		throw new ArrayIndexOutOfBoundsException("rectangle matrix");
/*
    	if (m==2) 
    	{ 
    		return (matrix.getValueAt(0, 0) * matrix.getValueAt(1, 1)) - ( matrix.getValueAt(0, 1) * matrix.getValueAt(1, 0));
    	} 
    	double sum = 0.0; 
    	for (int i=0; i<n; i++) 
    	{ 
    		sum += changeSign(i) * matrix.getValueAt(0, i) * determinant(createSubMatrix(matrix, 0, i)); 
    	}
*/
    	
    	return ret;
    }
    /*
    public SparseMatrix inverse() {
        SparseMatrix X = new SparseMatrix(n, m);
        for (int i = 0; i < n; i++) {
            X.rows[i] = this.getColumn(i);
        }
        return X;
    }
    

    public double[][] invert(double a[][]) 
    {
        int n = a.length;
        double x[][] = new double[n][n];
        double b[][] = new double[n][n];
        int index[] = new int[n];
        for (int i=0; i<n; ++i) 
            b[i][i] = 1;
 
 // Transform the matrix into an upper triangle
        gaussian(a, index);
 
 // Update the matrix b[i][j] with the ratios stored
        for (int i=0; i<n-1; ++i)
            for (int j=i+1; j<n; ++j)
                for (int k=0; k<n; ++k)
                    b[index[j]][k]
                    	    -= a[index[j]][i]*b[index[i]][k];
 
 // Perform backward substitutions
        for (int i=0; i<n; ++i) 
        {
            x[n-1][i] = b[index[n-1]][i]/a[index[n-1]][n-1];
            for (int j=n-2; j>=0; --j) 
            {
                x[j][i] = b[index[j]][i];
                for (int k=j+1; k<n; ++k) 
                {
                    x[j][i] -= a[index[j]][k]*x[k][i];
                }
                x[j][i] /= a[index[j]][j];
            }
        }
        return x;
    }
 
// Method to carry out the partial-pivoting Gaussian
// elimination.  Here index[] stores pivoting order.
 
    public void gaussian(double a[][], int index[]) 
    {
        int n = index.length;
        double c[] = new double[n];
 
 // Initialize the index
        for (int i=0; i<n; ++i) 
            index[i] = i;
 
 // Find the rescaling factors, one from each row
        for (int i=0; i<n; ++i) 
        {
            double c1 = 0;
            for (int j=0; j<n; ++j) 
            {
                double c0 = Math.abs(a[i][j]);
                if (c0 > c1) c1 = c0;
            }
            c[i] = c1;
        }
 
 // Search the pivoting element from each column
        int k = 0;
        for (int j=0; j<n-1; ++j) 
        {
            double pi1 = 0;
            for (int i=j; i<n; ++i) 
            {
                double pi0 = Math.abs(a[index[i]][j]);
                pi0 /= c[index[i]];
                if (pi0 > pi1) 
                {
                    pi1 = pi0;
                    k = i;
                }
            }
 
   // Interchange rows according to the pivoting order
            int itmp = index[j];
            index[j] = index[k];
            index[k] = itmp;
            for (int i=j+1; i<n; ++i) 	
            {
                double pj = a[index[i]][j]/a[index[j]][j];
 
 // Record pivoting ratios below the diagonal
                a[index[i]][j] = pj;
 
 // Modify other elements accordingly
                for (int l=j+1; l<n; ++l)
                    a[index[i]][l] -= pj*a[index[j]][l];
            }
        }
    }
    */
    
    public void gaussian(SparseMatrix a, int index[]) 
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
    
    
    public SparseMatrix invert() 
    {
//    	System.out.println("invert");
    	SparseMatrix a = this.copy();
    	if(m!= n)
    		 throw new ArrayIndexOutOfBoundsException(" invert2: m !=n");
        int n = a.m;
        SparseMatrix x = new SparseMatrix (n,n);
        double b[][] = new double[n][n];
        int index[] = new int[n];
        for (int i=0; i<n; ++i) 
            b[i][i] = 1;
 
 // Transform the matrix into an upper triangle
//        System.out.println("START gaussian "+Long.toString(System.currentTimeMillis()));
        gaussian(a, index);
//        System.out.println("END gaussian " +Long.toString(System.currentTimeMillis()));
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
//        System.out.println("END Invert "+Long.toString(System.currentTimeMillis()));
        return x;
    }
    

//    public SparseMatrix matrixRemoveRow(int index) {
//    	SparseMatrix X = this.copy();
//    	SparseMatrix X2 = new SparseMatrix(m-1, n);
////        System.out.println("matrixRemoveRow " + index);
//
////        try {
////            int k = 0;
////            for (int i = 0; i < getRowDimension() - 1; i++) {
////                if (k == index) {
////                    k++;
////                }
////                for (int j = 0; j <= getColumnDimension() - 1; j++) {
////                    X2.set(i, j, X.get(k, j));
////                }
////                k++;
////            }
////        } catch (ArrayIndexOutOfBoundsException e) {
////            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
////        }
//
////        X2.print(true);
//        return X2;
//    }
//
//    /*
//	 * TODO
//     */
//    public SparseMatrix matrixRemoveColumn(int index) {
//    	SparseMatrix X = this.copy();
//        // X.print(true);
////        System.out.println("matrixRemoveColumn " + index);
//    	SparseMatrix X2 = new SparseMatrix(m, n-1);
//
////        try {
////            for (int i = 0; i <= getRowDimension() - 1; i++) {
////                int k = 0;
////                for (int j = 0; j < getColumnDimension() - 1; j++) {
////                    if (k == index) {
////                        k++;
////                    }
////                    X2.set(i, j, X.get(i, k));
////                    k++;
////                }
////            }
////        } catch (ArrayIndexOutOfBoundsException e) {
////            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
////        }
//
////        X2.print(true);
//        return X2;
//    }
    /**
     * Get a submatrix.
     *
     * @param i0 Initial row index
     * @param i1 Final row index
     * @param j0 Initial column index
     * @param j1 Final column index
     * @return A(i0:i1,j0:j1)
     * @exception ArrayIndexOutOfBoundsException Submatrix indices
     */
    public SparseMatrix getMatrix(int ri0, int ri1, int cj0, int cj1) {
        SparseMatrix X = new SparseMatrix(ri1 - ri0 + 1, cj1 - cj0 + 1);
        for (int i = ri0; i <= ri1; i++) {
            for (int j = cj0; j <= cj1; j++) {
                X.put(i - ri0, j - cj0, this.get(i, j));
            }
        }

        return X;
    }

//    /**
//     * Get a submatrix.
//     *
//     * @param r Array of row indices.
//     * @param c Array of column indices.
//     * @return A(r(:),c(:))
//     * @exception ArrayIndexOutOfBoundsException Submatrix indices
//     */
//    public SparseMatrix getMatrix(int[] r, int[] c) {
//    	SparseMatrix X = new SparseMatrix(r.length, c.length);
////        double[][] B = X.getArray();
//        try {
//            for (int i = 0; i < r.length; i++) {
//                for (int j = 0; j < c.length; j++) {
////                    B[i][j] = A[r[i]][c[j]];
//                }
//            }
//        } catch (ArrayIndexOutOfBoundsException e) {
//            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
//        }
//        return X;
//    }
//
//    /**
//     * Get a submatrix.
//     *
//     * @param i0 Initial row index
//     * @param i1 Final row index
//     * @param c Array of column indices.
//     * @return A(i0:i1,c(:))
//     * @exception ArrayIndexOutOfBoundsException Submatrix indices
//     */
//    public SparseMatrix getMatrix(int i0, int i1, int[] c) {
//    	SparseMatrix X = new SparseMatrix(i1 - i0 + 1, c.length);
////        double[][] B = X.getArray();
//        try {
//            for (int i = i0; i <= i1; i++) {
//                for (int j = 0; j < c.length; j++) {
////                    B[i - i0][j] = A[i][c[j]];
//                }
//            }
//        } catch (ArrayIndexOutOfBoundsException e) {
//            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
//        }
//        return X;
//    }
//
//    /**
//     * Get a submatrix.
//     *
//     * @param r Array of row indices.
//     * @param j0 Initial column index
//     * @param j1 Final column index
//     * @return A(r(:),j0:j1)
//     * @exception ArrayIndexOutOfBoundsException Submatrix indices
//     */
//    public SparseMatrix getMatrix(int[] r, int j0, int j1) {
//    	SparseMatrix X = new SparseMatrix(r.length, j1 - j0 + 1);
////        double[][] B = X.getArray();
//        try {
//            for (int i = 0; i < r.length; i++) {
//                for (int j = j0; j <= j1; j++) {
////                    B[i][j - j0] = A[r[i]][j];
//                }
//            }
//        } catch (ArrayIndexOutOfBoundsException e) {
//            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
//        }
//        return X;
//    }
    public SparseMatrix echolonConvert(){ // throws IOException 
        SparseMatrix X = this.copy();//new SparseMatrix(m, n);
        if(m>n)
            throw new ArrayIndexOutOfBoundsException(" echolonConvert: m>n");
        if (X.rows[0].get(0) <= 0) {
            int j = 1;
            while (X.rows[0].get(0) <= 0 && j < m) {
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
            while (j < m) {
//    			System.out.println(j+": "+X.rows[j].get(0));
                if (X.rows[j].get(l) != 0) {
//        			System.out.println(j+"-"+i+": "+X.rows[j].get(i-1)+"==="+"-"+ X.rows[i-1].get(i-1));
                    double tmp = (-1.0) * X.rows[j].get(l) / X.rows[i].get(l);
//        			System.out.println(j+"-"+i+": "+tmp);
                    X.rows[j] = X.rows[j].plus(X.rows[i].scale(tmp));
//        			X.rows[j].print();
                }
                j++;
            }
//            X.print("X0 " + i);
            i++;
            l++;
//            System.err.println("else i " + i);
            if((i == m-1) || (l == n -1) )
                break;
            else
            {
                int k = i;
//                System.err.println("else i " +i + " l: "+l);
//                System.err.println("else i " +i + " l: "+l +" "+ X.rows[i].get(l));
                if (X.rows[i].get(l) == 0) {
                    while(X.rows[i].get(l) == 0 && i<m)
                    {
//                        System.err.println("else i " +i + " L: "+l);
                        if(X.rows[i].isZeroVector())
                        {
                            System.err.println("X.rows[i].isZeroVector()");
                        }
                        j = i+1;
//                        System.err.println(" j " +j + " l: "+l +" "+ X.rows[j].get(l));
                        while (X.rows[j].get(l) == 0 && j < m-1) {
                            j++;
                            if( j == m-1)
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
        SparseMatrix ret = copy();
        for(int row = X.m-1; row>0; row--)
        {
            if(X.getRow(row).nnz()==0){
                System.err.println("Zero row: "+ row);
//                X.removeColumn(row)
//                X.m = X.m--;
            }
        }
        for(int col = X.n-1; col>0; col--)
        {
            if(X.getColumn(col).nnz()==0){
                System.err.println("Zero col: "+ col);
//                ret = ret.removeColumn(col);
            }
        }
//        X.print("X");
        for(int col = X.n-1; col >0; col--)
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

    public SparseMatrix matrixRemoveRow(int index) {
        SparseMatrix X = this.copy();
        SparseMatrix X2 = new SparseMatrix(X.getRowDimension() - 1, X.getColumnDimension());
//        System.out.println("matrixRemoveRow " + index);

        try {
            int k = 0;
            for (int i = 0; i < getRowDimension() - 1; i++) {
                if (k == index) {
                    k++;
                }
                X2.rows[i] = X.rows[k];
                k++;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }

//        X2.print(true);
        return X2;
    }

    public SparseMatrix baseMatrix() throws IOException {
        SparseMatrix X = this.echolonConvert().copy();

        System.out.println("baseMatrix");
//        X.print(true);

        // remove zero row
        for (int i = m - 1; i >= 0; i--) {
            // System.out.println("========="+i);
            if (Maths.isVerterZero(X.getRow(i).toArrayDouble())) {
                X = X.matrixRemoveRow(i);
            } else {
                break;
            }
        }
        for (int i = n - 1; i >= 0; i--) {
            // System.out.println("========="+i);
            if (Maths.isVerterZero(X.getColumn(i).toArrayDouble())) {
                X = X.matrixRemoveColumn(i);
            }
            // else
            // break;
        }
        // sort echolon matrix, move un-use column to tail /*đang sai*/ 
        for (int i = 0; i < X.getRowDimension() - 1; i++) {
            // System.out.println("baseMatrix +"+j);
            if ((i < X.getColumnDimension()) && (X.get(i, i) == 0)) {
                int j = i + 1;
                while (X.get(i, i) == 0) {
                    X = X.changeColumn(i, j);
                    j++;
                    if (j >= n) {
                        break;
                    }
                }
            }
        }

        // X.print();
        // remove un-use column or same column
        for (int i = X.getColumnDimension() - 1; i >= 0; i--) {
            // System.out.println(i+"-"+X.getRowDimension()+"
            if (X.get(X.getRowDimension() - 1, i) == 0) {
                X = X.matrixRemoveColumn(i);
            } else {
                break;
            }
        }
        return X;
    }

    public SparseMatrix orthorNorm(){ // throws IOException 
        SparseMatrix X = this.copy(); 
        System.out.println("orthorNorm");

        for (int i = 0; i < n; i++) {
            SparseVector col = X.getColumn(i);
            if(col.nnz()>0)
            {
                double avr = col.getSum() / col.size();
//                System.out.println("orthorNorm "+i +": "+col.getSum()+"/"+ col.size());
//                col.print("column "+i);
                for (int j = 0; j < m; j++) {
                    X.put(j, i, X.get(j,i) - avr);
                }
            }
        }
        return X;
    }

    private SparseMatrix changeColumn(int i, int j) {
        SparseMatrix X = this.copy();
        // TODO Auto-generated method stub

        return X;
    }

    private SparseMatrix matrixRemoveColumn(int i) {
        SparseMatrix X = this.copy();
        // TODO Auto-generated method stub

        return X;
    }

    public SparseMatrix swapRow(int i, int j){ // throws IOException 
        SparseMatrix X = this.copy();

        SparseVector tmp = X.getRow(i);
        X.rows[i] = X.rows[j];
        X.rows[j] = tmp;

        return X;
    }

    public SparseMatrix sortRow() throws IOException {
        SparseMatrix X = this.copy();

        for (int i = 0; i < m; i++) {
            for (int j = i + 1; j < m; j++) {

                if (X.rows[i].nnz() < X.rows[j].nnz()) {
//        			System.out.println(X.rows[i].nnz()+"---"+X.rows[j].nnz());
                    X = X.swapRow(i, j);
                }
            }
        }
        return X;
    }

//    public SparseMatrix mullAll(int value) {
//        SparseMatrix X = this.copy();
//
//        for (int i = 0; i < m; i++) {
//            X.rows[i] = X.rows[i].scale((double) value);
////    		for (SparseVector sparseVector : X.rows) {
////    			sparseVector.scale((double)value);
////			}
//        }
//        return X;
//    }

    public SparseMatrix othogono(int value) {
        SparseMatrix X = this.copy();

        for (int i = 0; i < m; i++) {

        }
        
        return X;
    }
        
    public SparseMatrix othonormal(int value) {
        SparseMatrix X = this.copy();

        for (int i = 0; i < m; i++) {
            X.rows[i] = X.rows[i].scale((double) value);
        }
        return X;
    }
        
    public SparseMatrix othorStandeder() {
        SparseMatrix X = this.copy();

        for (int i = 0; i < n; i++) {
            double tmp = X.getColumn(i).norm();
            for(int j = 0; j< m; j++)
            {
                X.put(j, i, X.get(j, i)/tmp);
//            X.rows[i] = X.rows[i];
            }
        }
        return X;
    }
    
    public SparseMatrix othorStandeder2() {
        SparseMatrix X = this.copy();

        for (int i = 0; i < n; i++) {
            double tmp = X.getColumn(i).sum();
            for(int j = 0; j< m; j++)
            {
                X.put(j, i, X.get(j, i)/tmp);
            }
        }
        return X;
    }
    
    public SparseMatrix centered() {
        SparseMatrix X = this.copy();

        for (int i = 0; i < n; i++) {
            double tmp = X.getColumn(i).sum()/m;
            for(int j = 0; j< m; j++)
            {
                X.put(j, i, X.get(j, i) - tmp);
            }
        }
        return X;
    }
    public static SparseMatrix Algorithm1(SparseMatrix A, double lamda, double stopEpsilon) {
//    	SparseMatrix ret = new SparseMatrix(U.m, U.n);
        A.print(true,"A " +A.m+"-"+A.n);
        SparseMatrix U = A.echolonConvert();
        U.print(true,"U (echelon of A) " +U.m+"-"+U.n);
        /*
        U = echelon(A) -> new matrix has same ranks with A
        trực giao + chính chuẩn U để được U' trực chuẩn
        */
        
        U = U.orthorNorm();
        U.print(true,"U orthorNorm " +U.m+"-"+U.n);
        
        U = U.gramShmidt();
        U.toDensoMatrix().print(true,"U gramShmidt " +U.m+"-"+U.n);

        U = U.othorStandeder();
        U.print(true,"U othorStandeder " +U.m+"-"+U.n);
        
        SparseMatrix U1 = U.transpose();
        U1.print(true,"UT " +U1.m+"-"+U1.n);
        SparseMatrix Xk = new SparseMatrix(100, U.n);
//        U1.mulMatrix(A).print("A*U1 " +U1.m+"-"+U1.n);
        SparseMatrix Xk0 = U1.mulMatrix(A); //.transpose();// getRow(0);
        SparseMatrix Xk1 = Xk0.copy();
//      System.err.println("fisrt " + Xk.rows[0].size());
//    	U1.mulMatrix(A).rows[0].print();
        SparseVector Si = new SparseVector(U.n);
        SparseMatrix S = U1.mulMatrix(A);
        
        /*
        for (int i = 0; i < U.n; i++) {
//            U1.rows[i].dot(A);
            SparseVector vTmp = U1.rows[i];
            SparseMatrix tmp  = new SparseMatrix(1, U1.n);
//            tmp.rows

            Si.put(i, U1.rows[i].dot(A.getColumn(i)));
        }
        */
        SparseMatrix U2 = U.transpose();

        for(int j =0; j < U2.m; j++)
        {
            double tmp = 0.0;
//            System.out.println(" U: "+U2.n + "-"+U2.m+"\nA :"+A.n + "-"+A.m);
            for(int i =0; i< A.m; i++)
            {
//                System.out.println(" U: "+U1.n + "-"+U1.m+"\nA :"+A.n + "-"+A.m);
//                System.out.println(" U: "+U.m +"\nA :"+A.rows[i].size());
                tmp+= U2.rows[j].dot(A.getColumn(i));
            }
//            System.out.println("tmp = "+j+":"+tmp);
            Si.put(j, tmp);
        }
        
        int k = 1;
        Si.print("Si row :");
        S.print(true,"Si :");
        
        Xk0.print(true,"Xk 0: ");
        while (k<1000) { //k<100
            for (int i = 0; i < U.m; i++) {
                for(int j = 0; j< U.n; j++)
                {
                	/*
                    if (S.get(i,j) > lamda) {
                        Xk1.put(i, j, S.get(i,j) - lamda*A.n);
                    } else if (S.get(i,j) < -lamda) {
                        Xk1.put(i, j, S.get(i,j) + lamda*A.n);
                    } else {
                        Xk1.rows[i].put(j, 0);
                    }
                    /*/
                    //check condition 2(KKT condition), |xj - sj| <lamda 
                    /*/
                    if(((S.get(i,j)- Xk1.get(i,j)) > 0 && (S.get(i,j)- Xk1.get(i,j)) < lamda)
                    || ((S.get(i,j)- Xk1.get(i,j)) < 0 && (S.get(i,j)- Xk1.get(i,j)) > lamda))
                    {
                        Xk1.put(i,j, 0);
//                        System.out.println("XK ("+i+"-"+j+") = 0 "+k);
                    }
                    */
                    if (Si.get(j) > lamda) {
                        Xk1.put(i, j, Si.get(j) - lamda*A.n);
                    } else if (S.get(i,j) < -lamda) {
                        Xk1.put(i, j, Si.get(j) + lamda*A.n);
                    } else {
                        Xk1.rows[i].put(j, 0);
                    }
                    /*/
                    //check condition 2(KKT condition), |xj - sj| <lamda 
                    /*/
                    if(((Si.get(j)- Xk1.get(i,j)) > 0 && (Si.get(j)- Xk1.get(i,j)) < lamda)
                    || ((Si.get(j)- Xk1.get(i,j)) < 0 && (Si.get(j)- Xk1.get(i,j)) > lamda))
                    {
                        Xk1.put(i,j, 0);
//                        System.out.println("XK ("+i+"-"+j+") = 0 "+k);
                    }
                }
                
            }

            
            for(int i = 0; i < Xk1.m; i++)
            {
//                double epcilon =  Xk1.plus(Xk0.scale(-1)).norm();
//                Xk1.getColumn(i).print("Xk1 "+i+ ":");
//                Xk0.getColumn(i).print("Xk0 "+i+ ":");
//                double epcilon =  Xk1.getColumn(i).plus(Xk0.getColumn(i).scale(-1)).norm();
                
                Xk1.rows[i].print("Xk1 "+i+ ":");
                Xk0.rows[i].print("Xk0 "+i+ ":");
                double epcilon =  Xk1.rows[i].plus(Xk0.rows[i].scale(-1)).norm();
                
                System.out.println("epcilon "+k+":" + epcilon);
                if (epcilon < stopEpsilon) 
                {
                    System.out.println("top at: " + k);
                    return Xk1;
                }
            }
            Xk0= Xk1.copy();
//            S = Xk1.copy();
            k++;
//            System.out.println("top at: " + k);
        }
//System.out.println("top at: " + k);
        return Xk1;
    }

    public SparseMatrix removeColumn(int col) {
        SparseMatrix ret  = new SparseMatrix(m, n-1);
        for(int i = 0; i< m; i++)
        {
//            for(Node<Integer, Double> st : this.getRow(i).getNode())
//            {
//                
//            }
            for(int j = 0; j< n; j++)
            {
                if(j<col)
                    ret.put(i, j, this.get(i, j));
                else if( j>col)
                    ret.put(i, j-1, this.get(i, j));
//                else
                    
            }
        }
//        ret.setColumn(this.getColumn(n-1),col);
        return  ret;
    }

//    private SparseMatrix scale(double value) {
//        SparseMatrix ret = this.copy();
//        for(int i = 0; i<m; i++)
//            ret.setRow(i, ret.getRow(i).scale(value)); 
//        return ret;                
//    }

    private double norm() {
        double ret = 0;
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        for(int i = 0; i<m; i++)
            ret += this.rows[i].norm();
        return ret;
    }

    private SparseMatrix gramShmidt() {
        SparseMatrix X = this.copy();
        
        for (int i = 1; i < X.n; i++) {
//            SparseVector Vi_pre = new SparseVector(m);
            SparseVector Xi = X.getColumn(i);
//            double tmp2 = 0.0;
            // System.out.println("gramShmidt "+tmp2);
//            Xi.print("Xi "+i+":");
            SparseVector tmp = new SparseVector(m);
//            java.util.Arrays.fill(tmp, 0.0);
            for (int j = 0; j < i; j++) {
//                double tmp2 = 0.0;
                SparseVector Vi_pre = X.getColumn(j);
//                Vi_pre.print("vi "+j+":");
                double tmp2 =  Xi.dot(Vi_pre) / Vi_pre.dot(Vi_pre);
                // System.out.println("gramShmidt "+tmp2);
                tmp = tmp.plus(Vi_pre.scale(tmp2));  //tmp); //
//                Vi_pre.print("Vi scale "+tmp2 +" ("+Xi.dot(Vi_pre) + " "+Vi_pre.dot(Vi_pre)+") "+j +" "+tmp +":\n");
            }
//            Maths.vecSubtract(Xi, tmp);

            X.setColumn(i, Xi.plus(tmp.scale(-1)));
//            X.getColumn(i).print("Xi "+i+":");
//            System.out.println("-----");
        }
        return X;
    }
    

	

    
}
