/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hop.ui.core.widget;

import org.apache.hop.core.Const;
import org.apache.hop.core.extension.ExtensionPointHandler;
import org.apache.hop.core.extension.HopExtensionPoint;
import org.apache.hop.core.logging.LogChannel;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.variables.VariableRegistry;
import org.apache.hop.core.variables.VariableScope;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.ui.core.FormDataBuilder;
import org.apache.hop.ui.core.PropsUi;
import org.apache.hop.ui.core.gui.GuiResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ControlSpaceKeyAdapter extends KeyAdapter {

  private static final Class<?> PKG = ControlSpaceKeyAdapter.class; // For Translator

  private static final PropsUi props = PropsUi.getInstance();

  private final IGetCaretPosition getCaretPositionInterface;

  private final IInsertText insertTextInterface;

  private IVariables variables;

  private final Control control;

  /**
   * @param variables IVariables object
   * @param control a Text or CCombo box object
   */
  public ControlSpaceKeyAdapter(final IVariables variables, final Control control) {
    this(variables, control, null, null);
  }

  /**
   * @param variables IVariables object
   * @param control a Text or CCombo box object
   * @param getCaretPositionInterface
   * @param insertTextInterface
   */
  public ControlSpaceKeyAdapter(
      IVariables variables,
      final Control control,
      final IGetCaretPosition getCaretPositionInterface,
      final IInsertText insertTextInterface) {

    this.variables = variables;
    this.control = control;
    this.getCaretPositionInterface = getCaretPositionInterface;
    this.insertTextInterface = insertTextInterface;
  }

  /**
   * in chinese window, Ctrl-SPACE is reversed by system for input chinese character. use
   * Ctrl-ALT-SPACE instead.
   *
   * @param e the keyevent
   * @return true when ctrl-SPACE is pressed
   */
  private boolean isHotKey(KeyEvent e) {
    if (System.getProperty("user.language").equals("zh")) {
      return e.character == ' '
          && ((e.stateMask & SWT.CONTROL) != 0)
          && ((e.stateMask & SWT.ALT) != 0);
    } else if (OsHelper.isMac()) {
      // character is empty when pressing special key in macOs
      return e.keyCode == 32
          && ((e.stateMask & SWT.CONTROL) != 0)
          && ((e.stateMask & SWT.ALT) == 0);
    } else {
      return e.character == ' '
          && ((e.stateMask & SWT.CONTROL) != 0)
          && ((e.stateMask & SWT.ALT) == 0);
    }
  }

  @Override
  public void keyPressed(KeyEvent e) {
    // CTRL-<SPACE> --> Insert a variable
    if (isHotKey(e)) {
      e.doit = false;

      // textField.setData(TRUE) indicates we have transitioned from the textbox to list mode...
      // This will be set to false when the list selection has been processed
      // and the list is being disposed of.
      control.setData(Boolean.TRUE);

      final int position;
      if (getCaretPositionInterface != null) {
        position = getCaretPositionInterface.getCaretPosition();
      } else {
        position = -1;
      }

      // Drop down a list of variables...
      //
      Rectangle bounds = control.getBounds();
      Point location = GuiResource.calculateControlPosition(control);

      final Shell shell = new Shell(control.getShell(), SWT.NONE);
      shell.setSize(bounds.width > 300 ? bounds.width : 300, 200);
      shell.setLocation(location.x, location.y + bounds.height);
      shell.setLayout(new FormLayout());
      final List list = new List(shell, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
      PropsUi.setLook(list);
      list.setLayoutData(new FormDataBuilder().fullSize().result());
      list.setItems(getVariableNames(variables));
      final ToolTip toolTip = new ToolTip(list.getShell(), SWT.BALLOON);
      toolTip.setAutoHide(true);

      list.addSelectionListener(
          new SelectionAdapter() {
            // Enter or double-click: picks the variable
            //
            @Override
            public synchronized void widgetDefaultSelected(SelectionEvent e) {
              applyChanges(shell, list, control, position, insertTextInterface);
            }

            // Select a variable name: display the value in a tool tip
            //
            @Override
            public void widgetSelected(SelectionEvent event) {
              if (list.getSelectionCount() <= 0) {
                return;
              }
              String name = list.getSelection()[0];
              String value = variables.getVariable(name);
              Rectangle shellBounds = shell.getBounds();
              String message =
                  BaseMessages.getString(PKG, "TextVar.VariableValue.Message", name, value);
              if (name.startsWith(Const.INTERNAL_VARIABLE_PREFIX)) {
                message += BaseMessages.getString(PKG, "TextVar.InternalVariable.Message");
              }
              toolTip.setText(message);
              toolTip.setVisible(false);
              toolTip.setLocation(
                  shell.getLocation().x, shell.getLocation().y + shellBounds.height);
              toolTip.setVisible(true);
            }
          });

      list.addKeyListener(
          new KeyAdapter() {

            @Override
            public synchronized void keyPressed(KeyEvent e) {
              if (e.keyCode == SWT.CR
                  && ((e.keyCode & SWT.CONTROL) == 0)
                  && ((e.keyCode & SWT.SHIFT) == 0)) {
                applyChanges(shell, list, control, position, insertTextInterface);
              }
            }
          });

      list.addFocusListener(
          new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent event) {
              shell.dispose();
              if (!control.isDisposed()) {
                control.setData(Boolean.FALSE);
              }
            }
          });

      shell.open();
    }
  }

  private static void applyChanges(
      Shell shell, List list, Control control, int position, IInsertText insertTextInterface) {
    String selection =
        list.getSelection()[0].contains(Const.getDeprecatedPrefix())
            ? list.getSelection()[0].replace(Const.getDeprecatedPrefix(), "")
            : list.getSelection()[0];
    String extra = "${" + selection + "}";
    if (insertTextInterface != null) {
      insertTextInterface.insertText(extra, position);
    } else {
      if (control.isDisposed()) {
        return;
      }

      if (list.getSelectionCount() <= 0) {
        return;
      }
      if (control instanceof Text) {
        ((Text) control).insert(extra);
      } else if (control instanceof CCombo) {
        CCombo combo = (CCombo) control;
        combo.setText(
            extra); // We can't know the location of the cursor yet. All we can do is overwrite.
      } else if (control instanceof StyledTextComp) {
        ((StyledTextComp) control).insert(extra);
      }
    }
    if (!shell.isDisposed()) {
      shell.dispose();
    }
    if (!control.isDisposed()) {
      control.setData(Boolean.FALSE);
    }
  }

  public static String[] getVariableNames(IVariables variables) {
    // Deprecated variables will be displayed with the suffix (deprecated).
    String[] variableNames = variables.getVariableNames();
    for (int i = 0; i < variableNames.length; i++) {
      for (String deprecatedName : VariableRegistry.getInstance().getDeprecatedVariableNames()) {
        if (variableNames[i].equals(deprecatedName)) {
          variableNames[i] = variableNames[i] + Const.getDeprecatedPrefix();
          break;
        }
      }
    }

    // Get the system properties to sort 'm at the back...
    //
    Properties systemProperties = System.getProperties();

    // The internal Hop variables...
    //
    Set<String> hopVariablesSet = VariableRegistry.getInstance().getVariableNames();

    // The Deprecated variables...
    Set<String> deprecatedSet = new HashSet<>(VariableRegistry.getInstance().getDeprecatedVariableNames());

    // The Hop system settings variables
    //
    Set<String> hopSystemSettings = VariableRegistry.getInstance().getVariableNames(VariableScope.SYSTEM);        

    Map<String, String> pluginsPrefixesMap = new HashMap<>();

    try {
      ExtensionPointHandler.callExtensionPoint(
          LogChannel.UI,
          variables,
          HopExtensionPoint.HopGuiGetControlSpaceSortOrderPrefix.name(),
          pluginsPrefixesMap);
    } catch (Exception e) {
      LogChannel.UI.logError(
          "Error calling extension point 'HopGuiGetControlSpaceSortOrderPrefix'", e);
    }

    Arrays.sort(variableNames);
    return variableNames;
  }

  /**
   * Get a prefix to steer sorting of variables. Please note that variables can appear in multiple
   * sets so we check back to front.
   *
   * @param variableName The variable name to prefix
   * @param systemProperties
   * @param hopVariablesSet
   * @param deprecatedSet
   * @return a prefixed variable name
   */
  private static String addPrefix(
      String variableName,
      Properties systemProperties,
      Set<String> hopVariablesSet,
      Set<String> deprecatedSet,
      Set<String> hopSystemSettings,
      Map<String, String> pluginsPrefixesMap) {
    String prefix = "300_";
    String systemValue = systemProperties.getProperty(variableName);
    if (systemValue != null) {
      // var1 is a system property... push it to the very end..
      prefix = "900_";
    }
    if (hopVariablesSet.contains(variableName)) {
      prefix = "800_";
    }
    if (hopSystemSettings.contains(variableName)) {
      prefix = "700_";
    }
    if (deprecatedSet.contains(variableName)) {
      prefix = "600_";
    }
    if (variableName.startsWith(Const.INTERNAL_VARIABLE_PREFIX)) {
      prefix = "500_";
    }
    // Finally allow plugins to override a sort order...
    //
    String pluginPrefix = pluginsPrefixesMap.get(variableName);
    if (pluginPrefix != null) {
      prefix = pluginPrefix;
    }
    return prefix;
  }

  public void setVariables(IVariables vars) {
    variables = vars;
  }
}
