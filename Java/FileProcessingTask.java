/*
* Audiscope
* 
* Author: Alexander Khouri
* Date: September 2024
*/

import java.awt.Color;
import java.io.*;
import java.net.*;
import java.util.*;

import javafx.concurrent.Task;
import javafx.scene.control.TextArea;

import javax.sound.sampled.*;
import javax.sound.sampled.AudioFileFormat.*;
import javax.sound.sampled.spi.*;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.ui.*;
import org.jfree.chart.util.*;
import org.jfree.data.*;
import org.jfree.data.time.*;
import org.jfree.data.xy.*;

public class FileProcessingTask extends Task<Void> {
	public String tool;
	public ArrayList<ExecutionBatch> batchList;
	public int currentBatch;
	public boolean useMultipleBatches;
	public boolean deleteOriginal;
	public boolean generateGraph;
	public TextArea textOutput;
	public String outputProgressText;
	public long lastProgressOutputTime;	// Milliseconds; regulates frequency of text output to prevent crashes
	
	public FileProcessingTask() { }
	
	public void initialise(String tool, ArrayList<ExecutionBatch> batchList, int currentBatch,
							boolean useMultipleBatches, boolean deleteOriginal, boolean generateGraph, TextArea textOutput) {
		this.tool = tool;
		this.batchList = batchList;
		this.currentBatch = currentBatch;
		this.useMultipleBatches = useMultipleBatches;
		this.deleteOriginal = deleteOriginal;
		this.generateGraph = generateGraph;
		this.textOutput = textOutput;
		this.outputProgressText = "";
		this.lastProgressOutputTime = -1;	// Uninitialised
	}
	
	@Override
	protected Void call() throws Exception {
		try {
			for (int b = 0; b < batchList.size(); b++) {
				if (!useMultipleBatches) {
					b = currentBatch;
				}
				ExecutionBatch batch = batchList.get(b);
				if (tool.equals(Config.LOOP_SCAN)) {
					ArrayList<File> openFiles = batch.openFiles;
					if (openFiles.size() < 1) continue;
					int loopType = batch.loopIndex;
					int param1 = batch.param1;
					int param2 = batch.param2;
					File outputLocation = batch.outputLocation;
					boolean useInputFolder = batch.useInputFolder;
					boolean autoLengthDetection = (batch.loopIndex == 2);
					int minLoop = -1;	// i.e. Uninitialised
					int maxLoop = -1;
					if (loopType == 0) {
						double tempo = param1;
						double beats = param2;
						minLoop = (int) Math.floor((60.0 / tempo) * beats) - 2;
						maxLoop = (int) Math.ceil((60.0 / tempo) * beats) + 2;
					}
					else if (loopType == 1) {
						minLoop = param1;
						maxLoop = param2;
					}
					int loopLength = -1;
					int loopFrameRate = -1;
					for (File file : openFiles) {
						if (taskCancelled()) return null;
						String fileName = file.getName();
						this.printOut("Loading file: " + fileName, true, false);
						if (taskCancelled()) return null;
						AudioData audioData = getAudioData(file);
						int[] data = audioData.data;
						AudioFormat metaData = audioData.metaData;
						int currentSampleRate = Math.round(metaData.getSampleRate());
						if (taskCancelled()) return null;
						if ((loopType == 0 || loopType == 1 || loopType == 2)) {	// Music
							if (loopLength == -1) {		// Occurs during the first file in a batch
								this.printOut("Calculating length of audio loop...", true, false);
								loopFrameRate = currentSampleRate;
								loopLength = getLoopLength(data, loopFrameRate, minLoop, maxLoop, autoLengthDetection);
								if (taskCancelled()) return null;
								if (loopLength == -1) {
									throw new Exception("Error: Unable to calculate length of audio loop");
								}
							}	// For all subsequent files in a batch, no action is required
						} else if (loopType == 3) {		// Sine Tone
							loopLength = currentSampleRate * Config.SCAN_WINDOW_VALUES[Config.SINE_SCAN_WINDOW_INDEX];
							loopFrameRate = currentSampleRate;
						} else if (loopType == 4) {		// Noise
							loopLength = currentSampleRate * Config.SCAN_WINDOW_VALUES[Config.NOISE_SCAN_WINDOW_INDEX];
							loopFrameRate = currentSampleRate;
						} else {
							throw new Exception("Error: Invalid loop type");
						}
						int currentLoopLength = Math.round(((float)currentSampleRate / (float)loopFrameRate) * (float)loopLength);
						this.printOut("Analysing audio quality...", true, false);
						double[] variances = getLoopVariances(currentLoopLength, data, currentSampleRate);
						if (taskCancelled()) return null;
						this.printOut("Generating output files...", true, false);
						File outputPath = useInputFolder ? file.getParentFile() : outputLocation;
						drawAnalyticsOne(currentSampleRate, variances, fileName, outputPath, Config.LOOP_SCAN, "");
						if (taskCancelled()) return null;
						this.printOut("Analysis complete!", true, false);
					}
				} else if (tool.equals(Config.LINEAR_SCAN)) {
					ArrayList<File> openFiles = batch.openFiles;
					if (openFiles.size() < 1) continue;
					File outputLocation = batch.outputLocation;
					boolean useInputFolder = batch.useInputFolder;
					for (File file : openFiles) {
						if (taskCancelled()) return null;
						String fileName = file.getName();
						this.printOut("Loading file: " + fileName, true, false);
						if (taskCancelled()) return null;
						AudioData audioData = getAudioData(file);
						int[] data = audioData.data;
						AudioFormat metaData = audioData.metaData;
						int currentSampleRate = Math.round(metaData.getSampleRate());
						int bitDepth = metaData.getSampleSizeInBits();
						if (taskCancelled()) return null;
						File outputPath = useInputFolder ? file.getParentFile() : outputLocation;
						String linearScanMode = Config.LINEAR_SCAN_MODE_VALUES[Config.LINEAR_SCAN_MODE_INDEX];
						if (linearScanMode.equals(Config.DUAL_COMBINED) || linearScanMode.equals(Config.DUAL_SEPARATE)) {
							this.printOut("Analysing audio quality (1/2)...", true, false);
							double[] cutoutProbabilities = getSignalCutoutData(data, currentSampleRate, bitDepth);
							if (taskCancelled()) return null;
							this.printOut("Analysing audio quality (2/2)...", true, false);
							double[] gradientProbabilities = getSignalGradientData(data, currentSampleRate, bitDepth);
							if (taskCancelled()) return null;
							this.printOut("Consolidating analysis results...", true, false);
							normaliseDatasets(cutoutProbabilities, gradientProbabilities);
							if (taskCancelled()) return null;
							this.printOut("Generating output files...", true, false);
							if (linearScanMode.equals(Config.DUAL_COMBINED)) {
								drawAnalyticsTwo(currentSampleRate, cutoutProbabilities, gradientProbabilities,
												 fileName, outputPath, Config.LINEAR_SCAN, "");
							} else { // if linearScanMode.equals(Config.DUAL_SEPARATE)
								drawAnalyticsOne(currentSampleRate, cutoutProbabilities, fileName, outputPath,
												 Config.LINEAR_SCAN, "[AS Linear 1]");
								drawAnalyticsOne(currentSampleRate, gradientProbabilities, fileName, outputPath,
												 Config.LINEAR_SCAN, "[AS Linear 2]");
							}
						} else if (linearScanMode.equals(Config.SIGNAL_CUTOUT) || linearScanMode.equals(Config.SIGNAL_GRADIENT)) {
							this.printOut("Analysing audio quality...", true, false);
							double[] probabilities = {};
							if (linearScanMode.equals(Config.SIGNAL_CUTOUT)) {
								probabilities = getSignalCutoutData(data, currentSampleRate, bitDepth);
							} else { // if linearScanMode.equals(Config.SIGNAL_GRADIENT)
								probabilities = getSignalGradientData(data, currentSampleRate, bitDepth);
							}
							if (taskCancelled()) return null;
							this.printOut("Generating output files...", true, false);
							drawAnalyticsOne(currentSampleRate, probabilities, fileName, outputPath, Config.LINEAR_SCAN, "");
						} else {
							throw new Exception("Error: Invalid linear scan mode");
						}
						if (taskCancelled()) return null;
						this.printOut("Analysis complete!", true, false);
					}
				} else if (tool.equals(Config.FILE_SPLIT)) {
					ArrayList<File> openFiles = batch.openFiles;
					if (openFiles.size() < 1) continue;
					File outputLocation = batch.outputLocation;
					boolean useInputFolder = batch.useInputFolder;
					float splitLimit = batch.splitSizeValue;		// Measured in GB
					for (File file : openFiles) {
						if (taskCancelled()) return null;
						String fileName = file.getName();
						File outputPath = useInputFolder ? file.getParentFile() : outputLocation;
						this.printOut("Splitting file: " + fileName, true, false);
						if (batch.splitTypeValue == Config.TIME) {
							AudioFormat metaData = getAudioData(file).metaData;
							long sampleRate = Math.round(metaData.getSampleRate());
							long bitDepth = metaData.getSampleSizeInBits();
							long channels = metaData.getChannels();
							long splitMin = Math.max(batch.splitTimeMin, 0);
							long splitSec = Math.max(batch.splitTimeSec, 0);
							splitLimit = ((splitMin * 60 + splitSec)
											* sampleRate * (bitDepth / 8) * channels) / 1000000000.0f;
						}
						ArrayList<File> tempFiles = new ArrayList<File>();
						splitAudioFile(file, outputPath, splitLimit, tempFiles);
						for (File tempFile : tempFiles) {
							tempFile.delete();
						}
						tempFiles.clear();
						if (taskCancelled()) return null;
						this.printOut("File splitting complete!", true, false);
					}
				} else if (tool.equals(Config.ANALYSE_GAIN)) {
					ArrayList<File> openFiles = batch.openFiles;
					if (openFiles.size() < 1) continue;
					File outputLocation = batch.outputLocation;
					boolean useInputFolder = batch.useInputFolder;
					float waveformWindow = batch.waveformValue;		// Measured in seconds
					String placeholder = Config.GAIN_PRECISION_PLACEHOLDERS[batch.gainPrecisionIndex];
					for (File file : openFiles) {
						if (taskCancelled()) return null;
						String fileName = file.getName();
						this.printOut("Analysing file: " + fileName, true, false);
						if (taskCancelled()) return null;
						AudioData audioData = getAudioData(file);
						int[] data = audioData.data;
						AudioFormat metaData = audioData.metaData;
						int bitDepth = metaData.getSampleSizeInBits();
						if (taskCancelled()) return null;
						double waveformData[] = getWaveformData(data, bitDepth);	// {Peak, RMS} (dBFS)
						File outputPath = useInputFolder ? file.getParentFile() : outputLocation;
						if (this.generateGraph) {
							this.printOut("Generating output files...", true, false);
							drawWaveform(metaData, data, fileName, outputPath, waveformWindow, "");
						}
						this.printOut(String.format("File Peak: " + placeholder + " dBFS", waveformData[0]), true, false);
						this.printOut(String.format("File RMS: " + placeholder + " dBFS", waveformData[1]), true, false);
						this.printOut("--------------------", true, false);
						if (taskCancelled()) return null;
					}
				}
				if (!useMultipleBatches) {
					break;
				} else {
					this.printOut("--- Batch " + String.valueOf(b + 1) + "/" + String.valueOf(batchList.size()) +
								  " completed ---", true, false);
					this.printOut("----------------------------------------", true, false);
				}
			}
		} catch (Exception e) {
			this.printOut(e.toString(), true, false);
		} finally {
			if (tool.equals(Config.LOOP_SCAN)) {
				this.printOut("All done! Check 'AS Loop' files for analysis results.", true, false);
			} else if (tool.equals(Config.LINEAR_SCAN)) {
				this.printOut("All done! Check 'AS Linear' files for analysis results.", true, false);
			} else if (tool.equals(Config.FILE_SPLIT)) {
				this.printOut("All done! Output files end with (1), (2), etc.", true, false);
			} else if (tool.equals(Config.ANALYSE_GAIN)) {
				if (this.generateGraph) {
					this.printOut("All done! Check 'AS Gain' files for analysis results.", true, false);
				} else {
					this.printOut("All done!", true, false);
				}
			}
			this.printOut("----------------------------------------", true, false);
			System.gc();
			if (taskCancelled()) return null;	// Ensures correct text output before end of program execution
			return null;
		}
	}

	public void printOut(Object text, boolean newLine, boolean progressIndication) {
		long currentTime = System.currentTimeMillis();
		if (!progressIndication || this.lastProgressOutputTime == -1 ||		// -1 = uninitialised
			currentTime - this.lastProgressOutputTime > Config.PROGRESS_REFRESH_VALUES[Config.PROGRESS_REFRESH_INDEX]) {
			if (!outputProgressText.isEmpty()) {	// Erase previous progress indication before printing new text
				int deleteStart = this.textOutput.getLength() - outputProgressText.length();
				int deleteEnd = this.textOutput.getLength();
				this.textOutput.deleteText(deleteStart, deleteEnd);
				this.outputProgressText = "";
			}
			if (newLine) text += "\n";
			if (progressIndication) {
				this.outputProgressText = text.toString();	// Update the variable that stores the last progress text
				this.lastProgressOutputTime = currentTime;	// Update the time of the last progress text output
			}
			this.textOutput.appendText("" + text);	// appendText is needed to scroll to the bottom
		}
	}
	
	// Checked at the following times:
	//   * The start of a loop iteration
	//   * After a long-running helper function
	//   * After closing an IO stream
	//   * End of program execution
	public boolean taskCancelled() {
		if (this.isCancelled()) {
			this.textOutput.setText("");
			return true;
		} else {
			return false;
		}
	}
	
	public String sanitisePathForWindows(String path) {
		return path.replace('\\', '_').replace('/', '_').replace(':', '_').replace('*', '_').replace('?', '_').replace('"', '_').replace('<', '_').replace('>', '_').replace('|', '_').replace('.', '_'); // Remove forbidden Windows path characters
	}
	
	public int average(int[] items) {
		if (items.length == 1) { return items[0]; }
		float total = 0.0f;
		for (int item : items) { total += item; }
		return Math.round(total / items.length);
	}
	public double average(double[] items) {
		if (items.length == 1) { return items[0]; }
		double total = 0.0;
		for (double item : items) { total += item; }
		return total / items.length;
	}
	
	public void readStart(File input, File output, long splitMark) throws Exception {
		FileInputStream reader = new FileInputStream(input);
		FileOutputStream writer = new FileOutputStream(output);
		long dataToRead = splitMark;
		try {
			while (dataToRead > 0) {
				if (taskCancelled()) return;
				int bufferSize = (int) Math.min(dataToRead, Config.BUFFER_VALUES[Config.BUFFER_SIZE_INDEX]);
				byte[] buffer = new byte[bufferSize];
				reader.read(buffer);
				writer.write(buffer);
				dataToRead -= bufferSize;
			}
		} catch (Exception e) {
			this.printOut(e.toString(), true, false);
		} finally {
			reader.close();
			writer.close();
		}
	}
	
	public void convertRIFFtoRF64(byte[] header, int newFileLength) {
		header[0] = 82;		// 'R'
		header[1] = 73;		// 'I'
		header[2] = 70;		// 'F'
		header[3] = 70;		// 'F'
		header[4] = (byte) (newFileLength & 0x1111000000000000L >> 24);		// Bits 24-31 of file size
		header[5] = (byte) (newFileLength & 0x0000111100000000L >> 16);		// Bits 16-23 of file size
		header[6] = (byte) (newFileLength & 0x0000000011110000L >> 8);		// Bits 8-15 of file size
		header[7] = (byte) (newFileLength & 0x0000000000001111L);			// Bits 0-7 of file size
	}
	
	public byte[] getWavHeader(File file, int newFileLength) throws Exception {
		byte[] header = new byte[0];
		FileInputStream reader = new FileInputStream(file);
		try {
			byte[] window = {0, 0, 0, 0};
			byte[] buffer = {0};
			int headerLength = 0;
			while (reader.available() > 0) {
				if (taskCancelled()) return header;
				reader.read(buffer);
				window[0] = window[1];
				window[1] = window[2];
				window[2] = window[3];
				window[3] = buffer[0];
				if (window[0] == 100 && window[1] == 97 && window[2] == 116 && window[3] == 97) {	// i.e. "data"
					headerLength += 5;
					break;
				} else {
					headerLength += 1;
				}
			}
			header = new byte[headerLength];
			reader = new FileInputStream(file);
			reader.read(header);
			convertRIFFtoRF64(header, newFileLength);
		} catch (Exception e) {
			this.printOut(e.toString(), true, false);
		} finally {
			reader.close();
			return header;
		}
	}
	
	public void readEndLargeWav(File input, File output, long splitMark, byte[] header, String path, String fileExt,
								Type fileType) throws Exception {
		FileInputStream reader = new FileInputStream(input);
		FileOutputStream writer = new FileOutputStream(output);
		try {
			writer.write(header);
			reader.skip(splitMark);
			long dataToRead = input.length() - splitMark;
			while (dataToRead > 0) {
				if (taskCancelled()) return;
				int bufferSize = (int) Math.min(dataToRead, Config.BUFFER_VALUES[Config.BUFFER_SIZE_INDEX]);
				byte[] buffer = new byte[bufferSize];
				reader.read(buffer);
				writer.write(buffer);
				dataToRead -= bufferSize;
			}
		} catch (Exception e) {
			this.printOut(e.toString(), true, false);
		} finally {
			reader.close();
			writer.close();
		}
	}
	
	public void readEnd(File input, File output, long splitMark, Type fileType) throws Exception {
		AudioInputStream reader = AudioSystem.getAudioInputStream(input);
		try {
			reader.skip(splitMark);
			AudioSystem.write(reader, fileType, output);
		} catch (Exception e) {
			this.printOut(e.toString(), true, false);
		} finally {
			reader.close();
		}
	}
	
	public void splitAudioFile(File file, File outputPath, float splitLimit, ArrayList<File> tempFiles) throws Exception {
		long outputFileSizeLimit = (long) (splitLimit * 1000000000.0);
		String path = outputPath.getAbsolutePath();
		String fileName = file.getName();
		String fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf("."));
		String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
		Type fileType;
		switch (fileExt) {
			case "aif":
				fileType = Type.AIFF;
				break;
			case "aiff":
				fileType = Type.AIFF;
				break;
			case "au":
				fileType = Type.AU;
				break;
			case "wav":
				fileType = Type.WAVE;
				break;
			default:
				fileType = Type.WAVE;
		}
		if (taskCancelled()) return;
		File temp = new File(path, "TEMP." + fileExt);
		tempFiles.add(temp);
		try {
			int splitCount = (int) Math.ceil((float) file.length()/(float) outputFileSizeLimit);
			long[] splitMarks = new long[splitCount];
			for (int b = 0; b < splitCount; b++) {
				splitMarks[b] = b * outputFileSizeLimit;
			}
			int fileCountMagnitude = (int) Math.ceil(Math.log10(splitMarks.length + 1));
			readStart(file, temp, file.length());
			if (this.deleteOriginal) {
				file.delete();
			}
			for (int s = splitMarks.length - 1; s >= 0; s--) {
				if (taskCancelled()) return;
				long splitMark = splitMarks[s];
				String outputFileName = fileNameWithoutExt + " (" + String.format("%0" + fileCountMagnitude +"d", s + 1) + ")." + fileExt;
				File start = new File(path, "START." + fileExt);
				tempFiles.add(start);
				readStart(temp, start, splitMark);
				File end = new File(path, outputFileName);
				if (fileExt.equals("wav") && file.length() > Config.FILE_SPLIT_FILE_SIZE_LIMIT) {
					int newFileLength = (int) (temp.length() - splitMark);	// This should always be <=4GB
					byte[] header = getWavHeader(temp, newFileLength);
					readEndLargeWav(temp, end, splitMark, header, path, fileExt, fileType);
				} else {
					readEnd(temp, end, splitMark, fileType);
				}
				this.printOut("Saving file: " + outputFileName, true, false);
				temp.delete();
				if (s > 0) {
					temp = new File(path, "TEMP." + fileExt);
					tempFiles.add(temp);
					readStart(start, temp, splitMark);
				}
				start.delete();
			}
		} catch (Exception e) {
			this.printOut(e.toString(), true, false);
		} finally {
			for (File tempFile : tempFiles) {	// Safety precaution (temp files should be deleted procedurally)
				tempFile.delete();
			}
			tempFiles.clear();
			System.gc();
			if (taskCancelled()) return;
		}
	}
	
	// Convert raw data bytes to integer values
	public int convertBytesToInts(byte[] buffer, int[] data, int dataPointer, int sampleSize,
								  int channels, int frameSize, boolean bigEndian, boolean starting,
								  boolean ending, long progressTotal, long progressBaseline) {
		boolean usePrintProgress = progressBaseline != -1 && progressTotal != -1; 
		int channelSize = sampleSize / 8;
		int start = 0;										// Remove leading and trailing silence
		int end = buffer.length - frameSize;
		boolean silence = true;
		if (starting) {
			for (int f = start; f < end; f += frameSize) {	// Define start point after leading silence
				if (taskCancelled()) return 0;
				for (int b = 0; b < frameSize; b++) {
					if (buffer[f + b] != 0) {
						start = f;
						silence = false;
						break;
					}
				}
				if (!silence) { break; }
			}
		}
		if (ending) {
			silence = true;
			for (int f = end; f > start; f -= frameSize) {	// Define end point before trailing silence
				if (taskCancelled()) return 0;
				for (int b = 0; b < frameSize; b++) {
					if (buffer[f + b] != 0) {
						end = f;
						silence = false;
						break;
					}
				}
				if (!silence) { break; }
			}
		}
		for (int f = start; f < end; f += frameSize) {		// Parse frames
			if (taskCancelled()) return 0;
			if (usePrintProgress) {
				long progressPercentage = (long) ((((double) progressBaseline + f) / (double) progressTotal) * 100);
				this.printOut("    " + progressPercentage + "%", false, true);
			}
			int[] frame = new int[channels];
			for (int c = 0; c < channels; c++) {			// Parse channels
				for (int b = 0; b < channelSize; b++) {		// Parse bytes
					byte currentByte = buffer[f + (c * channelSize) + b];
					if (bigEndian) {						// "<< 24 >>> 24" prevents sign-extension
						if (b < channelSize - 1) {
							frame[c] |= (currentByte << 24 >>> 24 << ((channelSize - 1 - b) * 8));
						} else {
							frame[c] |= (currentByte << ((channelSize - 1 - b) * 8));
						}
					} else {
						if (b < channelSize - 1) {
							frame[c] |= (currentByte << 24 >>> 24 << (b * 8));
						} else {
							frame[c] |= (currentByte << (b * 8));
						}
					}
				}
			}
			data[dataPointer] = average(frame);	// Generate mono signal by averaging all channels
			dataPointer++;
		}
		return dataPointer;
	}
	
	public AudioData getAudioData(File file) throws Exception {
		AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
		AudioFormat metaData = audioStream.getFormat();
		int[] data = {};
		try {
			int channels = metaData.getChannels();
			int frameSize = metaData.getFrameSize();
			int sampleSize = metaData.getSampleSizeInBits();
			boolean bigEndian = metaData.isBigEndian();
			long bufferRemaining = file.length();
			boolean starting = true;	// Prevents leading space from being removed in non-starting buffer chunks
			boolean ending = false;		// Prevents trailing space from being removed in non-ending buffer chunks
			long progressTotal = file.length();
			long progressBaseline = 0;
			int audioDataLength = (int) Math.ceil((double) file.length() / (double) frameSize);
			data = new int[audioDataLength];
			int dataPointer = 0;
			while (bufferRemaining >= 0) {
				if (bufferRemaining <= Config.BUFFER_VALUES[Config.BUFFER_SIZE_INDEX]) {
					ending = true;
				}
				int bufferSize = (int) Math.min(bufferRemaining, Config.BUFFER_VALUES[Config.BUFFER_SIZE_INDEX]);
				byte[] buffer = new byte[bufferSize];
				audioStream.read(buffer);
				dataPointer = convertBytesToInts(buffer, data, dataPointer, sampleSize, channels, frameSize, bigEndian,
								   starting, ending, progressTotal, progressBaseline);
				progressBaseline += bufferSize;
				bufferRemaining -= Config.BUFFER_VALUES[Config.BUFFER_SIZE_INDEX];
				starting = false;
			}
		} catch (Exception e) {
			this.printOut(e.toString(), true, false);
		} finally {
			audioStream.close();
			return new AudioData(data, metaData);
		}
	}
	
	public int getLoopLength(int[] data, int frameRate, int minLoop, int maxLoop, boolean autoLengthDetection) throws Exception {
		int loopLength = -1;
		float loopVariance = -1.0f;
		int autoTimeLimit = -1;		// Not required if autoLengthDetection == false
		if (autoLengthDetection) {
			autoTimeLimit = frameRate * Config.SCAN_WINDOW_VALUES[Config.AUTO_SCAN_WINDOW_INDEX];
			minLoop = frameRate;
			maxLoop = data.length / 4;
		} else {
			minLoop = Math.max(1, minLoop) * frameRate;
			maxLoop = Math.min(data.length / 4, maxLoop * frameRate);
		}
		for (int length = minLoop; length < maxLoop; length++) {
			if (taskCancelled()) return 0;
			long progressPercentage = (long) (((double) (length - minLoop) / (double) (maxLoop - minLoop)) * 100);
			this.printOut("    " + progressPercentage + "%", false, true);
			int[] list1 = Arrays.copyOfRange(data, 0, length);
			int[] list2 = Arrays.copyOfRange(data, length, length * 2);
			float varianceSum = 0.0f;
			int optimisationFactor = Config.OPTIMISATION_VALUES[Config.OPTIMISATION_INDEX];
			int frameIncrement = Math.max(1, (list1.length / frameRate) * optimisationFactor);
			for (int i = 0; i < list1.length; i += frameIncrement) {
				if (taskCancelled()) return 0;
				int value1 = list1[i] == 0 ? list1[i] + 1 : list1[i]; // Guard against zero-division below
				int value2 = list1[i] == 0 ? list2[i] + 1 : list2[i]; // "list1[i] == 0" is not a typo
				varianceSum += Math.abs((value2 - value1) / value1);
			}
			float variance = varianceSum / list1.length;
			if (variance < loopVariance || loopLength == -1) {
				loopLength = length;
				loopVariance = variance;
			}
			if (autoLengthDetection && (length - loopLength) > autoTimeLimit) {
				break;	// If enough audio has been scanned since the lowest variance value detected so far, use that length
			}
		}
		return loopLength;
	}
	
	public double getScaledValue(double value, String scale) throws Exception {
		switch (scale) {
			case Config.LOGARITHMIC:
				value = Math.max(value, Config.SMALLEST_POSITIVE_DOUBLE);	// Undefined value guard for Math.log10 operation
				return Math.log10(value);
			case Config.SQUARE_ROOT:
				return Math.sqrt(value);
			case Config.LINEAR:
				return value;
			case Config.QUADRATIC:
				return Math.pow(value, 2);
			case Config.EXPONENTIAL:
				if (value > Config.LARGEST_EXPONENT) {						// Infinity guard for Math.pow operation
					return Config.LARGEST_POSITIVE_DOUBLE;
				} else {
					return Math.pow(10, value);
				}
			default:
				throw new Exception("Error: Invalid index for scaled value");
		}
	}
	
	public double[] getLoopVariances(int loopLength, int[] audioData, int sampleRate) throws Exception {
		int windowSize = sampleRate * Config.TIME_SCALE_VALUES[Config.TIME_SCALE_INDEX];
		int start = windowSize + loopLength;
		int end = audioData.length - windowSize;
		int variancesLength = (int) Math.ceil((double) (end - start) / (double) windowSize);
		double[] variances = new double[variancesLength];
		int variancesIndex = 0;
		for (int frame = start; frame <= end; frame += windowSize) {
			if (taskCancelled()) return variances;
			long progressPercentage = (long) (((double) (frame - start) / (double) (end - start)) * 100);
			this.printOut("    " + progressPercentage + "%", false, true);
			double varianceSum = 0.0;
			for (int i = frame; i < frame + windowSize; i++) {
				if (taskCancelled()) return variances;
				int prevValue = audioData[i];
				int currValue = audioData[i - loopLength];
				if (prevValue == 0) {	// Safeguard against zero-division below
					prevValue += 1; 
					currValue += 1;
				}
				varianceSum += Math.abs((double) (currValue - prevValue) / prevValue);
			}
			double variance = varianceSum / windowSize;
			String varianceScale = Config.VARIANCE_SCALE_VALUES[Config.VARIANCE_SCALE_INDEX];
			variances[variancesIndex] = getScaledValue(variance, varianceScale);
			variancesIndex++;
		}
		return variances;
	}
	
	// Assumes all numerical data values are positive
	public void normaliseDatasets(double[] data1, double[] data2) {
		if (data1.length != data2.length) return;
		double[] combinedProbabilities = new double[data1.length];
		double min1 = -1.0;
		double max1 = -1.0;
		double min2 = -1.0;
		double max2 = -1.0;
		for (int i = 0; i < data1.length; i++) {
			double value1 = data1[i];
			double value2 = data2[i];
			if (min1 == -1 || value1 < min1) min1 = value1;
			if (max1 == -1 || value1 > max1) max1 = value1;
			if (min2 == -1 || value2 < min2) min2 = value2;
			if (max2 == -1 || value2 > max2) max2 = value2;
		}
		double range1 = max1 - min1;
		double range2 = max2 - min2;
		double sourceMin, sourceRange, targetMin, targetRange;
		double[] sourceData, targetData;
		if (range1 < range2) {	
			sourceMin = min1;
			sourceRange = range1;
			sourceData = data1;
			targetMin = min2;
			targetRange = range2;
			targetData = data2;
		} else {
			sourceMin = min2;
			sourceRange = range2;
			sourceData = data2;
			targetMin = min1;
			targetRange = range1;
			targetData = data1;
		}
		for (int i = 0; i < sourceData.length; i++) {	// Normalise the dataset with the smallest range
			sourceData[i] = ((sourceData[i] - sourceMin) * (targetRange / sourceRange)) + targetMin;
		}
	}
	
	// Sudden amplitude jumps indicate sinusoidal waveform disruption, and therefore potential frame loss.
	public double[] getSignalGradientData(int[] audioData, int sampleRate, int bitDepth) throws Exception {
		long maxValue = Math.round(Math.pow(2, bitDepth));
		int windowSize = sampleRate * Config.TIME_SCALE_VALUES[Config.TIME_SCALE_INDEX];
		double maxGradient = 0.0;
		int start = 1;
		int end = audioData.length;
		int probabilitiesLength = (int) Math.ceil((double) (end - start) / (double) windowSize);
		double[] probabilities = new double[probabilitiesLength];
		int probabilitiesPointer = 0;
		for (int frame = start; frame < end; frame++) {
			if (taskCancelled()) return probabilities;
			long progressPercentage = (long) (((double) frame / (double) end) * 100);
			this.printOut("    " + progressPercentage + "%", false, true);
			if (frame % windowSize == 0) {
				String probabilityScale = Config.PROBABILITY_SCALE_VALUES[Config.PROBABILITY_SCALE_INDEX];
				probabilities[probabilitiesPointer] = getScaledValue(maxGradient, probabilityScale);
				probabilitiesPointer++;
				maxGradient = 0.0;
			}
			int prevValue = audioData[frame - 1];
			int currValue = audioData[frame];
			double currGradient = Math.abs((double) (currValue - prevValue) / maxValue);
			maxGradient = Math.max(maxGradient, currGradient);
		}
		return probabilities;
	}
	
	// A large/sudden change to zero amplitude indicates potential frame loss
	public double[] getSignalCutoutData(int[] audioData, int sampleRate, int bitDepth) throws Exception {
		long maxValue = Math.round(Math.pow(2, bitDepth));
		int windowSize = sampleRate * Config.TIME_SCALE_VALUES[Config.TIME_SCALE_INDEX];
		double maxCutout = 0.0;
		int start = 1;
		int end = audioData.length;
		int probabilitiesLength = (int) Math.ceil((double) (end - start) / (double) windowSize);
		double[] probabilities = new double[probabilitiesLength];
		int probabilitiesPointer = 0;
		for (int frame = start; frame < end; frame++) {
			if (taskCancelled()) return probabilities;
			long progressPercentage = (long) (((double) frame / (double) end) * 100);
			this.printOut("    " + progressPercentage + "%", false, true);
			if (frame % windowSize == 0) {
				String probabilityScale = Config.PROBABILITY_SCALE_VALUES[Config.PROBABILITY_SCALE_INDEX];
				probabilities[probabilitiesPointer] = getScaledValue(maxCutout, probabilityScale);
				probabilitiesPointer++;
				maxCutout = 0.0;
			}
			int currValue = audioData[frame];
			int prevValue = audioData[frame - 1];
			if (currValue == 0) {
				double currCutout = Math.abs((double) prevValue / (double) maxValue);
				maxCutout = Math.max(maxCutout, currCutout);
			}
		}
		return probabilities;
	}
	
	// Single dataset
	public void drawAnalyticsOne(int sampleRate, double[] yData, String fileName, File outputLocation,
								 String tool, String prefix) throws Exception {
		try {
			TimeSeries series1 = new TimeSeries("Raw Data");
			TimeSeries series2 = new TimeSeries("10-Point Average");
			TimeSeries series3 = new TimeSeries("20-Point Average");
			Second current = new Second(0, 0, 0, 1, 1, 2000);
			int timeScale = Config.TIME_SCALE_VALUES[Config.TIME_SCALE_INDEX];	// Measured in seconds
			int end = yData.length;
			for (int v = 0; v < end; v++) {
				if (taskCancelled()) return;
				long progressPercentage = (long) Math.floor(((double) v / (double) end) * 100);
				this.printOut("    " + progressPercentage + "%", false, true);
				for (int s = 0; s < timeScale; s++) { current = (Second) current.next(); }
				series1.add(current, yData[v]);
				series2.add(current, average(Arrays.copyOfRange(yData, Math.max(v - 9, 0), v + 1)));
				series3.add(current, average(Arrays.copyOfRange(yData, Math.max(v - 19, 0), v + 1)));
			}
			TimeSeriesCollection dataset = new TimeSeriesCollection();
			dataset.addSeries(series3);	// Series are layered in descending order (i.e. first = top)
			dataset.addSeries(series2);
			dataset.addSeries(series1);
			String yLabel;
			String outputPrefix;
			if (tool.equals(Config.LOOP_SCAN)) {
				yLabel = "Variance (" + Config.VARIANCE_SCALE_VALUES[Config.VARIANCE_SCALE_INDEX] + " Scale)";
				outputPrefix = prefix.equals("") ? "[AS Loop]" : prefix;
			} else if (tool.equals(Config.LINEAR_SCAN)) {
				yLabel = "Frame Loss Probability (" + Config.PROBABILITY_SCALE_VALUES[Config.PROBABILITY_SCALE_INDEX] + " Scale)";
				outputPrefix = prefix.equals("") ? "[AS Linear]" : prefix;
			} else {
				throw new Exception("Error: Invalid tool value passed into drawAnalyticsOne function");
			}
			String outputFileNameNoExt = outputPrefix + " " + sanitisePathForWindows(fileName);
			JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Audiscope Output\n" + fileName,	// Chart title
				"Time",								// X-axis label
				yLabel, 							// Y-axis label
				dataset,							// Chart data
				false, false, false					// Legend, tooltips, URLs
			);
			chart.setPadding(new RectangleInsets(20, 20, 20, 20));
			XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
			renderer.setSeriesPaint(0, new Color(0x00, 0x00, 0x00));
			renderer.setSeriesPaint(1, new Color(0x00, 0x00, 0xff));
			renderer.setSeriesPaint(2, new Color(0xff, 0x00, 0x00));
			renderer.setSeriesShapesVisible(0, false);
			renderer.setSeriesShapesVisible(1, false);
			renderer.setSeriesShapesVisible(2, false);
			XYPlot plot = (XYPlot)chart.getPlot();
			plot.setRenderer(0, renderer);
			if (taskCancelled()) return;
			int chartWidth = Config.RESOLUTION_VALUES[Config.OUTPUT_RESOLUTION_INDEX][0];
			int chartHeight = Config.RESOLUTION_VALUES[Config.OUTPUT_RESOLUTION_INDEX][1];
			if (Config.OUTPUT_FORMAT_INDEX == 0) {
				ExportUtils.writeAsPDF(chart, chartWidth, chartHeight,
									   new File(outputLocation, outputFileNameNoExt + ".pdf"));
			} else if (Config.OUTPUT_FORMAT_INDEX == 1) {
				ExportUtils.writeAsJPEG(chart, chartWidth, chartHeight,
										new File(outputLocation, outputFileNameNoExt + ".jpg"));
			} else if (Config.OUTPUT_FORMAT_INDEX == 2) {
				ExportUtils.writeAsPNG(chart, chartWidth, chartHeight,
									   new File(outputLocation, outputFileNameNoExt + ".png"));
			} else if (Config.OUTPUT_FORMAT_INDEX == 3) {
				ExportUtils.writeAsSVG(chart, chartWidth, chartHeight,
									   new File(outputLocation, outputFileNameNoExt + ".svg"));
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	// Two datasets
	public void drawAnalyticsTwo(int sampleRate, double[] yData1, double[] yData2, String fileName,
								 File outputLocation, String tool, String prefix) throws Exception {
		try {
			TimeSeries series1 = new TimeSeries("Raw Data 1");
			TimeSeries series2 = new TimeSeries("10-Point Average 1");
			TimeSeries series3 = new TimeSeries("20-Point Average 1");
			TimeSeries series4 = new TimeSeries("Raw Data 2");
			TimeSeries series5 = new TimeSeries("10-Point Average 2");
			TimeSeries series6 = new TimeSeries("20-Point Average 2");
			Second current = new Second(0, 0, 0, 1, 1, 2000);
			int timeScale = Config.TIME_SCALE_VALUES[Config.TIME_SCALE_INDEX];	// Measured in seconds
			int end = yData1.length;
			for (int v = 0; v < end; v++) {
				if (taskCancelled()) return;
				long progressPercentage = (long) Math.floor(((double) v / (double) end) * 100);
				this.printOut("    " + progressPercentage + "%", false, true);
				for (int s = 0; s < timeScale; s++) { current = (Second) current.next(); }
				if (average(yData2) > average(yData1)) {	// Dataset with higher average is layered on top
					series1.add(current, yData1[v]);
					series2.add(current, average(Arrays.copyOfRange(yData1, Math.max(v - 9, 0), v + 1)));
					series3.add(current, average(Arrays.copyOfRange(yData1, Math.max(v - 19, 0), v + 1)));
					series4.add(current, yData2[v]);
					series5.add(current, average(Arrays.copyOfRange(yData2, Math.max(v - 9, 0), v + 1)));
					series6.add(current, average(Arrays.copyOfRange(yData2, Math.max(v - 19, 0), v + 1)));
				} else {
					series1.add(current, yData2[v]);
					series2.add(current, average(Arrays.copyOfRange(yData2, Math.max(v - 9, 0), v + 1)));
					series3.add(current, average(Arrays.copyOfRange(yData2, Math.max(v - 19, 0), v + 1)));
					series4.add(current, yData1[v]);
					series5.add(current, average(Arrays.copyOfRange(yData1, Math.max(v - 9, 0), v + 1)));
					series6.add(current, average(Arrays.copyOfRange(yData1, Math.max(v - 19, 0), v + 1)));
				}
			}
			TimeSeriesCollection dataset = new TimeSeriesCollection();
			dataset.addSeries(series6);	// Series are layered in descending order (i.e. first = top)
			dataset.addSeries(series5);
			dataset.addSeries(series4);
			dataset.addSeries(series3);
			dataset.addSeries(series2);
			dataset.addSeries(series1);
			String yLabel;
			String outputPrefix;
			if (tool.equals(Config.LOOP_SCAN)) {
				yLabel = "Variance (" + Config.VARIANCE_SCALE_VALUES[Config.VARIANCE_SCALE_INDEX] + " Scale)";
				outputPrefix = prefix.equals("") ? "[AS Loop]" : prefix;
			} else if (tool.equals(Config.LINEAR_SCAN)) {
				yLabel = "Frame Loss Probability (" + Config.PROBABILITY_SCALE_VALUES[Config.PROBABILITY_SCALE_INDEX] + " Scale)";
				outputPrefix = prefix.equals("") ? "[AS Linear]" : prefix;
			} else {
				throw new Exception("Error: Invalid tool value passed into drawAnalyticsTwo function");
			}
			String outputFileNameNoExt = outputPrefix + " " + sanitisePathForWindows(fileName);
			JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Audiscope Output\n" + fileName,	// Chart title
				"Time",								// X-axis label
				yLabel, 							// Y-axis label
				dataset,							// Chart data
				false, false, false					// Legend, tooltips, URLs
			);
			chart.setPadding(new RectangleInsets(20, 20, 20, 20));
			XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
			renderer.setSeriesPaint(3, new Color(0x00, 0x00, 0x00));
			renderer.setSeriesPaint(4, new Color(0x00, 0x00, 0xff));
			renderer.setSeriesPaint(5, new Color(0xff, 0x00, 0x00));
			renderer.setSeriesPaint(0, new Color(0xff, 0xff, 0x00));
			renderer.setSeriesPaint(1, new Color(0xff, 0x3f, 0xff));
			renderer.setSeriesPaint(2, new Color(0x00, 0x7f, 0x7f));
			renderer.setSeriesShapesVisible(0, false);
			renderer.setSeriesShapesVisible(1, false);
			renderer.setSeriesShapesVisible(2, false);
			renderer.setSeriesShapesVisible(3, false);
			renderer.setSeriesShapesVisible(4, false);
			renderer.setSeriesShapesVisible(5, false);
			XYPlot plot = (XYPlot)chart.getPlot();
			plot.setRenderer(0, renderer);
			if (taskCancelled()) return;
			int chartWidth = Config.RESOLUTION_VALUES[Config.OUTPUT_RESOLUTION_INDEX][0];
			int chartHeight = Config.RESOLUTION_VALUES[Config.OUTPUT_RESOLUTION_INDEX][1];
			if (Config.OUTPUT_FORMAT_INDEX == 0) {
				ExportUtils.writeAsPDF(chart, chartWidth, chartHeight,
									   new File(outputLocation, outputFileNameNoExt + ".pdf"));
			} else if (Config.OUTPUT_FORMAT_INDEX == 1) {
				ExportUtils.writeAsJPEG(chart, chartWidth, chartHeight,
										new File(outputLocation, outputFileNameNoExt + ".jpg"));
			} else if (Config.OUTPUT_FORMAT_INDEX == 2) {
				ExportUtils.writeAsPNG(chart, chartWidth, chartHeight,
									   new File(outputLocation, outputFileNameNoExt + ".png"));
			} else if (Config.OUTPUT_FORMAT_INDEX == 3) {
				ExportUtils.writeAsSVG(chart, chartWidth, chartHeight,
									   new File(outputLocation, outputFileNameNoExt + ".svg"));
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public double[] getWaveformData(int[] audioData, int bitDepth) {	// {Peak, RMS} (dBFS)
		double limit = 0.0;
		double peak = 0.0;
		double rms = 0.0;
		for (int value : audioData) {
			if (taskCancelled()) return null;
			limit = value >= 0 ? Math.pow(2, bitDepth - 1) - 1 : Math.pow(2, bitDepth - 1) * -1;
			peak = Math.max(peak, Math.abs(value / limit));
			rms += Math.pow(value / limit, 2);
		}
		peak = 20.0 * Math.log10(peak);
		rms = 20.0 * Math.log10(Math.sqrt(rms / audioData.length));
		return new double[] {peak, rms};
	}
	
	public void drawWaveform(AudioFormat metaData, int[] audioData, String fileName,
							 File outputLocation, float waveformWindow, String outputPrefix) {
		try {
			int sampleRate = Math.round(metaData.getSampleRate());
			int bitDepth = metaData.getSampleSizeInBits();
			TimeSeries series1 = new TimeSeries("Peak");
			TimeSeries series2 = new TimeSeries("RMS");
			int timeInterval = Math.round(waveformWindow * 1000.0f);		// Measured in seconds
			int frameInterval = Math.round(waveformWindow * sampleRate);	// Measured in frames
			Millisecond current = new Millisecond(0, 0, 0, 0, 1, 1, 2000);
			int end = audioData.length - frameInterval;
			for (int v = 0; v <= end; v += frameInterval) {
				if (taskCancelled()) return;
				long progressPercentage = (long) (((double) v / (double) end) * 100);
				this.printOut("    " + progressPercentage + "%", false, true);
				for (int s = 0; s < timeInterval; s++) {
					if (taskCancelled()) return;
					current = (Millisecond)current.next();
				}
				double[] waveformData = getWaveformData(Arrays.copyOfRange(audioData, v, v + frameInterval), bitDepth);
				series1.add(current, waveformData[0]);
				series2.add(current, waveformData[1]);
			}
			TimeSeriesCollection dataset = new TimeSeriesCollection();
			dataset.addSeries(series1);	// Series are layered in descending order (i.e. first = top)
			dataset.addSeries(series2);
			JFreeChart chart = ChartFactory.createTimeSeriesChart(
				"Audiscope Output\n" + fileName,	// Chart title
				"Time",								// X-axis label
				"Decibels", 						// Y-axis label
				dataset,							// Chart data
				true, false, false					// Legend, tooltips, URLs
			);
			chart.setPadding(new RectangleInsets(20, 20, 20, 20));
			XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
			renderer.setSeriesPaint(0, new Color(0xff, 0x00, 0x00));
			renderer.setSeriesPaint(1, new Color(0x00, 0x00, 0xff));
			renderer.setSeriesShapesVisible(0, false);
			renderer.setSeriesShapesVisible(1, false);
			XYPlot plot = (XYPlot)chart.getPlot();
			plot.setRenderer(0, renderer);
			if (taskCancelled()) return;
			int chartWidth = Config.RESOLUTION_VALUES[Config.OUTPUT_RESOLUTION_INDEX][0];
			int chartHeight = Config.RESOLUTION_VALUES[Config.OUTPUT_RESOLUTION_INDEX][1];
			outputPrefix = outputPrefix.isEmpty() ? "[AS Gain]" : outputPrefix;
			String outputFileNameNoExt = outputPrefix + " " + sanitisePathForWindows(fileName);
			if (Config.OUTPUT_FORMAT_INDEX == 0) {
				ExportUtils.writeAsPDF(chart, chartWidth, chartHeight,
										new File(outputLocation, outputFileNameNoExt + ".pdf"));
			} else if (Config.OUTPUT_FORMAT_INDEX == 1) {
				ExportUtils.writeAsJPEG(chart, chartWidth, chartHeight,
										new File(outputLocation, outputFileNameNoExt + ".jpg"));
			} else if (Config.OUTPUT_FORMAT_INDEX == 2) {
				ExportUtils.writeAsPNG(chart, chartWidth, chartHeight,
										new File(outputLocation, outputFileNameNoExt + ".png"));
			} else if (Config.OUTPUT_FORMAT_INDEX == 3) {
				ExportUtils.writeAsSVG(chart, chartWidth, chartHeight,
										new File(outputLocation, outputFileNameNoExt + ".svg"));
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}