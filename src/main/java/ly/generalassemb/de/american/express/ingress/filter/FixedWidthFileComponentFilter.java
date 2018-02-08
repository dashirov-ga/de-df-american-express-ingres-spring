package ly.generalassemb.de.american.express.ingress.filter;

import ly.generalassemb.de.american.express.ingress.model.FixedWidthDataFileComponent;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FixedWidthFileComponentFilter<T> {
     private Set<FixedWidthDataFileComponent> include;

    public Set<FixedWidthDataFileComponent> getInclude() {
        return include;
    }

    public void setInclude(Set<FixedWidthDataFileComponent> include) {
        this.include = include;
    }

    public List<Map.Entry<FixedWidthDataFileComponent,T>> filter(List<Map.Entry<FixedWidthDataFileComponent,T>> input) {
        if (include!=null && !include.isEmpty())
           input.removeIf(n -> !include.contains(n.getKey()));
        return input;
    }
}
