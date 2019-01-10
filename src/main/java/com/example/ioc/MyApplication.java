package com.example.ioc;

import com.example.ioc.bean.Wheel;
import com.example.ioc.ioc.SimpleIoc;

/**
 * @author Quifar
 * @version V1.0
 **/
public class MyApplication {

    public static void main(String[] args) {
        try {
            String location = SimpleIoc.class.getClassLoader().getResource("bean.xml").getFile();
            System.err.println(location);
            SimpleIoc simpleIoc = new SimpleIoc(location);
            //Car car = (Car) simpleIoc.getBean("car");
            Wheel wheel = (Wheel) simpleIoc.getBean("wheel1");
            System.err.println(wheel);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
