/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.search.es;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.common.base.Preconditions;
import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.concurrent.ConcurrentValueTableReader;
import org.obiba.magma.concurrent.ConcurrentValueTableReader.ConcurrentReaderCallback;
import org.obiba.magma.type.BinaryType;
import org.obiba.opal.core.domain.VariableNature;
import org.obiba.opal.search.IndexManagerConfigurationService;
import org.obiba.opal.search.IndexSynchronization;
import org.obiba.opal.search.ValueTableIndex;
import org.obiba.opal.search.ValueTableValuesIndex;
import org.obiba.opal.search.ValuesIndexManager;
import org.obiba.opal.search.es.mapping.ValueTableMapping;
import org.obiba.runtime.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Component
public class EsValuesIndexManager extends EsIndexManager implements ValuesIndexManager {

  private static final Logger log = LoggerFactory.getLogger(EsValuesIndexManager.class);

  private final ThreadFactory threadFactory;

  private final Set<EsValueTableValuesIndex> indices = Sets.newHashSet();

  @SuppressWarnings("SpringJavaAutowiringInspection")
  @Autowired
  public EsValuesIndexManager(ElasticSearchProvider esProvider, ElasticSearchConfigurationService esConfig,
      IndexManagerConfigurationService indexConfig, ThreadFactory threadFactory, Version version) {
    super(esProvider, esConfig, indexConfig, version);
    Preconditions.checkNotNull(threadFactory);
    this.threadFactory = threadFactory;
  }

  @Override
  public EsValueTableValuesIndex getIndex(ValueTable vt) {
    Preconditions.checkNotNull(vt);

    for(EsValueTableValuesIndex i : indices) {
      if(i.isForTable(vt)) return i;
    }
    EsValueTableValuesIndex i = new EsValueTableValuesIndex(vt);
    indices.add(i);
    return i;
  }

  @Override
  public IndexSynchronization createSyncTask(ValueTable valueTable, ValueTableIndex index) {
    return new Indexer(valueTable, (EsValueTableValuesIndex) index);
  }

  @Override
  public String getName() {
    return esIndexName() + "-values";
  }

  private class Indexer extends EsIndexer {

    private final EsValueTableValuesIndex index;

    private Indexer(ValueTable table, EsValueTableValuesIndex index) {
      super(table, index);
      this.index = index;
    }

    @SuppressWarnings("OverlyComplexAnonymousInnerClass")
    @Override
    protected void index() {

      XContentBuilder b = new ValueTableMapping().createMapping(runtimeVersion, index.getIndexName(), valueTable);

      esProvider.getClient().admin().indices().preparePutMapping(getName()).setType(index.getIndexName()).setSource(b)
          .execute().actionGet();

      ConcurrentValueTableReader.Builder.newReader().withThreads(threadFactory).ignoreReadErrors().from(valueTable)
          .variables(index.getVariables()).to(new ConcurrentReaderCallback() {

        private BulkRequestBuilder bulkRequest = esProvider.getClient().prepareBulk();

        private final Map<Variable, VariableNature> natures = new HashMap<Variable, VariableNature>();

        @Override
        public void onBegin(List<VariableEntity> entitiesToCopy, Variable... variables) {
          for(Variable variable : variables) {
            natures.put(variable, VariableNature.getNature(variable));
          }
        }

        @Override
        public void onValues(VariableEntity entity, Variable[] variables, Value... values) {
          if(stop) {
            return;
          }

          bulkRequest.add(
              esProvider.getClient().prepareIndex(getName(), valueTable.getEntityType(), entity.getIdentifier())
                  .setSource("{\"identifier\":\"" + entity.getIdentifier() + "\"}"));
          try {
            XContentBuilder xcb = XContentFactory.jsonBuilder().startObject();
            for(int i = 0; i < variables.length; i++) {
              String fieldName = index.getFieldName(variables[i].getName());
              if(values[i].isSequence() && !values[i].isNull()) {
                List<Object> vals = Lists.newArrayList();
                for(Value v : values[i].asSequence().getValue()) {
                  vals.add(esValue(variables[i], v));
                }
                xcb.field(fieldName, vals);
              } else {
                xcb.field(fieldName, esValue(variables[i], values[i]));
              }
            }
            bulkRequest.add(esProvider.getClient().prepareIndex(getName(), index.getIndexName(), entity.getIdentifier())
                .setParent(entity.getIdentifier()).setSource(xcb.endObject()));
            done++;
            if(bulkRequest.numberOfActions() >= ES_BATCH_SIZE) {
              bulkRequest = sendAndCheck(bulkRequest);
            }
          } catch(IOException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public void onComplete() {
          if(stop) {
            index.delete();
          } else {
            sendAndCheck(bulkRequest);
            index.updateTimestamps();
          }
        }

        /**
         * OPAL-1158: missing values are indexed as null for continuous variables
         * @param variable the variable
         * @param value the value
         * @return an object
         */
        private Object esValue(Variable variable, Value value) {
          switch(natures.get(variable)) {
            case CONTINUOUS:
              if(variable.isMissingValue(value)) {
                return null;
              }
          }
          return value.getValue();
        }

      }).build().read();

    }

  }

  private class EsValueTableValuesIndex extends EsValueTableIndex implements ValueTableValuesIndex {

    private EsValueTableValuesIndex(ValueTable vt) {
      super(vt);
    }

    @Override
    public String getFieldName(String variable) {
      return getIndexName() + "-" + variable;
    }

    @Override
    public Iterable<Variable> getVariables() {
      // Do not index binary values, do not even extract the binary values
      // TODO Could be configurable at table level?
      return Iterables.filter(resolveTable().getVariables(), new Predicate<Variable>() {

        @Override
        public boolean apply(Variable input) {
          return !input.getValueType().equals(BinaryType.get());
        }

      });
    }

  }
}