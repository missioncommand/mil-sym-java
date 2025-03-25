/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package armyc2.c5isr.renderer.utilities;

//import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.*;

/**
 * Error Logging class for Renderer
 * 
 */
public class ErrorLogger {

    // private static ErrorLogger _el = null;
    public static final String LoggerName = "WebRenderer ErrorLogger";
    //private static Logger _Logger = null;//
    //private static final Logger _Logger = Logger.getLogger(LoggerName);
    private static Level _level = Level.INFO;
    //private static java.util.logging.FileHandler fh = null;
    private static Boolean _LoggingEnabled = false;
    //private static String _LoggingPath = System.getProperty("user.dir");
    //fate format: Nov 19, 2012 11:41:40 AM
    private static SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss aa ");

    /*
    private ErrorLogger()
    {
        Init();
    }


    private static synchronized ErrorLogger getInstance()
    {

        //if(_Logger == null)
        if(_el == null)
        {
            try
            {
                _el = new ErrorLogger();

            }
            catch(Exception exc)
            {
                System.err.println(exc.getMessage());
                //JOptionPane.showMessageDialog(null, ioe.getMessage(), "Message", JOptionPane.PLAIN_MESSAGE);
            }
        }

        return _el;
    }

    private void Init()
    {
        try
        {
            if(_Logger != null)
                _Logger.setLevel(Level.INFO);
        }
        catch(Exception exc)
        {
            System.err.println(exc.getMessage());
            //JOptionPane.showMessageDialog(null, ioe.getMessage(), "Message", JOptionPane.PLAIN_MESSAGE);
        }
    }//*/

    /**
     * True if logging is enabled
     * @return {@link Boolean}
     */
    public static Boolean getLoggingStatus()
    {
        return _LoggingEnabled;
    }

    /**
     * Takes a throwable and puts it's stacktrace into a string.
     * @param thrown {@link Throwable}
     * @return {@link String}
     */
    public static String getStackTrace(Throwable thrown)
    {
        try
        {
            /*
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            thrown.printStackTrace(printWriter);
            return writer.toString();*/
            String eol = System.getProperty("line.separator");
            StringBuilder sb = new StringBuilder();
            sb.append(thrown.toString());
            sb.append(eol);
            for(StackTraceElement element : thrown.getStackTrace())
            {
                sb.append("        at ");
                sb.append(element);
                sb.append(eol);
            }
            return sb.toString();
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("ErrorLogger", "getStackTrace", exc);
            return "Error - couldn't retrieve stack trace";
        }
    }

    /**
     * TRUE: Creates a file handler that will log message to a file.
     * FALSE: logging just goes to console.
     * @param enable {@link Boolean}
     */
    public static void EnableLogging(Boolean enable)
    {/*
        //_LoggingEnabled = enable;
        if(enable && _LoggingEnabled == false)
        {
            if(fh == null)
            {
                try
                {
                    fh = new java.util.logging.FileHandler(getFileName(),true);
                    fh.setFormatter(new SimpleFormatter());//comment out and will default to XML
                }
                catch(IOException ioe)
                {
                    //JOptionPane.showMessageDialog(null, ioe.getMessage(), "Message", JOptionPane.PLAIN_MESSAGE);
                }

            }
            getInstance();
            fh.setLevel(_Logger.getLevel());
            _Logger.addHandler(fh);
        }
        else
        {
            if(_LoggingEnabled && fh != null)
            {
                _Logger.removeHandler(fh);
                fh=null;
            }
        }//*/
        _LoggingEnabled = enable;
    }

    /**
     * Folder location to store the log file.
     * Defaults to "System.getProperty("user.dir")"
     * @param path {@link String}
     * @deprecated
     */
    public static void setLoggingPath(String path)
    {/*
        _LoggingPath = path;
        File foo = new File(path);
        if(foo.exists() && foo.isDirectory())
        {
            if(_LoggingEnabled)
            {   //toggle logging to reset the file path.
                EnableLogging(false);
                EnableLogging(true);
            }
        }
        else
        {
            ErrorLogger.LogMessage("ErrorLogger","setLoggingPath",
                    "\"" + path + "\" doesn't exist, logging path not changed.",
                    Level.WARNING);
        }//*/
    }

    /**
     * clears log files that are beyond a passed number of days old
     * @param DaysOld {@link Integer}
     * @deprecated
     */
    public static void CleanupOldFiles(int DaysOld)
    {/*

        Calendar Cal = new GregorianCalendar();
        Calendar CalLastModified = new GregorianCalendar();
        Cal.add(Calendar.DAY_OF_MONTH, -DaysOld);//remove anything this many days old

        String path = _LoggingPath;//System.getProperty("user.dir");
        File lookup = new File(path);
        File[] results = lookup.listFiles();
        for(File foo : results)
        {
            if(foo.getName().startsWith("TBCRendererLog"))
            {
                long age = foo.lastModified();

                CalLastModified.setTimeInMillis(age);
                if(Cal.after(CalLastModified))
                    foo.delete();
            }
        }//*/
    }

    /**
     * Set minimum level at which an item can be logged.
     * In descending order:
     * Severe
     * Warning
     * Info
     * Config
     * Fine
     * Finer
     * Finest
     * @param newLevel {@link Level}
     */
    public static synchronized void setLevel(Level newLevel)
    {
        setLevel(newLevel, false);
    }

    /**
     * Set minimum level at which an item can be logged.
     * In descending order:
     * Severe
     * Warning
     * Info
     * Config
     * Fine
     * Finer
     * Finest
     * @param newLevel {@link Level}
     * @param setConsoleHandler logger could be set to FINE but the console
     * handler could be set to INFO.  In that case, anything logged at FINE
     * wouldn't show because it'd get blocked by the console handler.  Set to
     * "true" to make sure the console handler will let you log at the level
     * you want.  If you're only concerned with the log file, you can leave
     * "false"
     */
    public static synchronized void setLevel(Level newLevel, Boolean setConsoleHandler)
    {
        _level = newLevel;

        /*
        _Logger.setLevel(newLevel);//set logger.

        if(fh != null)//handler that logs to file
            fh.setLevel(newLevel);

        //have to set top logger/consle handler or messages will still
        //get blocked.

        //get the top logger
        if(setConsoleHandler)
        {
            Logger topLogger = java.util.logging.Logger.getLogger("");
            //Handler for console (resuse if it already exists)
            Handler consoleHandler = null;
            //see if there already is a console handler
            for (Handler handler : topLogger.getHandlers())
            {
                if(handler instanceof ConsoleHandler)
                {
                    //found the console handler
                    consoleHandler = handler;
                    break;
                }
            }

            if(consoleHandler == null)
            {
                //there was no console handler found, create a new one
                consoleHandler = new ConsoleHandler();
                topLogger.addHandler(consoleHandler);
            }
            //set console handler to new level
            consoleHandler.setLevel(newLevel);
        }//*/
    }

    /**
     * Specify whether or not this logger should send its output
     * to it's parent Logger.  This means that any LogRecords will
     * also be written to the parent's Handlers, and potentially
     * to its parent, recursively up the namespace.
     * Defaults to true;
     *
     * @param useParentHandlers   true if output is to be sent to the
     *		logger's parent.
     * @exception  SecurityException  if a security manager exists and if
     *             the caller does not have LoggingPermission("control").
     */
    public static void setUseParentHandlers(boolean useParentHandlers)
    {
        //_Logger.setUseParentHandlers(useParentHandlers);
    }

    /**
     * Gets the java.util.logging.Level that the logger is set to.
     * @return {@link Level}
     */
    public static synchronized Level getLevel()
    {
        return _level;
        //return _Logger.getLevel();
    }

    /**
     *
     * @return {@link String}
     * @deprecated
     */
    private static String getFileName()
    {
        //String path = _LoggingPath;//System.getProperty("user.dir");
        String fileName = "";
        /*
        SimpleDateFormat dateFormat = new SimpleDateFormat("_MMMdd");
        fileName = "TBCRendererLog" + dateFormat.format(new Date()) + ".txt";
        fileName = path + "\\" + fileName;
        //fileName = path.substring(0, 2) + "\\" + fileName;//*/
        return fileName;
    }

    /**
     * Log a method entry.
     * <p>
     * This is a convenience method that can be used to log entry
     * to a method.  A LogRecord with message "ENTRY", log level
     * FINER, and the given sourceMethod and sourceClass is logged.
     * <p>
     * @param   sourceClass    name of class that issued the logging request
     * @param   sourceMethod   name of method that is being entered
     */
    public static void Entering(String sourceClass, String sourceMethod)
    {
        //_Logger.entering(sourceClass, sourceMethod);
        if(_level.intValue() <= Level.FINER.intValue())
        {
            System.out.println("Entering: " + sourceClass + "." + sourceMethod);
        }
    }

    /**
     * Log a method entry, with one parameter.
     * <p>
     * This is a convenience method that can be used to log entry
     * to a method.  A LogRecord with message "ENTRY {0}", log level
     * FINER, and the given sourceMethod, sourceClass, and parameter
     * is logged.
     * <p>
     * @param   sourceClass    name of class that issued the logging request
     * @param   sourceMethod   name of method that is being entered
     * @param   param1	       parameter to the method being entered
     */
    public static void Entering(String sourceClass, String sourceMethod, Object param1)
    {
        //_Logger.entering(sourceClass, sourceMethod,param1);
        if(_level.intValue() <= Level.FINER.intValue())
        {
            System.out.println("Entering: " + sourceClass + "." + sourceMethod +
                    " - " + String.valueOf(param1));
        }
    }

    /**
     * Log a method entry, with an array of parameters.
     * <p>
     * This is a convenience method that can be used to log entry
     * to a method.  A LogRecord with message "ENTRY" (followed by a
     * format {N} indicator for each entry in the parameter array),
     * log level FINER, and the given sourceMethod, sourceClass, and
     * parameters is logged.
     * <p>
     * @param   sourceClass    name of class that issued the logging request
     * @param   sourceMethod   name of method that is being entered
     * @param   params	       array of parameters to the method being entered
     */
    public static void Entering(String sourceClass, String sourceMethod, Object[] params)
    {
        //_Logger.entering(sourceClass, sourceMethod,params);
        if(_level.intValue() <= Level.FINER.intValue())
        {
            System.out.println("Entering: " + sourceClass + "." + sourceMethod + "with params:");
            if(params != null)
            {
                for(Object param : params)
                {
                    System.out.println(String.valueOf(param));
                }
            }
        }
    }

    /**
     * Log a method return.
     * <p>
     * This is a convenience method that can be used to log returning
     * from a method.  A LogRecord with message "RETURN", log level
     * FINER, and the given sourceMethod and sourceClass is logged.
     * <p>
     * @param   sourceClass    name of class that issued the logging request
     * @param   sourceMethod   name of the method
     */
    public static void Exiting(String sourceClass, String sourceMethod)
    {
        //_Logger.exiting(sourceClass, sourceMethod);
        if(_level.intValue() <= Level.FINER.intValue())
        {
            System.out.println("Exiting: " + sourceClass + "." + sourceMethod);
        }
    }

    /**
     * Log a method return, with result object.
     * <p>
     * This is a convenience method that can be used to log returning
     * from a method.  A LogRecord with message "RETURN {0}", log level
     * FINER, and the gives sourceMethod, sourceClass, and result
     * object is logged.
     * <p>
     * @param   sourceClass    name of class that issued the logging request
     * @param   sourceMethod   name of the method
     * @param   result  Object that is being returned
     */
    public static void Exiting(String sourceClass, String sourceMethod, Object result)
    {
        //_Logger.exiting(sourceClass, sourceMethod, result);
        if(_level.intValue() <= Level.FINER.intValue())
        {
            System.out.println("Entering: " + sourceClass + "." + sourceMethod +
                    " - " + String.valueOf(result));
        }
    }


    /**
     * Defaults to Level.INFO
     * @param message {@link String}
     */
    public static void LogMessage(String message)
    {
        LogMessage(message, Level.INFO, false);
    }

    /**
     * Defaults to Level.INFO
     * @param message {@link String}
     * @param showMessageBox (@link {@link Boolean}
     */
    public static void LogMessage(String message, Boolean showMessageBox)
    {
        LogMessage(message, Level.INFO, showMessageBox);
    }

    /**
     *
     * @param message {@link String}
     * @param lvl {@link Level}
     * @param showMessageBox {@link Boolean}
     */
    public static void LogMessage(String message, Level lvl, Boolean showMessageBox)
    {
        if(lvl.intValue() >= _level.intValue())
        {
            System.out.println(sdf.format(new Date()) + LoggerName);
            System.out.println("INFO: " + message);
        }
        /*_Logger.log(lvl, message);
        if(showMessageBox==true &&
                lvl.intValue() >= _Logger.getLevel().intValue() &&
                lvl.intValue() < Integer.MAX_VALUE)
        {
           JOptionPane.showMessageDialog(null, message, "Message", JOptionPane.PLAIN_MESSAGE);
        }//*/
    }

    public static void LogMessage(String sourceClass, String sourceMethod, String message)
    {
        LogMessage(sourceClass, sourceMethod, message, Level.INFO, false);
    }

    public static void LogMessage(String sourceClass, String sourceMethod, String message, Boolean showMessageBox)
    {
        LogMessage(sourceClass, sourceMethod, message, Level.INFO, showMessageBox);
    }

    public static void LogMessage(String sourceClass, String sourceMethod, String message, Level lvl)
    {
        LogMessage(sourceClass, sourceMethod, message, lvl, false);
    }

    public static void LogMessage(String sourceClass, String sourceMethod, String message, Level lvl, Boolean showMessageBox)
    {
        if(lvl.intValue() >= _level.intValue())
        {
            System.out.println(sdf.format(new Date()) + sourceClass + "." + sourceMethod);
            System.out.println(lvl.toString() + ": " + message);
        }
        /*
        _Logger.logp(lvl, sourceClass, sourceMethod, message);
        if(showMessageBox==true &&
                lvl.intValue() >= _Logger.getLevel().intValue() &&
                lvl.intValue() < Integer.MAX_VALUE)
        {
           //JOptionPane.showMessageDialog(null, message, "Message", JOptionPane.PLAIN_MESSAGE);
        }//*/
    }

    public static void LogMessage(String sourceClass, String sourceMethod, String message, Level lvl, Object param1, Boolean showMessageBox)
    {
        Object[] params = new Object[1];
        params[0] = param1;
        LogMessage(sourceClass, sourceMethod, message, lvl, params, showMessageBox);
    }

    public static void LogMessage(String sourceClass, String sourceMethod, String message, Level lvl, Object params[], Boolean showMessageBox)
    {
        if(lvl.intValue() >= _level.intValue())
        {
            System.out.println(sdf.format(new Date()) + sourceClass + "." + sourceMethod);
            System.out.println(lvl.toString() + ": " + message);
            for(Object param : params)
            {
                System.out.println(String.valueOf(param));
            }
        }
        /*
        _Logger.logp(lvl, sourceClass, sourceMethod, message, params);
        if(showMessageBox==true &&
                lvl.intValue() >= _Logger.getLevel().intValue() &&
                lvl.intValue() < Integer.MAX_VALUE)
        {
           //JOptionPane.showMessageDialog(null, message, "Message", JOptionPane.PLAIN_MESSAGE);
        }//*/
    }

    public static void LogException(String sourceClass, String sourceMethod, Exception exc)
    {
        LogException(sourceClass, sourceMethod, exc, Level.INFO, false);
    }

    public static void LogException(String sourceClass, String sourceMethod, Exception exc, Boolean showMessageBox)
    {
        LogException(sourceClass, sourceMethod, exc, Level.INFO, showMessageBox);
    }

    public static void LogException(String sourceClass, String sourceMethod, Exception exc, Level lvl)
    {
        LogException(sourceClass, sourceMethod, exc, lvl, false);
    }

    public static void LogException(String sourceClass, String sourceMethod, Exception exc, Level lvl, Boolean showMessageBox)
    {
        if(lvl.intValue() >= _level.intValue())
        {
            System.err.println(sdf.format(new Date()) + sourceClass + "." + sourceMethod);
            System.err.println(lvl.toString() + ": " + exc.getMessage());
            System.err.println(getStackTrace(exc));
        }
        /*
        _Logger.logp(lvl, sourceClass, sourceMethod, exc.getMessage(),exc);
        //_Logger.logp(Level.INFO, sourceClass, sourceMethod, exc.getMessage());

        if(exc != null && showMessageBox==true &&
                lvl.intValue() >= _Logger.getLevel().intValue() &&
                lvl.intValue() < Integer.MAX_VALUE)//shouldn't be showing if logging off
        {
           //JOptionPane.showMessageDialog(null, exc.getMessage(), "Exception: " + sourceClass + "." + sourceMethod, JOptionPane.PLAIN_MESSAGE);
        }//*/
    }

    public static String PrintList(List list)
    {
        String message = "";
        for(Object item : list)
        {
            message += String.valueOf(item) + "\n";
        }
        return message;
    }

    public static String PrintObjectMap(Map<String, Object> map)
    {

        String message = "";
        if(map != null)
        {
            for(Map.Entry<String,Object> entry: map.entrySet())
            {
                String key = entry.getKey();
                String val = String.valueOf(entry.getValue());

                message += key + " : " + val + "\n";
            }
        }
        //ErrorLogger.LogMessage(message);
        return message;
    }

    public static String PrintStringMap(Map<String, String> map)
    {
        String message = "";
        if(map != null)
        {
            for(Map.Entry<String,String> entry: map.entrySet())
            {
                String key = entry.getKey();
                String val = String.valueOf(entry.getValue());

                message += key + " : " + val + "\n";
            }
        }
        //ErrorLogger.LogMessage(message);
        return message;
    }

}
