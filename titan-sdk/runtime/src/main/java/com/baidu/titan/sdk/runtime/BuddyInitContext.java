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

package com.baidu.titan.sdk.runtime;

/**
 *
 * 标识DirectComponentBuddy的构造函数相关信息，持有BuddyInitHolder链。
 *
 * @author zhangdi07@baidu.com
 * @since 2017/11/23
 */
public class BuddyInitContext {

    public BuddyInitContext() {
    }

    public BuddyInitHolder head;

    public BuddyInitHolder current;

    public BuddyInitHolder last;

    public int initMethodId;

    /** Genesis instace */
    public Object genesisObj;

    /**
     *
     * return current BuddyInitHolder
     *
     * @return
     */
    public BuddyInitHolder current() {
        return this.current;
    }

    /**
     * make next BuddyInitHolder，and return it
     *
     * @param local
     * @param para
     * @return
     */
    public BuddyInitHolder makeNext(int local, int para) {

        BuddyInitHolder next = new BuddyInitHolder(local, para);

        if (head == null) {
            head = next;
        }

        if (current == null) {
            current = next;
        } else {
            current.next = next;
            current = next;
        }
        this.last = next;

        return next;
    }

    /**
     * move to first buddyInitHolder， and return it
     *
     * @return
     */
    public BuddyInitHolder moveToFirst() {
        current = head;
        return head;
    }

    public BuddyInitHolder next() {
        BuddyInitHolder next = current.next;
        current = next;
        return next;
    }

    public BuddyInitHolder last() {
        return this.last;
    }

}
