package org.demo;

import org.tools.exceptions.FrameworkException;

public class Toto {
    public String titi() {
        //return "titi";
        throw new FrameworkException("sdsd");
    }

    public static String tata() throws Exception {
        System.out.println("tata");
        return "tata";
    }
}
