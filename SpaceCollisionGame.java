import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class SpaceCollisionGame extends Applet implements Runnable, KeyListener{
    
    Thread thread;
    long startTime, endTime, framePeriod;
    Dimension dim;
    Image img;
    Graphics g;
    Ship ship;
    boolean paused;
    Shot[] shots;
    int numShots;
    boolean shooting;
    Asteroid[] asteroids;
    Obstacle[] obstacles;
    int numAsteroids;
    int numObstacles;
    double astRadius,minAstVel,maxAstVel;
    int astNumHits,astNumSplit;
    int level;
    int shotsHit;
    int totalShots;
    int lives;
    
    
    public void init()
    {
        resize(500,500);
        shots=new Shot[41];
        numAsteroids=0;
        numObstacles=0;
        level=0;
        astRadius=60; //values used to create the asteroids
        minAstVel=.5; 
        maxAstVel=5; 
        astNumHits=3; 
        astNumSplit=2; 
        lives=5;
        endTime=0; 
        startTime=0; 
        framePeriod=25; 
        addKeyListener(this); //tell it to listen for KeyEvents
        dim=getSize(); 
        img=createImage(dim.width, dim.height); 
        g=img.getGraphics(); 
        thread=new Thread(this); 
        thread.start();   
    }
    
    
    public void setUpNextLevel()
    {
        level++;
        ship=new Ship(250,250,0,.35,.98,.1,12); 
        numShots=0;
        paused=false;
        shooting=false;
        asteroids=new Asteroid[level * (int)Math.pow(astNumSplit,astNumHits-1)+1]; 
        obstacles=new Obstacle[level*2];
        numObstacles=level*2;
        numAsteroids=level;
        
        for(int i=0;i<numAsteroids;i++) 
            asteroids[i]=new Asteroid(Math.random()*dim.width,Math.random()*dim.height,astRadius,minAstVel,maxAstVel,astNumHits,astNumSplit); 
        for(int i=0;i<numObstacles;i++)
            obstacles[i]=new Obstacle(Math.random()*dim.width,(Math.random()*dim.height)+40,25);
    }
    
    
    public void paint(Graphics gfx)
    {
        g.setColor(Color.black); // clear the screen with black
        g.fillRect(0,0,500,500); 
        for(int i=0;i<numShots;i++) //draw all the shots on the screen
            shots[i].draw(g); 
        for(int i=0;i<numAsteroids;i++) 
            asteroids[i].draw(g); 
        for(int i=0;i<numObstacles;i++)
            obstacles[i].draw(g);
        ship.draw(g); //draw the ship
        g.setColor(Color.cyan); 
        g.drawString("Level " + level,20,20); 
        g.drawString("Lives : " + lives,20,35);
        g.drawString("Accuracy:",400,20);
        g.drawString("Ship Collision",200,20);
        g.drawString(percentHit(),400,35);
            g.drawString("0%", WIDTH, WIDTH);
        if(paused)
        {
            g.drawString("Ship Collision",200,200);
            g.drawString("press enter to unpause", 200, 220);
        }
        gfx.drawImage(img,0,0,this); 
    }
    
    
    public void update(Graphics gfx)
    {
        paint(gfx);
    }
    
    
    public void run()
    {
        for(;;)
        {
            startTime = System.currentTimeMillis();
            if(numAsteroids<=0)
                setUpNextLevel();
            if(lives<=0)
            {
                level=0;
                lives=5;
                totalShots=0;
                shotsHit=0;
                setUpNextLevel();  
            }
            if(!paused)
            {
                ship.move(dim.width, dim.height); //move the ship
                for(int i=0;i<numShots;i++)
                {
                    shots[i].move(dim.width, dim.height);
                    if(shots[i].getLifeLeft()<=0)
                    {
                        deleteShot(i);
                        i--;
                    }
                }
                updateAsteroids();
                updateObstacles();
                if(shooting && ship.canShoot())
                {
                    shots[numShots]=ship.shoot();
                    numShots++;
                    totalShots++;
                } 
            }
            repaint();
            try{
                endTime=System.currentTimeMillis();
                if(framePeriod-(endTime-startTime)>0)
                    Thread.sleep(framePeriod-(endTime-startTime));
            }catch(InterruptedException e){
            }
        }
    }
    
    
    private void deleteShot(int index)
    {
        numShots--;
        for(int i=index;i<numShots;i++)
            shots[i]=shots[i+1];
        shots[numShots]=null;
    }
    
    
    private void deleteAsteroid(int index)
    {
        numAsteroids--;
        for(int i=index;i<numAsteroids;i++)
            asteroids[i]=asteroids[i+1];
        asteroids[numAsteroids]=null; 
    }
    
    
    private void addAsteroid(Asteroid ast)
    { 
        asteroids[numAsteroids]=ast; 
        numAsteroids++; 
    } 
    
    
    private void updateObstacles()
    {
        for(int i=0;i<numObstacles;i++)
        {
            if(obstacles[i].shipCollision(ship))
            {
                lives--;
                level--; //restart this level
                numAsteroids=0; 
                return;
            }
        }
    }
    
    
    private void updateAsteroids()
    {
        for(int i=0;i<numAsteroids;i++)
        {
            asteroids[i].move(dim.width,dim.height);
            if(asteroids[i].shipCollision(ship))
            { 
                lives--;
                level--; //restart this level
                numAsteroids=0; 
                return; 
            }
            for(int j=0;j<numShots;j++)
            { 
                if(asteroids[i].shotCollision(shots[j]))
                {
                    deleteShot(j); 
                    shotsHit++;
                    if(asteroids[i].getHitsLeft()>1)
                    {
                        for(int k=0;k<asteroids[i].getNumSplit();k++)
                            addAsteroid(asteroids[i].createSplitAsteroid(minAstVel,maxAstVel));
                    }
                    deleteAsteroid(i);
                    j=numShots;
                    i--;
                } 
            }
        } 
    } 
    
    
    public void keyPressed(KeyEvent e)
    {
        if(e.getKeyCode()==KeyEvent.VK_ENTER){
            if(!ship.isActive() && !paused)
                ship.setActive(true);
            else{
                paused= !paused;
                if(paused)
                    ship.setActive(false);
                else
                    ship.setActive(true);
            }
        }else if(paused || !ship.isActive())
            return;
        else if(e.getKeyCode()==KeyEvent.VK_UP)
            ship.setAccelerating(true);
        else if(e.getKeyCode()==KeyEvent.VK_LEFT)
            ship.setTurningLeft(true);
        else if(e.getKeyCode()==KeyEvent.VK_RIGHT)
            ship.setTurningRight(true);  
        else if(e.getKeyCode()==KeyEvent.VK_SPACE)
            shooting=true;
    }
    
    
    public void keyReleased(KeyEvent e)
    {
        if(e.getKeyCode()==KeyEvent.VK_UP)
            ship.setAccelerating(false);
        else if(e.getKeyCode()==KeyEvent.VK_LEFT)
            ship.setTurningLeft(false);
        else if(e.getKeyCode()==KeyEvent.VK_RIGHT)
            ship.setTurningRight(false);
        else if(e.getKeyCode()==KeyEvent.VK_SPACE)
            shooting=false;
            
    }
    
    
    public String percentHit()
    {
        double percentHit =(double)shotsHit/(double)totalShots;
        percentHit*=100;
        percentHit=Math.floor(percentHit * 100) / 100;
        String percent;
        if(totalShots==0)
            percent = "100%";
        else
            percent = percentHit+"%";
        return percent;
    }
    
    
    public void keyTyped(KeyEvent e){
        
    }
}
