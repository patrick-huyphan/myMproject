package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import Jama.Node;
//import main.TextData.Topic;

public class TermList {

//}
//class Topic
//{
//	int ID;
//	String topicName;
	Node<String, String> termList;
	
	public TermList() {
		// TODO Auto-generated constructor stub
	}
	public TermList(int _ID, String _topicName) {
		// TODO Auto-generated constructor stub
//		ID = _ID;
//		topicName = _topicName;
		termList = new Node<String, String>();
	}


	
	void addTerm(String term, String value)
	{
		if(termList.get(term).contains(value))
		{
			return;
		}
		termList.put(term, value);
	}
	
	String getTerm(String term)
	{
		String ret = termList.get(term);
		if( ret != null)
		{
			return ret;
		}
		else
			return null;
	}
	
	
	public void ReadData(String fileName) throws IOException {
		// TODO Auto-generated constructor stub
		

/*
* Read file name is topic and store keyword, from file name, decede value to add into list
*/
		String s, temp, key = "a";
		StringTokenizer st;
		
		 BufferedReader br = new BufferedReader(new FileReader(fileName));
		 if(fileName.contains(""))
			 key = "1";
		 else if(fileName.contains(""))
			 key = "2";
		 else if(fileName.contains(""))
			 key = "3";
		 else if(fileName.contains(""))
			 key = "4";
		 else if(fileName.contains(""))
			 key = "5";
		 else if(fileName.contains(""))
			 key = "6";
		 else if(fileName.contains(""))
			 key = "7";
		 
		 while ((s = br.readLine()) != null) {
			 temp = this.getTerm(s);
			 if(!temp.contains(key))
			 {
				 temp = temp+ key;
				 this.addTerm(s, temp);
			 }
			 
             System.out.print("\n");
		 }
	}
	
	
}