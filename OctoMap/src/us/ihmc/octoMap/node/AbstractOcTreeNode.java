package us.ihmc.octoMap.node;

import static us.ihmc.octoMap.node.OcTreeNodeTools.*;

import java.lang.reflect.Array;
import java.util.Arrays;

import javax.vecmath.Point3d;

import us.ihmc.octoMap.key.OcTreeKey;
import us.ihmc.octoMap.key.OcTreeKeyReadOnly;
import us.ihmc.octoMap.tools.OcTreeKeyConversionTools;

public abstract class AbstractOcTreeNode<N extends AbstractOcTreeNode<N>>
{
   private static boolean DEBUG_PROPERTIES = false;

   protected N[] children;
   private int k0 = -1, k1 = -1, k2 = -1;
   private float x = Float.NaN, y = Float.NaN, z = Float.NaN;
   private float size = Float.NaN;

   public AbstractOcTreeNode()
   {
   }

   public abstract void copyData(N other);

   public abstract void updateOccupancyChildren();

   protected abstract void clear();

   final void clearProperties()
   {
      k0 = -1;
      k1 = -1;
      k2 = -1;
      x = Float.NaN;
      y = Float.NaN;
      z = Float.NaN;
      size = Float.NaN;
   }

   public final void setProperties(OcTreeKeyReadOnly key, int depth, double resolution, int treeDepth)
   {
      setProperties(key.getKey(0), key.getKey(1), key.getKey(2), depth, resolution, treeDepth);
   }

   public final void setProperties(int k0, int k1, int k2, int depth, double resolution, int treeDepth)
   {
      this.k0 = k0;
      this.k1 = k1;
      this.k2 = k2;
      this.x = (float) OcTreeKeyConversionTools.keyToCoordinate(k0, depth, resolution, treeDepth);
      this.y = (float) OcTreeKeyConversionTools.keyToCoordinate(k1, depth, resolution, treeDepth);
      this.z = (float) OcTreeKeyConversionTools.keyToCoordinate(k2, depth, resolution, treeDepth);
      this.size = (float) OcTreeKeyConversionTools.computeNodeSize(depth, resolution, treeDepth);
   }

   public final void getKey(OcTreeKey keyToPack)
   {
      keyToPack.set(k0, k1, k2);
   }

   public final void getCoordinate(Point3d coordinateToPack)
   {
      coordinateToPack.set(x, y, z);
   }

   @SuppressWarnings("unchecked")
   public void allocateChildren()
   {
      children = (N[]) Array.newInstance(getClass(), 8);
   }

   public void assignChildren(N[] newChildren)
   {
      children = newChildren;
   }

   @SuppressWarnings("unchecked")
   public final N cloneRecursive(NodeBuilder<N> nodeBuilder)
   {
      N ret = nodeBuilder.createNode();
      ret.copyData((N) this);

      if (!hasAtLeastOneChild())
         allocateChildren();

      for (int i = 0; i < 8; i++)
         ret.children[i] = children[i].cloneRecursive(nodeBuilder);

      return ret;
   }

   public final boolean hasArrayForChildren()
   {
      return children != null;
   }

   public final boolean hasAtLeastOneChild()
   {
      if (children == null)
         return false;

      for (int i = 0; i < 8; i++)
      {
         if (children[i] != null)
            return true;
      }
      return false;
   }

   public final void setChild(int childIndex, N newChild)
   {
      checkChildIndex(childIndex);
      if (!getClass().isInstance(newChild))
         throw new RuntimeException("Cannot add a child of a different type");
      children[childIndex] = newChild;
   }

   public final N getChild(int childIndex)
   {
      checkChildIndex(childIndex);
      checkNodeHasChildren(this);

      return children == null ? null : children[childIndex];
   }

   public final N removeChild(int childIndex)
   {
      OcTreeNodeTools.checkChildIndex(childIndex);

      N removedChild = children[childIndex];
      if (removedChild != null)
      {
         removedChild.clear();
         removedChild.clearProperties();
      }
      children[childIndex] = null;
      return removedChild;
   }

   public final N[] removeChildren()
   {
      N[] removedChildren = children;
      children = null;
      return removedChildren;
   }

   public abstract boolean epsilonEquals(N other);

   public final int getKey0()
   {
      if (DEBUG_PROPERTIES)
         if (k0 == -1)
            throw new RuntimeException("Key has not been set");
      return k0;
   }

   public final int getKey1()
   {
      if (DEBUG_PROPERTIES)
         if (k1 == -1)
            throw new RuntimeException("Key has not been set");
      return k1;
   }

   public final int getKey2()
   {
      if (DEBUG_PROPERTIES)
         if (k2 == -1)
            throw new RuntimeException("Key has not been set");
      return k2;
   }

   public final double getX()
   {
      if (DEBUG_PROPERTIES)
         if (Float.isNaN(x))
            throw new RuntimeException("Coordinate has not been set");
      return x;
   }

   public final double getY()
   {
      if (DEBUG_PROPERTIES)
         if (Float.isNaN(y))
            throw new RuntimeException("Coordinate has not been set");
      return y;
   }

   public final double getZ()
   {
      if (DEBUG_PROPERTIES)
         if (Float.isNaN(z))
            throw new RuntimeException("Coordinate has not been set");
      return z;
   }

   public final double getSize()
   {
      if (DEBUG_PROPERTIES)
         if (Float.isNaN(size))
            throw new RuntimeException("Size has not been set");
      return size;
   }

   @Override
   public String toString()
   {
      return getClass().getSimpleName() + ": children = " + Arrays.toString(getChildrenSimpleNames());
   }

   protected String[] getChildrenSimpleNames()
   {
      String[] childrenNames = new String[8];
      if (children != null)
      {
         for (int i = 0; i < 8; i++)
         {
            N child = children[i];
            childrenNames[i] = child == null ? null : child.getClass().getSimpleName();
         }
      }
      return childrenNames;
   }
}
