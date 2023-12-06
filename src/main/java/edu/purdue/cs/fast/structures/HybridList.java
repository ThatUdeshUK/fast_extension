package edu.purdue.cs.fast.structures;

import edu.purdue.cs.fast.models.KNNQuery;
import edu.purdue.cs.fast.models.MinimalRangeQuery;
import edu.purdue.cs.fast.models.Query;

import java.util.*;

public class HybridList implements Iterable<Query> {
    private final ArrayList<MinimalRangeQuery> mbrQueries;
    private final LinkedList<KNNQuery> kNNQueries;

    // TODO - Remove eager init
    public HybridList() {
        this.mbrQueries = new ArrayList<>();
        this.kNNQueries = new LinkedList<>();
    }

    public List<MinimalRangeQuery> mbrQueries() {
        return this.mbrQueries;
    }

    public List<KNNQuery> kNNQueries() {
        return this.kNNQueries;
    }

    public void add(Query query) {
        if (query instanceof MinimalRangeQuery) {
            this.mbrQueries.add((MinimalRangeQuery) query);
        } else if (query instanceof KNNQuery) {
            this.kNNQueries.add((KNNQuery) query);
        }
    }

    public void addAll(List<Query> queries) {
        for (Query q: queries) {
            add(q);
        }
    }

    public boolean contains(Query query) {
        if (query instanceof MinimalRangeQuery) {
            return this.mbrQueries.contains((MinimalRangeQuery) query);
        } else if (query instanceof KNNQuery) {
            return this.kNNQueries.contains((KNNQuery) query);
        }
        return false;
    }

    public boolean isEmpty() {
        return this.kNNQueries.isEmpty() && this.mbrQueries.isEmpty();
    }

    public int size() {
        return this.mbrQueries.size() + this.kNNQueries.size();
    }

    @Override
    public Iterator<Query> iterator() {
        return new HybridIterator();
    }

    private class HybridIterator implements Iterator<Query> {
        int cursor;       // index of next element to return
        int lastRet = -1; // index of last element returned; -1 if no such
        int expectedCount = mbrQueries.size() + kNNQueries.size();

        HybridIterator() {}

        @Override
        public boolean hasNext() {
            return cursor < mbrQueries.size() + kNNQueries.size();
        }

        @Override
        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            checkForComodification();

            try {
                Query query = getItemAtIndex(lastRet);

                if (query instanceof MinimalRangeQuery) {
                    mbrQueries.remove((MinimalRangeQuery) query);
                } else if (query instanceof KNNQuery) {
                    kNNQueries.remove((KNNQuery) query);
                }

                cursor = lastRet;
                lastRet = -1;
                expectedCount--;
            } catch (IndexOutOfBoundsException ex) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public Query next() {
            checkForComodification();
            int i = cursor;
            if (i >= mbrQueries.size() + kNNQueries.size())
                throw new NoSuchElementException();

            Query out = getItemAtIndex(i);
            lastRet = i;
            cursor = i + 1;
            return out;
        }

        private Query getItemAtIndex(int i) {
            Query out = null;
            if (i < mbrQueries.size()) {
                out = mbrQueries.get(i);
            } else if (i - mbrQueries.size() < kNNQueries.size()) {
                out = kNNQueries.get(i - mbrQueries.size());
            }
            return out;
        }

        final void checkForComodification() {
            if (expectedCount != mbrQueries.size() + kNNQueries.size())
                throw new ConcurrentModificationException();
        }
    }
}
