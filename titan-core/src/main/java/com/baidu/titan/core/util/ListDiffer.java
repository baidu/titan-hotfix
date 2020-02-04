/*
 * Copyright (C) Baidu Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baidu.titan.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 工具列，用于对比两个列表的差异性
 *
 * @author zhangdi07@baidu.com
 * @since 2017/4/18
 */
public abstract class ListDiffer<T> {

    private List<T> mFirst;
    private List<T> mSecond;

    public ListDiffer(List<T> first, List<T> second) {
        this.mFirst = first;
        this.mSecond = second;
    }

    public static final int DIFF_NONE = 1 << 0;
    /**
     * an element was added to the first collection.
     */
    public static final int DIFF_ADDITION = 1 << 1;
    /**
     * an element was removed from the first collection.
     */
    static final int DIFF_REMOVAL = 1 << 2;
    /**
     * an element was changed.
     */
    static final int DIFF_CHANGE = 1 << 3;

    public int diff() {
        if (mFirst != null && mFirst.size() == 0) {
            mFirst = null;
        }

        if (mSecond != null && mSecond.size() == 0) {
            mSecond = null;
        }

        if (mFirst == null && mSecond == null) {
            return DIFF_NONE;
        }

        int diffFlags = 0;

        if (mFirst == null) {
            for (T t : mSecond) {
                diffFlags |= DIFF_ADDITION;
                onAdd(t);
            }
            return diffFlags;
        }

        if (mSecond == null) {
            for (T t : mFirst) {
                diffFlags |= DIFF_REMOVAL;
                onRemove(t);
            }
            return diffFlags;
        }

        List<T> copyOfFirst = new ArrayList<>(mFirst);

        for (T elementOfTwo : mSecond) {
            T elementOfOne = getElementOf(copyOfFirst, elementOfTwo);
            if (elementOfOne != null) {
                copyOfFirst.remove(elementOfOne);
                if (areEqual(elementOfOne, elementOfTwo)) {
                    diffFlags |= DIFF_NONE;
                    onNoChange(elementOfOne, elementOfTwo);
                } else {
                    diffFlags |= DIFF_CHANGE;
                    onChange(elementOfOne, elementOfTwo);
                }
            } else {
                diffFlags |= DIFF_ADDITION;
                onAdd(elementOfTwo);
            }
        }

        for (T elementOfOne : copyOfFirst) {
            diffFlags |= DIFF_REMOVAL;
            onRemove(elementOfOne);
        }

        return diffFlags;
    }


    T getElementOf(List<T> list, T element) {
        for (T t : list) {
            if (canCompare(t, element)) {
                return t;
            }
        }
        return null;
    }


    public void onAdd(T item) {
    }

    public void onRemove(T item) {
    }

    public void onChange(T first, T second) {
    }

    public void onNoChange(T first, T second) {
    }

    protected abstract boolean canCompare(T first, T second);

    protected abstract boolean areEqual(T first, T second);

}
