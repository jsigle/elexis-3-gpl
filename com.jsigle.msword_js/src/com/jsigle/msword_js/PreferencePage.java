/*******************************************************************************
 * Copyright (c) 2013-2021 J. Sigle; Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    J. Sigle - derived this preference page for msword_js from the current PreferencePage at Omnivore
 *    			 namely, to provide configurable informative filenames here as well.
 *    			 N.B.: I've previously ported that to NoaText_jsl, which might also serve for here,
 *    			 but I want to start this with files below com.sigle.msword_js rather than below ag.ion.xyz  
 *    
 *******************************************************************************/

//TODO: js: I wonder why Constants, Variables and other functionality for one module have been
//split into three separate files - which are additionally located in different subtrees.
//This way, it is very difficult to do diffs, or to find out what has been included from
//the final version and what has been lost. It's also difficult to (re-)understand what's
//happening, because some comments went into the other files, some where removed.
//Moreover, an import... line needs exactly the same space as a definition of a constant -
//and not even all thematically related constants have made it into a single constants-file...

package com.jsigle.msword_js;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory;
import ch.elexis.data.Leistungsblock;

import static com.jsigle.msword_js.Constants.PREFBASE;
import com.jsigle.msword_js.Preferences;

//FIXME: Layout needs a thorough redesign. See: http://www.eclipse.org/articles/article.php?file=Article-Understanding-Layouts/index.html -- 20130411js: done to some extent.
//FIXME: We want a layout that will use all the available space, auto re-size input fields etc., have nested elements, and still NOT result in "dialog has invalid data" error messages.
//FIXME: Maybe we must add PREFERENCE_BRANCH to some editor element add etc. commands, to ensure the parameters are store.

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public static Logger log = LoggerFactory.getLogger("com.jsigle.msword_js.PreferencePage"); //$NON-NLS-1$
	
	public PreferencePage(){
		super(GRID);
		
		setPreferenceStore(new SettingsPreferenceStore(CoreHub.globalCfg));
		setDescription(com.jsigle.msword_js.Messages.Preferences_MSWord_js);
	}
	
	@Override
	protected void createFieldEditors(){
		// I'd like to place ALL groups in this preference dialog one under another,
		// so that each group completely occupies the available horizontal space.
		// But the default behaviour is to put the groups next to each other :-(
		
		// For instructions, see:
		// http://www.eclipse.org/articles/article.php?file=Article-Understanding-Layouts/index.html
		
		// I can't use any other layout but GridLayout.
		// Otherwise some adjustGridLayout() somewhere else will invariably throw:
		// "The currently displayed page contains invalid values." at runtime. 201304110439js
		// Besides, RowLayout() wouldn't make sense here.
		
		// ---
		
		// Nachdem ich aussenrum einmal eine globale Gruppe installiere,
		// bekomme ich wenigstens die nachfolgend tieferen Gruppen untereinander in einer Spalte,
		// und nicht mehr nebeneinander.
		// Offenbar hat die Zuweisung eines Layouts zu getFieldEditorParent() keinen Effekt gehabt.
		// Warum auch immer...
		
		Group gAllOmnivorePrefs = new Group(getFieldEditorParent(), SWT.NONE);
		
		//TODO: 20210327js: There are major differences / missing lines from the
		//latest version of Elexis 2.1.7js / omnivore_js to the version left over here.
		//It is yet unclear whether this happened intentionally - as part of an
		//integration of configurable settings related to my omnivore_js extensions
		//into what had become of the mainstream omnivore by then - or whether things
		//were unintentionally missed. PLEASE REVIEW ASAP.
		//Sources:
		//jsigle@blackbox  Sa Mär 27  15:39:44  /mnt/sdb3/Elexis-workspace/elexis-2.1.7-20130523/elexis-bootstrap-js-201712191036-last-20130605based-with-MSWord_js-as-used-by-JH-since-201701-before-gitpush  
		//$ kate ./elexis-base/ch.elexis.omnivore/src/ch/elexis/omnivore/preferences/PreferencePage.java
		//jsigle@blackbox  Sa Mär 27  15:28:44  /mnt/sdb3/Elexis-workspace/elexis-2.1.7-20130523/elexis-bootstrap-js-201712191036-last-20130605based-with-MSWord_js-as-used-by-JH-since-201701-before-gitpush  
		//$ kompare ./elexis-base/ch.elexis.omnivore/src/ch/elexis/omnivore/preferences/PreferencePage.java /mnt/think3/c/Users/jsigle/git/elexis/3.7/git/./elexis-3-base/bundles/ch.elexis.omnivore.ui/src/ch/elexis/omnivore/ui/preferences/PreferencePage.java
		//and also: Preferences.java, PreferenceContsants.java, Utils.java
		
		// getFieldEditorParent().setLayoutData(SWTHelper.getFillGridData(1,false,0,false));
		
		GridLayout gOmnivorePrefsGridLayout = new GridLayout();
		gOmnivorePrefsGridLayout.numColumns = 1; // this is sadly and apparently ignored.
		gOmnivorePrefsGridLayout.makeColumnsEqualWidth = true;
		
		gAllOmnivorePrefs.setLayout(gOmnivorePrefsGridLayout);
		// getFieldEditorParent().setLayout(gOmnivorePrefsGridLayout);
		
		GridData gOmnivorePrefsGridLayoutData = new GridData();
		gOmnivorePrefsGridLayoutData.grabExcessHorizontalSpace = true;
		gOmnivorePrefsGridLayoutData.horizontalAlignment = GridData.FILL;
		
		gAllOmnivorePrefs.setLayoutData(gOmnivorePrefsGridLayoutData);
		Group gGeneralOptions = new Group(gAllOmnivorePrefs, SWT.NONE);
		GridData gGeneralOptionsGridLayoutData = new GridData();
		gGeneralOptionsGridLayoutData.grabExcessHorizontalSpace = true;
		gGeneralOptionsGridLayoutData.horizontalAlignment = GridData.FILL;
		gGeneralOptions.setLayoutData(gGeneralOptionsGridLayoutData);
				
		// ---
		
		// 20130411js: Make the temporary filename configurable
		// which is generated to extract the document from the database for viewing.
		// Thereby, simplify tasks like adding a document to an e-mail.
		// For most elements noted below, we can set the maximum number of digits
		// to be used (taken from the source from left); which character to add thereafter;
		// and whether to fill leading digits by a given character.
		// This makes a large number of options, so I construct the required preference store keys
		// from arrays.
		
		// Originally, I would have preferred a simple tabular matrix:
		// columns: element name, fill_lead_char, num_digits, add_trail_char
		// lines: each of the configurable elements of the prospective temporary filename.
		// But such a simple thing is apparently not so simple to make using the PreferencePage
		// class.
		// So instead, I add a new group for each configurable element,
		// including each of the 3 parameters.
		
		Integer nCotfRules = Preferences.PREFERENCE_cotf_elements.length;
		
		Group gCotfRules = new Group(gAllOmnivorePrefs, SWT.NONE);
		// Group gCotfRules = new Group(getFieldEditorParent(), SWT.NONE);
		
		// gCotfRules.setLayoutData(SWTHelper.getFillGridData(6,false,nCotfRules,false)); //This
		// would probably make groups-within-group completely disappear.
		
		GridLayout gCotfRulesGridLayout = new GridLayout();
		gCotfRulesGridLayout.numColumns = nCotfRules; // at least this one is finally honoured...
		gCotfRules.setLayout(gCotfRulesGridLayout);
		
		GridData gCotfRulesGridLayoutData = new GridData();
		gCotfRulesGridLayoutData.grabExcessHorizontalSpace = true;
		gCotfRulesGridLayoutData.horizontalAlignment = GridData.FILL;
		gCotfRules.setLayoutData(gCotfRulesGridLayoutData);
		
		gCotfRules.setText(com.jsigle.msword_js.Messages.Preferences_construction_of_temporary_filename);
		
		for (int i = 0; i < nCotfRules; i++) {
			
			Group gCotfRule = new Group(gCotfRules, SWT.NONE);
			
			//gCotfRule.setLayoutData(SWTHelper.getFillGridData(2,false,2,false));	//This would probably make groups-within-group completely disappear.
				
			gCotfRule.setLayout(new FillLayout());
			GridLayout gCotfRuleGridLayout = new GridLayout();
			gCotfRuleGridLayout.numColumns = 6;
			gCotfRule.setLayout(gCotfRuleGridLayout);
			
			GridData gCotfRuleGridLayoutData = new GridData();
			gCotfRuleGridLayoutData.grabExcessHorizontalSpace = true;
			gCotfRuleGridLayoutData.horizontalAlignment = GridData.FILL;
			gCotfRule.setLayoutData(gCotfRuleGridLayoutData);
			
			//System.out.println("Messages.Omnivore_jsPREF_cotf_"+PREFERENCE_cotf_elements[i]);
			
			//gCotfRule.setText(PREFERENCE_cotf_elements[i]);	//The brackets are needed, or the string representations of i and 1 will both be added...
			
			//addField(new StringFieldEditor(PREFERENCE_BRANCH+PREFERENCE_COTF+PREFERENCE_cotf_elements[i]+"_"+PREFERENCE_cotf_parameters[0],PREFERENCE_cotf_parameters[0],gCotfRule));
			//addField(new StringFieldEditor(PREFERENCE_BRANCH+PREFERENCE_COTF+PREFERENCE_cotf_elements[i]+"_"+PREFERENCE_cotf_parameters[1],PREFERENCE_cotf_parameters[1],gCotfRule));
			//addField(new StringFieldEditor(PREFERENCE_BRANCH+PREFERENCE_COTF+PREFERENCE_cotf_elements[i]+"_"+PREFERENCE_cotf_parameters[2],PREFERENCE_cotf_parameters[2],gCotfRule));
			
			//Das hier geht leider nicht so einfach:
			//gCotfRule.setText(getObject("Messages.Omnivore_jsPREF_cotf_"+PREFERENCE_cotf_elements[i]));
			gCotfRule.setText(Preferences.PREFERENCE_cotf_elements_messages[i]);
			
			//addField(new StringFieldEditor(PREFERENCE_BRANCH+PREFERENCE_COTF+PREFERENCE_cotf_elements[i]+"_"+PREFERENCE_cotf_parameters[0],Messages.Omnivore_jsPREF_cotf_fill_leading_char,gCotfRule));
			//addField(new StringFieldEditor(PREFERENCE_BRANCH+PREFERENCE_COTF+PREFERENCE_cotf_elements[i]+"_"+PREFERENCE_cotf_parameters[1],Messages.Omnivore_jsPREF_cotf_num_digits,gCotfRule));
			//addField(new StringFieldEditor(PREFERENCE_BRANCH+PREFERENCE_COTF+PREFERENCE_cotf_elements[i]+"_"+PREFERENCE_cotf_parameters[2],Messages.Omnivore_jsPREF_cotf_add_trail_char,gCotfRule));
			
			//TODO: 20210327js: Once again, significant changes from the original 2.1.7js based code follow.
			//Moreover, readability and comparability of code is completely crippled by the ca. 80-char line length limit.
			//Sorry, need to review this some other time.
			
			String prefName = PREFBASE + Preferences.PREFERENCE_COTF + Preferences.PREFERENCE_cotf_elements[i] + "_" + Preferences.PREFERENCE_cotf_parameters[1];
			
			log.debug("Add  {} val {}", i, prefName);
			
			if (Preferences.PREFERENCE_cotf_elements[i].contains("constant")) {
				gCotfRuleGridLayoutData.horizontalAlignment = GridData.BEGINNING;
				gCotfRuleGridLayoutData.verticalAlignment = GridData.BEGINNING;
				addField(new StringFieldEditor(prefName, "", 10, gCotfRule));
			} else {
				String str0 = PREFBASE + Preferences.PREFERENCE_COTF + Preferences.PREFERENCE_cotf_elements[i] + "_" + Preferences.PREFERENCE_cotf_parameters[0];
				String str2 = PREFBASE + Preferences.PREFERENCE_COTF + Preferences.PREFERENCE_cotf_elements[i] + "_" + Preferences.PREFERENCE_cotf_parameters[2];
				log.debug("{}: keyl {} {} {} {}", i, str0, prefName, str2);
				log.debug("val {} {} {} {}", Preferences.PREFERENCE_cotf_parameters_messages[0],
					Preferences.PREFERENCE_cotf_parameters_messages[1],
					Preferences.PREFERENCE_cotf_parameters_messages[2]);
				addField(new StringFieldEditor(str0, Preferences.PREFERENCE_cotf_parameters_messages[0], 10, gCotfRule));
				addField(new StringFieldEditor(prefName, Preferences.PREFERENCE_cotf_parameters_messages[1], 10, gCotfRule));
				addField(new StringFieldEditor(str2, Preferences.PREFERENCE_cotf_parameters_messages[2], 10, gCotfRule));
			}
		}
		/*
		public static final Integer nOmnivore_jsPREF_cotf_element_digits_min=0;
			public static final Integer nOmnivore_jsPREF_cotf_element_digits_max=255;
			public static final String PREFERENCE_cotf_elements={"PID", "given_name", "family_name", "date_of_birth", "document_title", "constant", "dguid", "random"};
			public static final String PREFERENCE_cotf_parameters={"fill_lead_char", "num_digits", "add_trail_char"};
			 */
		
		//Doesn't help here.
		//adjustGridLayout();
	}
				
	private void addSeparator(){
		Label separator = new Label(getFieldEditorParent(), SWT.HORIZONTAL | SWT.SEPARATOR);
		GridData separatorGridData = new GridData();
		separatorGridData.horizontalSpan = 3;
		separatorGridData.grabExcessHorizontalSpace = true;
		separatorGridData.horizontalAlignment = GridData.FILL;
		separatorGridData.verticalIndent = 0;
		separator.setLayoutData(separatorGridData);
	}
	
	@Override
	public void init(IWorkbench workbench){		
	}
	
	//TODO: 20210328js: Der Button "Anwenden und Schliessen" wirkt NICHT auf die beiden checkboxes
	//"Spaltenbreite speichern" und "Sortierung speichern". Deren Einstellungen kann man nur mit
	//separatem Drücken des Buttons "Anwenden" speichern (löst das nachfolgende performApply aus).
	//Der direkt neben diesem liegend Button "Standardwerte wiederherstellen" hingegen setzt
	//*alle* darüberliegenden Felder zurück, aber nicht die beiden vorgenannten checkboxes...
	//Das ist beides sehr inkonsequent und kontra-intuitiv. Tatsächlich müsste der Button
	//"Anwenden und Schliessen" auch die gewählten Einstellungen der beiden checkboxes
	//ganz unten mit erfassen. Leider leider leider finde ich mal wieder nicht in endlicher
	//Zeit heraus, wo nun die Funktionalität dieses Buttons implementiert ist - oder wie man
	//sie auf die beiden übrigen checkboxes ausdehnt. Vermutlich steht das wieder in einem
	//ganz anderen File für eine ganz andere Klasse etc. - darum möge das bitte upstream richten.
	//P.S.: Nicht mal mit einer Suche nach "Anwenden und Schlie" über alle Files finde ich auch
	//nur den Text dieses Buttons, das dürfte also ein Stock button eines Standarddialogs sein -
	//und somit auch dessen Funktion "irgendwo" definiert, vielleicht auch mehrere Generationen
	//weiter oben, und wäre hier mittels irgendeines Overrides zu erweitern...
	@Override
	protected void performApply(){
		CoreHub.userCfg.flush();
		CoreHub.globalCfg.flush();
		CoreHub.localCfg.flush();
		super.performApply();
	}
}
