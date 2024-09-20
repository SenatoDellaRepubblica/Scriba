
package it.senato.areatesti.ebook.scriba.batch;

import it.senato.areatesti.ebook.scriba.Context;
import it.senato.areatesti.ebook.scriba.EbookType;
import it.senato.areatesti.ebook.scriba.Main;
import it.senato.areatesti.ebook.scriba.misc.Misc;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.format;


/**
 * Utility class for Scriba cmd line invokation
 */
public class DirectoryWorker {
    private static final String SCRIBA_CMD_DIFF_MAP_SER = "ScribaCmdDiffMap.ser";
    private static final String SCF_XML = "scf.xml";
    private static final int RUNS_AND_GC = 10;

    private final boolean isDiffExecution;
    private final CommandLine cmdline;
    private ConcurrentHashMap<String, SerializedRetValue> diffHashMap;
    private final boolean initDiffEngine;
    private final int numThread;
    private ExecutorService executor;

    /**
     * Constructor
     *
     * @param isDiffExecution execute Scriba only on the changed SCF files
     * @param initDiffEngine  initializes the Diff engine
     */
    public DirectoryWorker(CommandLine cmdline, boolean isDiffExecution, boolean initDiffEngine, int numThread) {
        this.isDiffExecution = isDiffExecution;
        this.cmdline = cmdline;
        this.initDiffEngine = initDiffEngine;
        this.numThread = numThread;
        this.diffHashMap = new ConcurrentHashMap<>();

        if (this.numThread > 0) {
            this.executor = Executors.newFixedThreadPool(this.numThread);
        }

    }

    /**
     * Execute
     */
    public void execute(File inputDir, EbookType etype) {
        if (this.isDiffExecution)
            executeOnDirDiff(inputDir, etype);
        else
            executeOnDir(inputDir, etype);
    }

    /**
     * Execute on Dir with Differences mechanism
     * <p>
     * It stores a ".ser" file where is stored an HashMap for correspondences
     * between the SCF content files MD5s and the file names.
     * <p>
     * When an SCF file is changed, the MD5 is not in the Hasmap and the ebook is recreated.
     * Then the HashMap is updated.
     */
    private void executeOnDirDiff(File inputDir, EbookType etype) {
        if (!this.initDiffEngine)
            readDiffHashmap();

        executeOnDir(inputDir, etype);
        writeDiffHashmap();
    }

    /**
     * Reads the Map for the Differences of the SCF files
     */
    private void readDiffHashmap() {
        try {
            deserializeHashMap();
        } catch (FileNotFoundException e) {
            Context.getInstance().getBatchLogger().info("Serialized Hashmap not found!");

        } catch (IOException | ClassNotFoundException e) {
            Context.getInstance().getBatchLogger().error(ExceptionUtils.getStackTrace(e));
        }

    }


    private void writeDiffHashmap() {
        try {
            //synchronized(this.diffHashMap)
            //{

            FileOutputStream fos = new FileOutputStream(SCRIBA_CMD_DIFF_MAP_SER);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this.diffHashMap);
            oos.close();
            //}


        } catch (IOException e) {
            Context.getInstance().getBatchLogger().error(ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Deserialize the hashMap
     */
    private void deserializeHashMap() throws IOException, ClassNotFoundException {
        //synchronized (this.diffHashMap)
        //{
        FileInputStream fis;
        fis = new FileInputStream(SCRIBA_CMD_DIFF_MAP_SER);
        ObjectInputStream ois = new ObjectInputStream(fis);

        this.diffHashMap = (ConcurrentHashMap<String, SerializedRetValue>) ois.readObject();
        ois.close();
        //}
    }

    /**
     * Gets the MD5 of the current SCF file
     */
    private String getMD5FileContent(File currFile) throws IOException {
        return DigestUtils.md5Hex(StringUtils.deleteWhitespace(FileUtils.readFileToString(currFile, Context.DEF_ENCODING)));
    }

    /**
     * Executes Scriba on a whole directory files
     */
    private void executeOnDir(File inputDir, EbookType etype) {
        List<File> fileList = (List<File>) FileUtils.listFiles(inputDir, new String[]{SCF_XML}, true);
        Collections.sort(fileList);

        if (!fileList.isEmpty()) {
            // Not concurrent version
            if (this.numThread <= 0) {
                int count = 0;
                for (File currFile : fileList) {
                    executeOnFile(etype, currFile);
                    if (count % RUNS_AND_GC == 0) {
                        Context.gc();
                        Context.getInstance().getBatchLogger().info("Batch (not concurrent): Garbage Collector invoked...");
                        count = 0;
                    }
                    count++;
                }
            }

            // Concurrent version
            else {
                concurrentRun(etype, fileList);
            }
        } else {
            Context.getInstance().getBatchLogger().info("Batch: no Scriba Contents File found! Please use an .scf.xml extension for SCF file.");
        }
    }

    private void concurrentRun(EbookType etype, Collection<File> fileList) {
        Context.getInstance().getBatchLogger().info(format("Starts the Executor for %d threads", this.numThread));
        int numCount = 0;
        List<File> setList = new ArrayList<>();
        List<Runnable> runList = new ArrayList<>();

        Context.getInstance().getBatchLogger().info("Prepare threads");
        for (File currFile : fileList) {
            setList.add(currFile);
            numCount++;

            if (numCount % numThread == 0 && setList.size() > 0) {
                addToConcRunList(etype, setList, runList);
                setList = new ArrayList<>();
            }
        }
        addToConcRunList(etype, setList, runList);


        Context.getInstance().getBatchLogger().info("Start threads");
        for (Runnable r : runList) {
            executor.execute(r);
        }
        executor.shutdown();

        while (!executor.isTerminated()) {
        }
        Context.getInstance().getBatchLogger().info(format("Ends the Executor for %d threads", this.numThread));
    }

    private void addToConcRunList(EbookType etype, List<File> setList,
                                  List<Runnable> runList) {
        Runnable worker = new FileWorkerRunnable(setList, etype);
        runList.add(worker);
    }

    private void executeOnFile(EbookType etype, File currFile) {
        if (!this.isDiffExecution) {
            createTheEBook(etype, currFile);
        } else {
            try {

                //synchronized(this.diffHashMap)
                //{
                // Gets the MD5 and the bean associated

                String md5 = getMD5FileContent(currFile);
                SerializedRetValue retValue = this.diffHashMap.get(md5);
                int numRetry = 0;
                boolean failed = false;

                if (retValue != null) {
                    numRetry = retValue.getNumRetry();
                    failed = retValue.isFailed();
                }

                // Evaluate the Bean and branch to choose
                if (!this.diffHashMap.containsKey(md5)) {
                    Context.getInstance().getBatchLogger().info("Making the ebook: " + currFile.getName());
                    createTheEbook(etype, currFile, md5, numRetry++);
                } else if (!failed) {
                    Context.getInstance().getBatchLogger().info("The SCF file has not changed: " + currFile.getName());
                } else if (numRetry < 5) {
                    Context.getInstance().getBatchLogger().info(format("Batch: previous ebook creation was failed. Retry: %s", currFile.getName()));
                    createTheEbook(etype, currFile, md5, numRetry++);
                } else {
                    Context.getInstance().getBatchLogger().info(format("Batch: previous ebook creation was failed. Maximum limit reached on file: %s", currFile.getName()));
                }
                //}
            } catch (IOException e) {
                Context.getInstance().getBatchLogger().error(ExceptionUtils.getStackTrace(e));
            }

        }
    }

    private void createTheEbook(EbookType etype, File currFile, String md5, int numRetry) {
        boolean ret = createTheEBook(etype, currFile);

        SerializedRetValue srv = new SerializedRetValue(currFile.getName(), numRetry, !ret);

        //synchronized(this.diffHashMap)
        //{
        this.diffHashMap.put(md5, srv);
        //}
        writeDiffHashmap();
    }

    /**
     * Creates the ebook
     */
    private boolean createTheEBook(EbookType etype, File currFile) {
        String onlyFileName = FilenameUtils.getName(currFile.getPath());
        int p = onlyFileName.indexOf('.');
        if (p > 0)
            onlyFileName = onlyFileName.substring(0, p);

        String extension = (etype == EbookType.ALL) ? "" : Misc.getEbookExtension(etype);

        String outputFileName = this.cmdline.getOptionValue(Context.CMD_O) +
                Context.PATH_SEP +
                onlyFileName + extension;

        return Main.createTheEbook(currFile.getPath(), outputFileName, this.cmdline.getOptionValue(Context.CMD_CR), etype, this.cmdline.hasOption(Context.CMD_NIMG));
    }

    /**
     * Thread for the execution on a subset of SCF files
     *
     * @author roberto.battistoni
     */
    class FileWorkerRunnable implements Runnable {
        private final List<File> fileList;
        private final EbookType etype;

        FileWorkerRunnable(List<File> fileList, EbookType etype) {
            this.fileList = fileList;
            this.etype = etype;
        }

        @Override
        public void run() {
            int count = 0;
            for (File currFile : fileList) {
                executeOnFile(etype, currFile);
                if (count % RUNS_AND_GC == 0) {
                    Context.gc();
                    Context.getInstance().getBatchLogger().info("Batch: Garbage Collector (concurrent) invoked...");
                    count = 0;
                }
                count++;
            }
        }
    }


}
