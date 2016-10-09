package us.ihmc.octoMap.rules.interfaces;

import us.ihmc.octoMap.node.AbstractOcTreeNode;

public interface IteratorSelectionRule<NODE extends AbstractOcTreeNode<NODE>>
{
   public boolean test(NODE superNode, int maxDepth);
}
