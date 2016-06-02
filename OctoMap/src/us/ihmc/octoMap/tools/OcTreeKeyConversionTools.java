package us.ihmc.octoMap.tools;

import static us.ihmc.octoMap.tools.OcTreeKeyTools.adjustKeyAtDepth;
import static us.ihmc.octoMap.tools.OcTreeKeyTools.computeCenterOffsetKey;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import us.ihmc.octoMap.key.OcTreeKey;
import us.ihmc.robotics.MathTools;

public abstract class OcTreeKeyConversionTools
{
   /**
    * Converts a single coordinate into a discrete addressing key, with boundary checking.
    *
    * @param coordinate 3d coordinate of a point
    * @param key discrete 16 bit addressing key, result
    * @return key if coordinate is within the octree bounds (valid), -1 otherwise
    */
   public static int coordinateToKey(double coordinate, double resolution, int maxDepth)
   {
      return coordinateToKey(coordinate, maxDepth, resolution, maxDepth);
   }

   /**
    * Converts a single coordinate into a discrete addressing key, with boundary checking.
    *
    * @param coordinate 3d coordinate of a point
    * @param depth level of the key from the top
    * @param key discrete 16 bit addressing key, result
    * @return key if coordinate is within the octree bounds (valid), -1 otherwise
    */
   public static int coordinateToKey(double coordinate, int depth, double resolution, int maxDepth)
   {
      MathTools.checkIfLessOrEqual(depth, maxDepth);

      int centerOffsetKey = computeCenterOffsetKey(maxDepth);
      // scale to resolution and shift center
      int scaledCoord = (int) Math.floor(coordinate / resolution) + centerOffsetKey;
      if (scaledCoord >= 0 && scaledCoord < 2 * centerOffsetKey)
         return adjustKeyAtDepth(scaledCoord, depth, maxDepth);
      else
         return -1;
   }

   /**
    * Converts a 3D coordinate into a 3D OcTreeKey, with boundary checking.
    *
    * @param coord 3d coordinate of a point
    * @return key if point is within the octree (valid), null otherwise
    */
   public static OcTreeKey coordinateToKey(Point3f coord, double resolution, int maxDepth)
   {
      return convertCartesianCoordinateToKey(coord.x, coord.y, coord.z, maxDepth, resolution, maxDepth);
   }
   
   /**
    * Converts a 3D coordinate into a 3D OcTreeKey, with boundary checking.
    *
    * @param coord 3d coordinate of a point
    * @return key if point is within the octree (valid), null otherwise
    */
   public static OcTreeKey coordinateToKey(Point3d coord, double resolution, int maxDepth)
   {
      return convertCartesianCoordinateToKey(coord.x, coord.y, coord.z, maxDepth, resolution, maxDepth);
   }

   /**
    * Converts a 3D coordinate into a 3D OcTreeKey, with boundary checking.
    *
    * @param coord 3d coordinate of a point
    * @param depth level of the key from the top
    * @return key if point is within the octree (valid), null otherwise
    */
   public static OcTreeKey coordinateToKey(Point3d coord, int depth, double resolution, int maxDepth)
   {
      return convertCartesianCoordinateToKey(coord.x, coord.y, coord.z, depth, resolution, maxDepth);
   }

   /**
    * Converts a 3D coordinate into a 3D OcTreeKey, with boundary checking.
    *
    * @param coord 3d coordinate of a point
    * @param depth level of the key from the top
    * @param keyToPack
    * @return true if point is within the octree (valid), false otherwise
    */
   public static boolean coordinateToKey(Point3d coord, double resolution, int maxDepth, OcTreeKey keyToPack)
   {
      OcTreeKey key = coordinateToKey(coord, maxDepth, resolution, maxDepth);
      if (key == null)
         return false;

      keyToPack.set(key);
      return true;
   }

   /**
    * Converts a 3D coordinate into a 3D OcTreeKey, with boundary checking.
    *
    * @param x 3d coordinate of a point
    * @param y 3d coordinate of a point
    * @param z 3d coordinate of a point
    * @return key if point is within the octree (valid), null otherwise
    */
   public static OcTreeKey coordinateToKey(double x, double y, double z, double resolution, int maxDepth)
   {
      return convertCartesianCoordinateToKey(x, y, z, maxDepth, resolution, maxDepth);
   }

   /**
    * Converts a 3D coordinate into a 3D OcTreeKey, with boundary checking.
    *
    * @param x 3d coordinate of a point
    * @param y 3d coordinate of a point
    * @param z 3d coordinate of a point
    * @param depth level of the key from the top
    * @return key if point is within the octree (valid), null otherwise
    */
   public static OcTreeKey convertCartesianCoordinateToKey(double x, double y, double z, int depth, double resolution, int maxDepth)
   {
      int k0 = coordinateToKey(x, depth, resolution, maxDepth);
      if (k0 == -1)
         return null;
      int k1 = coordinateToKey(y, depth, resolution, maxDepth);
      if (k1 == -1)
         return null;
      int k2 = coordinateToKey(z, depth, resolution, maxDepth);
      if (k2 == -1)
         return null;

      return new OcTreeKey(k0, k1, k2);
   }

   /** converts from a discrete key at a given depth into a coordinate corresponding to the key's center */
   public static double keyToCoordinate(int key, int depth, double resolution, int maxDepth)
   {
      MathTools.checkIfLessOrEqual(depth, maxDepth);

      // root is centered on 0 = 0.0
      if (depth == 0)
      {
         return 0.0;
      }
      else if (depth == maxDepth)
      {
         return keyToCoordinate(key, resolution, maxDepth);
      }
      else
      {
         double nodeSize = computeNodeSize(depth, resolution, maxDepth);
         int centerOffsetKey = computeCenterOffsetKey(maxDepth);
         int keyDivider = 1 << maxDepth - depth;
         return (Math.floor((double) (key - centerOffsetKey) / (double) keyDivider) + 0.5) * nodeSize;
//         return keyToCoordinate(adjustKeyAtDepth(key, depth, maxDepth), nodeSize, depth);
      }
   }

   /** converts from a discrete key at the lowest tree level into a coordinate corresponding to the key's center */
   public static double keyToCoordinate(int key, double resolution, int maxDepth)
   {
      int centerOffsetKey = computeCenterOffsetKey(maxDepth);
      return (key - centerOffsetKey + 0.5) * resolution;
   }

   /** converts from an addressing key at the lowest tree level into a coordinate corresponding to the key's center */
   public static Point3d keyToCoordinate(OcTreeKey key, double resolution, int maxDepth)
   {
      return keyToCoordinate(key, maxDepth, resolution, maxDepth);
   }

   /** converts from an addressing key at a given depth into a coordinate corresponding to the key's center */
   public static Point3d keyToCoordinate(OcTreeKey key, int depth, double resolution, int maxDepth)
   {
      double x = keyToCoordinate(key.k[0], depth, resolution, maxDepth);
      double y = keyToCoordinate(key.k[1], depth, resolution, maxDepth);
      double z = keyToCoordinate(key.k[2], depth, resolution, maxDepth);
      return new Point3d(x, y, z);
   }

   public static void keyToCoordinate(OcTreeKey key, Point3d coordinateToPack, double resolution, int maxDepth)
   {
      keyToCoordinate(key, maxDepth, coordinateToPack, resolution, maxDepth);
   }

   /** converts from an addressing key at a given depth into a coordinate corresponding to the key's center */
   public static void keyToCoordinate(OcTreeKey key, int depth, Point3d coordinateToPack, double resolution, int maxDepth)
   {
      coordinateToPack.x = keyToCoordinate(key.k[0], depth, resolution, maxDepth);
      coordinateToPack.y = keyToCoordinate(key.k[1], depth, resolution, maxDepth);
      coordinateToPack.z = keyToCoordinate(key.k[2], depth, resolution, maxDepth);
   }

   public static double computeNodeSize(int depth, double resolution, int maxDepth)
   {
      MathTools.checkIfLessOrEqual(depth, maxDepth);
      return (1 << maxDepth - depth) * resolution;
   }

   public static void main(String[] args)
   {
      int key = 11515;
      int maxDepth = 16;
      int depth = maxDepth;
      double resolution = 0.05;

      double nodeSize = computeNodeSize(depth, resolution, maxDepth);
      int centerOffsetKey = computeCenterOffsetKey(maxDepth);
      double coordBad = (Math.floor((double) (key - centerOffsetKey) / (double) (1 << maxDepth - depth)) + 0.5) * nodeSize;
      double coordGood = keyToCoordinate(key, resolution, maxDepth);
      System.out.println("coordBad = " + coordBad);
      System.out.println("coordGood = " + coordGood);
   }
}
