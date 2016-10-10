/* FeatureIDE - A Framework for Feature-Oriented Software Development
 * Copyright (C) 2005-2016  FeatureIDE team, University of Magdeburg, Germany
 *
 * This file is part of FeatureIDE.
 * 
 * FeatureIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FeatureIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with FeatureIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * See http://featureide.cs.ovgu.de/ for further information.
 */
package de.ovgu.featureide.ui.wizards;

import static de.ovgu.featureide.fm.core.localization.StringTable.ADD_FEATUREIDE_NATURE;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.datatransfer.FileSystemImportWizard;
import org.eclipse.ui.wizards.newresource.BasicNewFileResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import de.ovgu.featureide.core.CorePlugin;
import de.ovgu.featureide.fm.ui.FMUIPlugin;
import de.ovgu.featureide.fm.ui.editors.FeatureModelEditor;
import de.ovgu.featureide.fm.ui.handlers.base.SelectionWrapper;
import de.ovgu.featureide.ui.UIPlugin;

/**
 * TODO description
 * 
 * @author Anna-Liisa
 */
public class ImportFeatureHouseProjectWizard extends BasicNewProjectResourceWizard {

	private final static Image colorImage = FMUIPlugin.getDefault().getImageDescriptor("icons/FeatureIconSmall.ico").createImage();
	
	private IWorkbench workbench;

    private IStructuredSelection selection;
	
	protected ImportFeatureHouseProjectPage page;

	private IProject project;

	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
        this.selection = selection;

//        List selectedResources = IDE.computeSelectedResources(selection);
//        if (!selectedResources.isEmpty()) {
//            this.selection = new StructuredSelection(selectedResources);
//        }
		
		final IResource res = SelectionWrapper.init(selection, IResource.class).getNext();
		if (res != null) {
			project = res.getProject();
			page = new ImportFeatureHouseProjectPage(workbench, selection);
			
		}
		
	}

	@Override
	public void addPages() {
		// addPage(new ConversionPage(selection));
		setWindowTitle("Import FeatureHouse Project into JavaProject");
		page = new ImportFeatureHouseProjectPage(workbench, selection);
		Shell shell = getShell();
		if(shell != null){
			shell.setImage(colorImage);
		}
		addPage(page);
		//super.addPages();
	}

	public boolean performFinish() {
//		if (page.hasCompositionTool() && project.isOpen()) {
//			CorePlugin.setupProject(project, page.getCompositionTool().getId(), page.getSourcePath(), page.getConfigPath(), page.getBuildPath());
//			UIPlugin.getDefault().openEditor(FeatureModelEditor.ID, project.getFile("model.xml"));
//			return true;
//		}
//		return false;
		return page.finish();
	}

}
