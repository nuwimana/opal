/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.math;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.opal.core.magma.math.ContinuousVariableSummary;
import org.obiba.opal.core.magma.math.ContinuousVariableSummary.Distribution;
import org.obiba.opal.search.StatsIndexManager;
import org.obiba.opal.search.es.ElasticSearchProvider;
import org.obiba.opal.search.service.OpalSearchService;
import org.obiba.opal.web.TimestampedResponses;
import org.obiba.opal.web.magma.Dtos;
import org.obiba.opal.web.model.Math.ContinuousSummaryDto;
import org.obiba.opal.web.model.Math.SummaryStatisticsDto;
import org.obiba.opal.web.model.Search;
import org.obiba.opal.web.model.Search.EsContinuousSummaryDto;
import org.obiba.opal.web.search.support.EsQueryBuilders;
import org.obiba.opal.web.search.support.EsQueryExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ExtensionRegistry;
import com.googlecode.protobuf.format.JsonFormat;

public class ContinuousSummaryResource extends AbstractSummaryResource {

  private static final Logger log = LoggerFactory.getLogger(ContinuousSummaryResource.class);

  public ContinuousSummaryResource(OpalSearchService opalSearchService, StatsIndexManager statsIndexManager,
      ElasticSearchProvider esProvider, ValueTable valueTable, Variable variable, VariableValueSource vvs) {
    super(opalSearchService, statsIndexManager, esProvider, valueTable, variable, vvs);
  }

  @GET
  @POST
  public Response compute(@QueryParam("d") @DefaultValue("normal") Distribution distribution,
      @QueryParam("p") List<Double> percentiles, @QueryParam("intervals") @DefaultValue("10") int intervals,
      @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit) {

    SummaryStatisticsDto.Builder statisticsDto = SummaryStatisticsDto.newBuilder().setResource(getVariable().getName());

    if(offset == null && limit == null) {
      ContinuousSummaryDto summary = canQueryEsIndex()
          ? queryEs(distribution, percentiles, intervals)
          : queryMagma(distribution, percentiles, intervals);

      statisticsDto.setExtension(ContinuousSummaryDto.continuous, summary);
      return TimestampedResponses.ok(getValueTable(), statisticsDto.build()).build();
    } else {
      ContinuousSummaryDto summary = queryMagma(distribution, percentiles, intervals, offset, limit);
      statisticsDto.setExtension(ContinuousSummaryDto.continuous, summary);
      return Response.ok(statisticsDto.build()).build();
    }
  }

  private ContinuousSummaryDto queryEs(Distribution distribution, List<Double> percentiles, int intervals) {
    log.debug("Query ES for {} summary", getVariable().getName());

    try {

      // TODO query on defaultPercentiles
      JSONObject esQuery = new EsQueryBuilders.EsBoolTermsQueryBuilder() //
          .addTerm("_id", getVariable().getVariableReference(getValueTable())) //
          .addTerm("_type", statsIndexManager.getIndex(getValueTable()).getIndexName()) //
          .addTerm("continuous", String.valueOf(true)) // because I don't know to use field exists query filter
          .addTerm("distribution", distribution.name()) //
          .addTerm("intervals", String.valueOf(intervals)).build();
      log.trace("ES query: {}", esQuery.toString(2));

      JSONObject response = new EsQueryExecutor(esProvider).execute(esQuery);
      log.trace("ES Response: {}", response.toString(2));

      JSONObject jsonHitsInfo = response.getJSONObject("hits");
      if(jsonHitsInfo.getInt("total") != 1) {
        return queryMagma(distribution, percentiles, intervals); // fallback
      }

      JSONObject jsonObject = jsonHitsInfo.getJSONArray("hits").getJSONObject(0).getJSONObject("_source");

      log.trace("jsonObject: {}", jsonObject.toString(2));

      Search.EsStatsDto.Builder builder = Search.EsStatsDto.newBuilder();
      ExtensionRegistry registry = ExtensionRegistry.newInstance();
      registry.add(EsContinuousSummaryDto.continuousSummary);
      JsonFormat.merge(jsonObject.toString(), registry, builder);
      return builder.build().getExtension(EsContinuousSummaryDto.continuousSummary).getSummary();

    } catch(JSONException e) {
      log.error("Error while querying ES (will fallback to Magma)", e);
      return queryMagma(distribution, percentiles, intervals);
    } catch(IOException e) {
      log.error("Error while querying ES (will fallback to Magma)", e);
      return queryMagma(distribution, percentiles, intervals);
    }
  }

  private ContinuousSummaryDto queryMagma(Distribution distribution, List<Double> percentiles, int intervals) {
    return queryMagma(distribution, percentiles, intervals, null, null);
  }

  private ContinuousSummaryDto queryMagma(Distribution distribution, List<Double> percentiles, int intervals,
      Integer offset, Integer limit) {

    log.debug("Query Magma for {} summary", getVariable().getName());

    ContinuousVariableSummary summary = new ContinuousVariableSummary.Builder(getVariable(), distribution) //
        .defaultPercentiles(percentiles) //
        .intervals(intervals) //
        .filter(offset, limit) //
        .addTable(getValueTable(), getVariableValueSource()) //
        .build();

    if(!"_transient".equals(getVariable().getName()) && isEsAvailable() && !summary.isFiltered()) {
      statsIndexManager.getIndex(getValueTable()).indexSummary(summary);
    }

    return Dtos.asDto(summary).build();
  }

}
