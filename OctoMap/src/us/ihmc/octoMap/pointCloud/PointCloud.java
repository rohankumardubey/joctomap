package us.ihmc.octoMap.pointCloud;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import us.ihmc.robotics.geometry.RigidBodyTransform;

public class PointCloud implements Iterable<Point3f>
{
   protected RigidBodyTransform current_inv_transform = new RigidBodyTransform();
   protected List<Point3f> points = new ArrayList<>();

   /**
    * A collection of 3D coordinates (point3d), which are regarded as endpoints of a
    * 3D laser scan.
    */
   public PointCloud()
   {
   }

   public PointCloud(PointCloud other)
   {
      addAll(other);
   }

   public PointCloud(Point3f[] points)
   {
      addAll(points);
   }

   public int size()
   {
      return points.size();
   }

   public void clear()
   {
      // delete the points
      if (!points.isEmpty())
      {
         points.clear();
      }
   }

   public void add(double x, double y, double z)
   {
      add((float) x, (float) y, (float) z);
   }

   public void add(float x, float y, float z)
   {
      points.add(new Point3f(x, y, z));
   }

   public void add(Point3d point)
   {
      points.add(new Point3f(point));
   }

   public void add(Point3f point)
   {
      points.add(point);
   }

   /// Add points from other Pointcloud
   public void addAll(PointCloud other)
   {
      for (Point3f point : other)
         add(new Point3f(point));
   }

   public void addAll(Point3f[] points)
   {
      for (Point3f point : points)
         add(point);
   }

   public void addAll(float[] points)
   {
      int numberOfPoints = points.length / 3;

      for (int i = 0; i < numberOfPoints; i++)
      {
         float x = points[3 * i];
         float y = points[3 * i + 1];
         float z = points[3 * i + 2];
         add(x, y, z);
      }
   }

   /// Apply transform to each point
   public void transform(RigidBodyTransform transform)
   {
      for (int i = 0; i < size(); i++)
         transform.transform(points.get(i));

      // FIXME: not correct for multiple transforms
      current_inv_transform.invert(transform);
   }

   /// Apply transform to each point, undo previous transforms
   public void transformAbsolute(RigidBodyTransform transform)
   {
      // undo previous transform, then apply current transform
      current_inv_transform.multiply(transform);

      for (int i = 0; i < size(); i++)
         current_inv_transform.transform(points.get(i));

      current_inv_transform.invert(transform);
   }

   /// Calculate bounding box of Pointcloud
   public void calculateBoundingBox(Point3d lowerBound, Point3d upperBound)
   {
      double min_x, min_y, min_z;
      double max_x, max_y, max_z;
      min_x = min_y = min_z = 1e6;
      max_x = max_y = max_z = -1e6;

      double x, y, z;

      for (Point3f point : this)
      {
         x = point.getX();
         y = point.getY();
         z = point.getZ();

         if (x < min_x)
            min_x = x;
         if (y < min_y)
            min_y = y;
         if (z < min_z)
            min_z = z;

         if (x > max_x)
            max_x = x;
         if (y > max_y)
            max_y = y;
         if (z > max_z)
            max_z = z;
      }

      lowerBound.setX(min_x);
      lowerBound.setY(min_y);
      lowerBound.setZ(min_z);
      upperBound.setX(max_x);
      upperBound.setY(max_y);
      upperBound.setZ(max_z);
   }

   /// Crop Pointcloud to given bounding box
   public void crop(Point3d lowerBound, Point3d upperBound)
   {
      PointCloud result = new PointCloud();

      float min_x, min_y, min_z;
      float max_x, max_y, max_z;
      float x, y, z;

      min_x = (float) lowerBound.getX();
      min_y = (float) lowerBound.getY();
      min_z = (float) lowerBound.getZ();
      max_x = (float) upperBound.getX();
      max_y = (float) upperBound.getY();
      max_z = (float) upperBound.getZ();

      for (Point3f point : this)
      {
         x = point.getX();
         y = point.getY();
         z = point.getZ();

         if ((x >= min_x) && (y >= min_y) && (z >= min_z) && (x <= max_x) && (y <= max_y) && (z <= max_z))
         {
            result.add(x, y, z);
         }
      } // end for points

      clear();
      addAll(result);
   }

   // removes any points closer than [thres] to (0,0,0)
   public void minDist(float thres)
   {
      PointCloud result = new PointCloud();

      float x, y, z;
      for (Point3f point : this)
      {
         x = point.getX();
         y = point.getY();
         z = point.getZ();
         float dist = (float) Math.sqrt(x * x + y * y + z * z);
         if (dist > thres)
            result.add(x, y, z);
      } // end for points
      clear();
      addAll(result);
   }

   public static PointCloud createPointCloudFromSubSample(float[] points, int numberOfSamples)
   {
      PointCloud pointCloud = new PointCloud();
      int numberOfPoints = points.length / 3;

      if (numberOfPoints <= numberOfSamples)
      {
         pointCloud.addAll(points);
      }
      else
      {
         Random random = new Random();
         HashSet<Integer> indices = new HashSet<>(numberOfSamples);

         while (indices.size() < numberOfSamples)
            indices.add(random.nextInt(numberOfPoints));

         for (int index : indices)
         {
            float x = points[3 * index];
            float y = points[3 * index + 1];
            float z = points[3 * index + 2];
            pointCloud.add(x, y, z);
         }
      }
      return pointCloud;
   }

   public Point3f getLast()
   {
      return points.get(points.size() - 1);
   }

   /// Returns a copy of the ith point in point cloud.
   /// Use operator[] for direct access to point reference.
   public Point3f getPoint(int i) // may return NULL
   {
      return points.get(i);
   }

   public Stream<Point3f> stream()
   {
      return points.stream();
   }

   public Stream<Point3f> parallelStream()
   {
      return points.parallelStream();
   }

   @Override
   public Iterator<Point3f> iterator()
   {
      return points.iterator();
   }
}
