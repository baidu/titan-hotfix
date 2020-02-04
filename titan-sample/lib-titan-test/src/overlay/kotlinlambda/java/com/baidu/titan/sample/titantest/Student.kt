package com.baidu.titan.sample.titantest

import java.lang.StringBuilder

fun alphabet (): String {
    val stringBuilder = StringBuilder()
    return with(stringBuilder) {
        for (letter in 'A'..'Z') {
            this.append(letter)
        }
        append("\nNow I know the alphabet!")
        this.toString()
    }
}

fun getStudents() : List<Student> {
    return listOf(Student("Alice", 27), Student("Bob", 31))
}

fun getAStudents() : List<String> {
    return getStudents().asSequence()
            .map(Student::aname)
//            .map {it -> it.aname}
            .filter { it.startsWith("B") }
            .toList()
}

fun isNull(s: String?) {
    println(s ?: "null")
}

/**
 * 用于对kotlin lambda进行测试
 *
 * @author shanghuibo
 * @since 2019/02/13
 */
class Student(val aname: String, val age : Int)