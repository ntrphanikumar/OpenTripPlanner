package org.opentripplanner.updater.vehiclepositions;

import java.util.HashSet;
import java.util.Set;

import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition;
import com.google.transit.realtime.GtfsRealtime.VehiclePosition.OccupancyStatus;

public class VehiclePositionsUpdateHandler {
	
	private String feedId = null;
	
	private Set<VehiclePositionDTO> positions = new HashSet<>();
	
	public void update(FeedMessage message) {
		synchronized (positions) {
			positions.clear();
			for (FeedEntity entity : message.getEntityList()) {
	            if (!entity.hasVehicle()) {
	                continue;
	            }
	            positions.add(new VehiclePositionDTO(feedId, entity.getVehicle()));
	        }
		}
	}
	
	public void setFeedId(String feedId) {
		this.feedId = feedId;
	}
	
	public Set<VehiclePositionDTO> getPositions() {
		synchronized (positions) {
			return positions;
		}
	}

	public static final class VehiclePositionDTO {
		private String route, trip, stop, direction;
		private Float latitude, longitude;
		private String vehicleId, vehicleLabel;
		private OccupancyStatus occupancyStatus;
		private Long timestamp;
		
		public VehiclePositionDTO(String feedId, VehiclePosition pos) {
			if(pos.hasTrip()) {
				if(pos.getTrip().hasRouteId()) {
					route = feedId+":"+pos.getTrip().getRouteId();
				}
				if(pos.getTrip().hasTripId()) {
					trip = feedId+":"+pos.getTrip().getTripId();
				}
				if(pos.getTrip().hasDirectionId()) {
					direction = feedId+":"+pos.getTrip().getDirectionId();
				}
			}
			if(pos.hasStopId()) {
				stop = feedId+":"+pos.getStopId();
			}
			if(pos.hasOccupancyStatus()) {
				occupancyStatus = pos.getOccupancyStatus();
			}
			if(pos.hasPosition()) {
				latitude = pos.getPosition().getLatitude();
				longitude = pos.getPosition().getLongitude();
			}
			if(pos.hasVehicle()) {
				vehicleId = pos.getVehicle().getId();
				vehicleLabel = pos.getVehicle().getLabel();
			}
			if(pos.hasTimestamp()) {
				timestamp = pos.getTimestamp();
			}
		}

		public String getRoute() {
			return route;
		}
		
		public String getTrip() {
			return trip;
		}
		
		public String getStop() {
			return stop;
		}
		
		public String getDirection() {
			return direction;
		}

		public Float getLatitude() {
			return latitude;
		}

		public Float getLongitude() {
			return longitude;
		}

		public String getVehicleId() {
			return vehicleId;
		}

		public String getVehicleLabel() {
			return vehicleLabel;
		}
		
		public OccupancyStatus getOccupancyStatus() {
			return occupancyStatus;
		}

		public Long getTimestamp() {
			return timestamp;
		}
	}

}
