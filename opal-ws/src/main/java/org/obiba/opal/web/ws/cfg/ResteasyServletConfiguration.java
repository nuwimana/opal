/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.ws.cfg;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResteasyServletConfiguration {

  public static final String WS_ROOT = "/ws";

  @Autowired
  private ServletContextHandler servletContextHandler;

  @Autowired
  private Dispatcher dispatcher;

  @Autowired
  private ResteasyProviderFactory factory;

  @Autowired
  private Registry registry;

  @PostConstruct
  public void configureResteasyServlet() {
    ServletContext servletContext = servletContextHandler.getServletContext();
    servletContext.setAttribute(ResteasyProviderFactory.class.getName(), factory);
    servletContext.setAttribute(Dispatcher.class.getName(), dispatcher);
    servletContext.setAttribute(Registry.class.getName(), registry);

    ServletHolder sh = new ServletHolder(new HttpServletDispatcher());
    sh.setInitParameter("resteasy.servlet.mapping.prefix", WS_ROOT);
    servletContextHandler.addServlet(sh, WS_ROOT + "/*");
  }

}
