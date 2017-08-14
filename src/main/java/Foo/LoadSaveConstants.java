package main.java.Foo;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class LoadSaveConstants {
    public static void save(String fileLocation, Object[] objects) {
        save(fileLocation, new ArrayList<>(Arrays.asList(objects)));
    }

    public static void save(String fileLocation, Object object) {
        save(fileLocation, new ArrayList<>(Arrays.asList(new Object[] {object})));
    }

    public static void save(String fileLocation, List<Object> objects) {
        try {
            File saveFile = new File(fileLocation);
            saveFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(saveFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(objects);

            oos.close();
            fos.close();
        } catch (IOException e) {
            throw new IllegalStateException("Save Failed");
        }
    }


    public static List<Object> load(String fileLocation) {
        List<Object> objects;
        try {
            File saveFile = new File(fileLocation);
            FileInputStream fis = new FileInputStream(saveFile);
            ObjectInputStream ois = new ObjectInputStream(fis);

            objects = (List<Object>) ois.readObject();

            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Load Failed");
        }

        return objects;
    }

    public static Object loadFirstObject(String fileLocation) {
        return load(fileLocation).get(0);
    }
}
