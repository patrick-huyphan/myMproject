package Jama;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import Jama.util.Maths;

public interface IMatrix {
	
//	 public IMatrix(int m, int n);
//	    /**
//	     * Construct an m-by-n constant matrix.
//	     *
//	     * @param m Number of rows.
//	     * @param n Number of colums.
//	     * @param s Fill the matrix with this scalar value.
//	     */
//	    public IMatrix(int m, int n, double s) ;
//
//	    /**
//	     * Construct a matrix from a 2-D array.
//	     *
//	     * @param A Two-dimensional array of doubles.
//	     * @exception IllegalArgumentException All rows must have the same length
//	     * @see #constructWithCopy
//	     */
//	    public IMatrix(double[][] A) ;
//
//	    /**
//	     * Construct a matrix quickly without checking arguments.
//	     *
//	     * @param A Two-dimensional array of doubles.
//	     * @param m Number of rows.
//	     * @param n Number of colums.
//	     */
//	    public IMatrix(double[][] A, int m, int n);
//
//	    /**
//	     * Construct a matrix from a one-dimensional packed array
//	     *
//	     * @param vals One-dimensional array of doubles, packed by columns (ala
//	     * Fortran).
//	     * @param m Number of rows.
//	     * @exception IllegalArgumentException Array length must be a multiple of m.
//	     */
//	    public IMatrix(double vals[], int m);

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
	    public SparseVector getRow(int index) ;

	    /*
		 * @functionName:
		 * 
		 * @input param:
		 * 
		 * @output param:
	     */
	    public SparseVector getColumn(int index);

	    /*
		 * @functionName:
		 * 
		 * @input param:
		 * 
		 * @output param:
	     */
	    public void setRow(int index, double row[]) ;

	    /*
		 * @functionName:
		 * 
		 * @input param:
		 * 
		 * @output param:
	     */
	    public void setColumn(int index, double col[]) ;

	    public IMatrix changeRow(int row1, int row2);

	    public IMatrix changeColumn(int col1, int col2) ;

	    public IMatrix mulRow(int row, double value);

	    public IMatrix mulA_ChangeRow(double value, int row1, int row2);

	    /*
		
	     */
	    public IMatrix echolonConvert() throws IOException;
	    /*
		 * TODO
	     */
	    public int rankWithEcholon() ;
	    /*
		 * TODO
	     */
	    public IMatrix dotProc() ;
	    /*
		 * TODO
	     */
	    public IMatrix matrixRemoveRow(int index);
	    /*
		 * TODO
	     */
	    public IMatrix matrixRemoveColumn(int index);

	    /*
		 * TODO
	     */
	    public IMatrix matrixRemoveSameColumn() ;

	    /*
		 * TODO
	     */
	    public IMatrix matrixRemoveSameRow() ;

	    /*
		 * TODO
	     */

	    public IMatrix baseMatrix()throws IOException;

	    /*
		 * TODO
	     */
	    public IMatrix normX();

	    /*
		 * TODO
	     */
	    public IMatrix gramShmidt() ;

	    public IMatrix orthorlogyMat();

	    /**
	     * Construct a matrix from a copy of a 2-D array.
	     *
	     * @param A Two-dimensional array of doubles.
	     * @exception IllegalArgumentException All rows must have the same length
	     */
//	    public static IMatrix constructWithCopy(double[][] A);
	    
	    /**
	     * Make a deep copy of a matrix
	     */
	    public IMatrix copy();


	    /**
	     * Access the internal two-dimensional array.
	     *
	     * @return Pointer to the two-dimensional array of matrix elements.
	     */
	    public double[][] getArray();
	    /**
	     * Copy the internal two-dimensional array.
	     *
	     * @return Two-dimensional array copy of matrix elements.
	     */
	    public double[][] getArrayCopy() ;

	    /**
	     * Make a one-dimensional column packed copy of the internal array.
	     *
	     * @return Matrix elements packed in a one-dimensional array by columns.
	     */
	    public double[] getColumnPackedCopy();

	    /**
	     * Make a one-dimensional row packed copy of the internal array.
	     *
	     * @return Matrix elements packed in a one-dimensional array by rows.
	     */
	    public double[] getRowPackedCopy();

	    /**
	     * Get row dimension.
	     *
	     * @return m, the number of rows.
	     */
	    public int getRowDimension();

	    /**
	     * Get column dimension.
	     *
	     * @return n, the number of columns.
	     */
	    public int getColumnDimension();

	    /**
	     * Get a single element.
	     *
	     * @param i Row index.
	     * @param j Column index.
	     * @return A(i,j)
	     * @exception ArrayIndexOutOfBoundsException
	     */
	    public double get(int i, int j) ;

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
	    public IMatrix getMatrix(int i0, int i1, int j0, int j1);

	    /**
	     * Get a submatrix.
	     *
	     * @param r Array of row indices.
	     * @param c Array of column indices.
	     * @return A(r(:),c(:))
	     * @exception ArrayIndexOutOfBoundsException Submatrix indices
	     */
	    public IMatrix getMatrix(int[] r, int[] c);

	    /**
	     * Get a submatrix.
	     *
	     * @param i0 Initial row index
	     * @param i1 Final row index
	     * @param c Array of column indices.
	     * @return A(i0:i1,c(:))
	     * @exception ArrayIndexOutOfBoundsException Submatrix indices
	     */
	    public IMatrix getMatrix(int i0, int i1, int[] c) ;

	    /**
	     * Get a submatrix.
	     *
	     * @param r Array of row indices.
	     * @param j0 Initial column index
	     * @param j1 Final column index
	     * @return A(r(:),j0:j1)
	     * @exception ArrayIndexOutOfBoundsException Submatrix indices
	     */
	    public IMatrix getMatrix(int[] r, int j0, int j1);

	    /**
	     * Set a single element.
	     *
	     * @param i Row index.
	     * @param j Column index.
	     * @param s A(i,j).
	     * @exception ArrayIndexOutOfBoundsException
	     */
	    public void set(int i, int j, double s) ;

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
	    public void setMatrix(int i0, int i1, int j0, int j1, IMatrix X);

	    /**
	     * Set a submatrix.
	     *
	     * @param r Array of row indices.
	     * @param c Array of column indices.
	     * @param X A(r(:),c(:))
	     * @exception ArrayIndexOutOfBoundsException Submatrix indices
	     */
	    public void setMatrix(int[] r, int[] c, IMatrix X);

	    /**
	     * Set a submatrix.
	     *
	     * @param r Array of row indices.
	     * @param j0 Initial column index
	     * @param j1 Final column index
	     * @param X A(r(:),j0:j1)
	     * @exception ArrayIndexOutOfBoundsException Submatrix indices
	     */
	    public void setMatrix(int[] r, int j0, int j1, IMatrix X);

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
	    public void setMatrix(int i0, int i1, int[] c, IMatrix X);

	    /**
	     * Matrix transpose.
	     *
	     * @return A'
	     */
	    public IMatrix transpose();

	    /**
	     * One norm
	     *
	     * @return maximum column sum.
	     */
	    public double norm1();

	    /**
	     * Two norm
	     *
	     * @return maximum singular value.
	     */
	    public double norm2() ;

	    /**
	     * Infinity norm
	     *
	     * @return maximum row sum.
	     */
	    public double normInf() ;

	    /**
	     * Frobenius norm
	     *
	     * @return sqrt of sum of squares of all elements.
	     */
	    public double normF() ;

	    /**
	     * Unary minus
	     *
	     * @return -A
	     */
	    public IMatrix uminus();

	    /**
	     * C = A + B
	     *
	     * @param B another matrix
	     * @return A + B
	     */
	    public IMatrix plus(IMatrix B) ;

	    /**
	     * A = A + B
	     *
	     * @param B another matrix
	     * @return A + B
	     */
	    public IMatrix plusEquals(IMatrix B);

	    /**
	     * C = A - B
	     *
	     * @param B another matrix
	     * @return A - B
	     */
	    public IMatrix minus(IMatrix B);

	    /**
	     * A = A - B
	     *
	     * @param B another matrix
	     * @return A - B
	     */
	    public IMatrix minusEquals(IMatrix B) ;
	    /**
	     * Element-by-element multiplication, C = A.*B
	     *
	     * @param B another matrix
	     * @return A.*B
	     */
	    public IMatrix arrayTimes(IMatrix B) ;

	    /**
	     * Element-by-element multiplication in place, A = A.*B
	     *
	     * @param B another matrix
	     * @return A.*B
	     */
	    public IMatrix arrayTimesEquals(IMatrix B) ;

	    /**
	     * Element-by-element right division, C = A./B
	     *
	     * @param B another matrix
	     * @return A./B
	     */
	    public IMatrix arrayRightDivide(IMatrix B) ;

	    /**
	     * Element-by-element right division in place, A = A./B
	     *
	     * @param B another matrix
	     * @return A./B
	     */
	    public IMatrix arrayRightDivideEquals(IMatrix B);

	    /**
	     * Element-by-element left division, C = A.\B
	     *
	     * @param B another matrix
	     * @return A.\B
	     */
	    public IMatrix arrayLeftDivide(IMatrix B) ;

	    /**
	     * Element-by-element left division in place, A = A.\B
	     *
	     * @param B another matrix
	     * @return A.\B
	     */
	    public IMatrix arrayLeftDivideEquals(IMatrix B);

	    /**
	     * Multiply a matrix by a scalar, C = s*A
	     *
	     * @param s scalar
	     * @return s*A
	     */
	    public IMatrix times(double s) ;

	    /**
	     * Multiply a matrix by a scalar in place, A = s*A
	     *
	     * @param s scalar
	     * @return replace A by s*A
	     */
	    public IMatrix timesEquals(double s);

	    /**
	     * Linear algebraic matrix multiplication, A * B
	     *
	     * @param B another matrix
	     * @return Matrix product, A * B
	     * @exception IllegalArgumentException Matrix inner dimensions must agree.
	     */
	    public IMatrix times(IMatrix B);

	    /**
	     * LU Decomposition
	     *
	     * @return LUDecomposition
	     * @see LUDecomposition
	     */
	    public LUDecomposition lu() ;

	    /**
	     * QR Decomposition
	     *
	     * @return QRDecomposition
	     * @see QRDecomposition
	     */
	    public QRDecomposition qr() ;

	    /**
	     * Cholesky Decomposition
	     *
	     * @return CholeskyDecomposition
	     * @see CholeskyDecomposition
	     */
	    public CholeskyDecomposition chol() ;

	    /**
	     * Singular Value Decomposition
	     *
	     * @return SingularValueDecomposition
	     * @see SingularValueDecomposition
	     */
	    public SingularValueDecomposition svd() ;

	    /**
	     * Eigenvalue Decomposition
	     *
	     * @return EigenvalueDecomposition
	     * @see EigenvalueDecomposition
	     */
	    public EigenvalueDecomposition eig();

	    /**
	     * Solve A*X = B
	     *
	     * @param B right hand side
	     * @return solution if A is square, least squares solution otherwise
	     */
	    public IMatrix solve(IMatrix B);

	    /**
	     * Solve X*A = B, which is also A'*X' = B'
	     *
	     * @param B right hand side
	     * @return solution if A is square, least squares solution otherwise.
	     */
	    public IMatrix solveTranspose(IMatrix B) ;

	    /**
	     * Matrix inverse or pseudoinverse
	     *
	     * @return inverse(A) if A is square, pseudoinverse otherwise.
	     */
	    public IMatrix inverse();

	    /**
	     * Matrix determinant
	     *
	     * @return determinant
	     */
	    public double det();
	    /**
	     * Matrix rank
	     *
	     * @return effective numerical rank, obtained from SVD.
	     */
	    public int rank() ;

	    /**
	     * Matrix condition (2 norm)
	     *
	     * @return ratio of largest to smallest singular value.
	     */
	    public double cond() ;

	    /**
	     * Matrix trace.
	     *
	     * @return sum of the diagonal elements.
	     */
	    public double trace() ;

//	    /**
//	     * Generate matrix with random elements
//	     *
//	     * @param m Number of rows.
//	     * @param n Number of colums.
//	     * @return An m-by-n matrix with uniformly distributed random elements.
//	     */
//	    public static IMatrix random(int m, int n) ;
//	    /**
//	     * Generate identity matrix
//	     *
//	     * @param m Number of rows.
//	     * @param n Number of colums.
//	     * @return An m-by-n matrix with ones on the diagonal and zeros elsewhere.
//	     */
//	    public static IMatrix identity(int m, int n) ;

	    /**
	     * Print the matrix to stdout. Line the elements up in columns with a
	     * Fortran-like 'Fw.d' style format.
	     *
	     * @param w Column width.
	     * @param d Number of digits after the decimal.
	     */
	    public void print(boolean isPrint) ;

	    public void saveFile(String fileName);

	    public void print(int w, int d) ;

	    /**
	     * Print the matrix to the output stream. Line the elements up in columns
	     * with a Fortran-like 'Fw.d' style format.
	     *
	     * @param output Output stream.
	     * @param w Column width.
	     * @param d Number of digits after the decimal.
	     */
	    public void print(PrintWriter output, int w, int d) ;

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
	    public void print(NumberFormat format, int width);

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
	    public void print(PrintWriter output, NumberFormat format, int width);


}
