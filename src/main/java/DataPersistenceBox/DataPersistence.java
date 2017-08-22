package DataPersistenceBox;

import CharacterBox.UserCharacter;
import CoreBox.IDs;
import CoreBox.Quotes;
import CoreBox.SessionTimes;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/*
 * Thread which backs up the data at regular time intervals
 */
public class DataPersistence implements Runnable {
    public static final String fileLocation = IDs.mainFilePath + "DataPersistenceBox/";
    private static final int backupIntervalMins = 60;
    private static final int minsToMillisecondsConversion = 60 * 1000;


    private static void save(String fileLocation, Object[] objects) {
        save(fileLocation, new ArrayList<>(Arrays.asList(objects)));
    }


    private static void save(String fileName, List<Object> objects) {
        final File saveFile = new File(fileLocation + fileName);

        try (final FileOutputStream fos = new FileOutputStream(saveFile);
             final ObjectOutputStream oos = new ObjectOutputStream(fos))
        {
            saveFile.createNewFile();
            oos.writeObject(objects);

            oos.flush();
            oos.close();
            fos.close();
        } catch (IOException e) {
            throw new IllegalStateException("Save Failed");
        }
    }


    public static void save(String fileLocation, Object object) {
        save(fileLocation, new ArrayList<>(Arrays.asList(new Object[]{object})));
    }


    public static Object loadFirstObject(String fileLocation) {
        return load(fileLocation).get(0);
    }


    private static List<Object> load(String fileName) {
        final List<Object> objects;
        try (final FileInputStream fis = new FileInputStream(new File(fileLocation + fileName));
             final ObjectInputStream ois = new ObjectInputStream(fis))
        {
            objects = (List<Object>) ois.readObject();

            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Load Failed");
        }

        return objects;
    }


    /*
     * Load all data from files
     */
    public static void loadData() {
        UserCharacter.load();
        SessionTimes.load();
        Quotes.load();

        System.out.println("Load complete");
    }


    // TODO: Different backup versions
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(backupIntervalMins * minsToMillisecondsConversion);
            } catch (InterruptedException e) {
            }

            saveData();
        }
    }


    /*
     * Save all the data currently stored
     */
    public static void saveData() {
        UserCharacter.save();
        SessionTimes.save();
        Quotes.save();

        System.out.println("Saves complete");
    }
}
