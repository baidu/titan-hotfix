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

package com.baidu.titan.core.patch.full;

import com.baidu.titan.core.component.AndroidComponentFlag;
import com.baidu.titan.core.component.AndroidComponentMarker;
import com.baidu.titan.dex.DexAccessFlags;
import com.baidu.titan.dex.DexConstant;
import com.baidu.titan.dex.DexString;
import com.baidu.titan.dex.DexType;
import com.baidu.titan.dex.DexTypeList;
import com.baidu.titan.dex.extensions.DexSubClassHierarchyFiller;
import com.baidu.titan.dex.node.DexClassNode;
import com.baidu.titan.dex.node.DexMethodNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhangdi07
 * @since 2017/10/12
 */
@Deprecated
public class ClassBuddyCreator {

    private Map<DexType, ClassBuddyInfo> classBuddies = new HashMap<>();


    private static boolean filterOverrideableVirtualMethod(DexMethodNode dmn) {
        DexAccessFlags access = dmn.accessFlags;
        // filter non virtual method
        if (access.containsOneOf(DexAccessFlags.ACC_PRIVATE | DexAccessFlags.ACC_STATIC
                | DexAccessFlags.ACC_CONSTRUCTOR)) {
            return false;
        }

        if (dmn.name.toString().equals("<init>")) {
            return false;
        }

        // filter package access level
        if (access.containsNoneOf(DexAccessFlags.ACC_PUBLIC | DexAccessFlags.ACC_PROTECTED)) {
            return false;
        }
        return true;
    }


    private Set<NamedProtoMethod> postOrderBuildClassBuddy(DexClassNode genesisClassNode,
                                  DexClassNode dcn, Set<NamedProtoMethod> fullVirtualMethods,
                                  int componentType) {
        AndroidComponentFlag acf = AndroidComponentMarker.getComponentFlag(dcn);
        if (acf == null) {
            return null;
        }

        // TODO add a flag helper
        if (!acf.hasFlags(componentType, AndroidComponentFlag.FLAG_SUPER) || !acf.hasFlags
                (componentType, AndroidComponentFlag.FLAG_DIRECT)) {
            return null;
        }


        List<Set<NamedProtoMethod>> subVirtualMethodList = new ArrayList<>();
        DexSubClassHierarchyFiller.forEachSubClass(dcn, sub -> {

            Set<NamedProtoMethod> subVirtualMethods = postOrderBuildClassBuddy(genesisClassNode,
                    sub, fullVirtualMethods, componentType);
            // if subVirtualMethods ==  null implies this sub class not belong android component
            // hierarchy tree
            if (subVirtualMethods != null) {
                subVirtualMethodList.add(subVirtualMethods);
            }
        });

        Set<NamedProtoMethod> thisVirtualMethod = dcn.getMethods().stream()
                .map(k -> new NamedProtoMethod(k.name, k.returnType, k.parameters, k.accessFlags))
                .filter(k -> fullVirtualMethods.contains(k))
                .filter(k -> {
                    boolean containsAll = true;
                    boolean containsOne = false;
                    for (Set<NamedProtoMethod> oneSubVirtualMethods : subVirtualMethodList) {
                           if (oneSubVirtualMethods.contains(k)) {
                               containsOne = true;
                           } else {
                               containsAll = false;
                           }
                    }

                    // a opt way
                    if (containsOne && containsAll) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toSet());

        // TODO
        ClassBuddyInfo cbi = new ClassBuddyInfo(dcn.type, thisVirtualMethod);
        cbi.directClass = acf.hasFlags(componentType, AndroidComponentFlag.FLAG_DIRECT);
        cbi.componentFlag = acf;

        classBuddies.put(cbi.type, cbi);

        return thisVirtualMethod;
    }

    private void traversalGenesis(DexClassNode dcn, int componentType) {
        if (dcn.type.toTypeDescriptor().endsWith("$genesis;")) {
            Set<NamedProtoMethod> fullVirtualMethods = dcn.getMethods().stream()
                    .filter(k -> filterOverrideableVirtualMethod(k))
                    .map(k ->
                            new NamedProtoMethod(k.name, k.returnType, k.parameters, k.accessFlags))
                    .collect(Collectors.toSet());

            DexSubClassHierarchyFiller.forEachSubClass(dcn, sub ->
                postOrderBuildClassBuddy(dcn /** genesisClass */, sub, fullVirtualMethods,
                        componentType));
            return;
        }

        DexSubClassHierarchyFiller.forEachSubClass(dcn, sub -> traversalGenesis(sub, componentType));
    }


    public void createClassBuddyForComponent(DexClassNode rootClassNode, int componentType) {

        traversalGenesis(rootClassNode, componentType);
    }


    public static class ClassBuddyInfo {

        public ClassBuddyInfo(DexType type, Set<NamedProtoMethod> methods) {
            this.type = type;
            this.methods = methods;
        }

        public DexType type;

        public boolean directClass;

        public AndroidComponentFlag componentFlag;

        public Set<NamedProtoMethod> methods;

    }



    public static class NamedProtoMethod {

        public DexType returnType;

        public DexTypeList parameters;

        public DexString name;

        public DexAccessFlags accessFlags;

        public int flags;

        public NamedProtoMethod(DexString name, DexType returnType, DexTypeList parameters,
                                DexAccessFlags accessFlags) {
            this.name = name;
            this.parameters = parameters;
            this.returnType = returnType;
            this.accessFlags = accessFlags;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof NamedProtoMethod)) {
                return false;
            }
            NamedProtoMethod other = (NamedProtoMethod)o;
            return returnType.equals(other.returnType)
                    && parameters.equals(other.parameters)
                    && name.equals(other.name);
        }
    }



}
