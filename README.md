## Automatic Lyrics Display for Live Musical Performances

### A computational system in Java that follows live musical performances and displays pre-encoded lyrics in real-time. Key features:
1. A multi-threaded audio framework with a shared synchronous buffer to handle simultaneous recording and processing in Java. 
2. Real-time implementation of the dynamic time warping algorithm to align annotated and live temporal sequences, based on their harmonic similarity.
3. GUI to load lyric files and display lyrics in real time


Here is a flowchart overview of the project: 

![overall_flow](/images/Research%20System%20Flowchart.png)

Screenshot of GUI:
![GUI](/images/GUI%20screenshot.JPG)

As basic harmonic progression of two recordings is similar, I extracted chroma feature vectors from the waveform audio data - the chroma feature uses 12 bins to represent the relative energy of the audio in the 12 semitones of a musical octave, and represents the harmonic content of the audio. I then used online dynamic time warping to align the live audio stream with a pre-annotated version of the audio based on their harmonic similarity, and display lyrics in real-time. Shown below is a schematic of the shared synchronous audio buffer:

<img src="/images/Multithread%20Buffer%20Flowchart.png" width="600" height="400">
