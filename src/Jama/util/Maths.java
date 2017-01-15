package Jama.util;

public class Maths {

   /** sqrt(a^2 + b^2) without under/overflow. **/

   public static double hypot(double a, double b) {
      double r;
      if (Math.abs(a) > Math.abs(b)) {
         r = b/a;
         r = Math.abs(a)*Math.sqrt(1+r*r);
      } else if (b != 0) {
         r = a/b;
         r = Math.abs(b)*Math.sqrt(1+r*r);
      } else {
         r = 0.0;
      }
      return r;
   }
   
   /*
	 * @functionName:
	 * 
	 * @input param:
	 * 
	 * @output param:
     */
    public static double[] vecMul(double[] V1, double V2) {
        double ret[] = new double[V1.length];
        for (int i = 0; i < V1.length; i++) {
            ret[i] = V1[i] * V2;
//            System.out.println("vecMul "+ i +": "+V1[i] + " = "+ret[i]);
        }
        return ret;
    }

    /*
 	 * @functionName:
 	 * 
 	 * @input param:
 	 * 
 	 * @output param:
      */
     public static double[] vecDev(double[] V1, double V2) {
         double ret[] = new double[V1.length];
         for (int i = 0; i < V1.length; i++) {
             ret[i] = V1[i] / V2;
//             System.out.println("vecMul "+ i +": "+V1[i] + " = "+ret[i]);
         }
         return ret;
     }
       /*
	 * @functionName:
	 * 
	 * @input param:
	 * 
	 * @output param:
     */
    public static double[] vecSubtract(double[] V1, double[] V2) {
        double ret[] = new double[V1.length];
        for (int i = 0; i < V1.length; i++) {
            ret[i] = V1[i] - V2[i];
        }
        return ret;
    }
       /*
	 * @functionName:
	 * 
	 * @input param:
	 * 
	 * @output param:
     */
    public static double[] vecAdd(double[] V1, double[] V2) {
        double ret[] = new double[V1.length];
        for (int i = 0; i < V1.length; i++) {
            ret[i] = V1[i] + V2[i];
//            System.out.println("add "+ i +": "+V1[i] + " + "+V2[i]+" = "+ret[i]);
        }
        return ret;
    }
    /*
 	 * @functionName:
 	 * 
 	 * @input param:
 	 * 
 	 * @output param:
      */
     public static double[] vecNorm1(double[] V) {
         double ret[] = new double[V.length];
         double tmp = Math.sqrt(vecValue(V,V));
         for (int i = 0; i < V.length; i++) {
             ret[i] = V[i]/tmp;
         }
         return ret;
     }
    
    /*
	 * @functionName:
	 * 
	 * @input param:
	 * 
	 * @output param:
     */
    public static double vecValue(double[] V1, double[] V2) {
        if (V1.length != V2.length) {
            return 0.0;
        }
        double ret = 0.0;
        for (int i = 0; i < V1.length; i++) {
            ret += V1[i] * V2[i];
//            System.out.println("add "+ i +": "+V1[i] + " "+V2[i]+" = "+ret);
        }
//        System.out.println("vecValue "+ret);
        return ret;
    }
    
    public static boolean isVerterZero(double V[])
    {
        boolean ret = false;
        for (int i = 0; i< V.length; i++)
            if (V[i]!=0)
                return ret;
        return true;
    }
    
    public static boolean isSameVerter(double V1[], double V2[])
    {
        boolean ret = false;
        for (int i = 0; i< V1.length; i++)
            if (((V1[i]==0) && (V2[i]!=0))||((V2[i]==0) && (V1[i]!=0)))
                return ret;
        return true;
    }

    public static boolean isSameVerter2(double V1[], double V2[])
    {
        boolean ret = false;
        for (int i = 0; i< V1.length; i++)
            if (V1[i] != V2[i])
                return ret;
        return true;
    }
    public static void printArrayD(String name,double V[])
    {
    	String out = "Array "+ name+": ";
    	for (double d : V) {
			out+= d+"\t";
		}
    	System.out.println(out);
    }
}
