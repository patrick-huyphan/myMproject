package main;

import Jama.SparseVector;

public class NodeList {

//}
//class NodeList
//{
	int n;
	int size;
	SparseVector[][]  list;
//	double [][] weigh; 
	
	public NodeList() {
		// TODO Auto-generated constructor stub
//		list = new SparseVector[n][n];
	}
	public NodeList(int _n, int _size) {
		n= _n;
		size = _size;
		list = new SparseVector[n][n];
		for(int i = 0; i < n; i++)
		{
			for(int j = 0; j < n; j++)
			{
				list[i][j] = new SparseVector(size);
//				weigh [i][j] = 0;
			}
		}
	}
	
	public int getn() {
		return n;
	}

	public int getSize() {
		return size;
	}
	
	public SparseVector getE(int _n, int _m)
	{
		 return list[_n][_m];
	}
	
	public SparseVector getV(int _n)
	{
		SparseVector ret = new SparseVector(size);
		
		for(int i = 0; i< n; i++)
		{
			ret = ret.plus(list[_n][i]);
		}
		ret.scale(n);
		return ret;//list[_n][_m];
	}
	
	public void setE(int _n, int _m, SparseVector node )
	{
		list[_n][_m] = node;
	}
	
	public void EPrint(String mes)
	{
		System.out.println(mes);
		for(int i = 0; i < n; i++)
		{
			for(int j = 0; j < n; j++)
			{
				if(list[i][j].nnz() > 0)
					list[i][j].print("index: "+i+"-"+ j);
//				weigh [i][j] = 0;
			}
		}
	}
    public String toString() {
        StringBuilder s = new StringBuilder();
		for(int i = 0; i < n; i++)
		{
			for(int j = 0; j < n; j++)
			{
				if(list[i][j].nnz() > 0)
					s.append("((" + i +" " +j+"): " + list[i][j].toString() + ") ");
			}
		}
        return s.toString();
    }
/*
	public double getWeighE(int _n, int _m)
	{
		 return weigh[_n][_m];
	}
	
	public void setWeighE(int _n, int _m, double node )
	{
		weigh[_n][_m] = node;
	}
*/
	
	
}