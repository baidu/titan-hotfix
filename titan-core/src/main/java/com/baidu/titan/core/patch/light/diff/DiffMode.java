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

package com.baidu.titan.core.patch.light.diff;

import com.baidu.titan.dex.util.Flags;

/**
 * @author zhangdi07@baidu.com
 * @author shanghuibo
 * @since 2018/5/3
 */
public class DiffMode {

    private static final int MODE_ADDED = 1 << 0;

    private static final int MODE_UNCHANGED = 1 << 1;

    private static final int MODE_CHANGED = 1 << 2;

    private static final int MODE_REMOVED = 1 << 3;

    private static final int MODE_AFFECT_FINAL_FIELD = 1 << 4;

    public static final int REASON_CHANGED = 1;

    public static final int REASON_FINAL_FIELD_AFFECTED = 2;

    private int mDiffReason = 0;

    private final Flags mFlags = new Flags();

    public DiffMode() {
        super();
    }

    public DiffMode markAdded() {
        this.mFlags.appendFlags(MODE_ADDED);
        return this;
    }

    public boolean isAdded() {
        return this.mFlags.containsOneOf(MODE_ADDED);
    }

    public DiffMode markUnChanged() {
        this.mFlags.appendFlags(MODE_UNCHANGED);
        return this;
    }

    public boolean isUnChanged() {
        return this.mFlags.containsOneOf(MODE_UNCHANGED);
    }

    public DiffMode markChanged() {
        return markChanged(REASON_CHANGED);
    }

    public DiffMode markChanged(int reason) {
        this.mFlags.appendFlags(MODE_CHANGED);
        mDiffReason = reason;
        return this;
    }

    public boolean isChanged() {
        return this.mFlags.containsOneOf(MODE_CHANGED);
    }

    public DiffMode markRemoved() {
        this.mFlags.appendFlags(MODE_REMOVED);
        return this;
    }

    public boolean isRemoved() {
        return this.mFlags.containsOneOf(MODE_REMOVED);
    }

    public boolean isAffectFinalField() {
        return isChanged() && mDiffReason == REASON_FINAL_FIELD_AFFECTED;
    }

    @Override
    public String toString() {
        if (isAdded()) {
            return "diff/add";
        } else if (isUnChanged()) {
            return "diff/unchanged";
        } else if (isChanged()) {
            return "diff/changed";
        } else if (isRemoved()) {
            return "diff/removed";
        } else {
            return "diff/unknown";
        }
    }
}
