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

import com.cadplan.fill_patterns.fileio.TextFile;
import com.vividsolutions.jump.workbench.Logger;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicFillPattern;
import com.vividsolutions.jump.workbench.ui.renderer.style.FillPatternFactory;
import com.vividsolutions.jump.workbench.ui.renderer.style.WKTFillPattern;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.util.Collection;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.awt.*;
import java.net.URL;
import java.net.MalformedURLException;

import org.apache.batik.transcoder.*;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

/**
 * User: geoff
 * Date: 4/07/2007
 * Time: 09:41:24
 * Copyright 2005 Geoffrey G Roy.
 */
public class LoadFillPatterns extends Component {

  PlugInContext context;
  MediaTracker tracker;

  String[] patternNames = null;
  Collection<BasicFillPattern> customFillPatterns;

  public LoadFillPatterns(PlugInContext context) {
    this.context = context;

    tracker = new MediaTracker(this);

    customFillPatterns = (Collection) context.getWorkbenchContext().getWorkbench()
        .getBlackboard().get(FillPatternFactory.CUSTOM_FILL_PATTERNS_KEY, new ArrayList());

    loadNames();

  }

  public void loadNames() {
    File folder = FillPatternsExtension.getFillPatternsFolder(context.getWorkbenchContext());
    if (!folder.exists()) {
      return;
    }
    Logger.debug("Location: " + folder.getAbsolutePath());

    // list allowed file extensions only
    patternNames = folder.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        String lcname = name.toLowerCase();
        return lcname.endsWith(".gif") || lcname.endsWith(".jpeg")
            || lcname.endsWith(".jpg") || lcname.endsWith(".png")
            || lcname.endsWith(".svg") || lcname.endsWith(".wkt");
      }
    });
    if (patternNames == null || patternNames.length == 0) {
      Logger.error("Location: " + folder.getAbsolutePath() + "does not contain any valid patterns.");
      return;
    }
    FillPatternsParams.images = new Image[patternNames.length];
    FillPatternsParams.imageNames = patternNames;
    for (int i = 0; i < patternNames.length; i++) {
      Logger.trace("Loading pattern: " + patternNames[i]);
      if (patternNames[i].toLowerCase().endsWith(".wkt")) {
        TextFile tfile = new TextFile(folder.getPath(), patternNames[i]);
        tfile.openRead();
        String text = tfile.readAll();
        tfile.close();
        StringTokenizer st = new StringTokenizer(text, ":");
        int width = 1, extent = 10;
        String wkt;
        try {
          width = Integer.parseInt(st.nextToken());
          extent = Integer.parseInt(st.nextToken());
          wkt = st.nextToken();

          WKTReader testReader = new WKTReader();
          try {
            Geometry geometry = testReader.read(wkt);
            customFillPatterns.add(new WKTFillPattern(width, extent, wkt));
          } catch (ParseException ex) {
            JOptionPane.showMessageDialog(null, "Error parsing WKT in file: " + patternNames[i] + "\n" + wkt,
                "Error...", JOptionPane.ERROR_MESSAGE);
            System.out.println("Error parsing WKT file: " + patternNames[i] + "\n" + wkt);
          }
        } catch (Exception ex) {
          JOptionPane.showMessageDialog(null, "Error parsing WKT file: " + patternNames[i] + "\n" + text,
              "Error...", JOptionPane.ERROR_MESSAGE);
        }
        Logger.debug("WKT:" + text);

      } else if (patternNames[i].toLowerCase().endsWith(".svg")) {
        String name = patternNames[i];
        Image image = null;
        URL url = null;
        try {
          //url = new URL("file:///" + wd + File.separator + "FillPatterns" + File.separator + patternNames[i]);
          url = new File( folder, patternNames[i]).toURI().toURL();
        } catch (MalformedURLException ex) {
          JOptionPane.showMessageDialog(null, "Error: " + ex, "Error...", JOptionPane.ERROR_MESSAGE);
        }
        Logger.debug("Loading SVG image: " + name);

        SVGRasterizer r = new SVGRasterizer(url);
        int size = 32;
        int k = name.lastIndexOf("_x");
        if (k > 0) {
          int j = name.lastIndexOf(".");
          String ss = name.substring(k + 2, j);
          j = ss.indexOf("x");
          if (j > 0) {
            ss = ss.substring(j + 1);
            try {
              size = Integer.parseInt(ss);
            } catch (NumberFormatException ex) {
              size = 32;
            }
          } else {
            ss = ss.substring(j + 1);
            try {
              size = Integer.parseInt(ss);
            } catch (NumberFormatException ex) {
              size = 32;
            }
          }
        }
        Logger.trace("SVG Image:" + name + "   size=" + size);
        r.setImageWidth(size);
        r.setImageHeight(size);
        //r.setBackgroundColor(java.awt.Color.white);
        try {
          image = r.createBufferedImage();
        } catch (TranscoderException ex) {
          Logger.debug(ex);
        }
        try {
          tracker.addImage(image, 1);
          tracker.waitForID(1);
        } catch (InterruptedException ignored) {
        }

        Logger.debug("Image size: " + image.getWidth(this) + ", " + image.getHeight(this));
        FillPatternsParams.images[i] = image;
        customFillPatterns.add(new MyImageFillPattern(name));
      } else {
        Image image = loadImageIO(new File( folder, patternNames[i]));
            //loadImage( new File( folder, patternNames[i]).getPath() );
        FillPatternsParams.images[i] = image;
        customFillPatterns.add(new MyImageFillPattern(patternNames[i]));
      }
    }
  }

  public Image loadImageIO(File file) {
    try {
      return ImageIO.read(file);
    } catch (IOException e) {
      Logger.error(e);
      return null;
    }
  }

  public Image loadImage(String name) {
    URL url = null;
    Image image;
    try {
      url = new URL("file:///" + name);
    } catch (MalformedURLException ex) {
      JOptionPane.showMessageDialog(null, "Error: " + ex, "Error...", JOptionPane.ERROR_MESSAGE);
    }


    image = Toolkit.getDefaultToolkit().getImage(url);
    try {
      tracker.addImage(image, 1);
      tracker.waitForID(1);
    } catch (InterruptedException ignored) {
    }

    Logger.trace("Image size: " + image.getWidth(this) + ", " + image.getHeight(this));
    if (image.getWidth(this) < 0) image = null;

    return (image);
  }
}
