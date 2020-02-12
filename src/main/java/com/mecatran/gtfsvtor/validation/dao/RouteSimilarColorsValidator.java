package com.mecatran.gtfsvtor.validation.dao;

import java.util.ArrayList;
import java.util.List;

import com.mecatran.gtfsvtor.dao.IndexedReadOnlyDao;
import com.mecatran.gtfsvtor.model.GtfsRoute;
import com.mecatran.gtfsvtor.reporting.ReportSink;
import com.mecatran.gtfsvtor.reporting.issues.SimilarRouteColorWarning;
import com.mecatran.gtfsvtor.validation.ConfigurableOption;
import com.mecatran.gtfsvtor.validation.DaoValidator;

public class RouteSimilarColorsValidator implements DaoValidator {

	@ConfigurableOption
	private double minColorDistance = 0.005; // 0.5%

	@Override
	public void validate(DaoValidator.Context context) {
		IndexedReadOnlyDao dao = context.getDao();
		ReportSink reportSink = context.getReportSink();

		/*
		 * This is O(n²), but we do not care as the color distance computation
		 * is very fast and the max number of routes is relatively low. Having a
		 * color 3D spatial index would probably be slower than this, and
		 * certainly more complex.
		 */
		List<GtfsRoute> routes = new ArrayList<>(dao.getRoutes());
		for (int i = 0; i < routes.size(); i++) {
			for (int j = i + 1; j < routes.size(); j++) {
				GtfsRoute route1 = routes.get(i);
				GtfsRoute route2 = routes.get(j);
				double dc = route1.getNonNullColor()
						.getDistance(route2.getNonNullColor());
				if (dc < minColorDistance && dc != 0.0) {
					reportSink.report(new SimilarRouteColorWarning(route1,
							route2, dc, true));
				}
				double dt = route1.getNonNullTextColor()
						.getDistance(route2.getNonNullTextColor());
				if (dt < minColorDistance && dt != 0.0) {
					reportSink.report(new SimilarRouteColorWarning(route1,
							route2, dt, false));
				}
			}
		}
	}
}
