package main;

import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Struct;
import java.util.StringTokenizer;

import Jama.Node;
import Jama.SparseMatrix;

/*
 * TODO:
 * From post, create doc, clean data, clean stopwword, replace emotision
 * Read toppic list data.
 */
		

public class TextData {

	public enum topicName 
		{
		ADD,
		B,
		C,
		D,
		E,
		};
	/*
	 * Toppic ID, word id
	 */
	
		
		
	public void loadDic(String file)
	{
		String list [][];
		
		
		TermList listTop = new TermList();
		
		/*
		 * Read file, add to list term
		 */
//		ReadData(listTop, file);
		
	}
	
	/*
	 * TODO
	 */
	public static SparseMatrix readESet(String file)
	{
		SparseMatrix ret = null;
		return ret;
		
	}

}
