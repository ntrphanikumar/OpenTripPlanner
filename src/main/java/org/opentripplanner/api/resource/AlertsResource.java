package org.opentripplanner.api.resource;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import org.opentripplanner.routing.alertpatch.Alert;
import org.opentripplanner.routing.alertpatch.AlertPatch;
import org.opentripplanner.standalone.OTPServer;
import org.opentripplanner.updater.GraphUpdater;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.alerts.GtfsRealtimeAlertsUpdater;

@Path("/routers/{routerId}/alerts")
@XmlRootElement
public class AlertsResource {

    @Context OTPServer otpServer;

    @GET
    @Produces({ MediaType.APPLICATION_JSON})
    public Collection<AlertDTO> getAllAlerts(@PathParam("routerId") String routerId) {
    	GraphUpdaterManager updaterManager = otpServer.getRouter(routerId).graph.updaterManager;
    	Collection<AlertPatch> alertPatches = Collections.emptyList();
        for(int i=0;i<updaterManager.size();i++) {
        	GraphUpdater updater = updaterManager.getUpdater(i);
        	if(updater instanceof GtfsRealtimeAlertsUpdater) {
        		GtfsRealtimeAlertsUpdater alertsUpdater = (GtfsRealtimeAlertsUpdater)updater;
        		alertPatches = alertsUpdater.getAlertPatchService().getAllAlertPatches();
        		break;
        	}
        }
        return alertPatches.stream().map(AlertDTO::from).collect(Collectors.toSet());
    }
    
    public static final class AlertDTO {
    	private String header, description;
    	private Date start, end;
    	AlertDTO(Alert alert) {
    		header = alert.alertHeaderText.toString();
    		description = alert.alertDescriptionText.toString();
    		start = alert.effectiveStartDate;
    		end = alert.effectiveEndDate;
    	}
    	
    	static AlertDTO from(AlertPatch alert) {
    		return new AlertDTO(alert.getAlert());
    	}
    	
    	public String getHeader() {
			return header;
		}
    	
    	public String getDescription() {
			return description;
		}
    	
    	public Date getStart() {
			return start;
		}
    	
    	public Date getEnd() {
			return end;
		}
    	
    	@Override
    	public int hashCode() {
    		return header.hashCode()+start.hashCode();
    	}
    	
    	@Override
    	public boolean equals(Object obj) {
    		if(!(obj instanceof AlertDTO)) {
    			return false;
    		}
    		AlertDTO alert = (AlertDTO)obj;
    		return alert.description.equals(description) && alert.start.equals(start) && alert.end.equals(end);
    	}
    }
}
