package armyc2.c5isr.renderer.utilities;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Class that holds all the {@link MSInfo} objects with symbol information
 */
public class MSLookup {

    private static MSLookup _instance = null;
    private static Boolean _initCalled = false;

    private static Map<String, MSInfo> _MSLookupD = null;
    private static Map<String, MSInfo> _MSLookupE = null;
    private String TAG = "MSLookup";
    private List<String> _IDListD = null;
    private List<String> _IDListE = null;


    /*
     * Holds SymbolDefs for all symbols. (basicSymbolID, Description, MinPoint, MaxPoints, etc...) Call
     * getInstance().
     *
     */
    private MSLookup() {
        init();
    }

    public static synchronized MSLookup getInstance() {
        if (_instance == null) {
            _instance = new MSLookup();
        }
        return _instance;
    }

    private synchronized void init() {
        if (_initCalled == false) {
            _MSLookupD = new HashMap<>();
            _MSLookupE = new HashMap<>();
            _IDListD = new ArrayList<>();
            _IDListE = new ArrayList<>();

            try {
                InputStream isD = this.getClass().getClassLoader().getResourceAsStream("data/msd.txt");
                InputStream isE = this.getClass().getClassLoader().getResourceAsStream("data/mse.txt");

                BufferedReader brD = new BufferedReader(new InputStreamReader(isD,"UTF8"));
                populateLookup(brD, SymbolID.Version_2525Dch1);
                brD.close();

                BufferedReader brE = new BufferedReader(new InputStreamReader(isE,"UTF8"));
                populateLookup(brE, SymbolID.Version_2525E);
                brE.close();

                _initCalled = true;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void populateLookup(BufferedReader br, int version) {
        Map<String, MSInfo> lookup;
        List<String> list;
        String[] temp = null;
        String delimiter = "\t";

        try {
            if (version == SymbolID.Version_2525E) {
                lookup = _MSLookupE;
                list = _IDListE;
            } else {
                lookup = _MSLookupD;
                list = _IDListD;
            }

            String id = null;
            String ss = null;
            String e = null;
            String et = null;
            String est = null;
            String ec = null;
            String g = null;
            String dr = null;
            String m = null;
            String[] modifiers = null;

            String line = br.readLine();
            while (line != null) {
                //parse first line
                temp = line.split(delimiter);

                if (temp.length < 5)
                    ec = "000000";
                else
                    ec = temp[4];

                if(ec.equals("0"))
                    ec = "000000";

                if (temp.length < 4)
                    est = "";
                else
                    est = temp[3];
                if (temp.length < 3)
                    et = "";
                else if (est.equals(""))
                    et = temp[2];
                if (temp.length < 2)
                    e = "";
                else if (et.equals(""))
                    e = temp[1];

                if (!temp[0].equals(""))
                    ss = temp[0];

                id = ss + ec;

                if (!ec.equals("000000")) {
                    if (temp.length >= 7) {//Control Measures and METOCS
                        if (temp.length >= 8) {
                            m = temp[7];//modifiers
                            if (m != null && !m.equals(""))
                                //m = m.replace("\"","");
                                modifiers = m.split(",");
                            else
                                modifiers = null;
                        }
                        g = temp[5];//geometry
                        dr = temp[6];//draw rule
                        lookup.put(id, new MSInfo(version, ss, e, et, est, ec, g, dr, populateModifierList(modifiers)));
                    } else {//Everything else
                        //_MSLookupD.put(id, new MSInfo(ss, e, et, est, ec));
                        lookup.put(id, new MSInfo(version, ss, e, et, est, ec, populateModifierList(ss, ec, version)));
                    }
                    list.add(id);
                }

                modifiers = null;

                //read next line for next loop
                line = br.readLine();
            }

        } catch (Exception exc) {
            System.out.println(exc.getMessage());
        }

    }

    private ArrayList<String> populateModifierList(String[] modifiers) {
        ArrayList<String> mods = new ArrayList<String>();
        if (modifiers != null && modifiers.length > 0) {
            for (String mod : modifiers) {
                mods.add(Modifiers.getModifierKey(mod));
            }
        }
        return mods;
    }

    private ArrayList<String> populateModifierList(String symbolSet, String ec, int version) {
        int ss = Integer.valueOf(symbolSet);
        ArrayList<String> modifiers = new ArrayList<String>();

        if(version >= SymbolID.Version_2525E)
        {
            switch (ss) {
                case SymbolID.SymbolSet_LandUnit:
                case SymbolID.SymbolSet_LandCivilianUnit_Organization:
                    modifiers.add(Modifiers.A_SYMBOL_ICON);
                    modifiers.add(Modifiers.B_ECHELON);
                    modifiers.add(Modifiers.D_TASK_FORCE_INDICATOR);
                    modifiers.add(Modifiers.F_REINFORCED_REDUCED);
                    modifiers.add(Modifiers.G_STAFF_COMMENTS);
                    modifiers.add(Modifiers.H_ADDITIONAL_INFO_1);
                    modifiers.add(Modifiers.J_EVALUATION_RATING);
                    modifiers.add(Modifiers.K_COMBAT_EFFECTIVENESS);
                    modifiers.add(Modifiers.L_SIGNATURE_EQUIP);
                    modifiers.add(Modifiers.M_HIGHER_FORMATION);
                    modifiers.add(Modifiers.P_IFF_SIF_AIS);
                    modifiers.add(Modifiers.Q_DIRECTION_OF_MOVEMENT);
                    modifiers.add(Modifiers.S_HQ_STAFF_INDICATOR);
                    modifiers.add(Modifiers.T_UNIQUE_DESIGNATION_1);
                    modifiers.add(Modifiers.W_DTG_1);
                    modifiers.add(Modifiers.X_ALTITUDE_DEPTH);
                    modifiers.add(Modifiers.Y_LOCATION);
                    modifiers.add(Modifiers.Z_SPEED);
                    if(ss==SymbolID.SymbolSet_LandUnit && ec.equals("110000"))
                        modifiers.add(Modifiers.AA_SPECIAL_C2_HQ);
                    modifiers.add(Modifiers.AB_FEINT_DUMMY_INDICATOR);
                    modifiers.add(Modifiers.AD_PLATFORM_TYPE);
                    modifiers.add(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME);
                    modifiers.add(Modifiers.AF_COMMON_IDENTIFIER);
                    modifiers.add(Modifiers.AH_AREA_OF_UNCERTAINTY);
                    modifiers.add(Modifiers.AI_DEAD_RECKONING_TRAILER);
                    modifiers.add(Modifiers.AJ_SPEED_LEADER);
                    modifiers.add(Modifiers.AK_PAIRING_LINE);
                    modifiers.add(Modifiers.AL_OPERATIONAL_CONDITION);
                    modifiers.add(Modifiers.AO_ENGAGEMENT_BAR);
                    modifiers.add(Modifiers.AQ_GUARDED_UNIT);
                    modifiers.add(Modifiers.AS_COUNTRY);
                    break;
                case SymbolID.SymbolSet_LandEquipment:
                case SymbolID.SymbolSet_SignalsIntelligence:
                    modifiers.add(Modifiers.A_SYMBOL_ICON);
                    modifiers.add(Modifiers.C_QUANTITY);
                    modifiers.add(Modifiers.G_STAFF_COMMENTS);
                    modifiers.add(Modifiers.H_ADDITIONAL_INFO_1);
                    modifiers.add(Modifiers.J_EVALUATION_RATING);
                    modifiers.add(Modifiers.K_COMBAT_EFFECTIVENESS);
                    modifiers.add(Modifiers.L_SIGNATURE_EQUIP);
                    modifiers.add(Modifiers.M_HIGHER_FORMATION);
                    modifiers.add(Modifiers.N_HOSTILE);
                    modifiers.add(Modifiers.P_IFF_SIF_AIS);
                    modifiers.add(Modifiers.Q_DIRECTION_OF_MOVEMENT);
                    modifiers.add(Modifiers.R_MOBILITY_INDICATOR);
                    modifiers.add(Modifiers.T_UNIQUE_DESIGNATION_1);
                    modifiers.add(Modifiers.V_EQUIP_TYPE);
                    modifiers.add(Modifiers.W_DTG_1);
                    modifiers.add(Modifiers.X_ALTITUDE_DEPTH);
                    modifiers.add(Modifiers.Y_LOCATION);
                    modifiers.add(Modifiers.Z_SPEED);
                    modifiers.add(Modifiers.AB_FEINT_DUMMY_INDICATOR);
                    modifiers.add(Modifiers.AD_PLATFORM_TYPE);
                    modifiers.add(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME);
                    modifiers.add(Modifiers.AF_COMMON_IDENTIFIER);
                    modifiers.add(Modifiers.AG_AUX_EQUIP_INDICATOR);
                    modifiers.add(Modifiers.AH_AREA_OF_UNCERTAINTY);
                    modifiers.add(Modifiers.AI_DEAD_RECKONING_TRAILER);
                    modifiers.add(Modifiers.AJ_SPEED_LEADER);
                    modifiers.add(Modifiers.AK_PAIRING_LINE);
                    modifiers.add(Modifiers.AL_OPERATIONAL_CONDITION);
                    modifiers.add(Modifiers.AO_ENGAGEMENT_BAR);
                    modifiers.add(Modifiers.AQ_GUARDED_UNIT);
                    modifiers.add(Modifiers.AS_COUNTRY);
                    modifiers.add(Modifiers.AR_SPECIAL_DESIGNATOR);
                    break;
                case SymbolID.SymbolSet_LandInstallation:
                    modifiers.add(Modifiers.A_SYMBOL_ICON);
                    modifiers.add(Modifiers.G_STAFF_COMMENTS);
                    modifiers.add(Modifiers.H_ADDITIONAL_INFO_1);
                    modifiers.add(Modifiers.J_EVALUATION_RATING);
                    modifiers.add(Modifiers.K_COMBAT_EFFECTIVENESS);
                    modifiers.add(Modifiers.P_IFF_SIF_AIS);
                    modifiers.add(Modifiers.S_HQ_STAFF_INDICATOR);
                    modifiers.add(Modifiers.T_UNIQUE_DESIGNATION_1);
                    modifiers.add(Modifiers.W_DTG_1);
                    modifiers.add(Modifiers.X_ALTITUDE_DEPTH);
                    modifiers.add(Modifiers.Y_LOCATION);
                    modifiers.add(Modifiers.AB_FEINT_DUMMY_INDICATOR);
                    modifiers.add(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME);
                    modifiers.add(Modifiers.AH_AREA_OF_UNCERTAINTY);
                    modifiers.add(Modifiers.AI_DEAD_RECKONING_TRAILER);
                    modifiers.add(Modifiers.AJ_SPEED_LEADER);
                    modifiers.add(Modifiers.AK_PAIRING_LINE);
                    modifiers.add(Modifiers.AL_OPERATIONAL_CONDITION);
                    //modifiers.add(Modifiers.AO_ENGAGEMENT_BAR);
                    modifiers.add(Modifiers.AQ_GUARDED_UNIT);
                    modifiers.add(Modifiers.AS_COUNTRY);
                    break;
                case SymbolID.SymbolSet_DismountedIndividuals:
                    modifiers.add(Modifiers.A_SYMBOL_ICON);
                    modifiers.add(Modifiers.G_STAFF_COMMENTS);
                    modifiers.add(Modifiers.H_ADDITIONAL_INFO_1);
                    modifiers.add(Modifiers.J_EVALUATION_RATING);
                    modifiers.add(Modifiers.K_COMBAT_EFFECTIVENESS);
                    modifiers.add(Modifiers.M_HIGHER_FORMATION);
                    modifiers.add(Modifiers.Q_DIRECTION_OF_MOVEMENT);
                    modifiers.add(Modifiers.P_IFF_SIF_AIS);
                    modifiers.add(Modifiers.T_UNIQUE_DESIGNATION_1);
                    modifiers.add(Modifiers.V_EQUIP_TYPE);
                    modifiers.add(Modifiers.W_DTG_1);
                    modifiers.add(Modifiers.X_ALTITUDE_DEPTH);
                    modifiers.add(Modifiers.Y_LOCATION);
                    modifiers.add(Modifiers.AF_COMMON_IDENTIFIER);
                    modifiers.add(Modifiers.AS_COUNTRY);
                    modifiers.add(Modifiers.AV_LEADERSHIP);
                    break;
                case SymbolID.SymbolSet_Space:
                case SymbolID.SymbolSet_SpaceMissile:
                    modifiers.add(Modifiers.A_SYMBOL_ICON);
                    modifiers.add(Modifiers.G_STAFF_COMMENTS);
                    modifiers.add(Modifiers.H_ADDITIONAL_INFO_1);
                    modifiers.add(Modifiers.Q_DIRECTION_OF_MOVEMENT);
                    modifiers.add(Modifiers.T_UNIQUE_DESIGNATION_1);
                    modifiers.add(Modifiers.V_EQUIP_TYPE);
                    modifiers.add(Modifiers.X_ALTITUDE_DEPTH);
                    modifiers.add(Modifiers.Z_SPEED);
                    modifiers.add(Modifiers.AL_OPERATIONAL_CONDITION);
                    modifiers.add(Modifiers.AO_ENGAGEMENT_BAR);
                    modifiers.add(Modifiers.AS_COUNTRY);
                    break;
                case SymbolID.SymbolSet_Air:
                case SymbolID.SymbolSet_AirMissile:
                    modifiers.add(Modifiers.A_SYMBOL_ICON);
                    modifiers.add(Modifiers.G_STAFF_COMMENTS);
                    modifiers.add(Modifiers.H_ADDITIONAL_INFO_1);
                    modifiers.add(Modifiers.Q_DIRECTION_OF_MOVEMENT);
                    modifiers.add(Modifiers.T_UNIQUE_DESIGNATION_1);
                    modifiers.add(Modifiers.V_EQUIP_TYPE);
                    modifiers.add(Modifiers.X_ALTITUDE_DEPTH);
                    modifiers.add(Modifiers.Z_SPEED);
                    modifiers.add(Modifiers.AF_COMMON_IDENTIFIER);
                    modifiers.add(Modifiers.AL_OPERATIONAL_CONDITION);
                    modifiers.add(Modifiers.AO_ENGAGEMENT_BAR);
                    modifiers.add(Modifiers.AS_COUNTRY);
                    break;
                case SymbolID.SymbolSet_SeaSurface:
                    modifiers.add(Modifiers.A_SYMBOL_ICON);
                    modifiers.add(Modifiers.G_STAFF_COMMENTS);
                    modifiers.add(Modifiers.H_ADDITIONAL_INFO_1);
                    modifiers.add(Modifiers.P_IFF_SIF_AIS);
                    modifiers.add(Modifiers.Q_DIRECTION_OF_MOVEMENT);
                    modifiers.add(Modifiers.T_UNIQUE_DESIGNATION_1);
                    modifiers.add(Modifiers.V_EQUIP_TYPE);
                    modifiers.add(Modifiers.Y_LOCATION);
                    modifiers.add(Modifiers.Z_SPEED);
                    modifiers.add(Modifiers.AQ_GUARDED_UNIT);
                    modifiers.add(Modifiers.AL_OPERATIONAL_CONDITION);
                    modifiers.add(Modifiers.AO_ENGAGEMENT_BAR);
                    modifiers.add(Modifiers.AQ_GUARDED_UNIT);
                    modifiers.add(Modifiers.AR_SPECIAL_DESIGNATOR);
                    modifiers.add(Modifiers.AS_COUNTRY);
                    break;
                case SymbolID.SymbolSet_SeaSubsurface:
                    modifiers.add(Modifiers.A_SYMBOL_ICON);
                    modifiers.add(Modifiers.G_STAFF_COMMENTS);
                    modifiers.add(Modifiers.H_ADDITIONAL_INFO_1);
                    modifiers.add(Modifiers.P_IFF_SIF_AIS);
                    modifiers.add(Modifiers.Q_DIRECTION_OF_MOVEMENT);
                    modifiers.add(Modifiers.T_UNIQUE_DESIGNATION_1);
                    modifiers.add(Modifiers.V_EQUIP_TYPE);
                    modifiers.add(Modifiers.X_ALTITUDE_DEPTH);
                    modifiers.add(Modifiers.Y_LOCATION);
                    modifiers.add(Modifiers.Z_SPEED);
                    modifiers.add(Modifiers.AL_OPERATIONAL_CONDITION);
                    modifiers.add(Modifiers.AO_ENGAGEMENT_BAR);
                    modifiers.add(Modifiers.AQ_GUARDED_UNIT);
                    modifiers.add(Modifiers.AR_SPECIAL_DESIGNATOR);
                    modifiers.add(Modifiers.AS_COUNTRY);
                    break;
                case SymbolID.SymbolSet_Activities:
                    modifiers.add(Modifiers.A_SYMBOL_ICON);
                    modifiers.add(Modifiers.G_STAFF_COMMENTS);
                    modifiers.add(Modifiers.H_ADDITIONAL_INFO_1);
                    modifiers.add(Modifiers.J_EVALUATION_RATING);
                    modifiers.add(Modifiers.Q_DIRECTION_OF_MOVEMENT);
                    modifiers.add(Modifiers.T_UNIQUE_DESIGNATION_1);
                    modifiers.add(Modifiers.W_DTG_1);
                    modifiers.add(Modifiers.Y_LOCATION);
                    modifiers.add(Modifiers.AS_COUNTRY);
                    break;
                case SymbolID.SymbolSet_CyberSpace:
                    modifiers.add(Modifiers.A_SYMBOL_ICON);
                    modifiers.add(Modifiers.B_ECHELON);
                    modifiers.add(Modifiers.D_TASK_FORCE_INDICATOR);
                    modifiers.add(Modifiers.F_REINFORCED_REDUCED);
                    modifiers.add(Modifiers.G_STAFF_COMMENTS);
                    modifiers.add(Modifiers.H_ADDITIONAL_INFO_1);
                    modifiers.add(Modifiers.K_COMBAT_EFFECTIVENESS);
                    modifiers.add(Modifiers.L_SIGNATURE_EQUIP);
                    modifiers.add(Modifiers.M_HIGHER_FORMATION);
                    modifiers.add(Modifiers.S_HQ_STAFF_INDICATOR);
                    modifiers.add(Modifiers.T_UNIQUE_DESIGNATION_1);
                    modifiers.add(Modifiers.V_EQUIP_TYPE);
                    modifiers.add(Modifiers.W_DTG_1);
                    modifiers.add(Modifiers.Y_LOCATION);
                    modifiers.add(Modifiers.AO_ENGAGEMENT_BAR);
                    modifiers.add(Modifiers.AS_COUNTRY);
                    break;
            /*case SymbolID.SymbolSet_SignalsIntelligence_Air:
            case SymbolID.SymbolSet_SignalsIntelligence_Land:
            case SymbolID.SymbolSet_SignalsIntelligence_SeaSurface:
            case SymbolID.SymbolSet_SignalsIntelligence_SeaSubsurface:
            case SymbolID.SymbolSet_SignalsIntelligence_Space:
                modifiers.add(Modifiers.A_SYMBOL_ICON);
                modifiers.add(Modifiers.D_TASK_FORCE_INDICATOR);
                modifiers.add(Modifiers.G_STAFF_COMMENTS);
                modifiers.add(Modifiers.H_ADDITIONAL_INFO_1);
                modifiers.add(Modifiers.J_EVALUATION_RATING);
                modifiers.add(Modifiers.M_HIGHER_FORMATION);
                modifiers.add(Modifiers.R2_SIGNIT_MOBILITY_INDICATOR);
                modifiers.add(Modifiers.T_UNIQUE_DESIGNATION_1);
                modifiers.add(Modifiers.V_EQUIP_TYPE);
                modifiers.add(Modifiers.W1_DTG_2);
                modifiers.add(Modifiers.Y_LOCATION);
                modifiers.add(Modifiers.AD_PLATFORM_TYPE);//like equipment
                modifiers.add(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME);//like equipment
                modifiers.add(Modifiers.AF_COMMON_IDENTIFIER);//like equipment
                break;*/

                case SymbolID.SymbolSet_ControlMeasure:
                    //values come from files during MSLookup load
                    break;
                case SymbolID.SymbolSet_Atmospheric:
                    //Tropopause low, Tropopause high
                    if ((ec.equals("110102")) || (ec.equals("110202")) ||
                            (ec.equals("162200")))
                        modifiers.add(Modifiers.X_ALTITUDE_DEPTH);
                    else if (ec.equals("140200"))
                        modifiers.add(Modifiers.AN_AZIMUTH);
                    break;
                case SymbolID.SymbolSet_MineWarfare:
                case SymbolID.SymbolSet_Oceanographic:
                case SymbolID.SymbolSet_MeteorologicalSpace:
                default://no modifiers

            }
        }
        else
        {
            switch (ss) {
                case SymbolID.SymbolSet_LandUnit:
                case SymbolID.SymbolSet_LandCivilianUnit_Organization:
                    modifiers.add(Modifiers.A_SYMBOL_ICON);
                    modifiers.add(Modifiers.B_ECHELON);
                    modifiers.add(Modifiers.D_TASK_FORCE_INDICATOR);
                    modifiers.add(Modifiers.F_REINFORCED_REDUCED);
                    modifiers.add(Modifiers.G_STAFF_COMMENTS);
                    modifiers.add(Modifiers.H_ADDITIONAL_INFO_1);
                    modifiers.add(Modifiers.J_EVALUATION_RATING);
                    modifiers.add(Modifiers.K_COMBAT_EFFECTIVENESS);
                    modifiers.add(Modifiers.M_HIGHER_FORMATION);
                    modifiers.add(Modifiers.P_IFF_SIF_AIS);
                    modifiers.add(Modifiers.Q_DIRECTION_OF_MOVEMENT);
                    modifiers.add(Modifiers.S_HQ_STAFF_INDICATOR);
                    modifiers.add(Modifiers.T_UNIQUE_DESIGNATION_1);
                    modifiers.add(Modifiers.W_DTG_1);
                    modifiers.add(Modifiers.X_ALTITUDE_DEPTH);
                    modifiers.add(Modifiers.Y_LOCATION);
                    modifiers.add(Modifiers.Z_SPEED);
                    if(ss==SymbolID.SymbolSet_LandUnit && ec.equals("110000"))
                        modifiers.add(Modifiers.AA_SPECIAL_C2_HQ);
                    modifiers.add(Modifiers.AB_FEINT_DUMMY_INDICATOR);
                    modifiers.add(Modifiers.AH_AREA_OF_UNCERTAINTY);
                    modifiers.add(Modifiers.AI_DEAD_RECKONING_TRAILER);
                    modifiers.add(Modifiers.AJ_SPEED_LEADER);
                    modifiers.add(Modifiers.AK_PAIRING_LINE);
                    modifiers.add(Modifiers.AO_ENGAGEMENT_BAR);
                    modifiers.add(Modifiers.AS_COUNTRY);
                    break;
                case SymbolID.SymbolSet_LandEquipment:
                case SymbolID.SymbolSet_SignalsIntelligence_Land:
                    modifiers.add(Modifiers.A_SYMBOL_ICON);
                    modifiers.add(Modifiers.C_QUANTITY);
                    modifiers.add(Modifiers.G_STAFF_COMMENTS);
                    modifiers.add(Modifiers.H_ADDITIONAL_INFO_1);
                    modifiers.add(Modifiers.J_EVALUATION_RATING);
                    modifiers.add(Modifiers.L_SIGNATURE_EQUIP);
                    modifiers.add(Modifiers.N_HOSTILE);
                    modifiers.add(Modifiers.P_IFF_SIF_AIS);
                    modifiers.add(Modifiers.Q_DIRECTION_OF_MOVEMENT);
                    modifiers.add(Modifiers.R_MOBILITY_INDICATOR);
                    modifiers.add(Modifiers.T_UNIQUE_DESIGNATION_1);
                    modifiers.add(Modifiers.V_EQUIP_TYPE);
                    modifiers.add(Modifiers.W_DTG_1);
                    modifiers.add(Modifiers.X_ALTITUDE_DEPTH);
                    modifiers.add(Modifiers.Y_LOCATION);
                    modifiers.add(Modifiers.Z_SPEED);
                    modifiers.add(Modifiers.AB_FEINT_DUMMY_INDICATOR);
                    modifiers.add(Modifiers.AD_PLATFORM_TYPE);
                    modifiers.add(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME);
                    modifiers.add(Modifiers.AF_COMMON_IDENTIFIER);
                    modifiers.add(Modifiers.AG_AUX_EQUIP_INDICATOR);
                    modifiers.add(Modifiers.AH_AREA_OF_UNCERTAINTY);
                    modifiers.add(Modifiers.AI_DEAD_RECKONING_TRAILER);
                    modifiers.add(Modifiers.AJ_SPEED_LEADER);
                    modifiers.add(Modifiers.AK_PAIRING_LINE);
                    modifiers.add(Modifiers.AL_OPERATIONAL_CONDITION);
                    modifiers.add(Modifiers.AO_ENGAGEMENT_BAR);
                    modifiers.add(Modifiers.AQ_GUARDED_UNIT);
                    modifiers.add(Modifiers.AR_SPECIAL_DESIGNATOR);
                    break;
                case SymbolID.SymbolSet_LandInstallation:
                    modifiers.add(Modifiers.A_SYMBOL_ICON);
                    modifiers.add(Modifiers.G_STAFF_COMMENTS);
                    modifiers.add(Modifiers.H_ADDITIONAL_INFO_1);
                    modifiers.add(Modifiers.J_EVALUATION_RATING);
                    modifiers.add(Modifiers.K_COMBAT_EFFECTIVENESS);
                    modifiers.add(Modifiers.P_IFF_SIF_AIS);
                    modifiers.add(Modifiers.S_HQ_STAFF_INDICATOR);
                    modifiers.add(Modifiers.T_UNIQUE_DESIGNATION_1);
                    modifiers.add(Modifiers.W_DTG_1);
                    modifiers.add(Modifiers.X_ALTITUDE_DEPTH);
                    modifiers.add(Modifiers.Y_LOCATION);
                    modifiers.add(Modifiers.AB_FEINT_DUMMY_INDICATOR);
                    modifiers.add(Modifiers.AH_AREA_OF_UNCERTAINTY);
                    modifiers.add(Modifiers.AI_DEAD_RECKONING_TRAILER);
                    modifiers.add(Modifiers.AJ_SPEED_LEADER);
                    modifiers.add(Modifiers.AK_PAIRING_LINE);
                    modifiers.add(Modifiers.AL_OPERATIONAL_CONDITION);
                    modifiers.add(Modifiers.AO_ENGAGEMENT_BAR);
                    break;
                case SymbolID.SymbolSet_Space:
                case SymbolID.SymbolSet_SpaceMissile:
                case SymbolID.SymbolSet_SignalsIntelligence_Space:
                    modifiers.add(Modifiers.A_SYMBOL_ICON);
                    modifiers.add(Modifiers.G_STAFF_COMMENTS);
                    modifiers.add(Modifiers.H_ADDITIONAL_INFO_1);
                    modifiers.add(Modifiers.Q_DIRECTION_OF_MOVEMENT);
                    modifiers.add(Modifiers.T_UNIQUE_DESIGNATION_1);
                    modifiers.add(Modifiers.V_EQUIP_TYPE);
                    modifiers.add(Modifiers.X_ALTITUDE_DEPTH);
                    modifiers.add(Modifiers.Z_SPEED);
                    modifiers.add(Modifiers.AL_OPERATIONAL_CONDITION);
                    modifiers.add(Modifiers.AO_ENGAGEMENT_BAR);
                    break;
                case SymbolID.SymbolSet_Air:
                case SymbolID.SymbolSet_AirMissile:
                case SymbolID.SymbolSet_SignalsIntelligence_Air:
                    modifiers.add(Modifiers.A_SYMBOL_ICON);
                    modifiers.add(Modifiers.G_STAFF_COMMENTS);
                    modifiers.add(Modifiers.H_ADDITIONAL_INFO_1);
                    modifiers.add(Modifiers.P_IFF_SIF_AIS);//air only
                    modifiers.add(Modifiers.Q_DIRECTION_OF_MOVEMENT);
                    modifiers.add(Modifiers.T_UNIQUE_DESIGNATION_1);
                    modifiers.add(Modifiers.V_EQUIP_TYPE);
                    modifiers.add(Modifiers.X_ALTITUDE_DEPTH);
                    modifiers.add(Modifiers.Z_SPEED);
                    modifiers.add(Modifiers.AL_OPERATIONAL_CONDITION);
                    modifiers.add(Modifiers.AO_ENGAGEMENT_BAR);
                    break;
                case SymbolID.SymbolSet_SeaSurface:
                case SymbolID.SymbolSet_SignalsIntelligence_SeaSurface:
                    modifiers.add(Modifiers.A_SYMBOL_ICON);
                    modifiers.add(Modifiers.G_STAFF_COMMENTS);
                    modifiers.add(Modifiers.H_ADDITIONAL_INFO_1);
                    modifiers.add(Modifiers.P_IFF_SIF_AIS);
                    modifiers.add(Modifiers.Q_DIRECTION_OF_MOVEMENT);
                    modifiers.add(Modifiers.T_UNIQUE_DESIGNATION_1);
                    modifiers.add(Modifiers.V_EQUIP_TYPE);
                    modifiers.add(Modifiers.Y_LOCATION);
                    modifiers.add(Modifiers.Z_SPEED);
                    modifiers.add(Modifiers.AL_OPERATIONAL_CONDITION);
                    modifiers.add(Modifiers.AO_ENGAGEMENT_BAR);
                    modifiers.add(Modifiers.AQ_GUARDED_UNIT);
                    modifiers.add(Modifiers.AR_SPECIAL_DESIGNATOR);
                    break;
                case SymbolID.SymbolSet_SeaSubsurface:
                case SymbolID.SymbolSet_SignalsIntelligence_SeaSubsurface:
                    modifiers.add(Modifiers.A_SYMBOL_ICON);
                    modifiers.add(Modifiers.AG_AUX_EQUIP_INDICATOR);
                    modifiers.add(Modifiers.AL_OPERATIONAL_CONDITION);
                    modifiers.add(Modifiers.AO_ENGAGEMENT_BAR);
                    modifiers.add(Modifiers.AR_SPECIAL_DESIGNATOR);
                    modifiers.add(Modifiers.G_STAFF_COMMENTS);
                    modifiers.add(Modifiers.H_ADDITIONAL_INFO_1);
                    modifiers.add(Modifiers.Q_DIRECTION_OF_MOVEMENT);
                    modifiers.add(Modifiers.T_UNIQUE_DESIGNATION_1);
                    modifiers.add(Modifiers.V_EQUIP_TYPE);
                    modifiers.add(Modifiers.X_ALTITUDE_DEPTH);
                    break;
                case SymbolID.SymbolSet_Activities:
                    modifiers.add(Modifiers.A_SYMBOL_ICON);
                    modifiers.add(Modifiers.G_STAFF_COMMENTS);
                    modifiers.add(Modifiers.H_ADDITIONAL_INFO_1);
                    modifiers.add(Modifiers.J_EVALUATION_RATING);
                    modifiers.add(Modifiers.Q_DIRECTION_OF_MOVEMENT);
                    modifiers.add(Modifiers.W_DTG_1);
                    modifiers.add(Modifiers.Y_LOCATION);
                    modifiers.add(Modifiers.AS_COUNTRY);
                    break;
                case SymbolID.SymbolSet_CyberSpace:
                    modifiers.add(Modifiers.A_SYMBOL_ICON);
                    modifiers.add(Modifiers.B_ECHELON);
                    modifiers.add(Modifiers.D_TASK_FORCE_INDICATOR);
                    modifiers.add(Modifiers.F_REINFORCED_REDUCED);
                    modifiers.add(Modifiers.G_STAFF_COMMENTS);
                    modifiers.add(Modifiers.H_ADDITIONAL_INFO_1);
                    modifiers.add(Modifiers.K_COMBAT_EFFECTIVENESS);
                    modifiers.add(Modifiers.L_SIGNATURE_EQUIP);
                    modifiers.add(Modifiers.M_HIGHER_FORMATION);
                    modifiers.add(Modifiers.S_HQ_STAFF_INDICATOR);
                    modifiers.add(Modifiers.T_UNIQUE_DESIGNATION_1);
                    modifiers.add(Modifiers.V_EQUIP_TYPE);
                    modifiers.add(Modifiers.W_DTG_1);
                    modifiers.add(Modifiers.Y_LOCATION);
                    modifiers.add(Modifiers.AO_ENGAGEMENT_BAR);
                    modifiers.add(Modifiers.AS_COUNTRY);
                    break;
            /*case SymbolID.SymbolSet_SignalsIntelligence_Air:
            case SymbolID.SymbolSet_SignalsIntelligence_Land:
            case SymbolID.SymbolSet_SignalsIntelligence_SeaSurface:
            case SymbolID.SymbolSet_SignalsIntelligence_SeaSubsurface:
            case SymbolID.SymbolSet_SignalsIntelligence_Space:
                modifiers.add(Modifiers.A_SYMBOL_ICON);
                modifiers.add(Modifiers.D_TASK_FORCE_INDICATOR);
                modifiers.add(Modifiers.G_STAFF_COMMENTS);
                modifiers.add(Modifiers.H_ADDITIONAL_INFO_1);
                modifiers.add(Modifiers.J_EVALUATION_RATING);
                modifiers.add(Modifiers.M_HIGHER_FORMATION);
                modifiers.add(Modifiers.R2_SIGNIT_MOBILITY_INDICATOR);
                modifiers.add(Modifiers.T_UNIQUE_DESIGNATION_1);
                modifiers.add(Modifiers.V_EQUIP_TYPE);
                modifiers.add(Modifiers.W1_DTG_2);
                modifiers.add(Modifiers.Y_LOCATION);
                modifiers.add(Modifiers.AD_PLATFORM_TYPE);//like equipment
                modifiers.add(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME);//like equipment
                modifiers.add(Modifiers.AF_COMMON_IDENTIFIER);//like equipment
                break;*/

                case SymbolID.SymbolSet_ControlMeasure:
                    //values come from files during MSLookup load
                    break;
                case SymbolID.SymbolSet_Atmospheric:
                    //Tropopause low, Tropopause high
                    if ((ec.equals("110102")) || (ec.equals("110202")) ||
                            (ec.equals("162200")))
                        modifiers.add(Modifiers.X_ALTITUDE_DEPTH);
                    else if (ec.equals("140200"))
                        modifiers.add(Modifiers.AN_AZIMUTH);
                    break;
                case SymbolID.SymbolSet_MineWarfare:
                case SymbolID.SymbolSet_Oceanographic:
                case SymbolID.SymbolSet_MeteorologicalSpace:
                default://no modifiers

            }
        }

        if (ss == SymbolID.SymbolSet_SignalsIntelligence_Air ||
                ss == SymbolID.SymbolSet_SignalsIntelligence_Land ||
                ss == SymbolID.SymbolSet_SignalsIntelligence_SeaSurface ||
                ss == SymbolID.SymbolSet_SignalsIntelligence_SeaSubsurface ||
                ss == SymbolID.SymbolSet_SignalsIntelligence_Space)
            modifiers.add(Modifiers.R2_SIGNIT_MOBILITY_INDICATOR);

        return modifiers;
    }

    /**
     * @param symbolID Full 20-30 digits from the symbol code
     * @return {@link MSInfo}
     */
    public MSInfo getMSLInfo(String symbolID) {
        int length = symbolID.length();

        if (length >= 20 && length <= 30) {
            int version = SymbolID.getVersion(symbolID);
            return getMSLInfo(SymbolUtilities.getBasicSymbolID(symbolID), version);
        } else
            return null;
    }

    /**
     * @param basicID id SymbolSet + Entity code like 50110100
     * @param version like SymbolID.Version_2525Dch1
     * @return {@link MSInfo}
     */
    public MSInfo getMSLInfo(String basicID, int version)
    {
        int length = basicID.length();
        if (length == 8) {
            if (version < SymbolID.Version_2525E)
                return _MSLookupD.getOrDefault(basicID, null);
            else if (version == SymbolID.Version_2525E)
                return _MSLookupE.getOrDefault(basicID, null);
            else
                return _MSLookupD.getOrDefault(basicID, null);
        }
        else if (length >= 20 && length <= 30)//probably got a full id instead of a basic ID.
        {
            if (version < SymbolID.Version_2525E)
                return _MSLookupD.getOrDefault(SymbolUtilities.getBasicSymbolID(basicID), null);
            else if (version == SymbolID.Version_2525E)
                return _MSLookupE.getOrDefault(SymbolUtilities.getBasicSymbolID(basicID), null);
            else
                return _MSLookupD.getOrDefault(SymbolUtilities.getBasicSymbolID(basicID), null);
        } else
            return null;
    }

    /**
     * returns a list of all the keys in the order they are listed in the MilStd 2525D document.
     * @param version {@link Integer} see {@link SymbolID#Version_2525E} and {@link SymbolID#Version_2525Dch1}
     * @return {@link List}
     */
    public List<String> getIDList(int version) {
        if (version < SymbolID.Version_2525E)
            return _IDListD;
        else if (version == SymbolID.Version_2525E)
            return _IDListE;
        else
            return _IDListD;
    }

    /*
     * For use only by MilStdIconRenderer.addCustomSymbol()
     * @param msInfo
     * @return
     */
    public boolean addCustomSymbol(MSInfo msInfo)
    {
        boolean success = false;
        try
        {
            int version = msInfo.getVersion();
            if (version < SymbolID.Version_2525E)
            {
                if(this._IDListD.indexOf(msInfo.getBasicSymbolID()) == -1)
                {
                    this._IDListD.add(msInfo.getBasicSymbolID());
                    MSLookup._MSLookupD.put(msInfo.getBasicSymbolID(), msInfo);
                    success = true;
                }
                else
                    ErrorLogger.LogMessage("Symbol Set and Entity Code combination already exist: " + msInfo.getBasicSymbolID(), Level.INFO,false);
            }
            else if (version == SymbolID.Version_2525E)
            {
                if(this._IDListE.indexOf(msInfo.getBasicSymbolID()) == -1)
                {
                    this._IDListE.add(msInfo.getBasicSymbolID());
                    MSLookup._MSLookupE.put(msInfo.getBasicSymbolID(), msInfo);
                    success = true;
                }
                else
                    ErrorLogger.LogMessage("Symbol Set and Entity Code combination already exist: " + msInfo.getBasicSymbolID(), Level.INFO,false);
            }
        }
        catch(Exception e)
        {
            ErrorLogger.LogException("MSLookup", "addCustomSymbol",e);
        }
        return success;
    }
}