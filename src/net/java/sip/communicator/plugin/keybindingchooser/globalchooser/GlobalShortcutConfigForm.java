/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.plugin.keybindingchooser.globalchooser;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import net.java.sip.communicator.plugin.keybindingchooser.*;
import net.java.sip.communicator.service.globalshortcut.*;
import net.java.sip.communicator.service.keybindings.*;
import net.java.sip.communicator.util.*;
import net.java.sip.communicator.util.swing.*;

/**
 * This ConfigurationForm shows the list of global shortcut
 *
 * @author Sebastien Vincent
 */
public class GlobalShortcutConfigForm
    extends TransparentPanel
    implements ListSelectionListener
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 0L;

    /**
     * The logger for this class.
     */
    private static Logger logger = Logger.getLogger(
        GlobalShortcutConfigForm.class);

    /**
     * Displays the registered shortcuts.
     */
    private JTable shortcutsTable = new JTable();

    /**
     * Contains the shortcutsTable.
     */
    private JScrollPane scrollPane = new JScrollPane();

    /**
     * Contains listPanel.
     */
    private JPanel mainPanel = this;

    /**
     * Model for the shortcutsTable
     */
    private GlobalShortcutTableModel tableModel =
        new GlobalShortcutTableModel();

    /**
     * Current selected row.
     */
    private int currentRow = -1;

    /**
     * Current selected row.
     */
    private int currentColumn = -1;

    /**
     * Constructor
     */
    public GlobalShortcutConfigForm()
    {
        super(new BorderLayout());
        logger.trace("New global shortcut configuration form.");
        this.initComponents();
    }

    /**
     * Initialize the swing components.
     */
    private void initComponents()
    {
        shortcutsTable.setRowHeight(22);
        shortcutsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        shortcutsTable.setShowHorizontalLines(false);
        shortcutsTable.setShowVerticalLines(false);
        shortcutsTable.setModel(tableModel);
        shortcutsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        shortcutsTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if(e.getClickCount() >= 1)
                {
                    int row = GlobalShortcutConfigForm.this.shortcutsTable.
                        getSelectedRow();
                    int column = GlobalShortcutConfigForm.this.shortcutsTable.
                        getSelectedColumn();

                    if(currentRow != -1  && currentColumn != -1)
                        return;

                    if(row >= 0 && column >= 1)
                    {
                        currentRow = row;
                        currentColumn = column;

                        if(column == 1)
                            GlobalShortcutConfigForm.this.tableModel.getEntryAt(
                                row).setEditShortcut1(true);
                        else if(column == 2)
                            GlobalShortcutConfigForm.this.tableModel.getEntryAt(
                                row).setEditShortcut2(true);
                        else
                            return;

                        KeybindingChooserActivator.getGlobalShortcutService().
                            setEnable(false);
                        refresh();
                        shortcutsTable.setRowSelectionInterval(row, row);
                    }
                }
            }
        });

        shortcutsTable.addKeyListener(new KeyAdapter()
        {
            private KeyEvent buffer = null;

            @Override
            public void keyPressed(KeyEvent event)
            {
                if(currentRow == -1 || currentColumn == -1)
                    return;

                // delete shortcut
                if(event.getKeyCode() == KeyEvent.VK_BACK_SPACE)
                {
                    GlobalShortcutEntry en =
                        GlobalShortcutConfigForm.this.tableModel.getEntryAt(
                            currentRow);
                   List<AWTKeyStroke> kss = new ArrayList<AWTKeyStroke>();
                   if(currentColumn == 1)
                   {
                       kss.add(null);
                       kss.add(en.getShortcut2());
                   }
                   else if(currentColumn == 2)
                   {
                       kss.add(en.getShortcut());
                       kss.add(null);
                   }

                   currentRow = -1;
                   currentColumn = -1;
                   en.setShortcuts(kss);
                   en.setEditShortcut1(false);
                   en.setEditShortcut2(false);
                   GlobalShortcutConfigForm.this.saveConfig();
                   GlobalShortcutConfigForm.this.refresh();
                }
                else
                {
                    // Reports KEY_PRESSED events on release to support
                    // modifiers
                    this.buffer = event;
                }
            }

            @Override
            public void keyReleased(KeyEvent event)
            {
                if (buffer != null)
                {
                    AWTKeyStroke input = KeyStroke.getKeyStrokeForEvent(buffer);
                    buffer = null;

                    if(currentRow != -1)
                    {
                        GlobalShortcutEntry en =
                            GlobalShortcutConfigForm.this.tableModel.getEntryAt(
                                currentRow);
                        List<AWTKeyStroke> kss = new ArrayList<AWTKeyStroke>();

                        if(currentColumn == 1) // shortcut 1
                        {
                            kss.add(input);
                            kss.add(en.getShortcut2());
                        }
                        else if(currentColumn == 2) // shortcut 2
                        {
                            kss.add(en.getShortcut());
                            kss.add(input);
                        }
                        else
                        {
                            return;
                        }

                        en.setShortcuts(kss);
                        en.setEditShortcut1(false);
                        en.setEditShortcut2(false);

                        kss = new ArrayList<AWTKeyStroke>();
                        List<GlobalShortcutEntry> lst = tableModel.getEntries();

                        for(GlobalShortcutEntry e : lst)
                        {
                            boolean isEntry = (e == en);
                            AWTKeyStroke s1 = isEntry &&
                                currentColumn == 1 ? null : e.getShortcut();
                            AWTKeyStroke s2 = isEntry &&
                                currentColumn == 2 ? null : e.getShortcut2();

                            if(s1 != null &&
                                s1.getKeyCode() == input.getKeyCode() &&
                                s1.getModifiers() == input.getModifiers())
                            {
                                kss.add(null);
                                kss.add(e.getShortcut2());
                                e.setShortcuts(kss);
                                break;
                            }
                            else if(s2 != null &&
                                s2.getKeyCode() == input.getKeyCode() &&
                                s2.getModifiers() == input.getModifiers())
                            {
                                kss.add(e.getShortcut());
                                kss.add(null);
                                e.setShortcuts(kss);
                                break;
                            }
                        }

                        currentRow = -1;
                        currentColumn = -1;
                        GlobalShortcutConfigForm.this.saveConfig();
                        GlobalShortcutConfigForm.this.refresh();
                    }
                }
            }
        });

        scrollPane.getViewport().add(this.shortcutsTable);
        mainPanel.add(this.scrollPane,  BorderLayout.CENTER);
        mainPanel.setPreferredSize(new Dimension(500, 400));
        shortcutsTable.getSelectionModel().addListSelectionListener(this);
        loadConfig();
    }

    /**
     * Loads configuration.
     */
    private void loadConfig()
    {
        KeybindingsService keybindingService =
            KeybindingChooserActivator.getKeybindingsService();

        GlobalKeybindingSet set = keybindingService.getGlobalBindings();

        for(Map.Entry<String, List<AWTKeyStroke>> entry :
            set.getBindings().entrySet())
        {
            String key = entry.getKey();
            List<AWTKeyStroke> kss = entry.getValue();
            GlobalShortcutEntry gke = null;
            String desc = null;

            if(key.equals("answer"))
            {
                desc = Resources.getString(
                    "plugin.keybindings.globalchooser.ANSWER_CALL");
            }
            else if(key.equals("hangup"))
            {
                desc = Resources.getString(
                    "plugin.keybindings.globalchooser.HANGUP_CALL");
            }
            else if(key.equals("contactlist"))
            {
                desc = Resources.getString(
                    "plugin.keybindings.globalchooser.SHOW_CONTACTLIST");
            }
            else if(key.equals("mute"))
            {
                desc = Resources.getString(
                    "plugin.keybindings.globalchooser.MUTE_CALLS");
            }
            else
                continue;

            gke = new GlobalShortcutEntry(desc, kss);

            tableModel.addEntry(gke);
        }
        refresh();
    }

    /**
     * Save configuration.
     */
    public void saveConfig()
    {
        KeybindingsService keybindingService =
            KeybindingChooserActivator.getKeybindingsService();
        GlobalShortcutService globalShortcutService =
            KeybindingChooserActivator.getGlobalShortcutService();
        GlobalKeybindingSet globalBindingSet =
            keybindingService.getGlobalBindings();
        Map<String, List<AWTKeyStroke>> gBindings =
            globalBindingSet.getBindings();
        List<GlobalShortcutEntry> entries = tableModel.getEntries();
        List<AWTKeyStroke> kss = null;

        for(GlobalShortcutEntry entry : entries)
        {
            String desc = null;

            if(entry.getAction().equals(Resources.getString(
                "plugin.keybindings.globalchooser.ANSWER_CALL")))
            {
                desc = "answer";
            }
            else if(entry.getAction().equals(Resources.getString(
                "plugin.keybindings.globalchooser.HANGUP_CALL")))
            {
                desc = "hangup";
            }
            else if(entry.getAction().equals(Resources.getString(
                "plugin.keybindings.globalchooser.SHOW_CONTACTLIST")))
            {
                desc = "contactlist";
            }
            else if(entry.getAction().equals(Resources.getString(
                "plugin.keybindings.globalchooser.MUTE_CALLS")))
            {
                desc = "mute";
            }
            else
                continue;

            kss = gBindings.get(desc);
            kss.clear();
            kss.add(entry.getShortcut());
            kss.add(entry.getShortcut2());
            gBindings.put(desc, kss);
        }

        // save in configuration and reload the global shortcuts
        keybindingService.saveGlobalShortcutFromConfiguration();
        globalShortcutService.reloadGlobalShortcuts();
    }

    /**
     * Required by ListSelectionListener.
     *
     * @param e event triggered
     */
    public void valueChanged(ListSelectionEvent e)
    {
    }

    /**
     * refreshes the table display
     */
    private void refresh()
    {
        tableModel.fireTableStructureChanged();
    }

    /**
     * Indicates if this is an advanced configuration form.
     * @return <tt>true</tt> if this is an advanced configuration form,
     * otherwise it returns <tt>false</tt>
     */
    public boolean isAdvanced()
    {
        return true;
    }
}
