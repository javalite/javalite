package org.javalite.templator;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Eric Nielsen
 */
class ForNode extends RootNode {
    static final String TAG_NAME = "for";
    private final String itemId;
    private final ChainedIds iterableIds;

    ForNode(String itemId, ChainedIds iterableIds) {
        this.itemId = itemId;
        this.iterableIds = iterableIds;
    }

    @Override
    public String name() {
        return TAG_NAME;
    }
    @Override
    public void process(Map values, Appendable appendable) throws IOException {
        Iterator it = ((Iterable) iterableIds.valueFrom(values)).iterator();
        while (it.hasNext()) {
            values.put(itemId, it.next());
            super.process(values, appendable);
        }
        values.remove(itemId);
    }
}
