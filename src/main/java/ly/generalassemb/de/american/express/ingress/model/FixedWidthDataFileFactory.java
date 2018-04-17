package ly.generalassemb.de.american.express.ingress.model;

import ly.generalassemb.de.american.express.ingress.model.file.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FixedWidthDataFileFactory {
    private static final Log LOGGER = LogFactory.getLog(FixedWidthDataFileFactory.class);


    private static final Pattern fileNamePattern = Pattern.compile("^(?<account>[A-Za-z0-9]+)[.](?<type>(?:EPAPE|EPTRN|CBNOT|EMINQ|EMCBK))[#-](?<fileId>[A-Za-z0-9]+).*$");

    public static FixedWidthDataFile parse(File file) throws NotRegisteredException {
        return FixedWidthDataFileFactory.parse(getTypeforName(file.getName()), file);
    }


    public static FixedWidthDataFile parse(Path file) throws NotRegisteredException {
        return FixedWidthDataFileFactory.parse(getTypeforName(file.getFileName().toString()), file.toFile());
    }
    public static FixedWidthDataFileType getTypeforName(String name) throws NotRegisteredException {
        Matcher m = fileNamePattern.matcher(name);
        if (m.matches())
            return FixedWidthDataFileType.valueOf(m.group("type"));
        else
            throw new NotRegisteredException(String.format("Can't figure out how to parse '%s' file. The following types are registered: %s",
                    name,
                    Arrays.stream(FixedWidthDataFileType.values()).map(Enum::name).collect(Collectors.joining(", "))));
    }
    public static FixedWidthDataFile parse(FixedWidthDataFileType type, File file) {
        try {
            FixedWidthDataFile out;
            if (type == FixedWidthDataFileType.EPAPE) {
                out = new EPAPEFixedWidthDataFile(file);;
            } else if (type == FixedWidthDataFileType.EPTRN) {
                out = new EPTRNFixedWidthDataFile(file);
            } else if (type == FixedWidthDataFileType.CBNOT) {
                out = new CBNOTFixedWidthDataFile(file);
            } else if (type == FixedWidthDataFileType.EMCBK) {
                out = new EMCBKFixedWidthDataFile(file);
            } else if (type == FixedWidthDataFileType.EMINQ) {
                out = new EMINQFixedWidthDataFile(file);
            } else {
                return null;
            }
            out.setInputFile(file);
            return out;
        } catch (Exception e) {
            LOGGER.error("Factory method failed.");
            LOGGER.error(e);
            return null;
        }
    }
}
