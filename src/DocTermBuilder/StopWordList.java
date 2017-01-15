package DocTermBuilder;

import java.util.ArrayList;
import java.util.List;

public class StopWordList
{
	List<String> stopWord = new ArrayList<String>();
	StopWordList()
	{
		String [] stopW= {"a","about","above","across","after","afterwards","again","against","all","almost","alone","along","already","also","although","always","am",
				"among","amongst","amoungst","amount","an","and","another","any","anyhow","anyone","anything","anyway","anywhere","are","aren’t","around","as","at","back",
				"be","became","because","become","becomes","becoming","been","before","beforehand","behind","being","below","beside","besides","between","beyond","bill",
				"both","bottom","but","by","call","can","cannot","cant","can’t","co","computer","con","could","couldnt","couldn’t","cry","de","describe","detail","did",
				"didn’t","do","does","doesn’t","doing","done","don’t","down","due","during","each","eg","eight","either","eleven","else","elsewhere","empty","enough",
				"etc","even","ever","every","everyone","everything","everywhere","except","few","fifteen","fify","fill","find","fire","first","five","for","former",
				"formerly","forty","found","four","from","front","full","further","get","give","go","had","hadn’t","has","hasnt","hasn’t","have","haven’t","having",
				"he","he’d","he’ll","hence","her","here","hereafter","hereby","herein","here’s","hereupon","hers","herse”","herself","he’s","him","himse”","himself",
				"his","how","however","how’s","hundred","i","i’d","ie","if","i’ll","i’m","in","inc","indeed","interest","into","is","isn’t","it","its","it’s","itse\\”",
				"itself","i’ve","keep","last","latter","latterly","least","less","let’s","ltd","made","many","may","me","meanwhile","might","mill","mine","more","moreover",
				"most","mostly","move","much","must","mustn’t","my","myse\\”","myself","name","namely","neither","never","nevertheless","next","nine","no","nobody",
				"none","noone","nor","not","nothing","now","nowhere","of","off","often","on","once","one","only","onto","or","other","others","otherwise","ought",
				"our","ours","ourselves","out","over","own","part","per","perhaps","please","put","rather","re","same","see","seem","seemed","seeming","seems","serious",
				"several","shan’t","she","she’d","she’ll","she’s","should","shouldn’t","show","side","since","sincere","six","sixty","so","some","somehow","someone","something",
				"sometime","sometimes","somewhere","still","such","system","take","ten","than","that","that’s","the","their","theirs","them","themselves","then",
				"thence","there","thereafter","thereby","therefore","therein","there’s","thereupon","these","they","they’d","they’ll","they’re","they’ve","thick",
				"thin","third","this","those","though","three","","through","throughout","thru","thus","to","together","too","top","toward","towards","twelve","twenty",
				"two","un","under","until","up","upon","us","very","via","was","wasn’t","we","we’d","well","we’ll","were","we’re","weren’t","we’ve","what","whatever",
				"what’s","when","whence","whenever","when’s","where","whereafter","whereas","whereby","wherein","where’s","whereupon","wherever","whether","which","while",
				"whither","who","whoever","whole","whom","who’s","whose","why","why’s","will","with","within","without","won’t","would","wouldn’t","yet","you","you’d",
				"you’ll","your","you’re","yours","yourself","yourselves","you’ve"};
		
		for (String string : stopW) {
			stopWord.add(string);
		}
	}
}
