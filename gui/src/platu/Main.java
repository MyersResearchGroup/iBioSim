package platu;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.HashSet;
import java.util.logging.LogRecord;
import platu.lpn.VarVal;
import java.util.Arrays;
import java.util.Map.Entry;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import platu.project.Project;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import platu.lpn.LPN;
import platu.lpn.LPNTran;
import platu.lpn.LpnTranList;
import platu.stategraph.OutputDOT;
import platu.stategraph.state.State;
import static java.lang.System.*;
import static java.lang.Runtime.*;

public class Main {

   public final static int PROCESSORS = Runtime.getRuntime().availableProcessors();
   public final static int THREADS = PROCESSORS;
   public final static boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");

    static {
//        System.out.println("CORES: " + PROCESSORS);
    }
    
    public static ExecutorService exec = Executors.newFixedThreadPool(PROCESSORS);
    public static HashSet<String> ALL_VARS = new HashSet<String>();
    public static String RESULT_FOLDER;
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd hh.mm.ss a");

    static {
        RESULT_FOLDER = "results\\" + sdf.format(new Date());
    }
    
    public static String BASE_DIR = System.getProperty("user.dir") + "\\";
    // private static final Logger log = Logger.getLogger("Main");
    static OutputStream os;
    public static PrintStream out = System.out;
    static File outFile = null;
    public static boolean ZONE_OPTIMIZE_INF = false;
    public static int ZONE_VERSION = 3;
    public static long GRAPH_KEEP_ALIVE_TIME = 3000;//millisec
    public static boolean ZONE_ENABLE_PRINT = false;
    public static boolean PRINT_USAGE_STATS = false;
    public static boolean REMOVE_DUMMY = false;
    public static boolean FIND_FAILURE = true;
    public static boolean SHOW_SIGNALS = false;
    public static boolean SHOW_FAILURES = true;
    public static boolean NO_DATE = true;
    public static boolean PRINT_FINAL_SUMMARY = false;
    public static long MAX_MEM = 0;
    public static long MAX_STACK_HEIGHT = Long.MAX_VALUE;
    public static boolean PRINT_MAIN = false;
    public static boolean ENABLE_LOGGING = false;

    @SuppressWarnings("deprecation")
    private Main(String cmdFile) {
        try {
            BufferedReader br = null;

            if (cmdFile != null) {
                br = new BufferedReader(new FileReader(cmdFile));
//                if (false) {//ENABLE_LOGGING) {
//                    copyFile(new File(cmdFile), new File(TimedStateGraph.LPN_PATH + "\\"
//                            + Main.RESULT_FOLDER + "\\" + new File(cmdFile).getName()));
//                }
            } else {
                br = new BufferedReader(new InputStreamReader(System.in));
            }

            Interpretor in = new Interpretor();

            long memUse;
            long startTime = currentTimeMillis();

            Project prj = new Project();
            while (true) {

                String commandline = null;
                commandline = br.readLine();

                if (commandline == null) {
                    break;
                }
                if(commandline.equalsIgnoreCase("reset"))
                    prj=new Project();
                if (in.interpretcommand(prj, commandline) == 1) {
                    memUse = getRuntime().totalMemory() - getRuntime().freeMemory();
                    MAX_MEM = MAX_MEM > memUse ? MAX_MEM : memUse;
                    break;
                }
            }

            br.close();

            if (PRINT_FINAL_SUMMARY) {
                long time=currentTimeMillis();
                Thread.sleep(500);
            }
        } catch (java.io.FileNotFoundException ex) {
            System.err.println("MISSING: " + new File(cmdFile).getAbsoluteFile());
            ex.printStackTrace(out);
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(2);
        } catch (Exception ex) {
            ex.printStackTrace(out);
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param args
     */
    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    public static void main(String[] args) {
        try {
            if (false) {//ENABLE_LOGGING) {
                setLogFile(RESULT_FOLDER + "\\messages.log");
            }
            
            String optFileString = null, cmdFileString = null, switches = "";
            for (String arg : args) {
                if (arg.startsWith("/")) {
                    switches += arg + "\n";
                } 
                else if (arg.endsWith(".cmd") || arg.endsWith(".cmnd")) {
                    cmdFileString = arg;
                } 
                else if (arg.endsWith(".opt") || arg.endsWith(".cfg") || arg.endsWith(".conf")) {
                    optFileString = arg;
                }
            }
            
            String path = optFileString != null ? optFileString : cmdFileString;
            if (path != null) {
                File f = new File(path);
                path = f.getAbsoluteFile().getParentFile().getAbsolutePath();
            } else {
                path = System.getProperty("user.dir");
            }
            
//            TimedStateGraph.LPN_PATH = path;
            File optFile = null;
            if (optFileString != null) {
                try {
                    optFile = new File(optFileString);
                    if (!optFile.exists()) {
                        System.err.println("DOES NOT EXIST: " + optFile.getAbsolutePath());
                    }

                    Properties prop = new Properties();
                    prop.load(new FileInputStream(optFile));
                    setOptions(prop);
                    if (PRINT_MAIN) {
                        out.println("LOADED: " + optFileString);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
//                try {
//                System.err.println("OPT: " + new File(StateGraph.LPN_PATH + "\\"
//                        + Main.RESULT_FOLDER + "\\"
//                        + optFile.getName()).getAbsolutePath());
//                    if (false) {//ENABLE_LOGGING) {
//                        new File(TimedStateGraph.LPN_PATH + "\\" + Main.RESULT_FOLDER).mkdirs();
//                    }
//                    if (false) {//ENABLE_LOGGING) {
//                        out.println("RESULTS: "
//                                + new File(TimedStateGraph.LPN_PATH + "\\"
//                                + Main.RESULT_FOLDER).getAbsolutePath());
//                    }
//                    if (false) {//ENABLE_LOGGING) {
//                        copyFile(optFile,
//                                new File(TimedStateGraph.LPN_PATH + "\\"
//                                + Main.RESULT_FOLDER + "\\"
//                                + optFile.getName()));
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            } else {
                try {
                    Properties prop = new Properties();
                    setOptions(prop);//set defaults
                } catch (Exception ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
//            File dir = new File(TimedStateGraph.LPN_PATH + "\\" + Main.RESULT_FOLDER);
//            if (false) {//ENABLE_LOGGING) {
//                dir.mkdirs();
//            }
//            
//            if (!false) {//ENABLE_LOGGING) {
//                out = System.out;
//            }
            
            System.gc();
            System.gc();
            
            new Main(cmdFileString);
//            if (optFileString != null) {
//                File f = new File(TimedStateGraph.LPN_PATH + "\\"
//                        + Main.RESULT_FOLDER + "\\"
//                        + new File(optFileString).getName());
//                try {
//                    if (false) {//ENABLE_LOGGING) {
//                        storeOptions(f);
//                    }
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
            
            if (PRINT_USAGE_STATS) {
                VarVal.printUsageStats();
                LPNTran.printUsageStats();
//                Markings.printUsageStats();
//                TimedStateGraph.printUsageStats();
//                StateVector.printUsageStats();
//                if (AbstractStateGraph.TIMED_ANALYSIS) {
//                    TimedState.printUsageStats();
//                } else {
//                    State.printUsageStats();
//
//                }
            }
        } catch (Throwable e) {
            try {
                File f = new File("error.log");
                FileOutputStream os = new FileOutputStream(f,true);

                os.write(("   " + new Date() + "\n").getBytes());
                if (e instanceof Exception) {
                    e.printStackTrace();
                    System.err.println("AN UNCAUGHT EXCEPTION OCCURRED");
                    os.write(("AN UNCAUGHT EXCEPTION OCCURRED" + "\n").getBytes());
                } else if (e instanceof RuntimeException) {
                    System.err.println("A CRITICAL RUNTIME EXCEPTION OCCURRED");
                    os.write(("A CRITICAL RUNTIME EXCEPTION OCCURRED" + "\n").getBytes());
                } else if (e instanceof Error) {
                    System.err.println("A CRITICAL ERROR OCCURRED");
                    os.write(("A CRITICAL ERROR OCCURRED" + "\n").getBytes());
                } else {
                    System.err.println("A UNCAUGHT THROWABLE OCCURRED");
                    os.write(("A UNCAUGHT THROWABLE OCCURRED" + "\n").getBytes());
                }
                System.err.println("   " + e.toString());
                os.write(("   " + e.toString() + "\n").getBytes());
                System.err.println("MESSAGE: " + e.getMessage());
                os.write(("MESSAGE: " + e.getMessage() + "\n").getBytes());
                System.err.println("CAUSE: " + e.getCause());
                os.write(("CAUSE: " + e.getCause() + "\n").getBytes());
                StackTraceElement[] elements = e.getStackTrace();
                System.err.println("STACK HEIGHT: " + elements.length);
                os.write(("STACK HEIGHT: " + elements.length + "\n").getBytes());

                for (StackTraceElement el : elements) {
                    os.write((el.toString() + "\n").getBytes());
                }
                e.printStackTrace(new PrintStream(os));
                System.err.println("STACK TRACE WAS WRITTEN TO:\n" + f.getAbsolutePath());
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
            System.exit(1);
        }
    }

    static void setOptions(Properties prop) throws Exception {
        String value = prop.getProperty("DOT_PATH");
        if(value != null) Options.setDotPath(value);
        
        value = prop.getProperty("VERBOSITY");
        if(value != null){
        	int verbosity = 0;
        	
        	try{
        		verbosity = Integer.parseInt(value);
        		Options.setVerbosity(verbosity);
        	}
        	catch(Exception e){
        		System.out.println("warning: verbosity option is not valid - default is 0");
        	}
        }
        
        value = prop.getProperty("TIMING_ANALYSIS");
        if(value != null){
    		Options.setTimingAnalsysisType(value);
        }
        
        value = prop.getProperty("COMPOSITIONAL_MINIMIZATION");
        if(value != null){
    		Options.setCompositionalMinimization(value);
        }
        
        value = prop.getProperty("POR");
        if(value != null) Options.setPOR(value);
        
        value = prop.getProperty("SEARCH");
        if(value != null) Options.setSearchType(value);
        
        value = prop.getProperty("STATE_FORMAT");
        if(value != null) Options.setStateFormat(value);
        
        value = prop.getProperty("NEW_PARSER_FLAG");
        if(value != null) if(value.equals("true") || value.equals("TRUE")) Options.setNewParser();
        
        value = prop.getProperty("STICKY_SEMANTICS");
        if(value != null) if(value.equals("true") || value.equals("TRUE")) Options.setStickySemantics();
    }

    static Properties storeOptions(File propFile) throws Exception {
        Properties prop = new Properties();
//        prop.put("StateGraph.MAINTAIN_STATE_TRAN_LIST", "" + TimedStateGraph.MAINTAIN_STATE_TRAN_LIST);
//        prop.put("StateGraph.SHOW_STATE_INC_TREE", "" + TimedStateGraph.SHOW_STATE_INC_TREE);
//        prop.put("StateGraph.USING_POSET", "" + TimedStateGraph.USING_POSET);
        prop.put("REMOVE_DUMMY", "" + REMOVE_DUMMY);
        prop.put("FIND_FAILURE", "" + FIND_FAILURE);
        prop.put("SHOW_SIGNALS", "" + SHOW_SIGNALS);
        prop.put("SHOW_FAILURES", "" + SHOW_FAILURES);
//        prop.put("Expr.PRINT_DEBUG", "" + Expression.PRINT_DEBUG);
        prop.put("LPNTran.ENABLE_PRINT", "" + LPNTran.ENABLE_PRINT);
        prop.put("LPNTran.PRINT_LEVEL", "" + LPNTran.PRINT_LEVEL);
//        prop.put("Markings.INIT_SIZE", "" + Markings.INIT_SIZE);
        prop.put("OutputDOT.SIMPLE_TRAN_LABELS", "" + OutputDOT.SIMPLE_TRAN_LABELS);
        prop.put("OutputDOT.SIMPLE_STATES", "" + OutputDOT.SIMPLE_STATES);
//        prop.put("StateGraph.LPN_PATH", "" + TimedStateGraph.LPN_PATH);
//        prop.put("StateGraph.DOT_PATH", "" + TimedStateGraph.DOT_PATH);
//        prop.put("StateGraph.TIMED_ANALYSIS", "" + TimedStateGraph.TIMED_ANALYSIS);
//        prop.put("StateGraph.INTERACTIVE_MODE", "" + TimedStateGraph.INTERACTIVE_MODE);
//        prop.put("StateGraph.OPEN_STATE_EXPLORER", "" + TimedStateGraph.OPEN_STATE_EXPLORER);
//        prop.put("StateGraph.DRAW_JAVA_GRAPH", "" + TimedStateGraph.DRAW_JAVA_GRAPH);
//        prop.put("StateGraph.DRAW_MEMORY_GRAPH", "" + TimedStateGraph.DRAW_MEMORY_GRAPH);
//        prop.put("StateGraph.DRAW_STATE_GRAPH", "" + TimedStateGraph.DRAW_STATE_GRAPH);
//        prop.put("StateGraph.OUTPUT_DOT", "" + TimedStateGraph.OUTPUT_DOT);
//        prop.put("StateGraph.STOP_ON_ERROR", "" + TimedStateGraph.STOP_ON_ERROR);
//        prop.put("StateGraph.STOP_ON_FAILURE", "" + TimedStateGraph.STOP_ON_FAILURE);
//        prop.put("StateGraph.PRINT_LEVEL", "" + TimedStateGraph.PRINT_LEVEL);
        prop.put("ZONE_ENABLE_PRINT", "" + ZONE_ENABLE_PRINT);
        prop.put("LPN.ENABLE_PRINT", "" + LPN.ENABLE_PRINT);
        prop.put("NO_DATE", "" + NO_DATE);
        prop.put("PRINT_FINAL_SUMMARY", "" + PRINT_FINAL_SUMMARY);
        prop.put("PRINT_MAIN", "" + PRINT_MAIN);
        prop.put("GRAPH_KEEP_ALIVE_TIME", "" + GRAPH_KEEP_ALIVE_TIME);
        prop.put("MAX_STACK_HEIGHT", "" + MAX_STACK_HEIGHT);
        prop.put("ZONE_OPTIMIZE_INF", "" + ZONE_OPTIMIZE_INF);
        prop.put("ZONE_VERSION", "" + ZONE_VERSION);
        try {
            FileOutputStream fos = new FileOutputStream(propFile);
            String str = "";
            int idx = 0;
            String[] entries = new String[prop.entrySet().size()];
            for (Entry ent : prop.entrySet()) {
                str = String.format("%-30s = %-1s\n", ent.getKey(), ent.getValue());
                entries[idx++] = str.replace("\\", "\\\\");
            }
            Arrays.sort(entries);
            fos.write(("\n############ PRINT ##############\n").getBytes());
            for (String ent : entries) {
                if (ent.toLowerCase().contains("print")) {
                    fos.write((ent).getBytes());
                }
            }
            fos.write(("\n############ path ##############\n").getBytes());
            for (String ent : entries) {
                if (ent.toLowerCase().contains("print")) {
                    continue;
                }
                if (ent.toLowerCase().contains("path")) {
                    fos.write((ent).getBytes());
                }
            }
            fos.write(("\n############# DOT ###############\n").getBytes());

            for (String ent : entries) {
                if (ent.toLowerCase().contains("print")) {
                    continue;
                }
                if (ent.toLowerCase().contains("path")) {
                    continue;
                }
                if (ent.toLowerCase().contains("dot")) {
                    fos.write((ent).getBytes());
                }
            }
            fos.write(("\n############ GRAPHS #############\n").getBytes());

            for (String ent : entries) {
                if (ent.toLowerCase().contains("print")) {
                    continue;
                }
                if (ent.toLowerCase().contains("path")) {
                    continue;
                }
                if (ent.toLowerCase().contains("dot")) {
                    continue;
                }
                if (ent.toLowerCase().contains("graph")) {
                    fos.write((ent).getBytes());
                }
            }
            fos.write(("\n############ OTHER ##############\n").getBytes());

            for (String ent : entries) {
                if (ent.toLowerCase().contains("print")) {
                    continue;
                }
                if (ent.toLowerCase().contains("path")) {
                    continue;
                }
                if (ent.toLowerCase().contains("dot")) {
                    continue;
                }
                if (ent.toLowerCase().contains("graph")) {
                    continue;
                }
                fos.write((ent).getBytes());
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return prop;
    }

    private static void setLogFile(final String arg) {
        try {
//            String logDirStr = BASE_DIR + "\\";
//            new File(logDirStr + RESULT_FOLDER).mkdirs();
//            File logDir = new File(logDirStr);
//            logDir.mkdirs();
//            if (arg.contains(":") || arg.startsWith("\\")) {
//                outFile = new File(arg);
//                outFile.createNewFile();
//            } else {
//                outFile = new File(logDirStr + arg);
//                outFile.createNewFile();
//            }
//            if (!outFile.getParentFile().exists()) {
//                outFile = new File(logDirStr
//                        + new Date().toGMTString().replace(":", ".") + ".log");
//            }
//            outFile.createNewFile();
//            final Logger log = Logger.getLogger(outFile.getAbsolutePath());
//            Formatter logFormat = new Formatter() {
//
//                @Override
//                public String format(LogRecord record) {
//                    return record.getMessage();
//                }
//            };
//            FileHandler fh = new FileHandler(outFile.getAbsolutePath());
//
//            fh.setFormatter(logFormat);
//            log.addHandler(fh);
//            log.setLevel(Level.OFF);
//            os = new FileOutputStream(outFile) {
//
//                @Override
//                public void write(byte[] b, int off, int len) throws IOException {
//                    super.write(b, off, len);
//                    //out.write(b, off, len);
//                    log.info(new String(b, off, len));
//                }
//            };
//            out = new PrintStream(os);
//
//            out.println("logging to " + outFile.getAbsolutePath());
        } catch (Exception ex) {
            System.err.println("outFile=" + outFile);
            ex.printStackTrace();
        }
    }

    public static void copyFile(File in, File out) throws IOException {
//        FileChannel inChannel = new FileInputStream(in).getChannel();
//        FileChannel outChannel = new FileOutputStream(out).getChannel();
//        try {
//            inChannel.transferTo(0, inChannel.size(),
//                    outChannel);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (inChannel != null) {
//                inChannel.close();
//            }
//            if (outChannel != null) {
//                outChannel.close();
//            }
//        }
    }

    public static String mergeColumns(String a, String b, int w1, int w2) {
        String ret = "";
        StringTokenizer tk1 = new StringTokenizer(a, "\n");
        StringTokenizer tk2 = new StringTokenizer(b, "\n");
        while (tk1.hasMoreTokens() && tk2.hasMoreTokens()) {
            ret += String.format("%-" + w1 + "s | %-" + w2 + "s |\n", tk1.nextToken(), tk2.nextToken());
        }
        while (tk2.hasMoreTokens()) {
            ret += String.format("%-" + w1 + "s | %-" + w2 + "s |\n", "", tk2.nextToken());
        }
        while (tk1.hasMoreTokens()) {
            ret += String.format("%-" + w1 + "s | %-" + w2 + "s |\n", tk1.nextToken(), "");
        }
        return ret;
    }

    public static String mergeColumns(String a, String b, String c, int w1, int w2, int w3) {
        String ret = "";
        StringTokenizer tk1 = new StringTokenizer(a, "\n");
        StringTokenizer tk2 = new StringTokenizer(b, "\n");
        StringTokenizer tk3 = new StringTokenizer(c, "\n");
        String s1 = null, s2 = null, s3 = null;
        while (tk1.hasMoreTokens() || tk2.hasMoreTokens() || tk3.hasMoreTokens()) {
            s1 = tk1.hasMoreTokens() ? tk1.nextToken() : "";
            s2 = tk2.hasMoreTokens() ? tk2.nextToken() : "";
            s3 = tk3.hasMoreTokens() ? tk3.nextToken() : "";
            ret += String.format("%-" + w1 + "s | %-" + w2 + "s | %-" + w3 + "s |\n",
                    s1,// (""+s1).replace("\t", "...").replace(" ", "~"),
                    s2,// (""+s2).replace("\t", "...").replace(" ", "~"),
                    s3// (""+s3).replace("\t", "...").replace(" ", "~")
                    );
        }
        return ret;
    }
}
