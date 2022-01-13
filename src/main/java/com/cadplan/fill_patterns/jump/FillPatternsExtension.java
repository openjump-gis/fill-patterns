/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2006 Cadplan
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package com.cadplan.fill_patterns.jump;

import java.io.File;

import com.vividsolutions.jump.I18N;
import com.vividsolutions.jump.workbench.Logger;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

/**
 * User: geoff
 * Date: 28/04/2007
 * Time: 09:45:45
 * Copyright 2007 Geoffrey G Roy.
 */
public class FillPatternsExtension extends Extension {
  public static final I18N I18N = com.vividsolutions.jump.I18N.getInstance("com.cadplan.fill_patterns");
  public static final String VERSION = I18N.get("FillPatterns.Version");
  public static final String NAME = I18N.get("FillPatterns.Name");

  public void configure(PlugInContext context) {
    new FillPatternsPlugIn().initialize(context);
  }

  @Override
  public String getVersion() {
    return VERSION;
  }

  @Override
  public String getName() {
    return NAME;
  }

  /**
   * provide the folder where we search/store/expect fill pattern files
   * 
   * @param workbenchcontext
   * @return folder or null (if not found)
   */
  public static File getFillPatternsFolder(WorkbenchContext context) {
    File folder = context.getWorkbench().getPlugInManager()
        .findFileOrFolderInExtensionDirs("FillPatterns");
    if (folder.isDirectory()) {
      return folder;
    } else {
      Logger.error("Cannot find folder 'FillPatterns'!");
    }
    return null;
  }
}
