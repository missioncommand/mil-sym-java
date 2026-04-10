package armyc2.c5isr.renderer.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class C2DLookup {

    private static C2DLookup _instance = null;
    private static Boolean _initCalled = false;

    private static Map<String, String[]> _C2DLookup = null;

    private String TAG = "C2DLookup";


    private C2DLookup() {
        init();
    }

    public static synchronized C2DLookup getInstance() {
        if (_instance == null) {
            _instance = new C2DLookup();
        }
        return _instance;
    }

    private synchronized void init()
    {
        if (_initCalled == false)
        {
            _C2DLookup = new HashMap<>();
            String[] temp = null;
            String delimiter = "\t";

            try {
                //InputStream is = context.getResources().openRawResource(R.raw.genc);
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("data/c2d.txt");

                BufferedReader br = new BufferedReader(new InputStreamReader(is));


                String line = br.readLine();
                while (line != null) {
                    //parse first line
                    temp = line.split(delimiter);

                    if(temp.length >= 4) {
                        _C2DLookup.put(temp[0], temp);
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
     * Take a complete 15 character 2525C symbol code and converts it to 2525D if there is a match.
     * Returns null if no match.
     * @param symbolID 15 character 2525C symbol code.
     * @return 30 character 2525D code or null if no matching symbol
     */
    public String getDCode(String symbolID)
    {
        return getDCode(symbolID, true);
    }

    /**
     * Take a complete 15 character 2525C symbol code and converts it to 2525D if there is a match.
     * Returns null if no match.
     * @param symbolID 15 character 2525C symbol code.
     * @param includeCountryCode 2525C symbols don't display the country label so set to false if you don't want it displayed.
     * @return 30 character 2525D code or null if no matching symbol.
     */
    public String getDCode(String symbolID, boolean includeCountryCode)
    {
        String basicID = SymbolUtilities.getBasicSymbolID2525C(symbolID);
        String newCode = "110000000000000000000000000000";

        String[] parts = _C2DLookup.get(basicID);
        if(parts==null)
            return null;

        //set version
        newCode = SymbolID.setVersion(newCode, Integer.parseInt(parts[1]));
        //set symbol set
        newCode = SymbolID.setSymbolSet(newCode, Integer.parseInt(parts[2]));
        //set entity code
        newCode = SymbolID.setEntityCode(newCode, Integer.parseInt(parts[3]));
        //set ammplifier
        if(!parts[4].isEmpty())
            newCode = SymbolID.setAmplifierDescriptor(newCode, Integer.parseInt(parts[4]));
        //set sector modifier 1
        if(!parts[5].isEmpty()) {
            if(parts[5].length()==2)
                newCode = SymbolID.setModifier1(newCode, (parts[5]));
            else if(parts[5].length()==3)
            {
                newCode = SymbolID.setCommonModifier1(newCode, Integer.parseInt(parts[5].substring(0,1)));
                newCode = SymbolID.setModifier1(newCode, parts[5].substring(1));
            }
        }
        //set sector modifier 2
        if(!parts[6].isEmpty()) {
            if(parts[6].length()==2)
                newCode = SymbolID.setModifier2(newCode, (parts[6]));
            else if(parts[6].length()==3)
            {
                newCode = SymbolID.setCommonModifier2(newCode, Integer.parseInt(parts[6].substring(0,1)));
                newCode = SymbolID.setModifier2(newCode, parts[6].substring(1));
            }
        }

        //get affiliation to set context and affiliation
        char aff = symbolID.charAt(1);

        switch (aff)
        {
            case 'G':
            case 'W':
            case 'M':
            case 'D':
            case 'L':
            case 'J':
            case 'K':
                newCode = SymbolID.setContext(newCode, SymbolID.StandardIdentity_Context_Exercise);
                break;
            default:
                newCode = SymbolID.setContext(newCode, SymbolID.StandardIdentity_Context_Reality);
        }

        //set affiliation
        if(aff=='F' || aff=='D')
            newCode = SymbolID.setAffiliation(newCode, SymbolID.StandardIdentity_Affiliation_Friend);
        else if(aff=='H' || aff=='K')
            newCode = SymbolID.setAffiliation(newCode, SymbolID.StandardIdentity_Affiliation_Hostile_Faker);
        else if(aff=='N' || aff=='L')
            newCode = SymbolID.setAffiliation(newCode, SymbolID.StandardIdentity_Affiliation_Neutral);
        else if(aff=='P' || aff=='G')
            newCode = SymbolID.setAffiliation(newCode, SymbolID.StandardIdentity_Affiliation_Pending);
        else if(aff=='S' || aff=='J')
            newCode = SymbolID.setAffiliation(newCode, SymbolID.StandardIdentity_Affiliation_Suspect_Joker);
        else if(aff=='A' || aff=='M')
            newCode = SymbolID.setAffiliation(newCode, SymbolID.StandardIdentity_Affiliation_AssumedFriend);
        else if(aff=='U' || aff=='W')
            newCode = SymbolID.setAffiliation(newCode, SymbolID.StandardIdentity_Affiliation_Unknown);

        //set status
        char status = symbolID.charAt(3);

        if(status == 'A')
            newCode = SymbolID.setStatus(newCode, SymbolID.Status_Planned_Anticipated_Suspect);
        if(status == 'P')
            newCode = SymbolID.setStatus(newCode, SymbolID.Status_Present);
        if(status == 'C')
            newCode = SymbolID.setStatus(newCode, SymbolID.Status_Present_FullyCapable);
        if(status == 'D')
            newCode = SymbolID.setStatus(newCode, SymbolID.Status_Present_Damaged);
        if(status == 'X')
            newCode = SymbolID.setStatus(newCode, SymbolID.Status_Present_Destroyed);
        if(status == 'F')
            newCode = SymbolID.setStatus(newCode, SymbolID.Status_Present_FullToCapacity);

        String modifier = symbolID.substring(10,12);
        if(modifier.charAt(0)!='H' &&//installation
                modifier.charAt(0)!='M' && //mobility
                modifier.charAt(0)!='N') //towed array
        {
            switch(modifier.charAt(1))
            {
                case 'A':
                    newCode = SymbolID.setAmplifierDescriptor(newCode,SymbolID.Echelon_Team_Crew);
                    break;
                case 'B':
                    newCode = SymbolID.setAmplifierDescriptor(newCode,SymbolID.Echelon_Squad);
                    break;
                case 'C':
                    newCode = SymbolID.setAmplifierDescriptor(newCode,SymbolID.Echelon_Section);
                    break;
                case 'D':
                    newCode = SymbolID.setAmplifierDescriptor(newCode,SymbolID.Echelon_Platoon_Detachment);
                    break;
                case 'E':
                    newCode = SymbolID.setAmplifierDescriptor(newCode,SymbolID.Echelon_Company_Battery_Troop);
                    break;
                case 'F':
                    newCode = SymbolID.setAmplifierDescriptor(newCode,SymbolID.Echelon_Battalion_Squadron);
                    break;
                case 'G':
                    newCode = SymbolID.setAmplifierDescriptor(newCode,SymbolID.Echelon_Regiment_Group);
                    break;
                case 'H':
                    newCode = SymbolID.setAmplifierDescriptor(newCode,SymbolID.Echelon_Brigade);
                    break;
                case 'I':
                    newCode = SymbolID.setAmplifierDescriptor(newCode,SymbolID.Echelon_Division);
                    break;
                case 'J':
                    newCode = SymbolID.setAmplifierDescriptor(newCode,SymbolID.Echelon_Corps_MEF);
                    break;
                case 'K':
                    newCode = SymbolID.setAmplifierDescriptor(newCode,SymbolID.Echelon_Army);
                    break;
                case 'L':
                    newCode = SymbolID.setAmplifierDescriptor(newCode,SymbolID.Echelon_ArmyGroup_Front);
                    break;
                case 'M':
                    newCode = SymbolID.setAmplifierDescriptor(newCode,SymbolID.Echelon_Region_Theater);
                    break;
                case 'N':
                    newCode = SymbolID.setAmplifierDescriptor(newCode,SymbolID.Echelon_Region_Command);
                    break;

            }

            switch (modifier.charAt(0))
            {
                case 'A':
                    newCode = SymbolID.setHQTFD(newCode,SymbolID.HQTFD_Headquarters);
                    break;
                case 'B':
                    newCode = SymbolID.setHQTFD(newCode,SymbolID.HQTFD_TaskForce_Headquarters);
                    break;
                case 'C':
                    newCode = SymbolID.setHQTFD(newCode,SymbolID.HQTFD_FeintDummy_Headquarters);
                    break;
                case 'D':
                    newCode = SymbolID.setHQTFD(newCode,SymbolID.HQTFD_FeintDummy_TaskForce_Headquarters);
                    break;
                case 'E':
                    newCode = SymbolID.setHQTFD(newCode,SymbolID.HQTFD_TaskForce);
                    break;
                case 'F':
                    newCode = SymbolID.setHQTFD(newCode,SymbolID.HQTFD_FeintDummy);
                    break;
                case 'G':
                    newCode = SymbolID.setHQTFD(newCode,SymbolID.HQTFD_FeintDummy_TaskForce);
                    break;
            }
        }
        else
        {
            if(modifier.equals("HB"))
                SymbolID.setHQTFD(newCode,SymbolID.HQTFD_FeintDummy);
            else if(modifier.charAt(0)=='M')
            {
                switch(modifier.charAt(1))
                {
                    case 'O':
                        newCode = SymbolID.setAmplifierDescriptor(newCode, SymbolID.Mobility_WheeledLimitedCrossCountry);
                        break;
                    case 'P':
                        newCode = SymbolID.setAmplifierDescriptor(newCode, SymbolID.Mobility_WheeledCrossCountry);
                        break;
                    case 'Q':
                        newCode = SymbolID.setAmplifierDescriptor(newCode, SymbolID.Mobility_Tracked);
                        break;
                    case 'R':
                        newCode = SymbolID.setAmplifierDescriptor(newCode, SymbolID.Mobility_Wheeled_Tracked);
                        break;
                    case 'S':
                        newCode = SymbolID.setAmplifierDescriptor(newCode, SymbolID.Mobility_Towed);
                        break;
                    case 'T':
                        newCode = SymbolID.setAmplifierDescriptor(newCode, SymbolID.Mobility_Rail);
                        break;
                    case 'U':
                        newCode = SymbolID.setAmplifierDescriptor(newCode, SymbolID.Mobility_OverSnow);
                        break;
                    case 'V':
                        newCode = SymbolID.setAmplifierDescriptor(newCode, SymbolID.Mobility_Sled);
                        break;
                    case 'W':
                        newCode = SymbolID.setAmplifierDescriptor(newCode, SymbolID.Mobility_PackAnimals);
                        break;
                    case 'X':
                        newCode = SymbolID.setAmplifierDescriptor(newCode, SymbolID.Mobility_Barge);
                        break;
                    case 'Y':
                        newCode = SymbolID.setAmplifierDescriptor(newCode, SymbolID.Mobility_Amphibious);
                        break;
                }
            }
            else if(modifier.charAt(0)=='N')
            {
                switch(modifier.charAt(1)) {
                    case 'S':
                        newCode = SymbolID.setAmplifierDescriptor(newCode, SymbolID.Mobility_ShortTowedArray);
                        break;
                    case 'L':
                        newCode = SymbolID.setAmplifierDescriptor(newCode, SymbolID.Mobility_LongTowedArray);
                        break;
                }
            }
        }


        //country code
        if(includeCountryCode)
            newCode = newCode.substring(0,27) + GENCLookup.getInstance().get3DigitCode(symbolID.substring(12,14));

        return newCode;
    }

}
