package org.aspect.proxy;

import javassist.ClassPath;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class DirClassPath extends ClassLoader implements ClassPath {
    String directory;

    DirClassPath(String dirName) {
        this.directory = dirName;
    }

    public InputStream openClassfile(String classname) {
        try {
            char sep = File.separatorChar;
            String filename = this.directory + sep + classname.replace('.', sep) + ".class";
            return new FileInputStream(filename);
        } catch (FileNotFoundException var4) {
        } catch (SecurityException var5) {
        }

        return null;
    }

    public URL find(String classname) {
        char sep = File.separatorChar;
        String filename = this.directory + sep + classname.replace('.', sep) + ".class";
        File f = new File(filename);
        if (f.exists()) {
            try {
                return f.getCanonicalFile().toURI().toURL();
            } catch (MalformedURLException var6) {
            } catch (IOException var7) {
            }
        }

        return null;
    }

    public String toString() {
        return this.directory;
    }
}
