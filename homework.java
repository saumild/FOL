import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
class KB{
    //literals,clauses,mapping
    
}
class Predicate implements Comparable<Predicate>{
    //predicatename,number of args, list of args
    String predicate;
    int numOfArgs;
    List<String> listOfArgs; // <sit,var> or <John,const>
    public String getPredicate() {
        return predicate;
    }
    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }
    public int getNumOfArgs() {
        return numOfArgs;
    }
    public void setNumOfArgs(int numOfArgs) {
        this.numOfArgs = numOfArgs;
    }
    public List<String> getListOfArgs() {
        return listOfArgs;
    }
    public void setListOfArgs(List<String> listOfArgs) {
        this.listOfArgs = listOfArgs;
    }
    @Override
    public String toString() {
        return "Predicate [listOfArgs=" + listOfArgs + ", numOfArgs=" + numOfArgs + ", predicate=" + predicate + "]";
    }
    @Override
    public int compareTo(Predicate o) {
        // TODO Auto-generated method stub
        return this.predicate.compareTo(o.predicate);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Predicate) {
            Predicate p = (Predicate) o;
            return Objects.equals(p.predicate, this.predicate);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(predicate);
    }
    
}
public class homework {
    static HashSet<Predicate> predicateSet = new HashSet<>();
    //static HashSet<Clause> clauses = new HashSet<>();
    static HashMap<String,List<String>> predicateClauseMap = new HashMap<>();
    public static int counterInfinite = 0;
    public static void main(String[] args){
        File input_file = new File("input.txt");
        try {
            Scanner sc = new Scanner(input_file);
            int num_queries = sc.nextInt();
            sc.nextLine();
            //System.out.println(num_queries);
            ArrayList<String> negatedQueries = new ArrayList<>();
            //HashMap<> KB = new HashMap<>();
            for(int i = 0; i<num_queries;i++){
                negatedQueries.add(negateQuery(sc.nextLine()));
                //System.out.println(negatedQueries.size());
            }
            int originalkbsize = sc.nextInt();
            sc.nextLine();
            for(int i = 0; i< originalkbsize;i++){
                String sent = sc.nextLine().replaceAll(" ", "");
                //System.out.println(sent.replaceAll(" ", ""));
                convertToCNF(sent);
            }
            // for(Map.Entry map: predicateClauseMap.entrySet()){
            //     System.out.println(map.getKey() + " ===== " + map.getValue());
            // }
            //System.out.println(predicateClauseMap);
            //Run Resolution on query;
            ArrayList<String> answer = new ArrayList<>();
            for(int i = 0;i<negatedQueries.size();i++){
                Stack<String> queryStack = new Stack<>();
                queryStack.push(negatedQueries.get(i));
                findPredicatesAndPopulateKB(negatedQueries.get(i));
                Resolver resolver = new Resolver(predicateClauseMap, predicateSet);
                boolean result = resolver.dfs_function(queryStack,counterInfinite);
                ArrayList<String> predClauseList = (ArrayList<String>) predicateClauseMap.get(negatedQueries.get(i).split("\\(")[0]);
                if(predClauseList != null && predClauseList.size()>1){
                    predClauseList.remove(negatedQueries.get(i));
                }else{
                    predicateClauseMap.remove(negatedQueries.get(i).split("\\(")[0]);
                }
                if(result == true){ 
                    //findPredicatesAndPopulateKB(negateQuery(negatedQueries.get(i)));
                    answer.add("TRUE");
                }else{
                    answer.add("FALSE");
                    //predicateClauseMap.remove()
                }
            }
            //write output
            // for(String ans: answer)
            //     System.out.println(ans);
            sc.close(); 
            
            //write output
            FileWriter fw = new FileWriter("output.txt");
            PrintWriter pw = new PrintWriter(fw);

            for(int i=0;i<answer.size();i++){
                pw.println(answer.get(i));
            }

            fw.close();
        } catch (Exception e) {
            //TODO: handle exception
            Logger.getLogger(homework.class.getName()).log(Level.SEVERE, null, e);
        }
        
    }

    private static void convertToCNF(String sentence) {
        //DO 3 steps and then call add to KB
        int numOfPreds = findPredicatesCount(sentence);
        if(sentence.contains("=")){
            sentence = findAndReplaceAllImplications(sentence,numOfPreds);
            //System.out.println(sentence);
            if(numOfPreds>2)
                sentence = moveNegationInwards(sentence);
            else{
                sentence = sentence.replaceAll("~~", "");
            }
        }
        //System.out.println(sentence);
        //distribureAndOverOr()
        //splitStatementsOnAndOperator()
        findPredicatesAndPopulateKB(sentence);
        
    }

    private static String moveNegationInwards(String string){
        StringBuilder str = new StringBuilder();
            
        if(string.contains("~")){
            string = string.replaceAll("~\\(", "");
            string = string.replaceAll("\\)\\|", "\\|");
            string = string.replaceAll("~~", "");
            String[] terms = null;
            
            if(string.contains("&")){
                terms = string.split("&");
                for(int i = 0; i<terms.length;i++){
                    terms[i]= "~" + terms[i];
                    //System.out.println(terms[i]);
                    str.append(terms[i]);
                    if(i != terms.length-1)
                        str.append("|");
                }
            }else{
                str.append(string);
            }
            string = str.toString().replaceAll("~~", "");
            //System.out.println(str.substring(0,str.length()-1));
        }
        //System.out.println(str.toString());
        return string;
    }
    
    private static String findAndReplaceAllImplications(String string,int numOfPreds) {
        for ( int i = 0 ; i < string.length(); i++){
			if (string.charAt(i) == '=' && string.charAt(i+1) == '>'){
				int right = 0, left = 0;
				boolean foundRight = false;
				boolean foundLeft = false;
				int rightIndex = -1,leftIndex = -1;
				for ( int j = 1; i-j >= 0; j++){
					// Moving to the right
					if (!foundRight){
						if (string.charAt(i-j) == '(' ){
							if (right != 0){
								right--;
							} else {
								foundRight = true;
								rightIndex = i-j+1;
								break;
							}
						}
						if (string.charAt(i-j) == ')' ){
							right++;
						}
					}
				}
				
				for(int j = 0;  i+j < string.length();j++){
					// Moving to the left
					if (!foundLeft){
						if (string.charAt(i+j) == ')' ){
							if (left != 0){
								left--;
							} else {
								foundLeft = true;
								leftIndex = i+j;
								break;
							}
						}
						if (string.charAt(i+j) == '(' ){
							left++;
						}
					}
				}
				
				if (rightIndex == -1)
					rightIndex = 0;
				if (leftIndex == -1)
					leftIndex = string.length();
				string = replaceImplication(string,rightIndex,leftIndex,i,numOfPreds);
			}
		}
        //string = "(" + string.substring(0, bracketRight-1) + ")" +string.substring(bracketRight,string.length());
		//System.out.println(string);
		return string;
	}
	
	private static String replaceImplication(String string, int rightIndex, int leftIndex, int center,int numOfPreds) {
		String temp =string;
		StringBuilder builder = new StringBuilder(temp.substring(0,rightIndex));
		string = string.substring(rightIndex,leftIndex);
		String rightToken = "";
		//System.out.println(numOfPreds);
        if(numOfPreds>2){
    		rightToken = "~("+string.substring(0, center-rightIndex).trim() +")";
        }else{
            rightToken = "~"+string.substring(0, center-rightIndex).trim();
        }
		String leftToken = string.substring(center-rightIndex+2);
		string = rightToken+"|"+leftToken;
		
		builder.append(string);
		builder.append(temp.substring(leftIndex));
		
		return builder.toString();
	}

    private static int findPredicatesCount(String sentence){
        String[] predicateList = sentence.replaceAll("=>", "&").split("[&|]");
        return predicateList.length;
    }
    private static void findPredicatesAndPopulateKB(String sentence){
        Predicate pred = new Predicate();
        String[] predicateList = sentence.split("[&\\|]");  

        for(int i = 0; i<predicateList.length;i++){
            //System.out.println(predicateList[i]);
            String singlePred = predicateList[i].replace(")", "");
            //int j = 0;
            //StringBuilder s = new StringBuilder();
            String[] splitOnArgs = singlePred.split("\\(");
            pred.predicate = splitOnArgs[0];
            String[] args = splitOnArgs[1].split(",");
            pred.numOfArgs = args.length;
            pred.listOfArgs = new ArrayList<>();
            for(int j = 0;j<args.length;j++){
                if(Character.isUpperCase(args[j].charAt(0))){
                    pred.listOfArgs.add(args[j] + "c");
                }else if(Character.isLowerCase(args[j].charAt(0))){
                    pred.listOfArgs.add(args[j] + "v");
                }
            }
            //System.out.println(pred);    
            predicateSet.add(pred);
            if(predicateClauseMap!= null && predicateClauseMap.get(pred.predicate)!=null){
                List<String> ls = predicateClauseMap.get(pred.predicate);
                //System.out.println(ls.get(0));
                ls.add(sentence);
                predicateClauseMap.put(pred.predicate,ls);
            }else{
                List<String> ls = new ArrayList<>();
                ls.add(sentence);
                predicateClauseMap.put(pred.predicate,ls);
            }

        }
        //return predicateList.length;
    }

    private static String negateQuery(String query) {
        if(query.contains("~"))
            return query.substring(1);
        else
            return "~"+query;
    }
}

class Resolver{
    private HashMap<String,List<String>> KB = null;
    private HashSet<Predicate> predicate = null;
    HashSet<String> visited = new HashSet<>();
    private static boolean flag = false;
    public Resolver(HashMap<String, List<String>> predicateClauseMap, HashSet<Predicate> predicateSet){
        this.KB=predicateClauseMap;
        this.predicate=predicateSet;
    }

    boolean dfs_function(Stack<String> queryStack, int counterInfinite){
        //System.out.println(counterInfinite);
        if(flag){
            return false;
        }
        while(!queryStack.isEmpty()){
            String first = queryStack.pop();
            String toFind = negate(first);
            String[] splitForPred = toFind.split("\\(");
            String predicate = splitForPred[0];
            String[] argument1 = splitForPred[1].replaceAll("\\)", "").split(",");
            
            if(KB.containsKey(predicate)){
                ArrayList<String> clausesForLookup = (ArrayList<String>) KB.get(predicate);
                for(int i = 0;i<clausesForLookup.size(); i++){
                    
                    String clause = clausesForLookup.get(i);
                    ArrayList<String> ored = new ArrayList<>();
                    String[] splitOnDisjunction = clause.split("\\|");
                    if(visited.contains(clause)){
                        continue;
                    }
                    if(counterInfinite>1000){
                        flag = true;
                        return false;
                    }
                    String match = "";

                    for(String x: splitOnDisjunction){
                        ored.add(x);
                        if(x.contains(predicate)){
                            match = x;
                        }
                    }

                    String[] splitOnMatched = match.split("\\(");
                    String[] argumentOfMatchedPredicate = splitOnMatched[1].replaceAll("\\)", "").split(",");

                    boolean unificationResult = unification(argument1,argumentOfMatchedPredicate);

                    if(unificationResult){
                        HashMap<String,String> unifyMap = new HashMap<>();
                        for(int h=0;h<argument1.length;h++){
                            String stackargs = argument1[h];
                            String kbargs = argumentOfMatchedPredicate[h];
                            if(!unifyMap.containsKey(kbargs)){
                                unifyMap.put(kbargs, stackargs);
                            }
                        }

                        //Stack<String> copyStack = (Stack<String>) queryStack.clone();
                        ArrayList<String> stackArrayList = new ArrayList<>(queryStack);

                        for(int m = 0; m<ored.size();m++){
                            String currKbElement = ored.get(m);
                            Iterator iter = unifyMap.entrySet().iterator();
                            while(iter.hasNext()){
                                Map.Entry pair = (Map.Entry)iter.next();
                                String[] splitCurrElement = currKbElement.replaceAll("\\)", "").split("\\(");
                                String[] splitOnArgs = splitCurrElement[1].split(",");
                                currKbElement = splitCurrElement[0] + "(";
                                for(int args = 0;args < splitOnArgs.length;args++){
                                    if(splitOnArgs[args].equals((String)pair.getKey()))
                                        splitOnArgs[args]=(String)pair.getValue();
                                    currKbElement = currKbElement + splitOnArgs[args];
                                    if(args != splitOnArgs.length-1){
                                        currKbElement = currKbElement + ",";
                                    }
                                }
                                currKbElement = currKbElement + ")";
                                 
                            }
                            String checking = currKbElement.split("\\(")[0];

                            if(!checking.equals(predicate)){
                                String original = currKbElement;
                                String temp = negate(original);
                                int count  = 0;
                                for (Iterator<String> iterator = stackArrayList.iterator(); iterator.hasNext();) {
                                    String string = iterator.next();
                                    if (string.equals(temp)) {
                                        // Remove the current element from the iterator and the list.
                                        iterator.remove();
                                        count=1;
                                    }
                                }
                                if(count!=1)
                                    stackArrayList.add(original);
                            }

                        }
                        
                        Stack<String> finalstack=new Stack<String>();
                        for(String z:stackArrayList)
                            finalstack.push(z);
                        //  System.out.println("counter is "+counterInfinite);
                        boolean printing=dfs_function(finalstack,++counterInfinite);

                        if(printing)
                            return true;
                    }
                    visited.add(clause);
                    if(i == clausesForLookup.size()-1)
                        return false;    
                }
                visited.clear();
            }else{
                return false;
            }
        }
        return true;
    }

    
    boolean unification(String a[],String b[]){
        int counter=0;
        for(int i=0;i<a.length;i++){
            String x=a[i]; //stack
            String y=b[i];   //kbstring

            if(x.charAt(0)>='a'&& x.charAt(0)<='z' && y.charAt(0)>='a' && y.charAt(0)<='z'){
                counter++;
            }else if(x.charAt(0)>='a'&& x.charAt(0)<='z' && y.charAt(0)>='A' && y.charAt(0)<='Z'){
                counter++;
            }else if(x.charAt(0)>='A'&& x.charAt(0)<='Z' && y.charAt(0)>='a' && y.charAt(0)<='z'){
                counter++;
            }else if(x.equals(y)){
                counter++;
            }
        }
        if(counter==a.length)
            return true;
        else
            return false;
    }

    String negate(String query){
        if(query.contains("~"))
            return query.substring(1);
        else
            return "~"+query;
    }
}
