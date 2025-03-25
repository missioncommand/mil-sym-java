package armyc2.c5isr.renderer.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class that takes the 3 digit country code from the symbol ID and returns the 3 character string representation
 * of that country. For example, 840 turns into "USA" for the United States.
 */
public class GENCLookup
{
    private static GENCLookup _instance = null;
    private static Boolean _initCalled = false;

    private static Map<Integer, String> _GENCLookup = null;
    private String TAG = "GENCLookup";
    private List<String> _IDList = new ArrayList<String>();

    private GENCLookup() {
        init();
    }

    public static synchronized GENCLookup getInstance() {
        if (_instance == null) {
            _instance = new GENCLookup();
        }
        return _instance;
    }

    private synchronized void init()
    {
        if (_initCalled == false)
        {
            _GENCLookup = new HashMap<>();
            String[] temp = null;
            String delimiter = "\t";

            try {
                //InputStream is = context.getResources().openRawResource(R.raw.genc);
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("data/genc.txt");

                BufferedReader br = new BufferedReader(new InputStreamReader(is));


                String line = br.readLine();
                while (line != null) {
                    //parse first line
                    temp = line.split(delimiter);

                    if(temp != null && temp.length >= 2 && SymbolUtilities.isNumber(temp[1]))
                        _GENCLookup.put(Integer.valueOf(temp[1]),temp[0]);

                    //read next line for next loop
                    line = br.readLine();
                }

                _initCalled = true;
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public String get3CharCode(int id)
    {
        if(_GENCLookup != null && _GENCLookup.containsKey(id))
        {
            return _GENCLookup.get(id);
        }
        return "";
    }
}
