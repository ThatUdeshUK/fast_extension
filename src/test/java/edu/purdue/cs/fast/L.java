package edu.purdue.cs.fast;

import edu.purdue.cs.fast.models.DataObject;
import edu.purdue.cs.fast.models.KNNQuery;
import edu.purdue.cs.fast.models.Query;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class L {
    public static List<Query> of(Query... queries) {
        return Arrays.stream(queries).collect(Collectors.toList());
    }

    public static List<DataObject> of(DataObject... objects) {
        return Arrays.stream(objects).collect(Collectors.toList());
    }

    public static List<String> of(String... keywords) {
        return Arrays.stream(keywords).collect(Collectors.toList());
    }

    public static List<Integer> of(Integer... is) {
        return Arrays.stream(is).collect(Collectors.toList());
    }

//    public static List<Integer> of() {
//        return new ArrayList<>();
//    }

    public static List<Answer> of(Answer... list) {
        return Arrays.stream(list).collect(Collectors.toList());
    }
}
