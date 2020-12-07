package uber.location_service.algo;

import uber.location_service.structures.GeoPoint;
import uber.location_service.structures.SupplyInstance;

import java.util.*;
import java.util.function.Predicate;

public class GeoAlgorithms {
   private static final double MIN_LAT = Math.toRadians(-90d);  // -PI/2
   private static final double MAX_LAT = Math.toRadians(90d);   //  PI/2
   private static final double MIN_LON = Math.toRadians(-180d); // -PI
   private static final double MAX_LON = Math.toRadians(180d);  //  PI

   private static boolean validBounds(GeoPoint p) {
      if (p.getLatitude() < MIN_LAT || p.getLatitude() > MAX_LAT ||
         p.getLongitude() < MIN_LON || p.getLongitude() > MAX_LON)
         return false;
      return true;
   }

   private static GeoPoint[] boundingCoordinates(
         GeoPoint location, double distance, double radius) {

      if (radius < 0d || distance < 0d)
         throw new IllegalArgumentException();

      // angular distance in radians on a great circle
      double radDist = distance / radius;

      double minLat = location.getLatitude() - radDist;
      double maxLat = location.getLatitude() + radDist;

      double minLon, maxLon;
      if (minLat > MIN_LAT && maxLat < MAX_LAT) {
         double deltaLon = Math.asin(Math.sin(radDist) / Math.cos(location.getLatitude()));
         minLon = location.getLongitude() - deltaLon;
         if (minLon < MIN_LON) minLon += 2d * Math.PI;
         maxLon = location.getLongitude() + deltaLon;
         if (maxLon > MAX_LON) maxLon -= 2d * Math.PI;
      } else {
         // a pole is within the distance
         minLat = Math.max(minLat, MIN_LAT);
         maxLat = Math.min(maxLat, MAX_LAT);
         minLon = MIN_LON;
         maxLon = MAX_LON;
      }

      return new GeoPoint[]{
            GeoPoint.fromRadians(minLat, minLon),
            GeoPoint.fromRadians(maxLat, maxLon)
      };
   }


   public static List<SupplyInstance> getClosest(
         final Iterator<Map.Entry<String, SupplyInstance>> iterator, final double radius,
         final GeoPoint location) {
      double resDistance = Double.MAX_VALUE;
      SupplyInstance resInstance = null;

      while (iterator.hasNext()) {
         SupplyInstance ins = iterator.next().getValue();
         double dist = ins.getLocation().distanceTo(location, radius);
         if (dist < resDistance) {
            resInstance = ins;
            resDistance = dist;
         }
      }

      if (resInstance != null) return List.of(new SupplyInstance(resInstance));
      return new ArrayList<>();
   }

   public static List<SupplyInstance> findPlacesWithinDistance(
         final Iterator<Map.Entry<String, SupplyInstance>> iterator, final double radius,
         final GeoPoint location, final double distance) {

      final GeoPoint[] boundCoords = boundingCoordinates(location, distance, radius);
      final boolean meridian180WithinDistance =
            boundCoords[0].getLongitude() > boundCoords[1].getLongitude();

      final double[] pDistance = new double[1];
      Predicate isInsidePred = new Predicate<SupplyInstance>() {
         double b1 = boundCoords[0].getLatitude(), b2 = boundCoords[1].getLatitude();
         double b3 = boundCoords[0].getLongitude(), b4 = boundCoords[1].getLongitude();

         @Override
         public boolean test(SupplyInstance obj) {
            GeoPoint p = obj.getLocation();
            double Lat = p.getLatitude(), Lon = p.getLongitude();

            if (!(Lat >= b1 && Lat <= b2)) return false;

            boolean c1 = Lon >= b3, c2 = Lon <= b4;
            c1 = meridian180WithinDistance ? (c1 | c2) : (c1 & c2);
            if (!c1) return false;

            pDistance[0] = location.distanceTo(p, radius);
            return pDistance[0] <= distance;
         }
      };

      List<SupplyInstance> res = new ArrayList<>();
      while (iterator.hasNext()) {
         SupplyInstance entry = iterator.next().getValue();
         if (isInsidePred.test(entry)) {
            res.add(new SupplyInstance(entry));
         }
      }

      /*PreparedStatement statement = connection.prepareStatement(
            "SELECT * FROM Places WHERE (Lat >= ? AND Lat <= ?) AND (Lon >= ? " +
                  (meridian180WithinDistance ? "OR" : "AND") + " Lon <= ?) AND " +
                  "acos(sin(?) * sin(Lat) + cos(?) * cos(Lat) * cos(Lon - ?)) <= ?");
		statement.setDouble(1, boundingCoordinates[0].getLatitudeInRadians());
		statement.setDouble(2, boundingCoordinates[1].getLatitudeInRadians());
		statement.setDouble(3, boundingCoordinates[0].getLongitudeInRadians());
		statement.setDouble(4, boundingCoordinates[1].getLongitudeInRadians());
		statement.setDouble(5, location.getLatitudeInRadians());
		statement.setDouble(6, location.getLatitudeInRadians());
		statement.setDouble(7, location.getLongitudeInRadians());
		statement.setDouble(8, distance / radius);
		return statement.executeQuery();*/
      return res;
   }

}
