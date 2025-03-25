package armyc2.c5isr.renderer;

import armyc2.c5isr.JavaLineArray.TacticalLines;
import armyc2.c5isr.JavaTacticalRenderer.TGLight;
import armyc2.c5isr.RenderMultipoints.clsUtility;
import armyc2.c5isr.renderer.utilities.*;
import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.parser.SVGLoader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Created by michael.spinelli on 8/23/2017.
 */
public class PatternFillRenderer {
    private static final String duriBeachSlopeModerate = "iVBORw0KGgoAAAANSUhEUgAAAB4AAAAeBAMAAADJHrORAAAAJ1BMVEUAAADJycnLy8vMzMzNzc3Nzc3Ly8vMzMzNzc3Ly8vMzMzMzMz///8wQwFSAAAAC3RSTlMAISJkZWZn9PT19frFp7oAAAABYktHRAyBs1FjAAAAN0lEQVQY02NgGJKAKVQAhW+xsgVFunv3DmQFbKt37wpA4rPO3r3dAY96Bo+ZzSjmM7oIDM2AAgCrggxuTOFUWwAAAABJRU5ErkJggg==";
    private static final String duriBeachSlopeSteep = "iVBORw0KGgoAAAANSUhEUgAAAB4AAAAeBAMAAADJHrORAAAAJ1BMVEUAAADJycnLy8vMzMzNzc3Nzc3Ly8vMzMzNzc3Ly8vMzMzMzMz///8wQwFSAAAAC3RSTlMAISJkZWZn9PT19frFp7oAAAABYktHRAyBs1FjAAAAN0lEQVQY02NgGJKAKVQAhW+xsgVFunv3DmQFbKt37wpA4rPO3r3dAY96Bo+ZzSjmM7oIDM2AAgCrggxuTOFUWwAAAABJRU5ErkJggg==";
    private static final String duriFoulGround = "iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAMAAACahl6sAAABwlBMVEUAAAAAAACAgICqqqqAgICZmZmAgICSkpKOjo6AgICJiYmAgICIiIiHh4eGhoaAgICAgICFhYWFhYWEhISAgICAgICAgICAgICAgICAgICAgICAgICAgICCgoKAgICCgoKAgICAgICCgoKCgoKAgICAgICAgICBgYGAgICBgYGAgICAgICBgYGAgICBgYGBgYGBgYGAgICBgYGBgYGAgICBgYGBgYGBgYGAgICBgYGBgYGCgoKBgYGCgoKBgYGCgoKBgYGAgICBgYF/f3+AgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIB/f3+AgICAgICAgICBgYGAgICAgICBgYGAgICAgICAgIB/f3+AgICBgYGAgICAgICBgYGAgICBgYGBgYGAgICBgYGAgICAgICAgICBgYF/f3+AgICAgICBgYF/f3+AgICAgICBgYF/f3+AgICAgICBgYF/f3+AgICAgICBgYF/f3+AgICAgICBgYGAgICAgICBgYF/f3+AgICAgICBgYF/f3+AgID///+d1uhFAAAAk3RSTlMAAQIDBAUGBwkKDQ4PERMUFhcZHR4gIiQmKiwuMDM2Nzo8PT9CREhJTk9QVFVWV1tdXl9hZGVna25vcXJ1dnd4e36Ag4WIiYqLkpSVmZqcnp+ipaqvsLS4u72+v8HCxcjJzc7P0dLT19rb3t7f4+Tl5urr7O7v8PDx8fLy8/P09PX19vb39/j4+fn6+vv8/P39/v6q9ZhUAAAAAWJLR0SVCGB6gwAAAsBJREFUeNrt3MdTU1EcxXEEFVCxd1FU7L2iooINsBcUFStYsGMBA4+IGjAPg6CBfP9gF/eCYQe7+5Pz2Z3JJmdyX3JnYE5BgYiIebNjgGc+NQFkyywWWd8NZC/49BngtclPpDYDJLa5sPIrMHrFZJGHAPF8Fw5/A6JdFnsUpgDe+nQLyKUXWyyy5gswctmnDoAPJk/WkRQQ7XRheQqg0WSR2wA/FrlwsBfo3W+ySCdAuw83AVLLLPZwh+mGTx8BOk1+IAd6geReF5b054Amk0UagVzfUhf2JYHvVXbefWXNuA6AjA+PAZKXxl5bEHyR80zGwOr/pEikIioyVfVDsTeYA8YDwOBYirPhFykp8w71AP21LqyIAZrLxhXZ+R6+PgL0lLuwowvoP2ry57AdIFXowsUsEJVb7LEwDdDs0yuA/iKLRfZEQLrahTkDAA9Mnqxro3mPyNYEEJ80WeQdQMofpobfQHeFyUckBrjnUxtAepbFIrsjIH3c/7ZkAFoMvf33/y4ho8CvsdA34X7yIvwinyZ10XqjIioybYvU1HkRQJ8PrUNA4urYa3XH7Hx7lWYA7vv0BCAutvgrsjkBDJxwwf3ZqtXk/eTcEBCtc2FDAhg6a7LI0/wryakMkNhksUfxhCvJI4BMqcUiG7uAwTMuFPUBtJk8WaczQKLShbURMNxgskgLQFziQnUa6NpiscfMNMBzn+4C/JxnsUhFBAzXuzAjCfDS5MmqifMO06re/H9+sOUOwJ+5LlQBsL1AREREREREREREREREREREREREREREpjHt3IVGO3eB0c5daLRzFxrt3AVGO3dB0M6dzSKah1ORKdPOXbC0cxcY7dyFRjt3oT0i2rkLgnbuVERFpkkR7dyFSjt3odHOXWC0cxca7dwFRjt3odHOnYiIiIiIiIiIiIiIiIiIiIiIiATiL9SSl1hXwqRzAAAAAElFTkSuQmCC";
    private static final String duriKelp = "iVBORw0KGgoAAAANSUhEUgAAAMgAAAD6CAMAAADTNPgKAAACWFBMVEUAAAAAAACAgICqqqqAgICZmZmAgICSkpKAgICOjo6AgICLi4uAgICJiYmAgICIiIiAgICHh4eAgICGhoaAgICGhoaAgICFhYWAgICEhISAgICAgICEhISAgICAgICDg4OHh4eAgICDg4OAgICCgoKAgICCgoKAgICAgICCgoKAgICCgoKAgICCgoKCgoKAgICCgoKAgICCgoKAgICCgoKAgICEhISCgoKAgICBgYGAgICBgYGAgICBgYGAgICBgYGAgICBgYGAgICBgYGBgYGAgICBgYGAgICBgYGAgICBgYGAgICBgYGBgYGAgICBgYGAgICAgICAgICAgICBgYGAgICAgICBgYGAgICAgICBgYGAgICBgYGAgICBgYGCgoKBgYGAgICBgYGAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBgYGAgICAgICAgICAgICAgICAgICAgICBgYGAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBgYGAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICBgYGAgICAgICBgYGAgICAgICBgYGAgICAgICAgICAgIB/f3+AgID////WIOTmAAAAxXRSTlMAAQIDBAUGBwgJCgsMDQ4PEBESExQVFhcYGxweHyAiIyQmJyorLC0uMDEyMzQ1Nzg5Ojs8PT4+P0BBQkVGR0hLTE1OT1FSU1RVVldYWVteX2BkZmhpam5vcHJzdHV2d3h7fH1+gIGChYeIi46RkpSZmpucnZ+goqSlpqeqq62ur7GztLW4ubq7vLy9v8DBwsPExMXGx8nMzdDR0tPU1dfY29zd3t/h4uPk5ebn6Orr7O3u7/Dx8vP09fb29/j4+fr6+/z9/gntg1kAAAABYktHRMeNBUpbAAADR0lEQVR42u3dZ1MTURSH8aVawAb2EgXsWFBRQbErggUL9gpW1CiKBTv2LvZFRbGLKAoClhhEE+L5XC6ZcWy4m3GGzCXz/F4luTeZ/W+5ezZvjqb5ZEJ5nhYQYh0yKTCSLJeXUQERJLhQjgSpfdK08G2erUrWKx2k5OPxjAhfJo53edJVDlIkIjW7Rvh0mbwfrHIS2xrdyFKyqovlzL1SOUjtKzlmwzMR9/k5IebTQg5K1RDFF6XQKQVukafZ3UxnhR2ViuGKJwnK/CRfRVzHJof+e1JEn4tSt0jpHJ2PiRyKHpXrEKncN6WRLC2TNp4pFa/cUF9/NW2kv3NMfCnVcxtedFp539jUsp3pHX8dDp9+4F1DBOfTIv1FndyI8+1XE2tdA/wao5Vd5Fz3H+8GbH5sbHO9viMzsYP3gx455cYHt7dNtQU3vB1ZKrVZYb7snQ9i9+/xiHM6l/5WfgxefdbhPYvePNSvPqwTubOy18/RtrkeKU61qldabXFLXoifz6zZff++Jsat2H3rvTeNe3/CH4PJj4xsM8yiRC8rE9c6ZYqzqO1fxHMttpF1OMOIUmof2NiXIgfOzD5nHEV9mCoxQhZXiCvP1vhgeGbDlVSUMzlK65a8IGfvodOXdV0vflzxwXsUXSenKXM4ku4aS0A/kztP4s63xiZ/q5HfOYsLtqZGK3NjaW+vl+cZFpPGnP5sbHlV4Z6sjLSUsfHx8f1tHSM0pcwqF2e2+ZNK0KQrRoqba5OD1b3P9zghcinGfM7wm0YRsDlG6bJrYY1ULTXfz53zPVK2LFLpsivuulF2dbUool6LY0tbpWOEramVV6kWC8FhkVM91a7mE+6JZ5/F/z5Dn0j1fMWfSubVy4PRFnPSHaL3VjyH1rXC3tpiJdjkkR3hmvLaWYy3yBf3Eq35C7og1SlaIBhXEqcFhlANAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAgwLRvExg5aEShHBpR+O+koRGFWiJpRKEiGlEoiEYUZmhE8Z9oRGG6d2hE0QRoREEjiiZ4RqcRhUpoRKEUGlEohUYUiqERRXNDIwoF/27RAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAmovvgWPyAz3pI4IAAAAASUVORK5CYII=";
    private static final String duriRigField = "iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAMAAAAp4XiDAAAAZlBMVEUAAAD///+qqqrMzMzV1dW+vr6/v7/BwcG+vr6/v7/AwMDBwcHBwcG/v7/AwMDAwMDBwcG/v7/AwMC/v7/AwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMB3wKozAAAAIXRSTlMAAgMFBk9QUlNUVVZXiImKi4yjpKan19jZ2tvc3d7x8/RAdLIxAAAAAWJLR0QB/wIt3gAAAJBJREFUGBntwcsOgjAUBcCDlDciCIICWs7//6QbFtAmt03cmc4gCP6Xqodp26ahUvCUr9wtGXxcOh60Edw6njRwymlI4aAWGt4KspqWErIHLT1kL1omyDQtGjJNyweyJy0jZAMtd8gqWgrI4pmGVcEhoyGBU8uTK9yiGw+aCD7Smbs5gae47Eetx76IEQTBT77LTiffv21oRgAAAABJRU5ErkJggg==";
    private static final String duriSweptArea = "iVBORw0KGgoAAAANSUhEUgAAAJYAAACWCAMAAAAL34HQAAAAq1BMVEUAAAD/AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP//AP////9KmvTxAAAAN3RSTlMAAwQLDA0kJSYnKCorLC4vS0xNTltcXW1ub3BxcnOoqarMzc7P0NHS09TV1uvs7e7x8vP4+fr7bOJr4wAAAAFiS0dEOKAHpdYAAAGLSURBVHja7drrTsIwGMbxgiCKKAdFHBuIMBVh4yTQ+78zjR/Moju0Cdnb6P93BU/WdevbPEoltYPJy/pwWD9P/LZyRH201AmLoOZAqKq/0j/Ew6p0qmaoU8wuZVPd7nSqbU8y1f1RZzj2BZ9VZqrPXF2pVI2tzrFrCu3BUOeayuxHXxfwJFKdRUWx4rpArJEuFAjEWhbHWpSfqq0NXLu4hhIv/cQk1rj0WK8mscLSY8UmsaLSY+1NYr0Ty+lFdPSVd/QDEZjEeig91o1JrFb5P8W5k79qk1X03TwGiozXw6JYA5kRY5af6qkiM/qcb1wcyJTq5Y2vHbmxup897N9JXkJ0MwbrTUeJupimvu0N8Ws379cRJxpUlLxa8JYMNfdduKT80vIew9V+H4dj70oBAAAAAAAAACCJHrIhesjm6CFbPCt6yOZ7kB6yOXrINughn3YN6SF/o4dsgx7yH1hEesg26CFboYd84mMgPeTEiEEP2Qo9ZDv0kC33Iz1kW/SQAQAAAAAAAOCf+AADVeTkR5OAwAAAAABJRU5ErkJggg==";
    private static final String duriFishTraps = "iVBORw0KGgoAAAANSUhEUgAAAHwAAABoBAMAAADBd4kAAAAAIVBMVEUAAADCwsLFxcXCwsLAwMDAwMDAwMDAwMDAwMDAwMD///+Tgr1MAAAACXRSTlMAFRYZaWrS09eb/mznAAAAAWJLR0QKaND0VgAAAF5JREFUWMPt17EJgEAQRNERrMHYEmzHEi61BZvQbu1AhUW84E16PH54bJIk45zK1q3Ep2Op8KHt8vLy8g877/c1L7ziOI7jeNf81x/2zZw68vLyTh2nDo7jOI53dupcdiT5TbRBKr0AAAAASUVORK5CYII=";

    private static BufferedImage patternBeachSlopeModerate = null;
    private static BufferedImage patternBeachSlopeSteep = null;
    private static BufferedImage patternFoulGround = null;
    private static BufferedImage patternKelp = null;
    private static BufferedImage patternRigField = null;
    private static BufferedImage patternSweptArea = null;
    private static BufferedImage patternFishTraps = null;

    /**
     * @param hatchStyle Direction of hatch lines - constants from clsUtility
     * @param spacing horizontal spacing between lines
     * @param strokeWidth width of lines
     * @param color Color of lines
     * @return buffered image with one tile of hatch fill
     */
    public static BufferedImage MakeHatchPatternFill(int hatchStyle, int spacing, int strokeWidth, Color color) {
        if (hatchStyle == clsUtility.Hatch_Cross) {
            BufferedImage forward = MakeHatchPatternFill(clsUtility.Hatch_ForwardDiagonal, spacing, strokeWidth, color);
            BufferedImage backward = MakeHatchPatternFill(clsUtility.Hatch_BackwardDiagonal, spacing, strokeWidth, color);

            // concat results
            BufferedImage result = new BufferedImage(spacing, spacing, forward.getType());
            Graphics g = result.getGraphics();
            g.drawImage(forward, 0, 0, null);
            g.drawImage(backward, 0, 0, null);
            g.dispose();
            return result;
        }

        int x1, x2;
        if (hatchStyle == clsUtility.Hatch_ForwardDiagonal) {
            x1 = spacing + strokeWidth;
            x2 = -strokeWidth;
        } else if (hatchStyle == clsUtility.Hatch_BackwardDiagonal) {
            x1 = -strokeWidth;
            x2 = spacing + strokeWidth;
        } else {
            return new BufferedImage(spacing, spacing, BufferedImage.TYPE_INT_ARGB);
        }

        String colorStr = "#" + Integer.toHexString(color.getRGB()).substring(2);

        /*
         * SVG is a square with 3 diagonal lines going through it. All lines have the same slope x values
         * The middle line goes through both corners and the other lines are offset in y by +/- boxLength
         * Each line extends past the box to confirm the line fills each of the 4 corners
         */
        String hatchFillSVGString = "<svg width=\"" + spacing + "\" height=\"" + spacing + "\" xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">" +
                "<line id=\"middle_line\" stroke=\"" + colorStr + "\" stroke-width=\"" + strokeWidth + "\" x1=\"" + x1 + "\" x2=\"" + x2 + "\" y1=\"" + (spacing + strokeWidth) + "\" y2=\"" + (-strokeWidth) + "\"/>" +
                "<line id=\"bottom_line\" stroke=\"" + colorStr + "\" stroke-width=\"" + strokeWidth + "\" x1=\"" + x1 + "\" x2=\"" + x2 + "\" y1=\"" + (spacing * 2 + strokeWidth) + "\" y2=\"" + (spacing - strokeWidth) + "\"/>" +
                "<line id=\"top_line\" stroke=\"" + colorStr + "\" stroke-width=\"" + strokeWidth + "\" x1=\"" + x1 + "\" x2=\"" + x2 + "\" y1=\"" + strokeWidth + "\" y2=\"" + (-spacing - strokeWidth) + "\"/>" +
                "</svg>";
        
        // Convert SVG string to buffered image
        BufferedImage resBI = new BufferedImage(spacing, spacing, BufferedImage.TYPE_INT_ARGB);
        Graphics2D resGraphics = resBI.createGraphics();
        resGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        resGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        InputStream stream = new ByteArrayInputStream(hatchFillSVGString.getBytes(StandardCharsets.UTF_8));
        SVGDocument svgDocument = new SVGLoader().load(stream);

        if (svgDocument != null) {
            ViewBox vb = new ViewBox(0, 0, spacing, spacing);
            svgDocument.render(null, resGraphics, vb);
        } else {
            ErrorLogger.LogMessage("PatternFillRendererD", "MakeHatchPatternFill", "null SVGDocument");
        }
        resGraphics.dispose();
        return resBI;
    }

    public static BufferedImage MakeMetocPatternFill(TGLight tg) {
        switch (tg.get_LineType()) {
            case TacticalLines.BEACH_SLOPE_MODERATE:
                if (patternBeachSlopeModerate == null)
                    patternBeachSlopeModerate = LoadBIFromDataURL(duriBeachSlopeModerate);
                return patternBeachSlopeModerate;
            case TacticalLines.BEACH_SLOPE_STEEP:
                if (patternBeachSlopeSteep == null)
                    patternBeachSlopeSteep = LoadBIFromDataURL(duriBeachSlopeSteep);
                return patternBeachSlopeSteep;
            case TacticalLines.FOUL_GROUND:
                if (patternFoulGround == null)
                    patternFoulGround = LoadBIFromDataURL(duriFoulGround);
                return patternFoulGround;
            case TacticalLines.KELP:
                if (patternKelp == null)
                    patternKelp = LoadBIFromDataURL(duriKelp);
                return patternKelp;
            case TacticalLines.OIL_RIG_FIELD:
                if (patternRigField == null)
                    patternRigField = LoadBIFromDataURL(duriRigField);
                return patternRigField;
            case TacticalLines.SWEPT_AREA:
                if (patternSweptArea == null)
                    patternSweptArea = LoadBIFromDataURL(duriSweptArea);
                return patternSweptArea;
            case TacticalLines.FISH_TRAPS:
                if (patternFishTraps == null)
                    patternFishTraps = LoadBIFromDataURL(duriFishTraps);
                return patternFishTraps;
            default:
                return null;
        }
    }

    private static BufferedImage LoadBIFromDataURL(String durl)
    {
        try {
            byte[] bytes = Base64.getDecoder().decode(durl);
            return ImageIO.read(new ByteArrayInputStream(bytes));
        }
        catch(IOException ioe)
        {
            ErrorLogger.LogException("PatterFillRenderer","LoadBMPFromDAtaURL", ioe);
        }
        return null;
    }
}
