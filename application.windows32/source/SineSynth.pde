class SineSynth {

  AudioStream myStream;
  SineWave myWave;
  FadeOut myFadeOut; // Amplitude ramp function
  FadeIn myFadeIn; // Amplitude ramp function
  float x,y = 0;


  SineSynth() {
    // create a new AudioStream
    myStream=new AudioStream();
    myStream.smoothPan = true;
    // our wave
    myWave=new SineWave(960,3);

    // start
    myFadeOut = new FadeOut();  // Create amplitude ramp
    myFadeIn = new FadeIn(); // Create amplitude ramp
    myStream.start();
  }


  void audioStreamWrite(AudioStream theStream) {
    // next wav

    myWave.generate(myStream);

    // adjust our phase
    myWave.phase+=myStream.size;
    myWave.phase%=myStream.sampleRate;
    myFadeOut.filter(myStream);
    myWave.frequency = x*10;
    println("myWave.frequency = " + myWave.frequency);
    myWave.phase = 0;
    myFadeIn.filter(myStream);
  }
  
  void setSynthVars(float mx, float my) {
    this.x = mx;
    this.y = my;
  }
}

