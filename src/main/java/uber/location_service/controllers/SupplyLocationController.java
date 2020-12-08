package uber.location_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import uber.location_service.services.SupplyLocationImpl;
import uber.location_service.structures.GeoPoint;
import uber.location_service.structures.SupplyInstance;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

/**
 * This is the API that exists at the moment due to the chosen implementation model,
 * but eventually should be replaced with another approach. SupplyLocationService must not
 * consume any data from the Kafka - instead use the KD-tree or local-sensitive hashing
 * with Google S2 library to get the polygons, that requested geolocation intersects,
 * then make "MapReduce(...)" call to the Filter service in turn to the HDFS to get the
 * filtered drivers in the intersected polygons.
 */
@RestController()
@RequestMapping()
public class SupplyLocationController {
   private final SupplyLocationImpl impl;

   @Autowired
   public SupplyLocationController(final SupplyLocationImpl impl) {
      this.impl = impl;
   }

   @GetMapping(path="/get-closest")
   public DeferredResult<ResponseEntity<Object>> getClosestHandler(
         GeoPoint geoPoint) {
      DeferredResult<ResponseEntity<Object>> output = new DeferredResult<>();

      ForkJoinPool.commonPool().submit(() -> {
         List<SupplyInstance> arr = impl.getClosestSupply(geoPoint);
         output.setResult(new ResponseEntity<>(arr, HttpStatus.OK));
      });

      return output;
   }

   @GetMapping(path="/get-closest-in-radius")
   public DeferredResult<ResponseEntity<Object>> getClosestInRadiusHandler(
         GeoPoint geoPoint) {
      DeferredResult<ResponseEntity<Object>> output = new DeferredResult<>();

      ForkJoinPool.commonPool().submit(() -> {
         List<SupplyInstance> arr = impl.getRadiusSupply(geoPoint);
         output.setResult(new ResponseEntity<>(arr, HttpStatus.OK));
      });

      return output;
   }

   @PostMapping(path="/update-supply")
   public ResponseEntity<Object> updateSupplyInstance(
         @RequestBody SupplyInstance ins) {

      ins.getLocation().transformToRadians();
      impl.updateSupply(ins);
      return new ResponseEntity<>(HttpStatus.OK);
   }

   @GetMapping(path="/get-location")
   public ResponseEntity<Object> getSupplyLocation(
         @RequestParam(value = "id") String id) {
      GeoPoint location = impl.getSupplyLocation(id);
      if (location == null) return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);

      location = new GeoPoint(location);
      location.transformToDegrees();
      return new ResponseEntity<>(location, HttpStatus.OK);
   }
}
