package it.senato.areatesti.ebook.scriba.misc;

import it.senato.areatesti.ebook.scriba.Context;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Objects;

/**
 * It is the Garbage Collector of temp files
 */
public class TempCleaner {
    //private final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
    private final ArrayList<String> gcFiles;
    private String tempExtension;
    private String tempPrefixExtension;


    /**
     * Constructor
     */
    public TempCleaner() {
        this.gcFiles = new ArrayList<>();
        initTempExtension();
    }

    /**
     * Gets the temp dir
     */
    public static String getTempDirectory() {
        return FileUtils.getTempDirectory().getPath();
        //return System.getProperty("java.io.tmpdir");
    }

    /**
     * Initializes the temp extension
     */
    private void initTempExtension() {
        String suffix = SecUtils.getHex(SecUtils.getRandomBytes(2));
        this.tempPrefixExtension = ".scf";
        this.tempExtension = this.tempPrefixExtension + suffix;
    }

    /**
     * it returns the random extension to differentiate other running istances of SCRIBA
     */
    public String getTempExtension() {
        return this.tempExtension;
    }

    /**
     * @return the tempPrefixExtension
     */
    public String getTempPrefixExtension() {
        return tempPrefixExtension;
    }

    /**
     * Puts file in the garbage Collector
     */
    public void putFileInGarbageCollector(String fileNameFullPath) {
        this.gcFiles.add(fileNameFullPath);

    }

    /**
     * Cleans the garbage collector deleting the temp files inside it
     */
    public void cleanGarbageCollector() {
        // deletes files
        int cont = 0;
        for (String f : this.gcFiles) {
            File file = new File(f);
            boolean res = file.delete();
            if (res) cont++;
        }

        // clears the GC struct
        this.gcFiles.clear();

        Context.getInstance().getLogger().debug("Temp files deleted (" + cont + " files)");
    }

    /**
     * it cleans the temp directory from all the temp files created by previous instances of Scriba
     * (these files have a well determined extension)
     */
    public void cleanTempFromPrevFiles() {
        Context.getInstance().getLogger().debug("Checking temp directory for temp cleaning...");

        FilenameFilter filter = (dir, name) -> name.contains(tempPrefixExtension);

        int deleteCount = 0;
        for (String f : Objects.requireNonNull(FileUtils.getTempDirectory().list(filter))) {
            //System.out.println(f);
            File fi = new File(FileUtils.getTempDirectory() + File.separator + f);
            boolean res = fi.delete();
            if (res) deleteCount++;
        }

        Context.getInstance().getLogger().debug("Cleaned: " + deleteCount);
    }


}
