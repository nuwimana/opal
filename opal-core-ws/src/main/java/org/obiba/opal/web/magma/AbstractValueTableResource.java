package org.obiba.opal.web.magma;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.mozilla.javascript.Scriptable;
import org.obiba.magma.Datasource;
import org.obiba.magma.Value;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueType;
import org.obiba.magma.Variable;
import org.obiba.magma.VariableEntity;
import org.obiba.magma.js.JavascriptValueSource;
import org.obiba.magma.js.MagmaContext;
import org.obiba.magma.js.views.JavascriptClause;
import org.obiba.magma.support.ValueTableWrapper;
import org.obiba.magma.type.BooleanType;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

abstract class AbstractValueTableResource {

  private final ValueTable valueTable;

  private final Set<Locale> locales;

  AbstractValueTableResource(ValueTable valueTable, Set<Locale> locales) {
    this.valueTable = valueTable;
    this.locales = new LinkedHashSet<Locale>(locales);
  }

  protected ValueTable getValueTable() {
    return valueTable;
  }

  protected Datasource getDatasource() {
    return valueTable.getDatasource();
  }

  protected Set<Locale> getLocales() {
    return Collections.unmodifiableSet(locales);
  }

  protected LocalesResource getLocalesResource() {
    return new LocalesResource(locales);
  }

  protected Iterable<Variable> filterVariables(String script, Integer offset, @Nullable Integer limit) {
    List<Variable> filteredVariables = null;

    if(script != null) {
      JavascriptClause jsClause = new JavascriptClause(script);
      jsClause.initialise();

      filteredVariables = new ArrayList<Variable>();
      for(Variable variable : getValueTable().getVariables()) {
        if(jsClause.select(variable)) {
          filteredVariables.add(variable);
        }
      }
    } else {
      filteredVariables = Lists.newArrayList(getValueTable().getVariables());
    }

    int fromIndex = offset < filteredVariables.size() ? offset : filteredVariables.size();
    int toIndex = limit != null && limit >= 0 //
        ? Math.min(fromIndex + limit, filteredVariables.size()) //
        : filteredVariables.size();

    return filteredVariables.subList(fromIndex, toIndex);
  }

  protected Iterable<VariableEntity> filterEntities(String script) {
    return filterEntities(script, null, null);
  }

  protected Iterable<VariableEntity> filterEntities(@Nullable String script, @Nullable Integer offset,
      @Nullable Integer limit) {
    Iterable<VariableEntity> entities;
    entities = script == null ? Sets.newTreeSet(valueTable.getVariableEntities()) : getFilteredEntities(script);
    // Apply offset then limit (in that order)
    if(offset != null) {
      entities = Iterables.skip(entities, offset);
    }
    if(limit != null && limit >= 0) {
      entities = Iterables.limit(entities, limit);
    }
    return entities;
  }

  private Iterable<VariableEntity> getFilteredEntities(@Nonnull String script) {
    //noinspection ConstantConditions
    Preconditions.checkArgument(script != null, "Entities filter script cannot be null.");

    JavascriptValueSource jvs = newJavaScriptValueSource(BooleanType.get(), script);

    SortedSet<VariableEntity> entities = Sets.newTreeSet(valueTable.getVariableEntities());
    final Iterator<Value> values = jvs.asVectorSource().getValues(entities).iterator();

    // filter the entities once and for all
    ImmutableList.Builder<VariableEntity> entityDtos = ImmutableList.builder();
    for(VariableEntity dto : Iterables.filter(entities, new Predicate<VariableEntity>() {

      @Override
      public boolean apply(VariableEntity input) {
        return values.next().getValue() == Boolean.TRUE;
      }
    })) {
      entityDtos.add(dto);
    }

    return entityDtos.build();
  }

  protected JavascriptValueSource newJavaScriptValueSource(ValueType valueType, String script) {
    JavascriptValueSource jvs = new JavascriptValueSource(valueType, script) {
      @Override
      protected void enterContext(MagmaContext ctx, Scriptable scope) {
        super.enterContext(ctx, scope);

        if(getValueTable() instanceof ValueTableWrapper) {
          ctx.push(ValueTable.class, ((ValueTableWrapper) getValueTable()).getWrappedValueTable());
        } else {
          ctx.push(ValueTable.class, getValueTable());
        }
      }

      @Override
      protected void exitContext(MagmaContext ctx) {
        super.exitContext(ctx);

        ctx.pop(ValueTable.class);
      }
    };
    jvs.initialise();
    return jvs;
  }

}
