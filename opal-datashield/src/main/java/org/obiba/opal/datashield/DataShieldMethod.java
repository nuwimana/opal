/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datashield;

import org.obiba.opal.r.ROperationWithResult;
import org.rosuda.REngine.REXPRaw;

/**
 * Interface to be implemented by methods that can be executed through datashield
 */
public interface DataShieldMethod {

  public String getName();

  public ROperationWithResult asOperation(REXPRaw argument);

}