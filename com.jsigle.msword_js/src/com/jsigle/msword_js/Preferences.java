/*******************************************************************************
 * Copyright (c) 2013-2021 J. Sigle
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

package com.jsigle.msword_js;

import org.eclipse.jface.preference.IPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.activator.CoreHubHelper;
import ch.elexis.core.ui.preferences.SettingsPreferenceStore;

public class Preferences {
	
	private static SettingsPreferenceStore fsSettingsStore;
	public static Logger log = LoggerFactory.getLogger(Preferences.class);
	
	/**
	 * reload the fs settings store
	 */
	private static void initGlobalConfig(){
		log.debug("Preferences.java initGlobalConfig: is fsSettingsStore == null ? <{}>", (fsSettingsStore == null));
			
		if (fsSettingsStore == null) {
			log.debug("Preferences.java initGlobalConfig: Workaround for bug ... 9501 running, probably resetting MSWord_js user settings...");
			
			fsSettingsStore = new SettingsPreferenceStore(CoreHub.globalCfg);
		} else {
			log.debug("Preferences.java initGlobalConfig: fsSettingsStore already there, workaround not running");
		}
	}	
			
	public static void setFsSettingStore(SettingsPreferenceStore settingsPreferenceStore){
		Preferences.fsSettingsStore = settingsPreferenceStore;
	}
	
	public static SettingsPreferenceStore getFsSettingsStore(){
		return fsSettingsStore;
	}
	
	// 20130411js: Make the temporary filename configurable
	// which is generated to extract the document from the database for viewing.
	// Thereby, simplify tasks like adding a document to an e-mail.
	// For most elements noted below, we can set the maximum number of digits
	// to be used (taken from the source from left); which character to add thereafter;
	// and whether to fill leading digits by a given character.
	// This makes a large number of options, so I construct
	// the required preference store keys from arrays.
	// Note: The DocHandle.getTitle() javadoc says that a document title in omnivore
	// may contain 80 chars.
	// To enable users to copy that in full, I allow for a max of 80 chars to be
	// specified as num_digits for *any* element.
	// Using all elements to that extent will return filename that's vastly too long,
	// but that will probably be handled elsewhere.
	public static final Integer nPreferences_cotf_element_digits_max = 80;
	public static final String PREFERENCE_COTF = "cotf_";
	public static final String[] PREFERENCE_cotf_elements = {
		"constant1", "PID", "fn", "gn", "dob", "dt", "dk", "dguid", "random", "constant2"
	};
	public static final String[] PREFERENCE_cotf_parameters = {
		"fill_leading_char", "num_digits", "add_trailing_char"
	};
	
	// The following unwanted characters, and all below codePoint=32 will be cleaned in advance.
	// Please see the getMSWord_jsTemp_Filename_Element for details.
	// 20210327js: Slightly modified the string after comparison with the original 2.1.7js version.
	// Original Version in 2.1.7js:
	// static final String cotf_unwanted_chars="\\/:*?()+,;\"'�`"; 
	// Last version found here:
	// public static final String cotf_unwanted_chars = "[\\:/:*?()+,\';\"\r\t\n�`<>]";

	// TODO: 20210327js: Apparently, Niklaus replaced my cleanStringFromUnwantedCharsAndTrim
	// by a call to java.util.regex.Matcher in Utils.java - but not in all occasions.
	// So for now, I leave Niklaus' Variant here in addition to my original one plus <>
	// but we need to check whether his implementation does the same as mine.
	// Especially for all code points below 32, I'm afraid his will only find \r\t\n
	// CAVE: Whenever these lines are copy/pasted within Eclipse, Eclipse will duplicate EACH \ !!!
	// To ensure no call is missed, I renamed the two constants to ..._jsorig and ..._ngregex.
	// Maybe see (leftover in Niklaus' code in a comment below):
	// eclipse-javadoc:%E2%98%82=ch.elexis.core.data/%5C/usr%5C/lib%5C/jvm%5C/java-8-oracle%5C/jre%5C/lib%5C/rt.jar%3Cjava.util.regex(Matcher.class%E2%98%83Matcher~quoteReplacement~Ljava.lang.String;%E2%98%82java.lang.String

	public static final String cotf_unwanted_chars_jsorig = "\\/:*?()+,;\"'�`<>";
	public static final String cotf_unwanted_chars_ngregex = "[\\:/:*?()+,\';\"\r\t\n�`<>]";
	
	// Dank Eclipse's mglw. etwas übermässiger "Optimierung" werden externalisierte Strings nun als
	// Felder von Messges angesprochen - und nicht mehr wie zuvor über einen als String übergebenen key.
	// Insofern muss ich wohl zu den obigen Arrays korrespondierende Arrays vorab erstellen, welche
	// die jeweils zugehörigen Strings aus omnivore.Messages dann in eine definierte Reihenfolge bringen,
	// in der ich sie unten auch wieder gerne erhalten würde. Einfach per Programm at runtime die
	// keys generieren scheint nicht so leicht zu gehen.
	public static final String[] PREFERENCE_cotf_elements_messages = {
		Messages.Preferences_cotf_constant1,
		Messages.Preferences_cotf_pid,
		Messages.Preferences_cotf_fn,
		Messages.Preferences_cotf_gn,
		Messages.Preferences_cotf_dob,
		Messages.Preferences_cotf_dt,
		Messages.Preferences_cotf_dk,
		Messages.Preferences_cotf_dguid,
		Messages.Preferences_cotf_random,
		Messages.Preferences_cotf_constant2
	};
	public static final String[] PREFERENCE_cotf_parameters_messages = {
		Messages.Preferences_cotf_fill_lead_char,
		Messages.Preferences_cotf_num_digits,
		Messages.Preferences_cotf_add_trail_char
	};
}
