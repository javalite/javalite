package org.javalite.templator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eric Nielsen
 */
abstract class ParentNode extends Node {
    final List<Node> children = new ArrayList<Node>();
}
