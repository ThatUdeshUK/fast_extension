package edu.purdue.cs.fast.experiments;

import com.google.common.base.Stopwatch;
import edu.purdue.cs.fast.FAST;
import edu.purdue.cs.fast.Run;
import edu.purdue.cs.fast.SpatialKeywordIndex;
import edu.purdue.cs.fast.baselines.ckqst.CkQST;
import edu.purdue.cs.fast.baselines.fast.LFAST;
import edu.purdue.cs.fast.baselines.ckqst.AdoptCkQST;
import edu.purdue.cs.fast.baselines.ckqst.structures.IQuadTree;
import edu.purdue.cs.fast.exceptions.InvalidOutputFile;
import edu.purdue.cs.fast.helper.ObjectSizeCalculator;
import edu.purdue.cs.fast.helper.SpatialHelper;
import edu.purdue.cs.fast.models.*;
import edu.purdue.cs.fast.structures.KeywordFrequency;
import me.tongfei.progressbar.ProgressBar;
//import org.openjdk.jol.info.GraphLayout;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class Experiment<T> {
    public List<DataObject> preObjects;
    protected String name;
    protected String outputPath;
    protected SpatialKeywordIndex index;
    protected List<Query> queries;
    protected List<Query> preQueries;
    protected List<DataObject> objects;
    protected long creationTime;
    protected long objIdxSearchTime;
    protected long indexingTime;
    protected long searchTime;
    protected Metadata<Long> createMem = new Metadata<Long>();
    protected Metadata<Long> searchMem = new Metadata<Long>();
    protected boolean saveStats = true;
    protected boolean saveTimeline = false;
    protected boolean saveOutput = false;
    protected int seed = 7;
    protected ArrayList<Collection<Query>> results;
    protected List<Integer> searchTimeline = new LinkedList<>();
    protected List<Integer> createTimeline = new LinkedList<>();

    abstract void init();

    protected abstract void generateQueries(List<T> list);

    protected abstract void generateObjects(List<T> list);

    protected abstract Metadata<String> generateMetadata();

    abstract void run();

    public void preloadObjects() {
        if (preObjects == null)
            return;

        for (DataObject o : preObjects) {
            index.preloadObject(o);
        }
    }

    public void preloadQueries() {
        if (preQueries == null)
            return;

        for (Query q : preQueries) {
            index.preloadQuery(q);
        }
    }

    public void removeInf(Rectangle range, int leafCap, int treeHeight) {
        IQuadTree quadTree = new IQuadTree(range.min.x, range.min.y, range.max.x, range.max.y, leafCap, treeHeight);
        for (DataObject object : preObjects) {
            quadTree.insert(object);
        }

        ArrayList<Query> newList = new ArrayList<>();
        int count = 0;
        for (Query query: queries) {
            PriorityQueue<DataObject> objResults = (PriorityQueue<DataObject>) quadTree.search(query);

            double ar = Double.MAX_VALUE;

            if (objResults.size() >= ((KNNQuery) query).k) {
                DataObject o = objResults.peek();
                assert o != null;
                ar = SpatialHelper.getDistanceInBetween(((KNNQuery) query).location, o.location);
//                System.out.println("Non. inf. Q: " + query.id + ", ar: " + ar);
            }

            if (ar < range.max.x - range.min.x) {
                newList.add(query);
            }
//            else {
//                System.out.println("Inf Q: " + query.id + ", ar: " + ar);
//            }
        }
        System.out.println("Old: " + queries.size() + ", New: " + newList.size() +
                ", Count: " + (queries.size() - newList.size()));
        queries = newList;
    }

    public void calcCreateMem() {
        String javaVersion = System.getProperty("java.version");
        String javaVendor = System.getProperty("java.vendor");
        System.out.println("JDK: " + javaVendor + " - " + javaVersion);
        if (javaVersion.contains("1.8") && javaVendor.contains("OpenJDK")) {
            createMem.add("object_mem", new Long(ObjectSizeCalculator.getObjectSize(objects)));
            createMem.add("query_mem", new Long(ObjectSizeCalculator.getObjectSize(queries)));
            Run.logger.debug("Objects size =" + createMem.get("object_mem") / 1024 + " KB");
            Run.logger.debug("Queries size =" + createMem.get("query_mem") / 1024 + " KB");
            if (index instanceof FAST) {
                FAST i = (FAST) index;

                createMem.add("query_idx_mem", new Long(ObjectSizeCalculator.getObjectSize(i.index)));                            // used in the original
                createMem.add("query_struct_mem", new Long(ObjectSizeCalculator.getObjectSize(i.index)));                   // only the `pyramid` hashmap
                createMem.add("query_keymap_mem", new Long(ObjectSizeCalculator.getObjectSize(FAST.keywordFrequencyMap)));  // only the keyword freq. map
                createMem.add("object_idx_mem", new Long(ObjectSizeCalculator.getObjectSize(i.objIndex)));

                Run.logger.debug("Obj index size =" + createMem.get("object_idx_mem") / 1024 + " KB");
                Run.logger.debug("Query index size =" + createMem.get("query_idx_mem") / 1024 + " KB");
                Run.logger.debug("Query index (struct) size =" + createMem.get("query_struct_mem") / 1024 + " KB");
                Run.logger.debug("Query index (keymap) size =" + createMem.get("query_keymap_mem") / 1024 + " KB");
            } else if (index instanceof LFAST) {
                LFAST i = (LFAST) index;
                createMem.add("query_idx_mem", new Long(ObjectSizeCalculator.getObjectSize(i)));
                createMem.add("query_struct_mem", new Long(ObjectSizeCalculator.getObjectSize(i.index)));
                createMem.add("query_keymap_mem", new Long(ObjectSizeCalculator.getObjectSize(LFAST.overallQueryTextSummery)));
                createMem.add("object_idx_mem", new Long(0));
                Run.logger.debug("Obj index size =" + createMem.get("object_idx_mem") / 1024 + " KB");
                Run.logger.debug("Query index size =" + createMem.get("query_idx_mem") / 1024 + " KB");
                Run.logger.debug("Query index (struct) size =" + createMem.get("query_struct_mem") / 1024 + " KB");
                Run.logger.debug("Query index (keymap) size =" + createMem.get("query_keymap_mem") / 1024 + " KB");
            } else if (index instanceof CkQST) {
                CkQST i = (CkQST) index;
                createMem.add("query_idx_mem", new Long(ObjectSizeCalculator.getObjectSize(i.queryIndex)));
                createMem.add("object_idx_mem", new Long(ObjectSizeCalculator.getObjectSize(i.objectIndex)));
                Run.logger.debug("Obj index size =" + createMem.get("object_idx_mem") / 1024 + " KB");
                Run.logger.debug("Query index size =" + createMem.get("query_idx_mem") / 1024 + " KB");
            } else if (index instanceof AdoptCkQST) {
                AdoptCkQST i = (AdoptCkQST) index;
                createMem.add("query_idx_mem", new Long(ObjectSizeCalculator.getObjectSize(i.queryIndex)));
                createMem.add("object_idx_mem", new Long(ObjectSizeCalculator.getObjectSize(i.objectIndex)));
                Run.logger.debug("Obj index size =" + createMem.get("object_idx_mem") / 1024 + " KB");
                Run.logger.debug("Query index size =" + createMem.get("query_idx_mem") / 1024 + " KB");
            }
        }
    }

    public void create() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (Query q : ProgressBar.wrap(queries, "Create Index")) {
            Stopwatch createTimeWatch = null;
            if (saveTimeline)
                createTimeWatch = Stopwatch.createStarted();
            index.insertQuery(q);
            if (saveTimeline) {
                assert createTimeWatch != null;
                createTimeWatch.stop();
                createTimeline.add((int) createTimeWatch.elapsed(TimeUnit.NANOSECONDS));
            }
        }
        stopwatch.stop();

        calcCreateMem();
    //    createMem = GraphLayout.parseInstance(index).totalSize();
        if (index instanceof FAST) {
            this.creationTime = FAST.context.creationTime;
            this.objIdxSearchTime = FAST.context.objIdxSearchTime;
            this.indexingTime = FAST.context.indexingTime;
        } else if (index instanceof CkQST) {
            this.creationTime = CkQST.creationTime;
            this.objIdxSearchTime = CkQST.objIdxSearchTime;
            this.indexingTime = CkQST.indexingTime;
        }  
    }

    public void search() {
        results = new ArrayList<>();

        // Stopwatch totalTimeWatch = Stopwatch.createStarted();
        int resultCount = 0;
        for (DataObject o : ProgressBar.wrap(objects, "Stream Objects")) {
        // for (DataObject o : objects) {
            Stopwatch searchTimeWatch = null;
            if (saveTimeline)
                searchTimeWatch = Stopwatch.createStarted();
            Collection<Query> res = index.insertObject(o);
            resultCount += res.size();
            if (saveTimeline) {
                assert searchTimeWatch != null;
                searchTimeWatch.stop();
                searchTimeline.add((int) searchTimeWatch.elapsed(TimeUnit.NANOSECONDS));
            }
//            if (res.size() > 0)
//                System.out.println("Object: " + o.id + " -> " + res.toString());
            results.add(res);
        }
        // totalTimeWatch.stop();
        if (index instanceof FAST) {
            System.out.println("Insertions: " + FAST.context.insertions);
            System.out.println("Unbounded Insertions: " + FAST.context.initUnbounded);
            System.out.println("Bounded Insertions: " + FAST.context.initBounded + "\n");
            System.out.println("Insertions: " + FAST.context.insertions);
            System.out.println("No. of tries: " + FAST.context.numberOfTries);
            System.out.println("Results count: " + resultCount);
            System.out.println("Small Ar descends count: " + FAST.context.smallArDescends);
            System.out.println("Ar adj count: " + FAST.context.arAdjustments + "\n");
            System.out.println("Unbounded count: " + FAST.context.unboundedInserts);
            System.out.println("Unbounded keyword inserted: " + FAST.context.unboundedCounter);
            System.out.println("Unbounded alt keyword inserted: " + FAST.context.unboundedCounter2);
            System.out.println("Unbounded inserted at list: " + FAST.context.unboundedCounter3);
            System.out.println("Unbounded inserted at trie: " + FAST.context.unboundedCounter4);
            System.out.println("Unbounded inserted at trie (as Node): " + FAST.context.unboundedCounter5);
            System.out.println("Final queries: " + FAST.context.finalQueries + "\n");
            System.out.println("Nodes to lists: " + FAST.context.nodeToList);
            System.out.println("List to tries: " + FAST.context.listToTrie + "\n");

            List<String> maxList = FAST.keywordFrequencyMap.entrySet().stream().sorted(Comparator.comparingInt((e) -> ((Map.Entry<String, KeywordFrequency>) e).getValue().queryCount).reversed()).map((e) -> e.getKey() + " - " + e.getValue()).limit(10).collect(Collectors.toList());
            System.out.println(maxList.toString());
        } else {
            System.out.println("Results count: " + resultCount);
        }
        
        String javaVersion = System.getProperty("java.version");
        String javaVendor = System.getProperty("java.vendor");
        if (javaVersion.contains("1.8") && javaVendor.contains("OpenJDK")) {
           searchMem.add("object_mem", new Long(ObjectSizeCalculator.getObjectSize(objects)));
           searchMem.add("query_mem", new Long(ObjectSizeCalculator.getObjectSize(queries)));
           Run.logger.debug("Queries size =" + searchMem.get("object_mem") / 1024 + " KB");
           Run.logger.debug("Objects size =" + searchMem.get("query_mem") / 1024 + " KB");
           if (index instanceof FAST) {
                FAST i = (FAST) index;
                searchMem.add("query_idx_mem", new Long(ObjectSizeCalculator.getObjectSize(i.index)));
                searchMem.add("query_struct_mem", new Long(ObjectSizeCalculator.getObjectSize(i.index)));
                searchMem.add("query_keymap_mem", new Long(ObjectSizeCalculator.getObjectSize(FAST.keywordFrequencyMap))); 
                searchMem.add("object_idx_mem", new Long(ObjectSizeCalculator.getObjectSize(i.objIndex)));

                Run.logger.debug("Obj index size =" + searchMem.get("object_idx_mem") / 1024 + " KB");
                Run.logger.debug("Query index size =" + searchMem.get("query_idx_mem") / 1024 + " KB");
                Run.logger.debug("Query index (struct) size =" + searchMem.get("query_struct_mem") / 1024 + " KB");
                Run.logger.debug("Query index (keymap) size =" + searchMem.get("query_keymap_mem") / 1024 + " KB");
            } else if (index instanceof LFAST) {
                LFAST i = (LFAST) index;
                searchMem.add("query_idx_mem", new Long(ObjectSizeCalculator.getObjectSize(i)));
                searchMem.add("query_struct_mem", new Long(ObjectSizeCalculator.getObjectSize(i.index)));
                searchMem.add("query_keymap_mem", new Long(ObjectSizeCalculator.getObjectSize(LFAST.overallQueryTextSummery)));                
                searchMem.add("object_idx_mem", new Long(0));
                Run.logger.debug("Obj index size =" + searchMem.get("object_idx_mem") / 1024 + " KB");
                Run.logger.debug("Query index size =" + searchMem.get("query_idx_mem") / 1024 + " KB");
                Run.logger.debug("Query index (struct) size =" + searchMem.get("query_struct_mem") / 1024 + " KB");
                Run.logger.debug("Query index (keymap) size =" + searchMem.get("query_keymap_mem") / 1024 + " KB");
            } else if (index instanceof CkQST) {
                CkQST i = (CkQST) index;
                searchMem.add("query_idx_mem", new Long(ObjectSizeCalculator.getObjectSize(i.queryIndex)));
                searchMem.add("object_idx_mem", new Long(ObjectSizeCalculator.getObjectSize(i.objectIndex)));
                Run.logger.debug("Obj index size =" + searchMem.get("object_idx_mem") / 1024 + " KB");
                Run.logger.debug("Query index size =" + searchMem.get("query_idx_mem") / 1024 + " KB");
            } else if (index instanceof AdoptCkQST) {
                AdoptCkQST i = (AdoptCkQST) index;
                searchMem.add("query_idx_mem", new Long(ObjectSizeCalculator.getObjectSize(i.queryIndex)));
                searchMem.add("query_struct_mem", new Long(ObjectSizeCalculator.getObjectSize(i.queryIndex.index)));
                searchMem.add("query_keymap_mem", new Long(ObjectSizeCalculator.getObjectSize(LFAST.overallQueryTextSummery))); 
                searchMem.add("object_idx_mem", new Long(ObjectSizeCalculator.getObjectSize(i.objectIndex)));
                Run.logger.debug("Obj index size =" + searchMem.get("object_idx_mem") / 1024 + " KB");
                Run.logger.debug("Query index size =" + searchMem.get("query_idx_mem") / 1024 + " KB");
            }
       }
    //    if (System.getProperty("java.version").contains("1.8") && System.getProperty("java.vendor").contains("OpenJDK")) {
    //        long queriesSize = ObjectSizeCalculator.getObjectSize(queries);
    //        Run.logger.debug("Queries size =" + queriesSize / 1024 + " KB");
    //        long indexMemorySize = ObjectSizeCalculator.getObjectSize(index); // - queriesSize;
    //        Run.logger.debug("Local index size =" + indexMemorySize / 1024 + " KB");
    //        searchMem = indexMemorySize;
    //    }
//        searchMem = GraphLayout.parseInstance(index).totalSize();
        // this.searchTime = totalTimeWatch.elapsed(TimeUnit.NANOSECONDS);
        if (index instanceof FAST) {
            this.searchTime = FAST.context.searchTime;
            System.out.println("Obj. idx search count: " + FAST.context.objectSearchCount);
        } else if (index instanceof CkQST) {
            this.searchTime = CkQST.searchTime;
            System.out.println("Obj. idx search count: " + CkQST.objectSearchCount);
        }     
    }

    public void search(Function<DataObject, Boolean> fn) {
        results = new ArrayList<>();

        Stopwatch totalTimeWatch = Stopwatch.createStarted();
        for (DataObject o : objects) {
            Stopwatch searchTimeWatch = null;
            if (saveTimeline)
                searchTimeWatch = Stopwatch.createStarted();
            Collection<Query> res = index.insertObject(o);
            if (saveTimeline) {
                assert searchTimeWatch != null;
                searchTimeWatch.stop();
                searchTimeline.add((int) searchTimeWatch.elapsed(TimeUnit.NANOSECONDS));
            }
            fn.apply(o);
            results.add(res);
        }
        totalTimeWatch.stop();
        // if (System.getProperty("java.version").equals("1.8") && System.getProperty("java.vendor").contains("OpenJDK")) {
        //     long queriesSize = ObjectSizeCalculator.getObjectSize(queries);
        //     Run.logger.debug("Queries size =" + queriesSize / 1024 + " KB");
        //     long indexMemorySize = ObjectSizeCalculator.getObjectSize(index) - queriesSize;
        //     Run.logger.debug("Local index size =" + indexMemorySize / 1024 + " KB");
        //     searchMem = indexMemorySize;
        // }
//        searchMem = GraphLayout.parseInstance(index).totalSize();
        this.searchTime = totalTimeWatch.elapsed(TimeUnit.NANOSECONDS);
    }


    public void save() throws InvalidOutputFile {
        if (!this.saveStats)
            return;

        if (outputPath == null) {
            Run.logger.error("No output file specified. Skipping saving!");
            return;
        }

        Metadata<String> meta = generateMetadata();

        File outputFile = new File(outputPath);
        boolean fileExists = outputFile.exists();
        if (!outputFile.isDirectory()) {
            try {
                if (!fileExists && outputFile.createNewFile()) {
                    StringBuilder header = new StringBuilder("name,creation_time,search_time,index_time");
                    for (String k : meta.getKeys()) {
                        header.append(",").append(k);
                    }
                    for (String k : createMem.getKeys()) {
                        header.append(",").append("create_").append(k);
                    }
                    for (String k : searchMem.getKeys()) {
                        header.append(",").append("search_").append(k);
                    }
                    header.append(",").append("clean_time");
                    FileWriter fw = new FileWriter(outputFile);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(header + "\n");
                    bw.close();
                    fw.close();
                }

                FileWriter fw = new FileWriter(outputFile, true);
                BufferedWriter bw = new BufferedWriter(fw);

                StringBuilder line = new StringBuilder(name + "," + creationTime + "," + searchTime + "," + indexingTime);
                for (String v : meta.getValues()) {
                    line.append(",").append(v);
                }
                for (Long k : createMem.getValues()) {
                    line.append(",").append(k);
                }
                for (Long k : searchMem.getValues()) {
                    line.append(",").append(k);
                }
                // System.out.println(((FAST) index).cleanTime);
                // line.append(",").append(((FAST) index).cleanTime);
                bw.write(line + "\n");
                bw.close();
                fw.close();

                if (saveTimeline) {
//                    String timelinePath = getSuffixedPath("search_timeline", meta.getKeys(), meta.getValues());
//                    FileWriter timelineFW = new FileWriter(timelinePath);
//                    BufferedWriter timelineBW = new BufferedWriter(timelineFW);
//                    for (long v : searchTimeline) {
//                        timelineBW.write(v + "\n");
//                    }
//                    timelineBW.close();
//                    timelineFW.close();

                    if (index instanceof FAST || index instanceof CkQST) {
                        String queryInsObjSearchPath = getSuffixedPath("sub_timeline", meta.getKeys(), meta.getValues());
                        FileWriter queryInsObjSearchFW = new FileWriter(queryInsObjSearchPath);
                        BufferedWriter queryInsObjSearchBW = new BufferedWriter(queryInsObjSearchFW);

                        for (QueryStat queryInsStat : index.queryStats) {
                            queryInsObjSearchBW.write(queryInsStat.toJson() + "\n");
                        }

                        queryInsObjSearchBW.close();
                        queryInsObjSearchFW.close();
                    }

//                    timelinePath = getSuffixedPath("create_timeline", meta.getKeys(), meta.getValues());
//                    timelineFW = new FileWriter(timelinePath);
//                    timelineBW = new BufferedWriter(timelineFW);
//                    for (long v : createTimeline) {
//                        timelineBW.write(v + "\n");
//                    }
//                    timelineBW.close();
//                    timelineFW.close();
                }

//                if (saveOutput) {
//                    FileWriter outputFW = new FileWriter(getSuffixedPath("output", meta.getKeys(), meta.getValues()));
//                    BufferedWriter outputBW = new BufferedWriter(outputFW);
//                    for (Collection<Query> v : results) {
//                        outputBW.write(Arrays.toString(v.stream().map((query) -> query.id).sorted().toArray()) + "\n");
//                    }
//                    outputBW.close();
//                    outputFW.close();
//                }
                //  try {
                //      FileWriter fw2 = new FileWriter(getSuffixedPath("queries", meta.getKeys(), meta.getValues()));
                //      BufferedWriter bw2 = new BufferedWriter(fw2);

                //      bw2.write("id,ar,currentLevel,x,y\n");
                //      HashSet<KNNQuery> rSet = ((FAST) index).allKNNQueries(false);
                //      for (KNNQuery query: rSet) {
                //          try {
                //             // System.out.println(query.toString());
                //             bw2.write("" + query.id + ',' + query.ar + ',' + query.currentLevel +
                //                      ',' + query.location.x + ',' + query.location.y + "\n");
                //             bw2.flush();
                //          } catch (IOException e) {
                //             System.out.println("ERROR:" + query.toString());
                //              throw new RuntimeException(e);
                //          }
                //      }

                //     //  bw2.close();
                //     //  fw2.close();
                //  } catch (IOException e) {
                //      throw new InvalidOutputFile(outputPath);
                //  }
            } catch (IOException e) {
                throw new InvalidOutputFile(outputPath);
            }
        } else {
            throw new InvalidOutputFile(outputPath);
        }
    }

    private String getSuffixedPath(String type, List<String> keys, List<String> values) {
        int numQueriesI = keys.indexOf("num_queries");
        int numObjectsI = keys.indexOf("num_objects");
        String outSuffix = "_" + type + "_" + values.get(numQueriesI) + "_" + values.get(numObjectsI) + ".csv";

        return outputPath.replace(".csv", outSuffix);
    }

    public String getName() {
        return name;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public ArrayList<Collection<Query>> getResults() {
        return results;
    }

    public void setSaveTimeline(boolean saveTimeline) {
        this.saveTimeline = saveTimeline;
    }

    public void setSaveOutput() {
        this.saveOutput = true;
    }

    @SuppressWarnings("rawtypes")
    public SpatialKeywordIndex getIndex() {
        return index;
    }

    /**
     * Sets whether to save the run or not. Overwrite the setSaveTimeline and setSaveOutput settings.
     *
     * @param saveStats to save the run or not.
     */
    public void setSaveStats(boolean saveStats) {
        this.saveStats = saveStats;
    }

    public enum IndexType {
        FAST,
        FAST_NAIVE,
        CkQST,
        LFAST,
        AdoptCkQST
    }

    public static class Metadata<T> {
        List<String> keys;
        List<T> values;

        public Metadata() {
            this.keys = new ArrayList<>();
            this.values = new ArrayList<>();
        }

        public void add(String key, T value) {
            this.keys.add(key);
            this.values.add(value);
        }

        public T get(String key) {
            int idx = this.keys.indexOf(key);
            return this.values.get(idx);
        }

        public List<String> getKeys() {
            return keys;
        }

        public List<T> getValues() {
            return values;
        }
    }
}

