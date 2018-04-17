package ly.generalassemb.de.american.express.ingress.model.file.ComponentSerializer;

import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFile;
import ly.generalassemb.de.american.express.ingress.model.NotRegisteredException;
import ly.generalassemb.de.american.express.ingress.model.file.*;

public class FixedWidthDataFileComponentSerializerFactory {
    public static FixedWidthDataFileComponentSerializer get(FixedWidthDataFile o) throws NotRegisteredException{
        if (o instanceof CBNOTFixedWidthDataFile)
            return new CBNOTFixedWidthDataFileComponentSerializer();
        if (o instanceof EMCBKFixedWidthDataFile)
            return new EMCBKFixedWidthDataFileComponentSerializer();
        if (o instanceof EMINQFixedWidthDataFile)
            return new EMINQFixedWidthDataFileComponentSerializer();
        if (o instanceof EPTRNFixedWidthDataFile)
            return new EPTRNFixedWidthDataFileComponentSerializer();
        if (o instanceof EPAPEFixedWidthDataFile)
            return new EPAPEFixedWidthDataFileComponentSerializer();
        throw new NotRegisteredException(o.getClass() + " : can't determine an appropriate parser to handle this class.");
    }
}
