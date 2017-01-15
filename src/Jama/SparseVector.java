package Jama;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

public class SparseVector implements Serializable{
//	double A[];
//	double AI[];
	
    private final int n;             // length
    
    private final int weigh;             // length
//    private final int nx;
    private static final long serialVersionUID = -6552319171850636836L;
    private transient Node<Integer, Double> st;  // the vector, represented by index-value pairs

    public Node<Integer, Double> getNode() {
        return  st;
    }
    // initialize the all 0s vector of length n
    public SparseVector(int n) {
        this.n  = n;
        this.st = new Node<Integer, Double>();
        this.weigh = 0;
    }

    public SparseVector(double row[]) {
//    	System.out.println("1");
//    	int count = 0;
        this.st = new Node<Integer, Double>();
//        System.out.println("2");
        for(int i = 0; i<row.length; i++)
        {
            if (row[i] == 0.0) st.delete(i);
            else
            	st.put(i, row[i]);
        }
//        System.out.println("4 "+count);
//        	this.put(i, row[i]);
        this.n  = row.length;//count;
        this.weigh = 0;
    }
    
    public SparseVector(List<Double> row) {
//    	System.out.println("1");
//    	int count = 0;
        this.st = new Node<Integer, Double>();
//        System.out.println("2");
        for(int i = 0; i<row.size(); i++)
        {
            if (row.get(i) == 0.0) st.delete(i);
            else
            	st.put(i, row.get(i));
        }
//        System.out.println("4 "+count);
//        	this.put(i, row[i]);
        this.n  = row.size();//count;
        this.weigh = 0;
    }
    public SparseVector(double row[], Boolean rmz) {
//    	System.out.println("1");
//    	int count = 0;
        this.st = new Node<Integer, Double>();
//        System.out.println("2");
        for(int i = 0; i<row.length; i++)
        {
//        	System.out.println("3");
        	if(rmz)
        	{
        		if (row[i] == 0.0) st.delete(i);
	            else
	            	st.put(i, row[i]);
        	}
            else
            	st.put(i, row[i]);
        }
//        System.out.println("4 "+count);
//        	this.put(i, row[i]);
        this.n  = row.length;//count;
        this.weigh = 0;
    }
    // put st[i] = value

    public void put(int i, double value) {
        if (i < 0 || i >= n) throw new RuntimeException("Illegal index");
        DecimalFormat twoDForm = new DecimalFormat("0.00000000");
        value = Double.valueOf(twoDForm.format(value));
        
        if (value == 0.0) st.delete(i);
        else              st.put(i, value);
    }
    
    public void put(int i, double value, Boolean rmz ) {
        if (i < 0 || i >= n) throw new RuntimeException("Illegal index");
        DecimalFormat twoDForm = new DecimalFormat("0.00000000");
        value = Double.valueOf(twoDForm.format(value));
        if(rmz)
        {
	        if (value == 0.0) st.delete(i);
	        else              st.put(i, value);
        }
        else              st.put(i, value);
    }

    // return st[i]
    public double get(int i) {
        if (i < 0 || i >= n) throw new RuntimeException("Illegal index");
        if (st.contains(i)) return st.get(i);
        else                return 0.0;
    }

    public double getMaxABS() {
    	double ret = 0;
    	Iterator<Integer> list = st.iterator();
    	while(list.hasNext())
    	{
    		double c1 = st.get(list.next());
    		Math.abs(c1);
            ret = (c1 > ret)?c1:ret;
    	}
//    	System.out.println("getMaxABS "+ret+"---" +Long.toString(System.currentTimeMillis()));
    	return ret;
    }
    
    
    public double getMin() {
//      if (st.contains(i)) return st.get(i);
//      else                
    	  return 0.0;
  }
    
    public double getSum() {
//        if (i < 0 || i >= n) throw new RuntimeException("Illegal index");
//        if (st.contains(i)) return st.get(i);
    	double ret = 0;
    	   for (Integer integer : st) {
            ret = ret + st.get(integer);
        }
//                (int i = 0; i<n ;i++) {
//    		
//		}
        return ret;
    }

    // return the number of nonzero entries
    public int nnz() {
        return st.size();
    }

    // return the size of the vector
    public int size() {
        return n;
    }

    // return the dot product of this vector with that vector
    public double dot(SparseVector that) {
//        System.out.println(this.n+" that n--"+that.n);
//        System.out.println(this.m+" that n--"+that.n);
        if (this.n != that.n) throw new IllegalArgumentException("Vector lengths disagree");
        double sum = 0.0;

        // iterate over the vector with the fewest nonzeros
        if (this.st.size() <= that.st.size()) {
            for (int i : this.st.keys())
                if (that.st.contains(i)) sum += this.get(i) * that.get(i);
        }
        else  {
            for (int i : that.st.keys())
                if (this.st.contains(i)) sum += this.get(i) * that.get(i);
        }
        return sum;
    }

    public double sum() {
    	double sum = 0.0;
    	for (int i : this.st.keys())
    		sum+=st.get(i);
        return sum;
    }
    // return the 2-norm
    public double norm() {
        return Math.sqrt(this.dot(this));
    }

    public double normF2() {
        return this.dot(this);
    }
    
    // return alpha * this
    public SparseVector scale(double alpha) {
        SparseVector result = new SparseVector(n);
        for (int i : this.st.keys())
        {
            result.put(i, alpha * this.get(i));
        }
        return result;
    }

    // return this + that
    public SparseVector plus(SparseVector that) {
        if (this.n != that.n) throw new IllegalArgumentException("Vector lengths disagree "+this.n +" "+ that.n);
        SparseVector result = this.copy();//new SparseVector(n);
//        for (int i : this.st.keys()) result.put(i, this.get(i));
        for (int i : that.st.keys()) result.put(i, that.get(i) + result.get(i));
        return result;
    }

    // return this * that
    public SparseVector mul(SparseVector that) {
        if (this.n != that.n) throw new IllegalArgumentException("Vector lengths disagree"+this.n +" "+ that.n);
        SparseVector result = this.copy();//new SparseVector(n);
//        for (int i : this.st.keys()) result.put(i, this.get(i));
        for (int i : that.st.keys()) result.put(i, that.get(i) * result.get(i));
        return result;
    }
    
    // return a string representation
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i : st.keys()) {
            s.append("(" + i + ": " + st.get(i) + ") ");
        }
        return s.toString();
    }
    
    public double[] toArrayDouble() {
		double[] ret = new double[n];
		for (int i = 0; i<n; i++)
		{
            if(st.contains(i))
            	ret[i] = st.get(i);
            else
                ret[i] =  0;
//                    System.out.println(i+": "+ret[i]);
		}
//                this.print();
		return ret;
	}
    
    public Double[] toArrayDouble2() {
		Double[] ret = new Double[n];
		for (int i = 0; i<n; i++)
		{
            if(st.contains(i))
            	ret[i] = st.get(i);
            else
                ret[i] =  Double.valueOf(0);// (double) 0;
//                    System.out.println(i+": "+ret[i]);
		}
//                this.print();
		return ret;
	}
    
    public Double[] toArrayDoubleNonZero() {
		Double[] ret = new Double[n];
		for (int i = 0; i<n; i++)
		{
            if(st.contains(i))
            	ret[i] = st.get(i);
            else
                ret[i] =  Double.valueOf(0);// (double) 0;
//                    System.out.println(i+": "+ret[i]);
		}
//                this.print();
		return ret;
	}
    
    public void print(String mes) {
    	if(nnz()>0)
		System.out.println(mes+"  "+this.toString());
	}

    public SparseVector copy()
    {
        SparseVector ret = new SparseVector(n);
        for (int i : st.keys())
        {
                ret.put(i,st.get(i));
        }
        return ret;
    }
    
//    public SparseVector transpose()
//    {
//        SparseVector ret = new SparseVector(n);
//        for (int i : st.keys())
//        {
//                ret.put(i,st.get(i));
//        }
//        return ret;
//    }
    
    public boolean isDepenVector(SparseVector that) {
        if(this.nnz() != that.nnz())
            return false;
        for (Integer index : st) {
            if(that.get(index) == 0.0)
                return false;
        }
        return true;
    }
    public boolean isZeroVector() {
        return (this.nnz()>0)?false:true;
    }
    public SparseMatrix toSpaceMatrix()
    {
    	SparseVector[] arr = new SparseVector[1];
    	arr[0] = this;
    	return new SparseMatrix(arr, 1, this.n);
    	
    }
    
    public static SparseVector E(int size, int j)
    {
    	SparseVector ret = new SparseVector(size);
    	ret.put(j, 1);
    	
    	return ret;
    }
}
