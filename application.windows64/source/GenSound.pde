class GenSound {

  // Sampling rates to choose from
  int[] rates = {
    44100, 22050, 29433, 49500, 11025, 37083
  };

  boolean left = true;
  boolean middle = false;
  boolean right = false;

  AudioChannel[] mySound;
  Envelope myEnvelope;

  int currRate;
  int numChannels;
  int currRateIndex;

  GenSound(int sampRate, int nc) {
    currRateIndex = sampRate;
    if(currRateIndex<0)return;
    currRate = rates[currRateIndex];
    numChannels = nc;
    if (numChannels<=0) numChannels = 1;
    mySound = new AudioChannel[numChannels];


    for (int i = 0; i < numChannels; i++) {
      mySound[i] = new AudioChannel("cela3.aif");
      mySound[i].smoothPan = true;
      mySound[i].panTo(random(2)-1,4000);
    }

    EPoint[] myEnv = new EPoint[3]; // Three-step breakpoint function
    myEnv[0] = new EPoint(0, 0); // Start at 0
    myEnv[0] = new EPoint(0, 0); // Start at 0
    myEnv[1] = new EPoint(0.25, 1); // Attack
    myEnv[2] = new EPoint(2, 0); // Release
    myEnvelope = new Envelope(myEnv); // Bind an Envelope to the breakpoint function
    playSounds();
  }

  void playSounds() {
    int playSound = int(random(1,numChannels/2));
    int which = -1;



    // Voice allocation block; figure out which AudioChannels are free

    while(playSound>0) {
      for (int i = 0; i < mySound.length; i++) {
        if (mySound[i].state==Ess.STOPPED) {
          which = i; // find a free voice
        }
      }

      // If a voice is available and selected, play it
      if (which != -1) {
        mySound[which].sampleRate(rates[int(random(0, currRateIndex))], false);
        mySound[which].play();
        myEnvelope.filter(mySound[which]); // Apply envelop
      }
      playSound--;
    }
  }

  void audioChannelDone(AudioChannel ch) {
    ch.destroy();
  }

  void audioOutputPan(AudioOutput c) {
    c.panTo(-c.pan, 4000); // Reverse pan direction
  }
}

