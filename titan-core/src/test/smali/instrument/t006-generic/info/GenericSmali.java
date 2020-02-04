package com.baidu.titan.sample;

import java.util.Collection;
import java.util.Comparator;

public class GenericSmali<K, V> {
    public <T> void find(T t, int a) {

    }

    public void add(K k, V v) {

    }

    public static <T> T min(Collection<? extends T> coll, Comparator<? super T> comp) {

        if (comp == null) {
            return null;
        }

        if (coll == null) {
            return null;
        }

        return coll.iterator().next();
    }

}
