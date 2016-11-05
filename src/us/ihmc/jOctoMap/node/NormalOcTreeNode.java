package us.ihmc.jOctoMap.node;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

import org.apache.commons.math3.util.Precision;

import us.ihmc.jOctoMap.node.baseImplementation.AbstractOccupancyOcTreeNode;
import us.ihmc.jOctoMap.ocTree.NormalOcTree;

public class NormalOcTreeNode extends AbstractOccupancyOcTreeNode<NormalOcTreeNode>
{
   private float normalX = Float.NaN;
   private float normalY = Float.NaN;
   private float normalZ = Float.NaN;
   private float normalAverageDeviation = Float.NaN;
   private int normalConsensusSize = 0;
   private float hitLocationX = Float.NaN;
   private float hitLocationY = Float.NaN;
   private float hitLocationZ = Float.NaN;

   private long numberOfHits;

   public NormalOcTreeNode()
   {
   }

   @Override
   public void copyData(NormalOcTreeNode other)
   {
      super.copyData(other);
      normalX = other.normalX;
      normalY = other.normalY;
      normalZ = other.normalZ;
      normalAverageDeviation = other.normalAverageDeviation;
      normalConsensusSize = other.normalConsensusSize;
      hitLocationX = other.hitLocationX;
      hitLocationY = other.hitLocationY;
      hitLocationZ = other.hitLocationZ;

      if (NormalOcTree.UPDATE_NODE_HIT_WITH_AVERAGE)
         numberOfHits = other.numberOfHits;
   }

   @Override
   public void allocateChildren()
   {
      children = new NormalOcTreeNode[8];
   }

   @Override
   public void clear()
   {
      super.resetLogOdds();
      resetNormal();
      resetHitLocation();
   }

   public void resetNormal()
   {
      normalX = Float.NaN;
      normalY = Float.NaN;
      normalZ = Float.NaN;
      normalAverageDeviation = Float.NaN;
      normalConsensusSize = 0;
   }

   public void resetHitLocation()
   {
      hitLocationX = Float.NaN;
      hitLocationY = Float.NaN;
      hitLocationZ = Float.NaN;

      if (NormalOcTree.UPDATE_NODE_HIT_WITH_AVERAGE)
         numberOfHits = 0;
   }

   public void updateNormalChildren()
   {
      if (children == null)
      {
         resetNormal();
         return;
      }

      normalX = 0.0f;
      normalY = 0.0f;
      normalZ = 0.0f;
      int count = 0;

      for (int i = 0; i < 8; i++)
      {
         NormalOcTreeNode child = children[i];

         if (child != null && child.isNormalSet())
         {
            normalX += child.normalX;
            normalY += child.normalY;
            normalZ += child.normalZ;
            count++;
         }
      }

      if (count == 0)
      {
         resetNormal();
         return;
      }

      float invNorm = (float) (1.0 / Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ));
      normalX *= invNorm;
      normalY *= invNorm;
      normalZ *= invNorm;
   }

   public void getNormal(Vector3d normalToPack)
   {
      normalToPack.set(normalX, normalY, normalZ);
   }

   public void setNormal(Vector3d normal)
   {
      normalX = (float) normal.getX();
      normalY = (float) normal.getY();
      normalZ = (float) normal.getZ();
   }

   public void negateNormal()
   {
      normalX = - normalX;
      normalY = - normalY;
      normalZ = - normalZ;
   }

   public void setNormalQuality(float averageDeviation, int consensusSize)
   {
      normalAverageDeviation = averageDeviation;
      normalConsensusSize = consensusSize;
   }

   public float getNormalAverageDeviation()
   {
      return normalAverageDeviation;
   }

   public int getNormalConsensusSize()
   {
      return normalConsensusSize;
   }

   public boolean isNormalSet()
   {
      return !Float.isNaN(normalX) && !Float.isNaN(normalY) && !Float.isNaN(normalZ);
   }

   public boolean isHitLocationSet()
   {
      return !Float.isNaN(hitLocationX) && !Float.isNaN(hitLocationY) && !Float.isNaN(hitLocationY);
   }

   public void getHitLocation(Point3d hitLocationToPack)
   {
      hitLocationToPack.set(hitLocationX, hitLocationY, hitLocationZ);
   }

   public void updateHitLocation(Point3d centerUpdate, double alphaUpdate)
   {
      float xUpdate = (float) centerUpdate.getX();
      float yUpdate = (float) centerUpdate.getY();
      float zUpdate = (float) centerUpdate.getZ();
      updateHitLocation(xUpdate, yUpdate, zUpdate, (float) alphaUpdate);
   }

   public void updateHitLocation(Point3f centerUpdate, double alphaUpdate)
   {
      float xUpdate = centerUpdate.getX();
      float yUpdate = centerUpdate.getY();
      float zUpdate = centerUpdate.getZ();
      updateHitLocation(xUpdate, yUpdate, zUpdate, (float) alphaUpdate);
   }

   public void updateHitLocation(float xUpdate, float yUpdate, float zUpdate, float alphaUpdate)
   {
      if (NormalOcTree.UPDATE_NODE_HIT_WITH_AVERAGE)
      {
         if (numberOfHits == 0)
         {
            hitLocationX = 0.0f;
            hitLocationY = 0.0f;
            hitLocationZ = 0.0f;
         }

         numberOfHits++;
         float nInv = 1.0f / numberOfHits;
         hitLocationX += (xUpdate - hitLocationX) * nInv;
         hitLocationY += (yUpdate - hitLocationY) * nInv;
         hitLocationZ += (zUpdate - hitLocationZ) * nInv;
      }
      else
      {
         if (!isHitLocationSet())
         {
            hitLocationX = xUpdate;
            hitLocationY = yUpdate;
            hitLocationZ = zUpdate;
         }
         else
         {
            hitLocationX = alphaUpdate * xUpdate + (1.0f - alphaUpdate) * hitLocationX;
            hitLocationY = alphaUpdate * yUpdate + (1.0f - alphaUpdate) * hitLocationY;
            hitLocationZ = alphaUpdate * zUpdate + (1.0f - alphaUpdate) * hitLocationZ;
         }
      }
   }

   public void updateHitLocationChildren()
   {
      if (children == null)
      {
         resetHitLocation();
         return;
      }

      hitLocationX = 0.0f;
      hitLocationY = 0.0f;
      hitLocationZ = 0.0f;
      int count = 0;

      for (int i = 0; i < 8; i++)
      {
         NormalOcTreeNode child = children[i];

         if (child != null && child.isHitLocationSet())
         {
            hitLocationX += child.hitLocationX;
            hitLocationY += child.hitLocationY;
            hitLocationZ += child.hitLocationZ;
            count++;
         }
      }

      if (count == 0)
      {
         resetHitLocation();
         return;
      }
      double invCount = 1.0 / count;
      hitLocationX *= invCount;
      hitLocationY *= invCount;
      hitLocationZ *= invCount;
   }

   public double getHitLocationX()
   {
      return hitLocationX;
   }

   public double getHitLocationY()
   {
      return hitLocationY;
   }

   public double getHitLocationZ()
   {
      return hitLocationZ;
   }

   public double getNormalX()
   {
      return normalX;
   }

   public double getNormalY()
   {
      return normalY;
   }

   public double getNormalZ()
   {
      return normalZ;
   }

   @Override
   protected boolean epsilonEqualsInternal(NormalOcTreeNode other, double epsilon)
   {
      if (!Precision.equals(normalX, other.normalX, epsilon))
         return false;
      if (!Precision.equals(normalY, other.normalY, epsilon))
         return false;
      if (!Precision.equals(normalZ, other.normalZ, epsilon))
         return false;
      if (!Precision.equals(hitLocationX, other.hitLocationX, epsilon))
         return false;
      if (!Precision.equals(hitLocationY, other.hitLocationY, epsilon))
         return false;
      if (!Precision.equals(hitLocationZ, other.hitLocationZ, epsilon))
         return false;
      return super.epsilonEqualsInternal(other, epsilon);
   }
}