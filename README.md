# Titan热修复

## 描述
Titan是一个Android平台上的热修复解决方案，无需发版就可以修复线上问题。具有全Android版本兼容性、轻量性、实时生效、极高修复成功率、多维度修复等特性。

## 特性
* 支持java & kotlin代码修复
* 方法级问题修复
* 支持新增类，支持新增、删除匿名内部类
* 支持新增字段，包括静态字段和常量
* 支持Override方法
* 支持全量修复(beta)

## 原理概述
[Titan热修复概述](doc/introduction_of_titan.md)

## 接入Titan
[Titan快速接入指南](doc/quick_start.md)

## 已知问题
[Titan已知问题](doc/known_issues.md)

## 测试
在titan-core/src/test目录下编写了多个测试用例，在android studio中执行测试

## 如何贡献
贡献代码请联系以下同学review
* 张迪(zhangdi07@baidu.com)
* 尚会波(shanghuibo@baidu.com)

## License
This project is licensed under the Apache-2.0 license - see the LICENSE file for details