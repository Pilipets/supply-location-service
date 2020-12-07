package uber.location_service.algo;

import uber.location_service.structures.GeoPoint;
import uber.location_service.structures.SupplyInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class AlgoInterface {
   private final static double earthRadius = 6371.01; //km
   private final static double maxSearchDistance = 15000; // km
   private final static double minSearchDistance = 10; // km

   public List<SupplyInstance> getClosestSupply(
         ConcurrentHashMap<String, SupplyInstance> lhm, GeoPoint location) {
      return GeoAlgorithms.getClosest(
            lhm.entrySet().iterator(), earthRadius, location);
   }

   public List<SupplyInstance> getRadiusSupply(
         ConcurrentHashMap<String, SupplyInstance> lhm, GeoPoint location) {
      List<SupplyInstance> radiusSupplySet = new ArrayList<>();
      double curDistance = minSearchDistance;

      while (curDistance <= maxSearchDistance && radiusSupplySet.isEmpty()) {
         radiusSupplySet = GeoAlgorithms.findPlacesWithinDistance(
               lhm.entrySet().iterator(), earthRadius, location, curDistance);
         curDistance *= 2;
      }

      return radiusSupplySet;
   }
}
