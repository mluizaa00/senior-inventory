//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package br.com.luiza.inventory.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SQLReader {

    private final Map<String, String> sqlQueries = new HashMap();

    public SQLReader() {
    }

    public String getSql(String name) {
        return (String)this.sqlQueries.get(name.toLowerCase());
    }

    public void loadFromResources() throws IOException {
        this.loadFromResources("sql/");
    }

    public void loadFromResources(String path) throws IOException {
        Class callerClass;
        try {
            callerClass = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
        } catch (ClassNotFoundException var13) {
            throw new IOException(var13);
        }

        ClassLoader classLoader = callerClass.getClassLoader();

        URI uri;
        try {
            uri = callerClass.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (URISyntaxException var12) {
            throw new IllegalArgumentException(var12);
        }

        File file = new File(uri);
        if (!file.exists()) {
            throw new IllegalArgumentException("Could not find specified file");
        } else if (file.isDirectory()) {
            throw new IllegalArgumentException("The specified folder must be a file");
        } else {
            JarFile jarFile = new JarFile(file);
            Enumeration entries = jarFile.entries();

            while(entries.hasMoreElements()) {
                JarEntry entry = (JarEntry)entries.nextElement();
                String name = entry.getName();
                if (name.length() >= path.length() + 5 && name.startsWith(path) && name.endsWith(".sql")) {
                    InputStream stream = classLoader.getResourceAsStream(name);
                    if (stream == null) {
                        return;
                    }

                    InputStreamReader reader = new InputStreamReader(stream);
                    name = name.substring(path.length(), name.length() - 4);
                    this.read(name, reader);
                }
            }

        }
    }

    public void loadFromFile(File file) {
        if (!file.exists()) {
            throw new IllegalArgumentException("Could not find specified file");
        } else if (file.isDirectory()) {
            throw new IllegalArgumentException("The specified folder must be a file");
        } else {
            this.readFile(file);
        }
    }

    private void readFile(File file) {
        if (!file.isDirectory()) {
            String name = file.getName();
            if (name.length() >= 5 && name.endsWith(".sql")) {
                name = name.substring(0, name.length() - 4);

                try {
                    FileReader reader = new FileReader(file);
                    Throwable var4 = null;

                    try {
                        this.read(name, reader);
                    } catch (Throwable var14) {
                        var4 = var14;
                        throw var14;
                    } finally {
                        if (reader != null) {
                            if (var4 != null) {
                                try {
                                    reader.close();
                                } catch (Throwable var13) {
                                    var4.addSuppressed(var13);
                                }
                            } else {
                                reader.close();
                            }
                        }

                    }
                } catch (IOException var16) {
                    System.err.printf("Could not read \"%s\", skipping...", name);
                    var16.printStackTrace();
                }

            }
        }
    }

    private void read(String name, Reader reader) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            BufferedReader stream = new BufferedReader(reader);
            Throwable var5 = null;

            try {
                String line;
                try {
                    while((line = stream.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                } catch (Throwable var15) {
                    var5 = var15;
                    throw var15;
                }
            } finally {
                if (stream != null) {
                    if (var5 != null) {
                        try {
                            stream.close();
                        } catch (Throwable var14) {
                            var5.addSuppressed(var14);
                        }
                    } else {
                        stream.close();
                    }
                }

            }
        } catch (IOException var17) {
            System.err.printf("Could not read \"%s\", skipping...", name);
            var17.printStackTrace();
        }

        this.sqlQueries.put(name.toLowerCase(), stringBuilder.toString());
    }
}
