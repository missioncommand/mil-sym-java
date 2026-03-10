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
    private static Map<String, String> _GENCLookupAlpha = null;
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
            _GENCLookupAlpha = new HashMap<>();
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

                    if(temp.length >= 3) {
                        _GENCLookup.put(Integer.valueOf(temp[2]), temp[1]);
                        if(temp[0].length()==2)
                            _GENCLookupAlpha.put((temp[0]), temp[2]);
                    }

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

    /**
     *
     * @param id 3 digit code from 2525D+ symbol code
     * @return
     */
    public String get3CharCode(int id)
    {
        if(_GENCLookup != null && _GENCLookup.containsKey(id))
        {
            return _GENCLookup.get(id);
        }
        return "";
    }

    /**
     *
     * @param id 2 char string from 2525C symbol code
     * @return
     */
    public String get3DigitCode(String id)
    {
        if(_GENCLookupAlpha != null && _GENCLookupAlpha.containsKey(id))
        {
            String code = _GENCLookupAlpha.get(id);
            while(code.length()<3)
                code = "0" + code;
            return code;
        }
        return "000";
    }
}
