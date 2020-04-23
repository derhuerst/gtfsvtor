package com.mecatran.gtfsvtor.validation.dao;

import com.mecatran.gtfsvtor.dao.ReadOnlyDao;
import com.mecatran.gtfsvtor.geospatial.GeoCoordinates;
import com.mecatran.gtfsvtor.geospatial.Geodesics;
import com.mecatran.gtfsvtor.model.GtfsStop;
import com.mecatran.gtfsvtor.model.GtfsStopType;
import com.mecatran.gtfsvtor.reporting.ReportIssueSeverity;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.StopTooFarFromParentStationIssue;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.DaoValidator;

/*
 *  TODO Test for distance between quay and stop (another validator?)
 */
public class StopTooFarFromParentStationValidator implements DaoValidator {

	@ConfigurableOption(description = "Distance between stop and station above which a warning is generated")
	private double maxWarningDistanceMeters = 100;

	/* -1 will disable errors */
	@ConfigurableOption(description = "Distance between stop and station above which a error is generated")
	private double maxErrorDistanceMeters = 1000;

	@Override
	public void validate(DaoValidator.Context context) {
		ReadOnlyDao dao = context.getDao();
		ReportSink reportSink = context.getReportSink();

		dao.getStops().forEach(stop -> {
			// Should we test distance of entrances? nodes?
			if (stop.getType() != GtfsStopType.STOP)
				return;
			if (stop.getParentId() == null)
				return;
			GeoCoordinates pStop = stop.getCoordinates();
			if (pStop == null)
				return;
			GtfsStop station = dao.getStop(stop.getParentId());
			if (station == null)
				return;
			// Do not test distance if data is bogus
			if (station.getType() != GtfsStopType.STATION)
				return;
			GeoCoordinates pStation = station.getCoordinates();
			if (pStation == null)
				return;
			double distance = Geodesics.distanceMeters(pStop, pStation);
			if (distance >= maxWarningDistanceMeters
					|| distance >= maxErrorDistanceMeters) {
				reportSink.report(new StopTooFarFromParentStationIssue(stop,
						station, distance,
						distance >= maxErrorDistanceMeters
								? ReportIssueSeverity.ERROR
								: distance >= maxWarningDistanceMeters
										? ReportIssueSeverity.WARNING
										: ReportIssueSeverity.INFO));
			}
		});
	}
}
