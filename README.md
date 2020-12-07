## Automatic Lyrics Display for Live Musical Performances

### A computational system in Java that follows live musical performances and displays pre-encoded lyrics in real-time. Key features:
1. A multi-threaded audio framework with a shared synchronous buffer to handle simultaneous recording and processing in Java. 
2. Real-time implementation of the dynamic time warping algorithm to align annotated and live temporal sequences, based on their harmonic similarity.
3. GUI to load lyrics files and display lyrics in real time

Here is a overview of the computational system: 
<p align="center">
<img src="/images/Research%20System%20Flowchart.png" width="600" height="400">
</p>

A screenshot of the graphic user interface:
<p align="center">
<img src="/images/GUI%20screenshot.JPG" width="600" height="400">
</p>

As basic harmonic progression of two recordings is similar, I extracted chroma feature vectors from the waveform audio data - the chroma feature uses 12 bins to represent the relative energy of the audio in the 12 semitones of a musical octave, and represents the harmonic content of the audio. I then used online dynamic time warping to align the live audio stream with a pre-annotated version of the audio based on their harmonic similarity, and display lyrics in real-time. Shown below is a schematic of the shared synchronous audio buffer:
<p align="center">
<img src="/images/Multithread%20Buffer%20Flowchart.png" width="600" height="400">
</p>

### References
1. Bochen Li, Zhiyao Duan. Score Following for Piano Performances with Sustain-pedal Effects, International Society
for Music Information Retrieval, 2015.
2. Cano Pedro, Batlle Eloi, Kalker Ton, Haitsma Jaap. A review of audio fingerprinting. Journal of VLSI signal
processing systems for signal, image and video technology, 2005, 41(3): 271-284.
3. Simon Dixon. Live Tracking of Musical Performance Using Online Time Warping. International Conference on
Digital Audio Effects, 2005.

##### Link to SigPort Paper: https://sigport.org/sites/default/files/docs/VombatkereLi_AutomaticLyricsDisplaySystem_IEEESPM16_Final_0.pdf

##### Audio Information Research Lab, University of Rochester, Summer 2016
###### Contributors: Karan Vombatkere, Bochen Li
