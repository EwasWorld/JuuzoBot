package CoreBox;

import CharacterBox.UserCharacter;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;



/*
 * Used for reading and writing data to files
 * Thread backs up the data at regular time intervals
 */
public class DataPersistence implements Runnable {
    private static final String fileLocation = Bot.getPathToJuuzoBot() + "DataPersistenceBox/";
    private static final int backupIntervalMins = 180;
    private static final int minsToMillisecondsConversion = 60 * 1000;


    private static void save(String fileLocation, Object[] objects) {
        save(fileLocation, new ArrayList<>(Arrays.asList(objects)));
    }


    private static void save(String fileName, List<Object> objects) {
        final File saveFile = new File(fileLocation + fileName);

        try {
            saveFile.getParentFile().mkdirs();
            saveFile.createNewFile();
        } catch (IOException e) {
            throw new IllegalStateException("Save Failed");
        }

        try (final FileOutputStream fos = new FileOutputStream(saveFile);
             final ObjectOutputStream oos = new ObjectOutputStream(fos))
        {
            oos.writeObject(objects);

            oos.flush();
            oos.close();
            fos.close();
        } catch (IOException e) {
            throw new IllegalStateException("Save Failed");
        }
    }


    public static void save(String fileLocation, Object object) {
        save(fileLocation, new ArrayList<>(Collections.singletonList(object)));
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
    static void loadData() {
        UserCharacter.load();
        SessionTimes.load();
        Quotes.load();

        System.out.println("Load complete");
    }


    // TODO Implement different backup versions (grandfather, father, son)
    /*
     * Runs backups periodically
     */
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
     * Save everything to files
     */
    public static void saveData() {
        UserCharacter.save();
        SessionTimes.save();
        Quotes.save();

        System.out.println("Saves complete");
    }
}
