class Particle {

  float x, y, px, py, r, alph, angle;
  float xmove, ymove;
  color fillCol, strokeCol;



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


  void drawShape() {
    fill(fillCol, alph/2);
    pushMatrix();
    translate(x, y, 0);
    rotate(angle);
    box(r*random(.5, 1.5));
    popMatrix();
    noStroke();

    ellipse(random(x-15, x+15), random(y-15, y+15), r*random(1,2), r*random(1,2));
    stroke(strokeCol, alph/2);
    noFill();
    strokeWeight(random(1, 1.5));
    ellipse(x, y, r, r);
    //sphere(r);
  }



  void update() {
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

