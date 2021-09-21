package com.cadplan.fileio;


import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.awt.*;
import java.util.Properties;

/**
 * User: geoff
 * Date: 5/04/2006
 * Time: 12:54:42
 * Copyright 2005 Geoffrey G Roy.
 */
public class FileChooser {

  JFileChooser chooser;
  public String fileName;
  public String dirName;

  public FileChooser(Component parent, String defaultDirName, String defaultFileName,
                     String[] filterText, String info, int type) {
    int returnVal;
    File dir;
    Properties props = System.getProperties();
    if (defaultDirName == null) dir = new File(props.getProperty("user.dir"));
    else dir = new File(defaultDirName + File.separator);
    chooser = new JFileChooser();
    chooser.setDialogType(type);

    MyFileFilter filter = new MyFileFilter(info, filterText);

    chooser.setFileFilter(filter);
    chooser.setCurrentDirectory(dir);
    if (defaultFileName != null) chooser.setSelectedFile(new File(defaultFileName));

    if (type == JFileChooser.OPEN_DIALOG) {
      returnVal = chooser.showOpenDialog(parent);
    } else {
      returnVal = chooser.showSaveDialog(parent);
    }

    if (returnVal == JFileChooser.APPROVE_OPTION) {
      fileName = chooser.getSelectedFile().getName();
      if (type == JFileChooser.SAVE_DIALOG) fileName = addExtension(fileName, filterText);
      dirName = chooser.getCurrentDirectory().getPath();
    } else {
      fileName = null;
    }
  }

  public String getFile() {
    return fileName;
  }

  public String getDir() {
    return dirName;
  }

  private String addExtension(String name, String[] filterText) {
    boolean OK = false;
    for (String ext : filterText) {
      if (name.toLowerCase().endsWith("." + ext)) {
        OK = true;
        break;
      }
    }
    if (!OK) name = name + "." + filterText[0];
    return name;
  }
}

class MyFileFilter extends FileFilter {

  String description;
  String[] extensions;
  boolean acceptAll = false;

  public MyFileFilter(String description, String extension) {
    this(description, new String[]{extension});
  }

  public MyFileFilter(String description, String[] extensions) {
    if (description == null) this.description = "Files like: " + extensions[0];
    else this.description = description;
    this.extensions = extensions.clone();
    toLower(this.extensions);
    for (String extension : this.extensions) {
      if (extension.equals("*")) {
        acceptAll = true;
        break;
      }
    }
  }

  private void toLower(String[] sa) {
    for (int i = 0, n = sa.length; i < n; i++) {
      sa[i] = sa[i].toLowerCase();
    }
  }

  public String getDescription() {
    return description;
  }

  public boolean accept(File file) {
    if (file.isDirectory()) return true;
    if (acceptAll) return true;
    else {
      String path = file.getAbsolutePath().toLowerCase();
      for (String extension : extensions) {

        if (path.endsWith(extension) && (path.charAt(path.length() - extension.length() - 1) == '.')) {
          return true;
        }
      }
    }
    return false;
  }
}
