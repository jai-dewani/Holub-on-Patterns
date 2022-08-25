package com.jaid.ui;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.print.attribute.HashAttributeSet;
import javax.swing.*;

public final class MenuSite {
    private static JFrame menuFrame = null;
    private static JMenuBar menuBar = null;

    private static Map requesters = new HashMap();

    private static Properties nameMap;

    private static Pattern shortcurExtractor =
            Pattern.compile(
                    "\\s*([^;]+?)\\s*"                // value
                            + "(;\\s*([^\\s].*?))?\\s*$");

    private static Pattern submenuExtractor =
            Pattern.compile("(.*?)(?::(.*?))?"
                    + "(?::(.*?))?"
                    + "(?::(.*?))?"
                    + "(?::(.*?))?"
                    + "(?::(.*?))?"
                    + "(?::(.*?))?");

    private static final LinkedList menuBarContents = new LinkedList();

    private MenuSite(){}

    private static boolean valid() {
        assert menuFrame != null : "MenuSite not established";
        assert menuBar != null : "MenuSite not established";
        return true;
    }

    public synchronized static void establish(JFrame container) {
        assert container != null;
        assert menuFrame == null : "Tried to establish more than one MenuSite";

        menuFrame = container;
        menuFrame.setJMenuBar( menuBar = new JMenuBar());

        assert valid();
    }

    public static void addMenu(Object requester, String menuSpecifier) {
        createSubmenuByName(requester, menuSpecifier);
    }

    public static void addLine(Object requester,
                               String toThisMenu,
                               String name,
                               ActionListener listener) {
        assert requester != null : "null requester";
        assert name != null : "null item";
        assert toThisMenu != null : "null toThisMenu";

        Component element;

        if (name.equals("-"))
            element = new JSeparator();
        else {
            assert listener != null : "null listener";

            JMenuItem lineItem = new JMenuItem(name);
            lineItem.setName(name);
            lineItem.addActionListener(listener);
            setLabelAndShortcut(lineItem);

            element = lineItem;
        }

        JMenu found = createSubmenuByName(requester, toThisMenu);
        if (found == null)
            throw new IllegalArgumentException(
                    "addLine() can't find menu (" + toThisMenu + ")");

        Item item = new Item(element, found, toThisMenu);
        menusAddedBy(requester).add(item);
        item.attachYourselfToYourParent();
    }

    public static void removeMyMenus(Object requester) {
        assert requester != null;
        assert valid();

        Collection allItems = (Collection) (requesters.remove(requester));

        if (allItems != null) {
            Iterator i = allItems.iterator();
            while (i.hasNext()) {
                Item current = (Item) i.next();
                current.detachYourselfFromYourParent();
            }
        }
    }

    public static void setEnable(Object requester, boolean enable) {
        assert requester != null;
        assert valid();

        Collection allItems = (Collection) (requesters.get(requester));

        if (allItems != null) {
            Iterator i = allItems.iterator();
            while (i.hasNext()) {
                Item current = (Item) i.next();
                current.setEnableAttribute(enable);
            }
        }
    }

    public static JMenuItem getMyMenuItem(Object requester,
                                          String menuSpecifier, String name) {
        assert requester != null;
        assert menuSpecifier != null;
        assert valid();

        Collection allItems = (Collection) (requesters.get(requester));

        if (allItems != null) {
            for (Object allItem : allItems) {
                Item current = (Item) allItem;
                if (current.specifiedBy(menuSpecifier)) {
                    if (current.item() instanceof JSeparator)
                        continue;

                    if (name == null && current.item() instanceof JMenu) {
                        return (JMenu) (current.item());
                    }

                    if (((JMenuItem) current.item()).getName().equals(name))
                        return (JMenuItem) current.item();
                }
            }
        }
        return null;
    }

    private static JMenu createSubmenuByName(Object requester,
                                             String menuSpecifier) {
        assert requester != null;
        assert menuSpecifier != null;
        assert valid();

        Matcher m = submenuExtractor.matcher(menuSpecifier);
        if (!m.matches())
            throw new IllegalArgumentException(
                    "Malformed menu specifier.");

        // If it's null, then start the search at the menu bar,
        // otherwise start the search at the menu addressed by "parent"

        JMenuItem child = null;
        MenuElement parent = (MenuElement) menuBar;
        String childName;

        for (int i = 1; (childName = m.group(i++)) != null; parent = child) {
            child = getSubmenuByName(childName, parent.getSubElements());

            if (child != null) {
                if (!(child instanceof JMenu))    // it's a line item!
                    throw new IllegalArgumentException(
                            "Specifier identifes line item, not menu.");
            } else // it doesn't exist, create it
            {
                child = new JMenu(childName);
                child.setName(childName);
                setLabelAndShortcut(child);

                Item item = new Item(child, parent, menuSpecifier);
                menusAddedBy(requester).add(item);
                item.attachYourselfToYourParent();
            }
        }

        return (JMenu) child; // the earlier instanceof guarantees safety
    }

    private static JMenuItem getSubmenuByName(String name,
                                              MenuElement[] contents) {
        JMenuItem found = null;
        for (int i = 0; found == null && i < contents.length; ++i) {
            if (contents[i] instanceof JPopupMenu)
                found = getSubmenuByName(name,
                        ((JPopupMenu) contents[i]).getSubElements());

            else if (((JMenuItem) contents[i]).getName().equals(name))
                found = (JMenuItem) contents[i];
        }
        return found;
    }

    public static void mapNames(URL table) throws IOException {
        if (nameMap == null)
            nameMap = new Properties();
        nameMap.load(table.openStream());
    }

    public static void addMapping(String name, String label,
                                  String shortcut) {
        if (nameMap == null)
            nameMap = new Properties();
        nameMap.put(name, label + ";" + shortcut);
    }

    private static void setLabelAndShortcut(JMenuItem item) {
        String name = item.getName();
        if (name == null)
            return;

        String label;
        if (nameMap != null
                && (label = (String) (nameMap.get(name))) != null) {
            Matcher m = shortcurExtractor.matcher(label);
            if (!m.matches())    // Malformed input line
            {
                item.setText(name);
                Logger.getLogger("com.holub.ui").warning
                        (
                                "Bad "
                                        + "name-to-label map entry:"
                                        + "\n\tinput=[" + name + "=" + label + "]"
                                        + "\n\tSetting label to " + name
                        );
            } else {
                item.setText(m.group(1));

                String shortcut = m.group(3);

                if (shortcut != null) {
                    if (shortcut.length() == 1) {
                        item.setAccelerator
                                (KeyStroke.getKeyStroke
                                        (shortcut.toUpperCase().charAt(0),
                                                Toolkit.getDefaultToolkit().
                                                        getMenuShortcutKeyMask(),
                                                false
                                        )
                                );
                    } else {
                        KeyStroke key = KeyStroke.getKeyStroke(shortcut);
                        if (key != null)
                            item.setAccelerator(key);
                        else {
                            Logger.getLogger("com.holub.ui").warning
                                    ("Malformed shortcut parent specification "
                                            + "in MenuSite map file: "
                                            + shortcut
                                    );
                        }
                    }
                }
            }
        }
    }

    private static Collection menusAddedBy(Object requester) {
        assert requester != null : "Bad argument";
        assert requesters != null : "No requesters";
        assert valid();

        Collection menus = (Collection) (requesters.get(requester));
        if (menus == null) {
            menus = new LinkedList();
            requesters.put(requester, menus);
        }
        return menus;
    }

    private static final class Item {
        // private JMenuItem  item;
        private Component item;

        private String parentSpecification; // of JMenu or of
        // JMenuItem's parent
        private MenuElement parent;                 // JMenu or JMenuBar
        private boolean isHelpMenu;

        public String toString() {
            StringBuffer b = new StringBuffer(parentSpecification);
            if (item instanceof JMenuItem) {
                JMenuItem i = (JMenuItem) item;
                b.append(":");
                b.append(i.getName());
                b.append(" (");
                b.append(i.getText());
                b.append(")");
            }
            return b.toString();
        }

        private boolean valid() {
            assert item != null : "item is null";
            assert parent != null : "parent is null";
            return true;
        }

        public Item(Component item, MenuElement parent,
                    String parentSpecification) {
            assert parent != null;
            assert parent instanceof JMenu || parent instanceof JMenuBar
                    : "Parent must be JMenu or JMenuBar";

            this.item = item;
            this.parent = parent;
            this.parentSpecification = parentSpecification;
            this.isHelpMenu =
                    (item instanceof JMenuItem)
                            && (item.getName().compareToIgnoreCase("help") == 0);

            assert valid();
        }

        public boolean specifiedBy(String specifier) {
            return parentSpecification.equals(specifier);
        }

        public Component item() {
            return item;
        }

        public final void attachYourselfToYourParent() {
            assert valid();

            if (parent instanceof JMenu) {
                ((JMenu) parent).add(item);
            } else if (menuBarContents.size() <= 0) {
                menuBarContents.add(this);
                ((JMenuBar) parent).add(item);
            } else {
                Item last = (Item) (menuBarContents.getLast());
                if (!last.isHelpMenu) {
                    menuBarContents.addLast(this);
                    ((JMenuBar) parent).add(item);
                } else    // remove the help menu, add the new
                {        // item, then put the help menu back
                    // (following the new item).

                    menuBarContents.removeLast();
                    menuBarContents.add(this);
                    menuBarContents.add(last);

                    if (parent == menuBar)
                        parent = regenerateMenuBar();
                }
            }
        }

        public void detachYourselfFromYourParent() {
            assert valid();

            if (parent instanceof JMenu) {
                ((JMenu) parent).remove(item);
            } else // the parent's the menu bar.
            {

                ((JMenuBar)menuBar).remove(item);
                menuBarContents.remove(this);
                regenerateMenuBar(); // without me on it

                parent = null;
            }
        }

        public void setEnableAttribute(boolean on) {
            if (item instanceof JMenuItem) {
                JMenuItem item = (JMenuItem) this.item;
                item.setEnabled(on);
            }
        }

        private JMenuBar regenerateMenuBar() {
            assert valid();

            // Create the new menu bar and populate it from
            // the current-contents list.

            menuBar = new JMenuBar();
            ListIterator i = menuBarContents.listIterator(0);
            while (i.hasNext())
                menuBar.add(((Item) (i.next())).item);

            // Replace the old menu bar with the new one.
            // Calling setVisible causes the menu bar to be
            // redrawn with a minimum amount of flicker. Without
            // it, the redraw doesn't happen at all.

            menuFrame.setJMenuBar(menuBar);
            menuFrame.setVisible(true);
            return menuBar;
        }
    }

    private static class Debug {
        public interface Visitor {
            public void visit(JMenu e, int depth);
        }

        private static int traversalDepth = -1;

        public static void visitPostorder(MenuElement me, Visitor v) {
            // If it's actually a JMenuItem (as compared to a
            // JMenuItem derivative such as a JMenu), then it's
            // a leaf node and has no children.

            if (me.getClass() != JMenuItem.class) {
                MenuElement[] contents = me.getSubElements();
                for (int i = 0; i < contents.length; ++i) {
                    if (contents[i].getClass() != JMenuItem.class) {
                        ++traversalDepth;
                        visitPostorder(contents[i], v);
                        if (!(contents[i] instanceof JPopupMenu))
                            v.visit((JMenu) contents[i], traversalDepth);
                        --traversalDepth;
                    }

                }
            }
        }
    }
}
