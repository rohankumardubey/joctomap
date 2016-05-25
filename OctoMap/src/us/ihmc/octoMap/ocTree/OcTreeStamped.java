package us.ihmc.octoMap.ocTree;

import us.ihmc.octoMap.iterators.OcTreeSuperNode;
import us.ihmc.octoMap.node.OcTreeNodeStamped;

public class OcTreeStamped extends OccupancyOcTreeBase<OcTreeNodeStamped>
{

   public OcTreeStamped(double resolution)
   {
      super(resolution);
   }

   /** @return timestamp of last update */
   public long getLastUpdateTime()
   {
      // this value is updated whenever inner nodes are 
      // updated using updateOccupancyChildren()
      return root.getTimestamp();
   }

   public void degradeOutdatedNodes(long timeThreshold)
   {
      long query_time = System.currentTimeMillis();

      for (OcTreeSuperNode<OcTreeNodeStamped> superNode : this)
      {
         OcTreeNodeStamped node = superNode.getNode();
         if (isNodeOccupied(node) && query_time - node.getTimestamp() > timeThreshold)
         {
            integrateMissNoTime(node);
         }
      }
   }

   @Override
   public void updateNodeLogOdds(OcTreeNodeStamped node, float update)
   {
      updateNodeLogOdds(node, update);
      node.updateTimestamp();
   }

   public void integrateMissNoTime(OcTreeNodeStamped node)
   {
      updateNodeLogOdds(node, prob_miss_log);
   }

   @Override
   protected OcTreeNodeStamped createRootNode()
   {
      return new OcTreeNodeStamped();
   }
}
