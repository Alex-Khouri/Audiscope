/*
* Audiscope
* 
* Author: Alexander Khouri
* Date: September 2024
*/

public class Config {
	public static final String VERSION 					= "1.0.0";
	//----------------------------------------
	// STRING MACROS
	//----------------------------------------
	public static final String LOOP_SCAN			 	= "Loop Scan";
	public static final String LINEAR_SCAN			 	= "Linear Scan";
	public static final String FILE_SPLIT			 	= "File Split";
	public static final String ANALYSE_GAIN		 		= "Analyse Gain";
	public static final String GENERAL  		 	 	= "General";
	public static final String APPEARANCE			 	= "Appearance";
	public static final String OUTPUT				 	= "Output";
	public static final String INFO 	   			 	= "Info";
	public static final String LOGARITHMIC			 	= "Logarithmic [base 10]";
	public static final String SQUARE_ROOT 		 		= "Square Root";
	public static final String LINEAR 				 	= "Linear";
	public static final String QUADRATIC			 	= "Quadratic";
	public static final String EXPONENTIAL			 	= "Exponential [base 10]";
	public static final String MUSIC_TEMPO_BEATS	 	= "Tempo/Beats (Music)";
	public static final String MUSIC_ESTIMATE_RANGE 	= "Estimate Range (Music)";
	public static final String MUSIC_AUTOMATIC		 	= "Automatic (Music)";
	public static final String SINE_TONE			 	= "Sine Tone";
	public static final String NOISE				 	= "Noise";
	public static final String PDF					 	= "PDF";
	public static final String JPG					 	= "JPG";
	public static final String PNG					 	= "PNG";
	public static final String SVG					 	= "SVG";
	public static final String SIGNAL_CUTOUT		 	= "Signal Cutout";
	public static final String SIGNAL_GRADIENT			= "Signal Gradient";
	public static final String DUAL_COMBINED	 		= "Dual (Combined Graph)";
	public static final String DUAL_SEPARATE		 	= "Dual (Separate Graphs)";
	public static final String SIZE						= "Size";
	public static final String TIME						= "Time";
	//----------------------------------------
	// NUMBER MACROS
	//----------------------------------------
	public static final double SMALLEST_POSITIVE_DOUBLE		= Math.pow(10, -323);
	public static final double LARGEST_POSITIVE_DOUBLE		= Math.pow(10, 308);
	public static final double LARGEST_EXPONENT				= 308.0;
	//----------------------------------------
	// APPLICATION SETTINGS
	//----------------------------------------
	public static final int MAXIMUM_AUDIO_BIT_DEPTH  			 = 32;
	public static final int MAXIMUM_AUDIO_SAMPLE_RATE 			 = 96000;
	public static final long LOOP_SCAN_FILE_SIZE_LIMIT	 		 = 4200000000L;		// Measured in bytes
	public static final long LINEAR_SCAN_FILE_SIZE_LIMIT 		 = 4200000000L;
	public static final long FILE_SPLIT_FILE_SIZE_LIMIT			 = 4294967000L;		// Doesn't apply to WAV files
	public static final long ANALYSE_GAIN_FILE_SIZE_LIMIT 		 = 4200000000L;
	public static final String[] LOOP_SCAN_SUPPORTED_FORMATS	 = new String[] {"aif", "aiff", "au", "wav"};
	public static final String[] LINEAR_SCAN_SUPPORTED_FORMATS	 = new String[] {"aif", "aiff", "au", "wav"};
	public static final String[] FILE_SPLIT_SUPPORTED_FORMATS	 = new String[] {"wav"};
	public static final String[] ANALYSE_GAIN_SUPPORTED_FORMATS	 = new String[] {"aif", "aiff", "au", "wav"};
	//----------------------------------------
	// CONTROL LABELS & VALUES
	// --------------------
	// The order of LABEL elements must match the order of corresponding VALUE elements.
	// LABELS are used to populate GUI controls, and may also be used for internal processing.
	// In cases where labels aren't suitable for internal processing, VALUES are used for that purpose.
	//----------------------------------------
		// OPTIONS
			// OVERALL
	public static final String[] OPTION_LABELS			  = new String[] {GENERAL, OUTPUT, APPEARANCE, INFO};
			// GENERAL
	public static final long[] BUFFER_VALUES			  = new long[] {1000L, 10000L, 100000L, 1000000L,
																10000000L, 100000000L, 1000000000L};	// Measured in bytes
	public static final String[] BUFFER_LABELS			  = new String[] {"1 KB (most stable)", "10 KB", "100 KB", "1 MB",
																  "10 MB", "100 MB", "1 GB (fastest)"};
	public static final int[] SCAN_WINDOW_VALUES		  = new int[] {1, 2, 5, 10, 20, 30};	// Measured in seconds
	public static final String[] SCAN_WINDOW_LABELS	  	  = new String[] {"1 (fastest)", "2", "5", "10", "20", "30 (most accurate)"};
	public static final int[] OPTIMISATION_VALUES		  = new int[] {0, 1, 2, 5};
	public static final String[] OPTIMISATION_LABELS	  = new String[] {"None (most accurate)", "Low", "Medium", "High (fastest)"};
	public static final String[] SPLIT_TYPE_VALUES		  = new String[] {SIZE, TIME};
	public static final String[] SPLIT_TYPE_LABELS		  = new String[] {SIZE, TIME};
	public static final String[] SPLIT_SIZE_VALUES		  = new String[] {"0.1", "0.2", "0.5", "1", "2", "3", "4"};
	public static final String[] SPLIT_SIZE_LABELS		  = new String[] {"0.1", "0.2", "0.5", "1", "2", "3", "4"};
	public static final float[]  WAVEFORM_TYPE_VALUES	  = new float[]  {0.1f, 0.2f, 0.5f, 1.0f, 2.0f, 5.0f, 10.0f};
	public static final String[] WAVEFORM_TYPE_LABELS	  = new String[] {"0.1", "0.2", "0.5", "1", "2", "5", "10"};
	public static final int[]	 GAIN_PRECISION_VALUES		 = new int[] {1, 2, 3, 4, 5, 6};
	public static final String[] GAIN_PRECISION_LABELS		 = new String[] {"1", "2", "3", "4", "5", "6"};
	public static final String[] GAIN_PRECISION_PLACEHOLDERS = new String[] {"%.1f", "%.2f", "%.3f",
																			"%.4f", "%.5f", "%.6f"};
	public static final String[] LINEAR_SCAN_MODE_VALUES  = new String[] {SIGNAL_CUTOUT, SIGNAL_GRADIENT, DUAL_COMBINED, DUAL_SEPARATE};
	public static final String[] LINEAR_SCAN_MODE_LABELS  = new String[] {SIGNAL_CUTOUT, SIGNAL_GRADIENT, DUAL_COMBINED, DUAL_SEPARATE};
			// OUTPUT
	public static final int[][] RESOLUTION_VALUES		  = new int[][] {{1280, 720},		// HD	(width, height)
																		{1920, 1080},		// FHD
																		{2560, 1440},		// QHD
																		{3840, 2160}};		// UHD
	public static final String[] RESOLUTION_LABELS 		  = new String[] {"HD (720p)",
																		"FHD (1080p)",
																		"QHD (1440p)",
																		"UHD (2160p)"};
	public static final String[] VARIANCE_SCALE_VALUES	  = new String[] {LOGARITHMIC, SQUARE_ROOT, LINEAR, QUADRATIC, EXPONENTIAL};
	public static final String[] VARIANCE_SCALE_LABELS 	  = new String[] {LOGARITHMIC, SQUARE_ROOT, LINEAR, QUADRATIC, EXPONENTIAL};
	public static final String[] PROBABILITY_SCALE_VALUES = new String[] {LOGARITHMIC, SQUARE_ROOT, LINEAR, QUADRATIC, EXPONENTIAL};
	public static final String[] PROBABILITY_SCALE_LABELS = new String[] {LOGARITHMIC, SQUARE_ROOT, LINEAR, QUADRATIC, EXPONENTIAL};
	public static final int[] TIME_SCALE_VALUES				 = new int[] {1, 2, 5, 10};
	public static final String[] TIME_SCALE_LABELS			 = new String[] {"1", "2", "5", "10"};
			// APPEARANCE
	public static final String[] THEME_VALUES = new String[] {"theme-dark.css", "theme-light.css"};
	public static final String[] THEME_LABELS = new String[] {"Dark", "Light"};
	public static final String[] FONT_SIZE_VALUES = new String[] {"font-small.css", "font-medium.css", "font-large.css"};
	public static final String[] FONT_SIZE_LABELS = new String[] {"Small", "Medium", "Large"};
	public static final int[] PROGRESS_REFRESH_VALUES	 = new int[] {1000, 2000, 3000, 4000, 5000};	// Milliseconds
	public static final String[] PROGRESS_REFRESH_LABELS = new String[] {"1 (fastest)", "2", "3", "4", "5 (most stable)"}; // Seconds
	//----------------------------------------
	// COMPLEX STRINGS
	//----------------------------------------
	public static final String INFO_TEXT = "This application can be used to find artifacts in audio files generated by signal processors. It analyses the signal variance across the audio duration, then generates a graph of the results.\n\nThe following types of audio can analysed:\n    * Music\n    * Sine Tones\n    * Noise (White, Pink, etc)\n\nWhen analysing a music loop, users can provide an estimate of the loop's length using maximum and minimum values, or they can specify the length using musical metrics (i.e. tempo and number of beats). The application can also detect the loop length automatically, but this method is slow. When analysing sine tones or noise, the program will treat the signal as a series of short loops.\n\nThis application also includes a linear audio scanner, which can analyse the integrity of both looped and non-looped recordings by measuring frame loss probability. Multiple scanning algorithms are available, and they can be combined to increase the chance of successful detection.\n\nIf the audio recording is split across multiple files, they can be loaded into this application collectively and will be treated as a single stream. Multiple batches can also be loaded, in order to process multiple recordings in one continuous operation. The scanners can only process audio files up to " + String.valueOf(Math.floor(LOOP_SCAN_FILE_SIZE_LIMIT / 100000000.0) / 10.0) + "GB, but the file splitter can be used to break up large WAV files into smaller ones.\n\nMaximum Supported Audio Quality: " + MAXIMUM_AUDIO_BIT_DEPTH + "-bit/" + (float) MAXIMUM_AUDIO_SAMPLE_RATE / 1000.0 + " kHz\nSupported Audio File Formats: " + String.join(", ", LOOP_SCAN_SUPPORTED_FORMATS).toUpperCase() + "\n    (File splitter only supports WAV)";
	//----------------------------------------
	// GUI DIMENSIONS
	//----------------------------------------
		// Default stage settings used for window resizing
	public static final double STAGE_DEFAULT_WIDTH			= 690.0;
	public static final double STAGE_DEFAULT_HEIGHT			= 604.0;
	public static final double STAGE_MIN_WIDTH				= 690.0;
	public static final double STAGE_MIN_HEIGHT 			= 520.0;
	public static final int DEFAULT_MARGIN_SIZE 			= 10;
		// Default settings for components that respond to width resizing
	public static final int TEXT_DEFAULT_WIDTH 				= 670;
	public static final int LABEL_BATCH_DEFAULT_WIDTH		= 90;
	public static final int CONTROL_DEFAULT_WIDTH_NARROW	= 70;
	public static final int CONTROL_DEFAULT_WIDTH_MEDIUM	= 150;
	public static final int CONTROL_DEFAULT_WIDTH_WIDE		= 250;
	public static final int DESTINATION_DEFAULT_WIDTH		= 500;
		// Default settings for components that respond to height resizing
	public static final int INFO_DEFAULT_HEIGHT 			= 520;
	public static final int TEXT_DEFAULT_HEIGHT 			= 152;
	//----------------------------------------
	// DEFAULT VALUES
	//--------------------
	// A default must be defined for each variable appearance value
	//----------------------------------------
		// OVERALL
	public static final String CURRENT_DIRECTORY_DEFAULT 			= "";	// Excluded from the Reset button
		// TOOLS
	public static final int WAVEFORM_INDEX_DEFAULT					= 3;
	public static final float WAVEFORM_VALUE_DEFAULT				= WAVEFORM_TYPE_VALUES[WAVEFORM_INDEX_DEFAULT];
	public static final int GAIN_PRECISION_INDEX_DEFAULT			= 2;
	public static final int GAIN_PRECISION_VALUE_DEFAULT			= GAIN_PRECISION_VALUES[GAIN_PRECISION_INDEX_DEFAULT];
		// OPTIONS
			// GENERAL
	public static final int BUFFER_SIZE_INDEX_DEFAULT				= 6;
	public static final int AUTO_SCAN_WINDOW_INDEX_DEFAULT			= 3;
	public static final int SINE_SCAN_WINDOW_INDEX_DEFAULT			= 2;
	public static final int NOISE_SCAN_WINDOW_INDEX_DEFAULT 		= 3;
	public static final int OPTIMISATION_INDEX_DEFAULT				= 1;
	public static final int LINEAR_SCAN_MODE_INDEX_DEFAULT			= 2;
			// OUTPUT
	public static final int OUTPUT_FORMAT_INDEX_DEFAULT				= 0;
	public static final int OUTPUT_RESOLUTION_INDEX_DEFAULT			= 2;
	public static final int VARIANCE_SCALE_INDEX_DEFAULT			= 0;
	public static final int PROBABILITY_SCALE_INDEX_DEFAULT			= 2;
	public static final int TIME_SCALE_INDEX_DEFAULT				= 0;
			// APPEARANCE
	public static final int THEME_INDEX_DEFAULT						= 0;
	public static final int FONT_SIZE_INDEX_DEFAULT					= 1;
	public static final int PROGRESS_REFRESH_INDEX_DEFAULT 			= 2;
	//----------------------------------------
	// VARIABLE VALUES
	//----------------------------------------
		// OVERALL
	public static String CURRENT_DIRECTORY 				= CURRENT_DIRECTORY_DEFAULT;
		// OPTIONS
			// GENERAL
	public static int BUFFER_SIZE_INDEX					= BUFFER_SIZE_INDEX_DEFAULT;
	public static int AUTO_SCAN_WINDOW_INDEX			= AUTO_SCAN_WINDOW_INDEX_DEFAULT;
	public static int SINE_SCAN_WINDOW_INDEX			= SINE_SCAN_WINDOW_INDEX_DEFAULT;
	public static int NOISE_SCAN_WINDOW_INDEX 			= NOISE_SCAN_WINDOW_INDEX_DEFAULT;
	public static int OPTIMISATION_INDEX				= OPTIMISATION_INDEX_DEFAULT;
	public static int LINEAR_SCAN_MODE_INDEX			= LINEAR_SCAN_MODE_INDEX_DEFAULT;
			// OUTPUT
	public static int OUTPUT_FORMAT_INDEX				= OUTPUT_FORMAT_INDEX_DEFAULT;
	public static int OUTPUT_RESOLUTION_INDEX			= OUTPUT_RESOLUTION_INDEX_DEFAULT;
	public static int VARIANCE_SCALE_INDEX				= VARIANCE_SCALE_INDEX_DEFAULT;
	public static int PROBABILITY_SCALE_INDEX			= PROBABILITY_SCALE_INDEX_DEFAULT;
	public static int TIME_SCALE_INDEX					= TIME_SCALE_INDEX_DEFAULT;
			// APPEARANCE
	public static int THEME_INDEX						= THEME_INDEX_DEFAULT;
	public static int FONT_SIZE_INDEX					= FONT_SIZE_INDEX_DEFAULT;
	public static int PROGRESS_REFRESH_INDEX  			= PROGRESS_REFRESH_INDEX_DEFAULT;
}