import processing.opengl.*;
import javax.media.opengl.*;
import processing.serial.*;
import toxi.color.*;
import toxi.geom.*;
import toxi.util.datatypes.*;

import krister.Ess.*;

int num = 10;

int ellipseY = 0;

int angle = 0;


final int sample_rate = 30;
boolean readyToClear = false;
boolean firstIdleFlag = false;
boolean stopWaterfalling = false;
boolean idleOn = false;
int idleWindowStart = 0;
int TIME_WINDOW = 20000;

IdleScreen is;


// Set up waterfall shapes
Particle[] particles = {
};


Word w;
GenSound gen;

AudioStream myStream;
SineWave myWave;
FadeOut myFadeOut; // Amplitude ramp function
FadeIn myFadeIn; // Amplitude ramp function


Serial port; // our serial port
int linefeed = 10; // linefeed in ASCII
float xPot = width/2;
float yPot = height/2;
float oldX = width/2;
float oldY = height/2;

int[] xArray = new int[8];
int[] yArray = new int[8];
int[] zArray = new int[8];

boolean firstPass = true;

int clearAlpha = 5;

int count = 0;

ColorTheme colorTheme;
ColorList colorList;

void setup() {
  colorMode(RGB);
  Ess.start(this); // Start ESS
  size(800, 600, P3D);
  smooth();
  strokeWeight(1);
  fill(150, 50);
  port = new Serial(this, Serial.list()[0], 9600);
  port.bufferUntil(linefeed); // read bytes into a buffer until we get a linefeed(ASCII 10)
  setUpSineWave();
  setUpColors();
}

void glSettings() {
  //  PGraphicsOpenGL pgl = (PGraphicsOpenGL) g;
  //  GL gl = pgl.beginGL(); 
  //  gl.glClearColor(0, 0, 0, 0);
  //  gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
  //  pgl.endGL();
}

void fadeStrobe() {
  int r,g,b;
  loadPixels();
  for (int i=0;i<pixels.length;i++) {
    r=pixels[i]>>16&255;
    g=pixels[i]>>8&255;
    b=pixels[i]&255;
    pixels[i]=color(map(r, 255, 0, 0, 255)-1,map(g, 255, 0, 0, 255)-1,map(b, 255, 0, 0, 255)-1);
  }
  updatePixels();
}

void fade() {
  int r,g,b;
  loadPixels();
  for (int i=0;i<pixels.length;i++) {
    r=pixels[i]>>16&255;
    g=pixels[i]>>8&255;
    b=pixels[i]&255;
    pixels[i]=color(r-1,g-1,b-1);
  }
  updatePixels();
}

void setUpColors() {
  colorTheme = new ColorTheme("synthasktech");
  colorTheme.addRange("soft ivory", 0.5);
  colorTheme.addRange("intense goldenrod", 0.25);
  colorTheme.addRange("warm saddlebrown", 0.15);
  colorTheme.addRange("fresh teal", 0.05);
  colorTheme.addRange("bright yellow", 0.05);

  // now add another random hue which is using only bright shades
  colorTheme.addRange(ColorRange.BRIGHT, TColor.newRandom(), random(0.02, 0.05));

  // use the TColortheme to create a list of 160 colors
  colorList = colorTheme.getColors(160);

  background(colorList.getLightest().toARGB());
}


void draw() {

  if (idleOn) {
    if (is==null) {
      is = new IdleScreen();
      //println("setting up a new idle screen");
    }
    return;
  }

  if (!firstPass) {
    if (readyToClear) {
      //println("Count is " + count);
      //textFont(loadFont("04b03-48.vlw"));
      Word clearWord = new Word("DELETING ! ! !");
      clearWord.drawWordNormally(colorList);
      count++;
      glSettings();
      fadeStrobe();
      fade();

      if (count > 30) {
        background(colorList.get((int) random(160)).toARGB());
        readyToClear = false;
        count = 0;
      }

      return;
    }

    // If we're idle, let's waterfall effect
    int xBuffer =  (int)abs(oldX-xPot);
    int yBuffer = (int)abs(oldY-yPot);
    if ((xBuffer < 2) && (yBuffer < 2)) {
      if (!stopWaterfalling) showWaterFallEffect();
      return;
    }


    // Our main shape burst loop
    particles = new Particle[0];
    for (int i=0; i<15; i++) {
      Particle p = new Particle(xPot, yPot, oldX, oldY, colorList);
      p.update();
      particles = (Particle[])append(particles, p);
    }



    /*
      **  Every 60 Frames
     */
    if (frameCount%120==0) {
      gen = new GenSound((int)yPot%5, (int)xPot%5);
    } 
    else if (frameCount%60==0) {
      w = new Word("synth-a-sketch", xPot, yPot);
      w.drawWord();
    }
    else {
      gen = null;
      w = null;
    }
  }
}


void showWaterFallEffect() {

  // this is the main water-fall like effect

    for (int i = 0; i < particles.length; i++) {
    particles[i].y+=(i+1)*2; // drip down
    particles[i].x = particles[i].x + random(-3, 3); // brownian motion
    particles[i].r/=1.1; //reduce the radius
    particles[i].alph = particles[i].alph - .15*i; //reduce the alpha
    if (particles[i].alph > 0)
    {
      particles[i].drawShape();
    } 
    else {
      particles = (Particle[])shorten(particles); // clear invisible particles
    }
  }
}

void keyPressed() {
  if (key==' ') {
    readyToClear = true;
  }
}


void setUpSineWave() {
  // create a new AudioStream
  myStream=new AudioStream();
  myStream.smoothPan = true;
  // our wave
  myWave=new SineWave(960,.75);

  // start
  myFadeOut = new FadeOut();  // Create amplitude ramp
  myFadeIn = new FadeIn(); // Create amplitude ramp
  myStream.start();
}

void audioStreamWrite(AudioStream theStream) {
  // next wav
  if (!firstPass) {

    myWave.generate(myStream);

    // adjust our phase
    myWave.phase+=myStream.size;
    myWave.phase%=myStream.sampleRate;
    myFadeOut.filter(myStream);
    int vel = (int)(abs(xPot-oldX) + abs(yPot-oldY))/2;
    myWave.frequency = map(vel, 1, 7, 400, 1000);
    //myWave.frequency = map((abs(mouseX-pmouseX) + abs(mouseY-pmouseY))/2, 0, width+height/2, 600, 10000);
    if (readyToClear) myWave.frequency = random(500, 10000);
    myWave.phase = 0;
    myFadeIn.filter(myStream);
  }
}

void serialEvent(Serial port) {
  float xpos, ypos, zpos, potsDiff;
  int oldAverages = 0, newAverages = 0, delta = 0;

  String serialString = port.readStringUntil(linefeed);
  if (serialString != null) {
    serialString = trim(serialString);
    // split the string at the commas
    // convert the sections into integers
    float sensors[] = float(split(serialString, ","));
    // If we've received all the sensor strings, use them
    if (sensors.length>=5) {
      oldX = xPot;
      oldY = yPot;
      xPot = (float)map(sensors[0], 0, 1023, 0, width);
      yPot = (float)map(sensors[1], 1023, 0, 0, height);
      potsDiff = abs(oldX-xPot) + abs(oldY - yPot);

      if (millis() > 3000) {
        oldAverages = (getAverage(xArray) + getAverage(yArray) + getAverage(zArray)) / 3;
        //println("Pots Diff = " + potsDiff);
      }



      xpos = (int)sensors[2];
      addEntry((int)xpos, xArray);
      ypos = (int)sensors[3];
      addEntry((int)ypos, yArray);
      zpos = (int)sensors[4];
      addEntry((int)zpos, zArray);

      if (millis() > 3000) {
        newAverages = (getAverage(xArray) + getAverage(yArray) + getAverage(zArray)) / 3;
        if (oldAverages > 0) {
          delta = abs(oldAverages - newAverages);
          //println("Delta = " + delta);
          if (delta > 10) {
            readyToClear = true;
          }
        }
      }
      firstPass = false;

      if (!firstIdleFlag) {
        if (potsDiff < 3) {
          firstIdleFlag = true;
          idleWindowStart = millis();
        } 
        else {
          //print("Active!! ,");
        }
      } 
      else if (firstIdleFlag) {
        if (potsDiff < 4 && delta < 2) {
          // To stop the waterfall effect, let's do this
          if (abs(millis() - idleWindowStart) >= (TIME_WINDOW-1000)) {
            stopWaterfalling = true; // stop the waterfall effect so we don't get an error
          }


          if (abs(millis() - idleWindowStart) >= TIME_WINDOW) {
            if (!idleOn) {
              idleOn = true;
            }
          }
        } 
        else {
          if (idleOn) {
            is = null;
            idleOn = false;
            background(colorList.getLightest().toARGB());
          }
          firstIdleFlag = false;
          stopWaterfalling = false;
        }
      }
    }
  }
}

int getAverage(int valueArray[]) {
  int i;
  int sum = 0;
  int average;
  for (i=0;i<valueArray.length;i++) {
    sum += valueArray[i];
  }
  average = sum/valueArray.length;
  return average;
}

void addEntry(int value, int valueArray[]) {
  int i;
  for (i=0;i<valueArray.length-1;i++) {
    valueArray[i]=valueArray[i+1];
  }
  valueArray[valueArray.length-1] = value;
}

// we are done, clean up Ess

public void stop() {
  Ess.stop();
  super.stop();
}

