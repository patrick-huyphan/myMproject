package DocTermBuilder;

import java.io.File;
import java.io.IOException;

import Jama.SparseMatrix;
import Jama.SparseVector;
import main.TermList;

public class GraphTool {
	
	public static double compare_doc(SparseVector in1, SparseVector in2) {
		double ret = 0;
		for (int i = 0; i < in1.size(); i++) {
			if (((in1.get(i) != 0) && (in2.get(i) != 0)) || ((in1.get(i) == 0) && (in2.get(i) == 0))
					|| (in1.get(i) == in2.get(i)))
				ret++;
		}
		return ret;
	}
/*
 * form doc-term, build matrix store node and list of most related node of each node, maybe should define the limit of related node
 * Each row is the list of 1 node, each node has index and weigh, index is related node, and weigh is related weigh
 */
	// input doc term matrix
	// output: matrix present doc-doc relationship(0 if not related, else > 0 ).
	// compare 2 vector of 2 doc, if has same term, the relationship will be dif
	// 0, the weigh of relationship is the number of same terms between 2 doc.
	// output matrix will be m*m square matrix. m is the number of doc.
	// V is set of doc
	// E is set of egde between doc.
	// 1 doc has 1 set of V and E, this is the vector in matrix.
	// Algorithm:
	// while(i++ != end_doc)
	// while(j++ ! = = end_doc)
	// value = compare_doc(i,j)
	// putValue2Matrix(i,j,value)
	// putValue2Matrix(j,i,value)
	/*
	 * TODO:
	 * 
	 *  - Build dictionary for some related topic
	 *  - if 2 doc has same term in topic dictionary, it has related  
	 */
	public static SparseMatrix buildSetVEGraph(SparseMatrix matrix) throws IOException {

		int i = 0, end_doc = matrix.getm();
		SparseMatrix retMatrix = new SparseMatrix(end_doc, end_doc);
		String dataDir = "data/topic";
		/*
		 * List all file in data/topic
		 * from file name, init topic dictionary (topic_ID and topic_Name)
		 * for each file, load topic keyword as reference
		 */
	    File folder = new File(dataDir);
	    File[] listOfFiles = folder.listFiles();
	    
//		String listFileName[] = null;
	    // this list store data of term and topic
		TermList topicTerm = new TermList();
		for(File _file : listOfFiles)
		{
			if(!_file.isDirectory())
				topicTerm.ReadData(_file.getPath());
		}
//			topicTerm.getTerm(term)
		// compare doc to get topic related, may be not correct. the purpose of algorithm is detect the semantic? or just apply algorithm to calculate doc ranks and clustering???  	
		while (i != end_doc) {
			retMatrix.put(i, i, 1);
			int j = i + 1;
			while (j != end_doc) {
				double value = compare_doc(matrix.getRow(i), matrix.getRow(i)) / matrix.getn();/// end_doc;

				// System.out.println("i: "+i + " - j: "+j+" "+value);

				retMatrix.put(i, j, value);
				retMatrix.put(j, i, value);
				j++;
			}
			i++;
		}
		return retMatrix;
	}

	// for each node, build related.
	public static SparseMatrix relatedSet(SparseMatrix input)
	{
		SparseMatrix ret = null;
		
		return ret;
	}

	public static SparseMatrix weighSet(SparseMatrix input)
	{
		SparseMatrix ret = null;
		
		return ret;
	}

}
