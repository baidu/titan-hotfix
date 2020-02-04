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

package com.baidu.titan.core.component;

/**
 * store Android Component info
 *
 * @author zhangdi07
 * @since 2017/9/6
 */

public class AndroidComponentFlag {

    public static final int TYPE_APPLICATION = 0;

    public static final int TYPE_ACTIVITY = 1;

    public static final int TYPE_SERVICE = 2;

    public static final int TYPE_BROADCAST_RECEIVER = 3;

    public static final int TYPE_CONTENT_PROVIDER = 4;


    public static final int FLAG_NONE = 0;

    public static final int FLAG_DIRECT = 1 << 0;

    public static final int FLAG_SUPER = 1 << 1;

    public static final int FLAG_GENESIS = 1 << 2;

    public static final int FLAG_BUDDY = 1 << 3;

    private static final int FLAG_MAX_ID = 4;

    private static final int FLAG_MASK = (1 << FLAG_MAX_ID) - 1;

//    public static final int FLAG_SUPER_TOP = 1 << 2;

    private int mFlags;

    public int getFlag(int type) {
        int shift = type * FLAG_MAX_ID;
        return (mFlags >> shift) & FLAG_MASK;
    }

    public void setFlag(int type, int flag) {
        int shift = type * FLAG_MAX_ID;
        mFlags = mFlags | (flag << shift);
                // (mFlags & ~(FLAG_MASK << shift)) | (flag << shift);
    }

    public boolean hasFlags(int type, int flags) {
        return (getFlag(type) & flags) != 0;
    }

}
