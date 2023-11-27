package edu.purdue.cs.fast.baselines.ckqst.structures;

import edu.purdue.cs.fast.models.KNNQuery;

import java.util.HashMap;
import java.util.List;

public class OrderedInvertedIndex {
    private HashMap<String, List<Block>> postingLists;
    private double thetaU;

    public OrderedInvertedIndex(double thetaU) {
        this.postingLists = new HashMap<>();
        this.thetaU = thetaU;
    }

    public void insertQueryPL(KNNQuery query) {
        String key = getPLKey(query);
        System.out.println(key);

        // TODO - Create block if null
        // TODO - See if theres a b to put this
        // TODO - Else calc cost and find the best case
    }

    private String getPLKey(KNNQuery query) {
        String key = query.keywords.get(0) + "_";
        if (query.keywords.size() < 2) {
            key += query.keywords.get(0);
        } else {
            key += query.keywords.get(1);
        }
        return key;
    }

    private double calcCostCase2() {
        return 0;
    }

    private double calcCostCase3() {
        return 0;
    }

    private double calcCostCase4() {
        return 0;
    }

    @Override
    public String toString() {
        return "OrderedInvertedIndex{\n" +
                "postingLists=" + postingLists +
                "\n}";
    }
}

//class Index {
//    Map<Integer,String> sources;
//    HashMap<String, HashSet<Integer>> index;
//
//    Index(){
//        sources = new HashMap<Integer,String>();
//        index = new HashMap<String, HashSet<Integer>>();
//    }
//
//    public void buildIndex(String[] files){
//        int i = 0;
//        for(String fileName:files){
//
//
//            try(BufferedReader file = new BufferedReader(new FileReader(fileName)))
//            {
//                sources.put(i,fileName);
//                String ln;
//                while( (ln = file.readLine()) !=null) {
//                    String[] words = ln.split("\\W+");
//                    for(String word:words){
//                        word = word.toLowerCase();
//                        if (!index.containsKey(word))
//                            index.put(word, new HashSet<Integer>());
//                        index.get(word).add(i);
//                    }
//                }
//            } catch (IOException e){
//                System.out.println("File "+fileName+" not found. Skip it");
//            }
//            i++;
//        }
//
//    }
//
//    public void find(String phrase){
//        String[] words = phrase.split("\\W+");
//        HashSet<Integer> res = new HashSet<Integer>(index.get(words[0].toLowerCase()));
//        for(String word: words){
//            res.retainAll(index.get(word));
//        }
//
//        if(res.size()==0) {
//            System.out.println("Not found");
//            return;
//        }
//        System.out.println("Found in: ");
//        for(int num : res){
//            System.out.println("\t"+sources.get(num));
//        }
//    }
//}
//
//public class InvertedIndex {
//
//
//    public static void main(String args[]) throws IOException{
//        Index index = new Index();
//        index.buildIndex(new String[]{"testfile1.txt","testfile2.txt"});
//
//        System.out.println("Print search phrase: ");
//        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//        String phrase = in.readLine();
//
//        index.find(phrase);
//    }
//}
