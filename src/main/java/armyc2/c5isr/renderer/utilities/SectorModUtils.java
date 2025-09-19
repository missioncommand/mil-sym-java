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
 *
 */
public class SectorModUtils
{
    private static SectorModUtils _instance = null;
    private static Boolean _initCalled = false;

    private String TAG = "SectorModUtils";
    private List<String> _IDList = new ArrayList<String>();

    private static Map<String, String> _sectorMods = new HashMap<>();
    private static Map<String, ArrayList<String[]>> _sectorModLists = new HashMap<>();


    private SectorModUtils() {
        init();
    }

    public static synchronized SectorModUtils getInstance() {
        if (_instance == null) {
            _instance = new SectorModUtils();
        }
        return _instance;
    }

    private synchronized void init()
    {
        if (_initCalled == false)
        {

            try
            {
                loadData("data/smd.txt", SymbolID.Version_2525Dch1);
                loadData("data/sme.txt", SymbolID.Version_2525Ech1);
                _initCalled = true;

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     *
     * @param path like "data/smd.txt"
     */
    private void loadData(String path, int version)
    {
        String[] temp = null;
        String delimiter = "\t";
        int ver = 0;
        int ss = -1;
        int l = 0;
        String code = "00";
        String name = "";
        String id = null;
        StringBuilder sb = null;
        ArrayList<String[]> sectorList = null;


        try {

            if(version <= SymbolID.Version_2525Dch1)
                ver = SymbolID.Version_2525Dch1;
            else
                ver = SymbolID.Version_2525Ech1;

            //get sector mods
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(path);
            if(is != null)
            {
                String[] entry = null;
                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                String line = br.readLine();
                while (line != null)
                {
                    //parse first line
                    temp = line.split(delimiter);
                    if(temp.length == 2)
                    {
                        if(sectorList != null && sectorList.size() > 0)
                        {//add completed list to sectorModLists
                            sb = new StringBuilder();
                            sb.append(ver).append("-").append(ss).append("-").append(l);
                            id = sb.toString();
                            _sectorModLists.put(id,sectorList);
                        }

                        //get symbol set
                        ss = Integer.parseInt(temp[0].split(" ")[0]);
                        //get location; 1=top, 2=bottom
                        l = Integer.parseInt(temp[1]);
                        //start new list
                        sectorList = new ArrayList<>();
                    }
                    else if(temp != null && temp.length >= 3)
                    {
                        name = temp[0];
                        code = temp[2];
                        if(code.length()==1)
                            code = "0" + code;

                        sb = new StringBuilder();
                        id = sb.append(ver).append("-").append(ss).append("-").append(l).append("-").append(code).toString();
                        entry = new String[2];
                        entry[0] = code;
                        entry[1] = name;
                        sectorList.add(entry);
                        _sectorMods.put(id, name);
                    }
                    //read next line for next loop
                    line = br.readLine();
                }
                br.close();
                is.close();
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     *
     * @param version like SymbolID.Version_2525Dch1 or SymbolID.Version_2525Ech1  Only tracks sector mods for these 2 versions.
     * @param symbolSet  like SymbolID.SymbolSet_Air; use 0 for Common Modifiers as they are not tied to a symbol set.
     * @param location 1 for top, 2 for bottom
     * @return and ArrayList of String[] like ["00","Unspecified"],["01","Attack/Strike"]
     */
    public ArrayList<String[]> getSectorModList(int version, int symbolSet, int location)
    {
        StringBuilder sb = new StringBuilder();
        int ver = SymbolID.Version_2525Dch1;
        if(version >= SymbolID.Version_2525E )
            ver = SymbolID.Version_2525Ech1;

        int ss = symbolSet;
        if (ss > 50 && ss < 60)
            ss = 50;

        sb.append(ver).append("-").append(ss).append("-").append(location);
        String id = sb.toString();
        if(_sectorModLists.containsKey(id))
            return _sectorModLists.get(sb.toString());
        else
        {
            String[] entry = {"00","Unspecified"};
            ArrayList<String[]> al = new ArrayList<>();
            al.add(entry);
            return al;
        }
    }

    /**
     *
     * @param version like SymbolID.Version_2525Dch1 or SymbolID.Version_2525Ech1  Only tracks sector mods for these 2 versions.
     * @param symbolSet  like SymbolID.SymbolSet_Air; use 0 for Common Modifiers as they are not tied to a symbol set.
     * @param location 1 for top, 2 for bottom
     * @param code like "01" or "100"
     */
    public String getName(int version, int symbolSet, int location, String code)
    {
        StringBuilder sb = new StringBuilder();
        int ver = SymbolID.Version_2525Dch1;
        if(version >= SymbolID.Version_2525E )
            ver = SymbolID.Version_2525Ech1;

        int ss = symbolSet;
        if (ss > 50 && ss < 60)
            ss = 50;

        //verify code is the correct length
        if(ss > 0 && code.length() != 2)
        {
            if(code.length() > 2)
                code = code.substring(0, 2);
            else
            {
                while(code.length()<2)
                    code = "0" + code;
            }
        }
        else if(ss == 0 && code.length() != 3)
        {
            if (code.length() > 3)
                code = code.substring(0, 3);
            else
            {
                if (code.startsWith("0"))
                    code = "1" + code;
                while (code.length() < 3)
                    code = "0" + code;
            }
        }

        sb.append(ver).append("-").append(ss).append("-").append(location).append("-").append(code);
        String id = sb.toString();

        return _sectorMods.getOrDefault(id, "");
    }
}
