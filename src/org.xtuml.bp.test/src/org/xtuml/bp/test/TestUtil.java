package org.xtuml.bp.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.eclipse.compare.internal.CompareDialog;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jgit.util.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.progress.BlockedJobsDialog;
import org.xtuml.bp.core.CorePlugin;
import org.xtuml.bp.core.Ooaofooa;
import org.xtuml.bp.core.common.ModelElement;
import org.xtuml.bp.core.common.NonRootModelElement;
import org.xtuml.bp.core.common.Transaction;
import org.xtuml.bp.core.common.TransactionManager;
import org.xtuml.bp.core.ui.dialogs.ElementSelectionDialog;
import org.xtuml.bp.core.ui.dialogs.ElementSelectionFlatView;
import org.xtuml.bp.test.common.BaseTest;
import org.xtuml.bp.test.common.FailableRunnable;
import org.xtuml.bp.test.common.TestingUtilities;
import org.xtuml.bp.test.common.UITestingUtilities;
import org.xtuml.bp.ui.canvas.Ooaofgraphics;
import org.xtuml.bp.utilities.ui.CanvasUtilities;

import junit.framework.TestCase;

/**
 * Contains utility methods related to automated testing of BridgePoint.
 */
@SuppressWarnings("restriction")
public class TestUtil {
    /**
     * This is used to store the text of a dialog just before it is dismissed. It
     * allows us to validate that the text that was in the dialog is what we
     * expected.
     */
    public static String dialogText;

    /**
     * Asynchronously waits for the given duration, during which time the caller is
     * expected to cause a (likely modal) dialog to be displayed, then dismisses
     * that dialog if it is indeed displayed. If it's not displayed, another attempt
     * will be made through a recursive call, up to a certain number of attempts (in
     * case the dialog is never shown).
     */
    public static void dismissDialog(final long inHowManyMillis) {
        dismissDialog(inHowManyMillis, 0, true);
    }

    /**
     * Cancels (via the Cancel button) a dialog after waiting for the given time
     */
    public static void cancelDialog(final long inHowManyMillis) {
        dismissDialog(inHowManyMillis, 0, false);
    }

    /**
     * Answers no (via the No button) a dialog after waiting for the given time
     */
    public static void noToDialog(final long inHowManyMillis) {
        dismissDialog(inHowManyMillis, 0, false, "&No", null, true);
    }

    public static void dontSaveToDialog(final long inHowManyMillis) {
        dismissDialog(inHowManyMillis, 0, false, "Do&n't Save", null, true);
    }

    /**
     * Answers yes (via the Yes button) a dialog after waiting for the given time
     */
    public static void yesToDialog(final long inHowManyMillis) {
        dismissDialog(inHowManyMillis, 0, false, "&Yes", null, true);
    }

    /**
     * Answers save (via the Save button) a dialog after waiting for the given time
     */
    public static void saveToDialog(final long inHowManyMillis) {
        dismissDialog(inHowManyMillis, 0, false, "&Save", null, true);
    }

    /**
     * Answers OK (via the OK button) a dialog after waiting for the given time
     */
    public static void okToDialog(final long inHowManyMillis) {
        dismissDialog(inHowManyMillis, 0, false, "OK", null, true);
    }

    public static void okToDialog(final long inHowManyMillis, boolean throwException) {
        dismissDialog(inHowManyMillis, 0, false, "OK", null, throwException);
    }

    /**
     * Selects the Next button in the active dialog.
     */
    public static void nextToDialog(final long inHowManyMillis) {
        dismissDialog(inHowManyMillis, 0, false, "&Next >", null, false);
    }

    /**
     * Selects the Finish button in the active dialog
     */
    public static void finishToDialog(final long inHowManyMillis) {
        dismissDialog(inHowManyMillis, 0, false, "&Finish", null, false);
    }

    /**
     * Selects the Finish button in the active dialog
     */
    public static void mergeToDialog(final long inHowManyMillis) {
        dismissDialog(inHowManyMillis, 0, false, "&Merge", null, false);
    }

    /**
     * Selects a tree item in a dialog containing a tree
     */
    public static void selectItemInTreeDialog(final long inHowManyMillis, String treeItem) {
        dismissDialog(inHowManyMillis, 0, false, null, treeItem, false);
    }

    /**
     * Presses Debug in the dialog
     */
    public static void debugToDialog(long inHowManyMillis) {
        dismissDialog(inHowManyMillis, 0, false, "&Debug", null, false);
    }

    /**
     * Select the button on the dialog with the given text
     */
    public static void selectButtonInDialog(final long inHowManyMillis, String buttonName) {
        dismissDialog(inHowManyMillis, 0, false, buttonName, null, true);
    }

    /**
     * Select the button on the dialog with the given text
     */
    public static void selectButtonInDialog(final long inHowManyMillis, String buttonName, boolean throwException) {
        dismissDialog(inHowManyMillis, 0, false, buttonName, null, throwException);
    }

    private static void dismissDialog(final long inHowManyMillis, final int currentRecursionDepth,
            final boolean shouldDismiss) {
        dismissDialog(inHowManyMillis, currentRecursionDepth, shouldDismiss, null, null, true);
    }

    public interface ShellProcessor {
        public boolean processShell(Shell shell);
    }

    static boolean processed = false;
    static Shell[] shellsBeforeAction = new Shell[0];

    public static void dismissShell(ShellProcessor processor) {
        processShell(null, processor);
    }

    public static class ShellProcessorThread extends Thread {

        List<Thread> threads = new ArrayList<Thread>();
        boolean processing = false;

        public ShellProcessorThread(String name) {
            super(name);
        }

        public void addThread(Thread thread) {
            threads.add(thread);
        }

        public void removeThread(Thread thread) {
            threads.remove(thread);
        }

        @Override
        public void run() {
            while (true) {
                while (threads.size() > 0) {
                    Thread next = threads.remove(0);
                    processing = true;
                    next.start();
                    try {
                        next.join();
                    } catch (InterruptedException e) {
                        TestCase.fail(e.getMessage());
                    } finally {
                        processing = false;
                    }
                }
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    TestCase.fail(e.getMessage());
                }
            }
        }

        public boolean isEmpty() {
            return threads.isEmpty() && processing == false;
        }
    }

    public static ShellProcessorThread shellProcessorThread = new ShellProcessorThread("Shell Processing");
    static {
        shellProcessorThread.start();
    }
    public static long maxRunTime = 2000;

    public static void processShell(Shell[] shellsBeforeAction, ShellProcessor processor) {
        if (shellsBeforeAction == null) {
            // cache the current shells, we will use the knowledge that a new
            // one is imminent
            // to determine when and which shell to close
            TestUtil.shellsBeforeAction = PlatformUI.getWorkbench().getDisplay().getShells();
        } else {
            TestUtil.shellsBeforeAction = shellsBeforeAction;
        }
        // run a new thread which expires after 2 seconds
        // this can be increased if anything takes longer than
        // that to display (otherwise it is used for the case
        // where
        Thread processThread = new Thread(() -> {
            long startTime = System.currentTimeMillis();
            long runTime = System.currentTimeMillis() - startTime;
            processed = false;
            while (runTime < maxRunTime) {
                if (PlatformUI.getWorkbench().getDisplay().isDisposed()) {
                    return;
                }
                PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
                    Shell[] currentShells = PlatformUI.getWorkbench().getDisplay().getShells();
                    HashSet<Shell> uniqueSet = new HashSet<>(Arrays.asList(TestUtil.shellsBeforeAction));
                    // locate a unique shell in the latest
                    for (int i = currentShells.length; --i >= 0;) {
                        Shell shell = currentShells[i];
                        boolean added = uniqueSet.add(shell);
                        if (added) {
                            // unique shell, test to make sure it is
                            // not a temporary shell during setup of
                            // the one we expect
                            if (!(shell.getData() instanceof Object[])
                                    && !(shell.getData() instanceof ProgressMonitorDialog)
                                    && !(shell.getData() instanceof BlockedJobsDialog)
                                    && (!shell.getText().equals("") || (shell.getText().equals("")
											&& (shell.getData() instanceof WizardDialog
													|| shell.getData() instanceof CompareDialog)))) {
                                // we have our new shell, process as we
                                // did before
                                processed = processor.processShell(shell);
                                if (processed) {
                                    break;
                                }
                            }
                        }
                    }
                    if (!processed) {
                        TestUtil.shellsBeforeAction = currentShells;
                    }
                });
                if (processed) {
                    return;
                }
                sleep(50);
                runTime = System.currentTimeMillis() - startTime;
            }
        });
        // add thread to processor
        shellProcessorThread.addThread(processThread);
    }

    static Shell[] currentShells = null;
    public static boolean wasCompareDialog = false;
    public static void dismissDialog(final long inHowManyMillis, final int currentRecursionDepth,
            final boolean shouldDismiss, final String button, final String treeItem, final boolean throwException) {
    	wasCompareDialog = false;
        dismissShell(shell -> {
            dialogText = "";
            if(shell.getData() instanceof CompareDialog) {
            	// we do not have any labels are text, so toggle
            	// a boolean for testing
            	wasCompareDialog = true;
            }
            // close the dialog
            Control[] ctrls = ((Dialog) shell.getData()).getShell().getChildren();
            for (int i = 0; i < ctrls.length; i++) {
                Control ctrl = ctrls[i];

                if (ctrl instanceof Label) {
                    dialogText = dialogText + ((Label) ctrl).getText();
                }
            }
            if (shouldDismiss) {
                ((Dialog) shell.getData()).close();
            } else if (button != null) {
                Button foundButton = findButton(shell, button);
                if (foundButton != null) {
                    foundButton.setSelection(true);
                    foundButton.notifyListeners(SWT.Selection, null);
                    return true;
                }
            } else if (treeItem != null) {
                Tree tree = UITestingUtilities.findTree(shell);
                if (tree != null) {
                    TreeItem item = UITestingUtilities.findItemInTree(tree, treeItem);
                    if (item != null) {
                        tree.select(item);
                        return true;
                    } else {
                        CorePlugin.logError("Unable to locate tree item in tree: " + treeItem, null);
                    }
                } else {
                    CorePlugin.logError("Unable to locate a tree in the dialog.", null);
                }
            } else {
                cancelDialog((Dialog) shell.getData());
            }
            return false;
        });
    }

    //
    public static void checkTableItems(final long inHowManyMillis, final int currentRecursionDepth,
            final boolean shouldDismiss, final String actualResultFilePath) {
        dismissShell(shell -> {
            if (shell != null && shell.getData() instanceof Dialog) {
                // close the dialog
                if (!(shell.getData() instanceof ProgressMonitorDialog)) {
                    Control[] ctrls = ((Dialog) shell.getData()).getShell().getChildren();
                    for (int i = 0; i < ctrls.length; i++) {
                        Control ctrl = ctrls[i];

                        if (ctrl instanceof Label) {
                            dialogText = dialogText + ((Label) ctrl).getText();
                        }
                    }
                    if (shouldDismiss) {
                        ((Dialog) shell.getData()).close();
                        return true;
                    } else {
                        TableItem[] items = getTableItems(shell);
                        if (items.length > 0) {
                            String[] actualFirstColumn = new String[items.length];
                            String[] actualSecondColumn = new String[items.length];

                            for (int i = 0; i < items.length; i++) {
                                actualFirstColumn[i] = items[i].getText(0);
                                actualSecondColumn[i] = items[i].getText(1);
                            }
                            String output = createTable(actualFirstColumn, actualSecondColumn);
                            try {
                                FileWriter writer = new FileWriter(actualResultFilePath);
                                writer.write(output);
                                writer.flush();
                                writer.close();
                            } catch (IOException e) {

                                e.printStackTrace();
                            }

                        }
                        Button foundButton = findButton(shell, "OK");
                        if (foundButton != null) {
                            foundButton.setSelection(true);
                            foundButton.notifyListeners(SWT.Selection, null);
                            while (PlatformUI.getWorkbench().getDisplay().readAndDispatch())
                                ;
                            return true;
                        }
                    }
                }
            }
            return false;
        });

    }

    private static String createTable(String[] firstColumn, String[] secondColumn) {
        int shorterLenght;
        int difference = firstColumn.length - secondColumn.length;

        if (difference > 0) {
            shorterLenght = secondColumn.length;
        } else {
            shorterLenght = firstColumn.length;
        }
        String result = "";
        int i = 0;
        for (; i < shorterLenght; i++) {
            result += firstColumn[i] + "\t" + secondColumn[i] + "\r\n";
        }

        return result;
    }

    //
    private static void cancelDialog(Dialog dialog) {
        Control bb = dialog.buttonBar;
        Button cb = findButton(bb.getParent(), "Cancel");
        cb.notifyListeners(SWT.Selection, null);
    }

    public static Button findButton(Composite parent, String buttonName) {
        Control[] child_set = parent.getChildren();
        for (int i = 0; i < child_set.length; ++i) {
            if (child_set[i] instanceof Button) {
                Button cc = (Button) child_set[i];
                String l = cc.getText();
                if (l.equals(buttonName) || l.equals(buttonName.replaceAll("&", ""))) {
                    return cc;
                }
            } else if (child_set[i] instanceof Composite) {
                Button result = findButton((Composite) child_set[i], buttonName);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public static TableItem[] getTableItems(Composite parent) {
        Control[] child_set = parent.getChildren();
        for (int i = 0; i < child_set.length; ++i) {
            if (child_set[i] instanceof ElementSelectionFlatView) {
                ElementSelectionFlatView page = (ElementSelectionFlatView) child_set[i];
                Table table = page.getTable();
                TableItem[] items = table.getItems();
                return items;
            } else if (child_set[i] instanceof Table) {
                return ((Table) child_set[i]).getItems();
            } else if (child_set[i] instanceof Composite) {
                TableItem[] result = getTableItems((Composite) child_set[i]);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;

    }

    /**
     * A shorthand method for telling the current thread to sleep for the given
     * amount of milliseconds.
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Sleeps the current thread for the given amount of milliseconds, but
     * periodically dispatches all pending UI events. Inserting a temporary call to
     * this should "pause" the current test for the given duration, while still
     * allowing the UI to be seen and manipulated during that time, which may be an
     * aid in debugging.
     */
    public static void sleepWithDispatchOfEvents(long millis) {
        // the given duration must be at least as long as our sleep interval,
        // otherwise no sleeping will be done, below
        final int sleepInterval = 10;
        if (millis < sleepInterval)
            millis = sleepInterval;

        // for each interval that makes up the given duration
        for (int i = 0; i < millis / sleepInterval; i++) {
            // sleep for this interval
            sleep(sleepInterval);

            // dispatch any pending UI events
            while (Display.getCurrent().readAndDispatch())
                ;
        }
    }

    /**
     * Copies the given file to a new one at the given destination path. If a file
     * is already at that location, it will be overwritten.
     * 
     * For copying an Eclipse IFile, IFile.create() should be used instead of this.
     */
    public static void copyFile(File file, String destPath) {
        try {
            // open streams on the file and the
            // destination location
            File copy = new File(destPath);
            FileInputStream in = new FileInputStream(file);
            FileOutputStream out = new FileOutputStream(copy);

            // transfer the bytes from the file to the copy
            int c;
            while ((c = in.read()) != -1)
                out.write(c);

            // close the streams
            in.close();
            out.close();
        } catch (IOException e) {
            CorePlugin.logError("Could not copy file", e);
        }
    }

    /**
     * Dispatches outstanding events when progress is complete.
     */
    public static class DispatchOnDoneProgressMonitor extends NullProgressMonitor {
        /**
         * Is set to true when the done() method completes.
         */
        public boolean done = false;

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.core.runtime.IProgressMonitor#done()
         */
        public void done() {
            Display display = Display.getCurrent();
            while (display.readAndDispatch())
                ;

            done = true;
        }
    }

    /**
     * Copies a class file of the given name from the development workspace project
     * of the given name into the given test workspace project.
     */
    public static IFile copyClassFileIntoProject(String className, String copyFromProjectName, IProject toProject) {
        // locate the required file in the development workspace
        File workspaceSource = TestingUtilities.getSourceDirectory().toFile().getParentFile();
        File file = new File(workspaceSource, copyFromProjectName + "/bin/lib/" + className + ".class");
        IFile resource = toProject.getFile("/bin/lib/" + file.getName());
        File copyFile = resource.getLocation().toFile();
        // copy the test file into the given project (and wait
        // until the resulting model-events have been dispatched,
        // before proceeding)
        copyFile(file, copyFile.getAbsolutePath());
        try {
            FileInputStream stream = new FileInputStream(file);
            DispatchOnDoneProgressMonitor monitor = new DispatchOnDoneProgressMonitor();
            resource.create(stream, true, monitor);
            while (!monitor.done)
                TestUtil.sleep(10);
            stream.close();
        } catch (Exception e) {
            CorePlugin.logError("Could not copy test class into project. reason: ", e);
        }
        return resource;
    }

    /**
     * A convenience method for opening the given project.
     */
    public static void openProject(IProject project) {
        try {
            project.open(new NullProgressMonitor());
        } catch (CoreException e) {
            CorePlugin.logError("Could not open project", e);
        }
    }

    /**
     * A convenience method for closing the given project.
     */
    public static void closeProject(IProject project) {
        try {
            project.close(new NullProgressMonitor());
        } catch (CoreException e) {
            CorePlugin.logError("Could not close project", e);
        }
    }

    /**
     * A convenience method for closing the given project.
     */
    public static void deleteProject(IProject project) {
        try {
            project.delete(false, true, new NullProgressMonitor());
        } catch (CoreException e) {
            CorePlugin.logError("Could not delete project", e);
        }
    }

    /**
     * Makes the BridgePoint perspective the one that is currently active within the
     * Eclipse IDE. Returns the page on which the perspective is shown.
     */
    public static IWorkbenchPage showBridgePointPerspective() {
        return CanvasUtilities.showBridgePointPerspective();
    }

    /**
     * Is the multi-valued return value of createTestProjectAndImportModel(), below.
     */
    public static class Result1 {
        public Ooaofooa modelRoot;
        public IProject project;
        public IFile file;
    }

    /**
     * Changes the given file's readonly status
     * 
     * @param readonly
     *            - A boolean used to determine what status to set the file to
     * @param modelFile
     *            - The file in which the status should be altered
     */
    public static void changeFileReadonlyStatus(boolean readonly, IFile modelFile) {
        ResourceAttributes resourceAttributes = modelFile.getResourceAttributes();
        if (resourceAttributes != null) {
            resourceAttributes.setReadOnly(readonly);
            try {
                modelFile.setResourceAttributes(resourceAttributes);
            } catch (CoreException e) {
                CorePlugin.logError("Core Exception", e);
            }
        }
        BaseTest.dispatchEvents();
    }

    /**
     * Returns the concatenation of the given array of strings into one string, with
     * each of the smaller strings on a new line.
     * 
     * Note that any line-feed characters within the strings will be stripped out
     * before writing, to be consistent for comparison purposes with what this
     * class's writeToFile() method does.
     */
    public static String join(String[] strings) {
        // for each string in the given array
        StringBuffer buffer = new StringBuffer();
        String lineSeparator = System.getProperty("line.separator");
        for (int i = 0; i < strings.length; i++) {
            // strip out any line-feeds from this string, to be consistent
            // with what this class's writeToFile() does, in case a result
            // from this method is compared with one of that method
            String string = strings[i].replaceAll("\n", "");

            // append this string to the joined string we are building
            if (i > 0)
                buffer.append(lineSeparator);
            buffer.append(string);
        }

        return buffer.toString();
    }

    /**
     * Returns the result of joining the two arrays of strings together into one
     * array.
     */
    public static String[] join(String[] strings1, String[] strings2) {
        // if the second array is empty, just return the first
        if (strings2.length == 0)
            return strings1;

        // for each string in the given two arrays
        String[] result = new String[strings1.length + strings2.length];
        for (int i = 0; i < strings1.length + strings2.length; i++) {
            // put this string into our result, at the right place
            result[i] = (i < strings1.length) ? strings1[i] : strings2[i - strings1.length];
        }

        return result;
    }

    /**
     * Returns the contents of the given text file as a string.
     */
    public static String getTextFileContents(File file) {
        return getTextFileContents(file, true);
    }

    public static String getTextFileContents(File file, boolean trim) {
        try {
            // open a reader on the file
            FileInputStream in = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            // keep doing this
            StringBuffer contents = new StringBuffer();
            String lineSeparator = System.getProperty("line.separator");
            int linesRead = 0;
            while (true) {
                // get the next line of text from the file
                String line = reader.readLine();

                // if we've hit the end of the file, we're done
                if (line == null)
                    break;

                // add the line to the results string we're building
                if (linesRead > 0)
                    contents.append(lineSeparator);
                if (trim) {
                    contents.append(line.trim());
                } else {
                    contents.append(line);
                }

                linesRead++;
            }

            reader.close();

            return contents.toString();
        } catch (IOException e) {
            TestCase.fail(e.getLocalizedMessage());
            return null;
        }
    }

    /**
     * Writes the given array of strings out to a text file of the given name
     * (including path), one string per line.
     * 
     * Note that any line-feed characters within the strings will be stripped out
     * before writing, as they tend to complicate comparisons of the contents of the
     * file after they are read back in.
     * 
     * @return Whether the write was successful.
     */
    public static boolean writeToFile(String[] strings, String pathName) {
        try {
            // get the current file contents for comparison
            // if there is no difference then we don't need
            // to update the result
            File resultFile = new File(pathName);
            if (resultFile.exists()) {
                byte[] fileBytes = new byte[(int) resultFile.length()];
                FileInputStream fis = new FileInputStream(resultFile);
                fis.read(fileBytes);
                fis.close();
                String fileContents = new String(fileBytes);
                String[] currentContents = null;
                if (fileContents.indexOf("\r") != -1) {
                    currentContents = fileContents.split("\r\n");
                } else {
                    currentContents = fileContents.split("\n");
                }

                if (stringArraysAreEqual(currentContents, strings)) {
                    return false;
                }
            }

            // create the file of the given path and name
            FileWriter writer = new FileWriter(pathName);

            // for each element in the given strings array
            String lineSeparator = System.getProperty("line.separator");
            for (int i = 0; i < strings.length; i++) {
                // strip out any line-feeds from this string, as they
                // will cause it to be treated as two or more separate
                // strings when it is read back in, which screws up
                // comparisons
                String string = strings[i].replaceAll("\n", "");

                // write this string out to a new line in the text file
                if (i > 0)
                    writer.write(lineSeparator);
                writer.write(string);
            }

            writer.flush();
            writer.close();
        } catch (Exception e) {
            CorePlugin.logError("Could not write strings to text file", e);
            return false;
        }

        return true;
    }

    private static boolean stringArraysAreEqual(String[] currentContents, String[] strings) {
        // if the lengths are different then they are
        // not equal
        if (currentContents.length != strings.length) {
            return false;
        }
        // otherwise compare each array value
        for (int i = 0; i < currentContents.length; i++) {
            String string = strings[i].replaceAll("\n", "");
            String currentString = currentContents[i].replaceAll("\n", "");
            if (!currentString.equals(string)) {
                return false;
            }
        }
        return true;
    }

    public static FailableRunnable chooseItemInDialog(final int sleep, final String item, Shell[] existingShells) {
        return chooseItemInDialog(sleep, item, false, existingShells);
    }

    public static FailableRunnable chooseItemInDialog(final int sleep, final String item, final boolean locateOnly,
            Shell[] existingShells) {
        return chooseItemInDialog(sleep, item, locateOnly, false, existingShells);
    }

    public static FailableRunnable chooseItemInDialog(final int sleep, final String item, final boolean locateOnly,
            final boolean testNonExistence, Shell[] existingShells) {
        return chooseItemInDialog(sleep, null, new String[] { item }, locateOnly, testNonExistence, existingShells);
    }

    public static FailableRunnable toggleButtonInElementSelectionDialog(final int sleep, final String buttonName,
            Shell[] existingShells) {
        return toggleButtonInElementSelectionDialog(sleep, null, buttonName, existingShells);
    }

    public static void cancelElementSelectionDialog(final int sleep, Shell[] existingShells) {
        cancelElementSelectionDialog(sleep, null, existingShells);
    }

    public static void cancelElementSelectionDialog(final int sleep, final FailableRunnable waitRunnable,
            Shell[] existingShells) {
        Thread cancelThread = new Thread(new Runnable() {

            @Override
            public void run() {
                if (waitRunnable != null) {
                    waitRunnable.join();
                }
                PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

                    @Override
                    public void run() {
                        processShell(existingShells, shell -> {
                            if (shell != null && shell.getData() instanceof ElementSelectionDialog) {
                                ElementSelectionDialog dialog = (ElementSelectionDialog) shell.getData();
                                dialog.close();
                            }
                            return true;
                        });
                    }
                });
            }
        });
        cancelThread.start();
    }

    protected static void waitForRunnable(FailableRunnable waitRunnable) {
        // if there is a runnable given, wait for
        // it to complete
        if (waitRunnable != null) {
            // wait for a max of 60s
            int sleepTime = 0;
            while (!waitRunnable.getComplete()) {
                if (sleepTime > 60000) {
                    break;
                }
                sleep(1000);
                sleepTime = sleepTime + 1000;
            }
        }
    }

    public static void okElementSelectionDialog(final FailableRunnable runnable, Shell[] existingShells) {
        Thread cancelThread = new Thread(new Runnable() {

            @Override
            public void run() {
                waitForRunnable(runnable);
                processShell(existingShells, shell -> {
                    if (shell != null) {
                        ElementSelectionDialog dialog = (ElementSelectionDialog) shell.getData();
                        if (dialog.getOkButton().isEnabled()) {
                            dialog.getOkButton().notifyListeners(SWT.Selection, new Event());

                        }
                    }
                    return true;
                });
            }
        });
        cancelThread.start();
    }

    public static FailableRunnable chooseItemInDialog(FailableRunnable runnable, String item, boolean locateOnly,
            Shell[] existingShells) {
        return chooseItemInDialog(0, runnable, new String[] { item }, locateOnly, false, existingShells);
    }

    public static FailableRunnable chooseItemInDialog(final int sleep, final FailableRunnable waitRunnable,
            final String item, final boolean locateOnly, final boolean testNonExistence, Shell[] existingShells) {
    	String[] items = new String[1];
    	items[0] = item;
        return chooseItemInDialog(sleep, waitRunnable, items, locateOnly, testNonExistence, existingShells);
    }

    static int foundItemInDialog = 0;

    public static FailableRunnable chooseItemInDialog(final int sleep, final FailableRunnable waitRunnable,
            final String[] targetItems, final boolean locateOnly, final boolean testNonExistence,
            Shell[] existingShells) {
        FailableRunnable runnable = new FailableRunnable() {
            @Override
            public void run() {
                sleep(sleep);
                waitForRunnable(waitRunnable);
                FailableRunnable innerRunnable = new FailableRunnable() {
                    @Override
                    public void run() {
                        processShell(existingShells, shell -> {
                            List<String> foundItems = new ArrayList<>();
                            List<String> notFoundItems = new ArrayList<>(Arrays.asList(targetItems));
                            if (shell != null) {
                                Dialog dialog = (Dialog) shell.getData();
                                Control[] children = dialog.getShell().getChildren();
                                for (int i = 0; i < children.length; i++) {
                                    Table table = findTable(children);
                                    if (table != null) {
                                        // if a deselect all button is present
                                        // press it before selecting the desired
                                        // item
                                        Button deselect = findButton(shell, "&Deselect All");
                                        if (deselect != null) {
                                            deselect.notifyListeners(SWT.Selection, new Event());
                                            while (PlatformUI.getWorkbench().getDisplay().readAndDispatch())
                                                ;
                                        }
                                        TableItem[] items = table.getItems();
                                        for (String item : targetItems) {
                                            for (int j = 0; j < items.length; j++) {
                                                if (items[j].getText().equals(item)) {
                                                    // do not select if locateOnly is true
                                                    if (!locateOnly) {
                                                        List<TableItem> currentSelection = new ArrayList<>(
                                                                Arrays.asList(table.getSelection()));
                                                        currentSelection.add(items[j]);
                                                        table.setSelection(currentSelection.toArray(new TableItem[0]));
                                                        Event event = new Event();
                                                        event.item = items[j];
                                                        table.notifyListeners(SWT.Selection, event);
                                                        while (PlatformUI.getWorkbench().getDisplay().readAndDispatch())
                                                            ;
                                                    }
                                                    foundItemInDialog++;
                                                    foundItems.add(item);
                                                    notFoundItems.remove(item);
                                                    break;
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                            if (testNonExistence) {
                                if (foundItemInDialog == targetItems.length) {
                                    setFailure("Found the unexpected item(s) in the selection dialog ("
                                            + StringUtils.join(foundItems, ", ") + ").");
                                }
                            } else {
                                if (foundItemInDialog != targetItems.length) {
                                    setFailure("Could not locate the expected item(s) in the selection dialog ("
                                            + StringUtils.join(notFoundItems, ", ") + ").");
                                }
                            }
                            foundItemInDialog = 0;
                            setComplete();
                            return true;
                        });
                    }

                    private Table findTable(Control[] children) {
                        for (Control child : children) {
                            if (child instanceof Table) {
                                return (Table) child;
                            } else if (child instanceof Composite) {
                                Table result = findTable(((Composite) child).getChildren());
                                if (result != null) {
                                    return result;
                                }
                            }
                        }
                        return null;
                    }
                };
                if (!PlatformUI.getWorkbench().getDisplay().isDisposed()) {
                    // must be run in the UI thread
                    PlatformUI.getWorkbench().getDisplay().syncExec(innerRunnable);
                    waitForRunnable(innerRunnable);
                    if (!innerRunnable.getFailure().equals("")) {
                        setFailure(innerRunnable.getFailure());
                    }
                }
                setComplete();
            }
        };
        Thread chooserThread = new Thread(runnable);
        chooserThread.start();
        return runnable;
    }

    public static FailableRunnable toggleButtonInElementSelectionDialog(final int sleep,
            final FailableRunnable waitRunnable, final String buttonName, Shell[] existingShells) {
        FailableRunnable runnable = new FailableRunnable() {

            @Override
            public void run() {
                sleep(sleep);
                waitForRunnable(waitRunnable);
                FailableRunnable innerRunnable = new FailableRunnable() {

                    @Override
                    public void run() {
                        processShell(existingShells, shell -> {
                            if (shell != null) {
                                ElementSelectionDialog dialog = (ElementSelectionDialog) shell.getData();
                                ElementSelectionFlatView view = dialog.getFlatView();
                                Control[] children = view.getChildren();
                                for (int i = 0; i < children.length; i++) {
                                    if (children[i] instanceof Button) {
                                        Button button = (Button) children[i];
                                        if (button.getText().equals(buttonName)) {
                                            button.setSelection((button.getSelection()) ? false : true);
                                            button.notifyListeners(SWT.Selection, new Event());
                                            view.redraw();
                                            view.update();
                                            setComplete();
                                            while (PlatformUI.getWorkbench().getDisplay().readAndDispatch())
                                                ;
                                            return true;
                                        }
                                    }
                                }
                                // if we get here add an error to the thread, as
                                // the button could not be found
                                setFailure("Unable to locate button in selection dialog: " + buttonName);
                                setComplete();
                            }
                            return true;
                        });
                    }
                };
                // must be run in the UI thread
                PlatformUI.getWorkbench().getDisplay().syncExec(innerRunnable);
                if (!innerRunnable.getFailure().equals("")) {
                    setFailure(innerRunnable.getFailure());
                }
                setComplete();
            }
        };
        Thread chooserThread = new Thread(runnable);
        chooserThread.start();
        return runnable;
    }

    public static void executeInTransaction(NonRootModelElement element, String method, Object[] parameters) {
        executeInTransaction(element, method, parameters, true);
    }

    public static void executeInTransaction(NonRootModelElement element, String method, Object[] parameters,
            boolean undoable) {
        Class<?>[] paramClasses = new Class<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i] instanceof Integer) {
                paramClasses[i] = Integer.TYPE;
            } else if (parameters[i] instanceof Boolean) {
                paramClasses[i] = Boolean.TYPE;
            } else {
                paramClasses[i] = parameters[i].getClass();
            }
        }
        Transaction transaction = null;
        TransactionManager manager = TransactionManager.getSingleton();
        try {
            transaction = manager.startTransaction("test transaction",
                    new ModelElement[] { Ooaofooa.getDefaultInstance(), Ooaofgraphics.getDefaultInstance() }, undoable);
            Method m = element.getClass().getMethod(method, paramClasses);
            m.invoke(element, parameters);
            manager.endTransaction(transaction);
        } catch (Exception e) {
            if (transaction != null) {
                manager.cancelTransaction(transaction, e);
            }
            CorePlugin.logError("Unable to complete transaction.", e);
        }
        BaseTest.dispatchEvents(200);
    }

}
