package edu.purdue.cs.fast.baselines.ckqst.structures;

import edu.purdue.cs.fast.baselines.ckqst.CkQST;
import edu.purdue.cs.fast.baselines.ckqst.models.CkQuery;
import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.Query;

import java.util.*;

public class OrderedInvertedIndex {
    public final HashMap<String, Integer> keywords;
    public final HashMap<String, Double> probWV;
    private final HashMap<String, List<Block>> postingLists;
    public int countQueries;

    public OrderedInvertedIndex() {
        this.postingLists = new HashMap<>();

        keywords = new HashMap<>();
        probWV = new HashMap<>();
    }

    public void insertQueryPL(CkQuery query) {
        countQueries++;
        String key = getPLKey(query.keywords);

        if (!this.postingLists.containsKey(key)) {
            int i = 0;
            List<Block> blockList = new LinkedList<>();

            if (query.keywords.size() > 2) { // Line 1
                i = 1;

                Block b = new Block();
                blockList.add(b);

                String w3 = query.keywords.get(2);
                keywords.put(w3, keywords.getOrDefault(w3, 0) + 1);
                probWV.put(w3, (double) keywords.get(w3) / countQueries);
            }

            // Line 2: construct new block b
            Block b = new Block(query);
            blockList.add(b);
            this.postingLists.put(key, blockList);
            return;
        }

        if (query.id == 6) {
            System.out.println("DEBUG!");
        }
        List<Block> bList = this.postingLists.get(key);
        int br = getMinBlock(query, bList); // Line 3

        if (query.keywords.size() <= 2) {
            if (br == -1) {
                bList.add(0, new Block(query));
            } else
                bList.get(br).add(0, query);
            return;
        }

        String w3 = query.keywords.get(2);

        // Update probs
        keywords.put(w3, keywords.getOrDefault(w3, 0) + 1);
        probWV.put(w3, (double) keywords.get(w3) / countQueries);

        if (br != -1 && bList.get(br).minw.equals(w3)) { // Line 4: q.w[3] == br.minw
            bList.get(br).add(query);
            return;
        }

        if (br > 1 && bList.get(br - 1).maxw.compareTo(w3) >= 0) { // Line 5,6
            bList.get(br - 1).add(query);
            return;
        }

        // WARNING!: Addition to algorithm to make Case 2 consistent with definition.
        if (br == -1 && bList.size() == 2 && bList.get(1).maxw.compareTo(w3) >= 0) { // Line 5,6
            bList.get(1).add(query);
            return;
        }

        int choice = getChoice(w3, br, bList);

        if (choice == 2) {
            if (br == -1)
                br = bList.size();
            Block b = bList.get(br - 1);
            b.add(query);
            b.maxw = w3;
        } else if (choice == 3) {
            Block b = bList.get(br);
            b.add(0, query);
            b.minw = w3;
        } else if (choice == 4) {
            Block b = new Block(query);
            b.minw = w3;
            b.maxw = w3;

            int addTo = br;
            if (addTo == -1)
                addTo = bList.size();
            bList.add(addTo, b);
        }
    }

    private int getChoice(String w3, int i, List<Block> bList) {
        int choice = -1;
        if (i == -1) { // Line 7: br == null
            double cCase2 = calcCostCase2(w3, bList.get(bList.size() - 1), bList.size());
            double cCase4 = calcCostCase4(w3, bList, bList.size());

            if (cCase2 < cCase4)
                choice = 2;
            else
                choice = 4;
        } else if (i == 1) { // Line 8: r == 1
            double cCase3 = calcCostCase3(w3, bList.get(i), bList.size());
            double cCase4 = calcCostCase4(w3, bList, bList.size());

            if (cCase3 < cCase4)
                choice = 3;
            else
                choice = 4;
        } else if (i > 1) { // Line 9: r > 1
            double cCase2 = calcCostCase2(w3, bList.get(i), bList.size());
            double cCase3 = calcCostCase3(w3, bList.get(i), bList.size());
            double cCase4 = calcCostCase4(w3, bList, bList.size());

            if (cCase2 <= cCase3 && cCase2 <= cCase4) {
                choice = 2;
            } else if (cCase3 <= cCase2 && cCase3 <= cCase4) {
                choice = 3;
            } else {
                choice = 4;
            }
        }
        return choice;
    }

    public double verifyProb(CkQuery query) {
        String key = getPLKey(query.keywords);

        if (!this.postingLists.containsKey(key)) {
            if (query.keywords.size() <= 2)
                return probWV.getOrDefault(query.keywords.get(query.keywords.size() - 1), 0.0);
            return probWV.getOrDefault(query.keywords.get(2), 0.0);
        }

        List<Block> bList = this.postingLists.get(key);
        int br = getMinBlock(query, bList); // Line 3

        if (query.keywords.size() <= 2) {
            if (br == -1) {
                return probWV.getOrDefault(query.keywords.get(query.keywords.size() - 1), 0.0);
            } else
                return bList.get(br).probBVbr(probWV);
        }

        if (br == -1) {
            return probWV.getOrDefault(query.keywords.get(2), 0.0);
        } else {
            int bMaxW = bList.get(br).getKeywords().size();
            int w_i3 = bList.get(br).getKeywords().indexOf(query.keywords.get(2));
            return bList.get(br).probBVbr(probWV) * (bMaxW - w_i3 + 1) / bMaxW;
        }
    }

    public double estVerifyCost(CkQuery query) {
        String key = getPLKey(query.keywords);

        if (!this.postingLists.containsKey(key)) {
            return 1; // TODO - ambiguous
        }

        List<Block> bList = this.postingLists.get(key);
        int br = getMinBlock(query, bList); // Line 3

        if (br == -1) {
            return 1; // TODO - ambiguous
        } else {
            int bMaxW = bList.get(br).getKeywords().size();
            int w_i3 = bList.get(br).getKeywords().indexOf(query.keywords.get(2));

            double sum = 0;
            for (String wj : bList.get(br).getKeywords()) {
                sum += probWV.get(wj) * (bMaxW - w_i3);
            }
            return sum;
        }
    }

    public double updateCost(CkQuery query) {
        String key = getPLKey(query.keywords);

        if (!this.postingLists.containsKey(key)) {
            return 0; // o ops to create a new block with the new query
        }

        List<Block> bList = this.postingLists.get(key);
        int br = getMinBlock(query, bList); // Line 3

        if (br == -1) {
            return Math.log(bList.size()); // log(B) to find the the min block
        } else {
            return Math.log(bList.size()) * (bList.get(br).size() + 1) / 2;
        }
    }

    private int getMinBlock(CkQuery query, List<Block> blockList) {
        if (query.keywords.size() <= 2 && !blockList.isEmpty()) {
            return 0;
        }

        if (blockList.size() == 1)
            return -1;

        int i = 1;
        for (Block b : blockList.subList(1, blockList.size())) {
            if (b.minw.compareTo(query.keywords.get(2)) >= 0) { // Line 3: b.minw >= q.w[3]
                return i;
            }
            i++;
        }

        return -1;
    }

    private String getPLKey(List<String> keywords) {
        String key = keywords.get(0) + "_";
        if (keywords.size() < 2) {
            key += keywords.get(0);
        } else {
            key += keywords.get(1);
        }
        return key;
    }

    private double calcCostCase2(String w3, Block br, int numB) {
        double C_PL_V = (br.probBVbr_c(probWV, w3) - br.probBVbr(probWV)) * (Math.log(numB) + br.size()) + br.probBVbr_c(probWV, w3);
        return C_PL_V + CkQST.thetaU * 1; // CPLu = O(1)
    }

    private double calcCostCase3(String w3, Block br, int numB) {
        double C_PL_V = (br.probBVbr_c(probWV, w3) - br.probBVbr(probWV)) * (Math.log(numB) + br.size()) + br.probBVbr_c(probWV, w3);
        return C_PL_V + CkQST.thetaU * 1; //CPLu = O(1)
    }

    private double calcCostCase4(String w3, List<Block> brs, int numB) {
        double sum_p_B_V_br = 0;
        for (Block br : brs) {
            sum_p_B_V_br += br.probBVbr(probWV);
        }

        double sum_C_B_V_br = Math.log((float) (numB + 1) / numB) * sum_p_B_V_br;

        double C_B_V_b = probWV.get(w3) * Math.log(numB + 1);

        double C_PL_V = sum_C_B_V_br + C_B_V_b;
        return C_PL_V + CkQST.thetaU * (1 + Math.log(numB + 1)); // CPLu = O(1 + log |B|)
    }

    public void searchObject(DataObject obj, Collection<Query> results) {
        // ASSUMPTION: Paper doesn't include details on find the PL. We are doing an exhaustive search.
        for (int i = 0; i < obj.keywords.size(); i++) {
            String keyword = obj.keywords.get(i);
            String oneKey = keyword + "_" + keyword;
            searchOneKey(oneKey, obj, results);

            for (int j = i + 1; j < obj.keywords.size(); j++) {
                String key = keyword + "_" + obj.keywords.get(j);
                searchTwoKey(key, j, obj, results);
            }
        }
    }

    private void searchOneKey(String oneKey, DataObject obj, Collection<Query> results) {
        if (postingLists.containsKey(oneKey)) {
            List<Block> oneKeyBlockList = postingLists.get(oneKey);

            if (!oneKeyBlockList.isEmpty()) {
                for (CkQuery query : oneKeyBlockList.get(0).getQueries()) {
                    if (obj.id == 73 && query.id == 81039 && oneKey.equals("contractor_contractor")) {
                        System.out.println("DEBUG it");
                        Block  b = oneKeyBlockList.get(0);
                    }
                    if (query.containsPoint(obj.location)) {
                        if (!results.contains(query)) {
                            results.add(query);
                            query.updateSR(obj);
                        }
                    }
                }
            }
        }
    }

    private void searchTwoKey(String key, int idxJ, DataObject obj, Collection<Query> results) {
        if (postingLists.containsKey(key)) {
            List<Block> blockList = postingLists.get(key);
            if (obj.keywords.size() == 2 && !blockList.isEmpty()) {
                for (CkQuery query : blockList.get(0).getQueries()) {
                    if (query.containsPoint(obj.location)) {
                        if (!results.contains(query)) {
                            results.add(query);
                            query.updateSR(obj);
                        }
                    }
                }
                return;
            }

            if (blockList.isEmpty())
                return;

            for (Block b : blockList) {
                for (int j = idxJ; j < obj.keywords.size(); j++) {
//                    if (obj.id == 14 && key.equals("imaging_magnetic")) {
//                        System.out.println("DEBUG it");
//                        if (b.minw != null) {
//                            int diff = b.minw.compareTo(obj.keywords.get(j));
//                            System.out.println(diff);
//                        }
//                    }
                    if (b.minw == null || b.minw.compareTo(obj.keywords.get(j)) <= 0) {
                        for (CkQuery query : b.getQueries()) {
                            boolean match = query.keywords.size() <= obj.keywords.size();

                            if (match && query.keywords.size() > 2 && !new HashSet<>(obj.keywords).containsAll(query.keywords)) {
                                match = false;
                            }

                            if (match && query.containsPoint(obj.location)) {
                                if (!results.contains(query)) {
                                    results.add(query);
                                    query.updateSR(obj);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean remove(CkQuery query) {
        throw new RuntimeException("Not implemented!");
    }

    private String plToString() {
        StringBuilder s = new StringBuilder();
        for (Map.Entry<String, List<Block>> entry : postingLists.entrySet()) {
            s.append(entry.getKey()).append(" -> ");
            int i = 0;
            for (Block b : entry.getValue()) {
                s.append(i).append("-[").append(b.minw).append(", ").append(b.maxw).append("]:{");
                for (CkQuery q : b.getQueries()) {
                    s.append(q.id).append(", ");
                }
                s.append("}, ");
                i++;
            }
            s.append("\n");
        }
        return s.toString();
    }

    @Override
    public String toString() {
        return "OrderedInvertedIndex:\n" +
                "PostingLists:\n" +
                plToString();
    }
}
