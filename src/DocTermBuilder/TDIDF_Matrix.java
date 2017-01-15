package DocTermBuilder;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Arrays;

public class TDIDF_Matrix extends ReadingMultipleFile {

    DecimalFormat twoDForm = new DecimalFormat("0.00000");

    @Override
    public void compute(int x, int y) throws IOException {

        System.out.println("compute TDIDF_Matrix");
        // initializing the tdidf
        for (int i = 0; i < listOfFiles.length; i++) {
            for (int j = 0; j < keywordList.size(); j++) {
                tdidf[i][j] = 0.00000;
            }

        }
        FileWriter fw0 = new FileWriter("tdidf.txt");
        // ReadingMultipleFile re = new ReadingMultipleFile();
        for (int i = 0; i < listOfFiles.length; i++) {
            for (int j = 0; j < keywordList.size(); j++) {

                fw0.write(countMatrix[i][j] + " ");
//				tdidf[i][j] = (((countMatrix[i][j] * 10000) / 1 + tottal_no_words_in_doc[i])/10000)* (Math.log(50 / num_of_doc_in_which_word_i_appears[j]));
                if (countMatrix[i][j] > 0) {
//					System.out.println("i: "+i+" j: "+j);
//					System.out.println("countMatrix[i][j]: "+countMatrix[i][j]);
//					System.out.println("tottal_no_words_in_doc[i] "+ tottal_no_words_in_doc[i]);
//					System.out.println("num_of_doc_in_which_word_i_appears[j]: "+num_of_doc_in_which_word_i_appears[j]);

//					double tmp = (Double.valueOf(twoDForm.format((countMatrix[i][j] * 10000) / (1 + tottal_no_words_in_doc[i]))).doubleValue() / 10000)
//							* (Math.log(50 / num_of_doc_in_which_word_i_appears[j]));
                    double tmp = (Double.valueOf(twoDForm.format((countMatrix[i][j] * 10000) / (1 + tottal_no_words_in_doc[i]))).doubleValue() / 10000)
                            * (Math.log(num_of_doc_in_which_word_i_appears.length / num_of_doc_in_which_word_i_appears[j]));
                    tdidf[i][j] = Double.valueOf(twoDForm.format(tmp)).doubleValue();
                } else {
                    tdidf[i][j] = 0;
                }
            } // for closing
            fw0.write("\n");
        } // for closing
        fw0.close();

        System.out.println("");
        System.out.println(" ************** TFIDF Matrix **************");
        System.out.println("");
//		PrintWriter pr = new PrintWriter("TDM.txt");
        FileWriter fw = new FileWriter("TDM.txt");
        for (int i = 0; i < listOfFiles.length; i++) {
            for (int j = 0; j < keywordList.size(); j++) {
//				System.out.print(tdidf[i][j] + "  ,  ");
//				pr.println("" + tdidf[i][j]);  
                fw.write(tdidf[i][j] + "\t");
            }
//			pr.println("\n");
            fw.write("\n");
//			System.out.println("");
        }
        fw.close();
//		pr.close();
        // computeSVD();
    }

    public void computeSVD() {
        System.out.println("");
        System.out.println(" ************** TFIDF Matrix **************");
        System.out.println("");
        for (int i = 0; i < listOfFiles.length; i++) {
            for (int j = 0; j < keywordList.size(); j++) {
                System.out.print(tdidf[i][j] + "  ,  ");
            }
            System.out.println("");
        }
    }

}
