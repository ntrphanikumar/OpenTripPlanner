package org.opentripplanner.updater.vehiclepositions;

import java.io.InputStream;
import java.util.Set;

import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.updater.GraphUpdaterManager;
import org.opentripplanner.updater.GraphWriterRunnable;
import org.opentripplanner.updater.PollingGraphUpdater;
import org.opentripplanner.updater.vehiclepositions.VehiclePositionsUpdateHandler.VehiclePositionDTO;
import org.opentripplanner.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;

public class GtfsRealtimeVehiclePostionsUpdater extends PollingGraphUpdater {
	
	private static final Logger LOG = LoggerFactory.getLogger(GtfsRealtimeVehiclePostionsUpdater.class);

    private GraphUpdaterManager updaterManager;

    private Long lastTimestamp = Long.MIN_VALUE;

    private String url;
    
    private String feedId;

    private VehiclePositionsUpdateHandler updateHandler = null;

	@Override
	public void setGraphUpdaterManager(GraphUpdaterManager updaterManager) {
		this.updaterManager = updaterManager;
	}

	@Override
	public void setup(Graph graph) throws Exception {
		if (updateHandler == null) {
            updateHandler = new VehiclePositionsUpdateHandler();
        }
		updateHandler.setFeedId(feedId);
	}

	@Override
	public void teardown() {
	}

	@Override
	protected void runPolling() throws Exception {
		try {
            InputStream data = HttpUtils.getData(url);
            if (data == null) {
                throw new RuntimeException("Failed to get data from url " + url);
            }

            final FeedMessage feed = FeedMessage.PARSER.parseFrom(data);

            long feedTimestamp = feed.getHeader().getTimestamp();
            if (feedTimestamp <= lastTimestamp) {
                LOG.info("Ignoring feed with an old timestamp.");
                return;
            }

            // Handle update in graph writer runnable
            updaterManager.execute(new GraphWriterRunnable() {
                @Override
                public void run(Graph graph) {
                    updateHandler.update(feed);
                }
            });

            lastTimestamp = feedTimestamp;
        } catch (Exception e) {
            LOG.error("Error reading gtfs-realtime feed from " + url, e);
        }
	}

	@Override
	protected void configurePolling(Graph graph, JsonNode config) throws Exception {
        String url = config.path("url").asText();
        if (url == null) {
            throw new IllegalArgumentException("Missing mandatory 'url' parameter");
        }
        this.url = url;
        this.feedId = config.path("feedId").asText();
        LOG.info("Creating real-time vehicle positions updater running every {} seconds : {}", pollingPeriodSeconds, url);		
	}
	
	public Set<VehiclePositionDTO> getVehiclePositions() {
		return updateHandler.getPositions();
	}

}
