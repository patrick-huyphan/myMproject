package DocTermBuilder;

//public class T {
//}
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import com.google.common.io.Files;

import Jama.Matrix;
import Jama.SparseMatrix;

/**
 *
 * @author shakthydoss
 */
public class ReadingMultipleFile {

    public static List keywordList = new ArrayList();
    public static int[][] countMatrix;
    public static int[][] EMatrix;
    public static double[][] tdidf; //static 
    static String path = "/home/hduser/workspace/Java_prj/20160728/data/20news-18828/alt.atheism";
//  static String path = "Corpus2"; 
    public static File folder = new File(path);
    public static File[] listOfFiles = folder.listFiles();
    public static int[] tottal_no_words_in_doc;
    public static int[] num_of_doc_in_which_word_i_appears;
    
    static String pathTopic = "/home/hduser/workspace/Java_prj/20160728/data/20news-18828/alt.atheism";
    public static File folderTopic = new File(pathTopic);
    public static File[] listOfFilesTopic = folderTopic.listFiles();
    /**
     * @param args the command line arguments
     */
//    public static void main(String[] args) 
    public ReadingMultipleFile() {
        tdidf = new double[listOfFiles.length][keywordList.size()];
        tottal_no_words_in_doc = new int[listOfFiles.length];
        num_of_doc_in_which_word_i_appears = new int[keywordList.size()];
    }

    public ReadingMultipleFile(String input) {
        path = input;
        tdidf = new double[listOfFiles.length][keywordList.size()];
        tottal_no_words_in_doc = new int[listOfFiles.length];
        num_of_doc_in_which_word_i_appears = new int[keywordList.size()];
    }

    public static void calc(int mathType) throws FileNotFoundException, IOException {

        String s, temp;
        StringTokenizer st;
        int countKeyword = 0;
        int [][] topicListTerrm;
        int [][] docTopicListTerrm;
        Mystemmer stem = new Mystemmer();
        //  List keywordList = new  ArrayList();

        StopWordList swl = new StopWordList();

        BufferedWriter bw = new BufferedWriter(new FileWriter("keywordsList.txt"));
// Create list of word, should save create matrix of work, to reduce read file 
        if (listOfFiles.length > 0) {
            for (int i = 0; i < listOfFiles.length ; i++) {
                if (listOfFiles[i].isFile()) {
                    String p1 = listOfFiles[i].getName();
                    //  System.out.println("["+i+"] " + p1); 
                    BufferedReader br = new BufferedReader(new FileReader(listOfFiles[i].getPath()));

                    while ((s = br.readLine()) != null) {
                        s = s.replace(">", "").replace(".", "").replace("!", "").replace("*", "").replace("?", "").replace("^", "").replace("<", "");
                        s = s.replace("{", "").replace("}", "").replace("(", "").replace(")", "").replace("\"", "");
                        s = s.replace("#", "").replace("~", "").replace("=", "").replace("+", "").replace("_", "");
                        s = s.replace("/", "").replace(";", "").replace(":", "").replace(",", "").replace(".", "");
                        s = s.replace("0", "").replace("1", "").replace("2", "").replace("3", "").replace("4", "").replace("5", "");
                        s = s.replace("6", "").replace("7", "").replace("8", "").replace("9", "").replace(">", "").replace("|", "");
//                    	s = s.replaceAll("[.,<>:;()?@#$!~%&*-+={}\\]/\\'\"^]", "");
                        st = new StringTokenizer(s, " ", false);
                        while (st.hasMoreTokens()) {
                            temp = st.nextToken();
                            if (swl.stopWord.contains(temp)) {
                                if (st.hasMoreTokens()) {
                                    st.nextToken();
                                }
                                //System.out.println(temp); 
                            } else if (temp.length() <= 3 || temp.length() >= 15) {
                                if (st.hasMoreTokens()) {
                                    st.nextToken();
                                }
                            } else {
                                temp = stem.DoSuffixStremmer(temp);
                                // put the stemmer here 
                                if (keywordList.contains(temp) == false) //checking in keyword_array 
                                {
                                    temp = temp.replace(" ", "").replace("-", "").replace("'", "");
                                    keywordList.add(temp); // adding keyword to keyword_array 
                                    bw.write(temp);
                                    countKeyword++;
                                    bw.newLine();
                                }
                            }
                        } // while ends 
                    } // while ends 
                }
            }
            
            for (int i = 0; i < listOfFilesTopic.length ; i++) {
                if (listOfFilesTopic[i].isFile()) {
                    String p1 = listOfFilesTopic[i].getName();
                    //  System.out.println("["+i+"] " + p1); 
                    BufferedReader br = new BufferedReader(new FileReader(listOfFilesTopic[i].getPath()));

                    while ((s = br.readLine()) != null) {
//                        s = s.replace(">", "").replace(".", "").replace("!", "").replace("*", "").replace("?", "").replace("^", "").replace("<", "");
//                        s = s.replace("{", "").replace("}", "").replace("(", "").replace(")", "").replace("\"", "");
//                        s = s.replace("#", "").replace("~", "").replace("=", "").replace("+", "").replace("_", "");
//                        s = s.replace("/", "").replace(";", "").replace(":", "").replace(",", "").replace(".", "");
//                        s = s.replace("0", "").replace("1", "").replace("2", "").replace("3", "").replace("4", "").replace("5", "");
//                        s = s.replace("6", "").replace("7", "").replace("8", "").replace("9", "").replace(">", "").replace("|", "");
//                    	s = s.replaceAll("[.,<>:;()?@#$!~%&*-+={}\\]/\\'\"^]", "");
                        st = new StringTokenizer(s, " ", false);
                        while (st.hasMoreTokens()) {
                            temp = st.nextToken();
                            if (swl.stopWord.contains(temp)) {
                                if (st.hasMoreTokens()) {
                                    st.nextToken();
                                }
                                //System.out.println(temp); 
                            } else if (temp.length() <= 3 || temp.length() >= 15) {
                                if (st.hasMoreTokens()) {
                                    st.nextToken();
                                }
                            } else {
                                temp = stem.DoSuffixStremmer(temp);
                                // put the stemmer here 
                                if (keywordList.contains(temp) == false) //checking in keyword_array 
                                {
                                    temp = temp.replace(" ", "").replace("-", "").replace("'", "");
                                    keywordList.add(temp); // adding keyword to keyword_array 
                                    bw.write(temp);
                                    countKeyword++;
                                    bw.newLine();
                                }
                            }
                        } // while ends 
                    } // while ends 
                }
            }
            bw.close();
        }
        else
        	return;
        
        System.out.println("");
        System.out.println("No of Documents – " + listOfFiles.length);
        System.out.println("No of keywords – " + countKeyword);
        System.out.println("");
        System.out.println("");

        countMatrix = new int[listOfFiles.length][keywordList.size()];

//         Arrays.fill(countMatrix, 0);
        for (int i = 0; i < listOfFiles.length; i++) {
            for (int j = 0; j < keywordList.size(); j++) {
                countMatrix[i][j] = 0;
            }
        }



        // Create matrix doc - term.
        if (listOfFiles.length > 0) {
            for (int i = 0; i < listOfFiles.length ; i++) {
                if (listOfFiles[i].isFile()) {
                    String p1 = listOfFiles[i].getName();
                    //System.out.println("["+i+"] " + p1); 
                    BufferedReader br = new BufferedReader(new FileReader(listOfFiles[i].getPath()));
                    while ((s = br.readLine()) != null) {
                        st = new StringTokenizer(s, " ", false);
                        while (st.hasMoreTokens()) {
                            temp = st.nextToken();
                            if (swl.stopWord.contains(temp)) {
                                if (st.hasMoreTokens()) {
                                    st.nextToken();
                                }
                                //System.out.println(temp); 
                            } else if (temp.length() <= 3 || temp.length() >= 15) {
                                if (st.hasMoreTokens()) {
                                    st.nextToken();
                                }
                            } else {
                                // put stemmer here 
                                temp = stem.DoSuffixStremmer(temp);
                                if (keywordList.contains(temp) == true) // checking the keyword in keyword_array 
                                {
                                    //generating count matrix 
                                    countMatrix[i][keywordList.indexOf(temp)] = countMatrix[i][keywordList.indexOf(temp)] + 1;
                                }
                            }

                        } // while ends 
                    } // while ends 
                }
            }
//            bw.close();
            // System.out.println("no of keywords – "+ii); 
        }

        System.out.println("************************** Count Matrix *************************");
        System.out.println("");
        

        
        /*
         * TODO: add code to build matrix E, inlcude relationship bw 2 doc, 2 doc has relationship if it has the same keyword in some topic
         * - read keyword list of topic
         * - Compare doc with list keyword, calc weigh of doc in topic.
         * - each doc is a vector of topic weigh. doc relationship is the max value of (doca + docb)
         * - Note: list of key should add all term in topic
         */
        topicListTerrm = new int[listOfFilesTopic.length][keywordList.size()];
        for (int i = 0; i < listOfFilesTopic.length; i++) {
            for (int j = 0; j < keywordList.size(); j++) {
            	topicListTerrm[i][j] = 0;
            }
          }
        for (int i = 0; i < listOfFilesTopic.length ; i++) {
            if (listOfFilesTopic[i].isFile()) {
                String p1 = listOfFilesTopic[i].getName();
                //  System.out.println("["+i+"] " + p1); 
                BufferedReader br = new BufferedReader(new FileReader(listOfFilesTopic[i].getPath()));

                while ((s = br.readLine()) != null) {
                    st = new StringTokenizer(s, " ", false);
                    while (st.hasMoreTokens()) {
                        temp = st.nextToken();
                        if (swl.stopWord.contains(temp)) {
                            if (st.hasMoreTokens()) {
                                st.nextToken();
                            }
                            //System.out.println(temp); 
                        } else if (temp.length() <= 3 || temp.length() >= 15) {
                            if (st.hasMoreTokens()) {
                                st.nextToken();
                            }
                        } else {
                            temp = stem.DoSuffixStremmer(temp);
                            if (keywordList.contains(temp) == false) //checking in keyword_array 
                            {
                            	topicListTerrm[i][keywordList.indexOf(temp)] = topicListTerrm[i][keywordList.indexOf(temp)] + 1;
                            }
                        }
                    } // while ends 
                } // while ends 
            }
        }
        
        //compare doc with topic list, set to vector of topic for doc
        docTopicListTerrm = new int[listOfFiles.length][listOfFilesTopic.length];
        for (int i = 0; i < listOfFiles.length; i++) {
            for (int j = 0; j < listOfFilesTopic.length; j++) {
            	int weigh = 0;
            	for(int k = 0; k < keywordList.size(); k++)
            	{
            		if(keywordList.contains(topicListTerrm[j][k]))
            		{
            			weigh ++;
            		}
            	}
            	docTopicListTerrm[i][j] = weigh;
            }
        }
        
        // weigh = max of topic weigh
        EMatrix = new int[listOfFiles.length][listOfFiles.length];
        
        for (int i = 0; i < listOfFiles.length; i++) {
            for (int j = 0; j < listOfFiles.length; j++) {
            	int weigh = 0;
            	for(int k = 0; k<listOfFilesTopic.length; k++)
            	{
            		weigh = (docTopicListTerrm[i][k]+docTopicListTerrm[j][k]>weigh)? docTopicListTerrm[i][k]+docTopicListTerrm[j][k]:weigh; 
            	}
                EMatrix[i][j] = weigh;
            }
          }
        

//          for (int i = 0; i < listOfFiles.length; i++) { 
//            for (int j = 0; j < keywordList.size(); j++) { 
//                System.out.print(","+countMatrix[i][j]); 
//            } 
//             System.out.println(" "); 
//        } 
        
        /*
         * TODO: Save matrix to file, read matrix from file
         */
        ReadingMultipleFile tM = null;
        switch (mathType) {
            case 0:
            tM = new TDIDF_Matrix(); 
            tM.compute_tottal_no_words_in_doc(); 
            tM.compute_num_of_doc_in_which_word_i_appears(); 
            tM.compute(listOfFiles.length, keywordList.size());
            saveToFile("tfidf.txt", tM.tdidf);
            break;
                
            case 1:
            tM = new LogMatrix(); 
            tM.compute_tottal_no_words_in_doc(); 
            tM.compute_num_of_doc_in_which_word_i_appears(); 
            tM.compute(listOfFiles.length, keywordList.size());
            saveToFile("tfidf.txt", tM.tdidf);
            break;
            
            default:
                break;
        }
//        SparseMatrix sX = new SparseMatrix(new Matrix(tdidf));
//        sX.save("baseMatrixX6");
//          
    }// main closing 

    public void compute_tottal_no_words_in_doc() {
        int sum = 0;
        for (int i = 0; i < listOfFiles.length; i++) {
            for (int j = 0; j < keywordList.size(); j++) {
                if ((countMatrix[i][j]) > 0) {
                    sum = sum + 1;
                }
            }
            tottal_no_words_in_doc[i] = sum;
            sum = 0;
        }

//		for (int i = 0; i < listOfFiles.length; i++) {
//			System.out.println("Total no of words in document : " + i + " –> " + tottal_no_words_in_doc[i]);
//		}
    }

    public void compute_num_of_doc_in_which_word_i_appears() {
        int sum = 0;
        for (int i = 0; i < keywordList.size(); i++) {
            for (int j = 0; j < listOfFiles.length; j++) {
                if ((countMatrix[j][i]) > 0) {
                    sum = sum + 1;
                }
            }
            num_of_doc_in_which_word_i_appears[i] = sum;
            sum = 0;
        }

//		for (int i = 0; i < keywordList.size(); i++) {
//			System.out.println("word : " + i + " occured in " + num_of_doc_in_which_word_i_appears[i] + " documents ");
//		}
    }
    public void compute(int x, int y)throws IOException 
    {
    	
    };
    
    /*
     * Save data:
     * first line = size
     * 
     */

    
    public static void saveToFile(String fileName, double [][] data) throws IOException 
    {
		File file = new File(fileName);

		String saveData;
		saveData = data.length+ " " + data[0].length +"\n";
		for(int i =0; i< data.length; i++)
		{
			for(int j =0; j< data[i].length; j++)
			{
				saveData += data[i][j] + " ";
			}
			saveData += "\n";
		}
		try (FileOutputStream fop = new FileOutputStream(file)) {

			// if file doesn't exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			// get the content in bytes
			byte[] contentInBytes = saveData.getBytes();

			fop.write(contentInBytes);
			fop.flush();
			fop.close();

			System.out.println("save Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
    };
    
    public static double [][] readFromFile(String fileName) throws IOException 
    {
        String s, temp;
        StringTokenizer st;
    	BufferedReader br = new BufferedReader(new FileReader(new File(fileName).getPath()));
    	double [][] data = null;
    	s = br.readLine();
    	if( s != null)
    	{
    		data = new double[Integer.parseInt(s.split(" ")[0])][Integer.parseInt(s.split(" ")[1])];
    	}    	
    	int i = 0;
    	while ((s = br.readLine()) != null) {
    		int j = 0;
            st = new StringTokenizer(s, " ", false);
            while (st.hasMoreTokens()) {
                temp = st.nextToken();
                data[i][j] = Double.parseDouble(temp);

            } // while ends 
        } // while ends

        return data;
    };
} // class closing

