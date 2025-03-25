/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package armyc2.c5isr.renderer.tester;

import armyc2.c5isr.renderer.MilStdIconRenderer;
import armyc2.c5isr.renderer.SinglePointRenderer;
import armyc2.c5isr.renderer.SinglePointSVGRenderer;
import armyc2.c5isr.renderer.utilities.*;
import armyc2.c5isr.web.render.WebRenderer;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;



/**
 *
 * 
 */
public class Tester extends javax.swing.JFrame {

    int drawMode = 0;
    int drawModeSPDraw = 1;
    int drawModeMPDraw = 2;
    
    int maxPointCount = 1;
    ArrayList<Point2D> points = new ArrayList<Point2D>();

    String currentBasicID = "";
    
    /**
     * Creates new form Tester
     */
    public Tester() {
        initComponents();
        init();

    }
    
    private void init()
    {
        try
        {
           loadTree(SymbolID.Version_2525Dch1);
            //RendererSettings.getInstance().setLabelFont("algerian",Font.TRUETYPE_FONT,24);
           setCBItems();
            SinglePointRenderer.getInstance();
            SVGLookup.getInstance();
            RendererSettings.getInstance().setCacheEnabled(false);
            //RendererSettings.getInstance().setOperationalConditionModifierType(RendererSettings.OperationalConditionModifierType_SLASH);
            //RendererSettings.getInstance().setDrawAffiliationModifierAsLabel(false);

            //Test adding of custom symbol
            MSInfo miBase = MSLookup.getInstance().getMSLInfo("10110000",SymbolID.Version_2525E);
            MSInfo mi = new MSInfo(13,"10", "Sustainment","TEST","","165700",miBase.getModifiers());
            SVGInfo si = new SVGInfo("10165700", new Rectangle2D.Double(198.0,365.0,215.0,64.0),"<g id=\"10165700\"><text font-family=\"sans-serif\" fill=\"red\" font-size=\"89\" x=\"192\" y=\"428\">MWR</text></g>");
            MilStdIconRenderer.getInstance().AddCustomSymbol(mi,si);
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("Tester", "init", exc, Level.WARNING);
        }
    }
    
    private class MSNodeInfo {
        public String msName;
        public String msID;
 
        public MSNodeInfo(String id, String name) {
            msName = name;
            msID = id;
        }
 
        public String toString() {
            return msName;
        }
    }
    
    private class CBItemInfo{
        public String cbiText;
        public String cbiID;
 
        public CBItemInfo(String text, String id) {
            cbiText = text;
            cbiID = id;
        }
 
        public String toString() {
            return cbiText;
        }
    }
    
    private void updatedSymbolIDField()
    {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)msTree.getLastSelectedPathComponent();
        if (node == null)
        {
            //Nothing is selected.  
            tfSymbolID.setText("");
            return;
        }

        if (node.isLeaf()) 
        {
            Object nodeInfo = (MSNodeInfo)node.getUserObject();            
            MSNodeInfo msni = (MSNodeInfo) nodeInfo;
            String symbolID = buildSymbolID(msni.msID);
            //MSInfo msi = MSLookup.getInstance().getMSLInfo(symbolID);
            tfSymbolID.setText(symbolID);
            
        }
    }
    
    /**
     * 
     * @param basicID 8 character code which is symbol set + entity code
     * @return 
     */
    private String buildSymbolID(String basicID)
    {
        String id = cbVersion.getSelectedItem().toString().split("-")[0];
        id += cbContext.getSelectedItem().toString().split("-")[0];
        id += cbAffiliation.getSelectedItem().toString().split("-")[0];
        id += basicID.substring(0, 2);
        id += cbStatus.getSelectedItem().toString().split("-")[0];
        id += cbHQTFD.getSelectedItem().toString().split("-")[0];
        id += cbAmplifier.getSelectedItem().toString().split("-")[0];
        id += basicID.substring(2);
        
        String s1m = tfSector1Mod.getText();
        if(s1m.length() == 2 && SymbolUtilities.isNumber(s1m))
        {
            id += s1m;
        }
        else
            id += "00";
        
        String s2m = tfSector2Mod.getText();
        if(s2m.length() == 2 && SymbolUtilities.isNumber(s2m))
        {
            id += s2m;
        }
        else
            id += "00";

        id += cbS1I.getSelectedItem().toString();
        id += cbS2I.getSelectedItem().toString();

        String fs = cbFrameShape.getSelectedItem().toString().split("-")[0];

        if(Integer.parseInt(id.substring(0,2)) >= SymbolID.Version_2525E)
            id += fs + "0000";//2525E has frame shape and 4 reserved positions
        else
            id += "00000";//2525D has 5 reserved positions

        id += "000";//country code


        return id;
    }

    private void copyToClipboard(String value)
    {
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection strse1 = new StringSelection(value);
        clip.setContents(strse1, strse1);
    }
    
    private void loadTree(int version)
    {

        DefaultMutableTreeNode root = (DefaultMutableTreeNode)msTree.getModel().getRoot();

        if(version < SymbolID.Version_2525E)
            root.setUserObject("2525D");
        else if(version >= SymbolID.Version_2525E)
            root.setUserObject("2525E");

        
        DefaultMutableTreeNode msn00 = new DefaultMutableTreeNode(new MSNodeInfo("00","Unknown"));
        DefaultMutableTreeNode msn01 = new DefaultMutableTreeNode(new MSNodeInfo("01","Air"));
        DefaultMutableTreeNode msn02 = new DefaultMutableTreeNode(new MSNodeInfo("02","Air Missile"));
        DefaultMutableTreeNode msn51 = new DefaultMutableTreeNode(new MSNodeInfo("51","Air SIGINT"));
        DefaultMutableTreeNode msn05 = new DefaultMutableTreeNode(new MSNodeInfo("05","Space"));
        DefaultMutableTreeNode msn06 = new DefaultMutableTreeNode(new MSNodeInfo("06","Space Missile"));
        DefaultMutableTreeNode msn50 = new DefaultMutableTreeNode(new MSNodeInfo("50","Space SIGINT"));
        if(version >= SymbolID.Version_2525E)
            msn50 = new DefaultMutableTreeNode(new MSNodeInfo("50","SIGINT"));
        DefaultMutableTreeNode msn10 = new DefaultMutableTreeNode(new MSNodeInfo("10","Land Unit"));
        DefaultMutableTreeNode msn11 = new DefaultMutableTreeNode(new MSNodeInfo("11","Land Civ"));
        DefaultMutableTreeNode msn15 = new DefaultMutableTreeNode(new MSNodeInfo("15","Land Equipment"));
        DefaultMutableTreeNode msn52 = new DefaultMutableTreeNode(new MSNodeInfo("52","Land SIGINT"));
        DefaultMutableTreeNode msn20 = new DefaultMutableTreeNode(new MSNodeInfo("20","Land Installation"));
        DefaultMutableTreeNode msn27 = new DefaultMutableTreeNode(new MSNodeInfo("20","Dismounted Individual"));
        DefaultMutableTreeNode msn30 = new DefaultMutableTreeNode(new MSNodeInfo("30","Sea Surface"));
        DefaultMutableTreeNode msn53 = new DefaultMutableTreeNode(new MSNodeInfo("53","Sea Surface SIGINT"));
        DefaultMutableTreeNode msn35 = new DefaultMutableTreeNode(new MSNodeInfo("35","Sea Subsurface"));
        DefaultMutableTreeNode msn36 = new DefaultMutableTreeNode(new MSNodeInfo("36","Mine Warfare"));
        DefaultMutableTreeNode msn54 = new DefaultMutableTreeNode(new MSNodeInfo("54","Sea Subsurface SIGINT"));
        DefaultMutableTreeNode msn40 = new DefaultMutableTreeNode(new MSNodeInfo("40","Activities"));
        DefaultMutableTreeNode msn25 = new DefaultMutableTreeNode(new MSNodeInfo("25","Control Measure"));
        DefaultMutableTreeNode msn45 = new DefaultMutableTreeNode(new MSNodeInfo("45","Atmospheric"));
        DefaultMutableTreeNode msn46 = new DefaultMutableTreeNode(new MSNodeInfo("46","Oceanographic"));
        DefaultMutableTreeNode msn47 = new DefaultMutableTreeNode(new MSNodeInfo("47","Meteorological Space"));
        DefaultMutableTreeNode msn60 = new DefaultMutableTreeNode(new MSNodeInfo("60","Cyberspace"));
        
        

        DefaultMutableTreeNode MSNI = null;
        
        List<String> IDs = MSLookup.getInstance().getIDList(version);
        
        try
        {
            String name = null;
            MSInfo msi = null;
            for(String id : IDs)
            {
                if(id.length()==8)
                    msi = MSLookup.getInstance().getMSLInfo(id,version);
                else
                    msi = null;
                
                if(msi != null && msi.getDrawRule() != DrawRules.DONOTDRAW)
                {
                    name = msi.getName();
                    MSNI = new DefaultMutableTreeNode(new MSNodeInfo(id,name));
                    
                    if(id.startsWith("00"))
                    {
                        msn00.add(MSNI);
                    }
                    else if(id.startsWith("01"))
                    {
                        msn01.add(MSNI);
                    }
                    else if(id.startsWith("02"))
                    {
                        msn02.add(MSNI);
                    }
                    else if(id.startsWith("51"))
                    {
                        msn51.add(MSNI);
                    }
                    else if(id.startsWith("05"))
                    {
                        msn05.add(MSNI);
                    }
                    else if(id.startsWith("06"))
                    {
                        msn06.add(MSNI);
                    }
                    else if(id.startsWith("50"))
                    {
                        msn50.add(MSNI);
                    }
                    else if(id.startsWith("10"))
                    {
                        msn10.add(MSNI);
                    }
                    else if(id.startsWith("11"))
                    {
                        msn11.add(MSNI);
                    }
                    else if(id.startsWith("15"))
                    {
                        msn15.add(MSNI);
                    }
                    else if(id.startsWith("52"))
                    {
                        msn52.add(MSNI);
                    }
                    else if(id.startsWith("20"))
                    {
                        msn20.add(MSNI);
                    }
                    else if(id.startsWith("27"))
                    {
                        msn27.add(MSNI);
                    }
                    else if(id.startsWith("30"))
                    {
                        msn30.add(MSNI);
                    }
                    else if(id.startsWith("53"))
                    {
                        msn53.add(MSNI);
                    }
                    else if(id.startsWith("35"))
                    {
                        msn35.add(MSNI);
                    }
                    else if(id.startsWith("36"))
                    {
                        msn36.add(MSNI);
                    }
                    else if(id.startsWith("54"))
                    {
                        msn54.add(MSNI);
                    }
                    else if(id.startsWith("40"))
                    {
                        msn40.add(MSNI);
                    }
                    else if(id.startsWith("25"))
                    {
                        msn25.add(MSNI);
                    }
                    else if(id.startsWith("45"))
                    {
                        msn45.add(MSNI);
                    }
                    else if(id.startsWith("46"))
                    {
                        msn46.add(MSNI);
                    }
                    else if(id.startsWith("47"))
                    {
                        msn47.add(MSNI);
                    }
                    else if(id.startsWith("60"))
                    {
                        msn60.add(MSNI);
                    }
                    /*else if(id.startsWith("??"))//new 2525E catagory
                    {
                        msn01.add(MSNI);
                    }*/
                }
                else
                {
                    //ErrorLogger.LogMessage("No entry found for " + id);
                }
            }
        }    
        catch(Exception exc)
        {
            ErrorLogger.LogException("Tester", "loadTree", exc);
        }
            
        root.add(msn00);
        root.add(msn01);
        root.add(msn02);
        if(version < SymbolID.Version_2525E)
            root.add(msn51);
        root.add(msn05);
        root.add(msn06);
        root.add(msn50);
        root.add(msn10);
        root.add(msn11);
        root.add(msn15);
        if(version < SymbolID.Version_2525E)
            root.add(msn52);
        root.add(msn20);
        if(version >= SymbolID.Version_2525E)
            root.add(msn27);
        root.add(msn30);
        if(version < SymbolID.Version_2525E)
            root.add(msn53);
        root.add(msn35);
        root.add(msn36);
        if(version < SymbolID.Version_2525E)
            root.add(msn54);
        root.add(msn40);
        root.add(msn25);
        root.add(msn45);
        root.add(msn46);
        root.add(msn47);
        root.add(msn60);
        
        msTree.getSelectionModel().setSelectionMode((TreeSelectionModel.SINGLE_TREE_SELECTION));
        
    }
    
    private void setCBItems()
    {
        cbVersion.addItem("11-2525D");
        cbVersion.addItem("13-2525E");
        cbVersion.setSelectedIndex(0);
        
        cbContext.addItem("0-Reality");
        cbContext.addItem("1-Exercise");
        cbContext.addItem("2-Simulation");
        cbContext.setSelectedIndex(0);
        
        cbAffiliation.addItem("0-Pending");
        cbAffiliation.addItem("1-Unknown");
        cbAffiliation.addItem("2-Assumed Friend");
        cbAffiliation.addItem("3-Friend");
        cbAffiliation.addItem("4-Neutral");
        cbAffiliation.addItem("5-Suspect/Joker");
        cbAffiliation.addItem("6-Hostile/Faker");
        cbAffiliation.setSelectedIndex(3);
        
        cbStatus.addItem("0-Present");
        cbStatus.addItem("1-Planned/Anticipated/Suspect");
        cbStatus.addItem("2-Fully capabable");
        cbStatus.addItem("3-Damaged");
        cbStatus.addItem("4-Destroyed");
        cbStatus.addItem("5-Full to capacity");
        cbStatus.setSelectedIndex(0);
        
        cbHQTFD.addItem("0-Unknown");
        cbHQTFD.addItem("1-Dummy");
        cbHQTFD.addItem("2-HQ");
        cbHQTFD.addItem("3-D/HQ");
        cbHQTFD.addItem("4-TF");
        cbHQTFD.addItem("5-D/TF");
        cbHQTFD.addItem("6-TF/HQ");
        cbHQTFD.addItem("7-D/TF/HQ");
        cbHQTFD.setSelectedIndex(0);
        
        cbAmplifier.addItem("00-Unknown");
        cbAmplifier.addItem("11-Team/Crew");
        cbAmplifier.addItem("12-Squad");
        cbAmplifier.addItem("13-Section");
        cbAmplifier.addItem("14-Platoon/detachment");
        cbAmplifier.addItem("15-Company/battery/troop");
        cbAmplifier.addItem("16-Battalion/squadron");
        cbAmplifier.addItem("17-Regiment/group");
        cbAmplifier.addItem("18-Brigade");
        //cbAmplifier.addItem("19-Unknown");
        cbAmplifier.addItem("21-Division");
        cbAmplifier.addItem("22-Corps/MEF");
        cbAmplifier.addItem("23-Army");
        cbAmplifier.addItem("24-Army Group/front");
        cbAmplifier.addItem("25-Region/Theater");
        cbAmplifier.addItem("26-Command");
        
        cbAmplifier.addItem("31-Wheeled limited Cross country");
        cbAmplifier.addItem("32-Wheeled cross country");
        cbAmplifier.addItem("33-Tracked");
        cbAmplifier.addItem("34-Wheeled & Tracked");
        cbAmplifier.addItem("35-Towed");
        cbAmplifier.addItem("36-Rail");
        cbAmplifier.addItem("37-Pack Animals");
        
        cbAmplifier.addItem("41-Over Snow");
        cbAmplifier.addItem("42-Sled");
        
        cbAmplifier.addItem("51-Barge");
        cbAmplifier.addItem("52-Amphibious");

        cbAmplifier.addItem("61-Short towed Array");
        cbAmplifier.addItem("62-Long towed Array");

        cbAmplifier.addItem("71-Leadership Indicator");
        
        cbAmplifier.setSelectedIndex(0);
    }
    
    private Map<String,String> populateModifiers(String symbolID)
    {
        Map<String,String> modifier = new HashMap<>();
        try
        {
            MSInfo msInfo = MSLookup.getInstance().getMSLInfo(symbolID);
            
            modifier.put(Modifiers.H_ADDITIONAL_INFO_1, "H");
            modifier.put(Modifiers.H1_ADDITIONAL_INFO_2,"H1");
            modifier.put(Modifiers.AP_TARGET_NUMBER,"AP");
            modifier.put(Modifiers.AP1_TARGET_NUMBER_EXTENSION,"AP1");
            modifier.put(Modifiers.X_ALTITUDE_DEPTH,"0,10");//X
            modifier.put(Modifiers.K_COMBAT_EFFECTIVENESS,"100");//K
            modifier.put(Modifiers.Q_DIRECTION_OF_MOVEMENT,"45");//Q

            modifier.put(Modifiers.W_DTG_1, SymbolUtilities.getDateLabel(new Date()));//W
            modifier.put(Modifiers.W1_DTG_2, SymbolUtilities.getDateLabel(new Date()));//W1
            modifier.put(Modifiers.J_EVALUATION_RATING,"J");
            modifier.put(Modifiers.M_HIGHER_FORMATION,"M");
            modifier.put(Modifiers.N_HOSTILE,"ENY");
            modifier.put(Modifiers.P_IFF_SIF_AIS,"P");
            modifier.put(Modifiers.Y_LOCATION,"Y");
            
            modifier.put(Modifiers.C_QUANTITY,"C");
            
            modifier.put(Modifiers.F_REINFORCED_REDUCED,"RD");
            modifier.put(Modifiers.L_SIGNATURE_EQUIP,"!");
            modifier.put(Modifiers.AA_SPECIAL_C2_HQ,"AA");
            modifier.put(Modifiers.G_STAFF_COMMENTS,"G");
            //symbol.symbolicon A
            modifier.put(Modifiers.V_EQUIP_TYPE,"V");
            modifier.put(Modifiers.T_UNIQUE_DESIGNATION_1,"T");
            modifier.put(Modifiers.T1_UNIQUE_DESIGNATION_2,"T1");
            modifier.put(Modifiers.Z_SPEED,"999");//Z

            //sigint //TODO
            modifier.put(Modifiers.R2_SIGNIT_MOBILITY_INDICATOR, "2");
            modifier.put(Modifiers.AD_PLATFORM_TYPE, "AD");
            modifier.put(Modifiers.AE_EQUIPMENT_TEARDOWN_TIME, "AE");
            modifier.put(Modifiers.AF_COMMON_IDENTIFIER, "AF");
            //TODO
            modifier.put(Modifiers.AO_ENGAGEMENT_BAR, "AO:AOA-AO");
            modifier.put(Modifiers.AR_SPECIAL_DESIGNATOR, "AR");
            modifier.put(Modifiers.AQ_GUARDED_UNIT, "AQ");
            modifier.put(Modifiers.AS_COUNTRY, "USA");
            
            //*/
            //modifier.put(Modifiers.CN_CPOF_NAME_LABEL, "CPOF'D");

            if(msInfo.getSymbolSet()==25) {
                int drawRule = msInfo.getDrawRule();
                // Points
                if (drawRule == DrawRules.POINT17) {
                    modifier.put(Modifiers.AM_DISTANCE, "1500,1000");
                    modifier.put(Modifiers.AN_AZIMUTH, "45"); // degrees
                } else if (drawRule == DrawRules.POINT18) {
                    modifier.put(Modifiers.AM_DISTANCE, "1000,1500");
                    modifier.put(Modifiers.AN_AZIMUTH, "45,90"); // degrees
                }
                //Corridor
                else if (drawRule == DrawRules.CORRIDOR1) {
                    modifier.put(Modifiers.AM_DISTANCE, "1000");
                    modifier.put(Modifiers.X_ALTITUDE_DEPTH, "0,10");
                }
                // Ellipse
                else if (drawRule == DrawRules.ELLIPSE1) {
                    modifier.put(Modifiers.AM_DISTANCE, "1000,1500");
                    modifier.put(Modifiers.AN_AZIMUTH, "45");
                }
                // Rectangles
                if (drawRule == DrawRules.RECTANGULAR1) {
                    modifier.put(Modifiers.AM_DISTANCE, "1000");
                } else if (drawRule == DrawRules.RECTANGULAR2) {
                    modifier.put(Modifiers.AM_DISTANCE, "1000,6000");
                    modifier.put(Modifiers.AN_AZIMUTH, "45"); // mils
                } else if (drawRule == DrawRules.RECTANGULAR3) {
                    modifier.put(Modifiers.AM_DISTANCE, "1000");
                }
                // Circles
                else if (drawRule == DrawRules.CIRCULAR1) {
                    modifier.put(Modifiers.AM_DISTANCE, "1000");
                } else if (drawRule == DrawRules.CIRCULAR2) {
                    modifier.put(Modifiers.AM_DISTANCE, "750,1000,1500");
                    modifier.put(Modifiers.X_ALTITUDE_DEPTH, "0,10,100");
                }
                // Arc
                else if (drawRule == DrawRules.ARC1) {
                    modifier.put(Modifiers.AM_DISTANCE, "1000,1500,2200,3200,3500");
                    modifier.put(Modifiers.AN_AZIMUTH, "290,30,290,30,315,30,315,35"); // degrees
                    modifier.put(Modifiers.X_ALTITUDE_DEPTH, "0,10,100,200");
                }
            }
            /*
            modifier.putMap(new HashMap<String, String>());//reset
            if(SymbolUtilities.isWarfighting(symbol.getSymbolID()))
            {
                modifier.put(Modifiers.M_HIGHER_FORMATION,"1-10AVN/10AVNBDE/10THMTN");
                modifier.put(Modifiers.T_UNIQUE_DESIGNATION_1,"ACO/1-10AVN/10AVNBDE/10THMTN");
            }
            else
            {
                modifier.put(Modifiers.T_UNIQUE_DESIGNATION_1,"3BDE");
                modifier.put(Modifiers.T1_UNIQUE_DESIGNATION_2,"4BDE");
            }//*/
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("Tester", "populateModifiers", exc);
        }
        return modifier;
    }

    private void setModifiers_AM_AN_X(MilStdSymbol msd) {
        for (String mod : msd.getModifierMap().keySet()){
            if (mod == Modifiers.X_ALTITUDE_DEPTH || mod == Modifiers.AM_DISTANCE || mod == Modifiers.AN_AZIMUTH) {
                ArrayList<Double> values = new ArrayList<>();
                String[] arrValues = String.valueOf(msd.getModifierMap().get(mod)).split(",");
                for (String v : arrValues) {
                    if (!v.isEmpty()) {
                        values.add(Double.parseDouble(v));
                    }
                }
                msd.setModifiers_AM_AN_X(mod, values);
            }
        }
    }

    private Map<String,String> populateDefaultAttributes()
    {
        Map<String,String> attributes = new HashMap<>();
        attributes.put(MilStdAttributes.PixelSize, "50");
        return attributes;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        msTree = new javax.swing.JTree();
        lblVersion = new javax.swing.JLabel();
        cbVersion = new javax.swing.JComboBox<>();
        lblContext = new javax.swing.JLabel();
        cbContext = new javax.swing.JComboBox<>();
        lblAffiliation = new javax.swing.JLabel();
        cbAffiliation = new javax.swing.JComboBox<>();
        lblStatus = new javax.swing.JLabel();
        cbStatus = new javax.swing.JComboBox<>();
        lblHQTFD = new javax.swing.JLabel();
        cbHQTFD = new javax.swing.JComboBox<>();
        lblAmplifier = new javax.swing.JLabel();
        cbAmplifier = new javax.swing.JComboBox<>();
        lblSector1Mod = new javax.swing.JLabel();
        lblSector2Mod = new javax.swing.JLabel();
        tfSector1Mod = new javax.swing.JTextField();
        tfSector2Mod = new javax.swing.JTextField();
        btnDraw = new javax.swing.JButton();
        btnDrawMP = new javax.swing.JButton();
        tfSymbolID = new javax.swing.JTextField();
        cbPixelSize = new javax.swing.JComboBox<>();
        lblPixelSize = new javax.swing.JLabel();
        cbModifiers = new javax.swing.JCheckBox();
        btnSpeedTest = new javax.swing.JButton();
        cbOutlineStyle = new javax.swing.JComboBox<>();
        cbCache = new javax.swing.JCheckBox();
        lblS1I = new javax.swing.JLabel();
        cbS1I = new javax.swing.JComboBox<>();
        lblS2I = new javax.swing.JLabel();
        cbS2I = new javax.swing.JComboBox<>();
        lblLineWidth = new javax.swing.JLabel();
        tfLineWidth = new javax.swing.JTextField();
        lblFrameShape = new javax.swing.JLabel();
        cbFrameShape = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Tester");
        setMinimumSize(new java.awt.Dimension(1200, 900));
        setPreferredSize(new java.awt.Dimension(1200, 900));
        setSize(new java.awt.Dimension(1200, 900));
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(0, 153, 153));
        jPanel1.setPreferredSize(new java.awt.Dimension(1100, 819));

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("2525D");
        msTree.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        msTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                msTreeValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(msTree);

        lblVersion.setText("Version:");

        cbVersion.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbVersionItemStateChanged(evt);
            }
        });

        lblContext.setText("Context:");

        cbContext.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbContextItemStateChanged(evt);
            }
        });

        lblAffiliation.setText("Affiliation");

        cbAffiliation.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbAffiliationItemStateChanged(evt);
            }
        });

        lblStatus.setText("Status:");

        cbStatus.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbStatusItemStateChanged(evt);
            }
        });

        lblHQTFD.setText("HQTFD:");

        cbHQTFD.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbHQTFDItemStateChanged(evt);
            }
        });

        lblAmplifier.setText("Amplifier:");

        cbAmplifier.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbAmplifierItemStateChanged(evt);
            }
        });

        lblSector1Mod.setText("Sector1 Mod:");

        lblSector2Mod.setText("Sector2 Mod:");

        tfSector1Mod.setText("00");
        tfSector1Mod.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tfSector1ModKeyReleased(evt);
            }
        });

        tfSector2Mod.setText("00");
        tfSector2Mod.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tfSector2ModKeyReleased(evt);
            }
        });

        btnDraw.setText("Draw Icon");
        btnDraw.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDrawActionPerformed(evt);
            }
        });

        btnDrawMP.setText("Draw Symbol");
        btnDrawMP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDrawMPActionPerformed(evt);
            }
        });

        tfSymbolID.setEditable(false);
        tfSymbolID.setText("Symbol ID");

        cbPixelSize.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "50", "75", "100", "200" }));

        lblPixelSize.setText("Pixel Size");

        cbModifiers.setText("Modifiers");

        btnSpeedTest.setText("Speed Test");
        btnSpeedTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSpeedTestActionPerformed(evt);
            }
        });

        cbOutlineStyle.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "OutlineQ", "Outline", "ColorFill", "None" }));
        cbOutlineStyle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbOutlineStyleActionPerformed(evt);
            }
        });

        cbCache.setText("Cache");
        cbCache.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbCacheItemStateChanged(evt);
            }
        });

        lblS1I.setText("S1 Ind:");

        cbS1I.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" }));
        cbS1I.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbS1IItemStateChanged(evt);
            }
        });

        lblS2I.setText("S2 Ind:");

        cbS2I.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" }));
        cbS2I.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cbS2IItemStateChanged(evt);
            }
        });

        lblLineWidth.setText("Line Width");

        tfLineWidth.setText("5");

        lblFrameShape.setText("Frame Shape:");

        cbFrameShape.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0-Unknown", "1-Space", "2-Air", "3-Land U", "4-Land E/Sea", "5-Land Inst", "6-Dis. Ind.", "7-Sea Sub.", "8-Activity", "9-Cyber" }));
        cbFrameShape.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                cbFrameShapePropertyChange(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 217, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tfSymbolID))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(lblVersion)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblContext)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbContext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblAffiliation)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbAffiliation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblStatus)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblS1I, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cbS1I, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblS2I, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cbS2I, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(lblHQTFD)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbHQTFD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblAmplifier)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbAmplifier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblSector1Mod)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfSector1Mod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblSector2Mod)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tfSector2Mod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblFrameShape, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbFrameShape, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 42, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(cbCache, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnDraw))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(btnSpeedTest)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btnDrawMP))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(lblPixelSize)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbPixelSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(16, 16, 16))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(lblLineWidth)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cbModifiers)
                                    .addComponent(tfLineWidth, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(cbOutlineStyle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2)))
                        .addGap(14, 14, 14))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 493, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfSymbolID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 142, Short.MAX_VALUE)
                .addComponent(cbModifiers)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbOutlineStyle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfLineWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblLineWidth))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbPixelSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPixelSize))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblVersion)
                    .addComponent(cbVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblContext)
                    .addComponent(cbContext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblAffiliation)
                    .addComponent(cbAffiliation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblStatus)
                    .addComponent(cbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDraw)
                    .addComponent(cbCache)
                    .addComponent(lblS1I)
                    .addComponent(cbS1I, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblS2I)
                    .addComponent(cbS2I, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblHQTFD)
                    .addComponent(cbHQTFD, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblAmplifier)
                    .addComponent(cbAmplifier, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSector1Mod)
                    .addComponent(lblSector2Mod)
                    .addComponent(tfSector1Mod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfSector2Mod, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFrameShape)
                    .addComponent(cbFrameShape, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSpeedTest)
                    .addComponent(btnDrawMP))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnDrawActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDrawActionPerformed
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)msTree.getLastSelectedPathComponent();
        
        if (node == null)
        {
            //Nothing is selected.  
            return;
        }

        Object nodeInfo = (MSNodeInfo)node.getUserObject();
        if (node.isLeaf()) {
            
            MSNodeInfo msni = (MSNodeInfo) nodeInfo;
            String symbolID = buildSymbolID(msni.msID);
            drawMode = drawModeSPDraw;
            points.clear();
            btnDrawMP.setText("Draw Symbol");
            btnDraw.setBackground(Color.GREEN);
            btnDrawMP.setBackground(Color.LIGHT_GRAY);
        } 
    }//GEN-LAST:event_btnDrawActionPerformed

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        String symbolID = null;
        ImageInfo icon = null;
        Map<String,String> modifiers = null;
        Map<String,String> attributes = null;
        if(drawMode == drawModeSPDraw)
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)msTree.getLastSelectedPathComponent();
        
            if (node == null)
            {
                //Nothing is selected.  
                return;
            }

            Object nodeInfo = (MSNodeInfo)node.getUserObject();
            if (node.isLeaf()) {

                MSNodeInfo msni = (MSNodeInfo) nodeInfo;
                symbolID = buildSymbolID(msni.msID);
                drawMode = 0;
                btnDraw.setBackground(Color.LIGHT_GRAY);
                btnDrawMP.setBackground(Color.LIGHT_GRAY);
            } 
            
            if(symbolID != null)
            {
                modifiers = new HashMap<>();

                if(cbModifiers.isSelected())
                    modifiers = populateModifiers(symbolID);
                attributes = populateDefaultAttributes();
                //RendererSettings.getInstance().setLabelFont("algerian",Font.TRUETYPE_FONT,24);
                //RendererSettings.getInstance().setLabelFont("sans-serif",Font.TRUETYPE_FONT,24);
                //modifiers.put(Modifiers.T_UNIQUE_DESIGNATION_1,"MISSILE");
                //attributes.put(MilStdAttributes.LineColor,"#FF0000");
                //attributes.put(MilStdAttributes.FillColor,"#00FF00");
                //attributes.put(MilStdAttributes.FillColor,"#A020F0");
                //attributes.put(MilStdAttributes.TextColor,"#FF0000");
                //attributes.put(MilStdAttributes.TextBackgroundColor,"#00FF00");
                //attributes.put(MilStdAttributes.EngagementBarColor,"#FF0000");
                //attributes.put(MilStdAttributes.Alpha,"77");

                String pixelSize = String.valueOf(cbPixelSize.getSelectedItem());

                attributes.put(MilStdAttributes.PixelSize,pixelSize);
                
                Graphics2D g2d;
                try
                {
                    icon = MilStdIconRenderer.getInstance().RenderIcon(symbolID, modifiers, attributes);
                    g2d = (Graphics2D) this.getGraphics();
                    Graphics2D graphics = (Graphics2D)jPanel1.getGraphics();
                    Point2D mouseClickLocation = new Point2D.Double(evt.getPoint().getX(),evt.getPoint().getY());
                    if(icon != null) {
                        SymbolDraw.Draw(icon, g2d, (int) (mouseClickLocation.getX() - icon.getSymbolCenterX()), (int) (mouseClickLocation.getY() - icon.getSymbolCenterY()));
                        //icon.SaveImageToFile("C:\\temp\\test.png","png");
                    }
                    else
                    {
                        MSInfo msi = MSLookup.getInstance().getMSLInfo(symbolID);
                        String message = "RenderIcon - " + symbolID + " returned null.";
                        ErrorLogger.LogMessage("Tester", "formMouseClicked", message);
                    }
                }
                catch(RendererException rexc)
                {
                    ErrorLogger.LogException("Tester", "formMouseClicked", rexc);
                }
                catch(Exception exc)
                {
                    ErrorLogger.LogException("Tester", "formMouseClicked", exc);
                }
                drawMode = 0;
                btnDraw.setBackground(Color.LIGHT_GRAY);
                btnDrawMP.setBackground(Color.LIGHT_GRAY);
            }

            //System.out.println("COMPILED SVG:");
            //System.out.println(SVGLookup.compileSVG("D:/2525symbols/SymbolsE","130310000012110041600000000000","10120800"));
            String svg = MilStdIconRenderer.getInstance().RenderSVG(symbolID,modifiers, attributes).getSVG();
            copyToClipboard(svg);
            System.out.println(svg);

            //Test adding of custom symbol
            //test with code: 130310000016570000000000000000
            //should see "MWR" in red text
            //String svgCustom = MilStdIconRenderer.getInstance().RenderSVG("130310000016570000000000000000",modifiers, attributes).getSVG();
            //System.out.println("\nCustom SVG: \n" + svgCustom);
        }
        else if (drawMode == drawModeMPDraw)
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)msTree.getLastSelectedPathComponent();
        
            if (node == null)
            {
                //Nothing is selected.  
                return;
            }

            Object nodeInfo = (MSNodeInfo)node.getUserObject();
            

            MSNodeInfo msni = (MSNodeInfo) nodeInfo;
            symbolID = buildSymbolID(msni.msID);
               
             
            
            MSInfo msi = MSLookup.getInstance().getMSLInfo(symbolID);
            
            if(msi.getDrawRule() != DrawRules.DONOTDRAW)
            {
                Point2D mouseClickLocation = new Point2D.Double(evt.getPoint().getX(),evt.getPoint().getY());

                points.add(mouseClickLocation);
                btnDrawMP.setText("Points: " + points.size() + "         ");
                if(points.size() == maxPointCount)//draw symbol
                {
                    RenderMultiPoint();
                }
            }
            else
                return;
        }
    }//GEN-LAST:event_formMouseClicked

    private void RenderMultiPoint() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)msTree.getLastSelectedPathComponent();
        MSNodeInfo msni = (MSNodeInfo) node.getUserObject();

        String symbolID = buildSymbolID(msni.msID);
        MSInfo msi = MSLookup.getInstance().getMSLInfo(symbolID);

        int height = this.getHeight();
        int width = this.getWidth();

        ErrorLogger.LogMessage("panel w,h" + String.valueOf(width) + "," + String.valueOf(height));

        Map<String,String> modifiers = populateModifiers(symbolID);

        double geoLeft = 49.9;
        double geoRight = 50;
        double geoTop = 20;
        double geoBottom = geoTop - ((geoRight - geoLeft) * height / width);
        PointConversion pConverter = new PointConversion(width, height, geoTop, geoLeft, geoBottom, geoRight);

        ArrayList<Point2D> coords = new ArrayList<>();
        for (Point2D pt : points)
            coords.add(pConverter.PixelsToGeo(pt));

        MilStdSymbol ms = new MilStdSymbol(symbolID, "id", coords, modifiers);
        setModifiers_AM_AN_X(ms);
        ms.setUseFillPattern(true);

        ms.setUnitSize(Integer.valueOf(cbPixelSize.getSelectedItem().toString()));
        ms.setLineWidth(Integer.valueOf(tfLineWidth.getText()));

        // Not perfect but better than default 96
        RendererSettings.getInstance().setDeviceDPI(Toolkit.getDefaultToolkit().getScreenResolution());

        //font change test
        //RendererSettings.getInstance().setMPLabelFont("Times New Roman",Font.BOLD,32);

        ms = render(ms, pConverter, null);
        Graphics2D graphics = (Graphics2D) this.getGraphics();
        //Graphics2D graphics = (Graphics2D)jPanel1.getGraphics();
        try
        {
            SymbolDraw.Draw(ms, graphics, 0, 0);
        }
        catch(RendererException rexc)
        {
            ErrorLogger.LogException("Tester", "formMouseClicked", rexc);
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("Tester", "formMouseClicked", exc);
        }
        drawMode = 0;
        btnDraw.setBackground(Color.LIGHT_GRAY);
        btnDrawMP.setBackground(Color.LIGHT_GRAY);
        btnDrawMP.setText("Draw Symbol");

        //Generate GeoJSON
        Map<String,String> attributes = new HashMap<>();
        attributes.put(MilStdAttributes.LineWidth,String.valueOf(ms.getLineWidth()));
        int format = WebRenderer.OUTPUT_FORMAT_GEOJSON;
        renderWebFormat(msi, symbolID, ms, pConverter,modifiers,attributes,format);

        points.clear();

    }

    /**
     *
     * @param msi
     * @param symbolID
     * @param ms
     * @param pc
     * @param modifiers
     * @param attributes
     * @param format
     */
    private void renderWebFormat(MSInfo msi, String symbolID, MilStdSymbol ms, PointConversion pc,Map<String,String> modifiers, Map<String,String> attributes,int format)
    {

        String points = "";
        //bbox The viewable area of the map.  Passed in the format of a
        //lowerLeftX,lowerLeftY,upperRightX,upperRightY."
        //example: "-50.4,23.6,-42.2,24.2"
        String bbox = String.valueOf(pc.getLeftLon()) + "," + String.valueOf(pc.getLowerLat()) + "," + String.valueOf(pc.getRightLon()) + "," + String.valueOf(pc.getUpperLat());

        String svg = null;
        String result = null;
        String result3D = null;

        ArrayList<Point2D> coords =  ms.getCoordinates();

        for(Point2D point : coords)
        {
            points += String.valueOf(point.getX()) + "," + String.valueOf(point.getY()) + " ";
        }

        try
        {
            result = WebRenderer.RenderSymbol2D("id",msi.getName(),msi.getPath(),symbolID,points,pc.getPixelWidth(),pc.getPixelHeight(),bbox,modifiers,attributes,format);

            svg = WebRenderer.RenderSymbol2D("id",msi.getName(),msi.getPath(),symbolID,points,pc.getPixelWidth(),pc.getPixelHeight(),bbox,modifiers,attributes,WebRenderer.OUTPUT_FORMAT_GEOSVG);

            String altMode = "clampToGround";
            double scale = 50000;
            result3D = WebRenderer.RenderSymbol("id",msi.getName(),msi.getPath(),ms.getSymbolID(),points,altMode,scale,bbox,modifiers,attributes,format);



        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("Tester","renderWebFormat",exc);
        }

        ErrorLogger.LogMessage("Tester","renderWebFormat - 2D",result);
        ErrorLogger.LogMessage("Tester","renderWebFormat - 3D",result3D);
        ErrorLogger.LogMessage("Tester","renderWebFormat - 2D GeoSVG",svg);
        copyToClipboard(svg);
    }

    private void btnDrawMPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDrawMPActionPerformed
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)msTree.getLastSelectedPathComponent();
        if (node == null)
        {
            //Nothing is selected.  
            return;
        }

        Object nodeInfo = (MSNodeInfo)node.getUserObject();
        if (node.isLeaf()) {
            
            MSNodeInfo msni = (MSNodeInfo) nodeInfo;
            String symbolID = buildSymbolID(msni.msID);
            MSInfo msi = MSLookup.getInstance().getMSLInfo(symbolID);
            drawMode = drawModeMPDraw;
            btnDraw.setBackground(Color.LIGHT_GRAY);
            btnDrawMP.setBackground(Color.GREEN);
            maxPointCount = msi.getMaxPointCount();
            if(maxPointCount > 6)
                maxPointCount = 6;
            if (points.size() >= msi.getMinPointCount())
                RenderMultiPoint();
        }
    }//GEN-LAST:event_btnDrawMPActionPerformed

    private void msTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_msTreeValueChanged
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)msTree.getLastSelectedPathComponent();
        
        if (node == null)
        {
            //Nothing is selected.  
            return;
        }

        points.clear();
        btnDrawMP.setText("Draw Symbol");

        Object nodeInfo = (MSNodeInfo)node.getUserObject();
        if (node.isLeaf()) {
            
            MSNodeInfo msni = (MSNodeInfo) nodeInfo;
            String symbolID = buildSymbolID(msni.msID);
            currentBasicID = msni.msID;
            tfSymbolID.setText(symbolID);
            btnDrawMP.setEnabled(SymbolUtilities.isMultiPoint(symbolID));
        } 
    }//GEN-LAST:event_msTreeValueChanged

    private void btnSpeedTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSpeedTestActionPerformed

        String symbolID = null;
        ImageInfo icon = null;
        MSInfo msInfo = null;
        try
        {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) msTree.getLastSelectedPathComponent();

            if (node == null) {
                //Nothing is selected.
                return;
            }

            Object nodeInfo = (MSNodeInfo) node.getUserObject();
            if (node.isLeaf()) {

                MSNodeInfo msni = (MSNodeInfo) nodeInfo;
                symbolID = buildSymbolID(msni.msID);
                msInfo = MSLookup.getInstance().getMSLInfo(symbolID);
            }

            if (symbolID != null)
            {
                Map<String, String> modifiers = new HashMap<>();

                if (cbModifiers.isSelected())
                    modifiers = populateModifiers(symbolID);
                Map<String, String> attributes = populateDefaultAttributes();


                String pixelSize = String.valueOf(cbPixelSize.getSelectedItem());

                attributes.put(MilStdAttributes.PixelSize, pixelSize);

                ErrorLogger.setLevel(Level.WARNING,true);

                Graphics2D g2d;
                int limit = 3000;
                try
                {
                    long startTime = System.currentTimeMillis();
                    for(int i = 0; i < limit; i++)
                    {
                        icon = MilStdIconRenderer.getInstance().RenderIcon(symbolID, modifiers, attributes);
                    }
                    long endTime = System.currentTimeMillis();
                    String message = String.valueOf(limit) + " " + msInfo.getName() + " drawn in " + String.valueOf((endTime - startTime)/1000f) + " seconds.";
                    JOptionPane.showMessageDialog(null,message ,"Speed Test",1);

                    g2d = (Graphics2D) this.getGraphics();
                    Graphics2D graphics = (Graphics2D) jPanel1.getGraphics();
                    Point2D mouseClickLocation = new Point2D.Double(this.getWidth()/2, this.getHeight()/4);
                    SymbolDraw.Draw(icon, g2d, (int) (mouseClickLocation.getX() - icon.getSymbolCenterX()), (int) (mouseClickLocation.getY() - icon.getSymbolCenterY()));

                    //SVG Speed Test
                    SVGSymbolInfo svg = null;
                     startTime = System.currentTimeMillis();
                    for(int i = 0; i < limit; i++)
                    {
                        svg = MilStdIconRenderer.getInstance().RenderSVG(symbolID, modifiers, attributes);
                    }
                     endTime = System.currentTimeMillis();
                    message = String.valueOf(limit) + " " + msInfo.getName() + " drawn in " + String.valueOf((endTime - startTime)/1000f) + " seconds.";
                    JOptionPane.showMessageDialog(null,message ,"Speed Test",1);
                }
                catch (Exception exc) {
                    ErrorLogger.LogException("Tester", "formMouseClicked", exc);
                }

                ErrorLogger.setLevel(Level.INFO,true);
            }
        }
        catch(Exception exc)
        {
            ErrorLogger.LogException("Tester","btnSpeedTestActionPerformed",exc);
        }

    }//GEN-LAST:event_btnSpeedTestActionPerformed

    private void cbOutlineStyleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbOutlineStyleActionPerformed
        int index = cbOutlineStyle.getSelectedIndex();
        if(index == 0)
            RendererSettings.getInstance().setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_OUTLINE_QUICK);
        else if(index == 1)
            RendererSettings.getInstance().setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_OUTLINE);
        else if(index == 2)
            RendererSettings.getInstance().setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_COLORFILL);
        else if(index == 3)
            RendererSettings.getInstance().setTextBackgroundMethod(RendererSettings.TextBackgroundMethod_NONE);
    }//GEN-LAST:event_cbOutlineStyleActionPerformed

    private void cbCacheItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbCacheItemStateChanged
        RendererSettings.getInstance().setCacheEnabled(cbCache.isSelected());
    }//GEN-LAST:event_cbCacheItemStateChanged

    private void cbVersionItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbVersionItemStateChanged

        if (evt.getStateChange() == ItemEvent.SELECTED)
        {
            String version = (String)cbVersion.getSelectedItem();
            int ver = Integer.parseInt(version.substring(0,2));

            DefaultMutableTreeNode root = (DefaultMutableTreeNode)msTree.getModel().getRoot();
            if(root != null)
                root.removeAllChildren();
            loadTree(ver);
            msTree.updateUI();
            updatedSymbolIDField();
        }

    }//GEN-LAST:event_cbVersionItemStateChanged

    private void cbAffiliationItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbAffiliationItemStateChanged
        updatedSymbolIDField();
    }//GEN-LAST:event_cbAffiliationItemStateChanged

    private void cbContextItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbContextItemStateChanged
        updatedSymbolIDField();
    }//GEN-LAST:event_cbContextItemStateChanged

    private void cbStatusItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbStatusItemStateChanged
        updatedSymbolIDField();
    }//GEN-LAST:event_cbStatusItemStateChanged

    private void cbS1IItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbS1IItemStateChanged
        updatedSymbolIDField();
    }//GEN-LAST:event_cbS1IItemStateChanged

    private void cbS2IItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbS2IItemStateChanged
        updatedSymbolIDField();
    }//GEN-LAST:event_cbS2IItemStateChanged

    private void cbHQTFDItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbHQTFDItemStateChanged
        updatedSymbolIDField();
    }//GEN-LAST:event_cbHQTFDItemStateChanged

    private void cbAmplifierItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cbAmplifierItemStateChanged
        updatedSymbolIDField();
    }//GEN-LAST:event_cbAmplifierItemStateChanged

    private void cbFrameShapePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_cbFrameShapePropertyChange
        updatedSymbolIDField();
    }//GEN-LAST:event_cbFrameShapePropertyChange

    private void tfSector2ModKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfSector2ModKeyReleased
        updatedSymbolIDField();
    }//GEN-LAST:event_tfSector2ModKeyReleased

    private void tfSector1ModKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfSector1ModKeyReleased
        updatedSymbolIDField();
    }//GEN-LAST:event_tfSector1ModKeyReleased

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Tester.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Tester.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Tester.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Tester.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Tester().setVisible(true);
            }
        });
        
        
        
    }
    
        public MilStdSymbol render(MilStdSymbol symbol, IPointConversion converter, Object clipBounds) {
        
        try
        {
            armyc2.c5isr.RenderMultipoints.clsRenderer.renderWithPolylines(symbol, converter, clipBounds);
        }
        catch(Exception exc)
        {
            String message = "Failed to build multipoint TG";
            if(symbol != null)
                message = message + ": " + symbol.getSymbolID();
            //ErrorLogger.LogException(this.getClass().getName() ,"ProcessTGSymbol()",
            //        new RendererException(message, exc));
            System.err.println(exc.getMessage());
        }
        catch(Throwable t)
        {
            String message2 = "Failed to build multipoint TG";
            if(symbol != null)
                message2 = message2 + ": " + symbol.getSymbolID();
            //ErrorLogger.LogException(this.getClass().getName() ,"ProcessTGSymbol()",
            //        new RendererException(message2, t));
            System.err.println(t.getMessage());
        }
        
        return symbol;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDraw;
    private javax.swing.JButton btnDrawMP;
    private javax.swing.JButton btnSpeedTest;
    private javax.swing.JComboBox<String> cbAffiliation;
    private javax.swing.JComboBox<String> cbAmplifier;
    private javax.swing.JCheckBox cbCache;
    private javax.swing.JComboBox<String> cbContext;
    private javax.swing.JComboBox<String> cbFrameShape;
    private javax.swing.JComboBox<String> cbHQTFD;
    private javax.swing.JCheckBox cbModifiers;
    private javax.swing.JComboBox<String> cbOutlineStyle;
    private javax.swing.JComboBox<String> cbPixelSize;
    private javax.swing.JComboBox<String> cbS1I;
    private javax.swing.JComboBox<String> cbS2I;
    private javax.swing.JComboBox<String> cbStatus;
    private javax.swing.JComboBox<String> cbVersion;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAffiliation;
    private javax.swing.JLabel lblAmplifier;
    private javax.swing.JLabel lblContext;
    private javax.swing.JLabel lblFrameShape;
    private javax.swing.JLabel lblHQTFD;
    private javax.swing.JLabel lblLineWidth;
    private javax.swing.JLabel lblPixelSize;
    private javax.swing.JLabel lblS1I;
    private javax.swing.JLabel lblS2I;
    private javax.swing.JLabel lblSector1Mod;
    private javax.swing.JLabel lblSector2Mod;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JTree msTree;
    private javax.swing.JTextField tfLineWidth;
    private javax.swing.JTextField tfSector1Mod;
    private javax.swing.JTextField tfSector2Mod;
    private javax.swing.JTextField tfSymbolID;
    // End of variables declaration//GEN-END:variables
}
