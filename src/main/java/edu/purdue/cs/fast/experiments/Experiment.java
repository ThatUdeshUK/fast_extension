package edu.purdue.cs.fast.experiments;

import com.google.common.base.Stopwatch;
import edu.purdue.cs.fast.FAST;
import edu.purdue.cs.fast.exceptions.InvalidOutputFile;
import edu.purdue.cs.fast.models.DataObject;
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
    protected boolean saveStats = true;
    protected boolean saveTimeline = false;
    protected boolean saveOutput = false;
    protected int seed = 7;
    protected ArrayList<List<Query>> results;
    protected ArrayList<Long> searchTimeline = new ArrayList<>();

    protected abstract List<Query> generateQueries();

    protected abstract List<DataObject> generateObjects();

    abstract void run();

    public void create() {
        List<Query> queries = generateQueries();
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (Query q : queries) {
            fast.addContinuousQuery(q);
        }
        stopwatch.stop();
        this.creationTime = stopwatch.elapsed(TimeUnit.NANOSECONDS);
    }

    public void search() {
        List<DataObject> objects = generateObjects();
        results = new ArrayList<>();

        Stopwatch totalTimeWatch = Stopwatch.createStarted();
        for (DataObject o : objects) {
            Stopwatch searchTimeWatch = null;
            if (saveTimeline)
                searchTimeWatch = Stopwatch.createStarted();
            List<Query> res = fast.searchQueries(o);
            if (saveTimeline) {
                assert searchTimeWatch != null;
                searchTimeWatch.stop();
                searchTimeline.add(totalTimeWatch.elapsed(TimeUnit.NANOSECONDS));
            }
            results.add(res);
        }
        totalTimeWatch.stop();
        this.searchTime = totalTimeWatch.elapsed(TimeUnit.NANOSECONDS);
    }

    public void save(List<String> keys, List<String> values) throws InvalidOutputFile {
        if (!this.saveStats)
            return;

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
                    StringBuilder header = new StringBuilder("name,creation_time,search_time,gran,max_x,max_y");
                    for (String k : keys) {
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

                StringBuilder line = new StringBuilder(name + "," + creationTime + "," + searchTime + "," +
                        fast.gridGranularity + "," + fast.bounds.max.x + "," + fast.bounds.max.y);
                for (String v : values) {
                    line.append(",").append(v);
                }
                bw.write(line + "\n");
                bw.close();
                fw.close();

                if (saveTimeline) {
                    FileWriter timelineFW = new FileWriter(getSuffixedPath("timeline", keys, values));
                    BufferedWriter timelineBW = new BufferedWriter(timelineFW);
                    for (long v : searchTimeline) {
                        timelineBW.write(v + "\n");
                    }
                    timelineBW.close();
                    timelineFW.close();
                }

                if (saveOutput) {
                    FileWriter outputFW = new FileWriter(getSuffixedPath("output", keys, values));
                    BufferedWriter outputBW = new BufferedWriter(outputFW);
                    for (List<Query> v : results) {
                        outputBW.write(v.stream().map((query) -> query.id).sorted().toList() + "\n");
                    }
                    outputBW.close();
                    outputFW.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
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

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public ArrayList<List<Query>> getResults() {
        return results;
    }

    public void setSaveTimeline() {
        this.saveTimeline = true;
    }

    public void setSaveOutput() {
        this.saveOutput = true;
    }

    /**
     * Sets whether to save the run or not. Overwrite the setSaveTimeline and setSaveOutput settings.
     *
     * @param saveStats to save the run or not.
     */
    public void setSaveStats(boolean saveStats) {
        this.saveStats = saveStats;
    }
}

