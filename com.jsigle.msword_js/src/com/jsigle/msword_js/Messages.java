package com.jsigle.msword_js;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
  private static final String BUNDLE_NAME = "com.jsigle.msword_js.messages";
  	public static String MSWord_js_couldNotMakeTempFile;
    public static String Preferences_DEST_DIR;
    public static String Preferences_Rule;
    public static String Preferences_SRC_PATTERN;
    public static String Preferences_automatic_archiving_of_processed_files;
    public static String Preferences_construction_of_temporary_filename;
    public static String Preferences_cotf_add_trail_char;
    public static String Preferences_cotf_constant1;
    public static String Preferences_cotf_constant2;
    public static String Preferences_cotf_dguid;
    public static String Preferences_cotf_dk;
    public static String Preferences_cotf_dob;
    public static String Preferences_cotf_dt;
    public static String Preferences_cotf_fill_lead_char;
    public static String Preferences_cotf_fn;
    public static String Preferences_cotf_gn;
    public static String Preferences_cotf_num_digits;
    public static String Preferences_cotf_pid;
    public static String Preferences_cotf_random;
    public static String Preferences_MSWord_js;

    static { // load message values from bundle file
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }
}
