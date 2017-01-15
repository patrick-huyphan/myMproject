package DocTermBuilder;

//import java.awt.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import Jama.SparseMatrix;
import Jama.SparseVector;
import scala.reflect.internal.Trees.Return;

/*
 * - From matrix data, export to CSV file
 * - Read CSV file with spark
 */
public class CSVFile {
	
    private static final char DEFAULT_SEPARATOR = ',';

    //https://tools.ietf.org/html/rfc4180
    static String followCVSformat(String value) {

        String result = value;
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"");
        }
        return result;

    }

    static void writeLine(Writer w, List<String> values, char separators, char customQuote) throws IOException {

        boolean first = true;

        //default customQuote is empty

        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (!first) {
                sb.append(separators);
            }
            if (customQuote == ' ') {
                sb.append(followCVSformat(value));
            } else {
                sb.append(customQuote).append(followCVSformat(value)).append(customQuote);
            }

            first = false;
        }
        sb.append("\n");
        w.append(sb.toString());


    }
    
    static void writeLine(Writer w, List<String> values) throws IOException {
        writeLine(w, values, DEFAULT_SEPARATOR, ' ');
    }

    static void writeLine(Writer w, List<String> values, char separators) throws IOException {
        writeLine(w, values, separators, ' ');
    }
    
    
	public static void saveMatrixData(String csvFile, SparseMatrix data, String name) throws IOException
	{
        FileWriter writer = new FileWriter(csvFile);
        
        writeLine(writer, Arrays.asList(name, Integer.toString(data.getm()) , Integer.toString(data.getn())),':');
        
        for(int i =0; i<data.getm(); i++)
        {
        	for(int j =0; j<data.getn(); j++)
        		if(data.get(i, j) != 0)
        			writeLine(writer, Arrays.asList(name, Integer.toString(i), Integer.toString(j), Double.toString(data.get(i, j))),':');
        }
        System.out.println("SAVE DATA TO FILE---DONE---"+ data.getm()+"-"+ data.getn());
        writer.flush();
        writer.close();
	}
	
	public static void saveVectorData(String csvFile, SparseVector data, String name) throws IOException
	{
        FileWriter writer = new FileWriter(csvFile);

        int k = 0;
        writeLine(writer, Arrays.asList(name, Integer.toString(data.size())),':');
    	for(int j =0; j<data.size(); j++)
    		if(data.get(j) != 0)
    			writeLine(writer, Arrays.asList(name, Integer.toString(j) , Double.toString(data.get(j))),':');
    	System.out.println("SAVE DATA FROM FILE---DONE---"+ data.size());
        writer.flush();
        writer.close();
	}
	
	public static SparseMatrix readMatrixData(String csvFile)
	{
        String line = "";
        String cvsSplitBy = ":";
        SparseMatrix ret = null;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

        	line = br.readLine();
        	String[] sizeData = line.split(cvsSplitBy);
        	int m = Integer.parseInt(sizeData[1]);
        	int n = Integer.parseInt(sizeData[2]);
        	ret = new SparseMatrix(m, n);
        	System.out.println("READ DATA FROM FILE m: "+m+" n: "+n);
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] data = line.split(cvsSplitBy);
                ret.put(Integer.parseInt(data[1]), Integer.parseInt(data[2]), Double.parseDouble(data[3]));	
               // System.out.println("Country [code= " + country[4] + " , name=" + country[5] + "]");

            }
            return ret;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;

	}
	
	public static SparseVector readVectorData(String csvFile)
	{
        String line = "";
        String cvsSplitBy = ":";

        SparseVector ret = null;
         
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
        	
        	line = br.readLine();
        	String[] sizeData = line.split(cvsSplitBy);
        	int n = Integer.parseInt(sizeData[1]);
        	System.out.println("READ DATA FROM FILE n: "+n);
        	ret = new SparseVector(n);
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] data = line.split(cvsSplitBy);
                ret.put(Integer.parseInt(data[1]), Double.parseDouble(data[2]));
               // System.out.println("Country [code= " + country[4] + " , name=" + country[5] + "]");

            }

            return ret;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;

	}

}
