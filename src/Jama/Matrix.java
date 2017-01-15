package Jama;

import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import javax.sql.RowSet;

//import java.text.FieldPosition;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StreamTokenizer;
import Jama.util.*;
//import java.lang.reflect.Array;

/**
 * Jama = Java Matrix class.
 * <P>
 * The Java Matrix Class provides the fundamental operations of numerical linear
 * algebra. Various constructors create Matrices from two dimensional arrays of
 * double precision floating point numbers. Various "gets" and "sets" provide
 * access to submatrices and matrix elements. Several methods implement basic
 * matrix arithmetic, including matrix addition and multiplication, matrix
 * norms, and element-by-element array operations. Methods for reading and
 * printing matrices are also included. All the operations in this version of
 * the Matrix Class involve real matrices. Complex matrices may be handled in a
 * future version.
 * <P>
 * Five fundamental matrix decompositions, which consist of pairs or triples of
 * matrices, permutation vectors, and the like, produce results in five
 * decomposition classes. These decompositions are accessed by the Matrix class
 * to compute solutions of simultaneous linear equations, determinants, inverses
 * and other matrix functions. The five decompositions are:
 * <P>
 * <UL>
 * <LI>Cholesky Decomposition of symmetric, positive definite matrices.
 * <LI>LU Decomposition of rectangular matrices.
 * <LI>QR Decomposition of rectangular matrices.
 * <LI>Singular Value Decomposition of rectangular matrices.
 * <LI>Eigenvalue Decomposition of both symmetric and nonsymmetric square
 * matrices.
 * </UL>
 * <DL>
 * <DT><B>Example of use:</B></DT>
 * <P>
 * <DD>Solve a linear system A x = b and compute the residual norm, ||b - A x||.
 * <P>
 *
 * <PRE>
 * double[][] vals = { { 1., 2., 3 }, { 4., 5., 6. }, { 7., 8., 10. } };
 * Matrix A = new Matrix(vals);
 * Matrix b = Matrix.random(3, 1);
 * Matrix x = A.solve(b);
 * Matrix r = A.times(x).minus(b);
 * double rnorm = r.normInf();
 * </PRE>
 *
 * </DD>
 * </DL>
 *
 * @author The MathWorks, Inc. and the National Institute of Standards and
 * Technology.
 * @version 5 August 1998
 */
public class Matrix implements Cloneable, java.io.Serializable {

    /*
	 * ------------------------ Class variables ------------------------
     */
    /**
     * Array for internal storage of elements.
     *
     * @serial internal array storage.
     */
//    private double[][] A;
    private SparseVector[] rows;  

    /**
     * Row and column dimensions.
     *
     * @serial row dimension.
     * @serial column dimension.
     */
    private int m, n;

    DecimalFormat twoDForm = new DecimalFormat(" 0.00000");
    /*
	 * ------------------------ Constructors ------------------------
     */
    /**
     * Construct an m-by-n matrix of zeros.
     *
     * @param m Number of rows.
     * @param n Number of colums.
     */
    public Matrix(int m, int n) {
        this.m = m;
        this.n = n;
//        A = new double[m][n];
        rows = new SparseVector[m];
        for (int i = 0; i < m; i++)
            rows[i] = new SparseVector(n);
    }

    /**
     * Construct an m-by-n constant matrix.
     *
     * @param m Number of rows.
     * @param n Number of colums.
     * @param s Fill the matrix with this scalar value.
     */
    public Matrix(int m, int n, double s) {
        this.m = m;
        this.n = n;
        
        rows = new SparseVector[m];
        for (int i = 0; i < m; i++)
            rows[i] = new SparseVector(n);
        
//        A = new double[m][n];
//        for (int i = 0; i < m; i++) {
//            for (int j = 0; j < n; j++) {
//                A[i][j] = s;
//            }
//        }
    }

    /**
     * Construct a matrix from a 2-D array.
     *
     * @param A Two-dimensional array of doubles.
     * @exception IllegalArgumentException All rows must have the same length
     * @see #constructWithCopy
     */
    public Matrix(double[][] A) {
        m = A.length;
        n = A[0].length;
        rows = new SparseVector[m];
        for (int i = 0; i < m; i++) {
            if (A[i].length != n) {
                throw new IllegalArgumentException("All rows must have the same length.");
            }
            rows[i] = new SparseVector(A[i]);
        }
//        this.A = A;
    }

    /**
     * Construct a matrix quickly without checking arguments.
     *
     * @param A Two-dimensional array of doubles.
     * @param m Number of rows.
     * @param n Number of colums.
     */
    public Matrix(double[][] A, int m, int n) {
//        this.A = A;
        this.m = m;
        this.n = n;
        for (int i = 0; i < m; i++) {
            if (A[i].length != n) {
                throw new IllegalArgumentException("All rows must have the same length.");
            }
            rows[i] = new SparseVector(A[i]);
        }
    }

    /**
     * Construct a matrix from a one-dimensional packed array
     *
     * @param vals One-dimensional array of doubles, packed by columns (ala
     * Fortran).
     * @param m Number of rows.
     * @exception IllegalArgumentException Array length must be a multiple of m.
     */
    public Matrix(double vals[], int m) {
        this.m = m;
        n = (m != 0 ? vals.length / m : 0);
        if (m * n != vals.length) {
            throw new IllegalArgumentException("Array length must be a multiple of m.");
        }
        double A[][] = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                A[i][j] = vals[i + j * m];
            }
            rows[i] = new SparseVector(A[i], false);
        }
//        for (int i = 0; i < m; i++) {
//            if (A[i].length != n) {
//                throw new IllegalArgumentException("All rows must have the same length.");
//            }
//            rows[i] = new SparseVector(A[i], false);
//        }
    }
    

    /*
	 * ------------------------ Public Methods ------------------------
     */

 /*
	 * @functionName:
	 * 
	 * @input param:
	 * 
	 * @output param:
     */
    // public Matrix getRow(int index) {
    // Matrix retA = new Matrix(1, n);// A, m, n);
    //
    // // double ret[] = new double[n];
    // // for(int i =0; i<n; i++)
    // // ret[i] = getValue(index, i);
    // for (int i = 0; i < n; i++) {
    // retA.set(0, i, get(index, i));
    // }
    // return retA;
    // }
    public SparseVector getRow(int index) {
        return rows[index];
    }

    /*
	 * @functionName:
	 * 
	 * @input param:
	 * 
	 * @output param:
     */
    public SparseVector getColumn(int index) {
    	SparseVector ret = new SparseVector(m);
//        double ret[] = new double[m];
    	int j = 0;
    	for(SparseVector row : rows)
    	{
    			ret.put(j, row.get(index),false);
    			j++;
    	}
        return ret;
        // Matrix retA = new Matrix(m, 1);// A, m, n);
        // for (int i = 0; i < m; i++) {
        // retA.set(i, 0, get(i, index));
        // }

        // return retA;
    }

    /*
	 * @functionName:
	 * 
	 * @input param:
	 * 
	 * @output param:
     */
    public void setRow(int index, double row[]) {
//        A[index] = row;
        rows[index] = new SparseVector(row);
    }

    /*
	 * @functionName:
	 * 
	 * @input param:
	 * 
	 * @output param:
     */
    public void setColumn(int index, double col[]) {
        for (int i = 0; i < col.length; i++) {
//            A[i][index] = col[i];
            rows[i].put(index, col[i], false);
        }
    }

    public Matrix changeRow(int row1, int row2) {
        Matrix X = this.copy();
        for (int i = 0; i < n; i++) {
            double value = X.get(row1, i);
            X.set(row1, i, X.get(row2, i));
            X.set(row2, i, value);
        }
//        System.out.println("changeRow " + row1 + "-" + row2);
        // X.print();
        return X;
    }

    public Matrix changeColumn(int col1, int col2) {
        Matrix X = this.copy();
        for (int i = 0; i < m; i++) {
            double value = X.get(i, col1);
            X.set(i, col1, X.get(i, col2));
            X.set(i, col2, value);
        }
        // System.out.println("changeCol " + col1 + "-" + col2);
        X.print(false,"");
        return X;
    }

    public Matrix mulRow(int row, double value) {
        Matrix X = this.copy();// new Matrix(m,n);
        for (int i = 0; i < n; i++) {
            X.set(row, i, X.get(row, i) * value);
        }
        return X;
    }

    public Matrix mulA_ChangeRow(double value, int row1, int row2) {
        Matrix X = this.copy();// new Matrix(m,n);
        // System.out.println("mulA_ChangeRow " + row1 + "->" + row2 + "(" +
        // value + ")");
        for (int i = 0; i < n; i++) {
            X.set(row2, i, X.get(row2, i) + (X.get(row1, i) * value));
        }
        // System.out.println("mulA_ChangeRow ");
        // X.print();
        return X;
    }

    /*
	
     */
    public Matrix echolonConvert() throws IOException {
        Matrix X = this.copy();// new Matrix(m,n);
        if (m > n) {
//            System.out.println("echolonConvert 1 (m>n)");

            // X= X.transpose();
            if (X.get(0, 0) <= 0) {
                for (int j = 1; j < m; j++) { // m>n or n>m
                    if (X.get(j, 0) > 0) {
                        X = X.changeRow(0, j);
                        break;
                    }
                }
            }
//            X.print(true);
            for (int i = 1; i < m; i++) {
//			System.out.println("echolonConvert 1 " + i);

	            if ((i>1)&&(i<X.getColumnDimension())&&(X.get(i-1, i-1) == 0)) {
	            	int k =i-1;
	                for (int j = k+1; j < m; j++) { // m>n or n>m
	                	
	                	if (X.get(j, i-1) != 0) {
	                		
	                        X = X.changeRow(j, k);
//	                        break;
	                        k= j;
	                    }
	                }
	            }			
//                 System.out.println("echolonConvert 2");
//                 X.print(true);
                for (int j = i; j < m; j++) {
//                	if(i<n)
                	{
	                    if ((i<n)&&(X.get(j, i-1) != 0)) {
	                        
	                        double x = (-1.0) * (X.get(j, i-1) / X.get(i-1, i-1));
//	                      System.out.println("echolonConvert 3 "+ x+" - "+ X.get(j, i-1)+" - "+ X.get(i-1, i-1)+" - "+ j+" - "+i);
	                        X = X.mulA_ChangeRow(x, i-1, j);
//	                        X.print(true);
	                    }
                	}
//                    System.out.println("echelon " + j);
                    
                }
                
	            if ((i>=n)&&(X.get(i, n-1) == 0)) {
	            	int k =i-1;
	                for (int j = k+1; j < m; j++) {
//	                	System.out.println("echelon i>=n: "+ i);
	                	if (X.get(j, n-1) != 0) {
	                		
	                        X = X.changeRow(j, k);
//	                        break;
	                        k= j;
	                    }
	                }
	            }	

            }
        } else {
            // X= X.transpose();
            for (int i = 0; i < m; i++) {
//			System.out.println("echolonConvert 1 " + i);
                if (X.get(i, i) <= 0) {
                    for (int j = i + 1; j < m; j++) { // m>n or n>m
                        if (X.get(j, i) > 0) {
//						System.out.println("echolonConvert1");
                            X = X.changeRow(i, j);
                            break;
                        }
                    }
                    int k = i;
                    while (X.get(i, k) <= 0) {
//                        System.out.println("echolonConvert check " + k);
                        for (int j = k; j < m; j++) {
                            // System.out.println(X.get(j, i)+"("+j+"-"+i+")");
                            if (X.get(j, k) != 0) {
//							System.out.println("echolonConvert2");
                                X = X.changeRow(i, j);
                                break;
                            }
                        }
                        k++;
                        if (k == m || k == n) {
                            break;
                        }
//                        System.out.println("echolonConvert check 2" + k);
                    }

                }
                // System.out.println("echolonConvert 2");
                for (int j = i + 1; j < m; j++) {
                    if (X.get(j, i) != 0) {
                        // System.out.println("echolonConvert 3 "+ j);
                        double x = (-1.0) * (X.get(j, i) / X.get(i, i));
                        X = X.mulA_ChangeRow(x, i, j);
                    }
                }

            }
        }
//        System.out.println("echolonConvert final: ");
//         X.print(true);
        X.saveFile("echelonForm");
        return X;
    }

    /*
	 * TODO
     */
    public int rankWithEcholon() {
        int ret = 0;
        return ret;
    }

    /*
	 * TODO
     */
    public Matrix dotProc() {
        Matrix X = this.copy();
        System.out.println("dotProc");

        X.print(false, "");
        return X;
    }

    /*
	 * TODO
     */
    public Matrix matrixRemoveRow(int index) {
        Matrix X = this.copy();
        Matrix X2 = new Matrix(X.getRowDimension() - 1, X.getColumnDimension());
//        System.out.println("matrixRemoveRow " + index);

        try {
            int k = 0;
            for (int i = 0; i < getRowDimension() - 1; i++) {
                if (k == index) {
                    k++;
                }
                for (int j = 0; j <= getColumnDimension() - 1; j++) {
                    X2.set(i, j, X.get(k, j));
                }
                k++;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }

//        X2.print(true);
        return X2;
    }

    /*
	 * TODO
     */
    public Matrix matrixRemoveColumn(int index) {
        Matrix X = this.copy();
        // X.print(true);
//        System.out.println("matrixRemoveColumn " + index);
        Matrix X2 = new Matrix(X.getRowDimension(), X.getColumnDimension() - 1);

        try {
            for (int i = 0; i <= getRowDimension() - 1; i++) {
                int k = 0;
                for (int j = 0; j < getColumnDimension() - 1; j++) {
                    if (k == index) {
                        k++;
                    }
                    X2.set(i, j, X.get(i, k));
                    k++;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }

//        X2.print(true);
        return X2;
    }

    /*
	 * TODO
     */
    public Matrix matrixRemoveSameColumn() {
        Matrix X = this.copy();
        System.out.println("matrixRemoveSameColumn ");

        for (int i = 0; i < m; i++) {

        }

        X.print(true, "");
        return X;
    }

    /*
	 * TODO
     */
    public Matrix matrixRemoveSameRow() {
        Matrix X = this.copy();
        System.out.println("matrixRemoveSameRow ");

        X.print(true, "");
        return X;
    }

    /*
	 * TODO
     */

    public Matrix baseMatrix() throws IOException {
        Matrix X = this.echolonConvert().copy();
        
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
            if ((i<X.getColumnDimension())&&(X.get(i, i) == 0)) {
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

    /*
	 * TODO
     */
    public Matrix normX() {
        Matrix X = this.copy();
        System.out.println("normX");

        for (int i = 0; i < n; i++) {
            double[] X2 = Maths.vecNorm1(X.getColumn(i).toArrayDouble());
            X.setColumn(i, X2);
        }

        X.print(false, "");
        return X;
    }

    /*
	 * TODO
     */
    public Matrix gramShmidt()  throws IOException {
        Matrix X = this.copy();
        System.out.println("gramShmidt");
        // X.print();

        for (int i = 1; i < n; i++) {
            double Vi_pre[] = new double[m];
            double Xi[] = X.getColumn(i).toArrayDouble();
            double tmp2 = 0.0;
            // System.out.println("gramShmidt "+tmp2);
            double tmp[] = new double[m];
            java.util.Arrays.fill(tmp, 0);
            for (int j = 0; j < i; j++) {
                Vi_pre = X.getColumn(j).toArrayDouble();
                tmp2 = Maths.vecValue(Xi, Vi_pre) / Maths.vecValue(Vi_pre, Vi_pre);
                // System.out.println("gramShmidt "+tmp2);
                // Maths.printArrayD("0", Vi_pre);
                // Maths.printArrayD("1",Maths.vecMul(Vi_pre, tmp2, m));
                tmp = Maths.vecAdd(Maths.vecMul(Vi_pre, tmp2), tmp); //
                // Maths.printArrayD("2",tmp);
            }
            X.setColumn(i, Maths.vecSubtract(Xi, tmp));
        }
//        X.print(true);
        X.saveFile("gramShmidt");
        return X;
    }
    
    public Matrix orthorNorm() {
    	Matrix X = this.copy();
        System.out.println("gramShmidt");
        
        return X;
	}

    public Matrix orthorlogyMat() throws IOException {
        Matrix X = this.gramShmidt().copy();
        System.out.println("trucGiao");

        for (int i = 0; i < n; i++) {
            double Xi[] = X.getColumn(i).toArrayDouble();
            Xi = Maths.vecDev(Xi, Math.sqrt(Maths.vecValue(Xi, Xi)));
            X.setColumn(i, Xi);
        }
        X.print(false, "");
        X.saveFile("trucGiao");
        return X;
    }

    /**
     * Construct a matrix from a copy of a 2-D array.
     *
     * @param A Two-dimensional array of doubles.
     * @exception IllegalArgumentException All rows must have the same length
     */
    public static Matrix constructWithCopy(double[][] A) {
        int m = A.length;
        int n = A[0].length;
        Matrix X = new Matrix(m, n);
//        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            if (A[i].length != n) {
                throw new IllegalArgumentException("All rows must have the same length.");
            }
            for (int j = 0; j < n; j++) {
                X.rows[i].put(i, A[i][j],false);//[j] = A[i][j];
            }
        }
        return X;
    }

    /**
     * Make a deep copy of a matrix
     */
    public Matrix copy() {
        Matrix X = new Matrix(m, n);
//        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
        	X.rows[i] = rows[i];
//            for (int j = 0; j < n; j++) {
//                C[i][j] = A[i][j];
//            }
        }
        return X;
    }

    /**
     * Clone the Matrix object.
     */
    public Object clone() {
        return this.copy();
    }

    /**
     * Access the internal two-dimensional array.
     *
     * @return Pointer to the two-dimensional array of matrix elements.
     */
    public double[][] getArray() {
    	double[][] A = new double[m][n];
    	for(int i = 0; i< m; i++)
    		A[i] = rows[i].toArrayDouble();
    	return A;
    }

    /**
     * Copy the internal two-dimensional array.
     *
     * @return Two-dimensional array copy of matrix elements.
     */
    public double[][] getArrayCopy() {
        double[][] C = new double[m][n];
        for (int i = 0; i < m; i++) {
        	C[i] = rows[i].toArrayDouble();
//        	for (int j = 0; j < n; j++) {
//                C[i][j] = A[i][j];
//            }
        }
        return C;
    }

    /**
     * Make a one-dimensional column packed copy of the internal array.
     *
     * @return Matrix elements packed in a one-dimensional array by columns.
     */
    public double[] getColumnPackedCopy() {
        double[] vals = new double[m * n];
        for (int i = 0; i < m; i++) {
        	double[] valsR = rows[i].toArrayDouble();
        	for (int j = 0; j < n; j++) {
                vals[i + j * m] = valsR[j];
            }
        }
        return vals;
    }

    /**
     * Make a one-dimensional row packed copy of the internal array.
     *
     * @return Matrix elements packed in a one-dimensional array by rows.
     */
    public double[] getRowPackedCopy() {
        double[] vals = new double[m * n];
        for (int i = 0; i < m; i++) {
        	double[] valsR = rows[i].toArrayDouble();
            for (int j = 0; j < n; j++) {
                vals[i * n + j] = valsR[j];
            }
        }
        return vals;
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

    /**
     * Get a single element.
     *
     * @param i Row index.
     * @param j Column index.
     * @return A(i,j)
     * @exception ArrayIndexOutOfBoundsException
     */
    public double get(int i, int j) {
        // System.out.println("row: "+i+" col "+j+ " value "+A[i][j]);
//        return A[i][j];
        return rows[i].get(j);
    }

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
    public Matrix getMatrix(int i0, int i1, int j0, int j1) {
        Matrix X = new Matrix(i1 - i0 + 1, j1 - j0 + 1);
//        double[][] B = X.getArray();
        try {
            for (int i = i0; i <= i1; i++) {
                for (int j = j0; j <= j1; j++) {
                	X.rows[i - i0].put(j - j0, rows[i].get(j), false);
//                    B[i - i0][j - j0] = rows[i].get(j);// A[i][j];
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return X;
    }

    /**
     * Get a submatrix.
     *
     * @param r Array of row indices.
     * @param c Array of column indices.
     * @return A(r(:),c(:))
     * @exception ArrayIndexOutOfBoundsException Submatrix indices
     */
    public Matrix getMatrix(int[] r, int[] c) {
        Matrix X = new Matrix(r.length, c.length);
//        double[][] B = X.getArray();
        try {
            for (int i = 0; i < r.length; i++) {
                for (int j = 0; j < c.length; j++) {
                    X.rows[i].put(j, rows[r[i]].get(c[j]), false);//B[i][j] = rows[r[i]].get(c[j]);//A[r[i]][c[j]];
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return X;
    }

    /**
     * Get a submatrix.
     *
     * @param i0 Initial row index
     * @param i1 Final row index
     * @param c Array of column indices.
     * @return A(i0:i1,c(:))
     * @exception ArrayIndexOutOfBoundsException Submatrix indices
     */
    public Matrix getMatrix(int i0, int i1, int[] c) {
        Matrix X = new Matrix(i1 - i0 + 1, c.length);
//        double[][] B = X.getArray();
        try {
            for (int i = i0; i <= i1; i++) {
                for (int j = 0; j < c.length; j++) {
                	X.rows[i - i0].put(j, rows[i].get(c[j]), false);
//                    B[i - i0][j] = A[i][c[j]];
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return X;
    }

    /**
     * Get a submatrix.
     *
     * @param r Array of row indices.
     * @param j0 Initial column index
     * @param j1 Final column index
     * @return A(r(:),j0:j1)
     * @exception ArrayIndexOutOfBoundsException Submatrix indices
     */
    public Matrix getMatrix(int[] r, int j0, int j1) {
        Matrix X = new Matrix(r.length, j1 - j0 + 1);
//        double[][] B = X.getArray();
        try {
            for (int i = 0; i < r.length; i++) {
                for (int j = j0; j <= j1; j++) {
                	X.rows[i].put(j-j0, rows[r[i]].get(j), false);
//                    B[i][j - j0] = A[r[i]][j];
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return X;
    }

    /**
     * Set a single element.
     *
     * @param i Row index.
     * @param j Column index.
     * @param s A(i,j).
     * @exception ArrayIndexOutOfBoundsException
     */
    public void set(int i, int j, double value) {
//        A[i][j] = s;
        rows[i].put(j, value);
    }

    /**
     * Set a submatrix.
     *
     * @param i0 Initial row index
     * @param i1 Final row index
     * @param j0 Initial column index
     * @param j1 Final column index
     * @param X A(i0:i1,j0:j1)
     * @exception ArrayIndexOutOfBoundsException Submatrix indices
     */
    public void setMatrix(int i0, int i1, int j0, int j1, Matrix X) {
        try {
            for (int i = i0; i <= i1; i++) {
                for (int j = j0; j <= j1; j++) {
                	X.rows[i].put(j, rows[i-i0].get(j-j0), false);
//                    A[i][j] = X.get(i - i0, j - j0);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
    }

    /**
     * Set a submatrix.
     *
     * @param r Array of row indices.
     * @param c Array of column indices.
     * @param X A(r(:),c(:))
     * @exception ArrayIndexOutOfBoundsException Submatrix indices
     */
    public void setMatrix(int[] r, int[] c, Matrix X) {
        try {
            for (int i = 0; i < r.length; i++) {
                for (int j = 0; j < c.length; j++) {
                	X.rows[r[i]].put(c[j], rows[i].get(j), false);
//                    A[r[i]][c[j]] = X.get(i, j);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
    }

    /**
     * Set a submatrix.
     *
     * @param r Array of row indices.
     * @param j0 Initial column index
     * @param j1 Final column index
     * @param X A(r(:),j0:j1)
     * @exception ArrayIndexOutOfBoundsException Submatrix indices
     */
    public void setMatrix(int[] r, int j0, int j1, Matrix X) {
        try {
            for (int i = 0; i < r.length; i++) {
                for (int j = j0; j <= j1; j++) {
                	X.rows[r[i]].put(j, rows[i].get(j-j0), false);
//                    A[r[i]][j] = X.get(i, j - j0);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
    }

    // public Matrix normX()
    // {
    //
    // }
    /**
     * Set a submatrix.
     *
     * @param i0 Initial row index
     * @param i1 Final row index
     * @param c Array of column indices.
     * @param X A(i0:i1,c(:))
     * @exception ArrayIndexOutOfBoundsException Submatrix indices
     */
    public void setMatrix(int i0, int i1, int[] c, Matrix X) {
        try {
            for (int i = i0; i <= i1; i++) {
                for (int j = 0; j < c.length; j++) {
                	X.rows[i].put(c[j], rows[i-i0].get(j), false);
//                    A[i][c[j]] = X.get(i - i0, j);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
    }

    /**
     * Matrix transpose.
     *
     * @return A'
     */
    public Matrix transpose() {
        Matrix X = new Matrix(n, m);
//        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
            	X.rows[j].put(i, rows[i].get(j), false);
//                C[j][i] = A[i][j];
            }
        }
        return X;
    }

    /**
     * One norm
     *
     * @return maximum column sum.
     */
    public double norm1() {
        double f = 0;
        for (int j = 0; j < n; j++) {
            double s = 0;
            for (int i = 0; i < m; i++) {
                s += Math.abs(rows[i].get(j));//A[i][j]);
            }
            f = Math.max(f, s);
        }
        return f;
    }

    /**
     * Two norm
     *
     * @return maximum singular value.
     */
    public double norm2() {
        return (new SingularValueDecomposition(this).norm2());
    }

    /**
     * Infinity norm
     *
     * @return maximum row sum.
     */
    public double normInf() {
        double f = 0;
        for (int i = 0; i < m; i++) {
            double s = 0;
            for (int j = 0; j < n; j++) {
                s += Math.abs(rows[i].get(j));//A[i][j]);
            }
            f = Math.max(f, s);
        }
        return f;
    }

    /**
     * Frobenius norm
     *
     * @return sqrt of sum of squares of all elements.
     */
    public double normF() {
        double f = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                f = Maths.hypot(f, rows[i].get(j));//A[i][j]);
            }
        }
        return f;
    }

    /**
     * Unary minus
     *
     * @return -A
     */
    public Matrix uminus() {
        Matrix X = new Matrix(m, n);
//        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
            	X.rows[i].put(j, -rows[i].get(j), false);
//                C[i][j] = -A[i][j];
            }
        }
        return X;
    }

    /**
     * C = A + B
     *
     * @param B another matrix
     * @return A + B
     */
    public Matrix plus(Matrix B) {
        checkMatrixDimensions(B);
        Matrix X = new Matrix(m, n);
//        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
            	X.rows[i].put(j, rows[i].get(j)+B.rows[i].get(j), false);
//                C[i][j] = A[i][j] + B.A[i][j];
            }
        }
        return X;
    }

    /**
     * A = A + B
     *
     * @param B another matrix
     * @return A + B
     */
    public Matrix plusEquals(Matrix B) {
        checkMatrixDimensions(B);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
            	rows[i].put(j, rows[i].get(j)+ B.rows[i].get(j), false);
//                A[i][j] = A[i][j] + B.A[i][j];
            }
        }
        return this;
    }

    /**
     * C = A - B
     *
     * @param B another matrix
     * @return A - B
     */
    public Matrix minus(Matrix B) {
        checkMatrixDimensions(B);
        Matrix X = new Matrix(m, n);
//        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
            	X.rows[i].put(j, rows[i].get(j) - B.rows[i].get(j), false);
//                C[i][j] = A[i][j] - B.A[i][j];
            }
        }
        return X;
    }

    /**
     * A = A - B
     *
     * @param B another matrix
     * @return A - B
     */
    public Matrix minusEquals(Matrix B) {
        checkMatrixDimensions(B);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
            	rows[i].put(j, rows[i].get(j) - B.rows[i].get(j), false);
//                A[i][j] = A[i][j] - B.A[i][j];
            }
        }
        return this;
    }

    /**
     * Element-by-element multiplication, C = A.*B
     *
     * @param B another matrix
     * @return A.*B
     */
    public Matrix arrayTimes(Matrix B) {
        checkMatrixDimensions(B);
        Matrix X = new Matrix(m, n);
//        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
            	X.rows[i].put(j, rows[i].get(j)*B.rows[i].get(j), false);
//                C[i][j] = A[i][j] * B.A[i][j];
            }
        }
        return X;
    }

    /**
     * Element-by-element multiplication in place, A = A.*B
     *
     * @param B another matrix
     * @return A.*B
     */
    public Matrix arrayTimesEquals(Matrix B) {
        checkMatrixDimensions(B);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
            	rows[i].put(j, rows[i].get(j)*B.rows[i].get(j), false);
//                A[i][j] = A[i][j] * B.A[i][j];
            }
        }
        return this;
    }

    /**
     * Element-by-element right division, C = A./B
     *
     * @param B another matrix
     * @return A./B
     */
    public Matrix arrayRightDivide(Matrix B) {
        checkMatrixDimensions(B);
        Matrix X = new Matrix(m, n);
//        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
            	X.rows[i].put(j, rows[i].get(j)/B.get(i, j), false);
//                C[i][j] = A[i][j] / B.A[i][j];
            }
        }
        return X;
    }

    /**
     * Element-by-element right division in place, A = A./B
     *
     * @param B another matrix
     * @return A./B
     */
    public Matrix arrayRightDivideEquals(Matrix B) {
        checkMatrixDimensions(B);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
            	rows[i].put(j, this.get(i, j)/B.get(i, j), false);
//                A[i][j] = A[i][j] / B.A[i][j];
            }
        }
        return this;
    }

    /**
     * Element-by-element left division, C = A.\B
     *
     * @param B another matrix
     * @return A.\B
     */
    public Matrix arrayLeftDivide(Matrix B) {
        checkMatrixDimensions(B);
        Matrix X = new Matrix(m, n);
//        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
            	X.rows[i].put(j, B.get(i, j)/get(i, j), false);
//                C[i][j] = B.A[i][j] / A[i][j];
            }
        }
        return X;
    }

    /**
     * Element-by-element left division in place, A = A.\B
     *
     * @param B another matrix
     * @return A.\B
     */
    public Matrix arrayLeftDivideEquals(Matrix B) {
        checkMatrixDimensions(B);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
            	rows[i].put(j, B.get(i, j)/get(i, j), false);
//                A[i][j] = B.A[i][j] / A[i][j];
            }
        }
        return this;
    }

    /**
     * Multiply a matrix by a scalar, C = s*A
     *
     * @param s scalar
     * @return s*A
     */
    public Matrix times(double s) {
        Matrix X = new Matrix(m, n);
//        double[][] C = X.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
            	X.rows[i].put(j, s*rows[i].get(j), false);
//                C[i][j] = s * A[i][j];
            }
        }
        return X;
    }

    /**
     * Multiply a matrix by a scalar in place, A = s*A
     *
     * @param s scalar
     * @return replace A by s*A
     */
    public Matrix timesEquals(double s) {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
            	rows[i].put(j, s*rows[i].get(j), false);
//                A[i][j] = s * A[i][j];
            }
        }
        return this;
    }

    /**
     * Linear algebraic matrix multiplication, A * B
     *
     * @param B another matrix
     * @return Matrix product, A * B
     * @exception IllegalArgumentException Matrix inner dimensions must agree.
     */
    public Matrix times(Matrix B) {
        if (B.m != n) {
            throw new IllegalArgumentException("Matrix inner dimensions must agree.");
        }
        Matrix X = new Matrix(m, B.n);
//        double[][] C = X.getArray();
        double[] Bcolj = new double[n];
        for (int j = 0; j < B.n; j++) {
            for (int k = 0; k < n; k++) {
                Bcolj[k] = B.get(k, j);//A[k][j];
            }
            for (int i = 0; i < m; i++) {
                double[] Arowi = rows[i].toArrayDouble();//A[i];
                double s = 0;
                for (int k = 0; k < n; k++) {
                    s += Arowi[k] * Bcolj[k];
                }
                X.rows[i].put(j, s, false);
//                C[i][j] = s;
            }
        }
        return X;
    }

    /**
     * LU Decomposition
     *
     * @return LUDecomposition
     * @see LUDecomposition
     */
    public LUDecomposition lu() {
        return new LUDecomposition(this);
    }

    /**
     * QR Decomposition
     *
     * @return QRDecomposition
     * @see QRDecomposition
     */
    public QRDecomposition qr() {
        return new QRDecomposition(this);
    }

    /**
     * Cholesky Decomposition
     *
     * @return CholeskyDecomposition
     * @see CholeskyDecomposition
     */
    public CholeskyDecomposition chol() {
        return new CholeskyDecomposition(this);
    }

    /**
     * Singular Value Decomposition
     *
     * @return SingularValueDecomposition
     * @see SingularValueDecomposition
     */
    public SingularValueDecomposition svd() {
        return new SingularValueDecomposition(this);
    }

    /**
     * Eigenvalue Decomposition
     *
     * @return EigenvalueDecomposition
     * @see EigenvalueDecomposition
     */
    public EigenvalueDecomposition eig() {
        return new EigenvalueDecomposition(this);
    }

    /**
     * Solve A*X = B
     *
     * @param B right hand side
     * @return solution if A is square, least squares solution otherwise
     */
    public Matrix solve(Matrix B) {
        return (m == n ? (new LUDecomposition(this)).solve(B) : (new QRDecomposition(this)).solve(B));
    }

    /**
     * Solve X*A = B, which is also A'*X' = B'
     *
     * @param B right hand side
     * @return solution if A is square, least squares solution otherwise.
     */
    public Matrix solveTranspose(Matrix B) {
        return transpose().solve(B.transpose());
    }

    /**
     * Matrix inverse or pseudoinverse
     *
     * @return inverse(A) if A is square, pseudoinverse otherwise.
     */
    public Matrix inverse() {
        return solve(identity(m, m));
    }

    /**
     * Matrix determinant
     *
     * @return determinant
     */
    public double det() {
        return new LUDecomposition(this).det();
    }

    /**
     * Matrix rank
     *
     * @return effective numerical rank, obtained from SVD.
     */
    public int rank() {
        return new SingularValueDecomposition(this).rank();
    }

    /**
     * Matrix condition (2 norm)
     *
     * @return ratio of largest to smallest singular value.
     */
    public double cond() {
        return new SingularValueDecomposition(this).cond();
    }

    /**
     * Matrix trace.
     *
     * @return sum of the diagonal elements.
     */
    public double trace() {
        double t = 0;
        for (int i = 0; i < Math.min(m, n); i++) {
            t += get(i, i);// A[i][i];
        }
        return t;
    }

    /**
     * Generate matrix with random elements
     *
     * @param m Number of rows.
     * @param n Number of colums.
     * @return An m-by-n matrix with uniformly distributed random elements.
     */
    public static Matrix random(int m, int n) {
        Matrix A = new Matrix(m, n);
//        double[][] X = A.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
            	A.rows[i].put(j, Math.random(), false);
//            	X[i][j] = Math.random();
            }
        }
        return A;
    }

    /**
     * Generate identity matrix
     *
     * @param m Number of rows.
     * @param n Number of colums.
     * @return An m-by-n matrix with ones on the diagonal and zeros elsewhere.
     */
    public static Matrix identity(int m, int n) {
        Matrix A = new Matrix(m, n);
//        double[][] X = A.getArray();
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
//                X[i][j] = (i == j ? 1.0 : 0.0);
                A.rows[i].put(j, i == j ? 1.0 : 0.0, false);
            }
        }
        return A;
    }

    /**
     * Print the matrix to stdout. Line the elements up in columns with a
     * Fortran-like 'Fw.d' style format.
     *
     * @param w Column width.
     * @param d Number of digits after the decimal.
     */
    public void print(boolean isPrint, String mess) {
        System.out.println(mess);
        if (isPrint == true) {
            for (int i = 0; i < m; i++) {
                String line = " " + i + ":  ";
                for (int j = 0; j < n; j++) {
                    line += twoDForm.format( get(i, j)) + "\t";
                }
                System.out.println(line);
                // System.out.println("\n");
            }
        }
        // print(new PrintWriter(System.out, true), w, d);
    }

    public void saveFile(String fileName) throws IOException {
        FileWriter fw = new FileWriter(fileName);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                fw.write(get(i, j)  + "\t");
            }
            fw.write("\n");
        }
        fw.close();
    }

    public void print(int w, int d) {
        print(new PrintWriter(System.out, true), w, d);
    }

    /**
     * Print the matrix to the output stream. Line the elements up in columns
     * with a Fortran-like 'Fw.d' style format.
     *
     * @param output Output stream.
     * @param w Column width.
     * @param d Number of digits after the decimal.
     */
    public void print(PrintWriter output, int w, int d) {
        DecimalFormat format = new DecimalFormat();
        format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        format.setMinimumIntegerDigits(1);
        format.setMaximumFractionDigits(d);
        format.setMinimumFractionDigits(d);
        format.setGroupingUsed(false);
        print(output, format, w + 2);
    }

    /**
     * Print the matrix to stdout. Line the elements up in columns. Use the
     * format object, and right justify within columns of width characters. Note
     * that is the matrix is to be read back in, you probably will want to use a
     * NumberFormat that is set to US Locale.
     *
     * @param format A Formatting object for individual elements.
     * @param width Field width for each column.
     * @see java.text.DecimalFormat#setDecimalFormatSymbols
     */
    public void print(NumberFormat format, int width) {
        print(new PrintWriter(System.out, true), format, width);
    }

    // DecimalFormat is a little disappointing coming from Fortran or C's
    // printf.
    // Since it doesn't pad on the left, the elements will come out different
    // widths. Consequently, we'll pass the desired column width in as an
    // argument and do the extra padding ourselves.
    /**
     * Print the matrix to the output stream. Line the elements up in columns.
     * Use the format object, and right justify within columns of width
     * characters. Note that is the matrix is to be read back in, you probably
     * will want to use a NumberFormat that is set to US Locale.
     *
     * @param output the output stream.
     * @param format A formatting object to format the matrix elements
     * @param width Column width.
     * @see java.text.DecimalFormat#setDecimalFormatSymbols
     */
    public void print(PrintWriter output, NumberFormat format, int width) {
        output.println(); // start on new line.
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                String s = format.format(get(i, j)); // format the number
                int padding = Math.max(1, width - s.length()); // At _least_ 1
                // space
                for (int k = 0; k < padding; k++) {
                    output.print(' ');
                }
                output.print(s);
            }
            output.println();
        }
        output.println(); // end with blank line.
    }

    /**
     * Read a matrix from a stream. The format is the same the print method, so
     * printed matrices can be read back in (provided they were printed using US
     * Locale). Elements are separated by whitespace, all the elements for each
     * row appear on a single line, the last row is followed by a blank line.
     *
     * @param input the input stream.
     */
    public static Matrix read(BufferedReader input) throws java.io.IOException {
        StreamTokenizer tokenizer = new StreamTokenizer(input);

        // Although StreamTokenizer will parse numbers, it doesn't recognize
        // scientific notation (E or D); however, Double.valueOf does.
        // The strategy here is to disable StreamTokenizer's number parsing.
        // We'll only get whitespace delimited words, EOL's and EOF's.
        // These words should all be numbers, for Double.valueOf to parse.
        tokenizer.resetSyntax();
        tokenizer.wordChars(0, 255);
        tokenizer.whitespaceChars(0, ' ');
        tokenizer.eolIsSignificant(true);
        java.util.Vector<Double> vD = new java.util.Vector<Double>();

        // Ignore initial empty lines
        while (tokenizer.nextToken() == StreamTokenizer.TT_EOL)
			;
        if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
            throw new java.io.IOException("Unexpected EOF on matrix read.");
        }
        do {
            vD.addElement(Double.valueOf(tokenizer.sval)); // Read & store 1st
            // row.
        } while (tokenizer.nextToken() == StreamTokenizer.TT_WORD);

        int n = vD.size(); // Now we've got the number of columns!
        double row[] = new double[n];
        for (int j = 0; j < n; j++) // extract the elements of the 1st row.
        {
            row[j] = vD.elementAt(j).doubleValue();
        }
        java.util.Vector<double[]> v = new java.util.Vector<double[]>();
        v.addElement(row); // Start storing rows instead of columns.
        while (tokenizer.nextToken() == StreamTokenizer.TT_WORD) {
            // While non-empty lines
            v.addElement(row = new double[n]);
            int j = 0;
            do {
                if (j >= n) {
                    throw new java.io.IOException("Row " + v.size() + " is too long.");
                }
                row[j++] = Double.valueOf(tokenizer.sval).doubleValue();
            } while (tokenizer.nextToken() == StreamTokenizer.TT_WORD);
            if (j < n) {
                throw new java.io.IOException("Row " + v.size() + " is too short.");
            }
        }
        int m = v.size(); // Now we've got the number of rows.
        double[][] A = new double[m][];
        v.copyInto(A); // copy the rows out of the vector
        return new Matrix(A);
    }

    /*
	 * ------------------------ Private Methods ------------------------
     */
    /**
     * Check if size(A) == size(B) *
     */
    private void checkMatrixDimensions(Matrix B) {
        if (B.m != m || B.n != n) {
            throw new IllegalArgumentException("Matrix dimensions must agree.");
        }
    }

    private static final long serialVersionUID = 1;
}
