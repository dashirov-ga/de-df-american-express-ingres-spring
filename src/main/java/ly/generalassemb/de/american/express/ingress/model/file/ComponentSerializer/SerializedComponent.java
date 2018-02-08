package ly.generalassemb.de.american.express.ingress.model.file.ComponentSerializer;

import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileComponent;
import ly.generalassemb.de.american.express.ingress.model.file.FixedWidthDataFileType;

public class SerializedComponent<T> {
    private FixedWidthDataFileComponent type;
    private T payload;
    private FixedWidthDataFileType parentKind;
    private String parentId;

    public FixedWidthDataFileType getParentKind() {
        return parentKind;
    }

    public void setParentKind(FixedWidthDataFileType parentKind) {
        this.parentKind = parentKind;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public FixedWidthDataFileComponent getType() {
        return type;
    }

    public void setType(FixedWidthDataFileComponent type) {
        this.type = type;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

}
