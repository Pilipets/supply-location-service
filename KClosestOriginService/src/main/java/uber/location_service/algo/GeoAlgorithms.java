package uber.location_service.algo;

import org.javatuples.Pair;
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
      if (p.getLatRadians() < MIN_LAT || p.getLatRadians() > MAX_LAT ||
         p.getLonRadians() < MIN_LON || p.getLonRadians() > MAX_LON)
         return false;
      return true;
   }

   private static GeoPoint[] boundingCoordinates(
         GeoPoint location, double distance, double radius) {

      if (radius < 0d || distance < 0d)
         throw new IllegalArgumentException();

      // angular distance in radians on a great circle
      double radDist = distance / radius;

      double minLat = location.getLatRadians() - radDist;
      double maxLat = location.getLatRadians() + radDist;

      double minLon, maxLon;
      if (minLat > MIN_LAT && maxLat < MAX_LAT) {
         double deltaLon = Math.asin(Math.sin(radDist) / Math.cos(location.getLatRadians()));
         minLon = location.getLonRadians() - deltaLon;
         if (minLon < MIN_LON) minLon += 2d * Math.PI;
         maxLon = location.getLonRadians() + deltaLon;
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


   public static List<Pair<SupplyInstance, Double>> getClosest(
         final Iterator<Map.Entry<UUID, SupplyInstance>> iterator, final double radius,
         final GeoPoint location) {
      double resDistance;
      SupplyInstance resInstance;

      if (iterator.hasNext()) {
         resInstance = iterator.next().getValue();
         resDistance = resInstance.getLocation().distanceTo(location, radius);
      } else {
         return null;
      }

      while (iterator.hasNext()) {
         SupplyInstance ins = iterator.next().getValue();
         double dist = ins.getLocation().distanceTo(resInstance.getLocation(), radius);
         if (dist < resDistance) {
            resInstance = ins;
            resDistance = dist;
         }
      }


      List<Pair<SupplyInstance, Double>> res = new ArrayList<>();
      res.add(new Pair<>(resInstance, resDistance));
      return res;
   }

   public static List<Pair<SupplyInstance, Double>> findPlacesWithinDistance(
         final Iterator<Map.Entry<UUID, SupplyInstance>> iterator, final double radius,
         final GeoPoint location, final double distance) {

      final GeoPoint[] boundCoords = boundingCoordinates(location, distance, radius);
      final boolean meridian180WithinDistance =
            boundCoords[0].getLonRadians() > boundCoords[1].getLonRadians();

      final double[] pDistance = new double[1];
      Predicate isInsidePred = new Predicate<SupplyInstance>() {
         double b1 = boundCoords[0].getLatRadians(), b2 = boundCoords[1].getLatRadians();
         double b3 = boundCoords[0].getLonRadians(), b4 = boundCoords[1].getLonRadians();

         @Override
         public boolean test(SupplyInstance obj) {
            if (!obj.isAcceptTrips() || obj.isOnTrip()) return false;

            GeoPoint p = obj.getLocation();
            double Lat = p.getLatRadians(), Lon = p.getLonRadians();

            //System.out.println(p.distanceTo(location, radius));
            if (!(Lat >= b1 && Lat <= b2)) return false;

            boolean c1 = Lon >= b3, c2 = Lon <= b4;
            c1 = meridian180WithinDistance ? c1 | c2 : c1 & c2;
            if (!c1) return false;

            pDistance[0] = p.distanceTo(location, radius);
            return pDistance[0] <= distance;
         }
      };

      ArrayList res = new ArrayList<Pair<SupplyInstance, Double>>();
      while (iterator.hasNext()) {
         Map.Entry<UUID, SupplyInstance> entry = iterator.next();
         if (isInsidePred.test(entry.getValue())) {
            res.add(new Pair<>(entry.getValue(), pDistance[0]));
         }
      }

      /*PreparedStatement statement = connection.prepareStatement(
            "SELECT * FROM Places WHERE (Lat >= ? AND Lat <= ?) AND (Lon >= ? " +
                  (meridian180WithinDistance ? "OR" : "AND") + " Lon <= ?) AND " +
                  "acos(sin(?) * sin(Lat) + cos(?) * cos(Lat) * cos(Lon - ?)) <= ?");
      statement.setDouble(1, boundingCoordinates[0].getLatRadians());
      statement.setDouble(2, boundingCoordinates[1].getLatRadians());
      statement.setDouble(3, boundingCoordinates[0].getLonRadians());
      statement.setDouble(4, boundingCoordinates[1].getLonRadians());
      statement.setDouble(5, location.getLatRadians());
      statement.setDouble(6, location.getLatRadians());
      statement.setDouble(7, location.getLonRadians());
      statement.setDouble(8, distance / radius);
      return statement.executeQuery();*/
      return res;
   }

}