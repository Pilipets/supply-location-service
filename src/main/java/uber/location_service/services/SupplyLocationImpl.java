package uber.location_service.services;

import org.springframework.stereotype.Service;
import uber.location_service.algo.AlgoInterface;
import uber.location_service.structures.GeoPoint;
import uber.location_service.structures.SupplyInstance;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SupplyLocationImpl {
   private final AlgoInterface algoInterface;
   protected ConcurrentHashMap<UUID, SupplyInstance> lhm;

   public SupplyLocationImpl() {
      this.algoInterface = new AlgoInterface();
      this.lhm = new ConcurrentHashMap<>(100);
   }

   public void  updateSupply(SupplyInstance val) {
      lhm.put(val.getId(), val);
   }

   public List<SupplyInstance> getRadiusSupply(GeoPoint location) {
      List<SupplyInstance> res = algoInterface.getRadiusSupply(lhm, location);
      for (SupplyInstance ins : res) ins.getLocation().transformToDegrees();
      return res;
   }

   public List<SupplyInstance> getClosestSupply(GeoPoint location) {
      List<SupplyInstance> res = algoInterface.getClosestSupply(lhm, location);
      for (SupplyInstance ins : res) ins.getLocation().transformToDegrees();
      return res;
   }

   public GeoPoint  getSupplyLocation(UUID id) {
      SupplyInstance ins = lhm.getOrDefault(id, null);
      if (ins == null) {
         return null;
      }
      return ins.getLocation();
   }
}
