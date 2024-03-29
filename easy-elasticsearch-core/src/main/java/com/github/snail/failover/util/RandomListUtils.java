package com.github.snail.failover.util;

import static java.lang.Math.min;
import static java.util.Collections.emptyList;
import static java.util.Collections.shuffle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

/**
 * @author w.vela
 */
public final class RandomListUtils {

    private static final int LCG_THRESHOLD = 3;

    private RandomListUtils() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public static <T> T getRandom(List<T> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return source.get(ThreadLocalRandom.current().nextInt(source.size()));
    }

    public static <T> List<T> getRandom(Collection<T> source, int size) {
        if (source == null || source.isEmpty()) {
            return emptyList();
        }
        if (source instanceof List && source instanceof RandomAccess
                && size < source.size() / LCG_THRESHOLD) {
            return getRandomUsingLcg((List<T>) source, size);
        } else {
            return getRandomUsingShuffle(source, size);
        }
    }

    static <T> List<T> getRandomUsingShuffle(Collection<T> source, int size) {
        List<T> newList = new ArrayList<>(source);
        shuffle(newList, ThreadLocalRandom.current());
        return newList.subList(0, min(newList.size(), size));
    }

    static <T> List<T> getRandomUsingLcg(List<T> source, int size) {
        int targetSize = min(source.size(), size);
        List<T> newList = new ArrayList<>(targetSize);
        LcgRandomIterator<T> iterator = new LcgRandomIterator<>(source);
        for (int i = 0; i < targetSize; i++) {
            newList.add(iterator.next());
        }
        return newList;
    }
}
