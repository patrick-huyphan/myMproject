package DocTermBuilder;

public class Mystemmer {

    public Mystemmer() { 
    }

    public String ReplaceStem(String word) 
    { 
        if(word.toLowerCase().endsWith(".")) 
            return word.replace(word.trim(), word.substring(0, word.length()-1)); 
       else if(word.toLowerCase().endsWith(":")) 
              return word.replace(word.trim(), word.substring(0, word.length()-1)); 
        else if(word.toLowerCase().endsWith(",")) 
              return word.replace(word.trim(), word.substring(0, word.length()-1)); 
        else if(word.toLowerCase().endsWith(":")) 
              return word.replace(word.trim(), word.substring(0, word.length()-1)); 
        else if(word.toLowerCase().endsWith(";")) 
              return word.replace(word.trim(), word.substring(0, word.length()-1)); 
        else if(word.toLowerCase().endsWith("?")) 
              return word.replace(word.trim(), word.substring(0, word.length()-1)); 
         else if(word.toLowerCase().endsWith("’")) 
              return word.replace(word.trim(), word.substring(0, word.length()-1)); 
        else if(word.toLowerCase().endsWith("\"")) 
              return word.replace(word.trim(), word.substring(0, word.length()-1)); 
        else if(word.toLowerCase().endsWith(")")) 
              return word.replace(word.trim(), word.substring(0, word.length()-1)); 
        else if(word.toLowerCase().endsWith("(")) 
              return word.replace(word.trim(), word.substring(0, word.length()-1)); 
        else if(word.toLowerCase().endsWith("]")) 
              return word.replace(word.trim(), word.substring(0, word.length()-1)); 
        else if(word.toLowerCase().endsWith("}")) 
              return word.replace(word.trim(), word.substring(0, word.length()-1)); 
        else if(word.toLowerCase().endsWith("[")) 
              return word.replace(word.trim(), word.substring(0, word.length()-1)); 
        else if(word.toLowerCase().endsWith("{")) 
              return word.replace(word.trim(), word.substring(0, word.length()-1)); 
        else if (word.toLowerCase().startsWith("[")) 
              return word.replace(word.trim(), word.substring(1)); 
        else if (word.toLowerCase().startsWith("(")) 
              return word.replace(word.trim(), word.substring(1)); 
        else if (word.toLowerCase().startsWith("{")) 
              return word.replace(word.trim(), word.substring(1)); 
        else if (word.toLowerCase().startsWith("’")) 
              return word.replace(word.trim(), word.substring(1)); 
        else if (word.toLowerCase().startsWith("\"")) 
              return word.replace(word.trim(), word.substring(1)); 
        else if (word.toLowerCase().startsWith(".")) 
              return word.replace(word.trim(), word.substring(1)); 
        else if (word.toLowerCase().startsWith(":")) 
              return word.replace(word.trim(), word.substring(1)); 
        else if (word.toLowerCase().startsWith(",")) 
              return word.replace(word.trim(), word.substring(1)); 
        else if (word.toLowerCase().startsWith(";")) 
              return word.replace(word.trim(), word.substring(1));

        return word.trim(); 
    }

    public String DoSuffixStremmer(String word) 
    { 
        word = ReplaceStem(word); 
       if(word.toLowerCase().endsWith("ness")) 
               return word.replace(word, word.substring(0, word.length()-3)); 
       else if(word.toLowerCase().endsWith("tion")) 
              return word.replace(word, word.substring(0, word.length()-4)); 
       else if(word.toLowerCase().endsWith("sion")) 
              return word.replace(word, word.substring(0, word.length()-4)); 
       else if(word.toLowerCase().endsWith("iness")) 
              return word.replace(word, word.substring(0, word.length()-5)+"y"); 
       else if(word.toLowerCase().endsWith("er")) 
              return word.replace(word, word.substring(0, word.length()-2)); 
       else if(word.toLowerCase().endsWith("or")) 
              return word.replace(word, word.substring(0, word.length()-2)); 
       else if(word.toLowerCase().endsWith("ily")) 
              return word.replace(word, word.substring(0, word.length()-3)+"y"); 
       else if(word.toLowerCase().endsWith("ily")) 
              return word.replace(word, word.substring(0, word.length()-3)+"y"); 
       else if(word.toLowerCase().endsWith("ist")) 
              return word.replace(word, word.substring(0, word.length()-3)); 
        else if(word.toLowerCase().endsWith("ize")) 
              return word.replace(word, word.substring(0, word.length()-3)); 
        else if(word.toLowerCase().endsWith("en")) 
              return word.replace(word, word.substring(0, word.length()-2)); 
       else if(word.toLowerCase().endsWith("ful")) 
              return word.replace(word, word.substring(0, word.length()-3)); 
       else if(word.toLowerCase().endsWith("full")) 
              return word.replace(word, word.substring(0, word.length()-4)); 
        else if(word.toLowerCase().endsWith("ical")) 
              return word.replace(word, word.substring(0, word.length()-4)); 
       else if(word.toLowerCase().endsWith("ic")) 
              return word.replace(word, word.substring(0, word.length()-2)); 
        else if(word.toLowerCase().endsWith("sses")) 
              return word.replace(word, word.substring(0, word.length()-4)+"ss"); 
        else if(word.toLowerCase().endsWith("ies")) 
              return word.replace(word, word.substring(0, word.length()-3)+"i"); 
        else if(word.toLowerCase().endsWith("ss")) 
              return word.replace(word, word.substring(0, word.length()-2)+"ss"); 
        else if(word.toLowerCase().endsWith("s")) 
              return word.replace(word, word.substring(0, word.length()-1)); 
       else if(word.toLowerCase().endsWith("eed")) 
              return word.replace(word, word.substring(0, word.length()-3)+"ed"); 
       else if(word.toLowerCase().endsWith("ed")) 
              return word.replace(word, word.substring(0, word.length()-2)); 
       else if(word.toLowerCase().endsWith("ing")) 
              return word.replace(word, word.substring(0, word.length()-3)); 
       else if(word.toLowerCase().endsWith("ly")) 
              return word.replace(word, word.substring(0, word.length()-2)); 
       else if(word.toLowerCase().endsWith("es")) 
              return word.replace(word, word.substring(0, word.length()-2));

       else 
           return word; 
    }

}