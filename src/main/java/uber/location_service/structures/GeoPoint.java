package uber.location_service.structures;

public class GeoPoint {
   private double latitude, longitude; // latitude, longitude in radians

   public GeoPoint() {

   }

   /**
    * @param latitude the latitude, in radians.
    * @param longitude the longitude, in radians.
    */
   public static GeoPoint fromRadians(double latitude, double longitude) {
      GeoPoint result = new GeoPoint();
      result.latitude = latitude;
      result.longitude = longitude;
      return result;
   }

   public double getLatitude() {
      return latitude;
   }

   public double getLongitude() {
      return longitude;
   }

   public void transformToRadians() {
      latitude = Math.toRadians(latitude);
      longitude = Math.toRadians(longitude);
   }

   public void transformToDegrees() {
      latitude = Math.toDegrees(latitude);
      longitude = Math.toDegrees(longitude);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      GeoPoint geoPoint = (GeoPoint) o;
      if (Double.compare(geoPoint.latitude, latitude) != 0) return false;
      if (Double.compare(geoPoint.longitude, longitude) != 0) return false;
      return true;
   }

   @Override
   public String toString() {
      return "[" + latitude + ", " + longitude + "]";
   }

   /**
    * Computes the great circle distance between this GeoLocation instance
    * and the location argument.
    * @param radius the radius of the sphere, for the Earth ~6371.01 kilometers.
    * @return the distance, measured in the same unit as the radius
    * argument.
    */
   public double distanceTo(GeoPoint location, double radius) {
      return Math.acos(Math.sin(latitude) * Math.sin(location.latitude) +
            Math.cos(latitude) * Math.cos(location.latitude) *
                  Math.cos(longitude - location.longitude)) * radius;
   }
}