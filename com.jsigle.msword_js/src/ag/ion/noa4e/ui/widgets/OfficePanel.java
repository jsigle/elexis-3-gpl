/****************************************************************************
 *                                                                          *
 * NOA (Nice Office Access)                                     						*
 * ------------------------------------------------------------------------ *
 *                                                                          *
 * The Contents of this file are made available subject to                  *
 * the terms of GNU Lesser General Public License Version 2.1.              *
 *                                                                          * 
 * GNU Lesser General Public License Version 2.1                            *
 * ======================================================================== *
 * Copyright 2003-2006 by IOn AG                                            *
 *                                                                          *
 * This library is free software; you can redistribute it and/or            *
 * modify it under the terms of the GNU Lesser General Public               *
 * License version 2.1, as published by the Free Software Foundation.       *
 *                                                                          *
 * This library is distributed in the hope that it will be useful,          *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of           *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 * Lesser General Public License for more details.                          *
 *                                                                          *
 * You should have received a copy of the GNU Lesser General Public         *
 * License along with this library; if not, write to the Free Software      *
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,                    *
 * MA  02111-1307  USA                                                      *
 *                                                                          *
 * Contact us:                                                              *
 *  http://www.ion.ag																												*
 *  http://ubion.ion.ag                                                     *
 *  info@ion.ag                                                             *
 *                                                                          *
 ****************************************************************************/

/*
 * Last changes made by $Author: markus $, $Date: 2010-07-07 10:59:40 +0200 (Mi, 07 Jul 2010) $
 */
package ag.ion.noa4e.ui.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Link;

import ag.ion.bion.officelayer.OSHelper;
import ag.ion.bion.officelayer.application.IOfficeApplication;
import ag.ion.bion.officelayer.desktop.IFrame;
import ag.ion.bion.officelayer.document.IDocument;
import ag.ion.bion.officelayer.document.IDocumentDescriptor;
import ag.ion.bion.workbench.office.editor.core.EditorCorePlugin;
import ag.ion.noa4e.ui.NOAUIPlugin;
import ag.ion.noa4e.ui.operations.AsyncProgressMonitorWrapper;
import ag.ion.noa4e.ui.operations.LoadDocumentOperation;

/**
 * The office panel can be used in order to integrate the OpenOffice.org User
 * Interface into the SWT environment.
 * 
 * @author Andreas Br�ker
 * @version $Revision: 11742 $
 * @date 28.06.2006
 */
public class OfficePanel extends Composite {

	private IOfficeApplication officeApplication = null;

	private IFrame officeFrame = null;
	private StackLayout stackLayout = null;
	private Frame officeAWTFrame = null;
	private ProgressMonitorPart progressMonitorPart = null;

	private Composite baseComposite = null;
	private Composite progressComposite = null;
	private Composite officeComposite = null;

	private IDocument document = null;
	private IStatus lastLoadingStatus = null;

	private Thread loadingThread = null;

	private String currentDocumentPath = null;

	private boolean buildAlwaysNewFrames = false;
	private boolean showProgressIndicator = true;

	// ----------------------------------------------------------------------------
	/**
	 * Constructs new OfficePanel.
	 * 
	 * @param parent
	 *            parent to be used
	 * @param style
	 *            style to be used
	 * 
	 * @author Andreas Br�ker
	 * @date 28.06.2006
	 */
	public OfficePanel(Composite parent, int style) {
		this(parent, style, null);
	}

	// ----------------------------------------------------------------------------
	/**
	 * Constructs new OfficePanel.
	 * 
	 * @param parent
	 *            parent to be used
	 * @param style
	 *            style to be used
	 * @param officeApplication
	 *            the office application to be used, or null to use default
	 * 
	 * @author Markus Kr�ger
	 * @date 08.04.2009
	 */
	public OfficePanel(Composite parent, int style,
			IOfficeApplication officeApplication) {
		super(parent, style);
		System.out
				.println("OfficePanel: OfficePanel - just supered - Constructs new OfficePanel");
		buildControls();
		if (officeApplication == null) {
			officeApplication = EditorCorePlugin.getDefault()
					.getManagedLocalOfficeApplication();
		}
		this.officeApplication = officeApplication;
	}

	// ----------------------------------------------------------------------------
	/**
	 * Returns current office frame. Returns null if an office frame is not
	 * available.
	 * 
	 * @return current office frame or null if an office frame is not available
	 * 
	 * @author Andreas Br�ker
	 * @date 28.06.2006
	 */
	public IFrame getFrame() {
		System.out.println("OfficePanel: getFrame");
		return officeFrame;
	}

	// ----------------------------------------------------------------------------
	/**
	 * Returns current document. Returns null if a document is not available.
	 * 
	 * @return current document. Returns null if a document is not available.
	 * 
	 * @author Markus Kr�ger
	 * @date 19.03.2007
	 */
	public IDocument getDocument() {
		System.out.println("OfficePanel: getDocument");

		if (document == null)
			System.out
					.println("OfficePanel: WARNING: Please note: will return document==null");
		else
			System.out.println("OfficePanel: will return document="
					+ document.toString());

		return document;
	}

	// ----------------------------------------------------------------------------
	/**
	 * Sets focus to the office panel.
	 * 
	 * @author Andreas Br�ker
	 * @date 28.06.2006
	 */
	public boolean setFocus() {
		System.out.println("OfficePanel: setFocus");
		if (officeFrame != null) {
			officeFrame.setFocus();
			return true;
		}
		return super.setFocus();
	}

	// ----------------------------------------------------------------------------
	/**
	 * Sets information whether a new frame should be builded for new loaded
	 * documents. The default value is <code>false</code>.
	 * 
	 * @param buildAlwaysNewFrames
	 *            information whether a new frame should be builded for new
	 *            loaded documents
	 * 
	 * @author Andreas Br�ker
	 * @date 28.06.2006
	 */
	public void setBuildAlwaysNewFrames(boolean buildAlwaysNewFrames) {
		System.out.println("OfficePanel: setBuildAlwaysNewFrames");
		this.buildAlwaysNewFrames = buildAlwaysNewFrames;
	}

	// ----------------------------------------------------------------------------
	/**
	 * Sets information whether a progress indicator should be shown during
	 * document loading. The default value is <code>true</code>.
	 * 
	 * @param showProgressIndicator
	 *            information whether a progress indicator should be shown
	 *            during document loading
	 * 
	 * @author Andreas Br�ker
	 * @date 28.06.2006
	 */
	public void showProgressIndicator(boolean showProgressIndicator) {
		System.out.println("OfficePanel: showProgressIndicator");
		this.showProgressIndicator = showProgressIndicator;
	}

	// ----------------------------------------------------------------------------
	/**
	 * Loads document into the office panel.
	 * 
	 * @param fork
	 *            information whether the loading should be done in an own
	 *            thread
	 * @param documentPath
	 *            path of the document to be loaded
	 * @param documentDescriptor
	 *            document descriptor to be used
	 * 
	 * @author Andreas Br�ker
	 * @date 28.06.2006
	 */
	public final void loadDocument(boolean fork, final String documentPath,
			final IDocumentDescriptor documentDescriptor) {
		System.out.println("OfficePanel: loadDocument");

		if (isDisposed()) {
			System.out
					.println("OfficePanel: loadDocument: WARNING: isDisposed==true; will return immediately.");
			return;
		}

		System.out
				.println("OfficePanel: loadDocument: Status before doing the work:");
		if (documentPath == null)
			System.out.println("OfficePanel: documentPath==null");
		else
			System.out.println("OfficePanel: documentPath=" + documentPath);
		if (currentDocumentPath == null)
			System.out.println("OfficePanel: currentDocumentPath==null");
		else
			System.out.println("OfficePanel: currentDocumentPath="
					+ currentDocumentPath);
		if (document == null)
			System.out.println("OfficePanel: document==null");
		else
			System.out.println("OfficePanel: document=" + document.toString());
		if (officeFrame == null)
			System.out.println("OfficePanel: officeFrame==null");
		else
			System.out.println("OfficePanel: officeFrame="
					+ officeFrame.toString());
		if (lastLoadingStatus == null)
			System.out.println("OfficePanel: lastLoadingStatus==null");
		else
			System.out.println("OfficePanel: lastLoadingStatus="
					+ lastLoadingStatus.toString());

		if (documentPath != null
				&& (currentDocumentPath == null || !currentDocumentPath
						.equals(documentPath))) {
			try {

				System.out
						.println("OfficePanel: loadDocument: Setting currentDocumentPath=documentPath;");

				currentDocumentPath = documentPath;
				if (document != null && buildAlwaysNewFrames) {
					System.out
							.println("OfficePanel: loadDocument: closing currently open document...");
					document.close();
				}

				if (officeFrame == null || buildAlwaysNewFrames) {
					System.out
							.println("OfficePanel: loadDocument: activating new officeFrame...");
					officeFrame = activateNewFrame();
					if (officeFrame == null)
						System.out
								.println("OfficePanel: WARNING: FAILED: still, officeFrame==null");
					else
						System.out.println("OfficePanel: SUCCESS: officeFrame="
								+ officeFrame.toString());
				}

				if (!fork) {
					System.out.println("OfficePanel: fork=false");

					IProgressMonitor progressMonitor = getProgressMonitor();
					if (progressMonitor == null)
						System.out
								.println("OfficePanel: progressMonitor==null");
					else
						System.out.println("OfficePanel: progressMonitor="
								+ progressMonitor.toString());

					if (showProgressIndicator)
						showProgressIndicator();

					System.out
							.println("OfficePanel: loadDocument: loading document...");
					if (documentPath == null)
						System.out.println("OfficePanel: documentPath==null");
					else
						System.out.println("OfficePanel: documentPath="
								+ documentPath.toString());
					if (documentDescriptor == null)
						System.out
								.println("OfficePanel: documentDescriptor==null");
					else
						System.out.println("OfficePanel: documentDescriptor="
								+ documentDescriptor.toString());
					if (progressMonitor == null)
						System.out
								.println("OfficePanel: progressMonitor==null");
					else
						System.out.println("OfficePanel: progressMonitor="
								+ progressMonitor.toString());

					loadDocument(documentPath, documentDescriptor,
							progressMonitor);

					if (document != null)
						lastLoadingStatus = Status.OK_STATUS;

					if (document == null)
						System.out
								.println("OfficePanel: WARNING: FAILED: document==null");
					else
						System.out.println("OfficePanel: SUCCESS: document="
								+ document.toString());

					if (showProgressIndicator) {
						hideProgressIndicator();
						showOfficeFrame();
					}
				} else {
					System.out.println("OfficePanel: fork=true");

					final Display display = Display.getCurrent();
					loadingThread = new Thread() {
						AsyncProgressMonitorWrapper asyncProgressMonitorWrapper = null;

						public void run() {
							display.asyncExec(new Runnable() {
								public void run() {
									if (!isDisposed())
										if (showProgressIndicator)
											showProgressIndicator();
								}
							});

							asyncProgressMonitorWrapper = new AsyncProgressMonitorWrapper(
									getProgressMonitor(), getDisplay());

							try {
								loadDocument(documentPath, documentDescriptor,
										asyncProgressMonitorWrapper);
								if (document != null)
									lastLoadingStatus = Status.OK_STATUS;
								display.asyncExec(new Runnable() {
									public void run() {
										if (showProgressIndicator) {
											hideProgressIndicator();
											showOfficeFrame();
										}
									}
								});
							} catch (CoreException coreException) {
								if (showProgressIndicator) {
									hideProgressIndicator();
									showOfficeFrame();
								}
								lastLoadingStatus = coreException.getStatus();
							}
						}
					};
					loadingThread.start();
				}
			} catch (Throwable throwable) {
				if (showProgressIndicator) {
					hideProgressIndicator();
					showOfficeFrame();
				}
				lastLoadingStatus = new Status(IStatus.ERROR,
						NOAUIPlugin.PLUGIN_ID, IStatus.ERROR,
						throwable.getMessage(), throwable);
			}
		}

		System.out
				.println("OfficePanel: loadDocument: Status after doing the work:");
		if (documentPath == null)
			System.out.println("OfficePanel: documentPath==null");
		else
			System.out.println("OfficePanel: documentPath=" + documentPath);
		if (currentDocumentPath == null)
			System.out.println("OfficePanel: currentDocumentPath==null");
		else
			System.out.println("OfficePanel: currentDocumentPath="
					+ currentDocumentPath);
		if (document == null)
			System.out.println("OfficePanel: document==null");
		else
			System.out.println("OfficePanel: document=" + document.toString());
		if (officeFrame == null)
			System.out.println("OfficePanel: officeFrame==null");
		else
			System.out.println("OfficePanel: officeFrame="
					+ officeFrame.toString());
		if (lastLoadingStatus == null)
			System.out.println("OfficePanel: lastLoadingStatus==null");
		else
			System.out.println("OfficePanel: lastLoadingStatus="
					+ lastLoadingStatus.toString());

	}

	// ----------------------------------------------------------------------------
	/**
	 * Disposes the office panel.
	 * 
	 * @author Andreas Br�ker
	 * @date 28.06.2006
	 */
	public void dispose() {
		System.out.println("OfficePanel: dispose");
		if (officeFrame != null) {
			try {
				officeFrame.close();
			} catch (Throwable throwable) {
				// do not consume
			}
		}
		super.dispose();
	}

	// ----------------------------------------------------------------------------
	/**
	 * Returns status of the last document loading. Returns null if a status is
	 * not available.
	 * 
	 * @return status of the last document loading or null if a status is not
	 *         available
	 * 
	 * @author Andreas Br�ker
	 * @date 28.06.2006
	 */
	public IStatus getLastLoadingStatus() {
		System.out.println("OfficePanel: getLastLoadingStatus");
		return lastLoadingStatus;
	}

	// ----------------------------------------------------------------------------
	/**
	 * Sets the layout which is associated with the receiver to be the argument
	 * which may be null.
	 * 
	 * @param layout
	 *            the receiver's new layout or null
	 * 
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * 
	 * @author Andreas Br�ker
	 * @date 28.06.2006
	 */
	public void setLayout(Layout layout) {
		System.out.println("OfficePanel: setLayout, default: nop");
		// default is to do nothing
	}

	// ----------------------------------------------------------------------------
	/**
	 * Is called after a document loading operation was done. This method can be
	 * overwriten by subclasses in order to do some work after a document
	 * loading operation was done.
	 * 
	 * @author Andreas Br�ker
	 * @date 28.06.2006
	 */
	protected void documentLoadingOperationDone() {
		System.out
				.println("OfficePanel: documentLoadingOperationDone, default: nop");
		// default is to do nothing
	}

	// ----------------------------------------------------------------------------
	/**
	 * Returns progress monitor. Subclasses can overwrite this method in order
	 * to provide their own progress monitor.
	 * 
	 * @return progress monitor
	 * 
	 * @author Andreas Br�ker
	 * @date 28.06.2006
	 */
	protected IProgressMonitor getProgressMonitor() {
		System.out.println("OfficePanel: getProgressMonitor");
		if (progressMonitorPart != null)
			return progressMonitorPart;
		return new NullProgressMonitor();
	}

	// ----------------------------------------------------------------------------
	/**
	 * Starts office application.
	 * 
	 * @param officeApplication
	 *            office application to be started
	 * 
	 * @return information whether the office application was started
	 * 
	 * @author Andreas Br�ker
	 * @date 28.06.2006
	 */
	protected IStatus startOfficeApplication(
			IOfficeApplication officeApplication) {
		
		System.out.println("OfficePanel: StartOfficeApplication");
		
		if (officeApplication == null)
			System.out.println("OfficePanel: WARNING: officeApplication==null");
		else 
			System.out.println("OfficePanel: Please note: officeApplication="+officeApplication.toString());
		
		if (getShell() == null)
			System.out.println("OfficePanel: WARNING: getShell()==null");
		else 
			System.out.println("OfficePanel: Please note: getShell()="+getShell().toString());
		
		
		return NOAUIPlugin.startLocalOfficeApplication(getShell(),officeApplication);
	}

	// ----------------------------------------------------------------------------
	/**
	 * Builds progress indicator. Subclasses can overwrite this method in order
	 * to provide their own progress indicator.
	 * 
	 * @param parent
	 *            parent to be used
	 * 
	 * @author Andreas Br�ker
	 * @date 28.06.2006
	 */
	protected void buildProgressIndicator(Composite parent) {
		System.out.println("OfficePanel: buildProgressIndicator");
		progressComposite = new Composite(parent, SWT.EMBEDDED);
		progressComposite.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_DARK_GRAY));
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginLeft = -5;
		gridLayout.marginBottom = -5;
		gridLayout.marginRight = -5;
		progressComposite.setLayout(gridLayout);

		Composite composite = new Composite(progressComposite, SWT.EMBEDDED);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setBackground(Display.getCurrent().getSystemColor(
				SWT.COLOR_DARK_GRAY));

		Composite progressIndicator = new Composite(progressComposite,
				SWT.EMBEDDED);
		GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
		gridData.verticalAlignment = SWT.CENTER;
		progressIndicator.setLayoutData(gridData);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		progressIndicator.setLayout(gridLayout);
		progressMonitorPart = new ProgressMonitorPart(progressIndicator, null);
		gridData = new GridData(SWT.FILL, SWT.NONE, true, false);
		gridData.verticalAlignment = SWT.CENTER;
		progressMonitorPart.setLayoutData(gridData);

		Link linkCancel = new Link(progressIndicator, SWT.FLAT);
		linkCancel
				.setText("<a>" + Messages.OfficePanel_link_text_cancel + "</a>"); //$NON-NLS-1$ //$NON-NLS-3$
		progressMonitorPart.attachToCancelComponent(linkCancel);
	}

	// ----------------------------------------------------------------------------
	/**
	 * Shows progress indicator. Subclasses can overwrite this method in order
	 * to show their own progress indicator.
	 * 
	 * @author Andreas Br�ker
	 * @date 28.06.2006
	 */
	protected void showProgressIndicator() {
		System.out.println("OfficePanel: showProgressIndicator");
		if (progressComposite == null)
			buildProgressIndicator(baseComposite);
		stackLayout.topControl = progressComposite;
		baseComposite.layout();
	}

	// ----------------------------------------------------------------------------
	/**
	 * Hides the progress indicator. Subclasses can overwrite this method in
	 * order to hide their own progress indicator.
	 * 
	 * @author Andreas Br�ker
	 * @date 28.06.2006
	 */
	protected void hideProgressIndicator() {
		System.out.println("OfficePanel: hideProgressIndicator, default: nop");
		// default is to do nothing
	}

	// ----------------------------------------------------------------------------
	/**
	 * Returns office application.
	 * 
	 * @return office application
	 * 
	 * @author Andreas Br�ker
	 * @author Markus Kr�ger
	 * @date 28.06.2006
	 */
	protected final IOfficeApplication getOfficeApplication() {
		System.out.println("OfficePanel: getOfficeApplication");
		return officeApplication;
	}

	// ----------------------------------------------------------------------------
	/**
	 * Loads document.
	 * 
	 * @param documentPath
	 *            document path to be used
	 * @param documentDescriptor
	 *            document descriptor to be used
	 * @param progressMonitor
	 *            progress monitor to be used
	 * 
	 * @throws CoreException
	 *             if the document can not be loaded
	 * 
	 * @author Andreas Br�ker
	 * @date 28.06.2006
	 */
	private void loadDocument(String documentPath,
			IDocumentDescriptor documentDescriptor,
			IProgressMonitor progressMonitor) throws CoreException {
		System.out.println("OfficePanel: loadDocument begins");
		if (documentPath == null)
			System.out.println("OfficePanel: loadDocument: WARNING: documentPath==null");
		else
			System.out.println("OfficePanel: loadDocument: documentPath=" + documentPath);

		URL url = convertToURL(documentPath);
		System.out.println("OfficePanel: loadDocument: url=" + url.toString());
		try {
			System.out.println("OfficePanel: loadDocument: trying to allocate new LoadDocumentOperation()...");
			LoadDocumentOperation loadDocumentOperation = new LoadDocumentOperation(
					null, getOfficeApplication(), officeFrame, url,
					documentDescriptor);
			if (loadDocumentOperation==null)	System.out.println("OfficePanel: loadDocument: WARNING: loadDocumentOperation==null");
			else								System.out.println("OfficePanel: loadDocument: SUCCESS: loadDocumentOperation= "+loadDocumentOperation.toString());
			
			System.out.println("OfficePanel: loadDocument: trying loadDocumentOperation.run(progressMonitor)...");
			loadDocumentOperation.run(progressMonitor);
			System.out.println("OfficePanel: loadDocument: trying document=loadDocumentOperation.getDocument()...");
			document = loadDocumentOperation.getDocument();
		} catch (InvocationTargetException invocationTargetException) {
			System.out.println("OfficePanel: loadDocument: FAILURE: caught InvocationTargetException during loadDocumentOperation");
			documentLoadingOperationDone();
			throw new CoreException(new Status(IStatus.ERROR,
					NOAUIPlugin.PLUGIN_ID, IStatus.ERROR,
					invocationTargetException.getCause().getMessage(),
					invocationTargetException.getCause()));
		} catch (InterruptedException interruptedException) {
			// the operation was aborted
			System.out.println("OfficePanel: loadDocument: FAILURE: caught InterruptedException during loadDocumentOperation");
		}
		System.out.println("OfficePanel: loadDocument: loadDocumentOperationDone()...");
		documentLoadingOperationDone();
		System.out.println("OfficePanel: loadDocument ends");
	}

	// ----------------------------------------------------------------------------
	/**
	 * Shows office frame.
	 * 
	 * @author Andreas Br�ker
	 * @date 28.06.2006
	 */
	private void showOfficeFrame() {
		System.out.println("OfficePanel: showOfficeFrame: begin");
		
		System.out.println("OfficePanel: showOfficeFrame: baseComposite.isDisposed()="+baseComposite.isDisposed());
		if (officeComposite==null)	System.out.println("OfficePanel: showOfficeFrame: WARNING: officeComposite==null");
		else						System.out.println("OfficePanel: showOfficeFrame: officeComposite="+officeComposite.toString());
		
		if (!baseComposite.isDisposed()) {
			stackLayout.topControl = officeComposite;
			baseComposite.layout();
			officeComposite.layout();
			
		System.out.println("OfficePanel: showOfficeFrame: end");
		}
	}

	// ----------------------------------------------------------------------------
	/**
	 * Builds controls of the office panel.
	 * 
	 * @author Andreas Br�ker
	 * @date 28.06.2006
	 */
	private void buildControls() {
		System.out.println("OfficePanel: buildControls");
		super.setLayout(new GridLayout());
		baseComposite = new Composite(this, SWT.EMBEDDED);
		baseComposite
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		stackLayout = new StackLayout();
		stackLayout.marginHeight = -5;
		stackLayout.marginWidth = -5;
		baseComposite.setLayout(stackLayout);
		baseComposite.setBackground(this.getParent().getBackground());

		if (!showProgressIndicator)
			buildProgressIndicator(this);
	}

	// ----------------------------------------------------------------------------
	/**
	 * Activates a new office frame.
	 * 
	 * @return new builded office frame
	 * 
	 * @throws CoreException
	 *             if a new office frame can not be activated
	 * 
	 * @author Andreas Br�ker
	 * @date 28.06.2006
	 */
	private IFrame activateNewFrame() throws CoreException {
		System.out.println("OfficePanel: activateNewFrame");

		Control oldOfficeComposite = stackLayout.topControl;
		Frame oldOfficeAWTFrame = officeAWTFrame;

		officeComposite = new Composite(baseComposite, SWT.EMBEDDED);
		officeComposite.setBackground(this.getBackground());
		officeComposite.setLayout(new GridLayout());

		try {
			System.out.println("OfficePanel: activateNewFrame: Trying...");

			officeAWTFrame = SWT_AWT.new_Frame(officeComposite);
			officeAWTFrame.setVisible(true);
			officeAWTFrame.setBackground(Color.GRAY);
			Panel officeAWTPanel = new Panel();
			officeAWTPanel.setLayout(new BorderLayout());
			officeAWTPanel.setVisible(true);
			officeAWTFrame.add(officeAWTPanel);

			if (!getOfficeApplication().isActive()) {
				System.out.println("OfficePanel: activateNewFrame: !getOfficeApplication().isActive()...");
				System.out.println("OfficePanel: activateNewFrame: startOfficeApplication(getOfficeApplication)...");
								
				IStatus status = startOfficeApplication(getOfficeApplication());

				if (status==null)	System.out.println("OfficePanel: activateNewFrame: resulting status==null");
				else				System.out.println("OfficePanel: activateNewFrame: resulting status="+status.toString());

				if (status.getSeverity() == IStatus.ERROR) {
					System.out.println("OfficePanel: activateNewFrame: WARNING: status.getSeverity()==IStatus.ERROR");
					throw new CoreException(status);
				}
			}

			if (isDisposed()) {
				System.out.println("OfficePanel: activateNewFrame: isDisposed() ... throwing");
				
				throw new CoreException(new Status(IStatus.ERROR,
						NOAUIPlugin.PLUGIN_ID, IStatus.ERROR,
						"Widget disposed", null)); //$NON-NLS-1$
			}

			IFrame newOfficeFrame = getOfficeApplication().getDesktopService()
					.constructNewOfficeFrame(officeAWTFrame);

			if (oldOfficeAWTFrame != null)
				oldOfficeAWTFrame.dispose();
			if (oldOfficeComposite != null)
				oldOfficeComposite.dispose();

			stackLayout.topControl = officeComposite;
			baseComposite.layout();

			if (newOfficeFrame == null)
				System.out
						.println("OfficePanel: activateNewFrame: WARNING: Returning newOfficeFrame==null");
			else
				System.out
						.println("OfficePanel: activateNewFrame: Returning newOfficeFrame="
								+ newOfficeFrame.toString());

			return newOfficeFrame;
		} catch (Throwable throwable) {
			System.out
					.println("OfficePanel: activateNewFrame: CATCHING - SORRY...");
			throw new CoreException(new Status(IStatus.ERROR,
					NOAUIPlugin.PLUGIN_ID, IStatus.ERROR,
					throwable.getMessage(), throwable));
		}
	}

	// ----------------------------------------------------------------------------
	/**
	 * Converts the submitted document path an URL.
	 * 
	 * @param documentPath
	 *            document path to be used
	 * 
	 * @return converted document path
	 * 
	 * @throws CoreException
	 * 
	 * @author Andreas Br�ker
	 * @date 28.06.2006
	 * 
	 * @author Joerg Sigle
	 * @date 201202192307
	 * @date 201108222324 Modified the code got with noa 2.2.3 from ag.ion:
	 *       Changed the number of slashes for the Windows OS similar to
	 *       previouly discovered modification requirement in the
	 *       OfficePanel.java as downloaded with multiple Elexis code bases for
	 *       building in Windows Eclipse. Please note: Elexis code original has
	 *       5 slashes in Windows Portion; Working js modification to that keeps
	 *       3 slashes in Windows Portion; The original ag.ion version noa
	 *       2.2.3, noa4e 2.0.14 now has only ONE slash in Windows Portion?!?!
	 * 
	 *       See more comments below.
	 */
	private URL convertToURL(String documentPath) throws CoreException {
		System.out
				.println("OfficePanel: convertToURL - modified by js re. Windows part");
		System.out
				.println("OfficePanel: convertToURL: TO DO: Please note that the correction-mod may not be necessary any more in noa4e 2.0.14 (js)");
		System.out.println("OfficePanel: convertToURL: Now trying conversion; if it succeeds, will return immediately thereafter.");

		try {

			/*
			 * if (Debug.DEBUG) { //$NON-NLS-1$ //$NON-NLS-2$ return new
			 * URL("file:/" + documentPath); //$NON-NLS-1$ }
			 */
			// 201108222324 Joerg Sigle http://www.jsigle.com
			// For revision 4960..4974, on win32, Eclipse 3.6.2 Mercurial 1.8
			// JavaSE 1.6 or so.
			// Line +2 after this comment had: "file://///" with 5 slashes,
			// When an OpenOffice document was opened in Elexis,
			// this would cause an error message:
			// ************************************************************
			// Error:
			// URL seems to be an unsupported one.
			// file://///c:/documen~/username/local~/temp/noa1234567890.odt
			// ************************************************************
			// (with similar URL/filename), rendering NOAText unusable.
			// It must have "file:///" with 3 slashes instead.
			// I have not tested the other line (maybe for Linux/Mac systems?)
			// The unchanged code in Elexis probably was:
			//if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
			//     return new URL("file://///" + documentPath); //$NON-NLS-1$
			// And the corrected, working with outdated noa and OO 2.0.3 version
			// in Elexis was:
			//if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1) { //$NON-NLS-1$ //$NON-NLS-2$
			//       return new URL("file:///" + documentPath); //$NON-NLS-1$
			//
			// 201202192311 Joerg Sigle http://www.jsigle.com
			// NOW, with the updated noa 2.2.3, noa4e 2.0.14,
			// freshly obtained from ag.ion, I see NO colon, and only ONE slash
			// in the Windows portion?
			// N.B.: The Linux Portion has 4 slashes in all versions.
			//return new URL("file", "/", documentPath); //$NON-NLS-1$ //$NON-NLS-2$
			// I'm changing it now to:
			//return new URL("file:///" + documentPath); //$NON-NLS-1$ //$NON-NLS-2$
			// PLEASE NOTE, that URL() may have at least two different
			// constructors,
			// so the code found in noa4e 2.0.14 may indeed be ok,
			// because "file", "/", documentPath are supplied as 3 strings
			// so a different constructor may handle them properly.
			// That could mean that ag.ion have corrected the problem I found in
			// the meantime as well.

			if (OSHelper.IS_WINDOWS) {
				return new URL("file:///" + documentPath); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return new URL("file:////" + documentPath); //$NON-NLS-1$
		} catch (Throwable throwable) {
			System.out.println("OfficePanel: convertToURL: FAILURE - catching throwable.");

			throw new CoreException(new Status(IStatus.ERROR,
					NOAUIPlugin.PLUGIN_ID, IStatus.ERROR,
					throwable.getMessage(), throwable));
		}
	}

	// ----------------------------------------------------------------------------

}