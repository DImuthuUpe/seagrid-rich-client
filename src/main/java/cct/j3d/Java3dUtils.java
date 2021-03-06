package cct.j3d;

import org.scijava.vecmath.AxisAngle4d;
import org.scijava.vecmath.Matrix3d;

/**
 * <p>Title: Jamberoo - Computational Chemistry Toolkit</p>
 *
 * <p>Description: Collection of Computational Chemistry related code</p>
 *
 * <p>Copyright: Copyright (c) 2005-2010 Dr. Vladislav Vasilyev</p>
 *
 * <p>Company: The Australian National University</p>
 *
 * @author Dr. Vladislav Vasilyev
 * @version 1.0
 */
public class Java3dUtils {
  private Java3dUtils() {
  }

  static public void toAxisAngle(Matrix3d m, AxisAngle4d axisAngle4d) {
    double angle, x, y, z; // variables for result
    double epsilon = 0.01; // margin to allow for rounding errors
    double epsilon2 = 0.1; // margin to distinguish between 0 and 180 degrees
    // optional check that input is pure rotation, 'isRotationMatrix' is defined at:
    // http://www.euclideanspace.com/maths/algebra/matrix/orthogonal/rotation/

    //assert isRotationMatrix(m):"not valid rotation matrix"; // for debugging

    //if ( (Math.abs(m[0][1] - m[1][0]) < epsilon)
    //    && (Math.abs(m[0][2] - m[2][0]) < epsilon)
    //    && (Math.abs(m[1][2] - m[2][1]) < epsilon)) {

    if ( (Math.abs(m.m01 - m.m10) < epsilon) && (Math.abs(m.m02 - m.m20) < epsilon) && (Math.abs(m.m12 - m.m21) < epsilon)) {

      // singularity found
      // first check for identity matrix which must have +1 for all terms
      //  in leading diagonaland zero in other terms

      //if ( (Math.abs(m[0][1] + m[1][0]) < epsilon2)
      //    && (Math.abs(m[0][2] + m[2][0]) < epsilon2)
      //    && (Math.abs(m[1][2] + m[2][1]) < epsilon2)
      //    && (Math.abs(m[0][0] + m[1][1] + m[2][2] - 3) < epsilon2)) {

      if ( (Math.abs(m.m01 + m.m10) < epsilon2) && (Math.abs(m.m02 + m.m20) < epsilon2) && (Math.abs(m.m12 + m.m21) < epsilon2) &&
          (Math.abs(m.m00 + m.m11 + m.m22 - 3) < epsilon2)) {

        // this singularity is identity matrix so angle = 0
        //return new axisAngle(0, 1, 0, 0); // zero angle, arbitrary axis
        axisAngle4d.set(1, 0, 0, 0);
        return;
      }

      // otherwise this singularity is angle = 180
      angle = Math.PI;

      //double xx = (m[0][0] + 1) / 2;
      //double yy = (m[1][1] + 1) / 2;
      //double zz = (m[2][2] + 1) / 2;
      //double xy = (m[0][1] + m[1][0]) / 4;
      //double xz = (m[0][2] + m[2][0]) / 4;
      //double yz = (m[1][2] + m[2][1]) / 4;

      double xx = (m.m00 + 1) / 2;
      double yy = (m.m11 + 1) / 2;
      double zz = (m.m22 + 1) / 2;
      double xy = (m.m01 + m.m10) / 4;
      double xz = (m.m02 + m.m20) / 4;
      double yz = (m.m12 + m.m21) / 4;

      if ( (xx > yy) && (xx > zz)) { // m[0][0] is the largest diagonal term
        if (xx < epsilon) {
          x = 0;
          y = 0.7071;
          z = 0.7071;
        }
        else {
          x = Math.sqrt(xx);
          y = xy / x;
          z = xz / x;
        }
      }
      else if (yy > zz) { // m[1][1] is the largest diagonal term
        if (yy < epsilon) {
          x = 0.7071;
          y = 0;
          z = 0.7071;
        }
        else {
          y = Math.sqrt(yy);
          x = xy / y;
          z = yz / y;
        }
      }
      else { // m[2][2] is the largest diagonal term so base result on this
        if (zz < epsilon) {
          x = 0.7071;
          y = 0.7071;
          z = 0;
        }
        else {
          z = Math.sqrt(zz);
          x = xz / z;
          y = yz / z;
        }
      }
      //return new axisAngle(angle, x, y, z); // return 180 deg rotation
      axisAngle4d.set(x, y, z, angle);
      return;
    }

    // as we have reached here there are no singularities so we can handle normally
    //double s = Math.sqrt( (m[2][1] - m[1][2]) * (m[2][1] - m[1][2])
    //                     + (m[0][2] - m[2][0]) * (m[0][2] - m[2][0])
    //                     + (m[1][0] - m[0][1]) * (m[1][0] - m[0][1])); // used to normalise
    double s = Math.sqrt( (m.m21 - m.m12) * (m.m21 - m.m12) + (m.m02 - m.m20) * (m.m02 - m.m20) + (m.m10 - m.m01) * (m.m10 - m.m01)); // used to normalise

    if (Math.abs(s) < 0.001) {
      s = 1;
    }
    // prevent divide by zero, should not happen if matrix is orthogonal and should be
    // caught by singularity test above, but I've left it in just in case
    //angle = Math.acos( (m[0][0] + m[1][1] + m[2][2] - 1) / 2);
    //x = (m[2][1] - m[1][2]) / s;
    //y = (m[0][2] - m[2][0]) / s;
    //z = (m[1][0] - m[0][1]) / s;

    angle = Math.acos( (m.m00 + m.m11 + m.m22 - 1) / 2);
    x = (m.m21 - m.m12) / s;
    y = (m.m02 - m.m20) / s;
    z = (m.m10 - m.m01) / s;

    //return new axisAngle(angle, x, y, z);
    axisAngle4d.set(x, y, z, angle);
  }

}
