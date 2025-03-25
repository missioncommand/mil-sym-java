package armyc2.c5isr.renderer.utilities;
public class EntityCode {
    public static final int EntityCode_FLOT = 140100;

    public static final int EntityCode_BioContaminatedArea = 271700;

    public static final int EntityCode_ChemContaminatedArea = 271800;

    public static final int EntityCode_NuclearContaminatedArea = 271900;
    public static final int EntityCode_RadiologicalContaminatedArea = 272000;


    public static final int EntityCode_BioEvent = 281400;

    public static final int EntityCode_ChemicalEvent = 281300;

    public static final int EntityCode_NuclearEvent = 281500;

    public static final int EntityCode_RadiologicalEvent = 281700;

    public static final int EntityCode_AnchoragePoint = 120304;

    /**
     * Returns the modifier icon for a given contamination area
     * @param contaminationArea the entity code of the contamination area
     * @return the entity code of the icon that should be displayed within it
     */
    public static int getSymbolForContaminationArea(int contaminationArea) {
        switch (contaminationArea) {
            case EntityCode_BioContaminatedArea:
                return EntityCode_BioEvent;

            case EntityCode_ChemContaminatedArea:
                return EntityCode_ChemicalEvent;

            case EntityCode_NuclearContaminatedArea:
                return EntityCode_NuclearEvent;

            case EntityCode_RadiologicalContaminatedArea:
                return EntityCode_RadiologicalEvent;

            default:
                return -1;
        }
    }
}
