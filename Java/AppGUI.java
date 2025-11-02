/*
* Audiscope
* 
* Author: Alexander Khouri
* Date: September 2024
*/

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.DirectoryChooser.*;
import javafx.stage.Stage;

public class AppGUI extends Application {
	// Core Components
	boolean analysisRunning;
	FileProcessingTask task;
	ArrayList<ExecutionBatch> batchList;		// Maximum number of batches: 99
	int currentBatch;							// Zero-indexed
	long currentFileSizeLimit;
	
	// GUI Components
	Scene sceneTools;
	Scene sceneOptions;
	GridPane gridTools;
	GridPane gridOptions;
	Button buttonBatchLeft;
	Button buttonBatchRight;
	Label labelBatchNumber;
	Button buttonBatchAdd;
	Button buttonBatchRemove;
	Button buttonShowOptions;
	Button buttonShowTools;
	Label labelVersionNumber;
	ComboBox<String> boxOptionSelect;
	String userSelectedOption;
	Button buttonResetGeneral;
	Label labelBufferSize;
	ComboBox<String> boxBufferSize;
	Label labelAutoWindow;
	ComboBox<String> boxAutoWindow;
	Label labelSineWindow;
	ComboBox<String> boxSineWindow;
	Label labelNoiseWindow;
	ComboBox<String> boxNoiseWindow;
	Label labelOptimisationMode;
	ComboBox<String> boxOptimisationMode;
	Label labelLinearScanMode;
	ComboBox<String> boxLinearScanMode;
	Button buttonResetOutput;
	Label labelOutputFormat;
	ComboBox<String> boxOutputFormat;
	Label labelOutputResolution;
	ComboBox<String> boxOutputResolution;
	Label labelVarianceScale;
	ComboBox<String> boxVarianceScale;
	Label labelProbabilityScale;
	ComboBox<String> boxProbabilityScale;
	Label labelTimeScale;
	ComboBox<String> boxTimeScale;
	Button buttonResetAppearance;
	Label labelThemeSelect;
	ComboBox<String> boxThemeSelect;
	Label labelFontSize;
	ComboBox<String> boxFontSize;
	Label labelProgressRefresh;
	ComboBox<String> boxProgressRefresh;
	TextArea textInfo;
	Button buttonOpenFile;
	ArrayList<String> supportedExtensions;	// Used for the FileChooser extension filter
	FileChooser fileChooser;
	Button buttonClearFile;
	ComboBox<String> boxToolSelect;
	TextArea textFiles;
	DirectoryChooser destinationChooser;
	Button buttonDestination;
	TextField textDestination;
	CheckBox checkUseInputFolder;
	CheckBox checkMultipleBatches;
	CheckBox checkDeleteOriginal;
	CheckBox checkGenerateGraph;
	Button buttonRun;
	Button buttonCancel;
	Label labelLoopType;
	Label labelSplitType;
	Label labelSplitSize;
	Label labelSplitTimeMin;
	Label labelSplitTimeSec;
	Label labelWaveformType;
	Label labelGainPrecision;
	ComboBox<String> boxLoopType;
	ComboBox<String> boxSplitType;
	ComboBox<String> boxSplitSize;
	TextField textSplitTimeMin;
	TextField textSplitTimeSec;
	ComboBox<String> boxWaveformType;
	ComboBox<String> boxGainPrecision;
	Label labelLoopParam1;
	TextField textParam1;
	Label labelLoopParam2;
	TextField textParam2;
	TextArea textOutput;
	Alert alertFileSizeLimit;
	
	// GUI Component Groups
	ArrayList<javafx.scene.control.Control> controlsOptionsGeneral;
	ArrayList<javafx.scene.control.Control> controlsOptionsOutput;
	ArrayList<javafx.scene.control.Control> controlsOptionsAppearance;
	ArrayList<javafx.scene.control.Control> controlsOptionsInfo;
	ArrayList<javafx.scene.control.Labeled> controlsTextOverrunClip;
	
	// GUI Data
		// General
	String intRegex = "(\\d{0,9})";
	String intSecondsRegex = "([1-5]?[0-9])?";
		// Combo Box Values
			// TOOLS
	ArrayList<String> toolTypes = new ArrayList<String>(Arrays.asList(Config.LOOP_SCAN, Config.LINEAR_SCAN, Config.FILE_SPLIT, Config.ANALYSE_GAIN));
	ArrayList<String> loopTypes = new ArrayList<String>(Arrays.asList(Config.MUSIC_TEMPO_BEATS, Config.MUSIC_ESTIMATE_RANGE, Config.MUSIC_AUTOMATIC, Config.SINE_TONE, Config.NOISE));
	ArrayList<String> splitTypes = new ArrayList<String>(Arrays.asList(Config.SPLIT_TYPE_LABELS));
	ArrayList<String> splitSizes = new ArrayList<String>(Arrays.asList(Config.SPLIT_SIZE_LABELS));
	ArrayList<String> waveformTypes = new ArrayList<String>(Arrays.asList(Config.WAVEFORM_TYPE_LABELS));
	ArrayList<String> gainPrecisionTypes = new ArrayList<String>(Arrays.asList(Config.GAIN_PRECISION_LABELS));
			// OPTIONS
	ArrayList<String> optionTypes = new ArrayList<String>(Arrays.asList(Config.OPTION_LABELS));
				// GENERAL
	ArrayList<String> bufferTypes = new ArrayList<String>(Arrays.asList(Config.BUFFER_LABELS));
	ArrayList<String> windowTypes = new ArrayList<String>(Arrays.asList(Config.SCAN_WINDOW_LABELS));
	ArrayList<String> optimisationTypes = new ArrayList<String>(Arrays.asList(Config.OPTIMISATION_LABELS));
	ArrayList<String> linearScanModeTypes = new ArrayList<String>(Arrays.asList(Config.LINEAR_SCAN_MODE_LABELS));
				// OUTPUT
	ArrayList<String> outputTypes = new ArrayList<String>(Arrays.asList(Config.PDF, Config.JPG, Config.PNG, Config.SVG));
	ArrayList<String> resolutionTypes = new ArrayList<String>(Arrays.asList(Config.RESOLUTION_LABELS));
	ArrayList<String> varianceScaleTypes = new ArrayList<String>(Arrays.asList(Config.VARIANCE_SCALE_LABELS));
	ArrayList<String> probabilityScaleTypes = new ArrayList<String>(Arrays.asList(Config.PROBABILITY_SCALE_LABELS));
	ArrayList<String> timeScaleTypes = new ArrayList<String>(Arrays.asList(Config.TIME_SCALE_LABELS));
				// APPEARANCE
	ArrayList<String> themeTypes = new ArrayList<String>(Arrays.asList(Config.THEME_LABELS));
	ArrayList<String> fontSizeTypes = new ArrayList<String>(Arrays.asList(Config.FONT_SIZE_LABELS));
	ArrayList<String> progressRefreshTypes = new ArrayList<String>(Arrays.asList(Config.PROGRESS_REFRESH_LABELS));
	
	// Core Functions
	public FileProcessingTask shallowCopyOfTask(FileProcessingTask oldTask) {
		FileProcessingTask newTask = new FileProcessingTask();
		newTask.initialise(oldTask.tool, oldTask.batchList, oldTask.currentBatch, oldTask.useMultipleBatches, oldTask.deleteOriginal, oldTask.generateGraph, oldTask.textOutput);
		return newTask;
	}
	
	// GUI Functions
	// Send data from batch model to GUI
	public void populateToolsGrid() {
		ExecutionBatch batch = batchList.get(currentBatch);
		textFiles.clear();
		for (File file : batch.openFiles) {
			textFiles.appendText(file.getName() + "\n");
		}
		if (batch.outputLocation != null) {
			textDestination.setText(batch.outputLocation.getAbsolutePath());
		} else {
			textDestination.setText("");
		}
		if (batch.useInputFolder) {
			checkUseInputFolder.setSelected(true);
			textDestination.setDisable(true);
			buttonDestination.setDisable(true);
		} else {
			checkUseInputFolder.setSelected(false);
			textDestination.setDisable(false);
			buttonDestination.setDisable(false);
		}
		textParam1.setText(batch.param1 == -1 ? "" : String.valueOf(batch.param1));
		textParam2.setText(batch.param2 == -1 ? "" : String.valueOf(batch.param2));
		boxLoopType.getSelectionModel().select(loopTypes.get(batch.loopIndex));
		boxSplitType.getSelectionModel().select(splitTypes.get(batch.splitTypeIndex));
		boxSplitSize.getSelectionModel().select(splitSizes.get(batch.splitSizeIndex));
		textSplitTimeMin.setText(batch.splitTimeMin == -1 ? "" : String.valueOf(batch.splitTimeMin));
		textSplitTimeSec.setText(batch.splitTimeSec == -1 ? "" : String.valueOf(batch.splitTimeSec));
		boxWaveformType.getSelectionModel().select(waveformTypes.get(batch.waveformIndex));
		boxGainPrecision.getSelectionModel().select(gainPrecisionTypes.get(batch.gainPrecisionIndex));
		refreshToolsGrid();
	}
	public void populateOptionsGrid() {
		boxBufferSize.getSelectionModel().select(bufferTypes.get(Config.BUFFER_SIZE_INDEX));
		boxAutoWindow.getSelectionModel().select(windowTypes.get(Config.AUTO_SCAN_WINDOW_INDEX));
		boxSineWindow.getSelectionModel().select(windowTypes.get(Config.SINE_SCAN_WINDOW_INDEX));
		boxNoiseWindow.getSelectionModel().select(windowTypes.get(Config.NOISE_SCAN_WINDOW_INDEX));
		boxOptimisationMode.getSelectionModel().select(optimisationTypes.get(Config.OPTIMISATION_INDEX));
		boxLinearScanMode.getSelectionModel().select(linearScanModeTypes.get(Config.LINEAR_SCAN_MODE_INDEX));
		boxOutputFormat.getSelectionModel().select(outputTypes.get(Config.OUTPUT_FORMAT_INDEX));
		boxOutputResolution.getSelectionModel().select(resolutionTypes.get(Config.OUTPUT_RESOLUTION_INDEX));
		boxVarianceScale.getSelectionModel().select(varianceScaleTypes.get(Config.VARIANCE_SCALE_INDEX));
		boxProbabilityScale.getSelectionModel().select(probabilityScaleTypes.get(Config.PROBABILITY_SCALE_INDEX));
		boxTimeScale.getSelectionModel().select(timeScaleTypes.get(Config.TIME_SCALE_INDEX));
		boxThemeSelect.getSelectionModel().select(themeTypes.get(Config.THEME_INDEX));
		boxFontSize.getSelectionModel().select(fontSizeTypes.get(Config.FONT_SIZE_INDEX));
		boxProgressRefresh.getSelectionModel().select(progressRefreshTypes.get(Config.PROGRESS_REFRESH_INDEX));
		refreshOptionsGrid();
	}
	
	// Apply GUI display rules
	public void refreshToolsGrid() {
		ExecutionBatch batch = batchList.get(currentBatch);	// This is the only case of core dependency for GUI behaviour
		if (boxToolSelect.getValue() == Config.LOOP_SCAN) {
			checkUseInputFolder.setDisable(false);
			if (checkUseInputFolder.isSelected()) {
				buttonDestination.setDisable(true);
				textDestination.setDisable(true);
			} else {
				buttonDestination.setDisable(false);
				textDestination.setDisable(false);
			}
			checkUseInputFolder.setVisible(true);
			checkDeleteOriginal.setVisible(false);
			checkGenerateGraph.setVisible(false);
			labelLoopParam1.setVisible(true);
			labelLoopParam2.setVisible(true);
			labelLoopType.setVisible(true);
			labelSplitType.setVisible(false);
			labelSplitSize.setVisible(false);
			labelSplitTimeMin.setVisible(false);
			labelSplitTimeSec.setVisible(false);
			labelWaveformType.setVisible(false);
			labelGainPrecision.setVisible(false);
			boxLoopType.setVisible(true);
			boxSplitType.setVisible(false);
			boxSplitSize.setVisible(false);
			textSplitTimeMin.setVisible(false);
			textSplitTimeSec.setVisible(false);
			boxWaveformType.setVisible(false);
			boxGainPrecision.setVisible(false);
			if (boxLoopType.getValue() == Config.MUSIC_TEMPO_BEATS) {
				labelLoopParam1.setText("Tempo (BPM):");
				labelLoopParam2.setText("Number of Beats:");
				textParam1.setVisible(true);
				textParam2.setVisible(true);
				if (analysisRunning || batch.openFiles.isEmpty() || textParam1.getText().isEmpty() || textParam2.getText().isEmpty() || (textDestination.getText().isEmpty() && !checkUseInputFolder.isSelected())) {
					batch.readyToRun = false;
				} else {
					batch.readyToRun = true;
				}
			} else if (boxLoopType.getValue() == Config.MUSIC_ESTIMATE_RANGE) {
				labelLoopParam1.setText("Minimum Time (sec):");
				labelLoopParam2.setText("Maximum Time (sec):");
				textParam1.setVisible(true);
				textParam2.setVisible(true);
				if (analysisRunning || batch.openFiles.isEmpty() || textParam1.getText().isEmpty() || textParam2.getText().isEmpty() || (textDestination.getText().isEmpty() && !checkUseInputFolder.isSelected())) {
					batch.readyToRun = false;
				} else {
					batch.readyToRun = true;
				}
			} else {
				labelLoopParam1.setText("");
				labelLoopParam2.setText("");
				textParam1.setVisible(false);
				textParam2.setVisible(false);
				textParam1.setText("");
				textParam2.setText("");
				if (batch.openFiles.isEmpty() || (textDestination.getText().isEmpty() && !checkUseInputFolder.isSelected()) || analysisRunning) {
					batch.readyToRun = false;
				} else {
					batch.readyToRun = true;
				}
			}
		} else if (boxToolSelect.getValue() == Config.LINEAR_SCAN) {
			checkUseInputFolder.setDisable(false);
			if (checkUseInputFolder.isSelected()) {
				buttonDestination.setDisable(true);
				textDestination.setDisable(true);
			} else {
				buttonDestination.setDisable(false);
				textDestination.setDisable(false);
			}
			checkUseInputFolder.setVisible(true);
			checkDeleteOriginal.setVisible(false);
			checkGenerateGraph.setVisible(false);
			labelLoopParam1.setVisible(false);
			labelLoopParam2.setVisible(false);
			labelLoopType.setVisible(false);
			labelSplitType.setVisible(false);
			labelSplitSize.setVisible(false);
			labelSplitTimeMin.setVisible(false);
			labelSplitTimeSec.setVisible(false);
			labelWaveformType.setVisible(false);
			labelGainPrecision.setVisible(false);
			textParam1.setVisible(false);
			textParam2.setVisible(false);
			textParam1.setText("");
			textParam2.setText("");
			boxLoopType.setVisible(false);
			boxSplitType.setVisible(false);
			boxSplitSize.setVisible(false);
			textSplitTimeMin.setVisible(false);
			textSplitTimeSec.setVisible(false);
			boxWaveformType.setVisible(false);
			boxGainPrecision.setVisible(false);
			if (analysisRunning || batch.openFiles.isEmpty() || (textDestination.getText().isEmpty() && !checkUseInputFolder.isSelected())) {
				batch.readyToRun = false;
			} else {
				batch.readyToRun = true;
			}
		} else if (boxToolSelect.getValue() == Config.FILE_SPLIT) {
			checkUseInputFolder.setDisable(false);
			if (checkUseInputFolder.isSelected()) {
				buttonDestination.setDisable(true);
				textDestination.setDisable(true);
			} else {
				buttonDestination.setDisable(false);
				textDestination.setDisable(false);
			}
			checkUseInputFolder.setVisible(true);
			checkDeleteOriginal.setVisible(true);
			checkGenerateGraph.setVisible(false);
			labelLoopParam1.setVisible(false);
			labelLoopParam2.setVisible(false);
			labelLoopType.setVisible(false);
			labelSplitType.setVisible(true);
			boxSplitType.setVisible(true);
			if (boxSplitType.getValue() == Config.SIZE) {
				labelSplitSize.setVisible(true);
				boxSplitSize.setVisible(true);
				labelSplitTimeMin.setVisible(false);
				labelSplitTimeSec.setVisible(false);
				textSplitTimeMin.setVisible(false);
				textSplitTimeSec.setVisible(false);
			} else if (boxSplitType.getValue() == Config.TIME) {
				labelSplitSize.setVisible(false);
				boxSplitSize.setVisible(false);
				labelSplitTimeMin.setVisible(true);
				labelSplitTimeSec.setVisible(true);
				textSplitTimeMin.setVisible(true);
				textSplitTimeSec.setVisible(true);
			} else {
				System.out.println("WARNING: Invalid selection for 'Split Type' dropdown box");
			}
			labelWaveformType.setVisible(false);
			labelGainPrecision.setVisible(false);
			textParam1.setVisible(false);
			textParam2.setVisible(false);
			textParam1.setText("");
			textParam2.setText("");
			boxLoopType.setVisible(false);
			boxWaveformType.setVisible(false);
			boxGainPrecision.setVisible(false);
			if (analysisRunning || batch.openFiles.isEmpty() ||
				(!checkUseInputFolder.isSelected() && textDestination.getText().isEmpty()) ||
				(batch.splitTypeValue == Config.TIME &&
				(textSplitTimeMin.getText().isEmpty() && textSplitTimeSec.getText().isEmpty()) ||
				(textSplitTimeMin.getText().isEmpty() && textSplitTimeSec.getText().equals("0")) ||
				(textSplitTimeMin.getText().equals("0") && textSplitTimeSec.getText().isEmpty()) ||
				(textSplitTimeMin.getText().equals("0") && textSplitTimeSec.getText().equals("0")))) {
				batch.readyToRun = false;
			} else {
				batch.readyToRun = true;
			}
		} else if (boxToolSelect.getValue() == Config.ANALYSE_GAIN) {
			buttonDestination.setVisible(true);
			textDestination.setVisible(true);
			labelGainPrecision.setDisable(false);
			boxGainPrecision.setDisable(false);
			if (checkGenerateGraph.isSelected()) {
				labelWaveformType.setDisable(false);
				boxWaveformType.setDisable(false);
				checkUseInputFolder.setDisable(false);
				if (checkUseInputFolder.isSelected()) {
					buttonDestination.setDisable(true);
					textDestination.setDisable(true);
				} else {
					buttonDestination.setDisable(false);
					textDestination.setDisable(false);
				}
			} else {
				labelWaveformType.setDisable(true);
				boxWaveformType.setDisable(true);
				buttonDestination.setDisable(true);
				textDestination.setDisable(true);
				checkUseInputFolder.setDisable(true);
			}
			checkUseInputFolder.setVisible(true);
			checkDeleteOriginal.setVisible(false);
			checkGenerateGraph.setVisible(true);
			labelLoopParam1.setVisible(false);
			labelLoopParam2.setVisible(false);
			labelLoopType.setVisible(false);
			labelSplitType.setVisible(false);
			labelSplitSize.setVisible(false);
			labelSplitTimeMin.setVisible(false);
			labelSplitTimeSec.setVisible(false);
			labelWaveformType.setVisible(true);
			labelGainPrecision.setVisible(true);
			boxLoopType.setVisible(false);
			boxSplitType.setVisible(false);
			boxSplitSize.setVisible(false);
			textSplitTimeMin.setVisible(false);
			textSplitTimeSec.setVisible(false);
			boxWaveformType.setVisible(true);
			boxGainPrecision.setVisible(true);
			textParam1.setVisible(false);
			textParam2.setVisible(false);
			textParam1.setText("");
			textParam2.setText("");
			if (analysisRunning || batch.openFiles.isEmpty() || (checkGenerateGraph.isSelected() && textDestination.getText().isEmpty() && !checkUseInputFolder.isSelected())) {
				batch.readyToRun = false;
			} else {
				batch.readyToRun = true;
			}
		}
		buttonRun.setDisable(true);
		for (int b = 0; b < batchList.size(); b++) {
			if (batchList.get(b).readyToRun == true) {
				buttonRun.setDisable(false);
			}
		}
		labelBatchNumber.setText(String.valueOf(currentBatch + 1) + "/" + String.valueOf(batchList.size()));
		if (checkMultipleBatches.isSelected()) {
			labelBatchNumber.setDisable(false);
			buttonBatchAdd.setDisable(batchList.size() >= 99);
			buttonBatchRemove.setDisable(batchList.size() <= 1);
			buttonBatchLeft.setDisable(currentBatch <= 0);
			buttonBatchRight.setDisable(currentBatch >= batchList.size() - 1);
		} else {
			labelBatchNumber.setDisable(true);
			buttonBatchAdd.setDisable(true);
			buttonBatchRemove.setDisable(true);
			buttonBatchLeft.setDisable(true);
			buttonBatchRight.setDisable(true);
		}
	}
	
	public void refreshOptionsGrid() {
		if (boxOptionSelect.getValue() == Config.GENERAL) {
			for (javafx.scene.control.Control control : controlsOptionsGeneral) {
				control.setVisible(true);
			}
			for (javafx.scene.control.Control control : controlsOptionsOutput) {
				control.setVisible(false);
			}
			for (javafx.scene.control.Control control : controlsOptionsAppearance) {
				control.setVisible(false);
			}
			for (javafx.scene.control.Control control : controlsOptionsInfo) {
				control.setVisible(false);
			}
		} else if (boxOptionSelect.getValue() == Config.OUTPUT) {
			for (javafx.scene.control.Control control : controlsOptionsGeneral) {
				control.setVisible(false);
			}
			for (javafx.scene.control.Control control : controlsOptionsOutput) {
				control.setVisible(true);
			}
			for (javafx.scene.control.Control control : controlsOptionsAppearance) {
				control.setVisible(false);
			}
			for (javafx.scene.control.Control control : controlsOptionsInfo) {
				control.setVisible(false);
			}
		} else if (boxOptionSelect.getValue() == Config.APPEARANCE) {
			for (javafx.scene.control.Control control : controlsOptionsGeneral) {
				control.setVisible(false);
			}
			for (javafx.scene.control.Control control : controlsOptionsOutput) {
				control.setVisible(false);
			}
			for (javafx.scene.control.Control control : controlsOptionsAppearance) {
				control.setVisible(true);
			}
			for (javafx.scene.control.Control control : controlsOptionsInfo) {
				control.setVisible(false);
			}
		} else if (boxOptionSelect.getValue() == Config.INFO) {
			for (javafx.scene.control.Control control : controlsOptionsGeneral) {
				control.setVisible(false);
			}
			for (javafx.scene.control.Control control : controlsOptionsOutput) {
				control.setVisible(false);
			}
			for (javafx.scene.control.Control control : controlsOptionsAppearance) {
				control.setVisible(false);
			}
			for (javafx.scene.control.Control control : controlsOptionsInfo) {
				control.setVisible(true);
			}
		} else {
			System.out.println("WARNING: Invalid selection for 'Options' dropdown box");
		}
	}
	
	public void refreshCSS() {
		try {
			sceneTools.getStylesheets().clear();
			sceneOptions.getStylesheets().clear();
			String cssThemePath = Config.CURRENT_DIRECTORY + "/CSS/" + Config.THEME_VALUES[Config.THEME_INDEX];
			String cssFontSizePath = Config.CURRENT_DIRECTORY + "/CSS/" + Config.FONT_SIZE_VALUES[Config.FONT_SIZE_INDEX];
			String cssThemeURL = new File(cssThemePath).toURI().toURL().toString();
			String cssFontSizeURL = new File(cssFontSizePath).toURI().toURL().toString();
			sceneTools.getStylesheets().add(cssThemeURL);
			sceneTools.getStylesheets().add(cssFontSizeURL);
			sceneOptions.getStylesheets().add(cssThemeURL);
			sceneOptions.getStylesheets().add(cssFontSizeURL);
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}
	
	public void setApplicationOption(String option) {
		String previousOption = boxOptionSelect.getValue();
		boxOptionSelect.getSelectionModel().select(option);
		userSelectedOption = previousOption;
		refreshOptionsGrid();
	}
	
	// Change the current scene for the stage
	public void changeScene(Stage stage, Scene newScene, GridPane oldGrid, GridPane newGrid) {
		newGrid.setPrefWidth(oldGrid.getWidth());
		newGrid.setPrefHeight(oldGrid.getHeight());
		stage.setScene(newScene);
		stage.sizeToScene();
	}
	
	public static String cleanPathString(String path) {
		String cleanPath = path.replace("\\", "/");
		if (cleanPath.charAt(cleanPath.length() - 1) == '/') {
			cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
		}
		return cleanPath;
	}
	
	@Override
    public void start(Stage stage) {
		// Core Initialisation (1/2)
		analysisRunning = false;
		task = new FileProcessingTask();
		batchList = new ArrayList<ExecutionBatch>();
		batchList.add(new ExecutionBatch());
		currentBatch = 0;
		currentFileSizeLimit = Config.LOOP_SCAN_FILE_SIZE_LIMIT;
    
		// GUI Component Initialisation
		buttonBatchAdd = new Button("+");
		buttonBatchRemove = new Button("--");
		buttonBatchLeft = new Button("<<");
		buttonBatchRight = new Button(">>");
		labelBatchNumber = new Label("");
		boxOptionSelect = new ComboBox<String>();
		labelVersionNumber = new Label("Version " + Config.VERSION);
		buttonShowOptions = new Button("Options");
		buttonShowTools = new Button("Home");
		buttonResetGeneral = new Button("Set to Defaults");
		labelBufferSize = new Label("File Input Buffer:");
		labelAutoWindow = new Label("Auto Scan Threshold (sec):");
		labelSineWindow = new Label("Sine Tone Loop Size (sec):");
		labelOptimisationMode = new Label("Loop Scan Optimisation:");
		labelNoiseWindow = new Label("Noise Loop Size (sec):");
		labelLinearScanMode = new Label("Linear Scan Mode:");
		boxBufferSize = new ComboBox<String>();
		boxAutoWindow = new ComboBox<String>();
		boxSineWindow = new ComboBox<String>();
		boxNoiseWindow = new ComboBox<String>();
		boxOptimisationMode = new ComboBox<String>();
		boxLinearScanMode = new ComboBox<String>();
		buttonResetOutput = new Button("Set to Defaults");
		labelOutputFormat = new Label("File Format:");
		labelOutputResolution = new Label("Image Resolution:");
		labelVarianceScale = new Label("Variance Scale:");
		labelProbabilityScale = new Label("Probability Scale:");
		labelTimeScale = new Label("Time Scale (sec):");
		boxOutputFormat = new ComboBox<String>();
		boxOutputResolution = new ComboBox<String>();
		boxVarianceScale = new ComboBox<String>();
		boxProbabilityScale = new ComboBox<String>();
		boxTimeScale = new ComboBox<String>();
		buttonResetAppearance = new Button("Set to Defaults");
		labelThemeSelect = new Label("Visual Theme:");
		labelFontSize = new Label("Font Size:");
		labelProgressRefresh = new Label("Progress Refresh Rate (sec):");
		boxThemeSelect = new ComboBox<String>();
		boxFontSize = new ComboBox<String>();
		boxProgressRefresh = new ComboBox<String>();
		textInfo = new TextArea(Config.INFO_TEXT);
		textFiles = new TextArea();
		buttonOpenFile = new Button("Select File(s)");
		fileChooser = new FileChooser();
		buttonClearFile = new Button("Clear File(s)");
		boxToolSelect = new ComboBox<String>();
		buttonDestination = new Button("Output Location:");
		textDestination = new TextField();
		checkUseInputFolder = new CheckBox("Use Input Folder");
		checkMultipleBatches = new CheckBox("Multi-Batch");
		checkDeleteOriginal = new CheckBox("Delete Original");
		checkGenerateGraph = new CheckBox("Generate Graph");
		buttonRun = new Button("Run");
		buttonCancel = new Button("Cancel");
		labelLoopType = new Label("Loop Type:");
		labelSplitType = new Label("File Split Method:");
		labelSplitSize = new Label("Output Size (GB):");
		labelSplitTimeMin = new Label("Output Length (minutes):");
		labelSplitTimeSec = new Label("(seconds):");
		labelWaveformType = new Label("Window Size (sec):");
		labelGainPrecision = new Label("Precision (decimal places):");
		boxLoopType = new ComboBox<String>();
		boxSplitType = new ComboBox<String>();
		boxSplitSize = new ComboBox<String>();
		textSplitTimeMin = new TextField();
		textSplitTimeSec = new TextField();
		boxWaveformType = new ComboBox<String>();
		boxGainPrecision = new ComboBox<String>();
		labelLoopParam1 = new Label("Tempo (BPM):");
		textParam1 = new TextField();
		labelLoopParam2 = new Label("Number of Beats:");
		textParam2 = new TextField();
		destinationChooser = new DirectoryChooser();
		textOutput = new TextArea();
		alertFileSizeLimit = new Alert(AlertType.INFORMATION);
		
		// GUI Component Group Initialisation
		controlsOptionsGeneral = new ArrayList<javafx.scene.control.Control>();
		controlsOptionsOutput = new ArrayList<javafx.scene.control.Control>();
		controlsOptionsAppearance = new ArrayList<javafx.scene.control.Control>();
		controlsOptionsInfo = new ArrayList<javafx.scene.control.Control>();
		controlsTextOverrunClip = new ArrayList<javafx.scene.control.Labeled>();
		
		// GUI Component Group Population
			// Options
		controlsOptionsGeneral.add(buttonResetGeneral);
		controlsOptionsGeneral.add(labelBufferSize);
		controlsOptionsGeneral.add(labelAutoWindow);
		controlsOptionsGeneral.add(labelSineWindow);
		controlsOptionsGeneral.add(labelNoiseWindow);
		controlsOptionsGeneral.add(labelOptimisationMode);
		controlsOptionsGeneral.add(labelLinearScanMode);
		controlsOptionsGeneral.add(boxBufferSize);
		controlsOptionsGeneral.add(boxAutoWindow);
		controlsOptionsGeneral.add(boxSineWindow);
		controlsOptionsGeneral.add(boxNoiseWindow);
		controlsOptionsGeneral.add(boxOptimisationMode);
		controlsOptionsGeneral.add(boxLinearScanMode);
		controlsOptionsOutput.add(buttonResetOutput);
		controlsOptionsOutput.add(labelOutputFormat);
		controlsOptionsOutput.add(labelOutputResolution);
		controlsOptionsOutput.add(labelVarianceScale);
		controlsOptionsOutput.add(labelProbabilityScale);
		controlsOptionsOutput.add(labelTimeScale);
		controlsOptionsOutput.add(boxOutputFormat);
		controlsOptionsOutput.add(boxOutputResolution);
		controlsOptionsOutput.add(boxVarianceScale);
		controlsOptionsOutput.add(boxProbabilityScale);
		controlsOptionsOutput.add(boxTimeScale);
		controlsOptionsAppearance.add(buttonResetAppearance);
		controlsOptionsAppearance.add(labelThemeSelect);
		controlsOptionsAppearance.add(labelFontSize);
		controlsOptionsAppearance.add(labelProgressRefresh);
		controlsOptionsAppearance.add(boxThemeSelect);
		controlsOptionsAppearance.add(boxFontSize);
		controlsOptionsAppearance.add(boxProgressRefresh);
		controlsOptionsInfo.add(textInfo);
		
		// GUI Text Overrun Rule Group Population
		// These are all the GUI elements that contain some kind of text label.
		// This includes (but is not limited to) all JavaFX Label objects).
		// GUI text labels will be truncated if they exceeds the boundary of their element.
		controlsTextOverrunClip.add(buttonBatchLeft);
		controlsTextOverrunClip.add(buttonBatchRight);
		controlsTextOverrunClip.add(labelBatchNumber);
		controlsTextOverrunClip.add(buttonBatchAdd);
		controlsTextOverrunClip.add(buttonBatchRemove);
		controlsTextOverrunClip.add(buttonShowOptions);
		controlsTextOverrunClip.add(buttonShowTools);
		controlsTextOverrunClip.add(labelVersionNumber);
		controlsTextOverrunClip.add(buttonResetGeneral);
		controlsTextOverrunClip.add(labelBufferSize);
		controlsTextOverrunClip.add(labelAutoWindow);
		controlsTextOverrunClip.add(labelSineWindow);
		controlsTextOverrunClip.add(labelNoiseWindow);
		controlsTextOverrunClip.add(labelOptimisationMode);
		controlsTextOverrunClip.add(labelLinearScanMode);
		controlsTextOverrunClip.add(buttonResetOutput);
		controlsTextOverrunClip.add(labelOutputFormat);
		controlsTextOverrunClip.add(labelOutputResolution);
		controlsTextOverrunClip.add(labelVarianceScale);
		controlsTextOverrunClip.add(labelProbabilityScale);
		controlsTextOverrunClip.add(labelTimeScale);
		controlsTextOverrunClip.add(labelGainPrecision);
		controlsTextOverrunClip.add(buttonResetAppearance);
		controlsTextOverrunClip.add(labelThemeSelect);
		controlsTextOverrunClip.add(labelFontSize);
		controlsTextOverrunClip.add(labelProgressRefresh);
		controlsTextOverrunClip.add(buttonOpenFile);
		controlsTextOverrunClip.add(buttonClearFile);
		controlsTextOverrunClip.add(buttonDestination);
		controlsTextOverrunClip.add(checkUseInputFolder);
		controlsTextOverrunClip.add(checkMultipleBatches);
		controlsTextOverrunClip.add(checkDeleteOriginal);
		controlsTextOverrunClip.add(checkGenerateGraph);
		controlsTextOverrunClip.add(buttonRun);
		controlsTextOverrunClip.add(buttonCancel);
		controlsTextOverrunClip.add(labelLoopType);
		controlsTextOverrunClip.add(labelSplitSize);
		controlsTextOverrunClip.add(labelWaveformType);
		controlsTextOverrunClip.add(labelLoopParam1);
		controlsTextOverrunClip.add(labelLoopParam2);
		
		// GUI Component Settings
		textParam1.setTextFormatter(
			new TextFormatter<>(change -> change.getControlNewText().matches(intRegex) ? change : null));
		textParam2.setTextFormatter(
			new TextFormatter<>(change -> change.getControlNewText().matches(intRegex) ? change : null));
		textSplitTimeMin.setTextFormatter(
			new TextFormatter<>(change -> change.getControlNewText().matches(intRegex) ? change : null));
		textSplitTimeSec.setTextFormatter(
			new TextFormatter<>(change -> change.getControlNewText().matches(intSecondsRegex) ? change : null));
		boxOptionSelect.setItems(FXCollections.observableArrayList(optionTypes));
		boxOutputFormat.setItems(FXCollections.observableArrayList(outputTypes));
		boxOutputResolution.setItems(FXCollections.observableArrayList(resolutionTypes));
		boxVarianceScale.setItems(FXCollections.observableArrayList(varianceScaleTypes));
		boxProbabilityScale.setItems(FXCollections.observableArrayList(probabilityScaleTypes));
		boxTimeScale.setItems(FXCollections.observableArrayList(timeScaleTypes));
		boxBufferSize.setItems(FXCollections.observableArrayList(bufferTypes));
		boxAutoWindow.setItems(FXCollections.observableArrayList(windowTypes));
		boxSineWindow.setItems(FXCollections.observableArrayList(windowTypes));
		boxNoiseWindow.setItems(FXCollections.observableArrayList(windowTypes));
		boxOptimisationMode.setItems(FXCollections.observableArrayList(optimisationTypes));
		boxLinearScanMode.setItems(FXCollections.observableArrayList(linearScanModeTypes));
		boxThemeSelect.setItems(FXCollections.observableArrayList(themeTypes));
		boxFontSize.setItems(FXCollections.observableArrayList(fontSizeTypes));
		boxProgressRefresh.setItems(FXCollections.observableArrayList(progressRefreshTypes));
		boxToolSelect.setItems(FXCollections.observableArrayList(toolTypes));
		boxLoopType.setItems(FXCollections.observableArrayList(loopTypes));
		boxSplitType.setItems(FXCollections.observableArrayList(splitTypes));
		boxSplitSize.setItems(FXCollections.observableArrayList(splitSizes));
		boxWaveformType.setItems(FXCollections.observableArrayList(waveformTypes));
		boxGainPrecision.setItems(FXCollections.observableArrayList(gainPrecisionTypes));
		boxOptionSelect.getSelectionModel().selectFirst();
		userSelectedOption = boxOptionSelect.getValue();
		boxBufferSize.getSelectionModel().select(Config.BUFFER_SIZE_INDEX);
		boxAutoWindow.getSelectionModel().select(Config.AUTO_SCAN_WINDOW_INDEX);
		boxSineWindow.getSelectionModel().select(Config.SINE_SCAN_WINDOW_INDEX);
		boxNoiseWindow.getSelectionModel().select(Config.NOISE_SCAN_WINDOW_INDEX);
		boxOptimisationMode.getSelectionModel().select(Config.OPTIMISATION_INDEX);
		boxLinearScanMode.getSelectionModel().select(Config.LINEAR_SCAN_MODE_INDEX);
		boxOutputFormat.getSelectionModel().select(Config.OUTPUT_FORMAT_INDEX);
		boxOutputResolution.getSelectionModel().select(Config.OUTPUT_RESOLUTION_INDEX);
		boxVarianceScale.getSelectionModel().select(Config.VARIANCE_SCALE_INDEX);
		boxProbabilityScale.getSelectionModel().select(Config.PROBABILITY_SCALE_INDEX);
		boxTimeScale.getSelectionModel().select(Config.TIME_SCALE_INDEX);
		boxThemeSelect.getSelectionModel().select(Config.THEME_INDEX);
		boxFontSize.getSelectionModel().select(Config.FONT_SIZE_INDEX);
		boxProgressRefresh.getSelectionModel().select(Config.PROGRESS_REFRESH_INDEX);
		boxToolSelect.getSelectionModel().select(0);
		boxLoopType.getSelectionModel().select(0);
		boxSplitType.getSelectionModel().select(0);
		boxSplitSize.getSelectionModel().select(3);
		boxWaveformType.getSelectionModel().select(Config.WAVEFORM_INDEX_DEFAULT);
		boxGainPrecision.getSelectionModel().select(Config.GAIN_PRECISION_INDEX_DEFAULT);
		buttonRun.setDisable(true);
		buttonCancel.setDisable(true);
		labelBatchNumber.setDisable(true);
		buttonBatchAdd.setDisable(true);
		buttonBatchRemove.setDisable(true);
		buttonBatchLeft.setDisable(true);
		buttonBatchRight.setDisable(true);
		textInfo.setEditable(false);
		textFiles.setEditable(false);
		textDestination.setEditable(false);
		textOutput.setEditable(false);
		textInfo.setWrapText(true);
		textInfo.setPrefWidth(Config.TEXT_DEFAULT_WIDTH);
		buttonBatchAdd.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_NARROW);
		buttonBatchRemove.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_NARROW);
		buttonBatchLeft.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_NARROW);
		buttonBatchRight.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_NARROW);
		labelBatchNumber.setPrefWidth(Config.LABEL_BATCH_DEFAULT_WIDTH);
		buttonShowOptions.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_MEDIUM);
		buttonShowTools.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_MEDIUM);
		labelVersionNumber.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		buttonOpenFile.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_MEDIUM);
		buttonClearFile.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_MEDIUM);
		boxOptionSelect.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_MEDIUM);
		boxToolSelect.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_MEDIUM);
		checkUseInputFolder.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_MEDIUM);
		checkMultipleBatches.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_MEDIUM);
		checkDeleteOriginal.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_MEDIUM);
		checkGenerateGraph.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_MEDIUM);
		buttonRun.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_MEDIUM);
		buttonCancel.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_MEDIUM);
		buttonDestination.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_MEDIUM);
		labelLoopType.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		labelSplitType.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		labelSplitSize.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		labelSplitTimeMin.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		labelSplitTimeSec.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		labelWaveformType.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		labelGainPrecision.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		boxLoopType.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		boxSplitType.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		boxSplitSize.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		textSplitTimeMin.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		textSplitTimeSec.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		boxWaveformType.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		boxGainPrecision.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		labelLoopParam1.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		textParam1.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		labelLoopParam2.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		textParam2.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		buttonResetGeneral.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_MEDIUM);
		labelBufferSize.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		labelAutoWindow.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		labelSineWindow.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		labelNoiseWindow.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		labelOptimisationMode.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		labelLinearScanMode.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		buttonResetOutput.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_MEDIUM);
		labelOutputFormat.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		labelOutputResolution.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		labelVarianceScale.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		labelTimeScale.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		buttonResetAppearance.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_MEDIUM);
		labelThemeSelect.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		labelFontSize.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		labelProgressRefresh.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		boxBufferSize.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		boxAutoWindow.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		boxSineWindow.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		boxNoiseWindow.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		boxOptimisationMode.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		boxLinearScanMode.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		boxOutputFormat.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		boxOutputResolution.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		boxVarianceScale.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		boxProbabilityScale.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		boxTimeScale.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		boxThemeSelect.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		boxFontSize.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		boxProgressRefresh.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE);
		textDestination.setPrefWidth(Config.DESTINATION_DEFAULT_WIDTH);
		textInfo.setPrefHeight(Config.INFO_DEFAULT_HEIGHT);
		labelVersionNumber.setAlignment(Pos.CENTER_LEFT);
		labelBufferSize.setAlignment(Pos.CENTER_RIGHT);
		labelAutoWindow.setAlignment(Pos.CENTER_RIGHT);
		labelSineWindow.setAlignment(Pos.CENTER_RIGHT);
		labelNoiseWindow.setAlignment(Pos.CENTER_RIGHT);
		labelOptimisationMode.setAlignment(Pos.CENTER_RIGHT);
		labelLinearScanMode.setAlignment(Pos.CENTER_RIGHT);
		labelOutputFormat.setAlignment(Pos.CENTER_RIGHT);
		labelOutputResolution.setAlignment(Pos.CENTER_RIGHT);
		labelVarianceScale.setAlignment(Pos.CENTER_RIGHT);
		labelProbabilityScale.setAlignment(Pos.CENTER_RIGHT);
		labelTimeScale.setAlignment(Pos.CENTER_RIGHT);
		labelThemeSelect.setAlignment(Pos.CENTER_RIGHT);
		labelFontSize.setAlignment(Pos.CENTER_RIGHT);
		labelProgressRefresh.setAlignment(Pos.CENTER_RIGHT);
		textFiles.setPrefHeight(Config.TEXT_DEFAULT_HEIGHT);
		textOutput.setPrefHeight(Config.TEXT_DEFAULT_HEIGHT);
		labelBatchNumber.setAlignment(Pos.CENTER_LEFT);
		labelLoopType.setAlignment(Pos.CENTER_RIGHT);
		labelSplitType.setAlignment(Pos.CENTER_RIGHT);
		labelSplitSize.setAlignment(Pos.CENTER_RIGHT);
		labelSplitTimeMin.setAlignment(Pos.CENTER_RIGHT);
		labelSplitTimeSec.setAlignment(Pos.CENTER_RIGHT);
		labelWaveformType.setAlignment(Pos.CENTER_RIGHT);
		labelGainPrecision.setAlignment(Pos.CENTER_RIGHT);
		labelLoopParam1.setAlignment(Pos.CENTER_RIGHT);
		labelLoopParam2.setAlignment(Pos.CENTER_RIGHT);
		fileChooser.setTitle("Open Audio File(s)");
		supportedExtensions = new ArrayList<String>();
		for (String format : Config.LOOP_SCAN_SUPPORTED_FORMATS) { supportedExtensions.add("*." + format); }
		fileChooser.getExtensionFilters().setAll(new ExtensionFilter("Audio Files", supportedExtensions));
		destinationChooser.setTitle("Select Output Location");
		alertFileSizeLimit.setHeaderText("Cannot open audio file(s)");
		alertFileSizeLimit.setContentText("One or more files exceed " + String.valueOf(Math.floor(currentFileSizeLimit / 100000000.0) / 10.0) + "GB.\nPlease try again.");	// Arithmetic yields a file size in GB (with one decimal place)
		
		// GUI Component Text Overrun Setting (Labelled Components Only)
		for (javafx.scene.control.Labeled control : controlsTextOverrunClip) {
			control.setTextOverrun(OverrunStyle.CLIP);
		}
		
		// Core Initialisation (2/2)
		ExecutionBatch batch = batchList.get(currentBatch);
		task.initialise(boxToolSelect.getValue(), batchList, currentBatch, checkMultipleBatches.isSelected(), checkDeleteOriginal.isSelected(), checkGenerateGraph.isSelected(), textOutput);
		
		// GUI Component Layout
			// Grid Positions
				// Tools
		gridTools = new GridPane();
		gridTools.add(buttonBatchAdd,		 				0, 0, 1, 1);
		gridTools.add(buttonBatchRemove,		 			1, 0, 1, 1);
		gridTools.add(buttonBatchLeft,		 				2, 0, 1, 1);
		gridTools.add(buttonBatchRight,		 				3, 0, 1, 1);
		gridTools.add(buttonShowOptions,		 			5, 0, 1, 1);
		gridTools.add(labelBatchNumber,		 				4, 0, 1, 1);
		gridTools.add(buttonOpenFile,		 				0, 1, 2, 1);
        gridTools.add(buttonClearFile,		 				2, 1, 3, 1);
        gridTools.add(boxToolSelect,			 			5, 1, 1, 1);
		gridTools.add(textFiles,				 			0, 2, 6, 1);
		gridTools.add(buttonDestination,		 			0, 3, 2, 1);
		gridTools.add(textDestination, 		 				2, 3, 4, 1);
		gridTools.add(checkUseInputFolder,	 				0, 4, 2, 1);
		gridTools.add(checkMultipleBatches,	 				4, 1, 2, 1);
		gridTools.add(checkDeleteOriginal,	 				0, 5, 2, 1);
		gridTools.add(checkGenerateGraph,					0, 5, 2, 1);
		gridTools.add(buttonRun,				 			0, 6, 2, 1);
		gridTools.add(buttonCancel,			 				0, 7, 2, 1);
		gridTools.add(labelLoopType,			 			2, 4, 3, 1);
		gridTools.add(labelSplitType,		 				2, 4, 3, 1);
		gridTools.add(labelSplitSize,		 				2, 5, 3, 1);
		gridTools.add(labelSplitTimeMin,		 			2, 5, 3, 1);
		gridTools.add(labelSplitTimeSec,		 			2, 6, 3, 1);
		gridTools.add(labelWaveformType,		 			2, 4, 3, 1);
		gridTools.add(labelGainPrecision,		 			2, 7, 3, 1);
		gridTools.add(labelLoopParam1,		 				2, 5, 3, 1);
		gridTools.add(labelLoopParam2,		 				2, 6, 3, 1);
		gridTools.add(boxLoopType,			 				5, 4, 1, 1);
		gridTools.add(boxSplitType,			 				5, 4, 1, 1);
		gridTools.add(boxSplitSize,			 				5, 5, 1, 1);
		gridTools.add(textSplitTimeMin,			 			5, 5, 1, 1);
		gridTools.add(textSplitTimeSec,			 			5, 6, 1, 1);
		gridTools.add(boxWaveformType,		 				5, 4, 1, 1);
		gridTools.add(boxGainPrecision,			 			5, 7, 1, 1);
		gridTools.add(textParam1,			 				5, 5, 1, 1);
		gridTools.add(textParam2,			 				5, 6, 1, 1);
		gridTools.add(textOutput,			 				0, 8, 6, 1);
		gridTools.setHalignment(boxToolSelect,	  			HPos.RIGHT);
		gridTools.setHalignment(buttonShowOptions,			HPos.RIGHT);
		gridTools.setHgap(Config.DEFAULT_MARGIN_SIZE);
		gridTools.setVgap(Config.DEFAULT_MARGIN_SIZE);
		gridTools.setPadding(new Insets(Config.DEFAULT_MARGIN_SIZE));
				// Options
					// Overall
		gridOptions = new GridPane();
		gridOptions.add(boxOptionSelect,		 			0, 0, 1, 1);
		gridOptions.add(labelVersionNumber,	 	 			1, 0, 1, 1);
		gridOptions.add(buttonShowTools,	 	 			2, 0, 1, 1);
		gridOptions.setHalignment(boxOptionSelect,	  		HPos.LEFT);
		gridOptions.setHalignment(buttonShowTools, 			HPos.RIGHT);
					// General
		gridOptions.add(buttonResetGeneral, 	 			0, 1, 1, 1);
		gridOptions.add(labelBufferSize,		 			1, 1, 1, 1);
		gridOptions.add(boxBufferSize,			 			2, 1, 1, 1);
		gridOptions.add(labelAutoWindow,		 			1, 2, 1, 1);
		gridOptions.add(boxAutoWindow,			 			2, 2, 1, 1);
		gridOptions.add(labelSineWindow,		 			1, 3, 1, 1);
		gridOptions.add(boxSineWindow,			 			2, 3, 1, 1);
		gridOptions.add(labelNoiseWindow,		 			1, 4, 1, 1);
		gridOptions.add(boxNoiseWindow,			 			2, 4, 1, 1);
		gridOptions.add(labelOptimisationMode,		 		1, 5, 1, 1);
		gridOptions.add(boxOptimisationMode,			 	2, 5, 1, 1);
		gridOptions.add(labelLinearScanMode,				1, 6, 1, 1);
		gridOptions.add(boxLinearScanMode,					2, 6, 1, 1);
					// Output
		gridOptions.add(buttonResetOutput, 	 				0, 1, 1, 1);
		gridOptions.add(labelOutputFormat,		 			1, 1, 1, 1);
		gridOptions.add(boxOutputFormat,		 			2, 1, 1, 1);
		gridOptions.add(labelOutputResolution,	 			1, 2, 1, 1);
		gridOptions.add(boxOutputResolution,	 			2, 2, 1, 1);
		gridOptions.add(labelVarianceScale,		 			1, 3, 1, 1);
		gridOptions.add(boxVarianceScale,			 		2, 3, 1, 1);
		gridOptions.add(labelProbabilityScale,		 		1, 4, 1, 1);
		gridOptions.add(boxProbabilityScale,			 	2, 4, 1, 1);
		gridOptions.add(labelTimeScale,		 				1, 5, 1, 1);
		gridOptions.add(boxTimeScale,			 			2, 5, 1, 1);
					// Appearance
		gridOptions.add(buttonResetAppearance, 	 			0, 1, 1, 1);
		gridOptions.add(labelThemeSelect,		 			1, 1, 1, 1);
		gridOptions.add(boxThemeSelect,		 				2, 1, 1, 1);
		gridOptions.add(labelFontSize,	 					1, 2, 1, 1);
		gridOptions.add(boxFontSize,	 					2, 2, 1, 1);
		gridOptions.add(labelProgressRefresh,	 			1, 3, 1, 1);
		gridOptions.add(boxProgressRefresh,	 				2, 3, 1, 1);
					// Info
		gridOptions.add(textInfo, 				 			0, 1, 3, 6);
			// Alignment
				// Options
					// Overall
		gridOptions.setHalignment(labelVersionNumber, 		HPos.CENTER);
					// General
						// Column 1 (Buttons, Checkboxes)
		gridOptions.setValignment(buttonResetGeneral,		VPos.BASELINE);
						// Column 2 (Labels & Dropdown Boxes)
		gridOptions.setValignment(labelBufferSize,			VPos.CENTER);
		gridOptions.setValignment(boxBufferSize,		 	VPos.CENTER);
		gridOptions.setValignment(labelAutoWindow,			VPos.CENTER);
		gridOptions.setValignment(boxAutoWindow,		 	VPos.CENTER);
		gridOptions.setValignment(labelSineWindow,			VPos.CENTER);
		gridOptions.setValignment(boxSineWindow,		 	VPos.CENTER);		
		gridOptions.setValignment(labelNoiseWindow,			VPos.CENTER);
		gridOptions.setValignment(boxNoiseWindow,		 	VPos.CENTER);
		gridOptions.setValignment(labelOptimisationMode,	VPos.CENTER);
		gridOptions.setValignment(boxOptimisationMode,		VPos.CENTER);
		gridOptions.setValignment(labelLinearScanMode,		VPos.BASELINE);		// Bottom of each column must be BASLINE
		gridOptions.setValignment(boxLinearScanMode,		VPos.BASELINE);		// All others must be CENTER
					// Display
						// Column 1 (Buttons, Checkboxes)
		gridOptions.setValignment(buttonResetOutput,		VPos.BASELINE);
						// Column 2 (Labels & Dropdown Boxes)
		gridOptions.setValignment(labelOutputFormat,		VPos.CENTER);
		gridOptions.setValignment(boxOutputFormat,		 	VPos.CENTER);
		gridOptions.setValignment(labelOutputResolution,	VPos.CENTER);
		gridOptions.setValignment(boxOutputResolution,	 	VPos.CENTER);
		gridOptions.setValignment(labelVarianceScale,		VPos.CENTER);
		gridOptions.setValignment(boxVarianceScale,			VPos.CENTER);
		gridOptions.setValignment(labelProbabilityScale,	VPos.CENTER);
		gridOptions.setValignment(boxProbabilityScale,		VPos.CENTER);
		gridOptions.setValignment(labelTimeScale,			VPos.BASELINE);
		gridOptions.setValignment(boxTimeScale,				VPos.BASELINE);
					// Appearance
						// Column 1 (Buttons, Checkboxes)
		gridOptions.setValignment(buttonResetAppearance,	VPos.BASELINE);
						// Column 2 (Labels & Dropdown Boxes)
		gridOptions.setValignment(labelThemeSelect,			VPos.CENTER);
		gridOptions.setValignment(boxThemeSelect,		 	VPos.CENTER);
		gridOptions.setValignment(labelFontSize,			VPos.CENTER);
		gridOptions.setValignment(boxFontSize,	 			VPos.CENTER);
		gridOptions.setValignment(labelProgressRefresh,		VPos.BASELINE);
		gridOptions.setValignment(boxProgressRefresh,	 	VPos.BASELINE);
					// Info
		gridOptions.setValignment(textInfo,	 		 		VPos.CENTER);
			// Margins
		gridOptions.setHgap(Config.DEFAULT_MARGIN_SIZE);
		gridOptions.setVgap(Config.DEFAULT_MARGIN_SIZE);
		gridOptions.setPadding(new Insets(Config.DEFAULT_MARGIN_SIZE));
		// DEBUGGING
		// gridTools.setGridLinesVisible(true);
		// gridOptions.setGridLinesVisible(true);
		
		// GUI Scene Initialisation
		sceneTools = new Scene(gridTools);
		sceneOptions = new Scene(gridOptions);
        stage.setScene(sceneTools);
        stage.sizeToScene();
		
		// GUI Component Actions
		// All of these components should update their respective models explicitly
		buttonBatchAdd.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				batchList.add(new ExecutionBatch());
				refreshToolsGrid();
            }
        });
        buttonBatchRemove.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				batchList.remove(currentBatch);
				if (currentBatch >= batchList.size()) {
					currentBatch--;
				}
				populateToolsGrid();
				populateOptionsGrid();
            }
        });
        buttonBatchLeft.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				currentBatch--;
				populateToolsGrid();
				populateOptionsGrid();
            }
        });
        buttonBatchRight.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				currentBatch++;
				populateToolsGrid();
				populateOptionsGrid();
            }
        });
		buttonShowOptions.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				changeScene(stage, sceneOptions, gridTools, gridOptions);
            }
        });
        buttonShowTools.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				changeScene(stage, sceneTools, gridOptions, gridTools);
            }
        });
        boxOptionSelect.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				userSelectedOption = boxOptionSelect.getValue();
				refreshOptionsGrid();
            }
        });
        boxToolSelect.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				for (ExecutionBatch batch : batchList) {
					batch.openFiles.clear();
				}
				supportedExtensions = new ArrayList<String>();
				if (boxToolSelect.getValue() == Config.LOOP_SCAN) {
					currentFileSizeLimit = Config.LOOP_SCAN_FILE_SIZE_LIMIT;
					for (String format : Config.LOOP_SCAN_SUPPORTED_FORMATS) { supportedExtensions.add("*." + format); }
				} else if (boxToolSelect.getValue() == Config.LINEAR_SCAN) {
					currentFileSizeLimit = Config.LINEAR_SCAN_FILE_SIZE_LIMIT;
					for (String format : Config.LINEAR_SCAN_SUPPORTED_FORMATS) { supportedExtensions.add("*." + format); }
				} else if (boxToolSelect.getValue() == Config.FILE_SPLIT) {
					currentFileSizeLimit = Config.FILE_SPLIT_FILE_SIZE_LIMIT;
					for (String format : Config.FILE_SPLIT_SUPPORTED_FORMATS) { supportedExtensions.add("*." + format); }
				} else if (boxToolSelect.getValue() == Config.ANALYSE_GAIN) {
					currentFileSizeLimit = Config.ANALYSE_GAIN_FILE_SIZE_LIMIT;
					for (String format : Config.ANALYSE_GAIN_SUPPORTED_FORMATS) { supportedExtensions.add("*." + format); }
				}
				fileChooser.getExtensionFilters().setAll(new ExtensionFilter("Audio Files", supportedExtensions));
				alertFileSizeLimit.setContentText("One or more files exceed " + String.valueOf(Math.floor(currentFileSizeLimit / 100000000.0) / 10.0) + "GB.\nPlease try again.");
				populateToolsGrid();
				populateOptionsGrid();
            }
        });
        buttonResetGeneral.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				Config.BUFFER_SIZE_INDEX			= Config.BUFFER_SIZE_INDEX_DEFAULT;
				Config.AUTO_SCAN_WINDOW_INDEX		= Config.AUTO_SCAN_WINDOW_INDEX_DEFAULT;
				Config.SINE_SCAN_WINDOW_INDEX		= Config.SINE_SCAN_WINDOW_INDEX_DEFAULT;
				Config.NOISE_SCAN_WINDOW_INDEX 		= Config.NOISE_SCAN_WINDOW_INDEX_DEFAULT;
				Config.OPTIMISATION_INDEX			= Config.OPTIMISATION_INDEX_DEFAULT;
				Config.LINEAR_SCAN_MODE_INDEX		= Config.LINEAR_SCAN_MODE_INDEX_DEFAULT;
				populateOptionsGrid();
				refreshToolsGrid();
            }
        });
        boxBufferSize.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				Config.BUFFER_SIZE_INDEX = bufferTypes.indexOf(boxBufferSize.getValue());
				refreshOptionsGrid();
				refreshToolsGrid();
            }
        });
        boxAutoWindow.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				Config.AUTO_SCAN_WINDOW_INDEX = windowTypes.indexOf(boxAutoWindow.getValue());
				refreshOptionsGrid();
				refreshToolsGrid();
            }
        });
        boxSineWindow.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				Config.SINE_SCAN_WINDOW_INDEX = windowTypes.indexOf(boxSineWindow.getValue());
				refreshOptionsGrid();
				refreshToolsGrid();
            }
        });
        boxNoiseWindow.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				Config.NOISE_SCAN_WINDOW_INDEX = windowTypes.indexOf(boxNoiseWindow.getValue());
				refreshOptionsGrid();
				refreshToolsGrid();
            }
        });
        boxOptimisationMode.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				Config.OPTIMISATION_INDEX = optimisationTypes.indexOf(boxOptimisationMode.getValue());
				refreshOptionsGrid();
				refreshToolsGrid();
            }
        });
        boxLinearScanMode.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				Config.LINEAR_SCAN_MODE_INDEX = linearScanModeTypes.indexOf(boxLinearScanMode.getValue());
				refreshOptionsGrid();
				refreshToolsGrid();
            }
        });
        buttonResetOutput.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				Config.OUTPUT_FORMAT_INDEX			= Config.OUTPUT_FORMAT_INDEX_DEFAULT;
				Config.OUTPUT_RESOLUTION_INDEX		= Config.OUTPUT_RESOLUTION_INDEX_DEFAULT;
				Config.VARIANCE_SCALE_INDEX			= Config.VARIANCE_SCALE_INDEX_DEFAULT;
				Config.PROBABILITY_SCALE_INDEX		= Config.PROBABILITY_SCALE_INDEX_DEFAULT;
				Config.TIME_SCALE_INDEX				= Config.TIME_SCALE_INDEX_DEFAULT;
				populateOptionsGrid();
				refreshToolsGrid();
            }
        });
        boxOutputFormat.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				Config.OUTPUT_FORMAT_INDEX = outputTypes.indexOf(boxOutputFormat.getValue());
				refreshOptionsGrid();
				refreshToolsGrid();
            }
        });
        boxOutputResolution.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				Config.OUTPUT_RESOLUTION_INDEX = resolutionTypes.indexOf(boxOutputResolution.getValue());
				refreshOptionsGrid();
				refreshToolsGrid();
            }
        });
        boxVarianceScale.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				Config.VARIANCE_SCALE_INDEX = varianceScaleTypes.indexOf(boxVarianceScale.getValue());
				refreshOptionsGrid();
				refreshToolsGrid();
            }
        });
        boxProbabilityScale.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				Config.PROBABILITY_SCALE_INDEX = probabilityScaleTypes.indexOf(boxProbabilityScale.getValue());
				refreshOptionsGrid();
				refreshToolsGrid();
            }
        });
        boxTimeScale.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				Config.TIME_SCALE_INDEX = timeScaleTypes.indexOf(boxTimeScale.getValue());
				refreshOptionsGrid();
				refreshToolsGrid();
            }
        });
        buttonResetAppearance.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				Config.THEME_INDEX					= Config.THEME_INDEX_DEFAULT;
				Config.FONT_SIZE_INDEX				= Config.FONT_SIZE_INDEX_DEFAULT;
				Config.PROGRESS_REFRESH_INDEX		= Config.PROGRESS_REFRESH_INDEX_DEFAULT;
				refreshCSS();
				populateOptionsGrid();
				refreshToolsGrid();
            }
        });
        boxThemeSelect.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				Config.THEME_INDEX = themeTypes.indexOf(boxThemeSelect.getValue());
				refreshCSS();
				refreshOptionsGrid();
				refreshToolsGrid();
            }
        });
        boxFontSize.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				Config.FONT_SIZE_INDEX = fontSizeTypes.indexOf(boxFontSize.getValue());
				refreshCSS();
				refreshOptionsGrid();
				refreshToolsGrid();
            }
        });
        boxProgressRefresh.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				Config.PROGRESS_REFRESH_INDEX = progressRefreshTypes.indexOf(boxProgressRefresh.getValue());
				refreshOptionsGrid();
				refreshToolsGrid();
            }
        });
		buttonOpenFile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				List<File> openFilesList = fileChooser.showOpenMultipleDialog(stage);
				if (openFilesList != null) {
					boolean validFiles = true;
					for (File file : openFilesList) {
						String fileName = file.getName();
						String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
						if ((boxToolSelect.getValue() == Config.ANALYSE_GAIN && file.length() > currentFileSizeLimit) ||
							(boxToolSelect.getValue() == Config.FILE_SPLIT && file.length() > currentFileSizeLimit &&
							!fileExt.equals("wav")) ||
							(boxToolSelect.getValue() == Config.LOOP_SCAN && file.length() > currentFileSizeLimit) ||
							(boxToolSelect.getValue() == Config.LINEAR_SCAN && file.length() > currentFileSizeLimit)) {
							validFiles = false;
							break;
						}
					}
					if (validFiles) {
						ExecutionBatch batch = batchList.get(currentBatch);
						batch.openFiles.clear();
						batch.openFiles.addAll(openFilesList);
						textOutput.clear();
					} else {
						alertFileSizeLimit.show();
					}
				}
				populateToolsGrid();
				populateOptionsGrid();
            }
        });
        buttonClearFile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				ExecutionBatch batch = batchList.get(currentBatch);
				batch.openFiles.clear();
				populateToolsGrid();
				populateOptionsGrid();
				task = shallowCopyOfTask(task);	// Fulfilled tasks can't be re-used
            }
        });
		buttonRun.setOnAction(new EventHandler<ActionEvent>() {
			public void enableControls() {
				ExecutionBatch batch = batchList.get(currentBatch);	// This is the only case of core dependency for GUI behaviour
				if (checkMultipleBatches.isSelected()) {
					labelBatchNumber.setDisable(false);
					buttonBatchAdd.setDisable(batchList.size() >= 99);
					buttonBatchRemove.setDisable(batchList.size() <= 1);
					buttonBatchLeft.setDisable(currentBatch <= 0);
					buttonBatchRight.setDisable(currentBatch >= batchList.size() - 1);
				} else {
					labelBatchNumber.setDisable(true);
					buttonBatchAdd.setDisable(true);
					buttonBatchRemove.setDisable(true);
					buttonBatchLeft.setDisable(true);
					buttonBatchRight.setDisable(true);
				}
				buttonOpenFile.setDisable(false);
				buttonClearFile.setDisable(false);
				boxToolSelect.setDisable(false);
				buttonRun.setDisable(false);
				buttonCancel.setDisable(true);
				if (!checkUseInputFolder.isSelected()) {
					buttonDestination.setDisable(false);
					textDestination.setDisable(false);
				}
				checkUseInputFolder.setDisable(false);
				checkMultipleBatches.setDisable(false);
				checkDeleteOriginal.setDisable(false);
				checkGenerateGraph.setDisable(false);
				boxLoopType.setDisable(false);
				boxSplitType.setDisable(false);
				boxSplitSize.setDisable(false);
				textSplitTimeMin.setDisable(false);
				textSplitTimeSec.setDisable(false);
				boxWaveformType.setDisable(false);
				boxGainPrecision.setDisable(false);
				boxLinearScanMode.setDisable(false);
				textParam1.setDisable(false);
				textParam2.setDisable(false);
				analysisRunning = false;
				refreshToolsGrid();
				if (stage.getScene() != sceneOptions && !userSelectedOption.equals(Config.INFO)) {
					setApplicationOption(userSelectedOption);
				}
				userSelectedOption = boxOptionSelect.getValue(); // Re-synchronise 'userSelectedOption' with the currently-visible option
				boxOptionSelect.setDisable(false);
			}
			public void disableControls() {
				buttonBatchAdd.setDisable(true);
				buttonBatchRemove.setDisable(true);
				buttonBatchLeft.setDisable(true);
				buttonBatchRight.setDisable(true);
				buttonOpenFile.setDisable(true);
				buttonClearFile.setDisable(true);
				boxToolSelect.setDisable(true);
				buttonRun.setDisable(true);
				buttonCancel.setDisable(false);
				buttonDestination.setDisable(true);
				checkUseInputFolder.setDisable(true);
				checkMultipleBatches.setDisable(true);
				checkDeleteOriginal.setDisable(true);
				checkGenerateGraph.setDisable(true);
				boxLoopType.setDisable(true);
				boxSplitType.setDisable(true);
				boxSplitSize.setDisable(true);
				textSplitTimeMin.setDisable(true);
				textSplitTimeSec.setDisable(true);
				boxWaveformType.setDisable(true);
				boxGainPrecision.setDisable(true);
				boxLinearScanMode.setDisable(true);
				textParam1.setDisable(true);
				textParam2.setDisable(true);
				analysisRunning = true;
				setApplicationOption(Config.INFO);
				boxOptionSelect.setDisable(true);
			}
            @Override
            public void handle(ActionEvent event) {
                try {
					textOutput.clear();
					task.initialise(boxToolSelect.getValue(), batchList, currentBatch, checkMultipleBatches.isSelected(), checkDeleteOriginal.isSelected(), checkGenerateGraph.isSelected(), textOutput);
					task.setOnRunning(runningEvent -> {
						disableControls();
					});
					task.setOnCancelled(cancelledEvent -> {
						task = shallowCopyOfTask(task);	// Fulfilled tasks can't be re-used
						System.gc();
						enableControls();
						textOutput.clear();
					});
					task.setOnFailed(failedEvent -> {
						task.getException().printStackTrace(System.err);
						task = shallowCopyOfTask(task);	// Fulfilled tasks can't be re-used
						System.gc();
						enableControls();
						textOutput.clear();
					});
					task.setOnSucceeded(succeededEvent -> {
						task = shallowCopyOfTask(task);	// Fulfilled tasks can't be re-used
						System.gc();
						enableControls();
					});
					ExecutorService executorService = Executors.newSingleThreadExecutor();
					executorService.execute(task);
					executorService.shutdown();
				} catch (Exception e) {
					System.out.println(e.toString());
				}
            }
        });
        buttonCancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				task.cancel();
				textOutput.clear();
				refreshToolsGrid();
            }
        });
        buttonDestination.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				ExecutionBatch batch = batchList.get(currentBatch);
				File destination = destinationChooser.showDialog(stage);
				if (destination != null && !destination.getPath().equals("")) {
					batch.outputLocation = destination;
				}
				populateToolsGrid();
				populateOptionsGrid();
            }
        });
        checkUseInputFolder.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				ExecutionBatch batch = batchList.get(currentBatch);
				batch.useInputFolder = checkUseInputFolder.isSelected();
				refreshToolsGrid();
				populateOptionsGrid();
            }
        });
        checkMultipleBatches.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				populateToolsGrid();
				populateOptionsGrid();
            }
        });
        checkDeleteOriginal.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				populateToolsGrid();
				populateOptionsGrid();
            }
        });
        checkGenerateGraph.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				populateToolsGrid();
				populateOptionsGrid();
            }
        });
		boxLoopType.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				textParam1.clear();
				textParam2.clear();
				ExecutionBatch batch = batchList.get(currentBatch);
				batch.param1 = -1;
				batch.param2 = -1;
				batch.loopIndex = loopTypes.indexOf(boxLoopType.getValue());
				refreshToolsGrid();
            }
        });
        boxSplitType.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				ExecutionBatch batch = batchList.get(currentBatch);
				batch.splitTypeIndex = splitTypes.indexOf(boxSplitType.getValue());
				batch.splitTypeValue = boxSplitType.getValue();
				refreshToolsGrid();
            }
        });
        boxSplitSize.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				ExecutionBatch batch = batchList.get(currentBatch);
				batch.splitSizeIndex = splitSizes.indexOf(boxSplitSize.getValue());
				batch.splitSizeValue = Float.parseFloat(splitSizes.get(batch.splitSizeIndex));
				refreshToolsGrid();
            }
        });
        textSplitTimeMin.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
				ExecutionBatch batch = batchList.get(currentBatch);
				batch.splitTimeMin = textSplitTimeMin.getText().isEmpty() ? -1 : Integer.parseInt(textSplitTimeMin.getText());
				refreshToolsGrid();
            }
        });
        textSplitTimeSec.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
				ExecutionBatch batch = batchList.get(currentBatch);
				batch.splitTimeSec = textSplitTimeSec.getText().isEmpty() ? -1 : Integer.parseInt(textSplitTimeSec.getText());
				refreshToolsGrid();
            }
        });
        boxWaveformType.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				ExecutionBatch batch = batchList.get(currentBatch);
				batch.waveformIndex = waveformTypes.indexOf(boxWaveformType.getValue());
				batch.waveformValue = Float.parseFloat(waveformTypes.get(batch.waveformIndex));
				refreshToolsGrid();
            }
        });
        boxGainPrecision.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
				ExecutionBatch batch = batchList.get(currentBatch);
				batch.gainPrecisionIndex = gainPrecisionTypes.indexOf(boxGainPrecision.getValue());
				batch.gainPrecisionValue = Integer.parseInt(gainPrecisionTypes.get(batch.gainPrecisionIndex));
				refreshToolsGrid();
            }
        });
		textParam1.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
				ExecutionBatch batch = batchList.get(currentBatch);
				batch.param1 = textParam1.getText().isEmpty() ? -1 : Integer.parseInt(textParam1.getText());
				refreshToolsGrid();
            }
        });
		textParam2.setOnKeyTyped(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
				ExecutionBatch batch = batchList.get(currentBatch);
				batch.param2 = textParam2.getText().isEmpty() ? -1 : Integer.parseInt(textParam2.getText());
				refreshToolsGrid();
            }
        });
        
        // GUI Window Resize Actions
        stage.widthProperty().addListener((observable, oldValue, newValue) -> {
			double widthDifference = stage.getWidth() - Config.STAGE_DEFAULT_WIDTH;
			// Tools
			checkMultipleBatches.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_MEDIUM + widthDifference);
			labelBatchNumber.setPrefWidth(Config.LABEL_BATCH_DEFAULT_WIDTH + widthDifference);
			textFiles.setPrefWidth(Config.TEXT_DEFAULT_WIDTH + widthDifference);
			textDestination.setPrefWidth(Config.DESTINATION_DEFAULT_WIDTH + widthDifference);
			labelLoopType.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
			labelSplitSize.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
			labelSplitType.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
			labelSplitTimeMin.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
			labelSplitTimeSec.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
			labelWaveformType.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
			labelLoopParam1.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
			labelLoopParam2.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
			textOutput.setPrefWidth(Config.TEXT_DEFAULT_WIDTH + widthDifference);
			// Options
				// Overall
			labelVersionNumber.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
				// General
			labelBufferSize.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
			labelAutoWindow.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
			labelSineWindow.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
			labelNoiseWindow.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
			labelOptimisationMode.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
			labelLinearScanMode.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
				// Output
			labelOutputFormat.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
			labelOutputResolution.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
			labelVarianceScale.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
			labelProbabilityScale.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
			labelTimeScale.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
				// Appearance
			labelThemeSelect.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
			labelFontSize.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
			labelProgressRefresh.setPrefWidth(Config.CONTROL_DEFAULT_WIDTH_WIDE + widthDifference);
				// Info
			textInfo.setPrefWidth(Config.TEXT_DEFAULT_WIDTH + widthDifference);
		});

		stage.heightProperty().addListener((observable, oldValue, newValue) -> {
			double heightDifference = stage.getHeight() - Config.STAGE_DEFAULT_HEIGHT;
			// Tools
			textFiles.setPrefHeight(Config.TEXT_DEFAULT_HEIGHT + Math.floor(heightDifference / 2.0));
			textOutput.setPrefHeight(Config.TEXT_DEFAULT_HEIGHT + Math.ceil(heightDifference / 2.0));
			// Options
				// Info
			textInfo.setPrefHeight(Config.INFO_DEFAULT_HEIGHT + heightDifference);
		});
		
		// GUI Details
        stage.setTitle("Audiscope");
		// stage.getIcons().add(new Image("file:" + Config.CURRENT_DIRECTORY + "/Images/audiscope.png"));
		
		// GUI Execution
		stage.setResizable(true);
		stage.setMinWidth(Config.STAGE_MIN_WIDTH);
		stage.setMinHeight(Config.STAGE_MIN_HEIGHT);
		stage.setWidth(Config.STAGE_DEFAULT_WIDTH);
		stage.setHeight(Config.STAGE_DEFAULT_HEIGHT);
		refreshCSS();
		refreshToolsGrid();       
        refreshOptionsGrid();
        stage.show();
    }
    
    @SuppressWarnings({"deprecation", "fallthrough", "finally", "path", "serial", "unchecked"})
	public static void main(String[] args) {
		Config.CURRENT_DIRECTORY = args.length > 0 ? cleanPathString(args[0]) : ".";	// Convert Windows path separators
		launch(args);
	}
}