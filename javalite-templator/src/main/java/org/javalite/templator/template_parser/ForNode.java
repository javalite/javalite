package org.javalite.templator.template_parser;

import java.io.IOException;
import java.util.HashMap;
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
        int i = 0;
        String indexName = itemId + "_index";
        String hasNextName = itemId + "_has_next";
        Map newValues = new HashMap(values);
        while (it.hasNext()) {
            newValues.put(itemId, it.next());
            newValues.put(indexName, i++);
            newValues.put(hasNextName, it.hasNext());
            super.process(newValues, appendable);
        }
    }
}
