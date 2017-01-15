/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.FileNotFoundException;
import java.io.IOException;

import DocTermBuilder.ReadingMultipleFile;
import DocTermBuilder.loadRVC;
import Jama.Matrix;
import Jama.SparseMatrix;
import Jama.SparseVector;
import main.ReadData.DATA_ID;
/**
 *
 * @author patrick_huy
 */
public class JavaApplication1 {

    /**
     * @param args the command line arguments
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // TODO code application logic here
//        Process master;
//        ProcessBuilder masterBuilder;
        double matrix[][] = 
        
       {{1,	0,	0,	1,	0,	0,	0,	0,	0},
        {1,	0,	1,	0,	0,	0,	0,	0,	0},
        {1,	1,	0,	0,	0,	0,	0,	0,	0},
        {0,	1,	1,	0,	1,	0,	0,	0,	0},
        {0,	1,	1,	2,	0,	0,	0,	0,	0},
        {0,	1,	0,	0,	1,	0,	0,	0,	0},
        {0,	1,	0,	0,	1,	0,	0,	0,	0},
        {0,	0,	1,	1,	0,	0,	0,	0,	0},
        {0,	1,	0,	0,	0,	0,	0,	0,	1},
        {0,	0,	0,	0,	0,	1,	1,	1,	0},
        {0,	0,	0,	0,	0,	0,	1,	1,	1},
        {1,	3,	0,	1,	2,	0,	1,	1,	1},
        {0,	0,	1,	9,	0,	5,	0,	0,	0},
        {0,	1,	0,	0,	0,	0,	0,	0,	1},
        {0,	0,	0,	0,	4,	1,	1,	1,	0},
        {0,	0,	0,	0,	0,	0,	1,	0,	1},
        {1,	3,	0,	1,	2,	0,	1,	1,	1},
        {0,	0,	0,	0,	0,	0,	0,	1,	1}};

        double matrix2[][] =
        {
        	{1,	0,	0,	1,	2,	0,	4,	0,	1,	1,	0,	2,	1}
        };
        
     // the final column of each dataset is b vector,
     // number of rows: n, number of column: m, 
     //Dataset1: n=13, m=4.
     double Dataset1[][] =
     {{7,	26,	6,	60},
     {1,	29,	15,	52},
     {11,	56,	8,	20},
     {11,	31,	8,	47},
     {7,	52,	6,	33},
     {11,	55,	9,	22},
     {3,	71,	17,	6},
     {1,	31,	22,	44},
     {2,	54,	18,	22},
     {21,	47,	4,	26},
     {1,	40,	23,	34},
     {11,	66,	9,	12},
     {10,	68,	8,	12}};
     double Dataset1b[][] = {{78.5, 74.3, 104.3, 87.6, 95.9, 109.2, 102.7, 72.5, 93.1, 115.9, 83.3, 113.3, 109.4}};


     //Dataset2:  n=14, m=8
     double Dataset2[][] =
     {{8,	6.5,	7.7,	26,	16,	43.0,	6,	60},
     {2,	8.0,	6.7,	29,	22,	40.5,	15,	52},
     {12,	9.5,	11.0,	31,	20,	39.0,	8,	47},
     {7,	6.5,	7.0,	52,	29,	42.5,	6,	33},
     {12,	10.0,	11.0,	55,	32,	38.5,	9,	22},
     {4,	10.0,	8.7,	71,	44,	38.5,	17,	6},
     {1,	11.5,	8.3,	31,	27,	37.5,	22,	44},
     {2,	10.0,	7.7,	54,	36,	38.0,	18,	22},
     {12,	11.3,	11.8,	51,	31,	37.3,	11,	24},
     {22,	12.5,	16.0,	47,	26,	36.5,	4,	26},
     {2,	12.0,	9.0,	40,	32,	37.0,	23,	34},
     {7,	11.0,	9.8,	53,	35,	38.0,	16,	23},
     {11,	10.0,	10.7,	66,	38,	39.0,	9,	12},
     {11,	9.0,	10.0,	68,	38,	40.0,	8,	12}};
     double Dataset2b[][] = {{81.5, 76.3, 90.6, 98.9, 111.2, 104.7, 74.5, 95.1, 106.5, 117.9, 85.3, 100.8, 116.3, 112.4}};
        

        Matrix x = new Matrix(matrix);
        
        Matrix A = new Matrix(matrix2);
//        sX.print();
//        sX.getColumn(2).print();
        
//        sX = sX.transpose();

//        sX = sX.sortRow();
//        sX.print();
//      
        
        
//        x.print(true);
//        Matrix x2 = x.echolonConvert();
//        Matrix X = Matrix.
//        x2.print(true);
        
//        Matrix x2 = x.baseMatrix();//matrixRemoveRow(3);
//        x2.print(true);
//        double norm1 = x.norm1();
//        System.out.println("norm1: "+norm1);
//        Matrix x3 = x2.transpose();
//        x3.print(true);
//        Matrix x4 = new Matrix(new double[][]{{1.0,0.0,0.0},{1.0,1.0,0.0},{1.0,1.0,1.0},{1.0,1.0,1.0}}).trucGiao();
//        x3.gramShmidt().print(true);
//        Matrix x5 = x3.normX();
//        x5.print(true);
//        x3.trucGiao().print(true);
//        x5 = x5.orthorlogyMat();
//        x5.print(true);
//        x4.print(true);
        
//        ReadingMultipleFile.calc();
        
//        housingA.print(true, "Housing");
        
//        zooA.print(true, "Zoo");
        
        
        
        SparseMatrix sX = new SparseMatrix(new Matrix(matrix));
//        sX = sX.echolonConvert();
//        sX.print();

//        sX = sX.orthorNorm();
//        sX.print("sX");
        
//        SparseMatrix sX2 = new SparseMatrix(new Matrix(matrix2));
//        sX2 = sX2.echolonConvert();
        
//        SparseMatrix X = SparseMatrix.Algorithm1(sX.transpose(), 1.0E-4, 1.0E-6);
//        X.print(true,"X");
        int testRun = 3;
        SparseMatrix sA = null;
        SparseMatrix sB = null;
        switch (testRun)
	    {
	        case 0:
	        {
	        	sA = new SparseMatrix(new Matrix(Dataset1)).transpose();
		        sB = new SparseMatrix(new Matrix(Dataset1b));
		        break;
	        }
	        
	        case 1:
	        {
	        	sA = new SparseMatrix(new Matrix(Dataset2)).transpose();
		        sB = new SparseMatrix(new Matrix(Dataset2b));
		        break;
	        }
	        case 2:
	        {
	        	SparseMatrix RCVA = ReadData.readData(DATA_ID.RCV).getMatrix(0, 800, 0, 600).scale(1.0E3).transpose();
	        	sA = RCVA;
		        sB = RCVA.getRow(0).toSpaceMatrix();
		        break;
	        }
	        case 3:
	        {
	        	SparseMatrix housingA = ReadData.readData(DATA_ID.HOUSING).transpose();
	        	sA = housingA;
		        sB = housingA.getRow(0).toSpaceMatrix();
		        break;
	        }
	        case 4:
	        {
	        	SparseMatrix zooA = ReadData.readData(DATA_ID.ZOO).transpose();
	        	sA = zooA;
		        sB = zooA.getRow(0).toSpaceMatrix();
		        break;
	        }
	    }
      //A is U, b is dest
//        sA.transpose().print(true, "SA");
//        sB.transpose().print(true, "SB");
        
//        SparseVector Y = SparseMatrix.ADMM(sA.transpose(),sB.transpose(), 0.06, 2.0, 6.0E-2,1.0E-3);
        
        SparseVector Y = RankingMatrix.ADMM_1(sA.transpose(),sB.transpose(), 0.06, 2.0, 6.0E-2,1.0E-3);
        
        Y.print("X");
        
        /*
        ReadingMultipleFile xx = new ReadingMultipleFile();
        xx.calc(1);
        Matrix x6 = new Matrix(xx.tdidf);//.baseMatrix();
        x6.saveFile("baseMatrixX6");
        sX = new SparseMatrix(x6);
//        sX.save("SparseMatrix");
//        sX = sX.sortRow();
//        sX.save("SparseMatrixSorted");
        sX = sX.transpose();
//        sX.save("SparseMatrixTrans");
        sX = sX.sortRow();
//        sX.save("SparseMatrixTransSorted");
        sX = sX.getMatrix(0,800, 0, 300);
        sX.save("SparseMatrixTransSorted10050");
        sX = sX.mullAll(100);
        sX.print();
        sX = sX.echolonConvert();
        sX.save("SparseMatrixEcholonForm");
        SparseMatrix.Algorithm1(sX.getMatrix(0, 1, 0, 0).transpose(), sX, 0.01);

*/
//        sX.getMatrix(0, 100, 6, 150).save("SparseMatrixTrans100");
//        
//        x6.print(false);
//        x6.baseMatrix();
//        x2.print();
//        System.out.println(x.getArray().toString());
        
        System.out.println("DONE");
    }
    
}
