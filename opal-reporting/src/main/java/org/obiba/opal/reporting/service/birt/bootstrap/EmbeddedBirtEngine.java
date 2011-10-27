/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.reporting.service.birt.bootstrap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.birt.core.framework.Platform;
import org.eclipse.birt.report.engine.api.EngineConfig;
import org.eclipse.birt.report.engine.api.EngineException;
import org.eclipse.birt.report.engine.api.IEngineTask;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportEngineFactory;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.obiba.opal.reporting.service.birt.common.BirtEngine;
import org.obiba.opal.reporting.service.birt.common.BirtEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.common.io.Files;

public class EmbeddedBirtEngine implements BirtEngine {

  private static final Logger log = LoggerFactory.getLogger(EmbeddedBirtEngine.class);

  private static final String BIRT_HOME_SYSTEM_PROPERTY_NAME = "BIRT_HOME";

  private static final String OSGI_CONFIGURATION_AREA_PROPERTY_NAME = "osgi.configuration.area";

  private static final String OSGI_INSTANCE_AREA_PROPERTY_NAME = "osgi.instance.area";

  private IReportEngine engine;

  private File osgiHome;

  @Override
  public void render(String formatName, Map<String, String> parameters, String reportDesign, String reportOutput) throws BirtEngineException {
    if(isRunning() == false) {
      throw new BirtEngineException("Report engine not running. Please check startup logs for details.");
    }

    BirtReportFormat format = BirtReportFormat.valueOf(formatName.toUpperCase());

    try {
      // Open the report design
      IReportRunnable design = engine.openReportDesign(reportDesign);

      // Create task to run and render the report,
      IRunAndRenderTask task = createTask(design, parameters, format, reportOutput);

      try {
        runAndValidateTask(task);
      } finally {
        task.close();
      }
    } catch(EngineException e) {
      throw new BirtEngineException(e.getLocalizedMessage());
    }
  }

  @Override
  public boolean isRunning() {
    return engine != null;
  }

  @Override
  public void start() {
    if(isRunning()) return;

    try {
      // make sure BIRT_HOME is set and valid
      File reportEngineHome = new File(System.getProperty(BIRT_HOME_SYSTEM_PROPERTY_NAME), "ReportEngine").getAbsoluteFile();

      if(reportEngineHome.exists() == false) {
        log.warn("BIRT ReportEngine directory does not exist {}", reportEngineHome.getPath());
        return;
      }

      final EngineConfig config = new EngineConfig();
      config.setEngineHome(reportEngineHome.getAbsolutePath());
      configureOsgi(config);
      bindLoggingToSlf4J(config);

      Platform.startup(config);
      IReportEngineFactory factory = (IReportEngineFactory) Platform.createFactoryObject(IReportEngineFactory.EXTENSION_REPORT_ENGINE_FACTORY);
      engine = factory.createReportEngine(config);
    } catch(Exception e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  public void stop() {
    try {
      engine.destroy();
      Platform.shutdown();
    } catch(Exception e) {
      log.warn("Exception during BIRT shutdown", e);
    } finally {
      engine = null;
      try {
        if(osgiHome != null) {
          Files.deleteRecursively(osgiHome);
        }
      } catch(IOException e) {
        // ignore
      }
    }
  }

  private IRunAndRenderTask createTask(IReportRunnable design, Map<String, String> parameters, BirtReportFormat format, String reportOutput) {
    IRunAndRenderTask task = engine.createRunAndRenderTask(design);

    // Set parameter values and validate
    if(parameters != null) {
      for(Entry<String, String> entry : parameters.entrySet()) {
        task.setParameterValue(entry.getKey(), entry.getValue());
      }
    }

    if(task.validateParameters() == false) {
      // TODO: what do we do with invalid parameters?
    }

    task.setRenderOption(getOptions(format, reportOutput));
    return task;
  }

  private RenderOption getOptions(BirtReportFormat format, String reportOutput) {
    RenderOption options = format.createRenderOption();
    options.setOutputFileName(reportOutput);
    return options;
  }

  @SuppressWarnings("unchecked")
  private void runAndValidateTask(IRunAndRenderTask task) throws EngineException, BirtEngineException {
    // Run and render report
    task.run();

    // To get the errors after running, we must call getErrors() BEFORE task.close().
    switch(task.getStatus()) {
    case IEngineTask.STATUS_FAILED:
      List<EngineException> errors = task.getErrors();
      List<String> msgs = new ArrayList<String>();
      for(EngineException e : errors) {
        msgs.add(e.getLocalizedMessage());
      }
      throw new BirtEngineException(msgs);
    default:
    }
  }

  private void configureOsgi(EngineConfig config) throws IOException {
    final File osgiHome = Files.createTempDir();

    Map<String, String> osgiConfig = new HashMap<String, String>();
    osgiConfig.put(OSGI_CONFIGURATION_AREA_PROPERTY_NAME, osgiHome + "/configuration");
    osgiConfig.put(OSGI_INSTANCE_AREA_PROPERTY_NAME, osgiHome + "/workspace");
    config.setOSGiConfig(osgiConfig);
    this.osgiHome = osgiHome;
  }

  /**
   * Binds BIRT's logging output to SLF4J so that logging is consistent with Opal's configuration.
   * @param config
   */
  private void bindLoggingToSlf4J(EngineConfig config) {
    SLF4JBridgeHandler slf4jHandler = new SLF4JBridgeHandler();
    java.util.logging.Logger birtLogger = java.util.logging.Logger.getLogger(EmbeddedBirtEngine.class.getName());
    birtLogger.addHandler(slf4jHandler);
    birtLogger.setUseParentHandlers(false);
    config.setLogger(birtLogger);
  }

}
