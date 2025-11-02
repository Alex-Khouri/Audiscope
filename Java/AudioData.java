/*
* Audiscope
* 
* Author: Alexander Khouri
* Date: September 2024
*/

import javax.sound.sampled.*;
import javax.sound.sampled.AudioFileFormat.*;

public class AudioData {
	public int[] data;
	public AudioFormat metaData;
	
	AudioData(int[] data, AudioFormat metaData) {
		this.data = data;
		this.metaData = metaData;
	}
}