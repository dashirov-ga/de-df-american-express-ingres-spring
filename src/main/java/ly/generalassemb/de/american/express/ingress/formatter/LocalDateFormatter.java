package ly.generalassemb.de.american.express.ingress.formatter;

import com.ancientprogramming.fixedformat4j.exception.FixedFormatException;
import com.ancientprogramming.fixedformat4j.format.FixedFormatter;
import com.ancientprogramming.fixedformat4j.format.FormatInstructions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by davidashirov on 12/1/17.
 */
public class LocalDateFormatter implements FixedFormatter{
        @Override
        public Object parse(String s, FormatInstructions formatInstructions) throws FixedFormatException {
            DateTimeFormatter format = DateTimeFormatter.ofPattern(formatInstructions.getFixedFormatPatternData().getPattern());
            if ( (s!=null) && (s.length()>0)) {
                return LocalDate.parse(s,format);
            }
            return null;
        }
        @Override
        public String format(Object o, FormatInstructions formatInstructions) throws FixedFormatException {
            DateTimeFormatter format = DateTimeFormatter.ofPattern(formatInstructions.getFixedFormatPatternData().getPattern());
            if ((o!=null)){
                try {
                    return ((LocalDate) o).format(format);
                } catch (Exception e){
                    throw  new FixedFormatException("Error formatting object", e);
                }
            }
            return null;
        }
}