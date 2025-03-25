package armyc2.c5isr.renderer.utilities;

/**
 * Units of Measure to be used with {@link MilStdAttributes#DistanceUnits}
 * Default is meters.
 */
public class DistanceUnit
{
    private static final double FEET_PER_METER = 3.28084;
    private static final double FLIGHT_LEVEL_PER_METER = 0.0328084; // hundreds of feet
    public final double conversionFactor;
    public final String label;

    public DistanceUnit(double conversionFactor, String label){
        this.conversionFactor = conversionFactor;
        this.label = label;
    }

    public static DistanceUnit parse(String distanceUnitText){
        if(distanceUnitText == null){
            return null;
        }
        String[] parts = distanceUnitText.split(",");
        if(parts.length != 2){
            return null;
        }
        double conversionFactor = Double.parseDouble(parts[0].trim());
        String label = parts[1].trim();

        return new DistanceUnit(conversionFactor, label);
    }

    public String toAttribute(){
        return conversionFactor + "," + label;
    }

    public static DistanceUnit METERS = new DistanceUnit(1, "M");
    public static DistanceUnit FEET = new DistanceUnit(FEET_PER_METER, "FT");
    public static DistanceUnit FLIGHT_LEVEL = new DistanceUnit(FLIGHT_LEVEL_PER_METER, "FL");
}