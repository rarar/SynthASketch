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
 
 void drawWordNormally(ColorList colorList) {
   pushMatrix();
   translate(250, 600/2, 250);
   fill(colorList.getDarkest().toARGB());
   text(type);
   popMatrix();
 }
 
 void drawWord() {
   fill(0,random(200));
   pushMatrix();
   translate(x, y);
   rotateX(angle + random(3));
   rotateY(angle + random(1));
   text(type, 0, 0);
   popMatrix();
   
 }
}
