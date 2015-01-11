import processing.core.*; 
import processing.xml.*; 

import processing.opengl.*; 
import javax.media.opengl.*; 
import processing.serial.*; 
import toxi.color.*; 
import toxi.geom.*; 
import toxi.util.datatypes.*; 
import krister.Ess.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class SynthASketch_FUSE extends PApplet {










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

public void setup() {
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

public void glSettings() {
  //  PGraphicsOpenGL pgl = (PGraphicsOpenGL) g;
  //  GL gl = pgl.beginGL(); 
  //  gl.glClearColor(0, 0, 0, 0);
  //  gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
  //  pgl.endGL();
}

public void fadeStrobe() {
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

public void fade() {
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

public void setUpColors() {
  colorTheme = new ColorTheme("synthasktech");
  colorTheme.addRange("soft ivory", 0.5f);
  colorTheme.addRange("intense goldenrod", 0.25f);
  colorTheme.addRange("warm saddlebrown", 0.15f);
  colorTheme.addRange("fresh teal", 0.05f);
  colorTheme.addRange("bright yellow", 0.05f);

  // now add another random hue which is using only bright shades
  colorTheme.addRange(ColorRange.BRIGHT, TColor.newRandom(), random(0.02f, 0.05f));

  // use the TColortheme to create a list of 160 colors
  colorList = colorTheme.getColors(160);

  background(colorList.getLightest().toARGB());
}


public void draw() {

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


public void showWaterFallEffect() {

  // this is the main water-fall like effect

    for (int i = 0; i < particles.length; i++) {
    particles[i].y+=(i+1)*2; // drip down
    particles[i].x = particles[i].x + random(-3, 3); // brownian motion
    particles[i].r/=1.1f; //reduce the radius
    particles[i].alph = particles[i].alph - .15f*i; //reduce the alpha
    if (particles[i].alph > 0)
    {
      particles[i].drawShape();
    } 
    else {
      particles = (Particle[])shorten(particles); // clear invisible particles
    }
  }
}

public void keyPressed() {
  if (key==' ') {
    readyToClear = true;
  }
}


public void setUpSineWave() {
  // create a new AudioStream
  myStream=new AudioStream();
  myStream.smoothPan = true;
  // our wave
  myWave=new SineWave(960,.75f);

  // start
  myFadeOut = new FadeOut();  // Create amplitude ramp
  myFadeIn = new FadeIn(); // Create amplitude ramp
  myStream.start();
}

public void audioStreamWrite(AudioStream theStream) {
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

public void serialEvent(Serial port) {
  float xpos, ypos, zpos, potsDiff;
  int oldAverages = 0, newAverages = 0, delta = 0;

  String serialString = port.readStringUntil(linefeed);
  if (serialString != null) {
    serialString = trim(serialString);
    // split the string at the commas
    // convert the sections into integers
    float sensors[] = PApplet.parseFloat(split(serialString, ","));
    // If we've received all the sensor strings, use them
    if (sensors.length>=5) {
      oldX = xPot;
      oldY = yPot;
      xPot = (float)map(sensors[0], 1023, 0, 0, width);
      yPot = (float)map(sensors[1], 0, 1023, 0, height);
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
          if (delta > 7) {
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

public int getAverage(int valueArray[]) {
  int i;
  int sum = 0;
  int average;
  for (i=0;i<valueArray.length;i++) {
    sum += valueArray[i];
  }
  average = sum/valueArray.length;
  return average;
}

public void addEntry(int value, int valueArray[]) {
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
    myEnv[1] = new EPoint(0.25f, 1); // Attack
    myEnv[2] = new EPoint(2, 0); // Release
    myEnvelope = new Envelope(myEnv); // Bind an Envelope to the breakpoint function
    playSounds();
  }

  public void playSounds() {
    int playSound = PApplet.parseInt(random(1,numChannels/2));
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
        mySound[which].sampleRate(rates[PApplet.parseInt(random(0, currRateIndex))], false);
        mySound[which].play();
        myEnvelope.filter(mySound[which]); // Apply envelop
      }
      playSound--;
    }
  }

  public void audioChannelDone(AudioChannel ch) {
    ch.destroy();
  }

  public void audioOutputPan(AudioOutput c) {
    c.panTo(-c.pan, 4000); // Reverse pan direction
  }
}

class IdleScreen {
 
  
 IdleScreen() {
    textFont(loadFont("04b03-48.vlw"), 48);
    fill(255, 255);
    rect(0, 0, 800, 600);
    textAlign(CENTER);
    fill(0);
    text("SYNTH-A-SKETCH", 800/2, 600/2-25, 72);
    textSize(16);
    fill(50);
    text("PICK UP THE CONTROLLER. TURN KNOBS TO PLAY. SHAKE TO CLEAR. REPEAT. ENJOY.", 800/2, 600/2+25, 48);
 } 
  
}
class Particle {

  float x, y, px, py, r, alph, angle;
  float xmove, ymove;
  int fillCol, strokeCol;



  Particle(float xpos, float ypos, float pXpos, float pYpos, ColorList colorList) {
    this.x = xpos;
    this.y = ypos;
    this.px = pXpos;
    this.py = pYpos;
    r = (int)(abs(x-px) + abs(y-py))/2; // get the velocity
    r = constrain(r, 2, 150);
    //if (r>400) return;
    angle = map(x, 0, width, 0, TWO_PI);
    
    float numCols = colorList.size();
    fillCol = colorList.get((int) random(numCols)).toARGB();
    strokeCol = colorList.get((int) random(numCols)).toARGB();
    //fillCol = color(random(255), random(255), random(255));
    //strokeCol = color(random(255), random(255), random(255));
    alph = random(255);
    xmove = random(10)-5;
    ymove = random(10)-5;
  }


  public void drawShape() {
    fill(fillCol, alph/2);
    pushMatrix();
    translate(x, y, 0);
    rotate(angle);
    box(r*random(.5f, 1.5f));
    popMatrix();
    noStroke();

    ellipse(random(x-15, x+15), random(y-15, y+15), r*random(1,2), r*random(1,2));
    stroke(strokeCol, alph/2);
    noFill();
    strokeWeight(random(1, 1.5f));
    ellipse(x, y, r, r);
    //sphere(r);
  }



  public void update() {
    if (r>400) return;
    x+=xmove;
    y+=ymove;
    if (x > (width+r)) { 
      x = 0 - r;
    }
    if (x < (0-r)) { 
      x = width+r;
    }
    if (y > (height+r)) { 
      y = 0 - r;
    }
    if (y < (0-r)) { 
      y = height+r;
    }
  
    drawShape();
  }
}

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


  public void audioStreamWrite(AudioStream theStream) {
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
  
  public void setSynthVars(float mx, float my) {
    this.x = mx;
    this.y = my;
  }
}


class Word {
  
 float x,y, angle;
 String type = ""; 
 
 Word(String t, float xpos, float ypos) {
   x = xpos;
   y = ypos;
   type = t;
   angle = map(x, 0, 800, 0, TWO_PI);
   textFont(loadFont("04b03-48.vlw"), random(10, 24));
 }
 
 Word(String t) {
   type = t;
   textFont(loadFont("04b03-48.vlw"), 30);
 }
 
 public void drawWordNormally(ColorList colorList) {
   pushMatrix();
   translate(250, 600/2, 250);
   fill(colorList.getDarkest().toARGB());
   text(type);
   popMatrix();
 }
 
 public void drawWord() {
   fill(0,random(200));
   pushMatrix();
   translate(x, y);
   rotateX(angle + random(3));
   rotateY(angle + random(1));
   text(type, 0, 0);
   popMatrix();
   
 }
}
  static public void main(String args[]) {
    PApplet.main(new String[] { "--present", "--bgcolor=#666666", "--hide-stop", "SynthASketch_FUSE" });
  }
}
