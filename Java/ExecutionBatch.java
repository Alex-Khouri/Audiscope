/*
* Audiscope
* 
* Author: Alexander Khouri
* Date: September 2024
*/

import java.io.*;
import java.util.*;

public class ExecutionBatch {
	public ArrayList<File> openFiles;
	public int loopIndex;
	public int splitTypeIndex;
	public String splitTypeValue;
	public int splitSizeIndex;
	public float splitSizeValue;
	public int splitTimeMin;
	public int splitTimeSec;
	public int waveformIndex;
	public float waveformValue;
	public int gainPrecisionIndex;
	public int gainPrecisionValue;
	public int param1;
	public int param2;
	public File outputLocation;
	public boolean useInputFolder;
	public boolean readyToRun;
	
	ExecutionBatch() {
		this.openFiles = new ArrayList<File>();
		this.loopIndex = 0;
		this.splitTypeIndex = 0;
		this.splitTypeValue = Config.SIZE;
		this.splitSizeIndex = 3;
		this.splitSizeValue = 1.0f;
		this.splitTimeMin = -1;
		this.splitTimeSec = -1;
		this.waveformIndex = Config.WAVEFORM_INDEX_DEFAULT;
		this.waveformValue = Config.WAVEFORM_VALUE_DEFAULT;
		this.gainPrecisionIndex = Config.GAIN_PRECISION_INDEX_DEFAULT;
		this.gainPrecisionValue = Config.GAIN_PRECISION_VALUE_DEFAULT;
		this.param1 = -1;
		this.param2 = -1;
		this.outputLocation = null;
		this.useInputFolder = false;
		this.readyToRun = false;
	}
}