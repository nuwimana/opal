/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.project;

public class NoSuchProjectException extends RuntimeException {

  private final String projectName;

  public NoSuchProjectException(String name) {
    super("No project exists with the specified name '" + name + "'");
    this.projectName = name;
  }

  public String getProjectName() {
    return projectName;
  }
}
