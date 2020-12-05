package uber.location_service.structures;

import java.util.UUID;

public class SupplyInstance {
   private UUID id;
   private GeoPoint location;

   public SupplyInstance() {
   }

   public SupplyInstance(SupplyInstance other) {
      id = other.id;
      location = new GeoPoint(other.location);
   }

   public UUID getId() {
      return id;
   }

   public GeoPoint getLocation() {
      return location;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      SupplyInstance other = (SupplyInstance) o;
      return id.equals(other.id) && location.equals(other.location);
   }
}
