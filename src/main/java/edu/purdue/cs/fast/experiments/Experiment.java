package edu.purdue.cs.fast.experiments;

import com.google.common.base.Stopwatch;
import edu.purdue.cs.fast.FAST;
import edu.purdue.cs.fast.exceptions.InvalidOutputFile;
import edu.purdue.cs.fast.helper.ObjectSizeCalculator;
import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.KNNQuery;
import edu.purdue.cs.fast.models.MinimalRangeQuery;
import edu.purdue.cs.fast.models.Query;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class Experiment {
    protected String name;
    protected String outputPath;
    protected FAST fast;
    protected long creationTime;
    protected long searchTime;
    protected long queriesMem;
    protected long indexMem;

    protected abstract List<Query> generateQueries();

    protected abstract List<DataObject> generateObjects();

    abstract void run();

    protected void create() {
        create(true);
    }

    protected void create(boolean calculateMem) {
        List<Query> queries = generateQueries();
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (Query q : queries) {
            fast.addContinuousQuery(q);
        }
        stopwatch.stop();

//        System.gc();
//        System.gc();
//        if (calculateMem) {
//            for (Query q : queries) {
//                if (q instanceof MinimalRangeQuery)
//                    queriesMem += ObjectSizeCalculator.getObjectSize((MinimalRangeQuery) q);
//                if (q instanceof KNNQuery)
//                    queriesMem += ObjectSizeCalculator.getObjectSize((KNNQuery) q);
//            }
//            System.out.println("Queries size =" + queriesMem / 1024 / 1024 + " MB");
//            System.gc();
//            System.gc();
//        }
        this.creationTime = stopwatch.elapsed(TimeUnit.NANOSECONDS);
    }

    protected Iterable<List<Query>> search() {
        return search(true);
    }

    protected Iterable<List<Query>> search(boolean calculateMem) {
        List<DataObject> objects = generateObjects();
        ArrayList<List<Query>> results = new ArrayList<>();

        Stopwatch stopwatch = Stopwatch.createStarted();
        for (DataObject o : objects) {
            results.add(fast.searchQueries(o));
        }
        stopwatch.stop();
        this.searchTime = stopwatch.elapsed(TimeUnit.NANOSECONDS);

        System.gc();
        System.gc();
        if (calculateMem) {
            indexMem = ObjectSizeCalculator.getObjectSize(fast.index) - queriesMem;
            System.out.println("Local index size =" + indexMem / 1024 / 1024 + " MB");
            System.gc();
            System.gc();
        }

        return results;
    }

    protected void save(List<String> keys, List<String> values) throws InvalidOutputFile {
        if (outputPath == null) {
            System.out.println("No output file specified. Skipping saving!");
            return;
        }

        File outputFile = new File(outputPath);
        boolean fileExists = outputFile.exists();
        if (!outputFile.isDirectory()) {
            try {
                if (!fileExists) {
                    outputFile.createNewFile();
                    StringBuilder header = new StringBuilder("name,creation_time,search_time,query_mem,index_mem,gran,max_x,max_y");
                    for (String k: keys) {
                        header.append(",").append(k);
                    }

                    FileWriter fw = new FileWriter(outputFile);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(header + "\n");
                    bw.close();
                    fw.close();
                }

                FileWriter fw = new FileWriter(outputFile, true);
                BufferedWriter bw = new BufferedWriter(fw);

                StringBuilder line = new StringBuilder(name + "," + creationTime + "," + searchTime + "," + queriesMem + ","
                        + indexMem + ","+ fast.gridGranularity + "," + fast.bounds.max.x + "," + fast.bounds.max.y);
                for (String v: values) {
                    line.append(",").append(v);
                }
                bw.write(line + "\n");
                bw.close();
                fw.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new InvalidOutputFile(outputPath);
        }
    }
}

