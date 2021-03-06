/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.datashield;

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.obiba.opal.core.cfg.ExtensionConfigurationSupplier;
import org.obiba.opal.datashield.DataShieldEnvironment;
import org.obiba.opal.datashield.DataShieldLog;
import org.obiba.opal.datashield.NoSuchDataShieldMethodException;
import org.obiba.opal.datashield.cfg.DatashieldConfiguration;
import org.obiba.opal.datashield.cfg.DatashieldConfigurationSupplier;
import org.obiba.opal.r.service.OpalRService;
import org.obiba.opal.web.datashield.support.DataShieldMethodConverterRegistry;
import org.obiba.opal.web.model.DataShield;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.model.OpalR;
import org.rosuda.REngine.REXPMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

/**
 * Manage a Datashield Package.
 */
@SuppressWarnings("SpringJavaAutowiringInspection")
@Component
@Scope("request")
@Path("/datashield/package/{name}")
public class DataShieldPackageResource extends RPackageResource {

  private final DatashieldConfigurationSupplier configurationSupplier;

  private final DataShieldMethodConverterRegistry methodConverterRegistry;

  @PathParam("name")
  private String name;

  @Autowired
  public DataShieldPackageResource(OpalRService opalRService, DatashieldConfigurationSupplier configurationSupplier,
      DataShieldMethodConverterRegistry methodConverterRegistry) {
    super(opalRService);
    this.configurationSupplier = configurationSupplier;
    this.methodConverterRegistry = methodConverterRegistry;
  }

  public DataShieldPackageResource(String name, OpalRService opalRService,
      DatashieldConfigurationSupplier configurationSupplier,
      DataShieldMethodConverterRegistry methodConverterRegistry) {
    super(opalRService);
    this.name = name;
    this.configurationSupplier = configurationSupplier;
    this.methodConverterRegistry = methodConverterRegistry;
  }

  @GET
  public OpalR.RPackageDto getPackage() throws REXPMismatchException {
    return getDatashieldPackage(name);
  }

  /**
   * Get all the methods of the package.
   * @return
   * @throws REXPMismatchException
   */
  @GET
  @Path("/methods")
  public DataShield.DataShieldPackageMethodsDto getPackageMethods() throws REXPMismatchException {
    OpalR.RPackageDto packageDto = getPackage();
    List<DataShield.DataShieldMethodDto> aggregateMethodDtos = Lists.newArrayList();
    List<DataShield.DataShieldMethodDto> assignMethodDtos = Lists.newArrayList();
    for(Opal.EntryDto entry : packageDto.getDescriptionList()) {
      if(entry.getKey().equals(AGGREGATE_METHODS)) {
        aggregateMethodDtos.addAll(parsePackageMethods(entry.getValue()));
      } else if(entry.getKey().equals(ASSIGN_METHODS)) {
        assignMethodDtos.addAll(parsePackageMethods(entry.getValue()));
      }
    }
    return DataShield.DataShieldPackageMethodsDto.newBuilder().setName(name).addAllAggregate(aggregateMethodDtos)
        .addAllAssign(assignMethodDtos).build();
  }

  /**
   * Publish all the methods of the package.
   * @return the installed methods
   * @throws REXPMismatchException
   */
  @PUT
  @Path("methods")
  public DataShield.DataShieldPackageMethodsDto publishPackageMethods() throws REXPMismatchException {
    final DataShield.DataShieldPackageMethodsDto methods = getPackageMethods();

    configurationSupplier
        .modify(new ExtensionConfigurationSupplier.ExtensionConfigModificationTask<DatashieldConfiguration>() {

          @Override
          public void doWithConfig(DatashieldConfiguration config) {
            addMethods(configurationSupplier.get().getAggregateEnvironment(), methods.getAggregateList());
            addMethods(configurationSupplier.get().getAssignEnvironment(), methods.getAssignList());
          }

          private void addMethods(DataShieldEnvironment env, List<DataShield.DataShieldMethodDto> envMethods) {
            for(DataShield.DataShieldMethodDto method : envMethods) {
              if(env.hasMethod(method.getName())) {
                env.removeMethod(method.getName());
              }
              env.addMethod(methodConverterRegistry.parse(method));
            }
          }
        });

    return methods;
  }

  private List<DataShield.DataShieldMethodDto> parsePackageMethods(String value) {
    String normalized = value.replaceAll("[\n\r\t ]", "");
    List<DataShield.DataShieldMethodDto> methodDtos = Lists.newArrayList();
    for(String map : normalized.split(",")) {
      String[] entry = map.split("=");
      DataShield.RFunctionDataShieldMethodDto methodDto = DataShield.RFunctionDataShieldMethodDto.newBuilder()
          .setFunc(entry.length == 1 ? name + "::" + entry[0] : entry[1]).build();
      DataShield.DataShieldMethodDto.Builder builder = DataShield.DataShieldMethodDto.newBuilder();
      builder.setName(entry[0]);
      builder.setExtension(DataShield.RFunctionDataShieldMethodDto.method, methodDto);
      methodDtos.add(builder.build());
    }
    return methodDtos;
  }

  /**
   * Silently deletes a package and its methods.
   *
   * @return
   */
  @DELETE
  public Response deletePackage() {

    try {
      DataShield.DataShieldPackageMethodsDto methods = getPackageMethods();

      // Aggregate
      for(int i = 0; i < methods.getAggregateCount(); i++) {
        final String methodName = methods.getAggregate(i).getName();

        try {
          configurationSupplier
              .modify(new ExtensionConfigurationSupplier.ExtensionConfigModificationTask<DatashieldConfiguration>() {

                @Override
                public void doWithConfig(DatashieldConfiguration config) {
                  config.getEnvironment(DatashieldConfiguration.Environment.AGGREGATE).removeMethod(methodName);
                }
              });
          DataShieldLog.adminLog("deleted method '{}' from environment {}.", methodName,
              DatashieldConfiguration.Environment.AGGREGATE);
        } catch(NoSuchDataShieldMethodException nothing) {
          // nothing, the method may have been deleted manually
        }
      }

      // Assign
      for(int i = 0; i < methods.getAssignCount(); i++) {
        final String methodName = methods.getAssign(i).getName();

        try {
          configurationSupplier
              .modify(new ExtensionConfigurationSupplier.ExtensionConfigModificationTask<DatashieldConfiguration>() {

                @Override
                public void doWithConfig(DatashieldConfiguration config) {
                  config.getEnvironment(DatashieldConfiguration.Environment.ASSIGN).removeMethod(methodName);
                }
              });
          DataShieldLog.adminLog("deleted method '{}' from environment {}.", methodName,
              DatashieldConfiguration.Environment.ASSIGN);
        } catch(NoSuchDataShieldMethodException nothing) {
          // nothing, the method may have been deleted manually
        }
      }
      removePackage(name);
    } catch(REXPMismatchException e) {
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    return Response.ok().build();
  }

}
