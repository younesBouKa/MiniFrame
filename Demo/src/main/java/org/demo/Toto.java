package org.demo;

import org.tools.exceptions.FrameworkException;

public class Toto {
    public String titi() {
        //return "titi";
        if(false)
            throw new FrameworkException("sdsd");
        return "titi";
    }

    public static String tata() throws Exception {
        System.out.println("tata");
        return "tata";
    }
}
