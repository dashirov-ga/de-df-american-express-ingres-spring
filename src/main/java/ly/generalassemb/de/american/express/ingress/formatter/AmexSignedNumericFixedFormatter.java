package ly.generalassemb.de.american.express.ingress.formatter;

import com.ancientprogramming.fixedformat4j.exception.FixedFormatException;
import com.ancientprogramming.fixedformat4j.format.FixedFormatter;
import com.ancientprogramming.fixedformat4j.format.FormatInstructions;
import ly.generalassemb.de.american.express.ingress.util.CobolUtils;

import java.math.BigDecimal;

/**
 * Created by davidashirov on 12/1/17.
 */
public class AmexSignedNumericFixedFormatter implements FixedFormatter{
        @Override
        public Object parse(String s, FormatInstructions formatInstructions) throws FixedFormatException {
            if ( (s!=null) && (s.length()>0)) {
                BigDecimal r = new BigDecimal(CobolUtils.fromCobolSignedString(s, 0));
                return r.movePointLeft(formatInstructions.getFixedFormatDecimalData().getDecimals());
            }
            return null;
        }
        @Override
        public String format(Object o, FormatInstructions formatInstructions) throws FixedFormatException {
            if ((o!=null)){
                try {
                    return CobolUtils.padNum(o.toString(),formatInstructions.getLength(),0,true);
                } catch (Exception e){
                    throw  new FixedFormatException("Error formatting object", e);
                }
            }
            return null;
        }
}