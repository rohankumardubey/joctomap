package us.ihmc.octoMap.tools;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static us.ihmc.octoMap.tools.OcTreeCoordinateConversionTools.convertCartesianCoordinateToKey;
import static us.ihmc.octoMap.tools.OcTreeCoordinateConversionTools.convertKeyToCartesianCoordinate;

import java.util.Random;

import javax.vecmath.Point3d;

import org.junit.Test;

import us.ihmc.octoMap.key.OcTreeKey;
import us.ihmc.robotics.random.RandomTools;

public class OcTreeCoordinateConversionToolsTest
{
   @Test
   public void testConvertKeyCoordBackForthMaxDepth() throws Exception
   {
      Random random = new Random(6574961L);
      int maxDepth = 16;
      double resolution = 0.05;

      int keyMax = OcTreeKeyTools.computeMaximumKeyValueAtDepth(maxDepth);

      for (int i = 0; i < 10000; i++)
      {
         int expectedKey = RandomTools.generateRandomInt(random, 0, keyMax);
         double coord = convertKeyToCartesianCoordinate(expectedKey, resolution, maxDepth);
         double actualKey = convertCartesianCoordinateToKey(coord, resolution, maxDepth);
         assertTrue(expectedKey == actualKey);
      }

      for (int i = 0; i < 10000; i++)
      {
         OcTreeKey expectedKey = new OcTreeKey(random, keyMax);
         Point3d coord = convertKeyToCartesianCoordinate(expectedKey, resolution, maxDepth);
         OcTreeKey actualKey = convertCartesianCoordinateToKey(coord, resolution, maxDepth);
         assertTrue(expectedKey.equals(actualKey));
      }
   }

   @Test
   public void test()
   {
      fail("Not yet implemented");
   }

}
