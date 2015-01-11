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
