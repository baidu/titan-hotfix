package com.baidu.titan.sample.titantest;

import java.io.Serializable;
import java.util.Date;

/**
 * 用于测试实现Serializable接口类的patch
 *
 * @author shanghuibo
 * @since 2019/02/13
 */
public class Person implements Serializable {

    public String name;
    public int sex;
    public int age;
    public String id;

    private boolean isAdult;

    protected int birthYear;

    String desc;

    long lage;

    public Person(String name, int sex, int age, String id) {
        this.name = name;
        validateSex(sex);
        this.sex = sex;
        this.age = age;
        this.id = id;
        birthYear = new Date().getYear() + 1900 - age;
        desc = name + " " + (sex == 0 ? "male" : "female") + " age " + age;
        isAdult = age >= 18;
    }

    protected String getBirthYear() {
        if (validateAge()) {
            return String.valueOf(birthYear);
        }
        return "";
    }

    long getLage() {
        return lage;
    }

    boolean validateAge() {
        if (age >= 0 && age <= 100) {
            return true;
        }
        throw new IllegalArgumentException("age must between 0 and 100");
    }

    private boolean validateSex(int sex) {
        if (sex != 0 && sex != 1) {
            throw new IllegalArgumentException("sex must be 0 or 1");
        }
        return true;
    }

    public boolean isAdult() {
        return isAdult;
    }

}
