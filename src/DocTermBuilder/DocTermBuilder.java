package DocTermBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
//import java.util.Scanner;

import Jama.Matrix;

public class DocTermBuilder {
	public Matrix docTerm;
	
	public DocTermBuilder(String input) throws IOException {
		// TODO Auto-generated constructor stub
		//read doc, term load into matrix
		File inputFile = new File(input);
		if(inputFile.isDirectory())
		{
//			readInDirectory(inputFile);
			ReadingMultipleFile.calc(0);
		}
		else
		{
			readFile(inputFile);
		}
	}
	
	public void readInDirectory(File input) throws IOException
	{
//		Scanner scan = new Scanner(System.in);
		File[] listOfFiles = input.listFiles();
//		BufferedReader br = null;
//        String words[] = null;
//        String line;
//        String files;
//        Map<String, Integer> wordCount = new HashMap<String, Integer>();     //Creates an Hash Map for storing the words and its count
        for (File tmp: listOfFiles) {
        	readFile(tmp);	
		}
        
	}
	public void readFile(File input) throws IOException
	{
//		Scanner scan = new Scanner(System.in);
		BufferedReader br = null;
        String words[] = null;
        String line;
//        String files;
        Map<String, Integer> wordCount = new HashMap<String, Integer>();     //Creates an Hash Map for storing the words and its count
        
        br = new BufferedReader(new FileReader(input));      //creates an Buffered Reader to read the contents of the file
        while ((line = br.readLine()) != null) {
            line = line.toLowerCase();
            words = line.split("\\s+");                      //Splits the words with "space" as an delimeter 
        }
        br.close();
        for (String read : words) {
            Integer freq = wordCount.get(read);
            wordCount.put(read, (freq == null) ? 1 : freq + 1); //For Each word the count will be incremented in the Hashmap
        }
	}
}
